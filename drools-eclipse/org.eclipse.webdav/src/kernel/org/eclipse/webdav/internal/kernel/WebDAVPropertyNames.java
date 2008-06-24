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

import org.eclipse.webdav.dom.QualifiedName;

public interface WebDAVPropertyNames extends WebDAVConstants {

	// From WebDAV (RFC2518).
	QualifiedName DAV_CREATION_DATE = new QualifiedNameImpl(DAV_URI, "creationdate"); //$NON-NLS-1$
	QualifiedName DAV_DISPLAY_NAME = new QualifiedNameImpl(DAV_URI, "displayname"); //$NON-NLS-1$
	QualifiedName DAV_GET_CONTENT_LANGUAGE = new QualifiedNameImpl(DAV_URI, "getcontentlanguage"); //$NON-NLS-1$
	QualifiedName DAV_GET_CONTENT_LENGTH = new QualifiedNameImpl(DAV_URI, "getcontentlength"); //$NON-NLS-1$
	QualifiedName DAV_GET_CONTENT_TYPE = new QualifiedNameImpl(DAV_URI, "getcontenttype"); //$NON-NLS-1$
	QualifiedName DAV_GET_E_TAG = new QualifiedNameImpl(DAV_URI, "getetag"); //$NON-NLS-1$
	QualifiedName DAV_GET_LAST_MODIFIED = new QualifiedNameImpl(DAV_URI, "getlastmodified"); //$NON-NLS-1$
	QualifiedName DAV_HREF = new QualifiedNameImpl(DAV_URI, "href"); //$NON-NLS-1$
	QualifiedName DAV_LOCK_DISCOVERY = new QualifiedNameImpl(DAV_URI, "lockdiscovery"); //$NON-NLS-1$
	QualifiedName DAV_RESOURCE_TYPE = new QualifiedNameImpl(DAV_URI, "resourcetype"); //$NON-NLS-1$
	QualifiedName DAV_SOURCE = new QualifiedNameImpl(DAV_URI, "source"); //$NON-NLS-1$
	QualifiedName DAV_SUPPORTED_LOCK = new QualifiedNameImpl(DAV_URI, "supportedlock"); //$NON-NLS-1$

	// From Delta-V.

	// Properties defined in core versioning.
	QualifiedName DAV_ACTIVITY_CHECKOUT_SET = new QualifiedNameImpl(DAV_URI, "activity-checkout-set"); //$NON-NLS-1$
	QualifiedName DAV_ACTIVITY_VERSION_SET = new QualifiedNameImpl(DAV_URI, "activity-version-set"); //$NON-NLS-1$
	QualifiedName DAV_AUTO_CHECKIN = new QualifiedNameImpl(DAV_URI, "auto-checkin"); //$NON-NLS-1$
	QualifiedName DAV_AUTO_CHECKOUT = new QualifiedNameImpl(DAV_URI, "auto-checkout"); //$NON-NLS-1$
	QualifiedName DAV_AUTO_MERGE_SET = new QualifiedNameImpl(DAV_URI, "auto-merge-set"); //$NON-NLS-1$
	QualifiedName DAV_BASELINE_CONTROLLED_COLLECTION = new QualifiedNameImpl(DAV_URI, "baseline-controlled-collection"); //$NON-NLS-1$
	QualifiedName DAV_BASELINE_COLLECTION = new QualifiedNameImpl(DAV_URI, "baseline-collection"); //$NON-NLS-1$
	QualifiedName DAV_BASELINE_CONTROLLED_COLLECTION_SET = new QualifiedNameImpl(DAV_URI, "baseline-controlled-collection-set"); //$NON-NLS-1$
	QualifiedName DAV_BASELINE_SELECTOR = new QualifiedNameImpl(DAV_URI, "baseline-selector"); //$NON-NLS-1$
	QualifiedName DAV_CHECKED_IN = new QualifiedNameImpl(DAV_URI, "checked-in"); //$NON-NLS-1$
	QualifiedName DAV_CHECKED_OUT = new QualifiedNameImpl(DAV_URI, "checked-out"); //$NON-NLS-1$
	QualifiedName DAV_CHECKIN_DATE = new QualifiedNameImpl(DAV_URI, "checkin-date"); //$NON-NLS-1$
	QualifiedName DAV_CHECKIN_FORK = new QualifiedNameImpl(DAV_URI, "checkin-fork"); //$NON-NLS-1$
	QualifiedName DAV_CHECKOUT_FORK = new QualifiedNameImpl(DAV_URI, "checkout-fork"); //$NON-NLS-1$
	QualifiedName DAV_CHECKOUT_SET = new QualifiedNameImpl(DAV_URI, "checkout-date"); //$NON-NLS-1$
	QualifiedName DAV_COMMENT = new QualifiedNameImpl(DAV_URI, "comment"); //$NON-NLS-1$
	QualifiedName DAV_CREATOR_DISPLAYNAME = new QualifiedNameImpl(DAV_URI, "creator-displayname"); //$NON-NLS-1$
	QualifiedName DAV_CURRENT_ACTIVITY_SET = new QualifiedNameImpl(DAV_URI, "current-activity-set"); //$NON-NLS-1$
	QualifiedName DAV_CURRENT_WORKSPACE_SET = new QualifiedNameImpl(DAV_URI, "current-workspace-set"); //$NON-NLS-1$
	QualifiedName DAV_LATEST_VERSION = new QualifiedNameImpl(DAV_URI, "latest-version"); //$NON-NLS-1$
	QualifiedName DAV_LABEL_NAME_SET = new QualifiedNameImpl(DAV_URI, "label-name-set"); //$NON-NLS-1$
	QualifiedName DAV_MERGE_SET = new QualifiedNameImpl(DAV_URI, "merge-set"); //$NON-NLS-1$
	QualifiedName DAV_MUTABLE = new QualifiedNameImpl(DAV_URI, "mutable"); //$NON-NLS-1$
	QualifiedName DAV_PRECURSOR_SET = new QualifiedNameImpl(DAV_URI, "precursor-set"); //$NON-NLS-1$
	QualifiedName DAV_PREDECESSOR_SET = new QualifiedNameImpl(DAV_URI, "predecessor-set"); //$NON-NLS-1$
	QualifiedName DAV_ROOT_VERSION = new QualifiedNameImpl(DAV_URI, "root-version"); //$NON-NLS-1$
	QualifiedName DAV_SUBACTIVITY_SET = new QualifiedNameImpl(DAV_URI, "subactivity-set"); //$NON-NLS-1$
	QualifiedName DAV_SUBBASELINE_SET = new QualifiedNameImpl(DAV_URI, "subbaseline-set"); //$NON-NLS-1$
	QualifiedName DAV_SUCCESSOR_SET = new QualifiedNameImpl(DAV_URI, "successor-set"); //$NON-NLS-1$
	QualifiedName DAV_SUPPORTED_LIVE_PROPERTY_SET = new QualifiedNameImpl(DAV_URI, "supported-live-property-set"); //$NON-NLS-1$
	QualifiedName DAV_SUPPORTED_METHOD_SET = new QualifiedNameImpl(DAV_URI, "supported-method-set"); //$NON-NLS-1$
	QualifiedName DAV_SUPPORTED_REPORT_SET = new QualifiedNameImpl(DAV_URI, "supported-report-set"); //$NON-NLS-1$
	QualifiedName DAV_UNRESERVED = new QualifiedNameImpl(DAV_URI, "unreserved"); //$NON-NLS-1$
	QualifiedName DAV_VERSION_CONTROLLED_CONFIGURATION = new QualifiedNameImpl(DAV_URI, "version-controlled-configuration"); //$NON-NLS-1$
	QualifiedName DAV_VERSION_HISTORY = new QualifiedNameImpl(DAV_URI, "version-history"); //$NON-NLS-1$
	QualifiedName DAV_VERSION_NAME = new QualifiedNameImpl(DAV_URI, "version-name"); //$NON-NLS-1$
	QualifiedName DAV_VERSION_SET = new QualifiedNameImpl(DAV_URI, "version-set"); //$NON-NLS-1$
	QualifiedName DAV_WORKSPACE = new QualifiedNameImpl(DAV_URI, "workspace"); //$NON-NLS-1$
	QualifiedName DAV_WORKSPACE_CHECKOUT_SET = new QualifiedNameImpl(DAV_URI, "workspace-checkout-set"); //$NON-NLS-1$
	QualifiedName DAV_WORKSPACE_COLLECTION_SET = new QualifiedNameImpl(DAV_URI, "workspace-collection-set"); //$NON-NLS-1$

	// From Bindings spec.
	QualifiedName DAV_RESOURCE_ID = new QualifiedNameImpl(DAV_URI, "resourceid"); //$NON-NLS-1$

	// Added by this server implementation.
	QualifiedName DAV_WORKING_RESOURCE = new QualifiedNameImpl(DAV_URI, "working-resource"); //$NON-NLS-1$
}
