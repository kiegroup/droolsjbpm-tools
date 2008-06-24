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
package org.eclipse.webdav;

import java.util.Enumeration;

/**
 * The <code>IContext</code> interface provides access methods
 * for most HTTP and WebDAV message header fields.  Those that are
 * not explicitly enumerated can be accessed using <code>
 * Context#get(String)</code> and <code>Context#put(String,String)
 * </code>.  All context keys are available with <code>
 * Context#keys()</code>.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public interface IContext {

	// Depth directive header values.

	/** Depth constant indicating apply the method to the target resource.
	 * @see #getDepth()
	 */
	public static final String DEPTH_ZERO = "0"; //$NON-NLS-1$

	/** Depth constant indicating apply the method to the target resource.
	 * and (where the resource is a collection) its immediate members.
	 * @see #getDepth()
	 */
	public static final String DEPTH_ONE = "1"; //$NON-NLS-1$

	/** Depth constant indicating apply the method to the target resource.
	 * and (where the resource is a collection) recursively to all
	 * its members.
	 * @see #getDepth()
	 */
	public static final String DEPTH_INFINITY = "infinity"; //$NON-NLS-1$

	// Locking constants

	/** Lock type constant indicating an exclusive lock. 
	 * Only one exclusive lock can be granted at any time on a resource.
	 */
	public static final String EXCLUSIVE_LOCK = "exclusive"; //$NON-NLS-1$

	/** Lock type constant indicating a shared lock. 
	 * A resource may have many concurrent shared locks which indicate an
	 * intention to change the resource in some way. It is the responsibilty
	 * of the shared lock owners to coordinate their updates appropriately
	 * through other means.
	 */
	public static final String SHARED_LOCK = "shared"; //$NON-NLS-1$

	/** Lock type constant indicating a write lock. 
	 * Write locks allow a resource to be updated or deleted.
	 */
	public static final String WRITE_LOCK = "write"; //$NON-NLS-1$

	/** Constant indicating an indefinite timeout period.
	 */
	public static final int NO_EXPIRY_LOCK = -1;

	/** Constant used in the Cache-Control header indicating that clients and
	 * proxies MUST not cache the response of a method since it has
	 * non-idempotent semantics.
	 */
	public static final String NO_CACHE = "no-cache"; //$NON-NLS-1$

	/**
	 * Collapse teh receiver by copying all the keys and values from the
	 * default context into the receiver.  This effectively makes the Context
	 * stand-alone so that subsequent changes to shared contexts are no
	 * longer seen by the receiver.
	 */
	public void collapse();

	/**
	 * Returns the value for the given key, or <code>null</code>
	 * if the given key has no value.
	 * @param key the key to look up
	 * @return the value for the key
	 */
	public String get(String key);

	/**
	 * Returns the media types that are acceptable for a response.
	 * @return the value for <code>"Accept"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getAccept();

	/**
	 * Returns the character sets that are acceptable for a response.
	 * @return the value for <code>"Accept-Charset"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getAcceptCharset();

	/**
	 * Retruns what content-encoding values are acceptable for a response.
	 * @return the value for <code>"Accept-Encoding"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getAcceptEncoding();

	/**
	 * Returns the natural languages that are acceptable for a response.
	 * @return the value for <code>"Accept-Language"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getAcceptLanguage();

	/**
	 * Returns the range requests acceptable to a server.
	 * @return the value for <code>"Accept-Ranges"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getAcceptRanges();

	/**
	 * Returns the integer value of sender's estimate of the time since the 
	 * response was generated, or -1 if the value is not set.
	 * @return the integer value for <code>"Age"</code> key,
	 * or -1 if not set.
	 */
	public int getAge();

	/**
	 * Returns the string value for the ALL_BINDINGS key.
	 * @return the value for <code>"All-Bindings"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getAllBindings();

	/**
	 * Returns the methods allowed on a resource
	 * @return the value for <code>"Allow"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getAllow();

	/**
	 * Returns the user's credentials for the realm of the resource being requested.
	 * @return the value for <code>"Authorization"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getAuthorization();

	/**
	 * Returns the cache control directives that must be obeyed.
	 * @return the value for <code>"Cache-Control"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getCacheControl();

	/**
	 * Returns the sender connection options.
	 * @return the value for <code>"Connection"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getConnection();

	/**
	 * Returns the URL string value for the CONTENT_BASE key.
	 * @return the value for <code>"Content-Base"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getContentBase();

	/**
	 * Returns what additional content encodings have been applied to the entity body.
	 * @return the value for <code>"Content-Encoding"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getContentEncoding();

	/**
	 * Returns the natural language of the intended audience for the entity body.
	 * @return the value for <code>"Content-Language"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getContentLanguage();

	/**
	 * Returns the content length in bytes of the entity body.
	 * @return the value for <code>"Content-Length"</code> key, 
	 *   or -1 if that key has no value.
	 */
	public long getContentLength();

	/**
	 * Returns the URL that locates the content.
	 * @return the value for <code>"Content-Location"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getContentLocation();

	/**
	 * Returns the string value for the CONTENT_MD5 key.
	 * @return the value for <code>"Content-MD5"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getContentMD5();

	/**
	 * Returns the string value for the CONTENT_RANGE key.
	 * @return the value for <code>"Content-Range"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getContentRange();

	/**
	 * Returns the MIME type for the response contents. 
	 * @return the value for <code>"Content-Type"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getContentType();

	/**
	 * Returns the date the request was made.
	 * @return the value for <code>"Date"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getDate();

	/**
	 * Returns the DAV level supported by the server.
	 * [missing: what are these values]
	 */
	public String getDAV();

	/**
	 * Returns the string value for the "Depth" key.
	 * @return one of <code>DEPTH_ZERO</code>,
	 *  <code>DEPTH_ONE</code>, or <code>DEPTH_INFINITY</code>
	 * @see #DEPTH_ZERO
	 * @see #DEPTH_ONE
	 * @see #DEPTH_INFINITY
	 */
	public String getDepth();

	/**
	 * Returns the destination URL for a copy or move operation.
	 * @return the value for <code>"Destination"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getDestination();

	/**
	 * Returns the entity tag for the associated entity.
	 * @return the value for <code>"ETag"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getETag();

	/**
	 * Returns the date/time after which the response should be 
	 * considered stale.
	 * @return the value for <code>"Expires"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getExpires();

	/**
	 * Returns the string value for the FROM key.
	 * @return the value for <code>"From"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getFrom();

	/**
	 * Returns the Internet host and port of the resource being requested.
	 * @return the value for <code>"Host"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getHost();

	/**
	 * Returns the string value for the IF key.
	 * @return the value for <code>"If"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getIfKey();

	/**
	 * Returns the string value for the IF_MATCH key.
	 * @return the value for <code>"If-Match"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getIfMatch();

	/**
	 * Returns the string value for the IF_MODIFIED_SINCE key.
	 * @return the value for <code>"If-Modified-Since"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getIfModifiedSince();

	/**
	 * Returns the string value for the IF_NONE_MATCH key.
	 * @return the value for <code>"If-None-Match"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getIfNoneMatch();

	/**
	 * Returns the string value for the IF_RANGE key.
	 * @return the value for <code>"If-Range"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getIfRange();

	/**
	 * Returns the string value for the IF_UNMODIFIED_SINCE key.
	 * @return the value for <code>"If-Unmodified-Since"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getIfUnmodifiedSince();

	/**
	 * Returns the label header value.
	 * @return the value for <code>"Label"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getLabel();

	/**
	 * Returns when the resource was last modified.
	 * @return the value for <code>"Last-Modified"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getLastModified();

	/**
	 * Returns the URI of the redirect location.
	 * @return the value for <code>"Location"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getLocation();

	/**
	 * Returns the lock token for the resource, or null if it is not set.
	 * @return the value for <code>"Lock-Token"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getLockToken();

	/**
	 * Returns the integer value for the MAX_FORWARDS key.
	 * @return the value for <code>"Max-Forwards"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public int getMaxForwards();

	/**
	 * Returns the string value for the ORDERED key.
	 * @return the value for <code>"Ordered"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getOrdered();

	/**
	 * Returns the flag that indicates if copy or move should overwrite
	 * an existing destination.
	 * @return the value for <code>"Overwrite"</code> key, 
	 *   or <code>false</code> if that key has no value.
	 */
	public boolean getOverwrite();

	/**
	 * Returns the boolean value for the PASSTHROUGH key.
	 * @return the value for <code>"Passthrough"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public boolean getPassthrough();

	/**
	 * Returns the string value for the POSITION key.
	 * @return the value for <code>"Position"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getPosition();

	/**
	 * Returns the string value for the PRAGMA key.
	 * @return the value for <code>"Pragma"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getPragma();

	/**
	 * Returns the string value for the PROXY_AUTHENTICATE key.
	 * @return the value for <code>"Proxy-Authenticate"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getProxyAuthenticate();

	/**
	 * Returns the string value for the PROXY_AUTHORIZATION key.
	 * @return the value for <code>"Proxy-Authorization"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getProxyAuthorization();

	/**
	 * Returns the string value for the PUBLIC_KEY key.
	 * @return the value for <code>"Public-Key"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getPublicKey();

	/**
	 * Returns the string value for the RANGE key.
	 * @return the value for <code>"Range"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getRange();

	/**
	 * Returns the URI string of the resource from which the request was obtained.
	 * @return the value for <code>"Referer"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getReferer();

	/**
	 * Returns the URI string for the REF_TARGET key.
	 * @return the value for <code>"Ref-Target"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getRefTarget();

	/**
	 * Returns the string value for the RESOURCE_TYPE key.
	 * @return the value for <code>"Resource-Type"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getResourceType();

	/**
	 * Returns the string value for the RETRY_AFTER key.
	 * @return the value for <code>"Retry-After"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getRetryAfter();

	/**
	 * Returns information about the software used by the origin server
	 * to handle the request.
	 * @return the value for <code>"Server"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getServer();

	/**
	 * Returns the URI string of the resource whose method is in process.
	 * @return the value for <code>"Status-URI"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getStatusURI();

	/**
	 * Returns the lock timeout value.
	 * @return the integer value for <code>"Timeout"</code> key, 
	 *   where -1 means that the value was not set, and -2 means 
	 *   that the value was infinity.
	 */
	public int getTimeout();

	/**
	 * Returns the string value for the TRANSFER_ENCODING key.
	 * @return the value for <code>"Transfer-Encoding"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getTransferEncoding();

	/**
	 * Returns the string value for the UPGRADE key.
	 * @return the value for <code>"Upgrade"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getUpgrade();

	/**
	 * Returns information about the user agent originating the request.
	 * @return the value for <code>"User-Agent"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getUserAgent();

	/**
	 * Returns the string value for the VARY key.
	 * @return the value for <code>"Vary"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getVary();

	/**
	 * Returns the string value for the VIA key.
	 * @return the value for <code>"Via"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getVia();

	/**
	 * Returns the string value for the WARNING key.
	 * @return the value for <code>"Warning"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getWarning();

	/**
	 * Returns the string value for the WWW_AUTHENTICATE key.
	 * @return the value for <code>"WWW-Authenticate"</code> key, 
	 *   or <code>null</code> if that key has no value.
	 */
	public String getWWWAuthenticate();

	/**
	 * Returns keys of the context, as an enumeration
	 *
	 * @return an enumeration over the context keys
	 *   (<code>String</code>s).
	 */
	public Enumeration keys();

	/**
	 * Adds or replaces the given key-value pair into the context.
	 *
	 * @param key the key
	 * @param value its associated value
	 */
	public void put(String key, String value);

	/**
	 * Removes the given key from this context. Does nothing if the
	 * given key is not set.
	 *
	 * @param key the key to remove
	 */
	public void removeKey(String key);

	/**
	 * Sets what media types are acceptable for a response.
	 * Sets the string value for the ACCEPT key.
	 *
	 * @param value the value for ACCEPT
	 */
	public void setAccept(String value);

	/**
	 * Sets which character sets are acceptable for a response.
	 * Sets the string value for the ACCEPT_CHARSET key.
	 *
	 * @param value the value for ACCEPT_CHARSET
	 */
	public void setAcceptCharset(String value);

	/**
	 * Sets the content-encoding values which are acceptable for a response.
	 * Sets the string value for the ACCEPT_ENCODING key.
	 *
	 * @param value the value for ACCEPT_ENCODING
	 */
	public void setAcceptEncoding(String value);

	/**
	 * Sets which natural languages are acceptable for a response.
	 * Sets the string value for the ACCEPT_LANGUAGE key.
	 *
	 * @param value the value for ACCEPT_LANGUAGE
	 */
	public void setAcceptLanguage(String value);

	/**
	 * Sets the range requests acceptable to a server.
	 * Sets the string value for the ACCEPT_RANGES key.
	 *
	 * @param value the value for ACCEPT_RANGES
	 */
	public void setAcceptRanges(String value);

	/**
	 * Sets the sender's estimate of the time since the response was generated.
	 * Sets the int value for the AGE key. Set the value to -1 to remove the key.
	 *
	 * @param seconds the value for AGE
	 */
	public void setAge(int seconds);

	/**
	 * Sets the string value for the ALL_BINDINGS key.
	 *
	 * @param value the value for ALL_BINDINGS
	 */
	public void setAllBindings(String value);

	/**
	 * Sets methods allowed on a resource
	 * Sets the string value for the ALLOW key.
	 *
	 * @param value the value for ALLOW
	 */
	public void setAllow(String value);

	/**
	 * Sets the user's credentials for the realm of the resource being requested.
	 * Sets the string value for the AUTHORIZATION key.
	 *
	 * @param value the value for AUTHORIZATION
	 */
	public void setAuthorization(String value);

	/**
	 * Sets the cache control directives that must be obeyed.
	 * Sets the string value for the CACHE_CONTROL key.
	 *
	 * @param value the value for CACHE_CONTROL
	 */
	public void setCacheControl(String value);

	/**
	 * Sets sender connection options.
	 * Sets the string value for the CONNECTION key.
	 *
	 * @param value the value for CONNECTION
	 */
	public void setConnection(String value);

	/**
	 * Sets the URL string value for the CONTENT_BASE key.
	 *
	 * @param stringURL the value for CONTENT_BASE
	 */
	public void setContentBase(String stringURL);

	/**
	 * Sets the additional content encodings that have been applied to the entity body.
	 * Sets the string value for the CONTENT_ENCODING key.
	 *
	 * @param value the value for CONTENT_ENCODING
	 */
	public void setContentEncoding(String value);

	/**
	 * Sets the natural language of the intended audience for the entity body.
	 * Sets the string value for the CONTENT_LANGUAGE key.
	 *
	 * @param value the value for CONTENT_LANGUAGE
	 */
	public void setContentLanguage(String value);

	/**
	 * Sets the content length in bytes of the entity body.
	 * Sets the value for the CONTENT_LENGTH key.
	 * Passes the value -1 to remove the key.
	 *
	 * @param value the value for CONTENT_LENGTH
	 */
	public void setContentLength(long value);

	/**
	 * Sets the URL string value for the CONTENT_LOCATION key.
	 *
	 * @param stringURL the value for CONTENT_LOCATION
	 */
	public void setContentLocation(String stringURL);

	/**
	 * Sets the string value for the CONTENT_MD5 key.
	 *
	 * @param value the value for CONTENT_MD5
	 */
	public void setContentMD5(String value);

	/**
	 * Sets the string value for the CONTENT_RANGE key.
	 *
	 * @param value the value for CONTENT_RANGE
	 */
	public void setContentRange(String value);

	/**
	 * Sets the MIME type for the response contents. 
	 * Sets the string value for the CONTENT_TYPE key.
	 *
	 * @param value the value for CONTENT_TYPE
	 */
	public void setContentType(String value);

	/**
	 * Sets the date the request was made.
	 * Sets the string value for the DATE key.
	 *
	 * @param value the value for DATE
	 */
	public void setDate(String value);

	/**
	 * Sets the DAV level supported by the server.
	 * Sets the string value for the DAV key.
	 *
	 * @param value the value for DAV
	 */
	public void setDAV(String value);

	/**
	 * Sets the string value for the DEPTH key.
	 * @param depth one of <code>DEPTH_ZERO</code>,
	 *  <code>DEPTH_ONE</code>, or <code>DEPTH_INFINITY</code>
	 * @see #DEPTH_ZERO
	 * @see #DEPTH_ONE
	 * @see #DEPTH_INFINITY
	 */
	public void setDepth(String depth);

	/**
	 * Sets the destination URI for a copy or move operation.
	 * Sets the URI value for the DESTINATION key.
	 *
	 * @param stringURL the value for DESTINATION
	 */
	public void setDestination(String stringURL);

	/**
	 * Sets the entity tag for the associated entity.
	 * Sets the string value for the ETAG key.
	 *
	 * @param value the value for ETAG
	 */
	public void setETag(String value);

	/**
	 * Sets the date/time after which the response should be considered stale.
	 * Sets the string value for the EXPIRES key.
	 *
	 * @param value the value for EXPIRES
	 */
	public void setExpires(String value);

	/**
	 * Sets the string value for the FROM key.
	 *
	 * @param value the value for FROM
	 */
	public void setFrom(String value);

	/**
	 * Sets the Internet host and port of the resource being requested.
	 * Sets the string value for the HOST key.
	 *
	 * @param value the value for HOST
	 */
	public void setHost(String value);

	/**
	 * Sets the string value for the IF key.
	 *
	 * @param value the value for IF
	 */
	public void setIfKey(String value);

	/**
	 * Sets the string value for the IF_MATCH key.
	 *
	 * @param value the value for IF_MATCH
	 */
	public void setIfMatch(String value);

	/**
	 * Sets the string value for the IF_MODIFIED_SINCE key.
	 *
	 * @param value the value for IF_MODIFIED_SINCE
	 */
	public void setIfModifiedSince(String value);

	/**
	 * Sets the string value for the IF_NONE_MATCH key.
	 *
	 * @param value the value for IF_NONE_MATCH
	 */
	public void setIfNoneMatch(String value);

	/**
	 * Sets the string value for the IF_RANGE key.
	 *
	 * @param value the value for IF_RANGE
	 */
	public void setIfRange(String value);

	/**
	 * Sets the string value for the IF_UNMODIFIED_SINCE key.
	 *
	 * @param value the value for IF_UNMODIFIED_SINCE
	 */
	public void setIfUnmodifiedSince(String value);

	/**
	 * Sets when the label header value.
	 * Sets the string value for the LABEL key.
	 *
	 * @param value the value for LABEL
	 */
	public void setLabel(String value);

	/**
	 * Sets when the resource was last modified.
	 * Sets the string value for the LAST_MODIFIED key.
	 *
	 * @param value the value for LAST_MODIFIED
	 */
	public void setLastModified(String value);

	/**
	 * Sets the redirect location.
	 * Sets the URI value for the LOCATION key.
	 *
	 * @param stringURL the value for LOCATION
	 */
	public void setLocation(String stringURL);

	/**
	 * Sets the lock token for the resource.
	 * Sets the lock token value for the LOCK_TOKEN key.
	 *
	 * @param lockToken the value for LOCK_TOKEN
	 */
	public void setLockToken(String lockToken);

	/**
	 * Sets the integer value for the MAX_FORWARDS key.
	 *
	 * @param value the value for MAX_FORWARDS
	 */
	public void setMaxForwards(int value);

	/**
	 * Sets the string value for the ORDERED key.
	 *
	 * @param value the value for ORDERED
	 */
	public void setOrdered(String value);

	/**
	 * Sets if copy or move should overwrite an existing destination.
	 * Sets the boolean value for the OVERWRITE key.
	 *
	 * @param value the value for OVERWRITE
	 */
	public void setOverwrite(boolean value);

	/**
	 * Sets the boolean value for the PASSTHROUGH key.
	 *
	 * @param value the value for PASSTHROUGH
	 */
	public void setPassthrough(boolean value);

	/**
	 * Sets the string value for the POSITION key.
	 *
	 * @param value the value for POSITION
	 */
	public void setPosition(String value);

	/**
	 * Sets the string value for the PRAGMA key.
	 *
	 * @param value the value for PRAGMA
	 */
	public void setPragma(String value);

	/**
	 * Sets the string value for the PROXY_AUTHENTICATE key.
	 *
	 * @param value the value for PROXY_AUTHENTICATE
	 */
	public void setProxyAuthenticate(String value);

	/**
	 * Sets the string value for the PROXY_AUTHORIZATION key.
	 *
	 * @param value the value for PROXY_AUTHORIZATION
	 */
	public void setProxyAuthorization(String value);

	/**
	 * Sets the string value for the PUBLIC_KEY key.
	 *
	 * @param value the value for PUBLIC_KEY
	 */
	public void setPublicKey(String value);

	/**
	 * Sets the string value for the RANGE key.
	 *
	 * @param value the value for RANGE
	 */
	public void setRange(String value);

	/**
	 * Sets the URI of the resource from which the request was obtained.
	 * Sets the URI value for the REFERER key.
	 *
	 * @param stringURL the value for REFERER
	 */
	public void setReferer(String stringURL);

	/**
	 * Sets the URI value for the REF_TARGET key.
	 *
	 * @param stringURL the value for REF_TARGET
	 */
	public void setRefTarget(String stringURL);

	/**
	 * Sets the string value for the RESOURCE_TYPE key.
	 *
	 * @param value the value for RESOURCE_TYPE
	 */
	public void setResourceType(String value);

	/**
	 * Sets the string value for the RETRY_AFTER key.
	 *
	 * @param value the value for RETRY_AFTER
	 */
	public void setRetryAfter(String value);

	/**
	 * Sets information about the software used by the origin server
	 * to handle the request. Sets the string value for the SERVER key.
	 *
	 * @param value the value for SERVER
	 */
	public void setServer(String value);

	/**
	 * Sets the URI of the resource whose method is in process.
	 * Sets the URI value for the STATUS_URI key.
	 *
	 * @param statusURI the value for STATUS_URI
	 */
	public void setStatusURI(String statusURI);

	/**
	 * Sets the lock timeout value in seconds. Pass -1 to clear the 
	 * value, pass -2 to set "Infinity". Sets the integer value for the 
	 * TIMEOUT key.
	 *
	 * @param value the value for TIMEOUT
	 */
	public void setTimeout(int value);

	/**
	 * Sets the string value for the TRANSFER_ENCODING key.
	 *
	 * @param value the value for TRANSFER_ENCODING
	 */
	public void setTransferEncoding(String value);

	/**
	 * Sets the string value for the UPGRADE key.
	 *
	 * @param value the value for UPGRADE
	 */
	public void setUpgrade(String value);

	/**
	 * Sets information about the user agent originating the request.
	 * Sets the string value for the USER_AGENT key.
	 *
	 * @param value the value for USER_AGENT
	 */
	public void setUserAgent(String value);

	/**
	 * Sets the string value for the VARY key.
	 *
	 * @param value the value for VARY
	 */
	public void setVary(String value);

	/**
	 * Sets the string value for the VIA key.
	 *
	 * @param value the value for VIA
	 */
	public void setVia(String value);

	/**
	 * Sets the string value for the WARNING key.
	 *
	 * @param value the value for WARNING
	 */
	public void setWarning(String value);

	/**
	 * Sets the string value for the WWW_AUTHENTICATE key.
	 *
	 * @param value the value for WWW_AUTHENTICATE
	 */
	public void setWWWAuthenticate(String value);
}
