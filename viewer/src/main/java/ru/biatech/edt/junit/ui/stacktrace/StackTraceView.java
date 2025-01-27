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

package ru.biatech.edt.junit.ui.stacktrace;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.ui.stacktrace.events.Listener;

/**
 * Интерфейс для взаимодействия с компонентом отображения ошибок теста
 */
public interface StackTraceView {

  /**
   * Выводит опиание ошибки теста
   * @param testElement описание теста
   */
  void viewFailure(ITestElement testElement);

  /**
   * Очищает элемент
   */
  void clear();

  /**
   * Возвращяет выбранный элемент ошибки (строка стека и тд)
   * @return выбранный элемент
   */
  Object getSelected();

  /**
   * Возвращяет описание ошибки по выбранному элементу
   * @return описание ошибки
   */
  ErrorInfo getSelectedError();

  /**
   * Уничтожение элмента
   */
  void dispose();

  /**
   * Регистрирует подписку на изменеине выбранного элемента
   * @param listener обработчик события
   */
  void addSelectionChangedListeners(Listener listener);

  /**
   * Регистрирует подписку открытя (действия по двойному клику)
   * @param listener обработчик события
   */
  void addOpenListeners(Listener listener);

  /**
   * Регистрирует контекстое меню
   * @param menuManager управляющий меню
   */
  void registerMenu(MenuManager menuManager);

  /**
   * Служебный метода, возвращяющий элемент UI
   * @return элемент UI
   */
  Control getContent();
}
