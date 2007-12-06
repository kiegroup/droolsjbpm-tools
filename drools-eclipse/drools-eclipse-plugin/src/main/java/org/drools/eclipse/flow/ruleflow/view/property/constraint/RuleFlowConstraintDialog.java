package org.drools.eclipse.flow.ruleflow.view.property.constraint;
/*
 * Copyright 2005 JBoss Inc
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

import java.util.List;
import java.util.Map;

import org.drools.eclipse.editors.DRLSourceViewerConfig;
import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.drools.ruleflow.core.Constraint;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.ruleflow.core.impl.ConstraintImpl;
import org.drools.util.ArrayUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for editing constraints.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowConstraintDialog extends Dialog {

    private static final String[] DIALECTS = new String[] { "mvel", "java" };
    private static final String[] TYPES = new String[] { "rule", "code" };

    private Constraint constraint;
	private RuleFlowProcess process;
	private boolean success;
	private Button alwaysTrue;
	private Text nameText;
	private Text priorityText;
    private Combo typeCombo;
    private Combo dialectCombo;
	private TabFolder tabFolder;
	private SourceViewer constraintViewer;
	private ConstraintCompletionProcessor completionProcessor;

	public RuleFlowConstraintDialog(Shell parentShell, RuleFlowProcess process) {
		super(parentShell);
		this.process = process;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Constraint editor");
	}

	protected Point getInitialSize() {
		return new Point(600, 450);
	}

	private Control createTextualEditor(Composite parent) {
		constraintViewer = new SourceViewer(parent, null, SWT.BORDER);
		constraintViewer.configure(new DRLSourceViewerConfig(null) {
			public IReconciler getReconciler(ISourceViewer sourceViewer) {
				return null;
			}
			public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
				ContentAssistant assistant = new ContentAssistant();
				completionProcessor = new ConstraintCompletionProcessor(process);
				assistant.setContentAssistProcessor(
					completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
				assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
				return assistant;
			}
		});
		IDocument document = new Document();
		constraintViewer.setDocument(document);
		IDocumentPartitioner partitioner =
            new FastPartitioner(
                new DRLPartionScanner(),
                DRLPartionScanner.LEGAL_CONTENT_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
        constraintViewer.getControl().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.character == ' ' && e.stateMask == SWT.CTRL) {
					constraintViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				}
			}
			public void keyReleased(KeyEvent e) {
			}
        });
		return constraintViewer.getControl();
	}
	
	private String getConstraintText() {
		return constraintViewer.getDocument().get();
	}
	
	private void setConstraintText(String text) {
		constraintViewer.getDocument().set(text);
	}

	public Control createDialogArea(Composite parent) {

		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		layout.numColumns = 2;

		Composite top = new Composite(parent, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		top.setLayoutData(gd);

		layout = new GridLayout();
		layout.numColumns = 4;
		top.setLayout(layout);

		Label l1 = new Label(top, SWT.None);
		l1.setText("Name:");
		gd = new GridData();
		l1.setLayoutData(gd);
		nameText = new Text(top, SWT.BORDER);
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.widthHint = 200;
		gd.horizontalSpan = 3;
		nameText.setLayoutData(gd);

		Label l2 = new Label(top, SWT.NONE);
		gd = new GridData();
		l2.setLayoutData(gd);
		l2.setText("Priority:");
		priorityText = new Text(top, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 200;
		gd.horizontalSpan = 3;
		priorityText.setLayoutData(gd);

		alwaysTrue = new Button(top, SWT.CHECK);
		alwaysTrue.setText("Always true");
		gd = new GridData();
		gd.horizontalSpan = 2;
		alwaysTrue.setLayoutData(gd);
		
		Button importButton = new Button(top, SWT.PUSH);
		importButton.setText("Imports ...");
		importButton.setFont(JFaceResources.getDialogFont());
		importButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				importButtonPressed();
			}
		});
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.END;
		importButton.setLayoutData(gd);

		Button globalButton = new Button(top, SWT.PUSH);
		globalButton.setText("Globals ...");
		globalButton.setFont(JFaceResources.getDialogFont());
		globalButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				globalButtonPressed();
			}
		});
		gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		globalButton.setLayoutData(gd);

        Label l3 = new Label(top, SWT.NONE);
        gd = new GridData();
        l3.setLayoutData(gd);
        l3.setText("Type:");
        typeCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
        typeCombo.setItems(TYPES);
        typeCombo.select(0);
        gd = new GridData();
        typeCombo.setLayoutData(gd);
        
        Label l4 = new Label(top, SWT.NONE);
        gd = new GridData();
        l4.setLayoutData(gd);
        l4.setText("Dialect:");
        dialectCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
        dialectCombo.setItems(DIALECTS);
        dialectCombo.select(0);
        gd = new GridData();
        dialectCombo.setLayoutData(gd);
        
		tabFolder = new TabFolder(parent, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 4;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		tabFolder.setLayoutData(gd);
		TabItem textEditorTab = new TabItem(tabFolder, SWT.NONE);
		textEditorTab.setText("Textual Editor");

		textEditorTab.setControl(createTextualEditor(tabFolder));

		alwaysTrue.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				tabFolder.setVisible(!alwaysTrue.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		return tabFolder;
	}
	
	private void importButtonPressed() {
		final Runnable r = new Runnable() {
			public void run() {
				RuleFlowImportsDialog dialog =
					new RuleFlowImportsDialog(getShell(), process);
				dialog.create();
				int code = dialog.open();
				if (code != CANCEL) {
					List imports = dialog.getImports();
					process.setImports(imports);
					completionProcessor.reset();
				}
			}
		};
		r.run();
	}

	private void globalButtonPressed() {
		final Runnable r = new Runnable() {
			public void run() {
				RuleFlowGlobalsDialog dialog =
					new RuleFlowGlobalsDialog(getShell(), process);
				dialog.create();
				int code = dialog.open();
				if (code != CANCEL) {
					Map globals = dialog.getGlobals();
					process.setGlobals(globals);
					completionProcessor.reset();
				}
			}
		};
		r.run();
	}

	protected void okPressed() {
		int selectionIndex = tabFolder.getSelectionIndex();
		if (selectionIndex == 1) {
			updateTranslation();
		} else {
			updateConstraint();
		}
		super.okPressed();
	}

	private void updateTranslation() {
		// TODO add custom token model checker
		success = true;
		constraint = new ConstraintImpl();
		constraint.setConstraint(null);
		constraint.setConstraint(getConstraintText());
		constraint.setName(nameText.getText());
		try {
			constraint.setPriority(Integer.parseInt(priorityText.getText()));
		} catch (NumberFormatException exc) {
			constraint.setPriority(1);
		}
	}

	public boolean isSuccess() {
		return success;
	}

	public void updateConstraint() {
		constraint = new ConstraintImpl();
		if (!alwaysTrue.getSelection()) {
			constraint.setConstraint(getConstraintText());
		} else {
			constraint.setConstraint("eval(true)");
		}
		constraint.setName(nameText.getText());
		try {
			constraint.setPriority(Integer.parseInt(priorityText.getText()));
		} catch (NumberFormatException exc) {
			constraint.setPriority(1);
		}
		constraint.setType(typeCombo.getItem(typeCombo.getSelectionIndex()));
        constraint.setDialect(dialectCombo.getItem(dialectCombo.getSelectionIndex()));
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
		if (constraint != null) {
			if ("eval(true)".equals(constraint.getConstraint())) {
				alwaysTrue.setSelection(true);
			} else {
				setConstraintText(constraint.getConstraint().toString());
			}
			tabFolder.setVisible(!alwaysTrue.getSelection());
			nameText.setText(constraint.getName() == null ? "" : constraint
					.getName());
			priorityText.setText(constraint.getPriority() + "");
	        int index = 0;
            String type = constraint.getType();
            int found = ArrayUtils.indexOf(TYPES, type);
            if (found >= 0) {
                index = found;
            }
            typeCombo.select(index);
            index = 0;
            String dialect = constraint.getDialect();
            found = ArrayUtils.indexOf(DIALECTS, dialect);
            if (found >= 0) {
                index = found;
	        }
	        dialectCombo.select(index);
			setConstraintText(constraint.getConstraint());
		} else {
			priorityText.setText("1");
			nameText.setText("constraint");
		}
	}
}
