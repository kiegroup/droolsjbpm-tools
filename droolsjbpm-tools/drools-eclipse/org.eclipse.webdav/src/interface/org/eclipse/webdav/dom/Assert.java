/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.webdav.dom;

import org.eclipse.webdav.Policy;

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
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
/* package */class Assert {
	/** Asserts that the given object is not <code>null</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 */
	public static void isNotNull(Object o) {
		isNotNull(o, ""); //$NON-NLS-1$
	}

	/** Asserts that the given object is not <code>null</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 * The given message is included in that exception, to aid debugging.
	 */
	public static void isNotNull(Object o, String message) {
		if (o == null)
			throw new AssertionFailedException(Policy.bind("assert.nullArgument", message)); //$NON-NLS-1$
	}

	/** Asserts that the given boolean is <code>true</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 */
	public static boolean isTrue(boolean expression) {
		return isTrue(expression, ""); //$NON-NLS-1$
	}

	/** Asserts that the given boolean is <code>true</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 * The given message is included in that exception, to aid debugging.
	 */
	public static boolean isTrue(boolean expression, String message) {
		if (!expression)
			throw new AssertionFailedException(Policy.bind("assert.failed", message)); //$NON-NLS-1$
		return expression;
	}
}
