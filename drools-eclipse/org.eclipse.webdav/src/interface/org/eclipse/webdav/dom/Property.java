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

package org.eclipse.webdav.dom;

import org.w3c.dom.Element;

/**
 * An element editor that is the superclass of all WebDAV property
 * elements. This class only exists to group property editors under a
 * common class and to separate them from other types of editors.
 * Property elements always appear as a child of the WebDAV prop
 * element.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see Prop
 */
public abstract class Property extends ElementEditor {
    /**
     * Creates a new editor on the given property element. The element is
     * assumed to be well formed.
     *
     * @param element a property element
     * @param expectedType the tag name for the element tht this editor
     *  is expected to manipulate.
     * @throws        MalformedElementException if there is reason to
     *                believe that the element is not well formed
     */
    public Property(Element element, String expectedType) throws MalformedElementException {
        super(element, expectedType);
    }
}
