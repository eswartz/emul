/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Anna Dushistova (MontaVista) - [247164][tcf] a lot of file/directory properties are not supported
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.files;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;

public class TCFRemoteFile extends AbstractRemoteFile {

    public TCFRemoteFile(FileServiceSubSystem ss, IRemoteFileContext ctx, IRemoteFile parent, IHostFile file) {
        super(ss, ctx, parent, file);
    }

    @Override
    public IRemoteFile getParentRemoteFile() {
        if (this._parentFile == null) {
            if (isRoot()) return null;
            IRemoteFile parentFile = null;
            IRemoteFileSubSystem ss = _context.getParentRemoteFileSubSystem();
            if (ss != null) {
                IProgressMonitor monitor = new NullProgressMonitor();
                try {
                    parentFile = ss.getRemoteFileObject(getParentPath(), monitor);
                }
                catch (SystemMessageException e) {
                    SystemBasePlugin.logError("TCFRemoteFile.getParentRemoteFile()", e); //$NON-NLS-1$
                }
            }
            this._parentFile = parentFile;
        }
        return this._parentFile;
    }

    public String getCanonicalPath() {
        return getAbsolutePath();
    }

    public String getClassification() {
        String result;
        if (isFile()) {
                result = "file"; //$NON-NLS-1$
        } else if (isDirectory()) {
                result = "directory"; //$NON-NLS-1$
        } else {
                result = "unknown"; //default-fallback //$NON-NLS-1$
        }
        return result;
    }
}
