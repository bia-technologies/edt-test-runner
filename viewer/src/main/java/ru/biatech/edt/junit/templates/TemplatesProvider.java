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

package ru.biatech.edt.junit.templates;

import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.ScriptVariant;
import lombok.experimental.UtilityClass;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import ru.biatech.edt.junit.TestViewerPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Supplier;

@UtilityClass
public class TemplatesProvider {

  public static final String TEST_MODULE = "module"; //$NON-NLS-1$
  public static final String TEST_METHOD = "method"; //$NON-NLS-1$
  private static final String BSL_FILE_EXTENSION = "bsl"; //$NON-NLS-1$
  private static final String FOLDER_RU = "/templates/ru/"; //$NON-NLS-1$
  private static final String FOLDER_EN = "/templates/en/"; //$NON-NLS-1$
  private static final IPath FOLDER_SETTINGS = new Path(".settings/tests/templates"); //$NON-NLS-1$
  private static final char FILE_EXTENSION_SEPARATOR = '.';

  public Supplier<InputStream> getTestSuiteStructureTemplate(IV8Project project) {
    return getTemplate(project.getProject(), TemplatesProvider.TEST_MODULE, project.getScriptVariant());
  }

  public Optional<String> getTestCaseTemplate(IV8Project project) {
    return Optional.ofNullable(getStringTemplate(project, TEST_METHOD));
  }

  public Optional<String> getTestSuiteStructureTemplateString(IV8Project project) {
    return Optional.ofNullable(getStringTemplate(project, TEST_MODULE));
  }

  private Optional<URL> getBundleEntry(String path) {
    URL url = TemplatesProvider.class.getResource(path);
    return Optional.ofNullable(url);
  }

  private Supplier<InputStream> getTemplate(IProject project, String name, ScriptVariant script) {
    if (name == null || script == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    sb.append(name.toLowerCase());
    sb.append(FILE_EXTENSION_SEPARATOR);
    sb.append(BSL_FILE_EXTENSION);

    IFile templateFile = project.getFile(FOLDER_SETTINGS.append(sb.toString()));

    if (script == ScriptVariant.ENGLISH) {
      sb.insert(0, FOLDER_EN);
    } else {
      sb.insert(0, FOLDER_RU);
    }
    String path = sb.toString();

    if (templateFile.exists()) {
      return () -> {
        try {
          return templateFile.getContents();
        } catch (CoreException e) {
          TestViewerPlugin.log().logError(MessageFormat.format(Messages.TemplatesProvider_ReadTemplateFailed, name), e);
        }
        return TemplatesProvider.class.getResourceAsStream(path);
      };
    }

    Optional<URL> template = getBundleEntry(path);
    if (template.isPresent()) {
      return () -> TemplatesProvider.class.getResourceAsStream(path);
    }
    TestViewerPlugin.log().warning(Messages.TemplatesProvider_CannotFindTemplate, name, script, path);
    return null;
  }

  private String getStringTemplate(IV8Project project, String name) {
    var template = getTemplate(project.getProject(), name, project.getScriptVariant());
    String content = null;
    try (var stream = template.get()) {
      content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      TestViewerPlugin.log().logError(MessageFormat.format(Messages.TemplatesProvider_ReadTemplateFailed, name), e);
    }
    return content;
  }
}
