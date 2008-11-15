package org.drools.eclipse.flow.ruleflow.view.property;

import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
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

public class ElementNameSection extends AbstractPropertySection implements ModifyListener {

    private Text nameText;
    private ElementWrapper elementWrapper;

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
            if (input instanceof ElementEditPart) {
                input = ((ElementEditPart) input).getModel();
            }
            if (input instanceof ElementWrapper) {
                this.elementWrapper = (ElementWrapper) input;
            }
        }
    }
    
    public void refresh() {
        if (elementWrapper != null) {
            nameText.removeModifyListener(this);
            nameText.setText(elementWrapper.getName());
            nameText.addModifyListener(this);
        }
    }


    public void modifyText(ModifyEvent arg0) {
        if (elementWrapper != null) {
            elementWrapper.setName(nameText.getText());
        }
    }

}
