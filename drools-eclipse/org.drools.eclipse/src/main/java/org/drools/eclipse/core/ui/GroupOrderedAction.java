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

package org.drools.eclipse.core.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;

public class GroupOrderedAction extends Action {
    private StructuredViewer viewer;
    private String viewerId;

    public GroupOrderedAction(StructuredViewer viewer, String viewerId) {
        this.viewer = viewer;
        this.viewerId = viewerId;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if (true) {
            viewer.getControl().setRedraw(false);
            BusyIndicator.showWhile(viewer.getControl().getDisplay(), new Runnable() {
                public void run() {
                    viewer.refresh();
                }
            });
            viewer.getControl().setRedraw(true);
        }
    }

}
