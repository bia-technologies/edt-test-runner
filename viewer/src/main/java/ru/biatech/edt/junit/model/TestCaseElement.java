/*******************************************************************************
 * Copyright (c) 2025 BIA-Technologies Limited Liability Company.
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

import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.model.report.TestCase;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
@NoArgsConstructor
public class TestCaseElement extends TestCase implements ITestCaseElement {

  private ITestSuiteElement parent;
  private TestStatus status;

  public TestCaseElement(TestCase testCase) {
    this.className = testCase.getClassName();
    this.name = testCase.getName();
    this.time = testCase.getTime();
    this.skipped = testCase.getSkipped();
    this.error = testCase.getError();
    this.failure = testCase.getFailure();
    this.systemOut = testCase.getSystemOut();
    this.systemErr = testCase.getSystemErr();
    this.context = testCase.getContext();
  }

  public String getDisplayName() {
    return getName();
  }

  @Override
  public Stream<ErrorInfo> getErrorsList() {
    return Stream.of(getError(), getFailure(), getSkipped())
        .filter(Objects::nonNull)
        .flatMap(Arrays::stream);
  }

  @Override
  public String getMethodName() {
    String testName = getName();
    int index = testName.lastIndexOf('(');
    if (index > 0) {
      return testName.substring(0, index);
    } else {
      return testName;
    }
  }

  @Override
  public ProgressState getProgressState() {
    return null;
  }

  @Override
  public TestResult getResultStatus(boolean includeChildren) {
    return status.convertToResult();
  }

  @Override
  public double getElapsedTimeInSeconds() {
    return this.getTime();
  }

  void init(TestSuiteElement suite) {
    parent = suite;
    computeStatus();
  }

  private void computeStatus() {
    if (getError() != null) {
      status = TestStatus.ERROR;
    } else if (getFailure() != null) {
      status = TestStatus.FAILURE;
    } else if (getSkipped() != null) {
      status = TestStatus.SKIPPED;
    } else {
      status = TestStatus.OK;
    }
  }
}
