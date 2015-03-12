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

import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class AbstractCompletionProcessorTest {

    @Test
    @Ignore("Started failing after Eclipse core jars update (as described by krisv). Needs to be investigated.")
    public void testReadBackwards() throws BadLocationException {
        //setup
        IDocument doc = getDoc();
        int rhsStartOffset = 150;

        AbstractCompletionProcessor proc = new MockCompletionProcessor();
        String backText = proc.readBackwards( rhsStartOffset,
                                              doc );
        String rule2 = "\nrule YourRule \n" + //
                       "   dialect \"mvel\"\n" + //
                       "   when\n" + //
                       "       Class ( )\n" + //
                       "   then\n";

        assertEquals( rule2,
                      backText );
    }

    private IDocument getDoc() {
        String input = "rule MyRule \n" + //
                       "   when\n" + //
                       "       Class ( )\n" + //
                       "   then\n" + //
                       "       System.out.println(\"Hey\");\n" + //
                       "end\n" + //
                       "rule YourRule \n" + //
                       "   dialect \"mvel\"\n" + //
                       "   when\n" + //
                       "       Class ( )\n" + //
                       "   then\n" + //
                       "       " +//
                       "end\n\n" ;

        IDocument doc = new Document( input );
        IDocumentPartitioner partitioner = new FastPartitioner( new DRLPartionScanner(),
                                                                DRLPartionScanner.LEGAL_CONTENT_TYPES );
        partitioner.connect( doc );
        doc.setDocumentPartitioner( partitioner );
        return doc;
    }

}
