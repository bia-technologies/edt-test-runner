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
package ru.biatech.edt.xtest.launch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import ru.biatech.edt.xtest.Activator;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {

        monitor.beginTask("Запуск тестов", 1);

        if (monitor.isCanceled())
            return;

        LaunchHelper.checkConfiguration(configuration);

        var workPath = LaunchHelper.getWorkPath(configuration);

        String startupParameters = "RunUnitTests=" + createConfig(configuration, workPath);

        var target = LaunchHelper.getTargetConfiguration(configuration);
        var targetCopy = target.getWorkingCopy();

        String newValue = startupParameters;

        final String oldValue = targetCopy.getAttribute(V8LaunchConfigurationAttributes.ATTR_STARTUP_OPTION, (String) null);
        if (oldValue != null && !oldValue.isEmpty() && !oldValue.isBlank()) {
            newValue = newValue + ";" + oldValue;
        }

        targetCopy.setAttribute(V8LaunchConfigurationAttributes.ATTR_STARTUP_OPTION, newValue);
        targetCopy.launch(mode, SubMonitor.convert(monitor, 1)).setAttribute("workPath", workPath.toString());

        monitor.done();
    }

    public String createConfig(ILaunchConfiguration configuration, Path workPath) throws CoreException {
        File file = new File(workPath.toFile(), "xUnitParams.json");

        JsonObject config = new JsonObject();
        JsonObject filter = new JsonObject();
        String testModuleName = LaunchHelper.getTestModuleName(configuration);
        var testExtenstion = LaunchHelper.getTestExtension(configuration);

        if (testModuleName != null && !testModuleName.isBlank()) {
            JsonArray array = new JsonArray();
            array.add(testModuleName);
            filter.add("modules", array);
        } else if (testExtenstion != null ) {
            JsonArray array = new JsonArray();
            array.add(testExtenstion.getConfiguration().getName());
            filter.add("extensions", array);
        }

        config.add("filter", filter);
        config.addProperty("reportPath", workPath.toString());
        config.addProperty("reportFormat", "jUnit");
        config.addProperty("closeAfterTests", true);

        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(config, writer);
        } catch (Exception e) {
            Activator.logError(e);
        }
        return file.toString();
    }
}
