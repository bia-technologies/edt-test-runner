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
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;

public class ToggleSortingAction extends SettingsChangeAction {
  private final TestRunnerViewPart.SortingCriterion actionSortingCriterion;

  public ToggleSortingAction(TestRunnerViewPart.ReportSettings settings, TestRunnerViewPart.SortingCriterion sortingCriterion) {
    super(settings, "", IAction.AS_RADIO_BUTTON); //$NON-NLS-1$
    switch (sortingCriterion) {
      case SORT_BY_NAME:
        setText(UIMessages.TestRunnerViewPart_toggle_name_label);
        break;
      case SORT_BY_EXECUTION_ORDER:
        setText(UIMessages.TestRunnerViewPart_toggle_execution_order_label);
        break;
      case SORT_BY_EXECUTION_TIME:
        setText(UIMessages.TestRunnerViewPart_toggle_execution_time_label);
        break;
      default:
        break;
    }
    actionSortingCriterion = sortingCriterion;
  }

  @Override
  public void run() {
    if (isChecked()) {
      settings.setSortingCriterion(actionSortingCriterion);
    }
  }

  @Override
  public void update() {
    setChecked(settings.getSortingCriterion() == actionSortingCriterion);
  }
}
