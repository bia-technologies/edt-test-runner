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

import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.SimpleStatement;
import com._1c.g5.v8.dt.bsl.model.StaticFeatureAccess;
import com._1c.g5.v8.dt.bsl.ui.editor.BslXtextDocument;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import lombok.experimental.UtilityClass;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.ui.editor.XtextEditor;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.dialogs.Dialogs;
import ru.biatech.edt.junit.ui.editor.EditorHelper;
import ru.biatech.edt.junit.ui.editor.UIHelper;
import ru.biatech.edt.junit.v8utils.Modules;
import ru.biatech.edt.junit.v8utils.Projects;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Модуль-помощник для создания тестов.
 * Инкапсулирует всю логику создания, от формирования имени тестового модуля/метода до открытия редактора и позиционирования в нем.
 */
@UtilityClass
public class TestCreator {

  public void createNewTestCase(Module module, String methodName) throws InterruptedException {
    var testSuite = Dialogs.selectTestProject(module, null)
        .flatMap(p -> getOrCreateTestSuite(p, module));

    if (testSuite.isPresent()) {
      var editorPart = UIHelper.openModuleEditor(testSuite.get().getModule());
      var editor = EditorHelper.getEditor(editorPart);
      appendTestMethod(methodName, editor);
    }
  }

  public static void createNewTestCaseIn(Module testSuite, String methodName) {
    var editorPart = UIHelper.openModuleEditor(testSuite);
    var editor = EditorHelper.getEditor(editorPart);

    appendTestMethod(methodName, editor);
  }

  public void createTestSuite(Module module, String[] methodsName) throws InterruptedException {
    var testSuite = Dialogs.selectTestProject(module, null)
        .flatMap(p -> getOrCreateTestSuite(p, module));

    if (testSuite.isPresent()) {
      var editorPart = UIHelper.openModuleEditor(testSuite.get().getModule());
      var editor = EditorHelper.getEditor(editorPart);
      appendTestMethods(methodsName, editor);
    }
  }

  public void updateTestSuite(CommonModule testSuite, String[] methodsNames) {
    var editorPart = UIHelper.openModuleEditor(testSuite.getModule());
    var editor = EditorHelper.getEditor(editorPart);

    appendTestMethods(methodsNames, editor);
  }

  private String newTestSuiteRegistrationMethod(EditionContext context, String[] methodNames) {
    var testCasesRegistration = CodeGenerator.testCasesRegistration(methodNames);
    String content = null;
    var template = CodeGenerator.testSuiteTemplate(context.project);
    var registrationMethod = template
        .map(StringReader::new)
        .map(context.getParser()::parse)
        .map(IParseResult::getRootASTElement)
        .map(Module.class::cast)
        .flatMap(TestCreator::getRegistrationMethod);
    if (registrationMethod.isPresent()) {
      var node = NodeModelUtils.findActualNodeFor(registrationMethod.get());
      var start = node.getOffset();
      var end = node.getTotalEndOffset();
      var regOffset = getRegistrationOffset(registrationMethod.get());
      if (regOffset.isPresent()) {
        var registrationMethodBody = template.get().substring(start, end);

        content = new StringBuffer(registrationMethodBody).insert(regOffset.get() - start, testCasesRegistration).toString();
      }
    }
    var ls = System.lineSeparator();
    if (content == null) {
      content =
          "Процедура ИсполняемыеСценарии() Экспорт\n\t\n\tЮТТесты" + testCasesRegistration //$NON-NLS-1$
              + "\n\t;\n\t\nКонецПроцедуры"; //$NON-NLS-1$
      var linuxLS = "\n"; //$NON-NLS-1$
      if (!ls.equals(linuxLS)) {
        content = content.replace(linuxLS, ls);
      }
    }

    return content + ls;
  }

  private boolean createTestSuiteRegistrationMethod(EditionContext context, String[] methodNames) {
    String content = newTestSuiteRegistrationMethod(context, methodNames);
    var offset = EditorHelper.getStartContentOffset(context.module);
    context.insert(offset, content);
    return true;
  }

  private void appendTestMethods(String[] methodNames, XtextEditor editor) {
    var context = new EditionContext(editor);

    var registrationMethod = getRegistrationMethod(context.module);
    var registrationSuccess = registrationMethod.isEmpty() ?
        createTestSuiteRegistrationMethod(context, methodNames) :
        appendTestMethodsRegistration(context, methodNames);

    var methodsSuccess = appendTestMethodsImplementation(context, methodNames);
    var applySuccess = (registrationSuccess || methodsSuccess) && context.apply();

    StringBuilder messages = new StringBuilder();

    if (!applySuccess) {
      messages.append(Messages.TestsFactory_ApplyFailed);
    } else {
      if (!registrationSuccess) {
        messages.append(Messages.TestsFactory_TestRegistration_NeedManually);
      }
      if (!methodsSuccess) {
        messages.append(Messages.TestsFactory_TestImplementation_NeedManually);
      }
    }

    if (messages.length() > 0) {
      var ls = System.lineSeparator();
      messages.append(ls).append(ls).append(Messages.TestsFactory_AutomationFailed);
      MessageDialog.openWarning(null, Messages.TestsFactory_TestImplementation_Failed, messages.toString());
    }
    if (applySuccess) {
      editor.selectAndReveal(context.lastOffset(), 0);
    }
  }

  private void appendTestMethod(String methodName, XtextEditor editor) {
    appendTestMethods(new String[]{methodName}, editor);
  }

  private boolean appendTestMethodsImplementation(EditionContext context, String[] methodsNames) {
    var offset = getTestMethodInsertOffset(context.module);
    var content = CodeGenerator.testCasesImplementation(context.project, methodsNames);
    if (content == null) {
      return false;
    }

    context.insert(offset, content);
    return true;
  }

  private boolean appendTestMethodsRegistration(EditionContext context, String[] methodNames) {
    var offset = getRegistrationOffset(context.module);
    if (offset.isEmpty()) {
      return false;
    }
    var content = CodeGenerator.testCasesRegistration(methodNames);
    context.insert(offset.get(), content);
    return true;
  }

  private Optional<CommonModule> getOrCreateTestSuite(IExtensionProject testsProject, Module baseModule) {
    AtomicReference<CommonModule> testSuiteRef = new AtomicReference<>();

    var description = Messages.TestsFactory_CreatingTestModule;
    var job = Job.create(description, monitor -> {
      testSuiteRef.set(getOrCreateTestSuite(testsProject, baseModule, monitor));
      return Status.OK_STATUS;
    });

    job.setUser(true);
    job.schedule();

    try {
      job.join();
    } catch (InterruptedException e) {
      TestViewerPlugin.log().logError(e);
      return Optional.empty();
    }
    return Optional.ofNullable(testSuiteRef.get());
  }

  private CommonModule getOrCreateTestSuite(IExtensionProject testsProject, Module module, IProgressMonitor monitor) {
    var testSuiteName = Engine.getTestSuiteName(module);
    return Modules.findCommonModule(testsProject, testSuiteName)
        .orElseGet(() -> createTestSuiteModule(testsProject, module, monitor));
  }

  private Optional<Method> getRegistrationMethod(Module module) {
    var rootMethod = module.allMethods().stream()
        .filter(Engine::isRegistrationTestsMethod)
        .findFirst();

    if (rootMethod.isEmpty()) {
      TestViewerPlugin.log().logError(MessageFormat.format(Messages.TestsFactory_MethodNotFound, Constants.REGISTRATION_METHOD_NAME));
    }
    return rootMethod;
  }

  private boolean isTestRegistration(Invocation invocation) {
    String methodName = invocation.getMethodAccess().getName();
    return Constants.TEST_REGISTRATION_METHOD_NAMES.contains(methodName.toLowerCase()) && !invocation.getParams().isEmpty();
  }

  private Optional<Integer> getRegistrationOffset(Module module) {
    return getRegistrationMethod(module)
        .flatMap(TestCreator::getRegistrationOffset);
  }

  private Optional<Integer> getRegistrationOffset(Method method) {
    List<SimpleStatement> statements = method.getStatements()
        .stream()
        .filter(SimpleStatement.class::isInstance)
        .map(SimpleStatement.class::cast)
        .collect(Collectors.toList());

    var node = statements.stream() // Поиск ветки оканчивающейся на ДобавитьТест и тд
        .map(st -> st.getLeft() instanceof Invocation ? st.getLeft() : st.getRight())
        .filter(Invocation.class::isInstance)
        .map(Invocation.class::cast)
        .filter(TestCreator::isTestRegistration)
        .map(NodeModelUtils::findActualNodeFor)
        .findFirst();
    if (node.isEmpty()) {
      node = statements.stream() // Поиск прямого вызова ЮТТесты
          .filter(TestCreator::isRegistrationAccess)
          .map(NodeModelUtils::findActualNodeFor)
          .findFirst();
    }
    if (node.isEmpty()) {
      node = statements.stream()// Поиск цепочки вызовов от ЮТТесты
          .map(st -> st.getLeft() instanceof Invocation ? st.getLeft() : st.getRight())
          .filter(Invocation.class::isInstance)
          .map(Invocation.class::cast)
          .filter(TestCreator::isRegistrationAccess)
          .map(NodeModelUtils::findActualNodeFor)
          .findFirst();
    }

    return node.map(INode::getTotalEndOffset);
  }

  private boolean isRegistrationAccess(SimpleStatement item) {
    if (item.getLeft() instanceof StaticFeatureAccess) {
      return ((StaticFeatureAccess) item.getLeft()).getName().equalsIgnoreCase(Constants.REGISTRATION_MODULE_NAME);
    }
    return false;
  }

  private boolean isRegistrationAccess(Invocation invocation) {
    var ma = invocation.getMethodAccess();
    Expression source = null;
    if (ma instanceof DynamicFeatureAccess) {
      source = ((DynamicFeatureAccess) ma).getSource();
    } else if (ma instanceof StaticFeatureAccess) {
      source = ma;
    }

    if (source instanceof StaticFeatureAccess) {
      return ((StaticFeatureAccess) source).getName().equalsIgnoreCase(Constants.REGISTRATION_MODULE_NAME);
    } else if (source instanceof Invocation) {
      return isRegistrationAccess((Invocation) source);
    } else {
      return false;
    }
  }

  private int getTestMethodInsertOffset(Module module) {
    return module.allMethods().stream()
        .filter(Method::isExport)
        .reduce((m1, m2) -> m2)
        .map(NodeModelUtils::findActualNodeFor)
        .map(INode::getTotalEndOffset)
        .orElse(0);
  }

  private CommonModule createTestSuiteModule(IExtensionProject testsProject, Module module, IProgressMonitor monitor) {
    var newTestSuite = Engine.createTestSuite(testsProject, module);
    if (monitor.isCanceled()) {
      return null;
    }
    CodeGenerator.createTestSuiteStructure(testsProject, newTestSuite, monitor);
    return newTestSuite;
  }

  private static class EditionContext {
    private final BslXtextDocument document;
    private final Module module;
    private final IV8Project project;
    private final List<InsertEdit> changes = new ArrayList<>();
    private IParser parser;

    public EditionContext(XtextEditor editor) {
      document = EditorHelper.getDocument(editor);
      module = EditorHelper.getParsedModule(document);
      project = Projects.getParentProject(module);
    }

    public IParser getParser() {
      if (parser == null) {
        document.readOnly(state -> parser = state.getParser());
      }
      return parser;
    }

    public void insert(int offset, String text) {
      changes.add(new InsertEdit(offset, text));
    }

    public int lastOffset() {
      var ordered = changes.stream()
          .sorted(Comparator.comparingInt(TextEdit::getOffset))
          .toArray(TextEdit[]::new);
      return ordered[ordered.length - 1].getOffset() + 2;

    }

    public boolean apply() {
      return EditorHelper.applyChanges(document, changes);
    }
  }
}