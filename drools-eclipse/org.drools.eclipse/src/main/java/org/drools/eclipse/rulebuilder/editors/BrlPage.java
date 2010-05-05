package org.drools.eclipse.rulebuilder.editors;

import org.drools.ide.common.client.modeldriven.brl.RuleModel;
import org.drools.ide.common.server.util.BRXMLPersistence;
import org.drools.eclipse.rulebuilder.ui.RuleModeller;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Rule Builder main page used as a tab in {@link RuleEditor} multipage.
 * 
 * @author Ahti Kitsik
 *
 */
public class BrlPage extends FormPage {

    private static final String PAGE_NAME = "Rule Builder";
    private static final String PAGE_ID   = BrlPage.class.getName();

    private RuleModeller        modeller;
    private RuleModel           model;
    private RuleEditor          editor;

    public BrlPage(RuleEditor editor) {
        super( editor,
               PAGE_ID,
               PAGE_NAME );
        this.editor = editor;
    }

    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();

        modeller = new RuleModeller( form,
                                     toolkit,
                                     model,
                                     editor );
    }

    public void setModelXML(String xml) {
        model = BRXMLPersistence.getInstance().unmarshal( xml );
        modeller.setModel( model );
        modeller.reloadWidgets();
    }

    public RuleModel getRuleModel() {
        return model;
    }

    public RuleModeller getModeller() {
        return modeller;
    }

    public boolean isDirty() {
        return modeller.isDirty();
    }

    public void fireDirtyPropertyChanged() {
        editor.dirtyPropertyChanged();
    }

    public void refresh() {
        modeller.refresh();
    }

}
