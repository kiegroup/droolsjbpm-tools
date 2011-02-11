package org.eclipse.webdav.internal.authentication;

/**
 * A <code>ParserException</code> is thrown by the <code>Parser</code>
 * when there is a problem parsing a <code>String</code>.
 *
 * @see Parser
 */
public class ParserException extends Exception {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;

    /**
     * Creates a new <code>ParserException</code>.
     */
    public ParserException() {
        super();
    }

    /**
     * Creates a new <code>ParserException</code> with the given message.
     *
     * @param message
     */
    public ParserException(String message) {
        super(message);
    }
}
