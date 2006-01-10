package org.drools.ide.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.debug.actions.DroolsShowDetailPaneAction;
import org.drools.ide.debug.actions.ShowLogicalStructureAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.VariablesViewModelPresentation;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.internal.ui.views.variables.ViewerState;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A generic Drools debug view.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsDebugEventHandlerView extends AbstractDebugEventHandlerView
        implements ISelectionListener, IPropertyChangeListener,
        IValueDetailListener, IDebugExceptionHandler, INullSelectionListener {

    private static final String DETAIL_SELECT_ALL_ACTION = SELECT_ALL_ACTION + ".Detail";
    private static final String VARIABLES_SELECT_ALL_ACTION = SELECT_ALL_ACTION + ".Variables";
    private static final String DETAIL_COPY_ACTION = ITextEditorActionConstants.COPY + ".Detail";
    private static final String VARIABLES_COPY_ACTION = ITextEditorActionConstants.COPY + ".Variables";

    private VariablesViewModelPresentation fModelPresentation;
    private SashForm fSashForm;
    private ISourceViewer fDetailViewer;
    private IDocument fDetailDocument;
    private String fDebugModelIdentifier;
    private SourceViewerConfiguration fSourceViewerConfiguration;
    private IStructuredSelection fValueSelection = null;
    private IValue fLastValueDetail = null;
    private Iterator fSelectionIterator = null;
    private ISelectionChangedListener fTreeSelectionChangedListener;
    private ISelectionChangedListener fDetailSelectionChangedListener;
    private IDocumentListener fDetailDocumentListener;
    private VariablesViewSelectionProvider fSelectionProvider = new VariablesViewSelectionProvider();
    private List fSelectionActions = new ArrayList(3);
    private Map<Object, AbstractViewerState> fSelectionStates = new HashMap<Object, AbstractViewerState>(10);
    private AbstractViewerState fLastState = null;
    private HashMap fExpandedVariables = new HashMap(10);
    private Viewer fFocusViewer = null;
    private static final int[] DEFAULT_SASH_WEIGHTS = { 6, 2 };
    private int[] fLastSashWeights;
    private boolean fToggledDetailOnce;
    private boolean showLogical = false;

    public void dispose() {
        getSite().getPage().removeSelectionListener(
            IDebugUIConstants.ID_DEBUG_VIEW, this);
        DebugUIPlugin.getDefault().getPreferenceStore()
            .removePropertyChangeListener(this);
        JFaceResources.getFontRegistry().removeListener(this);
        Viewer viewer = getViewer();
        if (viewer != null) {
            getDetailDocument().removeDocumentListener(
                    getDetailDocumentListener());
        }
        super.dispose();
    }

    public boolean isShowLogicalStructure() {
        return showLogical;
    }

    public void setShowLogicalStructure(boolean showLogical) {
        this.showLogical = showLogical;
    }

    protected void setViewerInput(IStructuredSelection ssel) {
        IStackFrame frame = null;
        if (ssel.size() == 1) {
            Object input = ssel.getFirstElement();
            if (input instanceof IStackFrame) {
                frame = (IStackFrame) input;
            }
        }

        getDetailViewer().setEditable(frame != null);

        Object current = getViewer().getInput();

        if (current == null && frame == null) {
            return;
        }

        if (current != null && current.equals(frame)) {
            return;
        }

        if (current != null) {
            fLastState = getViewerState();
            fSelectionStates.put(current, fLastState);
        }

        if (frame != null) {
            setDebugModel(frame.getModelIdentifier());
        }
        showViewer();
        getViewer().setInput(frame);

        if (frame != null) {
            AbstractViewerState state = (AbstractViewerState) fSelectionStates
                    .get(frame);
            if (state == null) {
                state = fLastState;
            }
            if (state != null) {
                state.restoreState(getVariablesViewer());
            }
        }
    }

    protected DroolsVariablesViewer getVariablesViewer() {
        return (DroolsVariablesViewer) getViewer();
    }

    protected AbstractViewerState getViewerState() {
        return new ViewerState(getVariablesViewer());
    }

    protected void clearExpandedVariables(Object parent) {
        List list = null;
        if (parent instanceof IThread) {
            list = getCachedFrames((IThread) parent);
        } else if (parent instanceof IDebugTarget) {
            list = getCachedFrames((IDebugTarget) parent);
        }
        if (list != null) {
            Iterator frames = list.iterator();
            while (frames.hasNext()) {
                Object frame = frames.next();
                fExpandedVariables.remove(frame);
            }
        }
    }

    protected List<IStackFrame> getCachedFrames(IThread thread) {
        List<IStackFrame> list = null;
        Iterator frames = fExpandedVariables.keySet().iterator();
        while (frames.hasNext()) {
            IStackFrame frame = (IStackFrame) frames.next();
            if (frame.getThread().equals(thread)) {
                if (list == null) {
                    list = new ArrayList<IStackFrame>();
                }
                list.add(frame);
            }
        }
        return list;
    }

    protected List<IStackFrame> getCachedFrames(IDebugTarget target) {
        List<IStackFrame> list = null;
        Iterator frames = fExpandedVariables.keySet().iterator();
        while (frames.hasNext()) {
            IStackFrame frame = (IStackFrame) frames.next();
            if (frame.getDebugTarget().equals(target)) {
                if (list == null) {
                    list = new ArrayList<IStackFrame>();
                }
                list.add(frame);
            }
        }
        return list;
    }

    protected void configureDetailsViewer() {
        LazyModelPresentation mp = (LazyModelPresentation) fModelPresentation
                .getPresentation(getDebugModel());
        SourceViewerConfiguration svc = null;
        if (mp != null) {
            try {
                svc = mp.newDetailsViewerConfiguration();
            } catch (CoreException e) {
                DebugUIPlugin.errorDialog(getSite().getShell(),
                    "Drools Debug View Error", "Unable to configure drools debug view", e);
            }
        }
        if (svc == null) {
            svc = new SourceViewerConfiguration();
            getDetailViewer().setEditable(false);
        }
        getDetailViewer().configure(svc);
        updateAction("ContentAssist");
        setDetailViewerConfiguration(svc);
    }

    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getProperty();
        if (propertyName
                .equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION)) {
            setDetailPaneOrientation(DebugUIPlugin
                    .getDefault()
                    .getPreferenceStore()
                    .getString(
                            IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION));
        } else if (propertyName
                .equals(IDebugPreferenceConstants.CHANGED_VARIABLE_COLOR)) {
            getEventHandler().refresh();
        } else if (propertyName
                .equals(IInternalDebugUIConstants.DETAIL_PANE_FONT)) {
            getDetailViewer()
                    .getTextWidget()
                    .setFont(
                            JFaceResources
                                    .getFont(IInternalDebugUIConstants.DETAIL_PANE_FONT));
        }
    }

    public Viewer createViewer(Composite parent) {
        fModelPresentation = new VariablesViewModelPresentation();
        DebugUIPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);
        JFaceResources.getFontRegistry().addListener(this);

        fSashForm = new SashForm(parent, SWT.NONE);
        IPreferenceStore prefStore = DebugUIPlugin.getDefault()
                .getPreferenceStore();
        String orientString = prefStore
                .getString(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION);
        setDetailPaneOrientation(orientString);

        final TreeViewer variablesViewer = new DroolsVariablesViewer(getSashForm(),
                SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, this);
        variablesViewer.setContentProvider(createContentProvider());
        variablesViewer.setLabelProvider(new VariablesViewLabelProvider(
                getModelPresentation()));
        variablesViewer.setUseHashlookup(true);
        variablesViewer.getControl().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                getVariablesViewSelectionProvider()
                        .setUnderlyingSelectionProvider(variablesViewer);
                setAction(SELECT_ALL_ACTION,
                        getAction(VARIABLES_SELECT_ALL_ACTION));
                setAction(COPY_ACTION, getAction(VARIABLES_COPY_ACTION));
                getViewSite().getActionBars().updateActionBars();
                setFocusViewer(getVariablesViewer());
            }
        });
        variablesViewer
                .addSelectionChangedListener(getTreeSelectionChangedListener());
        getVariablesViewSelectionProvider().setUnderlyingSelectionProvider(
                variablesViewer);
        getSite().setSelectionProvider(getVariablesViewSelectionProvider());

        SourceViewer detailsViewer = new SourceViewer(getSashForm(), null,
                SWT.V_SCROLL | SWT.H_SCROLL);
        setDetailViewer(detailsViewer);
        detailsViewer.setDocument(getDetailDocument());
        detailsViewer.getTextWidget().setFont(
                JFaceResources
                        .getFont(IInternalDebugUIConstants.DETAIL_PANE_FONT));
        getDetailDocument().addDocumentListener(getDetailDocumentListener());
        detailsViewer.setEditable(false);
        getSashForm().setMaximizedControl(variablesViewer.getControl());

        detailsViewer.getSelectionProvider().addSelectionChangedListener(
                getDetailSelectionChangedListener());

        detailsViewer.getControl().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                getVariablesViewSelectionProvider()
                        .setUnderlyingSelectionProvider(
                                getDetailViewer().getSelectionProvider());
                setAction(SELECT_ALL_ACTION,
                        getAction(DETAIL_SELECT_ALL_ACTION));
                setAction(COPY_ACTION, getAction(DETAIL_COPY_ACTION));
                getViewSite().getActionBars().updateActionBars();
                setFocusViewer((Viewer) getDetailViewer());
            }
        });
        createDetailContextMenu(detailsViewer.getTextWidget());

        getSite().getPage().addSelectionListener(
                IDebugUIConstants.ID_DEBUG_VIEW, this);
        setEventHandler(createEventHandler(variablesViewer));

        return variablesViewer;
    }

    protected IContentProvider createContentProvider() {
        return null;
    }

    protected AbstractDebugEventHandler createEventHandler(Viewer viewer) {
        return new DroolsDebugViewEventHandler(this);
    }

    protected String getHelpContextId() {
        return IDebugHelpContextIds.VARIABLE_VIEW;
    }

    protected void setDetailPaneOrientation(String value) {
        int orientation = value
                .equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH) ? SWT.VERTICAL
                : SWT.HORIZONTAL;
        getSashForm().setOrientation(orientation);
    }

    public void toggleDetailPane(boolean on) {
        if (on) {
            getSashForm().setMaximizedControl(null);
            getSashForm().setWeights(getLastSashWeights());
            populateDetailPane();
            fToggledDetailOnce = true;
        } else {
            if (fToggledDetailOnce) {
                setLastSashWeights(getSashForm().getWeights());
            }
            getSashForm().setMaximizedControl(getViewer().getControl());
        }
    }

    public void toggleDetailPaneWordWrap(boolean on) {
        fDetailViewer.getTextWidget().setWordWrap(on);
    }

    public void populateDetailPane() {
        if (isDetailPaneVisible()) {
            IStructuredSelection selection = (IStructuredSelection) getViewer()
                    .getSelection();
            populateDetailPaneFromSelection(selection);
        }
    }

    protected int[] getLastSashWeights() {
        if (fLastSashWeights == null) {
            fLastSashWeights = DEFAULT_SASH_WEIGHTS;
        }
        return fLastSashWeights;
    }

    protected void setLastSashWeights(int[] weights) {
        fLastSashWeights = weights;
    }

    protected void setInitialContent() {
        ISelection selection = getSite().getPage().getSelection(
                IDebugUIConstants.ID_DEBUG_VIEW);
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            setViewerInput((IStructuredSelection) selection);
        }
    }

    protected void createDetailContextMenu(Control menuControl) {
        MenuManager menuMgr = new MenuManager(); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                fillDetailContextMenu(mgr);
            }
        });
        Menu menu = menuMgr.createContextMenu(menuControl);
        menuControl.setMenu(menu);

        // register the context menu such that other plugins may contribute to
        // it
        getSite().registerContextMenu(
                IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID, menuMgr,
                getDetailViewer().getSelectionProvider());
        addContextMenuManager(menuMgr);
    }

    protected void createActions() {
        IAction action = new DroolsShowDetailPaneAction(this);
        setAction("ShowDetailPane", action);
        action = new ShowLogicalStructureAction(this);
        setAction("ShowLogicalStructure", action);
        setInitialContent();
    }

    protected void configureToolBar(IToolBarManager tbm) {
        tbm.add(getAction("ShowDetailPane"));
        tbm.add(getAction("ShowLogicalStructure"));
    }

    protected void fillContextMenu(IMenuManager menu) {
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    protected void fillDetailContextMenu(IMenuManager menu) {
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    protected ISelectionChangedListener getTreeSelectionChangedListener() {
        if (fTreeSelectionChangedListener == null) {
            fTreeSelectionChangedListener = new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    if (event.getSelectionProvider().equals(
                            getVariablesViewer())) {
                        clearStatusLine();
                        getVariablesViewSelectionProvider()
                                .fireSelectionChanged(event);
                        // if the detail pane is not visible, don't waste time
                        // retrieving details
                        if (getSashForm().getMaximizedControl() == getViewer()
                                .getControl()) {
                            return;
                        }
                        IStructuredSelection selection = (IStructuredSelection) event
                                .getSelection();
                        populateDetailPaneFromSelection(selection);
                        treeSelectionChanged(event);
                    }
                }
            };
        }
        return fTreeSelectionChangedListener;
    }

    protected void treeSelectionChanged(SelectionChangedEvent event) {
    }

    protected void populateDetailPaneFromSelection(
            IStructuredSelection selection) {
        try {
            getDetailDocument().set("");
            if (!selection.isEmpty()) {
                IValue val = null;
                Object obj = selection.getFirstElement();
                if (obj instanceof IVariable) {
                    val = ((IVariable) obj).getValue();
                } else if (obj instanceof IExpression) {
                    val = ((IExpression) obj).getValue();
                }
                if (val == null) {
                    return;
                }
                if (fValueSelection != null && fValueSelection.equals(selection)) {
                    return;
                }

                setDebugModel(val.getModelIdentifier());
                fValueSelection = selection;
                fSelectionIterator = selection.iterator();
                fSelectionIterator.next();
                fLastValueDetail = val;
                getModelPresentation().computeDetail(val, this);
            }
        } catch (DebugException de) {
            DroolsIDEPlugin.log(de);
            getDetailDocument().set("Error occurred retrieving value");
        }
    }

    public void detailComputed(final IValue value, final String result) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (isAvailable()) {
                    // bug 24862
                    // don't display the result if an other detail has been
                    // requested
                    if (value == fLastValueDetail) {
                        String insert = result;
                        int length = getDetailDocument().get().length();
                        if (length > 0) {
                            insert = "\n" + result; //$NON-NLS-1$
                        }
                        try {
                            getDetailDocument().replace(length, 0, insert);
                        } catch (BadLocationException e) {
                            DroolsIDEPlugin.log(e);
                        }
                        fLastValueDetail = null;
                    }

                    if (fSelectionIterator != null
                            && fSelectionIterator.hasNext()) {
                        Object obj = fSelectionIterator.next();
                        IValue val = null;
                        try {
                            if (obj instanceof IVariable) {
                                val = ((IVariable) obj).getValue();
                            } else if (obj instanceof IExpression) {
                                val = ((IExpression) obj).getValue();
                            }
                            fLastValueDetail = val;
                            getModelPresentation().computeDetail(val,
                                    DroolsDebugEventHandlerView.this);
                        } catch (DebugException e) {
                            DroolsIDEPlugin.log(e);
                            getDetailDocument().set("Error occurred retrieving value");    
                        }
                    } else {
                        fValueSelection = null;
                        fSelectionIterator = null;
                    }
                }
            }
        };
        asyncExec(runnable);
    }

    protected ISelectionChangedListener getDetailSelectionChangedListener() {
        if (fDetailSelectionChangedListener == null) {
            fDetailSelectionChangedListener = new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    if (event.getSelectionProvider().equals(
                            getVariablesViewSelectionProvider()
                                    .getUnderlyingSelectionProvider())) {
                        getVariablesViewSelectionProvider()
                                .fireSelectionChanged(event);
                        updateSelectionDependentActions();
                    }
                }
            };
        }
        return fDetailSelectionChangedListener;
    }

    protected IDocumentListener getDetailDocumentListener() {
        if (fDetailDocumentListener == null) {
            fDetailDocumentListener = new IDocumentListener() {
                public void documentAboutToBeChanged(DocumentEvent event) {
                }

                public void documentChanged(DocumentEvent event) {
                    updateAction(ITextEditorActionConstants.FIND);
                }
            };
        }
        return fDetailDocumentListener;
    }

    protected IDocument getDetailDocument() {
        if (fDetailDocument == null) {
            fDetailDocument = new Document();
        }
        return fDetailDocument;
    }

    protected IDebugModelPresentation getModelPresentation() {
        if (fModelPresentation == null) {
            fModelPresentation = new VariablesViewModelPresentation();
        }
        return fModelPresentation;
    }

    private void setDetailViewer(ISourceViewer viewer) {
        fDetailViewer = viewer;
    }

    protected ISourceViewer getDetailViewer() {
        return fDetailViewer;
    }

    protected SashForm getSashForm() {
        return fSashForm;
    }

    public Object getAdapter(Class required) {
        if (IFindReplaceTarget.class.equals(required)) {
            return getDetailViewer().getFindReplaceTarget();
        }
        if (ITextViewer.class.equals(required)) {
            return getDetailViewer();
        }
        if (IDebugModelPresentation.class.equals(required)) {
            IBaseLabelProvider labelProvider = getStructuredViewer()
                    .getLabelProvider();
            if (labelProvider instanceof VariablesViewLabelProvider) {
                return ((VariablesViewLabelProvider) labelProvider)
                        .getPresentation();
            }
        }
        return super.getAdapter(required);
    }

    protected void updateSelectionDependentActions() {
        Iterator iterator = fSelectionActions.iterator();
        while (iterator.hasNext()) {
            updateAction((String) iterator.next());
        }
    }

    protected void updateAction(String actionId) {
        IAction action = getAction(actionId);
        if (action instanceof IUpdate) {
            ((IUpdate) action).update();
        }
    }

    protected boolean isDetailPaneVisible() {
        IAction action = getAction("ShowDetailPane"); //$NON-NLS-1$
        return action != null && action.isChecked();
    }

    /**
     * Sets the identifier of the debug model being displayed in this view, or
     * <code>null</code> if none.
     * 
     * @param id
     *            debug model identifier of the type of debug elements being
     *            displayed in this view
     */
    protected void setDebugModel(String id) {
        if (id != fDebugModelIdentifier) {
            fDebugModelIdentifier = id;
            configureDetailsViewer();
        } else {
            updateAction("ContentAssist"); //$NON-NLS-1$
        }
    }

    /**
     * Returns the identifier of the debug model being displayed in this view,
     * or <code>null</code> if none.
     * 
     * @return debug model identifier
     */
    protected String getDebugModel() {
        return fDebugModelIdentifier;
    }

    /**
     * Sets the current configuration being used in the details area.
     * 
     * @param config
     *            source viewer configuration
     */
    private void setDetailViewerConfiguration(SourceViewerConfiguration config) {
        fSourceViewerConfiguration = config;
    }

    /**
     * Returns the current configuration being used in the details area.
     * 
     * @return source viewer configuration
     */
    protected SourceViewerConfiguration getDetailViewerConfiguration() {
        return fSourceViewerConfiguration;
    }

    /**
     * @see AbstractDebugView#getDefaultControl()
     */
    protected Control getDefaultControl() {
        return getSashForm();
    }

    /**
     * @see IDebugExceptionHandler#handleException(DebugException)
     */
    public void handleException(DebugException e) {
        showMessage(e.getMessage());
    }

    protected VariablesViewSelectionProvider getVariablesViewSelectionProvider() {
        return fSelectionProvider;
    }

    /**
     * The <code>VariablesView</code> listens for selection changes in the
     * <code>LaunchView</code>
     * 
     * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!isAvailable()) {
            return;
        }

        if (selection == null) {
            setViewerInput(new StructuredSelection());
        } else if (selection instanceof IStructuredSelection) {
            setViewerInput((IStructuredSelection) selection);
        } else {
            getDetailViewer().setEditable(false);
        }
        updateAction("ContentAssist");
    }

    public void doubleClick(DoubleClickEvent event) {
        IAction action = getAction(DOUBLE_CLICK_ACTION);
        if (action != null && action.isEnabled()) {
            action.run();
        } else {
            ISelection selection = event.getSelection();
            if (!(selection instanceof IStructuredSelection)) {
                return;
            }
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object o = ss.getFirstElement();

            TreeViewer tViewer = (TreeViewer) getViewer();
            boolean expanded = tViewer.getExpandedState(o);
            tViewer.setExpandedState(o, !expanded);
        }
    }

    public void setFocus() {
        if (getFocusViewer() == null) {
            super.setFocus();
        } else {
            getFocusViewer().getControl().setFocus();
        }
    }

    protected void setFocusViewer(Viewer viewer) {
        fFocusViewer = viewer;
    }

    protected Viewer getFocusViewer() {
        return fFocusViewer;
    }

    public IDebugModelPresentation getPresentation(String id) {
        if (getViewer() instanceof StructuredViewer) {
            VariablesViewLabelProvider vvlp = (VariablesViewLabelProvider) ((StructuredViewer) getViewer())
                    .getLabelProvider();
            IDebugModelPresentation lp = vvlp.getPresentation();
            if (lp instanceof DelegatingModelPresentation) {
                return ((DelegatingModelPresentation) lp).getPresentation(id);
            }
            if (lp instanceof LazyModelPresentation) {
                if (((LazyModelPresentation) lp).getDebugModelIdentifier()
                        .equals(id)) {
                    return (IDebugModelPresentation) lp;
                }
            }
        }
        return null;
    }

    public void psetViewerInput(IStructuredSelection ssel) {
        setViewerInput(ssel);
    }

    public void pclearExpandedVariables(Object parent) {
        clearExpandedVariables(parent);
    }

    class VariablesViewLabelProvider implements ILabelProvider, IColorProvider {

        private IDebugModelPresentation presentation;

        public VariablesViewLabelProvider(IDebugModelPresentation presentation) {
            this.presentation = presentation;
        }

        public IDebugModelPresentation getPresentation() {
            return presentation;
        }

        public Image getImage(Object element) {
            return presentation.getImage(element);
        }

        public String getText(Object element) {
            return presentation.getText(element);
        }

        public void addListener(ILabelProviderListener listener) {
            presentation.addListener(listener);
        }

        public void dispose() {
            presentation.dispose();
        }

        public boolean isLabelProperty(Object element, String property) {
            return presentation.isLabelProperty(element, property);
        }

        public void removeListener(ILabelProviderListener listener) {
            presentation.removeListener(listener);
        }

        public Color getForeground(Object element) {
            if (element instanceof IVariable) {
                IVariable variable = (IVariable) element;
                try {
                    if (variable.hasValueChanged()) {
                        return JFaceResources.getColorRegistry()
                            .get(IDebugPreferenceConstants.CHANGED_VARIABLE_COLOR);
                    }
                } catch (DebugException e) {
                    DroolsIDEPlugin.log(e);
                }
            }
            return null;
        }

        public Color getBackground(Object element) {
            return null;
        }

    }

    class VariablesViewSelectionProvider implements ISelectionProvider {
        private ListenerList fListeners = new ListenerList();

        private ISelectionProvider fUnderlyingSelectionProvider;

        public void addSelectionChangedListener(
                ISelectionChangedListener listener) {
            fListeners.add(listener);
        }

        public ISelection getSelection() {
            return getUnderlyingSelectionProvider().getSelection();
        }

        public void removeSelectionChangedListener(
                ISelectionChangedListener listener) {
            fListeners.remove(listener);
        }

        public void setSelection(ISelection selection) {
            getUnderlyingSelectionProvider().setSelection(selection);
        }

        protected ISelectionProvider getUnderlyingSelectionProvider() {
            return fUnderlyingSelectionProvider;
        }

        protected void setUnderlyingSelectionProvider(
                ISelectionProvider underlyingSelectionProvider) {
            fUnderlyingSelectionProvider = underlyingSelectionProvider;
        }

        protected void fireSelectionChanged(SelectionChangedEvent event) {
            Object[] listeners = fListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                ISelectionChangedListener listener = (ISelectionChangedListener) listeners[i];
                listener.selectionChanged(event);
            }
        }
    }

}
