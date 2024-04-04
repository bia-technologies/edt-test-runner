/*******************************************************************************
 * Copyright (c) 2024 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.yaxunit.mocks;

import com._1c.g5.v8.dt.bsl.model.Pragma;
import com._1c.g5.v8.dt.bsl.services.BslGrammarAccess;
import com._1c.g5.v8.dt.bsl.ui.BslGeneratorMultiLangProposals;
import com._1c.g5.v8.dt.mcore.Event;
import com._1c.g5.v8.dt.mcore.ParamSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EventMockDefinition implements MockDefinition {
  private final Event event;
  private boolean isRussian;

  public EventMockDefinition(Event event) {
    this.event = event;
  }


  @Override
  public void setIsRussian(boolean value) {
    isRussian = value;
  }

  @Override
  public String getName() {
    return isRussian ? event.getNameRu() : event.getName();
  }

  @Override
  public List<String> getParams() {
    return event.getParamSet().stream()
        .map(ParamSet::getParams)
        .filter(ps -> ps != null && !ps.isEmpty())
        .flatMap(Collection::stream)
        .map(p -> isRussian ? p.getNameRu() : p.getName())
        .collect(Collectors.toList());
  }

  @Override
  public boolean isExport() {
    return false;
  }

  @Override
  public boolean isFunction() {
    return event.isRetVal();
  }

  @Override
  public List<Pragma> getPragmas() {
    return Collections.emptyList();
  }

  @Override
  public String getParamsDefinition(BslGeneratorMultiLangProposals bslGenProp, BslGrammarAccess bslGrammar) {
    return String.join(", ", getParams());
  }
}
