package org.guvnor.tools.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;

public class GuvnorWorkspaceFilePage extends PropertyPage implements IWorkbenchPropertyPage {

	public GuvnorWorkspaceFilePage() {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = PlatformUtils.createComposite(parent, 2);
		IResource resource = (IResource)super.getElement().getAdapter(IResource.class);
		if (resource != null) {
			displayGuvnorProperties(composite, resource);
		} else {
			indicateNotGuvnorAssociated(composite);
		}
		return composite;
	}
	
	private void displayGuvnorProperties(Composite composite, IResource resource) {
		GuvnorMetadataProps props;
		try {
			props = GuvnorMetadataUtils.getGuvnorMetadata(resource);
			if (props == null) {
				indicateNotGuvnorAssociated(composite);
			} else {
				addGuvnorProperties(composite, props);
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	private void addGuvnorProperties(Composite composite, GuvnorMetadataProps props) {
			
		new Label(composite, SWT.NONE).setText("Repository:");
		Label l = new Label(composite, SWT.NONE);
		String val = props.getRepository() != null?props.getRepository():"";
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText("Path:");
		l = new Label(composite, SWT.NONE);
		String fullpath = props.getFullpath() != null?props.getFullpath().substring(val.length()):"";
		l.setText(fullpath);
		
		new Label(composite, SWT.NONE).setText("Version:");
		l = new Label(composite, SWT.NONE);
		val = props.getVersion() != null?props.getVersion():"";
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText("Revision:");
		l = new Label(composite, SWT.NONE);
		val = props.getVersion() != null?props.getRevision():"";
		l.setText(val);
	}

	private void indicateNotGuvnorAssociated(Composite composite) {
		new Label(composite, SWT.NONE).setText("(Not associated with Guvnor)");
	}
}
