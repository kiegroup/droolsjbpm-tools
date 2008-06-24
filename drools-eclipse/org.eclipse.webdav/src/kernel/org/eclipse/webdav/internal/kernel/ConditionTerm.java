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
import java.util.Enumeration;
import java.util.Vector;
import org.eclipse.webdav.IResponse;

/** 
 * A ConditionTerm represents some state configuration of a resource that must be
 * satisfied in order for the associated request to be valid. The ConditionFactors in
 * a ConditionTerm must all match with states of the resource, i.e., they are AND'ed
 * together. ConditionTerms are contained in a Condition which is used in the Precondition
 * of a WebDAV If header.
 */
public class ConditionTerm {

	private Vector conditionFactors = new Vector();

	/** 
	 * Construct a Condition with no associated Resource URI.
	 */
	public ConditionTerm() {
		super();
	}

	/**
	 * Add a ConditionFactor to a ConditionTerm.
	 *
	 * @param factor the factor to add
	 * @exception WebDAVException thrown if the term already contains the factor
	 */
	public void addConditionFactor(ConditionFactor factor) throws WebDAVException {
		if (conditionFactors.contains(factor))
			throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseDuplicateEntry")); //$NON-NLS-1$
		conditionFactors.addElement(factor);
	}

	/** 
	 * Does this ConditionTerm contain the given ConditionFactor?
	 *
	 * @param factor the factor to check for
	 * @return true if the term contains the given factor
	 */
	public boolean contains(ConditionFactor factor) {
		return conditionFactors.contains(factor);
	}

	/** 
	 * Create a ConditionTerm by parsing the given If header as defined by
	 * section 9.4 in the WebDAV spec.
	 *
	 * @param tokenizer a StreamTokenizer on the contents of a WebDAV If header
	 * @return the parsed ConditionTerm
	 * @exception WebDAVException thrown if there is a syntax error in the If header
	 */
	public static ConditionTerm create(StreamTokenizer tokenizer) throws WebDAVException {
		ConditionTerm term = new ConditionTerm();
		try {
			int token = tokenizer.ttype;
			if (token == '(')
				token = tokenizer.nextToken();
			else
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissing", String.valueOf(token), "(")); //$NON-NLS-1$ //$NON-NLS-2$

			while (token == StreamTokenizer.TT_WORD || token == '<' || token == '[') {
				term.addConditionFactor(ConditionFactor.create(tokenizer));
				token = tokenizer.ttype;
			}
			if (token == ')')
				token = tokenizer.nextToken();
			else
				throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissing", String.valueOf(token), ")")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException exc) {
			// ignore or log?
		}
		if (!term.getConditionFactors().hasMoreElements())
			throw new WebDAVException(IResponse.SC_BAD_REQUEST, Policy.bind("error.parseMissingStateOrEntity")); //$NON-NLS-1$
		return term;
	}

	/** 
	 * Get all the ConditionFactors in this Condition. The ConditionFactors in
	 * a Condition must all match with states of the resource, i.e., they are AND'ed
	 * together. ConditionTerms are contained in a Condition which is used in the
	 * Precondition of a WebDAV If header.
	 *
	 * @return an Enumeration of ConditionFactors
	 */
	public Enumeration getConditionFactors() {
		return conditionFactors.elements();
	}

	/** 
	 * See if this ConditionTerm matches the given ConditionTerm. This is an
	 * AND operation. All the factors in the ConditionTerm must match.
	 *
	 * @param conditionTerm the term to match
	 * @return true if all the factors in the term match those in this term
	 */
	public boolean matches(ConditionTerm conditionTerm) {
		int numberOfItemsToMatch = 0;
		boolean match = true;
		Enumeration factors = getConditionFactors();
		while (match && factors.hasMoreElements()) {
			ConditionFactor factor = (ConditionFactor) factors.nextElement();
			if (factor.not()) {
				match = !conditionTerm.contains(factor);
			} else {
				match = conditionTerm.contains(factor);
				numberOfItemsToMatch++;
			}
		}
		match = match && numberOfItemsToMatch == conditionTerm.numberOfFactors();
		return match;
	}

	/** 
	 * Get the number of ConditionFactors in this ConditionTerm.
	 *
	 * @return the number of factors in this term
	 */
	public int numberOfFactors() {
		return conditionFactors.size();
	}

	/** 
	 * Return a String representation of this ConditionTerm as defined by section 9.4
	 * of the WebDAV Spec.
	 *
	 * @return a string representation of this term
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('(');
		Enumeration factors = getConditionFactors();
		while (factors.hasMoreElements()) {
			ConditionFactor factor = (ConditionFactor) factors.nextElement();
			buffer.append(factor.toString());
			if (factors.hasMoreElements())
				buffer.append(' ');
		}
		buffer.append(')');
		return buffer.toString();
	}
}
