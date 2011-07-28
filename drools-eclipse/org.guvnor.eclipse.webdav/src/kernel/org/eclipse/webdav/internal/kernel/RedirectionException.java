package org.eclipse.webdav.internal.kernel;

/** Represents exceptions that require further action by the user agent
 * in order to fulfill the request.
 * <p>
 * Status codes:
 * <ul>
 *    <li>300 Multiple Choices</li>
 *    <li>301 Moved Permanently</li>
 *    <li>302 Moved Temporarily</li>
 *    <li>303 See Other</li>
 *    <li>304 Not Modified</li>
 *    <li>305 Use Proxy</li>
 * </ul>
 * </p>
 */
public class RedirectionException extends WebDAVException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;

    /**
     * Construct a RedirectionException with a status code and simple message.
     *
     * @param statusCode the WebDAV status code corresponding to the exception
     * @param statusMessage a message describing the status code in the context of the exception
     */
    public RedirectionException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }

    /**
     * Construct a RedirectionException with a status code and simple message.
     *
     * @param statusCode the WebDAV status code corresponding to the exception
     * @param statusMessage a message describing the status code in the context of the exception
     */
    public RedirectionException(int statusCode, String statusMessage, Object data) {
        super(statusCode, statusMessage, data);
    }
}
