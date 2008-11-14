package org.drools.eclipse.flow.common.editor;
/*
 * Copyright 2005 JBoss Inc
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EventObject;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Abstract implementation of a graphical editor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class GenericModelEditor extends GraphicalEditorWithPalette { // implements ITabbedPropertySheetPageContributor {

	private Object model;
	private boolean savePreviouslyNeeded = false;
	private KeyHandler sharedKeyHandler;
	private PaletteRoot root;
	private OverviewOutlinePage overviewOutlinePage;

	public GenericModelEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	protected void setModel(Object model) {
		this.model = model;
	}

	public Object getModel() {
		return model;
	}

	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();

		IAction action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.LEFT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.CENTER);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.RIGHT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.TOP);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.MIDDLE);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.BOTTOM);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
	}

	public void commandStackChanged(EventObject event) {
		if (isDirty()) {
			if (!savePreviouslyNeeded()) {
				setSavePreviouslyNeeded(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
		} else {
			setSavePreviouslyNeeded(false);
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		super.commandStackChanged(event);
	}

	protected abstract void writeModel(OutputStream os) throws IOException;

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		getGraphicalViewer().setRootEditPart(new ScalableRootEditPart());
		getGraphicalViewer().setEditPartFactory(createEditPartFactory());
		getGraphicalViewer().setKeyHandler(
				new GraphicalViewerKeyHandler(getGraphicalViewer())
						.setParent(getCommonKeyHandler()));

		IAction showGrid = new ToggleGridAction(getGraphicalViewer());
		getActionRegistry().registerAction(showGrid);

		ContextMenuProvider provider = new GenericContextMenuProvider(
				getGraphicalViewer(), getActionRegistry());
		getGraphicalViewer().setContextMenu(provider);
		getSite().registerContextMenu("org.drools.eclipse.flow.editor.contextmenu",
				provider, getGraphicalViewer());
	}
	
	protected abstract EditPartFactory createEditPartFactory();

	protected void initializeGraphicalViewer() {
		getGraphicalViewer().setContents(model);
	}

	public void doSave(IProgressMonitor monitor) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			writeModel(out);
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.setContents(new ByteArrayInputStream(out.toByteArray()), true,
					false, monitor);
			out.close();
			getCommandStack().markSaveLocation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doSaveAs() {
		SaveAsDialog dialog = new SaveAsDialog(getSite().getWorkbenchWindow()
				.getShell());
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();
		IPath path = dialog.getResult();

		if (path == null) {
			return;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IFile file = workspace.getRoot().getFile(path);

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor)
					throws CoreException {
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					writeModel(out);
					file.create(new ByteArrayInputStream(out.toByteArray()),
							true, monitor);
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		try {
			new ProgressMonitorDialog(getSite().getWorkbenchWindow().getShell())
					.run(false, true, op);
			setInput(new FileEditorInput(file));
			getCommandStack().markSaveLocation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected KeyHandler getCommonKeyHandler() {
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();
			sharedKeyHandler
					.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
							getActionRegistry().getAction(
									ActionFactory.DELETE.getId()));
			sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
					getActionRegistry().getAction(
							GEFActionConstants.DIRECT_EDIT));
		}
		return sharedKeyHandler;
	}

	public boolean isDirty() {
		return isSaveOnCloseNeeded();
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	public boolean isSaveOnCloseNeeded() {
		return getCommandStack().isDirty();
	}

	private boolean savePreviouslyNeeded() {
		return savePreviouslyNeeded;
	}

	private void setSavePreviouslyNeeded(boolean value) {
		savePreviouslyNeeded = value;
	}

	protected PaletteRoot getPaletteRoot() {
		if (root == null) {
			root = createPalette();
		}
		return root;
	}

	protected abstract PaletteRoot createPalette();

	protected void setInput(IEditorInput input) {
		super.setInput(input);

		IFile file = getFile();
		setPartName(file.getName());
		try {
			InputStream is = file.getContents(false);
			createModel(is);
		} catch (Throwable t) {
			DroolsEclipsePlugin.log(t);
		}
		if (getGraphicalViewer() != null) {
			initializeGraphicalViewer();
		}
	}
	
	public IFile getFile() {
	    return ((IFileEditorInput) getEditorInput()).getFile();
	}
	
	public IProject getProject() {
		IFile file = getFile();
		if (file != null) {
			return file.getProject();
		}
		return null;
	}
	
	public IJavaProject getJavaProject() {
		IProject project = getProject();
		if (project != null) {
			try {
				if (project.getNature("org.eclipse.jdt.core.javanature") != null) {
	                IJavaProject javaProject = JavaCore.create(project);
	                if (javaProject.exists()){
	                	return javaProject;
	                }
	            }
			} catch (CoreException e) {
				DroolsEclipsePlugin.log(e);
			}
		}
		return null;
	}
	
	protected abstract void createModel(InputStream is);

	public Object getAdapter(Class type) {
		if (type == IContentOutlinePage.class) {
			return getOverviewOutlinePage();
		}
		if (type == ZoomManager.class) {
			return ((ScalableRootEditPart) getGraphicalViewer()
					.getRootEditPart()).getZoomManager();
		}
//		if (type == IPropertySheetPage.class) {
//            return new TabbedPropertySheetPage(this);
//		}
		return super.getAdapter(type);
	}

	protected OverviewOutlinePage getOverviewOutlinePage() {
		if (null == overviewOutlinePage && null != getGraphicalViewer()) {
			ScalableRootEditPart rootEditPart = (ScalableRootEditPart) getGraphicalViewer()
					.getRootEditPart();
			overviewOutlinePage = new OverviewOutlinePage(rootEditPart);
		}
		return overviewOutlinePage;
	}
	
	public String getContributorId() {
	    return getSite().getId();
	}
	
	/**
	 * Writes the content of this editor to the given stream.
	 * Possible formats are for example SWT.IMAGE_BMP, IMAGE_GIF,
	 * IMAGE_JPEG, IMAGE_PNG.
	 * @param stream
	 * @param format
	 */
	public void createImage(OutputStream stream, int format) {
        SWTGraphics g = null;
        GC gc = null;
        Image image = null;
        LayerManager layerManager = (LayerManager)
            getGraphicalViewer().getEditPartRegistry().get(LayerManager.ID);
        IFigure figure = layerManager.getLayer(LayerConstants.PRINTABLE_LAYERS);
        Rectangle r = figure.getBounds();
        try {
            image = new Image(Display.getDefault(), r.width, r.height);
            gc = new GC(image);
            g = new SWTGraphics(gc);
            g.translate(r.x * -1, r.y * -1);
            figure.paint(g);
            ImageLoader imageLoader = new ImageLoader();
            imageLoader.data = new ImageData[] { image.getImageData() };
            imageLoader.save(stream, format);
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
	    } finally {
            if (g != null) {
                g.dispose();
            }
            if (gc != null) {
                gc.dispose();
            }
            if (image != null) {
                image.dispose();
            }
        }
	}
}
