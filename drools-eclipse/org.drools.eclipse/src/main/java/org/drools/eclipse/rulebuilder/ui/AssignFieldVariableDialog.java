package org.drools.eclipse.rulebuilder.ui;

import org.drools.guvnor.client.modeldriven.SuggestionCompletionEngine;
import org.drools.guvnor.client.modeldriven.brl.FactPattern;
import org.drools.guvnor.client.modeldriven.brl.SingleFieldConstraint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class AssignFieldVariableDialog extends RuleDialog {

    private final FormToolkit     toolkit;

    private RuleModeller          modeller;

    private SingleFieldConstraint con;

    private final FactPattern     pattern;

    public AssignFieldVariableDialog(Shell parent,
                                     FormToolkit toolkit,
                                     RuleModeller modeller,
                                     SingleFieldConstraint con,
                                     FactPattern pattern) {
        super( parent,
               "Bind the field called [" + con.fieldName + "] to a variable.",
               "Type the variable name and hit the button." );

        this.toolkit = toolkit;
        this.modeller = modeller;
        this.con = con;
        this.pattern = pattern;
    }

    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea( parent );

        GridLayout l = new GridLayout();
        l.numColumns = 5;
        l.marginBottom = 0;
        l.marginHeight = 0;
        l.marginLeft = 0;
        l.marginRight = 0;
        l.marginTop = 0;
        l.marginWidth = 0;
        composite.setLayout( l );

        createVariableBindingRow( composite );
        createSubfieldConstraintRow( composite );
        toolkit.paintBordersFor( composite );
        return composite;
    }

    private void createSubfieldConstraintRow(final Composite composite) {
        SuggestionCompletionEngine engine = modeller.getSuggestionCompletionEngine();
        String[] fields = engine.getFieldCompletions( con.fieldType );
        if ( fields == null || fields.length <= 0 ) {
            return;
        }
        createLabel( composite,
                     "Add a restriction on a subfield" );
        final Combo combo = new Combo( composite,
                                       SWT.DROP_DOWN | SWT.READ_ONLY );
        combo.add( "..." );
        for ( int i = 0; i < fields.length; i++ ) {
            String field = fields[i];
            combo.add( field );
            combo.setData( field,
                           engine.getFieldType( con.fieldType + "." + field ) );
        }
        combo.select( 0 );

        combo.addModifyListener( new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String fName = combo.getText();

                if ( combo.getSelectionIndex() == 0 ) {
                    return;
                }

                pattern.addConstraint( new SingleFieldConstraint( fName,
                                                                  ((String) combo.getData( fName )),
                                                                  con ) );
                modeller.setDirty( true );
                modeller.reloadLhs();
                close();
            }
        } );
    }

    private void createVariableBindingRow(Composite composite) {
        createLabel( composite,
                     "Variable name" );
        final Text variableText = toolkit.createText( composite,
                                                      "" );

        if ( con.fieldBinding != null ) {
            variableText.setText( con.fieldBinding );
        }

        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 3;

        variableText.setLayoutData( gd );

        Button varButton = toolkit.createButton( composite,
                                                 "Set",
                                                 SWT.PUSH );
        varButton.addListener( SWT.Selection,
                               new Listener() {
                                   public void handleEvent(Event event) {
                                       con.fieldBinding = variableText.getText();
                                       modeller.reloadLhs();
                                       modeller.setDirty( true );
                                       close();
                                   }
                               } );
    }

}
