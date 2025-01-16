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


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import static ru.biatech.edt.junit.yaxunit.remote.dto.Message.HELLO_TYPE;

@JsonTypeName(HELLO_TYPE)
public class HelloMessage extends Message<HelloMessage.Params> {


  public HelloMessage() {
    super(HELLO_TYPE);
  }

  @Data
  public static class Params {
    String key;
    String protocolVersion;
  }
}
