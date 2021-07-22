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
package ru.biatech.edt.xtest;

import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.wiring.AbstractServiceAwareModule;
import com._1c.g5.wiring.InjectorAwareServiceRegistrator;
import com._1c.g5.wiring.ServiceInitialization;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import ru.biatech.edt.xtest.launch.UnitLauncherManager;

import java.text.MessageFormat;

public class Activator extends Plugin {

    public static final String PLUGIN_ID = "ru.biatech.edt.xtest"; //$NON-NLS-1$
    private static Activator plugin;
    private BundleContext bundleContext;
    private Injector injector;
    private InjectorAwareServiceRegistrator registrator;

    public static Activator getDefault() {
        return plugin;
    }

    public static void log(IStatus status) {
        plugin.getLog().log(status);
    }

    public static void logError(Throwable throwable) {
        log(createErrorStatus(throwable.getMessage(), throwable));
    }

    public static void logError(String message, Throwable exc) {
        log(createErrorStatus(message, exc));
    }

    public static void logError(String message) {
        log(createErrorStatus(message));
    }

    public static IStatus createErrorStatus(String message, Throwable throwable) {
        return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, throwable);
    }

    public static IStatus createErrorStatus(String message) {
        return new Status(IStatus.ERROR, PLUGIN_ID, message);
    }

    public static IStatus createWarningStatus(String message) {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null);
    }

    public static IStatus createWarningStatus(final String message, Exception throwable) {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, throwable);
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        super.start(bundleContext);

        registrator = new InjectorAwareServiceRegistrator(bundleContext, this::getInjector);
        ServiceInitialization.schedule(() -> registrator.activateManagedService(UnitLauncherManager.class));

        this.bundleContext = bundleContext;
        plugin = this;
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        plugin = null;
        super.stop(bundleContext);
    }

    protected BundleContext getContext() {
        return bundleContext;
    }

    public static BundleContext getBundleContext() {
        return getDefault().getContext();
    }

    /**
     * Returns Guice injector for this plugin.
     *
     * @return Guice injector for this plugin, never {@code null}
     */
    public synchronized Injector getInjector() {
        if (injector == null)
            return injector = createInjector();
        return injector;
    }

    private Injector createInjector() {
        try {
            return Guice.createInjector(new AbstractServiceAwareModule(plugin) {
                @Override
                protected void doConfigure() {
                    bind(IV8ProjectManager.class).toService();
                }
            });
        } catch (Exception var3) {
            String message = MessageFormat.format("Failed to create injector for {0}", this.getBundle().getSymbolicName());
            log(createErrorStatus(message, var3));
            throw new RuntimeException(message, var3);
        }
    }

    public static <T> T getService(Class<T> clazz) {
        BundleContext bundleContext = getDefault().bundleContext;
        ServiceReference<?> serviceReference = bundleContext.getServiceReference(clazz.getName());
        return (T) getDefault().bundleContext.getService(serviceReference);

    }
}
