package org.kie.eclipse.navigator.view.actions.project;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.kie.eclipse.navigator.view.actions.KieNavigatorActionProvider;

public class ProjectActionProvider extends KieNavigatorActionProvider {

	private ImportProjectAction importAction;
	
	public ProjectActionProvider() {
	}

    @Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		if (importAction.isEnabled())
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, importAction);
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        importAction = new ImportProjectAction(aSite.getStructuredViewer());
        addAction(importAction);
    }
}
