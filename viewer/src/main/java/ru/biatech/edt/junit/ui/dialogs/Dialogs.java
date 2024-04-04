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

package ru.biatech.edt.junit.ui.dialogs;

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import lombok.experimental.UtilityClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.labelProvider.CommonLabelProvider;
import ru.biatech.edt.junit.ui.labelProvider.MethodLabelProvider;
import ru.biatech.edt.junit.ui.labelProvider.MethodReferenceLabelProvider;
import ru.biatech.edt.junit.ui.labelProvider.TestItemActionLabelProvider;
import ru.biatech.edt.junit.ui.testitemaction.ITestItemAction;
import ru.biatech.edt.junit.ui.viewsupport.ColoringLabelProvider;
import ru.biatech.edt.junit.utils.StringUtilities;
import ru.biatech.edt.junit.v8utils.EventData;
import ru.biatech.edt.junit.v8utils.MethodReference;
import ru.biatech.edt.junit.v8utils.Present;
import ru.biatech.edt.junit.yaxunit.Engine;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class Dialogs {

  public Optional<IExtensionProject> selectProject(List<IExtensionProject> projects, String message) {
    if (projects.size() == 1) {
      return Optional.of(projects.get(0));
    }

    var projectsMap = projects.stream().collect(Collectors.toMap(IV8Project::getProject, Function.identity()));
    var shell = TestViewerPlugin.ui().getActiveWorkbenchShell();
    if (projects.size() > 1) {
      var result = selectItem(projectsMap.keySet(), Messages.Dialogs_Select_Project_Title, message, null);
      if (result != null) {
        return Optional.of(projectsMap.get(result));
      }
    } else {
      var notFoundMessage = Messages.Message_RelatedProjectsNotFound;
      MessageDialog.openInformation(shell, Messages.Dialogs_Information_Title, notFoundMessage);
    }

    return Optional.empty();
  }

  public Optional<IExtensionProject> selectTestProject(EObject source) {
    var projectsStream = source != null ? Engine.getTestProjects(source) : Engine.getTestProjects();
    String message = MessageFormat.format(Messages.Dialogs_Select_TestProject_ForTestSuite, Present.getPresent(source));
    return selectProject(projectsStream.collect(Collectors.toList()), message);
  }

  public MethodReference selectMethodReference(List<MethodReference> items) {
    return selectItem(items, Messages.Dialogs_Select_Method_Title, Messages.Dialogs_Select_Method_Message, new MethodReferenceLabelProvider());
  }

  public static Module selectModule(List<Module> items) {
    return selectItem(items, Messages.Dialogs_Select_Module_Title, Messages.Dialogs_Select_Module_Message,
        new CommonLabelProvider());
  }

  public void executeTestAction(List<ITestItemAction> items, Module module, String methodName) {
    if (items == null || items.isEmpty()) {
      return;
    }
    var shell = TestViewerPlugin.ui().getActiveWorkbenchShell();

    if (methodName == null) {
      methodName = StringUtilities.EMPTY_STRING;
    }
    var message = MessageFormat.format(Messages.Dialogs_Select_Action_Message, Present.getPresent(module), methodName);
    var dialog = new ElementSelectPopupDialog<>(shell,
        Messages.Dialogs_Select_Action_Title,
        message,
        new ColoringLabelProvider(new TestItemActionLabelProvider()),
        new ArrayContentProvider(),
        ITestItemAction::run);

    dialog.setInput(items.toArray(ITestItemAction[]::new));
    dialog.open();
  }

  public void showWarning(String title, String message) {
    MessageDialog.openWarning(TestViewerPlugin.ui().getShell(), title, message);
  }

  public Optional<List<Method>> selectMethodsForTesting(List<Method> items, String description) {
    return selectItems(items, description, Method.class);
  }

  private <T> T selectItem(Collection<T> items, String title, String message, ILabelProvider labelProvider) {
    return selectItem(items, title, message, labelProvider, null);
  }

  @SuppressWarnings("unchecked")
  private <T> T selectItem(Collection<T> items, String title, String message, ILabelProvider labelProvider, Image image) {

    var shell = TestViewerPlugin.ui().getActiveWorkbenchShell();

    if (labelProvider == null) {
      labelProvider = new WorkbenchLabelProvider();
    }
    var dialog = new ElementListSelectionDialog(shell, labelProvider);
    dialog.setElements(items.toArray());
    dialog.setMultipleSelection(false);
    dialog.setTitle(title);
    dialog.setMessage(message);
    dialog.setHelpAvailable(false);
    if (image != null) {
      dialog.setImage(image);
    }
    int result = dialog.open();
    labelProvider.dispose();
    if (result == Dialog.OK && dialog.getResult() != null && dialog.getResult().length > 0) {
      return (T) dialog.getResult()[0];
    } else {
      return null;
    }
  }

  public Optional<List<EventData>> selectEvents(List<EventData> items, String description) {
    return selectItems(items, description, EventData.class);
  }

  private <T> Optional<List<T>> selectItems(List<T> items, String description, Class<T> clazz) {
    var shell = TestViewerPlugin.ui().getActiveWorkbenchShell();
    var dialog = new CheckedTreeSelectionDialog(shell, new MethodLabelProvider(), new FlatTree());
    dialog.setTitle(MessageFormat.format(Messages.Dialogs_Select_Methods_ForTestSuite, description));
    dialog.setHelpAvailable(false);

    dialog.setInput(items.toArray());

    if (dialog.open() == Dialog.OK) {
      var result = dialog.getResult();
      if (result.length > 0) {
        var methods = Arrays.stream(dialog.getResult())
            .map(clazz::cast)
            .collect(Collectors.toList());

        return Optional.of(methods);
      }
    }
    return Optional.empty();
  }

  private class FlatTree implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      return (Object[]) inputElement;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
      return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
      return null;
    }

    @Override
    public boolean hasChildren(Object element) {
      return false;
    }
  }
}
