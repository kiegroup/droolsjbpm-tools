package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.client.modeldriven.brxml.FactPattern;
import org.eclipse.swt.SWT;
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

/**
 * Adding a new constraint for a field of a FactPattern
 * 
 * @author Anton Arhipov
 * @author Ahti Kitsik
 * 
 */
public class AddNewFieldConstraintDialog extends RuleDialog {

    private final FormToolkit toolkit;

    private RuleModeller      modeller;

    private FactPattern       pattern;

    private boolean           isNested;

    public AddNewFieldConstraintDialog(Shell parent,
                                       FormToolkit toolkit,
                                       RuleModeller modeller,
                                       FactPattern pattern,
                                       boolean isNested) {
        super( parent,
               "Update constraints",
               "Pick the values from combos and confirm the selection." );
        this.toolkit = toolkit;
        this.modeller = modeller;
        this.pattern = pattern;
        this.isNested = isNested;
    }

    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea( parent );

        GridLayout l = new GridLayout();
        l.numColumns = 3;
        l.marginBottom = 0;
        l.marginHeight = 0;
        l.marginLeft = 0;
        l.marginRight = 0;
        l.marginTop = 0;
        l.marginWidth = 0;
        composite.setLayout( l );

        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;

        createFieldRestrictionCombo( composite,
                                     gd );
        createFormulaRow( composite,
                          gd );
        if ( !isNested ) {
            createVariableBindingRow( composite );
        }

        toolkit.paintBordersFor( composite );
        return composite;
    }

    private void createVariableBindingRow(Composite composite) {
        toolkit.createLabel( composite,
                             "Variable name" );
        final Text variableText = toolkit.createText( composite,
                                                      "" );

        if ( pattern.boundName != null ) {
            variableText.setText( pattern.boundName );
        }

        Button varButton = toolkit.createButton( composite,
                                                 "Set",
                                                 SWT.PUSH );
        varButton.addListener( SWT.Selection,
                               new Listener() {
                                   public void handleEvent(Event event) {                                                                              
                                       pattern.boundName = variableText.getText();
                                       modeller.reloadLhs(); 
                                       modeller.setDirty( true );
                                       close();
                                   }
                               } );
    }

    private void createFormulaRow(Composite composite,
                                  GridData gd) {
        toolkit.createLabel( composite,
                             "Add a new formula style expression" );
        Button formulaButton = toolkit.createButton( composite,
                                                     "New formula",
                                                     SWT.PUSH );

        //TODO: adjust to new API
       /* formulaButton.addListener( SWT.Selection,
                                   new Listener() {
                                       public void handleEvent(Event event) {
                                           Constraint con = new Constraint();
                                           con.constraintValueType = IConstraint.TYPE_PREDICATE;
                                           pattern.addConstraint( con );
                                           modeller.setDirty( true );
                                           modeller.reloadLhs();
                                           close();
                                       }
                                   } );*/

        formulaButton.setLayoutData( gd );
    }

    private void createFieldRestrictionCombo(Composite composite,
                                             GridData gd) {
        toolkit.createLabel( composite,
                             "Add a restriction on a field" );
        String[] fieldCompletitions = getCompletion().getFieldCompletions( pattern.factType );
        final Combo fieldsCombo = new Combo( composite,
                                             SWT.READ_ONLY );
        fieldsCombo.setLayoutData( gd );
        fieldsCombo.add( "..." );
        for ( int i = 0; i < fieldCompletitions.length; i++ ) {
            fieldsCombo.add( fieldCompletitions[i] );
        }
        fieldsCombo.select( 0 );

        
        //TODO: adjust to new API
       /* fieldsCombo.addListener( SWT.Selection,
                                 new Listener() {
                                     public void handleEvent(Event event) {
                                         if ( fieldsCombo.getSelectionIndex() == 0 ) {
                                             return;
                                         }

                                         Constraint constraint = new Constraint();
                                         constraint.fieldName = fieldsCombo.getText();
                                         pattern.addConstraint( constraint );
                                         modeller.setDirty( true );
                                         modeller.reloadLhs();
                                         close();
                                     }
                                 } );*/
    }

    private SuggestionCompletionEngine getCompletion() {
        return modeller.getSuggestionCompletionEngine();
    }

}
