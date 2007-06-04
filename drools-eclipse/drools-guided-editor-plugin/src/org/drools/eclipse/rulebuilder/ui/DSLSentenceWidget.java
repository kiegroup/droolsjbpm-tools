package org.drools.eclipse.rulebuilder.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.brms.client.modeldriven.brxml.DSLSentence;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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
    
    private List widgets = new ArrayList();
    
    public DSLSentenceWidget(FormToolkit toolkit,
                             Composite parent,
                             DSLSentence sentence, RuleModeller modeller, int index) {
        super(parent,toolkit,modeller,index);
        
        this.sentence = sentence;
        
        GridLayout l = new GridLayout();
        l.numColumns = sentence.sentence.length();
        l.verticalSpacing = 0;
        l.marginTop = 0;
        l.marginHeight = 2;
        l.marginBottom = 0;
        parent.setLayout( l );
        
        makeWidget(sentence.sentence);
    }

    private void makeWidget(String dslLine) {
    	char[] chars = dslLine.toCharArray();
        Text currentBox = null;
        Label currentLabel = null;
        for ( int i = 0; i < chars.length; i++ ) {
            char c = chars[i];
            if (c == '{') {
                currentLabel = null;
                currentBox = toolkit.createText(parent, ""); 
                
                currentBox.addModifyListener(new ModifyListener(){
					public void modifyText(ModifyEvent e) {
						updateSentence();
					}
                });
                
                widgets.add(currentBox);
                
            } else if (c == '}') {
            	currentBox = null;
            } else {
                if (currentBox == null && currentLabel == null) {
                    currentLabel = toolkit.createLabel(parent, "");
                    widgets.add(currentLabel);
                }
                if (currentLabel != null) {
                    currentLabel.setText( currentLabel.getText() + c );
                } else if (currentBox != null) {
                    currentBox.setText( currentBox.getText() + c );
                }
            }
        }
        
        toolkit.paintBordersFor( parent );
	}
    
    protected void updateSentence() {
        String newSentence = "";
        for ( Iterator iter = widgets.iterator(); iter.hasNext(); ) {
            Control wid = (Control) iter.next();
            if (wid instanceof Label) {
                newSentence = newSentence + ((Label) wid).getText();
            } else if (wid instanceof Text) {
                newSentence = newSentence + " {" + ((Text) wid).getText() + "} ";
            }
        }
        this.sentence.sentence = newSentence.trim();
    }    
    

}
