/* $RCSfile: PropertyFileHandler.java,v $
 * Created on 19.09.2002, 21:01:06 by Oliver David
 * $Source: /cvsroot/eclpropfileedit/eclpropfileedit/src/org/sourceforge/eclpropfileedit/io/PropertyFileHandler.java,v $
 * $Id: PropertyFileHandler.java,v 1.3 2003/02/09 20:05:50 davoli Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
package org.sourceforge.eclpropfileedit.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.sourceforge.eclpropfileedit.core.PropertyConstants;
import org.sourceforge.eclpropfileedit.core.PropertyException;
import org.sourceforge.eclpropfileedit.core.PropertyFileUtil;
import org.sourceforge.eclpropfileedit.core.PropertyLineWrapper;


/** 
 * @author  Oliver
 * @version $Revision: 1.3 $
 */
public class PropertyFileHandler implements PropertyConstants
{
    private File i_file;
    private FileReader i_fileReader;
    private BufferedReader i_bufferedReader;
    
    private FileWriter i_fileWriter;
    private BufferedWriter i_bufferedWriter;
    

    /**
     * Constructor for PropertyFileHandler.
     */
    public PropertyFileHandler()
    {
    }

    /**
     * Constructor for PropertyFileHandler.
     */
    public PropertyFileHandler(File file)
    {
        i_file = file;
        initReader();
    }
    
    /**
     * Method initReader.
     */
    private void initReader()
    {
        try
        {
            i_fileReader = new FileReader(i_file);
        }
        catch (FileNotFoundException e)
        {
            // implement Exception Handling later!
        }
        i_bufferedReader = new BufferedReader(i_fileReader);
    }        
      
    
    /**
     * Method initWriter.
     */
    private void initWriter()
    {
        try
        {
            //i_fileWriter = new FileWriter(i_file, false);
            i_fileWriter = new FileWriter(i_file.getAbsolutePath(), false);
        }
        catch (IOException e)
        {
            // implement Exception Handling later!
        }
        i_bufferedWriter = new BufferedWriter(i_fileWriter);   
    }  
    
    /**
     * Method readLine.
     * @return String
     */
    private String readLine()
    {
        try
        {
            return i_bufferedReader.readLine();
        }
        catch (IOException e)
        {
            // implement Exception Handling later!
        }
        return null;        
    }
    
    /**
     * Method getPropertyLineWrappers.
     * @return ArrayList
     */
    public ArrayList getPropertyLineWrappers() throws PropertyException
    {        
        ArrayList propertiesResults = new ArrayList();
        String lineString = null;
        
        ArrayList propertiesFileLines = new ArrayList();
        int lineNumber = 0;
        while(true)
        {
            lineString = readLine();
            propertiesFileLines.add(lineNumber++, lineString);
            if(lineString == null)
            {
                break;
            }
        }
                
        try
        {
            i_bufferedReader.close();
        }
        catch (IOException e)
        {
            // implement Exception Handling later!
        }
        
        i_bufferedReader = null;                
        
        Iterator iter = propertiesFileLines.iterator();
        lineNumber = 0;
        while(iter.hasNext())
        {
        	String keyValuePair = (String)iter.next();
            if(keyValuePair != null)
            {
                keyValuePair = keyValuePair.trim();
            }
            lineNumber++;
            if(PropertyLineWrapper.isKeyValuePairValid(keyValuePair))
            {
                PropertyLineWrapper propertiesLineWrapper = new PropertyLineWrapper(keyValuePair, null);
                
                String comment = null;
                if(lineNumber >= 2)
                {
                    //*****************************************************************
                    comment = (String)propertiesFileLines.get(lineNumber - 2);
                    if(comment != null)
                    {
                        comment = comment.trim();
                    }
                }             
                
                if(PropertyLineWrapper.isCommentValid(comment))
                {
                    propertiesLineWrapper.setComment(comment);
                }
                
                // use trim() to avoid errors on blank space lines
                if(comment != null && !comment.trim().equals("") && !PropertyLineWrapper.isValid(comment))
                {
                    throw new PropertyException(getErrorMessage(comment));
                }
                propertiesResults.add(propertiesLineWrapper);
            }
            
            // use trim() to avoid errors on blank space lines
            if(keyValuePair != null && !keyValuePair.trim().equals("") && !PropertyLineWrapper.isValid(keyValuePair))
            {
                throw new PropertyException(getErrorMessage(keyValuePair));
            }
        }
        
        Collections.sort(propertiesResults);
   
        return propertiesResults;
    }
    
    /**
     * Method writeToPropertiesFile.
     * @param file
     */
    public void writeToPropertiesFile(File file, HashMap dataMap)
    {
        
        i_file = file;  
        
        initWriter();
        
        Iterator iter = PropertyFileUtil.getSortedIterator(dataMap.values());
        
        try
        {
            while(iter.hasNext())
            {
            	PropertyLineWrapper element = (PropertyLineWrapper)iter.next();
                
                String comment = element.getComment();
                String keyValuePair = element.getKeyValuePair();
                if(comment != null && !comment.equals(PropertyLineWrapper.COMMENT_PREFIX) && !comment.equals(""))
                {
                	i_bufferedWriter.write(comment);
                    i_bufferedWriter.newLine();
                }
                if(element.isCommentedProperty())
                {
                    keyValuePair = COMMENT_PREFIX + keyValuePair;
                }
                i_bufferedWriter.write(keyValuePair);
                i_bufferedWriter.newLine();
                i_bufferedWriter.newLine();
            }
        
            i_bufferedWriter.close();
        }
        catch (IOException e)
        {
            // implement Exception Handling later!
        }
        i_bufferedWriter = null;
    }
    
    /**
     * Method getErrorMessage.
     * @param textLine
     * @return String
     */
    private String getErrorMessage(String textLine)
    {
        return "There is an invalid text line \"" + textLine + "\" in this .properties file. Please close this editor part and check this file in a plain text editor.";
    }
}