package org.drools.ide.editors;


import org.drools.ide.builder.DroolsBuilder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * The Drools parse action.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ParseActionDelegate implements IEditorActionDelegate {

    private IEditorPart editor;
    
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        editor = targetEditor;
    }

    public void run(IAction action) {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IFileEditorInput) {
            editor.doSave(null);
            DroolsBuilder.parseResource(((IFileEditorInput) input).getFile());
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

}
