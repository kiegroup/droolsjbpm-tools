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
package org.eclipse.webdav.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.eclipse.webdav.*;
import org.eclipse.webdav.dom.*;
import org.eclipse.webdav.internal.kernel.*;
import org.eclipse.webdav.internal.kernel.utils.Assert;
import org.eclipse.webdav.internal.kernel.utils.EmptyEnumeration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The <code>AbstractResourceHandle</code> class is the abstract
 * superclass for all types of resource references.  A resource
 * handle is a client-side 'proxy' for the server resource.  Instances
 * of handle classes understand the methods appropriate for the
 * corresponding server resource and provide a convenient high-level
 * API for sending WebDAV methods to the server to manipulate
 * the resource.
 * <p>
 * It is certainly posible to create a stale or invalid handle in
 * numerous ways.  For example, the existance of a handle does not
 * imply the existance of a  corresponding server resource (indeed
 * resource can be created by sending create() to a handle, or deleted
 * using delete() -- so the life cycles are not coupled.  It is also
 * possible to create, say, a collection handle on a regular resource
 * and invoke invalid WebDAV methods.  These will typically result
 * in an exception.</p>
 * <p>
 * The API on these classes are intended to convenience methods for
 * the most common operations on server resources.  They make some
 * assumptions about the way you want to receive the results.  To
 * get finer (but possibly less convenient) control over the WebDAV
 * methods use the <code>Server</code> interface of <code>DAVClient
 * </code> directly.</p>
 */
public abstract class AbstractResourceHandle implements WebDAVPropertyNames, WebDAVPropertyValues {

	// The DAVClient that is used to access the WebDAV server.
	// Given during initialization, this object contains the
	// authentication and proxy information etc.
	protected DAVClient davClient;

	// The Locator represents the universal identifier of the server
	// resource.
	protected ILocator locator;

	/**
	 * Creates a new <code>AbstractResourceHandle</code> with the given
	 * DAV client and <code>Locator</code>.
	 *
	 * @param davClient the <code>DAVClient</code> that contains the server
	 * reference and proxy/authentication information.
	 * @param locator the <code>Locator</code> identity of the resource.
	 */
	public AbstractResourceHandle(DAVClient davClient, ILocator locator) {
		Assert.isNotNull(davClient);
		Assert.isNotNull(locator);
		this.davClient = davClient;
		this.locator = locator;
	}

	/**
	 * Answer a new collection handle on the same underlying server resource.
	 * Since the handle represents a means of accessing the resource, it is valid to
	 * consider a collection resource as a collection or regular resource depending
	 * upon how it is being accessed.  Note that not all resources have collection
	 * semantics.
	 * 
	 * @return an equivalent collection handle on the resource.
	 */
	public CollectionHandle asCollectionHandle() {
		return new CollectionHandle(davClient, locator);
	}

	/**
	 * Answer a new resource handle on the same underlying server resource.
	 * Since the handle represents a means of accessing the resource, it is valid to
	 * consider a collection resource as a collection or regular resource depending
	 * upon how it is being accessed.
	 * 
	 * @return an equivalent handle on the resource.
	 */
	public ResourceHandle asResourceHandle() {
		return new ResourceHandle(davClient, locator);
	}

	/**
	 * Return a boolean value indicating whether or not the server for this resource
	 * is DAV compliant.
	 *
	 * @return boolean <code>true</code> if the server can respond to DAV
	 *			requests, or <code>false</code> otherwise.
	 * @exception DAVException if there was a problem checking for DAV compliance
	 */
	public boolean canTalkDAV() throws DAVException {
		IResponse response = null;
		try {
			// Send an options request.
			response = davClient.options(locator, newContext());
			examineResponse(response);

			// Check for at least DAV level 1.
			String davHeader = response.getContext().getDAV();
			return !((davHeader == null) || (davHeader.indexOf("1") == -1)); //$NON-NLS-1$
		} catch (IOException exception) {
			throw new SystemException(exception);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Check-in this resource.  Returns a handle on the new version.
	 * <p>
	 * Note that versioned collections do not have internal members
	 * so they are represented by <code>ResourceHandle</code> handles.</p>
	 * <p>
	 * If the receiver is a working resource it becomes invalid after
	 * the check in (because the server deletes the working resource),
	 * however, if the receiver is a version-controlled resource the
	 * receiver can be used as a checked-in resource.</p>
	 *
	 * @return a handle to the newly created version.
	 * @throws DAVException if a problem occurs with the check in on
	 * the WebDAV server.
	 */
	public ResourceHandle checkIn() throws DAVException {
		ILocator versionLocator = protectedCheckIn();
		return new ResourceHandle(davClient, versionLocator);
	}

	/**
	 * Check out this resource. Returns a resource handle on the checked out
	 * version-controlled resource, or the working resource if a version is checked
	 * out.
	 * <p>
	 * Note that a checked-out version-controlled collection has members that are
	 * themselves version-controlled resources, or unversioned resources; however,
	 * working collection members are always version history resources.</p>
	 * 
	 * @throws DAVException if a problem occurs checking out the resource.
	 */
	public abstract AbstractResourceHandle checkOut() throws DAVException;

	/**
	 * Helper method to close a response from the server.
	 * <p>
	 * Note that the argument MAY be <code>null</code> in which case
	 * the call has no effect.</p>
	 *
	 * @param response the response from the server, or <cod>null</code>
	 * denoting a no-op.
	 * @throws SystemException if a problem occurred closing the response.
	 */
	protected void closeResponse(IResponse response) throws SystemException {
		if (response == null)
			return;
		try {
			response.close();
		} catch (IOException e) {
			throw new SystemException(e);
		}
	}

	/**
	 * Make a copy of this resource and place it at the location defined
	 * by the given locator.
	 * <p>
	 * Uses default values of depth: infinity and overwrite: false for
	 * the copy.</p>
	 *
	 * @param destination the <code>Locator</code> to the destination of the copy.
	 * @exception DAVException if there was a problem copying this resource.
	 * @see IServer#copy(ILocator, ILocator, IContext, Document)
	 */
	public void copy(ILocator destination) throws DAVException {
		copy(destination, IContext.DEPTH_INFINITY, false, null);
	}

	/**
	 * Make a copy of this resource and place it at the location specified
	 * by the given destination locator.
	 * 
	 * @param destination the location to put the copy.
	 * @param depth how deep to make the copy.
	 * @param overwrite how to react if a resource already exists at the destination.
	 * @param propertyNames <code>Collection</code> of <code>QualifiedName</code>s
	 * of properties that MUST be copied as live properties.  Specifying <code>null</code>
	 * mean that <i>all</i> properties must be kept alive; specifying an empty collection allows for
	 * no properties to be kept live. (ref http://andrew2.andrew.cmu.edu/rfc/rfc2518.html#sec-12.12.1)
	 * @exception DAVException if there was a problem copying this resource.
	 * @see IServer#copy(ILocator, ILocator, IContext, Document)
	 */
	public void copy(ILocator destination, String depth, boolean overwrite, Collection propertyNames) throws DAVException {
		// Define the request context.
		IContext context = newContext();
		context.setDepth(depth);
		context.setOverwrite(overwrite);

		// Set up the request body to specify which properties should be kept alive.
		Document document = newDocument();
		PropertyBehavior propertyBehavior = PropertyBehavior.create(document);

		if (propertyNames == null)
			propertyBehavior.setIsKeepAllAlive(true);
		else {
			Iterator namesItr = propertyNames.iterator();
			while (namesItr.hasNext()) {
				QualifiedName name = (QualifiedName) namesItr.next();
				String nameURI = name.getQualifier() + "/" + name.getLocalName(); //$NON-NLS-1$
				propertyBehavior.addProperty(nameURI);
			} // end-while
		} // end-if

		// Call the server to perform the copy.
		IResponse response = null;
		try {
			response = davClient.copy(locator, destination, context, document);
			examineResponse(response);
			examineMultiStatusResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Create this resource in the repository.
	 * <p>
	 * Subclasses should override this method with the correct behavior
	 * for their type.</p>
	 *
	 * @exception DAVException if there was a problem creating
	 * this resource.
	 */
	public abstract void create() throws DAVException;

	/**
	 * Delete this resource from the repository.
	 * <p>
	 * As a convenience, if the resource does not exist this method will
	 * do nothing (rather than throw an exception).  If the caller needs to know
	 * if a resource was deleted they can use delete(boolean).
	 *
	 * @exception DAVException if there was a problem deleting this resource.
	 * @see #delete(boolean)
	 * @see IServer#delete(Locator, Context)
	 */
	public void delete() throws DAVException {
		// As a convenience, we will assume an attempt to
		// delete a missing resource is a successful outcome.
		delete(false);
	}

	/**
	 * Answers true iff the receiver and the argument are considered equal,
	 * otherwise answers false.
	 * <p>
	 * Note that this is a handle equivalence test, and does not imply
	 * that the resources are the same resource on the server.  Indeed,
	 * the method does not contact the server.</p>
	 *
	 * @param obj the target of the comparison.
	 * @return whether the two objects are equal.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AbstractResourceHandle))
			return false;
		AbstractResourceHandle otherHandle = (AbstractResourceHandle) obj;
		// It all comes down to locator equality.
		return locator.equals(otherHandle.locator);
	}

	/**
	 * Answers the hashcode of the receiver as defined by <code>Object#hashCode()</code>.
	 * 
	 * @return the receiver's hash code.
	 */
	public int hashCode() {
		return locator.hashCode();
	}

	/**
	 * If the given response contains a multistatus body, the bodies status'
	 * are checked for errors. If an error is found, an exception is thrown.
	 *
	 * @param response the response from the server to examine.
	 * @throws DAVException if the given response contains a multistatus
	 * body that contains a status code signalling an error.
	 */
	protected void examineMultiStatusResponse(IResponse response) throws DAVException {
		// If it is not a multistatus we don't look at it.
		if (response.getStatusCode() != IResponse.SC_MULTI_STATUS)
			return;

		// It is declared a multistatus, so if there is no response body
		// then that is a problem.
		if (!response.hasDocumentBody())
			throw new DAVException(Policy.bind("exception.responseMustHaveDocBody")); //$NON-NLS-1$

		// Extract the XML document from the response.
		Element documentElement;
		try {
			documentElement = response.getDocumentBody().getDocumentElement();
			if (documentElement == null)
				throw new DAVException(Policy.bind("exception.invalidDoc")); //$NON-NLS-1$
		} catch (IOException exception) {
			throw new SystemException(exception);
		}

		// Enumerate all the responses in the multistat and check that
		// they are indicating success (i.e. are 200-series response codes).
		try {
			MultiStatus multistatus = new MultiStatus(documentElement);
			Enumeration responseEnum = multistatus.getResponses();
			while (responseEnum.hasMoreElements()) {
				ResponseBody responseBody = (ResponseBody) responseEnum.nextElement();
				Enumeration propstatEnum = responseBody.getPropStats();
				while (propstatEnum.hasMoreElements()) {
					PropStat propstat = (PropStat) propstatEnum.nextElement();
					examineStatusCode(propstat.getStatusCode(), propstat.getResponseDescription());
				} // end-while
			} // end-while
		} catch (MalformedElementException e) {
			throw new SystemException(e);
		}
	}

	/**
	 * Check the status code of the given response and throw a WebDAV
	 * exception if the code indicates failure.
	 *
	 * @param response the response to check
	 * @exception WebDAVException if the server returned an HTTP/WebDAV
	 * error code (i.e., anything outside the 200-series codes).
	 */
	protected void examineResponse(IResponse response) throws WebDAVException {
		examineStatusCode(response.getStatusCode(), response.getStatusMessage());
	}

	/**
	 * Helper method to extract the property status response from
	 * a multi status reponse, and populate a URLTable with the
	 * results.
	 *
	 * @param multiStatus an editor on the response from the server.
	 * @return all the property status in a <code>URLTable</code>.
	 * @throws IOException if there is a problem parsing the resource URLs.
	 * @throws MalformedElementException if the XML is badly formed.
	 */
	protected URLTable extractPropStats(MultiStatus multiStatus) throws IOException, MalformedElementException {
		// Construct a URLTable to return to the user.
		URLTable reply = new URLTable();

		// For each response (resource).
		Enumeration responses = multiStatus.getResponses();
		while (responses.hasMoreElements()) {
			ResponseBody responseBody = (ResponseBody) responses.nextElement();
			String href = responseBody.getHref();

			// The href may be relative to the request URL.
			URL resourceURL = new URL(new URL(locator.getResourceURL()), href);
			Hashtable properties = new Hashtable();
			reply.put(resourceURL, properties);

			// For each property status grouping.
			Enumeration propstats = responseBody.getPropStats();
			while (propstats.hasMoreElements()) {
				PropStat propstat = (PropStat) propstats.nextElement();
				org.eclipse.webdav.dom.Status status = new org.eclipse.webdav.dom.Status(propstat.getStatus());

				// For each property with this status.
				Enumeration elements = propstat.getProp().getProperties();
				while (elements.hasMoreElements()) {
					Element element = (Element) elements.nextElement();
					QualifiedName name = ElementEditor.getQualifiedName(element);
					// Add a property status object to the result set.
					PropertyStatus propertyStatus = new PropertyStatus(element, status.getStatusCode(), status.getStatusMessage());
					properties.put(name, propertyStatus);
				} // end-while
			} // end-while
		} // end-while

		return reply;
	}

	/**
	 * Return the content of this resource as an input stream. The input
	 * stream should be closed by the user.
	 *
	 * @return the input stream
	 * @exception DAVException if there was a problem getting the contents
	 * @see IServer#get(Locator, Context)
	 */
	public ResponseInputStream getContent() throws DAVException {
		IResponse response = null;
		try {
			response = davClient.get(locator, newContext());
			examineResponse(response);
		} catch (IOException e) {
			closeResponse(response);
			throw new SystemException(e);
		}
		return new ResponseInputStream(response);
	}

	/**
	 * Answer the DAVClient being used by this resource handle for accessing
	 * the resource.
	 *
	 * @return the receiver's <code>DAVClient</code>.
	 */
	public DAVClient getDAVClient() {
		return davClient;
	}

	/**
	 * Return the locator for this resource. 
	 *
	 * @return the locator for this resource
	 */
	public ILocator getLocator() {
		return locator;
	}

	/**
	 * Return an Enumeration over ActiveLocks which lists the locks currently
	 * held on this resource. Return an empty enumeration if the lock discovery
	 * property is not found on the resource.
	 *
	 * @return the enumeration of active locks
	 * @exception DAVException if there was a problem getting the locks
	 * @see #getProperty(QualifiedName)
	 */
	public Enumeration getLocks() throws DAVException {
		LockDiscovery lockdiscovery = null;
		try {
			Element element = getProperty(DAV_LOCK_DISCOVERY).getProperty();
			lockdiscovery = new LockDiscovery(element);
			return lockdiscovery.getActiveLocks();
		} catch (WebDAVException exception) {
			if (exception.getStatusCode() == IResponse.SC_NOT_FOUND)
				return new EmptyEnumeration();
			throw exception;
		} catch (MalformedElementException elemException) {
			throw new SystemException(elemException);
		}
	}

	/**
	 * Returns a collection handle for the parent of this resource.
	 * <p>
	 * Note that this method does NOT perform a method call to the
	 * server to ensure that the collection exists.</p>
	 * <p>
	 * Returns <code>null</code> if this resource is the root.
	 *
	 * <em>NOTE</em>
	 * The parent of a resource is, in general, ambiguous and may not
	 * be immediately discernable from a resource locator.  For example,
	 * a locator with a 'label' qualifier will identify a version of a version-
	 * controlled resource, and the parent will not be found by a simple URL
	 * operation.  Where a handle is created on a stable URL (i.e. a version URL)
	 * there is no concept of a 'parent' resource.
	 * Clients require further contextual information to determine
	 * the 'parent' of a resource in these cases.
	 *
	 * @return the handle for the parent of this resource, or
	 * <code>null</code>.
	 */
	public CollectionHandle getParent() throws DAVException {
		Assert.isTrue(locator.getLabel() == null);
		Assert.isTrue(!locator.isStable());

		try {
			URL url = URLTool.getParent(locator.getResourceURL());
			if (url == null)
				return null;
			String parentName = url.toString();
			ILocator parentLocator = davClient.getDAVFactory().newLocator(parentName);
			return new CollectionHandle(davClient, parentLocator);

		} catch (MalformedURLException e) {
			throw new SystemException(e);
		}
	}

	/**
	 * Return a <code>URLTable</code> which contains all of this resources
	 * properties to the given depth. The returned <code>URLTable</code>
	 * maps resource <code>URL</code>s to <code>Hashtable</code>s 
	 * which in turn maps property <code>QualifiedName</code>s to
	 * <code>PropertyStatus</code>.
	 *
	 * @param depth the depth of the request; for example, 
	 * <code>Context.DEPTH_ZERO</code>.
	 * @return a <code>URLTable</code> containing properties.
	 * @exception DAVException if there was a problem retrieving the properties.
	 * @see #getProperties(Collection, String)
	 */
	public URLTable getProperties(String depth) throws DAVException {
		return getProperties((Collection) null, depth);
	}

	/**
	 * Fetches and returns the specified properties for this resource and its
	 * children to the given depth. The returned table is a URLTable of
	 * hashtables. The keys in the first table are the <code>URL</code>s of
	 * the resources. The nested table is a table where the keys are the names
	 * (<code>QualifiedName</code>) of the properties and the values are the
	 * properties' values (<code>PropertyStatus</code>).
	 *
	 * @param propertyNames collection of property names to search for
	 * (<code>QualifiedName</code>), or <code>null</code> to retrieve
	 * all properties.
	 * @param depth the depth of the search (eg. <code>Context.DEPTH_INFINITY</code>)
	 * @return URLTable of hashtables keyed by resource <code>URLKey</code>
	 * then by property name.
	 * @exception DAVException if there was a problem fetching the properties.
	 * @see IServer#propfind(Locator, Context, Document)
	 */
	public URLTable getProperties(Collection propertyNames, String depth) throws DAVException {
		// Set up the request context.
		IContext context = newContext();
		context.setDepth(depth);

		// Set up the request body.
		Document document = newDocument();
		PropFind propfind = PropFind.create(document);

		// null is a special value meaning 'all properties'.
		if (propertyNames == null)
			propfind.setIsAllProp(true);
		else {
			// Add all the property names to the request body.
			Prop prop = propfind.setProp();
			Iterator namesItr = propertyNames.iterator();
			while (namesItr.hasNext())
				prop.addPropertyName((QualifiedName) namesItr.next());
		}

		// Were ready to make the server call.
		IResponse response = null;
		try {
			// This contacts the server.
			response = davClient.propfind(locator, context, document);
			examineResponse(response);

			// Create a multi-status element editor on the response.
			if (!response.hasDocumentBody())
				throw new DAVException(Policy.bind("exception.respMustShareXMLDoc")); //$NON-NLS-1$
			Element documentElement = response.getDocumentBody().getDocumentElement();
			if (documentElement == null)
				throw new DAVException(Policy.bind("exception.respHasInvalidDoc")); //$NON-NLS-1$
			MultiStatus multiStatus = new MultiStatus(documentElement);

			// Construct a URLTable of results to return to the user.
			return extractPropStats(multiStatus);

		} catch (IOException e) {
			throw new SystemException(e);
		} catch (MalformedElementException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Return the property status for the property with the given name.
	 *
	 * @param propertyName the name of the property
	 * @return the property status
	 * @exception DAVException if there was a problem getting the property
	 * @see #getProperties(Collection, String)
	 */
	public PropertyStatus getProperty(QualifiedName propertyName) throws DAVException {
		Collection names = new HashSet();
		names.add(propertyName);
		URLTable result = getProperties(names, IContext.DEPTH_ZERO);

		URL url = null;
		try {
			url = new URL(locator.getResourceURL());
		} catch (MalformedURLException e) {
			throw new SystemException(e);
		}

		Hashtable propTable = (Hashtable) result.get(url);
		if (propTable == null)
			throw new DAVException(Policy.bind("exception.lookup", url.toExternalForm())); //$NON-NLS-1$
		return (PropertyStatus) propTable.get(propertyName);
	}

	/**
	 * Fetch and return the property names for the resource, and the children
	 * resources to the specified depth. Returns <code>URLTable</code>
	 * mapping resource URLs to enumerations over the property names for that
	 * resource.
	 *
	 * @param depth eg. <code>Context.DEPTH_ZERO</code>
	 * @return a <code>URLTable</code> of <code>Enumerations</code> over
	 * <code>QualfiedNames</code>
	 * @throws DAVException if there was a problem getting the property names
	 * @see IServer#propfind(Locator, Context, Document)
	 */
	public URLTable getPropertyNames(String depth) throws DAVException {

		// create and send the request
		IContext context = newContext();
		context.setDepth(depth);

		IResponse response = null;
		try {
			Document document = newDocument();
			PropFind propfind = PropFind.create(document);
			propfind.setIsPropName(true);

			response = davClient.propfind(locator, context, document);
			examineResponse(response);

			if (!response.hasDocumentBody()) {
				throw new DAVException(Policy.bind("exception.respMustHaveElmtBody")); //$NON-NLS-1$
			}
			Element documentElement = response.getDocumentBody().getDocumentElement();
			if (documentElement == null) {
				throw new DAVException(Policy.bind("exception.bodyMustHaveElmt")); //$NON-NLS-1$
			}
			MultiStatus multistatus = new MultiStatus(documentElement);

			//construct the URLTable to return to the user
			URLTable reply = new URLTable(10);
			Enumeration responses = multistatus.getResponses();
			while (responses.hasMoreElements()) {
				ResponseBody responseBody = (ResponseBody) responses.nextElement();
				String href = responseBody.getHref();
				URL resourceUrl = new URL(new URL(locator.getResourceURL()), href);
				Enumeration propstats = responseBody.getPropStats();
				Vector vector = new Vector();
				while (propstats.hasMoreElements()) {
					PropStat propstat = (PropStat) propstats.nextElement();
					Prop prop = propstat.getProp();
					Enumeration names = prop.getPropertyNames();
					while (names.hasMoreElements()) {
						QualifiedName dname = (QualifiedName) names.nextElement();
						vector.addElement(dname);
					}
				}
				reply.put(resourceUrl, vector.elements());
			}
			return reply;
		} catch (IOException e) {
			throw new SystemException(e);
		} catch (MalformedElementException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Retrieve the version tree infomration for the receiver, assuming
	 * that the receiver is a version or a version-controlled resource.
	 * <p>
	 * The version tree info comprises a <code>URLTable</code> whose keys
	 * are the <code>URL</code>s of each version in the version history,
	 * and whose values are <code>Vector</code>s of the resource's immediate
	 * predecessor <code>URL</code>s.  Note that the root version is
	 * (uniquely) identified by an empty set of predecessors.</p>
	 *
	 * @return the map from resource URL to predecessor set.
	 */
	public URLTable getVersionTree() throws DAVException {

		// Issue a version tree report against the receiver to retrieve
		// the successor set of all the versions.
		Document document = newDocument();
		Element root = ElementEditor.create(document, "version-tree"); //$NON-NLS-1$
		Element propElement = ElementEditor.appendChild(root, "prop"); //$NON-NLS-1$
		ElementEditor.appendChild(propElement, "predecessor-set"); //$NON-NLS-1$

		IResponse response = null;
		try {
			// Run the REPORT and check for errors.
			response = davClient.report(locator, newContext(), document);
			examineResponse(response);

			if (!response.hasDocumentBody())
				throw new DAVException(Policy.bind("exception.respMustHaveElmtBody")); //$NON-NLS-1$

			// Get the body as a MultiStatus.
			Element documentElement = response.getDocumentBody().getDocumentElement();
			if (documentElement == null)
				throw new DAVException(Policy.bind("exception.bodyMustHaveElmt")); //$NON-NLS-1$
			MultiStatus multistatus = new MultiStatus(documentElement);

			// Construct the predecessor table.
			// This will contain the result.
			URLTable predecessorTable = new URLTable();

			// For each response.	
			Enumeration responses = multistatus.getResponses();
			while (responses.hasMoreElements()) {
				ResponseBody responseBody = (ResponseBody) responses.nextElement();

				// Get the absolute URL of the resource.
				String href = responseBody.getHref();
				URL resourceURL = new URL(new URL(locator.getResourceURL()), href);

				// Add an entry to the predecessor table.
				Vector predecessors = new Vector();
				predecessorTable.put(resourceURL, predecessors);

				// For each propstat.
				Enumeration propstats = responseBody.getPropStats();
				while (propstats.hasMoreElements()) {
					PropStat propstat = (PropStat) propstats.nextElement();

					// We are going to assume that the status is OK, or error out.
					if (propstat.getStatusCode() != IResponse.SC_OK)
						throw new DAVException(Policy.bind("exception.errorRetrievingProp")); //$NON-NLS-1$

					// For each property in the prop (there should only be one).
					Prop prop = propstat.getProp();
					Enumeration elements = prop.getProperties();
					while (elements.hasMoreElements()) {
						Element element = (Element) elements.nextElement();

						//  Look explicitly for the DAV:predecessor-set
						QualifiedName name = ElementEditor.getQualifiedName(element);
						if (name.equals(DAV_PREDECESSOR_SET)) {
							Enumeration e = new HrefSet(element, DAV_PREDECESSOR_SET).getHrefs();
							while (e .hasMoreElements()) {
								URL predURL = new URL((String) e.nextElement());
								predecessors.add(predURL);
							} // end-while
						} //end-if
					} // end-while
				} // end-while
			} //end-while

			// Phew, were done.
			return predecessorTable;

		} catch (IOException e) {
			throw new SystemException(e);
		} catch (MalformedElementException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	public CollectionHandle[] getWorkspaceCollections() throws DAVException {

		PropertyStatus propertyStatus = getProperty(DAV_WORKSPACE_COLLECTION_SET);

		Vector v = new Vector(5);
		Element element = propertyStatus.getProperty();

		if (!ElementEditor.isDAVElement(element, "workspace-collection-set")) //$NON-NLS-1$
			throw new DAVException(Policy.bind("exception.malformedElement")); //$NON-NLS-1$

		Element child = ElementEditor.getFirstChild(element, "href"); //$NON-NLS-1$
		while (child != null) {
			String href = ElementEditor.getFirstText(child);
			ILocator locator = davClient.getDAVFactory().newLocator(href);
			v.addElement(new CollectionHandle(davClient, locator));
			child = ElementEditor.getNextSibling(child);
		}

		CollectionHandle[] result = new CollectionHandle[v.size()];
		v.copyInto(result);
		return result;
	}

	/**
	 * Return the header from a message send to the server.
	 *
	 * @return a context with the message header contents
	 * @exception DAVException if there was a problem sending the message
	 * @see IServer#head(Locator, Context)
	 */
	public IContext head() throws DAVException {
		IResponse response = null;
		try {
			response = davClient.head(locator, newContext());
			examineResponse(response);
			return response.getContext();
		} catch (IOException exception) {
			throw new SystemException(exception);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Return a boolean value indicating whether or not this resource is a
	 * collection.
	 * <p>
	 * A resource is a collection (i.e., implements collection semantics) if
	 * it's resource type includes a &lt;DAV:collection&gt; element.</p>
	 *
	 * @return boolean <code>true</code> if the resource implements collection
	 * semantics, and <code>false</code> otherwise.
	 */
	public boolean isCollection() throws DAVException {
		return propertyHasChild(DAV_RESOURCE_TYPE, DAV_COLLECTION_RESOURCE_TYPE);
	}

	/**
	 * Return a boolean value indicating whether or not this resource
	 * is currently locked.
	 *
	 * @return boolean indicator
	 * @exception DAVException if there was a problem getting the locks
	 * @see #getLocks()
	 */
	public boolean isLocked() throws DAVException {
		// see if there are any active locks
		return getLocks().hasMoreElements();
	}

	/**
	 * Lock this resource with default values.
	 *
	 * <p>Note: default values of DEPTH_ZERO for depth and -1 for timeout are used.</p>
	 *
	 * @return the lock token
	 * @exception DAVException if there was a problem locking this resource
	 * @see #lock(boolean, String, int, String)
	 */
	public LockToken lock() throws DAVException {
		return lock(false, IContext.DEPTH_ZERO, -1, null);
	}

	/**
	 * Lock this resource using the specified parameters.
	 *
	 * @param isShared true if the lock is shared, false if the lock is exclusive
	 * @param depth eg. <code>Context.DEPTH_ZERO</code>
	 * @param timeout the timeout value for the lock
	 * @param owner the owner of the lock
	 * @return the lock token
	 * @exception DAVException if there was a problem locking this resource
	 * @see IServer#lock(Locator, Context, Document)
	 */
	public LockToken lock(boolean isShared, String depth, int timeout, String owner) throws DAVException {

		// Define the request context.
		IContext context = newContext();
		context.setDepth(depth);
		context.setTimeout(timeout);

		// Create the request body.
		Document document = newDocument();
		LockInfo lockinfo = LockInfo.create(document);
		lockinfo.setIsShared(isShared);

		// Add the owner if it is given.
		if (owner != null) {
			Owner ownerEditor = lockinfo.setOwner();
			ownerEditor.getElement().appendChild(document.createTextNode(owner));
		}

		// Send the lock request.
		IResponse response = null;
		try {
			response = davClient.lock(locator, context, document);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}

		// Extract the token from the resulting context.
		LockToken token = new LockToken(response.getContext().getLockToken());
		//fServerManager.addLock(newURL(fLocator.getResourceURL()), token, depth);
		return token;
	}

	/**
	 * Move this resource to the destination specified by the given locator.
	 * Use default values for overwrite and properties to move.
	 *
	 * @param destination the location to move this resource to
	 * @exception DAVException if there was a problem moving this resource
	 * @see #move(Locator, boolean, Enumeration)
	 */
	public void move(ILocator destination) throws DAVException {
		move(destination, false, null);
	}

	/**
	 * Move this resource to the location specified by the given locator.
	 * If a resource already exists at the destination and the overwrite
	 * boolean is true, then write over top of the existing resource. Otherwise
	 * do not. The enumeration is over qualified names which are the names of
	 * the properties to move.
	 *
	 * @param destination the location to move to
	 * @param overwrite how to react if a resource already exists at the
	 *   destination
	 * @param names <code>Enumeration</code> over <code>QualifiedNames</code>
	 * @exception DAVException if there was a problem moving this resource
	 * @see IServer#move(Locator, Locator, Context, Document)
	 */
	public void move(ILocator destination, boolean overwrite, Enumeration names) throws DAVException {
		IContext context = newContext();
		context.setOverwrite(overwrite);

		Document document = newDocument();
		PropertyBehavior propertyBehavior = PropertyBehavior.create(document);

		if (names == null) {
			propertyBehavior.setIsKeepAllAlive(true);
		} else {
			while (names.hasMoreElements()) {
				Object obj = names.nextElement();
				Assert.isTrue(obj instanceof QualifiedName, Policy.bind("assert.propNameMustBeEnumOverQual")); //$NON-NLS-1$
				// fix this...can we really add property names to href elements?
				propertyBehavior.addProperty(((QualifiedName) obj).getLocalName());
			}
		}

		IResponse response = null;
		try {
			response = davClient.move(locator, destination, context, document);
			examineResponse(response);
			examineMultiStatusResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Answer a new empty context for requests sent to the
	 * receivers server.
	 *
	 * @return a new request <code>Context</code>.
	 */
	protected IContext newContext() {
		return davClient.getDAVFactory().newContext();
	}

	/**
	 * Answer a new empty DOM Document suitable for creating requests
	 * to the receiver's server.
	 *
	 * @return a new DOM <code>Document</code>.
	 */
	protected Document newDocument() {
		return davClient.getDAVFactory().newDocument();
	}

	/**
	 * Check in the receiver and answer a new Locator on the
	 * resulting version resource.
	 *
	 * @return the <code>Locator</code> to the receiver's version.
	 * @throws DAVException if a problem occurs with the check in request.
	 */
	protected ILocator protectedCheckIn() throws DAVException {
		IResponse response = null;
		try {
			response = davClient.checkin(locator, newContext(), null);
			examineResponse(response);
			String versionUrl = response.getContext().getLocation();
			return davClient.getDAVFactory().newStableLocator(versionUrl);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Check out the receiver and answer a new Locator on the
	 * resulting checked out resource.  The result MAY be the same
	 * as the receiver's Locator if the server did not create
	 * a new resource as a consequence of the check out (i.e.
	 * if it was checking out a vesion-controlled resource rather
	 * than a version).
	 *
	 * @return the <code>Locator</code> to the receiver's version.
	 * @throws DAVException if a problem occurs with the check in request.
	 */
	protected ILocator protectedCheckOut() throws DAVException {
		IResponse response = null;
		try {
			response = davClient.checkout(locator, newContext(), null);
			examineResponse(response);
			String resourceUrl = response.getContext().getLocation();
			return davClient.getDAVFactory().newStableLocator(resourceUrl);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Refresh the lock on this resource with the given lock token. Use
	 * the specified timeout value.
	 *
	 * @param lockToken the lock token to refresh
	 * @param timeout the new timeout value to use
	 * @exception DAVException if there was a problem refreshing the lock
	 */
	public void refreshLock(LockToken lockToken, int timeout) throws DAVException {
		// Set up the request in the context.
		IContext context = newContext();
		context.setTimeout(timeout);
		context.setLockToken(lockToken.getToken());

		// Send the request to the server.
		IResponse response = null;
		try {
			response = davClient.lock(locator, context, null);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Remove the properties with the given names, from this resource.
	 *
	 * @param propertyNames <code>Enumeration</code> over
	 *   <code>QualifiedNames</code>
	 * @exception DAVException if there was a problem removing the
	 *   properties
	 * @see IServer#proppatch(Locator, Context, Document)
	 */
	public void removeProperties(Collection propertyNames) throws DAVException {
		Assert.isNotNull(propertyNames);

		// Removing no properties is easy.
		if (propertyNames.isEmpty())
			return;

		// Add the names of the properties to remove to the request body.
		Document document = newDocument();
		PropertyUpdate propertyUpdate = PropertyUpdate.create(document);
		Prop prop = propertyUpdate.addRemove();
		Iterator namesItr = propertyNames.iterator();
		while (namesItr.hasNext())
			prop.addPropertyName((QualifiedName) namesItr.next());

		// Send the PROPPATCH request.
		IResponse response = null;
		try {
			response = davClient.proppatch(locator, newContext(), document);
			examineResponse(response);
			examineMultiStatusResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Remove the property with the given name from this resource.
	 *
	 * @param propertyName the name of the property to remove
	 * @exception DAVException if there was a problem removing the property
	 * @see #removeProperties(Collection)
	 */
	public void removeProperty(QualifiedName propertyName) throws DAVException {
		Collection propertyNames = new Vector(1);
		propertyNames.add(propertyName);
		removeProperties(propertyNames);
	}

	/**
	 * Set the content of this resource to be the untyped data stored in the given
	 * input stream.
	 * The stream will automatically be closed after the data
	 * is consumed.  If the resource does not exist it is created with the
	 * given content.
	 *
	 * @param input the inputstream containing the resource contents.
	 * @exception DAVException if there was a problem setting the contents.
	 * @see #setContent(String, InputStream)
	 * @see IServer#put(Locator, Context, InputStream)
	 */
	public void setContent(InputStream input) throws DAVException {
		setContent("application/octet-stream", input); //$NON-NLS-1$
	}

	/**
	 * Set the content of this resource to be the data stored in the given
	 * input stream. The type encoding is given in the content type argument, and
	 * should be in the media format described by RFC2616 Sec 3.7.
	 * The stream will automatically be closed after the data
	 * is consumed.  If the resource does not exist it is created with the
	 * given content.
	 *
	 * @param contentType the media type for the data on the input stream.
	 * @param input the inputstream containing the resource contents.
	 * @exception DAVException if there was a problem setting the contents.
	 * @see IServer#put(Locator, Context, InputStream)
	 */
	public void setContent(String contentType, InputStream input) throws DAVException {
		IResponse response = null;
		try {
			IContext context = newContext();
			context.setContentType(contentType);
			response = davClient.put(locator, context, input);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Set the given properties on this resource.
	 *
	 * @param properties a <code>Collection</code> of property <code>Element</code>s.
	 * @exception DAVException if there was a problem setting the properties.
	 * @see IServer#proppatch(Locator, Context, Document)
	 */
	public void setProperties(Collection properties) throws DAVException {
		Assert.isNotNull(properties);

		// Setting no properties is a no-op.
		if (properties.isEmpty())
			return;

		// Build the request body to describe the properties to set.
		Document document = newDocument();
		PropertyUpdate propertyUpdate = PropertyUpdate.create(document);
		Prop prop = propertyUpdate.addSet();

		Iterator propertiesItr = properties.iterator();
		while (propertiesItr.hasNext()) {
			Element element = (Element) propertiesItr.next();
			try {
				prop.addProperty(element);
			} catch (MalformedElementException exception) {
				throw new SystemException(exception);
			}
		} // end-while

		// Send the request to the server and examine the response for failures.
		IResponse response = null;
		try {
			response = davClient.proppatch(locator, newContext(), document);
			examineResponse(response);
			examineMultiStatusResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Set the given property on this resource.
	 *
	 * @param property the property to set
	 * @exception DAVException if there was a problem setting the property
	 * @see #setProperties(Collection)
	 */
	public void setProperty(Element property) throws DAVException {
		Collection properties = new Vector(1);
		properties.add(property);
		setProperties(properties);
	}

	/**
	 * Return a string representation of this resource. Used for
	 * debugging purposes only.
	 *
	 * @return this resource, as a string
	 */
	public String toString() {
		return locator.getResourceURL();
	}

	/**
	 * Send a message to the server. The contents of the resulting
	 * input stream should be the message that was sent, echoed
	 * back to the client.
	 * <p>
	 * The input stream should be closed by the user.</p>
	 *
	 * @return an input stream on the request as received.
	 * @exception DAVException if there was a problem sending the
	 * request to the server.
	 * @see IServer#trace(Locator, Context)
	 */
	public ResponseInputStream trace() throws DAVException {

		IResponse response = null;
		try {
			response = davClient.trace(locator, newContext());
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} catch (DAVException e) {
			closeResponse(response);
			throw e;
		}
		return new ResponseInputStream(response);
	}

	/**
	 * Unlock this resource with the given lock token.
	 *
	 * @param token the lock token to remove from this resource
	 * @exception DAVException if there was a problem unlocking this resource
	 * @see IServer#unlock(Locator, Context)
	 */
	public void unlock(LockToken token) throws DAVException {
		// Send the lock token in the header of the request.
		IContext context = newContext();
		context.setLockToken("<" + token.getToken() + ">"); //$NON-NLS-1$ //$NON-NLS-2$

		IResponse response = null;
		try {
			response = davClient.unlock(locator, context);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Perform an UPDATE on the receiver to set the version it
	 * is based upon.
	 *
	 * @param version the <code>Locator</code> of the version that
	 * is the target of the update request.
	 * @throws DAVException if a problem occurs executing the update
	 * on the WebDAV server.
	 */
	public void update(ILocator version) throws DAVException {
		Document document = newDocument();
		Update.createVersion(document, version.getResourceURL());

		IResponse response = null;
		try {
			response = davClient.update(locator, newContext(), document);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Bring the receiver under version control.  This means that
	 * the receiver is replaced by a version-controlled resource.
	 * Note that the client may send version control to a resource
	 * that is already under version control with no adverse effects.
	 *
	 * @throws DAVException if a problem occurs bringing the
	 * resource under version control.
	 */
	public void versionControl() throws DAVException {
		IResponse response = null;
		try {
			response = davClient.versionControl(locator, newContext(), null);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Delete this resource from the repository, optionally succeeding
	 * in the delete if the resource was not found on the server.
	 *
	 * @param mustExist if <code>true</code> then the delete will
	 * fail if the resource was not on the server at the time of the
	 * delete request.  If <code>false</code> the delete will succeed if
	 * there was no such resource to delete.
	 * @exception DAVException if there was a problem deleting
	 * this resource.
	 * @see IServer#delete(Locator, Context)
	 */
	public void delete(boolean mustExist) throws DAVException {
		IResponse response = null;
		try {
			response = davClient.delete(locator, newContext());
			if (!mustExist && (response.getStatusCode() == IResponse.SC_NOT_FOUND))
				return;
			examineResponse(response);
			examineMultiStatusResponse(response);
		} catch (IOException exception) {
			throw new SystemException(exception);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Check the given status code and throw a WebDAV
	 * exception if the code indicates failure.  If the code
	 * is success, this method does nothing.
	 *
	 * @param code the status code to check.
	 * @param message the status message accompanying the code.
	 * @exception WebDAVException if the server returned an HTTP/WebDAV
	 * error code (i.e., anything outside the 200-series codes).
	 */
	protected void examineStatusCode(int code, String message) throws WebDAVException {
		if (code >= 300 && code <= 399)
			throw new RedirectionException(code, message);
		if (code >= 400 && code <= 499)
			throw new ClientException(code, message);
		if (code >= 500 && code <= 599)
			throw new ServerException(code, message);
	}

	/**
	 * Return a boolean value indicating whether or not this resource
	 * exists on the server.
	 * <p>
	 * This implementation uses the HTTP HEAD method so the URL may or
	 * may not exist in the DAV namespace. The DAV RESOURCE_TYPE property
	 * is NOT checked.</p>
	 *
	 * @return boolean <code>true</code> if the resource exists on the server
	 * or <code>false</code> otherwise.
	 * @exception DAVException if there was a problem checking for
	 * existence.
	 */
	public boolean exists() throws DAVException {

		// Test existance by issuing a HEAD request.
		IResponse response = null;
		try {
			response = davClient.head(locator, newContext());
			// If the resource was not found, then that answers the question.
			if (response.getStatusCode() == IResponse.SC_NOT_FOUND)
				return false;

			// Otherwise check for errors.
			examineResponse(response);

			// No problems by this point, so the resource is there and OK.
			return true;
		} catch (IOException exception) {
			throw new SystemException(exception);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Check to see if the resource is checked in (i.e., is an immutable
	 * resource).
	 * <p>
	 * The resource is checked in if it has a &lt;DAV:checked-in&gt;
	 * property.</p>
	 *
	 * @return <code>true</code> if the resource is checked in
	 * and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isCheckedIn() throws DAVException {
		return supportsLiveProperty(DAV_CHECKED_IN);
	}

	/**
	 * Check to see if the resource is checked-out.
	 * <p>
	 * The resource is checked out if it has a &lt;DAV:checked-out&gt;
	 * property.</p>
	 *
	 * @return <code>true</code> if the resource is checked out
	 * and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isCheckedOut() throws DAVException {
		return supportsLiveProperty(DAV_CHECKED_OUT);
	}

	/**
	 * Check to see if the resource is a version.
	 * <p>
	 * The resource is a version if it has &lt;DAV:version-name&gt;
	 * in the &lt;DAV:supported-live-properties-set&gt;.</p>
	 *
	 * @return <code>true</code> if the resource is a version
	 * and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isVersion() throws DAVException {
		return supportsLiveProperty(DAV_VERSION_NAME);
	}

	/**
	 * Check to see if the resource is under version control.
	 * <p>
	 * The resource is version controlled if it has &lt;DAV:auto-checkout&gt;
	 * in the &lt;DAV:supported-live-properties-set&gt;.</p>
	 *
	 * @return <code>true</code> if the resource is under version
	 * control and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isVersionControlled() throws DAVException {
		return supportsLiveProperty(DAV_AUTO_CHECKOUT);
	}

	/**
	 * Check to see if the resource is a working resource.
	 * <p>
	 * The resource is a working resource if it has
	 * &lt;DAV:checked-out&gt; and does not have &lt;DAV:auto-checkout&gt;
	 * in the &lt;DAV:supported-live-properties-set&gt;.</p>
	 *
	 * @return <code>true</code> if the resource is a working resource
	 * and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isWorkingResource() throws DAVException {
		PropertyStatus propertyStat = getProperty(DAV_SUPPORTED_LIVE_PROPERTY_SET);
		// If the live-property-set is not supported, then the answer is 'no'.
		if (propertyStat.getStatusCode() == IResponse.SC_NOT_FOUND)
			return false;
		// If there was a problem getting the live property set, throw an exception.
		examineStatusCode(propertyStat.getStatusCode(), propertyStat.getStatusMessage());
		// Check to see if the required properties are/are not in the supported set.
		try {
			Element propertySet = propertyStat.getProperty();
			return ((ElementEditor.hasChild(propertySet, DAV_CHECKED_OUT)) && !(ElementEditor.hasChild(propertySet, DAV_AUTO_CHECKOUT)));
		} catch (MalformedElementException exception) {
			throw new SystemException(exception);
		}
	}

	/**
	 * This is a helper method to check to see if the resource has a
	 * property with the given name that in turn has a child with a
	 * given name.
	 *
	 * @return <code>true</code> if the resource does have such a
	 * property with a named child and <code>false</code> if it does
	 * not have such a property or does not have such a child of the
	 * property.
	 * @throws DAVException if a problem occurs determining result
	 * from the server.
	 */
	protected boolean propertyHasChild(QualifiedName propertyName, QualifiedName childName) throws DAVException {
		// If the property is not found, then the answer is 'no'.
		PropertyStatus propertyStat = getProperty(propertyName);
		if (propertyStat.getStatusCode() == IResponse.SC_NOT_FOUND)
			return false;
		// If there was a problem getting the property, throw an exception.
		examineStatusCode(propertyStat.getStatusCode(), propertyStat.getStatusMessage());
		// Check to see if the named child is in the retrieved property.
		try {
			return ElementEditor.hasChild(propertyStat.getProperty(), childName);
		} catch (MalformedElementException exception) {
			throw new SystemException(exception);
		}
	}

	/**
	 * Check to see if the resource supports the named live property.
	 *
	 * @return <code>true</code> if the resource does support the live
	 * property and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining result
	 * from the server.
	 */
	public boolean supportsLiveProperty(QualifiedName propertyName) throws DAVException {
		return propertyHasChild(DAV_SUPPORTED_LIVE_PROPERTY_SET, propertyName);
	}

}
