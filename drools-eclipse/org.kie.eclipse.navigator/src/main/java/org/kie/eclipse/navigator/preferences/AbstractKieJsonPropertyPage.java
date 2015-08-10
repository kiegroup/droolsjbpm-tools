package org.kie.eclipse.navigator.preferences;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieOrganizationHandler;
import org.kie.eclipse.server.IKieProjectHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieResourceHandler;
import org.kie.eclipse.server.IKieServiceDelegate;

import com.eclipsesource.json.JsonObject;

public abstract class AbstractKieJsonPropertyPage extends AbstractKiePropertyPage {

	protected IPreferenceStore preferenceStore;
	protected JsonObject properties;

	public AbstractKieJsonPropertyPage() {
		super(GRID);
	}
	
	@Override
	protected String getPreferenceName(String name) {
		return name;
	}
	
	@Override
    public IPreferenceStore getPreferenceStore() {
		if (preferenceStore==null) {
			properties = new JsonObject(getResourceHandler().getProperties());
			preferenceStore = new JsonPreferenceStore(properties);
		}
		return preferenceStore;
	}
	
	@Override
	protected void performDefaults() {
		properties.copyFrom(getResourceHandler().getProperties());
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		boolean rtn = super.performOk();
		if (rtn) {
			JsonObject oldProperties = getResourceHandler().getProperties();
			if (!properties.equals(oldProperties)) {
				String oldName = getResourceHandler().getName();
				getResourceHandler().setProperties(properties);
				// update the values on the server
				IKieResourceHandler handler = getResourceHandler();
				IKieServiceDelegate delegate = handler.getDelegate();
				try {
					if (handler instanceof IKieOrganizationHandler) {
						delegate.updateOrganization(oldName, (IKieOrganizationHandler)handler);
					}
					else if (handler instanceof IKieRepositoryHandler) {
						delegate.updateRepository(oldName, (IKieRepositoryHandler)handler);
					}
					if (handler instanceof IKieProjectHandler) {
						delegate.updateProject(oldName, (IKieProjectHandler)handler);
					}
	            	IContainerNode<?> container = (IContainerNode<?>)getContentNode().getParent();
	            	container.refresh();

				}
				catch (IOException e) {
					e.printStackTrace();
					getResourceHandler().setProperties(oldProperties);
	            	MessageDialog.openError(getShell(), "Error", e.getMessage());
				}
			}
		}
		return rtn;
	}
}
