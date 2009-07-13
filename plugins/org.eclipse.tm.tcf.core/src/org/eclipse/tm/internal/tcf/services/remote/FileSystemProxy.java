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
package org.eclipse.tm.internal.tcf.services.remote;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IFileSystem;


public class FileSystemProxy implements IFileSystem {
    
    private final class FileHandle implements IFileHandle {
        final String id;
        
        FileHandle(String id) {
            this.id = id;
        }

        public IFileSystem getService() {
            return FileSystemProxy.this;
        }
        
        public String toString() {
            return "[File Handle '" + id + "']";
        }
    }
    
    private static final class Status extends FileSystemException implements IErrorReport {
        
        private static final long serialVersionUID = -1636567076145085980L;
        
        private final int status;
        private final Map<String,Object> attrs;
        
        Status(int status, String message, Map<String,Object> attrs) {
            super(message);
            this.status = status;
            this.attrs = attrs;
        }
        
        Status(Exception x) {
            super(x);
            this.status = IErrorReport.TCF_ERROR_OTHER;
            this.attrs = new HashMap<String,Object>();
        }
        
        public int getStatus() {
            return status;
        }

        public int getErrorCode() {
            Number n = (Number)attrs.get(ERROR_CODE);
            if (n == null) return 0;
            return n.intValue();
        }

        public int getAltCode() {
            Number n = (Number)attrs.get(ERROR_ALT_CODE);
            if (n == null) return 0;
            return n.intValue();
        }

        public String getAltOrg() {
            return (String)attrs.get(ERROR_ALT_ORG);
        }

        public Map<String, Object> getAttributes() {
            return attrs;
        }
    }
    
    private abstract class FileSystemCommand extends Command {
        
        FileSystemCommand(String command, Object[] args) {
            super(channel, FileSystemProxy.this, command, args);
        }
        
        @SuppressWarnings("unchecked")
        public Status toFSError(Object data) {
            if (data == null) return null;
            Map<String,Object> map = (Map<String,Object>)data;
            Number error_code = (Number)map.get(IErrorReport.ERROR_CODE);
            String cmd = getCommandString();
            if (cmd.length() > 72) cmd = cmd.substring(0, 72) + "...";
            Status s = new Status(error_code.intValue(),
                    "TCF command exception:" +
                    "\nCommand: " + cmd +
                    "\nException: " + toErrorString(data) +
                    "\nError code: " + error_code, map);
            Object caused_by = map.get(IErrorReport.ERROR_CAUSED_BY);
            if (caused_by != null) s.initCause(toError(caused_by, false));
            return s;
        }
    }
    
    private final IChannel channel;
    
    public FileSystemProxy(IChannel channel) {
        this.channel = channel;
    }

    public IToken close(IFileHandle handle, final DoneClose done) {
        assert handle.getService() == this;
        String id = ((FileHandle)handle).id;
        return new FileSystemCommand("close", new Object[]{ id }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneClose(token, s);
            }
        }.token;
    }

    public IToken setstat(String path, FileAttrs attrs, final DoneSetStat done) {
        Object dt = toObject(attrs);
        return new FileSystemCommand("setstat", new Object[]{ path, dt }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneSetStat(token, s);
            }
        }.token;
    }

    public IToken fsetstat(IFileHandle handle, FileAttrs attrs, final DoneSetStat done) {
        assert handle.getService() == this;
        String id = ((FileHandle)handle).id;
        Object dt = toObject(attrs);
        return new FileSystemCommand("fsetstat", new Object[]{ id, dt }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneSetStat(token, s);
            }
        }.token;
    }

    public IToken stat(String path, final DoneStat done) {
        return new FileSystemCommand("stat", new Object[]{ path }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                FileAttrs a = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 2;
                    s = toFSError(args[0]);
                    if (s == null) a = toFileAttrs(args[1]);
                }
                done.doneStat(token, s, a);
            }
        }.token;
    }

    public IToken fstat(IFileHandle handle, final DoneStat done) {
        assert handle.getService() == this;
        String id = ((FileHandle)handle).id;
        return new FileSystemCommand("fstat", new Object[]{ id }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                FileAttrs a = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 2;
                    s = toFSError(args[0]);
                    if (s == null) a = toFileAttrs(args[1]);
                }
                done.doneStat(token, s, a);
            }
        }.token;
    }

    public IToken lstat(String path, final DoneStat done) {
        return new FileSystemCommand("lstat", new Object[]{ path }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                FileAttrs a = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 2;
                    s = toFSError(args[0]);
                    if (s == null) a = toFileAttrs(args[1]);
                }
                done.doneStat(token, s, a);
            }
        }.token;
    }

    public IToken mkdir(String path, FileAttrs attrs, final DoneMkDir done) {
        Object dt = toObject(attrs);
        return new FileSystemCommand("mkdir", new Object[]{ path, dt }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneMkDir(token, s);
            }
        }.token;
    }

    public IToken open(String file_name, int flags, FileAttrs attrs, final DoneOpen done) {
        Object dt = toObject(attrs);
        return new FileSystemCommand("open", new Object[]{ file_name, new Integer(flags), dt }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                FileHandle h = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 2;
                    s = toFSError(args[0]);
                    if (s == null) h = toFileHandle(args[1]);
                }
                done.doneOpen(token, s, h);
            }
        }.token;
    }

    public IToken opendir(String path, final DoneOpen done) {
        return new FileSystemCommand("opendir", new Object[]{ path }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                FileHandle h = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 2;
                    s = toFSError(args[0]);
                    if (s == null) h = toFileHandle(args[1]);
                }
                done.doneOpen(token, s, h);
            }
        }.token;
    }

    public IToken read(IFileHandle handle, long offset, int len, final DoneRead done) {
        assert handle.getService() == this;
        String id = ((FileHandle)handle).id;
        return new FileSystemCommand("read", new Object[]{
                id, Long.valueOf(offset), Integer.valueOf(len) }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                byte[] b = null;
                boolean eof = false;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 3;
                    s = toFSError(args[1]);
                    if (s == null) {
                        b = JSON.toByteArray(args[0]);
                        eof = ((Boolean)args[2]).booleanValue();
                    }
                }
                done.doneRead(token, s, b, eof);
            }
        }.token;
    }

    public IToken readdir(IFileHandle handle, final DoneReadDir done) {
        assert handle.getService() == this;
        String id = ((FileHandle)handle).id;
        return new FileSystemCommand("readdir", new Object[]{ id }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                DirEntry[] b = null;
                boolean eof = false;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 3;
                    s = toFSError(args[1]);
                    if (s == null) {
                        b = toDirEntryArray(args[0]);
                        eof = ((Boolean)args[2]).booleanValue();
                    }
                }
                done.doneReadDir(token, s, b, eof);
            }
        }.token;
    }

    public IToken roots(final DoneRoots done) {
        return new FileSystemCommand("roots", null) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                DirEntry[] b = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 2;
                    s = toFSError(args[1]);
                    if (s == null) b = toDirEntryArray(args[0]);
                }
                done.doneRoots(token, s, b);
            }
        }.token;
    }

    public IToken readlink(String path, final DoneReadLink done) {
        return new FileSystemCommand("readlink", new Object[]{ path }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                String p = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 2;
                    s = toFSError(args[0]);
                    if (s == null) p = (String)args[1];
                }
                done.doneReadLink(token, s, p);
            }
        }.token;
    }

    public IToken realpath(String path, final DoneRealPath done) {
        return new FileSystemCommand("realpath", new Object[]{ path }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                String p = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 2;
                    s = toFSError(args[0]);
                    if (s == null) p = (String)args[1];
                }
                done.doneRealPath(token, s, p);
            }
        }.token;
    }

    public IToken remove(String file_name, final DoneRemove done) {
        return new FileSystemCommand("remove", new Object[]{ file_name }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneRemove(token, s);
            }
        }.token;
    }

    public IToken rename(String old_path, String new_path, final DoneRename done) {
        return new FileSystemCommand("rename", new Object[]{ old_path, new_path }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneRename(token, s);
            }
        }.token;
    }

    public IToken rmdir(String path, final DoneRemove done) {
        return new FileSystemCommand("rmdir", new Object[]{ path }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneRemove(token, s);
            }
        }.token;
    }

    public IToken symlink(String link_path, String target_path, final DoneSymLink done) {
        return new FileSystemCommand("symlink", new Object[]{ link_path, target_path }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneSymLink(token, s);
            }
        }.token;
    }

    public IToken write(IFileHandle handle, long offset, byte[] data,
            int data_pos, int data_size, final DoneWrite done) {
        assert handle.getService() == this;
        String id = ((FileHandle)handle).id;
        return new FileSystemCommand("write", new Object[]{
                id, Long.valueOf(offset), new JSON.Binary(data, data_pos, data_size) }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneWrite(token, s);
            }
        }.token;
    }

    public IToken copy(String src_path, String dst_path,
            boolean copy_permissions, boolean copy_uidgid, final DoneCopy done) {
        return new FileSystemCommand("copy", new Object[]{
                src_path, dst_path, Boolean.valueOf(copy_permissions),
                Boolean.valueOf(copy_uidgid) }) {
            public void done(Exception error, Object[] args) {
                Status s = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 1;
                    s = toFSError(args[0]);
                }
                done.doneCopy(token, s);
            }
        }.token;
    }

    public IToken user(final DoneUser done) {
        return new FileSystemCommand("user", null) {
            @Override
            public void done(Exception error, Object[] args) {
                Status s = null;
                int r_uid = 0;
                int e_uid = 0;
                int r_gid = 0;
                int e_gid = 0;
                String home = null;
                if (error != null) {
                    s = new Status(error);
                }
                else {
                    assert args.length == 5;
                    r_uid = ((Number)args[0]).intValue();
                    e_uid = ((Number)args[1]).intValue();
                    r_gid = ((Number)args[2]).intValue();
                    e_gid = ((Number)args[3]).intValue();
                    home = (String)args[4];
                }
                done.doneUser(token, s, r_uid, e_uid, r_gid, e_gid, home);
            }
        }.token;
    }

    public String getName() {
        return NAME;
    }
    
    private Object toObject(FileAttrs attrs) {
        if (attrs == null) return null;
        Map<String,Object> m = new HashMap<String,Object>();
        if (attrs.attributes != null) m.putAll(attrs.attributes);
        if ((attrs.flags & ATTR_SIZE) != 0) {
            m.put("Size", Long.valueOf(attrs.size));
        }
        if ((attrs.flags & ATTR_UIDGID) != 0) {
            m.put("UID", Integer.valueOf(attrs.uid));
            m.put("GID", Integer.valueOf(attrs.gid));
        }
        if ((attrs.flags & ATTR_PERMISSIONS) != 0) {
            m.put("Permissions", Integer.valueOf(attrs.permissions));
        }
        if ((attrs.flags & ATTR_ACMODTIME) != 0) {
            m.put("ATime", Long.valueOf(attrs.atime));
            m.put("MTime", Long.valueOf(attrs.mtime));
        }
        return m;
    }
    
    @SuppressWarnings("unchecked")
    private FileAttrs toFileAttrs(Object o) {
        if (o == null) return null;
        Map<String,Object> m = new HashMap<String,Object>((Map<String,Object>)o);
        int flags = 0;
        long size = 0;
        int uid = 0;
        int gid = 0;
        int permissions = 0;
        long atime = 0;
        long mtime = 0;
        Number n = (Number)m.remove("Size");
        if (n != null) {
            size = n.longValue();
            flags |= ATTR_SIZE;
        }
        Number n1 = (Number)m.remove("UID");
        Number n2 = (Number)m.remove("GID");
        if (n1 != null && n2 != null) {
            uid = n1.intValue();
            gid = n2.intValue();
            flags |= ATTR_UIDGID;
        }
        n = (Number)m.remove("Permissions");
        if (n != null) {
            permissions = n.intValue();
            flags |= ATTR_PERMISSIONS;
        }
        n1 = (Number)m.remove("ATime");
        n2 = (Number)m.remove("MTime");
        if (n1 != null && n2 != null) {
            atime = n1.longValue();
            mtime = n2.longValue();
            flags |= ATTR_ACMODTIME;
        }
        return new FileAttrs(flags, size, uid, gid, permissions, atime, mtime, m);
    }
    
    private FileHandle toFileHandle(Object o) {
        if (o == null) return null;
        return new FileHandle(o.toString());
    }
    
    @SuppressWarnings("unchecked")
    private DirEntry[] toDirEntryArray(Object o) {
        if (o == null) return null;
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        DirEntry[] res = new DirEntry[c.size()];
        int i = 0;
        for (Map<String,Object> m : c) {
            res[i++] = new DirEntry(
                    (String)m.get("FileName"),
                    (String)m.get("LongName"),
                    toFileAttrs(m.get("Attrs")));
        }
        return res;
    }
}
