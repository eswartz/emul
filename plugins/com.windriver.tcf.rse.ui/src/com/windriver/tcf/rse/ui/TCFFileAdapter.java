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
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;

public class TCFFileAdapter implements IHostFileToRemoteFileAdapter {

    public IRemoteFile convertToRemoteFile(FileServiceSubSystem ss,
            IRemoteFileContext ctx, IRemoteFile parent, IHostFile node) {
        return new TCFRemoteFile(ss, ctx, parent, node);
    }

    public IRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss,
            IRemoteFileContext ctx, IRemoteFile parent, IHostFile[] nodes) {
        if (nodes == null) return null;
        IRemoteFile[] res = new IRemoteFile[nodes.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new TCFRemoteFile(ss, ctx, parent, nodes[i]);
        }
        return res;
    }

}
