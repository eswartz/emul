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
package org.eclipse.tm.tcf.services;

import java.io.IOException;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;


/**
 * File System service provides file transfer (and more generally file
 * system access) functionality in TCF. The service design is
 * derived from SSH File Transfer Protocol specifications.
 *  
 *      Request Synchronization and Reordering
 *
 * The protocol and implementations MUST process requests relating to
 * the same file in the order in which they are received.  In other
 * words, if an application submits multiple requests to the server, the
 * results in the responses will be the same as if it had sent the
 * requests one at a time and waited for the response in each case.  For
 * example, the server may process non-overlapping read/write requests
 * to the same file in parallel, but overlapping reads and writes cannot
 * be reordered or parallelized.  However, there are no ordering
 * restrictions on the server for processing requests from two different
 * file transfer connections.  The server may interleave and parallelize
 * them at will.
 *
 * There are no restrictions on the order in which responses to
 * outstanding requests are delivered to the client, except that the
 * server must ensure fairness in the sense that processing of no
 * request will be indefinitely delayed even if the client is sending
 * other requests so that there are multiple outstanding requests all
 * the time.
 *
 * There is no limit on the number of outstanding (non-acknowledged)
 * requests that the client may send to the server.  In practice this is
 * limited by the buffering available on the data stream and the queuing
 * performed by the server.  If the server's queues are full, it should
 * not read any more data from the stream, and flow control will prevent
 * the client from sending more requests.
 * 
 *      File Names
 *
 * This protocol represents file names as strings.  File names are
 * assumed to use the slash ('/') character as a directory separator.
 *
 * File names starting with a slash are "absolute", and are relative to
 * the root of the file system.  Names starting with any other character
 * are relative to the user's default directory (home directory). Client
 * can use 'user()' command to retrieve current user home directory.
 *
 * Servers SHOULD interpret a path name component ".." as referring to
 * the parent directory, and "." as referring to the current directory.
 * If the server implementation limits access to certain parts of the
 * file system, it must be extra careful in parsing file names when
 * enforcing such restrictions.  There have been numerous reported
 * security bugs where a ".." in a path name has allowed access outside
 * the intended area.
 *
 * An empty path name is valid, and it refers to the user's default
 * directory (usually the user's home directory).
 *
 * Otherwise, no syntax is defined for file names by this specification.
 * Clients should not make any other assumptions; however, they can
 * splice path name components returned by readdir() together
 * using a slash ('/') as the separator, and that will work as expected.
 */
public interface IFileSystem extends IService {
    
    /**
     * Service name.
     */
    static final String NAME = "FileSystem";

    /**
     * Flags to be used with open() method.
     */
    static final int 
    
        /**
         * Open the file for reading.
         */
        TCF_O_READ              = 0x00000001,

        /**
         * Open the file for writing. If both this and TCF_O_READ are
         * specified, the file is opened for both reading and writing.
         */
        TCF_O_WRITE             = 0x00000002,

        /**
         * Force all writes to append data at the end of the file.
         */
        TCF_O_APPEND            = 0x00000004, 

        /**
         * If this flag is specified, then a new file will be created if one
         * does not already exist (if TCF_O_TRUNC is specified, the new file will
         * be truncated to zero length if it previously exists).
         */
        TCF_O_CREAT             = 0x00000008,

        /**
         * Forces an existing file with the same name to be truncated to zero
         * length when creating a file by specifying TCF_O_CREAT.
         * TCF_O_CREAT MUST also be specified if this flag is used.
         */
        TCF_O_TRUNC             = 0x00000010,

        /**
         * Causes the request to fail if the named file already exists.
         * TCF_O_CREAT MUST also be specified if this flag is used.
         */
        TCF_O_EXCL              = 0x00000020;

    /**
     * Flags to be used together with FileAttrs.
     * The flags specify which of the fields are present.  Those fields
     * for which the corresponding flag is not set are not present (not
     * included in the message).
     */
    static final int
        ATTR_SIZE               = 0x00000001,
        ATTR_UIDGID             = 0x00000002,
        ATTR_PERMISSIONS        = 0x00000004,
        ATTR_ACMODTIME          = 0x00000008;

    /**
     * FileAttrs is used both when returning file attributes from
     * the server and when sending file attributes to the server.  When
     * sending it to the server, the flags field specifies which attributes
     * are included, and the server will use default values for the
     * remaining attributes (or will not modify the values of remaining
     * attributes).  When receiving attributes from the server, the flags
     * specify which attributes are included in the returned data.  The
     * server normally returns all attributes it knows about.
     */
    final static class FileAttrs {
        
        /**
         * The `flags' specify which of the fields are present.
         */
        public final int flags;
        
        /**
         * The `size' field specifies the size of the file in bytes.
         */
        public final long size;
        
        /**
         * The `uid' and `gid' fields contain numeric Unix-like user and group
         * identifiers, respectively.
         */
        public final int uid;
        public final int gid;
        
        /**
         * The `permissions' field contains a bit mask of file permissions as
         * defined by posix [1].
         */
        public final int permissions;
        
        /**
         * The `atime' and `mtime' contain the access and modification times of
         * the files, respectively. They are represented as milliseconds from
         * midnight Jan 1, 1970 in UTC.
         */
        public final long atime;
        public final long mtime;

        /**
         * Additional (non-standard) attributes.
         */
        public final Map<String,Object> attributes;
        
        public FileAttrs(int flags, long size, int uid, int gid,
                int permissions, long atime, long mtime, Map<String,Object> attributes) {
            this.flags = flags;
            this.size = size;
            this.uid = uid;
            this.gid = gid;
            this.permissions = permissions;
            this.atime = atime;
            this.mtime = mtime;
            this.attributes = attributes;
        }
        
        /**
         * Determines if the file system object is a file on the remote file system.
         * 
         * @return true if and only if the object on the remote system can be considered to have "contents" that
         * have the potential to be read and written as a byte stream.
         */
        public boolean isFile() {
            if ((flags & ATTR_PERMISSIONS) == 0) return false;
            return (permissions & S_IFMT) == S_IFREG;
        }

        /**
         * Determines if the file system object is a directory on the remote file system.
         * 
         * @return true if and only if the object on the remote system is a directory.
         * That is, it contains entries that can be interpreted as other files.
         */
        public boolean isDirectory() {
            if ((flags & ATTR_PERMISSIONS) == 0) return false;
            return (permissions & S_IFMT) == S_IFDIR;
        }
    }
    
    /**
     * The following flags are defined for the 'permissions' field:
     */
    static final int
        S_IFMT     = 0170000,   // bitmask for the file type bitfields
        S_IFSOCK   = 0140000,   // socket
        S_IFLNK    = 0120000,   // symbolic link
        S_IFREG    = 0100000,   // regular file
        S_IFBLK    = 0060000,   // block device
        S_IFDIR    = 0040000,   // directory
        S_IFCHR    = 0020000,   // character device
        S_IFIFO    = 0010000,   // fifo
        S_ISUID    = 0004000,   // set UID bit
        S_ISGID    = 0002000,   // set GID bit (see below)
        S_ISVTX    = 0001000,   // sticky bit (see below)
        S_IRWXU    = 00700,     // mask for file owner permissions
        S_IRUSR    = 00400,     // owner has read permission
        S_IWUSR    = 00200,     // owner has write permission
        S_IXUSR    = 00100,     // owner has execute permission
        S_IRWXG    = 00070,     // mask for group permissions
        S_IRGRP    = 00040,     // group has read permission
        S_IWGRP    = 00020,     // group has write permission
        S_IXGRP    = 00010,     // group has execute permission
        S_IRWXO    = 00007,     // mask for permissions for others (not in group)
        S_IROTH    = 00004,     // others have read permission
        S_IWOTH    = 00002,     // others have write permisson
        S_IXOTH    = 00001;     // others have execute permission
    
    final static class DirEntry {
        /**
         * `filename' is a file name being returned. It is a relative name within
         * the directory, without any path components;
         */
        public final String filename;
        
        /**
         * `longname' is an expanded format for the file name, similar to what
         * is returned by "ls -l" on Unix systems.
         * The format of the `longname' field is unspecified by this protocol.
         * It MUST be suitable for use in the output of a directory listing
         * command (in fact, the recommended operation for a directory listing
         * command is to simply display this data).  However, clients SHOULD NOT
         * attempt to parse the longname field for file attributes; they SHOULD
         * use the attrs field instead.
         */
        public final String longname;
        
        /**
         * `attrs' is the attributes of the file.
         */
        public final FileAttrs attrs;
        
        public DirEntry(String filename, String longname, FileAttrs attrs) {
            this.filename = filename;
            this.longname = longname;
            this.attrs = attrs;
        }
    }
    
    /**
     * Opaque representation of open file handle.
     * Note: open file handle can be used only with service instance that 
     * created the handle.
     */
    interface IFileHandle {
        IFileSystem getService();
    }
    
    /**
     * Service specific error codes.
     */
    static final int
    
        /**
         * Indicates end-of-file condition; for read() it means that no
         * more data is available in the file, and for readdir() it
         * indicates that no more files are contained in the directory.
         */
        STATUS_EOF = 0x10001,

        /**
         * This code is returned when a reference is made to a file which
         * should exist but doesn't.
         */
        STATUS_NO_SUCH_FILE = 0x10002,

        /**
         * is returned when the authenticated user does not have sufficient
         * permissions to perform the operation.
         */
        STATUS_PERMISSION_DENIED = 0x10003;
    
    /**
     * The class to represent File System error reports. 
     */
    @SuppressWarnings("serial")
    abstract static class FileSystemException extends IOException {
        
        protected FileSystemException(String message) {
            super(message);
        }
        
        protected FileSystemException(Exception x) {
            super(x.getMessage());
            initCause(x);
        }
        
        /**
         * Get error code. The code can be standard TCF error code or
         * one of service specific codes, see STATUS_*. 
         * @return error code.
         */
        public abstract int getStatus();
    }

    /**
     * Open or create a file on a remote system.
     * 
     * @param file_name specifies the file name.  See 'File Names' for more information.
     * @param flags is a bit mask of TCF_O_* flags.
     * @param attrs specifies the initial attributes for the file.
     *  Default values will be used for those attributes that are not specified.
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken open(String file_name, int flags, FileAttrs attrs, DoneOpen done);
    
    interface DoneOpen {
        void doneOpen(IToken token, FileSystemException error, IFileHandle handle);
    }
    
    /**
     * Close a file on a remote system.
     * 
     * @param handle is a handle previously returned in the response to
     * open() or opendir().
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken close(IFileHandle handle, DoneClose done);
    
    interface DoneClose {
        void doneClose(IToken token, FileSystemException error);
    }
    
    /**
     * Read bytes from an open file.
     * In response to this request, the server will read as many bytes as it
     * can from the file (up to `len'), and return them in a byte array.
     * If an error occurs or EOF is encountered, the server may return
     * fewer bytes then requested. Call back method doneRead() argument 'error'
     * will be not null in case of error, and argument 'eof' will be
     * true in case of EOF. For normal disk files, it is guaranteed
     * that this will read the specified number of bytes, or up to end of file
     * or error. For e.g. device files this may return fewer bytes than requested.
     * 
     * @param handle is an open file handle returned by open().
     * @param offset is the offset (in bytes) relative
     * to the beginning of the file from where to start reading.
     * If offset < 0 then reading starts from current position in the file.
     * @param len is the maximum number of bytes to read.
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken read(IFileHandle handle, long offset, int len, DoneRead done);
    
    interface DoneRead {
        void doneRead(IToken token, FileSystemException error, byte[] data, boolean eof);
    }

    /**
     * Write bytes into an open file.
     * The write will extend the file if writing beyond the end of the file.
     * It is legal to write way beyond the end of the file; the semantics
     * are to write zeroes from the end of the file to the specified offset
     * and then the data.
     * 
     * @param handle is an open file handle returned by open().
     * @param offset is the offset (in bytes) relative
     * to the beginning of the file from where to start writing.
     * If offset < 0 then writing starts from current position in the file.
     * @param data is byte array that contains data for writing.
     * @param data_pos if offset in 'data' of first byte to write.
     * @param data_size is the number of bytes to write.
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken write(IFileHandle handle, long offset,
            byte[] data, int data_pos, int data_size, DoneWrite done);

    interface DoneWrite {
        void doneWrite(IToken token, FileSystemException error);
    }
    
    /**
     * Retrieve file attributes.
     * 
     * @param path - specifies the file system object for which
     * status is to be returned.
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken stat(String path, DoneStat done);

    /**
     * Retrieve file attributes.
     * Unlike 'stat()', 'lstat()' does not follow symbolic links.
     * 
     * @param path - specifies the file system object for which
     * status is to be returned.
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken lstat(String path, DoneStat done);
    
    /**
     * Retrieve file attributes for an open file (identified by the file handle).
     * 
     * @param handle is a file handle returned by 'open()'.
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken fstat(IFileHandle handle, DoneStat done);
    
    interface DoneStat {
        void doneStat(IToken token, FileSystemException error, FileAttrs attrs);
    }
    
    /**
     * Set file attributes.
     * This request is used for operations such as changing the ownership,
     * permissions or access times, as well as for truncating a file.
     * An error will be returned if the specified file system object does
     * not exist or the user does not have sufficient rights to modify the
     * specified attributes.
     * 
     * @param path specifies the file system object (e.g. file or directory)
     * whose attributes are to be modified.
     * @param attrs specifies the modifications to be made to file attributes.
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken setstat(String path, FileAttrs attrs, DoneSetStat done);
    
    /**
     * Set file attributes for an open file (identified by the file handle).
     * This request is used for operations such as changing the ownership,
     * permissions or access times, as well as for truncating a file.
     * 
     * @param handle is a file handle returned by 'open()'.
     * @param attrs specifies the modifications to be made to file attributes.
     * @param done is call back object.
     * @return pending command handle.
     */
    IToken fsetstat(IFileHandle handle, FileAttrs attrs, DoneSetStat done);
    
    interface DoneSetStat {
        void doneSetStat(IToken token, FileSystemException error);
    }
    
    /**
     * The opendir() command opens a directory for reading.
     * Once the directory has been successfully opened, files (and
     * directories) contained in it can be listed using readdir() requests.
     * When the client no longer wishes to read more names from the
     * directory, it SHOULD call close() for the handle.  The handle
     * should be closed regardless of whether an error has occurred or not.

     * @param path - name of the directory to be listed (without any trailing slash).
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken opendir(String path, DoneOpen done);
    
    /**
     * The files in a directory can be listed using the opendir() and
     * readdir() requests.  Each readdir() request returns one
     * or more file names with full file attributes for each file.  The
     * client should call readdir() repeatedly until it has found the
     * file it is looking for or until the server responds with a
     * message indicating an error or end of file. The client should then
     * close the handle using the close() request.
     * Note: directory entries "." and ".." are NOT included into readdir()
     * response.
     * @param handle - file handle created by opendir()
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken readdir(IFileHandle handle, DoneReadDir done);
    
    interface DoneReadDir {
        void doneReadDir(IToken token, FileSystemException error, DirEntry[] entries, boolean eof);
    }
    
    /**
     * Create a directory on the server.
     * 
     * @param path - specifies the directory to be created.
     * @param attrs - new directory attributes.
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken mkdir(String path, FileAttrs attrs, DoneMkDir done);
    
    interface DoneMkDir {
        void doneMkDir(IToken token, FileSystemException error);
    }

    /**
     * Remove a directory.
     * An error will be returned if no directory
     * with the specified path exists, or if the specified directory is not
     * empty, or if the path specified a file system object other than a
     * directory.
     * 
     * @param path - specifies the directory to be removed.
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken rmdir(String path, DoneRemove done);
    
    interface DoneRemove {
        void doneRemove(IToken token, FileSystemException error);
    }

    /**
     * Retrieve file system roots - top level file system objects.
     * UNIX file system can report just one root with path "/". Other types of systems
     * can have more the one root. For example, Windows server can return multiple roots:
     * one per disc (e.g. "/C:/", "/D:/", etc.). Note: even Windows implementation of
     * the service must use forward slash as directory separator, and must start 
     * absolute path with "/". Server should implement proper translation of
     * protocol file names to OS native names and back. 
     * 
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken roots(DoneRoots done);
    
    interface DoneRoots {
        void doneRoots(IToken token, FileSystemException error, DirEntry[] entries);
    }

    /**
     * Remove a file or symbolic link.
     * This request cannot be used to remove directories.
     * 
     * @param file_name is the name of the file to be removed.
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken remove(String file_name, DoneRemove done);
    
    /**
     * Canonicalize any given path name to an absolute path.
     * This is useful for converting path names containing ".." components or
     * relative pathnames without a leading slash into absolute paths.
     * 
     * @param path specifies the path name to be canonicalized.
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken realpath(String path, DoneRealPath done);
    
    interface DoneRealPath {
        void doneRealPath(IToken token, FileSystemException error, String path);
    }

    /**
     * Rename a file.
     * It is an error if there already exists a file
     * with the name specified by 'new_path'.  The server may also fail rename
     * requests in other situations, for example if `old_path' and `new_path'
     * point to different file systems on the server.
     * 
     * @param old_path is the name of an existing file or directory.
     * @param new_path is the new name for the file or directory.
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken rename(String old_path, String new_path, DoneRename done);
    
    interface DoneRename {
        void doneRename(IToken token, FileSystemException error);
    }

    /**
     * Read the target of a symbolic link.
     * 
     * @param path specifies the path name of the symbolic link to be read.
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken readlink(String path, DoneReadLink done);
    
    interface DoneReadLink {
        void doneReadLink(IToken token, FileSystemException error, String path);
    }

    /**
     * Create a symbolic link on the server.
     * 
     * @param link_path specifies the path name of the symbolic link to be created.
     * @param target_path specifies the target of the symbolic link.
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken symlink(String link_path, String target_path, DoneSymLink done);
    
    interface DoneSymLink {
        void doneSymLink(IToken token, FileSystemException error);
    }

    /**
     * Copy a file on remote system.
     * 
     * @param src_path specifies the path name of the file to be copied.
     * @param dst_path specifies destination file name.
     * @param copy_permissions - if true then copy source file permissions.
     * @param copy_ownership - if true then copy source file UID and GID.
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken copy(String src_path, String dst_path,
            boolean copy_permissions, boolean copy_ownership, DoneCopy done);
    
    interface DoneCopy {
        void doneCopy(IToken token, FileSystemException error);
    }
    
    /**
     * Retrieve information about user account, which is used by server 
     * to access file system on behalf of the client.
     *   
     * @param done - result call back object.
     * @return pending command handle.
     */
    IToken user(DoneUser done);
    
    interface DoneUser {
        void doneUser(IToken token, FileSystemException error,
                int real_uid, int effective_uid, int real_gid, int effective_gid,
                String home);
    }
}
