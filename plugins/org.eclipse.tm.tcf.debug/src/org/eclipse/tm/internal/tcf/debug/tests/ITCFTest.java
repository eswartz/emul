/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import org.eclipse.tm.tcf.services.IRunControl;

/**
 * Each (sub)test in TCF Test Suite should implement this interface.
 */
interface ITCFTest {

    /**
     * Start execution of the test.
     */
    void start();

    /**
     * Check if the test don't need the context to stay suspended.
     * @param ctx - run control context.
     * @return true if it is OK to resume the context.
     */
    boolean canResume(IRunControl.RunControlContext ctx);
}