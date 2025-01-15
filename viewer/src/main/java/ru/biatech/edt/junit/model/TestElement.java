/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *     Xavier Coulon <xcoulon@redhat.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102512 - [JUnit] test method name cut off before (
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class TestElement implements ITestElement, ITraceable {

  /**
   * The parent suite, or <code>null</code> for the root
   */
  private final TestSuiteElement parent;

  /**
   * имя контекста исполнения теста
   */
  private final String context;

  /**
   * The display name of the test element, can be <code>null</code>. In that case, use
   * {@link TestElement#testName fTestName}.
   */
  private final String displayName;

  /**
   * The array of method parameter types (as given by
   * org.junit.platform.engine.support.descriptor.MethodSource.getMethodParameterTypes()) if
   * applicable, otherwise <code>null</code>.
   */
  private final String[] parameterTypes;

  /**
   * The unique ID of the test element which can be <code>null</code> as it is applicable to JUnit 5
   * and above.
   */
  private final String uniqueId;

  /**
   * Running time in seconds. Contents depend on the current {@link #getProgressState()}:
   * <ul>
   * <li>{@link ProgressState#NOT_STARTED}: {@link Double#NaN}</li>
   * <li>{@link ProgressState#RUNNING}: negated start time</li>
   * <li>{@link ProgressState#STOPPED}: elapsed time</li>
   * <li>{@link ProgressState#COMPLETED}: elapsed time</li>
   * </ul>
   */
  @Setter
  protected double elapsedTimeInSeconds = Double.NaN;

  /**
   * Test element name
   */
  private final String testName;

  /**
   * Статус исполнения теста
   */
  private TestStatus status;

  /**
   * Список ошибок теста
   */
  private List<TestErrorInfo> errorsList = Collections.emptyList();

  @Setter
  private boolean assumptionFailure;

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
  public TestElement(TestSuiteElement parent, @NonNull String testName, String displayName, String[] parameterTypes, String uniqueId, String context) {
    this.parent = parent;
    this.testName = testName;
    this.displayName = displayName;
    this.parameterTypes = parameterTypes;
    this.uniqueId = uniqueId;
    this.context = context;
    status = TestStatus.NOT_RUN;
    if (parent != null) {
      parent.addChild(this);
    }
  }

  @Override
  public ProgressState getProgressState() {
    return status.convertToProgressState();
  }

  @Override
  public TestResult getTestResult(boolean includeChildren) {
    if (assumptionFailure) {
      return TestResult.IGNORED;
    }
    return status.convertToResult();
  }

  public void pushErrorInfo(TestStatus status, String message, String type, String trace, String expected, String actual) {
    var first = errorsList.isEmpty();
    if (first) {
      errorsList = new ArrayList<>();
    }

    errorsList.add(new TestErrorInfo(this, status, message, trace, type, actual, expected));

    if (first) {
      setStatus(status);
    } else {
      var cumulated = this.status == null ? status : this.status;

      for (var error : errorsList) {
        cumulated = TestStatus.combineStatus(cumulated, error.getStatus());
      }

      setStatus(cumulated);
    }

  }

  public void setStatus(TestStatus status) {
    if (status == TestStatus.RUNNING) {
      elapsedTimeInSeconds = -System.currentTimeMillis() / 1000d;
    } else if (status.convertToProgressState() == ProgressState.COMPLETED) {
      if (elapsedTimeInSeconds < 0) { // assert ! Double.isNaN(fTime)
        var endTime = System.currentTimeMillis() / 1000.0d;
        elapsedTimeInSeconds = endTime + elapsedTimeInSeconds;
      }
    }

    this.status = status;
    var parent = getParent();
    if (parent != null) {
      parent.childChangedStatus(this, status);
    }
  }

  @Override
  public boolean hasTrace() {
    return !errorsList.isEmpty();
  }

  public String getClassName() {
    return Factory.extractClassName(getTestName());
  }

  public TestRoot getRoot() {
    return getParent().getRoot();
  }

  @Override
  public double getElapsedTimeInSeconds() {
    if (Double.isNaN(elapsedTimeInSeconds) || elapsedTimeInSeconds < 0.0d) {
      return Double.NaN;
    }

    return elapsedTimeInSeconds;
  }

  @Override
  public String toString() {
    return getProgressState() + " - " + getTestResult(true); //$NON-NLS-1$
  }
}
