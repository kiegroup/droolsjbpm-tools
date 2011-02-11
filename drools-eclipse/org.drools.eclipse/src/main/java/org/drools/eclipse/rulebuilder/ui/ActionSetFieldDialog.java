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

package org.drools.eclipse.rulebuilder.ui;

import org.drools.ide.common.client.modeldriven.brl.ActionFieldValue;
import org.drools.ide.common.client.modeldriven.brl.ActionSetField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ActionSetFieldDialog extends RuleDialog {

    private String[]       fieldCompletions;

    private RuleModeller   modeller;

    private ActionSetField field;

    private String         variableClass;

    public ActionSetFieldDialog(Shell parent,
                                RuleModeller modeller,
                                ActionSetField field,
                                String[] fieldCompletions,
                                String variableClass) {
        super( parent,
               "Add a field",
               "Add a field" );

        this.fieldCompletions = fieldCompletions;
        this.modeller = modeller;
        this.field = field;
        this.variableClass = variableClass;
    }

    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea( parent );
        createLabel( composite,
                     "Add a field" );

        final Combo fieldsCombo = new Combo( parent,
                                             SWT.READ_ONLY );
        fieldsCombo.add( "Choose field..." );
        for ( int i = 0; i < fieldCompletions.length; i++ ) {
            fieldsCombo.add( fieldCompletions[i] );
        }
        fieldsCombo.select( 0 );

        fieldsCombo.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if ( fieldsCombo.getSelectionIndex() == 0 ) {
                    return; // no need to change anything
                }

                String fieldType = modeller.getSuggestionCompletionEngine().getFieldType( variableClass,
                                                                                          fieldsCombo.getText() );
                field.addFieldValue( new ActionFieldValue( fieldsCombo.getText(),
                                                           "",
                                                           fieldType ) );

                
                getShell().getDisplay().asyncExec( new Runnable() {

                    public void run() {
                        modeller.reloadRhs();
                        modeller.setDirty( true );
                        close();
                    }

                } );
            }
        } );

        return composite;
    }

}
