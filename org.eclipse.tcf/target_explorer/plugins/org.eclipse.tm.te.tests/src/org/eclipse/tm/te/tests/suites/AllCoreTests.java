/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tests.suites;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.tm.te.tests.statushandler.StatusHandlerTestCase;

/**
 * All core test suites.
 */
public class AllCoreTests {

	/**
	 * Main method called if the tests are running as part of the nightly
	 * Workbench wheel. Use only the <code>junit.textui.TestRunner</code>
	 * here to execute the tests!
	 *
	 * @param args The command line arguments passed.
	 */
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Static method called by the several possible test runners to fetch
	 * the test(s) to run.
	 * Do not rename this method, otherwise tests will not be called anymore!
	 *
	 * @return Any object of type <code>Test</code> containing the test to run.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("All Target Explorer Core Tests"); //$NON-NLS-1$

		suite.addTest(StatusHandlerTestCase.getTestSuite());

		return suite;
	}
}
