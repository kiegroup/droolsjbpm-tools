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
package org.eclipse.webdav.internal.kernel;

import java.io.IOException;
import java.io.StreamTokenizer;
import org.eclipse.webdav.IResponse;

/** 
 * A StateToken is a ConditionFactor describing some state of a resource represented
 * as a URI. A typical example would be the WebDAV lock token.
 */
public class StateToken extends ConditionFactor {

	private String uri = null;

	// represents some state of a resource expressed as a URI

	/** 
	 * Construct a StateToken. Should never be called.
	 */
	private StateToken() {
		super();
	}

	/** 
	 * Construct a StateToken with the given URI.
	 *
	 * @param uri the URI of the state token
	 */
	public StateToken(String uri) {
		this.uri = uri;
	}

	/** 
	 * Create a StateToken by parsing the given If header as defined by
	 * section 9.4 in the WebDAV spec.
	 *
	 * @param tokenizer a StreamTokenizer on the contents of a WebDAV If header
	 * @return the parsed ConditionFactor (StateToken)
	 */
	public static ConditionFactor create(StreamTokenizer tokenizer) throws WebDAVException {
		StateToken stateToken = new StateToken();
		try {
			int token = tokenizer.ttype;
			if (token == '<')
				token = tokenizer.nextToken();
			else
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissing", String.valueOf(token), "<")); //$NON-NLS-1$ //$NON-NLS-2$

			if (token == StreamTokenizer.TT_WORD) {
				stateToken.setURI(tokenizer.sval);
				token = tokenizer.nextToken();
			} else
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissingURI", String.valueOf(token))); //$NON-NLS-1$

			if (token == '>')
				token = tokenizer.nextToken();
			else
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissing", String.valueOf(token), ">")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException exc) {
			// ignore or log?
		}
		return stateToken;
	}

	/**
	 * Compare with another StateToken.
	 *
	 * @param factor the state token to compare with
	 * @return true if this state token has the same URI as the factor
	 */
	public boolean equals(Object factor) {
		return factor != null && factor instanceof StateToken && getURI().equals(((StateToken) factor).getURI());
	}

	/**
	 * Get the URI of this StateToken. The URI represents some state of the
	 * resource in the containing Condition, for example, the lock token.
	 *
	 * @return the URI for this state token
	 */
	public String getURI() {
		return uri;
	}

	/**
	 * Set the URI of this StateToken. The URI represents some state of the
	 * resource in the containing Condition, for example, the lock token.
	 *
	 * @param value the URI for this state token
	 */
	public void setURI(String value) {
		uri = value;
	}

	/**
	 * Return a String representation of this StateToken as defined by the If
	 * header in section 9.4 of the WebDAV spec.
	 * 
	 * @return a string representation of this state token
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (not())
			buffer.append("Not "); //$NON-NLS-1$
		buffer.append('<');
		buffer.append(getURI());
		buffer.append('>');
		return buffer.toString();
	}
}
