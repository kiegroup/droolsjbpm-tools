package org.guvnor.tools;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Updates local Guvnor properties when changes occur in the Eclipse workspace.
 * @author jgraham
 */
public class ResourceChangeListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		final List<IResource> toDelete = new ArrayList<IResource>();
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
							handleResourceDelete(delta.getResource(), toDelete);
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
			deleteResources(toDelete);
		} catch (Exception e) {
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
	
	private void deleteResources(final List<IResource> resources) throws CoreException {
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				IWorkspace ws = Activator.getDefault().getWorkspace();
				try {
					IResource[] res = new IResource[resources.size()];
					resources.toArray(res);
					ws.delete(res, true, null);
				} catch (CoreException e) {
					Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
				}
			}
			
		});
	}
	
	private void handleResourceDelete(IResource resource, List<IResource> resources) {
		final IFile mdFile = GuvnorMetadataUtils.findGuvnorMetadata(resource);
		if (mdFile == null) {
			return;
		}
		resources.add(mdFile);
	}
	
	private void handleResourceMoved(final IResource resource, IPath fromPath) throws Exception {
		IFile mdFile = GuvnorMetadataUtils.findGuvnorMetadata(fromPath);
		if (mdFile == null) {
			return;
		}
		final GuvnorMetadataProps mdProps = GuvnorMetadataUtils.loadGuvnorMetadata(mdFile);
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.asyncExec(new Runnable() {
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
