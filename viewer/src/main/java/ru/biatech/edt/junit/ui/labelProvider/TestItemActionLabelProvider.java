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

package ru.biatech.edt.junit.ui.labelProvider;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.ui.testitemaction.ITestItemAction;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;


public class TestItemActionLabelProvider extends LabelProvider implements DelegatingStyledCellLabelProvider.IStyledLabelProvider {

  private ImageProvider imageProvider = new ImageProvider();

  @Override
  public String getText(Object element) {
    if (element instanceof ITestItemAction) {
      return ((ITestItemAction) element).getPresent();
    }
    return super.getText(element);
  }

  @Override
  public StyledString getStyledText(Object element) {
    if (element instanceof ITestItemAction) {
      return ((ITestItemAction) element).getStyledString();
    }
    return new StyledString(getText(element));
  }

  @Override
  public Image getImage(Object element) {
    if (element instanceof ITestItemAction) {
      return ((ITestItemAction) element).getIcon(imageProvider);
    }
    return null;
  }

  @Override
  public void dispose() {
    super.dispose();
      imageProvider.dispose();
    }
}
