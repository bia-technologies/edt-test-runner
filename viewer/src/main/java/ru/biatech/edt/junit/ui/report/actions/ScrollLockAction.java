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


import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.IJUnitHelpContextIds;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;

/**
 * Toggles console auto-scroll
 */
public class ScrollLockAction extends SettingsChangeAction {

  public ScrollLockAction(TestRunnerViewPart.ReportSettings settings) {
    super(settings, JUnitMessages.ScrollLockAction_action_label);
    setToolTipText(JUnitMessages.ScrollLockAction_action_tooltip);
    setDisabledImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("dlcl16/lock.png")); //$NON-NLS-1$
    setHoverImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/lock.png")); //$NON-NLS-1$
    setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/lock.png")); //$NON-NLS-1$
    PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJUnitHelpContextIds.OUTPUT_SCROLL_LOCK_ACTION);
  }

  /**
   * @see org.eclipse.jface.action.IAction#run()
   */
  @Override
  public void run() {
    settings.setAutoScroll(!isChecked());
  }

  @Override
  public void update() {
    setChecked(settings.isAutoScroll());
  }
}
