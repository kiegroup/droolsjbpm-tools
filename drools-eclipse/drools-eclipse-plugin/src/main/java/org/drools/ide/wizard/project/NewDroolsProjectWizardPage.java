package org.drools.ide.wizard.project;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class NewDroolsProjectWizardPage extends WizardPage {

	private Button addSampleJavaRuleCodeButton;
	private Button addSampleRuleButton;
	private Button addSampleJavaDecisionTableCodeButton;
	private Button addSampleDecisionTableCodeButton;
	private boolean addSampleJavaRuleCode = true;
	private boolean addSampleRule = true;
	private boolean addSampleJavaDecisionTableCode = false;
	private boolean addSampleDecisionTableCode = false;
	
	public NewDroolsProjectWizardPage() {
		super("extendedNewProjectPage");
		setTitle("New JBoss Rules Project");
        setDescription("Create a new JBoss Rules Project");
	}
	
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createControls(composite);
        setPageComplete(true);
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
	}
	
	private void createControls(Composite parent) {
		addSampleRuleButton = createCheckBox(parent,
			"Add a sample HelloWorld rule file to this project.");
		addSampleRuleButton.setSelection(addSampleRule);
		addSampleRuleButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				addSampleRule = ((Button) e.widget).getSelection();
			}
		});
		addSampleJavaRuleCodeButton = createCheckBox(parent,
			"Add a sample Java class for loading and executing the HelloWorld rules.");
		addSampleJavaRuleCodeButton.setSelection(addSampleJavaRuleCode);
		addSampleJavaRuleCodeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				addSampleJavaRuleCode = ((Button) e.widget).getSelection();
			}
		});
		addSampleDecisionTableCodeButton = createCheckBox(parent,
			"Add a sample HelloWorld decision table file to this project.");
		addSampleDecisionTableCodeButton.setSelection(addSampleDecisionTableCode);
		addSampleDecisionTableCodeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				addSampleDecisionTableCode = ((Button) e.widget).getSelection();
			}
		});
		addSampleJavaDecisionTableCodeButton = createCheckBox(parent,
			"Add a sample Java class for loading and executing the HelloWorld decision table.");
		addSampleDecisionTableCodeButton.setSelection(addSampleDecisionTableCode);
		addSampleJavaDecisionTableCodeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				addSampleJavaDecisionTableCode = ((Button) e.widget).getSelection();
			}
		});
	}

	private Button createCheckBox(Composite group, String label) {
        Button button = new Button(group, SWT.CHECK | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        button.setLayoutData(data);
        return button;
    }
	
	public boolean createRuleFile() {
		return addSampleRule;
	}
	
	public boolean createJavaRuleFile() {
		return addSampleJavaRuleCode;
	}
	
	public boolean createDecisionTableFile() {
		return addSampleDecisionTableCode;
	}
	
	public boolean createJavaDecisionTableFile() {
		return addSampleJavaDecisionTableCode;
	}
}
