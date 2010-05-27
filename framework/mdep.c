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
 *     Nokia - Symbian support
 *******************************************************************************/

/*
 * Machine and OS dependent definitions.
 * This module implements host OS abstraction layer that helps make
 * agent code portable between Linux, Windows, VxWorks and potentially other OSes.
 */

#include <config.h>
#include <stdio.h>
#include <assert.h>
#include <errno.h>
#include <signal.h>
#include <framework/myalloc.h>
#include <framework/errors.h>

pthread_attr_t pthread_create_attr;

#if defined(WIN32)

#include <process.h>
#include <fcntl.h>
#include <MSWSock.h>

/*********************************************************************
    Support of pthreads on Windows is implemented according to
    recommendations from the paper:

    Strategies for Implementing POSIX Condition Variables on Win32
    C++ Report, SIGS, Vol. 10, No. 5, June, 1998

    Douglas C. Schmidt and Irfan Pyarali
    Department of Computer Science
    Washington University, St. Louis, Missouri
**********************************************************************/

typedef struct {
    int waiters_count;
    CRITICAL_SECTION waiters_count_lock;
    HANDLE sema;
    HANDLE waiters_done;
    size_t was_broadcast;
} PThreadCond;

int pthread_mutex_init(pthread_mutex_t * mutex, const pthread_mutexattr_t * attr) {
    assert(attr == NULL);
    *mutex = (pthread_mutex_t)CreateMutex(NULL, FALSE, NULL);
    if (*mutex == NULL) return set_win32_errno(GetLastError());
    return 0;
}

int pthread_mutex_lock(pthread_mutex_t * mutex) {
    assert(mutex != NULL);
    assert(*mutex != NULL);
    if (WaitForSingleObject(*mutex, INFINITE) == WAIT_FAILED) return set_win32_errno(GetLastError());
    return 0;
}

int pthread_mutex_unlock(pthread_mutex_t * mutex) {
    assert(mutex != NULL);
    assert(*mutex != NULL);
    if (!ReleaseMutex(*mutex)) return set_win32_errno(GetLastError());
    return 0;
}

int pthread_cond_init(pthread_cond_t * cond, const pthread_condattr_t * attr) {
    PThreadCond * p = (PThreadCond *)loc_alloc_zero(sizeof(PThreadCond));
    assert(attr == NULL);
    p->waiters_count = 0;
    p->was_broadcast = 0;
    p->sema = CreateSemaphore(NULL, 0, 0x7fffffff, NULL);
    if (p->sema == NULL) return set_win32_errno(GetLastError());
    InitializeCriticalSection(&p->waiters_count_lock);
    p->waiters_done = CreateEvent(NULL, FALSE, FALSE, NULL);
    if (p->waiters_done == NULL) return set_win32_errno(GetLastError());
    *cond = (pthread_cond_t)p;
    return 0;
}

int pthread_cond_wait(pthread_cond_t * cond, pthread_mutex_t * mutex) {
    DWORD res = 0;
    int last_waiter = 0;
    PThreadCond * p = (PThreadCond *)*cond;

    EnterCriticalSection(&p->waiters_count_lock);
    p->waiters_count++;
    LeaveCriticalSection(&p->waiters_count_lock);

    /* This call atomically releases the mutex and waits on the */
    /* semaphore until <pthread_cond_signal> or <pthread_cond_broadcast> */
    /* are called by another thread. */
    res = SignalObjectAndWait(*mutex, p->sema, INFINITE, FALSE);
    if (res == WAIT_FAILED) return set_win32_errno(GetLastError());

    /* Re-acquire lock to avoid race conditions. */
    EnterCriticalSection(&p->waiters_count_lock);

    /* We're no longer waiting... */
    p->waiters_count--;

    /* Check to see if we're the last waiter after <pthread_cond_broadcast>. */
    last_waiter = p->was_broadcast && p->waiters_count == 0;

    LeaveCriticalSection(&p->waiters_count_lock);

    /* If we're the last waiter thread during this particular broadcast */
    /* then let all the other threads proceed. */
    if (last_waiter) {
        /* This call atomically signals the <waiters_done_> event and waits until */
        /* it can acquire the <mutex>.  This is required to ensure fairness.  */
        DWORD err = SignalObjectAndWait(p->waiters_done, *mutex, INFINITE, FALSE);
        if (err == WAIT_FAILED) return set_win32_errno(GetLastError());
    }
    else {
        /* Always regain the external mutex since that's the guarantee we */
        /* give to our callers.  */
        DWORD err = WaitForSingleObject(*mutex, INFINITE);
        if (err == WAIT_FAILED) return set_win32_errno(GetLastError());
    }
    assert(res == WAIT_OBJECT_0);
    return 0;
}

int pthread_cond_timedwait(pthread_cond_t * cond, pthread_mutex_t * mutex, const struct timespec * abstime) {
    DWORD res = 0;
    int last_waiter = 0;
    PThreadCond * p = (PThreadCond *)*cond;
    DWORD timeout = 0;
    struct timespec timenow;

    if (clock_gettime(CLOCK_REALTIME, &timenow)) return errno;
    if (abstime->tv_sec < timenow.tv_sec) return ETIMEDOUT;
    if (abstime->tv_sec == timenow.tv_sec) {
        if (abstime->tv_nsec <= timenow.tv_nsec) return ETIMEDOUT;
    }
    timeout = (DWORD)((abstime->tv_sec - timenow.tv_sec) * 1000 + (abstime->tv_nsec - timenow.tv_nsec) / 1000000 + 5);

    EnterCriticalSection(&p->waiters_count_lock);
    p->waiters_count++;
    LeaveCriticalSection(&p->waiters_count_lock);

    /* This call atomically releases the mutex and waits on the */
    /* semaphore until <pthread_cond_signal> or <pthread_cond_broadcast> */
    /* are called by another thread. */
    res = SignalObjectAndWait(*mutex, p->sema, timeout, FALSE);
    if (res == WAIT_FAILED) return set_win32_errno(GetLastError());

    /* Re-acquire lock to avoid race conditions. */
    EnterCriticalSection(&p->waiters_count_lock);

    /* We're no longer waiting... */
    p->waiters_count--;

    /* Check to see if we're the last waiter after <pthread_cond_broadcast>. */
    last_waiter = p->was_broadcast && p->waiters_count == 0;

    LeaveCriticalSection(&p->waiters_count_lock);

    /* If we're the last waiter thread during this particular broadcast */
    /* then let all the other threads proceed. */
    if (last_waiter) {
        /* This call atomically signals the <waiters_done> event and waits until */
        /* it can acquire the <mutex>.  This is required to ensure fairness.  */
        DWORD err = SignalObjectAndWait(p->waiters_done, *mutex, INFINITE, FALSE);
        if (err == WAIT_FAILED) return set_win32_errno(GetLastError());
    }
    else {
        /* Always regain the external mutex since that's the guarantee we */
        /* give to our callers.  */
        DWORD err = WaitForSingleObject(*mutex, INFINITE);
        if (err == WAIT_FAILED) return set_win32_errno(GetLastError());
    }

    if (res == WAIT_TIMEOUT) return errno = ETIMEDOUT;
    assert(res == WAIT_OBJECT_0);
    return 0;
}

int pthread_cond_signal(pthread_cond_t * cond) {
    int have_waiters = 0;
    PThreadCond * p = (PThreadCond *)*cond;

    EnterCriticalSection(&p->waiters_count_lock);
    have_waiters = p->waiters_count > 0;
    LeaveCriticalSection(&p->waiters_count_lock);

    /* If there aren't any waiters, then this is a no-op.   */
    if (have_waiters) {
        if (!ReleaseSemaphore(p->sema, 1, 0)) return set_win32_errno(GetLastError());
    }
    return 0;
}

int pthread_cond_broadcast(pthread_cond_t * cond) {
    int have_waiters = 0;
    PThreadCond * p = (PThreadCond *)*cond;

    /* This is needed to ensure that <waiters_count_> and <was_broadcast_> are */
    /* consistent relative to each other. */
    EnterCriticalSection(&p->waiters_count_lock);

    if (p->waiters_count > 0) {
        /* We are broadcasting, even if there is just one waiter... */
        /* Record that we are broadcasting, which helps optimize */
        /* <pthread_cond_wait> for the non-broadcast case. */
        p->was_broadcast = 1;
        have_waiters = 1;
    }

    if (have_waiters) {
        /* Wake up all the waiters atomically. */
        if (!ReleaseSemaphore(p->sema, p->waiters_count, 0)) return set_win32_errno(GetLastError());

        LeaveCriticalSection(&p->waiters_count_lock);

        /* Wait for all the awakened threads to acquire the counting */
        /* semaphore.  */
        if (WaitForSingleObject(p->waiters_done, INFINITE) == WAIT_FAILED) return set_win32_errno(GetLastError());
        /* This assignment is okay, even without the <waiters_count_lock_> held  */
        /* because no other waiter threads can wake up to access it. */
        p->was_broadcast = 0;
    }
    else {
        LeaveCriticalSection(&p->waiters_count_lock);
    }
    return 0;
}

int pthread_cond_destroy(pthread_cond_t * cond) {
    PThreadCond * p = (PThreadCond *)*cond;

    DeleteCriticalSection(&p->waiters_count_lock);
    if (!CloseHandle(p->sema)) return set_win32_errno(GetLastError());
    if (!CloseHandle(p->waiters_done)) return set_win32_errno(GetLastError());

    loc_free(p);
    *cond = NULL;
    return 0;
}

typedef struct ThreadArgs ThreadArgs;

struct ThreadArgs {
    void * (*start)(void *);
    void * args;
};

static void start_thread(void * x) {
    ThreadArgs a = *(ThreadArgs *)x;

    loc_free(x);
    ExitThread((DWORD)a.start(a.args));
}

int pthread_create(pthread_t * res, const pthread_attr_t * attr,
                   void * (*start)(void *), void * args) {
    HANDLE thread = NULL;
    DWORD thread_id = 0;
    ThreadArgs * a = (ThreadArgs *)loc_alloc(sizeof(ThreadArgs));

    a->start = start;
    a->args = args;
    thread = CreateThread(0, 0, (LPTHREAD_START_ROUTINE)start_thread, a, 0, &thread_id);
    if (thread == NULL) {
        int err = set_win32_errno(GetLastError());
        loc_free(a);
        return errno = err;
    }
    if (!CloseHandle(thread)) return set_win32_errno(GetLastError());
    *res = (pthread_t)thread_id;
    return 0;
}

int pthread_join(pthread_t thread_id, void ** value_ptr) {
    int error = 0;
    HANDLE thread = OpenThread(SYNCHRONIZE | THREAD_QUERY_INFORMATION, FALSE, (DWORD)thread_id);

    if (thread == NULL) return set_win32_errno(GetLastError());
    if (WaitForSingleObject(thread, INFINITE) == WAIT_FAILED) error = set_win32_errno(GetLastError());
    if (!error && value_ptr != NULL && !GetExitCodeThread(thread, (LPDWORD)value_ptr)) error = set_win32_errno(GetLastError());
    if (!CloseHandle(thread) && !error) error = set_win32_errno(GetLastError());
    return error;
}

pthread_t pthread_self(void) {
    return (pthread_t)GetCurrentThreadId();
}

int pthread_equal(pthread_t thread1, pthread_t thread2) {
    return thread1 == thread2;
}

int pthread_attr_init(pthread_attr_t * attr) {
    *attr = NULL;
    return 0;
}

#ifndef SIO_UDP_CONNRESET
#define SIO_UDP_CONNRESET _WSAIOW(IOC_VENDOR,12)
#endif
#ifndef SIO_UDP_NETRESET
#define SIO_UDP_NETRESET _WSAIOW(IOC_VENDOR,15)
#endif
#undef socket
int wsa_socket(int af, int type, int protocol) {
    int res = 0;

    SetLastError(0);
    WSASetLastError(0);
    res = socket(af, type, protocol);
    if (res < 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    if (type == SOCK_DGRAM && protocol == IPPROTO_UDP) {
        DWORD dw = 0;
        BOOL b = FALSE;
        WSAIoctl(res, SIO_UDP_CONNRESET, &b, sizeof(b), NULL, 0, &dw, NULL, NULL);
        WSAIoctl(res, SIO_UDP_NETRESET, &b, sizeof(b), NULL, 0, &dw, NULL, NULL);
    }
    return res;
}

#undef connect
int wsa_connect(int socket, const struct sockaddr * addr, int addr_size) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = connect(socket, addr, addr_size);
    if (res != 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return 0;
}

#undef bind
int wsa_bind(int socket, const struct sockaddr * addr, int addr_size) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = bind(socket, addr, addr_size);
    if (res != 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return 0;
}

#undef listen
int wsa_listen(int socket, int size) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = listen(socket, size);
    if (res != 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return 0;
}

#undef recv
int wsa_recv(int socket, void * buf, size_t size, int flags) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = recv(socket, (char *)buf, size, flags);
    if (res < 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return res;
}

#undef recvfrom
int wsa_recvfrom(int socket, void * buf, size_t size, int flags,
                 struct sockaddr * addr, socklen_t * addr_size) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = recvfrom(socket, (char *)buf, size, flags, addr, addr_size);
    if (res < 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return res;
}

#undef send
int wsa_send(int socket, const void * buf, size_t size, int flags) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = send(socket, (char *)buf, size, flags);
    if (res < 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return res;
}

#undef sendto
int wsa_sendto(int socket, const void * buf, size_t size, int flags,
               const struct sockaddr * dest_addr, socklen_t dest_size) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = sendto(socket, (char *)buf, size, flags, dest_addr, dest_size);
    if (res < 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return res;
}

#undef setsockopt
int wsa_setsockopt(int socket, int level, int opt, const char * value, int size) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = setsockopt(socket, level, opt, value, size);
    if (res != 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return 0;
}

#undef getsockname
int wsa_getsockname(int socket, struct sockaddr * name, int * size) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = getsockname(socket, name, size);
    if (res != 0) {
        set_win32_errno(WSAGetLastError());
        return -1;
    }
    return 0;
}

/* inet_ntop()/inet_pton() are not available before Windows Vista */
const char * inet_ntop(int af, const void * src, char * dst, socklen_t size) {
    char * str = NULL;
    if (af != AF_INET) {
#ifdef EAFNOSUPPORT
        errno = EAFNOSUPPORT;
#else
        errno = EINVAL;
#endif
        return NULL;
    }
    str = inet_ntoa(*(struct in_addr *)src);
    if ((socklen_t)strlen(str) >= size) {
        errno = ENOSPC;
        return NULL;
    }
    return strcpy(dst, str);
}

int inet_pton(int af, const char * src, void * dst) {
    if (af != AF_INET) {
#ifdef EAFNOSUPPORT
        errno = EAFNOSUPPORT;
#else
        errno = EINVAL;
#endif
        return -1;
    }
    if (src == NULL || *src == 0) return 0;
    if ((((struct in_addr *)dst)->s_addr = inet_addr(src)) == INADDR_NONE) return 0;
    return 1;
}

#endif /* WIN32 */

#if defined(WIN32) && !defined(__CYGWIN__)

static __int64 file_time_to_unix_time(const FILETIME * ft) {
    __int64 res = (__int64)ft->dwHighDateTime << 32;

    res |= ft->dwLowDateTime;
    res /= 10;                   /* from 100 nano-sec periods to usec */
    res -= 11644473600000000ull; /* from Win epoch to Unix epoch */
    return res;
}

int clock_gettime(clockid_t clock_id, struct timespec * tp) {
    FILETIME ft;
    __int64 tim;

    assert(clock_id == CLOCK_REALTIME);
    if (!tp) {
        errno = EINVAL;
        return -1;
    }
    GetSystemTimeAsFileTime(&ft);
    tim = file_time_to_unix_time(&ft);
    tp->tv_sec  = (long)(tim / 1000000L);
    tp->tv_nsec = (long)(tim % 1000000L) * 1000;
    return 0;
}

void usleep(useconds_t useconds) {
    Sleep(useconds / 1000);
}

int truncate(const char * path, int64_t size) {
    int res = 0;
    int f = open(path, _O_RDWR | _O_BINARY, 0);
    if (f < 0) return -1;
    res = ftruncate(f, size);
    _close(f);
    return res;
}

int ftruncate(int fd, int64_t size) {
    int64_t cur, pos;
    BOOL ret = FALSE;
    HANDLE handle = (HANDLE)_get_osfhandle(fd);

    if (handle == INVALID_HANDLE_VALUE) {
        errno = EBADF;
        return -1;
    }
    /* save the current file pointer */
    cur = _lseeki64(fd, 0, SEEK_CUR);
    if (cur >= 0) {
        pos = _lseeki64(fd, size, SEEK_SET);
        if (pos >= 0) {
            ret = SetEndOfFile(handle);
            if (!ret) errno = EBADF;
        }
        /* restore the file pointer */
        _lseeki64(fd, cur, SEEK_SET);
    }
    return ret ? 0 : -1;
}

int getuid(void) {
    /* Windows user is always a superuser :) */
    return 0;
}

int geteuid(void) {
    return 0;
}

int getgid(void) {
    return 0;
}

int getegid(void) {
    return 0;
}

int utf8_stat(const char * name, struct utf8_stat * buf) {
    struct _stati64 tmp;
    wchar_t path[FILE_PATH_SIZE];
    if (!MultiByteToWideChar(CP_UTF8, 0, name, -1, path, sizeof(path) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return -1;
    }
    memset(&tmp, 0, sizeof(tmp));
    if (_wstati64(path, &tmp)) return -1;
    buf->st_dev = tmp.st_dev;
    buf->st_ino = tmp.st_ino;
    buf->st_mode = tmp.st_mode;
    buf->st_nlink = tmp.st_nlink;
    buf->st_uid = tmp.st_uid;
    buf->st_gid = tmp.st_gid;
    buf->st_rdev = tmp.st_rdev;
    buf->st_size = tmp.st_size;
    buf->st_atime = tmp.st_atime;
    buf->st_mtime = tmp.st_mtime;
    buf->st_ctime = tmp.st_ctime;
    return 0;
}

int utf8_fstat(int fd, struct utf8_stat * buf) {
    struct _stati64 tmp;
    memset(&tmp, 0, sizeof(tmp));
    if (_fstati64(fd, &tmp)) return -1;
    buf->st_dev = tmp.st_dev;
    buf->st_ino = tmp.st_ino;
    buf->st_mode = tmp.st_mode;
    buf->st_nlink = tmp.st_nlink;
    buf->st_uid = tmp.st_uid;
    buf->st_gid = tmp.st_gid;
    buf->st_rdev = tmp.st_rdev;
    buf->st_size = tmp.st_size;
    buf->st_atime = tmp.st_atime;
    buf->st_mtime = tmp.st_mtime;
    buf->st_ctime = tmp.st_ctime;
    return 0;
}

int utf8_open(const char * name, int flags, int perms) {
    wchar_t path[FILE_PATH_SIZE];
    if (!MultiByteToWideChar(CP_UTF8, 0, name, -1, path, sizeof(path) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return -1;
    }
    return _wopen(path, flags, perms);
}

int utf8_chmod(const char * name, int mode) {
    wchar_t path[FILE_PATH_SIZE];
    if (!MultiByteToWideChar(CP_UTF8, 0, name, -1, path, sizeof(path) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return -1;
    }
    return _wchmod(path, mode);
}

int utf8_remove(const char * name) {
    wchar_t path[FILE_PATH_SIZE];
    if (!MultiByteToWideChar(CP_UTF8, 0, name, -1, path, sizeof(path) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return -1;
    }
    return _wremove(path);
}

int utf8_rmdir(const char * name) {
    wchar_t path[FILE_PATH_SIZE];
    if (!MultiByteToWideChar(CP_UTF8, 0, name, -1, path, sizeof(path) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return -1;
    }
    return _wrmdir(path);
}

int utf8_mkdir(const char * name, int mode) {
    wchar_t path[FILE_PATH_SIZE];
    if (!MultiByteToWideChar(CP_UTF8, 0, name, -1, path, sizeof(path) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return -1;
    }
    return _wmkdir(path);
}

int utf8_rename(const char * name1, const char * name2) {
    wchar_t path1[FILE_PATH_SIZE];
    wchar_t path2[FILE_PATH_SIZE];
    if (!MultiByteToWideChar(CP_UTF8, 0, name1, -1, path1, sizeof(path1) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return -1;
    }
    if (!MultiByteToWideChar(CP_UTF8, 0, name2, -1, path2, sizeof(path2) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return -1;
    }
    return _wrename(path1, path2);
}

DIR * utf8_opendir(const char * path) {
    DIR * d = (DIR *)loc_alloc(sizeof(DIR));
    if (!d) { errno = ENOMEM; return 0; }
    strcpy(d->path, path);
    strcat(d->path, "/*.*");
    d->hdl = -1;
    return d;
}

struct dirent * utf8_readdir(DIR * d) {
    static struct dirent de;

    if (d->hdl < 0) {
        wchar_t path[FILE_PATH_SIZE];
        if (!MultiByteToWideChar(CP_UTF8, 0, d->path, -1, path, sizeof(path) / sizeof(wchar_t))) {
            set_win32_errno(GetLastError());
            return 0;
        }
        d->hdl = _wfindfirsti64(path, &d->blk);
        if (d->hdl < 0) {
            if (errno == ENOENT) errno = 0;
            return 0;
        }
    }
    else {
        int r = _wfindnexti64(d->hdl, &d->blk);
        if (r < 0) {
            if (errno == ENOENT) errno = 0;
            return 0;
        }
    }
    if (!WideCharToMultiByte(CP_UTF8, 0, d->blk.name, -1, de.d_name, sizeof(de.d_name), NULL, NULL)) {
        set_win32_errno(GetLastError());
        return 0;
    }
    de.d_size = d->blk.size;
    de.d_atime = d->blk.time_access;
    de.d_ctime = d->blk.time_create;
    de.d_wtime = d->blk.time_write;
    return &de;
}

int utf8_closedir(DIR * d) {
    int r = 0;
    if (!d) {
        errno = EBADF;
        return -1;
    }
    if (d->hdl >= 0) r = _findclose(d->hdl);
    loc_free(d);
    return r;
}

#endif /* defined(WIN32) && !defined(__CYGWIN__) */

#if defined(WIN32) && !defined(__CYGWIN__) || defined(_WRS_KERNEL) || defined(__SYMBIAN32__)

ssize_t pread(int fd, const void * buf, size_t size, off_t offset) {
    off_t offs0;
    ssize_t rd;
    if ((offs0 = lseek(fd, 0, SEEK_CUR)) == (off_t)-1) return -1;
    if (lseek(fd, offset, SEEK_SET) == (off_t)-1) return -1;
    rd = read(fd, (void *)buf, size);
    if (lseek(fd, offs0, SEEK_SET) == (off_t)-1) return -1;
    return rd;
}

ssize_t pwrite(int fd, const void * buf, size_t size, off_t offset) {
    off_t offs0;
    ssize_t wr;
    if ((offs0 = lseek(fd, 0, SEEK_CUR)) == (off_t)-1) return -1;
    if (lseek(fd, offset, SEEK_SET) == (off_t)-1) return -1;
    wr = write(fd, (void *)buf, size);
    if (lseek(fd, offs0, SEEK_SET) == (off_t)-1) return -1;
    return wr;
}

#endif /* defined(WIN32) && !defined(__CYGWIN__) || defined(_WRS_KERNEL) || defined(__SYMBIAN32__) */

#if defined(WIN32)

#include <shlobj.h>

char * get_os_name(void) {
    static char str[256];
    OSVERSIONINFOEX info;
    memset(&info, 0, sizeof(info));
    info.dwOSVersionInfoSize = sizeof(info);
    GetVersionEx((OSVERSIONINFO *)&info);
    switch (info.dwMajorVersion) {
    case 4:
        return "Windows NT";
    case 5:
        switch (info.dwMinorVersion) {
        case 0: return "Windows 2000";
        case 1: return "Windows XP";
        case 2: return "Windows Server 2003";
        }
        break;
    case 6:
        return "Windows Vista";
    }
    snprintf(str, sizeof(str), "Windows %d.%d", (int)info.dwMajorVersion, (int)info.dwMinorVersion);
    return str;
}

char * get_user_home(void) {
    WCHAR w_buf[MAX_PATH];
    static char a_buf[MAX_PATH];
    if (a_buf[0] != 0) return a_buf;
    if (!SUCCEEDED(SHGetFolderPathW(NULL, CSIDL_PROFILE, NULL, SHGFP_TYPE_CURRENT, w_buf))) {
        errno = ERR_OTHER;
        return NULL;
    }
    if (!WideCharToMultiByte(CP_UTF8, 0, w_buf, -1, a_buf, sizeof(a_buf), NULL, NULL)) {
        set_win32_errno(GetLastError());
        return 0;
    }
    return a_buf;
}

void ini_mdep(void) {
    WORD wVersionRequested = MAKEWORD(1, 1);
    WSADATA wsaData;
    int err;

    SetErrorMode(SEM_FAILCRITICALERRORS);
    err = WSAStartup(wVersionRequested, &wsaData);
    if (err != 0) {
        fprintf(stderr, "Couldn't access winsock.dll.\n");
        exit(1);
    }
    /* Confirm that the Windows Sockets DLL supports 1.1.*/
    /* Note that if the DLL supports versions greater */
    /* than 1.1 in addition to 1.1, it will still return */
    /* 1.1 in wVersion since that is the version we */
    /* requested.     */
    if (LOBYTE(wsaData.wVersion) != 1 || HIBYTE(wsaData.wVersion) != 1) {
        fprintf(stderr, "Unacceptable version of winsock.dll.\n");
        WSACleanup();
        exit(1);
    }
    pthread_attr_init(&pthread_create_attr);
#if defined(_DEBUG) && defined(_MSC_VER)
    _CrtSetDbgFlag(_CRTDBG_ALLOC_MEM_DF /* | _CRTDBG_LEAK_CHECK_DF */);
#endif
}

#elif defined(_WRS_KERNEL)

void usleep(useconds_t useconds) {
    struct timespec tv;
    tv.tv_sec = useconds / 1000000;
    tv.tv_nsec = (useconds % 1000000) * 1000;
    nanosleep(&tv, NULL);
}

int truncate(char * path, int64_t size) {
    int f = open(path, O_RDWR, 0);
    if (f < 0) return -1;
    if (ftruncate(f, size) < 0) {
        int err = errno;
        close(f);
        errno = err;
        return -1;
    }
    return close(f);
}

int getuid(void) {
    return 0;
}

int geteuid(void) {
    return 0;
}

int getgid(void) {
    return 0;
}

int getegid(void) {
    return 0;
}

char * get_os_name(void) {
    static char str[256];
#if _WRS_VXWORKS_MAJOR > 6 || _WRS_VXWORKS_MAJOR == 6 && _WRS_VXWORKS_MINOR >= 7
    snprintf(str, sizeof(str), "VxWorks %s", vxWorksVersion);
#else
    snprintf(str, sizeof(str), "VxWorks %s", kernelVersion());
#endif
    return str;
}

char * get_user_home(void) {
    return "/";
}

void ini_mdep(void) {
    pthread_attr_init(&pthread_create_attr);
    pthread_attr_setstacksize(&pthread_create_attr, 0x8000);
    pthread_attr_setname(&pthread_create_attr, "tTcf");
}

#elif defined(__SYMBIAN32__)

int truncate(const char * path, int64_t size) {
    int res = 0;
    int f = open(path, O_RDWR | O_BINARY, 0);
    if (f < 0) return -1;
    res = ftruncate(f, size);
    close(f);
    return res;
}

char * get_os_name(void) {
   static char str[] = "SYMBIAN";
   return str;
}

char * get_user_home(void) {
    static char buf[] = "C:";
    return buf;
}

void ini_mdep(void) {
    pthread_attr_init(&pthread_create_attr);
}

int loc_clock_gettime(int clock_id, struct timespec * now) {
    /*
     * OpenC has a bug for several releases using a timezone-sensitive time in clock_realtime().
     * gettimeofday() is more reliable.
     */
    struct timeval timenowval;
    int ret;
    assert(clock_id == CLOCK_REALTIME);
    if (!now) {
        errno = EINVAL;
        return -1;
    }
    ret = gettimeofday(&timenowval, NULL);
    if (ret < 0)
        return ret;
    now->tv_sec = timenowval.tv_sec;
    now->tv_nsec = timenowval.tv_usec * 1000L;
    return 0;
}

/**
 * Some of the dynamic IP interface scanning routines are unreliable, so
 * include a workaround to manually set the desired interface from outside.
 */
#include <framework/ip_ifc.h>

static ip_ifc_info* gSelectedIPInterface;

void set_ip_ifc(ip_ifc_info* info) {
    gSelectedIPInterface = info;
}
ip_ifc_info* get_ip_ifc(void) {
    return gSelectedIPInterface;
}

#else

#include <pwd.h>
#include <sys/utsname.h>
#if defined(__linux__)
#  include <asm/unistd.h>
#endif

#if defined(__FreeBSD__) || defined(__NetBSD__) || defined(__APPLE__)
int clock_gettime(clockid_t clock_id, struct timespec * tp) {
    struct timeval tv;

    assert(clock_id == CLOCK_REALTIME);
    if (!tp) {
        errno = EINVAL;
        return -1;
    }
    if (gettimeofday(&tv, NULL) < 0) {
        return -1;
    }
    tp->tv_sec  = tv.tv_sec;
    tp->tv_nsec = tv.tv_usec * 1000;
    return 0;
}
#endif

char * get_os_name(void) {
    static char str[256];
    struct utsname info;
    memset(&info, 0, sizeof(info));
    uname(&info);
    assert(strlen(info.sysname) + strlen(info.release) < sizeof(str));
    snprintf(str, sizeof(str), "%s %s", info.sysname, info.release);
    return str;
}

char * get_user_home(void) {
    static char buf[PATH_MAX];
    if (buf[0] == 0) {
        struct passwd * pwd = getpwuid(getuid());
        if (pwd == NULL) return NULL;
        strcpy(buf, pwd->pw_dir);
    }
    return buf;
}

int tkill(pid_t pid, int signal) {
#if defined(__linux__)
    return syscall(__NR_tkill, pid, signal);
#else
    return kill(pid, signal);
#endif
}

void ini_mdep(void) {
    signal(SIGPIPE, SIG_IGN);
    pthread_attr_init(&pthread_create_attr);
    pthread_attr_setstacksize(&pthread_create_attr, 0x8000);
}

#endif


/** canonicalize_file_name ****************************************************/

#if defined(WIN32)

char * canonicalize_file_name(const char * name) {
    DWORD len;
    int i = 0;
    wchar_t buf[FILE_PATH_SIZE];
    wchar_t * basename = NULL;
    wchar_t path[FILE_PATH_SIZE];
    char res[FILE_PATH_SIZE];

    assert(name != NULL);
    if (!MultiByteToWideChar(CP_UTF8, 0, name, -1, path, sizeof(path) / sizeof(wchar_t))) {
        set_win32_errno(GetLastError());
        return NULL;
    }
    len = GetFullPathNameW(path, sizeof(buf) / sizeof(wchar_t), buf, &basename);
    if (len == 0) {
        errno = ENOENT;
        return NULL;
    }
    if (len > FILE_PATH_SIZE - 1) {
        errno = ENAMETOOLONG;
        return NULL;
    }
    while (buf[i] != 0) {
        if (buf[i] == '\\') buf[i] = '/';
        i++;
    }
    len = WideCharToMultiByte(CP_UTF8, 0, buf, -1, res, sizeof(res), NULL, NULL);
    if (len == 0) {
        set_win32_errno(GetLastError());
        return NULL;
    }
    if (len > FILE_PATH_SIZE - 1) {
        errno = ENAMETOOLONG;
        return NULL;
    }
    return strdup(res);
}

#elif defined(_WRS_KERNEL)

char * canonicalize_file_name(const char * path) {
    char buf[PATH_MAX];
    int i = 0, j = 0;
    if (path[0] == '.' && (path[1] == '/' || path[1] == '\\' || path[1] == 0)) {
        getcwd(buf, sizeof(buf));
        j = strlen(buf);
        if (j == 1 && buf[0] == '/') j = 0;
        i = 1;
    }
    else if (path[0] == '.' && path[1] == '.' && (path[2] == '/' || path[2] == '\\' || path[2] == 0)) {
        getcwd(buf, sizeof(buf));
        j = strlen(buf);
        while (j > 0 && buf[j - 1] != '/') j--;
        if (j > 0 && buf[j - 1] == '/') j--;
        i = 2;
    }
    while (path[i] && j < PATH_MAX - 1) {
        char ch = path[i];
        if (ch == '\\') ch = '/';
        if (ch == '/') {
            if (path[i + 1] == '/' || path[i + 1] == '\\') {
                i++;
                continue;
            }
            if (path[i + 1] == '.') {
                if (path[i + 2] == 0) {
                    break;
                }
                if (path[i + 2] == '/' || path[i + 2] == '\\') {
                    i += 2;
                    continue;
                }
                if ((j == 0 || buf[0] == '/') && path[i + 2] == '.') {
                    if (path[i + 3] == '/' || path[i + 3] == '\\' || path[i + 3] == 0) {
                        while (j > 0 && buf[j - 1] != '/') j--;
                        if (j > 0 && buf[j - 1] == '/') j--;
                        i += 3;
                        continue;
                    }
                }
            }
        }
        buf[j++] = ch;
        i++;
    }
    if (j == 0 && path[0] != 0) buf[j++] = '/';
    buf[j] = 0;
    return strdup(buf);
}

#endif


/** getaddrinfo ***************************************************************/

#if defined(_WRS_KERNEL) && defined(USE_VXWORKS_GETADDRINFO)

/* TODO: VxWorks 6.6 getaddrinfo returns error when port is empty string, should return port 0 */
/* TODO: VxWorks 6.6 source (as shipped at 2007 fall release) does not include ipcom header files. */
extern void ipcom_freeaddrinfo();
extern int ipcom_getaddrinfo();

static struct ai_errlist {
    const char * str;
    int code;
} ai_errlist[] = {
    { "Success", 0 },
    /*
    { "Invalid value for ai_flags", IP_EAI_BADFLAGS },
    { "Non-recoverable failure in name resolution", IP_EAI_FAIL },
    { "ai_family not supported", IP_EAI_FAMILY },
    { "Memory allocation failure", IP_EAI_MEMORY },
    { "hostname nor servname provided, or not known", IP_EAI_NONAME },
    { "servname not supported for ai_socktype",     IP_EAI_SERVICE },
    { "ai_socktype not supported", IP_EAI_SOCKTYPE },
    { "System error returned in errno", IP_EAI_SYSTEM },
     */
    /* backward compatibility with userland code prior to 2553bis-02 */
    { "Address family for hostname not supported", 1 },
    { "No address associated with hostname", 7 },
    { NULL, -1 },
};

void loc_freeaddrinfo(struct addrinfo * ai) {
    ipcom_freeaddrinfo(ai);
}

int loc_getaddrinfo(const char * nodename, const char * servname,
       const struct addrinfo * hints, struct addrinfo ** res) {
    return ipcom_getaddrinfo(nodename, servname, hints, res);
}

const char * loc_gai_strerror(int ecode) {
    struct ai_errlist * p;
    static char buf[32];
    for (p = ai_errlist; p->str; p++) {
        if (p->code == ecode) return p->str;
    }
    snprintf(buf, sizeof(buf), "Error code %d", ecode);
    return buf;
}

#elif defined(_WRS_KERNEL)

union sockaddr_union {
    struct sockaddr sa;
    struct sockaddr_in sin;
    struct sockaddr_in6 sin6;
};

extern int ipcom_getsockaddrbyaddr();

void loc_freeaddrinfo(struct addrinfo * ai) {
    while (ai != NULL) {
        struct addrinfo * next = ai->ai_next;
        if (ai->ai_canonname != NULL) loc_free(ai->ai_canonname);
        if (ai->ai_addr != NULL) loc_free(ai->ai_addr);
        loc_free(ai);
        ai = next;
    }
}

int loc_getaddrinfo(const char * nodename, const char * servname,
       const struct addrinfo * hints, struct addrinfo ** res) {
    int family = 0;
    int flags = 0;
    int socktype = 0;
    int protocol = 0;
    int err = 0;
    int port = 0;
    char * canonname = NULL;
    const char * host = NULL;
    struct addrinfo * ai = NULL;
    union sockaddr_union * sa = NULL;

    *res = NULL;

    if (hints != NULL) {
        flags = hints->ai_flags;
        family = hints->ai_family;
        socktype = hints->ai_socktype;
        protocol = hints->ai_protocol;
    }
    if (family == AF_UNSPEC) {
        struct addrinfo lhints;
        int err_v6;

        if (hints == NULL) memset(&lhints, 0, sizeof(lhints));
        else memcpy(&lhints, hints, sizeof(lhints));
        lhints.ai_family = AF_INET6;
        err_v6 = loc_getaddrinfo(nodename, servname, &lhints, res);
        lhints.ai_family = AF_INET;
        while (*res != NULL) res = &(*res)->ai_next;
        err = loc_getaddrinfo(nodename, servname, &lhints, res);
        return err && err_v6 ? err : 0;
    }
    if (servname != NULL && servname[0] != 0) {
        char * p = NULL;
        port = strtol(servname, &p, 10);
        if (port < 0 || port > 0xffff || *p != '\0' || p == servname) {
            return 1;
        }
    }
    if (nodename != NULL && nodename[0] != 0) {
        host = nodename;
    }
    else if (flags & AI_PASSIVE) {
        host = family == AF_INET ? "0.0.0.0" : "::";
    }
    else {
        host = family == AF_INET ? "127.0.0.1" : "::1";
    }
    if (socktype == 0) {
        socktype = SOCK_STREAM;
    }
    if (protocol == 0) {
        protocol = socktype == SOCK_STREAM ? IPPROTO_TCP : IPPROTO_UDP;
    }

    sa = loc_alloc_zero(sizeof(*sa));
    err = ipcom_getsockaddrbyaddr(family, host, (struct sockaddr *)sa);
    if (err) {
        loc_free(sa);
        return err;
    }

    ai = loc_alloc_zero(sizeof(*ai));
    switch (family) {
    case AF_INET:
        assert(sa->sin.sin_family == AF_INET);
        sa->sin.sin_port = htons(port);
        ai->ai_addrlen = sizeof(struct sockaddr_in);
        break;
    case AF_INET6:
        assert(sa->sin6.sin6_family == AF_INET6);
        sa->sin6.sin6_port = htons(port);
        ai->ai_addrlen = sizeof(struct sockaddr_in6);
        break;
    default:
        loc_free(sa);
        loc_free(ai);
        return 2;
    }

    ai->ai_flags = 0;
    ai->ai_family = family;
    ai->ai_socktype = socktype;
    ai->ai_protocol = protocol;
    ai->ai_canonname = canonname;
    ai->ai_addr = (struct sockaddr *)sa;
    ai->ai_next = NULL;
    *res = ai;
    return 0;
}

const char * loc_gai_strerror(int ecode) {
    static char buf[32];
    if (ecode == 0) return "Success";
    snprintf(buf, sizeof(buf), "Error code %d", ecode);
    return buf;
}

#elif defined(WIN32)

const char * loc_gai_strerror(int ecode) {
    WCHAR * buf = NULL;
    static char msg[512];
    if (ecode == 0) return "Success";
    if (!FormatMessageW(
        FORMAT_MESSAGE_ALLOCATE_BUFFER |
        FORMAT_MESSAGE_FROM_SYSTEM |
        FORMAT_MESSAGE_IGNORE_INSERTS |
        FORMAT_MESSAGE_MAX_WIDTH_MASK,
        NULL,
        ecode,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
        (LPWSTR)&buf, 0, NULL) ||
        !WideCharToMultiByte(CP_UTF8, 0, buf, -1, msg, sizeof(msg), NULL, NULL))
    {
        snprintf(msg, sizeof(msg), "GAI Error Code %d", ecode);
    }
    if (buf != NULL) LocalFree(buf);
    return msg;
}

#elif defined(__SYMBIAN32__)

const char * loc_gai_strerror(int ecode) {
    static char buf[32];
    if (ecode == 0) return "Success";
    snprintf(buf, sizeof(buf), "Error code %d", ecode);
    return buf;
}

#endif

#if defined(WIN32) || defined(_WRS_KERNEL) || defined (__SYMBIAN32__)

int is_daemon(void) {
    return 0;
}

void become_daemon(void) {
    fprintf(stderr, "tcf-agent: Running in the background is not supported on %s\n", get_os_name());
    exit(1);
}

#else

#include <syslog.h>

static int running_as_daemon = 0;

int is_daemon(void) {
    return running_as_daemon;
}

void become_daemon(void) {
    assert(!running_as_daemon);
    openlog("tcf-agent", LOG_PID, LOG_DAEMON);
    if (daemon(0, 0) < 0) {
        syslog(LOG_MAKEPRI(LOG_DAEMON, LOG_ERR), "Cannot become a daemon: %m");
        exit(1);
    }
    running_as_daemon = 1;
}
#endif

#if !defined(__FreeBSD__) && !defined(__NetBSD__) && !defined(__APPLE__) && !defined(__VXWORKS__)

size_t strlcpy(char * dst, const char * src, size_t size) {
    char ch;
    const char * src0 = src;
    const char * dst1 = dst + size - 1;

    while ((ch = *src) != 0) {
        if (dst < dst1) *dst++ = ch;
        src++;
    }
    if (dst <= dst1) *dst = 0;
    return src - src0;
}

size_t strlcat(char * dst, const char * src, size_t size) {
    char ch;
    const char * dst0 = dst;
    const char * src0 = src;
    const char * dst1 = dst + size - 1;

    while (dst <= dst1 && *dst != 0) dst++;

    while ((ch = *src) != 0) {
        if (dst < dst1) *dst++ = ch;
        src++;
    }
    if (dst <= dst1) *dst = 0;
    return (dst - dst0) + (src - src0);
}

#endif
