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

import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import lombok.Getter;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.ui.editor.UIHelper;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.ui.viewsupport.LabelStylerFactory;

import java.text.MessageFormat;

@Getter
public class GotoTestSuiteAction implements ITestItemAction {

  private final CommonModule testSuite;

  public GotoTestSuiteAction(CommonModule testSuite) {
    this.testSuite = testSuite;
  }

  @Override
  public String getPresent() {
    return MessageFormat.format(INDENT + Messages.GotoTestSuiteAction_Present, testSuite.getName());
  }

  @Override
  public Image getIcon(ImageProvider provider) {
    return provider.getGotoTestSuite();
  }

  @Override
  public StyledString getStyledString() {
    return LabelStylerFactory.format(INDENT + Messages.GotoTestSuiteAction_Present, StyledString.COUNTER_STYLER, testSuite.getName());
  }

  @Override
  public void run() {
    UIHelper.openModuleEditor(testSuite.getModule());
  }
}
