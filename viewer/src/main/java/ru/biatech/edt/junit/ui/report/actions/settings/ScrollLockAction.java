/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2022-2025 BIA-Technologies Limited Liability Company.
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
package ru.biatech.edt.junit.ui.report.actions.settings;


import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.ReportSettings;
import ru.biatech.edt.junit.ui.report.actions.ActionsSupport;
import ru.biatech.edt.junit.ui.report.actions.SettingsChangeAction;

/**
 * Toggles console auto-scroll
 */
public class ScrollLockAction extends SettingsChangeAction {

  public ScrollLockAction(ReportSettings settings) {
    super(settings, UIMessages.ScrollLockAction_action_label);
    setToolTipText(UIMessages.ScrollLockAction_action_tooltip);

    ActionsSupport.setLocalImageDescriptors(this, "lock.png"); //$NON-NLS-1$
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
    setChecked(!settings.isAutoScroll());
  }
}
