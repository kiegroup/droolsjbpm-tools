package org.jbpm.process.builder;

import org.drools.compiler.*;
import org.drools.io.Resource;
import org.drools.rule.*;
import org.drools.rule.Package;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

public class ProcessBuilderFactoryServiceImpl implements ProcessBuilderFactoryService {

    public org.drools.compiler.ProcessBuilder newProcessBuilder(final PackageBuilder packageBuilder) {


        return new org.drools.compiler.ProcessBuilder() {
            public List<DroolsError> addProcessFromXml(Resource resource) throws IOException {


                String mock = "";
                mock += "package org.drools.test\n";
                mock += "rule mock\n";
                mock += "when\n";
                mock += "then\n";
                mock += "end\n";

                try {
                    packageBuilder.addPackageFromDrl(new StringReader(mock));
                } catch (DroolsParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return Collections.emptyList();
            }

            public List<DroolsError> getErrors() { 
                return Collections.emptyList();
            }
        };
    }
}
