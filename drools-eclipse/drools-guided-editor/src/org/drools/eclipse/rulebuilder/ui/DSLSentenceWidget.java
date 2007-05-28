package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.brxml.DSLSentence;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This displays a widget to edit a DSL sentence.
 * 
 * @author Michael Neale
 * @author Ahti Kitsik
 * @author Anton Arhipov
 */
public class DSLSentenceWidget extends Widget {

    private final DSLSentence sentence;

    public DSLSentenceWidget(FormToolkit toolkit,
                             Composite parent,
                             DSLSentence sentence, RuleModeller modeller, int index) {
        super(parent,toolkit,modeller,index);
        
        this.sentence = sentence;
        init();
    }

    private void init() {
        GridLayout l = new GridLayout();
        l.numColumns = sentence.sentence.length();//sentence.elements.length;
        l.verticalSpacing = 0;
        l.marginTop = 0;
        l.marginHeight = 2;
        l.marginBottom = 0;
        parent.setLayout( l );

        toolkit.createLabel( parent,
                             sentence.toString() );

        final Text box = toolkit.createText( parent,
                                             sentence.toString() );
        box.setTabs( sentence.toString().length() );

        box.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                sentence.sentence = box.getText();
                getModeller().setDirty( true );
            }
        } );

        /*for (int i = 0; i < sentence.length; i++) {
         //final DSLSentenceFragment el = sentence.elements[i];
         
         if (!el.isEditableField) {
         toolkit.createLabel(parent, sentence.toString());
         } else {
         final Text box = toolkit.createText(parent, el.value);
         box.setTabs(el.value.length());

         box.addModifyListener(new ModifyListener() {
         public void modifyText(ModifyEvent e) {
         el.value = box.getText();
         }
         });

         }
         }*/
        toolkit.paintBordersFor( parent );
    }

}
