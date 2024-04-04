/*******************************************************************************
 * Copyright (c) 2024 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.report.actions;

import lombok.experimental.UtilityClass;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import ru.biatech.edt.junit.TestViewerPlugin;

@UtilityClass
public class ActionsSupport {
  /**
   * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
   * are retrieved from the *lcl16 folders.
   *
   * @param action   the action
   * @param iconName the icon name
   */
  public void setLocalImageDescriptors(IAction action, String iconName) {
    setImageDescriptors(action, "lcl16", iconName); //$NON-NLS-1$
  }

  private void setImageDescriptors(IAction action, String type, String relPath) {
    ImageDescriptor id = getImageDescriptor("d" + type, relPath, false); //$NON-NLS-1$
    if (id != null)
      action.setDisabledImageDescriptor(id);

    ImageDescriptor descriptor = getImageDescriptor("e" + type, relPath, true); //$NON-NLS-1$
    action.setHoverImageDescriptor(descriptor);
    action.setImageDescriptor(descriptor);
  }

  public ImageDescriptor getImageDescriptor(String relativePath) {
    return TestViewerPlugin.getDefault().createImageDescriptor(relativePath, true);
  }

  public ImageDescriptor getImageDescriptor(String pathPrefix, String imageName, boolean useMissingImageDescriptor) {
    return TestViewerPlugin.getDefault().createImageDescriptor(pathPrefix, imageName, useMissingImageDescriptor);
  }
}
