/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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
package ru.biatech.edt.junit.model;

public class TestRoot extends TestSuiteElement {

  public TestRoot(ITestRunSession session) {
    super(null, session.getName(), session.getName(), null, null, null); //$NON-NLS-1$
  }

  @Override
  public TestRoot getRoot() {
    return this;
  }
}
