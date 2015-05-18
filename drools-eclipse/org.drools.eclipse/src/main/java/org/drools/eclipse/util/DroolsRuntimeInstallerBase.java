package org.drools.eclipse.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public abstract class DroolsRuntimeInstallerBase {
	static Hashtable<String, DroolsRuntimeInstallerBase> installers;
	public final static String DROOLS_RUNTIME_INSTALLER = "org.drools.eclipse.runtimeInstaller";
	public static DroolsRuntimeInstallerBase.Factory FACTORY = new DroolsRuntimeInstallerBase.Factory();

	public static class Artifact {
		String type;
		String name;
		String source;
		String target;
		public String getType() {
			return type;
		}
		public String getName() {
			return name;
		}
		public String getSource() {
			return source;
		}
		public String getTarget() {
			return target;
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class ArtifactList extends ArrayList<Artifact> implements Comparable<ArtifactList> {
		String id;
		String name;
		
		List<Artifact> artifacts;
		
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		
		@Override
		public int compareTo(ArtifactList o) {
			
			return 0;
		}
	}

	public static class Factory {
		protected Factory() {
		}
		
		public DroolsRuntimeInstallerBase getInstaller(String runtimeId) {
			DroolsRuntimeInstallerBase installer = null;
			if (DefaultDroolsRuntimeInstaller.installers==null) {
				DefaultDroolsRuntimeInstaller.installers = new Hashtable<String, DroolsRuntimeInstallerBase>();
			    try {
			        IConfigurationElement[] config = Platform.getExtensionRegistry()
			                .getConfigurationElementsFor(DefaultDroolsRuntimeInstaller.DROOLS_RUNTIME_INSTALLER);
			        for (IConfigurationElement e : config) {
			        	if ("installer".equals(e.getName())) {
			                Object o = e.createExecutableExtension("class");
			                if (o instanceof DroolsRuntimeInstallerBase) {
			                	DroolsRuntimeInstallerBase inst = (DroolsRuntimeInstallerBase) o;
				            	inst.id = e.getAttribute("id");
				            	String rid = e.getAttribute("runtimeId");
				            	String name = e.getAttribute("name");
				            	String url = e.getAttribute("url");
			                }
			        	}
			        }
			        
			    } catch (Exception ex) {
					MessageDialog.openError(
							Display.getDefault().getActiveShell(),
							"Error",
							ex.getMessage());
			    }
			}
			return installer;
		}
	}

	protected String id;
	protected String name;
	protected String runtimeId;
	protected String url;
	protected String type;
	protected String source;
	protected String target;
	protected ArtifactList artifacts;
	
	abstract String install(String runtimeId, IProject project, IProgressMonitor monitor);
}
