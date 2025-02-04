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
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.services.TestsManager;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

public class RunTestCaseAction implements ITestItemAction {
  private final Module module;
  private final String methodName;

  public RunTestCaseAction(Module module, String methodName) {
    this.module = module;
    this.methodName = methodName;
  }

  @Override
  public String getPresent() {
    return UIMessages.Run_Test_Label;
  }

  @Override
  public Image getIcon(ImageProvider provider) {
    return provider.getRunTestIcon();
  }

  @Override
  public StyledString getStyledString() {
    return new StyledString(UIMessages.Run_Test_Label);
  }

  @Override
  public void run() {
    TestsManager.runTestMethod(module, methodName, ILaunchManager.RUN_MODE);
  }
}
