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

import org.drools.ide.common.client.modeldriven.brl.BaseSingleFieldConstraint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ValueEditorTypeSelectionDialog extends RuleDialog {

    private final FormToolkit      toolkit;

    private RuleModeller           modeller;

    private BaseSingleFieldConstraint constraint;

    public ValueEditorTypeSelectionDialog(Shell parent,
                                          FormToolkit toolkit,
                                          RuleModeller modeller,
                                          BaseSingleFieldConstraint constraint) {
        super( parent,
               "Select value editor type",
               "Select value editor type" );
        this.toolkit = toolkit;
        this.modeller = modeller;
        this.constraint = constraint;
    }

    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea( parent );

        createLabel( composite,
                     "Field value:" );
        final Combo valueTypeCombo = new Combo( composite,
                                                SWT.READ_ONLY );
        valueTypeCombo.add( "Literal value" ); // 0
        valueTypeCombo.add( "A formula" ); // 1

        if ( modeller.getModel().getBoundVariablesInScope( constraint ).size() > 0 ) {
            valueTypeCombo.add( "Bound variable" ); // 2
        }

        valueTypeCombo.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {

                switch ( valueTypeCombo.getSelectionIndex() ) {
                    case 0 :
                        constraint.setConstraintValueType(BaseSingleFieldConstraint.TYPE_LITERAL);
                        break;
                    case 1 :
                        constraint.setConstraintValueType(BaseSingleFieldConstraint.TYPE_RET_VALUE);
                        break;
                    case 2 :
                        constraint.setConstraintValueType(BaseSingleFieldConstraint.TYPE_VARIABLE);
                        break;
                }

                getShell().getDisplay().asyncExec( new Runnable() {

                    public void run() {
                        modeller.reloadLhs();
                        modeller.setDirty( true );
                        close();
                    }

                } );
            }
        } );

        toolkit.paintBordersFor( composite );
        return composite;
    }

}
