/**
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

package org.guvnor.tools.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.PendingUpdateAdapter;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.views.model.TreeParent;
import org.guvnor.tools.views.model.TreeObject.Type;

/**
 * Label providers for Guvnor structure.
 * @author jgraham
 *
 */
public class RepositoryLabelProvider extends LabelProvider {
	private Image repImage;
	
	public RepositoryLabelProvider() {
		repImage = Activator.getImageDescriptor(Activator.IMG_GUVCONTROLLED).createImage();
	}
	
	public String getText(Object obj) {
		if (obj instanceof PendingUpdateAdapter) {
			return Messages.getString("pending"); //$NON-NLS-1$
		} else {
			return obj.toString();
		}
	}
	public Image getImage(Object obj) {
		Image res = null;
		if (obj instanceof TreeParent) {
			Type t = ((TreeParent)obj).getNodeType();
			if (t == Type.REPOSITORY) {
				res = repImage;
			}
			if (t == Type.PACKAGE) {
				res = PlatformUI.getWorkbench().getSharedImages().
										getImage(ISharedImages.IMG_OBJ_FOLDER);
			}
			if (t == Type.RESOURCE) {
				res = PlatformUI.getWorkbench().getSharedImages().
										getImage(ISharedImages.IMG_OBJ_FILE);
			}
		}
		return res != null?res:PlatformUI.getWorkbench().getSharedImages().
												getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}

}
