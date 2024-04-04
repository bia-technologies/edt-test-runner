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

import com._1c.g5.v8.dt.mcore.Event;
import com._1c.g5.v8.dt.md.ui.shared.MdUiSharedImages;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.v8utils.Present;

public class MethodLabelProvider extends LabelProvider {
  private final ImageProvider imageProvider = new ImageProvider();
  @Override
  public String getText(Object element) {
    return Present.getPresent(element);
  }

  @Override
  public Image getImage(Object element) {
    if (element instanceof Event) {
      return imageProvider.getEventIcon();

    } else {
      return MdUiSharedImages.getImage(MdUiSharedImages.OBJS_METHOD);
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    imageProvider.dispose();
  }
}
