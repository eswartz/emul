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

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.services.files.HostFilePermissions;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;
import org.eclipse.tm.tcf.services.IFileSystem;


public class TCFFileResource extends AbstractResource implements IHostFile, IHostFilePermissionsContainer{

    private final TCFFileService service;
    private String parent;
    private String name;
    private final IFileSystem.FileAttrs attrs;
    private final boolean root;
    private IHostFilePermissions permissions;

    public TCFFileResource(TCFFileService service, String parent, String name,
            IFileSystem.FileAttrs attrs, boolean root) {
        if (name == null) {
            int i = parent.lastIndexOf('/');
            if (i > 0) {
                name  = parent.substring(i + 1);
                parent = parent.substring(0, i);
            }
            else if (i == 0) {
                name  = parent.substring(i + 1);
                parent = "/"; //$NON-NLS-1$
            }
            else {
                name = parent;
                parent = null;
            }
        }
        this.service = service;
        this.parent = parent;
        this.name = name;
        this.attrs = attrs;
        this.root = root;
        if (attrs != null) this.permissions = new HostFilePermissions(
                attrs.permissions, "" + attrs.uid, "" + attrs.gid); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String toLocalPath(String path) {
        if (path.length() > 1 && path.charAt(1) == ':') {
            return path.replace('/', '\\');
        }
        else {
            return path.replace('\\', '/');
        }
    }

    public boolean canRead() {
        return attrs != null && service.canRead(attrs);
    }

    public boolean canWrite() {
        return attrs != null && service.canWrite(attrs);
    }

    public boolean exists() {
        return attrs != null;
    }

    public synchronized String getAbsolutePath() {
        if (root) return toLocalPath(name);
        if (parent.endsWith("/")) return toLocalPath(parent + name); //$NON-NLS-1$
        return toLocalPath(parent + '/' + name);
    }

    public long getModifiedDate() {
        if (attrs == null) return 0;
        if ((attrs.flags & IFileSystem.ATTR_ACMODTIME) == 0) return 0;
        return attrs.mtime;
    }

    public synchronized String getName() {
        return toLocalPath(name);
    }

    public synchronized String getParentPath() {
        if (root) return null;
        return toLocalPath(parent);
    }

    public long getSize() {
        if (attrs == null) return 0;
        if ((attrs.flags & IFileSystem.ATTR_SIZE) == 0) return 0;
        return attrs.size;
    }

    public boolean isArchive() {
        return false;
    }

    public boolean isDirectory() {
        if (attrs == null) return false;
        return attrs.isDirectory();
    }

    public boolean isFile() {
        if (attrs == null) return false;
        return attrs.isFile();
    }

    public synchronized boolean isHidden() {
        return name.startsWith("."); //$NON-NLS-1$
    }

    public synchronized boolean isRoot() {
        return root;
    }

    public synchronized void renameTo(String path) {
        path = path.replace('\\', '/');
        if (path.equals("/")) { //$NON-NLS-1$
            parent = name = "/"; //$NON-NLS-1$
            return;
        }
        assert !path.endsWith("/"); //$NON-NLS-1$
        int i = path.lastIndexOf('/');
        parent = path.substring(0, i);
        name = path.substring(i + 1);
    }

    public IHostFilePermissions getPermissions() {
        return permissions;
    }

    public void setPermissions(IHostFilePermissions permissions) {
        this.permissions = permissions;
    }
}
