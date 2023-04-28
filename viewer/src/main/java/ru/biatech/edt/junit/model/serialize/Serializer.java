/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.model.serialize;

import lombok.experimental.UtilityClass;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.xml.sax.SAXException;
import ru.biatech.edt.junit.BasicElementLabels;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.TestRunSession;
import ru.biatech.edt.junit.ui.JUnitMessages;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

/**
 * Клсс-загрузчик отчетов о тестировании
 */
@UtilityClass
public class Serializer {
  /**
   * Imports a test run session from the given file.
   *
   * @param file a file containing a test run session transcript
   * @return the imported test run session
   * @throws CoreException if the import failed
   */
  public TestRunSession importTestRunSession(File file, String defaultProjectName) throws CoreException {
    try {
      TestViewerPlugin.log().debug("Импорт отчета о тестировании: " + file.getAbsolutePath());
      var parserFactory = SAXParserFactory.newInstance();
      var parser = parserFactory.newSAXParser();
      var handler = new TestRunHandler();
      handler.fDefaultProjectName = defaultProjectName;
      parser.parse(file, handler);
      var session = handler.getTestRunSession();
      if(session!=null){
        TestViewerPlugin.core().getModel().addTestRunSession(session);}
      else{
        TestViewerPlugin.log().logError(JUnitMessages.JUnitModel_ReportIsEmpty);
      }
      return session;
    } catch (ParserConfigurationException | SAXException e) {
      throwImportError(file, e);
    } catch (IOException e) {
      throwImportError(file, e);
    } catch (IllegalArgumentException e) {
      // Bug in parser: can throw IAE even if file is not null
      throwImportError(file, e);
    }
    return null; // does not happen
  }

  /**
   * Imports a test run session from the given URL.
   *
   * @param url     an URL to a test run session transcript
   * @param monitor a progress monitor for cancellation
   * @return the imported test run session
   * @throws InvocationTargetException wrapping a CoreException if the import failed
   * @throws InterruptedException      if the import was cancelled
   * @since 3.6
   */
  public TestRunSession importTestRunSession(String url, String defaultProjectName, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    monitor.beginTask(JUnitMessages.JUnitModel_importing_from_url, IProgressMonitor.UNKNOWN);
    final var trimmedUrl = url.trim().replaceAll("\r\n?|\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
    final var handler = new TestRunHandler(monitor);
    handler.fDefaultProjectName = defaultProjectName;

    final CoreException[] exception = {null};
    final TestRunSession[] session = {null};

    var importThread = new Thread("JUnit URL importer") { //$NON-NLS-1$
      @Override
      public void run() {
        try {
          var parserFactory = SAXParserFactory.newInstance();
//					parserFactory.setValidating(true); // TODO: add DTD and debug flag
          var parser = parserFactory.newSAXParser();
          parser.parse(trimmedUrl, handler);
          session[0] = handler.getTestRunSession();
        } catch (OperationCanceledException e) {
          // canceled
        } catch (ParserConfigurationException | SAXException e) {
          storeImportError(e);
        } catch (IOException e) {
          storeImportError(e);
        } catch (IllegalArgumentException e) {
          // Bug in parser: can throw IAE even if URL is not null
          storeImportError(e);
        }
      }

      private void storeImportError(Exception e) {
        exception[0] = new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
            TestViewerPlugin.getPluginId(), JUnitMessages.JUnitModel_could_not_import, e));
      }
    };
    importThread.start();

    while (session[0] == null && exception[0] == null && !monitor.isCanceled()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // that's OK
      }
    }
    if (session[0] == null) {
      if (exception[0] != null) {
        throw new InvocationTargetException(exception[0]);
      } else {
        importThread.interrupt(); // have to kill the thread since we don't control URLConnection and XML parsing
        throw new InterruptedException();
      }
    }

    TestViewerPlugin.core().getModel().addTestRunSession(session[0]);
    monitor.done();
    return session[0];
  }

  public void importIntoTestRunSession(File swapFile, TestRunSession testRunSession) throws CoreException {
    try {
      TestViewerPlugin.log().debug("Обновление отчета о тестировании: " + swapFile.getAbsolutePath());
      var parserFactory = SAXParserFactory.newInstance();
//			parserFactory.setValidating(true); // TODO: add DTD and debug flag
      var parser = parserFactory.newSAXParser();
      var handler = new TestRunHandler(testRunSession);
      parser.parse(swapFile, handler);
    } catch (ParserConfigurationException | SAXException e) {
      throwImportError(swapFile, e);
    } catch (IOException e) {
      throwImportError(swapFile, e);
    } catch (IllegalArgumentException e) {
      // Bug in parser: can throw IAE even if file is not null
      throwImportError(swapFile, e);
    }
  }

  private void throwImportError(File file, Exception e) throws CoreException {
    var message = MessageFormat.format(JUnitMessages.JUnitModel_could_not_read, BasicElementLabels.getPathLabel(file));
    throw new CoreException(TestViewerPlugin.log().createErrorStatus(message, e));
  }
}
