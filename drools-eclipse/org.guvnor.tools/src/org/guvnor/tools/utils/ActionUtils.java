package org.guvnor.tools.utils;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.guvnor.tools.Activator;

/**
 * Convenience methods for actions.
 * @author jgraham
 */
public class ActionUtils {
	
	@SuppressWarnings("unchecked")
	public static boolean checkResourceSet(ISelection selection, boolean guvnorControlled) {
		boolean res = true;
		try {
			if (!(selection instanceof IStructuredSelection)) {
				return false;
			}
			IStructuredSelection sel = (IStructuredSelection)selection;
			for (Iterator<Object> it = sel.iterator(); it.hasNext();) {
				Object oneSelection = it.next();
				if (oneSelection instanceof IFile) {
					GuvnorMetadataProps props = 
						GuvnorMetadataUtils.getGuvnorMetadata((IFile)oneSelection);
					if ((guvnorControlled && props == null)
					   || (!guvnorControlled && props != null)) {
						res = false;
						break;
					}
				}
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
			res = false;
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean areFilesDirty(ISelection selection) {
		boolean res = true;
		try {
			if (!(selection instanceof IStructuredSelection)) {
				return false;
			}
			IStructuredSelection sel = (IStructuredSelection)selection;
			for (Iterator<Object> it = sel.iterator(); it.hasNext();) {
				Object oneSelection = it.next();
				if (oneSelection instanceof IFile) {
					boolean isCurrent = 
						GuvnorMetadataUtils.isGuvnorResourceCurrent((IFile)oneSelection);
					if (isCurrent) {
						res = false;
						break;
					}
				}
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
			res = false;
		}
		return res;
	}
}
