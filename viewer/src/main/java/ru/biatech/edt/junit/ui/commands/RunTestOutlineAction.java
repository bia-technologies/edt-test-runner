/*******************************************************************************
 * Copyright (c) 2023-2024 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.commands;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.xtext.ui.editor.XtextEditor;
import ru.biatech.edt.junit.services.TestsManager;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.editor.EditorHelper;

/**
 * Команда запуска теста из редактора
 */
public class RunTestOutlineAction extends OnMethodAction {

  @Override
  protected void runOnMethod(XtextEditor editor, String methodName) {
    var module = EditorHelper.getModule(editor);
    if (TestsManager.isTestMethod(module, methodName)) {
      TestsManager.runTestMethod(module, methodName, ILaunchManager.RUN_MODE);
    } else {
      MessageDialog.openError(editor.getShell(),
          JUnitMessages.LaunchTest_title,
          JUnitMessages.LaunchTest_error_is_not_test);
    }
  }
}
