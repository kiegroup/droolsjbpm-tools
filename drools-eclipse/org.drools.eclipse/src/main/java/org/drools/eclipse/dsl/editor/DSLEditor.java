package org.drools.eclipse.dsl.editor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.builder.IDroolsModelMarker;
import org.drools.lang.dsl.DSLMappingEntry;
import org.drools.lang.dsl.DSLMappingFile;
import org.drools.lang.dsl.DefaultDSLMappingEntry;
import org.drools.lang.dsl.MappingError;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This is the tablular DSL configuration editor.
 * @author Michael Neale
 */
public class DSLEditor extends EditorPart {

    private Table          table;
    private TableViewer    tableViewer;
    private NLGrammarModel model;          //this is the model that does all the work (from drools-compiler)
    private boolean        dirty = false;  //editing or deleting will make it dirty
    private Text           exprText;       //for language expression
    private Text           mappingText;    //for target rule expression
    private Text           descriptionText; //just a comment field
    private Text           objText;        // for the object name
    private Combo          sortCombo;      // for the sort field

    public void doSave(IProgressMonitor monitor) {

        FileEditorInput input = (FileEditorInput) getEditorInput();
        File outputFile = input.getFile().getLocation().toFile();
        saveFile( monitor,
                  outputFile,
                  input );

    }

    private void saveFile(IProgressMonitor monitor,
                          File outputFile,
                          FileEditorInput input) {
        try {
            validate( input );

            FileWriter writer = new FileWriter( outputFile );
            DSLMappingFile.saveMapping( writer,
                                        model );

            makeClean();
            writer.close();
            input.getFile().getProject().refreshLocal( IResource.DEPTH_INFINITE,
                                                       monitor );
        } catch ( IOException e ) {
            throw new IllegalStateException( "Unable to save DSL configuration file. (IOException: " + e.getMessage() + ")" );
        } catch ( CoreException e ) {
            throw new IllegalStateException( "Unable to resync workbench after DSL save. (CoreException: " + e.getMessage() + ")" );
        }
    }

    private void validate(FileEditorInput input) {
        removeProblemsFor( input.getFile() );
        List errs = new ArrayList();
        for ( Iterator iter = model.getEntries().iterator(); iter.hasNext(); ) {
            DSLMappingEntry item = (DSLMappingEntry) iter.next();
            errs.addAll( item.getErrors() );
        }
        if ( errs.size() > 0 ) {
            for ( Iterator iter = errs.iterator(); iter.hasNext(); ) {
                MappingError mapEr = (MappingError) iter.next();
                createMarker( input.getFile(),
                              mapEr.getMessage() + "  From [" + mapEr.getTemplateText() + "]",
                              -1 );
            }
        }
    }

    private void createMarker(final IResource res,
                              final String message,
                              final int lineNumber) {
        try {
            IWorkspaceRunnable r = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    IMarker marker = res.createMarker( IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER );
                    marker.setAttribute( IMarker.MESSAGE,
                                         message );
                    marker.setAttribute( IMarker.SEVERITY,
                                         IMarker.SEVERITY_WARNING );
                    marker.setAttribute( IMarker.LINE_NUMBER,
                                         lineNumber );
                }
            };
            res.getWorkspace().run( r,
                                    null,
                                    IWorkspace.AVOID_UPDATE,
                                    null );
        } catch ( CoreException e ) {
            DroolsEclipsePlugin.log( e );
        }
    }

    private void removeProblemsFor(IResource resource) {
        try {
            if ( resource != null && resource.exists() ) {
                resource.deleteMarkers( IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER,
                                        false,
                                        IResource.DEPTH_INFINITE );
            }
        } catch ( CoreException e ) {
            DroolsEclipsePlugin.log( e );
        }
    }

    void makeClean() {
        this.dirty = false;
        firePropertyChange( PROP_DIRTY );

    }

    public void doSaveAs() {
        // TODO Implement this.
    }

    public void init(IEditorSite site,
                     IEditorInput editorInput) throws PartInitException {
        setSite( site );
        setInput( editorInput );
		setVisibleName( editorInput );

        try {
            InputStream stream = null;
        	if (editorInput instanceof FileEditorInput) {
        		FileEditorInput input = (FileEditorInput) editorInput;
        		stream = input.getFile().getContents();
        	} else if (editorInput instanceof IStorageEditorInput) {
        		IStorageEditorInput input = (IStorageEditorInput) editorInput;
        		stream = input.getStorage().getContents();
        	}

        	model = new NLGrammarModel();
            DSLMappingFile file = new DSLMappingFile();
            file.parseAndLoad( new InputStreamReader( stream ) );
            model.addEntries( file.getMapping().getEntries() );
            stream.close();

        } catch ( CoreException e ) {
            throw new IllegalStateException( "Unable to load DSL configuration file. (CoreException: " + e.getMessage() + ")" );
        } catch ( IOException e ) {
            throw new IllegalStateException( "Unabel to close stream fo DSL config file. (IOException: " + e.getMessage() + ")" );
        }

    }

    private void setVisibleName(IEditorInput input) {
        setPartName( input.getName() );
        setContentDescription( "Editing Domain specific language: [" + input.getName() + "]" );
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the dirty flag, and notifies the workbench.
     */
    void makeDirty() {
        dirty = true;
        firePropertyChange( PROP_DIRTY );
    }

    /**
     * The method sorts th e
     *
     */
    public void sortModel() {
        if ( sortCombo.getSelectionIndex() == DSLMappingSorter.EXPRESSION ) {
            tableViewer.setSorter( new DSLMappingSorter( DSLMappingSorter.EXPRESSION ) );
        } else if ( sortCombo.getSelectionIndex() == DSLMappingSorter.OBJECT ) {
            tableViewer.setSorter( new DSLMappingSorter( DSLMappingSorter.OBJECT ) );
        } else if ( sortCombo.getSelectionIndex() == DSLMappingSorter.SCOPE ) {
            tableViewer.setSorter( new DSLMappingSorter( DSLMappingSorter.SCOPE ) );
        } else if ( sortCombo.getSelectionIndex() == DSLMappingSorter.MAPPING ) {
            tableViewer.setSorter( new DSLMappingSorter( DSLMappingSorter.MAPPING ) );
        }
    }

    public boolean isSaveAsAllowed() {
        // TODO implement SaveAs
        return false;
    }

    public void createPartControl(Composite parent) {

        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH );
        parent.setLayoutData( gridData );

        // Set numColumns to 3 in the overall grid
        GridLayout layout = new GridLayout( 3,
                                            false );
        layout.marginWidth = 4;
        parent.setLayout( layout );

        //create the overall desc field (comments).
        createDescriptionField( parent );

        // create the table
        createTable( parent );

        // Create and setup the TableViewer
        createTableViewer();

        //set up the table "binding" with the model
        tableViewer.setContentProvider( new DSLContentProvider( tableViewer,
                                                                model ) );
        tableViewer.setLabelProvider( new DSLLabelProvider() );
        refreshModel();

        //setup the fields below the table
        createExpressionViewField( parent );
        createEditButton( parent );
        createMappingViewField( parent );
        createDeleteButton( parent );
        createObjectViewField( parent );
        createAddButton( parent );
        createSortField( parent );
        createSortButton( parent );
        createCopyButton( parent );

        //listeners on the table...
        createTableListeners();

    }

    /**
     * Setup table listeners for GUI events.
     */
    private void createTableListeners() {

        //setup views into current selected
        table.addSelectionListener( new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                populate();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                populate();
            }

            private void populate() {
                DSLMappingEntry selected = getCurrentSelected();
                exprText.setText( selected.getMappingKey() );
                mappingText.setText( selected.getMappingValue() );
                objText.setText( selected.getMetaData().getMetaData() == null ? "" : selected.getMetaData().getMetaData() );
            }

        } );

        //double click support
        table.addMouseListener( new MouseListener() {

            public void mouseDoubleClick(MouseEvent e) {
                showEditPopup();
            }

            public void mouseDown(MouseEvent e) {
            }

            public void mouseUp(MouseEvent e) {
            }

        } );

    }

    private void createDescriptionField(Composite parent) {
        Label descLbl = new Label( parent,
                                   SWT.NONE );
        descLbl.setText( "Description:" );
        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        descLbl.setLayoutData( gridData );

        descriptionText = new Text( parent,
                                    SWT.BORDER );
        descriptionText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        descriptionText.setText( model.getDescription() == null ? "" : model.getDescription() );
        descriptionText.addModifyListener( new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String text = descriptionText.getText();
                if ( !text.equals( model.getDescription() ) ) {
                    model.setDescription( text );
                    makeDirty();
                }
            }

        } );
    }

    private void createMappingViewField(Composite parent) {
        Label mapping = new Label( parent,
                                   SWT.NONE );
        mapping.setText( "Mapping:" );
        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        mapping.setLayoutData( gridData );

        mappingText = new Text( parent,
                                SWT.BORDER );
        mappingText.setEditable( false );

        mappingText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    }

    private void createExpressionViewField(Composite parent) {

        Label expr = new Label( parent,
                                SWT.NONE );
        expr.setText( "Expression:" );
        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        expr.setLayoutData( gridData );

        exprText = new Text( parent,
                             SWT.BORDER );
        exprText.setEditable( false );
        gridData = new GridData( GridData.FILL_HORIZONTAL );

        exprText.setLayoutData( gridData );

    }

    private void createObjectViewField(Composite parent) {

        Label obj = new Label( parent,
                               SWT.NONE );
        obj.setText( "Object:" );
        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        obj.setLayoutData( gridData );

        objText = new Text( parent,
                            SWT.BORDER );
        objText.setEditable( false );
        gridData = new GridData( GridData.FILL_HORIZONTAL );

        objText.setLayoutData( gridData );

    }

    private void createSortField(Composite parent) {
        Label sort = new Label( parent,
                                SWT.NONE );
        sort.setText( "Sort by:" );
        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        sort.setLayoutData( gridData );

        sortCombo = new Combo( parent,
                               SWT.READ_ONLY );
        sortCombo.add( "Object",
                       DSLMappingSorter.OBJECT );
        sortCombo.add( "Language Expression",
                       DSLMappingSorter.EXPRESSION );
        sortCombo.add( "Rule Language Mapping",
                       DSLMappingSorter.MAPPING );
        sortCombo.add( "Scope",
                       DSLMappingSorter.SCOPE );
        gridData = new GridData( GridData.FILL_HORIZONTAL );

        sortCombo.setLayoutData( gridData );
    }

    /** Refreshes the table do make sure it is up to date with the model. */
    private void refreshModel() {
        tableViewer.setInput( model );
    }

    private void createEditButton(Composite parent) {
        // Create and configure the "Add" button
        Button add = new Button( parent,
                                 SWT.PUSH | SWT.CENTER );
        add.setText( "Edit" );

        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        add.setLayoutData( gridData );

        add.addSelectionListener( new SelectionAdapter() {

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                showEditPopup();
            }

        } );
    }

    private void showEditPopup() {
    	DSLMappingEntry selected = getCurrentSelected();
    	if (selected != null) {
	        MappingEditor editor = new MappingEditor( getSite().getShell() );
	        editor.create();
	        editor.getShell().setText( "Edit language mapping" );
	        editor.setTitle( "Edit an existing language mapping item." );
	        editor.setTitleImage( getTitleImage() );
	
	        editor.setNLMappingItem( selected );
	
	        editor.open();
	        if ( !editor.isCancelled() ) {
	            refreshModel();
	            makeDirty();
	        }
    	}
    }

    private void createDeleteButton(Composite parent) {
        // Create and configure the "Add" button
        Button add = new Button( parent,
                                 SWT.PUSH | SWT.CENTER );
        add.setText( "Remove" );

        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        add.setLayoutData( gridData );
        add.addSelectionListener( new SelectionAdapter() {
            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                model.removeEntry( getCurrentSelected() );
                refreshModel();
                makeDirty();
                exprText.setText( "" );
                mappingText.setText( "" );
                objText.setText( "" );
            }
        } );
    }

    private void createSortButton(Composite parent) {
        // Create and configure the "Add" button
        Button sort = new Button( parent,
                                  SWT.PUSH | SWT.CENTER );
        sort.setText( "Sort" );

        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        sort.setLayoutData( gridData );
        sort.addSelectionListener( new SelectionAdapter() {
            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                sortModel();
                refreshModel();
                makeDirty();
            }

        } );
    }

    /**
     * Return the selected item from the table grid thingy.
     */
    private DSLMappingEntry getCurrentSelected() {
        return (DSLMappingEntry) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
    }

    private void createAddButton(Composite parent) {
        // Create and configure the "Add" button
        Button add = new Button( parent,
                                 SWT.PUSH | SWT.CENTER );
        add.setText( "Add" );

        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        add.setLayoutData( gridData );

        add.addSelectionListener( new SelectionAdapter() {

            // Add an item, should pop up the editor
            public void widgetSelected(SelectionEvent e) {

                DSLMappingEntry newItem = new DefaultDSLMappingEntry();

                MappingEditor editor = new MappingEditor( getSite().getShell() );//shell);
                editor.create();
                editor.getShell().setText( "New language mapping" );
                editor.setTitle( "Create a new language element mapping." );
                editor.setTitleImage( getTitleImage() );

                editor.setNLMappingItem( newItem );

                editor.open();
                if ( !editor.isCancelled() ) {
                    model.addEntry( newItem );
                    refreshModel();
                    makeDirty();
                }

            }
        } );
    }

    private void createCopyButton(Composite parent) {
        // Create and configure the "Add" button
        Button copy = new Button( parent,
                                  SWT.PUSH | SWT.CENTER );
        copy.setText( "Copy" );

        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        gridData.widthHint = 80;
        copy.setLayoutData( gridData );

        copy.addSelectionListener( new SelectionAdapter() {

            // Add an item, should pop up the editor
            public void widgetSelected(SelectionEvent e) {

                DSLMappingEntry curr = getCurrentSelected();
                if (curr != null) {
	                DSLMappingEntry newItem = new DefaultDSLMappingEntry( curr.getSection(),
	                                                                      curr.getMetaData(),
	                                                                      curr.getMappingKey(),
	                                                                      curr.getMappingValue() );
	
	                MappingEditor editor = new MappingEditor( getSite().getShell() );//shell);
	                editor.create();
	                editor.getShell().setText( "New language mapping" );
	                editor.setTitle( "Create a new language element mapping from a copy." );
	                editor.setTitleImage( getTitleImage() );
	
	                editor.setNLMappingItem( newItem );
	
	                editor.open();
	                if ( !editor.isCancelled() ) {
	                    model.addEntry( newItem );
	                    refreshModel();
	                    makeDirty();
	                }
                }
            }
        } );
    }

    /**
     * Create the viewer.
     */
    private void createTableViewer() {
        tableViewer = new TableViewer( table );
        tableViewer.setUseHashlookup( true );
        //following is if we want default sorting... my thought is no...
    }

    /**
     * Create the Table
     */
    private void createTable(Composite parent) {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

        table = new Table( parent,
                           style );

        GridData gridData = new GridData( GridData.FILL_BOTH );
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 3;
        table.setLayoutData( gridData );

        table.setLinesVisible( true );
        table.setHeaderVisible( true );

        TableColumn column;

        //Expression col
        column = new TableColumn( table,
                                  SWT.LEFT,
                                  0 );
        column.setText( "Language Expression" );
        column.setWidth( 350 );
        // Add listener to column so sorted when clicked 
        column.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                tableViewer.setSorter( new DSLMappingSorter( DSLMappingSorter.EXPRESSION ) );
            }
        } );

        // 3rd column with task Owner
        column = new TableColumn( table,
                                  SWT.LEFT,
                                  1 );
        column.setText( "Rule Language Mapping" );
        column.setWidth( 200 );
        // Add listener to column so sorted when clicked
        column.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                tableViewer.setSorter( new DSLMappingSorter( DSLMappingSorter.MAPPING ) );
            }
        } );

        // 4th column with task PercentComplete 
        column = new TableColumn( table,
                                  SWT.LEFT,
                                  2 );
        column.setText( "Object" );
        column.setWidth( 80 );

        // 5th column with task PercentComplete 
        column = new TableColumn( table,
                                  SWT.LEFT,
                                  3 );
        column.setText( "Scope" );
        column.setWidth( 80 );

        //  Add listener to column so tasks are sorted when clicked
        column.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                tableViewer.setSorter( new DSLMappingSorter( DSLMappingSorter.SCOPE ) );
            }
        } );

    }

    public void setFocus() {
    }

    public void dispose() {
        super.dispose();
    }

}
