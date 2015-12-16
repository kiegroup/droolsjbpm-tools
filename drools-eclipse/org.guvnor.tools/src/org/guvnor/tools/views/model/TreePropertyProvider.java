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

package org.guvnor.tools.views.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.guvnor.tools.Messages;

public class TreePropertyProvider implements IPropertySource {

    private TreeObject node;

    public TreePropertyProvider(TreeObject node) {
        this.node = node;
    }

    public Object getEditableValue() {
        return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
            new TextPropertyDescriptor("name", Messages.getString("prop.name")), //$NON-NLS-1$ //$NON-NLS-2$
            new TextPropertyDescriptor("location", Messages.getString("prop.location")), //$NON-NLS-1$ //$NON-NLS-2$
            new TextPropertyDescriptor("type", Messages.getString("prop.type")), //$NON-NLS-1$ //$NON-NLS-2$
            new TextPropertyDescriptor("creationdate", Messages.getString("prop.created")), //$NON-NLS-1$ //$NON-NLS-2$
            new TextPropertyDescriptor("lastmodified", Messages.getString("prop.lastmod")), //$NON-NLS-1$ //$NON-NLS-2$
            new TextPropertyDescriptor("revision", Messages.getString("prop.revision")) //$NON-NLS-1$ //$NON-NLS-2$
        };
    }

    public Object getPropertyValue(Object id) {
        if (id.equals("name")) { //$NON-NLS-1$
            return node.getName();
        }
        if (id.equals("location")) { //$NON-NLS-1$
            return node.getFullPath().substring(node.getGuvnorRepository().getLocation().length());
        }
        if (id.equals("type")) { //$NON-NLS-1$
            if (node.getNodeType() == TreeObject.Type.REPOSITORY) {
                return Messages.getString("prop.rep.value"); //$NON-NLS-1$
            }
            if (node.getResourceProps().isDirectory()) {
                return Messages.getString("prop.dir.value"); //$NON-NLS-1$
            }
            if (node.getNodeType() == TreeObject.Type.RESOURCE) {
                return Messages.getString("prop.file.value"); //$NON-NLS-1$
            }
        }
        if (id.equals("creationdate")) { //$NON-NLS-1$
            return node.getResourceProps().getCreationDate();
        }
        if (id.equals("lastmodified")) { //$NON-NLS-1$
            return node.getResourceProps().getLastModifiedDate();
        }
        if (id.equals("revision")) { //$NON-NLS-1$
            return node.getResourceProps().getRevision();
        }
        return ""; //$NON-NLS-1$
    }

    public boolean isPropertySet(Object id) {
        // Guvnor properties are read-only, so do nothing
        return false;
    }

    public void resetPropertyValue(Object id) {
        // Guvnor properties are read-only, so do nothing
    }

    public void setPropertyValue(Object id, Object value) {
        // Guvnor properties are read-only, so do nothing
    }
}
