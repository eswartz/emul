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

import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * Toggles enablement for reverse run control support.
 */
public class TCFReverseToggleCommand implements IReverseToggleHandler {

    public void canExecute(IEnabledStateRequest request) {
        request.setEnabled(false);
        request.done();
    }

    public boolean execute(IDebugCommandRequest request) {
        return false;
    }

    public boolean toggleNeedsUpdating() {
        return true;
    }

    public boolean isReverseToggled(Object context) {
        // TODO should be queried from target
        return true;
    }

}
