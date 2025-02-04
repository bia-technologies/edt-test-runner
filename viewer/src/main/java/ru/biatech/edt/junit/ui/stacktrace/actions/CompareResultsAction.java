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


import org.eclipse.jface.action.Action;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.model.report.Failure;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.dialogs.CompareResultDialog;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

/**
 * Action to enable/disable stack trace filtering.
 */
public class CompareResultsAction extends Action {

  private CompareResultDialog fOpenDialog;
  private Failure failure;

  public CompareResultsAction() {
    super(UIMessages.CompareResultsAction_label);
    setEnabled(false);
    setDescription(UIMessages.CompareResultsAction_description);
    setToolTipText(UIMessages.CompareResultsAction_tooltip);

    setImageDescriptor(ImageProvider.getImageDescriptor(ImageProvider.ACTION_COMPARE));
    setDisabledImageDescriptor(ImageProvider.getImageDescriptor(ImageProvider.ACTION_COMPARE_DISABLED));
  }

  /*
   * @see Action#actionPerformed
   */
  @Override
  public void run() {
    if (fOpenDialog != null) {
      fOpenDialog.setInput(failure);
      fOpenDialog.getShell().setActive();

    } else {
      fOpenDialog = new CompareResultDialog(TestViewerPlugin.ui().getShell(), failure);
      fOpenDialog.create();
      fOpenDialog.getShell().addDisposeListener(e -> fOpenDialog = null);
      fOpenDialog.setBlockOnOpen(false);
      fOpenDialog.open();
    }
  }

  public void handleTestSelected(ErrorInfo error) {
    failure = error instanceof Failure ? (Failure) error : null;
    boolean enableCompare = failure != null && failure.isComparisonFailure();
    setEnabled(enableCompare);
    if (enableCompare && fOpenDialog != null) {
      fOpenDialog.setInput(failure);
    }
  }


}
