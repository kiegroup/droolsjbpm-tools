package org.drools.contrib;

import java.io.File;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.drools.compiler.PackageBuilder;

public class DroolsAnalyticsAntTask extends MatchingTask {

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

		try {
/* *** Uncomment for analytics support			
			
			// create a specialized classloader
			AntClassLoader loader = getClassLoader();

			Analyzer droolsanalyzer = new Analyzer();

			// get the list of files to be added to the rulebase
			String[] fileNames = getFileList();

			for (int i = 0; i < fileNames.length; i++) {
				compileAndAnalyzeFile(droolsanalyzer, fileNames[i]);
			}
			
			droolsanalyzer.fireAnalysis();
			droolsanalyzer.writeComponentsHTML(toFile.getAbsolutePath() + "/");
			
			System.out.println("Writing analytics report to " + toFile.getAbsolutePath() + "/report");
			
*/ 			
			
		} catch (Exception e) {
			throw new BuildException("RuleBaseTask failed: " + e.getMessage(),
					e);
		}
	}
	/* *** Uncomment for analytics support
	private void compileAndAnalyzeFile(Analyzer droolsanalyzer, String filename) throws DroolsParserException {
		PackageDescr descr = new DrlParser()
				.parse(new InputStreamReader(Analyzer.class
						.getResourceAsStream(filename)));
		
		droolsanalyzer.addPackageDescr(descr);
	}
	*/


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
