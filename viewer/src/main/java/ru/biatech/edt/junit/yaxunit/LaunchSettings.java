/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import org.eclipse.debug.core.ILaunchConfiguration;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.ui.utils.StringUtilities;
import ru.biatech.edt.junit.v8utils.Projects;
import ru.biatech.edt.junit.yaxunit.remote.RemoteLaunchManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class LaunchSettings {
  String projectPath;
  String reportPath;
  String reportFormat = Constants.REPORT_FORMAT;
  boolean closeAfterTests = true;
  Filter filter;
  LoggingSettings logging;
  RpcSettings rpc;

  @JsonIgnore
  String name;
  @JsonIgnore
  String workPath;
  @JsonIgnore
  String extensionName;
  @JsonIgnore
  Set<String> usedModules;

  public static LaunchSettings fromConfiguration(ILaunchConfiguration configuration) {
    var extension = LaunchHelper.getTestExtension(configuration);
    var settings = new LaunchSettings();

    var filter = new Filter();
    filter.addModule(LaunchConfigurationAttributes.getTestModuleName(configuration));
    if (extension != null) {
      filter.addExtension(Projects.getProjectName(extension));
      settings.extensionName = extension.getDtProject().getName();
    }

    var tests = LaunchConfigurationAttributes.getTestMethods(configuration);

    if (tests != null) {
      settings.usedModules = tests.stream().map(t -> t.split("\\.")[0])
          .collect(Collectors.toUnmodifiableSet());
    }

    if (tests != null && tests.size() == 1) {
      var chunks = tests.get(0).split("\\."); //$NON-NLS-1$
      if (chunks.length == 2 && chunks[1].equalsIgnoreCase(Constants.REGISTRATION_METHOD_NAME)) {
        filter.modules.add(chunks[0]);
        tests = Collections.emptyList();
      }
    }

    filter.tests = tests;

    var logging = new LoggingSettings();
    if (LaunchConfigurationAttributes.getLoggingToConsole(configuration)) {
      logging.setConsole(true);
      logging.setLevel("debug"); //$NON-NLS-1$
    }

    var useRemoteLaunch = LaunchConfigurationAttributes.useRemoteLaunch(configuration);
    settings.closeAfterTests = !useRemoteLaunch;
    settings.name = configuration.getName();
    settings.workPath = LaunchHelper.getWorkPath(settings.name).toString();
    settings.reportPath = settings.workPath;
    settings.filter = filter;
    settings.logging = logging;
    settings.projectPath = LaunchConfigurationAttributes.getProjectPath(configuration);

    if (useRemoteLaunch) {
      settings.rpc = new RpcSettings();
      RemoteLaunchManager.configureLaunch(settings.rpc);
    }

    return settings;
  }

  @Getter
  public static class Filter {
    List<String> extensions = new ArrayList<>();
    ;
    List<String> modules = new ArrayList<>();
    List<String> tests = new ArrayList<>();

    public void addModule(String moduleName) {
      if (!StringUtilities.isNullOrEmpty(moduleName)) {
        modules.add(moduleName);
      }
    }

    public void addExtension(String extensionName) {
      if (!StringUtilities.isNullOrEmpty(extensionName)) {
        extensions.add(extensionName);
      }
    }
  }

  @Data
  public static class RpcSettings {
    int port;
    boolean enable;
    String key;
    String transport = "ws";
  }

  @Data
  public static class LoggingSettings {
    boolean console;
    String level;
  }
}
