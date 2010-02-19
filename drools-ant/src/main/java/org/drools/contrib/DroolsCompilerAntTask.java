/*
 * Copyright 2006 JBoss Inc
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

package org.drools.contrib;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.RuntimeDroolsException;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.drools.definition.KnowledgePackage;
import org.drools.guvnor.client.modeldriven.brl.RuleModel;
import org.drools.guvnor.server.util.BRDRLPersistence;
import org.drools.guvnor.server.util.BRXMLPersistence;
import org.drools.io.ResourceFactory;
import org.drools.lang.Expander;
import org.drools.lang.dsl.DSLMappingFile;
import org.drools.lang.dsl.DSLTokenizedMappingFile;
import org.drools.lang.dsl.DefaultExpander;
import org.drools.lang.dsl.DefaultExpanderResolver;
import org.drools.core.util.DroolsStreamUtils;

/**
 * An ant task to allow rulebase compilation and serialization during a build.
 * 
 * @author etirelli
 */
public class DroolsCompilerAntTask extends MatchingTask {

    public static String BRLFILEEXTENSION           = ".brl";
    public static String XMLFILEEXTENSION           = ".xml";
    public static String RULEFLOWMODELFILEEXTENSION = ".rfm";
    public static String RULEFLOWFILEEXTENSION      = ".rf";
    public static String DSLFILEEXTENSION           = ".dsl";
    public static String DSLRFILEEXTENSION          = ".dslr";
    public static String XLSFILEEXTENSION           = ".xls";

    public static String PACKAGEBINFORMAT           = "package";
    public static String PACKAGEBINTYPE             = "knowledge";

    private File         srcdir;
    private File         toFile;
    private Path         classpath;

    private String       binformat;
    private String       bintype;

    /**
     * Source directory to read DRL files from
     * 
     * @param directory
     */
    public void setSrcDir(File directory) {
        this.srcdir = directory;
    }

    /**
     * File to serialize the rulebase to
     * 
     * @param toFile
     */
    public void setToFile(File toFile) {
        this.toFile = toFile;
    }

    /**
     * The classpath to use when compiling the rulebase
     * 
     * @param classpath
     */
    public void setClasspath(Path classpath) {
        createClasspath().append( classpath );
    }

    /**
     * Classpath to use, by reference, when compiling the rulebase
     * 
     * @param a
     *            reference to an existing classpath
     */
    public void setClasspathref(Reference r) {
        createClasspath().setRefid( r );
    }

    /**
     * Adds a path to the classpath.
     * 
     * @return created classpath
     */
    public Path createClasspath() {
        if ( this.classpath == null ) {
            this.classpath = new Path( getProject() );
        }
        return this.classpath.createPath();
    }

    /**
     * Task's main method
     */
    public void execute() throws BuildException {
        super.execute();

        // checking parameters are set
        if ( toFile == null ) {
            throw new BuildException( "Destination rulebase file does not specified." );
        }

        // checking parameters are set
        if ( srcdir == null ) {
            throw new BuildException( "Source directory not specified." );
        }

        if ( !srcdir.exists() ) {
            throw new BuildException( "Source directory does not exists." + srcdir.getAbsolutePath() );
        }

        AntClassLoader loader = null;
        try {
            // create a specialized classloader
            loader = getClassLoader();

            if ( PACKAGEBINTYPE.equals( bintype ) ) {
                createWithKnowledgeBuilder( loader );
            } else {
                createWithPackageBuilder( loader );
            }
        } catch ( Exception e ) {
            throw new BuildException( "RuleBaseTask failed: " + e.getMessage(),
                                      e );
        } finally {
            if ( loader != null ) {
                loader.resetThreadContextLoader();
            }
        }
    }

    private void createWithKnowledgeBuilder(AntClassLoader loader) throws FileNotFoundException,
                                                                  DroolsParserException,
                                                                  IOException {
        // create a package builder configured to use the given classloader
        KnowledgeBuilder kbuilder = getKnowledgeBuilder( loader );

        // get the list of files to be added to the rulebase
        String[] fileNames = getFileList();

        for ( int i = 0; i < fileNames.length; i++ ) {
            // compile rule file and add to the builder
            compileAndAddFile( kbuilder,
                               fileNames[i] );
        }

        if ( kbuilder.hasErrors() ) {
            System.err.println( kbuilder.getErrors().toString() );
        }

        // gets the packages
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        // creates the knowledge base
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        // adds the packages
        kbase.addKnowledgePackages( pkgs );

        if ( PACKAGEBINFORMAT.equals( binformat ) ) {
            serializeObject( pkgs.iterator().next() );
        } else {
            // serialize the knowledge base to the destination file
            serializeObject( kbase );
        }
    }

    private void createWithPackageBuilder(AntClassLoader loader) throws FileNotFoundException,
                                                                DroolsParserException,
                                                                IOException {
        // create a package builder configured to use the given classloader
        PackageBuilder builder = getPackageBuilder( loader );

        // get the list of files to be added to the rulebase
        String[] fileNames = getFileList();

        for ( int i = 0; i < fileNames.length; i++ ) {
            // compile rule file and add to the builder
            compileAndAddFile( builder,
                               fileNames[i] );
        }

        if ( builder.hasErrors() ) {
            System.err.println( builder.getErrors().toString() );
        }

        // gets the package
        org.drools.rule.Package pkg = builder.getPackage();

        // creates the rulebase
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();

        // adds the package
        ruleBase.addPackage( pkg );

        if ( PACKAGEBINFORMAT.equals( binformat ) ) {
            serializeObject( pkg );
        } else {
            // serialize the rule base to the destination file
            serializeObject( ruleBase );
        }
    }

    /**
     * @param ruleBase
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void serializeObject(Object object) throws FileNotFoundException,
                                               IOException {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream( toFile );
            DroolsStreamUtils.streamOut( fout,
                                         object );
        } finally {
            if ( fout != null ) {
                fout.close();
            }
        }
    }

    /**
     * @param builder
     * @param fileName
     * @return
     * @throws FileNotFoundException
     * @throws DroolsParserException
     * @throws IOException
     */
    private void compileAndAddFile(KnowledgeBuilder kbuilder,
                                   String fileName) throws FileNotFoundException,
                                                   DroolsParserException,
                                                   IOException {

        FileReader fileReader = new FileReader( new File( this.srcdir,
                                                          fileName ) );

        if ( fileName.endsWith( DroolsCompilerAntTask.BRLFILEEXTENSION ) ) {

            // TODO: Right now I have to first change this to String. Change to
            // use KnowledgeBuilder directly when the support for that is done.
            // -Toni Rikkola-

            RuleModel model = BRXMLPersistence.getInstance().unmarshal( loadResource( fileName ) );
            String packagefile = loadResource( resolvePackageFile( this.srcdir.getAbsolutePath() ) );
            model.name = fileName.replace( DroolsCompilerAntTask.BRLFILEEXTENSION,
                                           "" );
            ByteArrayInputStream istream = new ByteArrayInputStream( (packagefile + BRDRLPersistence.getInstance().marshal( model )).getBytes() );

            Reader instream = new InputStreamReader( istream );

            kbuilder.add( ResourceFactory.newReaderResource( instream ),
                          ResourceType.DRL );

        } else if ( fileName.endsWith( DroolsCompilerAntTask.RULEFLOWMODELFILEEXTENSION ) || fileName.endsWith( DroolsCompilerAntTask.RULEFLOWFILEEXTENSION ) ) {

            kbuilder.add( ResourceFactory.newReaderResource( fileReader ),
                          ResourceType.DRF );

        } else if ( fileName.endsWith( DroolsCompilerAntTask.XMLFILEEXTENSION ) ) {
            kbuilder.add( ResourceFactory.newReaderResource( fileReader ),
                          ResourceType.XDRL );
        } else if ( fileName.endsWith( DroolsCompilerAntTask.XLSFILEEXTENSION ) ) {

            DecisionTableConfiguration dtableconfiguration = KnowledgeBuilderFactory.newDecisionTableConfiguration();
            dtableconfiguration.setInputType( DecisionTableInputType.XLS );

            kbuilder.add( ResourceFactory.newReaderResource( fileReader ),
                          ResourceType.DTABLE,
                          dtableconfiguration );

            // } else if
            // (fileName.endsWith(DroolsCompilerAntTask.DSLFILEEXTENSION)) {
            //
            // kbuilder.add(ResourceFactory.newReaderResource(fileReader),
            // ResourceType.DSL);

        } else if ( fileName.endsWith( DroolsCompilerAntTask.DSLRFILEEXTENSION ) ) {

            // Get the DSL too.
            String[] dsls = resolveDSLFilesToArray();
            for ( int i = 0; i < dsls.length; i++ ) {
                kbuilder.add( ResourceFactory.newFileResource( new File( this.srcdir,
                                                                         dsls[i] ) ),
                              ResourceType.DSL );
            }

            kbuilder.add( ResourceFactory.newReaderResource( fileReader ),
                          ResourceType.DSLR );

        } else {
            kbuilder.add( ResourceFactory.newReaderResource( fileReader ),
                          ResourceType.DRL );
        }
    }

    /**
     * @param builder
     * @param fileName
     * @return
     * @throws FileNotFoundException
     * @throws DroolsParserException
     * @throws IOException
     */
    private void compileAndAddFile(PackageBuilder builder,
                                   String fileName) throws FileNotFoundException,
                                                   DroolsParserException,
                                                   IOException {
        InputStreamReader instream = null;
        File file = new File( this.srcdir,
                              fileName );

        try {

            if ( fileName.endsWith( DroolsCompilerAntTask.BRLFILEEXTENSION ) ) {

                RuleModel model = BRXMLPersistence.getInstance().unmarshal( loadResource( fileName ) );
                String packagefile = loadResource( resolvePackageFile( this.srcdir.getAbsolutePath() ) );
                model.name = fileName.replace( DroolsCompilerAntTask.BRLFILEEXTENSION,
                                               "" );
                ByteArrayInputStream istream = new ByteArrayInputStream( (packagefile + BRDRLPersistence.getInstance().marshal( model )).getBytes() );
                instream = new InputStreamReader( istream );

            } else {
                instream = new InputStreamReader( new FileInputStream( file ) );
            }

            if ( fileName.endsWith( DroolsCompilerAntTask.RULEFLOWMODELFILEEXTENSION ) || fileName.endsWith( DroolsCompilerAntTask.RULEFLOWFILEEXTENSION ) ) {
                builder.addRuleFlow( instream );
            } else if ( fileName.endsWith( DroolsCompilerAntTask.XMLFILEEXTENSION ) ) {
                builder.addPackageFromXml( instream );
            } else if ( fileName.endsWith( DroolsCompilerAntTask.XLSFILEEXTENSION ) ) {

                final SpreadsheetCompiler converter = new SpreadsheetCompiler();
                final String drl = converter.compile( new FileInputStream( file ),
                                                      InputType.XLS );

                System.out.println( drl );

                builder.addPackageFromDrl( new StringReader( drl ) );

            } else if ( fileName.endsWith( DroolsCompilerAntTask.DSLRFILEEXTENSION ) ) {
                DrlParser parser = new DrlParser();
                String expandedDRL = parser.getExpandedDRL( loadResource( fileName ),
                                                            resolveDSLFiles() );
                builder.addPackageFromDrl( new StringReader( expandedDRL ) );
            } else {
                builder.addPackageFromDrl( instream );
            }
        } finally {
            if ( instream != null ) {
                instream.close();
            }
        }
    }

    private String[] resolveDSLFilesToArray() {

        Collection<String> list = new ArrayList<String>();

        final File dir = new File( this.srcdir.getAbsolutePath() );

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir,
                                  String name) {
                return name.endsWith( ".dsl" );
            }
        };

        return dir.list( filter );
    }

    private DefaultExpanderResolver resolveDSLFiles() throws IOException {

        DefaultExpanderResolver resolver = new DefaultExpanderResolver();
        final File dir = new File( this.srcdir.getAbsolutePath() );
        DSLMappingFile file = new DSLTokenizedMappingFile();

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir,
                                  String name) {
                return name.endsWith( ".dsl" );
            }
        };

        String[] children = dir.list( filter );
        if ( children.length == 0 ) {
            throw new BuildException( "There are no DSL files for this directory:" + this.srcdir.getAbsolutePath() );
        }

        for ( int index = 0; index < children.length; index++ ) {
            if ( file.parseAndLoad( new StringReader( loadResource( children[index] ) ) ) ) {
                final Expander expander = new DefaultExpander();
                expander.addDSLMapping( file.getMapping() );
                resolver.addExpander( "*",
                                      expander );
            } else {
                throw new RuntimeDroolsException( "Error parsing and loading DSL file." + file.getErrors() );
            }
        }

        return resolver;
    }

    private String resolvePackageFile(String dirname) {

        File dir = new File( dirname );
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir,
                                  String name) {
                return name.endsWith( ".package" );
            }
        };

        String[] children = dir.list( filter );
        if ( children.length > 1 ) {
            throw new BuildException( "There are more than one package configuration file for this directory :" + dirname );
        }

        if ( children.length == 0 ) {
            throw new BuildException( "There is no package configuration file for this directory:" + dirname );
        }

        return children[0];
    }

    private String loadResource(final String name) throws IOException {

        final InputStream in = new FileInputStream( this.srcdir + "/" + name );
        final Reader reader = new InputStreamReader( in );
        final StringBuffer text = new StringBuffer();
        final char[] buf = new char[1024];
        int len = 0;

        while ( (len = reader.read( buf )) >= 0 ) {
            text.append( buf,
                         0,
                         len );
        }

        return text.toString();
    }

    /**
     * @return
     */
    private AntClassLoader getClassLoader() {
        // defining a new specialized classloader and setting it as the thread
        // context classloader
        AntClassLoader loader = null;
        if ( classpath != null ) {
            loader = new AntClassLoader( PackageBuilder.class.getClassLoader(),
                                         getProject(),
                                         classpath,
                                         false );
        } else {
            loader = new AntClassLoader( PackageBuilder.class.getClassLoader(),
                                         false );
        }
        loader.setThreadContextLoader();
        return loader;
    }

    /**
     * @param loader
     * @return
     */
    private PackageBuilder getPackageBuilder(AntClassLoader loader) {
        // creating package builder configured with the give classloader
        PackageBuilderConfiguration conf = new PackageBuilderConfiguration();
        conf.setClassLoader( loader );
        PackageBuilder builder = new PackageBuilder( conf );
        return builder;
    }

    /**
     * Returns the list of files to be added into the rulebase
     * 
     * @return
     */
    private String[] getFileList() {
        // scan source directory for rule files
        DirectoryScanner directoryScanner = getDirectoryScanner( srcdir );
        String[] fileNames = directoryScanner.getIncludedFiles();

        if ( fileNames == null || fileNames.length <= 0 ) {
            throw new BuildException( "No rule files found in include directory." );
        }
        return fileNames;
    }

    private KnowledgeBuilder getKnowledgeBuilder(AntClassLoader loader) {
        // creating package builder configured with the give classloader
        PackageBuilderConfiguration conf = new PackageBuilderConfiguration();
        conf.setClassLoader( loader );

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder( conf );
        return kbuilder;
    }

    public void setBinformat(String binformat) {
        this.binformat = binformat;
    }

    public String getBinformat() {
        return binformat;
    }

    public String getBintype() {
        return bintype;
    }

    public void setBintype(String bintype) {
        this.bintype = bintype;
    }

}
