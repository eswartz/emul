/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tests.statushandler;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.tm.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tm.te.tests.CoreTestCase;
import org.eclipse.tm.te.tests.interfaces.IInterruptCondition;

/**
 * Target Explorer: Status handler test cases.
 */
public class StatusHandlerTestCase extends CoreTestCase {

	/**
	 * Provides a test suite to the caller which combines all single
	 * test bundled within this category.
	 *
	 * @return Test suite containing all test for this test category.
	 */
	public static Test getTestSuite() {
		TestSuite testSuite = new TestSuite("Test status handler contributions"); //$NON-NLS-1$

			// add ourself to the test suite
			testSuite.addTestSuite(StatusHandlerTestCase.class);

		return testSuite;
	}

	/**
	 * Test the basic status handler extension contribution mechanism.
	 */
	public void testContributions() {
		assertNotNull("Unexpected return value 'null'.", StatusHandlerManager.getInstance()); //$NON-NLS-1$

		int testHandlerCount = 0;

		IStatusHandler[] handlers = StatusHandlerManager.getInstance().getHandlers(false);
		for (IStatusHandler handler : handlers) {
			if (handler.getId().startsWith("org.eclipse.tm.te.tests")) { //$NON-NLS-1$
				testHandlerCount++;
			}
		}

		assertEquals("Unexpected number of contributed test status handler.", 2, testHandlerCount); //$NON-NLS-1$
	}

	/**
	 * Test status handler extension contribution mechanism with context objects.
	 */
	public void testContributionsWithContext() {
		assertNotNull("Unexpected return value 'null'.", StatusHandlerManager.getInstance()); //$NON-NLS-1$

		List<String> handlerIds = new ArrayList<String>();

		IStatusHandler[] handlers = StatusHandlerManager.getInstance().getHandler(this);
		for (IStatusHandler handler : handlers) {
			handlerIds.add(handler.getId());
		}

		assertTrue("Global enabled test status handler not active.", handlerIds.contains("org.eclipse.tm.te.tests.handler1")); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("Context enabled test status handler is active.", handlerIds.contains("org.eclipse.tm.te.tests.handler2")); //$NON-NLS-1$ //$NON-NLS-2$

		handlerIds.clear();

		IInterruptCondition context = new IInterruptCondition() {

			@Override
			public boolean isTrue() {
				return false;
			}

			@Override
			public void dispose() {
			}
		};

		handlers = StatusHandlerManager.getInstance().getHandler(context);
		for (IStatusHandler handler : handlers) {
			handlerIds.add(handler.getId());
		}

		assertTrue("Global enabled test status handler not active.", handlerIds.contains("org.eclipse.tm.te.tests.handler1")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Context enabled test status handler not active.", handlerIds.contains("org.eclipse.tm.te.tests.handler2")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
