/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tm.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tm.tcf.util.TCFFileInputStream;
import org.eclipse.tm.tcf.util.TCFFileOutputStream;

class TestFileSystem implements ITCFTest, IFileSystem.DoneStat,
        IFileSystem.DoneOpen, IFileSystem.DoneClose,
        IFileSystem.DoneWrite, IFileSystem.DoneRead,
        IFileSystem.DoneRename, IFileSystem.DoneRealPath,
        IFileSystem.DoneRemove, IFileSystem.DoneRoots,
        IFileSystem.DoneReadDir {

    private final TCFTestSuite test_suite;
    private final int channel_id;

    private static final int
        STATE_PRE = 0,
        STATE_WRITE = 1,
        STATE_READ = 2,
        STATE_OUT = 3,
        STATE_INP = 4,
        STATE_EXIT = 5;

    private final IFileSystem files;
    private final byte[] data = new byte[0x1000];
    private String root;
    private String tmp_path;
    private String file_name;
    private IFileHandle handle;
    private int state = STATE_PRE;

    TestFileSystem(TCFTestSuite test_suite, IChannel channel, int channel_id) {
        this.test_suite = test_suite;
        this.channel_id = channel_id;
        files = channel.getRemoteService(IFileSystem.class);
    }

    public void start() {
        if (files == null) {
            test_suite.done(this, null);
        }
        else {
            files.roots(this);
        }
    }

    public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
        assert state == STATE_PRE;
        if (error != null) {
            exit(error);
        }
        else if (entries == null || entries.length == 0) {
            exit(new Exception("Invalid FileSysrem.roots responce: empty roots array"));
        }
        else {
            for (DirEntry d : entries) {
                if (d.filename.startsWith("A:")) continue;
                if (d.filename.startsWith("B:")) continue;
                root = d.filename;
                break;
            }
            if (root == null) exit(new Exception("Invalid FileSystem.roots responce: no suitable root"));
            else files.opendir(root, this);
        }
    }

    public void doneReadDir(IToken token, FileSystemException error,
            DirEntry[] entries, boolean eof) {
        assert state == STATE_PRE;
        if (error != null) {
            exit(error);
        }
        else {
            if (entries != null && tmp_path == null) {
                for (DirEntry e : entries) {
                    if (e.filename.equals("tmp") || e.filename.equalsIgnoreCase("temp")) {
                        tmp_path = root + "/" + e.filename;
                        break;
                    }
                }
            }
            if (eof) {
                if (tmp_path == null) {
                    exit(new Exception("File system test filed: cannot find temporary directory"));
                    return;
                }
                files.close(handle, this);
            }
            else {
                files.readdir(handle, this);
            }
        }
    }

    public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
        if (error != null) {
            exit(error);
        }
        else if (state == STATE_READ) {
            if (attrs.size != data.length) {
                exit(new Exception("Invalid FileSysrem.fstat responce: wrong file size"));
            }
            else {
                files.close(handle, this);
            }
        }
        else {
            file_name = tmp_path + "/tcf-test-" + channel_id + ".tmp";
            files.open(file_name, IFileSystem.TCF_O_CREAT | IFileSystem.TCF_O_TRUNC | IFileSystem.TCF_O_WRITE, null, this);
        }
    }

    public void doneOpen(IToken token, FileSystemException error, final IFileHandle handle) {
        if (error != null) {
            exit(error);
        }
        else {
            this.handle = handle;
            if (state == STATE_READ) {
                files.read(handle, 0, data.length + 1, this);
            }
            else if (state == STATE_WRITE) {
                new Random().nextBytes(data);
                files.write(handle, 0, data, 0, data.length, this);
            }
            else if (state == STATE_INP) {
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            int pos = 0;
                            int len = data.length * 16;
                            byte[] buf = new byte[333];
                            int buf_pos = 0;
                            int buf_len = 0;
                            Random rnd = new Random();
                            boolean mark = true;
                            boolean reset = true;
                            int mark_pos = rnd.nextInt(len - 1);
                            int reset_pos = mark_pos + rnd.nextInt(len - mark_pos);
                            assert reset_pos >= mark_pos;
                            InputStream inp = new TCFFileInputStream(handle, 133);
                            for (;;) {
                                if (mark && pos == mark_pos) {
                                    inp.mark(len);
                                    mark = false;
                                }
                                if (reset && pos == reset_pos) {
                                    inp.reset();
                                    reset = false;
                                    pos = mark_pos;
                                    buf_pos = buf_len = 0;
                                }
                                int ch = 0;
                                if (buf_pos >= buf_len && (pos >= mark_pos || pos + buf.length <= mark_pos)) {
                                    buf_len = inp.read(buf, buf_pos = 0, buf.length);
                                    if (buf_len < 0) break;
                                }
                                if (buf_pos < buf_len) {
                                    ch = buf[buf_pos++] & 0xff;
                                }
                                else {
                                    ch = inp.read();
                                    if (ch < 0) break;
                                }
                                int dt = data[pos % data.length] & 0xff;
                                if (ch != dt) {
                                    error(new Exception("Invalid TCFFileInputStream.read responce: wrong data at offset " + pos +
                                            ", expected " + dt + ", actual " + ch));
                                }
                                pos++;
                            }
                            if (pos != data.length * 16) {
                                error(new Exception("Invalid TCFFileInputStream.read responce: wrong file length: " +
                                        "expected " + data.length + ", actual " + pos));
                            }
                            inp.close();
                            Protocol.invokeLater(new Runnable() {
                                public void run() {
                                    state = STATE_EXIT;
                                    files.rename(file_name, file_name + ".rnm", TestFileSystem.this);
                                }
                            });
                        }
                        catch (Throwable x) {
                            error(x);
                        }
                    }
                    private void error(final Throwable x) {
                        Protocol.invokeLater(new Runnable() {
                            public void run() {
                                exit(x);
                            }
                        });
                    }
                };
                thread.setName("TCF FileSystem Test");
                thread.start();
            }
            else if (state == STATE_OUT) {
                new Random().nextBytes(data);
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            int pos = 0;
                            int len = data.length * 16;
                            Random rnd = new Random();
                            OutputStream out = new TCFFileOutputStream(handle, 121);
                            while (pos < len) {
                                int m = pos % data.length;
                                int n = rnd.nextInt(1021);
                                if (n > data.length - m) n = data.length - m;
                                out.write(data, m, n);
                                pos += n;
                                if (pos == len) break;
                                out.write(data[pos % data.length] & 0xff);
                                pos++;
                            }
                            out.close();
                            Protocol.invokeLater(new Runnable() {
                                public void run() {
                                    state = STATE_INP;
                                    files.open(file_name, IFileSystem.TCF_O_READ, null, TestFileSystem.this);
                                }
                            });
                        }
                        catch (Throwable x) {
                            error(x);
                        }
                    }
                    private void error(final Throwable x) {
                        Protocol.invokeLater(new Runnable() {
                            public void run() {
                                exit(x);
                            }
                        });
                    }
                };
                thread.setName("TCF FileSystem Test");
                thread.start();
            }
            else {
                assert state == STATE_PRE;
                files.readdir(handle, this);
            }
        }
    }

    public void doneWrite(IToken token, FileSystemException error) {
        if (error != null) {
            exit(error);
        }
        else {
            files.close(handle, this);
        }
    }

    public void doneRead(IToken token, FileSystemException error, byte[] data, boolean eof) {
        if (error != null) {
            exit(error);
        }
        else if (!eof) {
            exit(new Exception("Invalid FileSysrem.read responce: EOF expected"));
        }
        else if (data.length != this.data.length) {
            exit(new Exception("Invalid FileSysrem.read responce: wrong data array size"));
        }
        else {
            for (int i = 0; i < data.length; i++) {
                if (data[i] != this.data[i]) {
                    exit(new Exception("Invalid FileSysrem.read responce: wrong data at offset " + i +
                            ", expected " + this.data[i] + ", actual " + data[i]));
                    return;
                }
            }
            files.fstat(handle, this);
        }
    }

    public void doneClose(IToken token, FileSystemException error) {
        if (error != null) {
            exit(error);
        }
        else {
            handle = null;
            if (state == STATE_PRE) {
                files.realpath(tmp_path, this);
            }
            else if (state == STATE_WRITE) {
                state = STATE_READ;
                files.open(file_name, IFileSystem.TCF_O_READ, null, this);
            }
            else if (state == STATE_READ) {
                state = STATE_OUT;
                files.open(file_name, IFileSystem.TCF_O_WRITE, null, this);
            }
            else {
                assert false;
            }
        }
    }

    public void doneRename(IToken token, FileSystemException error) {
        assert state == STATE_EXIT;
        if (error != null) {
            exit(error);
        }
        else {
            files.realpath(file_name + ".rnm", this);
        }
    }

    public void doneRealPath(IToken token, FileSystemException error, String path) {
        if (error != null) {
            exit(error);
        }
        else if (state == STATE_PRE) {
            state = STATE_WRITE;
            tmp_path = path;
            files.stat(tmp_path, this);
        }
        else if (!path.equals(file_name + ".rnm")) {
            exit(new Exception("Invalid FileSysrem.realpath responce: " + path));
        }
        else {
            files.remove(file_name + ".rnm", this);
        }
    }

    public void doneRemove(IToken token, FileSystemException error) {
        assert state == STATE_EXIT;
        exit(error);
    }

    private void exit(Throwable x) {
        if (!test_suite.isActive(this)) return;
        test_suite.done(this, x);
    }
}