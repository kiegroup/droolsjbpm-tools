/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.eclipse.webdav.dom;

import org.eclipse.webdav.Policy;
import org.w3c.dom.Element;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 */
public class Label extends ElementEditor {

    // An ordered collection of the element names of the label
    // element's possible children.
    protected static final String[] childNames = new String[] {"add", "set", "remove"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    public Label(Element root) throws MalformedElementException {
        super(root, "label"); //$NON-NLS-1$
    }

    public String getLabelName() throws MalformedElementException {
        Element child = getFirstChild(root, childNames);
        ensureNotNull(Policy.bind("ensure.missingElmt"), child); //$NON-NLS-1$
        String labelName = getChildText(child, "label-name", true); //$NON-NLS-1$
        ensureNotNull(Policy.bind("ensure.missingLabel"), labelName); //$NON-NLS-1$
        return labelName;
    }

    public boolean isAdd() {
        Element child = getFirstChild(root, childNames);
        return getNSLocalName(child).equals("add"); //$NON-NLS-1$
    }

    public boolean isRemove() {
        Element child = getFirstChild(root, childNames);
        return getNSLocalName(child).equals("remove"); //$NON-NLS-1$
    }

    public boolean isSet() {
        Element child = getFirstChild(root, childNames);
        return getNSLocalName(child).equals("set"); //$NON-NLS-1$
    }
}
