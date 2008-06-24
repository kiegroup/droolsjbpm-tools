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

import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.webdav.IContext;
import org.eclipse.webdav.internal.kernel.utils.EnumerationConverter;
import org.eclipse.webdav.internal.kernel.utils.MergedEnumeration;

/**
 * The <code>Context</code> class is essentially a collection of key-value
 * pairings, with defaults.  Implements the <code>Context</code> interface
 * which provides convenience methods for most HTTP and WebDAV message
 * header fields.
 *
 * To be done:
 *
 * 1) Types that take and return dates have not been changed.  We need to write
 * date formats to cope with the different strings that we may be sent, see
 *the HTTP spec for details.
 * 2) Identify the fields where multiple values are permissible, and change the
 *params to be arrays of values.
 *
 */
public class Context implements IContext {

	protected Hashtable properties = new Hashtable(5);
	protected IContext defaults = null;

	protected final class ContextKey {

		protected String key;

		public ContextKey(String key) {
			super();
			this.key = key;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof ContextKey))
				return false;
			return ((ContextKey) obj).key.equalsIgnoreCase(key);
		}

		public int hashCode() {
			return key.toLowerCase().hashCode();
		}

		public String toString() {
			return key;
		}
	}

	public final class ContextKeyToStringEnum extends EnumerationConverter {
		ContextKeyToStringEnum(Enumeration sourceEnum) {
			super(sourceEnum);
		}

		public Object nextElement() {
			return ((ContextKey) sourceEnum.nextElement()).key;
		}
	}

	// Field names used in HTTP message headers.

	// from HTTP/1.1
	public final String ACCEPT = "Accept"; //$NON-NLS-1$
	public final String ACCEPT_CHARSET = "Accept-Charset"; //$NON-NLS-1$
	public final String ACCEPT_ENCODING = "Accept-Encoding"; //$NON-NLS-1$
	public final String ACCEPT_LANGUAGE = "Accept-Language"; //$NON-NLS-1$
	public final String ACCEPT_RANGES = "Accept-Ranges"; //$NON-NLS-1$
	public final String AGE = "Age"; //$NON-NLS-1$
	public final String ALLOW = "Allow"; //$NON-NLS-1$
	public final String AUTHORIZATION = "Authorization"; //$NON-NLS-1$
	public final String CACHE_CONTROL = "Cache-Control"; //$NON-NLS-1$
	public final String CONNECTION = "Connection"; //$NON-NLS-1$
	public final String CONTENT_BASE = "Content-Base"; //$NON-NLS-1$
	public final String CONTENT_ENCODING = "Content-Encoding"; //$NON-NLS-1$
	public final String CONTENT_LANGUAGE = "Content-Language"; //$NON-NLS-1$
	public final String CONTENT_LENGTH = "Content-Length"; //$NON-NLS-1$
	public final String CONTENT_LOCATION = "Content-Location"; //$NON-NLS-1$
	public final String CONTENT_MD5 = "Content-MD5"; //$NON-NLS-1$
	public final String CONTENT_RANGE = "Content-Range"; //$NON-NLS-1$
	public final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$
	public final String DATE = "Date"; //$NON-NLS-1$
	public final String ETAG = "ETag"; //$NON-NLS-1$
	public final String EXPIRES = "Expires"; //$NON-NLS-1$
	public final String FROM = "From"; //$NON-NLS-1$
	public final String HOST = "Host"; //$NON-NLS-1$
	public final String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
	public final String IF_MATCH = "If-Match"; //$NON-NLS-1$
	public final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
	public final String IF_RANGE = "If-Range"; //$NON-NLS-1$
	public final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since"; //$NON-NLS-1$
	public final String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
	public final String LOCATION = "Location"; //$NON-NLS-1$
	public final String MAX_FORWARDS = "Max-Forwards"; //$NON-NLS-1$
	public final String PRAGMA = "Pragma"; //$NON-NLS-1$
	public final String PROXY_AUTHENTICATE = "Proxy-Authenticate"; //$NON-NLS-1$
	public final String PROXY_AUTHORIZATION = "Proxy-Authorization"; //$NON-NLS-1$
	public final String PUBLIC = "Public"; //$NON-NLS-1$
	public final String RANGE = "Range"; //$NON-NLS-1$
	public final String REFERER = "Referer"; //$NON-NLS-1$
	public final String RETRY_AFTER = "Retry-After"; //$NON-NLS-1$
	public final String SERVER = "Server"; //$NON-NLS-1$
	public final String TRANSFER_ENCODING = "Transfer-Encoding"; //$NON-NLS-1$
	public final String UPGRADE = "Upgrade"; //$NON-NLS-1$
	public final String USER_AGENT = "User-Agent"; //$NON-NLS-1$
	public final String VARY = "Vary"; //$NON-NLS-1$
	public final String VIA = "Via"; //$NON-NLS-1$
	public final String WARNING = "Warning"; //$NON-NLS-1$
	public final String WWW_AUTHENTICATE = "WWW-Authenticate"; //$NON-NLS-1$

	// from WebDAV
	public final String DAV = "DAV"; //$NON-NLS-1$
	public final String DEPTH = "Depth"; //$NON-NLS-1$
	public final String DESTINATION = "Destination"; //$NON-NLS-1$
	public final String IF = "If"; //$NON-NLS-1$
	public final String LOCK_TOKEN = "Lock-Token"; //$NON-NLS-1$
	public final String OVERWRITE = "Overwrite"; //$NON-NLS-1$
	public final String STATUS_URI = "Status-URI"; //$NON-NLS-1$
	public final String TIMEOUT = "Timeout"; //$NON-NLS-1$

	// from Advanced Collections
	public final String ALL_BINDINGS = "All-Bindings"; //$NON-NLS-1$
	public final String REF_TARGET = "Ref-Target"; //$NON-NLS-1$
	public final String RES_TYPE = "Resource-Type"; //$NON-NLS-1$
	public final String PASSTHROUGH = "Passthrough"; //$NON-NLS-1$
	public final String ORDERED = "Ordered"; //$NON-NLS-1$
	public final String POSITION = "Position"; //$NON-NLS-1$

	// from Delta-V
	public final String LABEL = "Label"; //$NON-NLS-1$

	public Context() {
		super();
	}

	/**
	 * Constructor for the class. Set the property defaults to be
	 * the given value.
	 *
	 * @param defaults the default property values for the context
	 */
	public Context(IContext defaults) {
		super();
		this.defaults = defaults;
	}

	/**
	 * Copy all the default values into the receiver.
	 */
	public void collapse() {
		if (defaults != null) {
			Enumeration keysEnum = defaults.keys();
			while (keysEnum.hasMoreElements()) {
				String key = (String) keysEnum.nextElement();
				put(key, get(key));
			}
			defaults = null;
		}
	}

	/**
	 * Return the value for the given key.
	 *
	 * @param key the key to look up
	 * @return the value for the key or null if none.
	 */
	public String get(String key) {
		String value = (String) properties.get(new ContextKey(key));
		if (value == null && defaults != null)
			return defaults.get(key);
		return value;
	}

	/**
	 * Gets the media types that are acceptable for a response.
	 * Return the string value for the ACCEPT key.
	 *
	 * <p>Implements the corresponding API in the interface 
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ACCEPT
	 * @see Context#getAccept()
	 */
	public String getAccept() {
		return get(ACCEPT);
	}

	/**
	 * Gets the character sets that are acceptable for a response.
	 * Return the string value for the ACCEPT_CHARSET key.
	 *
	 * <p>Implements the corresponding API in the interface 
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ACCEPT_CHARSET
	 * @see Context#getAcceptCharset()
	 */
	public String getAcceptCharset() {
		return get(ACCEPT_CHARSET);
	}

	/**
	 * Get what content-encoding values are acceptable for a response.
	 * Return the string value for the ACCEPT_ENCODING key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ACCEPT_ENCODING
	 * @see Context#getAcceptEncoding()
	 */
	public String getAcceptEncoding() {
		return get(ACCEPT_ENCODING);
	}

	/**
	 * Get the natural languages that are acceptable for a response.
	 * Return the string value for the ACCEPT_LANGUAGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ACCEPT_LANGUAGE
	 * @see Context#getAcceptLanguage()
	 */
	public String getAcceptLanguage() {
		return get(ACCEPT_LANGUAGE);
	}

	/**
	 * Get the range requests acceptable to a server.
	 * Return the string value for the ACCEPT_RANGES key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ACCEPT_RANGES
	 * @see Context#getAcceptRanges()
	 */
	public String getAcceptRanges() {
		return get(ACCEPT_RANGES);
	}

	/**
	 * Get the sender's estimate of the time since the response was generated.
	 * Return the int value for the AGE key. Return -1 if the value is not set.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for AGE
	 * @see Context#getAge()
	 */
	public int getAge() {
		String ageString = get(AGE);
		return (ageString == null) ? -1 : Integer.parseInt(ageString);
	}

	/**
	 * Return the string value for the ALL_BINDINGS key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ALL_BINDINGS
	 * @see Context#getAllBindings()
	 */
	public String getAllBindings() {
		return get(ALL_BINDINGS);
	}

	/**
	 * Gets the methods allowed on a resource
	 * Return the string value for the ALLOW key.
	 *
	 * <p>Implements the corresponding API in the interface 
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ALLOW
	 * @see Context#getAllow()
	 */
	public String getAllow() {
		return get(ALLOW);
	}

	/**
	 * Get the user's credentials for the realm of the resource being requested.
	 * Return the string value for the AUTHORIZATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for AUTHORIZATION
	 * @see Context#getAuthorization()
	 */
	public String getAuthorization() {
		return get(AUTHORIZATION);
	}

	/**
	 * Get the cache control directives that must be obeyed.
	 * Return the string value for the CACHE_CONTROL key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CACHE_CONTROL
	 * @see Context#getCacheControl()
	 */
	public String getCacheControl() {
		return get(CACHE_CONTROL);
	}

	/**
	 * Get sender connection options.
	 * Return the string value for the CONNECTION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONNECTION
	 * @see Context#getConnection()
	 */
	public String getConnection() {
		return get(CONNECTION);
	}

	/**
	 * Return the String value for the CONTENT_BASE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONTENT_BASE
	 * @see Context#getContentBase()
	 */
	public String getContentBase() {
		return get(CONTENT_BASE);
	}

	/**
	 * Get what additional content encodings have been applied to the entity body.
	 * Return the string value for the CONTENT_ENCODING key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONTENT_ENCODING
	 * @see Context#getContentEncoding()
	 */
	public String getContentEncoding() {
		return get(CONTENT_ENCODING);
	}

	/**
	 * Gets the natural language of the intended audience for the entity body.
	 * Return the string value for the CONTENT_LANGUAGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONTENT_LANGUAGE
	 * @see Context#getContentLanguage()
	 */
	public String getContentLanguage() {
		return get(CONTENT_LANGUAGE);
	}

	/**
	 * Get the content length in bytes of the entity body.
	 * Return the value for the CONTENT_LENGTH key.
	 * Returns -1 if the Content-Length has not been set.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONTENT_LENGTH
	 * @see Context#getContentLength()
	 */
	public long getContentLength() {
		String lengthString = get(CONTENT_LENGTH);
		return (lengthString == null) ? -1 : Long.parseLong(lengthString);
	}

	/**
	 * Return the String value for the CONTENT_LOCATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONTENT_LOCATION
	 * @see Context#getContentLocation()
	 */
	public String getContentLocation() {
		return get(CONTENT_LOCATION);
	}

	/**
	 * Return the string value for the CONTENT_MD5 key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONTENT_MD5
	 * @see Context#getContentMD5()
	 */
	public String getContentMD5() {
		return get(CONTENT_MD5);
	}

	/**
	 * Return the string value for the CONTENT_RANGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONTENT_RANGE
	 * @see Context#getContentRange()
	 */
	public String getContentRange() {
		return get(CONTENT_RANGE);
	}

	/**
	 * Get the MIME type for the response contents. 
	 * Return the string value for the CONTENT_TYPE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for CONTENT_TYPE
	 * @see Context#getContentType()
	 */
	public String getContentType() {
		return get(CONTENT_TYPE);
	}

	/**
	 * The date the request was made.
	 * Return the string value for the DATE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for DATE
	 * @see Context#getDate()
	 */
	public String getDate() {
		// TBD parse and return as a Java date
		return get(DATE);
	}

	/**
	 * Get the DAV level supported by the server.
	 * Return the string value for the DAV key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for DAV
	 * @see Context#getDAV()
	 */
	public String getDAV() {
		return get(DAV);
	}

	/**
	 * Return the string value for the DEPTH key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for DEPTH
	 * @see Context#getDepth()
	 */
	public String getDepth() {
		return get(DEPTH);
	}

	/**
	 * Get the destination URI for a copy or move operation.
	 * Return the URI value for the DESTINATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for DESTINATION
	 * @see Context#getDestination()
	 */
	public String getDestination() {
		return get(DESTINATION);
	}

	/**
	 * Get the entity tag for the associated entity.
	 * Return the string value for the ETAG key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ETAG
	 * @see Context#getETag()
	 */
	public String getETag() {
		return get(ETAG);
	}

	/**
	 * Get the date/time after which the response should be considered stale.
	 * Return the string value for the EXPIRES key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for EXPIRES
	 * @see Context#getExpires()
	 */
	public String getExpires() {
		// TDB : Parse and return as a date
		return get(EXPIRES);
	}

	/**
	 * Return the string value for the FROM key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for FROM
	 * @see Context#getFrom()
	 */
	public String getFrom() {
		return get(FROM);
	}

	/**
	 * Get the Internet host and port of the resource being requested.
	 * Return the string value for the HOST key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for HOST
	 * @see Context#HOST
	 */
	public String getHost() {
		return get(HOST);
	}

	/**
	 * Return the string value for the IF key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for IF
	 * @see Context#getIfKey()
	 */
	public String getIfKey() {
		return get(IF);
	}

	/**
	 * Return the string value for the IF_MATCH key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for IF_MATCH
	 * @see Context#getIfMatch()
	 */
	public String getIfMatch() {
		return get(IF_MATCH);
	}

	/**
	 * Return the string value for the IF_MODIFIED_SINCE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for IF_MODIFIED_SINCE
	 * @see Context#getIfModifiedSince()
	 */
	public String getIfModifiedSince() {
		return get(IF_MODIFIED_SINCE);
	}

	/**
	 * Return the string value for the IF_NONE_MATCH key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for IF_NONE_MATCH
	 * @see Context#getIfNoneMatch()
	 */
	public String getIfNoneMatch() {
		return get(IF_NONE_MATCH);
	}

	/**
	 * Return the string value for the IF_RANGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for IF_RANGE
	 * @see Context#getIfRange()
	 */
	public String getIfRange() {
		return get(IF_RANGE);
	}

	/**
	 * Return the string value for the IF_UNMODIFIED_SINC key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for IF_UNMODIFIED_SINCE
	 * @see Context#getIfUnmodifiedSince()
	 */
	public String getIfUnmodifiedSince() {
		return get(IF_UNMODIFIED_SINCE);
	}

	/**
	 * Get the message label selector.
	 * Return the string value for the LABEL key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for LABEL
	 * @see Context#getLabel()
	 */
	public String getLabel() {
		return get(LABEL);
	}

	/**
	 * Get when the resource was last modified.
	 * Return the string value for the LAST_MODIFIED key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for LAST_MODIFIED
	 * @see Context#getLastModified()
	 */
	public String getLastModified() {
		// TDB: return as date
		return get(LAST_MODIFIED);
	}

	/**
	 * Get the redirect location.
	 * Return the URI value for the LOCATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for LOCATION
	 * @see Context#getLocation()
	 */
	public String getLocation() {
		return get(LOCATION);
	}

	/**
	 * Gets the lock token for the resource, or null if it is not set.
	 * Return the lock token value for the LOCK_TOKEN key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for LOCK_TOKEN
	 * @see Context#getLockToken()
	 */
	public String getLockToken() {
		return get(LOCK_TOKEN);
	}

	/**
	 * Return the integer value for the MAX_FORWARDS key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for MAX_FORWARDS
	 * @see Context#getMaxForwards()
	 */
	public int getMaxForwards() {
		String s = get(MAX_FORWARDS);
		return s == null ? -1 : Integer.parseInt(s);
	}

	/**
	 * Return the string value for the ORDERED key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ORDERED
	 * @see Context#getOrdered()
	 */
	public String getOrdered() {
		return get(ORDERED);
	}

	/**
	 * Get the flag that indicates if copy or move should overwrite
	 * an existing destination. Return the boolean value for the 
	 * OVERWRITE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for OVERWRITE
	 * @see Context#OVERWRITE
	 */
	public boolean getOverwrite() {
		String overwriteString = get(OVERWRITE);
		return overwriteString == null ? false : overwriteString.equalsIgnoreCase("T"); //$NON-NLS-1$
	}

	/**
	 * Return the boolean value for the PASSTHROUGH key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for PASSTHROUGH
	 * @see Context#getPassthrough()
	 */
	public boolean getPassthrough() {
		String s = get(PASSTHROUGH);
		return s == null ? false : s.equalsIgnoreCase("T"); //$NON-NLS-1$
	}

	/**
	 * Return the string value for the POSITION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for POSITION
	 * @see Context#getPosition()
	 */
	public String getPosition() {
		return get(POSITION);
	}

	/**
	 * Return the string value for the PRAGMA key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for PRAGMA
	 * @see Context#getPragma()
	 */
	public String getPragma() {
		return get(PRAGMA);
	}

	/**
	 * Get any precondition that must be true in order for method
	 * execution to be successful. A precondition corresponds to the
	 * WebDAV "If" header. 
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for PRECONDITION
	 * @see Context#getPrecondition()
	 */
	public String getPrecondition() {
		return get(IF);
	}

	/**
	 * Return the string value for the PROXY_AUTHENTICATE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for PROXY_AUTHENTICATE
	 * @see Context#getProxyAuthenticate()
	 */
	public String getProxyAuthenticate() {
		return get(PROXY_AUTHENTICATE);
	}

	/**
	 * Return the string value for the PROXY_AUTHORIZATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for PROXY_AUTHORIZATION
	 * @see Context#getProxyAuthorization()
	 */
	public String getProxyAuthorization() {
		return get(PROXY_AUTHORIZATION);
	}

	/**
	 * Return the string value for the PUBLIC_KEY key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for PUBLIC_KEY
	 * @see Context#getPublicKey()
	 */
	public String getPublicKey() {
		return get(PUBLIC);
	}

	/**
	 * Return the string value for the RANGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for RANGE
	 * @see Context#getRange()
	 */
	public String getRange() {
		return get(RANGE);
	}

	/**
	 * Get the URI of the resource from which the request was obtained.
	 * Return the String value for the REFERER key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for REFERER
	 * @see Context#getReferer()
	 */
	public String getReferer() {
		return get(REFERER);
	}

	/**
	 * Return the URI value for the REF_TARGET key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for REF_TARGET
	 * @see Context#getRefTarget()
	 */
	public String getRefTarget() {
		return get(REF_TARGET);
	}

	/**
	 * Return the string value for the RESOURCE_TYPE  key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for RESOURCE_TYPE
	 * @see Context#getResourceType()
	 */
	public String getResourceType() {
		return get(RES_TYPE);
	}

	/**
	 * Return the string value for the RETRY_AFTER key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for RETRY_AFTER
	 * @see Context#getRetryAfter()
	 */
	public String getRetryAfter() {
		return get(RETRY_AFTER);
	}

	/**
	 * Get information about the software used by the origin server
	 * to handle the request. Return the string value for the SERVER 
	 * key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for SERVER
	 * @see Context#getServer()
	 */
	public String getServer() {
		return get(SERVER);
	}

	/**
	 * Get the String of the resource whose method is in process.
	 * Return the URI value for the STATUS_URI key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for STATUS_URI
	 * @see Context#getStatusURI()
	 */
	public String getStatusURI() {
		return get(STATUS_URI);
	}

	/**
	 * Get the lock timeout value. The value -1 means that the 
	 * value was not set, the value -2 means that the value was "Infinity".
	 * Return the integer value for the TIMEOUT key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for TIMEOUT
	 * @see Context#getTimeout()
	 */
	public int getTimeout() {
		String timeoutString = get(TIMEOUT);
		if (timeoutString == null)
			return -1;
		if (timeoutString.equalsIgnoreCase(DEPTH_INFINITY))
			return -2;
		if (timeoutString.regionMatches(true, 1, "Second-", 1, 7)) //$NON-NLS-1$
			return Integer.parseInt(timeoutString.substring(7));
		// ignore all other cases, and use infinite timeout
		return -2;
	}

	/**
	 * Return the string value for the TRANSFER_ENCODING key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for TRANSFER_ENCODING
	 * @see Context#getTransferEncoding()
	 */
	public String getTransferEncoding() {
		return get(TRANSFER_ENCODING);
	}

	/**
	 * Return the string value for the UPGRADE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for UPGRADE
	 * @see Context#getUpgrade()
	 */
	public String getUpgrade() {
		return get(UPGRADE);
	}

	/**
	 * Get information about the user agent originating the request.
	 * Return the string value for the USER_AGENT key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for USER_AGENT
	 * @see Context#getUserAgent()
	 */
	public String getUserAgent() {
		return get(USER_AGENT);
	}

	/**
	 * Return the string value for the VARY key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for VARY
	 * @see Context#getVary()
	 */
	public String getVary() {
		return get(VARY);
	}

	/**
	 * Return the string value for the VIA key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for VIA
	 * @see Context#getVia()
	 */
	public String getVia() {
		return get(VIA);
	}

	/**
	 * Return the string value for the WARNING key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for WARNING
	 * @see Context#getWarning()
	 */
	public String getWarning() {
		return get(WARNING);
	}

	/**
	 * Return the string value for the WWW_AUTHENTICATE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for WWW_AUTHENTICATE
	 * @see Context#getWWWAuthenticate()
	 */
	public String getWWWAuthenticate() {
		return get(WWW_AUTHENTICATE);
	}

	/**
	 * Return an enumeration over the context's keys. (recursively computes the
	 * keys based on keys defaults as well)
	 *
	 * @return an enumeration over the context keys
	 */
	public Enumeration keys() {

		if (defaults == null)
			return new ContextKeyToStringEnum(properties.keys());

		Enumeration allKeys = new MergedEnumeration(new ContextKeyToStringEnum(properties.keys()), defaults.keys());

		Hashtable keysSet = new Hashtable();
		while (allKeys.hasMoreElements())
			keysSet.put(allKeys.nextElement(), "ignored"); //$NON-NLS-1$

		return keysSet.keys();
	}

	/**
	 * Put the given key-value pair into the context.
	 *
	 * @param key the key
	 * @param value its associated value
	 */
	public void put(String key, String value) {
		ContextKey ckey = new ContextKey(key);
		if ((value == null) || (value.length() == 0))
			properties.remove(ckey);
		else
			properties.put(ckey, value);
	}

	/**
	 * Remove the given key from the context
	 *
	 * @param key the key to remove
	 */
	public void removeKey(String key) {
		properties.remove(new ContextKey(key));
	}

	/**
	 * Set what media types are acceptable for a response.
	 * Set the string value for the ACCEPT key.
	 *
	 * <p>Implements the corresponding API in the interface 
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for ACCEPT
	 * @see Context#setAccept(String)
	 */
	public void setAccept(String value) {
		put(ACCEPT, value);
	}

	/**
	 * Sets which character sets are acceptable for a response.
	 * Set the string value for the ACCEPT_CHARSET key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for ACCEPT_CHARSET
	 * @see Context#setAcceptCharset(String)
	 */
	public void setAcceptCharset(String value) {
		put(ACCEPT_CHARSET, value);
	}

	/**
	 * Set the content-encoding values which are acceptable for a response.
	 * Set the string value for the ACCEPT_ENCODING key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for ACCEPT_ENCODING
	 * @see Context#setAcceptEncoding(String)
	 */
	public void setAcceptEncoding(String value) {
		put(ACCEPT_ENCODING, value);
	}

	/**
	 * Get the natural languages that are acceptable for a response.
	 * Return the string value for the ACCEPT_LANGUAGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @return the value for ACCEPT_LANGUAGE
	 * @see Context#setAcceptLanguage()
	 */
	public String setAcceptLanguage() {
		return get(ACCEPT_LANGUAGE);
	}

	/**
	 * Set which natural languages are acceptable for a response.
	 * Set the string value for the ACCEPT_LANGUAGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for ACCEPT_LANGUAGE
	 * @see Context#setAcceptLanguage(String)
	 */
	public void setAcceptLanguage(String value) {
		put(ACCEPT_LANGUAGE, value);
	}

	/**
	 * Set the range requests acceptable to a server.
	 * Set the string value for the ACCEPT_RANGES key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for ACCEPT_RANGES
	 * @see Context#setAcceptRanges(String)
	 */
	public void setAcceptRanges(String value) {
		put(ACCEPT_RANGES, value);
	}

	/**
	 * Set the sender's estimate of the time since the response was generated.
	 * Set the int value for the AGE key. Set the value to -1 to remove the key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param seconds the value for AGE
	 * @see Context#setAge(int)
	 */
	public void setAge(int seconds) {
		put(AGE, (seconds == -1) ? "" : Integer.toString(seconds)); //$NON-NLS-1$
	}

	/**
	 * Set the string value for the ALL_BINDINGS key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for ALL_BINDINGS
	 * @see Context#setAllBindings(String)
	 */
	public void setAllBindings(String s) {
		put(ALL_BINDINGS, s);
	}

	/**
	 * Sets methods allowed on a resource
	 * Set the string value for the ALLOW key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for ALLOW
	 * @see Context#setAllow(String)
	 */
	public void setAllow(String value) {
		put(ALLOW, value);
	}

	/**
	 * Set the user's credentials for the realm of the resource being requested.
	 * Set the string value for the AUTHORIZATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for AUTHORIZATION
	 * @see Context#setAuthorization(String)
	 */
	public void setAuthorization(String value) {
		put(AUTHORIZATION, value);
	}

	/**
	 * Set the cache control directives that must be obeyed.
	 * Set the string value for the CACHE_CONTROL key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CACHE_CONTROL
	 * @see Context#setCacheControl(String)
	 */
	public void setCacheControl(String value) {
		put(CACHE_CONTROL, value);
	}

	/**
	 * Set sender connection options.
	 * Set the string value for the CONNECTION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CONNECTION
	 * @see Context#setConnection(String)
	 */
	public void setConnection(String value) {
		put(CONNECTION, value);
	}

	/**
	 * Set the String URL value for the CONTENT_BASE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param url the value for CONTENT_BASE
	 * @see Context#setContentBase(String)
	 */
	public void setContentBase(String url) {
		put(CONTENT_BASE, url);
	}

	/**
	 * Sets the additional content encodings that have been applied to the entity body.
	 * Set the string value for the CONTENT_ENCODING key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CONTENT_ENCODING
	 * @see Context#setContentEncoding(String)
	 */
	public void setContentEncoding(String value) {
		put(CONTENT_ENCODING, value);
	}

	/**
	 * Sets the natural language of the intended audience for the entity body.
	 * Set the string value for the CONTENT_LANGUAGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CONTENT_LANGUAGE
	 * @see Context#setContentLanguage(String)
	 */
	public void setContentLanguage(String value) {
		put(CONTENT_LANGUAGE, value);
	}

	/**
	 * Set the content length in bytes of the entity body.
	 * Set the value for the CONTENT_LENGTH key.
	 * Pass the value -1 to remove the key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CONTENT_LENGTH
	 * @see Context#setContentLength(long)
	 */
	public void setContentLength(long value) {
		put(CONTENT_LENGTH, (value == -1) ? "" : Long.toString(value)); //$NON-NLS-1$
	}

	/**
	 * Set the String URI value for the CONTENT_LOCATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CONTENT_LOCATION
	 * @see Context#setContentLocation(String)
	 */
	public void setContentLocation(String value) {
		put(CONTENT_LOCATION, value);
	}

	/**
	 * Set the string value for the CONTENT_MD5 key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CONTENT_MD5
	 * @see Context#setContentMD5(String)
	 */
	public void setContentMD5(String value) {
		put(CONTENT_MD5, value);
	}

	/**
	 * Set the string value for the CONTENT_RANGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CONTENT_RANGE
	 * @see Context#setContentRange(String)
	 */
	public void setContentRange(String value) {
		put(CONTENT_RANGE, value);
	}

	/**
	 * Set the MIME type for the response contents. 
	 * Set the string value for the CONTENT_TYPE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for CONTENT_TYPE
	 * @see Context#setContentType(String)
	 */
	public void setContentType(String value) {
		put(CONTENT_TYPE, value);
	}

	/**
	 * Set the date the request was made.
	 * Set the string value for the DATE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for DATE
	 * @see Context#setDate(String)
	 */
	public void setDate(String value) {
		put(DATE, value);
	}

	/**
	 * Set the DAV level supported by the server.
	 * Set the string value for the DAV key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for DAV
	 * @see Context#setDAV(String)
	 */
	public void setDAV(String value) {
		put(DAV, value);
	}

	/**
	 * Set the string value for the DEPTH key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for DEPTH
	 * @see Context#setDepth(String)
	 */
	public void setDepth(String s) {
		put(DEPTH, s);
	}

	/**
	 * Set the destination URI for a copy or move operation.
	 * Set the URI value for the DESTINATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for DESTINATION
	 * @see Context#setDestination(String)
	 */
	public void setDestination(String value) {
		put(DESTINATION, value);
	}

	/**
	 * Set the entity tag for the associated entity.
	 * Set the string value for the ETAG key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for ETAG
	 * @see Context#setETag(String)
	 */
	public void setETag(String value) {
		put(ETAG, value);
	}

	/**
	 * Set the date/time after which the response should be considered stale.
	 * Set the string value for the EXPIRES key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for EXPIRES
	 * @see Context#setExpires(String)
	 */
	public void setExpires(String value) {
		// TDB accept a date
		put(EXPIRES, value);
	}

	/**
	 * Set the string value for the FROM key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for FROM
	 * @see Context#FROM
	 */
	public void setFrom(String s) {
		put(FROM, s);
	}

	/**
	 * Set the Internet host and port of the resource being requested.
	 * Set the string value for the HOST key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for HOST
	 * @see Context#setHost(String)
	 */
	public void setHost(String value) {
		put(HOST, value);
	}

	/**
	 * Set the string value for the IF key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for IF
	 * @see Context#setIfKey(String)
	 */
	public void setIfKey(String s) {
		put(IF, s);
	}

	/**
	 * Set the string value for the IF_MATCH key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for IF_MATCH
	 * @see Context#setIfMatch(String)
	 */
	public void setIfMatch(String s) {
		put(IF_MATCH, s);
	}

	/**
	 * Set the string value for the IF_MODIFIED_SINCE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for IF_MODIFIED_SINCE
	 * @see Context#setIfModifiedSince(String)
	 */
	public void setIfModifiedSince(String s) {
		put(IF_MODIFIED_SINCE, s);
	}

	/**
	 * Set the string value for the IF_NONE_MATCH key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for IF_NONE_MATCH
	 * @see Context#setIfNoneMatch(String)
	 */
	public void setIfNoneMatch(String s) {
		put(IF_NONE_MATCH, s);
	}

	/**
	 * Set the string value for the IF_RANGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for IF_RANGE
	 * @see Context#setIfRange(String)
	 */
	public void setIfRange(String s) {
		put(IF_RANGE, s);
	}

	/**
	 * Set the string value for the IF_UNMODIFIED_SINCE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for IF_UNMODIFIED_SINCE
	 * @see Context#setIfUnmodifiedSince(String)
	 */
	public void setIfUnmodifiedSince(String s) {
		put(IF_UNMODIFIED_SINCE, s);
	}

	/**
	 * Set the method's label selector.
	 * Set the string value for the LABEL key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for LABEL
	 * @see Context#setLabel(String)
	 */
	public void setLabel(String value) {
		put(LABEL, value);
	}

	/**
	 * Set when the resource was last modified.
	 * Set the string value for the LAST_MODIFIED key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for LAST_MODIFIED
	 * @see Context#setLastModified(String)
	 */
	public void setLastModified(String value) {
		// TDB set as a date
		put(LAST_MODIFIED, value);
	}

	/**
	 * Sets the redirect location.
	 * Set the URI value for the LOCATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for LOCATION
	 * @see Context#LOCATION
	 */
	public void setLocation(String value) {
		put(LOCATION, value);
	}

	/**
	 * Set the lock token for the resource.
	 * Set the lock token value for the LOCK_TOKEN key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param token the value for LOCK_TOKEN
	 * @see Context#setLockToken(String)
	 */
	public void setLockToken(String token) {
		put(LOCK_TOKEN, token);
	}

	/**
	 * Set the integer value for the MAX_FORWARDS key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param i the value for MAX_FORWARDS
	 * @see Context#setMaxForwards(int)
	 */
	public void setMaxForwards(int i) {
		put(MAX_FORWARDS, Integer.toString(i));
	}

	/**
	 * Set the string value for the ORDERED key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for ORDERED
	 * @see Context#setOrdered(String)
	 */
	public void setOrdered(String value) {
		put(ORDERED, value);
	}

	/**
	 * Set if copy or move should overwrite an existing destination.
	 * Set the boolean value for the OVERWRITE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for OVERWRITE
	 * @see Context#setOverwrite(boolean)
	 */
	public void setOverwrite(boolean value) {
		put(OVERWRITE, value ? "T" : "F"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Set the boolean value for the PASSTHROUGH key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for PASSTHROUGH
	 * @see Context#setPassthrough(boolean)
	 */
	public void setPassthrough(boolean value) {
		put(PASSTHROUGH, value ? "T" : "F"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Set the string value for the POSITION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param s the value for POSITION
	 * @see Context#setPosition(String)
	 */
	public void setPosition(String s) {
		put(POSITION, s);
	}

	/**
	 * Set the string value for the PRAGMA key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for PRAGMA
	 * @see Context#setPragma(String)
	 */
	public void setPragma(String value) {
		put(PRAGMA, value);
	}

	/**
	 * Set any precondition that must be true in order for method
	 * execution to be successful. A precondition corresponds to the
	 * WebDAV "If" header. 
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for the precondition
	 * @see Context#setPrecondition(String)
	 */
	public void setPrecondition(String value) {
		put(IF, value);
	}

	/**
	 * Set the string value for the PROXY_AUTHENTICATE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for PROXY_AUTHENTICATE
	 * @see Context#setProxyAuthenticate(String)
	 */
	public void setProxyAuthenticate(String value) {
		put(PROXY_AUTHENTICATE, value);
	}

	/**
	 * Set the string value for the PROXY_AUTHORIZATION key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for PROXY_AUTHORIZATION
	 * @see Context#setProxyAuthorization(String)
	 */
	public void setProxyAuthorization(String value) {
		put(PROXY_AUTHORIZATION, value);
	}

	/**
	 * Set the string value for the PUBLIC_KEY key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for PUBLIC_KEY
	 * @see Context#setPublicKey(String)
	 */
	public void setPublicKey(String value) {
		put(PUBLIC, value);
	}

	/**
	 * Set the string value for the RANGE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for RANGE
	 * @see Context#setRange(String)
	 */
	public void setRange(String value) {
		put(RANGE, value);
	}

	/**
	 * Set the URI of the resource from which the request was obtained.
	 * Set the String value for the REFERER key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for REFERER
	 * @see Context#setReferer(String)
	 */
	public void setReferer(String value) {
		put(REFERER, value);
	}

	/**
	 * Set the URI value for the REF_TARGET key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for REF_TARGET
	 * @see Context#setRefTarget(String)
	 */
	public void setRefTarget(String value) {
		put(REF_TARGET, value);
	}

	/**
	 * Set the string value for the RESOURCE_TYPE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for RESOURCE_TYPE
	 * @see Context#setResourceType(String)
	 */
	public void setResourceType(String value) {
		put(RES_TYPE, value);
	}

	/**
	 * Set the string value for the RETRY_AFTER key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for RETRY_AFTER
	 * @see Context#setRetryAfter(String)
	 */
	public void setRetryAfter(String value) {
		put(RETRY_AFTER, value);
	}

	/**
	 * Set information about the software used by the origin server
	 * to handle the request. Set the string value for the SERVER key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for SERVER
	 * @see Context#setServer(String)
	 */
	public void setServer(String value) {
		put(SERVER, value);
	}

	/**
	 * Set the URI of the resource whose method is in process.
	 * Set the String value for the STATUS_URI key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for STATUS_URI
	 * @see Context#setStatusURI(String)
	 */
	public void setStatusURI(String value) {
		put(STATUS_URI, value);
	}

	/**
	 * Set the lock timeout value in seconds. Pass -1 to clear the 
	 * value, pass -2 to set "Infinity". Set the integer value for the 
	 * TIMEOUT key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for TIMEOUT
	 * @see Context#setTimeout(int)
	 */
	public void setTimeout(int value) {
		if (value == -1)
			put(TIMEOUT, ""); //$NON-NLS-1$
		else
			put(TIMEOUT, (value == -2) ? DEPTH_INFINITY : "Second-" + Integer.toString(value)); //$NON-NLS-1$
	}

	/**
	 * Set the string value for the TRANSFER_ENCODING key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for TRANSFER_ENCODING
	 * @see Context#setTransferEncoding(String)
	 */
	public void setTransferEncoding(String value) {
		put(TRANSFER_ENCODING, value);
	}

	/**
	 * Set the string value for the UPGRADE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for UPGRADE
	 * @see Context#setUpgrade(String)
	 */
	public void setUpgrade(String value) {
		put(UPGRADE, value);
	}

	/**
	 * Set information about the user agent originating the request.
	 * Set the string value for the USER_AGENT key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for USER_AGENT
	 * @see Context#setUserAgent(String)
	 */
	public void setUserAgent(String value) {
		put(USER_AGENT, value);
	}

	/**
	 * Set the string value for the VARY key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for VARY
	 * @see Context#setVary(String)
	 */
	public void setVary(String value) {
		put(VARY, value);
	}

	/**
	 * Set the string value for the VIA key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for VIA
	 * @see Context#setVia(String)
	 */
	public void setVia(String value) {
		put(VIA, value);
	}

	/**
	 * Set the string value for the WARNING key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for WARNING
	 * @see Context#setWarning(String)
	 */
	public void setWarning(String value) {
		put(WARNING, value);
	}

	/**
	 * Set the string value for the WWW_AUTHENTICATE key.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Context</code>.</p>
	 *
	 * @param value the value for WWW_AUTHENTICATE
	 * @see Context#setWWWAuthenticate(String)
	 */
	public void setWWWAuthenticate(String value) {
		put(WWW_AUTHENTICATE, value);
	}

	/**
	 * Return a string representation of the context.
	 *
	 * @return the context, as a String
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Enumeration keysEnum = keys();
		while (keysEnum.hasMoreElements()) {
			String key = (String) keysEnum.nextElement();
			buffer.append(key);
			buffer.append(": "); //$NON-NLS-1$
			buffer.append(get(key));
			buffer.append('\n');
		}
		return buffer.toString();
	}
}
