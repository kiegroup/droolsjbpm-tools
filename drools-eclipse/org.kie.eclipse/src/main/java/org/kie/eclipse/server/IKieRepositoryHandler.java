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

package org.kie.eclipse.server;

import java.util.List;

import org.eclipse.jgit.lib.Repository;

/**
 *
 */
public interface IKieRepositoryHandler extends IKieResourceHandler {
	Repository getRepository();
	List<IKieProjectHandler> getProjects() throws Exception;
}
