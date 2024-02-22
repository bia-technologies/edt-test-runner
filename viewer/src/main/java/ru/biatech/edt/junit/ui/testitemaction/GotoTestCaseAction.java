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
public class GotoTestCaseAction implements ITestItemAction {
  private final CommonModule testSuite;
  private final String testCase;

  public GotoTestCaseAction(CommonModule testSuite, String testCase) {
    this.testSuite = testSuite;
    this.testCase = testCase;
  }

  @Override
  public String getPresent() {
    var fullName = testSuite.getName() + '.' + testCase;
    return MessageFormat.format(INDENT + Messages.GotoTestCaseAction_Present, fullName);
  }

  @Override
  public Image getIcon(ImageProvider provider) {
    return provider.getGotoTestCase();
  }

  @Override
  public StyledString getStyledString() {
    var fullName = testSuite.getName() + '.' + testCase;
    return LabelStylerFactory.format(INDENT + Messages.GotoTestCaseAction_Present, StyledString.COUNTER_STYLER, fullName);
  }

  @Override
  public void run() {
    UIHelper.openModuleEditor(testSuite.getModule(), testCase);
  }
}
