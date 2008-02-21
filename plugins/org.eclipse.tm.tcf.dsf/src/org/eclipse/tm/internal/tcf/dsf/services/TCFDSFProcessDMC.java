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
package org.eclipse.tm.internal.tcf.dsf.services;

import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.INativeProcesses.IProcessDMContext;
import org.eclipse.dd.dsf.service.IDsfService;

public abstract class TCFDSFProcessDMC extends AbstractDMContext implements IProcessDMContext {

    TCFDSFProcessDMC(IDsfService service, IDMContext[] parents) {
        super(service, parents);
    }
}
