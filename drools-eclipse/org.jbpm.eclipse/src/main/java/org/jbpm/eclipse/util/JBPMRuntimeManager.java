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

package org.jbpm.eclipse.util;

import org.eclipse.jface.preference.IPreferenceStore;
import org.jbpm.eclipse.JBPMEclipsePlugin;
import org.jbpm.eclipse.preferences.JBPMConstants;
import org.kie.eclipse.runtime.AbstractRuntimeManager;
import org.kie.eclipse.runtime.IRuntime;

public class JBPMRuntimeManager extends AbstractRuntimeManager {
	
    private static JBPMRuntimeManager manager;
    public static JBPMRuntimeManager getDefault() {
    	if( manager == null )
    		manager = new JBPMRuntimeManager();
    	return manager;
    }
    
	@Override
	public String getBundleRuntimeLocation() {
    	return ".jbpm.runtime";
	}
	
	@Override
	public String getRuntimePreferenceKey() {
		return JBPMConstants.JBPM_RUNTIMES;
	}
	
	@Override
	public boolean isMavenized(IRuntime runtime) {
    	return false; //DroolsRuntime.ID_DROOLS_6.equals(runtimeId);
	}
	
	@Override
	public String getSettingsFilename() {
		return ".jbpm.runtime";
	}
	@Override
	public String getBundleSymbolicName() {
		return "org.jbpm.eclipse";
	}
	@Override
	public IRuntime createNewRuntime() {
		return new JBPMRuntime();
	}
	@Override
	public void logException(Throwable t) {
		JBPMEclipsePlugin.log(t);
	}
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return JBPMEclipsePlugin.getDefault().getPreferenceStore();
	}
}
