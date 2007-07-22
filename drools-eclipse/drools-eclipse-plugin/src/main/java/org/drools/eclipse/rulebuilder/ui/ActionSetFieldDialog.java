package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.brl.ActionFieldValue;
import org.drools.brms.client.modeldriven.brl.ActionSetField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Anton Arhipov
 * @author Ahti Kitsik
 * 
 */
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
