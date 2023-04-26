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

import org.eclipse.swt.graphics.Color;
import ru.biatech.edt.junit.ui.Colors;

/**
 * Помощник формирования HTML
 */
public class HTMLRender {
  private final StringBuilder buffer = new StringBuilder();

  /**
   * Выполняет замену уветовых алиасов на значения темы оформления
   *
   * @param styleSheet Строка содержащяя css
   * @return Строка с установленными цветами темы оформления
   */
  public static String replaceColors(String styleSheet) {
    return styleSheet
        .replace("HOVER_BG_COLOR", hexColor(Colors.HOVER_BG_COLOR))
        .replace("SELECTION_BG_COLOR", hexColor(Colors.SELECTION_BG_COLOR))
        .replace("SELECTION_FG_COLOR", hexColor(Colors.SELECTION_FG_COLOR))
        .replace("BG_COLOR", hexColor(Colors.BG_COLOR))
        .replace("FG_COLOR", hexColor(Colors.FG_COLOR));
  }

  /**
   * Открывает новый тег с переданным именем
   *
   * @param name имя тега
   * @return генератор html
   */
  public HTMLRender start(String name) {
    buffer.append("<").append(name).append(">");
    return this;
  }

  /**
   * Открывает новый тег с переданным именем и классом
   *
   * @param name      имя тега
   * @param className имя класса
   * @return генератор html
   */
  public HTMLRender start(String name, String className) {
    buffer.append("<").append(name)
        .append(" class=\"").append(className).append("\">");
    return this;
  }

  /**
   * Закрывает тег
   *
   * @param name имя тега
   * @return генератор html
   */
  public HTMLRender end(String name) {
    buffer.append("</").append(name).append(">");
    return this;
  }

  /**
   * Вставляет произвольный текст
   *
   * @param text добавляемый текст
   * @return генератор html
   */
  public HTMLRender text(String text) {
    buffer.append(text);
    return this;
  }

  /**
   * Возвращает сформированный html фрагмент
   *
   * @return сформированный html фрагмент
   */
  public String getContent() {
    return buffer.toString();
  }

  private static String hexColor(Color rgb) {
    var buffer = new StringBuilder(7);
    buffer.append('#');
    appendAsHexString(buffer, rgb.getRed());
    appendAsHexString(buffer, rgb.getGreen());
    appendAsHexString(buffer, rgb.getBlue());
    return buffer.toString();
  }

  private static void appendAsHexString(StringBuilder buffer, int intValue) {
    var hexValue = Integer.toHexString(intValue);
    if (hexValue.length() == 1) buffer.append('0');
    buffer.append(hexValue);
  }
}
