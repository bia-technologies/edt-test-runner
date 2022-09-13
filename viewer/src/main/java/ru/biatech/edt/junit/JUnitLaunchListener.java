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

package ru.biatech.edt.junit;

import com.google.common.base.Strings;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IProcess;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.model.JUnitModel;
import ru.biatech.edt.junit.ui.JUnitMessages;

import java.util.Arrays;
import java.util.HashSet;

public class JUnitLaunchListener implements ILaunchListener, IDebugEventSetListener {

  /**
   * Used to track new launches. We need to do this
   * so that we only attach a TestRunner once to a launch.
   * Once a test runner is connected, it is removed from the set.
   */
  private final HashSet<ILaunch> fTrackedLaunches = new HashSet<>(20);
  private final HashSet<IProcess> fTrackedProcesses = new HashSet<>(20);

  @Override
  public void launchAdded(ILaunch launch) {
    var configuration = launch.getLaunchConfiguration();
    if (LaunchHelper.isOnecConfiguration(configuration) && !Strings.isNullOrEmpty(LaunchConfigurationAttributes.getTestKind(configuration))) {
      fTrackedLaunches.add(launch);
    }
  }

  @Override
  public void launchRemoved(final ILaunch launch) {
    fTrackedLaunches.remove(launch);
    for (IProcess process : launch.getProcesses()) {
      fTrackedProcesses.remove(process);
    }
  }

  @Override
  public void launchChanged(final ILaunch launch) {
    if (!fTrackedLaunches.contains(launch))
      return;

    var processes = launch.getProcesses();
    if (processes.length != 0) {
      fTrackedLaunches.remove(launch);
      fTrackedProcesses.addAll(Arrays.asList(processes));
    }
  }

  @Override
  public void handleDebugEvents(DebugEvent[] events) {
    for (var event : events) {
      if (event.getKind() != DebugEvent.TERMINATE || !(event.getSource() instanceof IProcess)) {
        continue;
      }
      IProcess process = (IProcess) event.getSource();

      if (fTrackedProcesses.contains(process)) {
        onTerminate(process);
      }
    }
  }

  private void onTerminate(IProcess process) {
    fTrackedProcesses.remove(process);
    var launch = process.getLaunch();
    int exitCode = 0;
    try {
      exitCode = process.getExitValue();
    } catch (DebugException e) { /* do nothing*/ }
    if (exitCode != 0) {
      TestViewerPlugin.log().debug(JUnitMessages.JUnitLaunchListener_ProcessError,
              launch.getLaunchConfiguration().getName(), process.getLabel(), exitCode);
    }
    JUnitModel.loadTestReport(launch);
  }
}