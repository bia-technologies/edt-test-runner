/*******************************************************************************
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

public class Factory {
  public static TestCaseElement createTestCase(TestSuiteElement parent, String testName, String displayName, boolean isDynamicTest, String[] parameterTypes, String uniqueId, String context) {
    return new TestCaseElement(parent, testName, displayName, isDynamicTest, trimParameterTypes(parameterTypes), uniqueId, context);
  }

  public static TestSuiteElement createTestSuite(TestSuiteElement parent, String testName, String displayName, String[] parameterTypes, String uniqueId, String context) {
    return new TestSuiteElement(parent, testName, 0, displayName, trimParameterTypes(parameterTypes), uniqueId, context);
  }
  static String extractClassName(String testNameString) {
    if (testNameString.startsWith("[") && testNameString.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
      // a group of parameterized tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=102512
      return testNameString;
    }

    int index = testNameString.lastIndexOf('(');

    if (index >= 0) {
      int end = testNameString.lastIndexOf(')');
      testNameString = testNameString.substring(index + 1, end > index ? end : testNameString.length());
    }
    testNameString = testNameString.replace('$', '.'); // see bug 178503
    return testNameString;
  }


  private static String[] trimParameterTypes(String[] parameterTypes) {
    if (parameterTypes != null && parameterTypes.length > 1) {
      for (int i = 0; i < parameterTypes.length; i++)
        parameterTypes[i] = parameterTypes[i].trim();
    }
    return parameterTypes;
  }
}
