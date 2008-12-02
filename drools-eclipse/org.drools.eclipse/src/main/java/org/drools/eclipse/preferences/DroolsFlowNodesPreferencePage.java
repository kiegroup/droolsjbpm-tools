package org.drools.eclipse.preferences;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DroolsFlowNodesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button ruleFlowGroupButton;
	private Button splitButton;
	private Button joinButton;
	private Button eventWaitButton;
	private Button subFlowButton;
	private Button actionButton;
	private Button timerButton;
	private Button faultButton;
	private Button eventButton;
	private Button humanTaskButton;
	private Button compositeButton;
	private Button forEachButton;
	private Button workItemsButton;
	
	public DroolsFlowNodesPreferencePage() {
		super("Drools Flow nodes");
	}

	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);
		noDefaultAndApplyButton();
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ancestor.setLayout(layout);
		Label l = new Label(ancestor, SWT.WRAP);
		l.setFont(ancestor.getFont());
		l.setText("Select which nodes are shown in the Drools Flow editor");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		l.setLayoutData(gd);
		l = new Label(ancestor, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 1;
		l.setLayoutData(gd);
		
		Button startButton = createCheckBox(ancestor, "Start");
		startButton.setSelection(true);
		startButton.setEnabled(false);
		Button endButton = createCheckBox(ancestor, "End");
		endButton.setSelection(true);
		endButton.setEnabled(false);
		ruleFlowGroupButton = createCheckBox(ancestor, "RuleFlowGroup");
		splitButton = createCheckBox(ancestor, "Split");
		joinButton = createCheckBox(ancestor, "Join");
		eventWaitButton = createCheckBox(ancestor, "Event Wait");
		subFlowButton = createCheckBox(ancestor, "SubFlow");
		actionButton = createCheckBox(ancestor, "Action");
		timerButton = createCheckBox(ancestor, "Timer");
		faultButton = createCheckBox(ancestor, "Fault");
		eventButton = createCheckBox(ancestor, "Event");
		humanTaskButton = createCheckBox(ancestor, "HumanTask");
		compositeButton = createCheckBox(ancestor, "Composite");
		forEachButton = createCheckBox(ancestor, "ForEach");
		workItemsButton = createCheckBox(ancestor, "WorkItems");
		initializeValues();
		
		applyDialogFont(ancestor);
		return ancestor;
	}
	
	private Button createCheckBox(Composite group, String label) {
        Button button = new Button(group, SWT.CHECK | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        button.setLayoutData(data);
        return button;
    }
	
	protected IPreferenceStore doGetPreferenceStore() {
        return DroolsEclipsePlugin.getDefault().getPreferenceStore();
    }
	
	private void initializeDefaults() {
        initializeValues("1111111111111");
	}

	private void initializeValues() {
        IPreferenceStore store = getPreferenceStore();
        String flowNodes = store.getString(IDroolsConstants.FLOW_NODES);
        if (flowNodes == null || flowNodes.length() != 13) {
        	flowNodes = "1111111111111";
        }
        initializeValues(flowNodes);
	}
	
	private void initializeValues(String flowNodes) {
    	ruleFlowGroupButton.setSelection(flowNodes.charAt(0) == '1');
		splitButton.setSelection(flowNodes.charAt(1) == '1');
		joinButton.setSelection(flowNodes.charAt(2) == '1');
		eventWaitButton.setSelection(flowNodes.charAt(3) == '1');
		subFlowButton.setSelection(flowNodes.charAt(4) == '1');
		actionButton.setSelection(flowNodes.charAt(5) == '1');
		timerButton.setSelection(flowNodes.charAt(6) == '1');
		faultButton.setSelection(flowNodes.charAt(7) == '1');
		eventButton.setSelection(flowNodes.charAt(8) == '1');
		humanTaskButton.setSelection(flowNodes.charAt(9) == '1');
		compositeButton.setSelection(flowNodes.charAt(10) == '1');
		forEachButton.setSelection(flowNodes.charAt(11) == '1');
		workItemsButton.setSelection(flowNodes.charAt(12) == '1');
    }

	protected void performDefaults() {
        super.performDefaults();
        initializeDefaults();
    }

	public boolean performOk() {
        storeValues();
        DroolsEclipsePlugin.getDefault().savePluginPreferences();
        return true;
    }
	
	private void storeValues() {
        IPreferenceStore store = getPreferenceStore();
        String flowNodes = "" +
        	(ruleFlowGroupButton.getSelection() ? '1' : '0') +
			(splitButton.getSelection() ? '1' : '0') +
			(joinButton.getSelection() ? '1' : '0') +
			(eventWaitButton.getSelection() ? '1' : '0') +
			(subFlowButton.getSelection() ? '1' : '0') +
			(actionButton.getSelection() ? '1' : '0') +
			(timerButton.getSelection() ? '1' : '0') +
			(faultButton.getSelection() ? '1' : '0') +
			(eventButton.getSelection() ? '1' : '0') +
			(humanTaskButton.getSelection() ? '1' : '0') +
			(compositeButton.getSelection() ? '1' : '0') +
			(forEachButton.getSelection() ? '1' : '0') +
			(workItemsButton.getSelection() ? '1' : '0');
        store.setValue(IDroolsConstants.FLOW_NODES, flowNodes);
    }

}
