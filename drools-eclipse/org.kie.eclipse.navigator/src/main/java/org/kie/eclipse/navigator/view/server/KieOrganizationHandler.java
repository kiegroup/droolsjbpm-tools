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

import java.util.List;

/**
 *
 */
public class KieOrganizationHandler extends KieResourceHandler implements IKieOrganizationHandler {

	/**
	 * 
	 */
	public KieOrganizationHandler(IKieServerHandler service, String name) {
		super(service, name);
	}

	@Override
	public Object getResource() {
		return properties;
	}

	public List<? extends IKieResourceHandler> getChildren() throws Exception {
		if (children==null || children.isEmpty()) {
			children = getDelegate().getRepositories(this);
		}
		return children;
	}
	
	public boolean isLoaded() {
		return true;
	}
	
	public List<IKieRepositoryHandler> getRepositories() throws Exception {
		return (List<IKieRepositoryHandler>) getChildren();
	}
}
