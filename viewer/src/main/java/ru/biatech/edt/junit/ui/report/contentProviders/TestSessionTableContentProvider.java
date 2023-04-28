/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.report.contentProviders;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.TestCaseElement;
import ru.biatech.edt.junit.model.TestRoot;
import ru.biatech.edt.junit.model.TestSuiteElement;

import java.util.ArrayList;

/**
 * Провайдер, для отображения дерева тестов в таблице
 */
public class TestSessionTableContentProvider implements IStructuredContentProvider {

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public Object[] getElements(Object inputElement) {
    ArrayList<ITestElement> all = new ArrayList<>();
    addAll(all, (TestRoot) inputElement);
    return all.toArray();
  }

  private void addAll(ArrayList<ITestElement> all, TestSuiteElement suite) {
    ITestElement[] children = suite.getChildren();
    for (ITestElement element : children) {
      if (element instanceof TestSuiteElement) {
        if (((TestSuiteElement) element).getSuiteStatus().isErrorOrFailure())
          all.add(element); // add failed suite to flat list too
        addAll(all, (TestSuiteElement) element);
      } else if (element instanceof TestCaseElement) {
        all.add(element);
      }
    }
  }

  @Override
  public void dispose() {
  }
}
