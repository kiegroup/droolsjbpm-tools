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

package org.kie.eclipse.server.jbpm60;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kie.eclipse.server.IKieOrganizationHandler;
import org.kie.eclipse.server.IKieProjectHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.KieOrganizationHandler;
import org.kie.eclipse.server.KieProjectHandler;
import org.kie.eclipse.server.KieRepositoryHandler;
import org.kie.eclipse.server.KieServiceDelegate;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 *
 */
public class Kie60Service extends KieServiceDelegate {

	/**
	 *
	 */
	public Kie60Service() {
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceImpl#getOrganizations()
	 */
	@Override
	public List<IKieOrganizationHandler> getOrganizations(IKieServerHandler service)  throws IOException {
		List<IKieOrganizationHandler> result = new ArrayList<IKieOrganizationHandler>();
		
		String response = httpGet("organizationalunits");
		JsonArray ja = JsonArray.readFrom(response);
		for (int i=0; i<ja.size(); ++i) {
			JsonObject jo = ja.get(i).asObject();
			KieOrganizationHandler ko = new KieOrganizationHandler(service, jo.get("name").asString());
			ko.setProperties(jo);
			result.add(ko);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceImpl#getRepositories(org.kie.eclipse.navigator.view.server.IKieOrganization)
	 */
	@Override
	public List<IKieRepositoryHandler> getRepositories(IKieOrganizationHandler organization) throws IOException {
		IKieServerHandler server = (IKieServerHandler) organization.getRoot();
		List<IKieRepositoryHandler> allRepositories = getRepositories(server);
		List<IKieRepositoryHandler> result = new ArrayList<IKieRepositoryHandler>();

		String response = httpGet("organizationalunits");
		JsonArray ja1 = JsonArray.readFrom(response);
		for (int i=0; i<ja1.size(); ++i) {
			JsonObject jo = ja1.get(i).asObject();
			if (organization.getName().equals(jo.get("name").asString())) {
				JsonArray ja2 = jo.get("repositories").asArray();
				for (int j=0; j<ja2.size(); ++j) {
					JsonValue jv = ja2.get(j);
					String name = jv.asString();
					for (IKieRepositoryHandler r : allRepositories) {
						if (r.getName().equals(name)) {
							r.setParent(organization);
							result.add(r);
							break;
						}
					}
//					KieRepository kr = new KieRepository(organization, name);
//					result.add(kr);
				}
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceDelegate#getRepositories(org.kie.eclipse.navigator.view.server.IKieServer)
	 */
	@Override
	public List<IKieRepositoryHandler> getRepositories(IKieServerHandler server) throws IOException {
		List<IKieRepositoryHandler> result = new ArrayList<IKieRepositoryHandler>();

		String response = httpGet("repositories");
		JsonArray ja1 = JsonArray.readFrom(response);
		for (int i=0; i<ja1.size(); ++i) {
			JsonObject jo = ja1.get(i).asObject();
			KieRepositoryHandler kr = new KieRepositoryHandler(server, jo.get("name").asString());
			kr.setProperties(jo);
			result.add(kr);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceImpl#getProjects(org.kie.eclipse.navigator.view.server.IKieOrganization, org.kie.eclipse.navigator.view.server.IKieRepository)
	 */
	@Override
	public List<IKieProjectHandler> getProjects(IKieRepositoryHandler repository) throws IOException {
		List<IKieProjectHandler> result = new ArrayList<IKieProjectHandler>();

		String response = httpGet("repositories/"+repository.getName()+"/projects/");
		JsonArray ja1 = JsonArray.readFrom(response);
		for (int i=0; i<ja1.size(); ++i) {
			JsonObject jo = ja1.get(i).asObject();
			KieProjectHandler kp = new KieProjectHandler(repository, jo.get("name").asString());
			kp.setProperties(jo);
			result.add(kp);
		}
		/*
		Object o = repository.load();
		if (o instanceof Repository) {
			Repository git = (Repository) o;
			try {
				String gitDir = git.getWorkTree().getAbsolutePath();
				for (String f : git.getWorkTree().list()) {
					File file = new File(gitDir + File.separator + f);
					if (file.isDirectory() && !file.getName().startsWith(".")) {
						KieProject kp = new KieProject(repository, file.getName());
						result.add(kp);
					}
				}
			}
			finally {
				git.close();
			}
		}
		*/
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceDelegate#createOrganization(org.kie.eclipse.navigator.view.server.IKieOrganization)
	 */
	@Override
	public void createOrganization(IKieOrganizationHandler organization) throws IOException {
		final String jobId = httpPost("organizationalunits", organization.getProperties());
		try {
			String status = getJobStatus(jobId, "Creating Organization '"+organization.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to create Organization '"+organization.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to create Organization '"+organization.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to create Organization '"+organization.getName()+"' was canceled");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceDelegate#createRepository(org.kie.eclipse.navigator.view.server.IKieRepository)
	 */
	@Override
	public void createRepository(IKieRepositoryHandler repository) throws IOException {
		final String jobId = httpPost("repositories", repository.getProperties());
		try {
			String status = getJobStatus(jobId, "Creating Repository '"+repository.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to create Repository '"+repository.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to create Repository '"+repository.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to create Repository '"+repository.getName()+"' was canceled");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceDelegate#addRepository(org.kie.eclipse.navigator.view.server.IKieRepository, org.kie.eclipse.navigator.view.server.IKieOrganization)
	 */
	public void addRepository(IKieRepositoryHandler repository, IKieOrganizationHandler organization) throws IOException {
		String jobId = httpPost("organizationalunits/" + organization.getName() + "/repositories/"+repository.getName(), null);
		try {
			String status = getJobStatus(jobId, "Adding Repository '"+repository.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to add Repository '"+repository.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to add Repository '"+repository.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to add Repository '"+repository.getName()+"' was canceled");
		}
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceDelegate#createProject(org.kie.eclipse.navigator.view.server.IKieProject)
	 */
	@Override
	public void createProject(IKieProjectHandler project) throws IOException {
		String jobId = httpPost("repositories/" + project.getParent().getName() + "/projects/", project.getProperties());
		try {
			String status = getJobStatus(jobId, "Creating Project '"+project.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to create Project '"+project.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to create Project '"+project.getName()+"' has failed with status "+status);
		}
		catch (Exception e) {
			deleteJob(jobId);
			throw new IOException("Request to create Project '"+project.getName()+"' was canceled");
		}
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceDelegate#deleteOrganization(org.kie.eclipse.navigator.view.server.IKieOrganization)
	 */
	@Override
	public void deleteOrganization(IKieOrganizationHandler organization) throws IOException {
		String jobId;
		jobId = httpDelete("organizationalunits/" + organization.getName());
		try {
			String status = getJobStatus(jobId, "Deleting Organization '"+organization.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to delete Organization '"+organization.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to delete Organization '"+organization.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to delete Organization '"+organization.getName()+"' was canceled");
		}
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceDelegate#deleteRepository(org.kie.eclipse.navigator.view.server.IKieRepository, boolean)
	 */
	@Override
	public void deleteRepository(IKieRepositoryHandler repository, boolean removeOnly) throws IOException {
		String jobId;
		if (removeOnly) {
			// only remove the repo from its organizational unit
			String organization = repository.getParent().getName();
			jobId = httpDelete("organizationalunits/" + organization + "/repositories/"+repository.getName());
		}
		else {
			// completely obliterate the repository
			jobId = httpDelete("repositories/"+repository.getName());
		}
		try {
			String status = getJobStatus(jobId, "Deleting Repository '"+repository.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to delete Repository '"+repository.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to delete Repository '"+repository.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to delete Repository '"+repository.getName()+"' was canceled");
		}
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.server.IKieServiceDelegate#deleteProject(org.kie.eclipse.navigator.view.server.IKieProject)
	 */
	@Override
	public void deleteProject(IKieProjectHandler project) throws IOException {
		String jobId;
		jobId = httpDelete("repositories/" + project.getParent().getName() + "/projects/" + project.getName());
		try {
			String status = getJobStatus(jobId, "Deleting project '"+project.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to delete project '"+project.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to delete project '"+project.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to delete project '"+project.getName()+"' was canceled");
		}
	}

	@Override
	public void updateOrganization(String oldName, IKieOrganizationHandler organization) throws IOException {
		JsonObject properties = new JsonObject(organization.getProperties());
		// remove illegal properties
		properties.remove("repositories");
		
		final String jobId = httpPost("organizationalunits/"+oldName, properties);
		try {
			String status = getJobStatus(jobId, "Updating Organization '"+organization.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to update Organization '"+organization.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to update Organization '"+organization.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to update Organization '"+organization.getName()+"' was canceled");
		}
	}

	@Override
	public void updateRepository(String oldName, IKieRepositoryHandler repository) throws IOException {
		/* TODO: This is not yet supported
		JsonObject properties = new JsonObject(repository.getProperties());
		// remove illegal properties
		properties.remove("requestType");
		properties.remove("gitURL");
		
		final String jobId = httpPost("repositories/"+oldName, repository.getProperties());
		try {
			String status = getJobStatus(jobId, "Updating Repository '"+repository.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to update Repository '"+repository.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to update Repository '"+repository.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to update Repository '"+repository.getName()+"' was canceled");
		}
		*/
	}

	@Override
	public void updateProject(String oldName, IKieProjectHandler project) throws IOException {
		/* TODO: This is not yet supported
		String jobId = httpPost("repositories/" + project.getParent().getName() + "/projects/", project.getProperties());
		try {
			String status = getJobStatus(jobId, "Updating Project '"+project.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to update Project '"+project.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to update Project '"+project.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to update Project '"+project.getName()+"' was canceled");
		}
		*/
	}

	public void mavenCompile(IKieProjectHandler project, JsonObject params) throws IOException {
		String jobId = httpPost("repositories/" + project.getParent().getName() + "/projects/"+
				"/maven/compile/", params);
		try {
			String status = getJobStatus(jobId, "Compiling Project '"+project.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to compile Project '"+project.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to compile Project '"+project.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to compile Project '"+project.getName()+"' was canceled");
		}
	}

	public void mavenInstall(IKieProjectHandler project, JsonObject params) throws IOException {
		String jobId = httpPost("repositories/" + project.getParent().getName() + "/projects/"+
				"/maven/install/", params);
		try {
			String status = getJobStatus(jobId, "Installing Project '"+project.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to install Project '"+project.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to install Project '"+project.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to install Project '"+project.getName()+"' was canceled");
		}
	}

	public void mavenTest(IKieProjectHandler project, JsonObject params) throws IOException {
		String jobId = httpPost("repositories/" + project.getParent().getName() + "/projects/"+
				"/maven/test/", params);
		try {
			String status = getJobStatus(jobId, "Testing Project '"+project.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to test Project '"+project.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to test Project '"+project.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to test Project '"+project.getName()+"' was canceled");
		}
	}

	public void mavenDeploy(IKieProjectHandler project, JsonObject params) throws IOException {
		String jobId = httpPost("repositories/" + project.getParent().getName() + "/projects/"+
				"/maven/deploy/", params);
		try {
			String status = getJobStatus(jobId, "Deploying Project '"+project.getName()+"'");
			
			if (status==null) {
				throw new IOException("Request to deploy Project '"+project.getName()+"' has timed out");
			}
			if (!status.startsWith(JOB_STATUS_SUCCESS))
				throw new IOException("Request to deploy Project '"+project.getName()+"' has failed with status "+status);
		}
		catch (InterruptedException e) {
			deleteJob(jobId);
			throw new IOException("Request to deploy Project '"+project.getName()+"' was canceled");
		}
	}
}
