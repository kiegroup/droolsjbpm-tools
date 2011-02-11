package org.eclipse.webdav.internal.kernel;

public class DAVException extends Exception {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;

    /**
     * DAV4JException default constructor.
     */
    public DAVException() {
        super();
    }

    public DAVException(String s) {
        super(s);
    }
}
