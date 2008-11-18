package org.drools.eclipse.editors;

import java.util.HashMap;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.editors.outline.RuleContentOutlinePage;
import org.drools.eclipse.editors.scanners.RuleEditorMessages;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Abstract text-based rule editor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class AbstractRuleEditor extends TextEditor {

	protected RuleContentOutlinePage ruleContentOutline = null;

    protected Annotation[] oldAnnotations;
    protected ProjectionAnnotationModel annotationModel;
	protected DroolsPairMatcher bracketMatcher = new DroolsPairMatcher();
	
	public AbstractRuleEditor() {
		setSourceViewerConfiguration(createSourceViewerConfiguration());
		setDocumentProvider(createDocumentProvider());
		getPreferenceStore().setDefault(IDroolsConstants.DRL_EDITOR_MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(getPreferenceStore(), IDroolsConstants.DRL_EDITOR_MATCHING_BRACKETS_COLOR, new RGB(192, 192, 192));
    }
	
	protected SourceViewerConfiguration createSourceViewerConfiguration() {
		return new DRLSourceViewerConfig(this); 
	}

    protected IDocumentProvider createDocumentProvider() {
    	return new DRLDocumentProvider();
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
		if (annotationModel != null) {
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
	
    protected ContentOutlinePage getContentOutline() {
        if (ruleContentOutline == null) {
            ruleContentOutline = new RuleContentOutlinePage(this);
            ruleContentOutline.update();
        }
        return ruleContentOutline;
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
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IContentOutlinePage.class)) {
			return getContentOutline();
		}
		return super.getAdapter(adapter);
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
	}

	public void dispose() {
		super.dispose();
		if (bracketMatcher != null) {
			bracketMatcher.dispose();
			bracketMatcher = null;
		}
	}
}
