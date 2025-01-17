/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package ru.biatech.edt.junit.model;

/**
 * Статус исполнения теста
 */
public enum TestStatus {
  RUNNING_ERROR("RUNNING_ERROR"), //$NON-NLS-1$
  RUNNING_FAILURE("RUNNING_FAILURE"), //$NON-NLS-1$
  RUNNING("RUNNING"), //$NON-NLS-1$

  ERROR("ERROR"), //$NON-NLS-1$
  FAILURE("FAILURE"), //$NON-NLS-1$
  SKIPPED("SKIPPED"), //$NON-NLS-1$
  OK("OK"), //$NON-NLS-1$
  NOT_RUN("NOT_RUN"); //$NON-NLS-1$

  private final String name;

  TestStatus(String name) {
    this.name = name;
  }

  public static TestStatus combineStatus(TestStatus one, TestStatus two) {
    TestStatus progress = combineProgress(one, two);
    TestStatus error = combineError(one, two);
    return combineProgressAndErrorStatus(progress, error);
  }

  /* error state predicates */

  private static TestStatus combineProgress(TestStatus one, TestStatus two) {
    if (one.isNotRun() && two.isNotRun()) return NOT_RUN;
    else if ((one.isDone() && two.isDone()) || (!one.isRunning() && !two.isRunning())) { // One done, one not-run -> a parent failed and its children are not run
      return OK;
    } else return RUNNING;
  }

  private static TestStatus combineError(TestStatus one, TestStatus two) {
    if (one.isError() || two.isError()) return ERROR;
    else if (one.isFailure() || two.isFailure()) return FAILURE;
    else if (one.isSkipped() || two.isSkipped()) return SKIPPED;
    else return OK;
  }

  private static TestStatus combineProgressAndErrorStatus(TestStatus progress, TestStatus error) {
    if (progress.isDone()) {
      if (error.isError()) return ERROR;
      if (error.isFailure()) return FAILURE;
      if (error.isSkipped()) return SKIPPED;
      return OK;
    }

    if (progress.isNotRun()) {
      return NOT_RUN;
    }

    if (error.isError()) return RUNNING_ERROR;
    if (error.isFailure()) return RUNNING_FAILURE;
    return RUNNING;
  }

  @Override
  public String toString() {
    return name;
  }

  /* progress state predicates */

  public boolean isOK() {
    return this == OK || this == RUNNING || this == NOT_RUN;
  }

  public boolean isFailure() {
    return this == FAILURE || this == RUNNING_FAILURE;
  }

  public boolean isError() {
    return this == ERROR || this == RUNNING_ERROR;
  }

  public boolean isErrorOrFailure() {
    return isError() || isFailure();
  }

  public boolean isNotRun() {
    return this == NOT_RUN;
  }

  public boolean isRunning() {
    return this == RUNNING || this == RUNNING_FAILURE || this == RUNNING_ERROR;
  }

  public boolean isDone() {
    return this == OK || this == FAILURE || this == ERROR || this == SKIPPED;
  }

  public boolean isSkipped() {
    return this == SKIPPED;
  }

  public TestResult convertToResult() {
    if (isNotRun()) return TestResult.UNDEFINED;
    if (isError()) return TestResult.ERROR;
    if (isFailure()) return TestResult.FAILURE;
    if (isSkipped()) return TestResult.SKIPPED;
    if (isRunning()) return TestResult.UNDEFINED;

    return TestResult.OK;
  }

  public ProgressState convertToProgressState() {
    if (isRunning()) {
      return ProgressState.RUNNING;
    }
    if (isDone()) {
      return ProgressState.COMPLETED;
    }
    return ProgressState.NOT_STARTED;
  }
}
