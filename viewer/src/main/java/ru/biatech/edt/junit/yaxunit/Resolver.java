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

import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import ru.biatech.edt.junit.kinds.ITestResolver;

import java.util.HashMap;
import java.util.Map;

public class Resolver implements ITestResolver {

  static final Map<String, String> testModulePrefix = getTestModulePrefixes();

  @Override
  public MethodPositionInfo getMethodPositionInfo(String className) {
    String[] chunks = className.split("\\.");
    return new MethodPositionInfo(MdClassPackage.Literals.COMMON_MODULE, chunks[0], chunks[1]);
  }

  @Override
  public MethodPositionInfo getMethodPositionInfo(String className, int lineNumber) {
    String[] chunks = className.split("\\.");
    return new MethodPositionInfo(MdClassPackage.Literals.COMMON_MODULE, chunks[0], chunks[1], lineNumber);
  }

  private static Map<String, String> getTestModulePrefixes() {
    Map<String, String> prefixes = new HashMap<>();
    prefixes.put("РБ", "AccountingRegisters");
    prefixes.put("РН", "AccumulationRegisters");
    prefixes.put("БП", "BusinessProcesses");
    prefixes.put("РР", "CalculationRegisters");
    prefixes.put("Спр", "Catalogs");
    prefixes.put("ПС", "ChartsOfAccounts");
    prefixes.put("ПВР", "ChartsOfCalculationTypes");
    prefixes.put("ПВХ", "ChartsOfCharacteristicTypes");
    prefixes.put("ОМ", "CommonModules");
    prefixes.put("Обр", "DataProcessors");
    prefixes.put("Док", "Documents");
    prefixes.put("Пер", "Enums");
    prefixes.put("ПО", "ExchangePlans");
    prefixes.put("РС", "InformationRegisters");
    prefixes.put("Отч", "Reports");
    prefixes.put("Зад", "Tasks");

    return prefixes;
  }
}
