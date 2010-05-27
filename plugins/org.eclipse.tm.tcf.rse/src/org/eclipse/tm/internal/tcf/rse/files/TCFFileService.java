/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Martin Oberhuber (Wind River) - [238564] Adopt TM 3.0 APIs
 *     Uwe Stieber (Wind River) - [271224] NPE in TCFFileService#download
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *     Uwe Stieber (Wind River) - [274277] The TCF file service subsystem implementation is not updating the progress monitor
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationFailedException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.tm.internal.tcf.rse.Activator;
import org.eclipse.tm.internal.tcf.rse.ITCFSubSystem;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorService;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorServiceManager;
import org.eclipse.tm.internal.tcf.rse.TCFRSETask;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tm.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tm.tcf.util.TCFFileInputStream;
import org.eclipse.tm.tcf.util.TCFFileOutputStream;


public class TCFFileService extends AbstractFileService {

    private final TCFConnectorService connector;

    private UserInfo user_info;

    private static final class UserInfo {
        final int r_uid;
        final int e_uid;
        final int r_gid;
        final int e_gid;
        final String home;

        final Throwable error;

        UserInfo(int r_uid, int e_uid, int r_gid, int e_gid, String home) {
            this.r_uid = r_uid;
            this.e_uid = e_uid;
            this.r_gid = r_gid;
            this.e_gid = e_gid;
            this.home = home;
            error = null;
        }

        UserInfo(Throwable error) {
            this.error = error;
            r_uid = -1;
            e_uid = -1;
            r_gid = -1;
            e_gid = -1;
            home = null;
        }
    }

    public TCFFileService(IHost host) {
        connector = (TCFConnectorService)TCFConnectorServiceManager
            .getInstance().getConnectorService(host, ITCFSubSystem.class);
    }

    @Override
    public String getDescription() {
        return "The TCF File Service uses the Target Communication Framework to provide service" + //$NON-NLS-1$
            "for the Files subsystem. It requires a TCF agent to be running on the remote machine."; //$NON-NLS-1$
    }

    public SystemMessage getMessage(Throwable x) {
        return new SimpleSystemMessage(Activator.PLUGIN_ID,
                SystemMessage.ERROR, x.getMessage(), x);
    }

    @Override
    public String getName() {
        return "TCF File Service"; //$NON-NLS-1$
    }

    private String toRemotePath(String parent, String name) throws SystemMessageException {
        assert !Protocol.isDispatchThread();
        String s  = null;
        if (parent != null) parent = parent.replace('\\', '/');
        if (name != null) name = name.replace('\\', '/');
        if (parent == null || parent.length() == 0) s = name;
        else if (name == null || name.equals(".")) s = parent; //$NON-NLS-1$
        else if (name.equals("/")) s = parent; //$NON-NLS-1$
        else if (parent.endsWith("/")) s = parent + name; //$NON-NLS-1$
        else s = parent + '/' + name;
        if (s.startsWith("./") || s.equals(".")) { //$NON-NLS-1$ //$NON-NLS-2$
            UserInfo ui = getUserInfo();
            if (ui.error != null) throw new SystemMessageException(getMessage(ui.error));
            s = ui.home.replace('\\', '/') + s.substring(1);
        }
        while (s.endsWith("/.")) s = s.substring(0, s.length() - 2); //$NON-NLS-1$
        return s;
    }

    public void copy(String srcParent,
            String srcName, String tgtParent, String tgtName, IProgressMonitor monitor)
            throws SystemMessageException {
        final String src = toRemotePath(srcParent, srcName);
        final String tgt = toRemotePath(tgtParent, tgtName);
        new TCFRSETask<Boolean>() {
            public void run() {
                IFileSystem fs = connector.getFileSystemService();
                fs.copy(src, tgt, false, false, new IFileSystem.DoneCopy() {
                    public void doneCopy(IToken token, FileSystemException error) {
                        if (error != null) error(error);
                        else done(Boolean.TRUE);
                    }
                });
            }
        }.getS(monitor, "Copy: " + srcName); //$NON-NLS-1$
    }

    public void copyBatch(String[] srcParents,
            String[] srcNames, String tgtParent, IProgressMonitor monitor) throws SystemMessageException {
        for (int i = 0; i < srcParents.length; i++) {
            copy(srcParents[i], srcNames[i], tgtParent, srcNames[i], monitor);
        }
    }

    public IHostFile createFile(String parent,
            String name, IProgressMonitor monitor) throws SystemMessageException {
        try {
            getOutputStream(parent, name, true, monitor).close();
            return getFile(parent, name, monitor);
        }
        catch (IOException e) {
            throw new SystemMessageException(getMessage(e));
        }
    }

    public IHostFile createFolder(final String parent, final String name, IProgressMonitor monitor) throws SystemMessageException {
        final String path = toRemotePath(parent, name);
        return new TCFRSETask<IHostFile>() {
            public void run() {
                final IFileSystem fs = connector.getFileSystemService();
                fs.mkdir(path, null, new IFileSystem.DoneMkDir() {
                    public void doneMkDir(IToken token, FileSystemException error) {
                        if (error != null) {
                            error(error);
                            return;
                        }
                        fs.stat(path, new IFileSystem.DoneStat() {
                            public void doneStat(IToken token,
                                    FileSystemException error, FileAttrs attrs) {
                                if (error != null) error(error);
                                else done(new TCFFileResource(TCFFileService.this,
                                        path, null, attrs, false));
                            }
                        });
                    }
                });
            }
        }.getS(monitor, "Create folder"); //$NON-NLS-1$
    }

    public void delete(String parent,
            String name, IProgressMonitor monitor) throws SystemMessageException {
        final String path = toRemotePath(parent, name);
        new TCFRSETask<Boolean>() {
            public void run() {
                final IFileSystem fs = connector.getFileSystemService();
                fs.stat(path, new IFileSystem.DoneStat() {
                    public void doneStat(IToken token,
                            FileSystemException error, FileAttrs attrs) {
                        if (error != null) {
                            error(error);
                            return;
                        }
                        IFileSystem.DoneRemove done = new IFileSystem.DoneRemove() {
                            public void doneRemove(IToken token, FileSystemException error) {
                                if (error != null) {
                                    error(error);
                                    return;
                                }
                                done(Boolean.TRUE);
                            }
                        };
                        if (attrs.isDirectory()) {
                            fs.rmdir(path, done);
                        }
                        else {
                            fs.remove(path, done);
                        }
                    }
                });
            }
        }.getS(monitor, "Delete"); //$NON-NLS-1$
    }

    @Override
    public void deleteBatch(String[] remoteParents, String[] fileNames,
            IProgressMonitor monitor)
            throws SystemMessageException {
        for (int i = 0; i < remoteParents.length; i++) {
            delete(remoteParents[i], fileNames[i], monitor);
        }
    }

    public void download(final String parent,
            final String name, final File file, final boolean is_binary,
            final String host_encoding, IProgressMonitor monitor) throws SystemMessageException {
        IHostFile hostFile = getFile(parent, name, new NullProgressMonitor());
        monitor.beginTask("Downloading " + toRemotePath(parent, name) + " ...", Long.valueOf(hostFile.getSize() / 1024).intValue()); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            file.getParentFile().mkdirs();
            InputStream inp = getInputStream(parent, name, is_binary, new NullProgressMonitor());
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            copyStream(inp, out, is_binary, "UTF8", host_encoding, monitor); //$NON-NLS-1$
        }
        catch (Exception x) {
            if (x instanceof SystemMessageException) throw (SystemMessageException)x;
            throw new SystemOperationFailedException(Activator.PLUGIN_ID, x);
        }
        finally {
            monitor.done();
        }
    }

    @Override
    public String getEncoding(IProgressMonitor monitor) throws SystemMessageException {
        return "UTF8"; //$NON-NLS-1$
    }

    public IHostFile getFile(final String parent,
            final String name, IProgressMonitor monitor) throws SystemMessageException {
        final String path = toRemotePath(parent, name);
        return new TCFRSETask<IHostFile>() {
            public void run() {
                IFileSystem fs = connector.getFileSystemService();
                fs.stat(path, new IFileSystem.DoneStat() {
                    public void doneStat(IToken token,
                            FileSystemException error, FileAttrs attrs) {
                        if (error != null) {
                            if (error.getStatus() == IFileSystem.STATUS_NO_SUCH_FILE) {
                                done(new TCFFileResource(TCFFileService.this, path, null, null, false));
                                return;
                            }
                            error(error);
                            return;
                        }
                        done(new TCFFileResource(TCFFileService.this, path, null, attrs, false));
                    }
                });
            }
        }.getS(monitor, "Stat"); //$NON-NLS-1$
    }

    @Override
    protected IHostFile[] internalFetch(final String parent, final String filter, final int fileType, final IProgressMonitor monitor)
            throws SystemMessageException {
        final String path = toRemotePath(parent, null);
        final boolean wantFiles = (fileType==FILE_TYPE_FILES_AND_FOLDERS || (fileType&FILE_TYPE_FILES)!=0);
        final boolean wantFolders = (fileType==FILE_TYPE_FILES_AND_FOLDERS || (fileType%FILE_TYPE_FOLDERS)!=0);
        return new TCFRSETask<IHostFile[]>() {
            private IMatcher matcher = null;
            public void run() {
                if (filter == null) {
                    matcher = null;
                }
                else if (filter.endsWith(",")) { //$NON-NLS-1$
                    String[] types = filter.split(","); //$NON-NLS-1$
                    matcher = new FileTypeMatcher(types, true);
                }
                else {
                    matcher = new NamePatternMatcher(filter, true, true);
                }
                final List<TCFFileResource> results = new ArrayList<TCFFileResource>();
                final IFileSystem fs = connector.getFileSystemService();
                fs.opendir(path, new IFileSystem.DoneOpen() {
                    public void doneOpen(IToken token, FileSystemException error, final IFileHandle handle) {
                        if (error != null) {
                            error(error);
                            return;
                        }
                        fs.readdir(handle, new IFileSystem.DoneReadDir() {
                            public void doneReadDir(IToken token,
                                    FileSystemException error, DirEntry[] entries, boolean eof) {
                                if (error != null) {
                                    error(error);
                                    return;
                                }
                                for (DirEntry e : entries) {
                                    if (e.attrs == null) {
                                        // Attrs are not available if, for example,
                                        // the entry is a broken symbolic link
                                    }
                                    else if (e.attrs.isDirectory()) {
                                        // dont filter folder names if getting both folders and files
                                        if (wantFolders && (matcher==null || fileType==FILE_TYPE_FILES_AND_FOLDERS || matcher.matches(e.filename))) {
                                            results.add(new TCFFileResource(TCFFileService.this,
                                                    path, e.filename, e.attrs, false));
                                        }
                                    }
                                    else if (e.attrs.isFile()) {
                                        if (wantFiles && (matcher == null || matcher.matches(e.filename))) {
                                            results.add(new TCFFileResource(TCFFileService.this,
                                                    path, e.filename, e.attrs, false));
                                        }
                                    }
                                }
                                if (eof) {
                                    fs.close(handle, new IFileSystem.DoneClose() {
                                        public void doneClose(IToken token, FileSystemException error) {
                                            if (error != null) {
                                                error(error);
                                                return;
                                            }
                                            done(results.toArray(new TCFFileResource[results.size()]));
                                        }
                                    });
                                }
                                else {
                                    fs.readdir(handle, this);
                                }
                            }
                        });
                    }
                });
            }
        }.getS(monitor, "Get files and folders"); //$NON-NLS-1$
    }

    @Override
    public InputStream getInputStream(final String parent, final String name, boolean isBinary, IProgressMonitor monitor)
            throws SystemMessageException {
        final String path = toRemotePath(parent, name);
        final IFileHandle handle = new TCFRSETask<IFileHandle>() {
            public void run() {
                IFileSystem fs = connector.getFileSystemService();
                fs.open(path, IFileSystem.TCF_O_READ, null, new IFileSystem.DoneOpen() {
                    public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
                        if (error != null) error(error);
                        else done(handle);
                    }
                });
            }
        }.getS(monitor, "Get input stream"); //$NON-NLS-1$
        return new TCFFileInputStream(handle);
    }

    @Override
    public OutputStream getOutputStream(final String parent, final String name, boolean isBinary, IProgressMonitor monitor)
            throws SystemMessageException {
        final String path = toRemotePath(parent, name);
        final IFileHandle handle = new TCFRSETask<IFileHandle>() {
            public void run() {
                IFileSystem fs = connector.getFileSystemService();
                int flags = IFileSystem.TCF_O_WRITE | IFileSystem.TCF_O_CREAT | IFileSystem.TCF_O_TRUNC;
                fs.open(path, flags, null, new IFileSystem.DoneOpen() {
                    public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
                        if (error != null) error(error);
                        else done(handle);
                    }
                });
            }
        }.getS(monitor, "Get output stream"); //$NON-NLS-1$
        return new TCFFileOutputStream(handle);
    }

    public IHostFile[] getRoots(IProgressMonitor monitor) throws SystemMessageException {
        return new TCFRSETask<IHostFile[]>() {
            public void run() {
                IFileSystem fs = connector.getFileSystemService();
                fs.roots(new IFileSystem.DoneRoots() {
                    public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
                        if (error != null) {
                            error(error);
                            return;
                        }
                        List<TCFFileResource> l = new ArrayList<TCFFileResource>();
                        for (DirEntry e : entries) {
                            if (e.attrs == null) continue;
                            l.add(new TCFFileResource(TCFFileService.this, null, e.filename, e.attrs, true));
                        }
                        done(l.toArray(new IHostFile[l.size()]));
                    }
                });
            }
        }.getS(monitor, "Get roots"); //$NON-NLS-1$
    }

    public IHostFile getUserHome() {
        UserInfo ui = getUserInfo();
        try {
            return getFile(ui.home, ".", new NullProgressMonitor()); //$NON-NLS-1$
        }
        catch (SystemMessageException e) {
            throw new Error(e);
        }
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public void move(final String srcParent,
            final String srcName, final String tgtParent, final String tgtName, IProgressMonitor monitor)
            throws SystemMessageException {
        final String src_path = toRemotePath(srcParent, srcName);
        final String tgt_path = toRemotePath(tgtParent, tgtName);
        new TCFRSETask<Boolean>() {
            public void run() {
                IFileSystem fs = connector.getFileSystemService();
                fs.rename(src_path, tgt_path, new IFileSystem.DoneRename() {
                    public void doneRename(IToken token, FileSystemException error) {
                        if (error != null) error(error);
                        else done(Boolean.TRUE);
                    }
                });
            }
        }.getS(monitor, "Move"); //$NON-NLS-1$
    }

    public void rename(String remoteParent,
            String oldName, String newName, IProgressMonitor monitor) throws SystemMessageException {
        move(remoteParent, oldName, remoteParent, newName, monitor);
    }

    public void rename(String remoteParent,
            String oldName, String newName, IHostFile oldFile, IProgressMonitor monitor)
            throws SystemMessageException {
        move(remoteParent, oldName, remoteParent, newName, monitor);
        oldFile.renameTo(toRemotePath(remoteParent, newName));
    }

    public void setLastModified(final String parent,
            final String name, final long timestamp, IProgressMonitor monitor) throws SystemMessageException {
        final String path = toRemotePath(parent, name);
        new TCFRSETask<Boolean>() {
            public void run() {
                IFileSystem fs = connector.getFileSystemService();
                FileAttrs attrs = new FileAttrs(IFileSystem.ATTR_ACMODTIME,
                        0, 0, 0, 0, timestamp, timestamp, null);
                fs.setstat(path, attrs, new IFileSystem.DoneSetStat() {
                    public void doneSetStat(IToken token, FileSystemException error) {
                        if (error != null) error(error);
                        else done(Boolean.TRUE);
                    }
                });
            }
        }.getS(monitor, "Set modification time"); //$NON-NLS-1$
    }

    public void setReadOnly(final String parent,
            final String name, final boolean readOnly, IProgressMonitor monitor) throws SystemMessageException {
        final String path = toRemotePath(parent, name);
        new TCFRSETask<Boolean>() {
            public void run() {
                final IFileSystem fs = connector.getFileSystemService();
                fs.stat(path, new IFileSystem.DoneStat() {
                    public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
                        if (error != null) {
                            error(error);
                            return;
                        }
                        int p = attrs.permissions;
                        if (readOnly) {
                            p &= ~IFileSystem.S_IWUSR;
                            p &= ~IFileSystem.S_IWGRP;
                            p &= ~IFileSystem.S_IWOTH;
                        }
                        else {
                            p |= IFileSystem.S_IWUSR;
                            p |= IFileSystem.S_IWGRP;
                            p |= IFileSystem.S_IWOTH;
                        }
                        FileAttrs new_attrs = new FileAttrs(IFileSystem.ATTR_PERMISSIONS,
                                0, 0, 0, p, 0, 0, null);
                        fs.setstat(path, new_attrs, new IFileSystem.DoneSetStat() {
                            public void doneSetStat(IToken token, FileSystemException error) {
                                if (error != null) error(error);
                                else done(Boolean.TRUE);
                            }
                        });
                    }
                });
            }
        }.getS(monitor, "Set permissions"); //$NON-NLS-1$
    }

    public void upload(InputStream inp,
            String parent, String name, boolean isBinary,
            String hostEncoding, IProgressMonitor monitor) throws SystemMessageException {
        monitor.beginTask("Upload", 1); //$NON-NLS-1$
        try {
            OutputStream out = getOutputStream(parent, name, isBinary, new NullProgressMonitor());
            // As we cannot determine the local file size, redirect the worked ticks to a NullProgressMonitor.
            copyStream(inp, out, isBinary, hostEncoding, "UTF8", new NullProgressMonitor()); //$NON-NLS-1$
        }
        catch (Throwable x) {
            if (x instanceof SystemMessageException) throw (SystemMessageException)x;
            throw new SystemMessageException(getMessage(x));
        }
        finally {
            monitor.done();
        }
    }

    public void upload(File localFile,
            String parent, String name, boolean isBinary,
            String srcEncoding, String hostEncoding, IProgressMonitor monitor)
            throws SystemMessageException {
        monitor.beginTask("Uploading " + localFile.toString() + " ...", Long.valueOf(localFile.length() / 1024).intValue()); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            OutputStream out = getOutputStream(parent, name, isBinary, new NullProgressMonitor());
            InputStream inp = new BufferedInputStream(new FileInputStream(localFile));
            copyStream(inp, out, isBinary, hostEncoding, "UTF8", monitor); //$NON-NLS-1$
        }
        catch (Throwable x) {
            if (x instanceof SystemMessageException) throw (SystemMessageException)x;
            throw new SystemMessageException(getMessage(x));
        }
        finally {
            monitor.done();
        }
    }

    private void copyStream(InputStream inp, OutputStream out,
            boolean is_binary, String inp_encoding, String out_encoding, IProgressMonitor monitor) throws IOException {
        try {
            if (!is_binary) {
                if (inp_encoding == null || inp_encoding.equals("UTF-8")) inp_encoding = "UTF8"; //$NON-NLS-1$ //$NON-NLS-2$
                if (out_encoding == null || out_encoding.equals("UTF-8")) out_encoding = "UTF8"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (is_binary || inp_encoding.equals(out_encoding)) {
                byte[] buf = new byte[0x1000];
                for (;;) {
                    int buf_len = inp.read(buf);
                    if (buf_len < 0) break;
                    out.write(buf, 0, buf_len);
                    if (monitor != null) monitor.worked(buf_len / 1024);
                }
            }
            else {
                Reader reader = new InputStreamReader(inp, inp_encoding);
                Writer writer = new OutputStreamWriter(out, out_encoding);
                char[] buf = new char[0x1000];
                for (;;) {
                    int buf_len = reader.read(buf);
                    if (buf_len < 0) break;
                    writer.write(buf, 0, buf_len);
                    if (monitor != null) monitor.worked(buf_len / 1024);
                }
                writer.flush();
            }
        }
        finally {
            out.close();
            inp.close();
        }
    }

    private synchronized UserInfo getUserInfo() {
        if (user_info == null || user_info.error != null) {
            user_info = new TCFRSETask<UserInfo>() {
                public void run() {
                    IFileSystem fs = connector.getFileSystemService();
                    fs.user(new IFileSystem.DoneUser() {
                        public void doneUser(IToken token, FileSystemException error, int real_uid,
                                int effective_uid, int real_gid, int effective_gid, String home) {
                            if (error != null) done(new UserInfo(error));
                            else done(new UserInfo(real_uid, effective_uid, real_gid, effective_gid, home));
                        }
                    });
                }
            }.getE();
        }
        return user_info;
    }

    public boolean canRead(FileAttrs attrs) {
        if ((attrs.flags & IFileSystem.ATTR_PERMISSIONS) == 0) return false;
        if ((attrs.flags & IFileSystem.ATTR_UIDGID) == 0) return false;
        UserInfo ui = getUserInfo();
        if (ui.error != null) return false;
        if (ui.e_uid == attrs.uid) {
            return (attrs.permissions & IFileSystem.S_IRUSR) != 0;
        }
        if (ui.e_gid == attrs.gid) {
            return (attrs.permissions & IFileSystem.S_IRGRP) != 0;
        }
        return (attrs.permissions & IFileSystem.S_IROTH) != 0;
    }

    public boolean canWrite(FileAttrs attrs) {
        if ((attrs.flags & IFileSystem.ATTR_PERMISSIONS) == 0) return false;
        if ((attrs.flags & IFileSystem.ATTR_UIDGID) == 0) return false;
        UserInfo ui = getUserInfo();
        if (ui.error != null) return false;
        if (ui.e_uid == attrs.uid) {
            return (attrs.permissions & IFileSystem.S_IWUSR) != 0;
        }
        if (ui.e_gid == attrs.gid) {
            return (attrs.permissions & IFileSystem.S_IWGRP) != 0;
        }
        return (attrs.permissions & IFileSystem.S_IWOTH) != 0;
    }

}
