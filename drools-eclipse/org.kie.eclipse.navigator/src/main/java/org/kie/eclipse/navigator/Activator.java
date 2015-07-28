package org.kie.eclipse.navigator;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.kie.eclipse.navigator"; //$NON-NLS-1$

	public final static String IMG_SERVER_STARTED = "server_started.gif";
	public final static String IMG_SERVER_STOPPED = "server_stopped.gif";
	public final static String IMG_ORGANIZATION = "organization.gif";
	public final static String IMG_PROJECT = "project.gif";
	public final static String IMG_PROJECT_CLOSED = "project_closed.gif";
	public final static String IMG_REPOSITORY = "repository.gif";
	public final static String IMG_REPOSITORY_UNAVAILABLE = "repository_unavailable.gif";
	
	// The shared instance
	private static Activator instance;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return instance;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(IMG_SERVER_STARTED, loadImageDescriptor(IMG_SERVER_STARTED));
        reg.put(IMG_SERVER_STOPPED, loadImageDescriptor(IMG_SERVER_STOPPED));
        reg.put(IMG_ORGANIZATION, loadImageDescriptor(IMG_ORGANIZATION));
        reg.put(IMG_PROJECT, loadImageDescriptor(IMG_PROJECT));
        reg.put(IMG_PROJECT_CLOSED, loadImageDescriptor(IMG_PROJECT_CLOSED));
        reg.put(IMG_REPOSITORY, loadImageDescriptor(IMG_REPOSITORY));
        reg.put(IMG_REPOSITORY_UNAVAILABLE, loadImageDescriptor(IMG_REPOSITORY_UNAVAILABLE));
	}

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
	
    public static ImageDescriptor getImageDescriptor(String path) {
        ImageRegistry registry = instance.getImageRegistry();
        ImageDescriptor descriptor = registry.getDescriptor( path );
        if ( descriptor == null ) {
            descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID,path);
            registry.put(path,descriptor);
        }
        return descriptor;
    }

    private static ImageDescriptor loadImageDescriptor(String id) {
        String iconPath = "icons/"; //$NON-NLS-1$

        try {
            URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
            URL url = new URL(installURL, iconPath + id);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

	public static Image getImage(String id) {
		getImageDescriptor(id);
		return instance.getImageRegistry().get(id);
	}
}
