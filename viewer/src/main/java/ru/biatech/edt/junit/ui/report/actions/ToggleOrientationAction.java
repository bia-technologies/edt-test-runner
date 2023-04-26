/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.report.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.IJUnitHelpContextIds;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;

public class ToggleOrientationAction extends SettingsChangeAction {
  private final int actionOrientation;

  public ToggleOrientationAction(TestRunnerViewPart.ReportSettings settings, int orientation) {
    super(settings, "", IAction.AS_RADIO_BUTTON); //$NON-NLS-1$
    switch (orientation) {
      case TestRunnerViewPart.VIEW_ORIENTATION_HORIZONTAL:
        setText(JUnitMessages.TestRunnerViewPart_toggle_horizontal_label);
        setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/th_horizontal.png")); //$NON-NLS-1$
        break;
      case TestRunnerViewPart.VIEW_ORIENTATION_VERTICAL:
        setText(JUnitMessages.TestRunnerViewPart_toggle_vertical_label);
        setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/th_vertical.png")); //$NON-NLS-1$
        break;
      case TestRunnerViewPart.VIEW_ORIENTATION_AUTOMATIC:
        setText(JUnitMessages.TestRunnerViewPart_toggle_automatic_label);
        setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/th_automatic.png")); //$NON-NLS-1$
        break;
      default:
        break;
    }
    actionOrientation = orientation;
    PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJUnitHelpContextIds.RESULTS_VIEW_TOGGLE_ORIENTATION_ACTION);
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
