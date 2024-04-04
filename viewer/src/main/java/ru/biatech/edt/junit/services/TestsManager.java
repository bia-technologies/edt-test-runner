/*******************************************************************************
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.services;

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import lombok.experimental.UtilityClass;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.XtextEditor;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestFinder;
import ru.biatech.edt.junit.kinds.TestKindRegistry;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.ui.editor.EditorHelper;
import ru.biatech.edt.junit.v8utils.MethodReference;
import ru.biatech.edt.junit.v8utils.Modules;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;

/**
 * Класс помощник для работы с тестами
 */
@UtilityClass
public class TestsManager {

  private final char METHOD_NAME_SEPARATOR = '.';
  private final String METHOD_NAME_SEPARATOR_PATTERN = "\\.";
  /**
   * Проверяет, является ли проектом с тестами
   *
   * @param project проект для проверки
   * @return признак, это тестовый проект
   */
  public boolean isTestProject(IV8Project project) {
    return getFinder(project).isTestProject(project);
  }

  /**
   * Проверяет, является ли модулем с тестами
   *
   * @param module модуль для проверки
   * @return признак, это тестовый модуль
   */
  public boolean isTestModule(Module module) {
    return getFinder(module).isTestModule(module);
  }

  /**
   * Проверяет, является ли модулем с тестами
   *
   * @param commonModule модуль для проверки
   * @return признак, это тестовый модуль
   */
  public boolean isTestModule(CommonModule commonModule) {
    return getFinder(commonModule).isTestModule(commonModule.getModule());
  }

  /**
   * Проверяет, является ли метод модуля тестом
   *
   * @param module     модуль, которому принадлежит метод
   * @param methodName имя метода
   * @return признак, это тест
   */
  public boolean isTestMethod(Module module, String methodName) {
    return getFinder(module).isTestMethod(module, methodName);
  }

  /**
   * Возвращает список тестов модуля
   *
   * @param module модуль с тестами
   * @return список тестов модуля
   */
  public Collection<Method> getTestMethods(Module module) {
    try {
      return getFinder(module).findTests(module, null);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return Collections.emptyList();
    }
  }

  /**
   * Возвращает ссылку на метод по полному имени метода
   *
   * @param project        проект, в котором выполняется поиск
   * @param fullMethodName полное имя метода. Шаблон: ИмяОбъектаМетаданны.ИмяМетода.
   *                       Например: ОбщийМодуль.Метод, Справочник.ИмяСправочника.ИмяМетода
   * @return ссылка на метода
   */
  public MethodReference getMethodReference(IV8Project project, String fullMethodName) {
    if (project == null) {
      return null;
    }
    var moduleName = getTestModuleName(fullMethodName);
    var methodName = getTestMethodName(fullMethodName);

    return Modules.findCommonModule(project, moduleName)
        .map(owner -> new MethodReference(owner.getModule(), methodName))
        .orElseGet(() -> {
          TestViewerPlugin.log().logError(MessageFormat.format(Messages.TestsManager_ModuleNotFound, moduleName));
          return null;
        });
  }

  /**
   * Извлекает имя метода из полного имени метода
   *
   * @param fullMethodName полное имя метода. Шаблон: ИмяОбъектаМетаданны.ИмяМетода.
   *                       Например: ОбщийМодуль.Метод, Справочник.ИмяСправочника.ИмяМетода
   * @return имя метода
   */
  public String getTestMethodName(String fullMethodName) {
    checkFullMethodName(fullMethodName);
    var chunks = fullMethodName.split(METHOD_NAME_SEPARATOR_PATTERN);
    return chunks[chunks.length - 1];
  }

  /**
   * Извлекает имя объекта методанных из полного имени метода
   *
   * @param fullMethodName полное имя метода. Шаблон: ИмяОбъектаМетаданны.ИмяМетода.
   *                       Например: ОбщийМодуль.Метод, Справочник.ИмяСправочника.ИмяМетода
   * @return имя объекта метаданных
   */
  public String getTestModuleName(String fullMethodName) {
    checkFullMethodName(fullMethodName);
    var chunks = fullMethodName.split(METHOD_NAME_SEPARATOR_PATTERN);
    return chunks.length == 2 ? chunks[0] : chunks[0] + METHOD_NAME_SEPARATOR + chunks[1];
  }

  private void checkFullMethodName(String fullMethodName) {
    var l = fullMethodName.split(METHOD_NAME_SEPARATOR_PATTERN).length;
    if (l != 3 && l != 2) {
      throw new IllegalArgumentException(Messages.TestsManager_InvalidFullMethodName);
    }
  }

  /**
   * Запускает тест из редактора
   *
   * @param editor     редактор
   * @param methodName имя теста
   * @param launchMode режим запуска
   */
  public void runTestMethod(XtextEditor editor, String methodName, String launchMode) {
    var moduleName = ((CommonModule) EditorHelper.getModule(editor).getOwner()).getName();
    LaunchHelper.runTestMethod(moduleName, methodName, launchMode);
  }

  /**
   * Запускает тест модуля
   *
   * @param module     модуль
   * @param methodName имя теста
   * @param launchMode режим запуска
   */
  public void runTestMethod(Module module, String methodName, String launchMode) {
    var moduleName = ((MdObject) module.getOwner()).getName();
    LaunchHelper.runTestMethod(moduleName, methodName, launchMode);
  }

  private ITestFinder getFinder(IV8Project project) {
    return TestKindRegistry.getContainerTestKind(project).getFinder();
  }

  private ITestFinder getFinder(EObject module) {
    return TestKindRegistry.getContainerTestKind(module).getFinder();
  }
}
