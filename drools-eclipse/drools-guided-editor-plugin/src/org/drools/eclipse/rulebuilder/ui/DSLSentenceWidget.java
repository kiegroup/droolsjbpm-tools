package org.drools.eclipse.rulebuilder.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.brms.client.modeldriven.brxml.DSLSentence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

	private List widgets = new ArrayList();

	public DSLSentenceWidget(FormToolkit toolkit, Composite parent,
			DSLSentence sentence, RuleModeller modeller, int index) {
		super(parent, toolkit, modeller, index);

		this.sentence = sentence;

		GridLayout l = new GridLayout();
		l.numColumns = sentence.sentence.length() + 1;
		l.verticalSpacing = 0;
		l.marginTop = 0;
		l.marginHeight = 2;
		l.marginBottom = 0;
		parent.setLayout(l);

		makeWidget();
		addDeleteAction();
	}

	protected abstract void updateModel();

	private void addDeleteAction() {
		ImageHyperlink delLink = addImage(parent, "icons/delete_obj.gif");
		delLink.addHyperlinkListener(new IHyperlinkListener() {

			public void linkActivated(HyperlinkEvent e) {
				MessageBox dialog = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
				dialog.setMessage("Remove this DSL sentense?");
				dialog.setText("Remove this DSL sentense?");
				if (dialog.open() == SWT.YES){
					updateModel();
				}
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
		delLink.setToolTipText("Remove this condition.");
	}

	private void makeWidget() {
		char[] chars = this.sentence.sentence.toCharArray();
		Text currentBox = null;
		Label currentLabel = null;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '{') {
				currentLabel = null;
				currentBox = toolkit.createText(parent, "");

				currentBox.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						updateSentence();
						getModeller().setDirty(true);
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
					currentLabel.setText(currentLabel.getText() + c);
				} else if (currentBox != null) {
					currentBox.setText(currentBox.getText() + c);
				}
			}
		}

		toolkit.paintBordersFor(parent);
	}

	protected void updateSentence() {
		String newSentence = "";
		for (Iterator iter = widgets.iterator(); iter.hasNext();) {
			Control wid = (Control) iter.next();
			if (wid instanceof Label) {
				newSentence = newSentence + ((Label) wid).getText();
			} else if (wid instanceof Text) {
				newSentence = newSentence + "{" + ((Text) wid).getText() + "}";
			}
		}
		this.sentence.sentence = newSentence.trim();
	}

}
