/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.model;


import ru.biatech.edt.junit.model.report.ErrorInfo;

import java.util.stream.Stream;
/**
 * Common protocol for test elements.
 * This set consists of {@link ITestCaseElement} , {@link ITestSuiteElement} and {@link ITestRunSession}
 *
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.3
 */
public interface ITestElement {

  ITestSuiteElement getParent();

  TestStatus getStatus();
  /**
   * Returns the progress state of this test element.
   * <ul>
   * <li>{@link ProgressState#NOT_STARTED}: the test has not yet started</li>
   * <li>{@link ProgressState#RUNNING}: the test is currently running</li>
   * <li>{@link ProgressState#STOPPED}: the test has stopped before being completed</li>
   * <li>{@link ProgressState#COMPLETED}: the test (and all its children) has completed</li>
   * </ul>
   *
   * @return returns one of {@link ProgressState#NOT_STARTED}, {@link ProgressState#RUNNING},
   * {@link ProgressState#STOPPED} or {@link ProgressState#COMPLETED}.
   */
  ProgressState getProgressState();

  /**
   * Returns the result of the test element.
   * <ul>
   * <li>{@link TestResult#UNDEFINED}: the result is not yet evaluated</li>
   * <li>{@link TestResult#OK}: the test has succeeded</li>
   * <li>{@link TestResult#ERROR}: the test has returned an error</li>
   * <li>{@link TestResult#FAILURE}: the test has returned an failure</li>
   * <li>{@link TestResult#SKIPPED}: the test has been ignored (skipped)</li>
   * </ul>
   *
   * @param includeChildren if <code>true</code>, the returned result is the combined
   *                        result of the test and its children (if it has any). If <code>false</code>,
   *                        only the test's result is returned.
   * @return returns one of {@link TestResult#UNDEFINED}, {@link TestResult#OK}, {@link TestResult#ERROR},
   * {@link TestResult#FAILURE} or {@link TestResult#SKIPPED}. Clients should also prepare for other, new values.
   */
  TestResult getResultStatus(boolean includeChildren);

  /**
   * Returns the estimated total time elapsed in seconds while executing this test element. The
   * total time for a test suite includes the time used for all tests in that suite. The total
   * time for a test session includes the time used for all tests in that session.
   * <p>
   * <strong>Note:</strong> The elapsed time is only valid for
   * {@link ProgressState#COMPLETED} test elements.
   * </p>
   *
   * @return total execution time for the test element in seconds, or {@link Double#NaN} if
   * the state of the element is not {@link ProgressState#COMPLETED}
   * @since 3.4
   */
  double getElapsedTimeInSeconds();

  /**
   * Return test element name
   * @return test element name
   */
  String getName();

  Stream<ErrorInfo> getErrorsList();
}
