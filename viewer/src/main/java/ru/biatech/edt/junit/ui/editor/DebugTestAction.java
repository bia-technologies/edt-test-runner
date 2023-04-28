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

package ru.biatech.edt.junit.ui.editor;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.xtext.ui.editor.XtextEditor;
import ru.biatech.edt.junit.services.TestsManager;
import ru.biatech.edt.junit.ui.JUnitMessages;

/**
 * Команда отладки теста из редактора
 */
public class DebugTestAction extends OnMethodAction {

  @Override
  protected void runOnMethod(XtextEditor editor, String methodName) {
    var module = Helper.getModule(editor);
    if (TestsManager.isTestMethod(module, methodName)) {
      TestsManager.runTestMethod(module, methodName, ILaunchManager.DEBUG_MODE);
    } else {
      MessageDialog.openError(editor.getShell(),
          JUnitMessages.LaunchTest_dialog_title,
          JUnitMessages.LaunchTest_error_is_not_test);
    }
  }
}
