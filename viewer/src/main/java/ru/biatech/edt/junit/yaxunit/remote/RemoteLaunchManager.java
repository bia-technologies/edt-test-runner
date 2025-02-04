/*******************************************************************************
 * Copyright (c) 2025 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.yaxunit.remote;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import ru.biatech.edt.junit.model.TestSuiteElement;
import ru.biatech.edt.junit.yaxunit.LaunchSettings;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class RemoteLaunchManager {
  @Getter
  RemoteLauncher launcher;

  public synchronized void start() {
    if (launcher == null) {
      launcher = new RemoteLauncherImpl();
      launcher.start();
    }
  }

  public synchronized void stop() {
    if (launcher != null) {
      launcher.stop();
      launcher = null;
    }
  }

  public CompletableFuture<TestSuiteElement[]> launchTest(String module, String moduleName, List<String> methods, boolean server, boolean client, boolean ordinaryClient) throws ClientNotFound{
    return launcher.launchTest(module, moduleName, methods, server, client, ordinaryClient);
  }

  public boolean isAvailable() {
    return launcher != null && launcher.isAvailable();
  }

  public void configureLaunch(LaunchSettings.RpcSettings rpc) {
    if (launcher == null) {
      return;
    }
    rpc.setEnable(true);
    rpc.setPort(launcher.getPort());
    rpc.setKey(UUID.randomUUID().toString());
    rpc.setTransport("ws");
  }
}
