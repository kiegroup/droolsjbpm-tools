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

package org.guvnor.tools.wizards;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;

/**
 * Wizard page for entering Guvnor connection details.
 * @author jgraham
 */
public class GuvnorConnectionEditPage extends GuvnorMainConfigPage {
	
	private GuvnorRepository rep;

	public GuvnorConnectionEditPage(String pageName) {
		super(pageName);
	}
	
	public GuvnorConnectionEditPage(GuvnorRepository rep, String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		this.rep = rep;
	}
	
	protected String getGuvnorLocation(){
		return rep.getLocation();
	}
	 
	
	protected Map getSecurityInfo(){
		Map info = null;
		try {
			info = Platform.getAuthorizationInfo(
					new URL(rep.getLocation()), "", "basic");
		} catch (MalformedURLException e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
			
			return null;
		} //$NON-NLS-1$ //$NON-NLS-2$
		
		return info;
	}
	
	protected boolean shouldSavePasswords(){
		return getSecurityInfo() != null;
	}

	
}
