/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *     Xavier Coulon <xcoulon@redhat.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102512 - [JUnit] test method name cut off before (
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

import org.eclipse.core.runtime.Assert;

public abstract class TestElement implements ITestElement {
  private final TestSuiteElement fParent;
  private final String fContext;
  /**
   * The display name of the test element, can be <code>null</code>. In that case, use
   * {@link TestElement#fTestName fTestName}.
   */
  private final String fDisplayName;
  /**
   * The array of method parameter types (as given by
   * org.junit.platform.engine.support.descriptor.MethodSource.getMethodParameterTypes()) if
   * applicable, otherwise <code>null</code>.
   */
  private final String[] fParameterTypes;
  /**
   * The unique ID of the test element which can be <code>null</code> as it is applicable to JUnit 5
   * and above.
   */
  private final String fUniqueId;
  /**
   * Running time in seconds. Contents depend on the current {@link #getProgressState()}:
   * <ul>
   * <li>{@link ProgressState#NOT_STARTED}: {@link Double#NaN}</li>
   * <li>{@link ProgressState#RUNNING}: negated start time</li>
   * <li>{@link ProgressState#STOPPED}: elapsed time</li>
   * <li>{@link ProgressState#COMPLETED}: elapsed time</li>
   * </ul>
   */
  /* default */ double fTime = Double.NaN;
  private String fTestName;
  private TestStatus fStatus;
  private String fTrace;
  private String fExpected;
  private String fActual;
  private String fMessage;

  private boolean fAssumptionFailed;

  /**
   * @param parent         the parent, can be <code>null</code>
   * @param testName       the test name
   * @param displayName    the test display name, can be <code>null</code>
   * @param parameterTypes the array of method parameter types (as given by
   *                       org.junit.platform.engine.support.descriptor.MethodSource.getMethodParameterTypes())
   *                       if applicable, otherwise <code>null</code>
   * @param uniqueId       the unique ID of the test element, can be <code>null</code> as it is applicable
   *                       to JUnit 5 and above
   */
  public TestElement(TestSuiteElement parent, String testName, String displayName, String[] parameterTypes, String uniqueId, String context) {
    Assert.isNotNull(testName);
    fParent = parent;
    fTestName = testName;
    fDisplayName = displayName;
    fParameterTypes = parameterTypes;
    fUniqueId = uniqueId;
    fStatus = TestStatus.NOT_RUN;
    fContext = context;
    if (parent != null) parent.addChild(this);
  }

  @Override
  public ProgressState getProgressState() {
    return getStatus().convertToProgressState();
  }

  @Override
  public TestResult getTestResult(boolean includeChildren) {
    if (fAssumptionFailed) {
      return TestResult.IGNORED;
    }
    return getStatus().convertToResult();
  }

  @Override
  public ITestRunSession getTestRunSession() {
    return getRoot().getTestRunSession();
  }

  @Override
  public ITestElementContainer getParentContainer() {
    if (fParent instanceof TestRoot) {
      return getTestRunSession();
    }
    return fParent;
  }

  /**
   * @return the parent suite, or <code>null</code> for the root
   */
  public TestSuiteElement getParent() {
    return fParent;
  }

  public String getTestName() {
    return fTestName;
  }

  /**
   * Возращает имя контекста исполнения теста
   * @return имя контекста исполнения теста
   */
  public String getContext() {
    return fContext;
  }

  public void setName(String name) {
    fTestName = name;
  }

  public void setStatus(TestStatus status, String message, String trace, String expected, String actual) {
    fTrace = concat(fTrace, trace);
    fExpected = concat(fExpected, expected);
    fActual = concat(fActual, actual);
    fMessage = concat(fMessage, message);
    setStatus(status);
  }

  public TestStatus getStatus() {
    return fStatus;
  }

  public void setStatus(TestStatus status) {
    if (status == TestStatus.RUNNING) {
      fTime = -System.currentTimeMillis() / 1000d;
    } else if (status.convertToProgressState() == ProgressState.COMPLETED) {
      if (fTime < 0) { // assert ! Double.isNaN(fTime)
        double endTime = System.currentTimeMillis() / 1000.0d;
        fTime = endTime + fTime;
      }
    }

    fStatus = status;
    TestSuiteElement parent = getParent();
    if (parent != null) parent.childChangedStatus(this, status);
  }

  public String getTrace() {
    return fTrace;
  }

  public String getMessage() {
    return fMessage;
  }

  public String getExpected() {
    return fExpected;
  }

  public String getActual() {
    return fActual;
  }

  public boolean isComparisonFailure() {
    return fExpected != null && fActual != null;
  }

  public String getClassName() {
    return Factory.extractClassName(getTestName());
  }

  public TestRoot getRoot() {
    return getParent().getRoot();
  }

  @Override
  public double getElapsedTimeInSeconds() {
    if (Double.isNaN(fTime) || fTime < 0.0d) {
      return Double.NaN;
    }

    return fTime;
  }

  public void setElapsedTimeInSeconds(double time) {
    fTime = time;
  }

  public void setAssumptionFailed(boolean assumptionFailed) {
    fAssumptionFailed = assumptionFailed;
  }

  public boolean isAssumptionFailure() {
    return fAssumptionFailed;
  }

  @Override
  public String toString() {
    return getProgressState() + " - " + getTestResult(true); //$NON-NLS-1$
  }

  /**
   * Returns the display name of the test. Can be <code>null</code>. In that case, use
   * {@link TestElement#getTestName() getTestName()}.
   *
   * @return the test display name, can be <code>null</code>
   */
  public String getDisplayName() {
    return fDisplayName;
  }

  /**
   * @return the array of method parameter types (as given by
   * org.junit.platform.engine.support.descriptor.MethodSource.getMethodParameterTypes()) if
   * applicable, otherwise <code>null</code>
   */
  public String[] getParameterTypes() {
    return fParameterTypes;
  }

  /**
   * Returns the unique ID of the test element. Can be <code>null</code> as it is applicable to JUnit
   * 5 and above.
   *
   * @return the unique ID of the test, can be <code>null</code>
   */
  public String getUniqueId() {
    return fUniqueId;
  }

  String concat(String s1, String s2) {
    if (s1 != null && s2 != null) {
      return s1 + s2;
    } else if (s1 != null) {
      return s1;
    } else {
      return s2;
    }
  }

}
