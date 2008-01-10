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

#ifndef D_mdep
#define D_mdep

#ifdef WIN32
/* MS Windows NT/XP */

#define _WIN32_WINNT 0x0400
#pragma warning(disable:4615)

#include <windows.h>
#include <winsock.h>
#include <memory.h>
#include <process.h>
#include <IPHlpApi.h>
#include <time.h>
#include <io.h>

#define FILE_PATH_SIZE MAX_PATH

typedef int socklen_t;
#ifdef __GNUC__
#define _WIN32_IE 0x0500
#else
#define __i386__
typedef unsigned long pid_t;
#endif
typedef unsigned long useconds_t;

typedef struct {
    unsigned long ebx, ecx, edx, esi, edi, ebp, eax;
    unsigned short ds, __ds, es, __es;
    unsigned short fs, __fs, gs, __gs;
    unsigned long orig_eax, eip;
    unsigned short cs, __cs;
    long eflags, esp;
    unsigned short ss, __ss;
} REG_SET;

#define get_regs_PC(x) x.eip
#define set_regs_PC(x,y) x.eip = (unsigned long)(y)

struct timespec {
    time_t  tv_sec;         /* seconds */
    long    tv_nsec;        /* nanoseconds */
};

#define SIGTRAP 5
#define SIGKILL 9
#define SIGSTOP 19

#define ETIMEDOUT 100

#define vsnprintf _vsnprintf

#define CLOCK_REALTIME 1
typedef int clockid_t;
extern int clock_gettime(clockid_t clock_id, struct timespec * tp); 

extern void usleep(useconds_t useconds);

/*
 * PThreads emulation.
 */
typedef HANDLE pthread_t;
typedef HANDLE pthread_mutex_t;
typedef int pthread_attr_t;
typedef struct {
    int waiters_count;
    CRITICAL_SECTION waiters_count_lock;
    HANDLE sema;
    HANDLE waiters_done;
    size_t was_broadcast;
} pthread_cond_t;

extern void pthread_mutex_init(pthread_mutex_t * mutex, void * attr);
extern void pthread_cond_init(pthread_cond_t * cond, void * attr);

extern void pthread_cond_signal(pthread_cond_t * cond);
extern void pthread_cond_broadcast(pthread_cond_t *cond);
extern int pthread_cond_wait(pthread_cond_t * cond, pthread_mutex_t * mutex);
extern int pthread_cond_timedwait(pthread_cond_t * cond, pthread_mutex_t * mutex,
                                  struct timespec * timeout);
extern void pthread_mutex_lock(pthread_mutex_t * mutex);
extern void pthread_mutex_unlock(pthread_mutex_t * mutex);
extern pthread_t pthread_self(void);
extern int pthread_create(pthread_t * thread, pthread_attr_t * attr,
                          void * (*start_routine)(void *), void * arg);
extern int pthread_join(pthread_t thread, void **value_ptr);

/*
 * Windows socket functions don't set errno as expected.
 * Wrappers are provided to workaround the problem.
 * TODO: more socket function wrappers are needed for better error reports on Windows
 */
#define socket(af, type, protocol) wsa_socket(af, type, protocol)
#define bind(socket, addr, addr_size) wsa_bind(socket, addr, addr_size)
extern int wsa_bind(int socket, const struct sockaddr * addr, int addr_size);
extern int wsa_socket(int af, int type, int protocol);

typedef __int64 int64;
typedef unsigned __int64 uns64;
#define lseek _lseeki64
typedef struct _stati64 struct_stat;
#define stat _stati64
#define lstat _stati64
#define fstat _fstati64
extern int truncate(const char * path, int64 size);
extern int ftruncate(int f, int64 size);
#define utimbuf _utimbuf
#define utime _utime
#define futime _futime
#define snprintf _snprintf

struct DIR {
  long hdl;
  struct _finddatai64_t blk;
  char path[FILE_PATH_SIZE];
};

struct dirent {
  char d_name[FILE_PATH_SIZE];
  int64 d_size;
  time_t d_atime;
  time_t d_ctime;
  time_t d_wtime;
};

typedef struct DIR DIR;

extern DIR * opendir(const char * path);
extern int closedir(DIR * dir);
extern struct dirent * readdir(DIR * dir);

extern char * canonicalize_file_name(const char * path);

#define O_LARGEFILE 0

extern int getuid(void);
extern int geteuid(void);
extern int getgid(void);
extern int getegid(void);

#elif defined(_WRS_KERNEL)
/* VxWork kernel module */

#define INET

#include <vxWorks.h>
#include <regs.h>
#include <pthread.h>
#include <sys/ioctl.h> 
#include <netinet/tcp.h>
#include <net/if.h>
#include <wrn/coreip/sockLib.h>
#include <wrn/coreip/hostLib.h>

#define environ taskIdCurrent->ppEnviron

#define get_regs_PC(x) (*(int *)((int)&(x) + PC_OFFSET))
#define set_regs_PC(x,y) *(int *)((int)&(x) + PC_OFFSET) = (int)(y)

#define closesocket close

typedef long long int64;
typedef unsigned long long uns64;
typedef unsigned long useconds_t;

#define FILE_PATH_SIZE PATH_MAX
#define O_BINARY 0
#define O_LARGEFILE 0
#define lstat stat
typedef struct stat struct_stat;
#define ifr_netmask ifr_addr
#define SA_LEN(addr) ((addr)->sa_len)  

extern int truncate(char * path, int64 size);
extern char * canonicalize_file_name(const char * path);

extern void usleep(useconds_t useconds);

extern int getuid(void);
extern int geteuid(void);
extern int getgid(void);
extern int getegid(void);

#else
/* Linux or UNIX */

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
#include <sys/user.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/ioctl.h> 
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <net/if.h> 

#define FILE_PATH_SIZE PATH_MAX

#define closesocket close

typedef __int64_t int64;
typedef __uint64_t uns64;
typedef struct user_regs_struct REG_SET; 
typedef struct stat struct_stat;
#define O_BINARY 0

#define get_regs_PC(x) x.eip
#define set_regs_PC(x,y) x.eip = (unsigned long)(y)

#ifndef SA_LEN  
# ifdef HAVE_SOCKADDR_SA_LEN  
#  define SA_LEN(addr) ((addr)->sa_len)  
# else /* HAVE_SOCKADDR_SA_LEN */  
#  ifdef HAVE_STRUCT_SOCKADDR_STORAGE  
static size_t get_sa_len(const struct sockaddr *addr) {  
    switch (addr->sa_family) {  
#   ifdef AF_UNIX  
        case AF_UNIX: return sizeof(struct sockaddr_un);
#   endif
#   ifdef AF_INET  
        case AF_INET: return (sizeof (struct sockaddr_in));  
#   endif  
#   ifdef AF_INET6  
        case AF_INET6: return (sizeof (struct sockaddr_in6));  
#   endif  
        default: return (sizeof (struct sockaddr));  
    }  
}  
#   define SA_LEN(addr)   (get_sa_len(addr))  
#  else /* HAVE_SOCKADDR_STORAGE */  
#   define SA_LEN(addr)   (sizeof (struct sockaddr))  
#  endif /* HAVE_SOCKADDR_STORAGE */  
# endif /* HAVE_SOCKADDR_SA_LEN */  
#endif /* SA_LEN */  

extern int tkill(pid_t pid, int signal);

#endif

extern pthread_attr_t pthread_create_attr;

extern char * get_os_name(void);
extern char * get_user_home(void);

extern void ini_mdep(void);

#endif
