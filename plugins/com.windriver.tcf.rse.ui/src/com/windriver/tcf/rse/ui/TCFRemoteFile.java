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
package com.windriver.tcf.rse.ui;

import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;

public class TCFRemoteFile extends AbstractRemoteFile {
    
    public TCFRemoteFile(FileServiceSubSystem ss, IRemoteFileContext ctx, IRemoteFile parent, IHostFile file) {
        super(ss, ctx, parent, file);
    }

    public String getCanonicalPath() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getClassification() {
        // TODO Auto-generated method stub
        return null;
    }
}
