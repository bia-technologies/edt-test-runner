/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.xml.sax.SAXException;
import ru.biatech.edt.junit.BasicElementLabels;
import ru.biatech.edt.junit.JUnitCore;
import ru.biatech.edt.junit.JUnitLaunchListener;
import ru.biatech.edt.junit.JUnitPreferencesConstants;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.model.TestElement.Status;
import ru.biatech.edt.junit.ui.JUnitMessages;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.text.MessageFormat;
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
  private final JUnitLaunchListener fLaunchListener = new JUnitLaunchListener();

  /**
   * Imports a test run session from the given file.
   *
   * @param file a file containing a test run session transcript
   * @return the imported test run session
   * @throws CoreException if the import failed
   */
  public static TestRunSession importTestRunSession(File file, String defaultProjectName) throws CoreException {
    try {
      TestViewerPlugin.log().debug("Импорт отчета о тестировании: " + file.getAbsolutePath());
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      SAXParser parser = parserFactory.newSAXParser();
      TestRunHandler handler = new TestRunHandler();
      handler.fDefaultProjectName = defaultProjectName;
      parser.parse(file, handler);
      TestRunSession session = handler.getTestRunSession();
      if(session!=null){
        TestViewerPlugin.core().getModel().addTestRunSession(session);}
      else{
        TestViewerPlugin.log().logError(JUnitMessages.JUnitModel_ReportIsEmpty);
      }
      return session;
    } catch (ParserConfigurationException | SAXException e) {
      throwImportError(file, e);
    } catch (IOException e) {
      throwImportError(file, e);
    } catch (IllegalArgumentException e) {
      // Bug in parser: can throw IAE even if file is not null
      throwImportError(file, e);
    }
    return null; // does not happen
  }

  /**
   * Imports a test run session from the given URL.
   *
   * @param url     an URL to a test run session transcript
   * @param monitor a progress monitor for cancellation
   * @return the imported test run session
   * @throws InvocationTargetException wrapping a CoreException if the import failed
   * @throws InterruptedException      if the import was cancelled
   * @since 3.6
   */
  public static TestRunSession importTestRunSession(String url, String defaultProjectName, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    monitor.beginTask(JUnitMessages.JUnitModel_importing_from_url, IProgressMonitor.UNKNOWN);
    final String trimmedUrl = url.trim().replaceAll("\r\n?|\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
    final TestRunHandler handler = new TestRunHandler(monitor);
    handler.fDefaultProjectName = defaultProjectName;

    final CoreException[] exception = {null};
    final TestRunSession[] session = {null};

    Thread importThread = new Thread("JUnit URL importer") { //$NON-NLS-1$
      @Override
      public void run() {
        try {
          SAXParserFactory parserFactory = SAXParserFactory.newInstance();
//					parserFactory.setValidating(true); // TODO: add DTD and debug flag
          SAXParser parser = parserFactory.newSAXParser();
          parser.parse(trimmedUrl, handler);
          session[0] = handler.getTestRunSession();
        } catch (OperationCanceledException e) {
          // canceled
        } catch (ParserConfigurationException | SAXException e) {
          storeImportError(e);
        } catch (IOException e) {
          storeImportError(e);
        } catch (IllegalArgumentException e) {
          // Bug in parser: can throw IAE even if URL is not null
          storeImportError(e);
        }
      }

      private void storeImportError(Exception e) {
        exception[0] = new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
                TestViewerPlugin.getPluginId(), JUnitMessages.JUnitModel_could_not_import, e));
      }
    };
    importThread.start();

    while (session[0] == null && exception[0] == null && !monitor.isCanceled()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // that's OK
      }
    }
    if (session[0] == null) {
      if (exception[0] != null) {
        throw new InvocationTargetException(exception[0]);
      } else {
        importThread.interrupt(); // have to kill the thread since we don't control URLConnection and XML parsing
        throw new InterruptedException();
      }
    }

    TestViewerPlugin.core().getModel().addTestRunSession(session[0]);
    monitor.done();
    return session[0];
  }

  public static void importIntoTestRunSession(File swapFile, TestRunSession testRunSession) throws CoreException {
    try {
      TestViewerPlugin.log().debug("Обновление отчета о тестировании: " + swapFile.getAbsolutePath());
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
//			parserFactory.setValidating(true); // TODO: add DTD and debug flag
      SAXParser parser = parserFactory.newSAXParser();
      TestRunHandler handler = new TestRunHandler(testRunSession);
      parser.parse(swapFile, handler);
    } catch (ParserConfigurationException | SAXException e) {
      throwImportError(swapFile, e);
    } catch (IOException e) {
      throwImportError(swapFile, e);
    } catch (IllegalArgumentException e) {
      // Bug in parser: can throw IAE even if file is not null
      throwImportError(swapFile, e);
    }
  }

  private static void throwImportError(File file, Exception e) throws CoreException {
    var message = MessageFormat.format(JUnitMessages.JUnitModel_could_not_read, BasicElementLabels.getPathLabel(file));
    throw new CoreException(TestViewerPlugin.log().createErrorStatus(message, e));
  }

  /**
   * Starts the model (called by the {@link JUnitCore} on startup).
   */
  public void start() {
    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchListener);
    DebugPlugin.getDefault().addDebugEventListener(fLaunchListener);
    addTestRunSessionListener(new TestRunSessionListener());
  }

  /**
   * Stops the model (called by the {@link JUnitCore} on shutdown).
   */
  public void stop() {
    DebugPlugin.getDefault().removeDebugEventListener(fLaunchListener);
    DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchListener);

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
  public void addTestRunSession(TestRunSession testRunSession) {
    Assert.isNotNull(testRunSession);
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

  public static void loadTestReport(ILaunch launch) {
    TestViewerPlugin.log().debug(JUnitMessages.JUnitModel_LoadReport);

    try {
      var configuration = launch.getLaunchConfiguration();
      String workPath = configuration.getAttribute(LaunchConfigurationAttributes.WORK_PATH, (String) null);
      String project = configuration.getAttribute(LaunchConfigurationAttributes.PROJECT, (String) null);

      File file = new File(workPath, LaunchHelper.REPORT_FILE_NAME); //$NON-NLS-1$
      TestViewerPlugin.log().debug(JUnitMessages.JUnitModel_ReportFile, file.getAbsolutePath());
      if (!file.exists()) {
        TestViewerPlugin.log().logError(JUnitMessages.JUnitModel_ReportFileNotFound);
        return;
      }

      TestRunSession session = JUnitModel.importTestRunSession(file, project);
      assert session != null;
      session.setLaunch(launch);

      TestViewerPlugin.ui().asyncShowTestRunnerViewPart();

      Files.deleteIfExists(file.toPath());
    } catch (CoreException|IOException e) {
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
        public void testFailed(TestElement testElement, Status status, String trace, String expected, String actual) {
          // not fire
//          TestViewerPlugin.core().getNewTestRunListeners().forEach(it->it.testCaseFinished(testElement));
        }

        @Override
        public void testEnded(TestCaseElement testCaseElement) {
          // not fire
//          TestViewerPlugin.core().getNewTestRunListeners().forEach(it->it.testCaseFinished(testCaseElement));
        }

        @Override
        public void testReran(TestCaseElement testCaseElement, Status status, String trace, String expectedResult, String actualResult) {
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