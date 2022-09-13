/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.model;


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

  /**
   * Running states of a test.
   */
  final class ProgressState {
    /**
     * state that describes that the test element has not started
     */
    public static final ProgressState NOT_STARTED = new ProgressState("Not Started"); //$NON-NLS-1$
    /**
     * state that describes that the test element has is running
     */
    public static final ProgressState RUNNING = new ProgressState("Running"); //$NON-NLS-1$
    /**
     * state that describes that the test element has been stopped before being completed
     */
    public static final ProgressState STOPPED = new ProgressState("Stopped"); //$NON-NLS-1$
    /**
     * state that describes that the test element has completed
     */
    public static final ProgressState COMPLETED = new ProgressState("Completed"); //$NON-NLS-1$

    private final String fName;

    private ProgressState(String name) {
      fName = name;
    }

    @Override
    public String toString() {
      return fName;
    }
  }

  /**
   * Result states of a test.
   */
  final class Result {
    /**
     * state that describes that the test result is undefined
     */
    public static final Result UNDEFINED = new Result("Undefined"); //$NON-NLS-1$
    /**
     * state that describes that the test result is 'OK'
     */
    public static final Result OK = new Result("OK"); //$NON-NLS-1$
    /**
     * state that describes that the test result is 'Error'
     */
    public static final Result ERROR = new Result("Error"); //$NON-NLS-1$
    /**
     * state that describes that the test result is 'Failure'
     */
    public static final Result FAILURE = new Result("Failure"); //$NON-NLS-1$
    /**
     * state that describes that the test result is 'Ignored'
     */
    public static final Result IGNORED = new Result("Ignored"); //$NON-NLS-1$

    private final String fName;

    private Result(String name) {
      fName = name;
    }

    @Override
    public String toString() {
      return fName;
    }
  }

  /**
   * A failure trace of a test.
   * <p>
   * This class is not intended to be instantiated or extended by clients.
   */
  final class FailureTrace {
    private final String fActual;
    private final String fExpected;
    private final String fTrace;
    private final String fMessage;

    public FailureTrace(String trace, String message, String expected, String actual) {
      fActual = actual;
      fExpected = expected;
      fTrace = trace;
      fMessage = message;
    }

    public String getMessage() {
      return fMessage;
    }

    /**
     * Returns the failure stack trace.
     *
     * @return the failure stack trace
     */
    public String getTrace() {
      return fTrace;
    }

    /**
     * Returns the expected result or <code>null</code> if the trace is not a comparison failure.
     *
     * @return the expected result or <code>null</code> if the trace is not a comparison failure.
     */
    public String getExpected() {
      return fExpected;
    }

    /**
     * Returns the actual result or <code>null</code> if the trace is not a comparison failure.
     *
     * @return the actual result or <code>null</code> if the trace is not a comparison failure.
     */
    public String getActual() {
      return fActual;
    }
  }

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
   * <li>{@link Result#UNDEFINED}: the result is not yet evaluated</li>
   * <li>{@link Result#OK}: the test has succeeded</li>
   * <li>{@link Result#ERROR}: the test has returned an error</li>
   * <li>{@link Result#FAILURE}: the test has returned an failure</li>
   * <li>{@link Result#IGNORED}: the test has been ignored (skipped)</li>
   * </ul>
   *
   * @param includeChildren if <code>true</code>, the returned result is the combined
   *                        result of the test and its children (if it has any). If <code>false</code>,
   *                        only the test's result is returned.
   * @return returns one of {@link Result#UNDEFINED}, {@link Result#OK}, {@link Result#ERROR},
   * {@link Result#FAILURE} or {@link Result#IGNORED}. Clients should also prepare for other, new values.
   */
  Result getTestResult(boolean includeChildren);

  /**
   * Returns the failure trace of this test element or <code>null</code> if the test has not resulted in an error or failure.
   *
   * @return the failure trace of this test or <code>null</code>.
   */
  FailureTrace getFailureTrace();

  /**
   * Returns the parent test element container or <code>null</code> if the test element is the test run session.
   *
   * @return the parent test suite
   */
  ITestElementContainer getParentContainer();

  /**
   * Returns the test run session.
   *
   * @return the parent test run session.
   */
  ITestRunSession getTestRunSession();

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

}
