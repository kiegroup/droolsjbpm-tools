package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.brl.CompositeFactPattern;
import org.drools.brms.client.modeldriven.brl.FactPattern;
import org.drools.eclipse.rulebuilder.modeldriven.HumanReadable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * This is the new smart widget that works off the model.
 * 
 * @author Anton Arhipov
 * @author Ahti Kitsik
 * 
 */
public class CompositeFactPatternWidget extends Widget {

    private final CompositeFactPattern pattern;

    public CompositeFactPatternWidget(FormToolkit toolkit,
                                      Composite parent,
                                      RuleModeller mod,
                                      CompositeFactPattern factPattern,
                                      int idx) {

        super( parent,
               toolkit,
               mod,
               idx );
        this.pattern = factPattern;

        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.marginBottom = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 2;
        parent.setLayout( layout );

        create();
    }

    private void create() {
        Label l = toolkit.createLabel( parent,
                             HumanReadable.getCEDisplayName( pattern.type ) );
        
        GridData labelGD = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
        labelGD.horizontalSpan = 2;
        //labelGD.verticalAlignment = SWT.CENTER;
        //labelGD.horizontalAlignment = SWT.CENTER;
        l.setLayoutData(labelGD);
        l.setBackground(new Color(parent.getShell().getDisplay(),240,240,240));
        
        l.setLayoutData(labelGD);
        addDeleteAction();
        addMoreOptionsAction();
        Composite composite = toolkit.createComposite( parent );
        

    	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = SWT.CENTER;
    	composite.setLayoutData(gd);
    	
        createFactRows( toolkit,
                        composite );
    }

    protected void addDeleteAction() {
        ImageHyperlink delWholeLink = addImage( parent,
                                                "icons/delete_obj.gif" );
        delWholeLink.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this ENTIRE condition, " + "and all the field constraints that belong to it." );
                dialog.setText( "Remove this entire condition?" );
                if ( dialog.open() == SWT.YES ) {

                    if ( getModeller().getModel().removeLhsItem( index ) ) {
                        getModeller().reloadLhs();
                        getModeller().setDirty( true );
                    } else {
                        showMessage( "Can't remove that item as it is used in the action part of the rule." );
                    }
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        delWholeLink.setToolTipText( "Remove the entire composite condition." );
    }

    private void addMoreOptionsAction() {
        final ImageHyperlink link = addImage( parent,
                                              // "icons/add_field_to_fact.gif");
                                              "icons/new_item.gif" );

        link.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                RuleDialog popup = new AddNewFactConstraintDialog( parent.getShell(),
                                                                   getModeller(),
                                                                   pattern );
                popup.open();
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        link.setToolTipText( "Add a fact to this constraint. " + "If it is an 'or' type, it will need at least 2." );
    }

    private void createFactRows(FormToolkit toolkit,
                                Composite constraintComposite) {
    	
    	
        if ( pattern.patterns != null ) {
            FactPattern[] facts = pattern.patterns;
            for ( int i = 0; i < facts.length; i++ ) {
            	
                new FactPatternWidget( toolkit,
                                       constraintComposite,
                                       getModeller(),
                                       facts[i],
                                       pattern,
                                       i,
                                       false );
                
                toolkit.createLabel(constraintComposite, "");
                toolkit.createLabel(constraintComposite, "");
            }
        }
    }

}
