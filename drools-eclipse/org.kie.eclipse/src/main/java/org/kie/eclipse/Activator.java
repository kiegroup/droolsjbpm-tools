package org.kie.eclipse;

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
	
	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
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
		Activator.context = null;
	}
	
    public static ImageDescriptor getImageDescriptor(String path) {
        ImageRegistry registry = instance.getImageRegistry();
        ImageDescriptor descriptor = registry.getDescriptor( path );
        if ( descriptor == null ) {
            descriptor = AbstractUIPlugin.imageDescriptorFromPlugin("org.kie.eclipse",path);
            registry.put(path,descriptor);
        }
        return descriptor;
    }

	public static Image getImage(String id) {
		getImageDescriptor(id);
		return instance.getImageRegistry().get(id);
	}

}
