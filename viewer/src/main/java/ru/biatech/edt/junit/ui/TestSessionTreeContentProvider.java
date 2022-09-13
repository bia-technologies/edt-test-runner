/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.model.TestRoot;
import ru.biatech.edt.junit.model.TestSuiteElement;


public class TestSessionTreeContentProvider implements ITreeContentProvider {

  private final Object[] NO_CHILDREN = new Object[0];

  @Override
  public void dispose() {
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof TestSuiteElement)
      return ((TestSuiteElement) parentElement).getChildren();
    else
      return NO_CHILDREN;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    return ((TestRoot) inputElement).getChildren();
  }

  @Override
  public Object getParent(Object element) {
    return ((TestElement) element).getParent();
  }

  @Override
  public boolean hasChildren(Object element) {
    if (element instanceof TestSuiteElement)
      return ((TestSuiteElement) element).getChildren().length != 0;
    else
      return false;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
}
