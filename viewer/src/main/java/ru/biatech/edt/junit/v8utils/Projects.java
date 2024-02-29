/*******************************************************************************
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.v8utils;

import com._1c.g5.v8.dt.core.platform.IConfigurationAware;
import com._1c.g5.v8.dt.core.platform.IConfigurationProject;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import lombok.experimental.UtilityClass;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Помощник работы с проектами
 */
@UtilityClass
public class Projects {

  /**
   * Возвращает проект по имени
   *
   * @param name имя проекта
   * @return проект
   */
  public IV8Project getProject(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    var project = VendorServices.getProjectManager().getProject(name);

    if (project == null) {
      project = Projects.getExtensions()
          .stream()
          .filter(it -> Projects.getProjectName(it).equals(name))
          .findFirst()
          .orElse(null);
    }
    return project;
  }

  /**
   * Возвращает проект, к которому принадлежит объект
   *
   * @param object объект проекта
   * @return проект
   */
  public IV8Project getParentProject(EObject object) {
    return VendorServices.getProjectManager().getProject(object);
  }

  /**
   * Возвращает проект конфигурации рабочей области
   *
   * @return проект конфигурации
   */
  public IConfigurationProject getConfiguration() {
    var projects = VendorServices.getProjectManager().getProjects(IConfigurationProject.class);
    if (projects.isEmpty()) {
      return null;
    } else {
      return projects.iterator().next();
    }
  }

  /**
   * Возвращает проекты расширения рабочей области
   *
   * @return
   */
  public Collection<IExtensionProject> getExtensions() {
    return VendorServices.getProjectManager().getProjects(IExtensionProject.class);
  }

  /**
   * Возвращает имя проекта, как обо будет выглядеть в конфигураторе
   *
   * @param project проект
   * @return имя проекта
   */
  public String getProjectName(IV8Project project) {
    if (project instanceof IConfigurationAware) {
      return ((IConfigurationAware) project).getConfiguration().getName();
    } else {
      return project.getDtProject().getName();
    }
  }

  public Stream<IExtensionProject> getRelatedExtensions(IConfigurationProject project) {
    var v8projectManager = VendorServices.getProjectManager();

    return v8projectManager.getProjects(IExtensionProject.class)
        .stream()
        .filter((candidate) -> Objects.equals(candidate.getParentProject(), project.getProject()));
  }
}
