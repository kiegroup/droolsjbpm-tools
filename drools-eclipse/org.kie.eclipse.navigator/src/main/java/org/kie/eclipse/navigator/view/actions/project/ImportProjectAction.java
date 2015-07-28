package org.kie.eclipse.navigator.view.actions.project;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.content.ProjectNode;
import org.kie.eclipse.navigator.view.utils.ActionUtils;

public class ImportProjectAction extends KieNavigatorAction {

	protected ImportProjectAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}
	
	public ImportProjectAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Import Project");
	}

	public void run() {
		IContainerNode<?> container = getContainer();
		if (!(container instanceof ProjectNode)) {
			return;
		}
		
		final ProjectNode projectNode = (ProjectNode) container;
		ActionUtils.importProject(projectNode, this);
    }
}