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
import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.node.MilestoneNode;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Dialog for editing constraints.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class MilestoneConstraintDialog extends EditBeanDialog<String> {

	private WorkflowProcess process;
	private TabFolder tabFolder;
	private SourceViewer constraintViewer;
	private ConstraintCompletionProcessor completionProcessor;

	public MilestoneConstraintDialog(Shell parentShell, WorkflowProcess process, MilestoneNode milestone) {
		super(parentShell, "Constraint editor");
		this.process = process;
		setValue(milestone.getConstraint());
	}

	protected String updateValue(String value) {
		if (tabFolder.getSelectionIndex() == 0) {
			return getConstraintText();
		}
		return null;
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
		String value = (String) getValue();
		if (value == null) {
			value = "";
		}
		IDocument document = new Document(value);
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
		layout.numColumns = 3;
		top.setLayout(layout);

		Button importButton = new Button(top, SWT.PUSH);
		importButton.setText("Imports ...");
		importButton.setFont(JFaceResources.getDialogFont());
		importButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				importButtonPressed();
			}
		});
		gd = new GridData();
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
		globalButton.setLayoutData(gd);

		tabFolder = new TabFolder(parent, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		tabFolder.setLayoutData(gd);
		TabItem textEditorTab = new TabItem(tabFolder, SWT.NONE);
		textEditorTab.setText("Textual Editor");

		textEditorTab.setControl(createTextualEditor(tabFolder));
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
					List<String> imports = dialog.getImports();
					process.setImports(imports);
					List<String> functionImports = dialog.getFunctionImports();
					process.setFunctionImports(functionImports);
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
					Map<String, String> globals = dialog.getGlobals();
					process.setGlobals(globals);
					completionProcessor.reset();
				}
			}
		};
		r.run();
	}

}
