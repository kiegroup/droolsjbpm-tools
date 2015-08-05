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

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * Provides a refresh action for IContainer nodes.
 */
public class RefreshActionProvider extends KieNavigatorActionProvider {

	public RefreshActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		IViewSite site = (IViewSite) aSite.getViewSite().getAdapter(IViewSite.class);
		IActionBars bars = site.getActionBars();
		RefreshAction refreshAction = new RefreshAction(aSite.getViewSite().getSelectionProvider());
		bars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		addAction(refreshAction);
	}
}
