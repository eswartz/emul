/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.dsf.core.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.windriver.debug.tcf.core.launch.TCFLaunchDelegate;

public class TCFDSFLaunchDelegate extends TCFLaunchDelegate {

    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
        return new TCFDSFLaunch(configuration, mode);
    }
}
