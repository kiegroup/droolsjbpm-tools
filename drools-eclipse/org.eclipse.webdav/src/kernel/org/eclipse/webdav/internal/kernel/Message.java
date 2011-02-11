package org.eclipse.webdav.internal.kernel;

import org.eclipse.webdav.IContext;

/**
 * The <code>Message</code> class represents a basic message
 * that has a context and a body.
 */
public class Message {

    protected IContext context = new ContextFactory().newContext();

    // The message body. Can be either an Element, an InputStream
    protected Object body;

    /**
     * Default constructor for the class.
     */
    public Message() {
        super();
    }

    /**
     * Return the message body.
     */
    public Object getBody() {
        return body;
    }

    /**
     * Return the message context.
     *
     * @return the message context.
     * @see Context
     */
    public IContext getContext() {
        return context;
    }
}
