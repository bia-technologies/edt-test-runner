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
import lombok.experimental.UtilityClass;
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
import ru.biatech.edt.junit.services.TestsManager;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.v8utils.Projects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class LaunchHelper {

  public final String REPORT_FILE_NAME = "junit.xml"; //$NON-NLS-1$

  public final String PREFIX_LAUNCH_TEST = "RunTest "; //$NON-NLS-1$

  public Stream<ILaunchConfiguration> getOnecLaunchConfigurations() {
    try {
      return Arrays.stream(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()).filter(LaunchHelper::isOnecConfiguration);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return Stream.empty();
    }
  }

  public Stream<ILaunchConfiguration> getTestLaunchConfigurations() {
    try {
      return Arrays.stream(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()).filter(LaunchHelper::isRunTestConfiguration);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return Stream.empty();
    }
  }

  public boolean isOnecConfiguration(ILaunchConfiguration configuration) {
    try {
      return Objects.equals(configuration.getType().getIdentifier(), ILaunchConfigurationTypes.RUNTIME_CLIENT);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return false;
    }
  }

  public boolean isRunTestConfiguration(ILaunchConfiguration configuration) {
    try {
      return Objects.equals(configuration.getType().getIdentifier(), LaunchConfigurationTypes.TEST_CLIENT);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return false;
    }
  }

  public ILaunchConfiguration getLaunchConfiguration(String name) throws CoreException {
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

  public List<IExtensionProject> getTestExtensions() {
    return Projects.getExtensions()
        .stream()
        .filter(TestsManager::isTestProject)
        .collect(Collectors.toList());
  }

  public void checkConfiguration(ILaunchConfiguration configuration) throws CoreException {
    String usedLC = configuration.getAttribute(LaunchConfigurationAttributes.USED_LAUNCH_CONFIGURATION, (String) null);

    var usedConfiguration = LaunchHelper.getLaunchConfiguration(usedLC);

    if (usedLC == null || usedLC.isEmpty()) {
      throw new CoreException(TestViewerPlugin.log().createErrorStatus(JUnitMessages.LaunchHelper_LaunchConfigurationNotSpecified));
    } else if (usedConfiguration == null) {
      throw new CoreException(TestViewerPlugin.log().createErrorStatus(JUnitMessages.LaunchHelper_LaunchConfigurationNotFound));
    }
  }

  public ILaunchConfiguration getTargetConfiguration(ILaunchConfiguration configuration) throws CoreException {
    String targetConfigurationName = LaunchConfigurationAttributes.getTargetConfigurationName(configuration);

    return getLaunchConfiguration(targetConfigurationName);
  }

  public IExtensionProject getTestExtension(ILaunchConfiguration configuration) {
    return (IExtensionProject) Projects.getProject(LaunchConfigurationAttributes.getTestExtensionName(configuration));
  }

  public Stream<CommonModule> getTestModulesStream(IExtensionProject extensionProject) {
    return extensionProject.getConfiguration().getCommonModules()
        .stream()
        .filter(TestsManager::isTestModule);
  }

  public List<String> getTestModules(IExtensionProject extensionProject) {
    return getTestModulesStream(extensionProject)
        .map(CommonModule::getName)
        .collect(Collectors.toList());
  }

  public Path getWorkPath(String name) {
    var reportLocation = Platform.getStateLocation(TestViewerPlugin.getBundleContext().getBundle()).append(name);
    var path = reportLocation.toFile().toPath();
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      TestViewerPlugin.log().logError(e);
    }

    return path;
  }

  public void runTestMethod(String moduleName, String methodName, String launchMode) {
    var methodFullName = moduleName + "." + methodName; //$NON-NLS-1$

    var configuration = getTestLaunchConfigurations().findFirst();
    String errorMessage = null;

    if (configuration.isEmpty()) {
      errorMessage = JUnitMessages.LaunchHelper_DefaultLaunchConfigurationNotFound;
    } else {
      try {
        checkConfiguration(configuration.get());
      } catch (CoreException e) {
        TestViewerPlugin.log().logError(e);
        errorMessage = e.getMessage();
      }
    }

    if (errorMessage != null) {
      Dialogs.showError(JUnitMessages.LaunchTest_title, errorMessage);
      return;
    }

    ILaunchConfigurationWorkingCopy copy;
    try {
      copy = configuration.get().copy(PREFIX_LAUNCH_TEST + methodFullName); //$NON-NLS-1$
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      Dialogs.showError(JUnitMessages.LaunchTest_title, e.getMessage());
      return;
    }

    LaunchConfigurationAttributes.clearFilter(copy);
    LaunchConfigurationAttributes.setTestMethods(copy, List.of(methodFullName));
    DebugUITools.launch(copy, launchMode);
  }

  public ITestKind getTestRunnerKind(ILaunchConfiguration launchConfiguration) {
    try {
      String loaderId = launchConfiguration.getAttribute(LaunchConfigurationAttributes.ATTR_TEST_RUNNER_KIND, (String) null);
      if (loaderId != null) {
        return TestKindRegistry.getDefault().getKind(loaderId);
      }
    } catch (CoreException e) {
    }
    return ITestKind.NULL;
  }

  public Path getReportPath(ILaunchConfiguration configuration) {
    var workPath = LaunchConfigurationAttributes.getWorkPath(configuration);
    return Path.of(workPath, REPORT_FILE_NAME);
  }

  public IV8Project getProject(ILaunchConfiguration configuration) {
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
