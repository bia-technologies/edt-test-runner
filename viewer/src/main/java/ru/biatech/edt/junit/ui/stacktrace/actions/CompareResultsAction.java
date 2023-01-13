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
package ru.biatech.edt.junit.ui.stacktrace.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.dialogs.CompareResultDialog;

/**
 * Action to enable/disable stack trace filtering.
 */
public class CompareResultsAction extends Action {

  private final Composite view;
  private CompareResultDialog fOpenDialog;
  private TestElement testElement;

  public CompareResultsAction(Composite view) {
    super(JUnitMessages.CompareResultsAction_label);
    setEnabled(false);
    setDescription(JUnitMessages.CompareResultsAction_description);
    setToolTipText(JUnitMessages.CompareResultsAction_tooltip);

    setDisabledImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("dlcl16/compare.png"));  //$NON-NLS-1$
    setHoverImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/compare.png"));  //$NON-NLS-1$
    setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/compare.png"));  //$NON-NLS-1$
    this.view = view;
  }

  /*
   * @see Action#actionPerformed
   */
  @Override
  public void run() {
    if (fOpenDialog != null) {
      fOpenDialog.setInput(testElement);
      fOpenDialog.getShell().setActive();

    } else {
      fOpenDialog = new CompareResultDialog(view.getShell(), testElement);
      fOpenDialog.create();
      fOpenDialog.getShell().addDisposeListener(e -> fOpenDialog = null);
      fOpenDialog.setBlockOnOpen(false);
      fOpenDialog.open();
    }
  }

  public void handleTestSelected(TestElement test) {
    testElement = test;
    boolean enableCompare = test != null && test.isComparisonFailure();
    setEnabled(enableCompare);
    if (enableCompare && fOpenDialog != null) {
      fOpenDialog.setInput(test);
    }
  }
}
