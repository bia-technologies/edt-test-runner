/*******************************************************************************
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.ui.editor.UIHelper;

public class BslSourceDisplay {

  public static final BslSourceDisplay INSTANCE = new BslSourceDisplay();

  private final BslModuleLocator moduleLocator = new BslModuleLocator();

  public void displayBslSource(Module module, IWorkbenchPage page, int lineNumber) {
    SourceDisplayJob sourceDisplay = new SourceDisplayJob(module, page, lineNumber);
    Job.getJobManager().cancel(sourceDisplay);
    sourceDisplay.schedule();
  }

  public void displayBslSource(IStacktraceFrame stackFrame, IWorkbenchPage page, boolean forceModuleSelect) {
    var module = this.getModule(stackFrame, forceModuleSelect);
      if (module != null) {
        displayBslSource(module, page, stackFrame.getLineNumber() - 1);
      }
  }

  public void displayBslSource(MethodReference reference, IWorkbenchPage page, boolean forceModuleSelect) {
    var method = reference.getMethod();

    int lineNumber = -1;
    if (method != null) {
      var node = NodeModelUtils.findActualNodeFor(method);
      lineNumber = node.getStartLine();
    }
    displayBslSource(reference.getModule(), page, lineNumber);
  }

  private Module getModule(IStacktraceFrame stackFrame, boolean forceModuleSelect) {
    return forceModuleSelect ? this.select(stackFrame, false) : this.get(stackFrame);
  }

  private static class SourceDisplayJob extends UIJob {
    private final IWorkbenchPage page;
    private final int line;
    private final Module module;

    private SourceDisplayJob(Module module, IWorkbenchPage page, int line) {
      super("Module Source Display");
      this.setSystem(true);
      this.setPriority(10);
      this.page = page;
      this.line = line;
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
        UIHelper.openModuleEditor(this.module, this.line);
      }

      return Status.OK_STATUS;
    }
  }

  private Module get(IStacktraceFrame stackFrame) {
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

  private Module select(IStacktraceFrame stackFrame, boolean autoModuleSelect) {
    Module module = null;
    var modules = this.moduleLocator.getModules(stackFrame);
    if (!modules.isEmpty()) {
      if (modules.size() == 1 && autoModuleSelect) {
        module = modules.get(0);
      } else {
        module = Dialogs.selectModule(modules);
      }

      if (module != null) {
        this.setProjectName(stackFrame, module);
        stackFrame.setModule(module);
      }
    }

    return module;
  }

  private void setProjectName(IStacktraceFrame stackFrame, Module foundedModule) {
    IV8Project v8Project = VendorServices.getProjectManager().getProject(foundedModule);
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
    return Projects.getProject(stackFrame.getProjectName());
  }
}
