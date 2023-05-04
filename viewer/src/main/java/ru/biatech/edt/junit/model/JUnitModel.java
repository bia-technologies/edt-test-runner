/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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
 *     Achim Demelt <a.demelt@exxcellent.de> - [junit] Separate UI from non-UI code - https://bugs.eclipse.org/bugs/show_bug.cgi?id=278844
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

import lombok.NonNull;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import ru.biatech.edt.junit.JUnitCore;
import ru.biatech.edt.junit.JUnitPreferencesConstants;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleEvent;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleItem;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleListener;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleMonitor;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.model.serialize.Serializer;
import ru.biatech.edt.junit.ui.JUnitMessages;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Central registry for JUnit test runs.
 */
public final class JUnitModel {

  private final ListenerList<ITestRunSessionListener> fTestRunSessionListeners = new ListenerList<>();
  /**
   * Active test run sessions, youngest first.
   */
  private final LinkedList<TestRunSession> fTestRunSessions = new LinkedList<>();
  private LifecycleListener lifecycleListener;

  /**
   * Imports a test run session from the given file.
   *
   * @param file a file containing a test run session transcript
   * @return the imported test run session
   * @throws CoreException if the import failed
   */
  public static TestRunSession importTestRunSession(File file, String defaultProjectName) throws CoreException {
    return Serializer.importTestRunSession(file, defaultProjectName);
  }

  /**
   * Imports a test run session from the given URL.
   *
   * @param url     an URL to a test run session transcript
   * @param monitor a progress monitor for cancellation
   * @throws InvocationTargetException wrapping a CoreException if the import failed
   * @throws InterruptedException      if the import was cancelled
   * @since 3.6
   */
  public static void importTestRunSession(String url, String defaultProjectName, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    Serializer.importTestRunSession(url, defaultProjectName, monitor);
  }

  /**
   * Starts the model (called by the {@link JUnitCore} on startup).
   */
  public void start() {
    LifecycleMonitor.addListener(lifecycleListener = (eventType, item) -> {
      if (LifecycleEvent.isFinished(eventType)) {
        JUnitModel.loadTestReport(item);
      }
    });
    addTestRunSessionListener(new TestRunSessionListener());
  }

  /**
   * Stops the model (called by the {@link JUnitCore} on shutdown).
   */
  public void stop() {
    LifecycleMonitor.removeListener(lifecycleListener);
    File historyDirectory = TestViewerPlugin.core().getHistoryDirectory();
    File[] swapFiles = historyDirectory.listFiles();
    if (swapFiles != null) {
      for (File swapFile : swapFiles) {
        swapFile.delete();
      }
    }
  }

  public void addTestRunSessionListener(ITestRunSessionListener listener) {
    fTestRunSessionListeners.add(listener);
  }

  public void removeTestRunSessionListener(ITestRunSessionListener listener) {
    fTestRunSessionListeners.remove(listener);
  }

  /**
   * @return a list of active {@link TestRunSession}s. The list is a copy of
   * the internal data structure and modifications do not affect the
   * global list of active sessions. The list is sorted by age, youngest first.
   */
  public synchronized List<TestRunSession> getTestRunSessions() {
    return new ArrayList<>(fTestRunSessions);
  }

  /**
   * Adds the given {@link TestRunSession} and notifies all registered
   * {@link ITestRunSessionListener}s.
   *
   * @param testRunSession the session to add
   */
  public void addTestRunSession(@NonNull TestRunSession testRunSession) {
    ArrayList<TestRunSession> toRemove = new ArrayList<>();

    synchronized (this) {
      Assert.isLegal(!fTestRunSessions.contains(testRunSession));
      fTestRunSessions.addFirst(testRunSession);

      int maxCount = Platform.getPreferencesService().getInt(TestViewerPlugin.PLUGIN_ID, JUnitPreferencesConstants.MAX_TEST_RUNS, 10, null);
      int size = fTestRunSessions.size();
      if (size > maxCount) {
        List<TestRunSession> excess = fTestRunSessions.subList(maxCount, size);
        for (Iterator<TestRunSession> iter = excess.iterator(); iter.hasNext(); ) {
          TestRunSession oldSession = iter.next();
          if (!oldSession.isStarting() && !oldSession.isRunning() && !oldSession.isKeptAlive()) {
            toRemove.add(oldSession);
            iter.remove();
          }
        }
      }
    }

    for (TestRunSession oldSession : toRemove) {
      notifyTestRunSessionRemoved(oldSession);
    }
    notifyTestRunSessionAdded(testRunSession);
  }

  /**
   * Removes the given {@link TestRunSession} and notifies all registered
   * {@link ITestRunSessionListener}s.
   *
   * @param testRunSession the session to remove
   */
  public void removeTestRunSession(TestRunSession testRunSession) {
    boolean existed;
    synchronized (this) {
      existed = fTestRunSessions.remove(testRunSession);
    }
    if (existed) {
      notifyTestRunSessionRemoved(testRunSession);
    }
    testRunSession.removeSwapFile();
  }

  private void notifyTestRunSessionRemoved(TestRunSession testRunSession) {
    testRunSession.stopTestRun();
    ILaunch launch = testRunSession.getLaunch();
    if (launch != null) {
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
      launchManager.removeLaunch(launch);
    }

    fTestRunSessionListeners.forEach(it -> it.sessionRemoved(testRunSession));
  }

  private void notifyTestRunSessionAdded(TestRunSession testRunSession) {
    fTestRunSessionListeners.forEach(it -> it.sessionAdded(testRunSession));
  }

  public static void loadTestReport(LifecycleItem item) {
    TestViewerPlugin.log().debug(JUnitMessages.JUnitModel_LoadReport);

    try {
      var launch = item.getMainLaunch();

      var configuration = launch.getLaunchConfiguration();
      var project = LaunchConfigurationAttributes.getProject(configuration);

      var reportPath = LaunchHelper.getReportPath(configuration);
      TestViewerPlugin.log().debug(JUnitMessages.JUnitModel_ReportFile, reportPath.toAbsolutePath());

      if (!Files.exists(reportPath)) {
        TestViewerPlugin.log().logError(JUnitMessages.JUnitModel_ReportFileNotFound);
        return;
      }

      var session = JUnitModel.importTestRunSession(reportPath.toFile(), project);
      assert session != null;
      session.setLaunch(item.getTestLaunch());

      TestViewerPlugin.ui().asyncShowTestRunnerViewPart();

      Files.deleteIfExists(reportPath);
    } catch (CoreException | IOException e) {
      TestViewerPlugin.log().logError(JUnitMessages.JUnitModel_UnknownErrorOnReportLoad, e);
    }
  }

  /**
   * @deprecated to prevent deprecation warnings
   */
  @Deprecated
  private static final class TestRunSessionListener implements ITestRunSessionListener {
    private TestRunSession fActiveTestRunSession;
    private ITestSessionListener fTestSessionListener;

    @Override
    public void sessionAdded(TestRunSession testRunSession) {
      // Only serve one legacy ITestRunListener at a time, since they cannot distinguish between different concurrent test sessions:
      if (fActiveTestRunSession != null)
        return;

      fActiveTestRunSession = testRunSession;

      fTestSessionListener = new ITestSessionListener() {
        @Override
        public void testAdded(TestElement testElement) {
        }

        @Override
        public void sessionStarted() {
          TestViewerPlugin.core().getNewTestRunListeners().forEach(it -> it.sessionStarted(fActiveTestRunSession));
        }

        @Override
        public void sessionTerminated() {
          TestViewerPlugin.core().getNewTestRunListeners().forEach(it -> it.sessionTerminated(fActiveTestRunSession));
          sessionRemoved(fActiveTestRunSession);
        }

        @Override
        public void sessionStopped(long elapsedTime) {
          TestViewerPlugin.core().getNewTestRunListeners().forEach(it -> it.sessionFinished(fActiveTestRunSession));
          sessionRemoved(fActiveTestRunSession);
        }

        @Override
        public void sessionEnded(long elapsedTime) {
          TestViewerPlugin.core().getNewTestRunListeners().forEach(it -> it.sessionFinished(fActiveTestRunSession));
          sessionRemoved(fActiveTestRunSession);
        }

        @Override
        public void runningBegins() {
          // ignore
        }

        @Override
        public void testStarted(TestCaseElement testCaseElement) {
          // not fire
//          TestViewerPlugin.core().getNewTestRunListeners().forEach(it->it.testCaseStarted(testCaseElement));
        }

        @Override
        public void testFailed(TestElement testElement, TestStatus status, String trace, String expected, String actual) {
          // not fire
//          TestViewerPlugin.core().getNewTestRunListeners().forEach(it->it.testCaseFinished(testElement));
        }

        @Override
        public void testEnded(TestCaseElement testCaseElement) {
          // not fire
//          TestViewerPlugin.core().getNewTestRunListeners().forEach(it->it.testCaseFinished(testCaseElement));
        }

        @Override
        public void testRerun(TestCaseElement testCaseElement, TestStatus status, String trace, String expectedResult, String actualResult) {
          // not fire
//          TestViewerPlugin.core().getNewTestRunListeners().forEach(it->it.testCaseRerun(testCaseElement));
        }

        @Override
        public boolean acceptsSwapToDisk() {
          return true;
        }
      };
      fActiveTestRunSession.addTestSessionListener(fTestSessionListener);
    }

    @Override
    public void sessionRemoved(TestRunSession testRunSession) {
      if (fActiveTestRunSession == testRunSession) {
        fActiveTestRunSession.removeTestSessionListener(fTestSessionListener);
        fTestSessionListener = null;
        fActiveTestRunSession = null;
      }
    }
  }

}