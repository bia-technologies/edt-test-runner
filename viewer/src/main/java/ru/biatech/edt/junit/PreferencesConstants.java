/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     David Saff (saff@mit.edu) - bug 102632: [JUnit] Support for JUnit 4.
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit;

/**
 * Defines constants which are used to refer to values in the plugin's preference store.
 */
public class PreferencesConstants {
  /**
   * Boolean preference controlling whether the JUnit view should be shown on
   * errors only.
   */
  public static final String SHOW_ON_ERROR_ONLY = TestViewerPlugin.getPluginId() + ".show_on_error"; //$NON-NLS-1$

  /**
   * Maximum number of remembered test runs.
   */
  public static final String MAX_TEST_RUNS = TestViewerPlugin.getPluginId() + ".max_test_runs"; //$NON-NLS-1$

  private PreferencesConstants() {
    // no instance
  }
}
