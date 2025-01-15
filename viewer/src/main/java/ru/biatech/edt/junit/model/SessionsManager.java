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
import org.eclipse.debug.core.DebugPlugin;
import ru.biatech.edt.junit.Core;
import ru.biatech.edt.junit.Preferences;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleEvent;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleItem;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleListener;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleMonitor;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.model.serialize.Serializer;
import ru.biatech.edt.junit.ui.UIMessages;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Central registry for JUnit test runs.
 */
public final class SessionsManager {

  private final ListenerList<ISessionListener> sessionListeners = new ListenerList<>();
  /**
   * Active test run sessions, youngest first.
   */
  private final LinkedList<Session> sessions = new LinkedList<>();
  private LifecycleListener lifecycleListener;

  public static void loadTestReport(LifecycleItem item) {
    TestViewerPlugin.log().debug(UIMessages.JUnitModel_LoadReport);

    try {
      var launch = item.getMainLaunch();

      var configuration = launch.getLaunchConfiguration();
      var project = LaunchConfigurationAttributes.getProject(configuration);

      var reportPath = LaunchHelper.getReportPath(configuration);
      TestViewerPlugin.log().debug(UIMessages.JUnitModel_ReportFile, reportPath.toAbsolutePath());

      if (!Files.exists(reportPath)) {
        TestViewerPlugin.log().logError(UIMessages.JUnitModel_ReportFileNotFound);
        return;
      }

      var session = SessionsManager.importSession(reportPath.toFile(), project);
      if (session == null) {
        TestViewerPlugin.log().logError("Session is null after import.");
        return;
      }
      session.setLaunch(item.getTestLaunch());

      TestViewerPlugin.ui().asyncShowTestRunnerViewPart();

      Files.deleteIfExists(reportPath);
    } catch (CoreException | IOException e) {
      TestViewerPlugin.log().logError(UIMessages.JUnitModel_UnknownErrorOnReportLoad, e);
    }
  }

  /**
   * Imports a test run session from the given file.
   *
   * @param file a file containing a test run session transcript
   * @return the imported test run session
   * @throws CoreException if the import failed
   */
  public static Session importSession(File file, String defaultProjectName) throws CoreException {
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
  public static void importSession(String url, String defaultProjectName, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    Serializer.importTestRunSession(url, defaultProjectName, monitor);
  }

  public void startSession(LifecycleItem item) {
    var session = new Session(item.getName(), LaunchHelper.getProject(item.getTestLaunch().getLaunchConfiguration()));
    session.setLaunch(item.getTestLaunch());
  }

  /**
   * Starts the model (called by the {@link Core} on startup).
   */
  public void start() {
    LifecycleMonitor.addListener(lifecycleListener = (eventType, item) -> {
      if (LifecycleEvent.START == eventType) {
        startSession(item);
      } else if (LifecycleEvent.isFinished(eventType)) {
        SessionsManager.loadTestReport(item);
      }
    });
    addTestRunSessionListener(new SessionListener());
  }

  /**
   * Stops the model (called by the {@link Core} on shutdown).
   */
  public void stop() {
    LifecycleMonitor.removeListener(lifecycleListener);
    var historyDirectory = TestViewerPlugin.core().getHistoryDirectory();
    var swapFiles = historyDirectory.listFiles();
    if (swapFiles != null) {
      for (var swapFile : swapFiles) {
        swapFile.delete();
      }
    }
  }

  public void addTestRunSessionListener(ISessionListener listener) {
    sessionListeners.add(listener);
  }

  public void removeTestRunSessionListener(ISessionListener listener) {
    sessionListeners.remove(listener);
  }

  /**
   * @return a list of active {@link Session}s. The list is a copy of
   * the internal data structure and modifications do not affect the
   * global list of active sessions. The list is sorted by age, youngest first.
   */
  public synchronized List<Session> getSessions() {
    return new ArrayList<>(sessions);
  }

  /**
   * Adds the given {@link Session} and notifies all registered
   * {@link ISessionListener}s.
   *
   * @param session the session to add
   */
  public void addSession(@NonNull Session session) {
    var toRemove = new ArrayList<Session>();

    synchronized (this) {
      Assert.isLegal(!sessions.contains(session));
      sessions.addFirst(session);

      var maxCount = Preferences.getMaxTestRuns();
      var size = sessions.size();
      if (size > maxCount) {
        var excess = sessions.subList(maxCount, size);
        for (var iter = excess.iterator(); iter.hasNext(); ) {
          var oldSession = iter.next();
          if (!oldSession.isStarting() && !oldSession.isRunning() && !oldSession.isKeptAlive()) {
            toRemove.add(oldSession);
            iter.remove();
          }
        }
      }
    }

    for (var oldSession : toRemove) {
      notifySessionRemoved(oldSession);
    }
    notifySessionAdded(session);
  }

  /**
   * Removes the given {@link Session} and notifies all registered
   * {@link ISessionListener}s.
   *
   * @param session the session to remove
   */
  public void removeSession(Session session) {
    boolean existed;
    synchronized (this) {
      existed = sessions.remove(session);
    }
    if (existed) {
      notifySessionRemoved(session);
    }
    session.removeSwapFile();
  }

  private void notifySessionRemoved(Session session) {
    session.stopTestRun();
    var launch = session.getLaunch();
    if (launch != null) {
      var launchManager = DebugPlugin.getDefault().getLaunchManager();
      launchManager.removeLaunch(launch);
    }

    sessionListeners.forEach(it -> it.sessionRemoved(session));
  }

  private void notifySessionAdded(Session session) {
    sessionListeners.forEach(it -> it.sessionAdded(session));
  }

  private static final class SessionListener implements ISessionListener {
    private Session activeSession;
    private ITestSessionListener sessionListener;

    @Override
    public void sessionAdded(Session session) {
      // Only serve one legacy ITestRunListener at a time, since they cannot distinguish between different concurrent test sessions:
      if (activeSession != null)
        return;

      activeSession = session;

      sessionListener = new ITestSessionListener() {
        @Override
        public void testAdded(TestElement testElement) {
        }

        @Override
        public void sessionStarted() {
          TestViewerPlugin.core().getNewTestRunListeners().forEach(it -> it.sessionStarted(activeSession));
        }

        @Override
        public void sessionTerminated() {
          TestViewerPlugin.core().getNewTestRunListeners().forEach(it -> it.sessionTerminated(activeSession));
          sessionRemoved(activeSession);
        }

        @Override
        public void sessionStopped(long elapsedTime) {
          TestViewerPlugin.core().getNewTestRunListeners().forEach(it -> it.sessionFinished(activeSession));
          sessionRemoved(activeSession);
        }

        @Override
        public void sessionEnded(long elapsedTime) {
          TestViewerPlugin.core().getNewTestRunListeners().forEach(it -> it.sessionFinished(activeSession));
          sessionRemoved(activeSession);
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
      activeSession.addTestSessionListener(sessionListener);
    }

    @Override
    public void sessionRemoved(Session session) {
      if (activeSession == session) {
        activeSession.removeTestSessionListener(sessionListener);
        sessionListener = null;
        activeSession = null;
      }
    }
  }


}