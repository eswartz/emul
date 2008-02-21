/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.ISelection;

public class TCFModelSelectionPolicy implements IModelSelectionPolicy {

    public boolean contains(ISelection selection, IPresentationContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSticky(ISelection selection, IPresentationContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean overrides(ISelection existing, ISelection candidate,
            IPresentationContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    public ISelection replaceInvalidSelection(ISelection invalidSelection,
            ISelection newSelection) {
        // TODO Auto-generated method stub
        return null;
    }
}
