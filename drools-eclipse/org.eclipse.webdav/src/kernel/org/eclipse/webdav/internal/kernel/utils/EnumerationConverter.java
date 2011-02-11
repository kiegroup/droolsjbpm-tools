package org.eclipse.webdav.internal.kernel.utils;

import java.util.Enumeration;

public abstract class EnumerationConverter extends EnumerationFilter {

    protected Enumeration sourceEnum;

    public EnumerationConverter(Enumeration sourceEnum) {
        super();
        this.sourceEnum = sourceEnum;
    }

    /**
     * @see #hasMoreElements()
     */
    public boolean hasMoreElements() {
        return sourceEnum.hasMoreElements();
    }

    /**
     * @see #nextElement()
     * Subclasses should override ths method to convert the
     * source enum objects to the new types.
     */
    public abstract Object nextElement();
}
