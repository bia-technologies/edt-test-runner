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

import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import ru.biatech.edt.xtest.Activator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LaunchHelper {

    public final static String REPORT_FILE_NAME = "junit.xml";

    public static ILaunch[] getLaunches() {
        return DebugPlugin.getDefault().getLaunchManager().getLaunches();
    }

    public static IV8ProjectManager getProjectManager() {
        return Activator.getService(IV8ProjectManager.class);
    }

    public static ILaunchConfiguration[] getLaunchConfigurations() {
        try {
            return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
        } catch (CoreException e) {
            Activator.logError(e);
            return new ILaunchConfiguration[0];
        }
    }

    public static ILaunchConfiguration getLaunchConfiguration(String name) throws CoreException {
        if (name == null || name.isEmpty())
            return null;
        ILaunchConfiguration result = null;
        for (ILaunchConfiguration configuration : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()) {
            if (configuration.getName().equals(name)) {
                result = configuration;
                break;
            }
        }
        return result;
    }

    public static List<IExtensionProject> getExtensions() {
        return new ArrayList<>(getProjectManager().getProjects(IExtensionProject.class));
    }

    public static List<IProject> getExtensionsProjects() {
        return getExtensions().
                stream()
                .map(IExtensionProject::getProject)
                .collect(Collectors.toList());
    }

    public static IV8Project getProjectByName(String name) {
        if (name == null || name.isEmpty())
            return null;
        return getProjectManager().getProject(name);
    }

    public static void checkConfiguration(ILaunchConfiguration configuration) throws CoreException {
        String usedLC = configuration.getAttribute(LaunchConfigurationAttributes.USED_LAUNCH_CONFIGURATION, (String) null);

        var usedConfiguration = LaunchHelper.getLaunchConfiguration(usedLC);

        if (usedLC == null || usedLC.isEmpty()) {
            throw new CoreException(Activator.createErrorStatus("Не указана запускаемая конфигурация"));
        } else if (usedConfiguration == null) {
            throw new CoreException(Activator.createErrorStatus("Не найдена запускаемая конфигурация"));
        }
    }

    public static ILaunchConfiguration getTargetConfiguration(ILaunchConfiguration configuration) throws CoreException {
        String targetConfigurationName = configuration.getAttribute(LaunchConfigurationAttributes.USED_LAUNCH_CONFIGURATION, (String) null);

        return getLaunchConfiguration(targetConfigurationName);
    }

    public static IExtensionProject getTestExtension(ILaunchConfiguration configuration) throws CoreException {
        String testExtensionName = configuration.getAttribute(LaunchConfigurationAttributes.TEST_EXTENSION, (String) null);

        return (IExtensionProject) getProjectByName(testExtensionName);
    }

    public static String getTestModuleName(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(LaunchConfigurationAttributes.TEST_MODULE, (String) null);
    }

    public static List<CommonModule> getCommonModules(IExtensionProject extensionProject) {
        return new ArrayList<>(extensionProject.getConfiguration().getCommonModules());
    }

    public static List<String> getCommonModuleNames(IExtensionProject extensionProject) {
        return extensionProject
                .getConfiguration()
                .getCommonModules()
                .stream()
                .map(CommonModule::getName)
                .collect(Collectors.toList());
    }

    public static Path getWorkPath(ILaunchConfiguration configuration) {
        var reportLocation = Platform.getStateLocation(Activator.getBundleContext().getBundle()).append(configuration.getName());
        var path = reportLocation.toFile().toPath();
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            Activator.logError(e);
        }

        return path;
    }
}
