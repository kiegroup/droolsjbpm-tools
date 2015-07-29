package org.kie.eclipse.navigator.view.actions.repository;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.actions.dialogs.CreateProjectRequestDialog;
import org.kie.eclipse.navigator.view.content.ContentNode;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.content.ProjectNode;
import org.kie.eclipse.navigator.view.content.RepositoryNode;
import org.kie.eclipse.navigator.view.utils.ActionUtils;
import org.kie.eclipse.server.IKieProjectHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieServiceDelegate;
import org.kie.eclipse.server.KieProjectHandler;
import org.kie.eclipse.server.KieRepositoryHandler;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CreateProjectAction extends KieNavigatorAction {

	protected CreateProjectAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public CreateProjectAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Create Project...");
	}

	@Override
	public String getToolTipText() {
		return "Create a new Project";
	}

	@Override
	public boolean isEnabled() {
		IContainerNode<?> container = getContainer();
		if (container instanceof ContentNode) {
			KieRepositoryHandler handler = (KieRepositoryHandler) ((ContentNode) container).getHandler();
			if (handler == null || !handler.isLoaded())
				return false;
		}
		return true;
	}

	public void run() {
		IContainerNode<?> container = getContainer();
		if (container==null)
			return;

		IKieRepositoryHandler repository = (IKieRepositoryHandler) container.getHandler();
        IKieServiceDelegate delegate = getDelegate();

        CreateProjectRequestDialog dlg = new CreateProjectRequestDialog(getShell(), repository);

        if (dlg.open()==Window.OK) {
        	JsonObject properties = dlg.getResult();
        	String name = properties.get("name").asString();
            IKieProjectHandler project = new KieProjectHandler(repository, name);
            project.setProperties(properties);
	        try {
	        	delegate.createProject(project);
	        	
				ProjectNode projectNode = null;
	            if (dlg.shouldImportProject()) {
	            	container.load();
	            	for (Object child : container.getChildren()) {
	            		if (child instanceof ProjectNode) {
	            			if (project.getName().equals(((ProjectNode)child).getName())) {
	            				projectNode = (ProjectNode) child;
	            				break;
	            			}
	            		}
	            	}
	            	if (projectNode==null) {
	            		MessageDialog.openError(getShell(), "Error", "The Project '"+project.getName()+"' is not found!");
	            		return;
	            	}
	            	IJavaProject javaProject = ActionUtils.importProject(projectNode, this);
	            	if (javaProject!=null && dlg.shouldCreateArtifacts()) {
	            		String artifactId = dlg.getArtifactId();
	                   	
	                   	JsonObject projectProperties = projectNode.getHandler().getProperties();
	                   	JsonValue jv = projectProperties.get("groupId");
	                   	String groupId = null;
	                   	if (jv==null || jv.asString().isEmpty()) {
	                   		if (projectNode.getParent() instanceof RepositoryNode) {
		                       	JsonObject orgProperties = projectNode.getParent().getParent().getHandler().getProperties();
		                   		jv = orgProperties.get("defaultGroupId");
	                   		}
	                   	}
	                   	if (jv!=null)
	                   		groupId = jv.asString();

	                   	String version = null;
						jv = projectProperties.get("version");
	                   	if (jv!=null)
	                   		version = jv.asString();

	                   	ActionUtils.createProjectArtifacts(javaProject, groupId, artifactId, version, null);
	            	}
	            }
				container.refresh();
	        }
	        catch (Exception e) {
	        	handleException(e);
	        }
        }
	}
}