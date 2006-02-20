/* $RCSfile: PropertyException.java,v $
 * Created on 16.10.2002, 20:55:02 by Oliver David
 * $Source: /cvsroot/eclpropfileedit/eclpropfileedit/src/org/sourceforge/eclpropfileedit/core/PropertyException.java,v $
 * $Id: PropertyException.java,v 1.2 2003/02/09 20:05:50 davoli Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
package org.sourceforge.eclpropfileedit.core;

/** 
 * @author  Oliver
 * @version $Revision: 1.2 $
 */
public class PropertyException extends Exception
{

    /**
     * Constructor for PropertyException.
     */
    public PropertyException()
    {
        super();
    }

    /**
     * Constructor for PropertyException.
     * @param arg0
     */
    public PropertyException(String message)
    {
        super(message);
    }

    /**
     * Constructor for PropertyException.
     * @param arg0
     * @param arg1
     */
//    public PropertyException(String message, Throwable throwable)
//    {
//        super(message, throwable);
//    }

    /**
     * Constructor for PropertyException.
     * @param arg0
     */
//   public PropertyException(Throwable throwable)
//    {
//        super(throwable);
//    }
}
