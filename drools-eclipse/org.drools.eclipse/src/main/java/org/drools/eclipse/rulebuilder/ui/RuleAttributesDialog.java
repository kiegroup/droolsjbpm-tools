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

import java.util.Iterator;
import java.util.List;

import org.drools.ide.common.client.modeldriven.brl.RuleAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class RuleAttributesDialog extends RuleDialog {

    private RuleModeller      modeller;

    public RuleAttributesDialog(Shell parent,
                                RuleModeller modeller) {
        super( parent,
               "Add new option to the rule",
               "Pick the value from combo." );

        this.modeller = modeller;
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

        createAtributesSelectionCombo( composite,
                                       gd );

        return composite;
    }

    private void createAtributesSelectionCombo(Composite composite,
                                               GridData gd) {
        createLabel( composite,
                     "Attributes" );
        final Combo combo = new Combo( composite,
                                       SWT.READ_ONLY );
        combo.setLayoutData( gd );
        List attributes = RuleAttributeWidget.getAttributeList();
        for ( Iterator iterator = attributes.iterator(); iterator.hasNext(); ) {
            String attr = (String) iterator.next();
            combo.add( attr );
        }
        combo.select( 0 );

        combo.addListener( SWT.Selection,
                           new Listener() {
                               public void handleEvent(Event event) {
                                   if ( combo.getSelectionIndex() == 0 ) {
                                       return;
                                   }
                                   modeller.getModel().addAttribute( new RuleAttribute( combo.getText(),
                                                                                        "" ) );
                                   modeller.setDirty( true );
                                   modeller.reloadOptions();
                                   close();
                               }
                           } );

    }

}
