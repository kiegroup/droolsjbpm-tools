/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.tools.utils;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.guvnor.tools.views.model.ResourceHistoryEntry;

/**
 * Sorts resource versions based on revision number.
 */
public class ResourceHistorySorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof ResourceHistoryEntry
           && e2 instanceof ResourceHistoryEntry) {
            ResourceHistoryEntry entry1 = (ResourceHistoryEntry)e1;
            ResourceHistoryEntry entry2 = (ResourceHistoryEntry)e2;
            return Integer.parseInt(entry2.getRevision()) - Integer.parseInt(entry1.getRevision());
        } else {
            return super.compare(viewer, e1, e2);
        }
    }

}
