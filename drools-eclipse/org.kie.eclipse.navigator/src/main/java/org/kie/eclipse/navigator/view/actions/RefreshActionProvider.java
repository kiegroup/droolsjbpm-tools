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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.kie.eclipse.navigator.view.content.IContainerNode;

/**
 * Provides a refresh action for IContainer nodes.
 */
@SuppressWarnings("restriction")
public class RefreshActionProvider extends KieNavigatorActionProvider {

    public RefreshActionProvider() {
    }

    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        addAction(new RefreshAction(aSite.getStructuredViewer()));
    }

    private class RefreshAction extends KieNavigatorAction {

        public RefreshAction(ISelectionProvider selectionProvider) {
            super(selectionProvider, WorkbenchMessages.Workbench_refresh);
        }

        public void run() {
            IContainerNode<?> container = getContainer();
            if (container==null)
            	return;
            
            refreshViewer(container);
        }

    }
}
