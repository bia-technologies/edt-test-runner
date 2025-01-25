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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = HelloMessage.class, name = Message.HELLO_TYPE),
    @JsonSubTypes.Type(value = RunMessage.class, name = Message.RUN_TEST_TYPE),
    @JsonSubTypes.Type(value = ReportMessage.class, name = Message.REPORT_TYPE),
    @JsonSubTypes.Type(value = ReportFileMessage.class, name = Message.REPORT_FILE_TYPE)
})
public abstract class Message<T> {
  public static final String
      HELLO_TYPE = "hello",
      RUN_TEST_TYPE = "runTest",
      REPORT_TYPE = "report",
      REPORT_FILE_TYPE = "reportFile";

  String type;
  int id;
  T data;

  public Message(String type) {
    this.type = type;
  }
}
