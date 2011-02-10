/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.editors.completion;

import org.eclipse.jdt.core.Signature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompletionUtilTest {

    @Test
    public void testIsGetter() {
        assertTrue( CompletionUtil.isGetter( "getThis",
                                             0,
                                             "String" ) );
    }

    @Test
    public void testIsGetterCannotReturnVoid() {
        assertFalse( CompletionUtil.isGetter( "getThis",
                                              0,
                                              Signature.SIG_VOID ) );
    }

    @Test
    public void testIsGetterWrongPrefix() {
        assertFalse( CompletionUtil.isGetter( "hasThis",
                                              0,
                                              Signature.SIG_VOID ) );
    }

    @Test
    public void testIsGetterTooManyArgs() {
        assertFalse( CompletionUtil.isGetter( "getThis",
                                              2,
                                              "String" ) );
    }

    @Test
    public void testIsGetterMethodNameTooShort() {
        assertFalse( CompletionUtil.isGetter( "get",
                                              2,
                                              "String" ) );
    }

    @Test
    public void testIsIsGetter() {
        assertTrue( CompletionUtil.isIsGetter( "isGood",
                                               0,
                                               Signature.SIG_BOOLEAN ) );
    }

    @Test
    public void testIsIsGetterWrongPrefix() {
        assertFalse( CompletionUtil.isIsGetter( "getThis",
                                                0,
                                                Signature.SIG_BOOLEAN ) );
    }

    @Test
    public void testIsIsGetterAlwaysReturnsBoolean() {
        assertFalse( CompletionUtil.isIsGetter( "isThis",
                                                0,
                                                Signature.SIG_VOID ) );
    }

    @Test
    public void testIsIsGetterTooManyArgs() {
        assertFalse( CompletionUtil.isIsGetter( "isThis",
                                                2,
                                                "String" ) );
    }

    @Test
    public void testIsIsGetterMethodNameTooShort() {
        assertFalse( CompletionUtil.isIsGetter( "is",
                                                2,
                                                "String" ) );
    }

    @Test
    public void testIsSetter() {
        assertTrue( CompletionUtil.isSetter( "setThat",
                                             1,
                                             Signature.SIG_VOID ) );
    }

    @Test
    public void testIsSetterWrongPrefix() {
        assertFalse( CompletionUtil.isSetter( "getThat",
                                              1,
                                              Signature.SIG_VOID ) );
    }

    @Test
    public void testIsSetterTooShort() {
        assertFalse( CompletionUtil.isSetter( "se",
                                              1,
                                              Signature.SIG_VOID ) );
    }

    @Test
    public void testIsSetterNoArgs() {
        assertFalse( CompletionUtil.isSetter( "setThat",
                                              0,
                                              Signature.SIG_VOID ) );
    }

    @Test
    public void testIsSetterWrongType() {
        assertFalse( CompletionUtil.isSetter( "setThat",
                                              1,
                                              "String" ) );
    }

    @Test
    public void testGetPropertyName() {
        assertEquals( "me",
                      CompletionUtil.getPropertyName( "getMe",
                                                      0,
                                                      "String" ) );
        assertEquals( "me",
                      CompletionUtil.getPropertyName( "isMe",
                                                      0,
                                                      Signature.SIG_BOOLEAN ) );
        assertEquals( "me",
                      CompletionUtil.getPropertyName( "setMe",
                                                      1,
                                                      Signature.SIG_VOID ) );

        assertEquals( "setMe",
                      CompletionUtil.getPropertyName( "setMe",
                                                      0,
                                                      Signature.SIG_VOID ) );

        assertEquals( "MySuperMethod",
                      CompletionUtil.getPropertyName( "MySuperMethod",
                                                      1,
                                                      Signature.SIG_VOID ) );
    }

    @Test
    public void testGetPropertyNameStripsParenthesis() {

        assertEquals( "MySuperMethod()",
                      CompletionUtil.getPropertyName( "MySuperMethod()",
                                                      1,
                                                      Signature.SIG_VOID ) );
        assertEquals( "me",
                      CompletionUtil.getPropertyName( "getMe()",
                                                      0,
                                                      "String" ) );
        assertEquals( "me",
                      CompletionUtil.getPropertyName( "isMe()",
                                                      0,
                                                      Signature.SIG_BOOLEAN ) );
        assertEquals( "me",
                      CompletionUtil.getPropertyName( "setMe()",
                                                      1,
                                                      Signature.SIG_VOID ) );
    }

    @Test
    public void testStripLastWord() {
        String backtext = "rule \"GoodBye\"\r\n" + "	no-loop true\r\n" + "	when\r\n" + "		m : Message( status == Message.GOODBYE, message : message )\r\n" + "	then\r\n" + "		m.message=message;\r\n" + "		m.last";

        String lastword = "last";
        assertEquals( lastword,
                      CompletionUtil.stripLastWord( backtext ) );
    }

    @Test
    public void testGetPreviousExpression1() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d';";
        String previous = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d';";
        assertEquals( previous,
                      CompletionUtil.getPreviousExpression( backText ) );
    }

    @Test
    public void testGetPreviousExpression2() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   message== ";
        String previous = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;";
        assertEquals( previous,
                      CompletionUtil.getPreviousExpression( backText ) );
    }

    @Test
    public void testGetPreviousExpression3() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   message(sdasdasd, ";
        String previous = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;";
        assertEquals( previous,
                      CompletionUtil.getPreviousExpression( backText ) );
    }

    @Test
    public void testGetPreviousExpression4() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   message( ";
        String previous = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;";
        assertEquals( previous,
                      CompletionUtil.getPreviousExpression( backText ) );
    }

    @Test
    public void testGetPreviousExpression5() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   this.asd ";
        String previous = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;";
        assertEquals( previous,
                      CompletionUtil.getPreviousExpression( backText ) );
    }

    @Test
    public void testGetPreviousExpression6() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   message(){ ";
        String previous = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;";
        assertEquals( previous,
                      CompletionUtil.getPreviousExpression( backText ) );
    }

    @Test
    public void testGetPreviousExpression7() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d';message== ";
        String previous = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d';";
        assertEquals( previous,
                      CompletionUtil.getPreviousExpression( backText ) );
    }

    @Test
    public void testGetLastExpression11() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d'";
        String previous = "\r\n" + "   adasd ='d'";
        assertEquals( previous,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression1() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d';";
        String previous = "\r\n   adasd ='d'";
        assertEquals( previous,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression10() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d';\r\n";
        assertEquals( backText,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression2() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   message== ";
        String previous = "\r\n   message== ";
        assertEquals( previous,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression3() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   message(sdasdasd, ";
        String previous = "\r\n   message(sdasdasd, ";
        assertEquals( previous,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression4() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   message( ";
        String previous = "\r\n   message( ";
        assertEquals( previous,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression5() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   this.asd ";
        String previous = "\r\n   this.asd ";
        assertEquals( previous,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression6() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   message(){ ";
        String previous = "\r\n   message(){ ";
        assertEquals( previous,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression7() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d';message== ";
        String previous = "message== ";
        assertEquals( previous,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetInnerExpression() {
        String backText = "  \r\n" + "   System.out.println( message );\r\n" + "   m.message = \"Goodbyte cruel world\";\r\n" + "   m.status = 1;\r\n" + "   adasd ='d';message== ";
        String previous = "";
        assertEquals( previous,
                      CompletionUtil.getInnerExpression( backText ) );
    }

    @Test
    public void testGetInnerExpression2() {
        String backText = "System.out.println(m ";
        String previous = "m";
        assertEquals( previous,
                      CompletionUtil.getInnerExpression( backText ) );
    }

    @Test
    public void testGetInnerExpression3() {
        String backText = "update(m) {";
        String previous = "";
        assertEquals( previous,
                      CompletionUtil.getInnerExpression( backText ) );
    }

    @Test
    public void testGetInnerExpression4() {
        String backText = "update(m) {some=";
        String previous = "";
        assertEquals( previous,
                      CompletionUtil.getInnerExpression( backText ) );
    }

    @Test
    public void testGetInnerExpression5() {
        String backText = "update(m) {asdasdas==asdasd, asdasd";
        String previous = "asdasd";
        assertEquals( previous,
                      CompletionUtil.getInnerExpression( backText ) );
    }

    @Test
    public void testGetInnerExpression6() {
        String backText = "update(m) {asdasdas==asdasd, asdasd}";
        String previous = "";
        assertEquals( previous,
                      CompletionUtil.getInnerExpression( backText ) );
    }

    @Test
    public void testGetLastExpression_withComments() {
        String backText = "dasdasdas\nsasasasa\n //fsdfsdfdsfdsf\n\n";
        String exp = "dasdasdas\n" + "sasasasa\n" + " //fsdfsdfdsfdsf\n" + "\n";
        assertEquals( exp,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetLastExpression_withComments2() {
        String backText = "dasdasdas\nsasasasa\n //fsdfsdfdsfdsf\n";
        String exp = "dasdasdas\n" + "sasasasa\n" + " //fsdfsdfdsfdsf\n" ;
        assertEquals( exp,
                      CompletionUtil.getLastExpression( backText ) );
    }
    @Test
    public void testGetLastExpression_withComments3() {
        String backText = "dasdasdas\nsasasasa\n //fsdfsdfdsfdsf\n";
        String exp = "dasdasdas\n" + "sasasasa\n" + " //fsdfsdfdsfdsf\n" ;
        assertEquals( exp,
                      CompletionUtil.getLastExpression( backText ) );
    }

    @Test
    public void testGetTextWithoutPrefix() {
        String text = "modify(m) {asdasdas==asdasd, asdasd.asa";
        String expected = "modify(m) {asdasdas==asdasd, asdasd.";
        assertEquals( expected,
                      CompletionUtil.getTextWithoutPrefix( text,
                                                           "asa" ) );
    }

    @Test
    public void testGetTextWithoutPrefix2() {
        String text = "it";
        String expected = "";
        assertEquals( expected,
                      CompletionUtil.getTextWithoutPrefix( text,
                                                           text ) );
    }

}
