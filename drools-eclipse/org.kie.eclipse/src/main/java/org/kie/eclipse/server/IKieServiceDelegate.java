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
 ******************************************************************************/

package org.kie.eclipse.server;

import java.io.IOException;
import java.util.List;

import org.eclipse.wst.server.core.IServer;

/**
 *
 */
public interface IKieServiceDelegate {

	public final static String JOB_STATUS_SUCCESS = "SUCCESS";
	public final static String JOB_STATUS_BAD_REQUEST = "BAD_REQUEST";
	

	void setServer(IServer server);
	IServer getServer();
	
	void setHandler(IKieResourceHandler handler);
	IKieResourceHandler getHandler();
	
	List<IKieSpaceHandler> getSpaces(IKieServerHandler service) throws IOException;
	List<IKieRepositoryHandler> getRepositories(IKieSpaceHandler space) throws IOException;
	List<IKieRepositoryHandler> getRepositories(IKieServerHandler server) throws IOException;
	List<IKieProjectHandler> getProjects(IKieRepositoryHandler repository) throws IOException;

	void createSpace(IKieSpaceHandler space) throws IOException;
	void createRepository(IKieRepositoryHandler repository) throws IOException;
	void addRepository(IKieRepositoryHandler repository, IKieSpaceHandler space) throws IOException;
	void createProject(IKieProjectHandler project) throws IOException;

	void deleteSpace(IKieSpaceHandler space) throws IOException;
	void deleteRepository(IKieRepositoryHandler repository, boolean removeOnly) throws IOException;
	void deleteProject(IKieProjectHandler project) throws IOException;

	void updateSpace(String oldName, IKieSpaceHandler space) throws IOException;
	void updateRepository(String oldName, IKieRepositoryHandler repository) throws IOException;
	void updateProject(String oldName, IKieProjectHandler project) throws IOException;

	String getUsername();
	String getPassword();
	int getHttpPort();
	int getGitPort();
	String getKieApplication();
}
