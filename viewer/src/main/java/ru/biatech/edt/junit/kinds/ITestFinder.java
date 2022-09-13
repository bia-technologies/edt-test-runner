/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.Collection;
import java.util.Collections;

/**
 * Interface to be implemented by for extension point
 * org.eclipse.jdt.junit.internal_testKinds.
 */
public interface ITestFinder {

  ITestFinder NULL = new ITestFinder() {
    @Override
    public Collection<Method> findTestsInContainer(Module module, IProgressMonitor pm) {
      return Collections.emptyList();
    }

    @Override
    public boolean isTestModule(Module module) {
      return false;
    }
  };

  /**
   * @param module element to search for tests
   * @param pm     the progress monitor
   * @throws CoreException thrown when tests can not be found
   */
  Collection<Method> findTestsInContainer(Module module, IProgressMonitor pm) throws CoreException;

  boolean isTestModule(Module module);
}
