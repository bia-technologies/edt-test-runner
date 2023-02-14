/*******************************************************************************
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.ui.util.Labeler;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import java.util.List;

/**
 * Диалог выбора модуля из списка
 */
public class ModuleSelectionDialog extends ElementListSelectionDialog {

  public ModuleSelectionDialog(Shell shell, List<Module> modules) {
    super(shell, new ObjectModuleLabelProvider());
    this.setElements(modules.toArray(Module[]::new));
    this.setTitle("Object modules");
    this.setMessage("Select module to open:");
    this.setMultipleSelection(false);
  }

  /**
   * Возвращает ссылку на выбранный модуль
   * @return выбранный модуль
   */
  public Module getSelectedItem(){
    return (Module) getFirstResult();
  }

  private static class ObjectModuleLabelProvider extends LabelProvider {
    private ObjectModuleLabelProvider() {
    }

    @Override
    public String getText(Object element) {
      return element instanceof Module ?
          Labeler.path(element, '.')
              .skipCommonNode()
              .stopAfter(IProject.class)
              .label() :
          super.getText(element);
    }
  }
}
