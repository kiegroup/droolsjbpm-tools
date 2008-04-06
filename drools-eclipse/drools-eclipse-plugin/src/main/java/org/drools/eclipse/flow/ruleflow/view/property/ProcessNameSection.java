package org.drools.eclipse.flow.ruleflow.view.property;

import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class ProcessNameSection extends AbstractPropertySection implements ModifyListener {

    private Text nameText;
    private ProcessWrapper processWrapper;

    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);
        Composite composite = getWidgetFactory().createFlatFormComposite(parent);

        nameText = getWidgetFactory().createText(composite, "");
        FormData data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        nameText.setLayoutData(data);
        nameText.addModifyListener(this);

        CLabel labelLabel = getWidgetFactory().createCLabel(composite, "Name:");
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(nameText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(nameText, 0, SWT.CENTER);
        labelLabel.setLayoutData(data);

    }
    
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        if (selection instanceof IStructuredSelection) {
            Object input = ((IStructuredSelection) selection).getFirstElement();
            if (input instanceof ProcessEditPart) {
                input = ((ProcessEditPart) input).getModel();
            }
            if (input instanceof ProcessWrapper) {
                this.processWrapper = (ProcessWrapper) input;
            }
        }
    }
    
    public void refresh() {
        if (processWrapper != null) {
            nameText.removeModifyListener(this);
            nameText.setText(processWrapper.getName());
            nameText.addModifyListener(this);
        }
    }


    public void modifyText(ModifyEvent arg0) {
        if (processWrapper != null) {
            processWrapper.setName(nameText.getText());
        }
    }

}
