/*******************************************************************************
 * Copyright (c) 2011, 2012, 2013, 2014 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 * @author Bob Brodt
 ******************************************************************************/

package org.kie.eclipse.navigator.view.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wst.server.core.IServer;
import org.kie.eclipse.navigator.Activator;

/**
 *
 */
public class KieServerHandler extends KieResourceHandler implements IKieServerHandler {

    private final IServer server;
	private IKieServiceDelegate delegate;
	
	/**
	 * 
	 */
	public KieServerHandler(IServer server) {
		super(null,server.getName());
		this.server = server;
	}

	public void dispose() {
	}
	
	public IKieServiceDelegate getDelegate() {
		if (delegate==null)
			delegate = loadDelegate();
		return delegate;
	}
	
	protected IKieServiceDelegate loadDelegate() {
		IKieServiceDelegate result = null;
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(IKieServiceDelegate.KIE_SERVICE_IMPL_ID);
			for (IConfigurationElement e : config) {
				if ("containerBinding".equals(e.getName())) {
					String serverId = e.getAttribute("serverId");
					if (getServerTypeId().equals(serverId)) {
						String kieVersion = e.getAttribute("runtimeId");
						if ( getRuntimeId().equals(kieVersion) ) {
							Object o = e.createExecutableExtension("class");
							if (o instanceof IKieServiceDelegate) {
								result = (IKieServiceDelegate)o;
								result.setServer(server);
								result.setHandler(this);
								return result;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		return result;
	}
	
	public static boolean isSupportedServer(IServer server) {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(IKieServiceDelegate.KIE_SERVICE_IMPL_ID);
		for (IConfigurationElement e : config) {
			if ("containerBinding".equals(e.getName())) {
				String serverId = e.getAttribute("serverId");
				if (server.getServerType().getId().equals(serverId)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Return the version number of the KIE Workbench that is installed on the
	 * given server. If the server is not running or not responsive, use a value
	 * from the Preference Store.
	 * 
	 * @param server
	 * @return
	 */
	public String getRuntimeId() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String value = store.getString(getKieVersionPreferenceKey());
		if (value==null || value.isEmpty())
			value = "org.jboss.kie.6.2"; // just a bootstrap for testing
		return value;
	}

	public void setRuntimeId(String version) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.putValue(getKieVersionPreferenceKey(), version);
	}

	public String getServerTypeId() {
		return server.getServerType().getId();
	}
	
	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieService#getOrganizations()
	 */
	@Override
	public List<IKieOrganizationHandler> getOrganizations() throws IOException {
		return getDelegate().getOrganizations(this);
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieService#getRepositories(org.kie.eclipse.navigator.view.server.IKieOrganization)
	 */
	@Override
	public List<IKieRepositoryHandler> getRepositories(IKieOrganizationHandler organization) throws IOException {
		return getDelegate().getRepositories(organization);
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieService#getProjects(org.kie.eclipse.navigator.view.server.IKieRepository)
	 */
	@Override
	public List<IKieProjectHandler> getProjects(IKieRepositoryHandler repository) throws IOException {
		return getDelegate().getProjects(repository);
	}

	public List<? extends IKieResourceHandler> getChildren() throws Exception {
		if (children==null)
			children = new ArrayList<IKieResourceHandler>();
		if (children.isEmpty()) {
			List<IKieRepositoryHandler> allRepositories = getDelegate().getRepositories(this);
			List<IKieOrganizationHandler> organizations = getDelegate().getOrganizations(this);
			for (IKieRepositoryHandler r1 : allRepositories) {
				boolean contained = false;
				for (IKieOrganizationHandler o : organizations) {
					for (IKieRepositoryHandler r2 : o.getRepositories()) {
						if (r1.getName().equals(r2.getName())) {
							contained = true;
							break;
						}
					}
				}
				if (!contained)
					children.add(r1);
			}
			children.addAll(organizations);
		}		
		return children;
	}
	
	public boolean isServerRunning() {
		return server.getServerState() == IServer.STATE_STARTED;
	}
	
	protected String getKieVersionPreferenceKey() {
		return server.getId()+"/kieVersion";
	}
	
	protected String getKieOrganizationsPreferenceKey() {
		return server.getId()+"/organizations";
	}
	
	protected String getKieRepositoriesPreferenceKey() {
		return server.getId()+"/repositories";
	}
	
	protected String getKieProjectsPreferenceKey() {
		return server.getId()+"/projects";
	}
	
	public boolean isLoaded() {
		return isServerRunning();
	}
	
	@Override
	public IServer getServer() {
		return server;
	}
}
