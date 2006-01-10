package org.drools.ide.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.drools.io.RuleSetReader;
import org.drools.rule.RuleSet;
import org.drools.spi.RuleBaseContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Extends RuleSetReader so that errors and warnings generated
 * during parsing can be retrieved later.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class RuleSetReaderWithErrors extends RuleSetReader {

    private List<SAXParseException> errors = new ArrayList<SAXParseException>();
    private List<SAXParseException> warnings = new ArrayList<SAXParseException>();
    
    public RuleSetReaderWithErrors(RuleBaseContext factoryContext) {
        super(factoryContext);
    }
    
    public void error(SAXParseException x) {
        errors.add(x);
    }

    public void warning(SAXParseException x) {
        warnings.add(x);
    }

    public void fatalError(SAXParseException x) throws SAXParseException {
        throw x;
    }
    
    public RuleSet read(InputSource in) throws SAXException, IOException {
        errors.clear();
        warnings.clear();
        return super.read(in);
    }

    public List<SAXParseException> getErrors() {
        return errors;
    }

    public List<SAXParseException> getWarnings() {
        return warnings;
    }
}
