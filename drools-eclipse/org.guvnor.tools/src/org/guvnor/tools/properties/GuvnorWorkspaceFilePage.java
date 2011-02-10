/*
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
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;

/**
 * A property page for displaying Guvnor details for a given resource.
 * @author jgraham
 */
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
			
		new Label(composite, SWT.NONE).setText(Messages.getString("repository.label")); //$NON-NLS-1$
		Label l = new Label(composite, SWT.NONE);
		String val = props.getRepository() != null?props.getRepository():""; //$NON-NLS-1$
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText(Messages.getString("repository.path")); //$NON-NLS-1$
		l = new Label(composite, SWT.NONE);
		String fullpath = props.getFullpath() != null?props.getFullpath().substring(val.length()):""; //$NON-NLS-1$
		l.setText(fullpath);
		
		new Label(composite, SWT.NONE).setText(Messages.getString("repository.resource.version")); //$NON-NLS-1$
		l = new Label(composite, SWT.NONE);
		val = props.getVersion() != null?props.getVersion():""; //$NON-NLS-1$
		l.setText(val);
		
		new Label(composite, SWT.NONE).setText(Messages.getString("repository.resource.revision")); //$NON-NLS-1$
		l = new Label(composite, SWT.NONE);
		val = props.getVersion() != null?props.getRevision():""; //$NON-NLS-1$
		l.setText(val);
	}

	private void indicateNotGuvnorAssociated(Composite composite) {
		new Label(composite, SWT.NONE).setText(Messages.getString("not.guvnor.associated")); //$NON-NLS-1$
	}
}
