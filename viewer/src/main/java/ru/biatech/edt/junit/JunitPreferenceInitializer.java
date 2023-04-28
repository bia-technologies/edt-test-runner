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
 *     Sebastian Davids <sdavids@gmx.de> - initial API and implementation
 *     Achim Demelt <a.demelt@exxcellent.de> - [junit] Separate UI from non-UI code - https://bugs.eclipse.org/bugs/show_bug.cgi?id=278844
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Default preference value initialization for the
 * <code>org.eclipse.jdt.junit</code> plug-in.
 */
public class JunitPreferenceInitializer extends AbstractPreferenceInitializer {

  @Override
  public void initializeDefaultPreferences() {
    IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JUnitCore.CORE_PLUGIN_ID);

    prefs.putBoolean(JUnitPreferencesConstants.SHOW_ON_ERROR_ONLY, false);
    prefs.putBoolean(JUnitPreferencesConstants.ENABLE_ASSERTIONS, JUnitPreferencesConstants.ENABLE_ASSERTIONS_DEFAULT);

    prefs.put(JUnitPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, ""); //$NON-NLS-1$
    prefs.putInt(JUnitPreferencesConstants.MAX_TEST_RUNS, 10);

    // see https://github.com/junit-team/junit/issues/570
    prefs.put(JUnitPreferencesConstants.JUNIT3_JAVADOC, "http://junit.sourceforge.net/junit3.8.1/javadoc/"); //$NON-NLS-1$
    prefs.put(JUnitPreferencesConstants.JUNIT4_JAVADOC, "http://junit.org/junit4/javadoc/latest/"); //$NON-NLS-1$
    prefs.put(JUnitPreferencesConstants.HAMCREST_CORE_JAVADOC, "http://hamcrest.org/JavaHamcrest/javadoc/1.3/"); //$NON-NLS-1$

    String junit5JavadocLocation = "http://junit.org/junit5/docs/current/api/"; //$NON-NLS-1$
    prefs.put(JUnitPreferencesConstants.JUNIT_JUPITER_API_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_JUPITER_ENGINE_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_JUPITER_MIGRATIONSUPPORT_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_JUPITER_PARAMS_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_PLATFORM_COMMONS_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_PLATFORM_ENGINE_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_PLATFORM_LAUNCHER_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_PLATFORM_RUNNER_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_PLATFORM_SUITE_API_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_PLATFORM_SUITE_ENGINE_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_PLATFORM_SUITE_COMMONS_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_VINTAGE_ENGINE_JAVADOC, junit5JavadocLocation);
    prefs.put(JUnitPreferencesConstants.JUNIT_OPENTEST4J_JAVADOC, "http://ota4j-team.github.io/opentest4j/docs/current/api/"); //$NON-NLS-1$
    prefs.put(JUnitPreferencesConstants.JUNIT_APIGUARDIAN_JAVADOC, "https://apiguardian-team.github.io/apiguardian/docs/current/api/"); //$NON-NLS-1$
  }
}