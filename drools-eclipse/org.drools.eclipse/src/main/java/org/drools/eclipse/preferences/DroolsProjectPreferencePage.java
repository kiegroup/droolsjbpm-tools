package org.drools.eclipse.preferences;

import java.io.ByteArrayInputStream;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.util.DroolsRuntime;
import org.drools.eclipse.util.DroolsRuntimeManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class DroolsProjectPreferencePage extends PropertyAndPreferencePage {

	public static final String PREF_ID= "org.drools.eclipse.preferences.DroolsRuntimesPreferencePage";
	public static final String PROP_ID= "org.drools.eclipse.preferences.DroolsProjectPreferencePage";

	private Combo droolsRuntimeCombo;
	
	public DroolsProjectPreferencePage() {
		setTitle("Drools Project Preferences");
	}
	
	protected Control createPreferenceContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Drools Runtime: ");
        droolsRuntimeCombo = new Combo(composite, SWT.LEFT);
        DroolsRuntime[] runtimes = DroolsRuntimeManager.getDroolsRuntimes();
        int selection = -1;
        String currentRuntime = DroolsRuntimeManager.getDroolsRuntime(getProject());
        for (int i = 0; i < runtimes.length; i++) {
        	droolsRuntimeCombo.add(runtimes[i].getName());
        	if (runtimes[i].getName().equals(currentRuntime)) {
        		selection = i;
        	}
        }
        if (selection != -1) {
        	droolsRuntimeCombo.select(selection);
        } else if (runtimes.length > 0) {
            droolsRuntimeCombo.select(0);
        }
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        droolsRuntimeCombo.setLayoutData(gridData);
		return composite;
	}
	
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	protected String getPropertyPageID() {
		return PROP_ID;
	}

	protected boolean hasProjectSpecificOptions(IProject project) {
		return project.getFile(".settings/.drools.runtime").exists();
	}

	public boolean performOk() {
		try {
			IFile file = getProject().getFile(".settings/.drools.runtime");
			if (useProjectSettings()) {
				String runtime = "<runtime>"
					+ droolsRuntimeCombo.getItem(droolsRuntimeCombo.getSelectionIndex())
					+ "</runtime>";
				if (!file.exists()) {
					IFolder folder = getProject().getFolder(".settings");
					if (!folder.exists()) {
						folder.create(true, true, null);
					}
					file.create(new ByteArrayInputStream(runtime.getBytes()), true, null);
				} else {
					file.setContents(new ByteArrayInputStream(runtime.getBytes()), true, false, null);
				}
			} else {
				if (file.exists()) {
					file.delete(true, null);
				}
			}
			getProject().close(null);
			getProject().open(null);
		} catch (Throwable t) {
			DroolsEclipsePlugin.log(t);
			return false;
		}
		return super.performOk();
	}
	
}
