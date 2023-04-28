/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Saff (saff@mit.edu) - initial API and implementation
 *             (bug 102632: [JUnit] Support for JUnit 4.)
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.kinds;

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import ru.biatech.edt.junit.v8utils.MethodReference;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Interface to be implemented by for extension point
 * org.eclipse.jdt.junit.internal_testKinds.
 */
public interface ITestFinder {

  ITestFinder NULL = new ITestFinder() {
    @Override
    public Collection<Method> findTests(Module module, IProgressMonitor pm) {
      return Collections.emptyList();
    }

    @Override
    public boolean isTestModule(Module module) {
      return false;
    }

    @Override
    public boolean isTestProject(IV8Project project) {
      return false;
    }

    @Override
    public boolean isTestMethod(Module module, String methodName) {
      return false;
    }

    @Override
    public List<MethodReference> findTestsFor(Module module, String methodName) {
      return null;
    }

    @Override
    public List<MethodReference> findTestedMethod(Module testModule, String testMethodName) {
      return null;
    }

    @Override
    public List<MethodReference> findTestedMethod(String testModuleName, String testMethodName) {
      return null;
    }
  };

  /** Выполняет поиск тестов модуля
   * @param module element to search for tests
   * @param pm     the progress monitor
   * @throws CoreException thrown when tests can not be found
   */
  Collection<Method> findTests(Module module, IProgressMonitor pm) throws CoreException;

  /**
   * Проверяет, является ли проектом с тестами
   * @param project проект для проверки
   * @return признак, это тестовый проект
   */
  boolean isTestProject(IV8Project project);

  /**
   * Проверяет, является ли модулем с тестами
   * @param module модуль для проверки
   * @return признак, это тестовый модуль
   */
  boolean isTestModule(Module module);

  /**
   * Проверяет, является ли метод модуля тестом
   * @param module модуль, которому принадлежит метод
   * @param methodName имя метода
   * @return признак, это тест
   */
  boolean isTestMethod(Module module, String methodName);

  /**
   * Ищет и возвращает список тестов метода модуля (тесты, которые проверяют функциональность метода модуля)
   * @param module модуль
   * @param methodName имя метода
   * @return список тестов метода
   */
  List<MethodReference> findTestsFor(Module module, String methodName);

  /**
   * Ищет и возвращает список прверяемых тестом методов
   * @param testModule модуль, содержищий тест
   * @param testMethodName имя метода теста
   * @return список проверяемых методов (модулей)
   */
  List<MethodReference> findTestedMethod(Module testModule, String testMethodName);

  /**
   * Ищет и возвращает список прверяемых тестом методов
   * @param testModuleName имя модуля, содержищего тест
   * @param testMethodName имя метода теста
   * @return список проверяемых методов (модулей)
   */
  List<MethodReference> findTestedMethod(String testModuleName, String testMethodName);
}
