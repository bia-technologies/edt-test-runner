/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

import com._1c.g5.v8.dt.core.platform.IV8Project;

/**
 * Represents a test run session.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.3
 */
public interface ITestRunSession extends ITestElementContainer {

  /**
   * Returns the name of the test run. The name is the name of the launch configuration use to run this test.
   *
   * @return returns the test run name
   */
  String getTestRunName();

  /**
   * Returns the Java project from which this test run session has been launched, or <code>null</code> if not available.
   *
   * @return the launched project, or <code>null</code> is not available.
   * @since 3.6
   */
  IV8Project getLaunchedProject();

}
