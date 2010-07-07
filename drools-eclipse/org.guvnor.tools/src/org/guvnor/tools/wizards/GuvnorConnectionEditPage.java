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
