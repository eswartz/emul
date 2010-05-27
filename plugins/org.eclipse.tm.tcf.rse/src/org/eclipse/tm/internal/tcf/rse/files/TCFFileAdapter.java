/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 * Martin Oberhuber (Wind River) - [238564] Adopt TM 3.0 APIs
 ******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.files;

import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;

public class TCFFileAdapter implements IHostFileToRemoteFileAdapter {

    public AbstractRemoteFile convertToRemoteFile(FileServiceSubSystem ss,
            IRemoteFileContext ctx, IRemoteFile parent, IHostFile node) {
        return new TCFRemoteFile(ss, ctx, parent, node);
    }

    public AbstractRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss,
            IRemoteFileContext ctx, IRemoteFile parent, IHostFile[] nodes) {
        if (nodes == null) return null;
        TCFRemoteFile[] res = new TCFRemoteFile[nodes.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new TCFRemoteFile(ss, ctx, parent, nodes[i]);
        }
        return res;
    }
}
