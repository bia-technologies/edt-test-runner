/*******************************************************************************
 * Copyright (c) 2021-2022 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.launcher.v8;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import ru.biatech.edt.junit.TestViewerPlugin;

import java.util.List;

import static ru.biatech.edt.junit.Constants.PLUGIN_ID;

public final class LaunchConfigurationAttributes {

  public static final String USED_LAUNCH_CONFIGURATION = PLUGIN_ID + ".launcher.USED_LAUNCH_CONFIGURATION"; //$NON-NLS-1$

  public static final String TEST_EXTENSION = PLUGIN_ID + ".launcher.TEST_EXTENSION"; //$NON-NLS-1$

  public static final String TEST_MODULE = PLUGIN_ID + ".launcher.TEST_MODULE"; //$NON-NLS-1$

  public static final String PROJECT_PATH = PLUGIN_ID + ".launcher.PROJECT_PATH"; //$NON-NLS-1$

  public static final String LOGGING_CONSOLE = PLUGIN_ID + ".launcher.LOGGING_TO_CONSOLE"; //$NON-NLS-1$

  public static final String WORK_PATH = PLUGIN_ID + ".launcher.WORK_PATH"; //$NON-NLS-1$

  public static final String PROJECT = PLUGIN_ID + ".launcher.PROJECT"; //$NON-NLS-1$

  public static final String TEST_FULL_NAME = PLUGIN_ID + ".launcher.TEST_FULL_NAME"; //$NON-NLS-1$

  public static final String ATTR_TEST_RUNNER_KIND = PLUGIN_ID + ".launcher.TEST_KIND"; //$NON-NLS-1$

  public static final String ATTR_KEEP_RUNNING = PLUGIN_ID + ".launcher.KEEP_RUNNING"; //$NON-NLS-1$
  public static final String ATTR_RPC_KEY = PLUGIN_ID + ".launcher.RPC_KEY"; //$NON-NLS-1$

  public static String getTargetConfigurationName(ILaunchConfiguration configuration) throws CoreException {
    return configuration.getAttribute(USED_LAUNCH_CONFIGURATION, (String) null);
  }

  public static String getTestExtensionName(ILaunchConfiguration configuration) {
    return getAttribute(configuration, TEST_EXTENSION);
  }

  public static void setTestMethods(ILaunchConfigurationWorkingCopy configuration, List<String> value) {
    configuration.setAttribute(TEST_FULL_NAME, value);
  }

  public static void clearFilter(ILaunchConfigurationWorkingCopy configuration) {
    configuration.removeAttribute(TEST_FULL_NAME);
    configuration.removeAttribute(TEST_MODULE);
    configuration.removeAttribute(TEST_EXTENSION);
  }

  public static List<String> getTestMethods(ILaunchConfiguration configuration) {
    try {
      return configuration.getAttribute(TEST_FULL_NAME, (List<String>) null);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  public static void clearTestMethods(ILaunchConfigurationWorkingCopy configuration) {
    setTestMethods(configuration, null);
  }

  public static String getTestModuleName(ILaunchConfiguration configuration) {
    return getAttribute(configuration, TEST_MODULE);
  }

  public static String getProjectPath(ILaunchConfiguration configuration) {
    return getAttribute(configuration, PROJECT_PATH);
  }

  public static boolean getLoggingToConsole(ILaunchConfiguration configuration) {
    return getBooleanAttribute(configuration, LOGGING_CONSOLE);
  }

  public static String getTestKind(ILaunchConfiguration configuration) {
    return getAttribute(configuration, ATTR_TEST_RUNNER_KIND);
  }

  public static String getAttribute(ILaunchConfiguration configuration, String attributeName) {
    try {
      return configuration.getAttribute(attributeName, (String) null);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean getBooleanAttribute(ILaunchConfiguration configuration, String attributeName) {
    try {
      return configuration.getAttribute(attributeName, false);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getWorkPath(ILaunchConfiguration configuration) {
    return getAttribute(configuration, WORK_PATH);
  }

  public static String getProject(ILaunchConfiguration configuration) {
    return getAttribute(configuration, PROJECT);
  }

  public static boolean getKeepAlive(ILaunchConfiguration configuration) {
    return true;//getBooleanAttribute(configuration, LaunchConfigurationAttributes.ATTR_KEEP_RUNNING);
  }
}