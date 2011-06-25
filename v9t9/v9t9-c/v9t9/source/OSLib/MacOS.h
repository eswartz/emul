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
 *	Mac types
 */

#include <MacTypes.h>
#include <Files.h>
#include <Errors.h>

#define OS_MAXNAMELEN	63
#define OS_MAXVOLLEN	27
#define OS_MAXPATHLEN 	255

#define OS_VOLSIZE		28
#define OS_NAMESIZE		64
#define OS_PATHSIZE		256

#define OS_PATHSEP		':'
#define OS_CWDSTR		":"
#define OS_PDSTR		":"

#define OS_ENVSEP		','
#define OS_ENVSEPLIST	",;"

#define OS_IS_CASE_INSENSITIVE	1
#define OS_REL_PATH_HAS_SEP		1

/*	As used by HOpen() */
typedef	
short					OSRef;		/*	file ref */

typedef
struct				OSPathSpec
{
	short 	vRefNum;
	long	dirID;
}					OSPathSpec;		/*  OS path specifier */

typedef
struct				OSNameSpec
{
	Str63	name;
}					OSNameSpec;		/*  OS name specifier */

/*	As used by PBGetCatInfoSync() */
typedef
struct
{
	short			vRefNum;		/*  original volume */
	long			dirID;			/*  original directory */
	long			index;			/*  current index */
}					OSDirRef;		/*	directory scan ref */

/*	As used by NewHandle, etc  */
typedef struct		OSHandle
{
	Handle		h;
}					OSHandle;

/*	As used by all I/O routines */
typedef 
OSErr					OSError;		/*	error type */

/*	No-error code */
#define	OS_NOERR		noErr

/*	Does OSError report error? */
#define OS_ISERR(x)		((x)!=noErr)

/*	OSError representing 'file not found' */
#define OS_FNFERR		fnfErr

/*	OSError representing 'directory not found' */
#define OS_DNFERR		dirNFErr

/*	OSError representing 'file is a directory' */
#define OS_FIDERR		notAFileErr

/*	OSError representing 'file is not a directory' */
#define OS_FNIDERR		dirNFErr

/*	OSError representing 'filename too long' */
#define OS_FNTLERR		bdNamErr

/*	OSError representing 'out of memory' */
#define OS_MEMERR		memFullErr

/*	OSError representing 'permission denied' */
#define OS_PERMERR		permErr

/*	OSError representing 'busy' (as for handles) */
#define OS_BUSYERR		memLockedErr

/*	File type code. */
typedef	
OSType				OSFileType;		/*	way to identify a file's type */

extern				OSFileType OS_TEXTTYPE;			/* text file */

/*	Time; a 32-bit value counting seconds since 1/1/1904. */
typedef
unsigned long		OSTime;

/*	Shared library access doesn't exist.  */
typedef
void *				OSLibrary;

typedef
void *			   	OSThread;
