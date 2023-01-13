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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import ru.biatech.edt.junit.BasicElementLabels;
import ru.biatech.edt.junit.JUnitPreferencesConstants;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.TestResult;
import ru.biatech.edt.junit.model.TestRunSession;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.report.actions.ImportTestRunSessionAction;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * Класс-помощник для работы с историей запусков тестирования
 */
public class RunnerViewHistory extends ViewHistory<TestRunSession> {

  private final TestRunnerViewPart testRunnerViewPart;

  public RunnerViewHistory(TestRunnerViewPart testRunnerViewPart) {
    this.testRunnerViewPart = testRunnerViewPart;
  }

  @Override
  public void configureHistoryListAction(IAction action) {
    action.setText(JUnitMessages.TestRunnerViewPart_history);
  }

  @Override
  public void configureHistoryDropDownAction(IAction action) {
    action.setToolTipText(JUnitMessages.TestRunnerViewPart_test_run_history);
    TestViewerPlugin.ui().setLocalImageDescriptors(action, "history_list.png"); //$NON-NLS-1$
  }

  @Override
  public Action getClearAction() {
    return new ClearAction(this);
  }

  @Override
  public String getHistoryListDialogTitle() {
    return JUnitMessages.TestRunnerViewPart_test_runs;
  }

  @Override
  public String getHistoryListDialogMessage() {
    return JUnitMessages.TestRunnerViewPart_select_test_run;
  }

  @Override
  public Shell getShell() {
    return testRunnerViewPart.getShell();
  }

  @Override
  public List<TestRunSession> getHistoryEntries() {
    return TestViewerPlugin.core().getModel().getTestRunSessions();
  }

  @Override
  public TestRunSession getCurrentEntry() {
    return testRunnerViewPart.getTestRunSession();
  }

  @Override
  public void setActiveEntry(TestRunSession entry) {
    TestRunSession deactivatedSession = testRunnerViewPart.setActiveTestRunSession(entry);
    if (deactivatedSession != null) {
      deactivatedSession.swapOut();
    }
  }

  @Override
  public void setHistoryEntries(List<TestRunSession> remainingEntries, TestRunSession activeEntry) {
    testRunnerViewPart.setActiveTestRunSession(activeEntry);

    List<TestRunSession> testRunSessions = TestViewerPlugin.core().getModel().getTestRunSessions();
    testRunSessions.removeAll(remainingEntries);
    for (TestRunSession testRunSession : testRunSessions) {
      TestViewerPlugin.core().getModel().removeTestRunSession(testRunSession);
    }
    for (TestRunSession remaining : remainingEntries) {
      remaining.swapOut();
    }
  }

  @Override
  public ImageDescriptor getImageDescriptor(Object element) {
    TestRunSession session = (TestRunSession) element;
    var imageProvider = testRunnerViewPart.getImageProvider();

    if (session.isStopped()) {
      return imageProvider.getSuiteIconDescriptor();
    }

    if (session.isRunning()) {
      return imageProvider.getSuiteRunningIconDescriptor();
    }

    TestResult result = session.getTestResult(true);

    switch (result) {
      case OK:
        return imageProvider.getSuiteOkIconDescriptor();
      case ERROR:
        return imageProvider.getSuiteErrorIconDescriptor();
      case FAILURE:
        return imageProvider.getSuiteFailIconDescriptor();
      default:
        return imageProvider.getSuiteIconDescriptor();
    }
  }

  @Override
  public String getText(TestRunSession session) {
    String testRunLabel = BasicElementLabels.getJavaElementName(session.getTestRunName());
    if (session.getStartTime() <= 0) {
      return testRunLabel;
    } else {
      String startTime = DateFormat.getDateTimeInstance().format(new Date(session.getStartTime()));
      return MessageFormat.format(JUnitMessages.TestRunnerViewPart_testName_startTime, testRunLabel, startTime);
    }
  }

  @Override
  public void addMenuEntries(MenuManager manager) {
    manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new ImportTestRunSessionAction(testRunnerViewPart.getShell()));
  }

  @Override
  public String getMaxEntriesMessage() {
    return JUnitMessages.TestRunnerViewPart_max_remembered;
  }

  @Override
  public int getMaxEntries() {
    return Platform.getPreferencesService().getInt(TestViewerPlugin.getPluginId(), JUnitPreferencesConstants.MAX_TEST_RUNS, 10, null);
  }

  @Override
  public void setMaxEntries(int maxEntries) {
    InstanceScope.INSTANCE.getNode(TestViewerPlugin.getPluginId()).putInt(JUnitPreferencesConstants.MAX_TEST_RUNS, maxEntries);
  }
}
