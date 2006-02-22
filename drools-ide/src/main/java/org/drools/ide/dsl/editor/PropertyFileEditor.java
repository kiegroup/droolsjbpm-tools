/* $RCSfile: PropertyFileEditor.java,v $
 * Created on 19.09.2002, 21:02:25 by Oliver David
 * $Source: /cvsroot/eclpropfileedit/eclpropfileedit/src/org/sourceforge/eclpropfileedit/editor/PropertyFileEditor.java,v $
 * $Id: PropertyFileEditor.java,v 1.3 2002/11/18 21:31:42 davoli Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
package org.drools.ide.dsl.editor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import org.sourceforge.eclpropfileedit.PropertyFileEditorPlugin;
import org.sourceforge.eclpropfileedit.core.PropertyException;
import org.sourceforge.eclpropfileedit.core.PropertyFileUtil;
import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;
import org.sourceforge.eclpropfileedit.service.PropertyFileService;

/**
 * @author  Oliver
 * @version $Revision: 1.3 $
 */
public class PropertyFileEditor extends EditorPart
    implements SelectionListener, MouseListener, ModifyListener, KeyListener, FocusListener, TraverseListener
{

    private String i_fileName;
    private File i_file;

    private PropertyFileService i_propertiesFileService;

    private HashMap i_dataMap;
    private Collection i_initData;

    private Table mappingTable;
    private Composite i_composite;

    private Label i_commentLabel;
    private Label i_valueLabel;
    private Text i_commentText;
    private Text i_valueText;
    private Button i_newButton;
    private Button i_delButton;
    private Button i_nullButton;

    private NewMapping i_propertiesFileDialog;

    private PropertyLineWrapper i_lastSelectedProperty;

    private int i_lastSelectedIndex;

    private boolean i_isDirty;

    private Image icon;

    private boolean i_isReadOnly;


    /**
     * Constructor for SampleEditor.
     */
    public PropertyFileEditor()
    {
        super();
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose()
    {
        super.dispose();
    }

    /**
     * @see org.eclipse.ui.IEditorPart#doSave(IProgressMonitor)
     */
    public void doSave(IProgressMonitor progressMonitor)
    {
        
        final FileEditorInput input = (FileEditorInput) getEditorInput();

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation()
        {
            public void execute(IProgressMonitor pm) throws CoreException
            {
                saveToPropertyFile();
                input.getFile().getProject().refreshLocal(IResource.DEPTH_INFINITE, pm);//new NullProgressMonitor());
            }
        };

        try
        {
//            operation.run(pm);
            setDirty(false);
            firePropertyChange(PROP_DIRTY);
            new ProgressMonitorDialog(getSite().getShell()).run(false, false, operation);
        }
        catch (InterruptedException x)
        {
        }
        catch (OperationCanceledException x)
        {
        }
        catch (InvocationTargetException x)
        {
        }
    }

    /**
     * @see org.eclipse.ui.IEditorPart#doSaveAs()
     */
    public void doSaveAs()
    {
        SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
        saveAsDialog.create();
        saveAsDialog.open();
        IPath newPath = saveAsDialog.getResult();

        IWorkspace workspace = PropertyFileEditorPlugin.getWorkspace();
        IFile file = workspace.getRoot().getFile(newPath);
        file.setReadOnly(isReadOnly());
        final FileEditorInput newInput = new FileEditorInput(file);

        i_file = newInput.getFile().getLocation().toFile();
        setTitle(i_file.getName());

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation()
        {
            public void execute(IProgressMonitor pm) throws CoreException
            {
                saveToPropertyFile();
                newInput.getFile().getProject().refreshLocal(IResource.DEPTH_INFINITE, pm);//new NullProgressMonitor());
            }
        };

        try
        {
//            operation.run(pm);
            setDirty(false);
            firePropertyChange(PROP_DIRTY);
            new ProgressMonitorDialog(getSite().getShell()).run(false, false, operation);
        }
        catch (InterruptedException x)
        {
            // do nothing so far
        }
        catch (OperationCanceledException x)
        {
            // do nothing so far
        }
        catch (InvocationTargetException x)
        {
            // do nothing so far
        }
    }

    /**
     * @see org.eclipse.ui.IEditorPart#gotoMarker(IMarker)
     */
    public void gotoMarker(IMarker marker)
    {
        // do nothing so far
    }

    /**
     * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
    {
        FileEditorInput input = (FileEditorInput)editorInput;
        setSite(site);
        setInput(editorInput);

        // set the file's name
        i_fileName = input.getName();

        // determine whether the file is read-only
        i_isReadOnly = input.getFile().isReadOnly();

        // set the title of the editor to the file's name
        setTitle(i_fileName);

        // get the file as java.io.File object
        i_file = input.getFile().getLocation().toFile();
        i_propertiesFileService = new PropertyFileService(i_file);

        // read the data from the properties file ans validate the file at the same time
        // if there is an unvalid text line int his file open a dilaog and co not allow to open the editor
        try
        {
            i_initData = i_propertiesFileService.readPropertiesFile();
        }
        catch(PropertyException e)
        {
            // if the file contains an uvalid text line then show the dialog
            MessageDialog messageDialog =
                new MessageDialog(
                    getSite().getShell(),
                    "Unvalid Properties File",
                    getLogo(),
                    e.getMessage(),
                    MessageDialog.WARNING,
                    new String[] {"OK"},
                    0);
            messageDialog.open();
            if(messageDialog.getReturnCode() == MessageDialog.OK)
            {
                dispose();
            }
        }

        // create the image after getting the image reference from the "plugin.xml" file
        icon = input.getImageDescriptor().createImage();
    }

    /**
     * @see org.eclipse.ui.IEditorPart#isDirty()
     */
    public boolean isDirty()
    {
        return i_isDirty;
    }

    /**
     * @see org.eclipse.ui.IEditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent)
    {
        parent.setData(this);

        GridLayout gridLayout = new GridLayout ();
        gridLayout.marginHeight = 10;
        gridLayout.verticalSpacing = 10;
        gridLayout.marginWidth = 10;
        gridLayout.horizontalSpacing = 10;
        gridLayout.numColumns = 3;
        parent.setLayout (gridLayout);

        createEditWidgets( parent );
        
        // create the table
        mappingTable = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION );//| SWT.CHECK);
        mappingTable.setToolTipText("Double click to edit a mapping.");
        mappingTable.setLinesVisible(true);
        mappingTable.setVisible(false);
        mappingTable.setLinesVisible (true);
        mappingTable.setHeaderVisible(true);
        mappingTable.addSelectionListener(this);
        mappingTable.addMouseListener(this);
        mappingTable.setRedraw(false);
        
        
        GridData data = new GridData ();
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        data.horizontalSpan = 3;
        data.grabExcessVerticalSpace = true;
        mappingTable.setLayoutData(data);

       // create the columns
        TableColumn scopeColumn = new TableColumn(mappingTable, SWT.LEFT);
        TableColumn keyColumn = new TableColumn(mappingTable, SWT.LEFT);
        TableColumn valueColumn = new TableColumn(mappingTable, SWT.LEFT);
        keyColumn.setText("Language expression");
        valueColumn.setText("Rule language mapping");
        scopeColumn.setText( "Scope" );
        keyColumn.setResizable(true);
        valueColumn.setResizable(true);

        // key column
        ColumnLayoutData keyColumnLayout = new ColumnPixelData(350, true);//ColumnWeightData(50, true);
        // value column
        ColumnLayoutData valueColumnLayout = new ColumnPixelData(350, false);//ColumnWeightData(50, false);
        ColumnLayoutData scopeColumnLayout = new ColumnPixelData(200, false);
        
        
        // set columns in Table layout
        TableLayout layout = new TableLayout();        
        layout.addColumnData( keyColumnLayout );
        layout.addColumnData( valueColumnLayout );
        layout.addColumnData( scopeColumnLayout );
        
        mappingTable.setLayout(layout);
        mappingTable.layout();

        parent.pack();

        // initialize and populate the table with the data of the file received from
        // the PropertyFileService object
        initPropertiesTable(i_initData);

        //set the icon on the editor
        setTitleImage(icon);

        if(isReadOnly())
        {
            i_commentText.setEditable(false);
            i_valueText.setEditable(false);
            i_newButton.setEnabled(false);
            i_delButton.setEnabled(false);
        }

        // set the widgets visible
        mappingTable.setVisible(true);
        i_commentLabel.setVisible(true);
        i_valueLabel.setVisible(true);
        i_commentText.setVisible(true);
        i_valueText.setVisible(true);
        i_newButton.setVisible(true);
        i_delButton.setVisible(true);

        // set the focus on the table and select the first item if existing
        mappingTable.select(0);
//        i_propertiesTable.setFocus();

        TableItem[] firstItem = mappingTable.getSelection();
        if(firstItem.length > 0)
        {
            String key = firstItem[0].getText(0);
            PropertyLineWrapper propertiesLine = (PropertyLineWrapper)i_dataMap.get(key);
            i_commentText.setText(propertiesLine.getCommentString());
            i_valueText.setText(propertiesLine.getValueString());
            i_lastSelectedProperty = propertiesLine;
            i_lastSelectedIndex = 0;
            setFocusOnRow();
        }

        // this is necessary because the other buttons would respond if you hit "Enter" with opening the dialogs
        i_nullButton = new Button (parent, SWT.PUSH);
        i_nullButton.addTraverseListener(this);
        i_nullButton.setVisible(false);
        getSite().getShell().setDefaultButton(i_nullButton);
    }

    private void createEditWidgets(Composite parent) {
        GridData data;
        // create the comment's label
        i_commentLabel = new Label (parent, SWT.NONE);
        i_commentLabel.setVisible(false);
        i_commentLabel.setAlignment(SWT.LEFT);
        i_commentLabel.setText("Comment:");
        data = new GridData ();
        data.verticalAlignment = GridData.BEGINNING;
        i_commentLabel.setLayoutData(data);

        // create the comment's textfield
        i_commentText = new Text (parent, SWT.BORDER);
        i_commentText.setVisible(false);
        i_commentText.setText("");
        i_commentText.addKeyListener(this);
        i_commentText.addFocusListener(this);
        data = new GridData ();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        i_commentText.setLayoutData (data);

        // create the "new" button
        i_newButton = new Button (parent, SWT.PUSH);
        i_newButton.setVisible(false);
        i_newButton.setText("New Property...");
        i_newButton.addMouseListener(this);
        data = new GridData ();
        data.widthHint = 105;
        data.horizontalAlignment = GridData.END;
        data.verticalAlignment = GridData.BEGINNING;
        i_newButton.setLayoutData (data);

        // create the value's label
        i_valueLabel = new Label (parent, SWT.NONE);
        i_valueLabel.setVisible(false);
        i_valueLabel.setAlignment(SWT.LEFT);
        i_valueLabel.setText("Value:");
        data = new GridData ();
        data.verticalAlignment = GridData.BEGINNING;
        i_valueLabel.setLayoutData (data);

        // create the value's textfield
        i_valueText = new Text (parent, SWT.BORDER/* | SWT.WRAP | SWT.V_SCROLL*/);
        i_valueText.setVisible(false);
        i_valueText.setText("");
        i_valueText.addKeyListener(this);
        i_valueText.addFocusListener(this);
        data = new GridData ();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        i_valueText.setLayoutData (data);

        // create the textfield
        i_delButton = new Button (parent, SWT.PUSH);
        i_delButton.setVisible(false);
        i_delButton.setText("Remove Property...");
        i_delButton.addMouseListener(this);
        data = new GridData ();
        data.widthHint = 105;
        data.horizontalAlignment = GridData.END;
        data.verticalAlignment = GridData.BEGINNING;
        i_delButton.setLayoutData (data);
    }

    /**
     * Method initPropertiesTable.
     * Populates the data table with the data of the file
     * @param properties
     */
    private void initPropertiesTable(Collection properties)
    {
        i_dataMap = new HashMap();

        Iterator iter = PropertyFileUtil.getSortedIterator(properties);
        while (iter.hasNext())
        {
            PropertyLineWrapper element = (PropertyLineWrapper) iter.next();
            TableItem keyValueItem = new TableItem(mappingTable, SWT.NONE);
            keyValueItem.setText(new String[]{element.getKeyString().trim(), element.getValueString()});
            //keyValueItem.setChecked(element.isCommentedProperty());
            i_dataMap.put(element.getKeyString(), element);
        }
        mappingTable.setRedraw(true);
        i_initData = null;
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e)
    {
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e)
    {
        getTableSelection();
        if(!isReadOnly())
        {
            if(e.getSource() == mappingTable)
            {
                updateData();
            }
        }
        else
        {
            if(e.getSource() == mappingTable)
            {

                TableItem[] tableItems = mappingTable.getItems();
                for(int i = 0; i < tableItems.length; i++)
                {
                    TableItem tableItem = tableItems[i];
                    String key = tableItem.getText(0);
                    PropertyLineWrapper propertiesLine = (PropertyLineWrapper)i_dataMap.get(key);
                    boolean isCurrentDataItemChecked = propertiesLine.isCommentedProperty();
                    tableItems[i].setChecked(isCurrentDataItemChecked);
                }
            }
        }
    }

    /**
     * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(MouseEvent)
     */
    public void mouseDoubleClick(MouseEvent e)
    {
        if(!isReadOnly())
        {
            if(e.getSource() == mappingTable)
            {
                openEditPropertyDialog();
            }
        }
        else
        {
            openReadOnlyDialog();
        }
    }

    /**
     * @see org.eclipse.swt.events.MouseListener#mouseUp(MouseEvent)
     */
    public void mouseUp(MouseEvent e)
    {
        if(!isReadOnly())
        {
            if(e.getSource() == i_newButton)
            {
                openNewPropertyDialog();
            }


            if(e.getSource() == i_delButton)
            {
                TableItem[] tableItems = mappingTable.getSelection();

                String keyString = null;
                if (tableItems.length != 0)
                {
                    keyString = tableItems[0].getText(0).trim();
                }

                if (keyString == null || keyString == "")
                {
                    MessageDialog messageDialog =
                        new MessageDialog(
                            getSite().getShell(),
                            "Remove Property",
                            getLogo(),
                            "Please select the item you want to remove from the table",
                            MessageDialog.INFORMATION,
                            new String[]{"OK"},
                            0);
                    messageDialog.open();
                }
                else
                {
                    MessageDialog messageDialog =
                        new MessageDialog(
                            getSite().getShell(),
                            "Remove Property",
                            getLogo(),
                            "Do you really want to remove the property with the key \"" + keyString + "\" ?",
                            MessageDialog.QUESTION,
                            new String[] {"Yes", "No"},
                            0);
                    messageDialog.open();
                    if(messageDialog.getReturnCode() == MessageDialog.OK)
                    {
                        removeProperty((PropertyLineWrapper) i_dataMap.get(keyString));
                    }
                }
                mappingTable.setFocus();
                mappingTable.select(0);
            }
        }
    }

    /**
     * @see org.eclipse.swt.events.MouseListener#mouseDown(MouseEvent)
     */
    public void mouseDown(MouseEvent e)
    {
    }

    /**
     * Method openNewPropertyDialog.
     */
    private void openNewPropertyDialog()
    {
        i_propertiesFileDialog = new NewMapping(this, getSite().getShell());
        i_propertiesFileDialog.create();
        i_propertiesFileDialog.setKeyTextEditable(true);
        i_propertiesFileDialog.getShell().setText("Create a new property...");
        i_propertiesFileDialog.getShell().setImage(getLogo());
        i_propertiesFileDialog.setNew(true);
        i_propertiesFileDialog.open();
    }

    /**
     * Method openEditPropertyDialog.
     */
    private void openEditPropertyDialog()
    {
        i_propertiesFileDialog = new NewMapping(this, getSite().getShell());

        TableItem[] tableItems = mappingTable.getSelection();
        String keyString = tableItems[0].getText(0).trim();
        PropertyLineWrapper propertiesLineWrapper = (PropertyLineWrapper) i_dataMap.get(keyString);
        String valueString = propertiesLineWrapper.getValueString();
        boolean isCommentedProperty = propertiesLineWrapper.isCommentedProperty();

        i_propertiesFileDialog.create();
        i_propertiesFileDialog.setKeyString(keyString);
        i_propertiesFileDialog.setValueString(valueString);
        i_propertiesFileDialog.setCommentedProperty(isCommentedProperty);

        // make the key mutable after all
        i_propertiesFileDialog.setKeyTextEditable(true);
        i_propertiesFileDialog.getShell().setText("Edit an existing property...");
        i_propertiesFileDialog.getShell().setImage(getLogo());
        i_propertiesFileDialog.open();
    }

    /**
     * @see org.eclipse.swt.events.ModifyListener#modifyText(ModifyEvent)
     */
    public void modifyText(ModifyEvent e)
    {
        if(!isReadOnly())
        {
            if(i_commentText.isFocusControl() || i_valueText.isFocusControl())
            {
                setDirty(true);
                firePropertyChange(PROP_DIRTY);
            }
        }
    }

    /**
     * Sets the i_isDirty.
     * @param isDirty The i_isDirty to set
     */
    public void setDirty(boolean isDirty)
    {
        i_isDirty = isDirty;
    }

    /**
     * Returns this plug-in's resource bundle.
     * @return the plugin's resource bundle
     */
    public static ResourceBundle getResourceBundle()
    {
        return PropertyFileEditorPlugin.getDefault().getResourceBundle();
    }

    /**
     * @see org.eclipse.swt.events.KeyListener#keyPressed(KeyEvent)
     */
    public void keyPressed(KeyEvent e)
    {

        int input;
        if(e.keyCode != 0)
        {
            input = e.keyCode | e.stateMask;
        }
        else
        {
            input = e.character | e.stateMask;
        }

        if(!isReadOnly())
        {
            if(e.getSource() == i_commentText)
            {
                if(e.character == SWT.CR)
                {
                    i_lastSelectedProperty.setCommentString(i_commentText.getText());
                    addNewProperty(i_lastSelectedProperty, null);
                    i_commentText.setText(i_lastSelectedProperty.getCommentString());
                    i_valueText.setText(i_lastSelectedProperty.getValueString());
                    setFocusOnRow();
                }

                if(isPaste(input))
                {
                    // works without it...
//                    i_commentText.paste();
                }

                if(isCopy(input))
                {
                    i_commentText.copy();
                }

//                if(isCut(input))
//                {
//                    i_commentText.cut();
//                }
            }

            if(e.getSource() == i_valueText)
            {
                if(e.character == SWT.CR)
                {
                    i_lastSelectedProperty.setValueString(i_valueText.getText());
                    addNewProperty(i_lastSelectedProperty, null);
                    i_commentText.setText(i_lastSelectedProperty.getCommentString());
                    i_valueText.setText(i_lastSelectedProperty.getValueString());
                    setFocusOnRow();
                }

                if(isPaste(input))
                {
                    // works without it...
//                    i_valueText.paste();
                }

                if(isCopy(input))
                {
                    i_valueText.copy();
                }

//                if(isCut(input))
//                {
//                    i_valueText.cut();
//                }
            }
        }
    }

    /**
     * @see org.eclipse.swt.events.KeyListener#keyReleased(KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
    }

    /**
     * Method saveToPropertyFile.
     */
    private void saveToPropertyFile()
    {
        i_propertiesFileService.writeToPropertiesFile(i_file, i_dataMap);
        setDirty(false);
        firePropertyChange(PROP_DIRTY);
    }

    /**
     * Method addNewProperty.
     * @param propertiesLineWrapper
     */
    public void addNewProperty(PropertyLineWrapper propertiesLineWrapper, String selectedKey)
    {
        String key = propertiesLineWrapper.getKeyString();
        if(selectedKey != null && !selectedKey.equals(key))
        {
            i_dataMap.remove(selectedKey);
        }
        i_dataMap.put(key, propertiesLineWrapper);
        updatePropertiesTable();
        setDirty(true);
        firePropertyChange(PROP_DIRTY);
        i_lastSelectedIndex = 0;
        mappingTable.select(i_lastSelectedIndex);
        getTableSelection();
    }

    /**
     * Method removeProperty.
     * @param propertiesLineWrapper
     */
    private void removeProperty(PropertyLineWrapper propertiesLineWrapper)
    {
        i_dataMap.remove(propertiesLineWrapper.getKeyString());
        updatePropertiesTable();
        setDirty(true);
        firePropertyChange(PROP_DIRTY);
        i_lastSelectedIndex = 0;
        mappingTable.select(i_lastSelectedIndex);
        getTableSelection();
    }

    /**
     * Method updatePropertiesTable.
     */
    private void updatePropertiesTable()
    {
        
        mappingTable.setRedraw(false);
        mappingTable.removeAll();

        Iterator iter = PropertyFileUtil.getSortedIterator(i_dataMap.values());
        while (iter.hasNext())
        {
            PropertyLineWrapper element = (PropertyLineWrapper) iter.next();
            TableItem keyValueItem = new TableItem(mappingTable, SWT.NONE);
            keyValueItem.setText(new String[] { element.getKeyString().trim(), element.getValueString()});
            keyValueItem.setChecked(element.isCommentedProperty());
        }
        mappingTable.setRedraw(true);
    }

    /**
     * Method update.
     */
    public void update()
    {
        updatePropertiesTable();
        setDirty(true);
        firePropertyChange(PROP_DIRTY);
    }

    /**
     * @see org.eclipse.swt.events.FocusListener#focusGained(FocusEvent)
     */
    public void focusGained(FocusEvent e)
    {
        if (e.getSource() == mappingTable)
        {
            setFocusOnRow();
        }
    }

    /**
     * @see org.eclipse.swt.events.FocusListener#focusLost(FocusEvent)
     */
    public void focusLost(FocusEvent e)
    {
        if(!isReadOnly())
        {
            if (e.getSource() == i_valueText && !i_valueText.getText().equals(i_lastSelectedProperty.getValueString()))
            {
                i_lastSelectedProperty.setCommentString(i_commentText.getText());
                i_lastSelectedProperty.setValueString(i_valueText.getText());
                addNewProperty(i_lastSelectedProperty, null);
            }

            if (e.getSource() == i_commentText && !i_commentText.getText().equals(i_lastSelectedProperty.getCommentString()))
            {
                i_lastSelectedProperty.setCommentString(i_commentText.getText());
                i_lastSelectedProperty.setValueString(i_valueText.getText());
                addNewProperty(i_lastSelectedProperty, null);
            }
            mappingTable.select(i_lastSelectedIndex);
            getTableSelection();
        }
    }

    /**
     * Returns the propertiesTable.
     * @return Table
     */
    public void setFocusOnRow()
    {
        mappingTable.setFocus();
        mappingTable.select(i_lastSelectedIndex);
    }

    /**
     * Method getTableSelection.
     */
    private void getTableSelection()
    {
        TableItem[] tableItems = mappingTable.getSelection();
        if(tableItems.length > 0)
        {
            String keyString = tableItems[0].getText(0);
            PropertyLineWrapper propertiesLineWrapper = (PropertyLineWrapper) i_dataMap.get(keyString);
            String valueString = propertiesLineWrapper.getValueString();
            String commentString = propertiesLineWrapper.getCommentString();
            i_valueText.setText(valueString);
            i_commentText.setText(commentString);
            i_lastSelectedIndex = mappingTable.getSelectionIndex();
            i_lastSelectedProperty = propertiesLineWrapper;
        }
    }

    /**
     * Method getLogo.
     * @return Image
     */
    public Image getLogo()
    {
        return icon;
    }

    /**
     * Method isCopy.
     * @param input
     * @return boolean
     */
    private boolean isCopy(int input)
    {

        if(input == (SWT.DEL | SWT.SHIFT))
        {
            return true;
        }

//        if(input == (/*new String("c").charAt(0)*/99 | SWT.CTRL))
//        {
//            return true;
//        }
        return false;

    }

    /**
     * Method isCut.
     * @param input
     * @return boolean
     */
    private boolean isCut(int input)
    {
//        if(input == (SWT.DEL | SWT.SHIFT))
//        {
//            return true;
//        }

//        if(input == (/*new String("x").charAt(0)*/120 | SWT.CTRL))
//        {
//            return true;
//        }
        return false;
    }

    /**
     * Method isPaste.
     * @param input
     * @return boolean
     */
    private boolean isPaste(int input)
    {
        if(input == (SWT.INSERT | SWT.SHIFT))
        {
            return true;
        }

//        if(input == (/*new String("v").charAt(0)*/118 | SWT.CTRL))
//        {
//            return true;
//        }
        return false;
    }

    /**
     * Method updateData.
     */
    private void updateData()
    {
        TableItem[] tableItems = mappingTable.getItems();
        for(int i = 0; i < tableItems.length; i++)
        {
        	TableItem tableItem = tableItems[i];
        	String key = tableItem.getText(0);
            PropertyLineWrapper propertiesLine = (PropertyLineWrapper)i_dataMap.get(key);
            boolean isCurrentTableItemChecked = tableItem.getChecked();
            boolean isCurrentDataItemChecked = propertiesLine.isCommentedProperty();
            if(isCurrentTableItemChecked != isCurrentDataItemChecked)
            {
                setDirty(true);
                firePropertyChange(PROP_DIRTY);
            }
            propertiesLine.setCommentedProperty(isCurrentTableItemChecked);
            i_dataMap.put(key, propertiesLine);
        }
    }
    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        setFocusOnRow();
    }

    /**
     * Returns the dataMap.
     * @return HashMap
     */
    public HashMap getDataMap()
    {
        return i_dataMap;
    }

    /**
     * @see org.eclipse.swt.events.TraverseListener#keyTraversed(TraverseEvent)
     */
    public void keyTraversed(TraverseEvent e)
    {
        if(e.getSource() == i_nullButton)
        {
            i_nullButton.traverse(SWT.TRAVERSE_TAB_NEXT);
        }
    }

    /**
     * Returns the isReadOnly.
     * @return boolean
     */
    public boolean isReadOnly()
    {
        return i_isReadOnly;
    }

    /**
     * Method openReadOnlyDialog.
     */
    private void openReadOnlyDialog()
    {
        MessageDialog messageDialog =
            new MessageDialog(
                getSite().getShell(),
                "Read-only File",
                getLogo(),
                "This file is marked as read-only!",
                MessageDialog.INFORMATION,
                new String[]{"OK"},
                0);
        messageDialog.open();
    }
    
    /**
     * Method getNewPropertIndex.
     * @param key
     * @return int
     */
    private int getNewPropertIndex(String key)
    {
        int index = -1;
        Iterator iter = PropertyFileUtil.getSortedIterator(i_dataMap.values());
        while(iter.hasNext())
        {
        	PropertyLineWrapper element = (PropertyLineWrapper)iter.next();
        	index++;
            if(key.equals(element.getKeyString()))
            {
                break;
            }
        }
        return index;
    }
}