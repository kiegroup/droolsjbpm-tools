package org.drools.eclipse.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.rulebuilder.wizards.NewBrlFileWizard;
import org.drools.eclipse.wizard.decisiontable.NewDTFileWizard;
import org.drools.eclipse.wizard.dsl.NewDSLFileWizard;
import org.drools.eclipse.wizard.project.NewDroolsProjectWizard;
import org.drools.eclipse.wizard.rule.NewRulePackageWizard;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

/**
 * Menu driver for launching wizards etc from the top level toolbar.
 * 
 * More can be added to this as needed.
 * 
 * @author Michael Neale
 */
public class RuleHelperActionDelegate
    implements
    IWorkbenchWindowPulldownDelegate {

    private IWorkbench workbench;
    private Menu menu;

    /** Return a menu which launches the various wizards */
    public Menu getMenu(Control parent) {

        setMenu( new Menu( parent ) );
        
        final Shell shell = parent.getShell();
        addProjectWizard( menu,
                shell );

        addRuleWizard( menu,
                       shell );

        addDSLWizard( menu,
                      shell );
        
        addDTWizard( menu,
                      shell );        
        
        addGuidedEditorWizard( menu,
                               shell );
        
        return menu;
    }
    
    private void setMenu(Menu menu) {
        if (this.menu != null) {
            this.menu.dispose();
        }
        this.menu = menu;
    }

    private void addDTWizard(Menu menu,
                             final Shell shell) {
        MenuItem dsl = new MenuItem( menu,
                                     SWT.NONE );
        dsl.setText( "New Decision Table" );
        dsl.addSelectionListener( new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                NewDTFileWizard wizard = new NewDTFileWizard();
                launchWizard( shell, wizard );
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
    }

    private void addProjectWizard(Menu menu, final Shell shell) {
		MenuItem rule = new MenuItem(menu, SWT.NONE);
		rule.setText("New Drools Project");

		rule.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				NewDroolsProjectWizard wizard = new NewDroolsProjectWizard();
				launchWizard(shell, wizard);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

    private void addRuleWizard(Menu menu,
                               final Shell shell) {
        MenuItem rule = new MenuItem( menu,
                                      SWT.NONE );
        rule.setText( "New Rule" );

        rule.addSelectionListener( new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                NewRulePackageWizard wizard = new NewRulePackageWizard();
                launchWizard( shell,
                              wizard );
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }

    private void addDSLWizard(Menu menu,
                              final Shell shell) {
        MenuItem dsl = new MenuItem( menu,
                                     SWT.NONE );
        dsl.setText( "New Domain Specific Language" );
        dsl.addSelectionListener( new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                NewDSLFileWizard wizard = new NewDSLFileWizard();
                launchWizard( shell, wizard );
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }
    
    private void addGuidedEditorWizard(Menu menu,
                                       final Shell shell) {
        MenuItem dsl = new MenuItem( menu,
                                     SWT.NONE );
        dsl.setText( "New Business Rule (using the guided editor)" );
        dsl.addSelectionListener( new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                NewBrlFileWizard wizard = new NewBrlFileWizard();
                launchWizard( shell,
                              wizard );
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        } );
    }

    private void launchWizard(Shell shell,
                              INewWizard wizard) {
        wizard.init( workbench,
                     new DummySelection() );
        WizardDialog dialog = new WizardDialog( shell,
                                                wizard );
        dialog.open();
    }    

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
        workbench = window.getWorkbench();
    }

    public void run(IAction action) {
    }

    public void selectionChanged(IAction action,
                                 ISelection selection) {
    }
    
    /** Stub structured selection listener, as is required to launch the wizard */
    static class DummySelection implements IStructuredSelection {
        public Object getFirstElement() {
            return null;
        }

        public Iterator iterator() {
            return (new ArrayList()).iterator();
        }

        public int size() {
            return 0;
        }

        public Object[] toArray() {
            return null;
        }

        public List toList() {
            return null;
        }

        public boolean isEmpty() {
            return true;
        }        
    }

}
