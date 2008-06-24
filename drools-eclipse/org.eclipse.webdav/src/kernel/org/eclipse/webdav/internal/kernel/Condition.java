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

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import org.eclipse.webdav.IResponse;

/** 
 * A Condition represents some state configuration of a particular resource that must be
 * satisfied in order for the associated request to be valid. At least one of
 * the ConditionTerms in a Condition must match with states of the resource, i.e., 
 * they are OR'd together. Conditions are contained in a Precondition which is used in a
 * WebDAV If header.
 */
public class Condition {
	private String uri = null;
	private Vector conditionTerms = new Vector();

	/** 
	 * Construct a Condition on the default resource.
	 */
	public Condition() {
		super();
	}

	/** 
	 * Construct a Condition with the given URI.
	 * 
	 * @param uri the URI of the resource associated with this condition
	 */
	public Condition(String uri) {
		this.uri = uri;
	}

	/** 
	 * Add a ConditionTerm to a Condition.
	 * 
	 * @param term the term to add
	 */
	public void addConditionTerm(ConditionTerm term) throws WebDAVException {
		conditionTerms.addElement(term);
	}

	/** 
	 * Does this Condition contain the given ConditionTerm?
	 *
	 * @param term the term to check for
	 * @return true if the condition contains the given term, false otherwise
	 */
	public boolean contains(ConditionTerm term) {
		// iterate through the factors looking for a match
		boolean match = false;
		Enumeration terms = getConditionTerms();
		while (!match && terms.hasMoreElements()) {
			ConditionTerm t = (ConditionTerm) terms.nextElement();
			match = term.matches(t);
		}
		return match;
	}

	/** 
	 * Create a Condition by parsing the given If header as defined by
	 * section 9.4 in the WebDAV spec.
	 *
	 * @param tokenizer a StreamTokenizer on the contents of a WebDAV If header
	 * @return the parsed condition
	 */
	public static Condition create(StreamTokenizer tokenizer) throws WebDAVException {
		Condition condition = new Condition();
		try {
			int token = tokenizer.ttype;
			if (token == '<') {
				token = tokenizer.nextToken();
				if (token == StreamTokenizer.TT_WORD) {
					condition.setResourceURI(tokenizer.sval);
				} else {
					throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissingResource")); //$NON-NLS-1$
				}
				token = tokenizer.nextToken();
				if (token == '>') {
					token = tokenizer.nextToken();
				} else {
					throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissing", String.valueOf(token), ">")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (token == '(') {
				while (token == '(') {
					condition.addConditionTerm(ConditionTerm.create(tokenizer));
					token = tokenizer.ttype;
				}
			} else {
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissingStart", String.valueOf(token))); //$NON-NLS-1$
			}
		} catch (IOException exc) {
			// ignore or log?
		}
		return condition;
	}

	/** 
	 * Create a Condition by parsing the given If header as defined by
	 * section 9.4 in the WebDAV spec.
	 *
	 * @param ifHeader the contents of a WebDAV If header
	 * @return the parsed condition
	 * @exception WebDAVException thrown if there is a syntax error in the header
	 */
	public static Condition create(String ifHeader) throws WebDAVException {
		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(ifHeader));
		// URI characters
		tokenizer.wordChars('!', '/');
		tokenizer.wordChars(':', '@');
		tokenizer.ordinaryChar('(');
		tokenizer.ordinaryChar(')');
		tokenizer.ordinaryChar('<');
		tokenizer.ordinaryChar('>');
		tokenizer.ordinaryChar('[');
		tokenizer.ordinaryChar(']');
		tokenizer.quoteChar('"');
		Condition condition = null;
		try {
			int token = tokenizer.nextToken();
			condition = Condition.create(tokenizer);
			token = tokenizer.ttype;
			if (token != StreamTokenizer.TT_EOF) {
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissing", String.valueOf(token), "EOF")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (IOException exc) {
			// ignore or log?
		}
		return condition;
	}

	/**
	 * Get all the ConditionTerms for this Condition. At least one of the ConditionTerms in
	 * a Condition must match with states of the resource, i.e., they are OR'd
	 * together. Conditions are contained in a Precondition which is used in a
	 * WebDAV If header.
	 */
	public Enumeration getConditionTerms() {
		return conditionTerms.elements();
	}

	/** 
	 * Get the URI of the associated Resource. The condition must match on this 
	 * resource. This is useful for Preconditions that span multiple resources.
	 *
	 * @return the resource URI whose state is described by this Condition, may be null
	 *    indicating the condition applies to the resource receiving the request
	 */
	public String getResourceURI() {
		return uri;
	}

	/** 
	 * See if this Condition matches the given Condition. This is an
	 * OR operation.
	 * 
	 * @param condition the condition to match against
	 * @return true if the conditions match, false otherwise.
	 */
	public boolean matches(Condition condition) {
		//// check the Resource if one was given
		//boolean match = true;
		//if (uri != null) {
		////try {
		////URL url1 = new URL(uri);
		////URL url2 = new URL(condition.getResourceURI());
		////match = match && url1.getProtocol().equals(url2.getProtocol());
		////match = match && url1.getHost().equals(url2.getHost());
		////int port1 = url1.getPort();
		////if (port1 == -1) { // use the default port
		////port1 = 80;
		////}
		////int port2 = url2.getPort();
		////if (port2 == -1) {
		////port2 = 80;
		////}
		////match = match && (port1 == port2);
		////match = match && url1.getFile().equals(url2.getFile());
		////} catch (Exception exc) {
		////match = false;
		////}
		//// added to fix bug. should not compare full uris, only the path
		////			match = uri.equals(condition.getResourceURI());
		//match = (new URI(uri)).getPath().removeTrailingSeparator().equals(
		//(new URI(condition.getResourceURI())).getPath().removeTrailingSeparator());
		//}
		//if (!match) {
		//return false;
		//}
		//// is each term in the condition in the given condition
		//match = false;
		//Enumeration terms = getConditionTerms();
		//while (!match && terms.hasMoreElements()) {
		//ConditionTerm term = (ConditionTerm) terms.nextElement();
		//match = condition.contains(term);
		//}
		//return match;
		return false;
	}

	/** 
	 * Set the URI of the associated Resource. The condition must match on this 
	 * resource. This is useful for Preconditions that span multiple resources.
	 * 
	 * @param value the resource URI whose state is described by this Condition.
	 *    value can be null if the condition applies to the resource executing 
	 *    the method.
	 */
	public void setResourceURI(String value) {
		uri = value;
	}

	/**
	 * Return a String representation of this Condition as defined by section 9.4
	 * of the WebDAV Spec.
	 * 
	 * @return a String representation of this condition
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (getResourceURI() != null) {
			buffer.append('<');
			buffer.append(getResourceURI());
			buffer.append("> "); //$NON-NLS-1$
		}
		Enumeration terms = getConditionTerms();
		while (terms.hasMoreElements()) {
			ConditionTerm term = (ConditionTerm) terms.nextElement();
			buffer.append(term.toString());
			if (terms.hasMoreElements())
				buffer.append(' ');
		}
		return buffer.toString();
	}
}
