/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.rulebuilder.editors;

import org.drools.eclipse.rulebuilder.ui.RuleModeller;
import org.drools.ide.common.client.modeldriven.brl.RuleModel;
import org.drools.ide.common.server.util.BRXMLPersistence;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Rule Builder main page used as a tab in {@link RuleEditor} multipage.
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
