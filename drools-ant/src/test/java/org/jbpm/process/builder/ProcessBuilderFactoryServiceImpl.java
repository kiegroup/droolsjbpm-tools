package org.jbpm.process.builder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.drools.compiler.DroolsError;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.ProcessBuilderFactoryService;
import org.kie.io.Resource;

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
        };
    }
}