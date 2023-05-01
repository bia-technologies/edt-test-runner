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

import lombok.SneakyThrows;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.TestRunSession;

import java.util.List;

public class RerunHelper {

  public static final String PREFIX_RERUN = "Rerun ";

  public static boolean isNotRerunConfiguration(ILaunchConfiguration launchConfiguration) {
    var attribute = LaunchConfigurationAttributes.getTestMethods(launchConfiguration);
    return attribute == null;
  }

  @SneakyThrows
  public static void rerunTest(TestRunSession session, String testClassName, String launchMode) {
    if (isBadSession(session)) {
      return;
    }

    var configName = PREFIX_RERUN + testClassName;
    rerunTests(session, List.of(testClassName), configName, launchMode);
  }

  @SneakyThrows
  public static void rerun(TestRunSession session) {
    if (isBadSession(session)) {
      return;
    }

    var configuration = session.getLaunch().getLaunchConfiguration();

    if (isNotRerunConfiguration(configuration)) {
      var configName = PREFIX_RERUN + configuration.getName();
      configuration = configuration.copy(configName);
    }
    DebugUITools.launch(configuration, session.getLaunch().getLaunchMode());
  }

  @SneakyThrows
  public static void rerunFailures(TestRunSession session) {
    if (isBadSession(session)) {
      return;
    }

    var configuration = session.getLaunch().getLaunchConfiguration();
    var failedTest = session.getAllFailedTestNames();

    var configName = configuration.getName();
    if (isNotRerunConfiguration(configuration)) {
      configName = PREFIX_RERUN + configName;
    }

    rerunTests(session, failedTest, configName, session.getLaunch().getLaunchMode());
  }

  @SneakyThrows
  private static boolean isBadSession(TestRunSession session) {
    if (session == null || session.getLaunch() == null) {
      return true;
    }
    var configuration = session.getLaunch().getLaunchConfiguration();

    if (!LaunchHelper.isRunTestConfiguration(configuration)) {
      TestViewerPlugin.log().logError("Некорректная конфигурация запуска. Должна быть передана конфигурация запуска тестов, а пришла " + configuration.getType().getIdentifier());
      return true;
    }
    return false;
  }

  @SneakyThrows
  private static void rerunTests(TestRunSession session, List<String> tests, String configName, String launchMode) {
    if (tests == null || tests.isEmpty()) {
      return;
    }

    var configurationCopy = session.getLaunch().getLaunchConfiguration().copy(configName);
    LaunchConfigurationAttributes.setTestMethods(configurationCopy, tests);

    DebugUITools.launch(configurationCopy, launchMode);

  }
}
