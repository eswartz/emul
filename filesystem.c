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

/*
 * Target service implementation: file system access (TCF name FileSystem)
 */

#include "config.h"
#if SERVICE_FileSystem

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <assert.h>
#include <fcntl.h>
#include <sys/stat.h>
#ifdef WIN32
#  include <sys/utime.h>
#  include <direct.h>
#else
#  include <utime.h>
#  include <dirent.h>
#endif
#include "mdep.h"
#include "myalloc.h"
#include "streams.h"
#include "channel.h"
#include "link.h"
#include "trace.h"
#include "json.h"
#include "exceptions.h"
#include "protocol.h"
#include "filesystem.h"

#define BUF_SIZE 0x1000

static const char * FILE_SYSTEM = "FileSystem";

static const int
    TCF_O_READ              = 0x00000001,
    TCF_O_WRITE             = 0x00000002,
    TCF_O_APPEND            = 0x00000004, 
    TCF_O_CREAT             = 0x00000008,
    TCF_O_TRUNC             = 0x00000010,
    TCF_O_EXCL              = 0x00000020;

static const int
    ATTR_SIZE               = 0x00000001,
    ATTR_UIDGID             = 0x00000002,
    ATTR_PERMISSIONS        = 0x00000004,
    ATTR_ACMODTIME          = 0x00000008;

static const int
    STATUS_OK = 0,
    STATUS_EOF = 1,
    STATUS_NO_SUCH_FILE = 2,
    STATUS_PERMISSION_DENIED = 3,
    STATUS_FAILURE = 4,
    STATUS_BAD_MESSAGE = 5,
    STATUS_NO_CONNECTION = 6,
    STATUS_CONNECTION_LOST = 7,
    STATUS_OP_UNSUPPORTED = 8;

typedef struct OpenFileInfo OpenFileInfo;

struct OpenFileInfo {
    unsigned long handle;
    char path[FILE_PATH_SIZE];
    int file;
    DIR * dir;
    InputStream * inp;
    LINK link_ring;
    LINK link_hash;
};

#define hash2file(A)    ((OpenFileInfo *)((char *)(A) - (int)&((OpenFileInfo *)0)->link_hash))
#define ring2file(A)    ((OpenFileInfo *)((char *)(A) - (int)&((OpenFileInfo *)0)->link_ring))

typedef struct FileAttrs FileAttrs;

struct FileAttrs {
    int flags;
    int64 size;
    int uid;
    int gid;
    int permissions;
    int64 atime;
    int64 mtime;
};

static unsigned long handle_cnt = 0;

#define HANDLE_HASH_SIZE 0x1000
static LINK handle_hash[HANDLE_HASH_SIZE];
static LINK file_info_ring = { NULL, NULL };

#if defined(_WRS_KERNEL)
#  define FS_ROOT "host:c:/"    
#endif

static OpenFileInfo * create_open_file_info(InputStream * inp, char * path, int file, DIR * dir) {
    int i = 0;
    LINK * list_head = NULL;

    OpenFileInfo * h = (OpenFileInfo *)loc_alloc_zero(sizeof(OpenFileInfo));
    for (;;) {
        LINK * list_next;
        OpenFileInfo * p = NULL;
        h->handle = handle_cnt++;
        list_head = &handle_hash[h->handle % HANDLE_HASH_SIZE];
        for (list_next = list_head->next; list_next != list_head; list_next = list_next->next) {
            if (hash2file(list_next)->handle == h->handle) {
                p = hash2file(list_next);
                break;
            }
        }
        if (p == NULL) break;
    }
    strcpy(h->path, path);
    h->file = file;
    h->dir = dir;
    h->inp = inp;
    list_add_first(&h->link_ring, &file_info_ring);
    list_add_first(&h->link_hash, list_head);
    return h;
}

static OpenFileInfo * find_open_file_info(char * id) {
    unsigned long handle = 0;
    LINK * list_head = NULL;
    LINK * list_next = NULL;

    if (id == NULL || id[0] != 'F' || id[1] != 'S' || id[2] == 0) return NULL;
    handle = strtoul(id + 2, &id, 10);
    if (id[0] != 0) return NULL;
    list_head = &handle_hash[handle % HANDLE_HASH_SIZE];
    for (list_next = list_head->next; list_next != list_head; list_next = list_next->next) {
        if (hash2file(list_next)->handle == handle) return hash2file(list_next);
    }
    return NULL;
}

static void delete_open_file_info(OpenFileInfo * h) {
    list_remove(&h->link_ring);
    list_remove(&h->link_hash);
    loc_free(h);
}

static void channel_close_listener(Channel * c) {
    LINK list;
    LINK * list_next = NULL;

    list_init(&list);
    for (list_next = file_info_ring.next; list_next != &file_info_ring; list_next = list_next->next) {
        OpenFileInfo * h = ring2file(list_next);
        if (h->inp == &c->inp) {
            trace(LOG_ALWAYS, "file handle left open by client: FS%d", h->handle);
            list_remove(&h->link_hash);
            if (h->dir != NULL) {
                closedir(h->dir);
                h->dir = NULL;
            }
            if (h->file >= 0) {
                close(h->file);
                h->file = -1;
            }
            list_add_last(&h->link_hash, &list);
        }
    }

    while (!list_is_empty(&list)) delete_open_file_info(hash2file(list.next));
}

static void write_fs_errno(OutputStream * out, int err) {
    char * msg = NULL;
    int status = 0;
    switch (err) {
    case 0:
        status = STATUS_OK;
        break;
    case ERR_EOF:
        status = STATUS_EOF;
        break;
    case ENOENT:
        status = STATUS_NO_SUCH_FILE;
        break;
    case EACCES:
        status = STATUS_PERMISSION_DENIED;
        break;
    default:
        status = STATUS_FAILURE;
        break;
    }
    json_write_long(out, status);
    out->write(out, 0);
    if (err != 0) msg = errno_to_str(err);
    json_write_string(out, msg);
    out->write(out, 0);
}

static void write_file_handle(OutputStream * out, OpenFileInfo * h) {
    if (h == NULL) {
        write_string(out, "null");
    }
    else {
        char s[32];
        char * p = s + sizeof(s);
        unsigned long n = h->handle;
        *(--p) = 0;
        do {
            *(--p) = (char)(n % 10 + '0');
            n = n / 10;
        }
        while (n != 0);
        *(--p) = 'S';
        *(--p) = 'F';
        json_write_string(out, p);
    }
    out->write(out, 0);
}

static void fill_attrs(FileAttrs * attrs, struct_stat * buf) {
    memset(attrs, 0, sizeof(FileAttrs));
    attrs->flags |= ATTR_SIZE | ATTR_UIDGID | ATTR_PERMISSIONS | ATTR_ACMODTIME;
    attrs->size = buf->st_size;
    attrs->uid = buf->st_uid;
    attrs->gid = buf->st_gid;
    attrs->permissions = buf->st_mode;
    attrs->atime = (int64)buf->st_atime * 1000;
    attrs->mtime = (int64)buf->st_mtime * 1000;
}

static void read_file_attrs(InputStream * inp, char * nm, void * arg) {
    FileAttrs * attrs = (FileAttrs *)arg;
    if (strcmp(nm, "Size") == 0) {
        attrs->size = json_read_int64(inp);
        attrs->flags |= ATTR_SIZE;
    }
    else if (strcmp(nm, "UID") == 0) {
        attrs->uid = (int)json_read_long(inp);
        attrs->flags |= ATTR_UIDGID;
    }
    else if (strcmp(nm, "GID") == 0) {
        attrs->gid = (int)json_read_long(inp);
        attrs->flags |= ATTR_UIDGID;
    }
    else if (strcmp(nm, "Permissions") == 0) {
        attrs->permissions = (int)json_read_long(inp);
        attrs->flags |= ATTR_PERMISSIONS;
    }
    else if (strcmp(nm, "ATime") == 0) {
        attrs->atime = json_read_int64(inp);
        attrs->flags |= ATTR_ACMODTIME;
    }
    else if (strcmp(nm, "MTime") == 0) {
        attrs->mtime = json_read_int64(inp);
        attrs->flags |= ATTR_ACMODTIME;
    }
    else {
        exception(ERR_JSON_SYNTAX);
    }
}

static void write_file_attrs(OutputStream * out, FileAttrs * attrs) {
    int cnt = 0;

    if (attrs == NULL) {
        write_stringz(out, "null");
        return;
    }

    out->write(out, '{');
    if (attrs->flags & ATTR_SIZE) {
        json_write_string(out, "Size");
        out->write(out, ':');
        json_write_int64(out, attrs->size);
        cnt++;
    }
    if (attrs->flags & ATTR_UIDGID) {
        if (cnt) out->write(out, ',');
        json_write_string(out, "UID");
        out->write(out, ':');
        json_write_long(out, attrs->uid);
        out->write(out, ',');
        json_write_string(out, "GID");
        out->write(out, ':');
        json_write_long(out, attrs->gid);
        cnt++;
    }
    if (attrs->flags & ATTR_SIZE) {
        if (cnt) out->write(out, ',');
        json_write_string(out, "Permissions");
        out->write(out, ':');
        json_write_long(out, attrs->permissions);
        cnt++;
    }
    if (attrs->flags & ATTR_ACMODTIME) {
        if (cnt) out->write(out, ',');
        json_write_string(out, "ATime");
        out->write(out, ':');
        json_write_int64(out, attrs->atime);
        out->write(out, ',');
        json_write_string(out, "MTime");
        out->write(out, ':');
        json_write_int64(out, attrs->mtime);
        cnt++;
    }
    out->write(out, '}');
}

static int to_local_open_flags(int flags) {
    int res = O_BINARY | O_LARGEFILE;
    if (flags & TCF_O_READ) res |= O_RDONLY;
    if (flags & TCF_O_WRITE) res |= O_WRONLY;
    if (flags & TCF_O_APPEND) res |= O_APPEND;
    if (flags & TCF_O_CREAT) res |= O_CREAT;
    if (flags & TCF_O_TRUNC) res |= O_TRUNC;
    if (flags & TCF_O_EXCL) res |= O_EXCL;
    return res;
}

static void read_path(InputStream * inp, char * path, int size) {
    int i = 0;
    char buf[FILE_PATH_SIZE];
    json_read_string(inp, path, size);
    while (path[i] != 0) {
        if (path[i] == '\\') path[i] = '/';
        i++;
    }
#ifdef WIN32
    if (path[0] != 0 && path[1] == ':' && path[2] == '/') return;
#elif defined(_WRS_KERNEL)
    if (strncmp(path, FS_ROOT, strlen(FS_ROOT)) == 0) return;
#endif
    if (path[0] == 0) {
        strcpy(path, get_user_home());
    }
    else if (path[0] != '/') {
        snprintf(buf, sizeof(buf), "%s/%s", get_user_home(), path);
        strcpy(path, buf);
    }
}

static void command_open(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    unsigned long flags = 0;
    FileAttrs attrs;
    int file = -1;
    int err = 0;
    OpenFileInfo * handle = NULL;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    flags = json_read_ulong(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    memset(&attrs, 0, sizeof(FileAttrs));
    json_read_struct(&c->inp, read_file_attrs, &attrs);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if ((attrs.flags & ATTR_PERMISSIONS) == 0) {
        attrs.permissions = 0775;
    }
    file = open(path, to_local_open_flags(flags), attrs.permissions); 

    if (file < 0){
        err = errno;
    }
    else {
        handle = create_open_file_info(&c->inp, path, file, NULL);
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_file_handle(&c->out, handle);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_close(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    h = find_open_file_info(id);
    if (h == NULL) {
        err = EBADF;
    }
    else if (h->dir != NULL) {
        if (closedir(h->dir) < 0) {
            err = errno;
        }
        else {
            delete_open_file_info(h);
        }
    }
    else {
        if (close(h->file) < 0) {
            err = errno;
        }
        else {
            delete_open_file_info(h);
        }
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_read(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;
    int err = 0;
    int eof = 0;
    int64 offset;
    unsigned long len;
    unsigned long cnt = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    offset = json_read_int64(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    len = json_read_ulong(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    h = find_open_file_info(id);
    if (h == NULL) {
        err = EBADF;
        write_stringz(&c->out, "null");
    }
    else {
        char buf[BUF_SIZE];
        JsonWriteBinaryState state;
        json_write_binary_start(&state, &c->out);
        while (cnt < len) {
            if (lseek(h->file, offset + cnt, SEEK_SET) == -1) {
                assert(errno != 0);
                err = errno;
                break;
            }
            else {
                int rd = read(h->file, buf, BUF_SIZE < len - cnt ? BUF_SIZE : len - cnt);
                if (rd < 0) {
                    assert(errno != 0);
                    err = errno;
                    break;
                }
                if (rd == 0) {
                    assert(cnt < len);
                    eof = 1;
                    break;
                }
                json_write_binary_data(&state, buf, rd);
                cnt += rd;
            }
        }
        json_write_binary_end(&state);
        c->out.write(&c->out, 0);
    }

    assert(err || eof || cnt == len);
    write_fs_errno(&c->out, err);
    json_write_boolean(&c->out, eof);
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_write(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;
    int err = 0;
    int64 offset;
    unsigned long len = 0;
    JsonReadBinaryState state;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    offset = json_read_int64(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);

    json_read_binary_start(&state, &c->inp);

    h = find_open_file_info(id);
    if (h == NULL) err = EBADF;
    for (;;) {
        char buf[BUF_SIZE];
        int rd = json_read_binary_data(&state, buf, sizeof(buf));
        if (rd == 0) break;
        if (err == 0 && lseek(h->file, offset + len, SEEK_SET) == -1) {
            err = errno;
        }
        if (err == 0) {
            int wr = write(h->file, buf, rd);
            if (wr < 0) err = errno;
            else if (wr < rd) err = ENOSPC;
        }
        len += rd;
    }
    json_read_binary_end(&state);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void write_stat_result(char * token, Channel * c, int err, struct_stat * buf) {
    FileAttrs attrs;

    if (err == 0) fill_attrs(&attrs, buf);
    else memset(&attrs, 0, sizeof(attrs));

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_file_attrs(&c->out, &attrs);
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_stat(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    struct_stat buf;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    memset(&buf, 0, sizeof(buf));
    if (stat(path, &buf) < 0) err = errno;

    write_stat_result(token, c, err, &buf);
}

static void command_lstat(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    struct_stat buf;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    memset(&buf, 0, sizeof(buf));
    if (lstat(path, &buf) < 0) err = errno;

    write_stat_result(token, c, err, &buf);
}

static void command_fstat(char * token, Channel * c) {
    char id[256];
    struct_stat buf;
    OpenFileInfo * h = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    h = find_open_file_info(id);
    memset(&buf, 0, sizeof(buf));
    if (h == NULL) err = EBADF;
    else if (fstat(h->file, &buf) < 0) err = errno;
    write_stat_result(token, c, err, &buf);
}

static void command_setstat(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    FileAttrs attrs;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    memset(&attrs, 0, sizeof(FileAttrs));
    json_read_struct(&c->inp, read_file_attrs, &attrs);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (attrs.flags & ATTR_SIZE) {
        if (truncate(path, attrs.size) < 0) err = errno;
    }
#if !defined(WIN32) && !defined(_WRS_KERNEL)
    if (attrs.flags & ATTR_UIDGID) {
        if (chown(path, attrs.uid, attrs.gid) < 0) err = errno;
    }
#endif
    if (attrs.flags & ATTR_PERMISSIONS) {
        if (chmod(path, attrs.permissions) < 0) err = errno;
    }
    if (attrs.flags & ATTR_ACMODTIME) {
        struct utimbuf buf;
        buf.actime = (long)(attrs.atime / 1000);
        buf.modtime = (long)(attrs.mtime / 1000);
        if (utime(path, &buf) < 0) err = errno;
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_fsetstat(char * token, Channel * c) {
    char id[256];
    FileAttrs attrs;
    OpenFileInfo * h = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    memset(&attrs, 0, sizeof(FileAttrs));
    json_read_struct(&c->inp, read_file_attrs, &attrs);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    h = find_open_file_info(id);
    if (h == NULL) {
        err = EBADF;
    }
    else {
        if (attrs.flags & ATTR_SIZE) {
            if (ftruncate(h->file, attrs.size) < 0) err = errno;
        }
#if defined(WIN32) || defined(_WRS_KERNEL)
        if (attrs.flags & ATTR_PERMISSIONS) {
            if (chmod(h->path, attrs.permissions) < 0) err = errno;
        }
#else
        if (attrs.flags & ATTR_UIDGID) {
            if (fchown(h->file, attrs.uid, attrs.gid) < 0) err = errno;
        }
        if (attrs.flags & ATTR_PERMISSIONS) {
            if (fchmod(h->file, attrs.permissions) < 0) err = errno;
        }
#endif
        if (attrs.flags & ATTR_ACMODTIME) {
            struct utimbuf buf;
            buf.actime = (long)(attrs.atime / 1000);
            buf.modtime = (long)(attrs.mtime / 1000);
            if (utime(h->path, &buf) < 0) err = errno;
        }
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_opendir(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    DIR * dir = NULL;
    int err = 0;
    OpenFileInfo * handle = NULL;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    dir = opendir(path); 
    if (dir == NULL){
        err = errno;
    }
    else {
        handle = create_open_file_info(&c->inp, path, -1, dir);
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_file_handle(&c->out, handle);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_readdir(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;
    int err = 0;
    int eof = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    h = find_open_file_info(id);
    if (h == NULL || h->dir == NULL) {
        write_stringz(&c->out, "null");
        err = EBADF;
    }
    else {
        int cnt = 0;
        c->out.write(&c->out, '[');
        while (cnt < 64) {
            struct dirent * e;
            char path[FILE_PATH_SIZE];
            struct_stat st;
            FileAttrs attrs;
            errno = 0;
            e = readdir(h->dir);
            if (e == NULL) {
                err = errno;
                if (err == 0) eof = 1;
                break;
            }
            if (strcmp(e->d_name, ".") == 0) continue;
            if (strcmp(e->d_name, "..") == 0) continue;
            if (cnt > 0) c->out.write(&c->out, ',');
            c->out.write(&c->out, '{');
            json_write_string(&c->out, "FileName");
            c->out.write(&c->out, ':');
            json_write_string(&c->out, e->d_name);
            memset(&st, 0, sizeof(st));
            snprintf(path, sizeof(path), "%s/%s", h->path, e->d_name);
            if (stat(path, &st) == 0) {
                fill_attrs(&attrs, &st);
                c->out.write(&c->out, ',');
                json_write_string(&c->out, "Attrs");
                c->out.write(&c->out, ':');
                write_file_attrs(&c->out, &attrs);
            }
            c->out.write(&c->out, '}');
            cnt++;
        }
        c->out.write(&c->out, ']');
        c->out.write(&c->out, 0);
    }

    write_fs_errno(&c->out, err);
    json_write_boolean(&c->out, eof);
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_remove(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    DIR * dir = NULL;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (remove(path) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_rmdir(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    DIR * dir = NULL;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (rmdir(path) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_mkdir(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    FileAttrs attrs;
    int err = 0;
    int mode = 0777;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    memset(&attrs, 0, sizeof(FileAttrs));
    json_read_struct(&c->inp, read_file_attrs, &attrs);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (attrs.flags & ATTR_PERMISSIONS) {
        mode = attrs.permissions;
    }
#if defined(WIN32) || defined(_WRS_KERNEL)
    if (mkdir(path) < 0) err = errno;
#else
    if (mkdir(path, mode) < 0) err = errno;
#endif

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_realpath(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    char * real = NULL;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    real = canonicalize_file_name(path);
    if (real == NULL) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    json_write_string(&c->out, real);
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
    if (real != NULL) free(real);
}

static void command_rename(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    char newp[FILE_PATH_SIZE];
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    read_path(&c->inp, newp, sizeof(newp));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (rename(path, newp) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_readlink(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    char link[FILE_PATH_SIZE];
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    link[0] = 0;
#if defined(WIN32) || defined(_WRS_KERNEL)
    err = ENOSYS;
#else
    if (readlink(path, link, sizeof(link)) < 0) err = errno;
#endif

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    json_write_string(&c->out, link);
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_symlink(char * token, Channel * c) {
    char link[FILE_PATH_SIZE];
    char target[FILE_PATH_SIZE];
    int err = 0;

    read_path(&c->inp, link, sizeof(link));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    read_path(&c->inp, target, sizeof(target));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if defined(WIN32) || defined(_WRS_KERNEL)
    err = ENOSYS;
#else
    if (symlink(target, link) < 0) err = errno;
#endif

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_copy(char * token, Channel * c) {
    char src[FILE_PATH_SIZE];
    char dst[FILE_PATH_SIZE];
    int copy_uidgid;
    int copy_perms;
    struct_stat st;
    int fi = -1;
    int fo = -1;
    int err = 0;
    int64 pos = 0;

    read_path(&c->inp, src, sizeof(src));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    read_path(&c->inp, dst, sizeof(dst));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    copy_uidgid = json_read_boolean(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    copy_perms = json_read_boolean(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (stat(src, &st) < 0) err = errno;
    if (err == 0 && (fi = open(src, O_RDONLY | O_BINARY, 0)) < 0) err = errno;
    if (err == 0 && (fo = open(dst, O_WRONLY | O_BINARY | O_CREAT, 0775)) < 0) err = errno;

    while (err == 0 && pos < st.st_size) {
        char buf[BUF_SIZE];
        int wr = 0;
        int rd = read(fi, buf, sizeof(buf));
        if (rd == 0) break;
        if (rd < 0) {
            err == errno;
            break;
        }
        wr = write(fo, buf, rd);
        if (wr < 0) {
            err = errno;
            break;
        }
        if (wr < rd) {
            err = ENOSPC;
            break;
        }
        pos += rd;
    }

    if (fo >= 0 && close(fo) < 0 && err == 0) err = errno;
    if (fi >= 0 && close(fi) < 0 && err == 0) err = errno;

    if (err == 0) {
        struct utimbuf buf;
        buf.actime = st.st_atime;
        buf.modtime = st.st_mtime;
        if (utime(dst, &buf) < 0) err = errno;
    }
    if (err == 0 && copy_perms && chmod(dst, st.st_mode) < 0) err = errno;
#if !defined(WIN32) && !defined(_WRS_KERNEL)
    if (err == 0 && copy_uidgid && chown(dst, st.st_uid, st.st_gid) < 0) err = errno;
#endif

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_user(char * token, Channel * c) {
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    json_write_long(&c->out, getuid());
    c->out.write(&c->out, 0);
    json_write_long(&c->out, geteuid());
    c->out.write(&c->out, 0);
    json_write_long(&c->out, getgid());
    c->out.write(&c->out, 0);
    json_write_long(&c->out, getegid());
    c->out.write(&c->out, 0);
    json_write_string(&c->out, get_user_home());
    c->out.write(&c->out, 0);

    c->out.write(&c->out, MARKER_EOM);
}

static void command_roots(char * token, Channel * c) {
    struct_stat st;
    int err = 0;
    int cnt = 0;
    int disk = 0;

    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    c->out.write(&c->out, '[');

#ifdef WIN32
    for (disk = 'A'; disk <= 'Z'; disk++) {
        char path[32];
        snprintf(path, sizeof(path), "%c:/", disk);
        memset(&st, 0, sizeof(st));
        if (stat(path, &st) == 0) {
            FileAttrs attrs;
            if (cnt > 0) c->out.write(&c->out, ',');
            c->out.write(&c->out, '{');
            json_write_string(&c->out, "FileName");
            c->out.write(&c->out, ':');
            json_write_string(&c->out, path);
            fill_attrs(&attrs, &st);
            c->out.write(&c->out, ',');
            json_write_string(&c->out, "Attrs");
            c->out.write(&c->out, ':');
            write_file_attrs(&c->out, &attrs);
            c->out.write(&c->out, '}');
            cnt++;
        }
    }
#elif defined(_WRS_KERNEL)
    c->out.write(&c->out, '{');
    json_write_string(&c->out, "FileName");
    c->out.write(&c->out, ':');
    json_write_string(&c->out, FS_ROOT);
    memset(&st, 0, sizeof(st));
    if (stat("/", &st) == 0) {
        FileAttrs attrs;
        fill_attrs(&attrs, &st);
        c->out.write(&c->out, ',');
        json_write_string(&c->out, "Attrs");
        c->out.write(&c->out, ':');
        write_file_attrs(&c->out, &attrs);
    }
    c->out.write(&c->out, '}');
#else
    c->out.write(&c->out, '{');
    json_write_string(&c->out, "FileName");
    c->out.write(&c->out, ':');
    json_write_string(&c->out, "/");
    memset(&st, 0, sizeof(st));
    if (stat("/", &st) == 0) {
        FileAttrs attrs;
        fill_attrs(&attrs, &st);
        c->out.write(&c->out, ',');
        json_write_string(&c->out, "Attrs");
        c->out.write(&c->out, ':');
        write_file_attrs(&c->out, &attrs);
    }
    c->out.write(&c->out, '}');
#endif

    c->out.write(&c->out, ']');
    c->out.write(&c->out, 0);
    write_fs_errno(&c->out, err);

    c->out.write(&c->out, MARKER_EOM);
}

void ini_file_system_service(Protocol * proto) {
    int i;

    add_channel_close_listener(channel_close_listener);
    list_init(&file_info_ring);
    for (i = 0; i < HANDLE_HASH_SIZE; i++) {
        list_init(&handle_hash[i]);
    }

    add_command_handler(proto, FILE_SYSTEM, "open", command_open);
    add_command_handler(proto, FILE_SYSTEM, "close", command_close);
    add_command_handler(proto, FILE_SYSTEM, "read", command_read);
    add_command_handler(proto, FILE_SYSTEM, "write", command_write);
    add_command_handler(proto, FILE_SYSTEM, "stat", command_stat);
    add_command_handler(proto, FILE_SYSTEM, "lstat", command_lstat);
    add_command_handler(proto, FILE_SYSTEM, "fstat", command_fstat);
    add_command_handler(proto, FILE_SYSTEM, "setstat", command_setstat);
    add_command_handler(proto, FILE_SYSTEM, "fsetstat", command_fsetstat);
    add_command_handler(proto, FILE_SYSTEM, "opendir", command_opendir);
    add_command_handler(proto, FILE_SYSTEM, "readdir", command_readdir);
    add_command_handler(proto, FILE_SYSTEM, "remove", command_remove);
    add_command_handler(proto, FILE_SYSTEM, "rmdir", command_rmdir);
    add_command_handler(proto, FILE_SYSTEM, "mkdir", command_mkdir);
    add_command_handler(proto, FILE_SYSTEM, "realpath", command_realpath);
    add_command_handler(proto, FILE_SYSTEM, "rename", command_rename);
    add_command_handler(proto, FILE_SYSTEM, "readlink", command_readlink);
    add_command_handler(proto, FILE_SYSTEM, "symlink", command_symlink);
    add_command_handler(proto, FILE_SYSTEM, "copy", command_copy);
    add_command_handler(proto, FILE_SYSTEM, "user", command_user);
    add_command_handler(proto, FILE_SYSTEM, "roots", command_roots);
}

#endif

