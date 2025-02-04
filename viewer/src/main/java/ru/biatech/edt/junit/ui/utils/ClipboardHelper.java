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

package ru.biatech.edt.junit.ui.utils;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import ru.biatech.edt.junit.TestViewerPlugin;

/**
 * Класс-помощник работы с буфером обмена
 */
@UtilityClass
public class ClipboardHelper {
  @Getter(lazy = true)
  private final Clipboard clipboard = new Clipboard(TestViewerPlugin.ui().getDisplay());

  /**
   * Вставляет текст в буфер обмена
   * @param text сохраняемый в буфер обмена текст
   */
  public void pasteToClipboard(String text) {
    var plainTextTransfer = TextTransfer.getInstance();
    getClipboard().setContents(
        new String[]{text},
        new Transfer[]{plainTextTransfer});
  }
}
