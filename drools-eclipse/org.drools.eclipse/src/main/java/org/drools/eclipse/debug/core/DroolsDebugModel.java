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

package org.drools.eclipse.debug.core;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;

import com.sun.jdi.VirtualMachine;

public class DroolsDebugModel {

    private DroolsDebugModel() {
    }

    public static IDebugTarget newDebugTarget(ILaunch launch, VirtualMachine vm, String name, IProcess process, boolean allowTerminate, boolean allowDisconnect) {
        return newDebugTarget(launch, vm, name, process, allowTerminate, allowDisconnect, true);
    }

    public static IDebugTarget newDebugTarget(final ILaunch launch, final VirtualMachine vm, final String name, final IProcess process, final boolean allowTerminate, final boolean allowDisconnect, final boolean resume) {
        final IJavaDebugTarget[] target = new IJavaDebugTarget[1];
        IWorkspaceRunnable r = new IWorkspaceRunnable() {
            public void run(IProgressMonitor m) {
                target[0]= new DroolsDebugTarget(launch, vm, name, allowTerminate, allowDisconnect, process, resume);
            }
        };
        try {
            ResourcesPlugin.getWorkspace().run(r, null, 0, null);
        } catch (CoreException exc) {
            DroolsEclipsePlugin.log(exc);
        }
        return target[0];
    }
        
}
