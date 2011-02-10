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

import org.eclipse.webdav.Policy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The mkworkspace element editor is simple since it is a placeholder
 * for future enhancements and implementation specific arguments.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class Mkworkspace extends ElementEditor {
    /**
     * Mkworkspace constructor.
     *
     * @param root Element forming the root of the mkworkspace tree.
     * @throws MalformedElementException if the root element is malformed.
     */
    public Mkworkspace(Element root) throws MalformedElementException {
        super(root, "mkworkspace"); //$NON-NLS-1$
    }

    /**
     * Creates a new WebDAV mkworkspace element and sets it as the root of
     * the given document.  Returns an editor on the new root element.
     * <p>
     * The document must not be <code>null</code>, and must not already have
     * a root element.</p>
     *
     * @param document the document that will become the root of a new
     *                 mkworkspace element
     * @return         an element editor on a mkworkspace element
     */
    public static Mkworkspace create(Document document) {
        Assert.isNotNull(document);
        Assert.isTrue(document.getOwnerDocument() == null);
        Element element = create(document, "mkworkspace"); //$NON-NLS-1$
        try {
            return new Mkworkspace(element);
        } catch (MalformedElementException e) {
            Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
            return null; // Never reached.
        }
    }
}
