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

package ru.biatech.edt.junit.ui.testitemaction;

import com._1c.g5.v8.dt.bsl.model.Module;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.v8utils.BslContext;
import ru.biatech.edt.junit.yaxunit.mocks.EventMockDefinition;
import ru.biatech.edt.junit.yaxunit.mocks.MockCreator;
import ru.biatech.edt.junit.yaxunit.mocks.MockDefinition;

import java.util.stream.Collectors;

public class GenerateMockForEvents implements ITestItemAction {
  private final Module module;

  public GenerateMockForEvents(Module module) {
    this.module = module;
  }

  @Override
  public String getPresent() {
    return INDENT + Messages.GenerateMockForEvents_present;
  }

  @Override
  public Image getIcon(ImageProvider provider) {
    return provider.getActionNewMock();
  }

  @Override
  public StyledString getStyledString() {
    return new StyledString(getPresent());
  }

  @Override
  public void run() {
    var events = BslContext.getEvents(module);
    var title = getPresent();

    if (events.isEmpty()) {
      Dialogs.showWarning(Messages.GenerateMockForEvents_present, Messages.GenerateMockForEvents_events_not_found);
      return;
    }

    var eventsForMocking = Dialogs.selectEvents(events, Messages.GenerateMockForEvents_select_event);
    if (eventsForMocking.isEmpty()) {
      return;
    }
    var creator = new MockCreator(module);
    var mocks = eventsForMocking.get().stream()
        .map(EventMockDefinition::new)
        .map(MockDefinition.class::cast)
        .collect(Collectors.toList());

    creator.createMock(mocks);

    if (!creator.getExceptions().isEmpty()) {
      creator.getExceptions().forEach(e -> TestViewerPlugin.log().logError(Messages.GenerateMock_failed_error_prefix, e));
      Dialogs.showError(title, Messages.GenerateMock_failed_message);
    }
  }
}
