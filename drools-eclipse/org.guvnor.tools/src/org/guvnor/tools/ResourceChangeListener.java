package org.guvnor.tools;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;

public class ResourceChangeListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					try {
						if (delta.getKind() == IResourceDelta.ADDED) {
							handleResourceAdded(delta.getResource());
						}
						if (delta.getKind() == IResourceDelta.CHANGED) {
							handleResourceChanged(delta.getResource());
						}
						if (delta.getKind() == IResourceDelta.REMOVED) {
							handleResourceDelete(delta.getResource());
						}
						if (delta.getMovedFromPath() != null) {
							handleResourceMoved(delta.getResource(), delta.getMovedFromPath());
						}
					} catch (Exception e) {
						Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
					}
					return true;
				}
			});
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	private void handleResourceAdded(IResource resource) throws Exception {
//System.out.println("Added: " + resource.getFullPath().toString());
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
	
	private void handleResourceDelete(IResource resource) throws CoreException {
//System.out.println("Deleted: " + resource.getFullPath().toString());
		final IFile mdFile = GuvnorMetadataUtils.findGuvnorMetadata(resource);
		if (mdFile == null) {
			return;
		}
		final IWorkspace ws = mdFile.getWorkspace();
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				try {
					ws.delete(new IResource[] { mdFile }, true, null);
				} catch (CoreException e) {
					Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
				}
			}
			
		});
	}
	
	private void handleResourceMoved(final IResource resource, IPath fromPath) throws Exception {
//System.out.println("Moved: " + resource.getFullPath().toString() + " from " + fromPath.toString());
//System.out.println(GuvnorMetadataUtils.isGuvnorResourceCurrent(resource));
		IFile mdFile = GuvnorMetadataUtils.findGuvnorMetadata(fromPath);
		if (mdFile == null) {
			return;
		}
		final GuvnorMetadataProps mdProps = GuvnorMetadataUtils.loadGuvnorMetadata(mdFile);
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				try {
					GuvnorMetadataUtils.setGuvnorMetadataProps(resource.getFullPath(), mdProps);
				} catch (Exception e) {
					Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
				}
			}
		});
	}
}
