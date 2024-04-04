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
import com._1c.g5.v8.dt.mcore.Event;
import lombok.experimental.UtilityClass;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.Pair;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class BslContext {
  private BslProposalProvider proposalProvider;

  public List<Event> getEvents(Module module) {
    return getProposalProvider(module).getAllCorrectEvent(module)
        .stream().map(Pair::getFirst)
        .collect(Collectors.toList());
  }

  private synchronized BslProposalProvider getProposalProvider(Module module) {
    if (proposalProvider == null) {
      IResourceServiceProvider resourceProvider = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(module.eResource().getURI());
      proposalProvider = resourceProvider.get(BslProposalProvider.class);
    }
    return proposalProvider;
  }
}
