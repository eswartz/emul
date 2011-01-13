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

import org.eclipse.cdt.debug.core.model.IReverseStepOverHandler;
import org.eclipse.tm.internal.tcf.debug.ui.commands.BackOverCommand;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;

/**
 * Debug command handler for reverse step over.
 */
public class TCFReverseStepOverCommand extends BackOverCommand
        implements IReverseStepOverHandler {

    public TCFReverseStepOverCommand(TCFModel model) {
        super(model);
    }
}
