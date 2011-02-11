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

package org.guvnor.tools.views;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.guvnor.tools.views.model.ResourceHistoryEntry;
/**
 * Label provider for the Resource History view.
 *
 */
public class ResourceHistoryLabelProvider implements ITableLabelProvider {

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        if (!(element instanceof ResourceHistoryEntry)) {
            return element.toString();
        }
        ResourceHistoryEntry entry = (ResourceHistoryEntry) element;
        String res = null;
        switch (columnIndex) {
        case 0:
            res = entry.getRevision();
            break;
        case 1:
            res = entry.getDate();
            break;
        case 2:
            res = entry.getAuthor();
            break;
        case 3:
            res = entry.getComment();
            break;
        default:
            res = entry.toString();
            break;
        }
        return res;

    }

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
}
