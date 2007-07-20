package org.drools.eclipse.editors.completion;

import org.drools.eclipse.editors.scanners.DRLPartionScanner;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

import junit.framework.TestCase;

public class AbstractCompletionProcessorTest extends TestCase {

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
