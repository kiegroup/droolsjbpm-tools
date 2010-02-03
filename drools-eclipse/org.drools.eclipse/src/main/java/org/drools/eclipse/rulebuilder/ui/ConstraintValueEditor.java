package org.drools.eclipse.rulebuilder.ui;

import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.guvnor.client.modeldriven.DropDownData;
import org.drools.guvnor.client.modeldriven.SuggestionCompletionEngine;
import org.drools.guvnor.client.modeldriven.brl.ConnectiveConstraint;
import org.drools.guvnor.client.modeldriven.brl.FactPattern;
import org.drools.guvnor.client.modeldriven.brl.ISingleFieldConstraint;
import org.drools.guvnor.client.modeldriven.brl.SingleFieldConstraint;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class ConstraintValueEditor {

    private Composite              composite;

    private ISingleFieldConstraint constraint;

    private FormToolkit            toolkit;

    private RuleModeller           modeller;

    private boolean                numericValue;

    private FactPattern            pattern;

    public ConstraintValueEditor(Composite composite,
                                 ISingleFieldConstraint constraint,
                                 FormToolkit toolkit,
                                 RuleModeller modeller,
                                 String numericType /* e.g. is "Numeric" */) {
        this( composite,
              constraint,
              toolkit,
              modeller,
              numericType,
              null );
    }

    public ConstraintValueEditor(Composite parent,
                                 ISingleFieldConstraint c,
                                 FormToolkit toolkit,
                                 RuleModeller modeller,
                                 String type,
                                 FactPattern pattern) {
        this.pattern = pattern;
        this.composite = parent;
        this.constraint = c;
        this.toolkit = toolkit;
        this.modeller = modeller;

        if ( SuggestionCompletionEngine.TYPE_NUMERIC.equals( type ) ) {
            this.numericValue = true;
        }
        create();
    }

    private void create() {
        if ( constraint.constraintValueType == ISingleFieldConstraint.TYPE_UNDEFINED ) {
            ImageHyperlink link = addImage( composite,
                                            "icons/edit.gif" );
            link.setToolTipText( "Choose value editor type" );
            link.addHyperlinkListener( new IHyperlinkListener() {
                public void linkActivated(HyperlinkEvent e) {
                    RuleDialog popup = new ValueEditorTypeSelectionDialog( composite.getShell(),
                                                                           toolkit,
                                                                           modeller,
                                                                           constraint );
                    popup.open();
                }

                public void linkEntered(HyperlinkEvent e) {
                }

                public void linkExited(HyperlinkEvent e) {
                }
            } );

            GridData gd = new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING );
            gd.horizontalSpan = 2;

            link.setLayoutData( gd );
        } else {
            switch ( constraint.constraintValueType ) {
                case ISingleFieldConstraint.TYPE_LITERAL :
                    literalValueEditor( composite,
                                        constraint,
                                        new GridData( GridData.FILL_HORIZONTAL ) );
                    break;
                case ISingleFieldConstraint.TYPE_RET_VALUE :
                    addImage( composite,
                              "icons/function_assets.gif" );
                    formulaValueEditor( composite,
                                        constraint,
                                        new GridData( GridData.FILL_HORIZONTAL ) );
                    break;
                case ISingleFieldConstraint.TYPE_VARIABLE :
                    variableEditor( composite,
                                    constraint,
                                    new GridData( GridData.FILL_HORIZONTAL ) );
                    break;
                default :
                    break;
            }
        }

    }

    private void literalValueEditor(Composite parent,
                                    final ISingleFieldConstraint c,
                                    GridData gd) {

        String fieldName = null;
        if (c instanceof SingleFieldConstraint)  {
        	fieldName = ((SingleFieldConstraint) c).fieldName;
        } else if (c instanceof ConnectiveConstraint) {
        	// TODO fieldName = ((ConnectiveConstraint) c).fieldName;
        }
        String fieldType = null;
        if (c instanceof SingleFieldConstraint)  {
        	fieldType = ((SingleFieldConstraint) c).fieldType;
        } else if (c instanceof ConnectiveConstraint) {
        	// TODO fieldType = ((ConnectiveConstraint) c).fieldType;
        }
        DropDownData enums = null;
        boolean found = false;
        if ( fieldType.equals( SuggestionCompletionEngine.TYPE_BOOLEAN )) {
            enums = DropDownData.create(new String[]{"true", "false"});
        }else
        {
            enums = modeller.getSuggestionCompletionEngine().getEnums( pattern,
                                                                       fieldName );
        }
        if ( enums != null && enums.fixedList.length > 0 ) {
            String[] list = enums.fixedList;
            final Combo combo = new Combo( parent,
                                           SWT.DROP_DOWN | SWT.READ_ONLY );
            for ( int i = 0; i < list.length; i++ ) {
                String e = list[i];
                String[] split = null;
                boolean s = false;
                if ( e.indexOf( '=' ) > 0 ) {
                    split = e.split( "=" );
                    e = split[1];
                    combo.add( e.trim() );
                    combo.setData( e.trim(),
                                   split[0].trim() );
                    s = true;
                } else {
                    combo.add( e );
                }
                if ( e.equals( c.value ) || (s && split[0].trim().equals( c.value )) ) {
                    combo.select( i );
                    found = true;
                }
            }
            if ( !found && c.value != null ) {
                combo.add( c.value );
                combo.select( combo.getItemCount() - 1 );
            }

            combo.addModifyListener( new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    String item = combo.getItem( combo.getSelectionIndex() );
                    if ( combo.getData( item ) != null ) {
                        item = (String) combo.getData( item );
                    }
                    c.value = item;
                    modeller.reloadLhs();
                    modeller.setDirty( true );
                }
            } );

            gd.horizontalSpan = 2;
            gd.grabExcessHorizontalSpace = true;
            gd.minimumWidth = 100;
            combo.setLayoutData( gd );

        } else {

            final Text box = toolkit.createText( parent,
                                                 "" );

            if ( c.value != null ) {
                box.setText( c.value );
            }

            gd.horizontalSpan = 2;
            gd.grabExcessHorizontalSpace = true;
            gd.minimumWidth = 100;
            box.setLayoutData( gd );

            box.addModifyListener( new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    c.value = box.getText();
                    modeller.setDirty( true );
                }
            } );

            if ( this.numericValue ) {
                box.addKeyListener( new KeyListener() {

                    public void keyPressed(KeyEvent e) {
                        if ( Character.isLetter( e.character ) ) {
                            e.doit = false;
                        }
                    }

                    public void keyReleased(KeyEvent e) {

                    }

                } );
            }
        }
    }

    private void formulaValueEditor(Composite parent,
                                    final ISingleFieldConstraint c,
                                    GridData gd) {

        final Text box = toolkit.createText( parent,
                                             "" );

        if ( c.value != null ) {
            box.setText( c.value );
        }

        gd.grabExcessHorizontalSpace = true;
        gd.minimumWidth = 100;
        box.setLayoutData( gd );

        box.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                c.value = box.getText();
                modeller.setDirty( true );
            }
        } );
    }

    private void variableEditor(Composite composite,
                                final ISingleFieldConstraint c,
                                GridData gd) {
        List vars = modeller.getModel().getBoundVariablesInScope( c );

        final Combo combo = new Combo( composite,
                                       SWT.READ_ONLY );

        gd.horizontalSpan = 2;
        combo.setLayoutData( gd );
        if ( c.value == null ) {
            combo.add( "Choose ..." );
        }

        int idx = 0;

        for ( int i = 0; i < vars.size(); i++ ) {
            String var = (String) vars.get( i );

            if ( c.value != null && c.value.equals( var ) ) {
                idx = i;
            }
            combo.add( var );
        }

        combo.select( idx );

        combo.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                c.value = combo.getText();
            }
        } );

    }

    public ImageHyperlink addImage(Composite parent,
                                   String fileName) {
        ImageHyperlink imageHyperlink = toolkit.createImageHyperlink( parent,
                                                                      0 );
        ImageDescriptor imageDescriptor = DroolsEclipsePlugin.getImageDescriptor( fileName );
        imageHyperlink.setImage( imageDescriptor.createImage() );
        return imageHyperlink;
    }

}
