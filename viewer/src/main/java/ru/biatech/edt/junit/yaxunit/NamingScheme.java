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

package ru.biatech.edt.junit.yaxunit;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.ModuleType;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.emf.ecore.EClass;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Схема наименования тестовых модулей
 */
public class NamingScheme {
  private static final Map<EClass, String> MDO_CLASS_PREFIXES = createPrefixes();
  private static final Map<String, EClass> PREFIXES = MDO_CLASS_PREFIXES.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
  private static final Map<ModuleType, String> MODULE_SUFFIXES = createSuffixes();
  private static final Map<String, ModuleType> SUFFIXES = MODULE_SUFFIXES.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
  private static final String SEPARATOR = "_"; //$NON-NLS-1$

  private static Map<EClass, String> createPrefixes() {
    var prefixes = new HashMap<EClass, String>();
    prefixes.put(MdClassPackage.Literals.COMMON_MODULE, "ОМ"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.ACCOUNTING_REGISTER, "РБ"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.ACCUMULATION_REGISTER, "РН"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.CALCULATION_REGISTER, "РР"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.INFORMATION_REGISTER, "РС"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.BUSINESS_PROCESS, "БП"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.CATALOG, "Спр"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.CHART_OF_ACCOUNTS, "ПС"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.CHART_OF_CALCULATION_TYPES, "ПВР"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.CHART_OF_CHARACTERISTIC_TYPES, "ПВХ"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.DOCUMENT, "Док"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.ENUM, "Пер"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.EXCHANGE_PLAN, "ПО"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.TASK, "Зад"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.DATA_PROCESSOR, "Обр"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.REPORT, "Отч"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.BOT, "Бот"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.INTEGRATION_SERVICE, "Инт"); //$NON-NLS-1$
    prefixes.put(MdClassPackage.Literals.EXTERNAL_DATA_SOURCE, "ВИД"); //$NON-NLS-1$
    return prefixes;
  }

  private static Map<ModuleType, String> createSuffixes() {
    var suffixes = new HashMap<ModuleType, String>();
    suffixes.put(ModuleType.MANAGER_MODULE, "ММ"); //$NON-NLS-1$
    suffixes.put(ModuleType.OBJECT_MODULE, "МО"); //$NON-NLS-1$
    suffixes.put(ModuleType.RECORDSET_MODULE, "НЗ"); //$NON-NLS-1$

    return suffixes;
  }

  /**
   * На основании имени тестового модуля возвращает информацию о тестируемом объекте
   *
   * @param testModuleName имя тестового модуля
   * @return информация о тестируемом объекте
   */
  public ModuleInfo getBaseModuleName(String testModuleName) {
    var chunks = testModuleName.split(SEPARATOR);

    if (chunks.length == 1) {
      return new ModuleInfo(null, null, chunks[0]);
    }

    var mdClass = PREFIXES.getOrDefault(chunks[0], null);
    var moduleType = chunks.length == 3 ? SUFFIXES.getOrDefault(chunks[2], null) : null;
    return new ModuleInfo(mdClass, moduleType, chunks[1]);
  }

  /**
   * Возвращает соответствующее схеме имя тестового модуля для объекта
   *
   * @param module объект, для которого нужен тестовый модуль
   * @return имя тестового модуля, сформированное по схеме
   */
  public String getTestSuiteName(Module module) {
    var prefix = MDO_CLASS_PREFIXES.getOrDefault(module.getOwner().eClass(), null);
    var suffix = MODULE_SUFFIXES.getOrDefault(module.getModuleType(), null);

    StringBuilder sb = new StringBuilder();
    if (prefix != null) {
      sb.append(prefix).append(SEPARATOR);
    }
    sb.append(((MdObject) module.getOwner()).getName());
    if (suffix != null) {
      sb.append(SEPARATOR).append(suffix);
    }

    return sb.toString();
  }

  /**
   * Возвращает все возможные имена тестовых модулей соответствующие схеме
   *
   * @param object объект, для которого нужен тестовый модуль
   * @return имена тестовых модулей
   */
  public String[] getTestSuiteNames(MdObject object) {
    var prefixe = MDO_CLASS_PREFIXES.getOrDefault(object.eClass(), null);
    if (prefixe == null) {
      return new String[0];
    }
    return Stream
        .concat(Stream.of(MessageFormat.format("{0}{1}{2}", prefixe, SEPARATOR, object.getName())), //$NON-NLS-1$
            SUFFIXES.keySet()
                .stream().map(s -> MessageFormat.format("{0}{1}{2}{1}{3}", prefixe, SEPARATOR, object.getName(), s))) //$NON-NLS-1$
        .toArray(String[]::new);
  }

  public String[] getTestSuiteNames(Module module) {
    var prefix = MDO_CLASS_PREFIXES.getOrDefault(module.getOwner().eClass(), null);
    var suffix = MODULE_SUFFIXES.getOrDefault(module.getModuleType(), null);

    String[] values = new String[suffix == null ? 1 : 2];
    StringBuilder sb = new StringBuilder();
    if (prefix != null) {
      sb.append(prefix).append(SEPARATOR);
    }
    sb.append(((MdObject) module.getOwner()).getName());

    if (suffix != null) {
      values[1] = sb.toString();
      sb.append(SEPARATOR).append(suffix);
      values[0] = sb.toString();
    } else {
      values[0] = sb.toString();
    }

    return values;
  }

  @Value
  @AllArgsConstructor
  public static class ModuleInfo {
    EClass mdClass;
    ModuleType moduleType;
    String objectName;
  }
}
