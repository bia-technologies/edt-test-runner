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

import java.util.stream.Stream;

public class TestSuiteElement extends TestSuite<TestCaseElement> implements ITestSuiteElement {
  @Getter
  private TestStatus status;
  private TestStatus childrenStatus;

  @Override
  public ITestSuiteElement getParent() {
    return null;
  }

  @Override
  public ProgressState getProgressState() {
    return null;
  }

  @Override
  public TestResult getResultStatus(boolean includeChildren) {
    if (includeChildren && !isEmpty()) {
      return getCommonStatus().convertToResult();
    } else {
      return status.convertToResult();
    }
  }

  private TestStatus getCommonStatus() {
    return TestStatus.combineStatus(childrenStatus, status);
  }

  @Override
  public double getElapsedTimeInSeconds() {
    return getTime();
  }

  @Override
  public Stream<ErrorInfo> getErrorsList() {
    return Stream.empty();
  }

  @Override
  public ITestElement[] getChildren() {
    return getTestcase();
  }

  @Override
  public String getSuiteTypeName() {
    return getClassName() != null ? getClassName() : getName();
  }

  @Override
  public String getDisplayName() {
    return getName() != null ? getName() : getClassName();
  }


  private boolean isEmpty() {
    return getError() == null || getError().length == 0;
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

  void init() {
    for (var test : getTestcase()) {
      test.init(this);
    }

    status = isEmpty() ? TestStatus.OK : TestStatus.ERROR;
    childrenStatus = getCumulatedStatus();
  }
}
