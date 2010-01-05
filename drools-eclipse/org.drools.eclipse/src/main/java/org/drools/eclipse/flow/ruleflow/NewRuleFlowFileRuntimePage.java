package org.drools.eclipse.flow.ruleflow;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NewRuleFlowFileRuntimePage extends WizardPage {

	public static final String DROOLS5 = "Drools 5.0.x";
    public static final String DROOLS5_1 = "Drools 5.1.x";
	
	private String generationType = DROOLS5;
	private Combo droolsGenerateCombo;
	
	public NewRuleFlowFileRuntimePage() {
		super("extendedNewProjectRuntimePage");
		setTitle("Drools Runtime");
        setDescription("Select a Drools Runtime");
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        Label generateLabel = new Label(composite, SWT.NONE);
        generateLabel.setText("Generate code compatible with: ");
        droolsGenerateCombo = new Combo(composite, SWT.READ_ONLY);
        droolsGenerateCombo.add("Drools 5.0.x");
        droolsGenerateCombo.add("Drools 5.1.x");
        droolsGenerateCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				generationType = droolsGenerateCombo.getText();
			}
			public void widgetSelected(SelectionEvent e) {
				generationType = droolsGenerateCombo.getText();
			}
        });
        droolsGenerateCombo.select(0);
        generationType = DROOLS5;
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        droolsGenerateCombo.setLayoutData(gridData);
        
        setMessage(null);
        setPageComplete(true);
        setControl(composite);
	}
	
	public String getGenerationType() {
		return generationType;
	}
	
}
