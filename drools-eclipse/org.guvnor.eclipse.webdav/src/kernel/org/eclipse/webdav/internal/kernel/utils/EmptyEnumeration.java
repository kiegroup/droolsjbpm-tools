package org.eclipse.webdav.internal.kernel.utils;

import java.util.NoSuchElementException;

public class EmptyEnumeration extends EnumerationFilter {
    public EmptyEnumeration() {
        super();
    }

    /**
     * @see #hasMoreElements()
     */
    public boolean hasMoreElements() {
        return false;
    }

    /**
     * @see #nextElement()
     */
    public Object nextElement() {
        throw new NoSuchElementException();
    }
}
