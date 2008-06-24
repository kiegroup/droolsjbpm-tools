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
package org.eclipse.webdav.internal.authentication;

import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.URL;
import java.util.Map;
import org.eclipse.webdav.http.client.IAuthenticator;
import org.eclipse.webdav.http.client.Request;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * The <code>BasicAuthority</code> provides the necessary behavior to
 * authorizes client <code>Request</codes>s for communication with HTTP
 * servers using the Basic authentication scheme.
 *
 * @see AuthorizationAuthority
 */
public class BasicAuthority extends AuthorizationAuthority {

	/**
	 * Creates a new authenticator that stores its authentication information
	 * in the given authentication store.
	 * <p>The <code>BasicAuthenticator</code> authenticates according to the
	 * "Basic" authentication scheme.
	 * <p>Instances of this class must not be created directly, instead, use
	 * an instance of the class <code>Authenticator</code> to authorize
	 * requests.
	 *
	 * @param authenticationStore a store that holds authentication
	 * information
	 */
	public BasicAuthority(IAuthenticator authenticationStore) {
		super(authenticationStore);
	}

	/**
	 * Returns the Basic authorization credentials for the given username and
	 * password. The credentials have the following form:
	 * <code>
	 * credentials 			= "Basic" basic-credentials
	 * basic-credentials	= base64-user-pass
	 * base64-user-pass		= &lt;base64 encoding of user-pass, except not
	 * 						  limited to 76 char/line&gt;
	 * user-pass			= userid ":" password
	 * userid				= *&lt;TEXT excluding ":"&gt;
	 * password				= *TEXT
	 * </code>
	 * <P>Userids might be case sensitive.
	 * <P>For example, if the user's name is "Aladdin" and the user's
	 * password is "open sesame", the following credentials are supplied:
	 * <code>
	 * Basic QWxhZGRpbjpvcGVuIHN1c2FtZQ==
	 * </code>
	 * @param username
	 * @param password
	 * @return         the Basic authorization credentials for the given
	 *                 username and password
	 */
	private String credentials(String username, String password) {
		Assert.isNotNull(username);
		Assert.isNotNull(password);

		String userpass = username + ":" + password; //$NON-NLS-1$
		byte[] data = null;

		try {
			data = userpass.getBytes("UTF8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			data = userpass.getBytes();
		}

		return "Basic " + Base64Encoder.encode(data); //$NON-NLS-1$
	}

	/**
	 * @see Authenticator#getAuthenticationInfo(AuthenticateChallenge, Map, URL, URL)
	 */
	protected Map getAuthenticationInfo(AuthenticateChallenge challenge, Map oldInfo, URL serverUrl, URL protectionSpaceUrl) {
		Assert.isNotNull(challenge);
		Assert.isNotNull(serverUrl);
		Assert.isNotNull(protectionSpaceUrl);

		return authenticatorStore.requestAuthenticationInfo(protectionSpaceUrl, challenge.getRealm(), challenge.getAuthScheme());
	}

	/**
	 * @see Authenticator#getAuthorization(Request, Map, URL, URL, URL)
	 */
	protected String getAuthorization(Request request, Map info, URL serverUrl, URL protectionSpaceUrl, URL proxyServerUrl) {
		Assert.isNotNull(request);
		Assert.isNotNull(info);
		Assert.isNotNull(serverUrl);
		Assert.isNotNull(protectionSpaceUrl);

		String username = (String) info.get("username"); //$NON-NLS-1$
		String password = (String) info.get("password"); //$NON-NLS-1$

		if (username == null || password == null) {
			return null;
		}
		return credentials(username, password);
	}
}
