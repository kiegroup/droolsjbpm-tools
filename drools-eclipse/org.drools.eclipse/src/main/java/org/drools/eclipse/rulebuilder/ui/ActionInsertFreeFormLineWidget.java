package org.drools.eclipse.rulebuilder.ui;

import org.drools.ide.common.client.modeldriven.brl.FreeFormLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class ActionInsertFreeFormLineWidget extends Widget {

    private FreeFormLine  action;
    private final boolean rhs;

    public ActionInsertFreeFormLineWidget(FormToolkit toolkit,
                                          Composite comp,
                                          RuleModeller ruleModeller,
                                          final FreeFormLine action,
                                          int i,
                                          boolean rhs) {

        super( comp,
               toolkit,
               ruleModeller,
               i );
        this.rhs = rhs;

        GridLayout l = new GridLayout();
        l.numColumns = 2;
        parent.setLayout( l );

        createTextfield( comp,
                         action );
        addRemoveFieldAction( parent,
                              i );
        toolkit.paintBordersFor( parent );
        this.action = action;
    }

    private void createTextfield(Composite comp,
                                 final FreeFormLine action) {
        final Text text = toolkit.createText( comp,
                                              action.text );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.grabExcessHorizontalSpace = true;
        gd.minimumWidth = 100;
        text.setLayoutData( gd );

        text.addModifyListener( new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                getModeller().setDirty( true );
                action.text = text.getText();
            }
        } );
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
                    if ( rhs ) {
                        getModeller().getModel().removeRhsItem( row );
                        getModeller().setDirty( true );
                        getModeller().reloadRhs();
                    } else {
                        getModeller().getModel().removeLhsItem( row );
                        getModeller().setDirty( true );
                        getModeller().reloadLhs();
                    }
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
    }

}
