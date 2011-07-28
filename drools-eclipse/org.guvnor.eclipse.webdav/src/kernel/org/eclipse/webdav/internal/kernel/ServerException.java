package org.eclipse.webdav.internal.kernel;

/** Represents exceptions that can happen on the server as the
 * result of a server error.
 * <p>
 * Status codes:
 * <ul>
 *    <li>500 Internal Server Error</li>
 *    <li>501 Not Implemented</li>
 *    <li>502 Bad Gateway</li>
 *    <li>503 Service Unavailable</li>
 *    <li>504 Gateway Timeout</li>
 *    <li>505 HTTP Version Not Supported</li>
 * </ul>
 * </p>
 */
public class ServerException extends WebDAVException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;

    /**
     * Construct a ServerException with a status code and simple message.
     *
     * @param statusCode the WebDAV status code corresponding to the exception
     * @param statusMessage a message describing the status code in the context of the exception
     */
    public ServerException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }

    /**
     * Construct a ServerException with a status code and simple message.
     *
     * @param statusCode the WebDAV status code corresponding to the exception
     * @param statusMessage a message describing the status code in the context of the exception
     */
    public ServerException(int statusCode, String statusMessage, Object data) {
        super(statusCode, statusMessage, data);
    }
}
