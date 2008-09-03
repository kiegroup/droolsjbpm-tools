package org.drools.eclipse.task;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.drools.eclipse.task";

	// The shared instance
	private static Activator plugin;
	
//    private EntityManagerFactory emf;
//    private EntityManager em;
//    private MinaTaskServer server;
	
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
//		try {
//			emf = Persistence.createEntityManagerFactory("org.drools.task");
//	        em = emf.createEntityManager();
//	        TaskService taskService = new TaskService(em);
//			server = new MinaTaskServer(taskService);
//	        Thread thread = new Thread(server);
//	        thread.start();
//	        Thread.sleep(1000);
//	        User user = new User("John Doe");
//	        taskService.addUser(user);
//	        System.out.println("Added user " + user.getId());
//		} catch (Throwable t) {
//			getLog().log(new Status(
//				IStatus.ERROR, getBundle().getSymbolicName(),
//				120, "Internal error in Drools Task Plugin", t));
//		}
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
//		if (em != null) {
//			em.close();
//		}
//		if (emf != null) {
//			emf.close();
//		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
