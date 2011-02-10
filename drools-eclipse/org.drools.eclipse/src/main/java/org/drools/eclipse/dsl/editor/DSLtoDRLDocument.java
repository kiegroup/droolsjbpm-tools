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

package org.drools.eclipse.dsl.editor;

import java.io.Reader;

import org.drools.lang.dsl.DSLMappingFile;
import org.drools.lang.dsl.DSLTokenizedMappingFile;
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
            DSLMappingFile mapping = new DSLTokenizedMappingFile();
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
