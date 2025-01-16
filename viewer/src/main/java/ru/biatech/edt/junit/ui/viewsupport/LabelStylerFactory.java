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

package ru.biatech.edt.junit.ui.viewsupport;

import lombok.experimental.UtilityClass;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.xtext.ui.label.StylerFactory;
import ru.biatech.edt.junit.ui.utils.StringUtilities;

import java.text.MessageFormat;

@UtilityClass
public class LabelStylerFactory {
  private final StylerFactory stylerFactory = new StylerFactory();

  public StyledString.Styler getNameStyler() {
    return stylerFactory
        .createStyler(FontDescriptor.createFrom(new FontData(StringUtilities.EMPTY_STRING, 9, SWT.BOLD)),
            JFacePreferences.ACTIVE_HYPERLINK_COLOR, null);
  }

  public StyledString format(String pattern, StyledString.Styler styler, Object... arguments) {
    var message = MessageFormat.format(pattern, arguments);
    var base = pattern.replaceAll("\\{.+?}", "");
    var result = new StyledString(base);
    return StyledCellLabelProvider.styleDecoratedString(message, styler, result);
  }
}
