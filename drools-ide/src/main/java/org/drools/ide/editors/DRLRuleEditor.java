package org.drools.ide.editors;

import java.util.HashMap;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.debug.core.IDroolsDebugConstants;
import org.drools.ide.editors.outline.RuleContentOutlinePage;
import org.drools.ide.editors.scanners.RuleEditorMessages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
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
import org.eclipse.ui.editors.text.TextEditor;
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

	public void setImports(List imports) {
		this.imports = imports;
	}

	public List getImports() {
		return imports;
	}

	public void setFunctions(List functions) {
		this.functions = functions;
	}

	public List getFunctions() {
		return functions;
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
