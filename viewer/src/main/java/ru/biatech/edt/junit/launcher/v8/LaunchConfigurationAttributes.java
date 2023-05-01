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

public final class LaunchConfigurationAttributes {

  public static final String USED_LAUNCH_CONFIGURATION = "ru.biatech.edt.junit.launcher.ATTR_USED_LAUNCH_CONFIGURATION"; //$NON-NLS-1$

  public static final String TEST_EXTENSION = "ru.biatech.edt.junit.launcher.TEST_EXTENSION"; //$NON-NLS-1$

  public static final String TEST_MODULE = "ru.biatech.edt.junit.launcher.TEST_MODULE"; //$NON-NLS-1$

  public static final String WORK_PATH = "ru.biatech.edt.junit.launcher.WORK_PATH"; //$NON-NLS-1$

  public static final String PROJECT = "ru.biatech.edt.junit.launcher.PROJECT"; //$NON-NLS-1$

  public static final String TEST_FULL_NAME = "ru.biatech.edt.junit.launcher.TEST_FULL_NAME"; //$NON-NLS-1$

  public static final String ATTR_TEST_RUNNER_KIND = "ru.biatech.edt.junit.launcher.TEST_KIND"; //$NON-NLS-1$

  public static final String ATTR_PORT = TestViewerPlugin.getPluginId() + ".PORT"; //$NON-NLS-1$

  /**
   * The test method name (followed by a comma-separated list of fully qualified parameter type
   * names in parentheses, if exists), or "" iff running the whole test type.
   */
  public static final String ATTR_TEST_NAME = TestViewerPlugin.getPluginId() + ".TESTNAME"; //$NON-NLS-1$

  public static final String ATTR_KEEPRUNNING = TestViewerPlugin.getPluginId() + ".KEEPRUNNING_ATTR"; //$NON-NLS-1$
  /**
   * The launch container, or "" iff running a single test type.
   */
  public static final String ATTR_TEST_CONTAINER = TestViewerPlugin.getPluginId() + ".CONTAINER"; //$NON-NLS-1$

  public static final String ATTR_FAILURES_NAMES = TestViewerPlugin.getPluginId() + ".FAILURENAMES"; //$NON-NLS-1$

  public static final String ATTR_TEST_HAS_INCLUDE_TAGS = TestViewerPlugin.getPluginId() + ".HAS_INCLUDE_TAGS"; //$NON-NLS-1$

  public static final String ATTR_TEST_HAS_EXCLUDE_TAGS = TestViewerPlugin.getPluginId() + ".HAS_EXCLUDE_TAGS"; //$NON-NLS-1$

  public static final String ATTR_TEST_INCLUDE_TAGS = TestViewerPlugin.getPluginId() + ".INCLUDE_TAGS"; //$NON-NLS-1$

  public static final String ATTR_TEST_EXCLUDE_TAGS = TestViewerPlugin.getPluginId() + ".EXCLUDE_TAGS"; //$NON-NLS-1$

  public static String getTargetConfigurationName(ILaunchConfiguration configuration) throws CoreException {
    return configuration.getAttribute(USED_LAUNCH_CONFIGURATION, (String) null);
  }

  public static String getTestExtensionName(ILaunchConfiguration configuration) {
    return getAttribute(configuration, TEST_EXTENSION);
  }

  public static void setTestMethods(ILaunchConfigurationWorkingCopy configuration, List<String> value) {
    configuration.setAttribute(TEST_FULL_NAME, value);
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

  public static String getTestKind(ILaunchConfiguration configuration){
    return getAttribute(configuration, ATTR_TEST_RUNNER_KIND);
  }

  public static String getAttribute(ILaunchConfiguration configuration, String attributeName) {
    try {
      return configuration.getAttribute(attributeName, (String) null);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getWorkPath(ILaunchConfiguration configuration){
    return getAttribute(configuration, WORK_PATH);
  }

  public static String getProject(ILaunchConfiguration configuration){
    return getAttribute(configuration, PROJECT);
  }
}