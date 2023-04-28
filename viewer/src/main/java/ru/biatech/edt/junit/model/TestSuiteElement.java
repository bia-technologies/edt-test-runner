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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

import java.util.ArrayList;
import java.util.List;


public class TestSuiteElement extends TestElement implements ITestSuiteElement {

  private final List<TestElement> fChildren;
  private TestStatus fChildrenStatus;

  public TestSuiteElement(TestSuiteElement parent, String testName, String displayName, String[] parameterTypes, String uniqueId, String context) {
    super(parent, testName, displayName, parameterTypes, uniqueId, context);
    fChildren = new ArrayList<>();
  }

  @Override
  public TestResult getTestResult(boolean includeChildren) {
    if (includeChildren) {
      return getStatus().convertToResult();
    } else {
      return super.getStatus().convertToResult();
    }
  }

  @Override
  public String getSuiteTypeName() {
    return getClassName();
  }

  @Override
  public ITestElement[] getChildren() {
    return fChildren.toArray(new ITestElement[fChildren.size()]);
  }

  public void addChild(TestElement child) {
    fChildren.add(child);
  }

  public void removeChild(TestElement child) {
    fChildren.remove(child);
  }

  @Override
  public TestStatus getStatus() {
    TestStatus suiteStatus = getSuiteStatus();
    if (fChildrenStatus != null) {
      // must combine children and suite status here, since failures can occur e.g. in @AfterClass
      return TestStatus.combineStatus(fChildrenStatus, suiteStatus);
    } else {
      return suiteStatus;
    }
  }

  private TestStatus getCumulatedStatus() {
    TestElement[] children = fChildren.toArray(new TestElement[fChildren.size()]); // copy list to avoid concurreny problems
    if (children.length == 0)
      return getSuiteStatus();

    TestStatus cumulated = children[0].getStatus();

    for (int i = 1; i < children.length; i++) {
      TestStatus childStatus = children[i].getStatus();
      cumulated = TestStatus.combineStatus(cumulated, childStatus);
    }
    return cumulated;
  }

  public TestStatus getSuiteStatus() {
    return super.getStatus();
  }

  public void childChangedStatus(TestElement child, TestStatus childStatus) {
    int childCount = fChildren.size();
    if (child == fChildren.get(0) && childStatus.isRunning()) {
      // is first child, and is running -> copy status
      internalSetChildrenStatus(childStatus);
      return;
    }
    TestElement lastChild = fChildren.get(childCount - 1);
    if (child == lastChild) {
      if (childStatus.isDone()) {
        // all children done, collect cumulative status
        internalSetChildrenStatus(getCumulatedStatus());
        return;
      }
      // go on (child could e.g. be a TestSuiteElement with RUNNING_FAILURE)

    } else if (!lastChild.getStatus().isNotRun()) {
      // child is not last, but last child has been run -> child has been rerun or is rerunning
      internalSetChildrenStatus(getCumulatedStatus());
      return;
    }

    // finally, set RUNNING_FAILURE/ERROR if child has failed but suite has not failed:
    if (childStatus.isFailure()) {
      if (fChildrenStatus == null || !fChildrenStatus.isErrorOrFailure()) {
        internalSetChildrenStatus(TestStatus.RUNNING_FAILURE);
        return;
      }
    } else if (childStatus.isError()) {
      if (fChildrenStatus == null || !fChildrenStatus.isError()) {
        internalSetChildrenStatus(TestStatus.RUNNING_ERROR);
        return;
      }
    }
  }

  private void internalSetChildrenStatus(TestStatus status) {
    if (fChildrenStatus == status)
      return;

    if (status == TestStatus.RUNNING) {
      if (elapsedTimeInSeconds >= 0.0d) {
        // re-running child: ignore change
      } else {
        elapsedTimeInSeconds = -System.currentTimeMillis() / 1000d;
      }
    } else if (status.convertToProgressState() == ProgressState.COMPLETED) {
      if (elapsedTimeInSeconds < 0) { // assert ! Double.isNaN(fTime)
        double endTime = System.currentTimeMillis() / 1000d;
        elapsedTimeInSeconds += endTime;
      }
    }

    fChildrenStatus = status;
    TestSuiteElement parent = getParent();
    if (parent != null)
      parent.childChangedStatus(this, getStatus());
  }

  @Override
  public String toString() {
    return "TestSuite: " + getTestName() + " : " + super.toString() + " (" + fChildren.size() + ")";   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

}
