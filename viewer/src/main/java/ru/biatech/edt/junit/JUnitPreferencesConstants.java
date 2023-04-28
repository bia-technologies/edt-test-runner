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
public class JUnitPreferencesConstants {
  /**
   * Boolean preference controlling whether the JUnit view should be shown on
   * errors only.
   */
  public static final String SHOW_ON_ERROR_ONLY = TestViewerPlugin.getPluginId() + ".show_on_error"; //$NON-NLS-1$

  /**
   * Boolean preference controlling whether '-ea' should be added to VM arguments when creating a
   * new JUnit launch configuration.
   */
  public static final String ENABLE_ASSERTIONS = TestViewerPlugin.getPluginId() + ".enable_assertions"; //$NON-NLS-1$

  public static final boolean ENABLE_ASSERTIONS_DEFAULT = true;

  /**
   * List of inactive stack filters. A String containing a comma separated
   * list of fully qualified type names/patterns.
   */
  public static final String PREF_INACTIVE_FILTERS_LIST = TestViewerPlugin.getPluginId() + ".inactive_filters"; //$NON-NLS-1$

  /**
   * Maximum number of remembered test runs.
   */
  public static final String MAX_TEST_RUNS = TestViewerPlugin.getPluginId() + ".max_test_runs"; //$NON-NLS-1$

  /**
   * Javadoc location for JUnit 3
   */
  public static final String JUNIT3_JAVADOC = TestViewerPlugin.getPluginId() + ".junit3.javadoclocation"; //$NON-NLS-1$


  /**
   * Javadoc location for JUnit 4
   */
  public static final String JUNIT4_JAVADOC = TestViewerPlugin.getPluginId() + ".junit4.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.hamcrest.core (JUnit 4)
   */
  public static final String HAMCREST_CORE_JAVADOC = TestViewerPlugin.getPluginId() + ".junit4.hamcrest.core.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.jupiter.api (JUnit 5)
   */
  public static final String JUNIT_JUPITER_API_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.jupiter.api.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.jupiter.engine (JUnit 5)
   */
  public static final String JUNIT_JUPITER_ENGINE_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.jupiter.engine.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.jupiter.migrationsupport (JUnit 5)
   */
  public static final String JUNIT_JUPITER_MIGRATIONSUPPORT_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.jupiter.migrationsupport.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.jupiter.params (JUnit 5)
   */
  public static final String JUNIT_JUPITER_PARAMS_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.jupiter.params.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.platform.commons (JUnit 5)
   */
  public static final String JUNIT_PLATFORM_COMMONS_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.platform.commons.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.platform.engine (JUnit 5)
   */
  public static final String JUNIT_PLATFORM_ENGINE_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.platform.engine.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.platform.launcher (JUnit 5)
   */
  public static final String JUNIT_PLATFORM_LAUNCHER_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.platform.launcher.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.platform.runner (JUnit 5)
   */
  public static final String JUNIT_PLATFORM_RUNNER_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.platform.runner.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.platform.suite.api (JUnit 5)
   */
  public static final String JUNIT_PLATFORM_SUITE_API_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.platform.suite.api.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.platform.suite.engine (JUnit 5)
   */
  public static final String JUNIT_PLATFORM_SUITE_ENGINE_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.platform.suite.engine.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.platform.suite.commons (JUnit 5)
   */
  public static final String JUNIT_PLATFORM_SUITE_COMMONS_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.platform.suite.commons.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.junit.vintage.engine (JUnit 5)
   */
  public static final String JUNIT_VINTAGE_ENGINE_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.vintage.engine.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.opentest4j (JUnit 5)
   */
  public static final String JUNIT_OPENTEST4J_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.opentest4j.javadoclocation"; //$NON-NLS-1$

  /**
   * Javadoc location for org.apiguardian (JUnit 5)
   */
  public static final String JUNIT_APIGUARDIAN_JAVADOC = TestViewerPlugin.getPluginId() + ".junit5.apiguardian.javadoclocation"; //$NON-NLS-1$

  private JUnitPreferencesConstants() {
    // no instance
  }
}
