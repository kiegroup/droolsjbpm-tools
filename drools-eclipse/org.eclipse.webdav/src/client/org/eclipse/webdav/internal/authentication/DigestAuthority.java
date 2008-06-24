/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Boris Pruessmann - Patch for bug 22374
 *******************************************************************************/
package org.eclipse.webdav.internal.authentication;

import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import org.eclipse.webdav.IResponse;
import org.eclipse.webdav.client.Policy;
import org.eclipse.webdav.http.client.*;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * The <code>DigestAuthority</code> provides the necessary behavior to
 * authorizes client <code>Request</codes>s for communication with HTTP
 * servers using the Digest authentication scheme.
 *
 * @see AuthorizationAuthority
 */
public class DigestAuthority extends AuthorizationAuthority {

	/**
	 * Creates a new authenticator that stores its authentication information
	 * in the given authentication store.
	 * <p>The <code>DigestAuthenticator</code> authenticates according to the
	 * "Digest" authentication scheme.
	 * <p>Instances of this class must not be created directly, instead, use
	 * an instance of the class <code>Authenticator</code> to authorize
	 * requests.
	 *
	 * @param authenticatorStore a store that holds authentication
	 * information
	 */
	public DigestAuthority(IAuthenticator authenticatorStore) {
		super(authenticatorStore);
	}

	/**
	 * @see Authenticator#confirmResponse(Request, Response, URL)
	 */
	protected boolean confirmResponse(Request request, IResponse response, URL proxyServerUrl) {
		Assert.isNotNull(request);
		Assert.isNotNull(response);

		String authInfoString = response.getContext().get("Authentication-Info"); //$NON-NLS-1$
		if (authInfoString == null) {
			return false;
		}

		AuthenticationInfo authInfo = null;
		try {
			authInfo = new AuthenticationInfo(authInfoString);
		} catch (ParserException e) {
			return false;
		}

		String nextNonce = authInfo.getNextNonce();
		String messageQop = authInfo.getMessageQop();
		String responseAuth = authInfo.getResponseAuth();
		String cnonce = authInfo.getCNonce();
		String nonceCount = authInfo.getNonceCount();

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

		Map info = authenticatorStore.getAuthenticationInfo(serverUrl, realm, "Digest"); //$NON-NLS-1$
		if (info == null) {
			return false;
		}

		String username = (String) info.get("username"); //$NON-NLS-1$
		String password = (String) info.get("password"); //$NON-NLS-1$
		String algorithm = (String) info.get("algorithm"); //$NON-NLS-1$
		String nonce = (String) info.get("nonce"); //$NON-NLS-1$
		String iNonceCount = (String) info.get("nc"); //$NON-NLS-1$
		String iCnonce = (String) info.get("cnonce"); //$NON-NLS-1$

		if (username == null || password == null || nonce == null) {
			return false;
		}

		if (cnonce != null && !cnonce.equals(iCnonce)) {
			return false;
		}

		if (nonceCount != null && !nonceCount.equals(iNonceCount)) {
			return false;
		}

		if (responseAuth != null) {
			try {
				String digestUri = resourceUrl.toString();
				if (proxyServerUrl == null) {
					digestUri = resourceUrl.getFile();
					String ref = resourceUrl.getRef();
					if (ref != null) {
						digestUri += "#" + ref; //$NON-NLS-1$
					}
				}
				String iResponseAuth = response(request, realm, username, password, algorithm, messageQop, nonce, nonceCount, cnonce, request.getMethod(), digestUri);
				if (!responseAuth.equals(iResponseAuth)) {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}

		info.put("nonce", nextNonce); //$NON-NLS-1$

		return true;
	}

	/**
	 * Returns the Digest authorization credentials for the given directives.
	 * The credentials have the following form:
	 * <code>
	 * credentials		= "Digest" digest-response
	 * digest-response	= 1#( username | realm | nonce | digest-uri
	 * 					| response | [ algorithm ] | [cnonce] |
	 * 					[opaque] | [message-qop] |
	 * 					[nonce-count]  | [auth-param] )
	 * username			= "username" "=" username-value
	 * username-value	= quoted-string
	 * realm			= "realm" "=" realm-value
	 * realm-value		= quoted-string
	 * nonce			= "nonce" "=" nonce-value
	 * nonce-value		= quoted-string
	 * digest-uri		= "uri" "=" digest-uri-value
	 * digest-uri-value	= request-uri   ; As specified by HTTP/1.1
	 * response			= "response" "=" request-digest
	 * request-digest	= &lt;"&gt; 32LHEX &lt;"&gt;
	 * LHEX				= "0" | "1" | "2" | "3" |
	 * 					  "4" | "5" | "6" | "7" |
	 * 					  "8" | "9" | "a" | "b" |
	 * 					  "c" | "d" | "e" | "f"
	 * algorithm		= "algorithm" "=" ("MD5" | "MD5-sess" | token)
	 * cnonce			= "cnonce" "=" cnonce-value
	 * cnonce-value		= nonce-value
	 * opaque			= "opaque" "=" quoted-string
	 * message-qop		= "qop" "=" qop-value
	 * nonce-count		= "nc" "=" nc-value
	 * nc-value			= 8LHEX
	 * </code>
	 * <P>If the "qop" value is "auth" or "auth-int":
	 * <code>
	 * request-digest	= &lt;"&gt; &lt; KD ( H(A1),	    unq(nonce-value)
	 * 													":" nc-value
	 * 													":" unq(cnonce-value)
	 * 													":" unq(qop-value)
	 * 													":" H(A2)
	 * 										) &lt;"&gt;
	 * KD(secret, data)	= H(concat(secret, ":", data))
	 * H(data)			= MD5(data)
	 * unq(data)		= unqouted(data)
	 * </code>
	 * <P>If the "qop" directive is not present:
	 * <code>
	 * request-digest	= &lt;"&gt; &lt; KD ( H(A1),
	 * 													    unq(nonce-value)
	 * 													":" H(A2)
	 * 										) &lt;"&gt;
	 * </code>
	 * <P>If the "algorithm" directive's value is "MD5" or is unspecified,
	 * then A1 is:
	 * <code>
	 * A1				= unq(username-value) ":" unq(realm-value) ":" passwd
	 * passwd			= &lt; user's password &gt;
	 * </code>
	 * <P>If the "algorithm" directive's value is "MD5-sess", then A1 is:
	 * <code>
	 * A1				= H( unq(username-value) ":" unq(realm-value)
	 * 							":" passwd )
	 * 							":" unq(nonce-value) ":" unq(cnonce-value)
	 * </code>
	 * <P>If the "qop" directive's value is "auth" or is unspecified, then
	 * A2 is:
	 * <code>
	 * A2				= Method ":" digest-uri-value
	 * </code>
	 * <P>If the "qop" value is "auth-int", then A2 is:
	 * <code>
	 * A2				= Method ":" digest-uri-value ":" H(entity-body)
	 * </code>
	 * @param request
	 * @param realm
	 * @param username
	 * @param password
	 * @param algorithm
	 * @param messageQop
	 * @param nonce
	 * @param nonceCount
	 * @param opaque
	 * @param cnonce
	 * @param method
	 * @param digestUri
	 * @return           the Digest authorization credentials for the given
	 *                   directives
	 */
	private String credentials(Request request, String realm, String username, String password, String algorithm, String messageQop, String nonce, String nonceCount, String opaque, String cnonce, String method, String digestUri) throws Exception {
		Assert.isNotNull(request);
		Assert.isNotNull(realm);
		Assert.isNotNull(username);
		Assert.isNotNull(password);
		Assert.isNotNull(nonce);
		Assert.isNotNull(method);
		Assert.isNotNull(digestUri);

		StringBuffer buf = new StringBuffer();

		buf.append("Digest username=\""); //$NON-NLS-1$
		buf.append(username);
		buf.append("\""); //$NON-NLS-1$

		buf.append(", realm="); //$NON-NLS-1$
		buf.append(realm);

		if (messageQop != null) {
			buf.append(", qop=\""); //$NON-NLS-1$
			buf.append(messageQop);
			buf.append("\""); //$NON-NLS-1$
		}

		if (algorithm != null) {
			buf.append(", algorithm="); //$NON-NLS-1$
			buf.append(algorithm);
		}

		buf.append(", uri=\""); //$NON-NLS-1$
		buf.append(digestUri);
		buf.append("\""); //$NON-NLS-1$

		buf.append(", nonce="); //$NON-NLS-1$
		buf.append(nonce);

		if (nonceCount != null) {
			buf.append(", nc="); //$NON-NLS-1$
			buf.append(nonceCount);
		}

		if (cnonce != null) {
			buf.append(", cnonce=\""); //$NON-NLS-1$
			buf.append(cnonce);
			buf.append("\""); //$NON-NLS-1$
		}

		if (opaque != null) {
			buf.append(", opaque="); //$NON-NLS-1$
			buf.append(opaque);
		}

		String response = response(request, realm, username, password, algorithm, messageQop, nonce, nonceCount, cnonce, method, digestUri);
		if (response == null) {
			return null;
		}

		buf.append(", response=\""); //$NON-NLS-1$
		buf.append(response);
		buf.append("\""); //$NON-NLS-1$

		return buf.toString();
	}

	/**
	 * @see Authenticator#getAuthenticationInfo(AuthenticateChallenge, Map, URL, URL)
	 */
	protected Map getAuthenticationInfo(AuthenticateChallenge challenge, Map oldInfo, URL serverUrl, URL protectionSpaceUrl) {
		Assert.isNotNull(challenge);
		Assert.isNotNull(serverUrl);
		Assert.isNotNull(protectionSpaceUrl);

		Hashtable info = new Hashtable(5);

		String stale = challenge.get("stale"); //$NON-NLS-1$
		if (oldInfo == null || stale == null || !Boolean.valueOf(stale).booleanValue()) {
			Map userpass = authenticatorStore.requestAuthenticationInfo(protectionSpaceUrl, challenge.getRealm(), challenge.getAuthScheme());
			if (userpass == null) {
				return null;
			}
			info.put("username", userpass.get("username")); //$NON-NLS-1$ //$NON-NLS-2$
			info.put("password", userpass.get("password")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			info.put("username", oldInfo.get("username")); //$NON-NLS-1$ //$NON-NLS-2$
			info.put("password", oldInfo.get("password")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		String realm = challenge.getRealm();
		String domain = challenge.get("domain"); //$NON-NLS-1$

		boolean addRoot = true;
		if (domain != null && domain.charAt(0) == '"' //$NON-NLS-1$
				&& domain.charAt(domain.length() - 1) == '"') { //$NON-NLS-1$
			int start = 1;
			boolean inSpace = false;
			for (int i = 1; i < domain.length(); ++i) {
				if (Character.isWhitespace(domain.charAt(i)) || i == domain.length() - 1) {
					if (!inSpace) {
						inSpace = true;
						String urlString = domain.substring(start, i);
						URL url = null;
						try {
							url = new URL(urlString);
						} catch (MalformedURLException e1) {
							try {
								url = new URL(serverUrl, urlString);
							} catch (MalformedURLException e2) {
								// ignore or log?
							}
						}
						if (url != null) {
							authenticatorStore.addProtectionSpace(url, realm);
							addRoot = false;
						}
					}
				} else {
					if (inSpace) {
						inSpace = false;
						start = i;
					}
				}
			}
		}

		if (addRoot) {
			authenticatorStore.addProtectionSpace(serverUrl, realm);
		}

		String nonce = challenge.get("nonce"); //$NON-NLS-1$
		if (nonce == null) {
			return null;
		}
		info.put("nonce", nonce); //$NON-NLS-1$

		String opaque = challenge.get("opaque"); //$NON-NLS-1$
		if (opaque != null) {
			info.put("opaque", opaque); //$NON-NLS-1$
		}

		String algorithm = challenge.get("algorithm"); //$NON-NLS-1$
		if (algorithm != null) {
			info.put("algorithm", algorithm); //$NON-NLS-1$
		}

		String qop = challenge.get("qop"); //$NON-NLS-1$
		if (qop != null && qop.charAt(0) == '"' //$NON-NLS-1$
				&& qop.charAt(qop.length() - 1) == '"') { //$NON-NLS-1$
			boolean foundAuth = false;
			boolean foundAuthInt = false;

			try {
				String token = null;
				boolean first = true;
				Parser parser = new Parser(qop.substring(1, qop.length() - 1));
				while (parser.pos < parser.s.length()) {
					if (first) {
						parser.skipWhiteSpace();
						first = false;
					} else {
						parser.match(',');
						parser.skipWhiteSpace();
					}
					token = parser.nextToken();
					if (token.equalsIgnoreCase("auth")) { //$NON-NLS-1$
						foundAuth = true;
					} else if (token.equalsIgnoreCase("auth-int")) { //$NON-NLS-1$
						foundAuthInt = true;
					}
					parser.skipWhiteSpace();
				}
			} catch (ParserException e) {
				// ignore or log?
			}

			if (foundAuthInt) {
				info.put("qop", "auth-int"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (foundAuth) {
				info.put("qop", "auth"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		return info;
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
		String algorithm = (String) info.get("algorithm"); //$NON-NLS-1$
		String messageQop = (String) info.get("qop"); //$NON-NLS-1$
		String nonce = (String) info.get("nonce"); //$NON-NLS-1$
		String nonceCount = (String) info.get("nc"); //$NON-NLS-1$
		String opaque = (String) info.get("opaque"); //$NON-NLS-1$
		String cnonce = null;

		if (username == null || password == null || nonce == null) {
			return null;
		}

		if (messageQop != null) {
			if (nonceCount == null) {
				nonceCount = "00000001"; //$NON-NLS-1$
			} else {
				int nc = Integer.parseInt(nonceCount, 16) + 1;
				nonceCount = HexConverter.toHex(new int[] {nc});
			}
			info.put("nc", nonceCount); //$NON-NLS-1$

			long milliseconds = new Date().getTime();
			SecureRandom random = new SecureRandom();
			random.setSeed(milliseconds);
			byte[] bytes = new byte[16];
			random.nextBytes(bytes);
			cnonce = HexConverter.toHex(bytes);
			info.put("cnonce", cnonce); //$NON-NLS-1$
		}

		String realm = authenticatorStore.getProtectionSpace(protectionSpaceUrl);
		if (realm == null) {
			return null;
		}

		String method = request.getMethod();
		URL resourceUrl = request.getResourceUrl();
		String digestUri = resourceUrl.toString();
		if (proxyServerUrl != null) {
			digestUri = resourceUrl.getFile();
			String ref = resourceUrl.getRef();
			if (ref != null) {
				digestUri += "#" + ref; //$NON-NLS-1$
			}
		}

		try {
			return credentials(request, realm, username, password, algorithm, messageQop, nonce, nonceCount, opaque, cnonce, method, digestUri);
		} catch (Exception e) {
			return null;
		}
	}

	private String ha1(String realm, String username, String password, String algorithm, String nonce, String cnonce) throws Exception {
		Assert.isNotNull(realm);
		Assert.isNotNull(username);
		Assert.isNotNull(password);
		Assert.isNotNull(nonce);

		String ha1 = md5(unquote(username) + ":" + unquote(realm) + ":" + password); //$NON-NLS-1$ //$NON-NLS-2$
		if (algorithm != null && !algorithm.equalsIgnoreCase("MD5")) { //$NON-NLS-1$
			if (algorithm.equalsIgnoreCase("MD5-sess")) { //$NON-NLS-1$
				if (cnonce == null) {
					return null;
				}
				ha1 = md5(ha1 + ":" + unquote(nonce) + ":" + unquote(cnonce)); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				throw new Exception(Policy.bind("exception.unregognizedAlgo", algorithm)); //$NON-NLS-1$
			}
		}

		return ha1;
	}

	private String ha2(Request request, String qop, String method, String digestUri) throws Exception {
		Assert.isNotNull(request);
		Assert.isNotNull(method);
		Assert.isNotNull(digestUri);

		String a2 = null;
		if (qop == null || qop.equalsIgnoreCase("auth")) { //$NON-NLS-1$
			a2 = md5((method == null ? "" : method) + ":" + digestUri); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (qop.equalsIgnoreCase("auth-int")) { //$NON-NLS-1$
			a2 = md5((method == null ? "" : method) + ":" + digestUri + md5(request)); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			throw new Exception(Policy.bind("exception.unregognizedQop", qop)); //$NON-NLS-1$
		}

		return a2;
	}

	private String response(Request request, String realm, String username, String password, String algorithm, String qop, String nonce, String nonceCount, String cnonce, String method, String digestUri) throws Exception {
		Assert.isNotNull(request);
		Assert.isNotNull(realm);
		Assert.isNotNull(username);
		Assert.isNotNull(password);
		Assert.isNotNull(nonce);
		Assert.isNotNull(method);
		Assert.isNotNull(digestUri);

		String ha1 = ha1(realm, username, password, algorithm, nonce, cnonce);
		if (ha1 == null) {
			return null;
		}

		String ha2 = ha2(request, qop, method, digestUri);
		if (ha2 == null) {
			return null;
		}

		if (qop == null) {
			return md5(ha1 + ":" + unquote(nonce) + ":" + ha2); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (nonceCount == null || cnonce == null || qop == null) {
			return null;
		}

		return md5(ha1 + ":" //$NON-NLS-1$
				+ unquote(nonce) + ":" //$NON-NLS-1$
				+ nonceCount + ":" //$NON-NLS-1$
				+ unquote(cnonce) + ":" //$NON-NLS-1$
				+ unquote(qop) + ":" //$NON-NLS-1$
				+ ha2);
	}
}
