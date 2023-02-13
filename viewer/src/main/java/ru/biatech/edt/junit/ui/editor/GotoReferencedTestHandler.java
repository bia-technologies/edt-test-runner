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

import com._1c.g5.v8.dt.bsl.ui.menu.BslHandlerUtil;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.XtextEditor;
import ru.biatech.edt.junit.ui.JUnitMessages;

/**
 * Обработчик команды перехода к связанному тесту/тестируемому методу из редактора
 */
public class GotoReferencedTestHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) {
    IWorkbenchPart part = HandlerUtil.getActivePart(event);
    XtextEditor target = BslHandlerUtil.extractXtextEditor(part);

    if (target != null) {
      var method = Helper.getNearestMethod(target);
      if (method == null) {
        MessageDialog.openInformation(target.getShell(),
            JUnitMessages.GotoReferencedTestAction_dialog_title,
            JUnitMessages.GotoReferencedTestAction_error_model_not_available);
      } else {
        ReferencedMethodHelper.transitionTo(target, method);
      }
    }

    return null;
  }
}
