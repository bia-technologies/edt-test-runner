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

package ru.biatech.edt.junit.v8utils;

import com._1c.g5.v8.dt.bm.index.emf.IBmEmfIndexManager;
import com._1c.g5.v8.dt.bm.index.emf.IBmEmfIndexProvider;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.metadata.mdclass.Method;
import org.eclipse.emf.ecore.EClass;

public class Resolver {

  private static Resolver instance;
  final IBmEmfIndexManager bmEmfIndexManager = Services.getBmEmfIndexManager();

  private static Resolver getInstance() {
    if (instance == null) {
      instance = new Resolver();
    }
    return instance;
  }

  public static MdObject findModule(IV8Project project, EClass moduleClass, String moduleName) {

    IBmEmfIndexProvider bmEmfIndexProvider = getInstance().bmEmfIndexManager.getEmfIndexProvider(project.getProject());

    return MdUtils.getConfigurationObject(moduleClass, moduleName, bmEmfIndexProvider);
  }

  public static IV8Project getProject(String name) {
    if (name == null || name.isEmpty())
      return null;
    return Services.getProjectManager().getProject(name);
  }


  public Method findMethod(MdObject module, String methodName) {
    return null;
  }
}
