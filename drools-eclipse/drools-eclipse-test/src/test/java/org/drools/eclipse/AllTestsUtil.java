package org.drools.eclipse;

//Copyright (c) 2006 Alex Blewitt
//All rights reserved. This program and the accompanying materials
//are made available under the terms of the Eclipse Public License v1.0
//which accompanies this distribution, and is available at
//http://www.eclipse.org/legal/epl-v10.html
//
//Contributors:
//Alex Blewitt - Initial API and implementation
//
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.runner.ClassPathTestCollector;
import junit.runner.TestCollector;
import org.eclipse.core.runtime.Platform;
/**
* Run all the tests in this project, either computed from the classpath or from
* the bundlepath. To use this as-is, drop it into a non-default package that
* has the same name as the plugin. For example, if the plugin is called
* <code>org.example.foo</code>, this should be placed in a package
* <code>org.example.foo</code>, and all tests should live under the
* <code>org.example.foo</code> package structure (either directly, or in any
* subpackage). By default this will include all non-abstract classes named
* <code>XxxTest</code>, excluding <code>XxxPlatformTest</code> if running
* outside of the platform.
*/

public class AllTestsUtil {
	/**
	 * Detects classes from the bundle PLUGIN_NAME's entries. Uses
	 * <code>bundle.findEntries</code> to obtain a list of classes that live
	 * in the specified PACKAGE_NAME, and adds those to the test path, providing
	 * that they are {@link AllTests#isValidTest(String, boolean) valid}.
	 */
	private static class BundleTestDetector implements TestCollector {
		/*
		 * @see junit.runner.TestCollector#collectTests()
		 */
		public Enumeration collectTests() {
			final Vector tests = new Vector();
			try {
				Enumeration entries = Platform.getBundle(PLUGIN_NAME).findEntries("/", "*" + SUFFIX + ".class", true);
				while (entries.hasMoreElements()) {
					URL entry = (URL) entries.nextElement();
					// Change the URLs to have Java class names
					String path = entry.getPath().replace('/', '.');
					int start = path.indexOf(PACKAGE_NAME);
					String name = path.substring(start, path.length()
							- ".class".length());
					if (isValidTest(name, true)) {
						tests.add(name);
					}
				}
			} catch (Exception e) {
				// If we get here, the Platform isn't installed and so we fail
				// quietly. This isn't a problem; we might be outside of the
				// Platform framework and just running tests locally. It's not
				// even worth printing anything out to the error log as it would
				// just confuse people investigating stack traces etc.
			}
			return tests.elements();
		}
	}
	/**
	 * Searches the current classpath for tests, which are those ending with
	 * SUFFIX, excluding those which end in IN_CONTAINER_SUFFIX, providing that
	 * they are {@link AllTests#isValidTest(String, boolean) valid}.
	 */
	private static class ClassFileDetector extends ClassPathTestCollector {
		/*
		 * @see junit.runner.ClassPathTestCollector#isTestClass(java.lang.String)
		 */
		protected boolean isTestClass(String classFileName) {
			return classFileName.endsWith(SUFFIX + ".class")
					&& isValidTest(classNameFromFile(classFileName), false);
		}
	}
	/**
	 * All tests should end in XxxTest
	 */
	public static final String SUFFIX = "Test";
	/**
	 * All in-container tests should end in XxxPlatformTest
	 */
	public static final String IN_CONTAINER_SUFFIX = "Platform" + SUFFIX;
	/**
	 * The base package name of the tests to run. This defaults to the name of
	 * the package that the AllTests class is in for ease of management but may
	 * be trivially changed if required. Note that at least some identifiable
	 * part must be provided here (so default package names are not allowed)
	 * since the URL that comes up in the bundle entries have a prefix that is
	 * not detectable automatically. Even if this is "org" or "com" that should
	 * be enough.
	 */
	public static final String PACKAGE_NAME = AllTests.class.getPackage()
			.getName();
	/**
	 * The name of the plugin to search if the platform is loaded. This defaults
	 * to the name of the package that the AllTests class is in for ease of
	 * management but may be trivially changed if required.
	 */
	
	//PO: this is wrong. we need to use the PLUGIN_ID of the host, not that of the 
	// fragment
//	public static final String PLUGIN_NAME = AllTests.class.getPackage()
//	.getName();
	public static final String PLUGIN_NAME = "org.drools.eclipse";
	
	/**
	 * Add the tests reported by collector to the list of tests to run
	 * @param collector the test collector to run
	 * @param suite the suite to add the tests to
	 */
	private static void addTestsToSuite(TestCollector collector, TestSuite suite) {
		Enumeration e = collector.collectTests();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			try {
				suite.addTestSuite(Class.forName(name));
			} catch (ClassNotFoundException e1) {
				System.err.println("Cannot load test: " + e1);
			}
		}
	}
	/**
	 * Is the test a valid test?
	 * @param name the name of the test
	 * @param inContainer true if we want to include the inContainer tests
	 * @return true if the name is a valid class (can be loaded), that it is not
	 *         abstract, and that it ends with SUFFIX, and that either
	 *         inContainer tests are to be included or the name does not end
	 *         with IN_CONTAINER_SUFFIX
	 */
	private static boolean isValidTest(String name, boolean inContainer) {
		try {
			return name.endsWith(SUFFIX)
					&& (inContainer || !name.endsWith(IN_CONTAINER_SUFFIX))
					&& ((Class.forName(name).getModifiers() & Modifier.ABSTRACT) == 0);
		} catch (ClassNotFoundException e) {
			System.err.println(e.toString());
			return false;
		}
	}
	/**
	 * Return all the tests. If we're in a platform, return everything. If not,
	 * we return those tests that end in SUFFIX but excluding those ending in
	 * IN_CONTAINER_SUFFIX.
	 * @return a suite of tests for JUnit to run
	 * @throws Error if there are no tests to run.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		addTestsToSuite(new ClassFileDetector(), suite);
		addTestsToSuite(new BundleTestDetector(), suite);
		if (suite.countTestCases() == 0) {
			throw new Error("There are no test cases to run");
		} else {
			return suite;
		}
	}
}
