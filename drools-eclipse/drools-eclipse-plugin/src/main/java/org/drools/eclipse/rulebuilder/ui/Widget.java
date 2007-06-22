package org.drools.eclipse.rulebuilder.ui;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public abstract class Widget {

    final protected Composite    parent;

    final protected FormToolkit  toolkit;

    final protected RuleModeller modeller;

    final protected int          index;

    public Widget(Composite parent,
                  FormToolkit toolkit,
                  RuleModeller modeller,
                  int index) {
        this.parent = parent;
        this.toolkit = toolkit;
        this.modeller = modeller;
        this.index = index;
    }

    public ImageHyperlink addImage(Composite parent,
                                   String fileName) {
        ImageHyperlink imageHyperlink = toolkit.createImageHyperlink( parent,
                                                                      0 );
        ImageDescriptor imageDescriptor = DroolsEclipsePlugin.getImageDescriptor( fileName );
        imageHyperlink.setImage( imageDescriptor.createImage() );
        return imageHyperlink;
    }

    protected void addDeleteRHSAction() {
        ImageHyperlink delWholeLink = addImage( parent,
                                                "icons/delete_obj.gif" );
        delWholeLink.addHyperlinkListener( new IHyperlinkListener() {
            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this action?" );
                dialog.setText( "Remove this action?" );
                if ( dialog.open() == SWT.YES ) {
                    getModeller().getModel().removeRhsItem( index );
                    getModeller().setDirty( true );
                    getModeller().reloadRhs();
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        delWholeLink.setToolTipText( "Remove this action." );
    }

    protected void showMessage(String msg) {
        MessageBox dialog = new MessageBox( Display.getDefault().getActiveShell(),
                                            SWT.OK | SWT.ICON_INFORMATION );
        dialog.setMessage( msg );
        dialog.setText( "Information" );
        dialog.open();
    }

    protected RuleModeller getModeller() {
        return modeller;
    }

}
