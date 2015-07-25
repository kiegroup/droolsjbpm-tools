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
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerPort;

/**
 *
 */
public class ServerProxy implements IServer {

	final IServer server;
	
	public ServerProxy(IServer server) {
		this.server = server;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return server.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getId()
	 */
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return server.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#delete()
	 */
	@Override
	public void delete() throws CoreException {
		server.delete();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return server.isReadOnly();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#isWorkingCopy()
	 */
	@Override
	public boolean isWorkingCopy() {
		// TODO Auto-generated method stub
		return server.isWorkingCopy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return server.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#loadAdapter(java.lang.Class, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Object loadAdapter(Class adapter, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return server.loadAdapter(adapter, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getHost()
	 */
	@Override
	public String getHost() {
		// TODO Auto-generated method stub
		return server.getHost();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getRuntime()
	 */
	@Override
	public IRuntime getRuntime() {
		// TODO Auto-generated method stub
		return server.getRuntime();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getServerType()
	 */
	@Override
	public IServerType getServerType() {
		// TODO Auto-generated method stub
		return server.getServerType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getServerConfiguration()
	 */
	@Override
	public IFolder getServerConfiguration() {
		// TODO Auto-generated method stub
		return server.getServerConfiguration();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#createWorkingCopy()
	 */
	@Override
	public IServerWorkingCopy createWorkingCopy() {
		// TODO Auto-generated method stub
		return server.createWorkingCopy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getModules()
	 */
	@Override
	public IModule[] getModules() {
		// TODO Auto-generated method stub
		return server.getModules();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#canModifyModules(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return server.canModifyModules(add, remove, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getAttribute(java.lang.String, int)
	 */
	@Override
	public int getAttribute(String attributeName, int defaultValue) {
		// TODO Auto-generated method stub
		return server.getAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getAttribute(java.lang.String, boolean)
	 */
	@Override
	public boolean getAttribute(String attributeName, boolean defaultValue) {
		// TODO Auto-generated method stub
		return server.getAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public String getAttribute(String attributeName, String defaultValue) {
		// TODO Auto-generated method stub
		return server.getAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getAttribute(java.lang.String, java.util.List)
	 */
	@Override
	public List<String> getAttribute(String attributeName, List<String> defaultValue) {
		// TODO Auto-generated method stub
		return server.getAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getAttribute(java.lang.String, java.util.Map)
	 */
	@Override
	public Map getAttribute(String attributeName, Map defaultValue) {
		// TODO Auto-generated method stub
		return server.getAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getChildModules(org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IModule[] getChildModules(IModule[] module, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return server.getChildModules(module, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getRootModules(org.eclipse.wst.server.core.IModule, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IModule[] getRootModules(IModule module, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return server.getRootModules(module, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerAttributes#getServerPorts(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public ServerPort[] getServerPorts(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return server.getServerPorts(monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean contains(ISchedulingRule rule) {
		// TODO Auto-generated method stub
		return server.contains(rule);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		// TODO Auto-generated method stub
		return server.isConflicting(rule);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getServerState()
	 */
	@Override
	public int getServerState() {
		return IServer.STATE_STARTED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getMode()
	 */
	@Override
	public String getMode() {
		// TODO Auto-generated method stub
		return server.getMode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getServerPublishState()
	 */
	@Override
	public int getServerPublishState() {
		// TODO Auto-generated method stub
		return server.getServerPublishState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#addServerListener(org.eclipse.wst.server.core.IServerListener)
	 */
	@Override
	public void addServerListener(IServerListener listener) {
		// TODO Auto-generated method stub
		server.addServerListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#addServerListener(org.eclipse.wst.server.core.IServerListener, int)
	 */
	@Override
	public void addServerListener(IServerListener listener, int eventMask) {
		// TODO Auto-generated method stub
		server.addServerListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#removeServerListener(org.eclipse.wst.server.core.IServerListener)
	 */
	@Override
	public void removeServerListener(IServerListener listener) {
		// TODO Auto-generated method stub
		server.removeServerListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#addPublishListener(org.eclipse.wst.server.core.IPublishListener)
	 */
	@Override
	public void addPublishListener(IPublishListener listener) {
		// TODO Auto-generated method stub
		server.addPublishListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#removePublishListener(org.eclipse.wst.server.core.IPublishListener)
	 */
	@Override
	public void removePublishListener(IPublishListener listener) {
		// TODO Auto-generated method stub
		server.removePublishListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#canPublish()
	 */
	@Override
	public IStatus canPublish() {
		// TODO Auto-generated method stub
		return server.canPublish();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#shouldPublish()
	 */
	@Override
	public boolean shouldPublish() {
		// TODO Auto-generated method stub
		return server.shouldPublish();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#publish(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus publish(int kind, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return server.publish(kind, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#publish(int, java.util.List, org.eclipse.core.runtime.IAdaptable, org.eclipse.wst.server.core.IServer.IOperationListener)
	 */
	@Override
	public void publish(int kind, List<IModule[]> modules, IAdaptable info, IOperationListener listener) {
		// TODO Auto-generated method stub
		server.publish(kind, modules, info, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#canStart(java.lang.String)
	 */
	@Override
	public IStatus canStart(String launchMode) {
		// TODO Auto-generated method stub
		return server.canStart(launchMode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#start(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void start(String launchMode, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		server.start(launchMode, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#start(java.lang.String, org.eclipse.wst.server.core.IServer.IOperationListener)
	 */
	@Override
	public void start(String launchMode, IOperationListener listener) {
		// TODO Auto-generated method stub
		server.start(launchMode, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#canRestart(java.lang.String)
	 */
	@Override
	public IStatus canRestart(String mode) {
		// TODO Auto-generated method stub
		return server.canRestart(mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#shouldRestart()
	 */
	@Override
	public boolean shouldRestart() {
		// TODO Auto-generated method stub
		return server.shouldRestart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getServerRestartState()
	 */
	@Override
	public boolean getServerRestartState() {
		// TODO Auto-generated method stub
		return server.getServerRestartState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#restart(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void restart(String launchMode, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		server.restart(launchMode, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#restart(java.lang.String, org.eclipse.wst.server.core.IServer.IOperationListener)
	 */
	@Override
	public void restart(String launchMode, IOperationListener listener) {
		// TODO Auto-generated method stub
		server.restart(launchMode, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#canStop()
	 */
	@Override
	public IStatus canStop() {
		// TODO Auto-generated method stub
		return server.canStop();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#stop(boolean)
	 */
	@Override
	public void stop(boolean force) {
		// TODO Auto-generated method stub
		server.stop(force);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#stop(boolean, org.eclipse.wst.server.core.IServer.IOperationListener)
	 */
	@Override
	public void stop(boolean force, IOperationListener listener) {
		// TODO Auto-generated method stub
		server.stop(force, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getModuleState(org.eclipse.wst.server.core.IModule[])
	 */
	@Override
	public int getModuleState(IModule[] module) {
		// TODO Auto-generated method stub
		return server.getModuleState(module);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getModulePublishState(org.eclipse.wst.server.core.IModule[])
	 */
	@Override
	public int getModulePublishState(IModule[] module) {
		// TODO Auto-generated method stub
		return server.getModulePublishState(module);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getModuleRestartState(org.eclipse.wst.server.core.IModule[])
	 */
	@Override
	public boolean getModuleRestartState(IModule[] module) {
		// TODO Auto-generated method stub
		return server.getModuleRestartState(module);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#canControlModule(org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus canControlModule(IModule[] module, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return server.canControlModule(module, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#canRestartModule(org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus canRestartModule(IModule[] module, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return server.canRestartModule(module, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#canPublishModule(org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus canPublishModule(IModule[] module, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return server.canPublishModule(module, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#startModule(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IServer.IOperationListener)
	 */
	@Override
	public void startModule(IModule[] module, IOperationListener listener) {
		// TODO Auto-generated method stub
		server.startModule(module, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#stopModule(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IServer.IOperationListener)
	 */
	@Override
	public void stopModule(IModule[] module, IOperationListener listener) {
		// TODO Auto-generated method stub
		server.stopModule(module, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#restartModule(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IServer.IOperationListener)
	 */
	@Override
	public void restartModule(IModule[] module, IOperationListener listener) {
		// TODO Auto-generated method stub
		server.restartModule(module, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getLaunchConfiguration(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public ILaunchConfiguration getLaunchConfiguration(boolean create, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return server.getLaunchConfiguration(create, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		// TODO Auto-generated method stub
		return server.getLaunch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getStartTimeout()
	 */
	@Override
	public int getStartTimeout() {
		// TODO Auto-generated method stub
		return server.getStartTimeout();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#getStopTimeout()
	 */
	@Override
	public int getStopTimeout() {
		// TODO Auto-generated method stub
		return server.getStopTimeout();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#synchronousStart(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void synchronousStart(String launchMode, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		server.synchronousStart(launchMode, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#synchronousStop(boolean)
	 */
	@Override
	public void synchronousStop(boolean force) {
		// TODO Auto-generated method stub
		server.synchronousStop(force);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServer#synchronousRestart(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void synchronousRestart(String launchMode, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		server.synchronousStart(launchMode, monitor);
	}

}
