/*
 * Copyright 2021-2022 BIA-Technologies Limited Liability Company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * За основу взят класс https://github.com/DoublesunRUS/ru.capralow.dt.unit/blob/master/bundles/ru.capralow.dt.unit.launcher.ui/src/ru/capralow/dt/unit/launcher/internal/ui/junit/ShowJUnitResult.java
 * Copyright (c) 2020, Alexander Kapralov
 */

package ru.biatech.edt.xtest.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.xtest.Activator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ShowJUnitResult implements IDebugEventSetListener {

    public static final String JUNIT_PANEL_ID = "org.eclipse.jdt.junit.ResultView"; //$NON-NLS-1$

    private static void showJUnitResult(IProcess process) {
        if (process.getLabel().contains("dbgs")) //$NON-NLS-1$
            return;

        try {
            String workPath = process.getLaunch().getAttribute("workPath");
            File file = new File(workPath, LaunchHelper.REPORT_FILE_NAME); //$NON-NLS-1$
            if (!file.exists()) {
                Activator.logError("Не найден файл отчета");
                return;
            }

            JUnitCore.importTestRunSession(file);

            Display.getDefault().asyncExec(() -> {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(JUNIT_PANEL_ID);

                } catch (PartInitException e) {
                    Activator.logError("Не удалось отобразить данные отчета", e);
                }
            });

            Files.deleteIfExists(file.toPath());

        } catch (CoreException | IOException e) {
            Activator.logError("Возникли проблемы при выводе отчета", e);
        }
    }

    @Override
    public void handleDebugEvents(DebugEvent[] events) {
        for (DebugEvent event : events) {
            Object source = event.getSource();
            if (event.getKind() == DebugEvent.TERMINATE && source instanceof IProcess)
                showJUnitResult((IProcess) source);
        }
    }
}
