package org.eclipse.webdav.internal.kernel.utils;

import java.util.Enumeration;

public class MergedEnumeration extends EnumerationFilter {

    protected Enumeration first;
    protected Enumeration second;

    /**
     * MergedEnumeration constructor comment.
     */
    public MergedEnumeration(Enumeration first, Enumeration second) {
        super();
        this.first = first;
        this.second = second;
    }

    public boolean hasMoreElements() {
        return (first.hasMoreElements() || second.hasMoreElements());
    }

    public Object nextElement() {
        if (first.hasMoreElements())
            return first.nextElement();
        return second.nextElement();
    }
}
