package org.eclipse.webdav.dom;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 */
public interface QualifiedName {
    /**
     * Returns the local part of this name.
     * The local part cannot be <code>null</code>.
     *
     * @return the local name.
     */
    public String getLocalName();

    /**
     * Returns the qualifier part for this qualifed name,
     * or <code>null</code> if none.
     * <p>
     * Note that the qualifier is a URI i.e. it follows
     * the syntax of RFC1630.</p>
     *
     * @return the name qualifier as a <code>String</code> URI
     * or <code>null</code> if there is no qualifier.
     */
    public String getQualifier();
}
