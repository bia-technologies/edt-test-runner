/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

import ru.biatech.edt.junit.model.report.ErrorInfo;

import java.util.stream.Stream;

/**
 * Represents a test case element.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.3
 */
public interface ITestCaseElement extends ITestElement {

  /**
   * Returns the name of the test method.
   *
   * @return returns the name of the test method.
   */
  String getMethodName();

  /**
   * Returns the qualified type name of the class the test is contained in.
   *
   * @return the qualified type name of the class the test is contained in.
   */
  String getClassName();

  /**
   * Возвращает имя контекста исполнения теста
   *
   * @return имя контекста исполнения теста
   */
  String getContext();

  String getDisplayName();

  Stream<ErrorInfo> getErrorsList();
}
