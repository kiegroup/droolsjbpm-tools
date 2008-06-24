/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.webdav;

import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.Document;

/**
 * Represents a response from a WebDAV server.
 * <p>
 * A response contains an open input stream, which is obtained
 * by <code>getInputStream()</code>.</p>
 * <p>
 * The client (i.e., the party that invoked the <code>Server</code>
 * method) is responsible for indicating that they're done with
 * the response object by calling its <code>close()</code> method;
 * this discards any remaining unread bytes, closes the stream,
 * and frees up any associated OS resources.</p>
 * <p>
 * Failing to close the response may lead to spurious bytes being
 * left on the response socket and causing unspecified behavior on
 * subsequent request/response interactions.</p>
 * <p>
 * The recommended basic usage pattern is as follows:
 * <pre>
 * Response r = server.mkcol(source, context);
 * try {
 *   if (r.getStatus() == Response.SC_OK) {
 *     ... // handle standard response
 *   } else {
 *     ... // handle non-standard response
 *   } 
 * } finally {
 *     r.close();
 * }
 * </pre>
 * Certain kinds of responses (e.g., certain status codes,
 * responses to certain methods) contain a WebDAV-specified
 * XML element in the response body. The presence (or expected
 * presence) of an XML element body is indicated by 
 * <code>hasDocumentBody</code> returning <code>true</code>,
 * in which case the client should access this element with
 * <code>readDocumentBody()</code>; in other cases, 
 * <code>hasDocumentBody</code> returns<code>false</code>,
 * and the client should access the stream of raw bytes with
 * <code>getInputStream()</code>.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public interface IResponse {

	/*
	 * HTTP/1.1 status codes; see RFC 1945 and the WebDAV specification (RFC 2518).
	 */

	/**
	 * Status code (100) indicating the client may continue with
	 * its request.  This interim response is used to inform the 
	 * client that the initial part of the request has been
	 * received and has not yet been rejected by the server.
	 */
	public final int SC_CONTINUE = 100;

	/**
	 * Status code (101) indicating the server is switching protocols 
	 * according to Upgrade header.
	 */
	public final int SC_SWITCHING_PROTOCOLS = 101;

	/**
	 * Status code (102) indicating the server is still processing the request. 
	 */
	public final int SC_PROCESSING = 102;

	// Request was successfully received, understood, and accepted.

	/**
	 * Status code (200) indicating the request succeeded normally.
	 */
	public final int SC_OK = 200;

	/**
	 * Status code (201) indicating the request succeeded and created
	 * a new resource on the server.
	 */
	public final int SC_CREATED = 201;

	/**
	 * Status code (202) indicating that a request was accepted for
	 * processing, but was not completed.
	 */
	public final int SC_ACCEPTED = 202;

	/**
	 * Status code (203) indicating that the meta information presented 
	 * by the client did not originate from the server. 
	 */
	public final int SC_NON_AUTHORITATIVE_INFORMATION = 203;

	/**
	 * Status code (204) indicating that the request succeeded but that
	 * there was no new information to return.
	 */
	public final int SC_NO_CONTENT = 204;

	/**
	 * Status code (205) indicating that the agent SHOULD reset the document 
	 * view which caused the request to be sent. 
	 */
	public final int SC_RESET_CONTENT = 205;

	/**
	 * Status code (206) indicating that the server has fulfilled the partial 
	 * GET request for the resource.
	 */
	public final int SC_PARTIAL_CONTENT = 206;

	/**
	 * Status code (207) indicating that the response provides status for multiple
	 * independent operations.
	 */
	public final int SC_MULTI_STATUS = 207;

	// Redirection: indicates further action needs to be taken by the user.

	/** Status code (300) indicating that the requested resource corresponds to any one of
	 * a set of representations, each with its own specific location
	 */
	public final int SC_MULTIPLE_CHOICES = 300;

	/**
	 * Status code (301) indicating that the resource has permanently
	 * moved to a new location, and that future references should use a
	 * new URI with their requests.
	 */
	public final int SC_MOVED_PERMANENTLY = 301;

	/**
	 * Status code (302) indicating that the resource has temporarily
	 * moved to another location, but that future references should
	 * still use the original URI to access the resource.
	 */
	public final int SC_MOVED_TEMPORARILY = 302;

	/**
	 * Status code (303) indicating that the response to the request can 
	 * be found under a different URI.
	 */
	public final int SC_SEE_OTHER = 303;

	/**
	 * Status code (304) indicating that a conditional GET operation
	 * found that the resource was available and not modified.
	 */
	public final int SC_NOT_MODIFIED = 304;

	/**
	 * Status code (305) indicating that the requested resource MUST be accessed 
	 * through the proxy given by the Location field. 
	 */
	public final int SC_USE_PROXY = 305;

	// Client error

	/**
	 * Status code (400) indicating the request sent by the client was
	 * syntactically incorrect.
	 */
	public final int SC_BAD_REQUEST = 400;

	/**
	 * Status code (401) indicating that the request requires HTTP
	 * authentication.
	 */
	public final int SC_UNAUTHORIZED = 401;

	/**
	 * Status code (402) reserved for future use.
	 */
	public final int SC_PAYMENT_REQUIRED = 402;

	/**
	 * Status code (403) indicating the server understood the request
	 * but refused to fulfill it.
	 */
	public final int SC_FORBIDDEN = 403;

	/**
	 * Status code (404) indicating that the requested resource is not
	 * available.
	 */
	public final int SC_NOT_FOUND = 404;

	/**
	 * Status code (405) indicating the method specified is not
	 * allowed for the resource.
	 */
	public final int SC_METHOD_NOT_ALLOWED = 405;

	/**
	 * Status code (406) indicating the resource identified by the 
	 * request is only capable of generating response entities 
	 * which have content characteristics not acceptable according 
	 * to the accept headerssent in the request. 
	 */
	public final int SC_NOT_ACCEPTABLE = 406;

	/**
	 * Status code (407) indicating the client MUST first authenticate
	 * itself with the proxy.
	 */
	public final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;

	/**
	 * Status code (408) indicating the client did not produce a request within 
	 * the time that the server was prepared to wait. 
	 */
	public final int SC_REQUEST_TIMEOUT = 408;

	/**
	 * Status code (409) indicating that the request could not be
	 * completed due to a conflict with the current state of the
	 * resource.
	 */
	public final int SC_CONFLICT = 409;

	/**
	 * Status code (410) indicating the server did not receive a timely 
	 * response from the upstream server while acting as a gateway or proxy. 
	 */
	public final int SC_GONE = 410;

	/**
	 * Status code (411) indicating the request cannot be handled 
	 * without a defined Content-Length. 
	 */
	public final int SC_LENGTH_REQUIRED = 411;

	/**
	 * Status code (412) indicating the precondition given in one
	 * or more of the request-header fields evaluated to false
	 * when it was tested on the server.
	 */
	public final int SC_PRECONDITION_FAILED = 412;

	/**
	 * Status code (413) indicating the server is refusing to
	 * process a request because the request entity is larger
	 * than the server is willing or able to process.
	 */
	public final int SC_REQUEST_TOO_LONG = 413;

	/**
	 * Status code (414) indicating the server is refusing to 
	 * service the request because the Request-URI is longer 
	 * than the server is willing to interpret. 
	 */
	public final int SC_REQUEST_URI_TOO_LONG = 414;

	/**
	 * Status code (415) indicating the server is refusing to service
	 * the request because the entity of the request is in a format
	 * not supported by the requested resource for the requested
	 * method.
	 */
	public final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

	/**
	 * Status code (422) indicating the server understands the content type of the
	 * request entity, but was unable to process the contained instructions.
	 */
	public final int SC_UNPROCESSABLE_ENTITY = 422;

	/**
	 * Status code (423) indicating the source or destination resource of a
	 * method is locked.
	 */
	public final int SC_LOCKED = 423;

	/**
	 * Status code (424) indicating the method was not executed on
	 * a particular resource within its scope because some part of
	 * the method's execution failed causing the entire method to be
	 * aborted.
	 */
	public final int SC_FAILED_DEPENDENCY = 424;

	/**
	 * Status code (425) indicating that the resource does not have sufficient
	 * space to record the state of the resource after the execution of the method.
	 */
	public final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 425;

	// Server errors

	/**
	 * Status code (500) indicating an error inside the HTTP service
	 * which prevented it from fulfilling the request.
	 */
	public final int SC_INTERNAL_SERVER_ERROR = 500;

	/**
	 * Status code (501) indicating the HTTP service does not support
	 * the functionality needed to fulfill the request.
	 */
	public final int SC_NOT_IMPLEMENTED = 501;

	/**
	 * Status code (502) indicating that the HTTP server received an
	 * invalid response from a server it consulted when acting as a
	 * proxy or gateway.
	 */
	public final int SC_BAD_GATEWAY = 502;

	/**
	 * Status code (503) indicating that the HTTP service is
	 * temporarily overloaded, and unable to handle the request.
	 */
	public final int SC_SERVICE_UNAVAILABLE = 503;

	/**
	 * Status code (504) indicating the server did not receive a 
	 * timely response from the upstream server while acting as a
	 * gateway or proxy. 
	 */
	public final int SC_GATEWAY_TIMEOUT = 504;

	/**
	 * Status code (505) indicating the server does not support or 
	 * refuses to support the HTTP protocol version that was used 
	 * in the request message.
	 */
	public final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

	/**
	 * Status code (506) indicating the server terminated an operation
	 * because it encountered an infinite loop while processing a request
	 * with "Depth: infinity".
	 */
	public final int SC_LOOP_DETECTED = 506;

	/**
	 * Status code (507) indicating the server is unable to create the
	 * requested binding because it would bind a segment in a collection
	 * on one server to a resource on a different server.
	 */
	public final int SC_CROSS_SERVER_BINDING_FORBIDDEN = 507;

	/**
	 * Closes this response. Any unread bytes that make up
	 * the body of this response are discarded; any underlying
	 * OS resources associated with this response are freed.
	 * <p>
	 * This method should be invoked on every response returned
	 * from the server <b>without exception</b>.</p>
	 *
	 * @exception IOException if there was a problem closing this
	 *  response
	 */
	public void close() throws IOException;

	/**
	 * Returns this response's header fields.
	 *
	 * @return the <code>Context</code> containing the header
	 * key, value pairs.
	 */
	public IContext getContext();

	/**
	 * Gets the contents of the response body as a DOM
	 * <code>Document</code>.
	 * <p>
	 * This response MUST have an XML body (i.e., <code>
	 * hasDocumentBody()</code> must return <code>true</code>).</p>
	 * <p>
	 * Once the body has been read as an <code>InputStream</code>
	 * it cannot be subsequently read as a <code>Document</code>
	 * (the bytes have been consumed).
	 * <p>
	 * If a problem occurs parsing the document body an <code>
	 * IOException</code> is thrown.
	 *
	 * @return DOM document obtained from the XML body.
	 * @exception IOException if there was a problem receiving the
	 * response or interpreting it as XML.
	 */
	public Document getDocumentBody() throws IOException;

	/**
	 * Returns an open input stream for reading the 
	 * the body of this response.
	 * <p>
	 * This response must not have an XML body (i.e.,
	 * <code>hasElementBody()</code> must return <code>false
	 * </code>).</p>
	 * <p>
	 * The client may close the stream early, however, the
	 * resulting stream will be closed if necessary when
	 * the client closes this response.</p>
	 *
	 * @return the bytes of the response as an <code>InputStream</code>.
	 */
	public InputStream getInputStream();

	/**
	 * Returns the HTTP response status code.
	 * <p>
	 * As a convenience, the status code should be
	 * one of the <code>SC_* </code> constants
	 * defined in this class.</p>
	 *
	 * @return the response status code.
	 */
	public int getStatusCode();

	/**
	 * Returns the response status message.
	 * <p>
	 * The status message is the HTTP string that follows
	 * the status code on the first line of the response.</p>
	 *
	 * @return the status message verbatim.
	 */
	public String getStatusMessage();

	/**
	 * Returns whether this response has an XML element
	 * as a body; this is the element that would be read
	 * if <code>readDocumentBody()</code> was called.
	 * <p>
	 * This method can be called numerous times, and can
	 * be called after the body has been read either as
	 * an InputStream or a Document -- all with no bad
	 * effect.</p>
	 *
	 * @return <code>true</code> if this message has a
	 *	document body and <code>false</code> otherwise.
	 */
	public boolean hasDocumentBody();
}
