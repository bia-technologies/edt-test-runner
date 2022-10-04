/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 *     David Saff (saff@mit.edu) - initial API and implementation
 *             (bug 102632: [JUnit] Support for JUnit 4.)
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.kinds;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.yaxunit.Resolver;

public class TestKind implements ITestKind {

  private final IConfigurationElement fElement;
  private ITestFinder fFinder;
  private IUnitLauncher fLauncher;

  public TestKind(IConfigurationElement element) {
    fElement = element;
    fFinder = null;
    fLauncher = null;
  }

  /*
   * @see ru.biatech.edt.junit.kinds.ITestKind#getFinder()
   */
  @Override
  public ITestFinder getFinder() {
    if (fFinder == null) {
      try {
        fFinder = (ITestFinder) fElement.createExecutableExtension(FINDER_CLASS_NAME);
      } catch (CoreException e1) {
        TestViewerPlugin.log().logError(e1);
        fFinder = ITestFinder.NULL;
      }
    }
    return fFinder;
  }

  /*
   * @see ru.biatech.edt.junit.kinds.ITestKind#getLauncher()
   */
  @Override
  public IUnitLauncher getLauncher() {
    if (fLauncher == null) {
      try {
        fLauncher = (IUnitLauncher) fElement.createExecutableExtension(LAUNCHER_CLASS_NAME);
      } catch (CoreException e1) {
        TestViewerPlugin.log().logError(e1);
        fLauncher = IUnitLauncher.NULL;
      }
    }
    return fLauncher;
  }

  /*
   * @see ru.biatech.edt.junit.kinds.ITestKind#getResolver()
   */
  @Override
  public ITestResolver getResolver() {
    return new Resolver(); // TODO
  }

  /*
   * @see ru.biatech.edt.junit.kinds.ITestKind#getDisplayName()
   */
  @Override
  public String getDisplayName() {
    return getAttribute(DISPLAY_NAME);
  }

  /*
   * @see ru.biatech.edt.junit.kinds.ITestKind#getId()
   */
  @Override
  public String getId() {
    return getAttribute(ID);
  }

  /*
   * @see ru.biatech.edt.junit.kinds.ITestKind#isNull()
   */
  @Override
  public boolean isNull() {
    return false;
  }

  /*
   * @see ru.biatech.edt.junit.kinds.ITestKind#getAttribute()
   */
  protected String getAttribute(String attributeName) {
    return fElement.getAttribute(attributeName);
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getDisplayName() + " (id: " + getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
  }
}
