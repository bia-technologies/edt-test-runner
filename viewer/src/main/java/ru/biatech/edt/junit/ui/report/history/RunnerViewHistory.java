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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import ru.biatech.edt.junit.BasicElementLabels;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.Session;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.report.actions.ActionsSupport;
import ru.biatech.edt.junit.ui.report.actions.ImportTestRunSessionAction;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * Класс-помощник для работы с историей запусков тестирования
 */
public class RunnerViewHistory extends ViewHistory<Session> {

  private final TestRunnerViewPart testRunnerViewPart;

  public RunnerViewHistory(TestRunnerViewPart testRunnerViewPart) {
    this.testRunnerViewPart = testRunnerViewPart;
  }

  @Override
  public void configureHistoryListAction(IAction action) {
    action.setText(UIMessages.TestRunnerViewPart_history);
  }

  @Override
  public void configureHistoryDropDownAction(IAction action) {
    action.setToolTipText(UIMessages.TestRunnerViewPart_test_run_history);
    ActionsSupport.setLocalImageDescriptors(action, "history_list.png"); //$NON-NLS-1$
  }

  @Override
  public Action getClearAction() {
    return new ClearAction(this);
  }

  @Override
  public String getHistoryListDialogTitle() {
    return UIMessages.TestRunnerViewPart_test_runs;
  }

  @Override
  public String getHistoryListDialogMessage() {
    return UIMessages.TestRunnerViewPart_select_test_run;
  }

  @Override
  public Shell getShell() {
    return testRunnerViewPart.getShell();
  }

  @Override
  public List<Session> getHistoryEntries() {
    return TestViewerPlugin.core().getSessionsManager().getSessions();
  }

  @Override
  public Session getCurrentEntry() {
    return testRunnerViewPart.getSession();
  }

  @Override
  public void setActiveEntry(Session entry) {
    var deactivatedSession = testRunnerViewPart.setActiveSession(entry);
    if (deactivatedSession != null) {
      deactivatedSession.swapOut();
    }
  }

  @Override
  public void setHistoryEntries(List<Session> remainingEntries, Session activeEntry) {
    testRunnerViewPart.setActiveSession(activeEntry);

    var sessions = TestViewerPlugin.core().getSessionsManager().getSessions();
    sessions.removeAll(remainingEntries);
    for (var session : sessions) {
      TestViewerPlugin.core().getSessionsManager().removeSession(session);
    }
    for (var remaining : remainingEntries) {
      remaining.swapOut();
    }
  }

  @Override
  public ImageDescriptor getImageDescriptor(Object element) {
    var session = (Session) element;
    var imageProvider = testRunnerViewPart.getImageProvider();

    if (session.isStopped()) {
      return imageProvider.getSuiteIconDescriptor();
    }

    if (session.isRunning()) {
      return imageProvider.getSuiteRunningIconDescriptor();
    }

    var result = session.getTestResult(true);

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
  public String getText(Session session) {
    var testRunLabel = BasicElementLabels.getElementName(session.getName());
    if (session.getStartTime() <= 0) {
      return testRunLabel;
    } else {
      var startTime = DateFormat.getDateTimeInstance().format(new Date(session.getStartTime()));
      return MessageFormat.format(UIMessages.TestRunnerViewPart_testName_startTime, testRunLabel, startTime);
    }
  }

  @Override
  public void addMenuEntries(MenuManager manager) {
    manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new ImportTestRunSessionAction());
  }
}
