/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * Copyright (c) 2021-2022 BIA-Technologies Limited Liability Company.
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
 *     David Saff (saff@mit.edu) - initial API and implementation
 *             (bug 102632: [JUnit] Support for JUnit 4.)
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.kinds;


public interface ITestKind {
  class NullTestKind extends TestKind {
    private NullTestKind() {
      super(null);
    }

    @Override
    public boolean isNull() {
      return true;
    }

    @Override
    protected String getAttribute(String arg0) {
      return null;
    }

    @Override
    public ITestFinder getFinder() {
      return ITestFinder.NULL;
    }
  }

  TestKind NULL = new NullTestKind();

  String ID = "id"; //$NON-NLS-1$
  String DISPLAY_NAME = "displayName"; //$NON-NLS-1$
  String FINDER_CLASS_NAME = "finderClass"; //$NON-NLS-1$
  String LAUNCHER_CLASS_NAME = "launcherClass"; //$NON-NLS-1$

  ITestFinder getFinder();

  IUnitLauncher getLauncher();

  String getId();

  String getDisplayName();

  boolean isNull();
}