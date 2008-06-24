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
 * An EntityTag is a ConditionFactor describing some state of a resource represented
 * as an opaque string. See section 3.11 of the HTTP/1.1 spec.
 */
public class EntityTag extends ConditionFactor {

	private static int bcnt = 0;
	private static String basetime = Long.toHexString(new java.util.Date().getTime());

	private String eTag = null;
	// represents some state of a resource expressed as a ETag
	private boolean weak = false;

	/**
	 * Construct a EntityTag. Should never be called.
	 */
	private EntityTag() {
		super();
	}

	/** 
	 * Construct a EntityTag with the given opaque string tag.
	 *
	 * @param tag the opaque string defining the entity tag
	 */
	public EntityTag(String tag) {
		this.eTag = tag;
	}

	/**
	 * Create an EntityTag by parsing the given If header as defined by
	 * section 3.11 of the HTTP/1.1 spec.
	 *
	 * @param tokenizer a StreamTokenizer on the contents of a WebDAV If header
	 * @return the parsed ConditionFactor (EntityTag)
	 * @exception WebDAVException thrown if there is a syntax error in the If header
	 */
	public static ConditionFactor create(StreamTokenizer tokenizer) throws WebDAVException {
		EntityTag entityTag = new EntityTag();
		try {
			int token = tokenizer.ttype;
			if (token == '[')
				token = tokenizer.nextToken();
			else
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissing", String.valueOf(token), "[")); //$NON-NLS-1$ //$NON-NLS-2$
			if (token == '"') { //$NON-NLS-1$
				entityTag.setETag(tokenizer.sval);
				token = tokenizer.nextToken();
			} else
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissingQuotedString", String.valueOf(token))); //$NON-NLS-1$
			if (token == ']')
				token = tokenizer.nextToken();
			else
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissing", String.valueOf(token), "]")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException exc) {
			// ignore or log?
		}
		return entityTag;
	}

	/**
	 * Compare with another EntityTag.
	 *
	 * @param etag the entity tag to compare
	 * @return true if the tags are equal, false otherwise
	 */
	public boolean equals(Object etag) {
		return etag != null && etag instanceof EntityTag && getETag().equals(((EntityTag) etag).getETag());
	}

	/** 
	 * Construct a unique EntityTag. The tag is constructed by concatening the 
	 * current time with the current thread's hash code.
	 * 
	 * @return a unique entity tag that servers may use for any purpose
	 */
	public static EntityTag generateEntityTag() {
		String xx = basetime + ":" + Integer.toHexString(Thread.currentThread().hashCode()); //$NON-NLS-1$
		bcnt++;
		xx += ":" + bcnt; //$NON-NLS-1$
		return new EntityTag(xx);
	}

	/** 
	 * Get the ETag of this EntityTag. The ETag represents some state of the
	 * resource in the containing Condition.
	 *
	 * @return the etag
	 */
	public String getETag() {
		return eTag;
	}

	/** 
	 * Is this a weak EntityTag?
	 *
	 * @return true if this is a weak entity tag
	 */
	public boolean isWeak() {
		return weak;
	}

	/**
	 * Set the ETag of this EntityTag. The ETag represents some state of the
	 * resource in the containing Condition, for example, the lock token.
	 *
	 * @value the etag to set
	 */
	public void setETag(String value) {
		eTag = value;
	}

	/** 
	 * Set the strength of this EntityTag.
	 * value true indicates this is a weak entity tag
	 */
	public void setWeak(boolean value) {
		weak = value;
	}

	/** 
	 * Return a String representation of this EntityTag as defined by the If
	 * header in section 9.4 of the WebDAV spec.
	 *
	 * @return a string representation of this entity tag
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (not())
			buffer.append("Not "); //$NON-NLS-1$
		if (isWeak())
			buffer.append("W/"); //$NON-NLS-1$
		buffer.append("[\""); //$NON-NLS-1$
		buffer.append(getETag());
		buffer.append("\"]"); //$NON-NLS-1$
		return buffer.toString();
	}
}
