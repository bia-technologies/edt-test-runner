/*******************************************************************************
 * Copyright (c) 2023-2025 BIA-Technologies Limited Liability Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package ru.biatech.edt.junit.ui.report.actions.settings;

import org.eclipse.jface.action.IAction;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.ReportSettings;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.report.actions.ActionsSupport;
import ru.biatech.edt.junit.ui.report.actions.SettingsChangeAction;

public class ToggleOrientationAction extends SettingsChangeAction {
  private final int actionOrientation;

  public ToggleOrientationAction(ReportSettings settings, int orientation) {
    super(settings, "", IAction.AS_RADIO_BUTTON); //$NON-NLS-1$
    switch (orientation) {
      case TestRunnerViewPart.VIEW_ORIENTATION_HORIZONTAL:
        setText(UIMessages.TestRunnerViewPart_toggle_horizontal_label);
        ActionsSupport.setLocalImageDescriptors(this, "th_horizontal.png"); //$NON-NLS-1$
        break;
      case TestRunnerViewPart.VIEW_ORIENTATION_VERTICAL:
        setText(UIMessages.TestRunnerViewPart_toggle_vertical_label);
        ActionsSupport.setLocalImageDescriptors(this, "th_vertical.png"); //$NON-NLS-1$
        break;
      case TestRunnerViewPart.VIEW_ORIENTATION_AUTOMATIC:
        setText(UIMessages.TestRunnerViewPart_toggle_automatic_label);
        ActionsSupport.setLocalImageDescriptors(this, "th_automatic.png"); //$NON-NLS-1$
        break;
      default:
        break;
    }
    actionOrientation = orientation;
  }

  @Override
  public void run() {
    if (isChecked()) {
      settings.setOrientation(actionOrientation);
    }
  }

  @Override
  public void update() {
    setChecked(settings.getOrientation() == actionOrientation);
  }
}
