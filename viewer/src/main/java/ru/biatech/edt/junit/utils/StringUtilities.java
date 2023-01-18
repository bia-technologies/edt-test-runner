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

package ru.biatech.edt.junit.utils;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import ru.biatech.edt.junit.model.ITraceable;
import ru.biatech.edt.junit.model.TestCaseElement;
import ru.biatech.edt.junit.model.TestErrorInfo;

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

  /**
   * Формирует текстовое представление стека ошибок
   * @param testElement объект содержащий стек
   * @return текстовое представление стека ошибок
   */
  public String getTrace(ITraceable testElement) {

    try (var stringWriter = new StringWriter(); var printWriter = new PrintWriter(stringWriter)) {
      if (testElement instanceof TestErrorInfo) {
        printTraceText(printWriter, (TestErrorInfo) testElement);
      } else if (testElement instanceof TestCaseElement) {
        printTraceText(printWriter, (TestCaseElement) testElement);
      }
      return stringWriter.toString();
    } catch (IOException ignored) {
    }
    return "<Failed to get trace>";
  }

  private void convertLineTerminators(String text, PrintWriter printWriter) throws IOException {
    var stringReader = new StringReader(text);
    var bufferedReader = new BufferedReader(stringReader);
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      printWriter.println(line);
    }
  }

  private void printTraceText(PrintWriter printWriter, TestErrorInfo error) throws IOException {
    if (!Strings.isNullOrEmpty(error.getMessage())) {
      convertLineTerminators(error.getMessage(), printWriter);
    }
    if (error.hasTrace()) {
      convertLineTerminators(error.getTrace(), printWriter);
    }
  }

  private void printTraceText(PrintWriter printWriter, TestCaseElement testElement) throws IOException {
    for (var error : testElement.getErrorsList()) {
      printTraceText(printWriter, error);
    }
  }
}
