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

/**
 *
 */
public interface IKieServerHandler extends IKieResourceHandler {

	List<IKieOrganizationHandler> getOrganizations() throws IOException;
	List<IKieRepositoryHandler> getRepositories(IKieOrganizationHandler organization) throws IOException;
	List<IKieProjectHandler> getProjects(IKieRepositoryHandler repository) throws IOException;
}
