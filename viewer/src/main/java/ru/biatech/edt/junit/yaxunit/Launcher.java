/*******************************************************************************
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.launching.core.ILaunchConfigurationAttributes;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.IUnitLauncher;
import ru.biatech.edt.junit.kinds.TestKindRegistry;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.v8utils.Projects;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class Launcher implements IUnitLauncher {

  private static final String RUN_PARAMETERS = "RunUnitTests=";
  private static final String PARAMETERS_FILE_NAME = "xUnitParams.json";
  private static final String REPORT_FORMAT = "jUnit";

  @Override
  public void launch(ILaunchConfiguration configuration, String launchMode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
    TestViewerPlugin.log().debug(JUnitMessages.Launcher_Launch, configuration);

    var oneCConfiguration = LaunchHelper.getTargetConfiguration(configuration);
    var oneCConfigurationCopy = oneCConfiguration.copy("YAX: " + configuration.getName()); //$NON-NLS-1$

    configure(oneCConfigurationCopy, configuration);
    copyAttributes(configuration, oneCConfigurationCopy);

    oneCConfigurationCopy.launch(launchMode, SubMonitor.convert(monitor, 1));
  }

  @Override
  public void configure(ILaunchConfigurationWorkingCopy oneCConfiguration, ILaunchConfiguration unitConfiguration) {
    var workPath = LaunchHelper.getWorkPath(unitConfiguration);
    String extensionName = LaunchConfigurationAttributes.getTestExtensionName(unitConfiguration);

    String startupParameters = RUN_PARAMETERS + createConfig(unitConfiguration, workPath);
    oneCConfiguration.setAttribute(ILaunchConfigurationAttributes.STARTUP_OPTION, startupParameters);
    oneCConfiguration.setAttribute(LaunchConfigurationAttributes.WORK_PATH, workPath.toString());
    oneCConfiguration.setAttribute(LaunchConfigurationAttributes.PROJECT, extensionName);
    oneCConfiguration.setAttribute(LaunchConfigurationAttributes.ATTR_TEST_RUNNER_KIND, TestKindRegistry.YAXUNIT_TEST_KIND_ID);
  }

  protected String createConfig(ILaunchConfiguration configuration, Path workPath) {
    File file = new File(workPath.toFile(), PARAMETERS_FILE_NAME); //$NON-NLS-1$

    JsonObject config = new JsonObject();

    setFilter(config, configuration);

    config.addProperty("reportPath", workPath.toString()); //$NON-NLS-1$
    config.addProperty("reportFormat", REPORT_FORMAT); //$NON-NLS-1$
    config.addProperty("closeAfterTests", true); //$NON-NLS-1$

    try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
      Gson gson = new GsonBuilder().create();
      gson.toJson(config, writer);
    } catch (Exception e) {
      TestViewerPlugin.log().logError(e);
    }
    TestViewerPlugin.log().debug(JUnitMessages.Launcher_ConfigurationLocation, file);
    return file.toString();
  }

  protected void setFilter(JsonObject config, ILaunchConfiguration configuration) {
    String testModuleName = LaunchConfigurationAttributes.getTestModuleName(configuration);
    IExtensionProject extension = LaunchHelper.getTestExtension(configuration);
    List<String> tests = LaunchConfigurationAttributes.getTestMethods(configuration);

    if (tests != null && tests.size() == 1) {
      String[] chunks = tests.get(0).split("\\."); //$NON-NLS-1$
      if (chunks.length == 2 && chunks[1].equalsIgnoreCase(TestFinder.REGISTRATION_METHOD_NAME)) {
        testModuleName = chunks[0];
        tests = Collections.emptyList();
      }
    }
    JsonObject filter = new JsonObject();

    if (testModuleName != null && !testModuleName.isBlank()) {
      JsonArray array = new JsonArray();
      array.add(testModuleName);
      filter.add("modules", array); //$NON-NLS-1$
    }

    if (extension != null) {
      JsonArray array = new JsonArray();
      array.add(Projects.getProjectName(extension));
      filter.add("extensions", array); //$NON-NLS-1$
    }

    if (tests != null && !tests.isEmpty()) {
      JsonArray array = new JsonArray();
      tests.forEach(array::add);
      filter.add("tests", array); //$NON-NLS-1$
    }

    config.add("filter", filter); //$NON-NLS-1$
  }

  protected void copyAttributes(ILaunchConfiguration unitConfiguration, ILaunchConfigurationWorkingCopy oneCConfiguration) {
    String[] attributes = new String[]{
        LaunchConfigurationAttributes.TEST_EXTENSION
    };

    for (String attribute : attributes) {
      String value = LaunchConfigurationAttributes.getAttribute(unitConfiguration, attribute);
      if (!Strings.isNullOrEmpty(value)) {
        oneCConfiguration.setAttribute(attribute, value);
      }
    }
  }
}
