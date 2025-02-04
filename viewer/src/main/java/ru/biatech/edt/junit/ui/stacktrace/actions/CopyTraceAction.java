/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package ru.biatech.edt.junit.ui.stacktrace.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.SelectionListenerAction;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.utils.ClipboardHelper;
import ru.biatech.edt.junit.ui.utils.StringUtilities;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

/**
 * Copies a test failure stack trace to the clipboard.
 */
public class CopyTraceAction extends SelectionListenerAction {
  private ITestElement testElement;
  private ErrorInfo errorInfo;

  public CopyTraceAction() {
    super(UIMessages.CopyTrace_action_label);
    setEnabled(false);
    setImageDescriptor(ImageProvider.getSharedImage(ISharedImages.IMG_TOOL_COPY));
  }

  /*
   * @see IAction#run()
   */
  @Override
  public void run() {
    if (errorInfo == null && testElement == null) {
      return;
    }

    String trace;
    if (errorInfo != null) {
      trace = errorInfo.getTrace();
    } else {
      trace = StringUtilities.getTrace(testElement);
    }

    String source = null;
    if (!StringUtilities.isNullOrEmpty(trace)) {
      source = trace;
    }
    if (StringUtilities.isNullOrEmpty(source)) {
      return;
    }

    try {
      ClipboardHelper.pasteToClipboard(source);
    } catch (SWTError e) {
      if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) throw e;
      if (MessageDialog.openQuestion(TestViewerPlugin.ui().getShell(), UIMessages.CopyTraceAction_problem, UIMessages.CopyTraceAction_clipboard_busy)) {
        run();
      }
    }
  }

  public void handleTestSelected(ErrorInfo error) {
    testElement = null;
    errorInfo = error;
    setEnabled(errorInfo != null);
  }

  public void handleTestSelected(ITestElement test) {
    testElement = test;
    errorInfo = null;
    setEnabled(testElement != null && testElement.getErrorsList().findAny().isPresent());
  }
}
