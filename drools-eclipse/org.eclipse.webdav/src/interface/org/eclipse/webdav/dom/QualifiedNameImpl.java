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

/**
 * Qualified names are two-part names: qualifier and local name.
 * The qualifier must be in URI form (see RFC2396).  
 * Note however that the qualifier may be <code>null</code> if
 * the default name space is being used.  The empty space is
 * not a valid qualifier.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class QualifiedNameImpl implements QualifiedName {

    /** Qualifier part (potentially <code>null</code>). */
    protected String qualifier = null;

    /** Local name part. */
    protected String localName = null;

    /**
     * Creates and returns a new qualified name with the given qualifier
     * and local name.  The local name must not be the empty string.
     * The qualifier may be <code>null</code>.
     */
    public QualifiedNameImpl(String qualifier, String localName) {
        Assert.isTrue(localName != null && localName.length() != 0);
        this.qualifier = qualifier;
        this.localName = localName;
    }

    /**
     * Returns <code>true</code> if this qualified name is equivalent to the given object.
     * <p>
     * Qualified names are equal if and only if they have the same
     * qualified parts and local parts.
     * Qualified names are not equal to objects other than qualified names.
     *
     */
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof QualifiedName))
            return false;
        // There may or may not be a qualifier.
        QualifiedName qName = (QualifiedName) obj;
        if (qualifier == null && qName.getQualifier() != null)
            return false;
        if (qualifier != null && !qualifier.equals(qName.getQualifier()))
            return false;
        return localName.equals(qName.getLocalName());
    }

    /**
     * Returns the local part of this name.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Returns the qualifier part for this qualifed name, or <code>null</code>
     * if none.
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * Returns the hash code for this qualified name.
     */
    public int hashCode() {
        return (qualifier == null ? 0 : qualifier.hashCode()) + localName.hashCode();
    }

    /**
     * Converts this qualified name into a string, suitable for
     * display (unsuitable for parsing back to a qualified name!).
     */
    public String toString() {
        return Policy.bind("qualifiedNameImpl.namespace") + ": \"" + //$NON-NLS-1$ //$NON-NLS-2$
                (getQualifier() == null ? "null" : getQualifier()) + //$NON-NLS-1$
                "\", " + Policy.bind("qualifiedNameImpl.localPart") + ": \"" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                getLocalName() + "\""; //$NON-NLS-1$
    }
}
