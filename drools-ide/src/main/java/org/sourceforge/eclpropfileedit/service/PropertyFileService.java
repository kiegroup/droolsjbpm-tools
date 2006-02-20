/* $RCSfile: PropertyFileService.java,v $
 * Created on 21.09.2002, 17:23:36 by Oliver David
 * $Source: /cvsroot/eclpropfileedit/eclpropfileedit/src/org/sourceforge/eclpropfileedit/service/PropertyFileService.java,v $
 * $Id: PropertyFileService.java,v 1.1 2002/10/25 15:18:05 davoli Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
package org.sourceforge.eclpropfileedit.service;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import org.sourceforge.eclpropfileedit.core.PropertyException;
import org.sourceforge.eclpropfileedit.io.PropertyFileHandler;

/** 
 * @author  Oliver
 * @version $Revision: 1.1 $
 */
public class PropertyFileService
{
    PropertyFileHandler i_fileHandler;

    /**
     * Constructor for PropertyFileService.
     */
    public PropertyFileService(File propertiesFile)
    {
        i_fileHandler = new PropertyFileHandler(propertiesFile);
    }
    
    /**
     * Method readPropertiesFile.
     * @return Collection
     */
    public Collection readPropertiesFile() throws PropertyException
    {
        return i_fileHandler.getPropertyLineWrappers();
    }
    
    /**
     * Method writeToPropertiesFile.
     * @param file
     */
    public void writeToPropertiesFile(File file, HashMap dataMap)
    {
        i_fileHandler.writeToPropertiesFile(file, dataMap);
    }
}
