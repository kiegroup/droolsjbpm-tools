package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.brxml.RuleAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class RuleAttributeWidget extends Widget {

	public RuleAttributeWidget(FormToolkit toolkit, Composite parent,
			RuleModeller modeller) {
		super(parent, toolkit, modeller, 0);

		GridLayout l = new GridLayout();
		l.numColumns = 4;
		l.marginBottom = 0;
		l.marginHeight = 0;
		l.marginLeft = 0;
		l.marginRight = 0;
		l.marginTop = 0;
		l.marginWidth = 0;
		l.verticalSpacing = 0;
		parent.setLayout(l);

		create();
	}

	private void create() {

		RuleAttribute[] attrs = modeller.getModel().attributes;
		for (int i = 0; i < attrs.length; i++) {
			RuleAttribute at = attrs[i];
			addAttribute(at);
		}

	}

	private void addAttribute(RuleAttribute at) {
		toolkit.createLabel(parent, at.attributeName); 
		
		if (at.attributeName.equals( "enabled" ) 
                || at.attributeName.equals( "auto-focus" )
                || at.attributeName.equals( "lock-on-active" )) {
			final Button chekbox = toolkit.createButton(parent, "", SWT.CHECK);
		}else{
			final Text box = toolkit.createText(parent, "");
		}
		
		addDeleteLink();

	}

	private void addDeleteLink() {
		ImageHyperlink delLink = addImage(parent, "icons/delete_item_small.gif");
		delLink.setToolTipText("Remove this fieldconstraint");
		delLink.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				MessageBox dialog = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
				dialog.setMessage("Remove this item?");
				dialog.setText("Remove this item?");
				if (dialog.open() == SWT.YES) {
					//TODO: delete relevant attribute
				}
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});

	}

}
