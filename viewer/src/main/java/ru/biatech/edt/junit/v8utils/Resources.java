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

package ru.biatech.edt.junit.v8utils;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import lombok.experimental.UtilityClass;
import org.eclipse.core.resources.IFile;
import ru.biatech.edt.junit.TestViewerPlugin;

@UtilityClass
public class Resources {
  public IFile getModuleResource(CommonModule commonModule) {
    Module module = commonModule.getModule();
    while (module == null) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        TestViewerPlugin.log().logError("Не удалось получить модуль для " + commonModule.getName(), e);
        break;
      }
      module = commonModule.getModule();
    }
    if (module != null) {
      return getModuleResource(module);
    } else {
      return null;
    }
  }

  public IFile getModuleResource(Module module) {
    return VendorServices.getResourceLookup().getPlatformResource(module);
  }
}
