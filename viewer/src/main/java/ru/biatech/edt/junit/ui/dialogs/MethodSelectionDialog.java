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

package ru.biatech.edt.junit.ui.dialogs;

import com._1c.g5.v8.dt.ui.util.Labeler;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.v8utils.MethodReference;

import java.util.List;

/**
 * Диалог выбора метода
 */
public class MethodSelectionDialog extends ElementListSelectionDialog {

  public static MethodReference chooseItem(List<MethodReference> modules) {
    var shell = TestViewerPlugin.ui().getActiveWorkbenchShell();
    var dialog = new MethodSelectionDialog(shell, modules);
    int result = dialog.open();
    if (result == Window.OK) {
      return dialog.getSelectedItem();
    } else {
      return null;
    }
  }

  public MethodSelectionDialog(Shell shell, List<MethodReference> modules) {
    super(shell, new MethodSelectionDialog.ObjectModuleLabelProvider());
    this.setElements(modules.toArray(MethodReference[]::new));
    this.setTitle("Object modules");
    this.setMessage("Select module to open:");
    this.setMultipleSelection(false);
  }

  /**
   * Возвращает ссылку выбранный метод
   * @return ссылка выбранный метод
   */
  public MethodReference getSelectedItem() {
    return (MethodReference) getFirstResult();
  }

  private static class ObjectModuleLabelProvider extends LabelProvider {
    private ObjectModuleLabelProvider() {
    }

    @Override
    public String getText(Object element) {
      if (element instanceof MethodReference) {
        var ref = (MethodReference) element;
        var present =
            Labeler.path(ref.getModule(), '.')
                .skipCommonNode()
                .stopAfter(IProject.class)
                .label();
        if (ref.getMethod() != null) {
          present += "." + ref.getMethod().getName();
        }

        return present;
      } else {
        return super.getText(element);
      }
    }
  }
}