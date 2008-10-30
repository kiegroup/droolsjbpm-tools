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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.eclipse.editors.DRLSourceViewerConfig;
import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.drools.knowledge.definitions.process.WorkflowProcess;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Dialog for editing imports.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowGlobalsDialog extends Dialog {

	private static final Pattern GLOBAL_PATTERN = Pattern.compile(
		"\\n\\s*global\\s+([^\\s;#]+)\\s+([^\\s;#]+);?", Pattern.DOTALL);
	
	private WorkflowProcess process;
	private boolean success;
	private TabFolder tabFolder;
	private SourceViewer globalsViewer;
	private Map<String, String> globals;

	public RuleFlowGlobalsDialog(Shell parentShell, WorkflowProcess process) {
		super(parentShell);
		this.process = process;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Globals editor");
	}

	protected Point getInitialSize() {
		return new Point(600, 450);
	}

	private Control createTextualEditor(Composite parent) {
		globalsViewer = new SourceViewer(parent, null, SWT.BORDER);
		globalsViewer.configure(new DRLSourceViewerConfig(null) {
			public IReconciler getReconciler(ISourceViewer sourceViewer) {
				return null;
			}
			public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
                ContentAssistant assistant = new ContentAssistant();
                IContentAssistProcessor completionProcessor = new GlobalCompletionProcessor();
                assistant.setContentAssistProcessor(
                    completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
                assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
                return assistant;
			}
		});
		IDocument document = new Document(getProcessImports());
		globalsViewer.setDocument(document);
        IDocumentPartitioner partitioner =
            new FastPartitioner(
                new DRLPartionScanner(),
                DRLPartionScanner.LEGAL_CONTENT_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
        globalsViewer.getControl().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.character == ' ' && e.stateMask == SWT.CTRL) {
                    globalsViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
                }
            }
            public void keyReleased(KeyEvent e) {
            }
        });
		return globalsViewer.getControl();
	}
	
	private String getProcessImports() {
		String result = "# define your globals here: e.g. global java.util.List myList\n";
		Map<String, String> globals = ((org.drools.process.core.Process) process).getGlobals();
		if (globals != null) {
			for (Map.Entry<String, String> entry: globals.entrySet()) {
				result += "global " + entry.getValue() + " " + entry.getKey() + "\n";
			}
		}
		return result;
	}
	
	public Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		layout.numColumns = 1;
		tabFolder = new TabFolder(parent, SWT.NONE);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		tabFolder.setLayoutData(gd);
		TabItem textEditorTab = new TabItem(tabFolder, SWT.NONE);
		textEditorTab.setText("Globals");
		textEditorTab.setControl(createTextualEditor(tabFolder));
		return tabFolder;
	}
	
	protected void okPressed() {
		success = true;
		updateGlobals();
		super.okPressed();
	}

	public boolean isSuccess() {
		return success;
	}

	public Map<String, String> getGlobals() {
		return globals;
	}
	
	private void updateGlobals() {
		this.globals = new HashMap<String, String>();
		Matcher matcher = GLOBAL_PATTERN.matcher(globalsViewer.getDocument().get());
		while (matcher.find()) {
			this.globals.put(matcher.group(2), matcher.group(1));
		}
	}
}
