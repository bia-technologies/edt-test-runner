/*******************************************************************************
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit;

import com._1c.g5.v8.dt.bm.index.emf.IBmEmfIndexManager;
import com._1c.g5.v8.dt.core.platform.IResourceLookup;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceParser;
import com._1c.g5.wiring.AbstractServiceAwareModule;
import com._1c.g5.wiring.InjectorAwareServiceRegistrator;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import ru.biatech.edt.junit.launcher.lifecycle.LifecycleMonitor;
import ru.biatech.edt.junit.ui.JUnitUI;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

public class TestViewerPlugin extends AbstractUIPlugin {
  public static final String PLUGIN_ID = "ru.biatech.edt.junit"; //$NON-NLS-1$
  private static final IPath ICONS_PATH = new Path("$nl$/icons/full"); //$NON-NLS-1$
  private static TestViewerPlugin plugin;
  JUnitCore core;
  JUnitUI ui;
  private BundleContext bundleContext;
  private Injector injector;
  private Logger logger = null;

  public TestViewerPlugin() {
    plugin = this;
    core = new JUnitCore();
    ui = new JUnitUI();
  }

  public static TestViewerPlugin getDefault() {
    return plugin;
  }

  public static Logger log() {
    return getDefault().getLogger();
  }

  public static BundleContext getBundleContext() {
    return getDefault().getContext();
  }

  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<T> clazz) {
    BundleContext bundleContext = getBundleContext();
    ServiceReference<?> serviceReference = bundleContext.getServiceReference(clazz.getName());
    return (T) bundleContext.getService(serviceReference);
  }

  public static JUnitCore core() {
    return getDefault().core;
  }

  public static JUnitUI ui() {
    return getDefault().ui;
  }

  public static String getPluginId() {
    return PLUGIN_ID;
  }

  public static boolean isStopped() {
    return false;// TODO
  }

  /**
   * Creates an image descriptor for the given path in a bundle. The path can
   * contain variables like $NL$. If no image could be found,
   * <code>useMissingImageDescriptor</code> decides if either the 'missing
   * image descriptor' is returned or <code>null</code>.
   *
   * @param bundle                    a bundle
   * @param path                      path in the bundle
   * @param useMissingImageDescriptor if <code>true</code>, returns the shared image descriptor
   *                                  for a missing image. Otherwise, returns <code>null</code> if the image could not
   *                                  be found
   * @return an {@link ImageDescriptor}, or <code>null</code> iff there's
   * no image at the given location and
   * <code>useMissingImageDescriptor</code> is <code>true</code>
   */
  private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
    URL url = FileLocator.find(bundle, path, null);
    if (url != null) {
      return ImageDescriptor.createFromURL(url);
    }
    if (useMissingImageDescriptor) {
      return ImageDescriptor.getMissingImageDescriptor();
    }
    return null;
  }

  public Logger getLogger() {
    return logger == null ? logger = new Logger() : logger;
  }

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    super.start(bundleContext);

    new InjectorAwareServiceRegistrator(bundleContext, this::getInjector);
    this.bundleContext = bundleContext;
    core().getModel().start();
    LifecycleMonitor.start();
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    LifecycleMonitor.stop();
    core().getModel().stop();
    plugin = null;
    super.stop(bundleContext);
  }

  protected BundleContext getContext() {
    return bundleContext;
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
      return Guice.createInjector(new AbstractServiceAwareModule(this) {
        @Override
        protected void doConfigure() {

          bind(IV8ProjectManager.class).toService();
          bind(IBmEmfIndexManager.class).toService();
          bind(IResourceLookup.class).toService();
          bind(IStacktraceParser.class).toService();
        }
      });
    } catch (Exception e) {
      String message = MessageFormat.format("Failed to create injector for {0}", this.getBundle().getSymbolicName());
      getLogger().logError(message, e);
      throw new RuntimeException(message, e);
    }
  }

  /*
   * Creates an image descriptor for the given prefix and name in the JDT UI bundle. The path can
   * contain variables like $NL$.
   * If no image could be found, <code>useMissingImageDescriptor</code> decides if either
   * the 'missing image descriptor' is returned or <code>null</code>.
   * or <code>null</code>.
   */
  public ImageDescriptor createImageDescriptor(String pathPrefix, String imageName, boolean useMissingImageDescriptor) {
    IPath path = ICONS_PATH.append(pathPrefix).append(imageName);
    return createImageDescriptor(getBundle(), path, useMissingImageDescriptor);
  }

  public ImageDescriptor createImageDescriptor(String imageName, boolean useMissingImageDescriptor) {
    IPath path = ICONS_PATH.append(imageName);
    return createImageDescriptor(getBundle(), path, useMissingImageDescriptor);
  }

  public URL getResource(String path) throws IOException {
    var url = FileLocator.find(getBundle(), new Path(path), null);
    return FileLocator.toFileURL(url);
  }

  public InputStream getResourceStream(String path) throws IOException {
    return FileLocator.openStream(getBundle(), new Path(path), false);
  }
}
