/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Target service implementation: file system access (TCF name FileSystem)
 */

#include <config.h>

#if SERVICE_FileSystem

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <assert.h>
#include <fcntl.h>
#include <sys/stat.h>
#if !defined(WIN32) || defined(__CYGWIN__)
#  include <utime.h>
#  include <dirent.h>
#endif
#include <framework/myalloc.h>
#include <framework/asyncreq.h>
#include <framework/streams.h>
#include <framework/channel.h>
#include <framework/link.h>
#include <framework/trace.h>
#include <framework/json.h>
#include <framework/exceptions.h>
#include <framework/protocol.h>
#include <services/filesystem.h>

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
    FSERR_EOF               = 0x10001,
    FSERR_NO_SUCH_FILE      = 0x10002,
    FSERR_PERMISSION_DENIED = 0x10003;

#define REQ_READ            1
#define REQ_WRITE           2
#define REQ_FSTAT           3
#define REQ_FSETSTAT        4
#define REQ_CLOSE           5

typedef struct OpenFileInfo OpenFileInfo;
typedef struct IORequest IORequest;
typedef struct FileAttrs FileAttrs;

struct FileAttrs {
    int flags;
    int64_t size;
    int uid;
    int gid;
    int permissions;
    uint64_t atime;
    uint64_t mtime;
};

struct OpenFileInfo {
    unsigned long handle;
    char path[FILE_PATH_SIZE];
    int file;
    DIR * dir;
    InputStream * inp;
    OutputStream * out;
    LINK link_ring;
    LINK link_hash;
    LINK link_reqs;
    IORequest * posted_req;
};

struct IORequest {
    int req;
    char token[256];
    OpenFileInfo * handle;
    FileAttrs attrs;
    AsyncReqInfo info;
    LINK link_reqs;
};

#define hash2file(A)    ((OpenFileInfo *)((char *)(A) - offsetof(OpenFileInfo, link_hash)))
#define ring2file(A)    ((OpenFileInfo *)((char *)(A) - offsetof(OpenFileInfo, link_ring)))
#define reqs2req(A)     ((IORequest *)((char *)(A) - offsetof(IORequest, link_reqs)))

static unsigned long handle_cnt = 0;

#define HANDLE_HASH_SIZE 0x100
static LINK handle_hash[HANDLE_HASH_SIZE];
static LINK file_info_ring = { NULL, NULL };

static OpenFileInfo * create_open_file_info(Channel * ch, char * path, int file, DIR * dir) {
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
    h->inp = &ch->inp;
    h->out = &ch->out;
    list_add_first(&h->link_ring, &file_info_ring);
    list_add_first(&h->link_hash, list_head);
    list_init(&h->link_reqs);
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
    assert(list_is_empty(&h->link_reqs));
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
                int posted = 0;
                while (!list_is_empty(&h->link_reqs)) {
                    LINK * link = h->link_reqs.next;
                    IORequest * req = reqs2req(link);
                    list_remove(link);
                    if (h->posted_req == req) {
                        req->handle = NULL;
                        posted = 1;
                    }
                    else {
                        loc_free(req->info.u.fio.bufp);
                        loc_free(req);
                    }
                }
                if (!posted) close(h->file);
            }
            list_add_last(&h->link_hash, &list);
        }
    }

    while (!list_is_empty(&list)) delete_open_file_info(hash2file(list.next));
}

static void write_fs_errno(OutputStream * out, int err) {
    switch (err) {
    case ERR_EOF:
        write_service_error(out, err, FILE_SYSTEM, FSERR_EOF);
        break;
    case ENOENT:
        write_service_error(out, err, FILE_SYSTEM, FSERR_NO_SUCH_FILE);
        break;
    case EACCES:
        write_service_error(out, err, FILE_SYSTEM, FSERR_PERMISSION_DENIED);
        break;
    default:
        write_errno(out, err);
        break;
    }
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
    write_stream(out, 0);
}

static void fill_attrs(FileAttrs * attrs, struct stat * buf) {
    memset(attrs, 0, sizeof(FileAttrs));
    attrs->flags |= ATTR_SIZE | ATTR_UIDGID | ATTR_PERMISSIONS | ATTR_ACMODTIME;
    attrs->size = buf->st_size;
    attrs->uid = buf->st_uid;
    attrs->gid = buf->st_gid;
    attrs->permissions = buf->st_mode;
    attrs->atime = (uint64_t)buf->st_atime * 1000;
    attrs->mtime = (uint64_t)buf->st_mtime * 1000;
}

static void read_file_attrs(InputStream * inp, const char * nm, void * arg) {
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
        attrs->atime = json_read_uint64(inp);
        attrs->flags |= ATTR_ACMODTIME;
    }
    else if (strcmp(nm, "MTime") == 0) {
        attrs->mtime = json_read_uint64(inp);
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

    write_stream(out, '{');
    if (attrs->flags & ATTR_SIZE) {
        json_write_string(out, "Size");
        write_stream(out, ':');
        json_write_int64(out, attrs->size);
        cnt++;
    }
    if (attrs->flags & ATTR_UIDGID) {
        if (cnt) write_stream(out, ',');
        json_write_string(out, "UID");
        write_stream(out, ':');
        json_write_long(out, attrs->uid);
        write_stream(out, ',');
        json_write_string(out, "GID");
        write_stream(out, ':');
        json_write_long(out, attrs->gid);
        cnt++;
    }
    if (attrs->flags & ATTR_PERMISSIONS) {
        if (cnt) write_stream(out, ',');
        json_write_string(out, "Permissions");
        write_stream(out, ':');
        json_write_long(out, attrs->permissions);
        cnt++;
    }
    if (attrs->flags & ATTR_ACMODTIME) {
        if (cnt) write_stream(out, ',');
        json_write_string(out, "ATime");
        write_stream(out, ':');
        json_write_uint64(out, attrs->atime);
        write_stream(out, ',');
        json_write_string(out, "MTime");
        write_stream(out, ':');
        json_write_uint64(out, attrs->mtime);
        cnt++;
    }
    write_stream(out, '}');
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
    {
        extern DL_LIST iosDvList;
        DEV_HDR * dev;
        for (dev = (DEV_HDR *)DLL_FIRST(&iosDvList); dev != NULL; dev = (DEV_HDR *)DLL_NEXT(&dev->node)) {
            if (strncmp(path, dev->name, strlen(dev->name)) == 0) return;
        }
    }
#endif
    if (path[0] == 0) {
        strlcpy(path, get_user_home(), size);
    }
    else if (path[0] != '/') {
        snprintf(buf, sizeof(buf), "%s/%s", get_user_home(), path);
        strlcpy(path, buf, size);
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
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    flags = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    memset(&attrs, 0, sizeof(FileAttrs));
    json_read_struct(&c->inp, read_file_attrs, &attrs);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if ((attrs.flags & ATTR_PERMISSIONS) == 0) {
        attrs.permissions = 0775;
    }
    file = open(path, to_local_open_flags(flags), attrs.permissions);

    if (file < 0) {
        err = errno;
    }
    else {
        handle = create_open_file_info(c, path, file, NULL);
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_file_handle(&c->out, handle);
    write_stream(&c->out, MARKER_EOM);
}

static void reply_close(char * token, OutputStream * out, int err) {
    write_stringz(out, "R");
    write_stringz(out, token);
    write_fs_errno(out, err);
    write_stream(out, MARKER_EOM);
}

static void reply_read(char * token, OutputStream * out, int err, void * buf, unsigned len, int eof) {
    write_stringz(out, "R");
    write_stringz(out, token);
    json_write_binary(out, buf, len);
    write_stream(out, 0);
    write_fs_errno(out, err);
    json_write_boolean(out, eof);
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void reply_write(char * token, OutputStream * out, int err) {
    write_stringz(out, "R");
    write_stringz(out, token);
    write_fs_errno(out, err);
    write_stream(out, MARKER_EOM);
}

static void reply_stat(char * token, OutputStream * out, int err, struct stat * buf) {
    FileAttrs attrs;

    if (err == 0) fill_attrs(&attrs, buf);
    else memset(&attrs, 0, sizeof(attrs));

    write_stringz(out, "R");
    write_stringz(out, token);
    write_fs_errno(out, err);
    write_file_attrs(out, &attrs);
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void reply_setstat(char * token, OutputStream * out, int err) {
    write_stringz(out, "R");
    write_stringz(out, token);
    write_fs_errno(out, err);
    write_stream(out, MARKER_EOM);
}

static void post_io_requst(OpenFileInfo * handle);

static void done_io_request(void * arg) {
    int err = 0;
    IORequest * req = (IORequest *)((AsyncReqInfo *)arg)->client_data;
    OpenFileInfo * handle = req->handle;

    if (handle == NULL) {
        /* Abandoned I/O request, channel is already closed */
        switch (req->req) {
        case REQ_READ:
        case REQ_WRITE:
            close(req->info.u.fio.fd);
            break;
        case REQ_CLOSE:
            break;
        default:
            assert(0);
        }
        loc_free(req->info.u.fio.bufp);
        loc_free(req);
        return;
    }

    assert(handle->posted_req == req);
    assert(&handle->posted_req->link_reqs == handle->link_reqs.next);
    handle->posted_req = NULL;
    list_remove(&req->link_reqs);

    switch (req->req) {
    case REQ_READ:
        if (req->info.error) {
            reply_read(req->token, handle->out, req->info.error, NULL, 0, 0);
        }
        else {
            reply_read(req->token, handle->out, 0,
                req->info.u.fio.bufp, req->info.u.fio.rval,
                (size_t)req->info.u.fio.rval < req->info.u.fio.bufsz);
        }
        break;
    case REQ_WRITE:
        if (req->info.error) err = req->info.error;
        else if ((size_t)req->info.u.fio.rval < req->info.u.fio.bufsz) err = ENOSPC;
        reply_write(req->token, handle->out, err);
        break;
    case REQ_CLOSE:
        err = req->info.error;
        reply_close(req->token, handle->out, err);
        if (err == 0) {
            loc_free(req);
            while (!list_is_empty(&handle->link_reqs)) {
                LINK * link = handle->link_reqs.next;
                req = reqs2req(link);
                switch (req->req) {
                case REQ_READ:
                    reply_read(req->token, handle->out, EBADF, NULL, 0, 0);
                    break;
                case REQ_WRITE:
                    reply_write(req->token, handle->out, EBADF);
                    break;
                case REQ_FSTAT:
                    reply_stat(req->token, handle->out, EBADF, NULL);
                    break;
                case REQ_FSETSTAT:
                    reply_setstat(req->token, handle->out, EBADF);
                    break;
                case REQ_CLOSE:
                    reply_close(req->token, handle->out, EBADF);
                    break;
                default:
                    assert(0);
                }
                list_remove(link);
                loc_free(req->info.u.fio.bufp);
                loc_free(req);
            }
            delete_open_file_info(handle);
            return;
        }
        break;
    default:
        assert(0);
    }

    loc_free(req->info.u.fio.bufp);
    loc_free(req);
    post_io_requst(handle);
}

static void post_io_requst(OpenFileInfo * handle) {
    while (handle->posted_req == NULL && !list_is_empty(&handle->link_reqs)) {
        LINK * link = handle->link_reqs.next;
        IORequest * req = reqs2req(link);
        switch (req->req) {
        case REQ_FSTAT:
            {
                int err = 0;
                struct stat buf;
                memset(&buf, 0, sizeof(buf));
                if (fstat(handle->file, &buf) < 0) err = errno;
                reply_stat(req->token, handle->out, err, &buf);
                list_remove(link);
                loc_free(req);
            }
            continue;
        case REQ_FSETSTAT:
            {
                int err = 0;
                FileAttrs attrs = req->attrs;
                if (attrs.flags & ATTR_SIZE) {
                    if (ftruncate(handle->file, attrs.size) < 0) err = errno;
                }
#if defined(WIN32) || defined(_WRS_KERNEL)
                if (attrs.flags & ATTR_PERMISSIONS) {
                    if (chmod(handle->path, attrs.permissions) < 0) err = errno;
                }
#else
                if (attrs.flags & ATTR_UIDGID) {
                    if (fchown(handle->file, attrs.uid, attrs.gid) < 0) err = errno;
                }
                if (attrs.flags & ATTR_PERMISSIONS) {
                    if (fchmod(handle->file, attrs.permissions) < 0) err = errno;
                }
#endif
                if (attrs.flags & ATTR_ACMODTIME) {
                    struct utimbuf buf;
                    buf.actime = (time_t)(attrs.atime / 1000);
                    buf.modtime = (time_t)(attrs.mtime / 1000);
                    if (utime(handle->path, &buf) < 0) err = errno;
                }
                reply_setstat(req->token, handle->out, err);
                list_remove(link);
                loc_free(req);
            }
            continue;
        }
        handle->posted_req = req;
        async_req_post(&req->info);
    }
}

static IORequest * create_io_request(char * token, OpenFileInfo * handle, int type) {
    IORequest * req = (IORequest *)loc_alloc_zero(sizeof(IORequest));
    req->req = type;
    req->handle = handle;
    req->info.done = done_io_request;
    req->info.client_data = req;
    strlcpy(req->token, token, sizeof(req->token));
    list_add_last(&req->link_reqs, &handle->link_reqs);
    return req;
}

static void command_close(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

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
        IORequest * req = create_io_request(token, h, REQ_CLOSE);
        req->info.type = AsyncReqClose;
        req->info.u.fio.fd = h->file;
        post_io_requst(h);
        return;
    }

    reply_close(token, &c->out, err);
}

static void command_read(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;
    int64_t offset;
    unsigned long len;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    offset = json_read_int64(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    len = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    h = find_open_file_info(id);
    if (h == NULL) {
        reply_read(token, &c->out, EBADF, NULL, 0, 0);
    }
    else {
        IORequest * req = create_io_request(token, h, REQ_READ);
        if (offset < 0) {
            req->info.type = AsyncReqRead;
        }
        else {
            req->info.type = AsyncReqSeekRead;
            req->info.u.fio.offset = (off_t)offset;
        }
        req->info.u.fio.fd = h->file;
        req->info.u.fio.bufp = loc_alloc(len);
        req->info.u.fio.bufsz = len;
        post_io_requst(h);
    }
}

static void command_write(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;
    int64_t offset;
    unsigned long len = 0;
    JsonReadBinaryState state;

    static size_t buf_size = 0;
    static char * buf = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    offset = json_read_int64(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);

    json_read_binary_start(&state, &c->inp);

    h = find_open_file_info(id);
    for (;;) {
        int rd;
        if (buf_size < len + BUF_SIZE) {
            buf_size += BUF_SIZE;
            buf = (char *)loc_realloc(buf, buf_size);
        }
        rd = json_read_binary_data(&state, buf + len, buf_size - len);
        if (rd == 0) break;
        len += rd;
    }
    json_read_binary_end(&state);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (h == NULL) {
        reply_write(token, &c->out, EBADF);
    }
    else {
        IORequest * req = create_io_request(token, h, REQ_WRITE);
        if (offset < 0) {
            req->info.type = AsyncReqWrite;
        }
        else {
            req->info.type = AsyncReqSeekWrite;
            req->info.u.fio.offset = (off_t)offset;
        }
        req->info.u.fio.fd = h->file;
        req->info.u.fio.bufp = loc_alloc(len);
        req->info.u.fio.bufsz = len;
        memcpy(req->info.u.fio.bufp, buf, len);
        post_io_requst(h);
    }
}

static void command_stat(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    struct stat buf;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    memset(&buf, 0, sizeof(buf));
    if (stat(path, &buf) < 0) err = errno;

    reply_stat(token, &c->out, err, &buf);
}

static void command_lstat(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    struct stat buf;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    memset(&buf, 0, sizeof(buf));
    if (lstat(path, &buf) < 0) err = errno;

    reply_stat(token, &c->out, err, &buf);
}

static void command_fstat(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    h = find_open_file_info(id);
    if (h == NULL) {
        reply_stat(token, &c->out, EBADF, NULL);
    }
    else {
        create_io_request(token, h, REQ_FSTAT);
        post_io_requst(h);
    }
}

static void command_setstat(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    FileAttrs attrs;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    memset(&attrs, 0, sizeof(FileAttrs));
    json_read_struct(&c->inp, read_file_attrs, &attrs);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

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
        buf.actime = (time_t)(attrs.atime / 1000);
        buf.modtime = (time_t)(attrs.mtime / 1000);
        if (utime(path, &buf) < 0) err = errno;
    }

    reply_setstat(token, &c->out, err);
}

static void command_fsetstat(char * token, Channel * c) {
    char id[256];
    FileAttrs attrs;
    OpenFileInfo * h = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    memset(&attrs, 0, sizeof(FileAttrs));
    json_read_struct(&c->inp, read_file_attrs, &attrs);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    h = find_open_file_info(id);
    if (h == NULL) {
        reply_setstat(token, &c->out, EBADF);
    }
    else {
        IORequest * req = create_io_request(token, h, REQ_FSETSTAT);
        req->attrs = attrs;
        post_io_requst(h);
    }
}

static void command_opendir(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    DIR * dir = NULL;
    int err = 0;
    OpenFileInfo * handle = NULL;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    dir = opendir(path);
    if (dir == NULL) {
        err = errno;
    }
    else {
        handle = create_open_file_info(c, path, -1, dir);
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_file_handle(&c->out, handle);
    write_stream(&c->out, MARKER_EOM);
}

static void command_readdir(char * token, Channel * c) {
    char id[256];
    OpenFileInfo * h = NULL;
    int err = 0;
    int eof = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    h = find_open_file_info(id);
    if (h == NULL || h->dir == NULL) {
        write_stringz(&c->out, "null");
        err = EBADF;
    }
    else {
        int cnt = 0;
        write_stream(&c->out, '[');
        while (cnt < 64) {
            struct dirent * e;
            char path[FILE_PATH_SIZE];
            struct stat st;
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
            if (cnt > 0) write_stream(&c->out, ',');
            write_stream(&c->out, '{');
            json_write_string(&c->out, "FileName");
            write_stream(&c->out, ':');
            json_write_string(&c->out, e->d_name);
            memset(&st, 0, sizeof(st));
            snprintf(path, sizeof(path), "%s/%s", h->path, e->d_name);
            if (stat(path, &st) == 0) {
                fill_attrs(&attrs, &st);
                write_stream(&c->out, ',');
                json_write_string(&c->out, "Attrs");
                write_stream(&c->out, ':');
                write_file_attrs(&c->out, &attrs);
            }
            write_stream(&c->out, '}');
            cnt++;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }

    write_fs_errno(&c->out, err);
    json_write_boolean(&c->out, eof);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_remove(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (remove(path) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_rmdir(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (rmdir(path) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_mkdir(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    FileAttrs attrs;
    int err = 0;
    int mode = 0777;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    memset(&attrs, 0, sizeof(FileAttrs));
    json_read_struct(&c->inp, read_file_attrs, &attrs);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (attrs.flags & ATTR_PERMISSIONS) {
        mode = attrs.permissions;
    }
#if defined(_WRS_KERNEL)
    if (mkdir(path) < 0) err = errno;
#else
    if (mkdir(path, mode) < 0) err = errno;
#endif

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_realpath(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    char * real = NULL;
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    real = canonicalize_file_name(path);
    if (real == NULL) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    json_write_string(&c->out, real);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
    free(real);
}

static void command_rename(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    char newp[FILE_PATH_SIZE];
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    read_path(&c->inp, newp, sizeof(newp));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (rename(path, newp) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_readlink(char * token, Channel * c) {
    char path[FILE_PATH_SIZE];
    char link[FILE_PATH_SIZE];
    int err = 0;

    read_path(&c->inp, path, sizeof(path));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

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
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_symlink(char * token, Channel * c) {
    char link[FILE_PATH_SIZE];
    char target[FILE_PATH_SIZE];
    int err = 0;

    read_path(&c->inp, link, sizeof(link));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    read_path(&c->inp, target, sizeof(target));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if defined(WIN32) || defined(_WRS_KERNEL)
    err = ENOSYS;
#else
    if (symlink(target, link) < 0) err = errno;
#endif

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_fs_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_copy(char * token, Channel * c) {
    char src[FILE_PATH_SIZE];
    char dst[FILE_PATH_SIZE];
    int copy_uidgid;
    int copy_perms;
    struct stat st;
    int fi = -1;
    int fo = -1;
    int err = 0;
    int64_t pos = 0;

    read_path(&c->inp, src, sizeof(src));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    read_path(&c->inp, dst, sizeof(dst));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    copy_uidgid = json_read_boolean(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    copy_perms = json_read_boolean(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (stat(src, &st) < 0) err = errno;
    if (err == 0 && (fi = open(src, O_RDONLY | O_BINARY, 0)) < 0) err = errno;
    if (err == 0 && (fo = open(dst, O_WRONLY | O_BINARY | O_CREAT, 0775)) < 0) err = errno;

    while (err == 0 && pos < st.st_size) {
        char buf[BUF_SIZE];
        int wr = 0;
        int rd = read(fi, buf, sizeof(buf));
        if (rd == 0) break;
        if (rd < 0) {
            err = errno;
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
    write_stream(&c->out, MARKER_EOM);
}

static void command_user(char * token, Channel * c) {
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    json_write_long(&c->out, getuid());
    write_stream(&c->out, 0);
    json_write_long(&c->out, geteuid());
    write_stream(&c->out, 0);
    json_write_long(&c->out, getgid());
    write_stream(&c->out, 0);
    json_write_long(&c->out, getegid());
    write_stream(&c->out, 0);
    json_write_string(&c->out, get_user_home());
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

static void command_roots(char * token, Channel * c) {
    struct stat st;
    int err = 0;

    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_stream(&c->out, '[');

#ifdef WIN32
    {
        int cnt = 0;
        int disk = 0;
        DWORD disks = GetLogicalDrives();
        for (disk = 0; disk <= 30; disk++) {
            if (disks & (1 << disk)) {
                char path[32];
                snprintf(path, sizeof(path), "%c:/", 'A' + disk);
                if (cnt > 0) write_stream(&c->out, ',');
                write_stream(&c->out, '{');
                json_write_string(&c->out, "FileName");
                write_stream(&c->out, ':');
                json_write_string(&c->out, path);
                if (disk >= 2) {
                    ULARGE_INTEGER total_number_of_bytes;
                    BOOL has_size = GetDiskFreeSpaceExA(path, NULL, &total_number_of_bytes, NULL);
                    memset(&st, 0, sizeof(st));
                    if (has_size && stat(path, &st) == 0) {
                        FileAttrs attrs;
                        fill_attrs(&attrs, &st);
                        write_stream(&c->out, ',');
                        json_write_string(&c->out, "Attrs");
                        write_stream(&c->out, ':');
                        write_file_attrs(&c->out, &attrs);
                    }
                }
                write_stream(&c->out, '}');
                cnt++;
            }
        }
    }
#elif defined(_WRS_KERNEL)
    {
        extern DL_LIST iosDvList;
        DEV_HDR * dev;
        int cnt = 0;
        for (dev = (DEV_HDR *)DLL_FIRST(&iosDvList); dev != NULL; dev = (DEV_HDR *)DLL_NEXT(&dev->node)) {
            FileAttrs attrs;
            char path[FILE_PATH_SIZE];
            if (strcmp(dev->name, "host:") == 0) {
                /* Windows host is special case */
                int d;
                for (d = 'a'; d < 'z'; d++) {
                    snprintf(path, sizeof(path), "%s%c:/", dev->name, d);
                    if (stat(path, &st) == 0 && S_ISDIR(st.st_mode)) {
                        fill_attrs(&attrs, &st);
                        if (cnt > 0) write_stream(&c->out, ',');
                        write_stream(&c->out, '{');
                        json_write_string(&c->out, "FileName");
                        write_stream(&c->out, ':');
                        json_write_string(&c->out, path);
                        write_stream(&c->out, ',');
                        json_write_string(&c->out, "Attrs");
                        write_stream(&c->out, ':');
                        write_file_attrs(&c->out, &attrs);
                        write_stream(&c->out, '}');
                        cnt++;
                    }
                }
            }
            snprintf(path, sizeof(path), "%s/", dev->name);
            if (stat(path, &st) == 0 && S_ISDIR(st.st_mode)) {
                fill_attrs(&attrs, &st);
                if (cnt > 0) write_stream(&c->out, ',');
                write_stream(&c->out, '{');
                json_write_string(&c->out, "FileName");
                write_stream(&c->out, ':');
                json_write_string(&c->out, path);
                write_stream(&c->out, ',');
                json_write_string(&c->out, "Attrs");
                write_stream(&c->out, ':');
                write_file_attrs(&c->out, &attrs);
                write_stream(&c->out, '}');
                cnt++;
            }
        }
    }
#else
    write_stream(&c->out, '{');
    json_write_string(&c->out, "FileName");
    write_stream(&c->out, ':');
    json_write_string(&c->out, "/");
    memset(&st, 0, sizeof(st));
    if (stat("/", &st) == 0) {
        FileAttrs attrs;
        fill_attrs(&attrs, &st);
        write_stream(&c->out, ',');
        json_write_string(&c->out, "Attrs");
        write_stream(&c->out, ':');
        write_file_attrs(&c->out, &attrs);
    }
    write_stream(&c->out, '}');
#endif

    write_stream(&c->out, ']');
    write_stream(&c->out, 0);
    write_fs_errno(&c->out, err);

    write_stream(&c->out, MARKER_EOM);
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

#endif /* SERVICE_FileSystem */


