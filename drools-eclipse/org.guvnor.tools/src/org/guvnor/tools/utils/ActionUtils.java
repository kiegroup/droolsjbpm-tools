/*
 * Copyright 2010 JBoss Inc
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
					if(!(((IFile)oneSelection).getName().indexOf(".") > 0)){
						res = false;
						break;
					}
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
