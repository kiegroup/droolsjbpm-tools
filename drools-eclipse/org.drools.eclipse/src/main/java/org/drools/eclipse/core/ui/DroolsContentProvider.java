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

import org.drools.eclipse.core.DroolsElement;
import org.drools.eclipse.core.Rule;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for Drools model elements. 
 */
public class DroolsContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Rule) {
            Rule rule = (Rule) parentElement;
            if (rule.getGroup()==null) {
                return new Object[]{};
            }
            return new Object[]{rule.getGroup()};
        }
        if (parentElement instanceof DroolsElement) {
            return ((DroolsElement) parentElement).getChildren();
        }
        return new Object[0];
    }

    public Object getParent(Object element) {
        if (element instanceof DroolsElement) {
            return ((DroolsElement) element).getParent();
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    public void dispose() {
        // do nothing
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing
    }

}
