/* $RCSfile: PropertyLineWrapper.java,v $
 * Created on 19.09.2002, 21:02:57 by Oliver David
 * $Source: /cvsroot/eclpropfileedit/eclpropfileedit/src/org/sourceforge/eclpropfileedit/core/PropertyLineWrapper.java,v $
 * $Id: PropertyLineWrapper.java,v 1.2 2002/10/27 15:49:19 davoli Exp $
 * Copyright (c) 2000-2002 Oliver David. All rights reserved. */
package org.sourceforge.eclpropfileedit.core;

/** 
 * @author  Oliver
 * @version $Revision: 1.2 $
 */
public class PropertyLineWrapper implements Comparable, PropertyConstants
{    
    private String i_keyValuePair = "";
    private String i_comment = "";
    private boolean i_isCommentedProperty;
    private int i_indexKVS;

    /**
     * Constructor for PropertyLineWrapper.
     */
    public PropertyLineWrapper()
    {
    }

    /**
     * Constructor for PropertyLineWrapper.
     * @param keyValuePair
     * @param comment
     */
    public PropertyLineWrapper(String keyValuePair, String comment)
    {
        i_indexKVS = keyValuePair.indexOf(KEY_VALUE_SEPARATOR);
        if(keyValuePair.startsWith(COMMENT_PREFIX))
        {
            i_keyValuePair = getTrimmedKeyValuePair(keyValuePair.substring(1));
            i_isCommentedProperty = true;
        }
        else
        {
            i_keyValuePair = getTrimmedKeyValuePair(keyValuePair);
            i_isCommentedProperty = false;
        }
        i_comment = comment != null ? comment.trim() : "";        
    }

    /**
     * Constructor for PropertyLineWrapper.
     * @param keyValuePair
     * @param comment
     */
    public PropertyLineWrapper(String keyValuePair, String comment, boolean isCommentedPoperty)
    {
        i_indexKVS = keyValuePair.indexOf(KEY_VALUE_SEPARATOR);
        if(isCommentedPoperty)
        {
            if(keyValuePair.startsWith(COMMENT_PREFIX))
            {
                i_keyValuePair = getTrimmedKeyValuePair(keyValuePair.substring(1));
                i_isCommentedProperty = true;
            }
            else
            {
                i_keyValuePair = getTrimmedKeyValuePair(keyValuePair);
                i_isCommentedProperty = true;
            }
        }
        else
        {
            if(keyValuePair.startsWith(COMMENT_PREFIX))
            {
                i_keyValuePair = getTrimmedKeyValuePair(keyValuePair.substring(1));
                i_isCommentedProperty = true;
            }
            else
            {
                i_keyValuePair = getTrimmedKeyValuePair(keyValuePair);
                i_isCommentedProperty = false;
            }
        }
        i_comment = comment != null ? comment.trim() : "";                
    }

    /**
     * Returns the comment.
     * @return String
     */
    public String getCommentString()
    {
        if(i_comment != null && !i_comment.equals(""))
        {
            return i_comment.substring(1);
        }
        return "";
    }

    /**
     * Returns the key.
     * @return String
     */
    public String getKeyString()
    {
        return i_keyValuePair.substring(0, i_keyValuePair.indexOf(KEY_VALUE_SEPARATOR)).trim();
    }

    /**
     * Returns the value.
     * @return String
     */
    public String getValueString()
    {
        return i_keyValuePair.substring(i_keyValuePair.indexOf(KEY_VALUE_SEPARATOR) + 1);
    }

    /**
     * Sets the comment.
     * @param comment The comment to set
     */
    public void setCommentString(String comment)
    {
        i_comment = new String(COMMENT_PREFIX + comment);
    }

    /**
     * Sets the key.
     * @param key The key to set
     */
    public void setKeyString(String key)
    {
        i_keyValuePair = new String(key.trim() + KEY_VALUE_SEPARATOR + getValueString());
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValueString(String value)
    {
        i_keyValuePair = new String(getKeyString() + KEY_VALUE_SEPARATOR + value);
    }
      
    /**
     * Returns the comment.
     * @return String
     */
    public String getComment()
    {
        return i_comment;
    }

    /**
     * Returns the keyValuePair.
     * @return String
     */
    public String getKeyValuePair()
    {
        return i_keyValuePair;
    }

    /**
     * Sets the comment.
     * @param comment The comment to set
     */
    public void setComment(String comment)
    {
        i_comment = comment;
    }

    /**
     * Sets the keyValuePair.
     * @param keyValuePair The keyValuePair to set
     */
    public void setKeyValuePair(String keyValuePair)
    {
        i_keyValuePair = keyValuePair;
    }    
    
    /**
     * Method isKeyValuePairValid.
     * @param keyValuePair
     * @return boolean
     */
    public static boolean isKeyValuePairValid(String keyValuePair)
    {
        return keyValuePair != null && keyValuePair.indexOf(KEY_VALUE_SEPARATOR) > - 1;
    }
    
    /**
     * Method isCommentValid.
     * @param comment
     * @return boolean
     */
    public static boolean isCommentValid(String comment)
    {
        return comment != null && comment.startsWith(COMMENT_PREFIX);        
    }
    
    /**
     * Method isValid.
     * @param textLine
     * @return boolean
     */
    public static boolean isValid(String textLine)
    {
        if((textLine.indexOf(KEY_VALUE_SEPARATOR) > - 1 || textLine.startsWith(COMMENT_PREFIX)))
        {
            return true;
        }
        return false;
    }
    
    /**
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object arg0)
    {
        return getKeyString().toLowerCase().compareTo(((PropertyLineWrapper)arg0).getKeyValuePair().toLowerCase());
    }
    
    /**
     * Returns the isCommentedPoperty.
     * @return boolean
     */
    public boolean isCommentedProperty()
    {
        return i_isCommentedProperty;
    }

    /**
     * Sets the isCommentedPoperty.
     * @param isCommentedPoperty The isCommentedPoperty to set
     */
    public void setCommentedProperty(boolean isCommentedPoperty)
    {
        i_isCommentedProperty = isCommentedPoperty;
    }
    
    /**
     * Method getTrimmedKeyValuePair.
     * @param keyValuePair
     * @return String
     */
    private String getTrimmedKeyValuePair(String keyValuePair)
    {
        String key = keyValuePair.substring(0, keyValuePair.indexOf(KEY_VALUE_SEPARATOR)).trim();
        String value = keyValuePair.substring(keyValuePair.indexOf(KEY_VALUE_SEPARATOR) + 1);
        return new String(key + KEY_VALUE_SEPARATOR + value);
    }
}