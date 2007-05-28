package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.client.modeldriven.brxml.CompositeFactPattern;
import org.drools.brms.client.modeldriven.brxml.FactPattern;
import org.drools.brms.client.modeldriven.brxml.IPattern;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This provides a popup for new LHS condition selection. (add new if-condition)
 * 
 * @author Anton Arhipov
 * @author Ahti Kitsik
 */
public class AddNewConditionDialog extends RuleDialog {

    private final FormToolkit toolkit;

    private IPattern          pattern;

    private RuleModeller      modeller;

    public AddNewConditionDialog(Shell parent,
                                 FormToolkit toolkit,
                                 RuleModeller modeller) {

        super( parent,
               "Add new condition to the rule",
               "Pick the values from combos and confirm the selection." );

        this.toolkit = toolkit;
        this.modeller = modeller;
    }

    protected Control createDialogArea(final Composite parent) {
        Control dialog = super.createDialogArea( parent );

        Composite composite = (Composite) dialog;

        toolkit.createLabel( composite,
                             "Fact" );

        String[] factTypes = getCompletion().getFactTypes();
        final Combo factsCombo = new Combo( composite,
                                            SWT.READ_ONLY );
        factsCombo.add( "Choose fact type..." );
        for ( int i = 0; i < factTypes.length; i++ ) {
            factsCombo.add( factTypes[i] );
        }
        factsCombo.select( 0 );

        dialog.addDisposeListener( new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                System.out.println( "DISPOSED " + e );
                Thread.dumpStack();
            }

        } );

        factsCombo.addListener( SWT.Selection,
                                new Listener() {
                                    public void handleEvent(Event event) {

                                        System.out.println( "HERE6666! event " + event );

                                        if ( factsCombo.getSelectionIndex() == 0 ) {
                                            return;
                                        }
                                        modeller.getModel().addLhsItem( new FactPattern( factsCombo.getText() ) );
                                        modeller.reloadLhs();
                                        modeller.setDirty( true );
                                        close();
                                    }
                                } );

        toolkit.createLabel( composite,
                             "Condition type" );

        final Combo conditionalsCombo = new Combo( composite,
                                                   SWT.READ_ONLY );
        String[] conditionalElements = getCompletion().getConditionalElements();
        conditionalsCombo.add( "Choose condition type..." );
        for ( int i = 0; i < conditionalElements.length; i++ ) {
            conditionalsCombo.add( conditionalElements[i] );
        }
        conditionalsCombo.select( 0 );

        conditionalsCombo.addListener( SWT.Selection,
                                       new Listener() {
                                           public void handleEvent(Event event) {
                                               if ( conditionalsCombo.getSelectionIndex() == 0 ) {
                                                   return;
                                               }

                                               modeller.getModel().addLhsItem( new CompositeFactPattern( conditionalsCombo.getText() ) );
                                               modeller.reloadLhs();
                                               modeller.setDirty( true );
                                               close();
                                           }
                                       } );

        //setBlockOnOpen( true );

        return composite;
    }

    public IPattern getPattern() {
        return pattern;
    }

    private SuggestionCompletionEngine getCompletion() {
        return modeller.getSuggestionCompletionEngine();
    }

}
