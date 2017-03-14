package org.kie.eclipse.wizard.project;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
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
    	control.getParent().getParent().layout();
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

	protected Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		label.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false, 1, 1));
		return label;
	}
	
	protected Text createText(Composite parent, String contents) {
		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		text.setText(contents);
		return text;
	}

	protected Button createCheckBox(Composite group, String label) {
        Button button = new Button(group, SWT.CHECK | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        data.horizontalIndent = 10;
        button.setLayoutData(data);
        return button;
    }

	protected Button createRadioButton(Composite group, String label) {
	    Button button = new Button(group, SWT.RADIO | SWT.LEFT);
	    button.setText(label);
	    GridData data = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
	    data.horizontalIndent = 10;
	    button.setLayoutData(data);
	    return button;
	}
	
	public IProgressMonitor getProgressMonitor() {
		AbstractKieProjectStartWizardPage startPage = (AbstractKieProjectStartWizardPage) getWizard().getPage(AbstractKieProjectWizard.START_PAGE);
		return startPage.getProgressMonitor();
	}
	
}
