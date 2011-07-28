package org.eclipse.webdav.internal.kernel;

public class SystemException extends DAVException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;
    protected Exception wrappedException;

    /**
     * SystemException default constructor.
     */
    public SystemException() {
        super();
    }

    public SystemException(Exception e) {
        super(e.getMessage());
        wrappedException = e;
    }

    public SystemException(String s) {
        super(s);
    }

    public Exception getWrappedException() {
        return wrappedException;
    }

    public void setWrappedException(Exception e) {
        wrappedException = e;
    }
}
