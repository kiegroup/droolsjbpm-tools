package org.guvnor.tools.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;

public class GuvnorWorkspaceFilePage extends PropertyPage implements IWorkbenchPropertyPage {

	public GuvnorWorkspaceFilePage() {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent, 2);
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
		new Label(composite, SWT.NONE).setText("Local name:");
		Label l = new Label(composite, SWT.NONE);
		String val = props.getFilename() != null?props.getFilename():"";
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText("Repository:");
		l = new Label(composite, SWT.NONE);
		val = props.getRepository() != null?props.getRepository():"";
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText("Repository Path:");
		l = new Label(composite, SWT.NONE);
		val = props.getFullpath() != null?props.getFullpath():"";
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText("Repository Version:");
		l = new Label(composite, SWT.NONE);
		val = props.getVersion() != null?props.getVersion():"";
		l.setText(val);
	}

	private void indicateNotGuvnorAssociated(Composite composite) {
		new Label(composite, SWT.NONE).setText("(Not associated with Guvnor)");
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
