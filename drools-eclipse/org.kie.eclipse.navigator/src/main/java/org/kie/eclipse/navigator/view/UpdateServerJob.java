/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.kie.eclipse.navigator.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
/**
 * Action to update a server's status.
 */
public class UpdateServerJob extends Job {
	/**
	 * An action to update the status of a server.
	 * 
	 * @param server a server
	 */
	private IServer[] servers;

	public UpdateServerJob(IServer[] servers2) {
		super("Updating Servers");
		this.servers = servers2;	
	}

	public IStatus run(IProgressMonitor monitor) {
		for (IServer server : servers){
			if (server.getServerType() != null && server.getServerState() == IServer.STATE_UNKNOWN) {
				monitor.subTask(NLS.bind("Updating {0}", server.getName()));
				server.loadAdapter(ServerBehaviourDelegate.class, monitor);	
			}
		}
		return Status.OK_STATUS;
	}
}