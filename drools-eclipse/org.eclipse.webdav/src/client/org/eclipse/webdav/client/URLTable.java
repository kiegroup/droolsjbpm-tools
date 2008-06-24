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
package org.eclipse.webdav.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * A <code>URLTable</code> is a simple hashtable whose keys are
 * <code>URL</code>s. A <code>URL<code> key with a trailing slash is
 * considered by the table to be equal to the same <code>URL</code>
 * without a trailing slash.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class URLTable {

	private Hashtable table;

	/**
	 * A <code>URLKey</code> is an equality wrapper class for
	 * <code>URL</code>s. The wrapper treats <code>URL</code>s with and
	 * without a trailing slash as equal. The <code>equals</code> method
	 * works with <code>URLKey</code>s, <code>URL</code>s, and
	 * <code>String</code>s.
	 * <p>
	 * <b>Note:</b> This class/interface is part of an interim API that is still under 
	 * development and expected to change significantly before reaching stability. 
	 * It is being made available at this early stage to solicit feedback from pioneering 
	 * adopters on the understanding that any code that uses this API will almost 
	 * certainly be broken (repeatedly) as the API evolves.
	 * </p>
	 */
	class URLKey {
		URL url;
		int hashCode = -1;

		/**
		 * Creates a new <code>URLKey</code> from the given <code>URL</code>.
		 *
		 * @param url the <code>URL</code> to wrap
		 */
		public URLKey(URL url) {
			this.url = url;
		}

		/**
		 * Returns <code>true</code> if this <code>URLKey</code> is equal to the
		 * given object. Returns <code>false</code> otherwise. The object may be
		 * a <code>URLKey<code>, <code>String</code>, or <code>URL</code>.
		 *
		 * @param obj the object to compare with this <code>URLKey</code>
		 * @return    a boolean indicating whether or not this
		 *            <code>URLKey</code> is equal to the given object
		 */
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (this == obj)
				return true;
			if (obj instanceof URLKey)
				return equals(((URLKey) obj).getURL());
			if (obj instanceof String) {
				try {
					return equals(new URL((String) obj));
				} catch (MalformedURLException e) {
					return false;
				}
			}
			if (!(obj instanceof URL))
				return false;
			if (url == (URL) obj)
				return true;
			URL url1 = URLTool.removeTrailingSlash(url);
			URL url2 = URLTool.removeTrailingSlash((URL) obj);
			return url1.equals(url2);
		}

		/**
		 * Returns the <code>URL</code> that this <code>URLKey</code> wraps.
		 *
		 * @return the <code>URL</code> that this <code>URLKey</code> wraps
		 */
		public URL getURL() {
			return url;
		}

		/**
		 * Returns an integer suitable for indexing this <code>URLKey</code> in
		 * a hash table.
		 *
		 * @return an integer suitable for hash table indexing
		 */
		public int hashCode() {
			if (hashCode == -1)
				hashCode = URLTool.removeTrailingSlash(url).hashCode();
			return hashCode;
		}

		/**
		 * Returns this <code>URLKey</code>s <code>URL</code> as a
		 * <code>String</code>.
		 *
		 * @return a string
		 */
		public String toString() {
			return url.toString();
		}
	} // end-class URLKey

	/**
	 * Construct an empty <code>URLTable</code>.
	 */
	public URLTable() {
		table = new Hashtable();
	}

	/**
	 * Construct an empty <code>URLTable</code> with the given size.
	 */
	public URLTable(int size) {
		table = new Hashtable(size);
	}

	/**
	 * Returns the value to which the given URL is mapped to in the table. If
	 * the given URL not mapped to any value, or is malformed, returns
	 * <code>null</code>.
	 *
	 * @param url a URL as a <code>String</code>
	 * @return    the value to which the given URL is mapped to in the table,
	 *            or <code>null</code>
	 * @throws    MalformedURLException if the given URL is malformed
	 */
	public Object get(String url) throws MalformedURLException {
		Assert.isNotNull(url);
		return get(new URL(url));
	}

	/**
	 * Returns the value to which the given <code>URL</code> is mapped to in
	 * the table. If the given <code>URL</code> not mapped to any value,
	 * returns <code>null</code>.
	 *
	 * @param url a <code>URL</code>
	 * @return    the value to which the given <code>URL</code> is mapped to
	 *            in the table, or <code>null</code>
	 */
	public Object get(URL url) {
		Assert.isNotNull(url);
		return get(new URLKey(url));
	}

	/**
	 * Returns the value to which the specified URL is mapped to in the
	 * table. If the specified URL is not mapped to any value, returns
	 * <code>null</code>.
	 *
	 * @param url a <code>URLKey</code>
	 * @return    the value to which the specified URL is mapped to in the
	 *            table, or <code>null</code>
	 */
	private Object get(URLKey url) {
		Assert.isNotNull(url);
		return table.get(url);
	}

	/**
	 * Returns an <code>Enumeration</code> over the keys in this
	 * <code>URLTable</code>.
	 *
	 * @return an <code>Enumeration</code> over <code>URL</code>s
	 */
	public Enumeration keys() {
		final Enumeration keys = table.keys();
		Enumeration e = new Enumeration() {
			public boolean hasMoreElements() {
				return keys.hasMoreElements();
			}

			public Object nextElement() {
				return ((URLKey) keys.nextElement()).getURL();
			}
		};
		return e;
	}

	/**
	 * Maps the given URL to the given value in this table.
	 *
	 * @param url   a URL as a <code>String</code>
	 * @param value an object
	 * @throws      MalformedURLException if the given URL is malformed
	 */
	public void put(String url, Object value) throws MalformedURLException {
		Assert.isNotNull(url);
		Assert.isNotNull(value);
		put(new URL(url), value);
	}

	/**
	 * Maps the given <code>URL</code> to the given value in this table.
	 *
	 * @param url   a <code>URL</code>
	 * @param value an object
	 */
	public void put(URL url, Object value) {
		Assert.isNotNull(url);
		Assert.isNotNull(value);
		put(new URLKey(url), value);
	}

	/**
	 * Maps the specified URL to the given value in this table.
	 *
	 * @param url   a <code>URLKey</code>
	 * @param value an object
	 */
	private void put(URLKey url, Object value) {
		Assert.isNotNull(url);
		Assert.isNotNull(value);
		// Remove the old entry so the url key is replaced
		if (table.get(url) != null)
			table.remove(url);
		table.put(url, value);
	}

	public void remove(String url) throws MalformedURLException {
		Assert.isNotNull(url);
		remove(new URL(url));
	}

	public void remove(URL url) {
		Assert.isNotNull(url);
		remove(new URLKey(url));
	}

	private void remove(URLKey url) {
		Assert.isNotNull(url);
		table.remove(url);
	}
}
