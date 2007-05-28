package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.client.modeldriven.brxml.ActionFieldValue;
import org.drools.brms.client.modeldriven.brxml.ActionSetField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Anton Arhipov
 * @author Ahti Kitsik
 *
 */
public class ActionSetFieldDialog extends RuleDialog {

    private String[]       fieldCompletions;
    private FormToolkit    toolkit;
    private RuleModeller   modeller;
    private ActionSetField field;

    public ActionSetFieldDialog(FormToolkit toolkit,
                                Shell parent,
                                RuleModeller modeller,
                                ActionSetField field,
                                String[] fieldCompletions) {
        super( parent,
               "Add a field",
               "Add a field" );

        this.fieldCompletions = fieldCompletions;
        this.toolkit = toolkit;
        this.modeller = modeller;
        this.field = field;
    }

    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea( parent );
        toolkit.createLabel( composite,
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

                //TODO Is Number correct?
                field.addFieldValue( new ActionFieldValue( fieldsCombo.getText(),
                                                           "",
                                                           SuggestionCompletionEngine.TYPE_NUMERIC ) );

                modeller.reloadRhs();
                modeller.setDirty( true );
                close();
            }
        } );

        return composite;
    }

}
