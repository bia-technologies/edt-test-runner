/*******************************************************************************
 * Copyright (c) 2025 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit;

import lombok.experimental.UtilityClass;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;

@UtilityClass
public class Preferences {

  public static final String SHOW_ON_ERROR_ONLY = Constants.PLUGIN_ID + ".show_on_error"; //$NON-NLS-1$

  public void putShowOnErrorOnly(boolean value) {
    InstanceScope.INSTANCE.getNode(TestViewerPlugin.getPluginId())
        .putBoolean(SHOW_ON_ERROR_ONLY, value);
  }

  public boolean getShowOnErrorOnly() {
    return Platform.getPreferencesService().getBoolean(TestViewerPlugin.getPluginId(), SHOW_ON_ERROR_ONLY, false, null);
  }

  public int getMaxTestRuns() {
    return 10;
  }
}
