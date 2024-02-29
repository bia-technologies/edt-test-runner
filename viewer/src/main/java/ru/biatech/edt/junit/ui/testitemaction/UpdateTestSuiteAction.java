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
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import lombok.Getter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.ui.editor.EditorHelper;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.ui.viewsupport.LabelStylerFactory;
import ru.biatech.edt.junit.v8utils.Present;
import ru.biatech.edt.junit.yaxunit.TestCreator;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@Getter
public class UpdateTestSuiteAction implements ITestItemAction {
  private final Module baseModule;
  private final CommonModule testSuite;

  public UpdateTestSuiteAction(CommonModule testSuite, Module baseModule) {
    this.baseModule = baseModule;
    this.testSuite = testSuite;
  }

  @Override
  public String getPresent() {
    return MessageFormat.format(INDENT + Messages.UpdateTestSuiteAction_Present, Present.getShortPresent(testSuite));
  }

  @Override
  public Image getIcon(ImageProvider provider) {
    return provider.getNewTestSuite();
  }

  @Override
  public StyledString getStyledString() {
    return LabelStylerFactory.format(INDENT + Messages.UpdateTestSuiteAction_Present, StyledString.COUNTER_STYLER, Present.getShortPresent(testSuite));
  }

  @Override
  public void run() {
    var testSuiteEditor = EditorHelper.findOpenedEditor(testSuite);
    Module testSuiteModule = null;
    if (testSuiteEditor != null) { // Если есть открытый активный редактор, то возьмем актуальный модуль из него
      testSuiteModule = EditorHelper.getParsedModule(testSuiteEditor);
    }
    if (testSuiteModule == null) {
      testSuiteModule = testSuite.getModule();
    }

    var existed = new HashMap<String, Method>(); // Не используется коллектор, тк могут быть дубли методов
    testSuiteModule
        .allMethods()
        .stream()
        .filter(Method::isExport)
        .forEach(m -> existed.put(m.getName(), m));

    var methods = baseModule.allMethods()
        .stream()
        .filter(Method::isExport)
        .filter(m -> !existed.containsKey(m.getName()));

    var forSelect = methods.collect(Collectors.toList());

    if (forSelect.isEmpty()) {
      var title = MessageFormat.format(Messages.UpdateTestSuiteAction_Description, Present.getShortPresent(testSuite));
      MessageDialog.openWarning(null, title, Messages.UpdateTestSuiteAction_NoMethodsToAdd);
      return;
    }
    var selected = Dialogs.selectMethodsForTesting(forSelect, Present.getShortPresent(testSuite));
    if (selected.isEmpty()) {
      return;
    }
    var methodNames = Arrays.stream(selected.get())
        .map(Method::getName)
        .toArray(String[]::new);
    TestCreator.updateTestSuite(testSuite, methodNames);
  }
}
