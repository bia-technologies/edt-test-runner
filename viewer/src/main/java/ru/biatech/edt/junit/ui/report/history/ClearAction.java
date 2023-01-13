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

package ru.biatech.edt.junit.ui.report.history;

import org.eclipse.jface.action.Action;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.TestRunSession;
import ru.biatech.edt.junit.ui.JUnitMessages;

import java.util.List;

/**
 * Команда очистки истории запусков тестирования
 */
public class ClearAction extends Action {

  private final RunnerViewHistory viewHistory;

  public ClearAction(RunnerViewHistory viewHistory) {
    this.viewHistory = viewHistory;

    setText(JUnitMessages.TestRunnerViewPart_clear_history_label);

    boolean enabled = false;
    List<TestRunSession> testRunSessions = TestViewerPlugin.core().getModel().getTestRunSessions();
    for (TestRunSession testRunSession : testRunSessions) {
      if (!testRunSession.isRunning() && !testRunSession.isStarting()) {
        enabled = true;
        break;
      }
    }
    setEnabled(enabled);
  }

  @Override
  public void run() {
    List<TestRunSession> testRunSessions = getRunningSessions();
    TestRunSession first = testRunSessions.isEmpty() ? null : testRunSessions.get(0);
    viewHistory.setHistoryEntries(testRunSessions, first);
  }

  private List<TestRunSession> getRunningSessions() {
    List<TestRunSession> testRunSessions = TestViewerPlugin.core().getModel().getTestRunSessions();
    testRunSessions.removeIf(testRunSession -> !testRunSession.isRunning() && !testRunSession.isStarting());
    return testRunSessions;
  }
}