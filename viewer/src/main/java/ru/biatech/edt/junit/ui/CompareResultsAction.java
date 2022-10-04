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
package ru.biatech.edt.junit.ui;


import org.eclipse.jface.action.Action;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.ui.dialogs.CompareResultDialog;
import ru.biatech.edt.junit.ui.view.FailureTrace;

/**
 * Action to enable/disable stack trace filtering.
 */
public class CompareResultsAction extends Action {

  private final FailureTrace fView;
  private CompareResultDialog fOpenDialog;

  public CompareResultsAction(FailureTrace view) {
    super(JUnitMessages.CompareResultsAction_label);
    setDescription(JUnitMessages.CompareResultsAction_description);
    setToolTipText(JUnitMessages.CompareResultsAction_tooltip);

    setDisabledImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("dlcl16/compare.png"));  //$NON-NLS-1$
    setHoverImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/compare.png"));  //$NON-NLS-1$
    setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/compare.png"));  //$NON-NLS-1$
    fView = view;
  }

  /*
   * @see Action#actionPerformed
   */
  @Override
  public void run() {
    TestElement failedTest = fView.getFailedTest();
    if (fOpenDialog != null) {
      fOpenDialog.setInput(failedTest);
      fOpenDialog.getShell().setActive();

    } else {
      fOpenDialog = new CompareResultDialog(fView.getShell(), failedTest);
      fOpenDialog.create();
      fOpenDialog.getShell().addDisposeListener(e -> fOpenDialog = null);
      fOpenDialog.setBlockOnOpen(false);
      fOpenDialog.open();
    }
  }

  public void updateOpenDialog(TestElement failedTest) {
    if (fOpenDialog != null) {
      fOpenDialog.setInput(failedTest);
    }
  }
}
