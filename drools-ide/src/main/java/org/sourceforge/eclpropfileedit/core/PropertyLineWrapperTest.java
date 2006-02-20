/* $RCSfile: PropertyLineWrapperTest.java,v $
 * Created on 20-Sep-2002, 16:45:36 by oliver
 * $Source: /cvsroot/eclpropfileedit/eclpropfileedit/src/org/sourceforge/eclpropfileedit/core/PropertyLineWrapperTest.java,v $
 * $Id: PropertyLineWrapperTest.java,v 1.2 2002/10/27 15:49:19 davoli Exp $
 * Copyright (c) 2000-2002 FinanCity Ltd.  All rights reserved. */
package org.sourceforge.eclpropfileedit.core;

import junit.framework.TestCase;

/** 
 * @author  Oliver
 * @version $Revision: 1.2 $
 */
public class PropertyLineWrapperTest extends TestCase
{
    
    PropertyLineWrapper i_propLine;

    /**
     * Constructor for PropertyLineWrapperTest.
     * @param arg0
     */
    public PropertyLineWrapperTest(String arg0)
    {
        super(arg0);
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
//        i_propLine = new PropertyLineWrapper("test=tester in line", "#blablacomment in line", false);
//        System.out.println("key = " + i_propLine.getKeyString());
//        System.out.println("value = " + i_propLine.getValueString());        
//        System.out.println("comment = " + i_propLine.getCommentString());
//        i_propLine.setKeyString("key");
//        i_propLine.setCommentString("new comment");
//        System.out.println("key = " + i_propLine.getKeyString());
//        System.out.println("value = " + i_propLine.getValueString());        
//        System.out.println("comment = " + i_propLine.getCommentString());
//        i_propLine.setValueString("value");        
//        System.out.println("key = " + i_propLine.getKeyString());
//        System.out.println("value = " + i_propLine.getValueString());        
//        System.out.println("comment = " + i_propLine.getCommentString());
//        System.out.println("key=value = " + i_propLine.getKeyValuePair());
//        System.out.println("#comment = " + i_propLine.getComment());        
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
   
    public void testIsCommentValid()
    {
        System.out.println("invoking testIsKeyCommentValid()...");
        i_propLine = new PropertyLineWrapper("key=value","#   this is the comment");
        assertTrue("Comment thinks it is valid", PropertyLineWrapper.isCommentValid(i_propLine.getComment()));
        i_propLine.setComment("this comment has no hash");
        assertTrue("Comment thinks it is valid", !PropertyLineWrapper.isCommentValid(i_propLine.getComment()));                
        assertTrue("Key-value-pair thinks it is commented", !i_propLine.isCommentedProperty());
    }

    public void testIsKeyValuePairValid()
    {
        System.out.println("invoking testIsKeyValuePairValid()...");
        i_propLine = new PropertyLineWrapper("key=value","#   this is the comment");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        assertTrue("Key-value-pair thinks it is commented", !i_propLine.isCommentedProperty());
        assertEquals("key=value", i_propLine.getKeyValuePair());
        assertEquals("key", i_propLine.getKeyString());
        assertEquals("value", i_propLine.getValueString());
        i_propLine.setKeyValuePair("some normal String");
        assertTrue("Key-value-pair thinks it is valid", !PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setKeyValuePair("newkey=newvalue");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setCommentedProperty(true);
        assertTrue("Key-value-pair thinks it is uncommented", i_propLine.isCommentedProperty());
                        
        i_propLine = new PropertyLineWrapper("#key=value","#   this is the comment");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        assertTrue("Key-value-pair thinks it is uncommented", i_propLine.isCommentedProperty());
        assertEquals("key=value", i_propLine.getKeyValuePair());
        assertEquals("key", i_propLine.getKeyString());
        assertEquals("value", i_propLine.getValueString());
        i_propLine.setKeyValuePair("some normal String");
        assertTrue("Key-value-pair thinks it is valid", !PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setKeyValuePair("newkey=newvalue");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setCommentedProperty(false);
        assertTrue("Key-value-pair thinks it is commented", !i_propLine.isCommentedProperty());

        i_propLine = new PropertyLineWrapper("#key=value","#   this is the comment", false);
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        assertTrue("Key-value-pair thinks it is uncommented", i_propLine.isCommentedProperty());
        assertEquals("key=value", i_propLine.getKeyValuePair());
        assertEquals("key", i_propLine.getKeyString());
        assertEquals("value", i_propLine.getValueString());
        i_propLine.setKeyValuePair("some normal String");
        assertTrue("Key-value-pair thinks it is valid", !PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setKeyValuePair("newkey=newvalue");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setCommentedProperty(false);
        assertTrue("Key-value-pair thinks it is uncommented", !i_propLine.isCommentedProperty());
 
        i_propLine = new PropertyLineWrapper("#key=value","#   this is the comment", true);
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        assertTrue("Key-value-pair thinks it is uncommented", i_propLine.isCommentedProperty());
        assertEquals("key=value", i_propLine.getKeyValuePair());
        assertEquals("key", i_propLine.getKeyString());
        assertEquals("value", i_propLine.getValueString());
        i_propLine.setKeyValuePair("some normal String");
        assertTrue("Key-value-pair thinks it is valid", !PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setKeyValuePair("newkey=newvalue");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setCommentedProperty(false);
        assertTrue("Key-value-pair thinks it is commented", !i_propLine.isCommentedProperty());

        i_propLine = new PropertyLineWrapper("key=value","#   this is the comment", false);
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        assertTrue("Key-value-pair thinks it is commented", !i_propLine.isCommentedProperty());
        assertEquals("key=value", i_propLine.getKeyValuePair());
        assertEquals("key", i_propLine.getKeyString());
        assertEquals("value", i_propLine.getValueString());
        i_propLine.setKeyValuePair("some normal String");
        assertTrue("Key-value-pair thinks it is valid", !PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setKeyValuePair("newkey=newvalue");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setCommentedProperty(true);
        assertTrue("Key-value-pair thinks it is uncommented", i_propLine.isCommentedProperty());

        i_propLine = new PropertyLineWrapper("key =value","#   this is the comment", true);
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        assertTrue("Key-value-pair thinks it is uncommented", i_propLine.isCommentedProperty());
        assertEquals("key=value", i_propLine.getKeyValuePair());
        assertEquals("key", i_propLine.getKeyString());
        assertEquals("value", i_propLine.getValueString());
        i_propLine.setKeyValuePair("some normal String");
        assertTrue("Key-value-pair thinks it is valid", !PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setKeyValuePair("newkey=newvalue");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setCommentedProperty(false);
        assertTrue("Key-value-pair thinks it is commented", !i_propLine.isCommentedProperty());
        
        i_propLine = new PropertyLineWrapper("# key = value","#   this is the comment");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        assertTrue("Key-value-pair thinks it is uncommented", i_propLine.isCommentedProperty());
        assertEquals("key= value", i_propLine.getKeyValuePair());
        assertEquals("key", i_propLine.getKeyString());
        assertEquals(" value", i_propLine.getValueString());
        i_propLine.setKeyValuePair("some normal String");
        assertTrue("Key-value-pair thinks it is valid", !PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setKeyValuePair("newkey=newvalue");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setCommentedProperty(false);
        assertTrue("Key-value-pair thinks it is commented", !i_propLine.isCommentedProperty());
        
        i_propLine = new PropertyLineWrapper("# cactus.filterRedirectorURL = http://localhost:@test.port@/test/FilterRedirector","#   this is the comment");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        assertTrue("Key-value-pair thinks it is uncommented", i_propLine.isCommentedProperty());
        assertEquals("cactus.filterRedirectorURL= http://localhost:@test.port@/test/FilterRedirector", i_propLine.getKeyValuePair());
        assertEquals("cactus.filterRedirectorURL", i_propLine.getKeyString());
        assertEquals(" http://localhost:@test.port@/test/FilterRedirector", i_propLine.getValueString());
        i_propLine.setKeyValuePair("some normal String");
        assertTrue("Key-value-pair thinks it is valid", !PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setKeyValuePair("newkey=newvalue");
        assertTrue("Key-value-pair thinks it is valid", PropertyLineWrapper.isKeyValuePairValid(i_propLine.getKeyValuePair()));
        i_propLine.setCommentedProperty(false);
        assertTrue("Key-value-pair thinks it is commented", !i_propLine.isCommentedProperty());
    }
}
