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
 *	WIN32 types
 */

#if __MWERKS__
#include <x86_prefix.h>
#include <windef.h>
#include <winerror.h>
#else
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#endif
#include <time.h>

#define OS_MAXPATHLEN 	(MAX_PATH-1)
#define OS_MAXNAMELEN	63
#define OS_MAXVOLLEN	63		/* why this long?  for network names */

#define OS_VOLSIZE		64
#define OS_NAMESIZE		64
#define OS_PATHSIZE		MAX_PATH

#define OS_PATHSEP		'\\'
#define OS_PDSTR		"..\\"
#define OS_CWDSTR		"."

#define OS_ENVSEP		';'
#define OS_ENVSEPLIST	";,"

#define	OS_IS_CASE_INSENSITIVE 1
#undef	OS_REL_PATH_HAS_SEP

/*	As used by CreateFile() */
typedef	
HANDLE					OSRef;		/*	file ref */

/*	C string ending in '\\' */
typedef
struct				OSPathSpec
{
	char			s[OS_PATHSIZE];
}					OSPathSpec;			/*  OS specifier for a path */

typedef
struct				OSNameSpec
{
	char			s[OS_NAMESIZE];
}					OSNameSpec;			/*  OS specifier for a name */

/*	As used by FindFirstFile()/FindNextFile() */
typedef
struct
{
	struct
	{
		HANDLE		handle;
		void		*ffd;			/* really WIN32_FIND_DATA* */
	}				dir;
	OSPathSpec		path;			/* original path */
}					OSDirRef;		/*	directory scan ref */

/*	As used by GlobalAlloc(), etc.  */
typedef
struct	OSHandle
{
	void	*glob;
	size_t	used;
}		OSHandle;						/*  memory handle */

/*	As used by GetLastError() */
typedef 
DWORD					OSError;		/*	error type */

/*	No-error code */
#define	OS_NOERR		NO_ERROR

/*	Does OSError report error? */
#define OS_ISERR(x)		((x)!=NO_ERROR)

/*	OSError representing 'file not found' */
#define OS_FNFERR		ERROR_FILE_NOT_FOUND

/*	OSError representing 'directory not found' */
#define OS_DNFERR		ERROR_PATH_NOT_FOUND

/*	OSError representing 'file is a directory' */
#define OS_FIDERR		ERROR_ACCESS_DENIED

/*	OSError representing 'file is not a directory' */
#define OS_FNIDERR		ERROR_DIRECTORY

/*	OSError representing 'filename too long' */
#define OS_FNTLERR		ERROR_BUFFER_OVERFLOW

/*	OSError representing 'out of memory' */
#define OS_MEMERR		ERROR_NOT_ENOUGH_MEMORY

/*	OSError representing 'permission denied' */
#define OS_PERMERR		ERROR_INVALID_ACCESS

/*	OSError representing 'busy' (as for handles) */
#define OS_BUSYERR		ERROR_SHARING_VIOLATION

/*	Meaningless. */
typedef	
int					OSFileType;		/*	way to identify a file's type */

extern				OSFileType OS_TEXTTYPE;			/* text file */

/*	Time; a replica of FILETIME, in Win32, 
	a 64-bit value counting nanoseconds since 1/1/1601 (obviously) */
typedef
struct
{
	DWORD			dwLowDateTime;
	DWORD			dwHighDateTime;
}					OSTime;

/*	As used by LoadLibrary, etc */
typedef
HINSTANCE				OSLibrary;		/*  library handle */

typedef DWORD __stdcall (*OSThreadEntry)(void *);	/* start running a thread */

typedef 
HANDLE					OSThread;		/*	thread handle */

typedef
HANDLE					OSMutex;		/*	mutex */

typedef 
HANDLE					OSSemaphore;	/*	semaphore */

typedef 
HANDLE					OSCondition;	/*	condition */
