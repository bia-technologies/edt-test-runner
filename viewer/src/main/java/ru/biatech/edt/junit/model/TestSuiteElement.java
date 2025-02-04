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
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.model.report.TestSuite;

import java.util.Arrays;
import java.util.stream.Stream;

public class TestSuiteElement extends TestSuite<TestCaseElement> implements ITestSuiteElement {
  @Getter
  private TestStatus status;
  private TestStatus childrenStatus;

  public TestSuiteElement() {
    testcase = new TestCaseElement[0];
  }

  @Override
  public ITestSuiteElement getParent() {
    return null;
  }

  @Override
  public TestResult getResultStatus(boolean includeChildren) {
    if (includeChildren && isNotEmpty()) {
      return TestStatus.combineStatus(childrenStatus, status).convertToResult();
    } else {
      return status.convertToResult();
    }
  }

  @Override
  public double getElapsedTimeInSeconds() {
    return getTime();
  }

  @Override
  public Stream<ErrorInfo> getErrorsList() {
    return getError() == null ? Stream.empty() : Arrays.stream(getError());
  }

  @Override
  public ITestElement[] getChildren() {
    return getTestcase();
  }

  @Override
  public String getDisplayName() {
    return getName() != null ? getName() : getClassName();
  }


  private boolean withErrors() {
    return getError() != null && getError().length != 0;
  }

  private boolean isNotEmpty() {
    return testcase != null && testcase.length != 0;
  }

  private TestStatus getCumulatedStatus() {
    var children = this.getTestcase();
    if (children.length == 0)
      return getStatus();

    var cumulated = children[0].getStatus();

    for (int i = 1; i < children.length; i++) {
      cumulated = TestStatus.combineStatus(cumulated, children[i].getStatus());
    }
    return cumulated;
  }

  /**
   * Заполняет необходимые поля. Вызывается после полного заполнения.
   */
  void init() {
    failures = 0;
    errors = 0;
    skipped = 0;
    tests = getTestcase().length;
    status = TestStatus.NOT_RUN;
    childrenStatus = TestStatus.OK;

    for (var test : getTestcase()) {
      test.init(this);
      switch (test.getStatus()) {
        case ERROR:
          errors++;
          break;
        case FAILURE:
          failures++;
          break;
        case SKIPPED:
          skipped++;
          break;
      }
    }

    status = withErrors() ? TestStatus.ERROR : TestStatus.OK;
    childrenStatus = getCumulatedStatus();
  }
}
