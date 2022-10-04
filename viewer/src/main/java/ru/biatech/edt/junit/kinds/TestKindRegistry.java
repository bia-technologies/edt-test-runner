/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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

import com._1c.g5.v8.dt.core.platform.IV8Project;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import ru.biatech.edt.junit.JUnitCore;

import java.util.ArrayList;
import java.util.Collections;

public class TestKindRegistry {

  public static final String YAXUNIT_TEST_KIND_ID = "ru.biatech.edt.junit.loader.yaxunit"; //$NON-NLS-1$
  private static TestKindRegistry fgRegistry;
  private final IExtensionPoint fPoint;
  private ArrayList<TestKind> fTestKinds;

  private TestKindRegistry(IExtensionPoint point) {
    fPoint = point;
  }

  public static TestKindRegistry getDefault() {
    if (fgRegistry != null) return fgRegistry;

    fgRegistry = new TestKindRegistry(Platform.getExtensionRegistry().getExtensionPoint(JUnitCore.ID_EXTENSION_POINT_TEST_KINDS));
    return fgRegistry;
  }

  public static String getContainerTestKindId(IV8Project element) {
    // TODO
    return YAXUNIT_TEST_KIND_ID;
  }

  public static ITestKind getContainerTestKind(IV8Project element) {
    return getDefault().getKind(getContainerTestKindId(element));
  }

  public ArrayList<TestKind> getAllKinds() {
    loadKinds();
    return fTestKinds;
  }

  private void loadKinds() {
    if (fTestKinds != null) return;

    ArrayList<TestKind> items = new ArrayList<>();
    for (IConfigurationElement configurationElement : getConfigurationElements()) {
      items.add(new TestKind(configurationElement));
    }

    fTestKinds = items;
  }

  /**
   * @param testKindId an id, can be <code>null</code>
   * @return a TestKind, ITestKind.NULL if not available
   */
  public ITestKind getKind(String testKindId) {
    if (testKindId != null) {
      for (TestKind kind : getAllKinds()) {
        if (testKindId.equals(kind.getId())) return kind;
      }
    }
    return ITestKind.NULL;
  }

  private ArrayList<IConfigurationElement> getConfigurationElements() {
    ArrayList<IConfigurationElement> items = new ArrayList<>();
    for (IExtension extension : fPoint.getExtensions()) {
      Collections.addAll(items, extension.getConfigurationElements());
    }
    return items;
  }
}
