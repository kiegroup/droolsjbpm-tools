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

import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.webdav.client.Policy;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * Parses an HTTP Content-Type entity-header field. See section 14.18 of
 * RFC2068 for more information on this field.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class ContentType {
	private String contentType;
	private int position;
	private boolean foundDelim;

	private String type;
	private String subtype;
	private Hashtable parameters = new Hashtable(5);

	/**
	 * Parses the given HTTP Content-Type entity-header field. For example,
	 * if the content type is "text/xml; charset="ISO-8859-4"" the type is
	 * "text", the subtype is "xml", and the value of the attribute "charset"
	 * is "ISO-8859-4".
	 *
	 * @param contentType the value of the content type field to parse
	 * @throws            IllegalArgumentException if the content type is
	 *                    malformed
	 */
	public ContentType(String contentType) throws IllegalArgumentException {
		Assert.isNotNull(contentType);
		this.contentType = contentType;
		position = 0;
		parse();
	}

	private void checkPosition() throws IllegalArgumentException {
		if (position >= contentType.length())
			illegalArgument();
	}

	/**
	 * Returns an <code>Enumeration</code> of this content type's attributes.
	 * For example, if the content type is "text/xml; charset="ISO-8859-4"",
	 * it has one attribute namely, "charset".
	 *
	 * @return an <code>Enumeration</code> of <code>String</code>s
	 */
	public Enumeration getAttributes() {
		return parameters.keys();
	}

	/**
	 * Returns this content type's subtype. For example, if the content type
	 * is "text/xml; charset="ISO-8859-4"", the the subtype is "xml".
	 *
	 * @return this content type's subtype
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * Returns this content type's type. For example, if the content type is
	 * "text/xml; charset="ISO-8859-4"", the the type is "text".
	 *
	 * @return this content type's type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the value of the given attribute for this content type. For
	 * example, if the content type is "text/xml; charset="ISO-8859-4"", then
	 * the value for the attribute "charset" is "IS-8859-4".
	 *
	 * @return the value of the given attribute for this content type
	 */
	public String getValue(String attribute) {
		return (String) parameters.get(attribute);
	}

	private void illegalArgument() throws IllegalArgumentException {
		throw new IllegalArgumentException(Policy.bind("exception.malformedContentType", contentType)); //$NON-NLS-1$
	}

	private String nextToken(char delim, boolean trim) throws IllegalArgumentException {
		int start = position;
		int end = start;

		boolean done = false;
		boolean trimming = false;

		while (!done) {
			if (position == contentType.length()) {
				done = true;
				foundDelim = false;
			} else {
				char c = contentType.charAt(position);
				if (Character.isWhitespace(c)) {
					if (trim) {
						trimming = true;
					} else {
						illegalArgument();
					}
				} else {
					if (c == delim) {
						done = true;
						foundDelim = true;
					} else {
						if (trimming) {
							illegalArgument();
						}
						end = position + 1;
					}
				}
				++position;
			}
		}

		return contentType.substring(start, end);
	}

	private void parse() throws IllegalArgumentException {
		checkPosition();
		skipWhiteSpace();
		checkPosition();
		type = nextToken('/', false);
		checkPosition();
		subtype = nextToken(';', true);
		skipWhiteSpace();
		if (foundDelim) {
			checkPosition();
		}

		while (position < contentType.length()) {
			String attribute = nextToken('=', false);
			checkPosition();
			String value = nextToken(';', true);
			if (value.startsWith("\"") && value.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
				value = value.substring(1, value.length() - 1);
			}
			skipWhiteSpace();
			if (foundDelim) {
				checkPosition();
			}
			parameters.put(attribute, value);
		}
	}

	private void skipWhiteSpace() {
		while (position < contentType.length() && Character.isWhitespace(contentType.charAt(position))) {
			++position;
		}
	}
}
