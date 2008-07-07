package org.guvnor.tools;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;

public class GuvnorDecorator implements ILightweightLabelDecorator {
	
	public static final String DECORATOR_ID = "org.guvnor.tools.decorator";
	
	private boolean isGuvnorResource(Object element) {
		if (element instanceof IResource) {
			return GuvnorMetadataUtils.findGuvnorMetadata((IResource)element) != null;
		} else {
			return false;
		}
	}
	
	private void decorateResource(IResource resource, IDecoration decoration) {
		try {	
			if (resource instanceof IFile) {
				decoration.addOverlay(Activator.getImageDescriptor(Activator.IMG_GUVCONTROLLED), 
						             IDecoration.TOP_RIGHT);
				if (!GuvnorMetadataUtils.isGuvnorResourceCurrent(resource)) {
					decoration.addSuffix(" >");
				}
				GuvnorMetadataProps props = GuvnorMetadataUtils.getGuvnorMetadata(resource);
				if (props.getVersion() != null) {
					decoration.addSuffix(" " + props.getVersion());
				}
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	public void decorate(Object element, IDecoration decoration) {
		if (isGuvnorResource(element)) {
			if (element instanceof IResource) {
				decorateResource((IResource)element, decoration);
			} else {
				Activator.getDefault().writeLog(IStatus.ERROR, 
						"Called to decorate unknown: " + element.getClass().toString(), new Exception());
			}
		}
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO: Need to implement this?
	}

	public void dispose() { }

	public boolean isLabelProperty(Object element, String property) {
		// TODO: Need to implement this?
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO: Need to implement this?
	}
}
