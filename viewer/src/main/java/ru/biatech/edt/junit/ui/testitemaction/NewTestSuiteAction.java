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

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.yaxunit.TestCreator;

import java.util.stream.Collectors;

public class NewTestSuiteAction implements ITestItemAction {
  private final Module baseModule;

  public NewTestSuiteAction(Module baseModule) {
    this.baseModule = baseModule;
  }


  @Override
  public String getPresent() {
    return INDENT + Messages.NewTestSuiteAction_Present;
  }

  @Override
  public Image getIcon(ImageProvider provider) {
    return provider.getNewTestSuite();
  }

  @Override
  public StyledString getStyledString() {
    return new StyledString(getPresent());
  }

  @Override
  public void run() {
    var methods = baseModule.allMethods()
        .stream()
        .filter(Method::isExport);
    var owner = baseModule.getOwner();
    var selected = Dialogs.selectMethodsForTesting(methods.collect(Collectors.toList()), owner.toString());

    if (selected.isEmpty()) {
      return;
    }
    var methodNames = selected.get().stream()
        .map(Method::getName)
        .toArray(String[]::new);

    try {
      TestCreator.createTestSuite(baseModule, methodNames);
    } catch (InterruptedException e) {
      TestViewerPlugin.log().logError(Messages.NewTestSuiteAction_Failed, e);
    }
  }
}
