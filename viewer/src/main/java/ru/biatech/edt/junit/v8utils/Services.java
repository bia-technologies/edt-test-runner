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

import com._1c.g5.v8.dt.bm.index.emf.IBmEmfIndexManager;
import com._1c.g5.v8.dt.bsl.model.resource.owner.IBslOwnerComputerService;
import com._1c.g5.v8.dt.core.filesystem.IQualifiedNameFilePathConverter;
import com._1c.g5.v8.dt.core.platform.IResourceLookup;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceParser;
import com._1c.g5.v8.dt.ui.util.OpenHelper;
import ru.biatech.edt.junit.TestViewerPlugin;

public class Services {
  private static OpenHelper openHelper;

  public static IV8ProjectManager getProjectManager() {
    return TestViewerPlugin.getService(IV8ProjectManager.class);
  }

  public static IBmEmfIndexManager getBmEmfIndexManager() {
    return TestViewerPlugin.getService(IBmEmfIndexManager.class);
  }

  public static IResourceLookup getResourceLookup() {
    return TestViewerPlugin.getService(IResourceLookup.class);
  }

  public synchronized static OpenHelper getOpenHelper() {
    if (openHelper != null) {
      return openHelper;
    } else {
      return openHelper = new OpenHelper();
    }
  }

  public static IStacktraceParser getStacktraceParser() {
    return TestViewerPlugin.getService(IStacktraceParser.class);
  }

  public static IQualifiedNameFilePathConverter getQualifiedNameFilePathConverter() {
    return TestViewerPlugin.getService(IQualifiedNameFilePathConverter.class);
  }

  public static IBslOwnerComputerService getBslOwnerComputerService() {
    return TestViewerPlugin.getService(IBslOwnerComputerService.class);
  }
}