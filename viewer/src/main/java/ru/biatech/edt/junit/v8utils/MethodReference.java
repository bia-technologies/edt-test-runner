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

package ru.biatech.edt.junit.v8utils;

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import lombok.Getter;
import ru.biatech.edt.junit.ui.utils.StringUtilities;

/**
 * Ссылка на метод, включает в себя ссылку на модуль.
 * Позволяет при проблемах поиска метода открыть пользователю модуль
 */
public class MethodReference {
  @Getter
  private final Module module;

  private Method method;
  private String methodName;

  public MethodReference(Method method, Module module) {
    this.method = method;
    this.module = module;
  }

  public MethodReference(Module module, String methodName) {
    this.module = module;
    this.methodName = methodName;
  }

  public Method getMethod() {
    if (method != null) {
      return method;
    } else if (!StringUtilities.isNullOrEmpty(methodName)) {
      return method = Methods.findMethod(module, methodName);
    } else {
      return null;
    }
  }
}
