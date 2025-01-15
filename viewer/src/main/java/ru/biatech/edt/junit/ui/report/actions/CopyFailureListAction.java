/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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
package ru.biatech.edt.junit.ui.report.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.utils.ClipboardHelper;
import ru.biatech.edt.junit.utils.StringUtilities;

/**
 * Copies the names of the methods that failed and their traces to the clipboard.
 */
public class CopyFailureListAction extends Action {

  private final TestRunnerViewPart fRunner;

  public CopyFailureListAction(TestRunnerViewPart runner) {
    super(UIMessages.CopyFailureList_action_label);
    fRunner = runner;
    IWorkbench workbench = PlatformUI.getWorkbench();
    setImageDescriptor(ImageProvider.getSharedImage(ISharedImages.IMG_TOOL_COPY));
  }

  /*
   * @see IAction#run()
   */
  @Override
  public void run() {
    try {
      ClipboardHelper.pasteToClipboard(getAllFailureTraces());
    } catch (SWTError e) {
      if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
        throw e;
      }
      if (MessageDialog.openQuestion(TestViewerPlugin.ui().getActiveWorkbenchShell(), UIMessages.CopyFailureList_problem, UIMessages.CopyFailureList_clipboard_busy)) {
        run();
      }
    }
  }

  public String getAllFailureTraces() {
    var buf = new StringBuilder();
    var failures = fRunner.getAllFailures();
    var lineSeparator = System.lineSeparator();

    for (var failure : failures) {
      buf.append(failure.getTestName()).append(lineSeparator);
      buf.append(StringUtilities.getTrace(failure));
    }
    return buf.toString();
  }
}
