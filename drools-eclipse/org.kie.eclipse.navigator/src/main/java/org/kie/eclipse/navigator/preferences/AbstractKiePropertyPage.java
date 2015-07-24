package org.kie.eclipse.navigator.preferences;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.kie.eclipse.navigator.IKieNavigatorConstants;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.server.IKieOrganizationHandler;
import org.kie.eclipse.navigator.view.server.IKieProjectHandler;
import org.kie.eclipse.navigator.view.server.IKieRepositoryHandler;
import org.kie.eclipse.navigator.view.server.IKieResourceHandler;
import org.kie.eclipse.navigator.view.server.IKieServiceDelegate;

import com.eclipsesource.json.JsonObject;

public abstract class AbstractKiePropertyPage extends FieldEditorPropertyPage implements IKieNavigatorConstants {

	protected IPreferenceStore preferenceStore;
	protected JsonObject properties;

	public AbstractKiePropertyPage() {
		super(GRID);
	}
	
	@Override
	protected abstract void createFieldEditors();
	
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
	protected void initialize() {
		super.initialize();
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
	            	container.clearChildren();
	            	container.getNavigator().getCommonViewer().refresh(container);

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
