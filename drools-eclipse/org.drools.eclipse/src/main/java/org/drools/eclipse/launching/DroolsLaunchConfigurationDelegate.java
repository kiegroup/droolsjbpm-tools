/*
 * Copyright 2010 JBoss Inc
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

import java.text.MessageFormat;

import org.drools.eclipse.debug.core.IDroolsDebugConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

public class DroolsLaunchConfigurationDelegate extends JavaLaunchDelegate {

    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (monitor.isCanceled()) {
            return;
        }
        // TODO make sure that all DRLs needed during execution are built and cached
        super.launch(configuration, mode, launch, monitor);
        if (mode.equals(ILaunchManager.DEBUG_MODE)) {
            // TODO only retrieve breakpoints of this project or any
            // of its dependent projects
            IBreakpoint[] breakpoints = getDroolsBreakpoints();
            for (int i = 0; i < breakpoints.length; i++) {
                launch.getDebugTarget().breakpointAdded(breakpoints[i]);
            }
        }
    }

    private IBreakpoint[] getDroolsBreakpoints() {
        return DebugPlugin.getDefault().getBreakpointManager()
             .getBreakpoints(IDroolsDebugConstants.ID_DROOLS_DEBUG_MODEL);
    }

    public IVMRunner getVMRunner(ILaunchConfiguration configuration, String mode) throws CoreException {
        IVMInstall vm = verifyVMInstall(configuration);
        IVMRunner runner = new DroolsVMDebugger(vm);
        if (runner == null) {
            abort(MessageFormat.format(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_0, new String[]{vm.getName(), mode}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);
        }
        return runner;
    }

}
