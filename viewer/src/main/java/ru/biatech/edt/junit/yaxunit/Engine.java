/*******************************************************************************
 * Copyright (c) 2024 BIA-Technologies Limited Liability Company.
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

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.core.platform.IConfigurationProject;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import lombok.experimental.UtilityClass;
import org.eclipse.emf.ecore.EObject;
import ru.biatech.edt.junit.v8utils.BmModelHelper;
import ru.biatech.edt.junit.v8utils.Modules;
import ru.biatech.edt.junit.v8utils.Projects;
import ru.biatech.edt.junit.v8utils.VendorServices;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static ru.biatech.edt.junit.yaxunit.TestFinder.NAMING_SCHEME;

/**
 * Содержит сервисные методы-помощники для работы с тестами YAxUnit.
 * Перекликается с TestManager, но проще его. Модуль является заготовокой для будущего рефакторинга
 */
@UtilityClass
public class Engine {
  public boolean isTestProject(IV8Project project) {
    return project instanceof IExtensionProject;
  }

  public boolean isTestModule(Module module) {
    var project = Projects.getParentProject(module);
    if (!isTestProject(project)) {
      return false;
    }
    for (Method m : module.allMethods()) {
      if (isRegistrationTestsMethod(m)) {
        return true;
      }
    }
    return false;
  }

  public boolean isRegistrationTestsMethod(Method method) {
    return method.isExport() && method.getName().equalsIgnoreCase(Constants.REGISTRATION_METHOD_NAME);
  }

  public Stream<IExtensionProject> getTestProjects() {
    return Projects.getExtensions().stream();
  }

  public Stream<IExtensionProject> getTestProjects(EObject projectItem) {
    var v8projectManager = VendorServices.getProjectManager();
    var project = v8projectManager.getProject(projectItem);
    return getTestProjects(project);
  }

  public Stream<IExtensionProject> getTestProjects(IV8Project baseProject) {
    if (baseProject instanceof IConfigurationProject) {
      return Projects.getRelatedExtensions((IConfigurationProject) baseProject);
    } else {
      return Projects.getExtensions()
          .stream()
          .filter(p -> p != baseProject);
    }
  }

  public Stream<CommonModule> getTestSuites(IV8Project project, Module module) {
    return Arrays.stream(NAMING_SCHEME.getTestSuiteNames(module))
        .map(n -> Modules.findCommonModule(project, n))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  public Stream<CommonModule> getTestSuites(Module module) {
    var owner = (MdObject) module.getOwner();
    var parentProject = Projects.getParentProject(owner);
    return getTestProjects(parentProject)
        .flatMap(p -> getTestSuites(p, module));
  }

  public String getTestSuiteName(Module module) {
    return NAMING_SCHEME.getTestSuiteName(module);
  }

  public CommonModule createTestSuite(IExtensionProject project, Module baseModule) {
    var testSuiteModule = Modules.newCommonModule();
    var testSuiteName = getTestSuiteName(baseModule);
    testSuiteModule.setName(testSuiteName);

    var owner = baseModule.getOwner();

    if (owner instanceof CommonModule) {
      var commonModule = (CommonModule) owner;
      if (commonModule.isClientManagedApplication()) {
        testSuiteModule.setClientManagedApplication(true);
      }
      if (commonModule.isClientOrdinaryApplication()) {
        testSuiteModule.setClientOrdinaryApplication(true);
      }
      if (commonModule.isServer()) {
        testSuiteModule.setServer(true);
      }
    } else {
      testSuiteModule.setServer(true);
    }
    BmModelHelper.appendItem(project, testSuiteModule, Messages.TestsFactory_CreatingTestModule);

    return testSuiteModule;
  }
}
