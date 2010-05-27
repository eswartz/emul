/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.launch;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.tm.tcf.services.ILineNumbers;

/**
 * The TCF source lookup participant knows how to translate a ILineNumbers.CodeArea
 * into a source file name
 */
public class TCFSourceLookupParticipant extends AbstractSourceLookupParticipant {

    public String getSourceName(Object object) throws CoreException {
        if (object instanceof String) {
            return (String)object;
        }
        if (object instanceof ILineNumbers.CodeArea) {
            ILineNumbers.CodeArea area = (ILineNumbers.CodeArea)object;
            // TODO: map file path from remote file system to local
            if (area.directory != null && area.file != null) {
                String d = area.directory;
                if (d.startsWith("/cygdrive/")) {
                    d = d.substring(10, 11) + ":" + d.substring(11);
                }
                return new File(d, area.file).getAbsolutePath();
            }
            return area.file;
        }
        return null;
    }

    @Override
    public Object[] findSourceElements(Object object) throws CoreException {
        String name = getSourceName(object);
        if (name != null) {
            IPath path = new Path(name);
            if (path.isAbsolute()) {
                IFile[] arr = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
                if (arr != null && arr.length > 0) return arr;
            }
        }
        Object[] res = super.findSourceElements(name);
        if (name != null && (res == null || res.length == 0)) {
            // Remove file path and search by file base name
            String base = name;
            int i = name.lastIndexOf('/');
            int j = name.lastIndexOf('\\');
            if (i > j) base = name.substring(i + 1);
            if (j > i) base = name.substring(j + 1);
            res = super.findSourceElements(base);
        }
        ArrayList<Object> list = new ArrayList<Object>();
        for (Object o : res) {
            if (o instanceof LocalFileStorage) {
                IPath path = ((LocalFileStorage)o).getFullPath();
                IFile[] arr = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
                if (arr != null && arr.length > 0) {
                    for (Object x : arr) list.add(x);
                    continue;
                }
            }
            list.add(o);
        }
        return list.toArray(new Object[list.size()]);
    }
}
