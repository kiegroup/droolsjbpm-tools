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
 * A ConditionFactor represents some state of a resource that must be
 * satisfied in order for the associated request to be valid. The ConditionFactors in
 * a ConditionTerm must all match with states of the resource, i.e., they are AND'ed
 * together. Conditions are contained in a Precondition which is used in a
 * WebDAV If header. ConditionFactors are either constructed by the client, or may
 * have been given to the client in a previous method request. A ConditionFactor can
 * be either a StateToken or an EntityTag as defined by section 9.4 of the WebDAV
 * spec.
 */
public abstract class ConditionFactor {

	private boolean not = false;

	/** 
	 * Create a ConditionFactor (either a StateToken or EntityTag) by parsing
	 * the tokenizer contining an If header value.
	 *
	 * @param tokenizer a StreamTokenizer containing the contents of a state token or entity tag
	 *    from a WebDAV If header
	 * @return the parsed ConditionFactor
	 * @exception WebDAVException thrown if there is a syntax error in the If header
	 */
	public static ConditionFactor create(StreamTokenizer tokenizer) throws WebDAVException {
		boolean not = false;
		ConditionFactor factor = null;
		try {
			int token = tokenizer.ttype;
			if (token == StreamTokenizer.TT_WORD) {
				if (tokenizer.sval.equalsIgnoreCase("Not")) { //$NON-NLS-1$
					not = true;
				} else {
					throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissingNot")); //$NON-NLS-1$
				}
				token = tokenizer.nextToken();
			}
			switch (token) {
				case '<' :
					factor = StateToken.create(tokenizer);
					break;
				case '[' :
					factor = EntityTag.create(tokenizer);
					break;
				default :
					throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissingOpen", String.valueOf(token))); //$NON-NLS-1$
			}
		} catch (IOException exc) {
			// ignore or log?
		}
		factor.setNot(not);
		return factor;
	}

	/** 
	 * Negate the comparison on this ConditionFactor?
	 *
	 * @return true if the condition factor was negated in the If header
	 */
	public boolean not() {
		return not;
	}

	/** 
	 * Set how to compare to this ConditionFactor. Value is true implies match for
	 * a valid request, false implies the request is valid only if the ConditionFactor
	 * doesn't match.
	 * 
	 * @param value true means negate the condition
	 */
	public void setNot(boolean value) {
		not = value;
	}

	/**
	 * Return a String representation of this ConditionFactor as defined by the If
	 * header in section 9.4 of the WebDAV spec.
	 * 
	 * @return a string representation of a state token or entity tag
	 */
	public abstract String toString();
}
