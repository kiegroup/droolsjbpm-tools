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

import org.eclipse.webdav.IResponse;
import org.w3c.dom.Document;

public class Status implements Cloneable {

	// Instance fields all set during instance creation.

	protected int code;
	protected String message;
	protected Document extendedStatus;

	/* Static fields -- Predefined status messages. */

	// 100-series
	public final static Status CONTINUE = new Status(IResponse.SC_CONTINUE, "Continue"); //$NON-NLS-1$
	public final static Status SWITCHING_PROTOCOLS = new Status(IResponse.SC_SWITCHING_PROTOCOLS, "Switching Protocols"); //$NON-NLS-1$
	public final static Status PROCESSING = new Status(IResponse.SC_PROCESSING, "Processing"); //$NON-NLS-1$

	// 200-series
	public final static Status OK = new Status(IResponse.SC_OK, "OK"); //$NON-NLS-1$
	public final static Status CREATED = new Status(IResponse.SC_CREATED, "Created"); //$NON-NLS-1$
	public final static Status ACCEPTED = new Status(IResponse.SC_ACCEPTED, "Accepted"); //$NON-NLS-1$
	public final static Status NON_AUTHORITATIVE_INFORMATION = new Status(IResponse.SC_NON_AUTHORITATIVE_INFORMATION, "Non Authoritative Information"); //$NON-NLS-1$
	public final static Status NO_CONTENT = new Status(IResponse.SC_NO_CONTENT, "No Content"); //$NON-NLS-1$
	public final static Status RESET_CONTENT = new Status(IResponse.SC_RESET_CONTENT, "Reset Content"); //$NON-NLS-1$
	public final static Status PARTIAL_CONTENT = new Status(IResponse.SC_PARTIAL_CONTENT, "Partial Content"); //$NON-NLS-1$
	public final static Status MULTI_STATUS = new Status(IResponse.SC_MULTI_STATUS, "Multi-Status"); //$NON-NLS-1$

	// 300-series
	public final static Status MULTIPLE_CHOICES = new Status(IResponse.SC_MULTIPLE_CHOICES, "Multiple Choices"); //$NON-NLS-1$
	public final static Status MOVED_PERMANENTLY = new Status(IResponse.SC_MOVED_PERMANENTLY, "Moved Permanently"); //$NON-NLS-1$
	public final static Status MOVED_TEMPORARILY = new Status(IResponse.SC_MOVED_TEMPORARILY, "Moved Temporarily"); //$NON-NLS-1$
	public final static Status SEE_OTHER = new Status(IResponse.SC_SEE_OTHER, "See Other"); //$NON-NLS-1$
	public final static Status NOT_MODIFIED = new Status(IResponse.SC_NOT_MODIFIED, "Not Modified"); //$NON-NLS-1$
	public final static Status USE_PROXY = new Status(IResponse.SC_USE_PROXY, "Use Proxy"); //$NON-NLS-1$

	// 400-series
	public final static Status BAD_REQUEST = new Status(IResponse.SC_BAD_REQUEST, "Bad Request"); //$NON-NLS-1$
	public final static Status UNAUTHORIZED = new Status(IResponse.SC_UNAUTHORIZED, "Unauthorized"); //$NON-NLS-1$
	public final static Status PAYMENT_REQUIRED = new Status(IResponse.SC_PAYMENT_REQUIRED, "Payment Required"); //$NON-NLS-1$
	public final static Status FORBIDDEN = new Status(IResponse.SC_FORBIDDEN, "Forbidden"); //$NON-NLS-1$
	public final static Status NOT_FOUND = new Status(IResponse.SC_NOT_FOUND, "Not Found"); //$NON-NLS-1$
	public final static Status METHOD_NOT_ALLOWED = new Status(IResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed"); //$NON-NLS-1$
	public final static Status NOT_ACCEPTABLE = new Status(IResponse.SC_NOT_ACCEPTABLE, "Not Acceptable"); //$NON-NLS-1$
	public final static Status PROXY_AUTHENTICATION_REQUIRED = new Status(IResponse.SC_PROXY_AUTHENTICATION_REQUIRED, "Proxy Authentication Required"); //$NON-NLS-1$
	public final static Status REQUEST_TIMEOUT = new Status(IResponse.SC_REQUEST_TIMEOUT, "Request Timeout"); //$NON-NLS-1$
	public final static Status CONFLICT = new Status(IResponse.SC_CONFLICT, "Conflict"); //$NON-NLS-1$
	public final static Status GONE = new Status(IResponse.SC_GONE, "Gone"); //$NON-NLS-1$
	public final static Status LENGTH_REQUIRED = new Status(IResponse.SC_LENGTH_REQUIRED, "Length Required"); //$NON-NLS-1$
	public final static Status PRECONDITION_FAILED = new Status(IResponse.SC_PRECONDITION_FAILED, "Precondition Failed"); //$NON-NLS-1$
	public final static Status REQUEST_TOO_LONG = new Status(IResponse.SC_REQUEST_TOO_LONG, "Request Too Long"); //$NON-NLS-1$
	public final static Status REQUEST_URI_TOO_LONG = new Status(IResponse.SC_REQUEST_URI_TOO_LONG, "Request URI Too Long"); //$NON-NLS-1$
	public final static Status UNSUPPORTED_MEDIA_TYPE = new Status(IResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type"); //$NON-NLS-1$
	public final static Status UNPROCESSABLE_ENTITY = new Status(IResponse.SC_UNPROCESSABLE_ENTITY, "Unprocessable Entity"); //$NON-NLS-1$
	public final static Status LOCKED = new Status(IResponse.SC_LOCKED, "Locked"); //$NON-NLS-1$
	public final static Status FAILED_DEPENDENCY = new Status(IResponse.SC_FAILED_DEPENDENCY, "Failed Dependency"); //$NON-NLS-1$
	public final static Status INSUFFICIENT_SPACE_ON_RESOURCE = new Status(IResponse.SC_INSUFFICIENT_SPACE_ON_RESOURCE, "Insufficient Space on Resource"); //$NON-NLS-1$

	// 500-series
	public final static Status INTERNAL_SERVER_ERROR = new Status(IResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error"); //$NON-NLS-1$
	public final static Status NOT_IMPLEMENTED = new Status(IResponse.SC_NOT_IMPLEMENTED, "Not Implemented"); //$NON-NLS-1$
	public final static Status BAD_GATEWAY = new Status(IResponse.SC_BAD_GATEWAY, "Bad Gateway"); //$NON-NLS-1$
	public final static Status SERVICE_UNAVAILABLE = new Status(IResponse.SC_SERVICE_UNAVAILABLE, "Service Unavailable"); //$NON-NLS-1$
	public final static Status GATEWAY_TIMEOUT = new Status(IResponse.SC_GATEWAY_TIMEOUT, "Gateway Timeout"); //$NON-NLS-1$
	public final static Status HTTP_VERSION_NOT_SUPPORTED = new Status(IResponse.SC_HTTP_VERSION_NOT_SUPPORTED, "HTTP Version Not Supported"); //$NON-NLS-1$
	public final static Status LOOP_DETECTED = new Status(IResponse.SC_LOOP_DETECTED, "Loop Detected"); //$NON-NLS-1$
	public final static Status CROSS_SERVER_BINDING_FORBIDDEN = new Status(IResponse.SC_CROSS_SERVER_BINDING_FORBIDDEN, "Cross-Server Binding Forbidden"); //$NON-NLS-1$

	public Status(int code, String message) {
		super();
		this.code = code;
		this.message = message;
		this.extendedStatus = null;
	}

	public Status(Status basicStatus, Document extendedStatus) {
		super();
		this.code = basicStatus.code;
		this.message = basicStatus.message;
		this.extendedStatus = extendedStatus;
	}

	/**
	 * Answers whether the receiver and the argument are considered
	 * equal.  Note that in this implementation of equal, the only
	 * field that is considered is the status code.  In particular,
	 * the status message and the extended status information are
	 * not considered revlevant to equality.
	 *
	 * @param obj other object with which to test equality.
	 * @return boolean indicating equality.
	 * @see Status#sameAs(Object)
	 */
	public boolean equals(Object obj) {
		return (obj != null) && (obj instanceof Status) && (this.code == ((Status) obj).getCode());
	}

	public int getCode() {
		return code;
	}

	/**
	 * Get the extended status information for the receiver
	 * as an XML document.  This method will return <code>null
	 * </code> if there is no extended information.
	 *
	 * @return the extended infomration as a <code>Document</code>,
	 *or <code>null</code> if there is no such information.
	 */
	public Document getExtendedStatus() {
		return extendedStatus;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * @see #hashCode()
	 */
	public int hashCode() {
		return code;
	}

	/**
	 * Answers whether the receiver and the argument are considered
	 * identical.  To be identical, the receiver and the argument
	 * must have the same status code, message, and extended status
	 * information.
	 *
	 * @param obj other object with which to test.
	 * @return boolean indicating whether they are the same or not.
	 * @see Status#equals(Object)
	 */
	public boolean sameAs(Object obj) {
		if (obj == null || !(obj instanceof Status))
			return false;
		Status other = (Status) obj;
		if (other.code != code || !other.message.equals(message))
			return false;
		return other.extendedStatus.equals(extendedStatus);
	}

	public String toHTTPString() {
		return "HTTP/1.1 " + code + " " + message; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String toString() {
		return getClass().getName() + "(" + toHTTPString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
