package org.drools.ide.editors;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.builder.DroolsBuilder;
import org.drools.ide.debug.core.IDroolsDebugConstants;
import org.drools.ide.editors.outline.RuleContentOutlinePage;
import org.drools.ide.editors.scanners.RuleEditorMessages;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.PackageDescr;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Generic rule editor for drools.
 * @author Michael Neale
 */
public class DRLRuleEditor extends TextEditor {

	//used to provide additional content assistance/popups when DSLs are used.
	private DSLAdapter dslAdapter;
	private List imports;
	private List functions;
	private String packageName;
	private List classesInPackage;
	private RuleContentOutlinePage ruleContentOutline = null;

	private Annotation[] oldAnnotations;
	private ProjectionAnnotationModel annotationModel;
	
	public DRLRuleEditor() {
		setSourceViewerConfiguration(new DRLSourceViewerConfig(this));
		setDocumentProvider(new DRLDocumentProvider());
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
		ProjectionSupport projectionSupport = new ProjectionSupport(viewer,
			getAnnotationAccess(), getSharedColors());
		projectionSupport.install();
		// turn projection mode on
		viewer.doOperation(ProjectionViewer.TOGGLE);
		annotationModel = viewer.getProjectionAnnotationModel();
	}
	
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

	public void updateFoldingStructure(List positions) {
		Annotation[] annotations = new Annotation[positions.size()];
		// this will hold the new annotations along
		// with their corresponding positions
		HashMap newAnnotations = new HashMap();
		for (int i = 0; i < positions.size(); i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation();
			newAnnotations.put(annotation, positions.get(i));
			annotations[i] = annotation;
		}
		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
		oldAnnotations = annotations;
	}

	/** For user triggered content assistance */
	protected void createActions() {
		super.createActions();

		IAction a = new TextOperationAction(RuleEditorMessages
				.getResourceBundle(), "ContentAssistProposal.", this,
				ISourceViewer.CONTENTASSIST_PROPOSALS);
		a
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a);

		a = new TextOperationAction(
				RuleEditorMessages.getResourceBundle(),
				"ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
		a
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a);
	}

	/** Return the DSL adapter if one is present */
	public DSLAdapter getDSLAdapter() {
		return dslAdapter;
	}

	/** Set the DSL adapter, used for content assistance */
	public void setDSLAdapter(DSLAdapter adapter) {
		dslAdapter = adapter;
	}

	public List getImports() {
		if (imports == null) {
			loadImportsAndFunctions();
		}
		return imports;
	}
	
	private void loadImportsAndFunctions() {
		try {
			String content = getSourceViewer().getDocument().get();
	        Reader dslReader = DSLAdapter.getDSLContent(content, ((FileEditorInput) getEditorInput()).getFile());
	        DrlParser parser = new DrlParser();
	        PackageDescr descr = DroolsBuilder.parsePackage(content, parser, dslReader);
	        // package
	        this.packageName = descr.getName();
	        // imports
	        List allImports = descr.getImports();
	        this.imports = new ArrayList();
	        Iterator iterator = allImports.iterator();
	        while (iterator.hasNext()) {
				String importName = (String) iterator.next();
				if (importName.endsWith(".*")) {
					String packageName = importName.substring(0, importName.length() - 2);
					imports.addAll(getAllClassesInPackage(packageName));
				} else {
					imports.add(importName);
				}
			}
	        // functions
	        List functionDescrs = descr.getFunctions();
	        List functions = new ArrayList(functionDescrs.size());
	        iterator = functionDescrs.iterator();
	        while (iterator.hasNext()) {
				functions.add(((FunctionDescr) iterator.next()).getName());
			}
	        this.functions = functions;
		} catch (CoreException exc) {
			DroolsIDEPlugin.log(exc);
		} catch (DroolsParserException exc) {
			// do nothing
		}
	}

	public List getFunctions() {
		if (functions == null) {
			loadImportsAndFunctions();
		}
		return functions;
	}
	
	public String getPackage() {
		if (packageName == null) {
			loadImportsAndFunctions();
		}
		return packageName;
	}
	
	public List getClassesInPackage() {
		if (classesInPackage == null) {
			classesInPackage = getAllClassesInPackage(getPackage());
		}
		return classesInPackage;
	}
	
	private List getAllClassesInPackage(String packageName) {
		final List list = new ArrayList();
		if (packageName != null) {
			IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput) {
				IProject project = ((IFileEditorInput) input).getFile().getProject();
				IJavaProject javaProject = JavaCore.create(project);
				
				CompletionRequestor requestor = new CompletionRequestor() {
					public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
						String className = new String(proposal.getCompletion());
						if (proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.TYPE_REF) {
							list.add(className);
						}
						// ignore all other proposals
					}
				};
	
				try {
					javaProject.newEvaluationContext().codeComplete(packageName + ".", packageName.length() + 1, requestor);
				} catch (Throwable t) {
					DroolsIDEPlugin.log(t);
				}
			}
		}
		return list;
	}


	public Object getAdapter(Class adapter) {
		if (adapter.equals(IContentOutlinePage.class)) {
			return getContentOutline();
		} else if (adapter.equals(IToggleBreakpointsTarget.class)) {
			return getBreakpointAdapter();
		}
		return super.getAdapter(adapter);
	}

	protected ContentOutlinePage getContentOutline() {
		if (ruleContentOutline == null) {
			ruleContentOutline = new RuleContentOutlinePage(this);
			ruleContentOutline.update();
		}
		return ruleContentOutline;
	}

	private Object getBreakpointAdapter() {
		return new DroolsLineBreakpointAdapter();
	}

	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (ruleContentOutline != null) {
			ruleContentOutline.update();
		}
		dslAdapter = null;
		imports = null;
		functions = null;
		packageName = null;
		classesInPackage = null;
	}

	public void gotoMarker(IMarker marker) {
		try {
			if (marker.getType().equals(IDroolsDebugConstants.DROOLS_MARKER_TYPE)) {
				int line = marker.getAttribute(IDroolsDebugConstants.DRL_LINE_NUMBER, -1);
	            if (line > -1)
	            	--line;
	                try {
	                    IDocument document = getDocumentProvider().getDocument(getEditorInput());
	                    selectAndReveal(document.getLineOffset(line), document.getLineLength(line));
	                } catch(BadLocationException exc) {
	                	DroolsIDEPlugin.log(exc);
	                }
			} else {
				super.gotoMarker(marker);
			}
		} catch (CoreException exc) {
			DroolsIDEPlugin.log(exc);
		}
	}
}
