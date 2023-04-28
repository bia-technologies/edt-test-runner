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

import com._1c.g5.v8.dt.bsl.model.ModuleType;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.emf.ecore.EClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Схема наименования тестовых модулей
 */
public class NamingScheme {

  private static final List<Item> schema = createScheme();

  /**
   * Возвращает соответствующее схеме имя тестового модуля для объекта
   * @param object объект, для которого нужен тестовый модуль
   * @return имя тестового модуля, сформированное по схеме
   */
  public String getTestModuleName(MdObject object) {
    var templates = getTemplates(object);

    if (templates.length > 0) {
      return String.format(templates[0], object.getName());
    } else {
      return null;
    }
  }

  /**
   * Возвращает все возможные имена тестовых модулей соответствующие схеме
   * @param object объект, для которого нужен тестовый модуль
   * @return имена тестовых модулей
   */
  public String[] getTestModuleNames(MdObject object) {
    var templates = getTemplates(object);
    var names = new String[templates.length];

    for (int i = 0; i < names.length; i++) {
      names[i] = String.format(templates[i], object.getName());
    }
    return names;
  }

  /**
   * На основании имени тестового модуля возвращает информацию о тестируемом объекте
   * @param testModuleName имя тестового модуля
   * @return информация о тестируемом объекте
   */
  public ModuleInfo getBaseModuleName(String testModuleName) {
    for (var item : schema) {
      for (var template : item.getTemplates()) {
        var matcher = getPattern(template).matcher(testModuleName);
        if (matcher.matches()) {
          return new ModuleInfo(item.mdClass, null, matcher.group(1));
        }
      }
    }
    return null;
  }

  private Pattern getPattern(String template) {
    return Pattern.compile(template.replace("%s", "(.+)"));
  }

  private String[] getTemplates(MdObject object) {
    var objClass = object.eClass();
    var templates = new String[0];
    for (var item : schema) {
      if (item.getMdClass() == objClass) {
        templates = item.getTemplates();
        break;
      }
    }
    return templates;
  }

  private static List<Item> createScheme() {
    var result = new ArrayList<Item>();

    result.add(new Item(MdClassPackage.Literals.COMMON_MODULE, "ОМ_%s"));

    result.add(new Item(MdClassPackage.Literals.ACCOUNTING_REGISTER, "РБ_%s_MM", "РБ_%s_НЗ", "РБ_%s"));
    result.add(new Item(MdClassPackage.Literals.ACCUMULATION_REGISTER, "РН_%s_MM", "РН_%s_НЗ", "РН_%s"));
    result.add(new Item(MdClassPackage.Literals.CALCULATION_REGISTER, "РР_%s_MM", "РР_%s_НЗ", "РР_%s"));
    result.add(new Item(MdClassPackage.Literals.INFORMATION_REGISTER, "РС_%s_MM", "РС_%s_НЗ", "РС_%s"));

    result.add(new Item(MdClassPackage.Literals.BUSINESS_PROCESS, "БП_%s_ММ", "БП_%s_МО", "БП_%s"));
    result.add(new Item(MdClassPackage.Literals.CATALOG, "Спр_%s_ММ", "Спр_%s_МО", "Спр_%s"));
    result.add(new Item(MdClassPackage.Literals.CHART_OF_ACCOUNTS, "ПС_%s_ММ", "ПС_%s_МО", "ПС_%s"));
    result.add(new Item(MdClassPackage.Literals.CHART_OF_CALCULATION_TYPES, "ПВР_%s_ММ", "ПВР_%s_МО", "ПВР_%s"));
    result.add(new Item(MdClassPackage.Literals.CHART_OF_CHARACTERISTIC_TYPES, "ПВХ_%s_ММ", "ПВХ_%s_МО", "ПВХ_%s"));
    result.add(new Item(MdClassPackage.Literals.DOCUMENT, "Док_%s_ММ", "Док_%s_МО", "Док_%s"));
    result.add(new Item(MdClassPackage.Literals.ENUM, "Пер_%s_ММ", "Пер_%s_МО", "Пер_%s"));
    result.add(new Item(MdClassPackage.Literals.EXCHANGE_PLAN, "ПО_%s_ММ", "ПО_%s_МО", "ПО_%s"));
    result.add(new Item(MdClassPackage.Literals.TASK, "Зад_%s_ММ", "Зад_%s_МО", "Зад_%s"));

    result.add(new Item(MdClassPackage.Literals.DATA_PROCESSOR, "Обр_%s_ММ", "Обр_%s_МО", "Обр_%s"));
    result.add(new Item(MdClassPackage.Literals.REPORT, "Отч_%s_ММ", "Отч_%s_МО", "Отч_%s"));
    return result;
  }

  @Value
  private static class Item {
    EClass mdClass;
    String[] templates;

    private Item(EClass mdClass, String... templates) {
      this.mdClass = mdClass;
      this.templates = templates;
    }
  }

  @Value
  @AllArgsConstructor
  public static class ModuleInfo {
    EClass mdClass;
    ModuleType moduleType;
    String objectName;
  }
}
