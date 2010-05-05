package org.drools.eclipse.rulebuilder.ui;

import org.drools.eclipse.rulebuilder.modeldriven.HumanReadable;
import org.drools.ide.common.client.modeldriven.DropDownData;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.ActionFieldValue;
import org.drools.ide.common.client.modeldriven.brl.ActionInsertFact;
import org.drools.ide.common.client.modeldriven.brl.ActionInsertLogicalFact;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * 
 * 
 * @author Anton Arhipov
 * @author Ahti Kitsik
 * 
 */
public class ActionInsertFactWidget extends Widget {

    private final ActionInsertFact fact;

    public ActionInsertFactWidget(FormToolkit toolkit,
                                  Composite parent,
                                  RuleModeller mod,
                                  ActionInsertFact fact,
                                  int index) {
        super( parent,
               toolkit,
               mod,
               index );

        this.fact = fact;

        GridLayout l = new GridLayout();
        l.numColumns = 4;
        l.marginBottom = 0;
        l.marginHeight = 0;
        l.marginLeft = 0;
        l.marginRight = 0;
        l.marginTop = 0;
        l.marginWidth = 0;
        l.verticalSpacing = 0;
        parent.setLayout( l );

        create();
    }

    private void create() {

        String assertType = "assert";
        if ( fact instanceof ActionInsertLogicalFact ) {
            assertType = "assertLogical";
        }

        toolkit.createLabel( parent,
                             HumanReadable.getActionDisplayName( assertType ) + " " + this.fact.factType );
        addDeleteRHSAction();
        addMoreOptionsAction();
        Composite constraintComposite = toolkit.createComposite( parent );
        GridLayout constraintLayout = new GridLayout();
        constraintLayout.numColumns = 3;
        constraintComposite.setLayout( constraintLayout );
        createConstraintRows( constraintComposite );
        toolkit.paintBordersFor( constraintComposite );
    }

    private void addMoreOptionsAction() {
        final Shell shell = new Shell( Display.getCurrent() );
        ImageHyperlink link = addImage( parent,
                                        "icons/new_item.gif" );

        link.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                RuleDialog popup = new AddNewInsertedFactFieldDialog( shell,
                                                                      getModeller(),
                                                                      fact );
                popup.open();
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        link.setToolTipText( "Add another field to this so you can set its value" );
    }

    private void createConstraintRows(Composite constraintComposite) {
        for ( int row = 0; row < fact.fieldValues.length; row++ ) {
            ActionFieldValue val = fact.fieldValues[row];
            toolkit.createLabel( constraintComposite,
                                 val.field );
            valueEditor( constraintComposite,
                         val );
            addRemoveFieldAction( constraintComposite,
                                  row );
        }
    }

    private void addRemoveFieldAction(Composite constraintComposite,
                                      final int row) {
        ImageHyperlink delLink = addImage( constraintComposite,
                                           "icons/delete_item_small.gif" );
        delLink.setToolTipText( "Remove this field action" );

        delLink.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this item?" );
                dialog.setText( "Remove this item?" );
                if ( dialog.open() == SWT.YES ) {
                    fact.removeField( row );
                    getModeller().setDirty( true );
                    getModeller().reloadRhs();
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
    }

    private void valueEditor(Composite parent,
                             final ActionFieldValue val) {
        String fieldName = val.field;
        DropDownData enums = modeller.getSuggestionCompletionEngine().getEnums( fact.factType,
                                                                                fact.fieldValues,
                                                                                val.field );
        boolean found = false;
        if ( enums != null && enums.fixedList.length > 0 ) {
            String[] list = enums.fixedList;
            final Combo combo = new Combo( parent,
                                           SWT.DROP_DOWN | SWT.READ_ONLY );
            for ( int i = 0; i < list.length; i++ ) {
                String e = list[i];
                combo.add( e );
                if ( e.equals( val.value ) ) {
                    combo.select( i );
                    found = true;
                }
            }
            if ( !found && val.value != null ) {
                combo.add( val.value );
                combo.select( combo.getItemCount() - 1 );
            }

            combo.addModifyListener( new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    val.value = combo.getItem( combo.getSelectionIndex() );
                    modeller.reloadRhs();
                    modeller.setDirty( true );
                }
            } );

        } else {

            final Text box = toolkit.createText( parent,
                                                 "" );

            if ( val.value != null ) {
                box.setText( val.value );
            }

            GridData gd = new GridData( GridData.FILL_HORIZONTAL );
            gd.grabExcessHorizontalSpace = true;
            gd.minimumWidth = 100;
            box.setLayoutData( gd );

            box.addModifyListener( new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    getModeller().setDirty( true );
                    val.value = box.getText();
                }
            } );

            if ( val.type.equals( SuggestionCompletionEngine.TYPE_NUMERIC ) ) {
                new NumericKeyFilter( box );
            }
        }

    }

    public SuggestionCompletionEngine getCompletion() {
        return getModeller().getSuggestionCompletionEngine();
    }

}
