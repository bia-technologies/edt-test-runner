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

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import org.eclipse.debug.core.ILaunchConfiguration;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.v8utils.Projects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class LaunchSettings {
  String name;
  String workPath;
  @Expose
  String reportPath;
  @Expose
  String reportFormat = Launcher.REPORT_FORMAT;
  @Expose
  boolean closeAfterTests = true;
  @Expose
  Filter filter;
  String extensionName;

  public static class Filter {
    @Expose
    List<String> extensions = new ArrayList<>();;
    @Expose
    List<String> modules = new ArrayList<>();
    @Expose
    List<String> tests = new ArrayList<>();

    public void addModule(String moduleName) {
      if (!Strings.isNullOrEmpty(moduleName)) {
        modules.add(moduleName);
      }
    }

    public void addExtension(String extensionName) {
      if (!Strings.isNullOrEmpty(extensionName)) {
        extensions.add(extensionName);
      }
    }
  }

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
    if (tests != null && tests.size() == 1) {
      var chunks = tests.get(0).split("\\."); //$NON-NLS-1$
      if (chunks.length == 2 && chunks[1].equalsIgnoreCase(TestFinder.REGISTRATION_METHOD_NAME)) {
        filter.modules.add(chunks[0]);
        tests = Collections.emptyList();
      }
    }
    filter.tests = tests;

    settings.name = configuration.getName();
    settings.workPath = LaunchHelper.getWorkPath(settings.name).toString();
    settings.reportPath = settings.workPath;
    settings.filter = filter;

    return settings;
  }
}
