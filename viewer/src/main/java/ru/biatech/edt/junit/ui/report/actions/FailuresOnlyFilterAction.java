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
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

public class FailuresOnlyFilterAction extends SettingsChangeAction {
  public FailuresOnlyFilterAction(TestRunnerViewPart.ReportSettings settings) {
    super(settings, JUnitMessages.TestRunnerViewPart_show_failures_only, IAction.AS_CHECK_BOX);
    setToolTipText(JUnitMessages.TestRunnerViewPart_show_failures_only);
    setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor(ImageProvider.FAILURES_ICON));
  }

  @Override
  public void run() {
    settings.setShowFailuresOnly(isChecked());
  }
}
