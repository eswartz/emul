/*
  OSLib.h						-- header for operating system interface

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
 *	Operating system library.  
 *
 *	This module handles interaction with the operating-system specific
 *	intricacies with file manipulation, memory management, process
 *	spawning, etc.
 *
 */

#ifndef __OSLIB_H__
#define __OSLIB_H__

#include <config.h>

#include "centry.h"
#include "clstandardtypes.h"
#include "clstandardheaders.h"

#include "sysdeps.h"

/*	Operating system implementation */

/*	Guess the type;

	although there are references to compilers,
	these are only based on what I know; these
	xxx_FS defines should not make assumptions
	about the C library                         
*/
#if defined(UNDER_MACOS)
#define MAC_FS 1
#endif

#if defined(UNDER_QNX)
#define QNX_FS 		1
#define POSIX_FS 	1
#endif

#if defined(UNDER_BEOS)
#define BEWORKS_FS	1
#define POSIX_FS	1
#endif

#if defined(UNDER_UNIX)
#define POSIX_FS	1
#endif

#if defined(UNDER_WIN32)
#define WIN32_FS	1
#endif

#if (defined(WIN32_FS) && defined(POSIX_FS)) || \
	(defined(WIN32_FS) && defined(QNX_FS)) || \
	(defined(MAC_FS) && defined(POSIX_FS)) || \
	(defined(MAC_FS) && defined(WIN32_FS))
#error Conflicting operating systems selected.
#elif !defined(WIN32_FS) && !defined(POSIX_FS) && !defined(MAC_FS)
#error No operating system specified.
#endif

#if WIN32_FS
#include "Win32.h"
#elif POSIX_FS
#include "Posix.h"
#elif MAC_FS
#include "MacOS.h"
#endif

/*	OS specifier */

typedef
struct	OSSpec
{
	OSPathSpec	path;
	OSNameSpec	name;
}	OSSpec;


/*	get error text for an OSError */
char
*OS_GetErrText(OSError err);				

/*********************/

/*	Initialize C program context  */
OSError
OS_InitProgram(int *argc, char ***argv);

/*	Terminate C program context  */
OSError
OS_TermProgram(void);

/*********************/

typedef unsigned long	OSSize;
typedef signed long		OSPos;

/*	create a new file, overwrite an old one if existing */
OSError	
OS_Create(const OSSpec *spec, OSFileType *type);

/*	get status of a file */
OSError
OS_Status(const OSSpec *spec);

/*  get type of a file */
OSError
OS_GetFileType(const OSSpec *spec, OSFileType *type);

/*  set type for a file */
OSError
OS_SetFileType(const OSSpec *spec, OSFileType *type);

/*  get timestamps of a file */
OSError
OS_GetFileTime(const OSSpec *spec, OSTime *crtm, OSTime *chtm);

/*  set timestamps of a file; crtm or chtm may be NULL */
OSError
OS_SetFileTime(const OSSpec *spec, OSTime *crtm, OSTime *chtm);

/*	modify protection on a file */
OSError
OS_ModifyProtection(const OSSpec *spec, bool protect);

/*	get protection on a file */
OSError
OS_CheckProtection(const OSSpec *spec, bool *is_protected);

/*	get disk space info */
OSError
OS_GetDiskStats(const OSPathSpec *spec, 
				OSSize *blocksize, OSSize *total, OSSize *free);
				

/*************************************/

typedef enum	
{
	OSReadOnly, 						/* only read */
	OSWrite, 							/* only write */
	OSReadWrite,					 	/* read and write */
	OSAppend			 				/* only append */
} 
OSOpenMode;

typedef enum 
{ 
 	OSSeekRel, 							/* seek relative to current position */
	OSSeekAbs, 							/* absolute position */
	OSSeekEnd 							/* from end of file */
}
OSSeekMode;

/*	open an existing file */
OSError
OS_Open(const OSSpec *spec, OSOpenMode mode, OSRef *ref);

/*	write binary data, up to length bytes;
	length==0 can extend file;
	update length;
	error indicates serious failure */
OSError
OS_Write(OSRef ref, void *buffer, OSSize *length);

/*	read binary data, up to length bytes;
	update length;
	error indicates serious failure.  */
OSError
OS_Read(OSRef ref, void *buffer, OSSize *length);

/*	seek a file;
	illegal seek is revealed by next write or read;
	error indicates serious failure.  */
OSError
OS_Seek(OSRef ref, OSSeekMode how, OSPos offset);

/*	tell file position */
OSError
OS_Tell(OSRef ref, OSPos *offs);

/*	close a file */
OSError
OS_Close(OSRef ref);

/*  get length of a file;
	return error if directory or not found */
OSError
OS_GetSize(OSRef ref, OSSize *length);

/*  set length of a file;
	return error if directory or not found */
OSError
OS_SetSize(OSRef ref, OSSize length);


/*******************************************/

/*	delete a file */
OSError
OS_Delete(const OSSpec *spec);

/*	rename a file */
OSError
OS_Rename(const OSSpec *oldspec, const OSSpec *newspec);

/*	make directory */
OSError
OS_Mkdir(const OSSpec *spec);

/*	remove directory */
OSError
OS_Rmdir(const OSPathSpec *spec);

/*	change directory */
OSError
OS_Chdir(const OSPathSpec *spec);

/*	get current working directory */
OSError
OS_GetCWD(OSPathSpec *spec);

/*	spawn a subprocess; 
	pass the full environment block or NULL for the	current one */
OSError
OS_Execute(const OSSpec *spec, char **argv, char **envp, 
			const char *stdoutfile, const char *stderrfile, int *exitcode);

/*************************************/

/*	tell if a canonical filepath is legal for filesystem */
OSError
OS_IsLegalPath(const char *path);

/*	tell if a canonical filepath represents a full path */
int
OS_IsFullPath(const char *path);

/*	return ptr to directory in a vol+dir path */
const char *
OS_GetDirPtr(const char *path);

/*	compare paths */
int
OS_EqualPath(const char *a, const char *b);

/*************************************/

/*	make OSSpec from a path;
	path may be relative or absolute, or a filename. */
OSError
OS_MakeSpec(const char *path, OSSpec *spec, bool *isfile);

/*	make OSSpec from a path;
	must resolve to a file. */
OSError
OS_MakeFileSpec(const char *path, OSSpec *spec);

/*	make OSPathSpec from a volume and directory;
	does not necessarily validate 'vol' as a volume
	or 'dir' as a directory; but only the final path;
	'vol' or 'dir' may each be NULL. */
OSError
OS_MakePathSpec(const char *vol, const char *dir, OSPathSpec *spec);

/*	make OSNameSpec from a filename;
	tests name for illegal characters */
OSError
OS_MakeNameSpec(const char *name, OSNameSpec *spec);

/*	return FS root spec */
OSError
OS_GetRootSpec(OSPathSpec *spec);

/*************************************/

/*
	For the OS_xxxToString functions, the string buffer
	and its maximum size are passed.  If the output is too
	big for the buffer, the output is truncated.  If the
	buffer is given as NULL, the buffer is malloc()ed.
	
	A pointer to the buffer is returned, or NULL if memory is out.
*/

extern char STSbuf[OS_PATHSIZE];

/*	make a full pathname from OSSpec */
char *
OS_SpecToString(const OSSpec *spec, char *fullpath, int size);

#define OS_SpecToString1(spec) \
	OS_SpecToString(spec, STSbuf, OS_PATHSIZE)
#define OS_SpecToString2(spec,buf) \
	OS_SpecToString(spec, buf, OS_PATHSIZE)

/*	make a path from OSPathSpec */
char *
OS_PathSpecToString(const OSPathSpec *spec, char *path, int size);

#define OS_PathSpecToString1(spec) \
	OS_PathSpecToString(spec, STSbuf, OS_PATHSIZE)
#define OS_PathSpecToString2(spec,buf) \
	OS_PathSpecToString(spec, buf, OS_PATHSIZE)

/*	make a name from OSNameSpec */
char *
OS_NameSpecToString(const OSNameSpec *spec, char *name, int size);

#define OS_NameSpecToString1(spec) \
	OS_NameSpecToString(spec, STSbuf, OS_PATHSIZE)
#define OS_NameSpecToString2(spec,buf) \
	OS_NameSpecToString(spec, buf, OS_PATHSIZE)

/*	return the size of an OSPathSpec, for duplication purposes */
int
OS_SizeOfPathSpec(const OSPathSpec *spec);

/*	return the size of an OSNameSpec, for duplication purposes */
int
OS_SizeOfNameSpec(const OSNameSpec *spec);

/*	compare OSSpecs */
int
OS_EqualSpec(const OSSpec *a, const OSSpec *b);

/*	compare OSPathSpecs */
int
OS_EqualPathSpec(const OSPathSpec *a, const OSPathSpec *b);

/*	compare OSNameSpecs */
int
OS_EqualNameSpec(const OSNameSpec *a, const OSNameSpec *b);

/*	tell if OSSpec is a directory */
int
OS_IsDir(const OSSpec *spec);

/*	tell if OSSpec is a file */
int
OS_IsFile(const OSSpec *spec);

/*	tell if OSSpec is a [soft] link / alias */
int
OS_IsLink(const OSSpec *spec);

/*	resolve a [soft] link / alias;  link ptr may be equal to target ptr */
OSError
OS_ResolveLink(const OSSpec *link, OSSpec *target);

/*************************************/

/*	open a directory for reading */
OSError 
OS_OpenDir(const OSPathSpec *spec, OSDirRef *ref);

/*	read an entry from a directory;
	don't return "." or "..";
	return error when end-of-directory reached */
OSError
OS_ReadDir(OSDirRef *ref, OSSpec *entry, char *filename, bool *isfile);

/*	close directory */
OSError
OS_CloseDir(OSDirRef *ref);


/*************************************/

/*	return time in milliseconds */
unsigned long
OS_GetMilliseconds(void);

/*	return current time */
void
OS_GetTime(OSTime *tm);

/*	allocate a memory handle */
OSError
OS_NewHandle(OSSize size, OSHandle *hand);

/*	resize handle; handle may not be locked  */
OSError
OS_ResizeHandle(OSHandle *hand, OSSize size);

/*	lock handle into memory; always succeeds  */
void *
OS_LockHandle(OSHandle *hand);

/*	unlock handle  */
void
OS_UnlockHandle(OSHandle *hand);

/*	free handle  */
OSError
OS_FreeHandle(OSHandle *hand);

/*	get handle size */
OSError
OS_GetHandleSize(OSHandle *hand, OSSize *size);

/*	invalidate handle */
void
OS_InvalidateHandle(OSHandle *hand);

/*	tell whether a handle is valid */
bool
OS_ValidHandle(OSHandle *hand);

/*************************************/

/*	Shared library / DLL routines  */

/*	open a shared library  */
OSError
OS_OpenLibrary(const OSSpec *spec, OSLibrary *lib);

/*	find a symbol in the library */
OSError
OS_GetLibrarySymbol(OSLibrary lib, char *name, void **sym);

/*	close a shared library */
OSError
OS_CloseLibrary(OSLibrary lib);

/*************************************/

/*	Thread management */

/*	create a thread  */
OSError
OS_CreateThread(OSThread *thread, OSThreadEntry routine, void *arg);

/*	suspend a thread */
OSError
OS_SuspendThread(OSThread thread);

/*	resume a thread */
OSError
OS_ResumeThread(OSThread thread);

/*	wait for a thread to complete */
OSError
OS_JoinThread(OSThread thread, void **ret);

/*	kill a thread */
OSError
OS_KillThread(OSThread thread, bool force);

/*	create a mutex */
OSError
OS_CreateMutex(OSMutex *mx);

/* 	lock a mutex */
OSError
OS_LockMutex(OSMutex *mx);

/*	unlock a mutex */
OSError
OS_UnlockMutex(OSMutex *mx);

/*	try to lock a mutex */
OSError
OS_TryLockMutex(OSMutex *mx);

/*	destroy a mutex */
OSError
OS_KillMutex(OSMutex *mx);

/*	create a semaphore */
OSError
OS_CreateSemaphore(OSSemaphore *sem, int value);

/* 	increment a semaphore */
OSError
OS_PostSemaphore(OSSemaphore *sem);

/*	wait on a semaphore and decrement it */
OSError
OS_WaitSemaphore(OSSemaphore *sem);

/*	try to wait on a semaphore and decrement it */
OSError
OS_TryWaitSemaphore(OSSemaphore *sem);

/*	destroy a semaphore */
OSError
OS_KillSemaphore(OSSemaphore *sem);

/*	create a condition */
OSError
OS_CreateCondition(OSCondition *cd);

/* 	trigger a condition */
OSError
OS_TriggerCondition(OSCondition *cd);

/*	wait on a condition */
OSError
OS_WaitCondition(OSCondition *cd);

/*	done with condition handling (pair with OS_WaitCondition) */
OSError
OS_HandledCondition(OSCondition *cd);

/*	try to wait on a condition for usec microseconds */
OSError
OS_TimedWaitCondition(OSCondition *cd, int usec);

/*	destroy a condition */
OSError
OS_KillCondition(OSCondition *cd);


/*************************************/

#define __INSIDE_OSLIB_H__
#include "OSLibGeneric.h"
#include "OSLibExtras.h"
#undef __INSIDE_OSLIB_H__


#include "cexit.h"

#endif	//__OSLIB_H__
