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

package org.kie.eclipse.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractRuntimeInstaller implements IRuntimeInstaller {
	static Hashtable<String, AbstractRuntimeInstaller> installers;
	static Hashtable<String, ArtifactList> artifacts;
	
	public final static String KIE_RUNTIME_INSTALLER = "org.kie.eclipse.runtimeInstaller";
	public static AbstractRuntimeInstaller.Factory FACTORY = new AbstractRuntimeInstaller.Factory();

	/**
	 * Represents a "repository" definition in the KIE Runtime Installer extension point.
	 */
	public static class Repository {
		protected String url;
		protected String type;
		protected String source;
		protected String target;
		protected String artifactsId;
		
		public String getUrl() {
			return url;
		}
		public String getType() {
			return type;
		}
		public String getSource() {
			return source;
		}
		public String getTarget() {
			return target;
		}
		
		public ArtifactList getArtifactList() {
			FACTORY.createInstallers();
			return artifacts.get(artifactsId);
		}
		
		public List<Artifact> getArtifacts() {
			ArtifactList al = getArtifactList();
			return al.getArtifacts();
		}
	}
	
	/**
	 * Represents an "artifact" definition in the KIE Runtime Installer extension point.
	 */
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
	
	/**
	 * Represents an "artifacts" definition in the KIE Runtime Installer extension point.
	 */
	@SuppressWarnings("serial")
	public static class ArtifactList extends ArrayList<Artifact> {
		String id;
		String name;
		List<Artifact> artifacts = new ArrayList<Artifact>();
		
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		
		public List<Artifact> getArtifacts() {
			return artifacts;
		}
	}

	/**
	 * Factory class for creating Runtime Installers
	 */
	public static class Factory {
		protected Factory() {
		}
		
		public AbstractRuntimeInstaller getInstaller(String runtimeId) {
			createInstallers();
			return installers.get(runtimeId);
		}

		public Collection<? extends IRuntimeInstaller> createInstallers() {
			if (installers==null) {
				installers = new Hashtable<String, AbstractRuntimeInstaller>();
			    try {
			        IConfigurationElement[] config = Platform.getExtensionRegistry()
			                .getConfigurationElementsFor(DefaultRuntimeInstaller.KIE_RUNTIME_INSTALLER);
			        for (IConfigurationElement e : config) {
			        	if ("installer".equals(e.getName())) {
			                Object o = e.createExecutableExtension("class");
			                if (o instanceof AbstractRuntimeInstaller) {
			                	AbstractRuntimeInstaller installer = (AbstractRuntimeInstaller) o;
				            	installer.id = e.getAttribute("id");
				            	installer.product = e.getAttribute("product");
				            	installer.version = e.getAttribute("version");
				            	installer.runtimeName = e.getAttribute("runtimeName");
				            	for (IConfigurationElement r : e.getChildren("repository")) {
				            		Repository repository = new Repository();
				            		repository.url = r.getAttribute("url");
				            		repository.type = r.getAttribute("type");
				            		repository.source = r.getAttribute("source");
				            		repository.target = r.getAttribute("target");
				            		repository.artifactsId = r.getAttribute("artifacts");
				            		installer.getRepositories().add(repository);
				            	}
				            	try {
				            		installer.priority = Integer.getInteger(e.getAttribute("priority"));
				            	}
				            	catch (Exception ex) {
				            		installer.priority = 1;
				            	}
				            	// replace lower priority installers with higher priority
				            	AbstractRuntimeInstaller oldInstaller = installers.get(installer.version);
				            	if (oldInstaller==null || installer.priority>oldInstaller.priority)
						            installers.put(installer.version, installer);
			                }
			        	}
			        	else if ("artifacts".equals(e.getName())) {
			        		ArtifactList artifactList = new ArtifactList();
			        		artifactList.id = e.getAttribute("id");
			        		artifactList.name = e.getAttribute("name");
			        		for (IConfigurationElement a : e.getChildren("artifact")) {
			        			Artifact artifact = new Artifact();
			        			artifact.type = a.getAttribute("type");
			        			artifact.name = a.getAttribute("name");
			        			artifact.source = a.getAttribute("source");
			        			artifact.target = a.getAttribute("target");
			        			artifactList.artifacts.add(artifact);
			        		}
			        		if (artifacts==null)
			        			artifacts = new Hashtable<String, ArtifactList>();
			        		artifacts.put(artifactList.id, artifactList);
			        	}
			        }
			        
			    } catch (Exception ex) {
					MessageDialog.openError(
							Display.getDefault().getActiveShell(),
							"Error",
							ex.getMessage());
			    }
			}
			
			return installers.values();
		}
	}

	protected String id;
	protected String product;
	protected String version;
	protected String runtimeName;
	protected int priority;
	protected List<Repository> repositories;
	
	public abstract String install(String runtimeId, String location, IProgressMonitor monitor);
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRuntimeName() {
		return runtimeName;
	}

	public void setRuntimeName(String runtimeName) {
		this.runtimeName = runtimeName;
	}
	
	public List<Repository> getRepositories() {
		if (repositories==null) {
			repositories = new ArrayList<Repository>();
		}
		return repositories;
	}
}
