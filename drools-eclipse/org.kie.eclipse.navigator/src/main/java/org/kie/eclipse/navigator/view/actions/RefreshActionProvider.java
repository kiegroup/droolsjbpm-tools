/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * Provides a refresh action for IContainer nodes.
 */
public class RefreshActionProvider extends KieNavigatorActionProvider {

	private RefreshAction refreshAction;
	
	public RefreshActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		refreshAction = new RefreshAction(aSite.getViewSite().getSelectionProvider());
	}

    @Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(refreshAction.getId(), refreshAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, refreshAction);
    }
}
