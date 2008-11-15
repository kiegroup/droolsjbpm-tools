package org.drools.eclipse.rulebuilder.ui;

import org.drools.guvnor.client.modeldriven.brl.ISingleFieldConstraint;
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

    private ISingleFieldConstraint constraint;

    public ValueEditorTypeSelectionDialog(Shell parent,
                                          FormToolkit toolkit,
                                          RuleModeller modeller,
                                          ISingleFieldConstraint constraint) {
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
                        constraint.constraintValueType = ISingleFieldConstraint.TYPE_LITERAL;
                        break;
                    case 1 :
                        constraint.constraintValueType = ISingleFieldConstraint.TYPE_RET_VALUE;
                        break;
                    case 2 :
                        constraint.constraintValueType = ISingleFieldConstraint.TYPE_VARIABLE;
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
