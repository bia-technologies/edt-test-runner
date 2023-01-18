/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Thirumala Reddy Mutchukota <thirumala@google.com> - [JUnit] Avoid rerun test launch on UI thread - https://bugs.eclipse.org/bugs/show_bug.cgi?id=411841
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.model;

import com._1c.g5.v8.dt.core.platform.IV8Project;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunch;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestKind;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A test run session holds all information about a test run, i.e.
 * launch configuration, launch, test tree (including results).
 */
@Getter
public class TestRunSession implements ITestRunSession {

  private static final String EMPTY_STRING = ""; //$NON-NLS-1$
  /**
   * Ссылка на 1С проект, or <code>null</code>.
   */
  private final IV8Project launchedProject;

  @Getter(AccessLevel.NONE)
  private final ListenerList<ITestSessionListener> fSessionListeners = new ListenerList<>();

  /**
   * Number of tests started during this test run.
   */
  @Getter(AccessLevel.NONE)
  private final AtomicInteger startedCount = new AtomicInteger(0);

  /**
   * Number of tests ignored during this test run.
   */
  @Getter(AccessLevel.NONE)
  private final AtomicInteger ignoredCount = new AtomicInteger(0);

  /**
   * Number of tests whose assumption failed during this test run.
   */
  @Getter(AccessLevel.NONE)
  private final AtomicInteger assumptionFailureCount = new AtomicInteger(0);

  /**
   * Number of errors during this test run.
   */
  @Getter(AccessLevel.NONE)
  private final AtomicInteger errorCount = new AtomicInteger(0);

  /**
   * Number of failures during this test run.
   */
  @Getter(AccessLevel.NONE)
  private final AtomicInteger failureCount = new AtomicInteger(0);

  /**
   * Total number of tests to run.
   */
  @Getter(AccessLevel.NONE)
  private final AtomicInteger totalCount = new AtomicInteger(0);

  /**
   * <ul>
   * <li>If &gt; 0: Start time in millis</li>
   * <li>If &lt; 0: Unique identifier for imported test run</li>
   * <li>If = 0: Session not started yet</li>
   * </ul>
   */
  private final long startTime;

  /**
   * <code>true</code> iff this session has been started, but not ended nor stopped nor terminated
   */
  private volatile boolean running;

  /**
   * <code>true</code> iff the session has been stopped or terminated
   */
  private volatile boolean stopped;

  /**
   * The launch, or <code>null</code> iff this session was run externally.
   */
  private ILaunch launch;

  private String testRunName;

  private ITestKind testRunnerKind;

  /**
   * The model root, or <code>null</code> if swapped to disk.
   */
  private TestRoot testRoot;

  /**
   * The test run session's cached result, or <code>null</code> if <code>fTestRoot != null</code>.
   */
  @Getter(AccessLevel.NONE)
  private TestResult fTestResult;

  /**
   * Tags included in this test run.
   */
  @Setter
  @Getter(AccessLevel.NONE)
  private String includeTags;

  /**
   * Tags excluded from this test run.
   */
  @Setter
  @Getter(AccessLevel.NONE)
  private String excludeTags;

  /**
   * Creates a test run session.
   *
   * @param pTestRunName name of the test run
   * @param project      may be <code>null</code>
   */
  public TestRunSession(String pTestRunName, IV8Project project) {
    //TODO: check assumptions about non-null fields

    Assert.isNotNull(pTestRunName);
    testRunName = pTestRunName;
    launchedProject = project;

    startTime = System.currentTimeMillis();
    testRunnerKind = ITestKind.NULL; //TODO

    reset();
  }

  public void reset() {
    startedCount.set(0);
    failureCount.set(0);
    assumptionFailureCount.set(0);
    errorCount.set(0);
    ignoredCount.set(0);
    totalCount.set(0);

    testRoot = Factory.createRoot(this);
    fTestResult = null;
  }

  @Override
  public ProgressState getProgressState() {
    if (isRunning()) {
      return ProgressState.RUNNING;
    }
    if (isStopped()) {
      return ProgressState.STOPPED;
    }
    return ProgressState.COMPLETED;
  }

  @Override
  public TestResult getTestResult(boolean includeChildren) {
    if (testRoot != null) {
      return testRoot.getTestResult(true);
    } else {
      return fTestResult;
    }
  }

  @Override
  public ITestElement[] getChildren() {
    return getTestRoot().getChildren();
  }

  @Override
  public ITestElementContainer getParentContainer() {
    return null;
  }

  @Override
  public ITestRunSession getTestRunSession() {
    return this;
  }

  public void setLaunch(ILaunch pLaunch) {
    launch = pLaunch;
    var launchConfiguration = launch.getLaunchConfiguration();
    if (launchConfiguration != null) {
      testRunName = launchConfiguration.getName();
      testRunnerKind = LaunchHelper.getTestRunnerKind(launchConfiguration);
    } else {
      testRunName = launchedProject.getProject().getName();
      testRunnerKind = ITestKind.NULL;
    }
  }

  public String getTestRunPresent() {
    return testRunName + " " + DateFormat.getDateTimeInstance().format(new Date(startTime));
  }

  public synchronized void addTestSessionListener(ITestSessionListener listener) {
//		swapIn();
    fSessionListeners.add(listener);
  }

  public void removeTestSessionListener(ITestSessionListener listener) {
    fSessionListeners.remove(listener);
  }

  public synchronized void swapOut() { // TODO Не ясно, нужно или нет
//		if (fTestRoot == null)
//			return;
//		if (isRunning() || isStarting() || isKeptAlive())
//			return;
//
//		for (ITestSessionListener registered : fSessionListeners) {
//			if (! registered.acceptsSwapToDisk())
//				return;
//		}
//
//		try {
//			File swapFile= getSwapFile();
//
//			JUnitModel.exportTestRunSession(this, swapFile);
//			fTestResult= fTestRoot.getTestResult(true);
//			fTestRoot= null;
//			fTestRunnerClient= null;
//			fUnrootedSuite= null;
//
//		} catch (IllegalStateException | CoreException e) {
//			JUnitCorePlugin.log(e);
//		}
  }

  public boolean isStarting() {
    return getStartTime() == 0 && launch != null && !launch.isTerminated();
  }

  public void removeSwapFile() {
    var swapFile = getSwapFile();
    if (swapFile.exists()) {
      swapFile.delete();
    }
  }

//	public synchronized void swapIn() {// TODO Не ясно, нужно или нет
//		if (fTestRoot != null)
//			return;
//
//		try {
//			JUnitModel.importIntoTestRunSession(getSwapFile(), this);
//		} catch (IllegalStateException | CoreException e) {
//			JUnitCorePlugin.log(e);
//			fTestRoot= Factory.createRoot(this);
//			fTestResult= null;
//		}
//	}

  private File getSwapFile() throws IllegalStateException {
    var historyDir = TestViewerPlugin.core().getHistoryDirectory();
    var isoTime = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date(getStartTime())); //$NON-NLS-1$
    var swapFileName = isoTime + ".xml"; //$NON-NLS-1$
    return new File(historyDir, swapFileName);
  }

  public void stopTestRun() {
    if (isRunning() || !isKeptAlive()) {
      stopped = true;
    }
  }

  /**
   * @return <code>true</code> iff the runtime VM of this test session is still alive
   */
  public boolean isKeptAlive() {
    return false;
  }

  public void registerTestFailureStatus(TestElement testElement) {
    if (!testElement.isAssumptionFailure()) {
      if (testElement.getStatus().isError()) {
        errorCount.incrementAndGet();
      } else if (testElement.getStatus().isFailure()) {
        failureCount.incrementAndGet();
      }
    }
  }

  public void registerTestEnded(TestElement testElement, boolean completed) {
    if (testElement instanceof TestCaseElement) {
      totalCount.incrementAndGet();
      if (!completed) {
        return;
      }
      startedCount.incrementAndGet();
      if (((TestCaseElement) testElement).isIgnored()) {
        ignoredCount.incrementAndGet();
      }
      if (!testElement.getStatus().isErrorOrFailure()) {
        setStatus(testElement, TestStatus.OK);
      }
    }

    if (testElement.isAssumptionFailure()) {
      assumptionFailureCount.incrementAndGet();
    }
  }

  private void setStatus(TestElement testElement, TestStatus status) {
    testElement.setStatus(status);
  }

  public ITestCaseElement[] getAllFailedTestElements() {
    var failures = new ArrayList<ITestCaseElement>();
    addFailures(failures, getTestRoot());
    return failures.toArray(ITestCaseElement[]::new);
  }

  private void addFailures(ArrayList<ITestCaseElement> failures, ITestElement testElement) {
    var testResult = testElement.getTestResult(true);
    if (testElement instanceof ITestCaseElement && (testResult == TestResult.ERROR || testResult == TestResult.FAILURE)) {
      failures.add((ITestCaseElement) testElement);
    }
    if (testElement instanceof ITestElementContainer) {
      var children = ((ITestElementContainer) testElement).getChildren();
      for (var child : children) {
        addFailures(failures, child);
      }
    }
  }

  @Override
  public double getElapsedTimeInSeconds() {
    return testRoot == null ? Double.NaN : testRoot.getElapsedTimeInSeconds();
  }

  @Override
  public String getTestName() {
    return "Test session";
  }

  public String getIncludeTags() {
    if (launch != null) {
      try {
        var launchConfig = launch.getLaunchConfiguration();
        if (launchConfig != null) {
          var hasIncludeTags = launchConfig.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_HAS_INCLUDE_TAGS, false);
          if (hasIncludeTags) {
            return launchConfig.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_INCLUDE_TAGS, EMPTY_STRING);
          }
        }
      } catch (CoreException ignore) {
      }
      return EMPTY_STRING;
    }
    return includeTags;
  }

  public String getExcludeTags() {
    if (launch != null) {
      try {
        var launchConfig = launch.getLaunchConfiguration();
        if (launchConfig != null) {
          var hasExcludeTags = launchConfig.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_HAS_EXCLUDE_TAGS, false);
          if (hasExcludeTags) {
            return launchConfig.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_EXCLUDE_TAGS, EMPTY_STRING);
          }
        }
      } catch (CoreException e) {
        //ignore
      }
      return EMPTY_STRING;
    }
    return excludeTags;
  }

  public int getStartedCount() {
    return startedCount.get();
  }

  public int getIgnoredCount() {
    return ignoredCount.get();
  }

  public int getAssumptionFailureCount() {
    return assumptionFailureCount.get();
  }

  public int getErrorCount() {
    return errorCount.get();
  }

  public int getFailureCount() {
    return failureCount.get();
  }

  public int getTotalCount() {
    return totalCount.get();
  }

  @Override
  public String toString() {
    return testRunName + " " + DateFormat.getDateTimeInstance().format(new Date(startTime)); //$NON-NLS-1$
  }
}