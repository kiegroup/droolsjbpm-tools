package org.kie.eclipse.wizard.project;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class KieProjectWizardPage extends WizardNewProjectCreationPage implements IKieProjectWizardPage {

	public KieProjectWizardPage(String pageName) {
		super(pageName);
	}

    protected void setControlVisible(Control control, boolean visible) {
    	Object ld = control.getLayoutData();
    	if (ld instanceof GridData) {
    		((GridData)ld).exclude = !visible;
    	}
    	control.setVisible(visible);
    	control.getParent().layout();
    }

	@Override
	public Collection<IProjectDescription> getNewProjectDescriptions() {
	   	Collection<IProjectDescription> result = new ArrayList<IProjectDescription> ();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject project = getProjectHandle();
        IPath newPath = useDefaults() ? null : getLocationPath();
        IProjectDescription description = workspace.newProjectDescription(project.getName());
        description.setLocation(newPath);
        result.add(description);
    	return result;
	}
}
