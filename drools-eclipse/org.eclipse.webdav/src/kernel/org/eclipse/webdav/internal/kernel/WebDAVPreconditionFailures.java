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

public interface WebDAVPreconditionFailures extends WebDAVConstants {

	// The following precondition failures are considered 403 Forbidden.
	// The server understood the request, but is refusing to fulfill it.
	// Authorization will not help and the request SHOULD NOT be repeated. 
	QualifiedName DAV_ACTIVITY_LOCATION_OK = new QualifiedNameImpl(DAV_URI, "activity-location-ok"); //$NON-NLS-1$
	QualifiedName DAV_BASELINE_SELECTOR_MUST_BE_EMPTY = new QualifiedNameImpl(DAV_URI, "baseline-selector-must-be-empty"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_COPY_HISTORY = new QualifiedNameImpl(DAV_URI, "cannot-copy-history"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_CHECKED_IN_PARENT = new QualifiedNameImpl(DAV_URI, "cannot-modify-checked-in-parent"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_DESTINATION_CHECKED_IN_PARENT = new QualifiedNameImpl(DAV_URI, "cannot-modify-destination-checked-in-parent"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_PROTECTED_PROPERTY = new QualifiedNameImpl(DAV_URI, "cannot-modify-protected-property"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_UNSUPPORTED_PROPERTY = new QualifiedNameImpl(DAV_URI, "cannot-modify-unsupported-property"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_VERSION = new QualifiedNameImpl(DAV_URI, "cannot-modify-version"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_RENAME_RESOURCE = new QualifiedNameImpl(DAV_URI, "cannot-rename-resource"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_RENAME_VARIANT = new QualifiedNameImpl(DAV_URI, "cannot-rename-variant"); //$NON-NLS-1$
	QualifiedName DAV_CHECKIN_FORK_FORBIDDEN = new QualifiedNameImpl(DAV_URI, "checkin-fork-forbidden"); //$NON-NLS-1$
	QualifiedName DAV_CHECKOUT_NOT_ALLOWED = new QualifiedNameImpl(DAV_URI, "checkout-not-allowed"); //$NON-NLS-1$
	QualifiedName DAV_CHECKOUT_OF_CHECKED_OUT_VERSION_IS_FORBIDDEN = new QualifiedNameImpl(DAV_URI, "checkout-of-checked-out-version-is-forbidden"); //$NON-NLS-1$
	QualifiedName DAV_CHECKOUT_OF_VERSION_WITH_DESCENDANT_IS_FORBIDDEN = new QualifiedNameImpl(DAV_URI, "checkout-of-version-with-descendant-is-forbidden"); //$NON-NLS-1$
	QualifiedName DAV_DELETE_VARIANT_REFERENCE = new QualifiedNameImpl(DAV_URI, "delete-variant-reference"); //$NON-NLS-1$
	QualifiedName DAV_LINEAR_ACTIVITY = new QualifiedNameImpl(DAV_URI, "linear-activity"); //$NON-NLS-1$
	QualifiedName DAV_MUST_BE_ACTIVITY = new QualifiedNameImpl(DAV_URI, "must-be-activity"); //$NON-NLS-1$
	QualifiedName DAV_MUST_BE_BASELINE = new QualifiedNameImpl(DAV_URI, "must-be-baseline"); //$NON-NLS-1$
	QualifiedName DAV_MUST_BE_CHECKED_IN = new QualifiedNameImpl(DAV_URI, "must-be-checked-in"); //$NON-NLS-1$
	QualifiedName DAV_MUST_BE_CHECKED_IN_VERSION_CONTROLLED_RESOURCE = new QualifiedNameImpl(DAV_URI, "must-be-checked-in-version-controlled-resource"); //$NON-NLS-1$
	QualifiedName DAV_MUST_HAVE_NO_VERSION_CONTROLLED_MEMBERS = new QualifiedNameImpl(DAV_URI, "must-have-no-version-controlled-members"); //$NON-NLS-1$
	QualifiedName DAV_MUST_SELECT_VARIANT = new QualifiedNameImpl(DAV_URI, "must-select-variant"); //$NON-NLS-1$
	QualifiedName DAV_MUST_SELECT_VERSION = new QualifiedNameImpl(DAV_URI, "must-select-version"); //$NON-NLS-1$
	QualifiedName DAV_PREDECESSOR_IN_CHECKED_OUT_VERSION_HISTORY = new QualifiedNameImpl(DAV_URI, "predecessor-in-checked-out-version-history"); //$NON-NLS-1$
	QualifiedName DAV_WORKSPACE_REQUIRED = new QualifiedNameImpl(DAV_URI, "workspace-required"); //$NON-NLS-1$

	// The following precondition failures are considered 409 Conflict
	// The request could not be completed due to a conflict with the current
	// state of the resource. This code is only allowed in situations where
	// it is expected that the user might be able to resolve the conflict
	// and resubmit the request. 
	QualifiedName DAV_ATOMIC_ACTIVITY_CHECKIN = new QualifiedNameImpl(DAV_URI, "atomic-activity-check-in"); //$NON-NLS-1$
	QualifiedName DAV_BASELINE_CONTROLLED_MEMBERS_MUST_BE_CHECKED_IN = new QualifiedNameImpl(DAV_URI, "baseline-controlled-members-must-be-checked-in"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_ADD_TO_EXISTING_HISTORY = new QualifiedNameImpl(DAV_URI, "cannot-add-to-existing-history"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MERGE_CHECKED_OUT_RESOURCE = new QualifiedNameImpl(DAV_URI, "canot-merge-checked-out-resource"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_DESTINATION_PARENT_VERSION = new QualifiedNameImpl(DAV_URI, "cannot-modify-destination-parent-version"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_PARENT_VERSION = new QualifiedNameImpl(DAV_URI, "cannot-modify-parent-version"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_VERSION_CONTROLLED_CONTENT = new QualifiedNameImpl(DAV_URI, "cannot-modify-version-controlled-content"); //$NON-NLS-1$
	QualifiedName DAV_CANNOT_MODIFY_VERSION_CONTROLLED_PROPERTY = new QualifiedNameImpl(DAV_URI, "cannot-modify-version-controlled-property"); //$NON-NLS-1$
	QualifiedName DAV_CHECKIN_FORK_DISCOURAGED = new QualifiedNameImpl(DAV_URI, "checkin-fork-discouraged"); //$NON-NLS-1$
	QualifiedName DAV_CHECKOUT_OF_CHECKED_OUT_VERSION_IS_DISCOURAGED = new QualifiedNameImpl(DAV_URI, "checkout-of-checked-out-version-is-discouraged"); //$NON-NLS-1$
	QualifiedName DAV_CHECKOUT_OF_VERSION_WITH_DESCENDANT_IS_DISCOURAGED = new QualifiedNameImpl(DAV_URI, "checkout-of-version-with-descendant-is-discouraged"); //$NON-NLS-1$
	QualifiedName DAV_LABEL_MUST_EXIST = new QualifiedNameImpl(DAV_URI, "label-must-exist"); //$NON-NLS-1$
	QualifiedName DAV_MERGE_MUST_BE_COMPLETE = new QualifiedNameImpl(DAV_URI, "merge-must-be-complete"); //$NON-NLS-1$
	QualifiedName DAV_MUST_BE_NEW_LABEL = new QualifiedNameImpl(DAV_URI, "must-be-new-label"); //$NON-NLS-1$
	QualifiedName DAV_MUST_BE_VERSION = new QualifiedNameImpl(DAV_URI, "must-be-version"); //$NON-NLS-1$
	QualifiedName DAV_MUST_BE_CHECKED_OUT = new QualifiedNameImpl(DAV_URI, "must-be-checked-out"); //$NON-NLS-1$
	QualifiedName DAV_MUST_BE_CHECKED_OUT_VERSION_CONTROLLED_RESOURCE = new QualifiedNameImpl(DAV_URI, "must-be-checked-out-version-controlled-resource"); //$NON-NLS-1$
	QualifiedName DAV_MUST_NOT_BE_CHECKED_OUT = new QualifiedNameImpl(DAV_URI, "must-not-be-checked-out"); //$NON-NLS-1$
	QualifiedName DAV_NO_CHECKED_OUT_BASELINE_CONTROLLED_COLLECTION_MEMBERS = new QualifiedNameImpl(DAV_URI, "no-checked-out-baseline-controlled-collection-members"); //$NON-NLS-1$
	QualifiedName DAV_ONE_BASELINE_CONTROLLED_COLLECTION_PER_HISTORY_PER_WORKSPACE = new QualifiedNameImpl(DAV_URI, "one-baseline-controlled-collection-per-history-per-workspace"); //$NON-NLS-1$
	QualifiedName DAV_ONE_VERSION_CONTROLLED_RESOURCE_PER_HISTORY_PER_WORKSPACE = new QualifiedNameImpl(DAV_URI, "one-version-controlled-resource-per-history-per-workspace"); //$NON-NLS-1$
	QualifiedName DAV_ONE_CHECKOUT_PER_ACTIVITY_PER_HISTORY = new QualifiedNameImpl(DAV_URI, "one-checkout-per-activity-per-history"); //$NON-NLS-1$
	QualifiedName DAV_RESOURCE_MUST_BE_NULL = new QualifiedNameImpl(DAV_URI, "resource-must-be-null"); //$NON-NLS-1$

	// Postconditions
	/*	QualifiedName DAV_ACTIVITY_COLLECTION_SET_OK = new QualifiedNameImpl(DAV_URI, "activity-collection-set-ok");
	 QualifiedName DAV_ADD_LABEL = new QualifiedNameImpl(DAV_URI, "add-label");
	 QualifiedName DAV_ALREADY_UNDER_VERSION_CONTROL = new QualifiedNameImpl(DAV_URI, "already-under-version-control");
	 QualifiedName DAV_ANCESTOR_VERSION = new QualifiedNameImpl(DAV_URI, "ancestor-version");
	 QualifiedName DAV_AUTO_BASELINE = new QualifiedNameImpl(DAV_URI, "auto-baseline");
	 QualifiedName DAV_AUTO_CHECKOUT = new QualifiedNameImpl(DAV_URI, "auto-checkout");
	 QualifiedName DAV_AUTO_CHECKOUT_CHECKIN = new QualifiedNameImpl(DAV_URI, "auto-checkout-checkin");
	 QualifiedName DAV_BASELINE_CONTROLLED_COLLECTION_SET_OK = new QualifiedNameImpl(DAV_URI, "baseline-controlled-collection-set-ok");
	 QualifiedName DAV_BASELINE_VERSION_OK = new QualifiedNameImpl(DAV_URI, "baseline-version-ok");
	 QualifiedName DAV_BASELINES_FROM_SAME_HISTORY = new QualifiedNameImpl(DAV_URI, "baselines-from-same-history");
	 QualifiedName DAV_CANNOT_DELETE_REFERENCED_VERSION = new QualifiedNameImpl(DAV_URI, "cannot-delete-referenced-version");
	 QualifiedName DAV_CANNOT_DELETE_ROOT_VERSION = new QualifiedNameImpl(DAV_URI, "cannot-delete-root-version");
	 QualifiedName DAV_CHECKED_IN = new QualifiedNameImpl(DAV_URI, "checked-in");
	 QualifiedName DAV_CHECK_IN_ACTIVITY = new QualifiedNameImpl(DAV_URI, "check-in-activity");
	 QualifiedName DAV_CHECKED_OUT_FOR_MERGE = new QualifiedNameImpl(DAV_URI, "checked-out-for-merge");
	 QualifiedName DAV_CREATE_BASELINE_SELECTOR = new QualifiedNameImpl(DAV_URI, "create-baseline-selector");
	 QualifiedName DAV_CREATE_BASELINE_VERSION_SET = new QualifiedNameImpl(DAV_URI, "create-baseline-version-set");
	 QualifiedName DAV_CREATE_NEW_VARIANT = new QualifiedNameImpl(DAV_URI, "create-new-variant");
	 QualifiedName DAV_CREATE_VERSION = new QualifiedNameImpl(DAV_URI, "create-version");
	 QualifiedName DAV_DELETE_ACTIVITY_REFERENCE = new QualifiedNameImpl(DAV_URI, "delete-activity-reference");
	 QualifiedName DAV_DELETE_VARIANT_PREDECESSOR = new QualifiedNameImpl(DAV_URI, "delete-variant-predecessor");
	 QualifiedName DAV_DELETE_VERSION_SET = new QualifiedNameImpl(DAV_URI, "delete-version-set");
	 QualifiedName DAV_DELETE_VERSION_REFERENCE = new QualifiedNameImpl(DAV_URI, "delete-version-reference");
	 QualifiedName DAV_DELETE_WORKING_RESOURCE = new QualifiedNameImpl(DAV_URI, "delete-working-resource");
	 QualifiedName DAV_DESCENDANT_VERSION = new QualifiedNameImpl(DAV_URI, "descendant-version");
	 QualifiedName DAV_DEPTH_UPDATE = new QualifiedNameImpl(DAV_URI, "depth-update");
	 QualifiedName DAV_INITIALIZE_ACTIVITY_SET = new QualifiedNameImpl(DAV_URI, "initialize-activity-set");
	 QualifiedName DAV_INITIALIZE_COLLECTION_VERSION_BINDINGS = new QualifiedNameImpl(DAV_URI, "initialize-collection-version-bindings");
	 QualifiedName DAV_INITIALIZE_PRECURSOR = new QualifiedNameImpl(DAV_URI, "initialize-precursor");
	 QualifiedName DAV_INITIALIZE_PREDECESSOR_SET = new QualifiedNameImpl(DAV_URI, "initialize-predecessor-set");
	 QualifiedName DAV_INITIALIZE_UNRESERVED = new QualifiedNameImpl(DAV_URI, "initialize-unreserved");
	 QualifiedName DAV_INITIALIZE_VERSION_CONTENT_AND_PROPERTIES = new QualifiedNameImpl(DAV_URI, "initialize-version-content-and-properties");
	 QualifiedName DAV_INITIALIZE_WORKSPACE_PROPERTIES = new QualifiedNameImpl(DAV_URI, "initialize-workspace-properties");
	 QualifiedName DAV_IS_CHECKED_OUT = new QualifiedNameImpl(DAV_URI, "is-checked-out");
	 QualifiedName DAV_KEEP_CHECKED_OUT = new QualifiedNameImpl(DAV_URI, "keep-checked-out");
	 QualifiedName DAV_LATEST_ACTIVITY_VERSION_OK = new QualifiedNameImpl(DAV_URI, "latest-activity-version-ok");
	 QualifiedName DAV_MERGE_BASELINE = new QualifiedNameImpl(DAV_URI, "merge-baseline");
	 QualifiedName DAV_NO_VERSION_DELETE = new QualifiedNameImpl(DAV_URI, "no-version-delete");
	 QualifiedName DAV_NEW_VERSION_HISTORY = new QualifiedNameImpl(DAV_URI, "new-version-history");
	 QualifiedName DAV_NEW_VERSION_CONTROLLED_RESOURCE = new QualifiedNameImpl(DAV_URI, "new-version-controlled-resource");
	 QualifiedName DAV_PRESERVE_HISTORY = new QualifiedNameImpl(DAV_URI, "preserve-history");
	 QualifiedName DAV_PUT_UNDER_VERSION_CONTROL = new QualifiedNameImpl(DAV_URI, "put-under-version-control");
	 QualifiedName DAV_REMOVE_LABEL = new QualifiedNameImpl(DAV_URI, "remove-label");
	 QualifiedName DAV_SELECT_EXISTING_BASELINE = new QualifiedNameImpl(DAV_URI, "select-existing-baseline");
	 QualifiedName DAV_SET_BASELINE_CONTROLLED_COLLECTION_MEMBERS = new QualifiedNameImpl(DAV_URI, "set-baseline-controlled-collection-members");
	 QualifiedName DAV_UPDATE_ACTIVITY_REFERENCE = new QualifiedNameImpl(DAV_URI, "update-activity-reference");
	 QualifiedName DAV_UPDATE_ACTIVITY_VERSION_SET = new QualifiedNameImpl(DAV_URI, "update-activity-version-set");
	 QualifiedName DAV_UPDATE_CHECKED_IN_PROPERTY = new QualifiedNameImpl(DAV_URI, "update-checked-in-property");
	 QualifiedName DAV_UPDATE_CHECKED_OUT_REFERENCE = new QualifiedNameImpl(DAV_URI, "update-checked-out-reference");
	 QualifiedName DAV_UPDATE_CONTENT_AND_DEAD_PROPERTIES = new QualifiedNameImpl(DAV_URI, "update-content-and-dead-properties");
	 QualifiedName DAV_UPDATE_DEFAULT_VARIANT = new QualifiedNameImpl(DAV_URI, "update-default-variant");
	 QualifiedName DAV_UPDATE_MERGE_SET = new QualifiedNameImpl(DAV_URI, "update-merge-set");
	 QualifiedName DAV_UPDATE_PREDECESSOR_SET = new QualifiedNameImpl(DAV_URI, "update-predecessor-set");
	 QualifiedName DAV_UPDATE_VARIANT_CONTROLLED_RESOURCE = new QualifiedNameImpl(DAV_URI, "update-variant-controlled-resource");
	 QualifiedName DAV_UPDATE_VERSION_CONTROLLED_COLLECTION_MEMBERS = new QualifiedNameImpl(DAV_URI, "update-version-controlled-collection-members");
	 QualifiedName DAV_UPDATE_WORKSPACE_REFERENCE = new QualifiedNameImpl(DAV_URI, "update-workspace-reference");
	 QualifiedName DAV_VARIANT_CONTROL = new QualifiedNameImpl(DAV_URI, "variant-control");
	 QualifiedName DAV_VERSION_CONTROL_WORKING_COLLECTION_MEMBERS = new QualifiedNameImpl(DAV_URI, "version-control-working-collection-members");
	 QualifiedName DAV_VERSION_CONTROLLED_RESOURCE_URL_OK = new QualifiedNameImpl(DAV_URI, "version-controlled-resource-url-ok");
	 QualifiedName DAV_VERSION_HISTORY_COLLECTION_SET_OK = new QualifiedNameImpl(DAV_URI, "version-history-collection-set-ok");
	 QualifiedName DAV_VERSION_HISTORY_IS_TREE = new QualifiedNameImpl(DAV_URI, "version-history-is-tree");
	 QualifiedName DAV_VERSION_TREE_OK = new QualifiedNameImpl(DAV_URI, "version-tree-ok");
	 QualifiedName DAV_WORKSPACE_LOCATION_OK = new QualifiedNameImpl(DAV_URI, "workspace-location-ok");
	 QualifiedName DAV_WORKSPACE_MEMBER_MOVED = new QualifiedNameImpl(DAV_URI, "workspace-member-moved");
	 QualifiedName DAV_WORKSPACE_MOVED = new QualifiedNameImpl(DAV_URI, "workspace-moved");
	 */
}
