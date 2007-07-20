package org.drools.eclipse.editors.completion;

import org.eclipse.jdt.core.Signature;

import junit.framework.TestCase;

public class CompletionUtilTest extends TestCase {

    public void testIsGetter() {
        assertTrue( CompletionUtil.isGetter( "getThis",
                                             0,
                                             "String" ) );
    }

    public void testIsGetterCannotReturnVoid() {
        assertFalse( CompletionUtil.isGetter( "getThis",
                                              0,
                                              Signature.SIG_VOID ) );
    }

    public void testIsGetterWrongPrefix() {
        assertFalse( CompletionUtil.isGetter( "hasThis",
                                              0,
                                              Signature.SIG_VOID ) );
    }

    public void testIsGetterTooManyArgs() {
        assertFalse( CompletionUtil.isGetter( "getThis",
                                              2,
                                              "String" ) );
    }

    public void testIsGetterMethodNameTooShort() {
        assertFalse( CompletionUtil.isGetter( "get",
                                              2,
                                              "String" ) );
    }

    public void testIsIsGetter() {
        assertTrue( CompletionUtil.isIsGetter( "isGood",
                                               0,
                                               Signature.SIG_BOOLEAN ) );
    }

    public void testIsIsGetterWrongPrefix() {
        assertFalse( CompletionUtil.isIsGetter( "getThis",
                                                0,
                                                Signature.SIG_BOOLEAN ) );
    }

    public void testIsIsGetterAlwaysReturnsBoolean() {
        assertFalse( CompletionUtil.isIsGetter( "isThis",
                                                0,
                                                Signature.SIG_VOID ) );
    }

    public void testIsIsGetterTooManyArgs() {
        assertFalse( CompletionUtil.isIsGetter( "isThis",
                                                2,
                                                "String" ) );
    }

    public void testIsIsGetterMethodNameTooShort() {
        assertFalse( CompletionUtil.isIsGetter( "is",
                                                2,
                                                "String" ) );
    }

    public void testIsSetter() {
        assertTrue( CompletionUtil.isSetter( "setThat",
                                             1,
                                             Signature.SIG_VOID ) );
    }

    public void testIsSetterWrongPrefix() {
        assertFalse( CompletionUtil.isSetter( "getThat",
                                              1,
                                              Signature.SIG_VOID ) );
    }

    public void testIsSetterTooShort() {
        assertFalse( CompletionUtil.isSetter( "se",
                                              1,
                                              Signature.SIG_VOID ) );
    }

    public void testIsSetterNoArgs() {
        assertFalse( CompletionUtil.isSetter( "setThat",
                                              0,
                                              Signature.SIG_VOID ) );
    }

    public void testIsSetterWrongType() {
        assertFalse( CompletionUtil.isSetter( "setThat",
                                              1,
                                              "String" ) );
    }

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
}