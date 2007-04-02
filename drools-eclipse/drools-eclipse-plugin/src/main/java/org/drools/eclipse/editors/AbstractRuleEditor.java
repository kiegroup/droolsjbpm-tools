package org.drools.eclipse.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.compiler.DroolsParserException;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.debug.core.IDroolsDebugConstants;
import org.drools.eclipse.editors.outline.RuleContentOutlinePage;
import org.drools.eclipse.editors.scanners.RuleEditorMessages;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.FunctionImportDescr;
import org.drools.lang.descr.ImportDescr;
import org.drools.lang.descr.PackageDescr;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Generic rule editor for drools.
 * @author Michael Neale
 */
public abstract class AbstractRuleEditor extends TextEditor {

	//used to provide additional content assistance/popups when DSLs are used.
    protected DSLAdapter dslAdapter;
    protected List imports;
    protected List functions;
    protected Map templates;
    protected String packageName;
    protected List classesInPackage;
	protected RuleContentOutlinePage ruleContentOutline = null;

    protected Annotation[] oldAnnotations;
    protected ProjectionAnnotationModel annotationModel;
	protected DroolsPairMatcher bracketMatcher = new DroolsPairMatcher();
	
	public AbstractRuleEditor() {
        super();
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
	
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		support.setCharacterPairMatcher(bracketMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(
			IDroolsConstants.DRL_EDITOR_MATCHING_BRACKETS,
			IDroolsConstants.DRL_EDITOR_MATCHING_BRACKETS_COLOR);
		super.configureSourceViewerDecorationSupport(support);
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
		
		a = new ToggleBreakpointAction(getSite().getPart() , null, getVerticalRuler());
		setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, a);

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
	
	public String getContent() {
		return getSourceViewer().getDocument().get();
	}
	
	public IResource getResource() {
		if (getEditorInput() instanceof IFileEditorInput) {
			return ((IFileEditorInput) getEditorInput()).getFile();
		}
		return null;
	}
	
	protected abstract void loadImportsAndFunctions();

	public List getFunctions() {
		if (functions == null) {
			loadImportsAndFunctions();
		}
		return functions;
	}
	
	public Set getTemplates() {
		if (templates == null) {
			loadImportsAndFunctions();
		}
		return templates.keySet();
	}
	
	public FactTemplateDescr getTemplate(String name) {
		if (templates == null) {
			loadImportsAndFunctions();
		}
		return (FactTemplateDescr) templates.get(name);
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
	
	protected List getAllClassesInPackage(String packageName) {
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
					DroolsEclipsePlugin.log(t);
				}
			}
		}
		return list;
	}

	protected List getAllStaticMethodsInClass(String className) {
		final List list = new ArrayList();
		if (className != null) {
			IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput) {
				IProject project = ((IFileEditorInput) input).getFile().getProject();
				IJavaProject javaProject = JavaCore.create(project);
				
				CompletionRequestor requestor = new CompletionRequestor() {
					public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
						String functionName = new String(proposal.getCompletion());
						if (proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.METHOD_REF) {
							list.add(functionName.substring(0, functionName.length() - 2)); // remove the ()
						}
						// ignore all other proposals
					}
				};
	
				try {
					javaProject.newEvaluationContext().codeComplete(className + ".", className.length() + 1, requestor);
				} catch (Throwable t) {
					DroolsEclipsePlugin.log(t);
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

	protected abstract ContentOutlinePage getContentOutline();

	private Object getBreakpointAdapter() {
		return new DroolsLineBreakpointAdapter();
	}

	public void doSave(IProgressMonitor monitor) {
		// invalidate cached parsed rules
		DroolsEclipsePlugin.getDefault().invalidateResource(getResource());
		// save
		super.doSave(monitor);
		// update outline view
		if (ruleContentOutline != null) {
			ruleContentOutline.update();
		}
		// remove cached content
		dslAdapter = null;
		imports = null;
		functions = null;
		templates = null;
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
	                	DroolsEclipsePlugin.log(exc);
	                }
			} else {
				super.gotoMarker(marker);
			}
		} catch (CoreException exc) {
			DroolsEclipsePlugin.log(exc);
		}
	}
	
	public void dispose() {
		super.dispose();
		if (bracketMatcher != null) {
			bracketMatcher.dispose();
			bracketMatcher = null;
		}
	}
}
