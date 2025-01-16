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

import com.google.common.base.Strings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.ITraceable;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.ui.utils.ClipboardHelper;
import ru.biatech.edt.junit.ui.utils.StringUtilities;

/**
 * Copies a test failure stack trace to the clipboard.
 */
public class CopyTraceAction extends SelectionListenerAction {
  private ITraceable testElement;

  public CopyTraceAction() {
    super(UIMessages.CopyTrace_action_label);
    setEnabled(false);
    IWorkbench workbench = PlatformUI.getWorkbench();
    setImageDescriptor(ImageProvider.getSharedImage(ISharedImages.IMG_TOOL_COPY));
  }

  /*
   * @see IAction#run()
   */
  @Override
  public void run() {
    if (testElement == null) {
      return;
    }

    String trace = StringUtilities.getTrace(testElement);
    String source = null;
    if (!Strings.isNullOrEmpty(trace)) {
      source = trace;
    } else if (testElement != null) {
      source = testElement.getTestName();
    }
    if (Strings.isNullOrEmpty(source)) {
      return;
    }

    try {
      ClipboardHelper.pasteToClipboard(source);
    } catch (SWTError e) {
      if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
        throw e;
      if (MessageDialog.openQuestion(TestViewerPlugin.ui().getShell(), UIMessages.CopyTraceAction_problem, UIMessages.CopyTraceAction_clipboard_busy))
        run();
    }
  }

  public void handleTestSelected(ITraceable test) {
    testElement = test;
    setEnabled(testElement != null);
  }
}
