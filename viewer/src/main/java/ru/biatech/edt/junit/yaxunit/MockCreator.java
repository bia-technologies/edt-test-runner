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

package ru.biatech.edt.junit.yaxunit;

import com._1c.g5.v8.dt.bsl.model.Function;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.ModuleType;
import com._1c.g5.v8.dt.bsl.services.BslGrammarAccess;
import com._1c.g5.v8.dt.bsl.ui.BslGeneratorMultiLangProposals;
import com._1c.g5.v8.dt.bsl.ui.contentassist.BslProposalProvider;
import com._1c.g5.v8.dt.bsl.util.BslUtil;
import com._1c.g5.v8.dt.common.PreferenceUtils;
import com._1c.g5.v8.dt.core.platform.IConfigurationProject;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.mcore.NamedElement;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import ru.biatech.edt.junit.templates.TemplatesProvider;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.ui.dialogs.Messages;
import ru.biatech.edt.junit.ui.editor.EditionContext;
import ru.biatech.edt.junit.ui.editor.EditorHelper;
import ru.biatech.edt.junit.ui.editor.UIHelper;
import ru.biatech.edt.junit.v8utils.Present;
import ru.biatech.edt.junit.v8utils.Projects;
import ru.biatech.edt.junit.v8utils.VendorServices;

import java.text.MessageFormat;
import java.util.stream.Collectors;

/**
 * Модуль помощник для гереации моков
 */
public class MockCreator {
  private final Module module;
  private final Method method;
  private final BslGeneratorMultiLangProposals bslGenProp;
  private final BslGrammarAccess bslGrammar;
  private final boolean isRussian;
  private final boolean isFunction;
  private String lineFormatter;

  public MockCreator(Module module, String methodName) {
    this.module = module;
    method = module.allMethods().stream()
        .filter(m -> m.getName().equals(methodName))
        .findFirst()
        .orElseThrow();
    isFunction = method instanceof Function;
    isRussian = BslUtil.isRussian(module, VendorServices.getProjectManager());

    var resourceProvider = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(module.eResource().getURI());
    bslGrammar = resourceProvider.get(BslGrammarAccess.class);
    bslGenProp = resourceProvider.get(BslGeneratorMultiLangProposals.class);
    bslGenProp.setRussianLang(isRussian);
  }

  public void createMock() throws CoreException {
    var parent = Projects.getParentProject(module);
    var relatedProjects = Projects.getRelatedExtensions((IConfigurationProject) parent).collect(Collectors.toList());
    var project = Dialogs.selectProject(relatedProjects, MessageFormat.format(Messages.Dialogs_Select_Project_ForMock, Present.getPresent(module)));
    if (project.isEmpty()) {
      return;
    }

    var extensionModule = (Module) adopt(module, project.get());
    var editorPart = UIHelper.openModuleEditor(extensionModule);
    var editor = EditorHelper.getEditor(editorPart);

    extensionModule = EditorHelper.getParsedModule(editor);

    var prefix = isRussian ? "Мок_" : "Mock_";
    var newMethodName = prefix + method.getName();

    var existMethod = extensionModule.allMethods().stream()
        .filter(m -> m.getName().equalsIgnoreCase(newMethodName))
        .findFirst();

    var content = createContent(project.get(), newMethodName, existMethod.isEmpty());

    var editorContext = new EditionContext(editor);
    if (existMethod.isPresent()) {
      var node = NodeModelUtils.findActualNodeFor(extensionModule);
      editorContext.replace(node.getOffset(), node.getLength(), content);
    } else {
      editorContext.append(content);
    }
    editorContext.apply();
    editorContext.revealLast();
  }

  private EObject adopt(EObject modelObject, IExtensionProject target) throws CoreException {

    var modelObjectAdopter = VendorServices.getModelObjectAdopter();
    if (modelObjectAdopter.isAdopted(modelObject, target)) {
      return modelObjectAdopter.getAdopted(modelObject, target);
    } else {
      return modelObjectAdopter.adoptAndAttach(modelObject, target, new NullProgressMonitor());
    }
  }

  private String createContent(IExtensionProject project, String newMethodName, boolean appendFirstLine) {
    lineFormatter = PreferenceUtils.getLineSeparator(project.getProject());

    StringBuilder builder = new StringBuilder();

    if (appendFirstLine) {
      builder.append(lineFormatter);
    }

    appendAroundAnnotation(builder);
    appendPragma(builder);
    appendSignature(builder, newMethodName);

    appendMockito(builder, project);

    appendEndMethod(builder);
    if (appendFirstLine) {
      builder.append(lineFormatter);
    }
    return builder.toString();
  }

  private void appendAroundAnnotation(StringBuilder builder) {
    builder.append('&').append(isRussian ? "Вместо" : "Around");
    builder.append(this.bslGenProp.getOpenBracketPropStr()).append(this.bslGenProp.getQuotePropStr()).append(method.getName()).append(this.bslGenProp.getQuotePropStr()).append(this.bslGenProp.getCloseBracketPropStr());
  }

  private void appendPragma(StringBuilder builder) {
    method.getPragmas().forEach(p -> builder.append(lineFormatter).append('&').append(p.getSymbol()));
  }

  private void appendSignature(StringBuilder builder, String newMethodName) {
    builder.append(lineFormatter).append(BslProposalProvider.getTypeMethodName(this.bslGrammar, isFunction, isRussian));
    builder.append(this.bslGenProp.getSpacePropStr());
    builder.append(newMethodName);
    builder.append(this.bslGenProp.getOpenBracketPropStr());

    builder.append(method.getFormalParams().stream()
        .map(p -> p.isByValue() ? BslProposalProvider.getByValueLiteralName(this.bslGrammar, isRussian) + this.bslGenProp.getSpacePropStr() + p.getName() : p.getName())
        .collect(Collectors.joining(bslGenProp.getCommaPropStr())));
    builder.append(this.bslGenProp.getCloseBracketPropStr());
    if (method.isExport()) {
      builder.append(this.bslGenProp.getSpacePropStr()).append(BslProposalProvider.getExportLiteralName(this.bslGrammar, isRussian));
    }
  }

  private void appendEndMethod(StringBuilder builder) {
    builder.append(lineFormatter).append(BslProposalProvider.getTypeEndMethodName(this.bslGrammar, isFunction, isRussian));
  }

  private void appendMockito(StringBuilder builder, IExtensionProject project) {
    var template = isFunction ? TemplatesProvider.getMockFunctionTemplateString(project) : TemplatesProvider.getMockProcedureTemplateString(project);
    if (template.isEmpty()) {
      return;
    }
    var modulePresent = getModulePresent();
    var methodPresent = method.getName();
    var paramsPresent = method.getFormalParams().stream()
        .map(NamedElement::getName)
        .collect(Collectors.joining(bslGenProp.getCommaPropStr()));
    var content = MessageFormat.format(template.get(), modulePresent, methodPresent, paramsPresent);
    builder.append(lineFormatter);
    builder.append(content);
  }

  private String getModulePresent() {
    var moduleType = module.getModuleType();
    if (moduleType == ModuleType.COMMON_MODULE) {
      return ((CommonModule) module.getOwner()).getName();
    } else if (moduleType == ModuleType.MANAGER_MODULE) {
      return Present.getPresent((MdObject) module.getOwner());
    } else {
      return isRussian ? "ЭтотОбъект" : "ThisObject";
    }
  }
}
