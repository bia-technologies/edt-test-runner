/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

/**
 * A listener interface for observing the execution of a test session (initial run and reruns).
 */
public interface ITestSessionListener {
  // TODO Удалить неиспользуемые события
  /**
   * A test run has started.
   */
  void sessionStarted();

  /**
   * A test run has ended.
   *
   * @param elapsedTime the total elapsed time of the test run
   */
  void sessionEnded(long elapsedTime);

  /**
   * A test run has been stopped prematurely.
   *
   * @param elapsedTime the time elapsed before the test run was stopped
   */
  void sessionStopped(long elapsedTime);

  /**
   * The VM instance performing the tests has terminated.
   */
  void sessionTerminated();

  /**
   * A test has been added to the plan.
   *
   * @param testElement the test
   */
  void testAdded(TestElement testElement);

  /**
   * All test have been added and running begins
   */
  void runningBegins();

  /**
   * An individual test has started.
   *
   * @param testCaseElement the test
   */
  void testStarted(TestCaseElement testCaseElement);

  /**
   * An individual test has ended.
   *
   * @param testCaseElement the test
   */
  void testEnded(TestCaseElement testCaseElement);

  /**
   * An individual test has failed with a stack trace.
   *
   * @param testElement the test
   * @param status      the outcome of the test; one of
   *                    {@link TestStatus#ERROR} or
   *                    {@link TestStatus#FAILURE}
   * @param trace       the stack trace
   * @param expected    expected value
   * @param actual      actual value
   */
  void testFailed(TestElement testElement, TestStatus status, String trace, String expected, String actual);

  /**
   * An individual test has been rerun.
   *
   * @param testCaseElement the test
   * @param status          the outcome of the test that was rerun; one of
   *                        {@link TestStatus#OK}, {@link TestStatus#ERROR}, or {@link TestStatus#FAILURE}
   * @param trace           the stack trace in the case of abnormal termination,
   *                        or the empty string if none
   * @param expectedResult  expected value
   * @param actualResult    actual value
   */
  void testRerun(TestCaseElement testCaseElement, TestStatus status, String trace, String expectedResult, String actualResult);

  /**
   * @return <code>true</code> if the test run session can be swapped to disk although
   * this listener is still installed
   */
  boolean acceptsSwapToDisk();

}
