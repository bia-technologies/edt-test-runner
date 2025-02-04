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
import ru.biatech.edt.junit.ui.report.actions.SettingsChangeAction;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

public class IgnoredOnlyFilterAction extends SettingsChangeAction {
  public IgnoredOnlyFilterAction(ReportSettings settings) {
    super(settings, UIMessages.TestRunnerViewPart_show_ignored_only, IAction.AS_CHECK_BOX);
    setToolTipText(UIMessages.TestRunnerViewPart_show_ignored_only);
    setImageDescriptor(ImageProvider.getImageDescriptor(ImageProvider.TEST_IGNORED_ICON));
  }

  @Override
  public void run() {
    settings.setShowIgnoredOnly(isChecked());
  }

  @Override
  public void update() {
    setChecked(settings.isIgnoredOnly());
  }
}
