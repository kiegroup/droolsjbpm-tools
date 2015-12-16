/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.launching;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Map;

import org.drools.eclipse.debug.core.DroolsDebugModel;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.SocketAttachConnector;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;


@SuppressWarnings("restriction")
public class DroolsVMConnector extends SocketAttachConnector {
    public static final String CONNECTOR_ID = "org.drools.eclipse.launching.droolsVMConnector";

    @Override
    public String getIdentifier() {
        return CONNECTOR_ID;
    }

    @Override
    public void connect(Map<String, String> arguments, IProgressMonitor monitor, ILaunch launch) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        subMonitor.beginTask(LaunchingMessages.SocketAttachConnector_Connecting____1, 2); 
        subMonitor.subTask(LaunchingMessages.SocketAttachConnector_Configuring_connection____1); 
        
        AttachingConnector connector= getAttachingConnector();
        String portNumberString = arguments.get("port"); //$NON-NLS-1$
        if (portNumberString == null) {
            abort(LaunchingMessages.SocketAttachConnector_Port_unspecified_for_remote_connection__2, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PORT); 
        }
        String host = arguments.get("hostname"); //$NON-NLS-1$
        if (host == null) {
            abort(LaunchingMessages.SocketAttachConnector_Hostname_unspecified_for_remote_connection__4, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_HOSTNAME); 
        }
        Map<String, Connector.Argument> map= connector.defaultArguments();
        
        Connector.Argument param= map.get("hostname"); //$NON-NLS-1$
        param.setValue(host);
        param= map.get("port"); //$NON-NLS-1$
        param.setValue(portNumberString);
        
        String timeoutString = arguments.get("timeout"); //$NON-NLS-1$
        if (timeoutString != null) {
            param= map.get("timeout"); //$NON-NLS-1$
            param.setValue(timeoutString);
        }
        
        ILaunchConfiguration configuration = launch.getLaunchConfiguration();
        boolean allowTerminate = false;
        if (configuration != null) {
            allowTerminate = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
        }
        subMonitor.worked(1);
        subMonitor.subTask(LaunchingMessages.SocketAttachConnector_Establishing_connection____2); 
        try {
            VirtualMachine vm = connector.attach(map);
            String vmLabel = constructVMLabel(vm, host, portNumberString, configuration);
            IDebugTarget debugTarget= DroolsDebugModel.newDebugTarget(launch, vm, vmLabel, null, allowTerminate, true);
            launch.addDebugTarget(debugTarget);
            subMonitor.worked(1);
            subMonitor.done();
        } catch (TimeoutException e) {
            abort(LaunchingMessages.SocketAttachConnector_0, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
        } catch (UnknownHostException e) {
            abort(NLS.bind(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_because_of_unknown_host____0___1, new String[]{host}), e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
        } catch (ConnectException e) {
            abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_as_connection_was_refused_2, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
        } catch (IOException e) {
            abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_1, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
        } catch (IllegalConnectorArgumentsException e) {
            abort(LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_1, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
        }
    }
}
