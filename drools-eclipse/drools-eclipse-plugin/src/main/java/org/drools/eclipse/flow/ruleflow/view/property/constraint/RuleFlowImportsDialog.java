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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.eclipse.editors.DRLSourceViewerConfig;
import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.drools.workflow.core.WorkflowProcess;
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
public class RuleFlowImportsDialog extends Dialog {

	private static final Pattern IMPORT_PATTERN = Pattern.compile(
		"\\n\\s*import\\s+([^\\s;#]+);?", Pattern.DOTALL);
	private static final Pattern FUNCTION_IMPORT_PATTERN = Pattern.compile(
			"\\n\\s*import\\s+function\\s+([^\\s;#]+);?", Pattern.DOTALL);
	
	private WorkflowProcess process;
	private boolean success;
	private TabFolder tabFolder;
	private SourceViewer importsViewer;
	private List<String> imports;
	private List<String> functionImports;

	public RuleFlowImportsDialog(Shell parentShell, WorkflowProcess process) {
		super(parentShell);
		this.process = process;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Imports editor");
	}

	protected Point getInitialSize() {
		return new Point(600, 450);
	}

	private Control createTextualEditor(Composite parent) {
		importsViewer = new SourceViewer(parent, null, SWT.BORDER);
		importsViewer.configure(new DRLSourceViewerConfig(null) {
			public IReconciler getReconciler(ISourceViewer sourceViewer) {
				return null;
			}
			public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
				ContentAssistant assistant = new ContentAssistant();
				IContentAssistProcessor completionProcessor = new ImportCompletionProcessor();
				assistant.setContentAssistProcessor(
					completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
				assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
				return assistant;
			}
		});
		IDocument document = new Document(getProcessImports());
		importsViewer.setDocument(document);
		IDocumentPartitioner partitioner =
            new FastPartitioner(
                new DRLPartionScanner(),
                DRLPartionScanner.LEGAL_CONTENT_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
        importsViewer.getControl().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.character == ' ' && e.stateMask == SWT.CTRL) {
					importsViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				}
			}
			public void keyReleased(KeyEvent e) {
			}
        });
		return importsViewer.getControl();
	}
	
	private String getProcessImports() {
		String result = "# define your imports here: e.g. import com.sample.MyClass\n";
		List<String> imports = process.getImports();
		if (imports != null) {
			for (String importString: imports) {
				result += "import " + importString + "\n";
			}
		}
		imports = process.getFunctionImports();
		if (imports != null) {
			for (String importString: imports) {
				result += "import function " + importString + "\n";
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
		textEditorTab.setText("Imports");
		textEditorTab.setControl(createTextualEditor(tabFolder));
		return tabFolder;
	}
	
	protected void okPressed() {
		success = true;
		updateImports();
		super.okPressed();
	}

	public boolean isSuccess() {
		return success;
	}

	public List<String> getImports() {
		return imports;
	}
	
	public List<String> getFunctionImports() {
		return functionImports;
	}
	
	private void updateImports() {
		this.imports = new ArrayList<String>();
		Matcher matcher = IMPORT_PATTERN.matcher(importsViewer.getDocument().get());
		while (matcher.find()) {
			String importString = matcher.group(1);
			if (!"function".equals(importString)) {
				this.imports.add(importString);
			}
		}
		this.functionImports = new ArrayList<String>();
		matcher = FUNCTION_IMPORT_PATTERN.matcher(importsViewer.getDocument().get());
		while (matcher.find()) {
			this.functionImports.add(matcher.group(1));
		}
	}
}
