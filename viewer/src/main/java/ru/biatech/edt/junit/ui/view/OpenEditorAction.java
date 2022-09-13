/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui.view;

import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.ui.util.OpenHelper;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestResolver;
import ru.biatech.edt.junit.kinds.TestKindRegistry;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.v8utils.Resolver;
import ru.biatech.edt.junit.v8utils.Services;

/**
 * Abstract Action for opening a Java editor.
 */
public abstract class OpenEditorAction extends Action {

  protected String fClassName;
  protected TestRunnerViewPart fTestRunner;

  protected OpenEditorAction(TestRunnerViewPart testRunner, String testClassName) {
    this(testRunner, testClassName, true);
  }

  public OpenEditorAction(TestRunnerViewPart testRunner, String className, boolean activate) {
    super(JUnitMessages.OpenEditorAction_action_label);
    fClassName = className;
    fTestRunner = testRunner;
    setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/goto_input.png")); //$NON-NLS-1$
  }

  /*
   * @see IAction#run()
   */
  @Override
  public void run() {

    IEditorPart editor;
    var position = getPosition();

    var element = findType(getLaunchedProject(), position);
    if (element == null) {
      MessageDialog.openError(getShell(), JUnitMessages.OpenEditorAction_error_cannotopen_title, JUnitMessages.OpenEditorAction_error_cannotopen_message);
      return;
    }

    TextSelection selection = getSelection(element, position);
    if (selection == null) {
      editor = new OpenHelper().openEditor(element);
    } else {
      URI uri = Services.getResourceLookup().getPlatformResourceUri(element);
      editor = new OpenHelper().openEditor(uri, selection);
    }

    if (!(editor instanceof ITextEditor)) {
      fTestRunner.registerInfoMessage(JUnitMessages.OpenEditorAction_message_cannotopen);
      return;
    }
  }

  protected Shell getShell() {
    return fTestRunner.getSite().getShell();
  }

  /**
   * @return the Java project, or <code>null</code>
   */
  protected IV8Project getLaunchedProject() {
    return fTestRunner.getLaunchedProject();
  }

  protected final MdObject findType(final IV8Project project, ITestResolver.MethodPositionInfo position) {
    return Resolver.findModule(project, position.getOwnerClass(), position.getOwnerName());
  }

  protected TextSelection getSelection(MdObject moduleOwner, ITestResolver.MethodPositionInfo position) {
    return null;
  }

  protected abstract ITestResolver.MethodPositionInfo getPosition();

  protected ITestResolver getResolver(IV8Project project) {
    var kind = TestKindRegistry.getContainerTestKind(project);
    if (kind != null) {
      return kind.getResolver();
    } else {
      return null;
    }
  }
}
