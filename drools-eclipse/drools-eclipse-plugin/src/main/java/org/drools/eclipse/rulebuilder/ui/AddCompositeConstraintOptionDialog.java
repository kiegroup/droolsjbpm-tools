package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.brl.CompositeFieldConstraint;
import org.drools.brms.client.modeldriven.brl.FactPattern;
import org.drools.brms.client.modeldriven.brl.SingleFieldConstraint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class AddCompositeConstraintOptionDialog extends RuleDialog {

    private RuleModeller                   modeller;

    private FactPattern                    pattern;

    private final CompositeFieldConstraint constraint;

    public AddCompositeConstraintOptionDialog(Shell parent,
                                              RuleModeller modeller,
                                              CompositeFieldConstraint constraint,
                                              FactPattern pattern) {
        super( parent,
               "Add fields to this constriant",
               "Pick the value from combo." );

        this.modeller = modeller;
        this.constraint = constraint;
        this.pattern = pattern;
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
        createMultiFieldCombo( composite,
                               gd );

        return composite;
    }

    private void createFieldRestrictionCombo(Composite composite,
                                             GridData gd) {
        createLabel( composite,
                     "Add a restriction on a field" );
        final Combo combo = new Combo( composite,
                                       SWT.READ_ONLY );
        combo.setLayoutData( gd );
        combo.add( "..." );
        String[] fields = this.modeller.getSuggestionCompletionEngine().getFieldCompletions( this.pattern.factType );
        for ( int i = 0; i < fields.length; i++ ) {
            combo.add( fields[i] );
        }
        combo.select( 0 );

        combo.addListener( SWT.Selection,
                           new Listener() {
                               public void handleEvent(Event event) {
                                   if ( combo.getSelectionIndex() == 0 ) {
                                       return;
                                   }

                                   constraint.addConstraint( new SingleFieldConstraint( combo.getText() ) );
                                   modeller.setDirty( true );
                                   modeller.reloadLhs();
                                   close();
                               }
                           } );

    }

    private void createMultiFieldCombo(Composite composite,
                                       GridData gd) {
        createLabel( composite,
                     "Multiple field constraint" );
        final Combo combo = new Combo( composite,
                                       SWT.READ_ONLY );
        combo.setLayoutData( gd );
        combo.add( "..." );
        combo.add( "All of (And)" );
        combo.add( "Any of (Or)" );
        combo.setData( "All of (And)",
                       CompositeFieldConstraint.COMPOSITE_TYPE_AND );
        combo.setData( "Any of (Or)",
                       CompositeFieldConstraint.COMPOSITE_TYPE_OR );
        combo.select( 0 );

        combo.addListener( SWT.Selection,
                           new Listener() {
                               public void handleEvent(Event event) {
                                   if ( combo.getSelectionIndex() == 0 ) {
                                       return;
                                   }
                                   CompositeFieldConstraint comp = new CompositeFieldConstraint();
                                   comp.compositeJunctionType = combo.getText();
                                   constraint.addConstraint( comp );

                                   modeller.reloadLhs();
                                   modeller.setDirty( true );
                                   close();
                               }
                           } );

    }

}
