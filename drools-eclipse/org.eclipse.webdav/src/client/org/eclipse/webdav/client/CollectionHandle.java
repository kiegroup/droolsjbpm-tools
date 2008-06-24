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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.eclipse.webdav.*;
import org.eclipse.webdav.dom.ElementEditor;
import org.eclipse.webdav.dom.MalformedElementException;
import org.eclipse.webdav.internal.kernel.*;
import org.eclipse.webdav.internal.kernel.utils.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The <code>CollectionHandle</code> class represents a resource on the
 * WebDAV server that supports collection semantics.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class CollectionHandle extends AbstractResourceHandle {

	/**
	 * Creates a new <code>CollectionHandle</code> from the given
	 * <code>DAVClient</code> and <code>Locator</code>.
	 * <p>
	 * A CollectionHandle is a resource handle for DAV resources
	 * with internal members.</p>
	 *
	 * @param davClient the client used to access the WebDAV server.
	 * @param locator the reference to the collection resource on the
	 * server.
	 */
	public CollectionHandle(DAVClient davClient, ILocator locator) {
		super(davClient, locator);
	}

	/**
	 * Bring the receiver under baseline control.
	 *
	 * @throws DAVException if the baseline control operation failed.
	 * @see IServer#baselineControl(Locator, Context, Document)
	 */
	public void baselineControl() throws DAVException {
		IResponse response = null;
		try {
			response = davClient.baselineControl(locator, newContext(), null);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Create a new version-controlled configuration on the given baseline.
	 *
	 * @throws DAVException if the baseline control operation failed.
	 * @see IServer#baselineControl(Locator, Context, Document)
	 */
	public void baselineControl(ILocator baseline) throws DAVException {
		Assert.isNotNull(baseline);

		// Build the document body to describe the baseline control element.
		Document document = newDocument();
		Element root = ElementEditor.create(document, "baseline-control"); //$NON-NLS-1$
		ElementEditor.addChild(root, "baseline", //$NON-NLS-1$
				baseline.getResourceURL(), new String[] {"baseline"}, //$NON-NLS-1$
				true);

		// Send the baseline control method to the server and check the response.
		IResponse response = null;
		try {
			response = davClient.baselineControl(locator, newContext(), document);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Binds the given member in this collection to the resource identified
	 * by the given source locator. If the member already exists, or is
	 * already bound to a resource, it is not replaced.
	 *
	 * @param member a member in this collection
	 * @param source the location of a resource
	 * @throws DAVException if the binding could not be created
	 * @see #bind(String, Locator, boolean)
	 * @see IServer#bind(Locator, Locator, Context)
	 */
	public void bind(String member, ILocator source) throws DAVException {
		bind(member, source, false);
	}

	/**
	 * Binds the given member in this collection to the resource identified
	 * by the given source locator. If overwrite is <code>false</code> and
	 * such a member already exists, or such a member is already bound to a
	 * resource, it is not replaced. Otherwise, if overwrite is
	 * <code>true</code> and such a member already exists, or such a member
	 * is already bound to a resource, it is replaced.
	 *
	 * @param member    a member in this collection
	 * @param source    the location of a resource
	 * @param overwrite a boolean indicating whether or not any existing
	 *                  resource or binding is replaced
	 * @throws DAVException if the binding could not be created
	 * @see #bind(String, Locator, boolean)
	 * @see IServer#bind(Locator, Locator, Context)
	 */
	public void bind(String member, ILocator source, boolean overwrite) throws DAVException {
		IContext context = newContext();
		context.setOverwrite(overwrite);
		ILocator destination = getMember(member);

		IResponse response = null;
		try {
			response = davClient.bind(source, destination, context);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Check out this resource. Returns a resource handle on the checked out
	 * version selector, or the working resource if a version is checked out.
	 * <p>
	 * Note that a checked-out version-controlled collection has members that are
	 * themselves version-controlled resources, or unversioned resources; however,
	 * working collection members are always version history resources.</p>
	 * 
	 * @return the checked out resource as a <code>CollectionHandle</code>.
	 * @throws DAVException if there is a problem checking out the receiver.
	 */
	public AbstractResourceHandle checkOut() throws DAVException {
		ILocator locator = protectedCheckOut();
		return new CollectionHandle(davClient, locator);
	}

	/**
	 * Create this collection in the repository.
	 * <p>
	 * This corresponds to a WebDAV MKCOL method.</p>
	 *
	 * @exception DAVException if there was a problem creating this collection
	 * @see IServer#mkcol(ILocator, IContext, IElement)
	 */
	public void create() throws DAVException {
		IResponse response = null;
		try {
			response = davClient.mkcol(locator, newContext(), null);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Create the receiver and any parent collections that must be created
	 * on the path to the receiver.
	 * 
	 * @throws DAVException if there is a problem creating the collections.
	 */
	public void mkdirs() throws DAVException {
		mkdirs(this);
	}

	protected void mkdirs(CollectionHandle handle) throws DAVException {
		IResponse response = null;
		try {
			// Attempt to create the collection.
			response = davClient.mkcol(handle.locator, newContext(), null);

			int status = response.getStatusCode();
			if (status == IResponse.SC_METHOD_NOT_ALLOWED) {
				// A resource already exists at this location.
				// Check that it is a collection resource (otherwise fall through to examine response)
				if (handle.isCollection())
					return;
			}
			
			if (status == IResponse.SC_FORBIDDEN) {
				// if this is a collection and exists return
				if (handle.isCollection() && handle.exists())
					return;
			}
			
			if (status == IResponse.SC_CONFLICT) {
				// close the response and try to create the parents
				closeResponse(response);
				// Intermediate collections are missing.
				CollectionHandle parent = handle.getParent();
				mkdirs(parent);
				// re-attempt to create the folder after the parents have been created
				response = davClient.mkcol(handle.locator, newContext(), null);
			}
			// We got some other response code, so check for failures.
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}

	/**
	 * Answer the given member of the receiver as a collection handle.
	 * 
	 * @throws DAVException if there is a problem creating the new handle.
	 */
	public CollectionHandle getCollectionHandle(String name) throws DAVException {
		return new CollectionHandle(davClient, getMember(name));
	}

	/**
	 * Return the locator of the member of this collection, with the given
	 * name.  Does NOT perform a call to the server to check the existence
	 * of the member.
	 *
	 * @param memberName the name of the receiver's internal member.
	 * @return the Locator for the member.
	 */
	public ILocator getMember(String memberName) {
		Assert.isTrue(locator.getLabel() == null);
		Assert.isTrue(!locator.isStable());
		String parentName = locator.getResourceURL();
		String childName;
		if (parentName.endsWith("/")) //$NON-NLS-1$
			childName = parentName + memberName;
		else
			childName = parentName + "/" + memberName; //$NON-NLS-1$
		return davClient.getDAVFactory().newLocator(childName);
	}

	/**
	 * Return a set of handles representing the members of this
	 * collection.
	 * <p>
	 * Each member of the set will be typed to be a <code>ResourceHandle</code>
	 * or a <code>CollectionHandle</code> depending upon whether it implements
	 * collection semantics.  Note that workspaces will be returned as
	 * regular collection handles and should be converted to workspace handles
	 * if required (test using isWorkspace()).</p>
	 *
	 * @return a <code>Set</code> of <code>ResourceHandle</code> and/or <code>
	 * CollectionHandle</code> or an empty set if the receiver has no members.
	 * @exception DAVException if there was a problem getting the members.
	 */
	public Set getMembers() throws DAVException {

		// Query the DAV:resource-type property to depth one.
		Collection querySet = new Vector();
		querySet.add(DAV_RESOURCE_TYPE);
		URLTable resourceTable = getProperties(querySet, IContext.DEPTH_ONE);

		// Create a collection for the reply, and remove
		// ourselves from the answer.
		Set reply = new HashSet();
		try {
			resourceTable.remove(locator.getResourceURL());
		} catch (MalformedURLException exception) {
			throw new DAVException(Policy.bind("exception.malformedLocator")); //$NON-NLS-1$
		}

		// The keys of the result correspond to the receiver's internal members.
		Enumeration resourceNameEnum = resourceTable.keys();
		while (resourceNameEnum.hasMoreElements()) {
			URL url = (URL) resourceNameEnum.nextElement();

			// Get the props for that resource
			Hashtable propertyTable = (Hashtable) resourceTable.get(url);
			Assert.isNotNull(propertyTable);
			PropertyStatus propertyStatus = (PropertyStatus) propertyTable.get(DAV_RESOURCE_TYPE);
			Assert.isNotNull(propertyStatus);

			// If we have a DAV:collection element, then create a collection handle,
			// all other resource types are created as regular resource handles.
			ILocator newLocator = davClient.getDAVFactory().newLocator(url.toString());
			Element property = propertyStatus.getProperty();
			try {
				if (ElementEditor.hasChild(property, DAV_COLLECTION_RESOURCE_TYPE))
					reply.add(new CollectionHandle(davClient, newLocator));
				else
					reply.add(new ResourceHandle(davClient, newLocator));
			} catch (MalformedElementException exception) {
				throw new SystemException(exception);
			}
		} // end-while

		return reply;
	}

	public ResourceHandle getResourceHandle(String name) throws DAVException {
		return new ResourceHandle(davClient, getMember(name));
	}

	public WorkspaceHandle getWorkspaceHandle(String name) throws DAVException {
		return new WorkspaceHandle(davClient, getMember(name));
	}

	/**
	 * Check to see if the receiver is a workspace resource.
	 * <p>
	 * The resource is a workspace resource if it has
	 * &lt;DAV:workspace-checkout-set&gt; in the
	 * &lt;DAV:supported-live-properties-set&gt;.</p>
	 *
	 * @return <code>true</code> if the resource is a workspace
	 * and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isWorkspace() throws DAVException {
		return supportsLiveProperty(DAV_WORKSPACE_CHECKOUT_SET);
	}
}
