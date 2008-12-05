package org.drools.eclipse.editors;

/*
 * Copyright 2006 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.InvocationTargetException;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.editors.rete.ReteViewer;
import org.drools.eclipse.editors.rete.model.ReteGraph;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * This is a multi table editor wrapper for both the text editor and the RETE
 * viewer.
 * 
 * @author Kris
 * @author Ahti Kitsik
 */
public class DRLRuleEditor2 extends FormEditor {

    private DRLRuleEditor             textEditor;

    private ReteViewer                reteViewer;

    private ZoomComboContributionItem zitem;

    private ZoomInAction2             zoomIn;
    private ZoomOutAction2            zoomOut;

    protected ReteGraph               graph;

    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site,
                     IEditorInput input) throws PartInitException {
        super.init( site,
                    input );
        setPartName( input.getName() );
    }

    /**
     * Adds Text Editor for rules and Rete graph viewer
     * 
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
            textEditor = new DRLRuleEditor() {
                public void close(boolean save) {
                    super.close( save );
                    DRLRuleEditor2.this.close( save );
                }

                protected void setPartName(String partName) {
                    super.setPartName( partName );
                    DRLRuleEditor2.this.setPartName( partName );
                }
            };

            reteViewer = new ReteViewer(textEditor);

            int text = addPage( textEditor,
                                getEditorInput() );

            int rete = addPage( reteViewer,
                                getEditorInput() );

            setPageText( text,
                         "Text Editor" );
            setPageText( rete,
                         "Rete Tree" );

            textEditor.getDocumentProvider().getDocument( getEditorInput() ).addDocumentListener( new IDocumentListener() {

                public void documentAboutToBeChanged(DocumentEvent event) {
                }

                public void documentChanged(DocumentEvent event) {
                    reteViewer.fireDocumentChanged();
                }

            } );

        } catch ( PartInitException e ) {
            DroolsEclipsePlugin.log( e );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
        textEditor.doSave( monitor );
        setInput( getEditorInput() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    public void doSaveAs() {
        textEditor.doSaveAs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return textEditor.isSaveAsAllowed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if ( adapter == ZoomManager.class ) {

            if ( getActiveEditor() instanceof ReteViewer ) {
                return reteViewer.getAdapter( adapter );
            } else if ( getActiveEditor() instanceof DRLRuleEditor ) {
                return null;
            }

        } else if ( adapter == ZoomInAction2.class ) {
            return zoomIn;
        } else if ( adapter == ZoomOutAction2.class ) {
            return zoomOut;
        } else if ( adapter == ZoomComboContributionItem.class ) {
            return zitem;
        }

        return textEditor.getAdapter( adapter );
    }

    /**
     * Updates ZoomManagers for contributed actions.
     */
    private void updateZoomItems() {
        updateZoomIn();

        updateZoomOut();

        updateZoomCombo();

    }

    private void updateZoomCombo() {
        if ( zitem != null ) {
            zitem.setZoomManager( getZoomManager() );
        }
    }

    private void updateZoomIn() {
        boolean zoomActive = getZoomManager() != null;
        if ( zoomIn != null ) {
            zoomIn.setZoomManager( getZoomManager() );
            zoomIn.setEnabled( zoomActive );
        }
    }

    private void updateZoomOut() {
        boolean zoomActive = getZoomManager() != null;
        if ( zoomOut != null ) {
            zoomOut.setZoomManager( getZoomManager() );
            zoomOut.setEnabled( zoomActive );
        }
    }

    /**
     * Sets ZoomComboContributionItem to be used for updating it's
     * ZoomManager when multipage tab is switched.
     * 
     * @param zitem contribution item
     */
    public void setZoomComboContributionItem(ZoomComboContributionItem zitem) {
        this.zitem = zitem;
        updateZoomCombo();
    }

    private ZoomManager getZoomManager() {
        return (ZoomManager) getAdapter( ZoomManager.class );
    }

    /**
     * Sets ZoomOutAction2 to be used for updating it's
     * ZoomManager when multipage tab is switched.
     * 
     * @param zoomOutAction zoom action
     */
    public void setZoomOutAction(ZoomOutAction2 zoomOutAction) {
        this.zoomOut = zoomOutAction;
        updateZoomOut();
    }

    /**
     * Sets ZoomInAction to be used for updating it's
     * ZoomManager when multipage tab is switched. 
     * @param zoomInAction zoom action
     */
    public void setZoomInAction(ZoomInAction2 zoomInAction) {
        this.zoomIn = zoomInAction;
        updateZoomIn();
    }

    public void setFocus() {
        if ( getActivePage() == 1 ) {
            boolean reteFailed = false;
            graph = null;
            try {
                final String contents = textEditor.getDocumentProvider().getDocument( getEditorInput() ).get();
                final IRunnableWithProgress runnable = new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor) throws InvocationTargetException,
                                                             InterruptedException {
                        try {
                            graph = reteViewer.loadReteModel( monitor,
                                                              contents );
                        } catch ( Throwable e ) {
                            if ( e instanceof InvocationTargetException ) {
                                throw (InvocationTargetException) e;
                            } else if ( e instanceof InterruptedException ) {
                                throw (InterruptedException) e;
                            }
                            throw new InvocationTargetException( e );
                        }

                    }

                };

                getEditorSite().getWorkbenchWindow().getWorkbench().getProgressService().busyCursorWhile( runnable );

                reteViewer.drawGraph( graph );

            } catch ( InvocationTargetException e ) {
            	System.out.println(e.getTargetException().getMessage());
            	if (e.getTargetException() != null
            			&& ReteViewer.MSG_PARSE_ERROR.equals(e.getTargetException().getMessage())) {
            		IStatus status = new Status( IStatus.ERROR,
                        DroolsEclipsePlugin.getUniqueIdentifier(),
                        -1,
                        "Unable to show Rete Tree when rules cannot be parsed correctly.",
                        null);
            		ErrorDialog.openError( getSite().getShell(),
                        "Rete Tree Build Error",
                        "Unable to parse rules, please correct rules first.",
                        status);
            	} else {
            		handleError( e );
            	}
                reteFailed = true;
            } catch ( InterruptedException e ) {
                MessageDialog.openError( getSite().getShell(),
                                         "Rete Tree Error!",
                                         "Rete Tree Calculation Cancelled!" );
                reteFailed = true;
            } catch ( Throwable t ) {
                handleError( t );
                reteFailed = true;
            }
            if ( reteFailed ) {
                setActivePage( 0 );
            }
        }

        super.setFocus();
        updateZoomItems();

    }

    private void handleError(Throwable t) {
        DroolsEclipsePlugin.log( t );
        Throwable cause = t.getCause();
        if ( cause == null ) {
            cause = t;
        }
        String message = cause.getClass().getName()+": "+cause.getMessage();
        if ( message == null || message.length() == 0 ) {
            message = "Uncategorized Error!";
        }
        IStatus status = new Status( IStatus.ERROR,
                                     DroolsEclipsePlugin.getUniqueIdentifier(),
                                     -1,
                                     message,
                                     null);
        ErrorDialog.openError( getSite().getShell(),
                               "Rete Tree Build Error!",
                               "Rete Tree Build Error!",
                               status );

    }

    /**
     * Increasing visibility to allow switching tabs by page index
     */
    public void setActivePage(int pageIndex) {
        super.setActivePage( pageIndex );
    }

}
