/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.report.actions;

import ru.biatech.edt.junit.kinds.TestKindRegistry;
import ru.biatech.edt.junit.model.ITestCaseElement;
import ru.biatech.edt.junit.services.TestsManager;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.editor.ReferencedMethodHelper;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

/**
 * Действие для перехода к проверяемому методу
 */
public class OpenUnderTestMethodAction extends OpenEditorAction {

  public OpenUnderTestMethodAction(TestRunnerViewPart testRunnerPart, ITestCaseElement testCase) {
    super(UIMessages.OpenUnderTestMethodAction_label, ImageProvider.ACTION_GOTO_METHOD, testRunnerPart, testCase.getClassName());
  }

  @Override
  public void run() {
    var testKind = TestKindRegistry.getContainerTestKind(getLaunchedProject());
    var moduleName = TestsManager.getTestModuleName(fClassName);
    var methodName = TestsManager.getTestMethodName(fClassName);

    var list = testKind.getFinder().findTestedMethod(moduleName, methodName);
    ReferencedMethodHelper.displayMethod(list, UIMessages.OpenUnderTestMethodAction_error_not_found);
  }
}
