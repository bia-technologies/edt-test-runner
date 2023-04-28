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
package ru.biatech.edt.junit.ui.report.actions;

import org.eclipse.jface.action.Action;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;

/**
 * Команда навигации по дереву тестов, выполняет переход к предыдущему упавшему тесту
 */
public class ShowPreviousFailureAction extends Action {

  private final TestRunnerViewPart fPart;

  public ShowPreviousFailureAction(TestRunnerViewPart part) {
    super(JUnitMessages.ShowPreviousFailureAction_label);
    setDisabledImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("dlcl16/select_prev.png")); //$NON-NLS-1$
    setHoverImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/select_prev.png")); //$NON-NLS-1$
    setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/select_prev.png")); //$NON-NLS-1$
    setToolTipText(JUnitMessages.ShowPreviousFailureAction_tooltip);
    fPart = part;
  }

  @Override
  public void run() {
    fPart.selectPreviousFailure();
  }
}
