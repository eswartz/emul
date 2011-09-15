/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.commands;

import org.eclipse.core.expressions.PropertyTester;


/**
 * Tester for property "org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled"
 * to enable reverse run control actions.
 */
public class TCFReverseDebuggingPropertyTester extends PropertyTester {

    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        // TODO should be queried from target
        return true;
    }

}
