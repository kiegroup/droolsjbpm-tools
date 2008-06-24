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
package org.eclipse.webdav.http.client;

import java.net.URL;
import java.util.Map;

/**
 * Implementations of this interface are used by clients to store and
 * retrieve information for authentication purposes.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public interface IAuthenticator {

	/**
	 * Adds the given authentication information to the store. The
	 * information is relevant for the specified protection space and the
	 * given authentication scheme. The protection space is defined by the
	 * combination of the given server URL and realm. The authentication 
	 * scheme determines what the authentication information contains and how 
	 * it should be used. The authentication information is a <code>Map</code> 
	 * of <code>String</code> to <code>String</code> and typically
	 * contain information such as usernames and passwords.
	 *
	 * @param serverUrl the URL identifying the server for this authentication
	 *		information. For example, "http://www.hostname.com/".
	 * @param realm the subsection of the given server to which this
	 *		authentication information applies.  For example,
	 *		"realm1@hostname.com" or "" for no realm.
	 * @param scheme the scheme for which this authentication information
	 *		applies. For example, "Basic" or "" for no authentication scheme
	 * @param info a <code>Map</code> containing authentication information 
	 *		such as usernames and passwords
	 */
	public void addAuthenticationInfo(URL serverUrl, String realm, String scheme, Map info);

	/**
	 * Adds the specified resource to the protection space specified by the
	 * given realm. All resources at or deeper than the depth of the last
	 * symbolic element in the path of the given resource URL are assumed to
	 * be in the same protection space.
	 *
	 * @param resourceUrl the URL identifying the resources to be added to
	 *		the specified protection space. For example,
	 *		"http://www.hostname.com/folder/".
	 * @param realm the name of the protection space. For example,
	 *		"realm1@hostname.com"
	 */
	public void addProtectionSpace(URL resourceUrl, String realm);

	/**
	 * Returns the authentication information for the specified protection
	 * space and given authentication scheme. The protection space is defined
	 * by the given server URL and realm. Returns <code>null</code> if no
	 * such information exists.
	 * <p>This method is similar to <code>requestAuthenticationInfo</code>
	 * except the user is not prompted for the result. Instead, the store is
	 * queried for the information (such as username and password).
	 *
	 * @param serverUrl the URL identifying the server for the authentication
	 *		information. For example, "http://www.hostname.com/".
	 * @param realm the subsection of the given server to which the
	 *		authentication information applies.  For example,
	 *		"realm1@hostname.com" or "" for no realm.
	 * @param scheme the scheme for which the authentication information
	 *		applies. For example, "Basic" or "" for no authentication scheme
	 * @return the authentication information for the specified protection
	 *		space and given authentication scheme, or <code>null</code> if no
	 *		such information exists
	 * @see #requestAuthenticationInfo(URL, String, String)
	 */
	public Map getAuthenticationInfo(URL serverUrl, String realm, String scheme);

	/**
	 * Returns the protection space (realm) for the specified resource, or
	 * <code>null</code> if the realm is unknown.
	 *
	 * @param resourceUrl the URL of the resource whose protection space is
	 *		returned. For example, "http://www.hostname.com/folder/".
	 * @return the protection space (realm) for the specified resource, or
	 *		<code>null</code> if the realm is unknown
	 */
	public String getProtectionSpace(URL resourceUrl);

	/**
	 * Returns the authentication information for the specified protection
	 * space and given authentication scheme. The protection space is defined
	 * by the given server URL and realm. Returns <code>null</code> if no
	 * such information exists.
	 * <p>This method is similar to <code>getAuthenticationInfo</code> except
	 * the store is usually not queried for the result. Instead, the user is
	 * prompted for the information (such as username and password).
	 *
	 * @param resourceUrl the URL identifying the server for the authentication
	 *		information. For example, "http://www.hostname.com/".
	 * @param realm the subsection of the given server to which the
	 *		authentication information applies.  For example,
	 *		"realm1@hostname.com" or "" for no realm.
	 * @param scheme the scheme for which the authentication information
	 *		applies. For example, "Basic" or "" for no authentication scheme
	 * @return the authentication information for the specified protection
	 *		space and given authentication scheme, or <code>null</code> if no
	 *		such information exists
	 * @see #getAuthenticationInfo(URL, String, String)
	 */
	public Map requestAuthenticationInfo(URL resourceUrl, String realm, String scheme);
}
