package org.drools.eclipse.dsl.editor;

import org.drools.lang.dsl.DSLMappingEntry;
import org.drools.lang.dsl.DSLMappingEntry.Section;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This provides an editor for mapping language mappings.
 * This is preferable to in place editing, as it fits the usage pattern of read lots,
 * edit little.
 * 
 * This is a simple popup modal dialog.
 * 
 * @author Michael Neale
 */
public class MappingEditor extends TitleAreaDialog {

    private static final int       SCOPE_KEYWORD = 0;
    private static final int       SCOPE_WHEN    = 1;
    private static final int       SCOPE_THEN    = 2;
    private static final int       SCOPE_ALL     = 3;
    
    private static final String    SCOPE_STR_KEYWORD = "keyword"; 
    private static final String    SCOPE_STR_WHEN = "condition"; 
    private static final String    SCOPE_STR_THEN = "consequence"; 
    private static final String    SCOPE_STR_ALL  = "*"; 

    private Text                   exprText;
    private Text                   mappingText;
    private Text                   objText;
    private Combo                  scopeCombo;
    private boolean                cancelled;

    private DSLMappingEntry model;

    protected MappingEditor(Shell parent) {
        super( parent );
    }

    /**
     * Pass in a NLMapping item for display/edits.
     * Changes will be applied to this object only if the user clicks OK.
     */
    public void setNLMappingItem(DSLMappingEntry item) {
        model = item;
        setSection( model.getSection() );
        exprText.setText( model.getMappingKey() == null ? "" : model.getMappingKey() );
        mappingText.setText( model.getMappingValue() == null ? "" : model.getMappingValue() );
        objText.setText( model.getMetaData().getMetaData() == null ? "" : model.getMetaData().getMetaData() );
    }

    private void setSection(Section section) {
        if ( section == DSLMappingEntry.CONDITION ) {
            scopeCombo.select( SCOPE_WHEN );
        } else if ( section == DSLMappingEntry.CONSEQUENCE ) {
            scopeCombo.select( SCOPE_THEN );
        } else if ( section == DSLMappingEntry.ANY ) {
            scopeCombo.select( SCOPE_ALL );
        } else if ( section == DSLMappingEntry.KEYWORD ) {
            scopeCombo.select( SCOPE_KEYWORD );
        } else {
            throw new IllegalArgumentException( "Unknown scope type: " + section );
        }
    }

    private Section getSection(String sectionStr) {
        DSLMappingEntry.Section section = DSLMappingEntry.ANY;
        if ( SCOPE_STR_KEYWORD.equals( sectionStr ) ) {
            section = DSLMappingEntry.KEYWORD;
        } else if ( SCOPE_STR_WHEN.equals( sectionStr ) ) {
            section = DSLMappingEntry.CONDITION;
        } else if ( SCOPE_STR_THEN.equals( sectionStr ) ) {
            section = DSLMappingEntry.CONSEQUENCE;
        }
        return section;
    }

    protected void cancelPressed() {
        this.cancelled = true;
        super.cancelPressed();
    }

    protected void okPressed() {
        this.cancelled = false;
        this.model.setMappingKey( this.exprText.getText() );
        this.model.setMappingValue( this.mappingText.getText() );
        this.model.setSection( this.getSection( this.scopeCombo.getText() ) );
        this.model.setMetaData( new DSLMappingEntry.DefaultDSLEntryMetaData( this.objText.getText() ) );
        super.okPressed();
    }

    /** This will tell if the user cancelled the edit */
    public boolean isCancelled() {
        return cancelled;
    }

    protected Control createDialogArea(Composite parent) {

        //set the overall layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 10;
        gridLayout.verticalSpacing = 10;
        gridLayout.marginWidth = 10;
        gridLayout.numColumns = 2;
        parent.setLayout( gridLayout );

        //setup fields
        createExpressionField( parent );
        createMappingField( parent );
        createObjectField( parent );
        createScopeField( parent );

        // create the top level composite wrapper
        Composite composite = new Composite( parent,
                                             SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        layout.verticalSpacing = 10;
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        composite.setFont( parent.getFont() );

        return composite;
    }

    private void createMappingField(Composite parent) {
        Label mappingLbl = new Label( parent,
                                      SWT.NONE );
        mappingLbl.setText( "Rule mapping:" );
        mappingLbl.setFont( parent.getFont() );
        mappingLbl.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );

        mappingText = new Text( parent,
                                SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 450;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        mappingText.setLayoutData( data );

        mappingText.setToolTipText( "Enter the rule language mapping that the \nlanguage item will be translated to." + " Use the named variables (holes) \nthat you specify in the language expression above." );

    }

    private void createExpressionField(Composite parent) {
        Label exprLbl = new Label( parent,
                                   SWT.NONE );
        exprLbl.setText( "Language expression:" );
        exprLbl.setFont( parent.getFont() );
        exprLbl.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );

        exprText = new Text( parent,
                             SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 450;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        exprText.setLayoutData( data );
        exprText.setToolTipText( "Enter the language expression that you want to use in a rule.\n" + "Use curly brackets to mark 'holes' where the values will be extracted\n" + "from in the rule source. " + "Such as: Person has a name of {name} \n"
                                 + "This will then parse the rule source to extract the data out of \n" + "the place where {name} would appear." );
    }

    private void createObjectField(Composite parent) {
        Label objectLbl = new Label( parent,
                                     SWT.NONE );
        objectLbl.setText( "Object:" );
        objectLbl.setFont( parent.getFont() );
        objectLbl.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );

        objText = new Text( parent,
                            SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 450;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        objText.setLayoutData( data );

        objText.setToolTipText( "Enter the name of the object." );

    }

    private void createScopeField(Composite parent) {

        //type
        Label scopeLbl = new Label( parent,
                                    SWT.NONE );
        scopeLbl.setText( "Scope:" );
        scopeLbl.setFont( parent.getFont() );
        scopeLbl.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );

        scopeCombo = new Combo( parent,
                                SWT.READ_ONLY );

        scopeCombo.add( SCOPE_STR_KEYWORD,
                        SCOPE_KEYWORD );
        scopeCombo.add( SCOPE_STR_WHEN,
                        SCOPE_WHEN );
        scopeCombo.add( SCOPE_STR_THEN,
                        SCOPE_THEN );
        scopeCombo.add( SCOPE_STR_ALL,
                        SCOPE_ALL );

        scopeCombo.select( SCOPE_ALL ); //the default

        scopeCombo.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING ) );
        scopeCombo.setFont( parent.getFont() );
        scopeCombo.setToolTipText( "This specifies what part of the rule the expression applies. Indicating '*' means global." );

    }

}
