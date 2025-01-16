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

import lombok.Setter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.ui.utils.StringUtilities;

import java.util.function.Consumer;

public class ElementSelectPopupDialog<T> extends PopupDialog {
  private final String title;
  private final Consumer<T> handler;
  IBaseLabelProvider labelProvider;
  IContentProvider contentProvider;
  private TableViewer completionsTable = null;
  @Setter
  private T[] input;
  private final ImageProvider imageProvider;

  public ElementSelectPopupDialog(Shell parent, String title, String info, IBaseLabelProvider labelProvider, IContentProvider contentProvider, Consumer<T> handler) {
    super(parent, HOVER_SHELLSTYLE | SWT.RESIZE, true, true, true, false, false, StringUtilities.EMPTY_STRING, info);
    this.labelProvider = labelProvider;
    this.contentProvider = contentProvider;
    this.title = title;
    this.handler = handler;
    imageProvider = new ImageProvider();
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    completionsTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
    completionsTable.setUseHashlookup(true);
    completionsTable.setLabelProvider(labelProvider);
    completionsTable.setContentProvider(contentProvider);

    var gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = completionsTable.getTable().getItemHeight() * 12;
    completionsTable.getTable().setLayoutData(gridData);
    
    completionsTable.getTable().setBackground(parent.getForeground());
    completionsTable.getTable().setLinesVisible(false);
    completionsTable.addOpenListener(event -> onSelect());
    completionsTable.getTable().addKeyListener(KeyListener.keyPressedAdapter(keyEvent -> {
      if (keyEvent.character == 0x1B) {// ESC{
        close();
      }
    }));

    Dialog.applyDialogFont(parent);
    if (input != null) {
      completionsTable.setInput(input);
    }

    return completionsTable.getTable();
  }

  @Override
  protected Control getFocusControl() {
    return completionsTable.getControl();
  }

  @Override
  protected IDialogSettings getDialogSettings() {
    var sectionName = "PopupSelection"; //$NON-NLS-1$
    var settings = TestViewerPlugin.getDefault().getDialogSettings().getSection(sectionName);
    if (settings == null) {
      settings = TestViewerPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
    }
    return settings;
  }

  @Override
  protected Control createTitleControl(Composite parent) {
    var titleLabel = new Label(parent, SWT.NONE);

    GridDataFactory.fillDefaults()
        .align(SWT.LEFT, SWT.CENTER)
        .indent(4, 4)
        .applyTo(titleLabel);

    titleLabel.setImage(imageProvider.getLogo());
    titleLabel = new Label(parent, SWT.NONE);

    GridDataFactory.fillDefaults()
        .align(SWT.FILL, SWT.CENTER)
        .indent(16, 0)
        .applyTo(titleLabel);

    if (title != null) {
      titleLabel.setText(title);
    }
    titleLabel.pack();
    return titleLabel;
  }

  private void onSelect() {
    @SuppressWarnings("unchecked")
    var result = (T) completionsTable.getStructuredSelection().getFirstElement();
    close();
    handler.accept(result);
  }

  @Override
  public boolean close() {
    imageProvider.dispose();
    return super.close();
  }
}
