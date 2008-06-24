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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import org.eclipse.webdav.IContext;
import org.eclipse.webdav.IResponse;
import org.eclipse.webdav.http.client.IAuthenticator;
import org.eclipse.webdav.http.client.Request;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * The <code>AuthorizationAuthority</code> authorizes client
 * <code>Request</codes>s for communication with HTTP servers. Subclasses
 * provide the necessary behavior for different authentication schemes.
 */
public class AuthorizationAuthority {

	public static String[] authenticationSchemes = {"Digest", "Basic"}; //$NON-NLS-1$ //$NON-NLS-2$
	protected IAuthenticator authenticatorStore = null;

	/**
	 * Creates a new authenticator that stores its authentication information
	 * in the given authenticator store.
	 *
	 * @param authenticatorStore a store that holds authentication
	 * information
	 */
	public AuthorizationAuthority(IAuthenticator authenticatorStore) {
		Assert.isNotNull(authenticatorStore);
		this.authenticatorStore = authenticatorStore;
	}

	/**
	 * Authorizes the given request by setting its authorization credentials
	 * in the given context. If the given response is not <code>null</code>,
	 * it is assumed to contain an authenticate challenge that is used to
	 * derive the authorization credentials. Returns true if the authorization
	 * succeeds, and false otherwise.
	 *
	 * @param request the request to authorize
	 * @param response the response containing the authenticate challenge
	 * @param context the context where the authorization credentials are set
	 * @param proxyServerUrl the URL of the proxy server, or <code>null</code>
	 * if there is no proxy server
	 * @param isProxyAuthorization a boolean indicating whether the
	 * authorization credentials should be computed for the proxy server or
	 * the origin server
	 * @return a boolean indicating whether the request was successfully
	 * authorized
	 */
	public boolean authorize(Request request, IResponse response, IContext context, URL proxyServerUrl, boolean isProxyAuthorization) {
		Assert.isNotNull(request);
		Assert.isNotNull(context);

		URL serverUrl = null;
		URL protectionSpaceUrl = null;

		if (isProxyAuthorization) {
			if (proxyServerUrl == null) {
				return false;
			}
			serverUrl = proxyServerUrl;
			protectionSpaceUrl = proxyServerUrl;
		} else {
			URL resourceUrl = request.getResourceUrl();
			try {
				serverUrl = new URL(resourceUrl.getProtocol(), resourceUrl.getHost(), resourceUrl.getPort(), "/"); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				return false;
			}
			protectionSpaceUrl = resourceUrl;
		}

		if (response != null) {
			String challengeString = null;
			if (isProxyAuthorization) {
				challengeString = response.getContext().getProxyAuthenticate();
			} else {
				challengeString = response.getContext().getWWWAuthenticate();
			}

			if (challengeString == null) {
				return false;
			}

			AuthenticateChallenge challenge = null;
			try {
				challenge = new AuthenticateChallenge(challengeString);
			} catch (ParserException e) {
				return false;
			}

			String authScheme = challenge.getAuthScheme();
			String realm = challenge.getRealm();

			AuthorizationAuthority authority = getAuthorizationAuthority(authScheme);
			if (authority == null) {
				return false;
			}

			Map oldInfo = authenticatorStore.getAuthenticationInfo(serverUrl, realm, authScheme);
			Map info = authority.getAuthenticationInfo(challenge, oldInfo, serverUrl, protectionSpaceUrl);
			if (info == null) {
				return false;
			}

			authenticatorStore.addAuthenticationInfo(serverUrl, realm, authScheme, info);
			authenticatorStore.addProtectionSpace(protectionSpaceUrl, realm);
		}

		String realm = authenticatorStore.getProtectionSpace(protectionSpaceUrl);
		if (realm == null) {
			return false;
		}

		Map info = null;
		String authScheme = null;

		for (int i = 0; i < authenticationSchemes.length; ++i) {
			authScheme = authenticationSchemes[i];
			info = authenticatorStore.getAuthenticationInfo(serverUrl, realm, authScheme);
			if (info != null) {
				break;
			}
		}

		if (info == null) {
			return false;
		}

		AuthorizationAuthority authority = getAuthorizationAuthority(authScheme);
		if (authority == null) {
			return false;
		}

		String authorization = authority.getAuthorization(request, info, serverUrl, protectionSpaceUrl, proxyServerUrl);
		if (authorization == null) {
			return false;
		}

		if (isProxyAuthorization) {
			if (authorization.equals(context.getProxyAuthorization()))
				return false; // we already had that auth so it must've failed
			context.setProxyAuthorization(authorization);
		} else {
			if (authorization.equals(context.getAuthorization()))
				return false; // we already had that auth so it must've failed
			context.setAuthorization(authorization);
		}

		return true;
	}

	/**
	 * Confirms whether the given response is valid by proving the server
	 * knows the client's authentication secret (password). Moreover, the
	 * server may wish to communicate some authentication information in the
	 * response for the purposes of authorizing future request.
	 *
	 * @param request the request that has already been sent
	 * @param response the response back from the server to be verified
	 * @param proxyServerUrl the URL of the proxy server, or <code>null</code>
	 * if there is none
	 * @returns a boolean indicating whether the given response is valid
	 */
	public boolean confirm(Request request, IResponse response, URL proxyServerUrl) {
		Assert.isNotNull(request);
		Assert.isNotNull(response);

		URL resourceUrl = request.getResourceUrl();

		URL serverUrl = null;
		try {
			serverUrl = new URL(resourceUrl.getProtocol(), resourceUrl.getHost(), resourceUrl.getPort(), "/"); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			return false;
		}

		String realm = authenticatorStore.getProtectionSpace(resourceUrl);
		if (realm == null) {
			return false;
		}

		Map info = null;
		String authScheme = null;

		for (int i = 0; i < authenticationSchemes.length; ++i) {
			authScheme = authenticationSchemes[i];
			info = authenticatorStore.getAuthenticationInfo(serverUrl, realm, authScheme);
			if (info != null) {
				break;
			}
		}

		if (info == null) {
			return false;
		}

		AuthorizationAuthority authority = getAuthorizationAuthority(authScheme);
		if (authority == null) {
			return false;
		}

		return authority.confirmResponse(request, response, proxyServerUrl);
	}

	/**
	 * Confirms whether the given response is valid by proving the server
	 * knows the client's authentication secret (password). Moreover, the
	 * server may wish to communicate some authentication information in the
	 * response for the purposes of authorizing future request.
	 * <p>This method should be overridden by schema specific authenticators.
	 *
	 * @param request the request that has already been sent
	 * @param response the response back from the server to be verified
	 * @param proxyServerUrl the URL of the proxy server, or <code>null</code>
	 * if there is none
	 * @returns a boolean indicating whether the given response is valid
	 */
	protected boolean confirmResponse(Request request, IResponse response, URL proxyServerUrl) {
		Assert.isNotNull(request);
		Assert.isNotNull(response);
		return false;
	}

	/**
	 * Returns the new authentication information gleaned from the given
	 * authenticate challenge and the given old authentication information.
	 * The old authentication information may be <code>null</code>.
	 * The authentication information usually contains directives such as
	 * usernames and passwords.
	 * <p>This method should be overridden by schema specific authenticators.
	 *
	 * @param challenge the authenticate challenge from the server
	 * @param oldInfo the old authentication information
	 * @param serverUrl the URL of the server
	 * @param protectionSpaceUrl the URL of the protected resource
	 * @return new authentication information
	 */
	protected Map getAuthenticationInfo(AuthenticateChallenge challenge, Map oldInfo, URL serverUrl, URL protectionSpaceUrl) {
		Assert.isNotNull(challenge);
		Assert.isNotNull(serverUrl);
		Assert.isNotNull(protectionSpaceUrl);
		return null;
	}

	/**
	 * Returns the authorization credentials for the given request. The
	 * authorization credentials are derived from the given authentication
	 * info. The authentication info may contain directives such as usernames
	 * and passwords.
	 * <p>This method should be overridden by schema specific authenticators.
	 *
	 * @param request the request being authorized
	 * @param info the authentication information used to derive the
	 * authorization credentials
	 * @param serverUrl the URL of the server
	 * @param protectionSpaceUrl the URL of the protected resource
	 * @param proxyServerUrl the URL of the proxy server, or <code>null</code>
	 * if there is none
	 * @return the authorization credentials for the given request
	 */
	protected String getAuthorization(Request request, Map info, URL serverUrl, URL protectionSpaceUrl, URL proxyServerUrl) {
		Assert.isNotNull(request);
		Assert.isNotNull(info);
		Assert.isNotNull(serverUrl);
		Assert.isNotNull(protectionSpaceUrl);
		return null;
	}

	/**
	 * Returns an authorization authority for the given authentication
	 * scheme, or <code>null</code> if there is no such authority.
	 *
	 * @param scheme an authentication scheme, for example: "Basic"
	 * @return an authorization authority for the given authentication scheme
	 */
	private AuthorizationAuthority getAuthorizationAuthority(String scheme) {
		try {
			scheme = Character.toUpperCase(scheme.charAt(0)) + scheme.substring(1).toLowerCase();
			String packageName = "org.eclipse.webdav.internal.authentication"; //$NON-NLS-1$
			String className = scheme + "Authority"; //$NON-NLS-1$
			Class clazz = Class.forName(packageName + "." + className); //$NON-NLS-1$
			Constructor constructor = clazz.getConstructor(new Class[] {IAuthenticator.class});
			return (AuthorizationAuthority) constructor.newInstance(new Object[] {authenticatorStore});
		} catch (ClassCastException e) {
			// ignore or log?
		} catch (Exception e) {
			// ignore or log?
		}
		return null;
	}

	/**
	 * Computes the MD5 hash value of the given <code>String</code> and
	 * returns the result as a HEX <code>String</code>.
	 *
	 * @param s
	 * @return a HEX <code>String</code> containing the MD5 hash value of the
	 * given <code>String</code>
	 * @exception NoSuchAlgorithmException
	 * @exception UnsupportedEncodingException
	 */
	protected String md5(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		byte[] hash = md5.digest(s.getBytes("UTF8")); //$NON-NLS-1$
		return HexConverter.toHex(hash);
	}

	/**
	 * Computes the MD5 hash value of the body of the given request and
	 * returns the result as a HEX <code>String</code>.
	 *
	 * @param request
	 * @return a HEX <code>String</code> containing the MD5 hash value of the
	 * body of the given request
	 * @exception NoSuchAlgorithmException
	 * @exception IOException
	 */
	protected String md5(Request request) throws NoSuchAlgorithmException, IOException {
		DigestOutputStream dos = new DigestOutputStream("MD5"); //$NON-NLS-1$
		request.write(dos);
		String result = HexConverter.toHex(dos.digest());
		dos.close();
		return result;
	}

	/**
	 * Returns the given <code>String</code> with its quotes removed.
	 *
	 * @param s a <code>String</code>
	 * @return the given <code>String</code> with its quotes removed
	 */
	protected String unquote(String s) {
		if (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') //$NON-NLS-1$ //$NON-NLS-2$
			return s.substring(1, s.length() - 1);
		return s;
	}
}
