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

package ru.biatech.edt.junit.yaxunit;

import com._1c.g5.v8.dt.launching.core.ILaunchConfigurationAttributes;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import lombok.SneakyThrows;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import ru.biatech.edt.junit.Serializer;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.IUnitLauncher;
import ru.biatech.edt.junit.kinds.TestKindRegistry;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.model.SessionsManager;
import ru.biatech.edt.junit.ui.utils.StringUtilities;
import ru.biatech.edt.junit.v8utils.Modules;
import ru.biatech.edt.junit.v8utils.Projects;
import ru.biatech.edt.junit.yaxunit.remote.RemoteLaunchManager;

import java.nio.file.Path;
import java.util.Optional;

public class Launcher implements IUnitLauncher {

  @Override
  public void launch(ILaunchConfiguration configuration, String launchMode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
    TestViewerPlugin.log().debug(Messages.Launcher_Launch, configuration);

    if (LaunchConfigurationAttributes.useRemoteLaunch(configuration)) {
      RemoteLaunchManager.start();
    }

    var settings = LaunchSettings.fromConfiguration(configuration);

    var oneCConfiguration = LaunchHelper.getTargetConfiguration(configuration);
    if (oneCConfiguration == null) {
      TestViewerPlugin.log().logError(Messages.Launcher_LaunchConfigurationNotSpecified);
      launch.terminate();
      return;
    }

    var oneCConfigurationCopy = oneCConfiguration.copy("YAX. " + configuration.getName()); //$NON-NLS-1$

    if (remoteLaunchTest(configuration, settings, launch, monitor)) {
      return;
    }

    configure(oneCConfigurationCopy, settings);
    copyAttributes(configuration, oneCConfigurationCopy);

    oneCConfigurationCopy.launch(launchMode, SubMonitor.convert(monitor, 1));
  }

  public void configure(ILaunchConfigurationWorkingCopy oneCConfiguration, LaunchSettings settings) {
    var startupParameters = Constants.RUN_PARAMETERS + createConfig(settings);

    oneCConfiguration.setAttribute(ILaunchConfigurationAttributes.STARTUP_OPTION, startupParameters);
    oneCConfiguration.setAttribute(LaunchConfigurationAttributes.WORK_PATH, settings.getWorkPath());
    oneCConfiguration.setAttribute(LaunchConfigurationAttributes.PROJECT, settings.getExtensionName());
    oneCConfiguration.setAttribute(LaunchConfigurationAttributes.TEST_RUNNER_KIND, TestKindRegistry.YAXUNIT_TEST_KIND_ID);
    if (settings.rpc != null) {
      oneCConfiguration.setAttribute(LaunchConfigurationAttributes.RPC_KEY, settings.rpc.key);
    }
  }

  protected String createConfig(LaunchSettings settings) {
    var path = Path.of(settings.getWorkPath(), Constants.PARAMETERS_FILE_NAME);

    try {
      Serializer.getJsonMapper().writeValue(path.toFile(), settings);
    } catch (Exception e) {
      TestViewerPlugin.log().logError(e);
    }
    TestViewerPlugin.log().debug(Messages.Launcher_ConfigurationLocation, path);
    return path.toString();
  }

  protected void copyAttributes(ILaunchConfiguration unitConfiguration, ILaunchConfigurationWorkingCopy oneCConfiguration) {
    String[] attributes = new String[]{
        LaunchConfigurationAttributes.TEST_EXTENSION
    };

    for (String attribute : attributes) {
      String value = LaunchConfigurationAttributes.getAttribute(unitConfiguration, attribute);
      if (!StringUtilities.isNullOrEmpty(value)) {
        oneCConfiguration.setAttribute(attribute, value);
      }
    }
  }

  @SneakyThrows
  public boolean remoteLaunchTest(ILaunchConfiguration configuration, LaunchSettings settings, ILaunch launch, IProgressMonitor monitor) {
    if (!RemoteLaunchManager.isAvailable() || LaunchConfigurationAttributes.useRemoteLaunch(configuration)) {
      return false;
    }

    if (settings.usedModules == null || settings.usedModules.size() != 1) {
      TestViewerPlugin.log().info("Будет выполнен перезапуск предприятия. Запуск тестов без перезапуска работает только для одного модуля.");
      return false;
    }

    var moduleName = settings.usedModules.stream().findFirst().get();

    var project = LaunchHelper.getProject(configuration);
    Optional<CommonModule> moduleOpt;
    if (project == null) {
      moduleOpt = Projects.getExtensions().stream()
          .map(p -> Modules.findCommonModule(p, moduleName))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findAny();
    } else {
      moduleOpt = Modules.findCommonModule(project, moduleName);
    }
    if (moduleOpt.isEmpty()) {
      TestViewerPlugin.log().logError("Не удалось найти модуль " + moduleName);
      return false;
    }
    var module = moduleOpt.get();

    var content = Modules.getModuleContent(module);
    RemoteLaunchManager.launchTest(content, moduleName, settings.filter.tests, module.isServer(), module.isClientManagedApplication(), module.isClientOrdinaryApplication())
        .thenAccept(suites -> {
          SessionsManager.getInstance().importSession(suites, launch);
          monitor.done();
        });

    return true;
  }
}
