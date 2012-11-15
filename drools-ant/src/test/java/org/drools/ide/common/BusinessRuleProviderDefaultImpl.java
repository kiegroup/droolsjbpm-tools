package org.drools.ide.common;

import org.kie.Service;
import org.drools.compiler.BusinessRuleProvider;
import org.kie.io.Resource;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class BusinessRuleProviderDefaultImpl implements Service, BusinessRuleProvider {

    public Reader getKnowledgeReader(Resource ruleResource) throws IOException {
        if (ruleResource != null) {
            String drl = "";
            drl += "package org.drools.test\n";
            drl += "rule test\n";
            drl += "when\n";
            drl += "then\n";
            drl += "end\n";

            return new StringReader(drl);
        }

        return null;

    }

    public boolean hasDSLSentences() {
        return false;
    }

}
