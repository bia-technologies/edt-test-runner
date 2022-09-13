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

package ru.biatech.edt.junit.kinds;

import org.eclipse.emf.ecore.EClass;

public interface ITestResolver {

  MethodPositionInfo getMethodPositionInfo(String className);

  MethodPositionInfo getMethodPositionInfo(String className, int lineNumber);

  class MethodPositionInfo {
    private final String ownerName;
    private final EClass ownerClass;
    private final String methodName;
    private final int lineNumber;

    public MethodPositionInfo(EClass ownerClass, String ownerName, String methodName) {
      this(ownerClass, ownerName, methodName, -1);
    }

    public MethodPositionInfo(EClass ownerClass, String ownerName, String methodName, int lineNumber) {
      this.ownerName = ownerName;
      this.ownerClass = ownerClass;
      this.methodName = methodName;
      this.lineNumber = lineNumber;
    }

    public String getOwnerName() {
      return ownerName;
    }

    public EClass getOwnerClass() {
      return ownerClass;
    }

    public String getMethodName() {
      return methodName;
    }

    public int getLineNumber() {
      return lineNumber;
    }
  }
}
