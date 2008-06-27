package org.guvnor.tools.wizards;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.utils.webdav.WebDavSessionAuthenticator;

public class NewRepLocationWizard extends Wizard implements INewWizard, IGuvnorWizard {
	
	private GuvnorMainConfigPage mainPage;
//	private IWorkbench workbench;
	
	private GuvWizardModel model = new GuvWizardModel();
	
	public GuvWizardModel getModel() {
		return model;
	}
	
	@Override
	public void addPages() {
		mainPage = new GuvnorMainConfigPage("config_page", "New Guvnor location", 
										Activator.getImageDescriptor(Activator.IMG_GUVLOCADD));
		mainPage.setDescription("Create a new Guvnor repository connection");
		super.addPage(mainPage);
		super.addPages();
	}
	
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}
	
	@Override
	public boolean performFinish() {
		try {
			Activator.getLocationManager().addRepository(new GuvnorRepository(model.getRepLocation()));
			URL serverUrl = new URL(model.getRepLocation());
			Map<String, String> info = new HashMap<String, String>();
			info.put("username", model.getUsername());
			info.put("password", model.getPassword());
			if (model.shouldSaveAuthInfo()) {
				Platform.addAuthorizationInfo(serverUrl, "", "basic", info);	
			} else {
				IWebDavClient client = WebDavClientFactory.createClient(serverUrl);
				WebDavServerCache.cacheWebDavClient(serverUrl.toString(), client);
				WebDavSessionAuthenticator authen = new WebDavSessionAuthenticator();
				authen.addAuthenticationInfo(serverUrl, "", "basic", info);
				client.setSessionAuthenticator(authen);
				client.setSessionAuthentication(true);
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
//		this.workbench = workbench;
	}
}
