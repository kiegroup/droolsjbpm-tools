package org.guvnor.tools;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;

public class ResourceChangeListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		try {
			//TODO: Need to handle delete and move events as well
			// For delete, we just remove any corresponding guvnor metadata
			// Not clear what to do about moves...
			event.getDelta().accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					try {
						if (delta.getKind() == IResourceDelta.ADDED) {
							handleResourceAdded(delta.getResource());
						}
						if (delta.getKind() == IResourceDelta.CHANGED) {
							handleResourceChanged(delta.getResource());
						}
					} catch (Exception e) {
						Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
					}
					return true;
				}
			});
		} catch (CoreException e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	private void handleResourceAdded(IResource resource) throws Exception {
		if (GuvnorMetadataUtils.isGuvnorMetadata(resource)) {
			// Look for the corresponding file
			IFile target = GuvnorMetadataUtils.getGuvnorControlledResource(resource);
			if (target != null) {
				GuvnorMetadataUtils.markCurrentGuvnorResource(target);
			}
		} else {
			// Look for the corresponding metadata
			if (GuvnorMetadataUtils.isGuvnorControlledResource(resource)) {
				GuvnorMetadataUtils.markCurrentGuvnorResource(resource);
			}
		}
	}
	
	private void handleResourceChanged(IResource resource) throws CoreException {
		if (GuvnorMetadataUtils.getGuvnorResourceProperty(resource) != null) {
			GuvnorMetadataUtils.markExpiredGuvnorResource(resource);
			PlatformUtils.updateDecoration();
		}
	}
}
