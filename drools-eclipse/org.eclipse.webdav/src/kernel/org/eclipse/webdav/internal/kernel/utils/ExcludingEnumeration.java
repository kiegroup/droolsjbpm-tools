package org.eclipse.webdav.internal.kernel.utils;

import java.util.Enumeration;
import java.util.Vector;

public class ExcludingEnumeration extends EnumerationFilter {

    protected Enumeration e;
    protected Vector excludeList;
    protected Object next;

    public ExcludingEnumeration(Enumeration e, Vector excludeList) {
        super();
        this.e = e;
        this.excludeList = excludeList;
        getNextCandidate();
    }

    private void getNextCandidate() {
        while (e.hasMoreElements()) {
            Object candidate = e.nextElement();
            if (excludeList.indexOf(candidate) != -1) {
                next = candidate;
                return;
            }
        }
        next = null;
    }

    /**
     * @see #hasMoreElements()
     */
    public boolean hasMoreElements() {
        return (next != null);
    }

    /**
     * @see #nextElement()
     */
    public Object nextElement() {
        Object answer = next;
        getNextCandidate();
        return answer;
    }
}
