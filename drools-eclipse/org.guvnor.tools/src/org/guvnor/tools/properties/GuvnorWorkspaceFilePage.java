package org.guvnor.tools.properties;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class GuvnorWorkspaceFilePage extends PropertyPage implements IWorkbenchPropertyPage {

	public GuvnorWorkspaceFilePage() {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent, 2);
		IResource resource = (IResource)super.getElement().getAdapter(IResource.class);
		if (resource != null) {
			displayGuvnorProperties(composite, findGuvnorMetadata(resource));
		} else {
			indicateNotGuvnorAssociated(composite);
		}
		return composite;
	}
	
	private void displayGuvnorProperties(Composite composite, IFile md) {
		if (md == null) {
			indicateNotGuvnorAssociated(composite);
		} else {
			try {
				Properties props = new Properties();
				props.load(md.getContents());
				addGuvnorProperties(composite, props);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void addGuvnorProperties(Composite composite, Properties props) {
		new Label(composite, SWT.NONE).setText("Local name:");
		Label l = new Label(composite, SWT.NONE);
		String val = props.getProperty("filename") != null?props.getProperty("filename"):"";
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText("Repository:");
		l = new Label(composite, SWT.NONE);
		val = props.getProperty("repository") != null?props.getProperty("repository"):"";
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText("Repository Path:");
		l = new Label(composite, SWT.NONE);
		val = props.getProperty("fullpath") != null?props.getProperty("fullpath"):"";
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText("Repository Version:");
		l = new Label(composite, SWT.NONE);
		val = props.getProperty("lastmodified") != null?props.getProperty("lastmodified"):"";
		l.setText(val);
	}

	private void indicateNotGuvnorAssociated(Composite composite) {
		new Label(composite, SWT.NONE).setText("(Not associated with Guvnor)");
	}
	
	private IFile findGuvnorMetadata(IResource resource) {
		IFile res = null;
		IPath dir = resource.getFullPath().removeLastSegments(1);
		IPath mdpath = dir.append(".guvnorinfo").append(resource.getName());
		IResource mdResource = resource.getWorkspace().getRoot().findMember(mdpath);
		if (mdResource != null 
		   && mdResource.exists() 
		   && mdResource instanceof IFile) {
			res = (IFile)mdResource;
		}
		return res;
	}
	
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}
}
