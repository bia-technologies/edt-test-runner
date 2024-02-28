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
import com._1c.g5.v8.dt.bsl.ui.editor.BslXtextDocument;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import lombok.Data;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.ui.editor.XtextEditor;
import ru.biatech.edt.junit.v8utils.Projects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class EditionContext {
  private final XtextEditor editor;
  private final BslXtextDocument document;
  private final Module module;
  private final IV8Project project;
  private final List<TextEdit> changes = new ArrayList<>();
  private IParser parser;

  public EditionContext(XtextEditor editor) {
    this.editor = editor;
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

  public void replace(int offset, int length, String content) {
    changes.add(new ReplaceEdit(offset, length, content));
  }

  public void append(String text) {
    changes.add(new InsertEdit(document.getLength(), text));
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

  public void revealLast() {
    editor.selectAndReveal(lastOffset(), 0);
  }

}
