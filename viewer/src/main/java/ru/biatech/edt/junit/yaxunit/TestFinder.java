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
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.util.EcoreUtil;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestFinder;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.v8utils.MethodReference;
import ru.biatech.edt.junit.v8utils.Modules;
import ru.biatech.edt.junit.v8utils.Projects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Предоставляет инструментарий для работы с тестами YAxUnit
 */
public class TestFinder implements ITestFinder {

  /**
   * Имя метода регистрации тестов в yaxUnit
   */
  public static final String REGISTRATION_METHOD_NAME = "ИсполняемыеСценарии".toLowerCase();

  private static final String REGISTRATION_MODULE_NAME = "ЮТТесты";

  private static final Set<String> TEST_REGISTRATION_METHOD_NAMES = Set.of(
      "Тест".toLowerCase(),
      "ТестКлиент".toLowerCase(),
      "ТестСервер".toLowerCase(),
      "ДобавитьТест".toLowerCase(),
      "ДобавитьКлиентскийТест".toLowerCase(),
      "ДобавитьСерверныйТест".toLowerCase()
  );

  private static final NamingScheme NAMING_SCHEME = new NamingScheme();

  /**
   * @inheritDoc
   */
  @Override
  public boolean isTestProject(IV8Project project) {
    return project instanceof IExtensionProject;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean isTestModule(Module module) {
    var project = Projects.getParentProject(module);
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

  /**
   * @inheritDoc
   */
  @Override
  public boolean isTestMethod(Module module, String methodName) {
    // TODO Оптимизировать
    try {
      return findTests(module, null)
          .stream()
          .anyMatch(m -> m.getName().equalsIgnoreCase(methodName));
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return false;
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public Collection<Method> findTests(Module module, IProgressMonitor pm) throws CoreException {
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

  /**
   * @inheritDoc
   */
  @Override
  public List<MethodReference> findTestsFor(Module module, String methodName) {
    var owner = (MdObject) module.getOwner();
    var names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    var result = new ArrayList<MethodReference>();
    names.addAll(Arrays.asList(NAMING_SCHEME.getTestModuleNames(owner)));

    var testModules = LaunchHelper.getTestExtensions()
        .stream()
        .filter(this::isTestProject)
        .flatMap(LaunchHelper::getTestModulesStream)
        .filter(tm -> names.contains(tm.getName()))
        .collect(Collectors.toList());

    for (var testModule : testModules) {
      try {
        findTests(testModule.getModule(), null)
            .stream()
            .filter(tm -> tm.getName().contains(methodName))
            .map(tm -> new MethodReference(tm, testModule.getModule()))
            .forEach(result::add);
      } catch (CoreException e) {
        TestViewerPlugin.log().logError(e);
      }
    }

    if (result.isEmpty() && !testModules.isEmpty()) {
      testModules.stream().map(m -> new MethodReference(null, m.getModule())).forEach(result::add);
    }

    return result;
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<MethodReference> findTestedMethod(Module testModule, String testMethodName) {
    var testModuleOwner = (MdObject) testModule.getOwner();
    return findTestedMethod(testModuleOwner.getName(), testMethodName);
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<MethodReference> findTestedMethod(String testModuleName, String testMethodName) {
    var result = new ArrayList<MethodReference>();

    var baseModuleInfo = NAMING_SCHEME.getBaseModuleName(testModuleName);

    var baseProject = Projects.getConfiguration();
    var baseModule = Modules.findModule(baseProject, baseModuleInfo.getMdClass(), baseModuleInfo.getObjectName());

    if (baseModule != null) {
      baseModule = (MdObject) EcoreUtil.resolve(baseModule, baseProject.getConfiguration());
    }

    var testModules = Modules.getObjectModules(baseModule);
    for (var testModule : testModules) {
      var method = findMethods(testModule, testMethodName);

      if (method.isPresent()) {
        result.add(new MethodReference(method.get(), testModule));
      } else {
        result.add(new MethodReference(null, testModule));
      }
    }
    return result;
  }

  private boolean isRootTestMethod(Method method) {
    return method.isExport() && method.getName().equalsIgnoreCase(REGISTRATION_METHOD_NAME);
  }

  private boolean fill(Invocation invocation, List<String> names) {
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

  private List<String> fill(Invocation invocation) {
    List<String> names = new ArrayList<>();
    if (fill(invocation, names)) {
      return names;
    } else {
      return Collections.emptyList();
    }
  }

  private Optional<Method> findMethods(Module module, String methodName) {
    var methods = module.allMethods();
    return methods.stream()
        .filter(m -> m.getName().equalsIgnoreCase(methodName))
        .findFirst();
  }
}
