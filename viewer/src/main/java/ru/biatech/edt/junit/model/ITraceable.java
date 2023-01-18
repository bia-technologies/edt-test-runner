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

package ru.biatech.edt.junit.model;

/**
 * Интерфейс маркер для объекта содержащего стек ошибок
 */
public interface ITraceable {
  /**
   * Возвращает имя владельца ошибки
   * @return имя владельца ошибки
   */
  String getTestName();

  /**
   * Возврашает признак наличия стека ошибок
   * @return признак наличия стека ошибок
   */
  boolean hasTrace();
}