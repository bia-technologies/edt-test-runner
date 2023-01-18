/*******************************************************************************
 * Copyright (c) 2021-2022 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.launcher.ui;

import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;

@SuppressWarnings("restriction")
public class UtilsUI {

  public static void setValueSource(ComboViewer control, Iterable<?> values, IBaseLabelProvider labelProvider) {

    control.setContentProvider(ArrayContentProvider.getInstance());
    control.setLabelProvider(labelProvider);
    control.setComparator(new ViewerComparator());
    control.setInput(values);
  }

  public static void setValueSource(ComboViewer control, Iterable<?> values) {
    setValueSource(control, values, new DefaultLabelProvider());
  }

  public static void setSelection(ComboViewer control, Object value) {
    if (value != null)
      control.setSelection(new StructuredSelection(value));
    else
      control.setSelection(StructuredSelection.EMPTY);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getSelection(ComboViewer control, Class<T> clazz) {
    IStructuredSelection selection = control.getStructuredSelection();
    return !selection.isEmpty() && clazz.isInstance(selection.getFirstElement()) ? (T) selection.getFirstElement() : null;
  }
}
