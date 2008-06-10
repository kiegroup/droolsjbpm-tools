package org.drools.contrib;

import java.io.File;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.drools.verifier.Verifier;
import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.lang.descr.PackageDescr;

public class DroolsVerifierAntTask extends MatchingTask {

	public static final String DRLFILEEXTENSION = ".drl";

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
			
			// create a specialized classloader

			Verifier droolsanalyzer = new Verifier();

			// get the list of files to be added to the rulebase
			String[] fileNames = getFileList();

			for (int i = 0; i < fileNames.length; i++) {
				compileAndAnalyzeFile(droolsanalyzer, fileNames[i]);
			}
			
			droolsanalyzer.fireAnalysis();
			droolsanalyzer.writeComponentsHTML(toFile.getAbsolutePath() + "/");
			
			System.out.println("Writing verifier report to " + toFile.getAbsolutePath() + "/report");
			
		} catch (Exception e) {
			throw new BuildException("RuleBaseTask failed: " + e.getMessage(),
					e);
		}
	}
	private void compileAndAnalyzeFile(Verifier droolsanalyzer, String filename) throws DroolsParserException {
		
		// Verifier just works with drl files 
		if ( !filename.endsWith(DroolsVerifierAntTask.DRLFILEEXTENSION) ) {
			throw new UnsupportedOperationException();
		}
		
		PackageDescr descr = new DrlParser()
				.parse(new InputStreamReader(Verifier.class
						.getResourceAsStream(filename)));
		
		droolsanalyzer.addPackageDescr(descr);
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
