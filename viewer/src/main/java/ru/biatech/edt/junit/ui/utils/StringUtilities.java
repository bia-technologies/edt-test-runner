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

import lombok.experimental.UtilityClass;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.ITestCaseElement;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.report.ErrorInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Класс-помощник для работы с текстом
 */
@UtilityClass
public class StringUtilities {
  public final String EMPTY_STRING = ""; //$NON-NLS-1$

  /**
   * Формирует текстовое представление стека ошибок
   * @param testElement объект содержащий стек
   * @return текстовое представление стека ошибок
   */
  public String getTrace(ITestElement testElement) {

    try (var stringWriter = new StringWriter(); var printWriter = new PrintWriter(stringWriter)) {
      printTraceText(printWriter, (ITestCaseElement) testElement);
      return stringWriter.toString();
    } catch (IOException ignored) {
    }
    return "<Failed to get trace>";
  }

  public boolean isNullOrEmpty(String string) {
    return string == null || string.isEmpty();
  }

  private void convertLineTerminators(String text, PrintWriter printWriter) {
    var stringReader = new StringReader(text);
    var bufferedReader = new BufferedReader(stringReader);
    String line;
    try {
    while ((line = bufferedReader.readLine()) != null) {
      printWriter.println(line);
    }
    } catch (Exception e) {
      TestViewerPlugin.log().logError(e);
    }
  }

  private void printTraceText(PrintWriter printWriter, ErrorInfo error) {
    if (!isNullOrEmpty(error.getMessage())) {
      convertLineTerminators(error.getMessage(), printWriter);
    }
    if (!isNullOrEmpty(error.getTrace())) {
      convertLineTerminators(error.getTrace(), printWriter);
    }
  }

  private void printTraceText(PrintWriter printWriter, ITestCaseElement testElement) throws IOException {
    testElement.getErrorsList()
        .forEach(e -> printTraceText(printWriter, e));
  }
}
