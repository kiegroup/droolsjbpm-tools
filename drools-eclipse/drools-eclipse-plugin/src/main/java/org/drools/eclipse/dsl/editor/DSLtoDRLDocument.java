package org.drools.eclipse.dsl.editor;

import java.io.Reader;

import org.drools.lang.dsl.DSLMappingFile;
import org.drools.lang.dsl.DefaultExpander;
import org.eclipse.jface.text.IDocument;

public class DSLtoDRLDocument extends TransformedDocument {

	private DSLtoDRLRuleViewer viewer;
	
	public DSLtoDRLDocument(IDocument dslDocument, DSLtoDRLRuleViewer viewer) {
		super(dslDocument);
		this.viewer = viewer;
	}
	
	protected String transformInput(String content) {
        DefaultExpander expander = new DefaultExpander();
        try {
        	Reader reader = DSLAdapter.getDSLContent(content, viewer.getResource());
            DSLMappingFile mapping = new DSLMappingFile();
            mapping.parseAndLoad(reader);
            reader.close();
            expander.addDSLMapping(mapping.getMapping());
            return expander.expand(content);
        } catch (Throwable t) {
        	//viewer.handleError(t);
        	return content;
        }

	}

}
