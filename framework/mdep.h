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
 * Machine and OS dependend definitions.
 * This module implements host OS abstraction layer that helps make
 * agent code portable between Linux, Windows, VxWorks and potentially other OSes.
 *
 * mdep.h must be included first, before any other header files.
 */

#ifndef D_mdep
#define D_mdep

#define __STDC_FORMAT_MACROS 1

#if defined(WIN32) || defined(__CYGWIN__)
/* MS Windows NT/XP */

#define _WIN32_WINNT 0x0501

#if defined(__CYGWIN__)
#  define _WIN32_IE 0x0501
#elif defined(__MINGW32__)
#  define _WIN32_IE 0x0501
#elif defined(_MSC_VER)
#  pragma warning(disable:4054) /* 'type cast' : from function pointer '...' to data pointer 'void *' */
#  pragma warning(disable:4055) /* 'type cast' : from data pointer 'void *' to function pointer '...' */
#  pragma warning(disable:4127) /* conditional expression is constant */
#  pragma warning(disable:4152) /* nonstandard extension, function/data pointer conversion in expression */
#  pragma warning(disable:4100) /* unreferenced formal parameter */
#  pragma warning(disable:4996) /* 'strcpy': This function or variable may be unsafe */
#  ifdef UNICODE
/* TCF code uses UTF-8 multibyte character encoding */
#    undef UNICODE
#  endif
#  ifdef _DEBUG
#    define _CRTDBG_MAP_ALLOC
#    include <stdlib.h>
#    include <crtdbg.h>
#  endif
#  define _WSPIAPI_H_
#endif

#include <winsock2.h>
#include <ws2tcpip.h>
#include <iphlpapi.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/utime.h>
#include <stdio.h>
#include <io.h>

#if defined(_MSC_VER)
   typedef signed __int8 int8_t;
   typedef unsigned __int8 uint8_t;
   typedef signed __int16 int16_t;
   typedef unsigned __int16 uint16_t;
   typedef signed __int32 int32_t;
   typedef unsigned __int32 uint32_t;
   typedef signed __int64 int64_t;
   typedef unsigned __int64 uint64_t;
   typedef int ssize_t;
#  define PRId64 "I64d"
#  define PRIX64 "I64X"
#  define SCNx64 "I64x"
#else
#  include <inttypes.h>
#endif

#define FILE_PATH_SIZE MAX_PATH

typedef int socklen_t;

#if defined(__CYGWIN__)

#ifndef _LARGEFILE_SOURCE
#error "Need CC command line option: -D_LARGEFILE_SOURCE"
#endif

#ifndef _GNU_SOURCE
#error "Need CC command line option: -D_GNU_SOURCE"
#endif

#include <sys/unistd.h>

extern void __stdcall freeaddrinfo(struct addrinfo *);
extern int __stdcall getaddrinfo(const char *, const char *,
                const struct addrinfo *, struct addrinfo **);

#else /* not __CYGWIN__ */

#include <direct.h>

struct timespec {
    time_t  tv_sec;         /* seconds */
    long    tv_nsec;        /* nanoseconds */
};

#define SIGKILL 1

#define ETIMEDOUT 100

#if defined(__MINGW32__)
typedef unsigned int useconds_t;
#elif defined(_MSC_VER)
#define __i386__
#define strcasecmp(x,y) stricmp(x,y)
typedef unsigned long pid_t;
typedef unsigned long useconds_t;
#endif

#define CLOCK_REALTIME 1
typedef int clockid_t;
extern int clock_gettime(clockid_t clock_id, struct timespec * tp);
extern void usleep(useconds_t useconds);

#define off_t __int64
#define lseek _lseeki64
extern int truncate(const char * path, off_t size);
extern int ftruncate(int f, off_t size);

#if defined(_MSC_VER)
#define utimbuf _utimbuf
#define utime   _utime
#define futime  _futime
#define snprintf _snprintf
#endif

extern int getuid(void);
extern int geteuid(void);
extern int getgid(void);
extern int getegid(void);

extern ssize_t pread(int fd, const void * buf, size_t size, off_t offset);
extern ssize_t pwrite(int fd, const void * buf, size_t size, off_t offset);

/* UTF-8 support */
struct utf8_stat {
    dev_t      st_dev;
    ino_t      st_ino;
    unsigned short st_mode;
    short      st_nlink;
    short      st_uid;
    short      st_gid;
    dev_t      st_rdev;
    int64_t    st_size;
    int64_t    st_atime;
    int64_t    st_mtime;
    int64_t    st_ctime;
};
#define stat   utf8_stat
#define lstat  utf8_stat
#define fstat  utf8_fstat
#define open   utf8_open
#define chmod  utf8_chmod
#define remove utf8_remove
#define rmdir  utf8_rmdir
#define mkdir  utf8_mkdir
#define rename utf8_rename
extern int utf8_stat(const char * name, struct utf8_stat * buf);
extern int utf8_fstat(int fd, struct utf8_stat * buf);
extern int utf8_open(const char * name, int flags, int perms);
extern int utf8_chmod(const char * name, int mode);
extern int utf8_remove(const char * path);
extern int utf8_rmdir(const char * path);
extern int utf8_mkdir(const char * path, int mode);
extern int utf8_rename(const char * path1, const char * path2);

/*
 * readdir() emulation with UTF-8 support
 */
struct UTF8_DIR {
  long hdl;
  struct _wfinddatai64_t blk;
  char path[FILE_PATH_SIZE];
};

struct utf8_dirent {
  char d_name[FILE_PATH_SIZE];
  int64_t d_size;
  time_t d_atime;
  time_t d_ctime;
  time_t d_wtime;
};

typedef struct UTF8_DIR UTF8_DIR;

#define DIR UTF8_DIR
#define dirent   utf8_dirent
#define opendir  utf8_opendir
#define closedir utf8_closedir
#define readdir  utf8_readdir

extern DIR * utf8_opendir(const char * path);
extern int utf8_closedir(DIR * dir);
extern struct utf8_dirent * readdir(DIR * dir);

#endif /* __CYGWIN__ */

extern const char * loc_gai_strerror(int ecode);

#define MSG_MORE 0

extern const char * inet_ntop(int af, const void * src, char * dst, socklen_t size);
extern int inet_pton(int af, const char * src, void * dst);

/*
 * PThreads emulation.
 */
#if defined(__CYGWIN__)
#  include <cygwin/types.h>
#else
typedef void * pthread_t;
typedef void * pthread_attr_t;
typedef void * pthread_mutex_t;
typedef void * pthread_cond_t;
typedef void * pthread_mutexattr_t;
typedef void * pthread_condattr_t;
#endif

extern int pthread_attr_init(pthread_attr_t * attr);
extern int pthread_mutex_init(pthread_mutex_t * mutex, const pthread_mutexattr_t * attr);
extern int pthread_cond_init(pthread_cond_t * cond, const pthread_condattr_t * attr);
extern int pthread_cond_destroy(pthread_cond_t * cond);

extern int pthread_cond_signal(pthread_cond_t * cond);
extern int pthread_cond_broadcast(pthread_cond_t * cond);
extern int pthread_cond_wait(pthread_cond_t * cond, pthread_mutex_t * mutex);
extern int pthread_cond_timedwait(pthread_cond_t * cond, pthread_mutex_t * mutex,
                                  const struct timespec * abstime);
extern int pthread_mutex_lock(pthread_mutex_t * mutex);
extern int pthread_mutex_unlock(pthread_mutex_t * mutex);
extern pthread_t pthread_self(void);
extern int pthread_create(pthread_t * thread, const pthread_attr_t * attr,
                          void * (*start_routine)(void *), void * arg);
extern int pthread_join(pthread_t thread, void **value_ptr);
extern int pthread_equal(pthread_t thread1, pthread_t thread2);

/*
 * Windows socket functions don't set errno as expected.
 * Wrappers are provided to workaround the problem.
 * TODO: more socket function wrappers are needed for better error reports on Windows
 */
#define socket(af, type, protocol) wsa_socket(af, type, protocol)
#define connect(socket, addr, addr_size) wsa_connect(socket, addr, addr_size)
#define bind(socket, addr, addr_size) wsa_bind(socket, addr, addr_size)
#define listen(socket, size) wsa_listen(socket, size)
#define recv(socket, buf, size, flags) wsa_recv(socket, buf, size, flags)
#define recvfrom(socket, buf, size, flags, addr, addr_size) wsa_recvfrom(socket, buf, size, flags, addr, addr_size)
#define send(socket, buf, size, flags) wsa_send(socket, buf, size, flags)
#define sendto(socket, buf, size, flags, dest_addr, dest_size) wsa_sendto(socket, buf, size, flags, dest_addr, dest_size)
#define setsockopt(socket, level, opt, value, size) wsa_setsockopt(socket, level, opt, value, size)
#define getsockname(socket, name, size) wsa_getsockname(socket, name, size)

extern int wsa_socket(int af, int type, int protocol);
extern int wsa_connect(int socket, const struct sockaddr * addr, int addr_size);
extern int wsa_bind(int socket, const struct sockaddr * addr, int addr_size);
extern int wsa_listen(int socket, int size);
extern int wsa_recv(int socket, void * buf, size_t size, int flags);
extern int wsa_recvfrom(int socket, void * buf, size_t size, int flags,
                    struct sockaddr * addr, socklen_t * addr_size);
extern int wsa_send(int socket, const void * buf, size_t size, int flags);
extern int wsa_sendto(int socket, const void * buf, size_t size, int flags,
                  const struct sockaddr * dest_addr, socklen_t dest_size);
extern int wsa_setsockopt(int socket, int level, int opt, const char * value, int size);
extern int wsa_getsockname(int socket, struct sockaddr * name, int * size);

#ifndef SHUT_WR
#define SHUT_WR SD_SEND
#endif

extern char * canonicalize_file_name(const char * path);

#define O_LARGEFILE 0

#define loc_freeaddrinfo freeaddrinfo
#define loc_getaddrinfo getaddrinfo

#elif defined(_WRS_KERNEL)
/* VxWork kernel module */

#if !defined(INET)
#  define INET
#endif

#include <vxWorks.h>
#include <inetLib.h>
#include <pthread.h>
#include <strings.h>
#include <sys/ioctl.h>
#include <netinet/tcp.h>
#include <net/if.h>
#include <selectLib.h>
#include <wrn/coreip/sockLib.h>
#include <wrn/coreip/hostLib.h>
#if _WRS_VXWORKS_MAJOR > 6 || _WRS_VXWORKS_MAJOR == 6 && _WRS_VXWORKS_MINOR >= 7
#  include <private/taskLibP.h>
#endif

#define environ taskIdCurrent->ppEnviron

#define closesocket close

#if _WRS_VXWORKS_MAJOR < 6 || _WRS_VXWORKS_MAJOR == 6 && _WRS_VXWORKS_MINOR < 9
typedef unsigned long uintptr_t;
#define send(s, buf, len, flags) (send)(s, (char *)(buf), len, flags)
#endif

typedef unsigned long useconds_t;

#define FILE_PATH_SIZE PATH_MAX
#define O_BINARY 0
#define O_LARGEFILE 0
#define lstat stat
#define ifr_netmask ifr_addr
#define SA_LEN(addr) ((addr)->sa_len)
#define MSG_MORE 0

extern int truncate(char * path, int64_t size);
extern char * canonicalize_file_name(const char * path);
extern ssize_t pread(int fd, const void * buf, size_t size, off_t offset);
extern ssize_t pwrite(int fd, const void * buf, size_t size, off_t offset);

extern void usleep(useconds_t useconds);

extern int getuid(void);
extern int geteuid(void);
extern int getgid(void);
extern int getegid(void);

extern void loc_freeaddrinfo(struct addrinfo * ai);
extern int loc_getaddrinfo(const char * nodename, const char * servname,
       const struct addrinfo * hints, struct addrinfo ** res);
extern const char * loc_gai_strerror(int ecode);

#elif defined __SYMBIAN32__
/* Symbian / OpenC */

#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <socket.h>
#include <in.h>
#include <netdb.h>
#include <errno.h>
#include <utime.h>
#include <memory.h>
#include <string.h>
#include <limits.h>
#include <inttypes.h>
#include <fcntl.h>
#include <utime.h>
#include <inet.h>
#include <pthreadtypes.h>
#include <pthread.h>
#include <timespec.h>
#include <e32def.h>
#include <sys/sockio.h>
#include <net/if.h>
#include <unistd.h>

#include <framework/link.h>

#define MAX_PATH _POSIX_PATH_MAX
#define FILE_PATH_SIZE _POSIX_PATH_MAX

#define closesocket close
#define SIGKILL 1

#define ETIMEDOUT 60

extern const char * loc_gai_strerror(int ecode);
extern int truncate(const char * path, int64_t size);

extern ssize_t pread(int fd, const void * buf, size_t size, off_t offset);
extern ssize_t pwrite(int fd, const void * buf, size_t size, off_t offset);

#define loc_freeaddrinfo freeaddrinfo
#define loc_getaddrinfo getaddrinfo

extern int loc_clock_gettime(int, struct timespec *);
#define clock_gettime loc_clock_gettime /* override Open C impl */

struct ip_ifc_info;
extern void set_ip_ifc(struct ip_ifc_info * info);
extern struct ip_ifc_info * get_ip_ifc(void);

#else
/* Linux, BSD, MacOS, UNIX */

#ifndef _LARGEFILE_SOURCE
#error "Need CC command line option: -D_LARGEFILE_SOURCE"
#endif

#ifndef _GNU_SOURCE
#error "Need CC command line option: -D_GNU_SOURCE"
#endif

#include <unistd.h>
#include <memory.h>
#include <pthread.h>
#include <netdb.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/ioctl.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <net/if.h>
#include <limits.h>
#include <inttypes.h>

#define loc_freeaddrinfo freeaddrinfo
#define loc_getaddrinfo getaddrinfo
#define loc_gai_strerror gai_strerror

#define O_BINARY 0

#if defined(__FreeBSD__) || defined(__NetBSD__) || defined(__APPLE__)

#  define O_LARGEFILE 0
#  define canonicalize_file_name(path) realpath(path, NULL)
#  define SA_LEN(addr) ((addr)->sa_len)
extern char ** environ;

#else /* not BSD */

#  define SA_LEN(addr) (sizeof(struct sockaddr))

#endif /* BSD */

#if defined(__APPLE__)
#  define CLOCK_REALTIME 1
  typedef int clockid_t;
  extern int clock_gettime(clockid_t clock_id, struct timespec * tp);
#endif

extern int tkill(pid_t pid, int signal);

#define FILE_PATH_SIZE PATH_MAX

#define closesocket close

#endif

#ifndef PRId64
#  define PRId64 "lld"
#endif
#ifndef PRIX64
#  define PRIX64 "llX"
#endif
#ifndef SCNx64
#  define SCNx64 "llx"
#endif

#if !defined(__FreeBSD__) && !defined(__NetBSD__) && !defined(__APPLE__) && !defined(__VXWORKS__)
extern size_t strlcpy(char * dst, const char * src, size_t size);
extern size_t strlcat(char * dst, const char * src, size_t size);
#endif

extern pthread_attr_t pthread_create_attr;

/* Return Operating System name */
extern char * get_os_name(void);

/* Get user home directory path */
extern char * get_user_home(void);

/* Switch to running in the background, rather than under the direct control of a user */
extern void become_daemon(void);

/* Return 1 if running in the background, return 0 othewise */
extern int is_daemon(void);

/* Initialize mdep module */
extern void ini_mdep(void);

#endif
