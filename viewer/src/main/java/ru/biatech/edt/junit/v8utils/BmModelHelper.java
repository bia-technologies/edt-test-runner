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

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.core.platform.IConfigurationAware;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BmModelHelper {
  public <T extends MdObject> T appendItem(IV8Project project, T item, String description) {

    var modelMng = VendorServices.getBmModelManager();
    return modelMng.getGlobalEditingContext()
        .execute(description, null, null, transaction -> {
          addItem(transaction.toTransactionObject(((IConfigurationAware) project).getConfiguration()), item);
          var fqn = VendorServices.getTopObjectFqnGenerator().generateStandaloneObjectFqn(item.eClass(), item.getName());
          transaction.attachTopObject(modelMng.getBmNamespace(project.getProject()), (IBmObject) item, fqn);
          return item;
        });
  }

  public void addItem(Configuration configuration, MdObject item) {
    if (item instanceof CommonModule) {
      configuration.getCommonModules().add((CommonModule) item);
    } else {
      throw new IllegalArgumentException("Не реализовано получение списка для " + item);
    }
  }
}
