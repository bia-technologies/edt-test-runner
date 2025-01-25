/*******************************************************************************
 * Copyright (c) 2021-2022 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.launcher.v8;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import ru.biatech.edt.junit.kinds.TestKindRegistry;
import ru.biatech.edt.junit.ui.UIMessages;

public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

    monitor.beginTask(UIMessages.LaunchConfigurationDelegate_Launching, 1);

    if (monitor.isCanceled())
      return;

    LaunchHelper.checkConfiguration(configuration);

    var project = LaunchHelper.getTestExtension(configuration);
    var kind = TestKindRegistry.getContainerTestKind(project);

    kind.getLauncher().launch(configuration, mode, launch, monitor);

    monitor.done();
  }
}
