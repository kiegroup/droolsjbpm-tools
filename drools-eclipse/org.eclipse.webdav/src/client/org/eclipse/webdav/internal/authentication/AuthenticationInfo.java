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
 * Parses the HTTP "Authentication-Info" header field. The header field
 * has the following form:
 *
 * <code>
 * auth-info		= 1#(nextnonce | [ message-qop ]
 * 						| [ response-auth ] | [ cnonce ]
 * 						| [ nonce-count ] )
 * nextnonce		= "nextnonce" "=" nonce-value
 * nonce-value		= quoted-string
 * message-qop		= "qop" "=" qop-value
 * qop-value		= "auth" | "auth-int" | token
 * response-auth	= "rspauth" "=" response-digest
 * response-digest	= <"> *LHEX <">
 * cnonce			= "cnonce" "=" cnonce-value
 * cnonce-value		= nonce-value
 * nonce-count		= "nc" "=" nc-value
 * nc-value			= 8LHEX
 * </code>
 *
 * An auth-info may look like this:
 *
 * <code>
 * Authentication-Info:
 * nextnonce="dcd98b7102dd2f0e8b11d0f600bfb0c093",
 * qop="auth",
 * rspauth="5ccc069c403ebaf9f0171e9517f40e41",
 * cnonce="0a4f113b",
 * nc=00000001
 * </code>
 */
public class AuthenticationInfo {
	private String authInfo;
	private Parser parser;
	private Hashtable info;

	/**
	 * Parses the given authentication info.
	 *
	 * @param authInfo
	 * @throws ParserException if the info is malformed
	 */
	public AuthenticationInfo(String authInfo) throws ParserException {
		this.authInfo = authInfo;
		this.parser = new Parser(authInfo);
		parse();
	}

	/**
	 * Returns the value of the cnonce parameter, or <code>null</code> if
	 * the parameter does not exist.
	 */
	public String getCNonce() {
		return (String) info.get("cnonce"); //$NON-NLS-1$
	}

	/**
	 * Returns the value of the qop parameter, or <code>null</code> if
	 * the parameter does not exist.
	 */
	public String getMessageQop() {
		return (String) info.get("qop"); //$NON-NLS-1$
	}

	/**
	 * Returns the value of the nextnonce parameter, which must exist.
	 */
	public String getNextNonce() {
		return (String) info.get("nextnonce"); //$NON-NLS-1$
	}

	/**
	 * Returns the value of the nc parameter, or <code>null</code> if
	 * the parameter does not exist.
	 */
	public String getNonceCount() {
		return (String) info.get("nc"); //$NON-NLS-1$
	}

	/**
	 * Returns the value of the rspauth parameter, or <code>null</code> if
	 * the parameter does not exist.
	 */
	public String getResponseAuth() {
		return (String) info.get("rspauth"); //$NON-NLS-1$
	}

	private void parse() throws ParserException {
		info = new Hashtable(5);

		boolean done = false;
		while (!done) {
			String param = parser.nextToken();
			parser.match('=');
			String value = null;
			parser.checkPosition();
			if (authInfo.charAt(parser.pos) == '"') { //$NON-NLS-1$
				value = parser.nextQuotedString();
			} else {
				value = parser.nextToken();
			}
			info.put(param, value);
			parser.skipWhiteSpace();
			done = parser.pos == authInfo.length();
			if (!done) {
				parser.match(',');
				parser.skipWhiteSpace();
			}
		}

		if (getNextNonce() == null) {
			throw new ParserException(Policy.bind("exception.missingNextnonce")); //$NON-NLS-1$
		}
	}
}
