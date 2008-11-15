package org.drools.eclipse.flow.common.editor.editpart.work;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.process.core.ParameterDefinition;
import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.WorkEditor;
import org.drools.process.core.impl.WorkImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Custom Work editor that can handle work definitions that only have
 * String parameters.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class SampleCustomEditor extends EditBeanDialog implements WorkEditor {

    private WorkDefinition workDefinition;
    private Map<String, Text> texts = new HashMap<String, Text>(); 
    
    public SampleCustomEditor(Shell parentShell) {
        super(parentShell, "Custom Work Editor");
        setBlockOnOpen(true);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        Work work = (Work) getValue();
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name: ");
        Text nameText = new Text(composite, SWT.NONE);
        nameText.setEditable(false);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        nameText.setLayoutData(gridData);
        String name = work.getName();
        nameText.setText(name == null ? "" : name);
        
        Set<ParameterDefinition> parameters = workDefinition.getParameters();
        for (ParameterDefinition param: parameters) {
            Label label = new Label(composite, SWT.NONE);
            label.setText(param.getName() + ": ");
            Text text = new Text(composite, SWT.NONE);
            gridData = new GridData();
            gridData.grabExcessHorizontalSpace = true;
            gridData.horizontalAlignment = GridData.FILL;
            text.setLayoutData(gridData);
            texts.put(param.getName(), text);
            Object value = work.getParameter(param.getName());
            text.setText(value == null ? "" : value.toString());
        }
        
        return composite;
    }
    
    protected Object updateValue(Object value) {
        Work work = new WorkImpl();
        work.setName(((Work) value).getName());
        for (Map.Entry<String, Text> entry: texts.entrySet()) {
            String text = entry.getValue().getText();
            work.setParameter(entry.getKey(), "".equals(text) ? null : text);
        }
        work.setParameterDefinitions(((Work) value).getParameterDefinitions());
        return work;
    }
        
    public Work getWork() {
        return (Work) getValue();
    }

    public void setWork(Work work) {
        setValue(work);
    }

    public void setWorkDefinition(WorkDefinition workDefinition) {
        this.workDefinition = workDefinition;
    }

    public boolean show() {
        int result = open();
        return result == OK;
    }

}
