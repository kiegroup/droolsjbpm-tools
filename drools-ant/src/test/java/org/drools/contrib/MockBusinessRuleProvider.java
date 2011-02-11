package org.drools.contrib;

import org.drools.Service;
import org.drools.compiler.BusinessRuleProvider;
import org.drools.io.Resource;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class MockBusinessRuleProvider implements Service, BusinessRuleProvider {


    public MockBusinessRuleProvider() {
    }

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
}
