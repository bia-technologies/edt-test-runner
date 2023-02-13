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

import com._1c.g5.v8.dt.bsl.core.IBslConstruct;
import com._1c.g5.v8.dt.bsl.core.IMethod;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.xtext.ui.editor.XtextEditor;

import java.util.List;

/**
 * Абстрактный класс для действий с методами редактора
 */
public abstract class OnMethodAction extends Action implements IActionDelegate {

  private IStructuredSelection selection;

  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      this.selection = (IStructuredSelection) selection;
      List<?> elements = ((IStructuredSelection) selection).toList();
      this.setEnabled(elements.size() == 1 && elements.get(0) instanceof IMethod);
    } else {
      this.setEnabled(false);
    }
  }

  @Override
  public void run(IAction action) {
    run();
  }

  @Override
  public void run() {
    var editor = getEditor();

    if (editor == null) {
      return;
    }

    var method = getMethod();

    runOnMethod(editor, method.getName());
  }

  protected IBslConstruct getMethod() {
    if (selection == null || selection.isEmpty()) {
      return null;
    }

    var elements = this.selection.toList();
    return (IBslConstruct) elements.get(0);
  }

  protected XtextEditor getEditor() {
    return Helper.getActiveBslEditor();
  }

  protected abstract void runOnMethod(XtextEditor editor, String methodName);

}
