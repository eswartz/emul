/*
  (c) 1994-2001 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.  

*/

/*
  $Id$
 */

/*
 *	POSIX types
 */
#ifndef _POSIX_C_SOURCE
#define _POSIX_C_SOURCE 199309
#endif

#include <limits.h>
#include <errno.h>
#include <time.h>
#include <pthread.h>
#include <semaphore.h>

#if !defined(PATH_MAX) && __linux__
#include <linux/limits.h>
#endif

#if defined(BEWORKS_FS) || defined(QNX_FS)
#define OS_MAXVOLLEN	63
#else
#define OS_MAXVOLLEN	7
#endif

// until we can fudge the FSSpecs...
#define OS_MAXNAMELEN	63

#if OSLIB_USE_MAXIMUM_PATH
	#define OS_MAXPATHLEN (PATH_MAX-1)
#else
	#if PATH_MAX > 255
	#define OS_MAXPATHLEN 255
	#else
	#define OS_MAXPATHLEN (PATH_MAX-1)
	#endif
#endif

#define OS_VOLSIZE		(OS_MAXVOLLEN+1)
#define OS_NAMESIZE		(OS_MAXNAMELEN+1)
#define OS_PATHSIZE		(OS_MAXPATHLEN+1)

#define OS_PATHSEP		'/'
#define OS_CWDSTR		"."
#define OS_PDSTR		"../"

#define	OS_ENVSEP		':'
#define	OS_ENVSEPLIST	":;"

#undef	OS_IS_CASE_INSENSITIVE
#undef	OS_REL_PATH_HAS_SEP

/*	As used by open() */
typedef	
int					OSRef;		/*	file ref */

/*	C string representing a full path;
	directories must end in '/'
 */
typedef
struct				OSPathSpec
{
	char			s[OS_PATHSIZE];
}					OSPathSpec;		/*	OS specifier for a path */

/*	C string representing a name  */
typedef
struct				OSNameSpec
{
	char			s[OS_NAMESIZE];	
}					OSNameSpec;		/* 	OS specifier for a name */

/*	As used by opendir() */
typedef
struct
{
	void		*dir;					/* really DIR* */
	OSPathSpec	path;					/* original path */
}					OSDirRef;		/*	directory scan ref */

/*	As used by malloc(), etc.  */
typedef
struct				OSHandle
{
	void			*addr;					/* of block */
	size_t			used,size;				/* used, total bytes */
}					OSHandle;		/*  memory handle */

/*	As used by errno */
typedef 
int					OSError;		/*	error type */

/*	No-error code */
#define	OS_NOERR		0

/*	Does OSError report error? */
#define OS_ISERR(x)		((x)<0)

#if defined(__BEOS__)
#include <be/support/Errors.h>
#endif

/*	OSError representing 'file not found' */
#define OS_FNFERR		ENOENT

/*	OSError representing 'directory not found' */
#define OS_DNFERR		ENOENT

/*	OSError representing 'file is a directory' */
#define OS_FIDERR		EISDIR

/*	OSError representing 'file is not a directory' */
#define OS_FNIDERR		ENOTDIR

/*	OSError representing 'filename too long' */
#define OS_FNTLERR		ENAMETOOLONG

/*	OSError representing 'out of memory' */
#define OS_MEMERR		ENOMEM

/*	OSError representing 'permission denied' */
#define OS_PERMERR		EACCES

/*	OSError representing 'busy' (as for handles) */
#define OS_BUSYERR		EBUSY

/*	OSError representing 'timeout' (for conditions) */
#define OS_TIMEOUTERR	ETIMEDOUT

/*	A permissions mask [and mime type] */
typedef	
struct
{
	int		perm;
#if defined(BEWORKS_FS)
	char	mime[256];
#endif	
}					OSFileType;		/*	way to identify a file's type */

extern				OSFileType OS_TEXTTYPE;			/* text file */

/*	Time */
typedef
time_t				OSTime;

/*
	Libraries:  although QNX does not have 
	run-time library support, we define the type to
	allow stuff to compile.
 */
#if !defined(UNDER_BEWORKS)
	/*	As used by dlopen() */
	typedef
	void *			OSLibrary;		/*	library handle */
#else
	/*	As used by load_add_on() */
	typedef
	long			OSLibrary;		/*	library handle */
#endif

/*	Threads: using the pthread library */

typedef pthread_t 	OSThread;		/* thread handle */

typedef void *(*OSThreadEntry)(void *);	/* start running a thread */

typedef pthread_mutex_t OSMutex;	/* mutex */

typedef sem_t		OSSemaphore;	/* semaphore */

typedef struct {
	pthread_mutex_t	mutex;			/* mutex for condition */
	pthread_cond_t	cond;			/* the condition */
}					OSCondition;	/* condition variable */
