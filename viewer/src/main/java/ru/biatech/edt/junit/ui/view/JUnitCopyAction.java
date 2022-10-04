/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.ui.IJUnitHelpContextIds;
import ru.biatech.edt.junit.ui.JUnitMessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Copies a test failure stack trace to the clipboard.
 */
public class JUnitCopyAction extends SelectionListenerAction {
  private final FailureTrace fView;

  private final Clipboard fClipboard;

  private TestElement fTestElement;

  public JUnitCopyAction(FailureTrace view, Clipboard clipboard) {
    super(JUnitMessages.CopyTrace_action_label);
    Assert.isNotNull(clipboard);
    IWorkbench workbench = PlatformUI.getWorkbench();
    workbench.getHelpSystem().setHelp(this, IJUnitHelpContextIds.COPYTRACE_ACTION);
    setImageDescriptor(workbench.getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
    fView = view;
    fClipboard = clipboard;
  }

  /*
   * @see IAction#run()
   */
  @Override
  public void run() {
    String trace = fView.getTrace();
    String source = null;
    if (trace != null) {
      source = convertLineTerminators(trace);
    } else if (fTestElement != null) {
      source = fTestElement.getTestName();
    }
    if (source == null || source.length() == 0)
      return;

    TextTransfer plainTextTransfer = TextTransfer.getInstance();
    try {
      fClipboard.setContents(
              new String[]{convertLineTerminators(source)},
              new Transfer[]{plainTextTransfer});
    } catch (SWTError e) {
      if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
        throw e;
      if (MessageDialog.openQuestion(fView.getComposite().getShell(), JUnitMessages.CopyTraceAction_problem, JUnitMessages.CopyTraceAction_clipboard_busy))
        run();
    }
  }


  public void handleTestSelected(TestElement test) {
    fTestElement = test;
  }

  private String convertLineTerminators(String in) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    StringReader stringReader = new StringReader(in);
    BufferedReader bufferedReader = new BufferedReader(stringReader);
    String line;
    try {
      while ((line = bufferedReader.readLine()) != null) {
        printWriter.println(line);
      }
    } catch (IOException e) {
      return in; // return the trace unfiltered
    }
    return stringWriter.toString();
  }
}
