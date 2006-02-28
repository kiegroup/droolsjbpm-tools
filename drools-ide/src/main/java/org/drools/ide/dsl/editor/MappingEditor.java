package org.drools.ide.dsl.editor;

import org.drools.lang.dsl.template.NLMappingItem;
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

    private static final int SCOPE_WHEN = 0;
    private static final int SCOPE_THEN = 1;
    private static final int SCOPE_ALL = 2;    
    
    private Text exprText;
    private Text mappingText;
    private Combo scopeCombo;
    private boolean cancelled;

    private NLMappingItem model;    
    
    protected MappingEditor(Shell parent) {
        super( parent );
    }
        
    /**
     * Pass in a NLMapping item for display/edits.
     * Changes will be applied to this object only if the user clicks OK.
     */
    public void setNLMappingItem(NLMappingItem item) {
        model = item;
        setScope( model.getScope() );
        exprText.setText( model.getNaturalTemplate() );
        mappingText.setText( model.getTargetTemplate() );
    }
    
    
    private void setScope(String scope) {
        if (scope.equals( "when" )) {
            scopeCombo.select( SCOPE_WHEN );
        } else if (scope.equals( "then" )) {
            scopeCombo.select( SCOPE_THEN );
        } else if (scope.equals( "*" )) {
            scopeCombo.select( SCOPE_ALL );
        } else {
            throw new IllegalArgumentException("Unknown scope type: " + scope);
        }
    }
    
    
    protected void cancelPressed() {
        this.cancelled = true;
        super.cancelPressed();
    }



    protected void okPressed() {
        this.cancelled = false;
        this.model.setNaturalTemplate( this.exprText.getText() );
        this.model.setTargetTemplate( this.mappingText.getText() );
        this.model.setScope( this.scopeCombo.getText() );
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
        createScopeField( parent );
        
        // create the top level composite wrapper
        Composite composite = new Composite( parent,
                                             SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        layout.verticalSpacing = 10;
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ));
        composite.setFont( parent.getFont() );    
        
        return composite;
    }



    private void createMappingField(Composite parent) {
        Label mappingLbl = new Label( parent,
                                      SWT.NONE );
        mappingLbl.setText( "Rule mapping:" );
        mappingLbl.setFont( parent.getFont() );
        mappingLbl.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_END) );        

        mappingText = new Text( parent,
                                SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 450;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        mappingText.setLayoutData( data );

        mappingText.setToolTipText( "Enter the rule language mapping that the \nlanguage item will be translated to." +
                " Use the named variables (holes) \nthat you specify in the language expression above." );
        
    }

    private void createExpressionField(Composite parent) {
        Label exprLbl = new Label( parent,
                                   SWT.NONE );
        exprLbl.setText( "Language expression:" );
        exprLbl.setFont( parent.getFont() );
        exprLbl.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_END) );        

        exprText = new Text( parent,
                             SWT.BORDER );
        GridData data = new GridData();
        data.widthHint = 450;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        exprText.setLayoutData( data );
        exprText.setToolTipText( "Enter the language expression that you want to use in a rule.\n" +
                "Use curly brackets to mark 'holes' where the values will be extracted\n" +
                "from in the rule source. " +
                "Such as: Person has a name of {name} \n" +
                "This will then parse the rule source to extract the data out of \n" +
                "the place where {name} would appear." );
    }
    
    private void createScopeField(Composite parent) {
        
        //type
        Label scopeLbl = new Label(parent, SWT.NONE);
        scopeLbl.setText( "Scope:" );
        scopeLbl.setFont( parent.getFont() );
        scopeLbl.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_END) );
        
        scopeCombo = new Combo( parent,
                           SWT.READ_ONLY);
        
        scopeCombo.add( "when", SCOPE_WHEN );
        scopeCombo.add( "then", SCOPE_THEN );
        scopeCombo.add( "*", SCOPE_ALL );
        
        scopeCombo.select( SCOPE_ALL ); //the default
        
        scopeCombo.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING) );
        scopeCombo.setFont( parent.getFont() );        
        scopeCombo.setToolTipText( "This specifies what part of the rule the expression applies. Indicating '*' means global." );
        
    }

}
