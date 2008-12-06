/*
 * Created on 11-jan-2005
 *
 */
package org.drools.eclipse.wizard.rule;

import java.io.IOException;
import java.io.InputStream;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

/**
 * A page to create a new .drl package/package file.
 * There is only one page for this wizard, its very simple.
 * 
 * Enhancements may be made to allow configuration of semantic languages, DSLs (locate a DSL) and other 
 * package level options.
 * 
 * @author Michael Neale
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class NewRulePackagePage extends WizardNewFileCreationPage {

    private static final int TYPE_RULE = 1;
    private static final int TYPE_PACKAGE = 0;
    private IWorkbench workbench;
    private Combo  ruleFileType;
    private Button  expander;
    private Button  function;
    private Text    packageName;
    
    
    public NewRulePackagePage(IWorkbench workbench, IStructuredSelection selection) {
        super("createDRLFilePage", selection);
        setTitle("New Rules File");
        setDescription("Create a new rules file (drl)");
        this.workbench = workbench;
    }

    protected void createAdvancedControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout( layout );
        setControl( container );

        //setup the controls.
        createType( container );
        createDSL( container );
        createFunctions( container );
        createPackageName( container );
        
        super.createAdvancedControls( parent );
    }
    
    protected boolean validatePage() {
    	return super.validatePage() && validate();
    }

    private void createPackageName(Composite container) {
        //package name
        Label pack = new Label(container, SWT.NONE);
        pack.setText( "Rule package name:" );
        pack.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_END) );
        pack.setFont( this.getFont() );
        packageName = new Text(container, SWT.BORDER);
        packageName.setLayoutData(  new GridData(GridData.FILL_HORIZONTAL) );
        packageName.setToolTipText( "Rules require a namespace." );
        packageName.setFont( this.getFont() );
        packageName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
        });
    }

    private void createFunctions(Composite container) {
        //function
        Label func = new Label(container, SWT.NONE);
        func.setText( "Use functions:" );
        func.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_END) );
        func.setFont( this.getFont() );
        function = new Button(container, SWT.CHECK);
        function.setSelection( false );
        function.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING) );
        function.setToolTipText( "Functions are methods you embed in your rule source." );
    }

    private void createDSL(Composite container) {
        //expander
        Label exp = new Label(container, SWT.NONE);
        exp.setText( "Use a DSL:" );
        exp.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_END) );
        exp.setFont( this.getFont() );
        expander = new Button(container, SWT.CHECK);
        expander.setSelection( false );
        expander.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING) );
        expander.setToolTipText( "Domain Specific Language: allows you to create your own domain specific languages\n for use in rules." );
    }

    private void createType(Composite container) {
        //type
        Label type = new Label(container, SWT.NONE);
        type.setText( "Type of rule resource:" );
        type.setFont( this.getFont() );
        type.setLayoutData( new GridData(GridData.HORIZONTAL_ALIGN_END) );
        ruleFileType = new Combo( container,
                           SWT.READ_ONLY);
        ruleFileType.add( "New DRL (rule package)", TYPE_PACKAGE );
        ruleFileType.add( "New Rule (individual rule)", TYPE_RULE );
        ruleFileType.select( 0 );
        ruleFileType.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
        ruleFileType.setFont( this.getFont() );
        
    }
    
    public boolean finish() {
        if (!validate()) {
            return false;
        }
        String fileName = getFileName();
        String extension = expander.getSelection() ? ".dslr" : ".drl";
        if (!fileName.endsWith(extension)) {
            setFileName(fileName + extension);
        }
        org.eclipse.core.resources.IFile newFile = createNewFile();
        if (newFile == null)
            return false;
        try {
            IWorkbenchWindow dwindow = workbench.getActiveWorkbenchWindow();
            org.eclipse.ui.IWorkbenchPage page = dwindow.getActivePage();
            if (page != null)
                IDE.openEditor(page, newFile, true);
        } catch (PartInitException e) {
            DroolsEclipsePlugin.log(e);
            return false;
        }
        return true;
    }

    private boolean validate() {
        if (this.packageName.getText() == null || packageName.getText().equals( "" )) {
            setErrorMessage( "You must provide a rule package name" );
            return false;
        } else {
            return true;
        }
    }
    
    protected InputStream getInitialContents() {
        try {
            DRLGenerator gen = new DRLGenerator();
            if (this.ruleFileType.getSelectionIndex() == TYPE_RULE) {
                InputStream template = getTemplate("org/drools/eclipse/wizard/rule/new_rule.drl.template");                
                return gen.generateRule( this.packageName.getText(), 
                                         template );
            } else {
                InputStream template = getTemplate("org/drools/eclipse/wizard/rule/new_package.drl.template");                
                return gen.generatePackage( this.packageName.getText(), 
                                            function.getSelection(), 
                                            expander.getSelection(), 
                                            template );
            }
        } catch (IOException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private InputStream getTemplate(String templatePath) throws IOException {
        return DroolsEclipsePlugin.getDefault().getBundle().getResource(templatePath).openStream();
    }
    
}
