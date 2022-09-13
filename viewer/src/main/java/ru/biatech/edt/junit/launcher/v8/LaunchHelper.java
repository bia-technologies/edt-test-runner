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

import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.launching.core.ILaunchConfigurationTypes;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestKind;
import ru.biatech.edt.junit.kinds.TestKindRegistry;
import ru.biatech.edt.junit.launcher.LaunchConfigurationTypes;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.v8utils.Resolver;
import ru.biatech.edt.junit.v8utils.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LaunchHelper {

  public final static String REPORT_FILE_NAME = "junit.xml"; //$NON-NLS-1$

  public final static String PREFIX_LAUNCH_TEST = "RunTest "; //$NON-NLS-1$
  public static Stream<ILaunchConfiguration> getOnecLaunchConfigurations() {
    try {
      return Arrays.stream(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()).filter(LaunchHelper::isOnecConfiguration);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return Stream.empty();
    }
  }

  public static Stream<ILaunchConfiguration> getTestLaunchConfigurations() {
    try {
      return Arrays.stream(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()).filter(LaunchHelper::isRunTestConfiguration);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return Stream.empty();
    }
  }

  public static boolean isOnecConfiguration(ILaunchConfiguration configuration) {
    try {
      return Objects.equals(configuration.getType().getIdentifier(), ILaunchConfigurationTypes.RUNTIME_CLIENT);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return false;
    }
  }

  public static boolean isRunTestConfiguration(ILaunchConfiguration configuration) {
    try {
      return Objects.equals(configuration.getType().getIdentifier(), LaunchConfigurationTypes.TEST_CLIENT);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return false;
    }
  }

  public static ILaunchConfiguration getLaunchConfiguration(String name) throws CoreException {
    if (name == null || name.isEmpty()) return null;
    ILaunchConfiguration result = null;
    for (ILaunchConfiguration configuration : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()) {
      if (configuration.getName().equals(name)) {
        result = configuration;
        break;
      }
    }
    return result;
  }

  public static List<IExtensionProject> getExtensions() {
    return Services.getProjectManager().getProjects(IExtensionProject.class)
                   .stream()
                   .filter(TestViewerPlugin.getTestManager()::isTestProject)
                   .collect(Collectors.toList());
  }

  public static void checkConfiguration(ILaunchConfiguration configuration) throws CoreException {
    String usedLC = configuration.getAttribute(LaunchConfigurationAttributes.USED_LAUNCH_CONFIGURATION, (String) null);

    var usedConfiguration = LaunchHelper.getLaunchConfiguration(usedLC);

    if (usedLC == null || usedLC.isEmpty()) {
      throw new CoreException(TestViewerPlugin.log().createErrorStatus(JUnitMessages.LaunchHelper_LaunchConfigurationNotSpecified));
    } else if (usedConfiguration == null) {
      throw new CoreException(TestViewerPlugin.log().createErrorStatus(JUnitMessages.LaunchHelper_LaunchConfigurationNotFound));
    }
  }

  public static ILaunchConfiguration getTargetConfiguration(ILaunchConfiguration configuration) throws CoreException {
    String targetConfigurationName = LaunchConfigurationAttributes.getTargetConfigurationName(configuration);

    return getLaunchConfiguration(targetConfigurationName);
  }

  public static IExtensionProject getTestExtension(ILaunchConfiguration configuration) {
    return (IExtensionProject) Resolver.getProject(LaunchConfigurationAttributes.getTestExtensionName(configuration));
  }

  public static List<String> getTestModules(IExtensionProject extensionProject) {
    return extensionProject.getConfiguration().getCommonModules()
                   .stream()
                   .filter(TestViewerPlugin.getTestManager()::isTestModule)
                   .map(CommonModule::getName)
                   .collect(Collectors.toList());
  }

  public static Path getWorkPath(ILaunchConfiguration configuration) {
    var reportLocation = Platform.getStateLocation(TestViewerPlugin.getBundleContext().getBundle()).append(configuration.getName());
    var path = reportLocation.toFile().toPath();
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      TestViewerPlugin.log().logError(e);
    }

    return path;
  }

  public static void runTestMethod(String methodFullName, String launchMode) {
    var configuration = getTestLaunchConfigurations().findFirst();
    if (configuration.isEmpty()) return;

    ILaunchConfigurationWorkingCopy copy;
    try {
      copy = configuration.get().copy(PREFIX_LAUNCH_TEST + methodFullName); //$NON-NLS-1$
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return;
    }

    LaunchConfigurationAttributes.setTestMethods(copy, List.of(methodFullName));
    DebugUITools.launch(copy, launchMode);
  }

  public static ITestKind getTestRunnerKind(ILaunchConfiguration launchConfiguration) {
    try {
      String loaderId = launchConfiguration.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_RUNNER_KIND, (String) null);
      if (loaderId != null) {
        return TestKindRegistry.getDefault().getKind(loaderId);
      }
    } catch (CoreException e) {
    }
    return ITestKind.NULL;
  }

  public static IV8Project getProject(ILaunchConfiguration configuration) {
    // TODO
//		try {
//			String projectName= configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
//			if (projectName != null && projectName.length() > 0) {
//				return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
//			}
//		} catch (CoreException e) {
//		}
    return null;
  }
}
