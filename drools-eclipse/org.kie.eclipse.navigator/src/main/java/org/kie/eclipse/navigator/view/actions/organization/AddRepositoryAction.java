package org.kie.eclipse.navigator.view.actions.organization;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.ListDialog;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieOrganizationHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.IKieServiceDelegate;

public class AddRepositoryAction extends KieNavigatorAction {

	protected AddRepositoryAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}
	
	public AddRepositoryAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Add Repository...");
	}

	public void run() {
        IContainerNode<?> container = getContainer();
        if (container==null)
        	return;
        
        IKieOrganizationHandler organization = (IKieOrganizationHandler) container.getHandler();
        IKieServerHandler server = (IKieServerHandler) container.getRoot().getHandler();
        IKieServiceDelegate delegate = getDelegate();

        try {
        	// get a list of all repositories
			List<IKieRepositoryHandler> allRepositories = delegate.getRepositories(server);
			// and remove the ones that are owned by an Organization
			for (IKieOrganizationHandler org : delegate.getOrganizations(server)) {
				for (IKieRepositoryHandler rep : org.getRepositories()) {
					for (IKieRepositoryHandler ar : allRepositories) {
						if (ar.getName().equals(rep.getName())) {
							allRepositories.remove(ar);
							break;
						}
					}
				}
			}
			// if there's anything left, allow the "Add Repository" operation
			if (allRepositories.isEmpty()) {
            	MessageDialog.openInformation(getShell(),
            			"No Repositories",
            			"All known Repositories are already assigned to other Organizational Units.");
			}
			else {
				ListDialog dlg = new ListDialog(getShell());
				dlg.setContentProvider(new IStructuredContentProvider() {

					@Override
					public void dispose() {
					}

					@Override
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}

					@Override
					public Object[] getElements(Object inputElement) {
						return ((List<IKieRepositoryHandler>)inputElement).toArray();
					}
					
				});
				dlg.setLabelProvider(new ILabelProvider() {
					@Override
					public void removeListener(ILabelProviderListener listener) {
					}
					
					@Override
					public boolean isLabelProperty(Object element, String property) {
						return false;
					}
					
					@Override
					public void dispose() {
					}
					
					@Override
					public void addListener(ILabelProviderListener listener) {
					}
					
					@Override
					public String getText(Object element) {
						return ((IKieRepositoryHandler)element).getName();
					}
					
					@Override
					public Image getImage(Object element) {
						return null;
					}
				});
				dlg.setInput(allRepositories);
				dlg.setMessage("Select a Repository to add to Organizational Unit "+organization.getName());
				dlg.setTitle("Add Repository to Organizationl Unit");
				if (dlg.open()==Window.OK) {
					Object[] result = dlg.getResult();
					if (result.length==1) {
			            try {
			            	delegate.addRepository((IKieRepositoryHandler)result[0], organization);
			            	refreshViewer(container.getRoot());
			            }
			            catch (Exception e) {
			            	handleException(e);
			            }

					}
				}
				
			}
		}
		catch (Exception e) {
        	handleException(e);
		}
    }
}