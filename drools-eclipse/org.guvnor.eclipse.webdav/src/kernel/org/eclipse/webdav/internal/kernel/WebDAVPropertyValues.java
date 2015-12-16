/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
