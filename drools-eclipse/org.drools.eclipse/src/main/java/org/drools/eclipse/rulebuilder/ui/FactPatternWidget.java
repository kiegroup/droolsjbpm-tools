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

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.rulebuilder.modeldriven.HumanReadable;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.BaseSingleFieldConstraint;
import org.drools.ide.common.client.modeldriven.brl.CompositeFactPattern;
import org.drools.ide.common.client.modeldriven.brl.CompositeFieldConstraint;
import org.drools.ide.common.client.modeldriven.brl.ConnectiveConstraint;
import org.drools.ide.common.client.modeldriven.brl.FactPattern;
import org.drools.ide.common.client.modeldriven.brl.FieldConstraint;
import org.drools.ide.common.client.modeldriven.brl.SingleFieldConstraint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * This is the new smart widget that works off the model.
 */
public class FactPatternWidget extends Widget {

    private final CompositeFactPattern parentPattern;

    private final FactPattern          pattern;

    private boolean                    bindable;

    public FactPatternWidget(FormToolkit toolkit,
                             Composite parent,
                             RuleModeller mod,
                             FactPattern factPattern,
                             CompositeFactPattern parentPattern,
                             int idx,
                             boolean canBind) {

        super( parent,
               toolkit,
               mod,
               idx );

        this.pattern = factPattern;
        this.parentPattern = parentPattern;
        this.bindable = canBind;

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
        Composite composite = new Composite( parent,
                                             SWT.NO_FOCUS );
        composite.setLayout( new GridLayout( 1,
                                             true ) );
        Label l = toolkit.createLabel( composite,
                                       getPatternLabel() );
        l.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_GRAY ) );

        GridData labelGD = new GridData( GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL );
        labelGD.horizontalSpan = 2;
        labelGD.verticalAlignment = SWT.CENTER;
        //labelGD.horizontalAlignment = SWT.CENTER;
        composite.setLayoutData( labelGD );
        composite.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_GRAY ) );

        addDeleteAction();
        addMoreOptionsAction();

        Composite constraintComposite = toolkit.createComposite( parent );
        GridLayout constraintLayout = new GridLayout();
        constraintLayout.numColumns = 8;
        constraintLayout.marginLeft = 5;
        constraintComposite.setLayout( constraintLayout );

        for ( int row = 0; row < pattern.getFieldConstraints().length; row++ ) {
            FieldConstraint constraint = pattern.getFieldConstraints()[row];
            boolean nested = false;
            if ( constraint instanceof SingleFieldConstraint ) {
                nested = ((SingleFieldConstraint) constraint).getParent() != null;
            }
            renderFieldConstraints( constraintComposite,
                                    constraint,
                                    null,
                                    row,
                                    true,
                                    false );
        }

        toolkit.paintBordersFor( constraintComposite );
    }

    private void addMoreOptionsAction() {
        ImageHyperlink link = addImage( parent,
                                        "icons/new_item.gif" );

        link.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                RuleDialog popup = new AddNewFieldConstraintDialog( parent.getShell(),
                                                                    toolkit,
                                                                    getModeller(),
                                                                    pattern,
                                                                    parentPattern != null );
                popup.open();
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        link.setToolTipText( "Add a field to this condition, or bind a varible to this fact." );
    }

    private void addDeleteAction() {
        ImageHyperlink delWholeLink = addImage( parent,
                                                "icons/delete_obj.gif" );
        delWholeLink.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this ENTIRE condition, " + "and all the field constraints that belong to it." );
                dialog.setText( "Remove this entire condition?" );
                if ( dialog.open() == SWT.YES ) {
                    if ( parentPattern == null ) {
                        if ( getModeller().getModel().removeLhsItem( index ) ) {
                            getModeller().reloadLhs();
                        } else {
                            showMessage( "Can't remove that item as it is used in the action part of the rule." );
                        }
                    } else {
                        deleteBindedFact();
                    }
                    getModeller().setDirty( true );
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        delWholeLink.setToolTipText( "Remove this condition." );
    }

    private void renderFieldConstraints(Composite constraintComposite,
                                        FieldConstraint constraint,
                                        final CompositeFieldConstraint parentConstraint,
                                        int row,
                                        boolean showBinding,
                                        boolean nested) {
        if ( constraint instanceof SingleFieldConstraint ) {
            renderSingleFieldConstraint( constraintComposite,
                                         row,
                                         constraint,
                                         parentConstraint,
                                         showBinding,
                                         nested );
        } else if ( constraint instanceof CompositeFieldConstraint ) {
            compositeFieldConstraintEditor( constraintComposite,
                                            (CompositeFieldConstraint) constraint,
                                            parentConstraint,
                                            row,
                                            nested );
        }
    }

    private void compositeFieldConstraintEditor(Composite constraintComposite,
                                                final CompositeFieldConstraint constraint,
                                                final CompositeFieldConstraint parentConstraint,
                                                final int row,
                                                boolean nested) {

        // Label
        if ( constraint.compositeJunctionType.equals( CompositeFieldConstraint.COMPOSITE_TYPE_AND ) ) {
            toolkit.createLabel( constraintComposite,
                                 "All of:" );
        } else {
            toolkit.createLabel( constraintComposite,
                                 "Any of:" );
        }

        addRemoveButton( constraintComposite,
                         parentConstraint,
                         row,
                         "icons/delete_obj.gif",
                         nested );

        // button "add"
        ImageHyperlink link = addImage( constraintComposite,
                                        "icons/new_item.gif" );
        link.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                RuleDialog popup = new AddCompositeConstraintOptionDialog( parent.getShell(),
                                                                           getModeller(),
                                                                           constraint,
                                                                           pattern );
                popup.open();
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );

        link.setToolTipText( "Add fields to this constriant." );

        addNestedElements( constraintComposite,
                           constraint );
    }

    private void addNestedElements(Composite constraintComposite,
                                   final CompositeFieldConstraint constraint) {
        // Nested elementss
        FieldConstraint[] nestedConstraints = constraint.constraints;
        if ( nestedConstraints != null ) {
            Composite nestedComposite = toolkit.createComposite( constraintComposite );
            GridData gd = new GridData( GridData.FILL_HORIZONTAL );
            gd.horizontalSpan = 5;
            nestedComposite.setLayoutData( gd );

            GridLayout l = new GridLayout();
            l.numColumns = 8;
            l.marginBottom = 0;
            l.marginHeight = 0;
            l.marginLeft = 0;
            l.marginRight = 0;
            l.marginTop = 0;
            l.marginWidth = 0;
            l.verticalSpacing = 0;
            nestedComposite.setLayout( l );

            for ( int i = 0; i < nestedConstraints.length; i++ ) {
                renderFieldConstraints( nestedComposite,
                                        nestedConstraints[i],
                                        constraint,
                                        i,
                                        false,
                                        true );
                toolkit.paintBordersFor( nestedComposite );
            }
        } else {
            GridData gd = new GridData( GridData.FILL_HORIZONTAL );
            gd.horizontalSpan = 5;
            Label dummyLabel = toolkit.createLabel( constraintComposite,
                                                    "" ); // dummy
            dummyLabel.setLayoutData( gd );
        }
    }

    private void renderSingleFieldConstraint(Composite constraintComposite,
                                             int row,
                                             FieldConstraint constraint,
                                             CompositeFieldConstraint parentConstraint,
                                             boolean showBinding,
                                             boolean nested) {
        final SingleFieldConstraint c = (SingleFieldConstraint) constraint;
        if ( c.getConstraintValueType() != BaseSingleFieldConstraint.TYPE_PREDICATE ) {
            createConstraintRow( constraintComposite,
                                 parentConstraint,
                                 row,
                                 c,
                                 showBinding,
                                 nested );
        } else {
            createPredicateConstraintRow( constraintComposite,
                                          row,
                                          c );
        }
    }

    private void createConstraintRow(Composite constraintComposite,
                                     CompositeFieldConstraint parentConstraint,
                                     int row,
                                     final SingleFieldConstraint c,
                                     boolean showBinding,
                                     boolean nested) {

        Composite pad = new Composite( constraintComposite,
                                       SWT.NONE );
        pad.setLayout( new GridLayout( 2,
                                       false ) );
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.LEFT;
        gd.horizontalSpan = 2;

        pad.setLayoutData( gd );
        GridData data = new GridData();

        data.horizontalIndent = 15 * getNests( c );
        addBindingField( pad,
                         c,
                         showBinding,
                         data );
        toolkit.createLabel( pad,
                             c.getFieldName() );
        if (!hasChildren( row ) &&(c.connectives == null || c.connectives.length == 0 )) {
            addRemoveButton( constraintComposite,
                             parentConstraint,
                             row,
                             "icons/delete_item_small.gif",
                             nested );
        } else {
            toolkit.createLabel( constraintComposite,
                                 "" );
        }
        operatorDropDown( constraintComposite,
                          c );

        constraintValueEditor( constraintComposite,
                               c,
                               c.getFieldName() );

        createConnectives( constraintComposite,
                           c );
        addConnectiveAction( constraintComposite,
                             c );
    }

    private int getNests(final SingleFieldConstraint c) {
        SingleFieldConstraint s = (SingleFieldConstraint) c.getParent();
        int nests = 0;
        while ( s != null ) {
            nests++;
            s = (SingleFieldConstraint) s.getParent();
        }
        return nests;
    }

    private void addBindingField(Composite constraintComposite,
                                 final SingleFieldConstraint c,
                                 boolean showBinding,
                                 Object data) {
        if ( !c.isBound() ) {
            if ( bindable && showBinding ) {
                ImageHyperlink link = addImage( constraintComposite,
                                                "icons/new_item.gif" );
                link.addHyperlinkListener( new IHyperlinkListener() {
                    public void linkActivated(HyperlinkEvent e) {
                        RuleDialog popup = new AssignFieldVariableDialog( parent.getShell(),
                                                                          toolkit,
                                                                          getModeller(),
                                                                          c,
                                                                          pattern );
                        popup.open();
                    }

                    public void linkEntered(HyperlinkEvent e) {
                    }

                    public void linkExited(HyperlinkEvent e) {
                    }
                } );

                link.setToolTipText( "Bind the field called [" + c.getFieldName() + "] to a variable." );
                link.setLayoutData( data );
            } else {
                toolkit.createLabel( constraintComposite,
                                     "" );
            }
        } else {
            toolkit.createLabel( constraintComposite,
                                 "[" + c.getFieldBinding() + "]" );
        }

    }

    private void createPredicateConstraintRow(Composite constraintComposite,
                                              int row,
                                              final SingleFieldConstraint c) {
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 6;
        addImage( constraintComposite,
                  "icons/function_assets.gif" );
        formulaValueEditor( constraintComposite,
                            c,
                            gd );
        addRemoveButton( constraintComposite,
                         null,
                         row,
                         "icons/delete_item_small.gif",
                         false );
    }

    private void createConnectives(Composite parent,
                                   SingleFieldConstraint c) {
        if ( c.connectives != null && c.connectives.length > 0 ) {
            for ( int i = 0; i < c.connectives.length; i++ ) {
                toolkit.createLabel( parent,
                                     "" ); // dummy
                toolkit.createLabel( parent,
                                     "" ); // dummy
                toolkit.createLabel( parent,
                                     "" ); // dummy
                ConnectiveConstraint con = c.connectives[i];
                addRemoveConstraintAction( parent,
                                           c,
                                           con );
                connectiveOperatorDropDown( parent,
                                            con,
                                            c.getFieldName() );
                constraintValueEditor( parent,
                                       con,
                                       c.getFieldName() );

            }
        }
    }

    private void constraintValueEditor(Composite parent,
                                       BaseSingleFieldConstraint c,
                                       String name) {
        String type = this.modeller.getSuggestionCompletionEngine().getFieldType( pattern.getFactType(),
                                                                                  name );
        new ConstraintValueEditor( parent,
                                   c,
                                   toolkit,
                                   modeller,
                                   type,
                                   pattern );
    }

    private void addConnectiveAction(Composite constraintComposite,
                                     final SingleFieldConstraint c) {
        ImageHyperlink link = addImage( constraintComposite,
                                        "icons/add_connective.gif" );
        link.setToolTipText( "Add more options to this fields values." );
        link.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                c.addNewConnective();
                getModeller().reloadLhs();
                getModeller().setDirty( true );
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );

        link.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING ) );
    }

    private void addRemoveButton(Composite constraintComposite,
                                 final CompositeFieldConstraint parentConstraint,
                                 final int row,
                                 String iconRef,
                                 boolean nested) {
        if ( nested ) {
            addNestedConstraintDeleteAction( constraintComposite,
                                             parentConstraint,
                                             row,
                                             iconRef );
        } else {
            addRemoveFieldAction( constraintComposite,
                                  row,
                                  iconRef );
        }

    }

    private void addNestedConstraintDeleteAction(Composite constraintComposite,
                                                 final CompositeFieldConstraint parentConstraint,
                                                 final int row,
                                                 String iconRef) {
        ImageHyperlink delLink = addImage( constraintComposite,
                                           iconRef );
        // "icons/delete_obj.gif");
        delLink.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this (nested) restriction." );
                dialog.setText( "Remove this item from nested constraint?" );
                if ( dialog.open() == SWT.YES ) {
                    parentConstraint.removeConstraint( row );
                    getModeller().reloadLhs();
                    getModeller().setDirty( true );
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
    }

    private void addRemoveFieldAction(Composite constraintComposite,
                                      final int currentRow,
                                      String iconRef) {
        ImageHyperlink delLink = addImage( constraintComposite,
                                           iconRef );
        delLink.setToolTipText( "Remove this fieldconstraint" );
        delLink.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this item?" );
                dialog.setText( "Remove this item?" );
                if ( dialog.open() == SWT.YES ) {
                    pattern.removeConstraint(currentRow);
                    getModeller().reloadLhs();
                    getModeller().setDirty( true );
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        delLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING ) );
    }

    private boolean hasChildren(int row) {
        SingleFieldConstraint con = (SingleFieldConstraint) pattern.getFieldConstraints()[row];
        FieldConstraint[] fc = pattern.getFieldConstraints();
        for ( int i = row; i < fc.length; i++ ) {
            SingleFieldConstraint f = (SingleFieldConstraint) fc[i];
            if (con.equals( f.getParent())){
                return true;
            }
        }
        return false;
    }

    private void addRemoveConstraintAction(Composite composite,
                                           final SingleFieldConstraint constraint,
                                           final ConnectiveConstraint connConstraint) {
        ImageHyperlink delLink = addImage( composite,
                                           "icons/delete_item_small.gif" );
        delLink.setToolTipText( "Remove this field constraint" );
        delLink.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this item?" );
                dialog.setText( "Remove this item?" );
                if ( dialog.open() == SWT.YES ) {
                    ConnectiveConstraint[] connectives = constraint.connectives;
                    List nConnectives = new ArrayList();
                    for ( int i = 0; i < connectives.length; i++ ) {
                        if ( connectives[i] != connConstraint ) {
                            nConnectives.add( connectives[i] );
                        }
                    }
                    constraint.connectives = (ConnectiveConstraint[]) nConnectives.toArray( new ConnectiveConstraint[nConnectives.size()] );

                    getModeller().reloadLhs();
                    getModeller().setDirty( true );
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        delLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END ) );
    }

    /**
     * This returns the pattern label.
     */
    private String getPatternLabel() {
        if ( pattern.getBoundName() != null ) {
            return pattern.getFactType() + " [" + pattern.getBoundName() + "]";
        }
        return pattern.getFactType();
    }

    private void operatorDropDown(Composite parent,
                                  final SingleFieldConstraint c) {
        String[] ops = getCompletions().getOperatorCompletions( pattern.getFactType(),
                                                                c.getFieldName() );
        final Combo box = new Combo( parent,
                                     SWT.SIMPLE | SWT.DROP_DOWN | SWT.READ_ONLY );
        for ( int i = 0; i < ops.length; i++ ) {
            String op = ops[i];
            box.add( HumanReadable.getOperatorDisplayName( op ) );
            if ( op.equals( c.getOperator() ) ) {
                box.select( i );
            }
        }
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = 2;
        box.setLayoutData( gridData );
        box.addListener( SWT.Selection,
                         new Listener() {
                             public void handleEvent(Event event) {
                                 c.setOperator(HumanReadable.getOperatorName( box.getText() ));
                                 getModeller().setDirty( true );
                             }
                         } );
    }

    private void connectiveOperatorDropDown(Composite parent,
                                            final ConnectiveConstraint con,
                                            String fieldName) {
        String[] ops = getCompletions().getConnectiveOperatorCompletions( pattern.getFactType(),
                                                                          fieldName );
        final Combo box = new Combo( parent,
                                     SWT.SIMPLE | SWT.DROP_DOWN | SWT.READ_ONLY );
        for ( int i = 0; i < ops.length; i++ ) {
            String op = ops[i];
            box.add( HumanReadable.getOperatorDisplayName( op ) );
            if ( op.equals( con.getOperator() ) ) {
                box.select( i );
            }
        }
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = 2;
        box.setLayoutData( gridData );
        box.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                con.setOperator( HumanReadable.getOperatorName( box.getText() ) );
                getModeller().setDirty( true );

            }
        } );
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
                getModeller().setDirty( true );
            }
        } );
    }

    private void deleteBindedFact() {
        List newPatterns = new ArrayList();
        for ( int i = 0; i < parentPattern.getPatterns().length; i++ ) {
            if ( parentPattern.getPatterns()[i] != pattern ) {
                newPatterns.add( parentPattern.getPatterns()[i] );
            }
        }
        
        parentPattern.clearFactPatterns();
        parentPattern.addFactPatterns((FactPattern[]) newPatterns.toArray( new FactPattern[newPatterns.size()] ));
        getModeller().reloadLhs();
    }

    private SuggestionCompletionEngine getCompletions() {
        return getModeller().getSuggestionCompletionEngine();
    }

}
