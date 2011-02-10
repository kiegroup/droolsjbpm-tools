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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MvelParsingTest {
    @Test
    public void testGetInnerExpression4() {
        String backText =
            "modify(m) {some=";
        String previous ="";
        assertEquals(previous, CompletionUtil.getInnerExpression( backText ));
    }
    @Test
    public void testGetInnerExpression5() {
        String backText =
            "modify(m) {asdasdas==asdasd, asdasd";
        String previous ="asdasd";
        assertEquals(previous, CompletionUtil.getInnerExpression( backText ));
    }
    @Test
    public void testGetInnerExpression6() {
        String backText =
            "modify(m) {asdasdas==asdasd, asdasd}";
        String previous ="";
        assertEquals(previous, CompletionUtil.getInnerExpression( backText ));
    }

}
