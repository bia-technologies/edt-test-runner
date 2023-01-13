/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
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
package ru.biatech.edt.junit.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import ru.biatech.edt.junit.TestViewerPlugin;

/**
 * Defines constants which are used to refer to values in the plugin's preference store.
 *
 * @since 3.7
 */
public class JUnitUIPreferencesConstants {
  /**
   * Boolean preference controlling whether newly launched JUnit tests should be shown in all
   * JUnit views (in all windows).
   */
  public static final String SHOW_IN_ALL_VIEWS = JUnitUI.PLUGIN_ID + ".show_in_all_views"; //$NON-NLS-1$

  public static final boolean SHOW_IN_ALL_VIEWS_DEFAULT = false; // would need a PreferenceInitializer if this was changed to true!

  private JUnitUIPreferencesConstants() {
    // no instance
  }

  public static boolean getShowInAllViews() {
    return Platform.getPreferencesService().getBoolean(JUnitUI.PLUGIN_ID, SHOW_IN_ALL_VIEWS, SHOW_IN_ALL_VIEWS_DEFAULT, null);
  }

  public static void setShowInAllViews(boolean show) {
    IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JUnitUI.PLUGIN_ID);
    preferences.putBoolean(SHOW_IN_ALL_VIEWS, show);
    try {
      preferences.flush();
    } catch (BackingStoreException e) {
      TestViewerPlugin.log().logError(e);
    }
  }
}
