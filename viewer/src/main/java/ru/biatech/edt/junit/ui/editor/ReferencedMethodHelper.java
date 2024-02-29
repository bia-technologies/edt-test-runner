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

package ru.biatech.edt.junit.ui.editor;

import lombok.experimental.UtilityClass;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.v8utils.BslSourceDisplay;
import ru.biatech.edt.junit.v8utils.MethodReference;

import java.util.List;

/**
 * Класс-помощник для обработки переходов к связаннму тесту/тестируемому методу
 */
@UtilityClass
public class ReferencedMethodHelper {

  /**
   * Выполняет переход к переданному методу
   * Если в списке передано несколько объектов - отображается диалог выбора
   * Если в списке один объект - происходит открытие модуля и позиционирование на методе
   *
   * @param list список ссылок методы
   */
  public void displayMethod(List<MethodReference> list, String notFoundMessage) {
    MethodReference reference;
    if (list.isEmpty()) {
      TestViewerPlugin.log().logError(notFoundMessage);
      return;
    } else if (list.size() == 1) {
      reference = list.get(0);
    } else {
      reference = Dialogs.selectMethodReference(list);
    }

    if (reference != null) {
      BslSourceDisplay.INSTANCE.displayBslSource(reference, TestViewerPlugin.ui().getActivePage(), true);
    }
  }
}
