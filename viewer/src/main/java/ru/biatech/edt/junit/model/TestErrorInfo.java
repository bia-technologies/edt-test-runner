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

import com.google.common.base.Strings;
import lombok.Value;

/**
 * Содержит информацию об ошибке
 */
@Value
public class TestErrorInfo implements ITraceable {
  ITestElement parent;
  TestStatus status;
  String message;
  String trace;
  String type;
  String actual;
  String expected;

  public String getTestName() {
    return parent.getTestName();
  }

  @Override
  public boolean hasTrace() {
    return !Strings.isNullOrEmpty(trace);
  }

  public boolean isComparisonFailure() {
    return getExpected() != null && getActual() != null;
  }
}
