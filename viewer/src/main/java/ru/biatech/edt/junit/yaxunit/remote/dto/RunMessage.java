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

package ru.biatech.edt.junit.yaxunit.remote.dto;

import lombok.Builder;
import lombok.Data;

public class RunMessage extends Message<RunMessage.Params> {
  public RunMessage() {
    super(RUN_TEST_TYPE);
  }

  @Builder
  public RunMessage(String module, String moduleName, String method, boolean server, boolean client, boolean ordinaryClient) {
    this();
    data = new Params();
    data.module = module;
    data.moduleName = moduleName;
    data.method = method;
    data.server = server;
    data.client = client;
    data.ordinaryClient = ordinaryClient;
  }

  @Data
  public static class Params {
    String module;
    String moduleName;
    String method;
    boolean server;
    boolean client;
    boolean ordinaryClient;
  }
}