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

package ru.biatech.edt.junit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Serializer {

  @Getter
  private final ObjectMapper jsonMapper = new ObjectMapper()
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  @Getter
  ObjectMapper xmlMapper = new XmlMapper()
      .setDefaultUseWrapper(false)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
}
