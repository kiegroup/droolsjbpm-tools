/* $RCSfile: PropertyFileUtil.java,v $
 * Created on 28.09.2002, 19:54:20 by Oliver David
 * $Source: /cvsroot/eclpropfileedit/eclpropfileedit/src/org/sourceforge/eclpropfileedit/core/PropertyFileUtil.java,v $
 * $Id: PropertyFileUtil.java,v 1.1 2002/10/25 15:18:06 davoli Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
package org.sourceforge.eclpropfileedit.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/** 
 * @author  Oliver
 * @version $Revision: 1.1 $
 */
public class PropertyFileUtil
{

    /**
     * Constructor for PropertyFileUtil.
     */
    public PropertyFileUtil()
    {
        super();
    }
    
    public static Iterator getSortedIterator(Collection data)
    {
        Iterator iter = data.iterator();
        ArrayList sortedValues = new ArrayList();
        
        while(iter.hasNext())
        {
            PropertyLineWrapper element = (PropertyLineWrapper)iter.next();
            sortedValues.add(element);
        }
        
        // now sort the ArrayList
        Collections.sort(sortedValues);
        
        return iter = sortedValues.iterator();
    }
}