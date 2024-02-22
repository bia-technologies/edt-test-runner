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

package ru.biatech.edt.junit.ui.editor;

import com._1c.g5.v8.dt.bsl.model.Module;
import lombok.experimental.UtilityClass;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import ru.biatech.edt.junit.v8utils.VendorServices;

@UtilityClass
public class UIHelper {
  public IEditorPart openModuleEditor(Module module) {
    return VendorServices.getOpenHelper().openEditor(EcoreUtil.getURI(module), null);
  }

  public void openModuleEditor(Module module, int line) {
    IEditorPart editorPart = openModuleEditor(module);
    var editor = EditorHelper.getEditor(editorPart);
    if (editor != null) {
      editor.setFocus();
    }
    if (editor != null && line >= 0) {
      positionEditor(editor, line);
    }
  }

  public void openModuleEditor(Module module, String methodName) {
    IEditorPart editorPart = openModuleEditor(module);
    var editor = EditorHelper.getEditor(editorPart);
    if (editor == null) {
      return;
    }
    editor.setFocus();

    EditorHelper.getMethodOffset(editor, methodName)
        .ifPresent(offset -> editor.selectAndReveal(offset, 0));
  }

  public void positionEditor(ITextEditor editor, int lineNumber) {
    IRegion region = getLineInformation(editor, lineNumber);
    if (region != null) {
      editor.selectAndReveal(region.getOffset(), 0);
    }
  }

  private IRegion getLineInformation(ITextEditor editor, int lineNumber) {
    IDocumentProvider provider = editor.getDocumentProvider();
    IEditorInput input = editor.getEditorInput();

    try {
      provider.connect(input);
    } catch (CoreException e) {
      return null;
    }

    try {
      IDocument document = provider.getDocument(input);
      if (document == null) {
        return null;
      }

      return document.getLineInformation(lineNumber);
    } catch (BadLocationException ignored) {
    } finally {
      provider.disconnect(input);
    }
    return null;
  }
}
