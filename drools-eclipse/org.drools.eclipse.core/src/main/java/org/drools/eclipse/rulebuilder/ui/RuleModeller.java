package org.drools.eclipse.rulebuilder.ui;

import org.drools.guvnor.client.modeldriven.SuggestionCompletionEngine;
import org.drools.guvnor.client.modeldriven.brl.ActionInsertFact;
import org.drools.guvnor.client.modeldriven.brl.ActionRetractFact;
import org.drools.guvnor.client.modeldriven.brl.ActionSetField;
import org.drools.guvnor.client.modeldriven.brl.CompositeFactPattern;
import org.drools.guvnor.client.modeldriven.brl.DSLSentence;
import org.drools.guvnor.client.modeldriven.brl.FactPattern;
import org.drools.guvnor.client.modeldriven.brl.IAction;
import org.drools.guvnor.client.modeldriven.brl.IPattern;
import org.drools.guvnor.client.modeldriven.brl.RuleModel;
import org.drools.eclipse.rulebuilder.editors.RuleEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Main modeling class responsible for Eclipse Forms-based rule builder widget
 * rendering
 * 
 * @author Anton Arhipov
 * @author Ahti Kitsik
 * 
 */
public class RuleModeller {

    private Composite          ifComposite;

    private Composite          thenComposite;

    private Composite          optionsComposite;

    private final ScrolledForm form;

    private final FormToolkit  toolkit;

    private RuleModel          model;

    private boolean            dirty;

    private RuleEditor         editor;

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        editor.dirtyPropertyChanged();
    }

    public RuleModeller(ScrolledForm form,
                        FormToolkit toolkit,
                        RuleModel model,
                        RuleEditor editor) {

        this.form = form;
        this.toolkit = toolkit;
        this.model = model;
        this.editor = editor;

        setTitleAndFont(form);

        ColumnLayout colLayout = new ColumnLayout();
        colLayout.minNumColumns = 1;
        colLayout.maxNumColumns = 1;

        form.getBody().setLayout( colLayout );

        // addToolBar(toolkit, form);

        Shell shell = new Shell( Display.getCurrent() );
        Window conditionPopup = new AddNewConditionDialog( shell,
                                                           this );
        Window actionPopup = new AddNewActionDialog( shell,
                                                     this );

        Window optionsPopup = new RuleAttributesDialog( shell,
                                                        this );

        Section ifSection = createMainSection( form,
                                               toolkit,
                                               "WHEN",
                                               conditionPopup );
        Section thenSection = createMainSection( form,
                                                 toolkit,
                                                 "THEN",
                                                 actionPopup );
        Section optionsSection = createMainSection( form,
                                                    toolkit,
                                                    "(options)",
                                                    optionsPopup );

        ColumnLayout layout = new ColumnLayout();
        layout.minNumColumns = 1;
        layout.maxNumColumns = 1;
        // layout.verticalSpacing = 0;

        ((Composite) (ifSection.getClient())).setLayout( layout );
        ((Composite) (thenSection.getClient())).setLayout( layout );
        ((Composite) (optionsSection.getClient())).setLayout( layout );
        ifSection.setLayout( layout );
        thenSection.setLayout( layout );
        optionsSection.setLayout( layout );

        ifComposite = (Composite) ifSection.getClient();
        thenComposite = (Composite) thenSection.getClient();
        optionsComposite = (Composite) optionsSection.getClient();

    }

	private void setTitleAndFont(ScrolledForm form) {
		form.setText( "Guided rule editor" );
        
		Font systemFont = form.getDisplay().getSystemFont();
		FontData[] exfds = systemFont.getFontData();
        if ( exfds.length > 0 ) {
            FontData fd = exfds[0];
            fd.setHeight( fd.getHeight() + 2 );
            fd.setStyle(SWT.BOLD);
            Font f = new Font( systemFont.getDevice(),
                               fd );
            form.setFont(f);
        }
	}

    public SuggestionCompletionEngine getSuggestionCompletionEngine() {
        return editor.getCompletionEngine();
    }

    public RuleModel getModel() {
        return model;
    }

    public void setModel(RuleModel model) {
        this.model = model;
    }

    private void clearComposite(Composite composite) {
        if ( composite != null ) {
            Control[] c = composite.getChildren();
            for ( int i = 0; i < c.length; i++ ) {
                Control c2 = c[i];
                c2.dispose();
            }
        }
    }

    private void reloadCommon() {
        toolkit.paintBordersFor( form.getBody() );
        form.redraw();
        Dialog.applyDialogFont( form.getBody() );
        form.reflow( true );
    }

    public void reloadRhs() {
        clearComposite( thenComposite );
        redrawRhs();
        reloadCommon();
    }

    public void reloadLhs() {
        clearComposite( ifComposite );
        redrawLhs();
        reloadCommon();
    }

    public void reloadOptions() {
        clearComposite( optionsComposite );
        redrawOptions();
        reloadCommon();
    }

    public void reloadWidgets() {
        reloadLhs();
        reloadRhs();
        reloadOptions();
    }

    private void redrawOptions() {
        Composite comp = toolkit.createComposite( optionsComposite );
        new RuleAttributeWidget( toolkit,
                                 comp,
                                 this );
    }

    private void redrawRhs() {
        for ( int i = 0; i < model.rhs.length; i++ ) {
            IAction action = model.rhs[i];

            if ( action instanceof ActionSetField ) {
                addActionSetFieldWidget( action,
                                         i );
            } else if ( action instanceof ActionInsertFact ) {
                addActionInsertFactWidget( action,
                                           i );
            } else if ( action instanceof ActionRetractFact ) {
                addActionRetractFactWidget( action,
                                            i );
            } else if ( action instanceof DSLSentence ) {
                addRHSDSLSentenceWidget( i,
                                         (DSLSentence) action );
            }

        }
    }

    private void addActionInsertFactWidget(IAction action,
                                           int i) {
        Composite comp = toolkit.createComposite( thenComposite );
        new ActionInsertFactWidget( toolkit,
                                    comp,
                                    this,
                                    (ActionInsertFact) action,
                                    i );
    }

    private void redrawLhs() {
        for ( int i = 0; i < model.lhs.length; i++ ) {
            IPattern pattern = model.lhs[i];

            if ( pattern instanceof FactPattern ) {
                addFactPatternWidget( i,
                                      (FactPattern) pattern );
            }
            if ( pattern instanceof CompositeFactPattern ) {
                addCompositeFactPatternWidget( i,
                                               (CompositeFactPattern) pattern );
            } else if ( pattern instanceof DSLSentence ) {
                // skip for now
            } else {
                // dont' care
            }
        }

        for ( int i = 0; i < model.lhs.length; i++ ) {
            IPattern pattern = model.lhs[i];
            if ( pattern instanceof DSLSentence ) {
                addLHSDSLSentenceWidget( i,
                                         (DSLSentence) pattern );
            }
        }
    }

    private void addActionRetractFactWidget(IAction action,
                                            int i) {
        Composite comp = toolkit.createComposite( thenComposite );
        new ActionRetractFactWidget( toolkit,
                                     comp,
                                     this,
                                     (ActionRetractFact) action,
                                     i );
    }

    /*private void addActionAssertFactWidget(IAction action,
                                           int i) {
        Composite comp = toolkit.createComposite( thenComposite );
        new ActionInsertFactWidget( toolkit,
                                    comp,
                                    this,
                                    (ActionInsertFact) action,
                                    i );

    }*/

    private void addActionSetFieldWidget(IAction action,
                                         int i) {
        Composite comp = toolkit.createComposite( thenComposite );
        new ActionSetFieldWidget( toolkit,
                                  comp,
                                  this,
                                  model,
                                  (ActionSetField) action,
                                  i );
    }

    private void addRHSDSLSentenceWidget(int idx,
                                         DSLSentence pattern) {
        Composite comp = toolkit.createComposite( thenComposite );
        new RHSDSLSentenceWidget( toolkit,
                                  comp,
                                  pattern,
                                  this,
                                  idx );
    }

    private void addLHSDSLSentenceWidget(int idx,
                                         DSLSentence pattern) {
        Composite comp = toolkit.createComposite( ifComposite );
        new LHSDSLSentenceWidget( toolkit,
                                  comp,
                                  pattern,
                                  this,
                                  idx );
    }

    private void addCompositeFactPatternWidget(int idx,
                                               CompositeFactPattern pattern) {
        Composite comp = toolkit.createComposite( ifComposite );
        new CompositeFactPatternWidget( toolkit,
                                        comp,
                                        this,
                                        pattern,
                                        idx );
    }

    private void addFactPatternWidget(int idx,
                                      FactPattern pattern) {
        Composite comp = toolkit.createComposite( ifComposite );
        new FactPatternWidget( toolkit,
                               comp,
                               this,
                               pattern,
                               null,
                               idx,
                               true );
    }

    private Section createMainSection(final ScrolledForm form,
                                      FormToolkit toolkit,
                                      String title,
                                      Window popup) {
        ColumnLayout layout = new ColumnLayout();
        layout.minNumColumns = 1;
        layout.maxNumColumns = 1;
        Section l1Sect = toolkit.createSection( form.getBody(),
                                                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED );
        l1Sect.setActiveToggleColor( toolkit.getHyperlinkGroup().getActiveForeground() );
        l1Sect.setToggleColor( toolkit.getColors().getColor( FormColors.SEPARATOR ) );
        l1Sect.setText( title );
        createAddToolItem( l1Sect,
                           popup );
        Composite comp = toolkit.createComposite( l1Sect );
        l1Sect.setClient( comp );
        return l1Sect;
    }

    private void createAddToolItem(Section sect,
                                   final Window popup) {
        ToolBar tbar = new ToolBar( sect,
                                    SWT.FLAT | SWT.HORIZONTAL );
        ToolItem titem = new ToolItem( tbar,
                                       SWT.SEPARATOR );
        titem = new ToolItem( tbar,
                              SWT.PUSH );
        titem.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_TOOL_NEW_WIZARD ) );

        titem.addListener( SWT.Selection,
                           new Listener() {
                               public void handleEvent(Event event) {
                                   popup.open();
                               }
                           } );
        sect.setTextClient( tbar );
    }

    public void refresh() {
        ifComposite.layout();
        ifComposite.redraw();

        thenComposite.layout();
        thenComposite.redraw();

        optionsComposite.layout();
        optionsComposite.redraw();
    }

}
