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

package ru.biatech.edt.junit.yaxunit.mocks;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.ModuleType;
import com._1c.g5.v8.dt.bsl.services.BslGrammarAccess;
import com._1c.g5.v8.dt.bsl.ui.BslGeneratorMultiLangProposals;
import com._1c.g5.v8.dt.bsl.ui.contentassist.BslProposalProvider;
import com._1c.g5.v8.dt.bsl.util.BslUtil;
import com._1c.g5.v8.dt.common.PreferenceUtils;
import com._1c.g5.v8.dt.core.platform.IConfigurationProject;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import lombok.Getter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.editor.XtextEditor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Модуль помощник для гереации моков
 */
public class MockCreator {
  private final Module module;
  protected final BslGeneratorMultiLangProposals bslGenProp;
  protected final BslGrammarAccess bslGrammar;
  protected final boolean isRussian;
  protected MockDefinition mockDefinition;
  protected String lineSeparator;
  @Getter
  List<Exception> exceptions = new ArrayList<>();
  private IExtensionProject projectForMocks;

  public MockCreator(Module module) {
    this.module = module;
    isRussian = BslUtil.isRussian(module, VendorServices.getProjectManager());

    var resourceProvider = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(module.eResource().getURI());
    bslGrammar = resourceProvider.get(BslGrammarAccess.class);
    bslGenProp = resourceProvider.get(BslGeneratorMultiLangProposals.class);
    bslGenProp.setRussianLang(isRussian);
  }

  public void createMock(MockDefinition mockDefinition) {
    XtextEditor editor;
    if (getProject() == null || (editor = getAdoptEditor()) == null) {
      return;
    }
    var editorContext = new EditionContext(editor);


    this.mockDefinition = mockDefinition;
    mockDefinition.setIsRussian(isRussian);
    createMock(editor, editorContext);
    editorContext.apply();
    editorContext.revealLast();
  }

  public void createMock(List<MockDefinition> mockDefinitions) {
    XtextEditor editor;
    if (mockDefinitions.isEmpty() || getProject() == null || (editor = getAdoptEditor()) == null) {
      return;
    }
    var editorContext = new EditionContext(editor);

    for (MockDefinition definition : mockDefinitions) {
      this.mockDefinition = definition;
      mockDefinition.setIsRussian(isRussian);
      createMock(editor, editorContext);
    }
    editorContext.apply();
    editorContext.revealLast();
  }

  protected String getPrefix() {
    return isRussian ? "Мок_" : "Mock_";
  }

  private IExtensionProject getProject() {
    if (projectForMocks != null) {
      return projectForMocks;
    }
    var parent = Projects.getParentProject(module);
    var relatedProjects = Projects.getRelatedExtensions((IConfigurationProject) parent).collect(Collectors.toList());
    return projectForMocks = Dialogs.selectProject(relatedProjects, MessageFormat.format(Messages.Dialogs_Select_Project_ForMock, Present.getPresent(module))).orElse(null);
  }

  private XtextEditor getAdoptEditor() {
    Module extensionModule;
    try {
      extensionModule = (Module) adopt(module, projectForMocks);
    } catch (CoreException e) {
      exceptions.add(e);
      return null;
    }
    var editorPart = UIHelper.openModuleEditor(extensionModule);
    return EditorHelper.getEditor(editorPart);

  }

  private void createMock(XtextEditor editor, EditionContext editorContext) {
    lineSeparator = PreferenceUtils.getLineSeparator(projectForMocks.getProject());

    var extensionModule = EditorHelper.getParsedModule(editor);

    var prefix = getPrefix();
    var newMethodName = prefix + mockDefinition.getName();

    var existMethod = extensionModule.allMethods().stream()
        .filter(m -> m.getName().equalsIgnoreCase(newMethodName))
        .findFirst();

    var content = createContent(projectForMocks, newMethodName);
    if (existMethod.isPresent()) {
      var node = NodeModelUtils.findActualNodeFor(existMethod.get());
      editorContext.replace(node.getOffset(), node.getLength(), content);
    } else {
      content = lineSeparator + content + lineSeparator;
      editorContext.append(content);
    }
  }

  private EObject adopt(EObject modelObject, IExtensionProject target) throws CoreException {

    var modelObjectAdopter = VendorServices.getModelObjectAdopter();
    if (modelObjectAdopter.isAdopted(modelObject, target)) {
      return modelObjectAdopter.getAdopted(modelObject, target);
    } else {
      return modelObjectAdopter.adoptAndAttach(modelObject, target, new NullProgressMonitor());
    }
  }

  protected String createContent(IExtensionProject project, String newMethodName) {
    StringBuilder builder = new StringBuilder();

    appendAroundAnnotation(builder);
    appendPragma(builder);
    appendSignature(builder, newMethodName);

    appendMockito(builder, project);

    appendEndMethod(builder);

    return builder.toString();
  }

  private void appendAroundAnnotation(StringBuilder builder) {
    builder.append('&').append(isRussian ? "Вместо" : "Around");
    builder.append(this.bslGenProp.getOpenBracketPropStr()).append(this.bslGenProp.getQuotePropStr()).append(mockDefinition.getName()).append(this.bslGenProp.getQuotePropStr()).append(this.bslGenProp.getCloseBracketPropStr());
  }

  private void appendPragma(StringBuilder builder) {
    mockDefinition.getPragmas().forEach(p -> builder.append(lineSeparator).append('&').append(p.getSymbol()));
  }

  private void appendSignature(StringBuilder builder, String newMethodName) {
    builder.append(lineSeparator).append(BslProposalProvider.getTypeMethodName(this.bslGrammar, mockDefinition.isFunction(), isRussian))
        .append(this.bslGenProp.getSpacePropStr())
        .append(newMethodName)
        .append(this.bslGenProp.getOpenBracketPropStr())

        .append(mockDefinition.getParamsDefinition(bslGenProp, bslGrammar))
        .append(this.bslGenProp.getCloseBracketPropStr());
    if (mockDefinition.isExport()) {
      builder.append(this.bslGenProp.getSpacePropStr()).append(BslProposalProvider.getExportLiteralName(this.bslGrammar, isRussian));
    }
  }

  private void appendEndMethod(StringBuilder builder) {
    builder.append(lineSeparator).append(BslProposalProvider.getTypeEndMethodName(this.bslGrammar, mockDefinition.isFunction(), isRussian));
  }

  private void appendMockito(StringBuilder builder, IExtensionProject project) {
    var template = mockDefinition.isFunction() ? TemplatesProvider.getMockFunctionTemplateString(project) : TemplatesProvider.getMockProcedureTemplateString(project);
    if (template.isEmpty()) {
      return;
    }
    var modulePresent = getModulePresent();
    var methodPresent = mockDefinition.getName();
    var paramsPresent = mockDefinition.getParams().stream()
        .collect(Collectors.joining(bslGenProp.getCommaPropStr()));

    var content = MessageFormat.format(template.get(), modulePresent, methodPresent, paramsPresent);
    builder.append(lineSeparator);
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
