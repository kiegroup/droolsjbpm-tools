package org.eclipse.webdav.dom;

/**
 * <code>MalformedElementException</code> is a checked exception thrown
 * when a malformed element is encountered.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class MalformedElementException extends Exception {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;

    /** Constructs a new exception.
     */
    public MalformedElementException() {
        super();
    }

    /** Constructs a new exception with the given message.
     */
    public MalformedElementException(String detail) {
        super(detail);
    }
}
