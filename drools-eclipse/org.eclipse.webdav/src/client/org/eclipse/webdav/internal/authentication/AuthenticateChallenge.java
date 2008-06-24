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

import java.util.Hashtable;
import org.eclipse.webdav.client.Policy;

/**
 * Parses the HTTP "WWW-Authenticate" and "Proxy-Authenticate" header
 * fields. These header fields have the following form:
 *
 * <code>
 * challenge	= auth-scheme 1*SP 1#auth-param
 * auth-scheme	= token
 * auth-param	= token "=" ( token | quoted-string )
 * </code>
 *
 * The authentication parameter realm is defined for all authentication
 * schemes:
 *
 * <code>
 * realm		= "realm" "=" realm-value
 * realm-value	= quoted-string
 * </code>
 *
 * A challenge may look like this:
 *
 * <code>
 * WWW-Authenticate: Digest
 * realm="testrealm@host.com",
 * qop="auth,auth-int",
 * nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093",
 * opaque="5ccc069c403ebaf9f0171e9517f40e41"
 * </code>
 */
public class AuthenticateChallenge {
	private String challenge;
	private Parser parser;

	private String authScheme;
	private Hashtable authParams;

	/**
	 * Parses the given authenticate challenge.
	 *
	 * @param challenge
	 * @throws ParserException if the challenge is malformed
	 */
	public AuthenticateChallenge(String challenge) throws ParserException {
		this.challenge = challenge;
		this.parser = new Parser(challenge);
		parse();
	}

	/**
	 * Returns the value of the given authentication parameter, or
	 * <code>null</code> if the param does not exist.
	 */
	public String get(String param) {
		return (String) authParams.get(param);
	}

	/**
	 * Returns the authentication scheme. For example, "Basic" or "Digest".
	 */
	public String getAuthScheme() {
		return authScheme;
	}

	/**
	 * Returns the authenticate challenge, unparsed.
	 */
	public String getChallenge() {
		return challenge;
	}

	/**
	 * Returns the realm authentication parameter, which must exist.
	 */
	public String getRealm() {
		return get("realm"); //$NON-NLS-1$
	}

	private void parse() throws ParserException {
		authParams = new Hashtable(5);
		authScheme = parser.nextToken();
		parser.skipWhiteSpace();

		boolean done = false;
		while (!done) {
			String param = parser.nextToken();
			parser.match('=');
			String value = null;
			parser.checkPosition();
			if (challenge.charAt(parser.pos) == '"') { //$NON-NLS-1$
				value = parser.nextQuotedString();
			} else {
				value = parser.nextToken();
			}
			authParams.put(param, value);
			parser.skipWhiteSpace();
			done = parser.pos == challenge.length();
			if (!done) {
				parser.match(',');
				parser.skipWhiteSpace();
			}
		}

		if (getRealm() == null) {
			throw new ParserException(Policy.bind("exception.missingRealm")); //$NON-NLS-1$
		}
	}
}
