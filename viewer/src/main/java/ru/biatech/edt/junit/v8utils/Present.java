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

package ru.biatech.edt.junit.v8utils;

import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import com._1c.g5.v8.dt.ui.util.Labeler;
import lombok.experimental.UtilityClass;
import org.eclipse.core.resources.IProject;

@UtilityClass
public class Present {
  public String getPresent(Object item) {
    if (item == null) {
      return "<NULL>"; //$NON-NLS-1$
    } else if (item instanceof Method) {
      return getPresent((Method) item);

    } else if (item instanceof Module) {
      return getPresent((Module) item);
    } else if (item instanceof CommonModule) {
      return getPresent((CommonModule) item);
    }
    return item.toString();
  }

  public String getPresent(Method item) {
    return item.getName();
  }

  public String getPresent(Module item) {
    return Labeler.path(item, '.')
        .skipCommonNode()
        .stopAfter(IProject.class)
        .label();
  }

  public String getPresent(CommonModule item) {
    return Labeler.path(item, '.')
        .skipCommonNode()
        .stopAfter(IProject.class)
        .label();
  }
  public String getShortPresent(CommonModule item) {
    return item.getName();
  }
}
