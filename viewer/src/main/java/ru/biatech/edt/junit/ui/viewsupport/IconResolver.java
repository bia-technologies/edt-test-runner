/*******************************************************************************
 * Copyright (c) 2025 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.viewsupport;

import com._1c.g5.v8.dt.stacktraces.model.IStacktraceElement;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceError;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import lombok.experimental.UtilityClass;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.model.report.Failure;

@UtilityClass
public class IconResolver {
  public Image getIcon(ErrorInfo error, ImageProvider imageProvider) {
    if (error instanceof Failure) {
      return imageProvider.getTestFailIcon();
    } else {
      return imageProvider.getTestErrorIcon();
    }
    // return imageProvider.getMessageIcon();
  }

  public Image getIcon(IStacktraceElement element, ImageProvider imageProvider) {
    if (element instanceof IStacktraceError) {
      return imageProvider.getErrorIcon();
    } else if (element instanceof IStacktraceFrame) {
      return imageProvider.getStackIcon();
    } else {
      return null;
    }
  }
}
