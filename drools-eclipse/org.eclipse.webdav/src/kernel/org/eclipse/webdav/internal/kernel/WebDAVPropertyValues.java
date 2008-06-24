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

public interface WebDAVPropertyValues extends WebDAVConstants {

	// Resource types.
	QualifiedName DAV_ACTIVITY_RESOURCE_TYPE = new QualifiedNameImpl(DAV_URI, "activity-resourcetype"); //$NON-NLS-1$
	QualifiedName DAV_BASELINE_RESOURCE_TYPE = new QualifiedNameImpl(DAV_URI, "baseline-resourcetype"); //$NON-NLS-1$
	QualifiedName DAV_COLLECTION_RESOURCE_TYPE = new QualifiedNameImpl(DAV_URI, "collection"); //$NON-NLS-1$
	QualifiedName DAV_VERSION_HISTORY_RESOURCE_TYPE = new QualifiedNameImpl(DAV_URI, "version-history-resourcetype"); //$NON-NLS-1$
	QualifiedName DAV_WORKSPACE_RESOURCE_TYPE = new QualifiedNameImpl(DAV_URI, "workspace-resourcetype"); //$NON-NLS-1$
	QualifiedName DAV_HISTORY_RESOURCE_TYPE = new QualifiedNameImpl(DAV_URI, "history-resourcetype"); //$NON-NLS-1$

	// Check-in policies.
	QualifiedName DAV_KEEP_CHECKED_OUT = new QualifiedNameImpl(DAV_URI, "keep-checked-out"); //$NON-NLS-1$
	QualifiedName DAV_NEW_VERSION = new QualifiedNameImpl(DAV_URI, "new-version"); //$NON-NLS-1$
	QualifiedName DAV_OVERWRITE = new QualifiedNameImpl(DAV_URI, "overwrite"); //$NON-NLS-1$

	// Auto-version policies.
	QualifiedName DAV_LOCKED_UPDATE = new QualifiedNameImpl(DAV_URI, "locked-update"); //$NON-NLS-1$
	QualifiedName DAV_UNLOCKED_UPDATE = new QualifiedNameImpl(DAV_URI, "unlocked-update"); //$NON-NLS-1$

	// Fork control policy.
	QualifiedName DAV_OK = new QualifiedNameImpl(DAV_URI, "ok"); //$NON-NLS-1$
	QualifiedName DAV_DISCOURAGED = new QualifiedNameImpl(DAV_URI, "discouraged"); //$NON-NLS-1$
	QualifiedName DAV_FORBIDDEN = new QualifiedNameImpl(DAV_URI, "forbidden"); //$NON-NLS-1$
}
