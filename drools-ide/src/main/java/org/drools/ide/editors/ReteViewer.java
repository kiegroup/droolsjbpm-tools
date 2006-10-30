package org.drools.ide.editors;

import java.awt.BorderLayout;
import java.awt.Frame;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.ide.DRLInfo;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.rule.Package;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class ReteViewer extends EditorPart {

	private DRLRuleEditor drlEditor;

	private Frame frame;

	private Composite parent;

	public ReteViewer(DRLRuleEditor drlEditor, IDocumentProvider documentProvider) {
		this.drlEditor = drlEditor;
	}

	public void createPartControl(Composite parent) {
		this.parent = parent;
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
		Button generateButton = new Button(parent, SWT.PUSH);
		generateButton.setText("Generate Rete View");
		generateButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				generateReteView();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				generateReteView();
			}
		});

		try {
			Composite frameParent = new Composite(parent, SWT.EMBEDDED);
			frameParent.setLayoutData(new GridData(GridData.FILL_BOTH));
			frame = SWT_AWT.new_Frame(frameParent);
			frame.setLayout(new BorderLayout());
		} catch (SWTError exc) {
			// it is possible that this exception is thrown if 
			// SWT is not supported, e.g. in Mac
			DroolsIDEPlugin.log(exc);
		}
	}

	private RuleBase getRuleBase() {
		try {
			DRLInfo drlInfo = DroolsIDEPlugin.getDefault().parseResource(drlEditor, true, true);
			if (drlInfo != null) {
				Package pkg = drlInfo.getPackage();
				// add the package to a rulebase
	            RuleBase ruleBase = RuleBaseFactory.newRuleBase();
				ruleBase.addPackage(pkg);
				return ruleBase;
			}
		} catch (Throwable t) {
			DroolsIDEPlugin.log(t);
		}
		return null;
	}

	public void doSave(IProgressMonitor monitor) {
		// Do nothing
	}

	public void doSaveAs() {
		// Do nothing
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	public void clear() {
		if (frame != null) { // possible if frame creation failed
			frame.removeAll();
		}
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void setFocus() {
		if (drlEditor.isDirty()) {
			clear();
		}
	}

	private void generateReteView() {
		if (frame != null) { // possible if frame creation failed
			clear();
			try {
				RuleBase ruleBase = getRuleBase();
				if (ruleBase == null) {
					// TODO signal user that rule cannot be parsed
				} else {
//					ReteooJungViewerPanel viewer = new ReteooJungViewerPanel(
//							ruleBase);
//					frame.add(viewer);
					frame.validate();
					parent.layout();
				}
			} catch (Throwable t) {
				t.printStackTrace();
				DroolsIDEPlugin.log(t);
			}
		}
	}
}
