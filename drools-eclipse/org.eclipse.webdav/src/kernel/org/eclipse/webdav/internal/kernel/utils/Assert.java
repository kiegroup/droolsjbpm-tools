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
package org.eclipse.webdav.internal.kernel.utils;

import org.eclipse.webdav.internal.kernel.Policy;

/**
 * <code>Assert</code> is useful for for embedding runtime sanity checks
 * in code.
 * The predicate methods all test a condition and throw some
 * type of unchecked exception if the condition does not hold.
 * <p>
 * Assertion failure exceptions, like most runtime exceptions, are
 * thrown when something is misbehaving. Assertion failures are invariably
 * unspecified behavior; consequently, clients should never rely on
 * these being thrown (and certainly should not being catching them
 * specifically).
 * 
 */
public class Assert {
	/** Block instantiation. */
	private Assert() {
		super();
	}

	/** Asserts that an argument is legal. If the given boolean is
	 * not <code>true</code>, an <code>IllegalArgumentException</code>
	 * is thrown.
	 *
	 * @exception IllegalArgumentException Thrown if the legality test failed
	 */
	static public boolean isLegal(boolean expression) {
		return isLegal(expression, ""); //$NON-NLS-1$
	}

	/** Asserts that an argument is legal. If the given boolean is
	 * not <code>true</code>, an <code>IllegalArgumentException</code>
	 * is thrown.
	 * The given message is included in that exception, to aid debugging.
	 *
	 * @exception IllegalArgumentException Thrown if the legality test failed
	 */
	static public boolean isLegal(boolean expression, String message) {
		if (!expression)
			throw new IllegalArgumentException(message);
		return expression;
	}

	/** Asserts that the given object is not <code>null</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 */
	static public void isNotNull(Object o) {
		isNotNull(o, ""); //$NON-NLS-1$
	}

	/** Asserts that the given object is not <code>null</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 * The given message is included in that exception, to aid debugging.
	 */
	static public void isNotNull(Object o, String message) {
		if (o == null)
			throw new AssertionFailedException(Policy.bind("assert.null", message)); //$NON-NLS-1$
	}

	/** Asserts that the given boolean is <code>true</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 */
	static public boolean isTrue(boolean expression) {
		return isTrue(expression, ""); //$NON-NLS-1$
	}

	/** Asserts that the given boolean is <code>true</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 * The given message is included in that exception, to aid debugging.
	 */
	static public boolean isTrue(boolean expression, String message) {
		if (!expression)
			throw new AssertionFailedException(Policy.bind("assert.fail", message)); //$NON-NLS-1$
		return expression;
	}
}
