package org.eclipse.webdav.internal.kernel.utils;

/**
 * <code>AssertionFailedException</code> is a runtime exception thrown
 * by some of the methods in <code>Assert</code>.
 * <p>
 * This class is not declared public to prevent some misuses; programs that catch 
 * or otherwise depend on assertion failures are susceptible to unexpected
 * breakage when assertions in the code are added or removed.
 */
/* package */class AssertionFailedException extends RuntimeException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;

    /** Constructs a new exception.
     */
    public AssertionFailedException() {
        super();
    }

    /** Constructs a new exception with the given message.
     */
    public AssertionFailedException(String detail) {
        super(detail);
    }
}
