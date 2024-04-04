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
import ru.biatech.edt.junit.ui.viewsupport.LabelStylerFactory;
import ru.biatech.edt.junit.yaxunit.mocks.MethodMockDefinition;
import ru.biatech.edt.junit.yaxunit.mocks.MockCreator;

import java.text.MessageFormat;

public class GenerateMock implements ITestItemAction {
  private final Module module;
  private final String methodName;

  public GenerateMock(Module module, String methodName) {
    this.module = module;
    this.methodName = methodName;
  }

  @Override
  public String getPresent() {
    return INDENT + MessageFormat.format(Messages.GenerateMock_Present, methodName);
  }

  @Override
  public Image getIcon(ImageProvider provider) {
    return provider.getActionNewMock();
  }

  @Override
  public StyledString getStyledString() {
    return LabelStylerFactory.format(INDENT + Messages.GenerateMock_Present, StyledString.COUNTER_STYLER, methodName);
  }

  @Override
  public void run() {
    var creator = new MockCreator(module);

    creator.createMock(new MethodMockDefinition(module, methodName));
    if (!creator.getExceptions().isEmpty()) {
      creator.getExceptions().forEach(e -> TestViewerPlugin.log().logError(Messages.GenerateMock_failed_error_prefix, e));
      Dialogs.showError(getPresent(), Messages.GenerateMock_failed_message);
    }
  }

}
