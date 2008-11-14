package org.drools.eclipse.editors.completion;

import junit.framework.TestCase;

public class MvelParsingTest extends TestCase {
    public void testGetInnerExpression4() {
        String backText =
            "modify(m) {some=";
        String previous ="";
        assertEquals(previous, CompletionUtil.getInnerExpression( backText ));
    }
    public void testGetInnerExpression5() {
        String backText =
            "modify(m) {asdasdas==asdasd, asdasd";
        String previous ="asdasd";
        assertEquals(previous, CompletionUtil.getInnerExpression( backText ));
    }
    public void testGetInnerExpression6() {
        String backText =
            "modify(m) {asdasdas==asdasd, asdasd}";
        String previous ="";
        assertEquals(previous, CompletionUtil.getInnerExpression( backText ));
    }

}
