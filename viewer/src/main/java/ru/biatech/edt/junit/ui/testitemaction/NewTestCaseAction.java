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
import lombok.Getter;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.yaxunit.TestCreator;

import java.util.List;

@Getter
public class NewTestCaseAction implements ITestItemAction {
  private final Module module;
  private final String testCaseName;
  private final List<Module> existsSuites;

  public NewTestCaseAction(Module module, String testCaseName, List<Module> existsSuites) {
    this.module = module;
    this.testCaseName = testCaseName;
    this.existsSuites = existsSuites;
  }

  @Override
  public String getPresent() {
    return INDENT + Messages.NewTestCaseAction_Present;
  }

  @Override
  public Image getIcon(ImageProvider provider) {
    return provider.getActionNewTestCase();
  }

  @Override
  public StyledString getStyledString() {
    return new StyledString(getPresent());
  }

  @Override
  public void run() {
    try {
      if (existsSuites.isEmpty()) {
        TestCreator.createNewTestCase(module, testCaseName);
      } else if (existsSuites.size() == 1) {
        TestCreator.createNewTestCaseIn(existsSuites.get(0), testCaseName);
      } else {
        var testSuite = Dialogs.selectModule(existsSuites);
        TestCreator.createNewTestCaseIn(testSuite, testCaseName);
      }
    } catch (InterruptedException e) {
      TestViewerPlugin.log().logError(Messages.NewTestCaseAction_Failed, e);
    }
  }
}
