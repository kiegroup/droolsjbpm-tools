/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.eclipse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.kie.eclipse"; //$NON-NLS-1$

	private static BundleContext context;
	private static Activator instance;
	private static boolean debug = false;
	
	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
		instance = this;
	}
	
	public static Activator getDefault() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		context = null;
		instance = null;
		super.stop(bundleContext);
	}
	
    public static ImageDescriptor getImageDescriptor(String path) {
        ImageRegistry registry = instance.getImageRegistry();
        ImageDescriptor descriptor = registry.getDescriptor( path );
        if ( descriptor == null ) {
            descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID,path);
            registry.put(path,descriptor);
        }
        return descriptor;
    }

	public static Image getImage(String id) {
		getImageDescriptor(id);
		return instance.getImageRegistry().get(id);
	}

	public static void println(Object arg) {
		if (debug)
			System.out.println(arg);
	}

	public static void print(Object arg) {
		if (debug)
			System.out.print(arg);
	}

	/**
	 * Utility to create an error status for this plug-in.
	 *
	 * @param message User comprehensible message
	 * @param thr cause
	 * @return an initialized error status
	 */
	public static IStatus error(final String message, final Throwable thr) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0,	message, thr);
	}

	/**
	 * Utility method to log errors in the Egit plugin.
	 * @param message User comprehensible message
	 * @param thr The exception through which we noticed the error
	 */
	public static void logError(final String message, final Throwable thr) {
		getDefault().getLog().log(error(message, thr));
	}
}
