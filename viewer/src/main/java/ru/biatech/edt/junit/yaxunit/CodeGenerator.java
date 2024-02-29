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

package ru.biatech.edt.junit.yaxunit;

import com._1c.g5.v8.dt.common.FileUtil;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import lombok.experimental.UtilityClass;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.templates.TemplatesProvider;
import ru.biatech.edt.junit.v8utils.Resources;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Модуль-помощник для кодогенерации тестов.
 * На основании шаблонов формирует тело тестовго метода/модуля и тд
 */
@UtilityClass
public class CodeGenerator {
  public final String TEST_CASE_REGISTRATION_TEMPLATE = String.format("%s\t\t.%s(\"{0}\")", System.lineSeparator(), Constants.ADD_TEST_METHOD); //$NON-NLS-1$

  public void createTestSuiteStructure(IV8Project project, CommonModule testSuite, IProgressMonitor monitor) {
    var description = Messages.CodeGenerator_CreateTestStructure;
    monitor.beginTask(description, IProgressMonitor.UNKNOWN);
    var template = TemplatesProvider.getTestSuiteStructureTemplate(project);
    var resource = Resources.getModuleResource(testSuite);

    if (resource == null) {
      TestViewerPlugin.log().logError(MessageFormat.format(Messages.CodeGenerator_Failed, description));
      monitor.setCanceled(true);
    } else if (template == null) {
      TestViewerPlugin.log().logError(Messages.CodeGenerator_TestStructureTemplateFailed);
      monitor.setCanceled(true);
    } else {

      try (var stream = template.get()) {
        if (!resource.exists()) {
          FileUtil.createParentFolders(resource);
        }
        resource.create(stream, true, monitor);
        resource.touch(monitor);
      } catch (IOException | CoreException e) {
        TestViewerPlugin.log().logError(description, e);
        monitor.setCanceled(true);
      }
    }
    monitor.done();
  }

  public String testCasesRegistration(String[] methodsNames) {
    return Arrays.stream(methodsNames)
        .map(n -> MessageFormat.format(TEST_CASE_REGISTRATION_TEMPLATE, n))
        .collect(Collectors.joining());
  }

  public String testCaseImplementationTemplate(IV8Project project) {
    var ls = System.lineSeparator();
    return TemplatesProvider.getTestCaseTemplate(project)
        .map(t -> ls + ls + t)
        .orElse(null);
  }

  public Optional<String> testSuiteTemplate(IV8Project project) {
    return TemplatesProvider.getTestSuiteStructureTemplateString(project);
  }

  public String testCasesImplementation(IV8Project project, String[] methodsNames) {
    final var template = testCaseImplementationTemplate(project);
    if (template == null) {
      return null;
    }
    return Arrays.stream(methodsNames)
        .map(n -> MessageFormat.format(template, n))
        .collect(Collectors.joining());
  }
}
