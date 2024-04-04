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
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;

/**
 * Команда навигации по дереву тестов, выполняет переход к следующему упавшему тесту
 */
public class ShowNextFailureAction extends Action {

  private final TestRunnerViewPart fPart;

  public ShowNextFailureAction(TestRunnerViewPart part) {
    super(JUnitMessages.ShowNextFailureAction_label);
    setToolTipText(JUnitMessages.ShowNextFailureAction_tooltip);

    ActionsSupport.setLocalImageDescriptors(this, "select_next.png"); //$NON-NLS-1$
    fPart = part;
  }

  @Override
  public void run() {
    fPart.selectNextFailure();
  }
}
