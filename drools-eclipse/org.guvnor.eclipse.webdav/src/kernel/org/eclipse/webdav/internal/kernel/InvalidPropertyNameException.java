package org.eclipse.webdav.internal.kernel;

/** 
 * When thrown, this class signals that the property name
 * string provided was not legal based on the definition
 * provided in the documentation of the PropertyName(String)
 * constructor.
 */
public class InvalidPropertyNameException extends Exception {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;

    /**
     * Construct an InvalidPropertyNameException object.
     */
    public InvalidPropertyNameException() {
        super("InvalidPropertyName"); //$NON-NLS-1$
    }

    /**
     * InvalidPropertyNameException constructor comment.
     * @param statusMessage a message describing the exception of status code
     */
    public InvalidPropertyNameException(String statusMessage) {
        super(statusMessage);
    }
}
