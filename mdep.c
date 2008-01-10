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
 * Machine and OS dependend definitions.
 * This module implements host OS abstraction layer that helps make
 * agent code portable between Linux, Windows, VxWorks and potentially other OSes.
 */

#include <stdio.h>
#include <assert.h>
#include <errno.h>
#include "mdep.h"

pthread_attr_t pthread_create_attr;

#ifdef WIN32

#include <fcntl.h>
#include <shlobj.h>

#define ERR_SOCKET (-1)
#define ERR_WIN32  (-2)

/*********************************************************************
    Support of pthreads on Windows is implemented according to
    reccomendations from the paper:
    
    Strategies for Implementing POSIX Condition Variables on Win32
    C++ Report, SIGS, Vol. 10, No. 5, June, 1998

    Douglas C. Schmidt and Irfan Pyarali
    Department of Computer Science
    Washington University, St. Louis, Missouri
**********************************************************************/

void pthread_mutex_init(pthread_mutex_t * mutex, void * attr) {
    assert(attr == NULL);
    *mutex = CreateMutex(NULL, FALSE, NULL);
    if (*mutex == NULL) {
        fprintf(stderr, "Can't create mutex: error %d\n", GetLastError());
        exit(1);
    }
}

void pthread_mutex_lock(pthread_mutex_t * mutex) {
    assert(mutex != NULL);
    assert(*mutex != NULL);
    WaitForSingleObject(*mutex, INFINITE);
}

void pthread_mutex_unlock(pthread_mutex_t * mutex) {
    assert(mutex != NULL);
    assert(*mutex != NULL);
    ReleaseMutex(*mutex);
}

void pthread_cond_init(pthread_cond_t * cond, void * attr) {
    assert(attr == NULL);
    cond->waiters_count = 0;
    cond->was_broadcast = 0;
    cond->sema = CreateSemaphore(NULL, 0, 0x7fffffff, NULL);
    InitializeCriticalSection(&cond->waiters_count_lock);
    cond->waiters_done = CreateEvent(NULL, FALSE, FALSE, NULL);
}

int pthread_cond_wait(pthread_cond_t * cond, pthread_mutex_t * mutex) {
    DWORD res = 0;
    int last_waiter = 0;

    EnterCriticalSection(&cond->waiters_count_lock);
    cond->waiters_count++;
    LeaveCriticalSection(&cond->waiters_count_lock);

    // This call atomically releases the mutex and waits on the
    // semaphore until <pthread_cond_signal> or <pthread_cond_broadcast>
    // are called by another thread.
    res = SignalObjectAndWait(*mutex, cond->sema, INFINITE, FALSE);

    // Reacquire lock to avoid race conditions.
    EnterCriticalSection(&cond->waiters_count_lock);

    // We're no longer waiting...
    cond->waiters_count--;

    // Check to see if we're the last waiter after <pthread_cond_broadcast>.
    last_waiter = cond->was_broadcast && cond->waiters_count == 0;

    LeaveCriticalSection(&cond->waiters_count_lock);

    // If we're the last waiter thread during this particular broadcast
    // then let all the other threads proceed.
    if (last_waiter) {
        // This call atomically signals the <waiters_done_> event and waits until
        // it can acquire the <mutex>.  This is required to ensure fairness. 
        SignalObjectAndWait(cond->waiters_done, *mutex, INFINITE, FALSE);
    }
    else {
        // Always regain the external mutex since that's the guarantee we
        // give to our callers. 
        WaitForSingleObject(*mutex, INFINITE);
    }
    assert(res == WAIT_OBJECT_0);
    return 0;
}

int pthread_cond_timedwait(pthread_cond_t * cond, pthread_mutex_t * mutex, struct timespec * timeout) {
    DWORD res = 0;
    int last_waiter = 0;

    EnterCriticalSection(&cond->waiters_count_lock);
    cond->waiters_count++;
    LeaveCriticalSection(&cond->waiters_count_lock);

    // This call atomically releases the mutex and waits on the
    // semaphore until <pthread_cond_signal> or <pthread_cond_broadcast>
    // are called by another thread.
    res = SignalObjectAndWait(*mutex, cond->sema, timeout->tv_sec * 1000 + timeout->tv_nsec / 1000000, FALSE);

    // Reacquire lock to avoid race conditions.
    EnterCriticalSection(&cond->waiters_count_lock);

    // We're no longer waiting...
    cond->waiters_count--;

    // Check to see if we're the last waiter after <pthread_cond_broadcast>.
    last_waiter = cond->was_broadcast && cond->waiters_count == 0;

    LeaveCriticalSection(&cond->waiters_count_lock);

    // If we're the last waiter thread during this particular broadcast
    // then let all the other threads proceed.
    if (last_waiter) {
        // This call atomically signals the <waiters_done> event and waits until
        // it can acquire the <mutex>.  This is required to ensure fairness. 
        SignalObjectAndWait(cond->waiters_done, *mutex, INFINITE, FALSE);
    }
    else {
        // Always regain the external mutex since that's the guarantee we
        // give to our callers. 
        WaitForSingleObject(*mutex, INFINITE);
    }

    if (res == WAIT_TIMEOUT) return errno = ETIMEDOUT;
    assert(res == WAIT_OBJECT_0);
    return 0;
}

void pthread_cond_signal(pthread_cond_t *cond) {
    int have_waiters = 0;
    
    EnterCriticalSection(&cond->waiters_count_lock);
    have_waiters = cond->waiters_count > 0;
    LeaveCriticalSection(&cond->waiters_count_lock);

    // If there aren't any waiters, then this is a no-op.  
    if (have_waiters) ReleaseSemaphore(cond->sema, 1, 0);
}

void pthread_cond_broadcast(pthread_cond_t *cond) {
    int have_waiters = 0;

    // This is needed to ensure that <waiters_count_> and <was_broadcast_> are
    // consistent relative to each other.
    EnterCriticalSection(&cond->waiters_count_lock);

    if (cond->waiters_count > 0) {
        // We are broadcasting, even if there is just one waiter...
        // Record that we are broadcasting, which helps optimize
        // <pthread_cond_wait> for the non-broadcast case.
        cond->was_broadcast = 1;
        have_waiters = 1;
    }

    if (have_waiters) {
        // Wake up all the waiters atomically.
        ReleaseSemaphore(cond->sema, cond->waiters_count, 0);

        LeaveCriticalSection(&cond->waiters_count_lock);

        // Wait for all the awakened threads to acquire the counting
        // semaphore. 
        WaitForSingleObject(cond->waiters_done, INFINITE);
        // This assignment is okay, even without the <waiters_count_lock_> held 
        // because no other waiter threads can wake up to access it.
        cond->was_broadcast = 0;
    }
    else {
        LeaveCriticalSection(&cond->waiters_count_lock);
    }
}

typedef struct ThreadArgs ThreadArgs;

struct ThreadArgs {
    void * (*start)(void *);
    void * args;
};

static void start_thread(void * x) {
    ThreadArgs a = *(ThreadArgs *)x;

    free(x);
    ExitThread((DWORD)a.start(a.args));
}

int pthread_create(pthread_t * thread, pthread_attr_t * attr,
                   void * (*start)(void *), void * args) {
    unsigned long r;
    ThreadArgs * a;

    a = (ThreadArgs *)malloc(sizeof(ThreadArgs));
    a->start = start;
    a->args = args;
    r = _beginthread(start_thread, 0, a);
    if (r == (unsigned long)-1) {
        int error = errno;
        free(a);
        errno = error;
        return error;
    }
    *thread = (HANDLE)r;
    return 0;
}

int pthread_join(pthread_t thread, void **value_ptr) {
    if (WaitForSingleObject(thread, INFINITE) == WAIT_FAILED) {
        return EINVAL;
    }
    if (!GetExitCodeThread(thread, (LPDWORD)value_ptr)) {
        return EINVAL;
    }
    CloseHandle(thread);
    return 0;
}

pthread_t pthread_self(void) {
    return GetCurrentThread();
}

static __int64 file_time_to_unix_time (const FILETIME * ft) {
    __int64 res = (__int64)ft->dwHighDateTime << 32;

    res |= ft->dwLowDateTime;
    res /= 10;                  /* from 100 nano-sec periods to usec */
    res -= 11644473600000000u;  /* from Win epoch to Unix epoch */
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

void perror(const char * msg) {
    int error = errno;
    if (error == ERR_SOCKET) {
        fprintf(stderr, "%s: socket error 0x%08x\n", msg, WSAGetLastError());
    }
    else if (error == ERR_WIN32) {
        fprintf(stderr, "%s: Win32 error 0x%08x\n", msg, GetLastError());
    }
    else {
        fprintf(stderr, "%s: %s\n", msg, strerror(error));
    }
}

#undef bind
int wsa_bind(int socket, const struct sockaddr * addr, int addr_size) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = bind(socket, addr, addr_size);
    if (res != 0) {
        errno = ERR_SOCKET;
        return -1;
    }
    return 0;
}

#undef socket
int wsa_socket(int af, int type, int protocol) {
    int res = 0;
    SetLastError(0);
    WSASetLastError(0);
    res = socket(af, type, protocol);
    if (res < 0) {
        errno = ERR_SOCKET;
        return -1;
    }
    return res;
}

int truncate(const char * path, int64 size) {
    int res = 0;
    int f = _open(path, _O_RDWR | _O_BINARY);
    if (f < 0) return -1;
    res = ftruncate(f, size);
    _close(f);
    return res;
}

int ftruncate(int fd, int64 size) {
    int64 cur, pos;
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

DIR * opendir(const char *path) {
    DIR * d = (DIR *)malloc(sizeof(DIR));
    if (!d) { errno = ENOMEM; return 0; }
    strcpy(d->path, path);
    strcat(d->path, "/*.*");
    d->hdl = -1;
    return d;
}

struct dirent * readdir(DIR *d) {
    static struct dirent de;
    if (d->hdl < 0) {
        d->hdl = _findfirsti64(d->path, &d->blk);
        if (d->hdl < 0) {
            if (errno == ENOENT) errno = 0;
            return 0;
        }
    }
    else {
        int r = _findnexti64(d->hdl, &d->blk);
        if (r < 0) {
            if (errno == ENOENT) errno = 0;
            return 0;
        }
    }
    strcpy(de.d_name, d->blk.name);
    de.d_size = d->blk.size;
    de.d_atime = d->blk.time_access;
    de.d_ctime = d->blk.time_create;
    de.d_wtime = d->blk.time_write;
    return &de;
}

int closedir(DIR * d) {
    int r = 0;
    if (!d) {
        errno = EBADF;
        return -1;
    }
    if (d->hdl >= 0) r = _findclose(d->hdl);
    free(d);
    return r;
}

char * canonicalize_file_name(const char * path) {
    char buf[MAX_PATH];
    char * basename;
    int i = 0;
    DWORD len = GetFullPathName(path, sizeof(buf), buf, &basename);
    if (len == 0) {
        errno = ENOENT;
        return NULL;
    }
    if (len > MAX_PATH - 1) {
        errno = ENAMETOOLONG;
        return NULL;
    }
    while (buf[i] != 0) {
        if (buf[i] == '\\') buf[i] = '/';
        i++;
    }
    return strdup(buf);
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
    snprintf(str, sizeof(str), "Windows %d.%d", info.dwMajorVersion, info.dwMajorVersion);
    return str;
}

char * get_user_home(void) {
    static char buf[MAX_PATH];
    if (buf[0] != 0) return buf;
    if (SUCCEEDED(SHGetFolderPath(NULL, CSIDL_PERSONAL, NULL, SHGFP_TYPE_CURRENT, buf))) return buf;
    return NULL;
}

void ini_mdep(void) {
    WORD wVersionRequested;
    WSADATA wsaData;
    int err;
    wVersionRequested = MAKEWORD( 1, 1 );
    err = WSAStartup( wVersionRequested, &wsaData );
    if ( err != 0 ) {
        fprintf(stderr, "Couldn't access winsock.dll.\n");
        exit(1);
    }
    /* Confirm that the Windows Sockets DLL supports 1.1.*/
    /* Note that if the DLL supports versions greater */
    /* than 1.1 in addition to 1.1, it will still return */
    /* 1.1 in wVersion since that is the version we */
    /* requested.     */
    if (LOBYTE( wsaData.wVersion ) != 1 || HIBYTE( wsaData.wVersion ) != 1) {
        fprintf(stderr, "Unacceptable version of winsock.dll.\n");
        WSACleanup();
        exit(1);
    }
}

#elif defined(_WRS_KERNEL)

void usleep(useconds_t useconds) {
    struct timespec tv;
    tv.tv_sec = useconds / 1000000;
    tv.tv_nsec = (useconds % 1000000) * 1000;
    nanosleep(&tv, NULL);
}

int truncate(char * path, int64 size) {
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
    snprintf(str, sizeof(str), "VxWorks %s", kernelVersion());
    return str;
}

char * get_user_home(void) {
    return "/";
}

void ini_mdep(void) {
    pthread_attr_init(&pthread_create_attr);
    pthread_attr_setstacksize(&pthread_create_attr, 0x4000);
    pthread_attr_setname(&pthread_create_attr, "tTcf");
}

#else

#include <pwd.h>
#include <sys/utsname.h>
#include <asm/unistd.h>

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
    return syscall(__NR_tkill, pid, signal);
}

void ini_mdep(void) {
    pthread_attr_init(&pthread_create_attr);
    pthread_attr_setstacksize(&pthread_create_attr, 0x8000);
}

#endif

