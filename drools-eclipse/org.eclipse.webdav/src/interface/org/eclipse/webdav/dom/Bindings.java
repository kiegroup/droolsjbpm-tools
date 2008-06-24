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
package org.eclipse.webdav.dom;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An element editor for the WebDAV bindings element. See INTERNET DRAFT
 * draft-ietf-webdav-binding-protocol-02 section 13.1 for the element's
 * definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class Bindings extends Property {
	/**
	 * An ordered collection of the element names of the bindings
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"href", "segment"}; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * This class represent a single binding.
	 *
	 * @see Bindings#getBindings()
	 */
	public class Binding {
		private String fHref = null;
		private String fSegment = null;

		/**
		 * Construct a new binding with the given href and segment.  The
		 * href and segment must not be null.
		 *
		 * @param href    the binding's href
		 * @param segment the binding's segment
		 */
		public Binding(String href, String segment) {
			Assert.isNotNull(href);
			Assert.isNotNull(segment);
			fHref = href;
			fSegment = segment;
		}

		/**
		 * Return this binding's href.
		 */
		public String getHref() {
			return fHref;
		}

		/**
		 * Return this binding's segment.
		 */
		public String getSegment() {
			return fSegment;
		}
	}

	/**
	 * Creates a new editor on the given WebDAV bindings element. The
	 * element is assumed to be well formed.
	 *
	 * @param root bindings element
	 * @throws      MalformedElementException if there is reason to
	 *              believe that the element is not well formed
	 */
	public Bindings(Element root) throws MalformedElementException {
		super(root, "bindings"); //$NON-NLS-1$
	}

	/**
	 * Adds the specified binding to this editor's bindings element. The
	 * given href and segment must not be <code>null</code>.
	 *
	 * @param href the href part of the binding to add
	 * @param segment the segment part of the binding to add
	 */
	public void addBinding(String href, String segment) {
		Assert.isNotNull(href);
		Assert.isNotNull(segment);
		appendChild(root, "href", encodeHref(href)); //$NON-NLS-1$
		appendChild(root, "segment", segment); //$NON-NLS-1$
	}

	/**
	 * Returns an <code>Enumeration</code> over this bindings
	 * <code>Binding</code>s.
	 *
	 * @return an <code>Enumeration</code> of <code>Bindings.Binding</code>s
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public Enumeration getBindings() throws MalformedElementException {

		final Node firstHref = getFirstChild(root, "href"); //$NON-NLS-1$
		Node segment = null;
		if (firstHref != null)
			segment = getNextSibling((Element) firstHref, "segment"); //$NON-NLS-1$

		final Node firstSegment = segment;

		Enumeration e = new Enumeration() {
			Node fCurrentHref = firstHref;
			Node fCurrentSegment = firstSegment;

			public boolean hasMoreElements() {

				return fCurrentHref != null && fCurrentSegment != null;
			}

			public Object nextElement() {

				if (!hasMoreElements())
					throw new NoSuchElementException();

				String nextHref = getFirstText((Element) fCurrentHref);
				String nextSegment = getFirstText((Element) fCurrentSegment);
				Binding nextBinding = new Binding(decodeHref(nextHref), nextSegment);

				fCurrentHref = getNextSibling((Element) fCurrentSegment, "href"); //$NON-NLS-1$
				fCurrentSegment = null;
				if (fCurrentHref != null)
					fCurrentSegment = getNextSibling((Element) fCurrentHref, "segment"); //$NON-NLS-1$

				return nextBinding;
			}
		};

		return e;
	}
}
