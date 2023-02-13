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

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.ui.editor.BslXtextDocument;
import com._1c.g5.v8.dt.bsl.ui.menu.BslHandlerUtil;
import com._1c.g5.v8.dt.lcore.nodemodel.util.CustomNodeModelUtils;
import lombok.experimental.UtilityClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.XtextEditor;

/**
 * Класс-помощник работы с редактором
 */
@UtilityClass
public class Helper {

  /**
   * Возвращает активный редактор кода
   * @return редактор
   */
  public XtextEditor getActiveBslEditor(){
    return BslHandlerUtil.extractXtextEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
  }

  /**
   * Возвращает AST модуля открытого в редакторе
   * @param editor
   * @return
   */
  public Module getModule(XtextEditor editor){
    return getDocument(editor).readOnlyDataModel(state -> {
      EObject root = state.getParseResult().getRootASTElement();
      return (Module) root;
    });
  }

  /**
   * Возвращает ссылку метод из текущей позиции курсора
   * @param editor редактор
   * @return метод
   */
  public Method getNearestMethod(XtextEditor editor) {
    ITextViewer viewer = BslHandlerUtil.getTextViewer(editor);
    var module = Helper.getModule(editor);

    INode moduleNode = NodeModelUtils.findActualNodeFor(module);
    ILeafNode node = CustomNodeModelUtils.findLeafNodeAtOffset(moduleNode, viewer.getSelectedRange().x);
    EObject actualObject = NodeModelUtils.findActualSemanticObjectFor(node);
    return actualObject instanceof Method ? (Method)actualObject : (Method) EcoreUtil2.getContainerOfType(actualObject, Method.class);
  }

  private BslXtextDocument getDocument(XtextEditor editor) {
    return (BslXtextDocument) editor.getDocumentProvider().getDocument(editor.getEditorInput());
  }

}
