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
 * Represents a WebDAV server API.
 * <p>
 * There is a one-to-one correspondence between the methods
 * in this interface and methods in the HTTP and WebDAV
 * protocols.</p>
 * <p>
 * The methods all return a response object; it is the
 * client's responsibility to <code>close()</code> the response
 * when they are done with it.</p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public interface IServer {

	/**
	 * Corresponds to the BASELINE-CONTROL method defined in the WebDAV
	 * Versioning Specification.
	 * The <code>IResponse</code> body is undefined.
	 *
	 * @param locator the location of the collection to put under baseline control.
	 * @param context key-value pairings defined by the user.
	 * @param body the DOM document for the DAV:baseline element.
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it.
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response.
	 */
	public IResponse baselineControl(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Binds the given source locator, to the given destination. An overwrite boolean may
	 * be defined in the user context. Corresponds to the WebDAV method BIND defined 
	 * in the WebDAV Bindings Specification (part of the Advanced Collections Protocol)
	 *
	 * @param source the location of the resource
	 * @param destination the location of the resource's desired parent
	 * @param context key-value pairings as set by the user
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse bind(ILocator source, ILocator destination, IContext context) throws IOException;

	/**
	 * Corresponds to the CHECKIN method defined in the WebDAV
	 * Versioning Specification.
	 * The <code>IResponse</code> body is undefined.
	 *
	 * @param locator the location of the resource to check in.
	 * @param context key-value pairings defined by the user.
	 * @param body DOM document for DAV:checkin.
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it.
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response.
	 */
	public IResponse checkin(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Corresponds to the CHECKOUT method defined in the WebDAV
	 * Versioning Specification.
	 * The <code>IResponse</code> body is undefined.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param body the XML elements that describe the parameters
	 *	of the checkout in a DAV:checkout element.
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it.
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response.
	 */
	public IResponse checkout(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Copies the resource with the given locator, to the given
	 * destination. Corresponds to the COPY method defined in
	 * the WebDAV Specification.
	 *
	 * @param source the location of the resource
	 * @param destination the desired location of the resource copy
	 * @param context key-value pairings as defined by the user
	 * @param body XML document describing the properties to copy
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse copy(ILocator source, ILocator destination, IContext context, Document body) throws IOException;

	/**
	 * Deletes the resource with the given locator. Corresponds to
	 * the DELETE method defined in the HTTP/1.1 Specfication.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings as defined by the user
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse delete(ILocator locator, IContext context) throws IOException;

	/**
	 * Gets the content of the resource with the given locator.
	 * Corresponds to the GET method defined in the HTTP/1.1 specification.
	 * <p>
	 * The input stream in the resulting response body should be closed
	 * by the user.</p>
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings as defined by the user
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse get(ILocator locator, IContext context) throws IOException;

	/**
	 * Returns the message headers from a message send to the server.
	 * Corresponds to the HEAD method defined in the HTTP/1.1 specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings as defined by the user
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse head(ILocator locator, IContext context) throws IOException;

	/**
	 * Corresponds to the LABEL method defined in the WebDAV
	 * Versioning Specification.
	 * The <code>IResponse</code> body is undefined.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param body DOM document for DAV:label element
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse label(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Locks the resource with the given locator. Use the information
	 * contained in the context and DAV:lockinfo element. Corresponds
	 * to the LOCK method defined in the WebDAV specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param body XML document containing lock information
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse lock(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Corresponds to the MERGE method defined in the WebDAV
	 * Versioning Specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param body XML document containing MERGE parameters
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse merge(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Creates an activity as specified by the given locator.
	 * Corresponds to the MKACTIVITY method as defined by the
	 * Delta-V Versioning Specification.
	 *
	 * @param locator the location of the new resource.
	 * @param context key-value pairings defined by the user.
	 * @param element an undefined XML body document.
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response.
	 */
	public IResponse mkactivity(ILocator locator, IContext context, Document element) throws IOException;

	/**
	 * Creates the collection specified by the given locator.
	 * Corresponds to the MKCOL method as defined by the WebDAV specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param element XML document containing properties
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse mkcol(ILocator locator, IContext context, Document element) throws IOException;

	/**
	 * Creates a workspace as specified by the given locator. Corresponds to the
	 * MKWORKSPACE method as defined by the Delta-V Versioning Specification.
	 *
	 * @param locator the location of the new resource.
	 * @param context key-value pairings defined by the user.
	 * @param element an undefined XML body document.
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response.
	 */
	public IResponse mkworkspace(ILocator locator, IContext context, Document element) throws IOException;

	/**
	 * Moves the resource with the given source locator, to the
	 * specified destination. Corresponds to the MOVE method as
	 * defined by the WebDAV specification.
	 *
	 * @param source the location of the resource
	 * @param destination the desired location for the resource
	 * @param context key-value pairing defined by the user
	 * @param body XML document specifying the properties to move
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse move(ILocator source, ILocator destination, IContext context, Document body) throws IOException;

	/**
	 * Performs an options call to the server. Answers a list of
	 * characteristics of the target resource.
	 * <p>
	 * Corresponds to the OPTIONS method as defined by the HTTP/1.1
	 * Specification.</p>
	 * <p>
	 * If the resource URL (in the locator) is "*", the server's
	 * general capabilities are queried.</p>
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse options(ILocator locator, IContext context) throws IOException;

	/**
	 * Corresponds to the POST method as defined by the HTTP/1.1
	 * specification.
	 * <p>
	 * The given input stream will be closed by the server after
	 * the contents have been consumed.</p>
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param input the input stream containing the resource data
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse post(ILocator locator, IContext context, InputStream input) throws IOException;

	/**
	 * Performs a property find on the server. Corresponds to the
	 * PROPFIND method as defined by the WebDAV specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param body XML document as defined by the spec
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse propfind(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Performs a property patch call on the server. Corresponds to the
	 * PROPPATCH method as defined by the WebDAV specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param body XML document as defined by the spec
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse proppatch(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Puts the given contents onto the server into the specified
	 * location. Corresponds to the PUT method as defined by the
	 * HTTP/1.1 specification.
	 * <p>
	 * The given input stream will be closed by the server after the
	 * contents have been consumed.</p>
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param input the input stream containing the resource data
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse put(ILocator locator, IContext context, InputStream input) throws IOException;

	/**
	 * Corresponds to the REPORT method defined in the WebDAV
	 * Versioning Specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @param body XML document containing REPORT parameters
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse report(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Does a trace call to the server. Corresponds to the TRACE method
	 * as defined by the HTTP/1.1 specification.
	 * <p>
	 * The input stream in the response body should be closed by the user.</p>
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse trace(ILocator locator, IContext context) throws IOException;

	/**
	 * Corresponds to the UNCHECKOUT method defined in the WebDAV
	 * Versioning Specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse uncheckout(ILocator locator, IContext context) throws IOException;

	/**
	 * Unlocks the resource with the given locator. Corresponds to the
	 * UNLOCK method as defined by the WebDAV specification.
	 *
	 * @param locator the location of the resource
	 * @param context key-value pairings defined by the user
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse unlock(ILocator locator, IContext context) throws IOException;

	/**
	 * Performs an update call on the server. Corresponds to the
	 * UPDATE method as defined by the Delta-V specification.
	 *
	 * @param locator the location of the version-controlled resource.
	 * @param context key-value pairings defined by the client.
	 * @param body DAV:update XML document as defined by the spec.
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse update(ILocator locator, IContext context, Document body) throws IOException;

	/**
	 * Corresponds to the VERSION-CONTROL method defined in the WebDAV
	 * Versioning Specification.
	 *
	 * @param locator the location of the versionable resource.
	 * @param context key-value pairings defined by the user.
	 * @param body the request body elements as a DOM document.
	 * @return the response from the server; the client
	 *   must send <code>close()</code> to the response when done with it
	 * @exception IOException if there was a problem sending the request
	 *   or receiving the response
	 */
	public IResponse versionControl(ILocator locator, IContext context, Document body) throws IOException;
}
