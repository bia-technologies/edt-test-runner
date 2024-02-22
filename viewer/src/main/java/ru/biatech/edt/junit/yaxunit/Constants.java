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

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Содержит константные значения YAxUnit, которые могут быть использованы в разных пакетах
 */
@UtilityClass
public class Constants {
  public final String ADD_TEST_METHOD = "ДобавитьТест"; //$NON-NLS-1$
  public final String ADD_CLIENT_TEST_METHOD = "ДобавитьКлиентскийТест"; //$NON-NLS-1$
  public final String ADD_SERVER_TEST_METHOD = "ДобавитьСерверныйТест"; //$NON-NLS-1$
  public final Set<String> TEST_REGISTRATION_METHOD_NAMES = Set.of(
      "Тест".toLowerCase(), //$NON-NLS-1$
      "ТестКлиент".toLowerCase(), //$NON-NLS-1$
      "ТестСервер".toLowerCase(), //$NON-NLS-1$
      ADD_TEST_METHOD.toLowerCase(),
      ADD_CLIENT_TEST_METHOD.toLowerCase(),
      ADD_SERVER_TEST_METHOD.toLowerCase()
  );
  /**
   * Имя метода регистрации тестов в yaxUnit
   */
  public final String REGISTRATION_METHOD_NAME = "ИсполняемыеСценарии".toLowerCase(); //$NON-NLS-1$
  public final String REGISTRATION_MODULE_NAME = "ЮТТесты"; //$NON-NLS-1$
  public final String RUN_PARAMETERS = "RunUnitTests="; //$NON-NLS-1$
  public final String PARAMETERS_FILE_NAME = "xUnitParams.json"; //$NON-NLS-1$
  public final String REPORT_FORMAT = "jUnit"; //$NON-NLS-1$
  public final String NEW_TEST_DEFAULT_NAME = "НовыйТест"; //$NON-NLS-1$
}
