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

package ru.biatech.edt.junit.yaxunit;

import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.SimpleStatement;
import com._1c.g5.v8.dt.bsl.model.StaticFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import ru.biatech.edt.junit.kinds.ITestFinder;
import ru.biatech.edt.junit.v8utils.Services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TestFinder implements ITestFinder {

  static final String REGISTRATION_METHOD_NAME = "ИсполняемыеСценарии".toLowerCase();
  static final String REGISTRATION_MODULE_NAME = "ЮТТесты";
  static final Set<String> TEST_REGISTRATION_METHOD_NAMES = Set.of(
          "Тест".toLowerCase(),
          "ТестКлиент".toLowerCase(),
          "ТестСервер".toLowerCase());

  @Override
  public boolean isTestModule(Module module) {
    var project = Services.getProjectManager().getProject(module);
    if (!isTestProject(project)) {
      return false;
    }
    for (Method m : module.allMethods()) {
      if (isRootTestMethod(m)) {
        return true;
      }
    }
    return false;
  }

  public boolean isRootTestMethod(Method method) {
    return method.isExport() && method.getName().equalsIgnoreCase(REGISTRATION_METHOD_NAME);
  }

  @Override
  public Collection<Method> findTestsInContainer(Module module, IProgressMonitor pm) throws CoreException {
    Map<String, Method> moduleMethods = new HashMap<>();
    Method rootMethod = null;
    for (Method method : module.allMethods()) {
      if (!method.isExport()) {
        continue;
      }
      moduleMethods.put(method.getName().toLowerCase(), method);
      if (isRootTestMethod(method)) {
        rootMethod = method;
      }
    }
    if (rootMethod == null) {
      return Collections.emptyList();
    }
    List<String> names = new ArrayList<>();
    rootMethod.getStatements()
            .stream()
            .filter(SimpleStatement.class::isInstance)
            .map(SimpleStatement.class::cast)
            .map(st -> st.getLeft() instanceof Invocation ? st.getLeft() : st.getRight())
            .filter(Invocation.class::isInstance)
            .map(Invocation.class::cast)
            .filter(invocation -> TEST_REGISTRATION_METHOD_NAMES.contains(invocation.getMethodAccess().getName().toLowerCase()))
            .forEach(invocation -> names.addAll(fill(invocation)));

    List<Method> result = names.stream()
                                  .map(String::toLowerCase)
                                  .filter(moduleMethods::containsKey)
                                  .map(moduleMethods::get)
                                  .collect(Collectors.toList());
    if (!result.isEmpty()) {
      result.add(rootMethod);
    }
    return result;
  }

  public boolean isTestProject(IV8Project project) {
    return project instanceof IExtensionProject;
  }

  List<String> fill(Invocation invocation) {
    List<String> names = new ArrayList<>();
    if (fill(invocation, names)) {
      return names;
    } else {
      return Collections.emptyList();
    }
  }

  boolean fill(Invocation invocation, List<String> names) {
    var ma = invocation.getMethodAccess();
    String methodName = invocation.getMethodAccess().getName();
    if (TEST_REGISTRATION_METHOD_NAMES.contains(methodName.toLowerCase()) && !invocation.getParams().isEmpty()) {
      String param = String.join("", ((StringLiteral) invocation.getParams().get(0)).lines(false));
      names.add(param);
    }
    Expression source = null;
    if (ma instanceof DynamicFeatureAccess) {
      source = ((DynamicFeatureAccess) ma).getSource();
    } else if (ma instanceof StaticFeatureAccess) {
      source = ma;
    }

    if (source instanceof StaticFeatureAccess) {
      return ((StaticFeatureAccess) source).getName().equalsIgnoreCase(REGISTRATION_MODULE_NAME);
    } else if (source instanceof Invocation) {
      return fill((Invocation) source, names);
    } else {
      return false;
    }

  }
}
