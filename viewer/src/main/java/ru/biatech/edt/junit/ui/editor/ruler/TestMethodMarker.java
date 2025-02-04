/*******************************************************************************
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.editor.ruler;

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.validation.CustomValidationMessageAcceptor;
import com._1c.g5.v8.dt.bsl.validation.IExternalBslValidator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.services.TestsManager;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.v8utils.MdUtils;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestMethodMarker implements IExternalBslValidator {

  @Override
  public boolean needValidation(EObject object) {
    return object instanceof Module && TestsManager.isTestModule((Module) object);
  }

  @Override
  @Check(CheckType.EXPENSIVE)
  public void validate(EObject object, CustomValidationMessageAcceptor messageAcceptor, CancelIndicator monitor) {

    if (monitor.isCanceled()) {
      return;
    }

    Module module = (Module) object;
    IResource resource = MdUtils.getResource(object);
    try {
      resource.deleteMarkers(RulerAttributes.MARKER_ID, false, 0);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(UIMessages.TestMethodMarker_MarkersCleanError, e);
    }
    Collection<Method> methods = TestsManager.getTestMethods(module);
    methods.forEach(method -> createMarket(resource, method));
  }

  private void createMarket(IResource resource, Method method) {
    var node = NodeModelUtils.findActualNodeFor(method);
    Map<String, Object> attributes = new HashMap<>();
    MarkerUtilities.setLineNumber(attributes, node.getStartLine());
    attributes.put(IMarker.SEVERITY, 1);
    attributes.put(RulerAttributes.ATTRIBUTE_METHOD, method.getName());
    String message = MessageFormat.format(UIMessages.TestMethodMarker_LaunchTest, method.getName());
    MarkerUtilities.setMessage(attributes, message);

    try {
      MarkerUtilities.createMarker(resource, attributes, RulerAttributes.MARKER_ID);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(UIMessages.TestMethodMarker_MarkerCreationError, e);
    }
  }
}
