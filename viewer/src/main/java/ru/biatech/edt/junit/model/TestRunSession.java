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
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestKind;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A test run session holds all information about a test run, i.e.
 * launch configuration, launch, test tree (including results).
 */
public class TestRunSession implements ITestRunSession {

  private static final String EMPTY_STRING = ""; //$NON-NLS-1$
  /**
   * Java project, or <code>null</code>.
   */
  private final IV8Project fProject;
  private final ListenerList<ITestSessionListener> fSessionListeners;
  /**
   * Number of tests started during this test run.
   */
  volatile int fStartedCount;
  /**
   * Number of tests ignored during this test run.
   */
  volatile int fIgnoredCount;
  /**
   * Number of tests whose assumption failed during this test run.
   */
  volatile int fAssumptionFailureCount;
  /**
   * Number of errors during this test run.
   */
  volatile int fErrorCount;
  /**
   * Number of failures during this test run.
   */
  volatile int fFailureCount;
  /**
   * Total number of tests to run.
   */
  volatile int fTotalCount;
  /**
   * <ul>
   * <li>If &gt; 0: Start time in millis</li>
   * <li>If &lt; 0: Unique identifier for imported test run</li>
   * <li>If = 0: Session not started yet</li>
   * </ul>
   */
  volatile long fStartTime;
  volatile boolean fIsRunning;
  volatile boolean fIsStopped;
  /**
   * The launch, or <code>null</code> iff this session was run externally.
   */
  private ILaunch fLaunch;
  private String fTestRunName;
  private ITestKind fTestRunnerKind;
  /**
   * The model root, or <code>null</code> if swapped to disk.
   */
  private TestRoot fTestRoot;
  /**
   * The test run session's cached result, or <code>null</code> if <code>fTestRoot != null</code>.
   */
  private TestResult fTestResult;
  /**
   * Tags included in this test run.
   */
  private String fIncludeTags;
  /**
   * Tags excluded from this test run.
   */
  private String fExcludeTags;

  /**
   * Creates a test run session.
   *
   * @param testRunName name of the test run
   * @param project     may be <code>null</code>
   */
  public TestRunSession(String testRunName, IV8Project project) {
    //TODO: check assumptions about non-null fields

    fLaunch = null;
    fProject = project;
    fStartTime = System.currentTimeMillis();

    Assert.isNotNull(testRunName);
    fTestRunName = testRunName;
    fTestRunnerKind = ITestKind.NULL; //TODO

    fTestRoot = Factory.createRoot(this);

    fSessionListeners = new ListenerList<>();
  }

  void reset() {
    fStartedCount = 0;
    fFailureCount = 0;
    fAssumptionFailureCount = 0;
    fErrorCount = 0;
    fIgnoredCount = 0;
    fTotalCount = 0;

    fTestRoot = Factory.createRoot(this);
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
    if (fTestRoot != null) {
      return fTestRoot.getTestResult(true);
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

  public synchronized TestRoot getTestRoot() {
//		swapIn(); //TODO: TestRoot should stay (e.g. for getTestRoot().getStatus())
    return fTestRoot;
  }

  /*
   * @see org.eclipse.jdt.junit.model.ITestRunSession#getJavaProject()
   */
  @Override
  public IV8Project getLaunchedProject() {
    return fProject;
  }

  public ITestKind getTestRunnerKind() {
    return fTestRunnerKind;
  }

  /**
   * @return the launch, or <code>null</code> iff this session was run externally
   */
  public ILaunch getLaunch() {
    return fLaunch;
  }

  public void setLaunch(ILaunch launch) {
    fLaunch = launch;
    ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
    if (launchConfiguration != null) {
      fTestRunName = launchConfiguration.getName();
      fTestRunnerKind = LaunchHelper.getTestRunnerKind(launchConfiguration);
    } else {
      fTestRunName = "";
      fTestRunName = fProject.getProject().getName();
      fTestRunnerKind = ITestKind.NULL;
    }

  }

  @Override
  public String getTestRunName() {
    return fTestRunName;
  }

  public String getTestRunPresent() {
    return fTestRunName + " " + DateFormat.getDateTimeInstance().format(new Date(fStartTime));
  }

  public int getErrorCount() {
    return fErrorCount;
  }

  public int getFailureCount() {
    return fFailureCount;
  }

  public int getAssumptionFailureCount() {
    return fAssumptionFailureCount;
  }

  public int getStartedCount() {
    return fStartedCount;
  }

  public int getIgnoredCount() {
    return fIgnoredCount;
  }

  public int getTotalCount() {
    return fTotalCount;
  }

  public long getStartTime() {
    return fStartTime;
  }

  /**
   * @return <code>true</code> iff the session has been stopped or terminated
   */
  public boolean isStopped() {
    return fIsStopped;
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
    return getStartTime() == 0 && fLaunch != null && !fLaunch.isTerminated();
  }

  public void removeSwapFile() {
    File swapFile = getSwapFile();
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
    File historyDir = TestViewerPlugin.core().getHistoryDirectory();
    String isoTime = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date(getStartTime())); //$NON-NLS-1$
    String swapFileName = isoTime + ".xml"; //$NON-NLS-1$
    return new File(historyDir, swapFileName);
  }

  public void stopTestRun() {
    if (isRunning() || !isKeptAlive()) {
      fIsStopped = true;
    }
  }

  /**
   * @return <code>true</code> iff the runtime VM of this test session is still alive
   */
  public boolean isKeptAlive() {
    return false;
  }

  /**
   * @return <code>true</code> iff this session has been started, but not ended nor stopped nor terminated
   */
  public boolean isRunning() {
    return fIsRunning;
  }

  public void registerTestFailureStatus(TestElement testElement) {
    if (!testElement.isAssumptionFailure()) {
      if (testElement.getStatus().isError()) {
        fErrorCount++;
      } else if (testElement.getStatus().isFailure()) {
        fFailureCount++;
      }
    }
  }

  public void registerTestEnded(TestElement testElement, boolean completed) {
    if (testElement instanceof TestCaseElement) {
      fTotalCount++;
      if (!completed) {
        return;
      }
      fStartedCount++;
      if (((TestCaseElement) testElement).isIgnored()) {
        fIgnoredCount++;
      }
      if (!testElement.getStatus().isErrorOrFailure()) {
        setStatus(testElement, TestStatus.OK);
      }
    }

    if (testElement.isAssumptionFailure()) {
      fAssumptionFailureCount++;
    }
  }

  private void setStatus(TestElement testElement, TestStatus status) {
    testElement.setStatus(status);
  }

  public ITestCaseElement[] getAllFailedTestElements() {
    ArrayList<ITestCaseElement> failures = new ArrayList<>();
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
    return fTestRoot == null ? Double.NaN : fTestRoot.getElapsedTimeInSeconds();
  }

  @Override
  public String getTestName() {
    return "Test session";
  }

  public String getIncludeTags() {
    if (fLaunch != null) {
      try {
        ILaunchConfiguration launchConfig = fLaunch.getLaunchConfiguration();
        if (launchConfig != null) {
          boolean hasIncludeTags = launchConfig.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_HAS_INCLUDE_TAGS, false);
          if (hasIncludeTags) {
            return launchConfig.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_INCLUDE_TAGS, EMPTY_STRING);
          }
        }
      } catch (CoreException ignore) {
      }
      return EMPTY_STRING;
    }
    return fIncludeTags;
  }

  public void setIncludeTags(String includeTags) {
    fIncludeTags = includeTags;
  }

  public String getExcludeTags() {
    if (fLaunch != null) {
      try {
        ILaunchConfiguration launchConfig = fLaunch.getLaunchConfiguration();
        if (launchConfig != null) {
          boolean hasExcludeTags = launchConfig.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_HAS_EXCLUDE_TAGS, false);
          if (hasExcludeTags) {
            return launchConfig.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_EXCLUDE_TAGS, EMPTY_STRING);
          }
        }
      } catch (CoreException e) {
        //ignore
      }
      return EMPTY_STRING;
    }
    return fExcludeTags;
  }

  public void setExcludeTags(String excludeTags) {
    fExcludeTags = excludeTags;
  }

  @Override
  public String toString() {
    return fTestRunName + " " + DateFormat.getDateTimeInstance().format(new Date(fStartTime)); //$NON-NLS-1$
  }
}