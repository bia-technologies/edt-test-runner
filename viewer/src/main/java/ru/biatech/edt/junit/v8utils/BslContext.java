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

package ru.biatech.edt.junit.v8utils;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.ui.contentassist.BslProposalProvider;
import com._1c.g5.v8.dt.bsl.util.BslUtil;
import com._1c.g5.v8.dt.mcore.Event;
import lombok.experimental.UtilityClass;
import org.eclipse.xtext.resource.IResourceServiceProvider;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BslContext {
  private BslProposalProvider proposalProvider;
  private boolean isRussian;

  public List<EventData> getEvents(Module module) {
    var events = getProposalProvider(module).getAllCorrectEvent(module);
    var result = new ArrayList<EventData>();
    for (var item : events) {
      var event = createEvent(item.getFirst());
      event.handler = item.getSecond();
      result.add(event);
    }
    return result;
  }

  private synchronized BslProposalProvider getProposalProvider(Module module) {
    if (proposalProvider == null) {
      IResourceServiceProvider resourceProvider = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(module.eResource().getURI());
      proposalProvider = resourceProvider.get(BslProposalProvider.class);
      isRussian = BslUtil.isRussian(module, VendorServices.getProjectManager());
    }
    return proposalProvider;
  }

  EventData createEvent(Event rawEvent) {
    var event = new EventData();
    event.event = rawEvent;
    event.name = !isRussian ? rawEvent.getName() : rawEvent.getNameRu();

    for (var paramSet : rawEvent.getParamSet()) {
      if (paramSet.getParams() != null && !paramSet.getParams().isEmpty()) {
        for (var param : paramSet.getParams()) {
          String nameParam = !isRussian ? param.getName() : param.getNameRu();
          event.parameters.add(nameParam);
        }
      }
    }
    return event;
  }
}
