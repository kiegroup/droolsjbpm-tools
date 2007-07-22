package org.drools.eclipse.rulebuilder.ui;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

public class NumericKeyFilter implements KeyListener {

	private Text box;
	
	public NumericKeyFilter(Text box){
		this.box = box;
		box.addKeyListener(this);
	}
	
	public void keyPressed(KeyEvent e) {
		char c = e.character;
		if (Character.isLetter( c ) && c != '='
            && !(this.box.getText().startsWith( "=" ))) {
			e.doit = false;
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}

}
