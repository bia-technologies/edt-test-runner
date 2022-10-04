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

package ru.biatech.edt.junit.services;

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestFinder;
import ru.biatech.edt.junit.kinds.TestKindRegistry;
import ru.biatech.edt.junit.v8utils.Services;

import java.util.Collection;
import java.util.Collections;

public class TestsManager {
  public boolean isTestProject(IV8Project project) {
    return project instanceof IExtensionProject;
  }

  public boolean isTestModule(Module module) {
    var project = Services.getProjectManager().getProject(module);
    if (!isTestProject(project)) {
      return false;
    }

    return getFinder(module).isTestModule(module);
  }

  public boolean isTestModule(CommonModule module) {
    var project = Services.getProjectManager().getProject(module);
    if (!isTestProject(project)) {
      return false;
    }

    return getFinder(module).isTestModule(module.getModule());
  }

  public Collection<Method> getTestMethods(Module module) {
    try {
      return getFinder(module).findTestsInContainer(module, null);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return Collections.emptyList();
    }
  }

  public ITestFinder getFinder(EObject module) {
    var project = Services.getProjectManager().getProject(module);
    return TestKindRegistry.getContainerTestKind(project).getFinder();
  }
}
