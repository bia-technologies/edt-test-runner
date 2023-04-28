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

package ru.biatech.edt.junit.launcher.lifecycle;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IProcess;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.ui.JUnitMessages;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@UtilityClass
public class LifecycleMonitor {

  private final List<LifecycleListener> listeners = new ArrayList<>();
  private final LaunchMonitor monitor = new LaunchMonitor();

  public void addListener(LifecycleListener listener) {
    listeners.add(listener);
  }

  public void removeListener(LifecycleListener listener) {
    listeners.remove(listener);
  }

  public void start() {
    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(monitor);
    DebugPlugin.getDefault().addDebugEventListener(monitor);
  }

  public void stop() {
    DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(monitor);
    DebugPlugin.getDefault().removeDebugEventListener(monitor);
  }

  public void removeTerminated() {
    var removed = new HashSet<ILaunch>();
    var manager = DebugPlugin.getDefault().getLaunchManager();
    var elements = manager.getLaunches();
    for (var launch : elements) {
      if (launch.isTerminated() && LaunchHelper.isRunTestConfiguration(launch.getLaunchConfiguration())) {
        removed.add(launch);
      }
    }

    for (var item : monitor.monitoringItems.values()) {
      if (!item.isActive()) {
        removed.add(item.mainLaunch);
        removed.add(item.testLaunch);
      }
    }

    if (!removed.isEmpty()) {
      manager.removeLaunches(removed.toArray(ILaunch[]::new));
      for (var launch : removed) {
        monitor.monitoringItems.remove(launch);
      }
    }
  }

  private void debug(String message) {
    TestViewerPlugin.log().debug(message);
  }

  private void riseEvent(int eventType, MonitoringItem item) {
    TestViewerPlugin.log().debug("Launch event: {0} for {1}", LifecycleEvent.getPresent(eventType), item.name);
    if (LifecycleEvent.isStop(eventType)) {
      try {
        item.testLaunch.terminate();
      } catch (DebugException e) {
        TestViewerPlugin.log().logError("Terminate test launch", e);
      }
    }
    listeners.forEach(l -> l.handle(eventType, item.mainLaunch));
  }

  private void onItemStart(MonitoringItem item) {
    item.active = true;
    item.start = Instant.now();
    riseEvent(LifecycleEvent.START, item);
    removeTerminated();
  }

  private void onItemStop(MonitoringItem item, int eventType) {
    item.active = false;
    item.end = Instant.now();
    riseEvent(eventType, item);
  }

  private static class LaunchMonitor implements ILaunchListener, IDebugEventSetListener {
    ReentrantLock lock = new ReentrantLock();
    Map<ILaunch, MonitoringItem> monitoringItems = new HashMap<>();

    @Override
    public void handleDebugEvents(DebugEvent[] events) {
      for (var event : events) {
        debug("handleDebugEvents " + event.toString());
        if (event.getKind() != DebugEvent.TERMINATE || !(event.getSource() instanceof IProcess)) {
          continue;
        }
        var process = (IProcess) event.getSource();
        var launch = process.getLaunch();
        lock.lock();
        try {
          if (monitoringItems.containsKey(launch)) {
            var item = monitoringItems.get(launch);
            handleProcesses(item);
            if (item.active) {
              debug("Finish " + item.name);
              item.active = false;
              onTerminate(item, process);
            }
          }
        } finally {
          lock.unlock();
        }
      }
    }

    @Override
    public void launchRemoved(ILaunch launch) {
      onLaunchEvent("launchRemoved", launch);
      lock.lock();
      try {
        if (monitoringItems.containsKey(launch)) {
          var item = monitoringItems.get(launch);
          handleProcesses(item);
          if (item.active) {
            debug("canceled " + item.name);
            onItemStop(item, LifecycleEvent.CANCELED);
          }
        }
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void launchAdded(ILaunch launch) {
      onLaunchEvent("launchAdded", launch);
      var configuration = launch.getLaunchConfiguration();

      if (LaunchHelper.isRunTestConfiguration(configuration)) {
        var item = new MonitoringItem();
        item.testLaunch = launch;
        item.name = configuration.getName();
        monitoringItems.put(launch, item);
        onItemStart(item);
      } else if (LaunchHelper.isOnecConfiguration(configuration) && !Strings.isNullOrEmpty(LaunchConfigurationAttributes.getTestKind(configuration))) {
        var name = configuration.getName();
        for (var item : monitoringItems.values()) {
          if (name.contains(item.name)) {
            item.mainLaunch = launch;
            handleProcesses(item);
            monitoringItems.put(launch, item);
            break;
          }
        }
      }
    }

    @Override
    public void launchChanged(ILaunch launch) {
      onLaunchEvent("launchChanged", launch);
      if (monitoringItems.containsKey(launch)) {
        var item = monitoringItems.get(launch);
        handleProcesses(item);
      }
    }

    private void handleProcesses(MonitoringItem item) {
      if (item.testLaunch.getProcesses().length < item.mainLaunch.getProcesses().length) {
        var processes = new HashSet<>(List.of(item.testLaunch.getProcesses()));
        for (var process : item.mainLaunch.getProcesses()) {
          if (!processes.contains(process)) {
            item.testLaunch.addProcess(process);
          }
        }
      }
    }

    @SneakyThrows
    private void onLaunchEvent(String eventName, ILaunch launch) {
      String message = eventName + " " + launch
          + "\ntype: " + launch.getLaunchConfiguration().getType().getIdentifier()
          + "\nname: " + launch.getLaunchConfiguration().getName();
      debug(message);
    }

    private void onTerminate(MonitoringItem item, IProcess process) {
      int exitCode = 0;
      try {
        exitCode = process.getExitValue();
      } catch (DebugException e) { /* do nothing*/ }
      if (exitCode != 0) {
        TestViewerPlugin.log().warning(JUnitMessages.JUnitLaunchListener_ProcessError, item.name, process.getLabel(), exitCode);
        onItemStop(item, LifecycleEvent.FINISHED_WITH_ERROR);
      } else {
        onItemStop(item, LifecycleEvent.FINISHED);
      }
    }
  }

  @Getter
  private static class MonitoringItem {
    private ILaunch testLaunch;
    private ILaunch mainLaunch;
    private String name;
    private boolean active = true;
    private Instant start;
    private Instant end;
  }
}
