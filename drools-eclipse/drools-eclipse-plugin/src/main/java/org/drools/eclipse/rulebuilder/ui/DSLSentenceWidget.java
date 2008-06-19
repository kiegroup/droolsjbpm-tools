package org.drools.eclipse.rulebuilder.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.guvnor.client.modeldriven.brl.DSLSentence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * This displays a widget to edit a DSL sentence.
 * 
 * @author Ahti Kitsik
 * @author Anton Arhipov
 */
public abstract class DSLSentenceWidget extends Widget {

    private final DSLSentence sentence;

    private List              widgets = new ArrayList();

    public DSLSentenceWidget(FormToolkit toolkit,
                             Composite parent,
                             DSLSentence sentence,
                             RuleModeller modeller,
                             int index) {
        super( parent,
               toolkit,
               modeller,
               index );

        this.sentence = sentence;

        makeWidget();
        addDeleteAction();
    }

    protected abstract void updateModel();

    private void addDeleteAction() {

        ImageHyperlink delLink = addImage( parent,
                                           "icons/delete_item_small.gif" );
        delLink.addHyperlinkListener( new IHyperlinkListener() {

            public void linkActivated(HyperlinkEvent e) {
                MessageBox dialog = new MessageBox( Display.getCurrent().getActiveShell(),
                                                    SWT.YES | SWT.NO | SWT.ICON_WARNING );
                dialog.setMessage( "Remove this DSL sentense?" );
                dialog.setText( "Remove this DSL sentense?" );
                if ( dialog.open() == SWT.YES ) {
                    updateModel();
                }
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }
        } );
        delLink.setToolTipText( "Remove this condition." );
    }

    private void makeWidget() {

        int elems = 0;

        char[] chars = this.sentence.sentence.toCharArray();
        Text currentBox = null;
        Label currentLabel = null;
        for ( int i = 0; i < chars.length; i++ ) {
            char c = chars[i];
            if ( c == '{' ) {
                currentLabel = null;

                currentBox = toolkit.createText( parent,
                                                 "" );

                //final Text thisBox = currentBox;
                elems++;

                GridData gd = new GridData( GridData.FILL_HORIZONTAL );
                gd.grabExcessHorizontalSpace = true;
                gd.minimumWidth = 100;
                currentBox.setLayoutData( gd );
                
                widgets.add( currentBox );

            } else if ( c == '}' ) {
                currentBox = null;
            } else {
                if ( currentBox == null && currentLabel == null ) {
                    currentLabel = toolkit.createLabel( parent,
                                                        "" );
                    elems++;

                    widgets.add( currentLabel );
                }
                if ( currentLabel != null ) {
                    currentLabel.setText( currentLabel.getText() + c );
                } else if ( currentBox != null ) {
                    currentBox.setText( currentBox.getText() + c );
                }
            }
        }

        GridLayout l = new GridLayout();
        int cols = elems + 1;
        l.numColumns = cols;
        l.verticalSpacing = 0;
        l.marginTop = 0;
        l.marginHeight = 2;
        l.marginBottom = 0;
        parent.setLayout( l );

        // Attach listeners
        Iterator widgetiter = widgets.iterator();
        while ( widgetiter.hasNext() ) {
            Object o = (Object) widgetiter.next();
            if (o instanceof Text) {
                ((Text)o).addModifyListener( new ModifyListener() {
                    public void modifyText(ModifyEvent e) {
                        updateSentence();
                        /*                        Point p = thisBox.getSize();
                         
                         GC gc = new GC(thisBox);
                         gc.setFont(thisBox.getFont());
                         FontMetrics fontMetrics = gc.getFontMetrics();
                         int w = fontMetrics.getAverageCharWidth()*thisBox.getText().length();
                         gc.dispose();
                         
                         
                         thisBox.setSize( w, p.y );
                         thisBox.redraw();
                         parent.redraw();
                         */
                        getModeller().setDirty( true );
                    }
                } );
                
            }
        }
        toolkit.paintBordersFor( parent );
    }

    protected void updateSentence() {
        String newSentence = "";
        for ( Iterator iter = widgets.iterator(); iter.hasNext(); ) {
            Control wid = (Control) iter.next();
            if ( wid instanceof Label ) {
                newSentence = newSentence + ((Label) wid).getText();
            } else if ( wid instanceof Text ) {
                newSentence = newSentence + "{" + ((Text) wid).getText() + "}";
            }
        }
        this.sentence.sentence = newSentence;
    }

}
