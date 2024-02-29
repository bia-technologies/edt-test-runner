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

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.AccountingRegister;
import com._1c.g5.v8.dt.metadata.mdclass.AccumulationRegister;
import com._1c.g5.v8.dt.metadata.mdclass.BasicDbObject;
import com._1c.g5.v8.dt.metadata.mdclass.CalculationRegister;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.metadata.mdclass.Constant;
import com._1c.g5.v8.dt.metadata.mdclass.DataProcessor;
import com._1c.g5.v8.dt.metadata.mdclass.Enum;
import com._1c.g5.v8.dt.metadata.mdclass.InformationRegister;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassFactory;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.metadata.mdclass.Report;
import lombok.experimental.UtilityClass;
import org.eclipse.emf.ecore.EClass;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Помощник для работы с модулями
 */
@UtilityClass
public class Modules {

  /**
   * Выполняет поиск общего модуля в проекте по имени
   *
   * @param project    проект, содержащий общий модуль
   * @param moduleName имя общего модуля
   * @return ссылка на общий модуль
   */
  public Optional<CommonModule> findCommonModule(IV8Project project, String moduleName) {
    return Optional.ofNullable((CommonModule) findModule(project, MdClassPackage.Literals.COMMON_MODULE, moduleName));
  }

  /**
   * Выполнетя поиск объекта метаданных по классу и имени
   *
   * @param project     проект, содержащий объект
   * @param moduleClass класс метаданных объекта
   * @param moduleName  имя объекта
   * @return объект метаданных
   */
  public static MdObject findModule(IV8Project project, EClass moduleClass, String moduleName) {
    if (project == null) {
      return null;
    }

    var bmEmfIndexProvider = VendorServices.getBmEmfIndexManager().getEmfIndexProvider(project.getProject());
    return MdUtils.getConfigurationObject(moduleClass, moduleName, bmEmfIndexProvider);
  }

  /**
   * Возвращает список модулей объекта метаданных
   *
   * @param owner объект метаданных
   * @return список модулей
   */
  public List<Module> getObjectModules(MdObject owner) {
    if (owner instanceof CommonModule) {
      var mdObj = (CommonModule) owner;
      return List.of(mdObj.getModule());
    } else if (owner instanceof BasicDbObject) {
      var mdObj = (BasicDbObject) owner;
      return List.of(mdObj.getManagerModule(),
          mdObj.getObjectModule());
    } else if (owner instanceof InformationRegister) {
      var mdObj = (AccountingRegister) owner;
      return List.of(mdObj.getManagerModule(),
          mdObj.getRecordSetModule());
    } else if (owner instanceof AccountingRegister) {
      var mdObj = (AccountingRegister) owner;
      return List.of(mdObj.getManagerModule(),
          mdObj.getRecordSetModule());
    } else if (owner instanceof AccumulationRegister) {
      var mdObj = (AccumulationRegister) owner;
      return List.of(mdObj.getManagerModule(),
          mdObj.getRecordSetModule());
    } else if (owner instanceof CalculationRegister) {
      var mdObj = (CalculationRegister) owner;
      return List.of(mdObj.getManagerModule(),
          mdObj.getRecordSetModule());
    } else if (owner instanceof Enum) {
      var mdObj = (Enum) owner;
      return List.of(mdObj.getManagerModule());
    } else if (owner instanceof DataProcessor) {
      var mdObj = (DataProcessor) owner;
      return List.of(mdObj.getManagerModule(),
          mdObj.getObjectModule());
    } else if (owner instanceof Report) {
      var mdObj = (Report) owner;
      return List.of(mdObj.getManagerModule(),
          mdObj.getObjectModule());
    } else if (owner instanceof Constant) {
      var mdObj = (Constant) owner;
      return List.of(mdObj.getManagerModule(),
          mdObj.getValueManagerModule());
    } else if (owner instanceof Configuration) {
      var mdObj = (Configuration) owner;
      return List.of(mdObj.getSessionModule(),
          mdObj.getExternalConnectionModule(),
          mdObj.getManagedApplicationModule(),
          mdObj.getOrdinaryApplicationModule());
    } else {
      return List.of();
    }
  }

  public CommonModule newCommonModule() {
    var newCommonModule = MdClassFactory.eINSTANCE.createCommonModule();
    newCommonModule.setUuid(UUID.randomUUID());
    return newCommonModule;
  }
}
