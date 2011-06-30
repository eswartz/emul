/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate.PathMapRule;
import org.eclipse.tm.tcf.services.ILineNumbers;

/**
 * The TCF source lookup participant knows how to translate a ILineNumbers.CodeArea
 * into a source file name
 */
public class TCFSourceLookupParticipant extends AbstractSourceLookupParticipant {

    @SuppressWarnings("serial")
    private final LinkedHashMap<String,Object[]> cache = new LinkedHashMap<String,Object[]>(511, 0.75f, true) {
        @Override
        @SuppressWarnings("rawtypes")
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1023;
        }
    };

    @Override
    public void sourceContainersChanged(ISourceLookupDirector director) {
        cache.clear();
    }

    public String getSourceName(Object object) throws CoreException {
        if (object instanceof String) {
            return (String)object;
        }
        if (object instanceof ILineNumbers.CodeArea) {
            ILineNumbers.CodeArea area = (ILineNumbers.CodeArea)object;
            return toFileName(area);
        }
        return null;
    }

    public static String toFileName(ILineNumbers.CodeArea area) {
        if (area.directory != null && area.file != null && !isAbsolutePath(area.file)) {
            return area.directory + "/" + area.file;
        }
        return area.file;
    }

    private static boolean isAbsolutePath(String fnm) {
        if (fnm.length() == 0) return false;
        char ch = fnm.charAt(0);
        if (ch == '/' || ch == '\\') return true;
        if (fnm.length() >= 3 && fnm.charAt(1) == ':') {
            ch = fnm.charAt(2);
            if (ch == '/' || ch == '\\') return true;
        }
        return false;
    }

    private String applyPathMap(String fnm) {
        ILaunchConfiguration cfg = getDirector().getLaunchConfiguration();
        if (cfg == null) return fnm;
        try {
            String path_map = cfg.getAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, "");
            if (path_map.length() == 0) return fnm;
            ArrayList<PathMapRule> map = TCFLaunchDelegate.parsePathMapAttribute(path_map);
            for (PathMapRule r : map) {
                String src = r.getSource();
                if (!fnm.startsWith(src)) continue;
                String host = r.getHost();
                if (host != null && host.length() > 0) {
                    if (!InetAddress.getLocalHost().equals(InetAddress.getByName(host))) continue;
                }
                String dst = r.getDestination();
                if (dst == null || dst.length() == 0) continue;
                int l = src.length();
                if (dst.endsWith("/") && l < fnm.length() && fnm.charAt(l) == '/') l++;
                return dst + fnm.substring(l);
            }
            if (fnm.startsWith("/cygdrive/")) {
                fnm = fnm.substring(10, 11) + ":" + fnm.substring(11);
            }
            return fnm;
        }
        catch (Exception x) {
            return fnm;
        }
    }

    private Object[] findSource(String name) throws CoreException {
        name = applyPathMap(name);
        File file = new File(name);
        Object[] res;
        if (file.isAbsolute() && file.exists() && file.isFile()) {
            res = new Object[]{ new LocalFileStorage(file) };
        }
        else {
            res = super.findSourceElements(name);
            if (res == null || res.length == 0) {
                // Remove file path and search by file base name
                String base = name;
                int i = name.lastIndexOf('/');
                int j = name.lastIndexOf('\\');
                if (i > j) base = name.substring(i + 1);
                if (j > i) base = name.substring(j + 1);
                if (!base.equals(name)) res = super.findSourceElements(base);
            }
        }
        ArrayList<Object> list = new ArrayList<Object>();
        for (Object o : res) {
            if (o instanceof IStorage && !(o instanceof IFile)) {
                IPath path = ((IStorage)o).getFullPath();
                if (path != null) {
                    URI uri = URIUtil.toURI(path);
                    IFile[] arr = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
                    if (arr != null && arr.length > 0) {
                        int cnt = list.size();
                        for (IFile fileResource : arr) {
                            if (fileResource.isAccessible()) {
                                list.add(fileResource);
                            }
                        }
                        if (list.size() > cnt) continue;
                    }
                }
            }
            list.add(o);
        }
        return list.toArray(new Object[list.size()]);
    }

    @Override
    public Object[] findSourceElements(Object object) throws CoreException {
        String name = getSourceName(object);
        if (name == null) return null;
        if (cache.containsKey(name)) return cache.get(name);
        Object[] res = findSource(name);
        cache.put(name, res);
        return res;
    }
}
