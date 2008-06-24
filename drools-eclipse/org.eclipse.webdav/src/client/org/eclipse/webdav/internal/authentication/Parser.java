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
package org.eclipse.webdav.internal.authentication;

/**
 * A simple parser that is mainly used to parse HTTP header fields.
 */
public class Parser {
	/**
	 * The string being parsed.
	 */
	public String s;

	/**
	 * The index of the next character in s to be parsed.
	 */
	public int pos;

	/**
	 * Creates a new parser on the given string.
	 *
	 * @param s the string to be parsed
	 */
	public Parser(String s) {
		this.s = s;
	}

	/**
	 * Throws a <code>ParserException</code> if <code>pos</code> is out of
	 * range.
	 */
	public void checkPosition() throws ParserException {
		if (pos < 0 || pos >= s.length())
			throw new ParserException();
	}

	/**
	 * Skips the next character in s if it matches c, otherwise a
	 * <code>ParserException</code> is thrown.
	 *
	 * @param c
	 * @throws  ParserException
	 */
	public void match(char c) throws ParserException {
		checkPosition();
		if (s.charAt(pos) != c)
			throw new ParserException();
		++pos;
	}

	/**
	 * Returns the next quoted string is s (quotes included). Throws a
	 * <code>ParserException</code> if the next substring in s is not a
	 * quoted string.
	 *
	 * @return the next quoted string in s (quotes included)
	 * @throws ParserException
	 */
	public String nextQuotedString() throws ParserException {
		int start = pos;
		match('"');

		checkPosition();
		while (s.charAt(pos) != '"') {
			++pos;
			checkPosition();
		}
		match('"');

		return s.substring(start, pos);
	}

	/**
	 * Returns the next token in s. Throws a <code>ParserException</code> if
	 * the next substring in s is not a token.
	 *
	 * @return the next token in s
	 * @throws ParserException
	 */
	public String nextToken() throws ParserException {
		int start = pos;

		checkPosition();
		boolean done = false;

		while (!done && pos < s.length()) {
			int c = s.charAt(pos);
			if (c <= 31 //
					|| c == 127 //
					|| c == '(' //
					|| c == ')' //
					|| c == '<' //
					|| c == '>' //
					|| c == '@' //
					|| c == ',' //
					|| c == ';' //
					|| c == ':' //
					|| c == '\\' //
					|| c == '"' //
					|| c == '/' //
					|| c == '[' //
					|| c == ']' //
					|| c == '?' //
					|| c == '=' //
					|| c == '{' //
					|| c == '}' //
					|| Character.isWhitespace((char) c)) {
				done = true;
			} else {
				++pos;
			}
		}

		if (start == pos) {
			throw new ParserException();
		}

		return s.substring(start, pos);
	}

	/**
	 * Skips the next sequence of white space in s. An exception is not
	 * thrown if there is no matching white space.
	 */
	public void skipWhiteSpace() {
		while (pos < s.length() && Character.isWhitespace(s.charAt(pos)))
			++pos;
	}
}
