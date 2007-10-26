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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.RuntimeDroolsException;
import org.drools.brms.client.modeldriven.brl.RuleModel;
import org.drools.brms.server.util.BRDRLPersistence;
import org.drools.brms.server.util.BRXMLPersistence;
import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.lang.Expander;
import org.drools.lang.dsl.DSLMappingFile;
import org.drools.lang.dsl.DefaultExpander;
import org.drools.lang.dsl.DefaultExpanderResolver;

/**
 * An ant task to allow rulebase compilation and serialization during a build.
 * 
 * @author etirelli
 */
public class DroolsCompilerAntTask extends MatchingTask {

	public static String BRLFILEEXTENSION = ".brl";
	public static String XMLFILEEXTENSION = ".xml";
	public static String RULEFLOWFILEEXTENSION = ".rfm";
	public static String DSLFILEEXTENSION = ".dslr";

	private File srcdir;
	private File toFile;
	private Path classpath;

	
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
		createClasspath().append(classpath);
	}

	/**
	 * Classpath to use, by reference, when compiling the rulebase
	 * 
	 * @param r
	 *            a reference to an existing classpath
	 */
	public void setClasspathref(Reference r) {
		createClasspath().setRefid(r);
	}

	/**
	 * Adds a path to the classpath.
	 * 
	 * @return created classpath
	 */
	public Path createClasspath() {
		if (this.classpath == null) {
			this.classpath = new Path(getProject());
		}
		return this.classpath.createPath();
	}

	/**
	 * Task's main method
	 */
	public void execute() throws BuildException {
		super.execute();

		// checking parameters are set
		if (toFile == null) {
			throw new BuildException(
					"Destination rulebase file does not specified.");
		}

		// checking parameters are set
		if (srcdir == null) {
			throw new BuildException("Source directory not specified.");
		}

		if (!srcdir.exists()) {
			throw new BuildException("Source directory does not exists."
					+ srcdir.getAbsolutePath());
		}

		AntClassLoader loader = null;
		try {
			// create a specialized classloader
			loader = getClassLoader();

			// create a package builder configured to use the given classloader
			PackageBuilder builder = getPackageBuilder(loader);

			// get the list of files to be added to the rulebase
			String[] fileNames = getFileList();

			for (int i = 0; i < fileNames.length; i++) {
				// compile rule file and add to the builder
				compileAndAddFile(builder, fileNames[i]);
			}

			// gets the package
			org.drools.rule.Package pkg = builder.getPackage();

			// creates the rulebase
			RuleBase ruleBase = RuleBaseFactory.newRuleBase();

			// adds the package
			ruleBase.addPackage(pkg);

			// serialize the rule base to the destination file
			serializeRulebase(ruleBase);

		} catch (Exception e) {
			throw new BuildException("RuleBaseTask failed: " + e.getMessage(),
					e);
		} finally {
			if (loader != null) {
				loader.resetThreadContextLoader();
			}
		}
	}

	/**
	 * @param ruleBase
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void serializeRulebase(RuleBase ruleBase)
			throws FileNotFoundException, IOException {
		ObjectOutputStream outstream = null;
		try {
			FileOutputStream fout = new FileOutputStream(toFile);
			outstream = new ObjectOutputStream(fout);
			outstream.writeObject(ruleBase);
		} finally {
			if (outstream != null) {
				outstream.close();
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
	private void compileAndAddFile(PackageBuilder builder, String fileName)
			throws FileNotFoundException, DroolsParserException, IOException {
		InputStreamReader instream = null;

		try {

			if (fileName.endsWith(DroolsCompilerAntTask.BRLFILEEXTENSION)) {

				RuleModel model = BRXMLPersistence.getInstance().unmarshal(
						loadResource(fileName));
				String packagefile = loadResource(resolvePackageFile(this.srcdir
						.getAbsolutePath()));
				model.name = fileName.replace(DroolsCompilerAntTask.BRLFILEEXTENSION,
						"");
				ByteArrayInputStream istream = new ByteArrayInputStream(
						(packagefile + BRDRLPersistence.getInstance().marshal(
								model)).getBytes());
				instream = new InputStreamReader(istream);

			} else {
				File file = new File(this.srcdir, fileName);
				instream = new InputStreamReader(new FileInputStream(file));
			}

			if (fileName.endsWith(DroolsCompilerAntTask.RULEFLOWFILEEXTENSION)) {
				builder.addRuleFlow(instream);
			} else if (fileName.endsWith(DroolsCompilerAntTask.XMLFILEEXTENSION)) {
				builder.addPackageFromXml(instream);
			} else if (fileName.endsWith(DroolsCompilerAntTask.DSLFILEEXTENSION)) {
				DrlParser parser = new DrlParser();
				String expandedDRL = parser.getExpandedDRL(
						loadResource(fileName), resolveDSLFiles());
				builder.addPackageFromDrl(new StringReader(expandedDRL));
			} else {
				builder.addPackageFromDrl(instream);
			}
		} finally {
			if (instream != null) {
				instream.close();
			}
		}
	}

	private DefaultExpanderResolver resolveDSLFiles() throws IOException {

		DefaultExpanderResolver resolver = new DefaultExpanderResolver();
		final File dir = new File(this.srcdir.getAbsolutePath());
		DSLMappingFile file = new DSLMappingFile();

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".dsl");
			}
		};

		String[] children = dir.list(filter);
		if (children.length == 0) {
			throw new BuildException(
					"There are no DSL files for this directory:"
							+ this.srcdir.getAbsolutePath());
		}

		for (int index = 0; index < children.length; index++) {
			if (file.parseAndLoad(new StringReader(loadResource(children[index])))) {
				final Expander expander = new DefaultExpander();
				expander.addDSLMapping(file.getMapping());
				resolver.addExpander("*", expander);
			} else {
				throw new RuntimeDroolsException(
						"Error parsing and loading DSL file."
								+ file.getErrors());
			}
		}

		return resolver;
	}

	private String resolvePackageFile(String dirname) {

		File dir = new File(dirname);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".package");
			}
		};

		String[] children = dir.list(filter);
		if (children.length > 1) {
			throw new BuildException(
					"There are more than one package configuration file for this directory :"
							+ dirname);
		}

		if (children.length == 0) {
			throw new BuildException(
					"There is no package configuration file for this directory:"
							+ dirname);
		}

		return children[0];
	}

	private String loadResource(final String name) throws IOException {

		final InputStream in = new FileInputStream(this.srcdir + "/" + name);
		final Reader reader = new InputStreamReader(in);
		final StringBuffer text = new StringBuffer();
		final char[] buf = new char[1024];
		int len = 0;

		while ((len = reader.read(buf)) >= 0) {
			text.append(buf, 0, len);
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
		if (classpath != null) {
			loader = new AntClassLoader(PackageBuilder.class.getClassLoader(),
					getProject(), classpath, false);
		} else {
			loader = new AntClassLoader(PackageBuilder.class.getClassLoader(),
					false);
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
		conf.setClassLoader(loader);
		PackageBuilder builder = new PackageBuilder(conf);
		return builder;
	}

	/**
	 * Returns the list of files to be added into the rulebase
	 * 
	 * @return
	 */
	private String[] getFileList() {
		// scan source directory for rule files
		DirectoryScanner directoryScanner = getDirectoryScanner(srcdir);
		String[] fileNames = directoryScanner.getIncludedFiles();

		if (fileNames == null || fileNames.length <= 0) {
			throw new BuildException(
					"No rule files found in include directory.");
		}
		return fileNames;
	}

}
