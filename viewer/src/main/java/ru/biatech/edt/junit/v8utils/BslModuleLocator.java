/*******************************************************************************
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

import com._1c.g5.v8.dt.bsl.model.BslFactory;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.resource.owner.IBslOwnerComputerService;
import com._1c.g5.v8.dt.bsl.util.BslUtil;
import com._1c.g5.v8.dt.core.filesystem.IQualifiedNameFilePathConverter;
import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.URIConverter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BslModuleLocator {
  private final IQualifiedNameFilePathConverter qualifiedNameFilePathConverter;
  private final IBslOwnerComputerService bslOwnerComputerService;
  private final IV8ProjectManager v8projectManager;

  public BslModuleLocator() {
    qualifiedNameFilePathConverter = Services.getQualifiedNameFilePathConverter();
    bslOwnerComputerService = Services.getBslOwnerComputerService();
    v8projectManager = Services.getProjectManager();
  }

  public Module getModule(String symlink, IV8Project v8Project, boolean extension) {
    if (v8Project == null) {
      return null;
    } else if (extension && !(v8Project instanceof IExtensionProject)) {
      return null;
    } else if (!extension && v8Project instanceof IExtensionProject) {
      return null;
    } else {
      if (symlink.endsWith(".Form") || symlink.endsWith(".Форма")) {
        symlink = symlink + ".Module";
      }

      if (symlink.indexOf(46) == -1) {
        symlink = "Configuration." + symlink;
      }

      String path = "/" + v8Project.getProject().getName() + "/" + this.qualifiedNameFilePathConverter.getFilePath(symlink);
      URI moduleUri = URI.createPlatformResourceURI(path, true).appendFragment("/0");
      if (URIConverter.INSTANCE.exists(moduleUri, null)) {
        Module module = BslFactory.eINSTANCE.createModule();
        ((InternalEObject) module).eSetProxyURI(moduleUri);
        BslUtil.setModuleType(module, this.qualifiedNameFilePathConverter);
        BslUtil.setModuleOwner(module, this.bslOwnerComputerService);
        return module;
      } else {
        return null;
      }
    }
  }

  public List<Module> getModules(IStacktraceFrame stackFrame) {
    return this.v8projectManager.getProjects().stream()
        .filter((v8Project) -> !stackFrame.isExtension() || v8Project instanceof IExtensionProject)
        .map((v8Project) -> this.getModule(stackFrame.getSymlink(), v8Project, stackFrame.isExtension()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
