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

package org.guvnor.tools.views.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.utils.webdav.ResourceProperties;

/**
 * Basic node for the tree representation of Guvnor contents.
 * @author jgraham
 *
 */
public class TreeObject implements IAdaptable {

    private GuvnorRepository theRep;

    public enum Type {
        NONE,
        REPOSITORY,
        PACKAGE,
        RESOURCE
    }

    private String         name;
    private Type         nodeType;
    private TreeParent     parent;

    private ResourceProperties props;

    public TreeObject(String name, Type nodeType) {
        this.name = name;
        this.nodeType = nodeType;
    }
    public String getName() {
        return name;
    }
    public Type getNodeType() {
        return nodeType;
    }
    public void setParent(TreeParent parent) {
        this.parent = parent;
    }
    public TreeParent getParent() {
        return parent;
    }
    public String toString() {
        return getName();
    }

    private TreePropertyProvider propProvider;

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            if (propProvider == null) {
                propProvider = new TreePropertyProvider(this);
            }
            return propProvider;
        }
        return null;
    }

    public ResourceProperties getResourceProps() {
        return props;
    }
    public void setResourceProps(ResourceProperties props) {
        this.props = props;
    }
    public void setGuvnorRepository(GuvnorRepository theRep) {
        this.theRep = theRep;
    }
    public GuvnorRepository getGuvnorRepository() {
        return theRep;
    }
    public String getFullPath() {
        if (props.getBase().trim().length() > 0) {
            if (props.getBase().endsWith("/")) { //$NON-NLS-1$
                return props.getBase() + getName();
            } else {
                return props.getBase() + "/" + getName(); //$NON-NLS-1$
            }
        } else {
            return getName();
        }
    }
}
