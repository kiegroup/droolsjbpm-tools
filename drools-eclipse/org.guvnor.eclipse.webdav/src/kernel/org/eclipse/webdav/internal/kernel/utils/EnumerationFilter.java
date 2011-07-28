package org.eclipse.webdav.internal.kernel.utils;

import java.util.Enumeration;

public abstract class EnumerationFilter implements Enumeration {
    public EnumerationFilter() {
        super();
    }

    /**
     * @see #hasMoreElements()
     */
    public abstract boolean hasMoreElements();

    /**
     * @see #nextElement()
     */
    public abstract Object nextElement();
}
