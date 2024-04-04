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

import com._1c.g5.ides.ui.texteditor.xtext.embedded.EmbeddedEditorBuffer;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.ui.editor.BslXtextDocument;
import com._1c.g5.v8.dt.bsl.ui.menu.BslHandlerUtil;
import com._1c.g5.v8.dt.lcore.nodemodel.util.CustomNodeModelUtils;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.ui.editor.input.DtEditorInput;
import lombok.experimental.UtilityClass;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.handly.buffer.BufferChange;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;
import ru.biatech.edt.junit.TestViewerPlugin;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Класс-помощник работы с редактором
 */
@UtilityClass
public class EditorHelper {

  /**
   * Возвращает активный редактор кода
   *
   * @return редактор
   */
  public XtextEditor getActiveBslEditor() {

    return BslHandlerUtil.extractXtextEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
  }

  /**
   * Возвращает AST модуля открытого в редакторе
   *
   * @param editor
   * @return
   */
  public Module getModule(XtextEditor editor) {
    return getModule(getDocument(editor));
  }

  public static Module getParsedModule(XtextEditor editor) {
    return getParsedModule(getDocument(editor));
  }

  public static Module getParsedModule(IEditorPart editorPart) {
    return getParsedModule(getDocument(getEditor(editorPart)));

  }

  public static Module getParsedModule(BslXtextDocument document) {
    return document.readOnly(EditorHelper::getParsedModule);
  }

  public Module getParsedModule(XtextResource res) {
    return (Module) res.getParseResult().getRootASTElement();
  }

  public Module getModule(final IXtextDocument doc) {
    return doc.readOnly(EditorHelper::getModule);
  }

  /**
   * Возвращает ссылку метод из текущей позиции курсора
   *
   * @param editor редактор
   * @return метод
   */

  public Pair<Module, Method> getNearestModuleMethod(XtextEditor editor) {
    ITextViewer viewer = BslHandlerUtil.getTextViewer(editor);
    var module = EditorHelper.getParsedModule(editor);

    var method = getNearestMethod(module, viewer.getSelectedRange().x);
    return Tuples.create(module, method);
  }

  public Method getNearestMethod(Module module, int offset) {
    INode moduleNode = NodeModelUtils.findActualNodeFor(module);
    ILeafNode node = CustomNodeModelUtils.findLeafNodeAtOffset(moduleNode, offset);
    EObject actualObject = NodeModelUtils.findActualSemanticObjectFor(node);
    return actualObject instanceof Method ? (Method) actualObject : (Method) EcoreUtil2.getContainerOfType(actualObject, Method.class);
  }

  public BslXtextDocument getDocument(XtextEditor editor) {
    return (BslXtextDocument) editor.getDocumentProvider().getDocument(editor.getEditorInput());
  }

  public XtextEditor getEditor(IEditorPart editor) {
    XtextEditor textEditor = null;
    if (editor instanceof XtextEditor) {
      textEditor = (XtextEditor) editor;
    } else if (editor != null) {
      textEditor = editor.getAdapter(XtextEditor.class);
    }
    return textEditor;
  }

  public XtextEditor getEditor(ExecutionEvent event) {
    IWorkbenchPart part = HandlerUtil.getActivePart(event);
    return BslHandlerUtil.extractXtextEditor(part);
  }

  public Optional<Integer> getMethodOffset(XtextEditor editor, String name) {
    var editorModule = getParsedModule(editor);
    return editorModule.allMethods().stream()
        .filter(m -> m.getName().equals(name))
        .map(NodeModelUtils::findActualNodeFor)
        .map(INode::getOffset)
        .findFirst();
  }

  public Integer getStartContentOffset(Module module) {
    var root = NodeModelUtils.findActualNodeFor(module);
    for (ILeafNode node : root.getLeafNodes()) {
      if (!node.isHidden()) {
        return 0;
      }

      if (!isCommentNode(node)) {
        return node.getOffset();
      }
    }
    return 0;
  }

  public boolean isCommentNode(ILeafNode leafNode) {
    return leafNode.getGrammarElement() instanceof TerminalRule
        && "SL_COMMENT".equalsIgnoreCase(((TerminalRule) leafNode.getGrammarElement()).getName()); //$NON-NLS-1$
  }

  public boolean applyChanges(IDocument document, List<TextEdit> changes) {

    var textEdit = new MultiTextEdit();
    changes.stream()
        .sorted(Comparator.comparing(TextEdit::getOffset, Comparator.reverseOrder()))
        .forEach(textEdit::addChild);

    try (var buffer = new EmbeddedEditorBuffer(document)) {
      var snapshot = new NonExpiringSnapshot(buffer);
      var bufferChange = new BufferChange(textEdit);
      bufferChange.setBase(snapshot);
      buffer.applyChange(bufferChange, new NullProgressMonitor());
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      return false;
    }
    return true;
  }

  public IEditorPart findOpenedEditor(MdObject object) {

    var editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
    for (var editorRef : editors) {
      var editor = editorRef.getEditor(false);
      IEditorInput input;
      try {
        input = editorRef.getEditorInput();
      } catch (PartInitException e) {
        continue;
      }
      if (editor == null || !(input instanceof DtEditorInput)) {
        continue;
      }

      var model = (MdObject) ((DtEditorInput) input).getModel();
      if (model.equals(object)) {
        return editor;
      }
    }
    return null;
  }

  private Module getModule(XtextResource res) {
    if (res.getContents() != null && !res.getContents().isEmpty()) {
      EObject obj = res.getContents().get(0);
      if (obj instanceof Module) {
        return (Module) obj;
      }
    }
    return null;
  }
}
