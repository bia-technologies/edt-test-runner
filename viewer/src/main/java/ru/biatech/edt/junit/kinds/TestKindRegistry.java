/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
 *     David Saff (saff@mit.edu) - initial API and implementation
 *             (bug 102632: [JUnit] Support for JUnit 4.)
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.kinds;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import ru.biatech.edt.junit.Constants;
import ru.biatech.edt.junit.v8utils.Projects;

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

    fgRegistry = new TestKindRegistry(Platform.getExtensionRegistry().getExtensionPoint(Constants.ID_EXTENSION_POINT_TEST_KINDS));
    return fgRegistry;
  }

  public static String getContainerTestKindId(IV8Project element) {
    // TODO
    return YAXUNIT_TEST_KIND_ID;
  }

  /**
   * Возвращает описание вида тестового движка для объекта
   * @param module модуль с тестами
   * @return описание тестового движка
   */
  public static ITestKind getContainerTestKind(Module module) {
    var project = Projects.getParentProject(module.getOwner());
    return getContainerTestKind(project);
  }

  /**
   * Возвращает описание вида тестового движка для объекта
   * @param object объект с тестами
   * @return описание тестового движка
   */
  public static ITestKind getContainerTestKind(EObject object) {
    var project = Projects.getParentProject(object);
    return getContainerTestKind(project);
  }

  /**
   * Возвращает описание вида тестового движка для объекта
   * @param project проект с тестами
   * @return описание тестового движка
   */
  public static ITestKind getContainerTestKind(IV8Project project) {
    return getDefault().getKind(getContainerTestKindId(project));
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
