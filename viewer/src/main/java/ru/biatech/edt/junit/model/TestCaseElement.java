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
 *     Xavier Coulon <xcoulon@redhat.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102512 - [JUnit] test method name cut off before (
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

import lombok.NonNull;


public class TestCaseElement extends TestElement implements ITestCaseElement {

  private boolean fIgnored;
  private final boolean fIsDynamicTest;

  public TestCaseElement(@NonNull TestSuiteElement parent, @NonNull String testName, String displayName, boolean isDynamicTest, String[] parameterTypes, String uniqueId, String context) {
    super(parent, testName, displayName, parameterTypes, uniqueId, context);
    fIsDynamicTest = isDynamicTest;
  }

  @Override
  public String getTestMethodName() {
    String testName = getTestName();
    int index = testName.lastIndexOf('(');
    if (index > 0)
      return testName.substring(0, index);
    index = testName.indexOf('@');
    if (index > 0)
      return testName.substring(0, index);
    return testName;
  }

  /**
   * {@inheritDoc}
   *
   * @see ru.biatech.edt.junit.model.ITestCaseElement#getTestClassName()
   */
  @Override
  public String getTestClassName() {
    return getClassName();
  }

  /*
   * @see ru.biatech.edt.junit.model.TestElement#getTestResult(boolean)
   * @since 3.6
   */
  @Override
  public TestResult getTestResult(boolean includeChildren) {
    if (fIgnored) {
      return TestResult.IGNORED;
    } else {
      return super.getTestResult(includeChildren);
    }
  }

  public void setIgnored(boolean ignored) {
    fIgnored = ignored;
  }

  public boolean isIgnored() {
    return fIgnored;
  }

  @Override
  public String toString() {
    return "TestCase: " + getTestClassName() + "." + getTestMethodName() + " : " + super.toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public boolean isDynamicTest() {
    return fIsDynamicTest;
  }
}
