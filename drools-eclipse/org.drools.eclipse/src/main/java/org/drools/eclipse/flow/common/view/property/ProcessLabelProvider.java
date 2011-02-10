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

package org.drools.eclipse.flow.common.view.property;

import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPart;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;

public class ProcessLabelProvider implements ILabelProvider {

    public Image getImage(Object element) {
        return null;
    }

    public String getText(Object element) {
        if (element instanceof IStructuredSelection) {
            element = ((IStructuredSelection)element).getFirstElement();
        }
        if (element instanceof ElementEditPart) {
            return "Element " + ((ElementEditPart) element).getElementWrapper().getName();
        } else if (element instanceof ProcessEditPart) {
            return "Process " + ((ProcessEditPart) element).getProcessWrapper().getName();
        } else if (element instanceof ElementConnectionEditPart) {
            element = ((ElementConnectionEditPart) element).getModel().toString();
        }
        return element.toString();
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public void removeListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

}
