/*******************************************************************************
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.IUnitLauncher;
import ru.biatech.edt.junit.model.TestRunSession;

import java.util.List;

public class RerunHelper {
  public static ILaunchConfiguration getLaunchConfiguration(TestRunSession testRunSession) {
    if (testRunSession != null) {
      ILaunch launch = testRunSession.getLaunch();
      if (launch != null) {
        // run the selected test using the previous launch configuration
        return launch.getLaunchConfiguration();
      }
    }
    return null;
  }

  public static boolean isRerunConfiguration(ILaunchConfiguration launchConfiguration) {
    var attribute = LaunchConfigurationAttributes.getTestMethods(launchConfiguration);
    return attribute != null;
  }

  public static void rerun(TestRunSession testRunSession, String configName, List<String> testClasses) throws CoreException {
    if (testRunSession == null || testRunSession.getLaunch() == null) {
      return;
    }
    rerun(testRunSession, configName, testClasses, testRunSession.getLaunch().getLaunchMode());
  }

  public static void rerun(TestRunSession testRunSession, String configName, List<String> testClasses, String launchMode) throws CoreException {
    if (!canRerun(testRunSession, testClasses)) {
      return;
    }
    ILaunchConfiguration launchConfiguration = RerunHelper.getLaunchConfiguration(testRunSession);
    ILaunchConfigurationWorkingCopy tmp = launchConfiguration.copy(configName);
    tmp.setAttribute(LaunchConfigurationAttributes.TEST_FULL_NAME, testClasses); //$NON-NLS-1$
    launch(testRunSession, tmp, launchMode);
  }

  public static void launch(TestRunSession testRunSession, ILaunchConfiguration launchConfiguration, String launchMode) {
    ILaunchConfigurationWorkingCopy workingCopy;
    if (launchConfiguration.isWorkingCopy()) {
      workingCopy = (ILaunchConfigurationWorkingCopy) launchConfiguration;
    } else {
      try {
        workingCopy = launchConfiguration.getWorkingCopy();
      } catch (CoreException e) {
        TestViewerPlugin.log().logError(e);
        return;
      }
    }
    launch(testRunSession, workingCopy, launchMode);
  }

  public static void launch(TestRunSession testRunSession, ILaunchConfigurationWorkingCopy launchConfiguration, String launchMode) {
    IUnitLauncher launcherKind = testRunSession.getTestRunnerKind().getLauncher();
    launcherKind.configure(launchConfiguration, launchConfiguration);
    DebugUITools.launch(launchConfiguration, launchMode);

  }

  private static boolean canRerun(TestRunSession testRunSession, List<String> testClasses) {
    if (testClasses.isEmpty()) {
      return false;
    }
    ILaunchConfiguration launchConfiguration = RerunHelper.getLaunchConfiguration(testRunSession);
    return launchConfiguration != null;
  }

}
