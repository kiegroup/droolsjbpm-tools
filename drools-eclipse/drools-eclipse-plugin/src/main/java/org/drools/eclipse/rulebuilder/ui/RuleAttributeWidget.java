package org.drools.eclipse.rulebuilder.ui;

import java.util.ArrayList;
import java.util.List;

import org.drools.guvnor.client.modeldriven.brl.RuleAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class RuleAttributeWidget extends Widget {

    public RuleAttributeWidget(FormToolkit toolkit,
                               Composite parent,
                               RuleModeller modeller) {
        super( parent,
               toolkit,
               modeller,
               0 );

        GridLayout l = new GridLayout();
        l.numColumns = 3;
        // l.marginBottom = 5;
        // l.marginHeight = 5;
        // l.marginLeft = 5;
        // l.marginRight = 5;
        // l.marginTop = 10;
        // l.marginWidth = 10;
        // l.verticalSpacing = 15;
        parent.setLayout( l );

        create();
    }

    private void create() {

        RuleAttribute[] attrs = modeller.getModel().attributes;
        for ( int i = 0; i < attrs.length; i++ ) {
            RuleAttribute at = attrs[i];
            addAttribute( at );
        }
        toolkit.paintBordersFor( parent );
    }

    private void addAttribute(RuleAttribute at) {
        toolkit.createLabel( parent,
                             at.attributeName );

        if ( at.attributeName.equals( "no-loop" ) ) {
            toolkit.createLabel( parent,
                                 "" );
        } else if ( at.attributeName.equals( "enabled" ) || at.attributeName.equals( "auto-focus" ) || at.attributeName.equals( "lock-on-active" ) ) {
            createCheckbox( at );
        } else {
            createText( at );
        }

        addDeleteLink( at );

    }

    private void createText(final RuleAttribute at) {
        final Text box = toolkit.createText( parent,
                                             "" );

        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.grabExcessHorizontalSpace = true;
        gd.minimumWidth = 100;
        box.setLayoutData( gd );
        
        box.setText( at.value );

        box.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                at.value = box.getText();
                modeller.setDirty( true );
            }
        } );

        if ( at.attributeName.equals( "date-effective" ) || at.attributeName.equals( "date-expires" ) ) {
            if ( at.value == null || "".equals( at.value.trim() ) ) {
                box.setText( "dd-MMM-yyyy" );
            }
        }

    }

    private void createCheckbox(final RuleAttribute at) {
        final Button checkbox = toolkit.createButton( parent,
                                                      "",
                                                      SWT.CHECK );

        if ( at.value == null ) {
            checkbox.setSelection( true );
            at.value = "true";
        } else {
            checkbox.setSelection( at.value.equals( "true" ) ? true : false );
        }

        checkbox.addSelectionListener( new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                at.value = (checkbox.getSelection()) ? "true" : "false";
                modeller.setDirty( true );
            }

        } );

    }

    private void addDeleteLink(final RuleAttribute at) {
        ImageHyperlink delLink = addImage( parent,
                                           "icons/delete_item_small.gif" );
        delLink.setToolTipText( "Remove this fieldconstraint" );
        delLink.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this rule option?" );
                dialog.setText( "Remove this rule option?" );
                if ( dialog.open() == SWT.YES ) {
                    RuleAttribute[] attrs = modeller.getModel().attributes;
                    for ( int i = 0; i < attrs.length; i++ ) {
                        if ( attrs[i] == at ) {
                            modeller.getModel().removeAttribute( i );

                            modeller.setDirty( true );
                            modeller.reloadOptions();
                        }
                    }
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );

    }

    /**
     * Return a listbox of choices for rule attributes.
     * 
     * @return
     */
    public static List getAttributeList() {
        List list = new ArrayList();
        list.add( "..." );

        list.add( "salience" );
        list.add( "enabled" );
        list.add( "date-effective" );
        list.add( "date-expires" );
        list.add( "no-loop" );
        list.add( "agenda-group" );
        list.add( "activation-group" );
        list.add( "duration" );
        list.add( "auto-focus" );
        list.add( "lock-on-active" );
        list.add( "ruleflow-group" );
        list.add( "dialect" );

        return list;
    }

}
