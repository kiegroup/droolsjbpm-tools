package org.drools.ide.editors;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.ide.editors.completion.RuleCompletionProposal;
import org.drools.lang.dsl.template.NLGrammar;
import org.drools.lang.dsl.template.NLMappingItem;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This holds the DSL configuration for an editor instance.
 * When loading, it will find the DSL file, and load the applicable lists.
 * 
 * This provides a link between the editor and the DSL features of the rule language.  
 * 
 * It will look for a DSL configuration, as named in the rule file, in the same directory as the rule file.
 * Failing this, it will search one directory above the rule file. 
 * Failing that, it will search the root of the project in the workspace.
 * 
 * @author Michael Neale
 */
public class DSLAdapter {

    private String dslConfigName;
    private boolean valid = false;
    private List conditionProposals = new ArrayList();
    private List consequenceProposals = new ArrayList();
    
    
    //to dig out the expander, without using the parser.
    private static final Pattern expander = Pattern.compile( "^.*expander\\s*(.*)\\.dsl.*", 
                                                             Pattern.DOTALL | Pattern.MULTILINE );
    
    public DSLAdapter(String content, FileEditorInput input) {
        
        dslConfigName = findDSLConfigName( content );
        if (dslConfigName == null) return;
        loadConfig( input );
        
        
    }

    private void loadConfig(FileEditorInput input) {
        IResource res = input.getFile().getParent().findMember( dslConfigName );
        if (res == null) res = input.getFile().getParent().getParent().findMember( dslConfigName ); //try parent directory
        if (res == null) res = input.getFile().getProject().findMember( dslConfigName ); //try root of project.
        if (res instanceof IFile) {
            IFile dslConf = (IFile) res;
            if (dslConf.exists()) {
                InputStream stream = null; 
                try {
                    stream = dslConf.getContents();
                    readConfig( stream );
                    valid = true;
                } catch ( Exception e ) {
                    throw new IllegalStateException("Unable to open DSL config file.", e);
                } finally {
                    closeStream( stream );
                }
                
            }
        }
    }

    
    /** This will load in the DSL config file, using the NLGrammar from drools-compiler */
    void readConfig(InputStream stream) throws IOException, CoreException {
        NLGrammar grammar = new NLGrammar();
        grammar.load( new InputStreamReader(stream) );

        List conditions = grammar.getMappings( "when" );
        List consequences = grammar.getMappings( "then" );
        
        conditionProposals = new ArrayList(conditions.size());
        consequenceProposals = new ArrayList(consequences.size());
        
        buildProposals( conditions, conditionProposals );
        buildProposals( consequences, consequenceProposals );

    }

    private void buildProposals(List suggestions, List proposals) {
        for ( Iterator iter = suggestions.iterator(); iter.hasNext(); ) {
            NLMappingItem text = (NLMappingItem) iter.next();
            RuleCompletionProposal proposal = new RuleCompletionProposal(text.getNaturalTemplate());
            proposals.add(proposal);
        }
    }

    private void closeStream(InputStream stream) {
        if (stream != null) try {
            stream.close();
        } catch ( IOException e ) {}
    }

    DSLAdapter() {
        
    }

    String findDSLConfigName(String content) {
        String name = null;
        Matcher matches = expander.matcher( content );
        if (matches.matches()) {
            name = matches.group(1) + ".dsl";
        }
        return name;
    }
    
    
    String getDSLConfigName() {
        return dslConfigName;
    }
    
    
    public boolean isValid() {
        return valid;
    }
    
    
    public List listConditionItems() {
        return conditionProposals;
    }
    
    public List listConsequenceItems() {
        return consequenceProposals;
    }    
    
}
