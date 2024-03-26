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

import org.eclipse.jface.action.Action;
import ru.biatech.edt.junit.launcher.v8.RerunHelper;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;

public class RerunLastAction extends Action {
  private final TestRunnerViewPart testRunnerViewPart;

  public RerunLastAction(TestRunnerViewPart testRunnerViewPart) {
    this.testRunnerViewPart = testRunnerViewPart;
    setText(JUnitMessages.TestRunnerViewPart_rerunaction_label);
    setToolTipText(JUnitMessages.TestRunnerViewPart_rerunaction_tooltip);

    setEnabled(false);

    setActionDefinitionId(TestRunnerViewPart.RERUN_LAST_COMMAND);

    ActionsSupport.setLocalImageDescriptors(this, "rerun.png"); //$NON-NLS-1$
  }

  @Override
  public void run() {
    RerunHelper.rerun(testRunnerViewPart.getTestRunSession());
  }
}
