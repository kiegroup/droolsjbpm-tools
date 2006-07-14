package org.drools.ide.editors;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.Reader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DrlParser;
import org.drools.compiler.PackageBuilder;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.builder.DroolsBuilder;
import org.drools.ide.util.ProjectClassLoader;
import org.drools.lang.descr.PackageDescr;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.rule.Package;
import org.drools.visualize.ReteooJungViewerPanel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class ReteViewer extends EditorPart {

	private DRLRuleEditor2 drlEditor;

	private IDocumentProvider documentProvider;

	private Frame frame;

	private Composite parent;

	public ReteViewer(DRLRuleEditor2 drlEditor,
			IDocumentProvider documentProvider) {
		this.drlEditor = drlEditor;
		this.documentProvider = documentProvider;
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
		if (getEditorInput() instanceof IFileEditorInput) {
			try {
				String contents = documentProvider.getDocument(getEditorInput()).get();

	            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
	            ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
	            IFile file = ((IFileEditorInput) getEditorInput()).getFile();
	            if (file.getProject().getNature("org.eclipse.jdt.core.javanature") != null) {
	                IJavaProject project = JavaCore.create(file.getProject());
	                newLoader = ProjectClassLoader.getProjectClassLoader(project);
	            }
	            
	            Reader dslReader = DSLAdapter.getDSLContent(contents, file);
	            
	            try {
	                Thread.currentThread().setContextClassLoader(newLoader);

	                DrlParser parser = new DrlParser();
	                
	                PackageDescr packageDescr = null;
	                if (dslReader == null) {
	                	packageDescr = parser.parse(contents);
	                } else {
	                	packageDescr = parser.parse(contents, dslReader);
	                }

					//pre build the package
					PackageBuilder builder = new PackageBuilder();
					builder.addPackage(packageDescr);
					Package pkg = builder.getPackage();

					//add the package to a rulebase
                    RuleBase ruleBase = RuleBaseFactory.newRuleBase();
					ruleBase.addPackage(pkg);
					return ruleBase;
					
	            } catch (Exception t) {
	                throw t;
	            } finally {
	                Thread.currentThread().setContextClassLoader(oldLoader);
	            }
			} catch (Throwable t) {
				t.printStackTrace();
			}
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
					ReteooJungViewerPanel viewer = new ReteooJungViewerPanel(
							ruleBase);
					frame.add(viewer);
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
