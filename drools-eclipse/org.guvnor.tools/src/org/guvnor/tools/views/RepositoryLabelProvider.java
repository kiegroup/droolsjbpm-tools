package org.guvnor.tools.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.PendingUpdateAdapter;
import org.guvnor.tools.Activator;
import org.guvnor.tools.views.model.TreeParent;
import org.guvnor.tools.views.model.TreeObject.Type;

public class RepositoryLabelProvider extends LabelProvider {
	private Image repImage;
	
	public RepositoryLabelProvider() {
		repImage = Activator.getImageDescriptor(Activator.IMG_GUVCONTROLLED).createImage();
	}
	
	public String getText(Object obj) {
		if (obj instanceof PendingUpdateAdapter) {
			return "Pending...";
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
