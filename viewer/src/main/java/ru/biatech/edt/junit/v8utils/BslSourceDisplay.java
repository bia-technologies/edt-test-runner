/*******************************************************************************
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package ru.biatech.edt.junit.v8utils;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceElement;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.dialogs.ModuleSelectionDialog;

import java.util.List;

public class BslSourceDisplay {

  public static final BslSourceDisplay INSTANCE = new BslSourceDisplay();

  private final BslModuleLocator moduleLocator = new BslModuleLocator();

  public void displayBslSource(Object element, IWorkbenchPage page, boolean forceModuleSelect) {
    if (element instanceof IStacktraceFrame) {
      IStacktraceFrame stackFrame = (IStacktraceFrame) element;
      Module module = this.getModule(stackFrame, forceModuleSelect);
      if (module != null) {
        this.displayModule(page, stackFrame, module);
      }
    }
  }

  protected IEditorPart openModuleEditor(Module module) {
    return Services.getOpenHelper().openEditor(EcoreUtil.getURI(module), null);
  }

  private void displayModule(IWorkbenchPage page, IStacktraceFrame stackFrame, Module module) {
    SourceDisplayJob sourceDisplay = new SourceDisplayJob(module, page, stackFrame);
    Job.getJobManager().cancel(sourceDisplay);
    sourceDisplay.schedule();
  }

  private void displayModule(Module module, IStacktraceFrame stackFrame) {
    IEditorPart editor = this.openModuleEditor(module);
    ITextEditor textEditor = null;
    if (editor instanceof ITextEditor) {
      textEditor = (ITextEditor) editor;
    } else if (editor != null) {
      textEditor = editor.getAdapter(ITextEditor.class);
    }

    if (textEditor != null) {
      this.positionEditor(textEditor, stackFrame);
    }
  }

  private IRegion getLineInformation(ITextEditor editor, int lineNumber) {
    IDocumentProvider provider = editor.getDocumentProvider();
    IEditorInput input = editor.getEditorInput();

    try {
      provider.connect(input);
    } catch (CoreException e) {
      return null;
    }

    try {
      IDocument document = provider.getDocument(input);
      if (document == null) {
        return null;
      }

      return document.getLineInformation(lineNumber);
    } catch (BadLocationException ignored) {
    } finally {
      provider.disconnect(input);
    }
    return null;
  }

  private void positionEditor(ITextEditor editor, IStacktraceFrame frame) {
    int lineNumber = frame.getLineNumber();
    --lineNumber;
    IRegion region = this.getLineInformation(editor, lineNumber);
    if (region != null) {
      editor.selectAndReveal(region.getOffset(), 0);
    }
  }

  private Module getModule(IStacktraceFrame stackFrame, boolean forceModuleSelect) {
    return forceModuleSelect ? this.select(stackFrame, false) : this.get(stackFrame);
  }

  private class SourceDisplayJob extends UIJob {
    private final IWorkbenchPage page;
    private final IStacktraceFrame stackFrame;
    private final Module module;

    private SourceDisplayJob(Module module, IWorkbenchPage page, IStacktraceFrame stackFrame) {
      super("Module Source Display");
      this.setSystem(true);
      this.setPriority(10);
      this.page = page;
      this.stackFrame = stackFrame;
      this.module = module;
    }

    @Override
    public boolean belongsTo(Object family) {
      if (family instanceof SourceDisplayJob) {
        SourceDisplayJob other = (SourceDisplayJob) family;
        return other.page.equals(this.page);
      } else {
        return false;
      }
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
      if (!monitor.isCanceled()) {
        BslSourceDisplay.this.displayModule(this.module, this.stackFrame);
      }

      return Status.OK_STATUS;
    }
  }

  public Module get(IStacktraceFrame stackFrame) {
    Module module = stackFrame.getModule();
    if (module == null) {
      module = this.moduleLocator.getModule(stackFrame.getSymlink(), this.getProject(stackFrame), stackFrame.isExtension());
      if (module == null) {
        module = this.select(stackFrame, true);
      } else {
        stackFrame.setModule(module);
      }
    }

    return module;
  }

  public Module select(IStacktraceFrame stackFrame, boolean autoModuleSelect) {
    Module module = null;
    List<Module> modules = this.moduleLocator.getModules(stackFrame);
    if (!modules.isEmpty()) {
      if (modules.size() == 1 && autoModuleSelect) {
        module = modules.get(0);
      } else {
        Shell shell = TestViewerPlugin.ui().getActiveWorkbenchShell();
        ModuleSelectionDialog dialog = new ModuleSelectionDialog(shell, modules);
        int result = dialog.open();
        if (result == 0) {
          module = (Module) dialog.getFirstResult();
        }
      }

      if (module != null) {
        this.setProjectName(stackFrame, module);
        stackFrame.setModule(module);
      }
    }

    return module;
  }

  private void setProjectName(IStacktraceFrame stackFrame, Module foundedModule) {
    IV8Project v8Project = Services.getProjectManager().getProject(foundedModule);
    if (v8Project != null) {
      stackFrame.setProjectName(v8Project.getProject().getName());
    }
  }

  private IV8Project getProject(IStacktraceFrame stackFrame) {
    if (stackFrame.getProjectName() == null) {
      for (IStacktraceElement e = stackFrame.getParent(); e != null; e = e.getParent()) {
        if (e.getProjectName() != null) {
          stackFrame.setProjectName(e.getProjectName());
        }
      }
    }
    return Resolver.getProject(stackFrame.getProjectName());
  }
}
