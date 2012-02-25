/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.rulebuilder.ui;

import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.ide.common.client.modeldriven.DropDownData;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.BaseSingleFieldConstraint;
import org.drools.ide.common.client.modeldriven.brl.ConnectiveConstraint;
import org.drools.ide.common.client.modeldriven.brl.FactPattern;
import org.drools.ide.common.client.modeldriven.brl.SingleFieldConstraint;
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

    private BaseSingleFieldConstraint constraint;

    private FormToolkit            toolkit;

    private RuleModeller           modeller;

    private boolean                numericValue;

    private FactPattern            pattern;

    public ConstraintValueEditor(Composite composite,
                                 BaseSingleFieldConstraint constraint,
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
                                 BaseSingleFieldConstraint c,
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
        if ( constraint.getConstraintValueType() == BaseSingleFieldConstraint.TYPE_UNDEFINED ) {
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
            switch ( constraint.getConstraintValueType() ) {
                case BaseSingleFieldConstraint.TYPE_LITERAL :
                    literalValueEditor( composite,
                                        constraint,
                                        new GridData( GridData.FILL_HORIZONTAL ) );
                    break;
                case BaseSingleFieldConstraint.TYPE_RET_VALUE :
                    addImage( composite,
                              "icons/function_assets.gif" );
                    formulaValueEditor( composite,
                                        constraint,
                                        new GridData( GridData.FILL_HORIZONTAL ) );
                    break;
                case BaseSingleFieldConstraint.TYPE_VARIABLE :
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
                                    final BaseSingleFieldConstraint c,
                                    GridData gd) {

        String fieldName = null;
        if (c instanceof SingleFieldConstraint)  {
            fieldName = ((SingleFieldConstraint) c).getFieldName();
        } else if (c instanceof ConnectiveConstraint) {
            fieldName = ((ConnectiveConstraint) c).getFieldName();
        }
        String fieldType = null;
        if (c instanceof SingleFieldConstraint)  {
            fieldType = ((SingleFieldConstraint) c).getFieldType();
        } else if (c instanceof ConnectiveConstraint) {
            fieldType = ((ConnectiveConstraint) c).getFieldType();
        }
        DropDownData enums = null;
        boolean found = false;
        if ( fieldType.equals( SuggestionCompletionEngine.TYPE_BOOLEAN )) {
            enums = DropDownData.create(new String[]{"true", "false"});
        }else
        {
            enums = modeller.getSuggestionCompletionEngine().getEnums( pattern.getFactType(),
                                                                       pattern.constraintList,
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
                if ( e.equals( c.getValue() ) || (s && split[0].trim().equals( c.getValue() )) ) {
                    combo.select( i );
                    found = true;
                }
            }
            if ( !found && c.getValue() != null ) {
                combo.add( c.getValue() );
                combo.select( combo.getItemCount() - 1 );
            }

            combo.addModifyListener( new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    String item = combo.getItem( combo.getSelectionIndex() );
                    if ( combo.getData( item ) != null ) {
                        item = (String) combo.getData( item );
                    }
                    c.setValue(item);
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

            if ( c.getValue() != null ) {
                box.setText( c.getValue() );
            }

            gd.horizontalSpan = 2;
            gd.grabExcessHorizontalSpace = true;
            gd.minimumWidth = 100;
            box.setLayoutData( gd );

            box.addModifyListener( new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    c.setValue(box.getText());
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
                                    final BaseSingleFieldConstraint c,
                                    GridData gd) {

        final Text box = toolkit.createText( parent,
                                             "" );

        if ( c.getValue() != null ) {
            box.setText( c.getValue() );
        }

        gd.grabExcessHorizontalSpace = true;
        gd.minimumWidth = 100;
        box.setLayoutData( gd );

        box.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                c.setValue(box.getText());
                modeller.setDirty( true );
            }
        } );
    }

    private void variableEditor(Composite composite,
                                final BaseSingleFieldConstraint c,
                                GridData gd) {
        List vars = modeller.getModel().getBoundVariablesInScope( c );

        final Combo combo = new Combo( composite,
                                       SWT.READ_ONLY );

        gd.horizontalSpan = 2;
        combo.setLayoutData( gd );
        if ( c.getValue() == null ) {
            combo.add( "Choose ..." );
        }

        int idx = 0;

        for ( int i = 0; i < vars.size(); i++ ) {
            String var = (String) vars.get( i );

            if ( c.getValue() != null && c.getValue().equals( var ) ) {
                idx = i;
            }
            combo.add( var );
        }

        combo.select( idx );

        combo.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                c.setValue(combo.getText());
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
