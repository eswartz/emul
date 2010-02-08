/*
  Win32.c						-- operating system interface for Win32

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
 *	Operating system library for WIN32.
 *
 *	This module handles interaction with the operating-system specific
 *	intricacies with file manipulation, memory management, process
 *	spawning, etc.
 *
 */

#if (defined(POSIX_FS) || defined(MAC_FS)) && !defined(WIN32_FS)
#error Wrong module!
#endif

#include "config.h"
#include "OSLib.h"

#include <string.h>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <ctype.h>

#if __MWERKS_
#include <x86_prefix.h>
#endif
#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#include <stdlib.h>
#include <assert.h>

#if DEBUG
#define ASSERT(x) 	assert(x)
#else
#define ASSERT(x)
#endif

#define ERROR_RETURN 0xffffffff

#define ERROR_UNKNOWN 0xfafafafa

/*	get error text for an OSError */
char
           *
OS_GetErrText(OSError err)
{
	static char errmsg[256];
	char       *ptr;

	if (err != ERROR_UNKNOWN)
		FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, NULL, err,
					  0, errmsg, sizeof(errmsg), NULL);
	else
		strcpy(errmsg, "Unknown process spawning error");

	/*  Pointlessly, it has added CR/LF */
	ptr = errmsg + strlen(errmsg) - 2;
	if (ptr > errmsg && ptr[0] == 13 && ptr[1] == 10)
		*ptr = 0;

	return errmsg;
}

/*********************/
#if 0
#pragma mark -
#endif


/*	Initialize C program context  */
OSError
OS_InitProgram(int *argc, char ***argv)
{
	/*  This is needed to allow mangled old-style V9t9 names 
	   to be read properly without the OS re-mangling them. */
	SetFileApisToOEM();
	return OS_NOERR;
}

/*	Terminate C program context  */
OSError
OS_TermProgram(void)
{
	return OS_NOERR;
}

/*********************/
#if 0
#pragma mark -
#endif


/*  This buffer is used to hold one-time SpecToString() calls */
static char intbuf[OS_PATHSIZE];

#define GETSPECSTR(sp,bf) if (OS_SpecToString(sp, bf, sizeof(bf))==NULL) return OS_FNTLERR
#define GETPATHSPECSTR(sp,bf) if (OS_PathSpecToString(sp, bf, sizeof(bf))==NULL) return OS_FNTLERR

OSFileType  OS_TEXTTYPE = 0;

/*	create a new file, overwrite an old one if existing */
OSError
OS_Create(const OSSpec * spec, OSFileType * type)
{
	HANDLE      h;

	GETSPECSTR(spec, intbuf);
	h = CreateFile(intbuf,
				   GENERIC_WRITE | GENERIC_READ,
				   FILE_SHARE_READ,
				   NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
	if (h == INVALID_HANDLE_VALUE)
		return GetLastError();
	else {
		CloseHandle(h);
		return OS_SetFileType(spec, type);
	}
}

/*	get status of a file */
OSError
OS_Status(const OSSpec * spec)
{
	DWORD       attr;

	GETSPECSTR(spec, intbuf);
	attr = GetFileAttributes(intbuf);	/* works for directories */
	if (attr == ERROR_RETURN)
		return GetLastError();
	else
		return OS_NOERR;
}


/*  get type of a file;
	return error if directory or not found */
OSError
OS_GetFileType(const OSSpec * spec, OSFileType * type)
{
	*type = 0;
	return OS_NOERR;
}

/*  set type for a file;
	return error if directory or not found */

OSError
OS_SetFileType(const OSSpec * spec, OSFileType * type)
{
	return OS_NOERR;
}

/*  get timestamp of a file;
	return error if directory or not found */
OSError
OS_GetFileTime(const OSSpec * spec, OSTime * crtm, OSTime * chtm)
{
	FILETIME    written, created;
	OSRef       ref;
	OSError     err;

	if ((err = OS_Open(spec, OSReadOnly, &ref)) != OS_NOERR)
		return err;

	err = GetFileTime(ref, &created, NULL, &written) == 0 ?
		GetLastError() : OS_NOERR;
	OS_Close(ref);

	/*  Not simpler assignments because OSTime != FILETIME */
	if (chtm) {
		chtm->dwLowDateTime = written.dwLowDateTime;
		chtm->dwHighDateTime = written.dwHighDateTime;
	}

	if (crtm) {
		crtm->dwLowDateTime = created.dwLowDateTime;
		crtm->dwHighDateTime = created.dwHighDateTime;
	}

	return err;
}

/*  set timestamp of a file */
OSError
OS_SetFileTime(const OSSpec * spec, OSTime * crtm, OSTime * chtm)
{
	OSRef       ref;
	OSError     err;
	FILETIME    written, created;

	if (crtm) {
		created.dwLowDateTime = crtm->dwLowDateTime;
		created.dwHighDateTime = crtm->dwHighDateTime;
	}

	if (chtm) {
		written.dwLowDateTime = chtm->dwLowDateTime;
		written.dwHighDateTime = chtm->dwHighDateTime;
	}

	if ((err = OS_Open(spec, OSWrite, &ref)) != OS_NOERR)
		return err;

	err =
		SetFileTime(ref, crtm ? &created : NULL, NULL,
					chtm ? &written : NULL) == 0 ? GetLastError() : OS_NOERR;
	OS_Close(ref);

	return err;
}

/*	modify protection on a file */
OSError
OS_ModifyProtection(const OSSpec * spec, bool protect)
{
	DWORD       oldattr;

	GETSPECSTR(spec, intbuf);
	if ((oldattr = GetFileAttributes(intbuf)) != ~0)
		if (SetFileAttributes(intbuf,
							  protect ? (oldattr | FILE_ATTRIBUTE_READONLY)
							  : (oldattr & ~FILE_ATTRIBUTE_READONLY)))
			return OS_NOERR;
	return GetLastError();
}

/*	check protection on a file */
OSError
OS_CheckProtection(const OSSpec * spec, bool *is_protected)
{
	DWORD       oldattr;

	GETSPECSTR(spec, intbuf);
	if ((oldattr = GetFileAttributes(intbuf)) != ~0) {
		*is_protected = !!(oldattr & FILE_ATTRIBUTE_READONLY);
		return OS_NOERR;
	}
	return GetLastError();
}

/*	get disk space info */
OSError
OS_GetDiskStats(const OSPathSpec * spec,
				OSSize * blocksize, OSSize * total, OSSize * free)
{
	char       *dptr;
	DWORD       secsperclus, bytespersec, freeclus, totclus;

	GETPATHSPECSTR(spec, intbuf);
	dptr = (char *) OS_GetDirPtr(intbuf);
	if (*dptr == OS_PATHSEP)
		dptr++;
	*dptr = 0;
	if (GetDiskFreeSpace
		(intbuf, &secsperclus, &bytespersec, &freeclus, &totclus)) {
		*blocksize = bytespersec * secsperclus;
		*total = totclus;
		*free = freeclus;
		return OS_NOERR;
	} else
		return GetLastError();
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	open an existing file */
OSError
OS_Open(const OSSpec * spec, OSOpenMode mode, OSRef * ref)
{
	static int  modetrans[] = { GENERIC_READ, GENERIC_WRITE,
		GENERIC_READ | GENERIC_WRITE, GENERIC_WRITE
	};

	GETSPECSTR(spec, intbuf);
	*ref = CreateFile(intbuf,
					  modetrans[mode],
					  FILE_SHARE_READ,
					  NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	if (*ref == INVALID_HANDLE_VALUE)
		return GetLastError();
	else {
		if (mode == OSAppend)
			if (SetFilePointer(*ref, 0, NULL, FILE_END) == ERROR_RETURN)
				return GetLastError();

		return OS_NOERR;
	}
}

/*	write binary data, up to length bytes;
	length==0 can extend file;
	update length;
	error indicates serious failure */
OSError
OS_Write(OSRef ref, void *buffer, OSSize * length)
{
	OSPos       pos;
	OSSize      size;

	/*  This sucks, but Win32 apparently has a bug when
	   writing past position 0 on an empty file. */
	if ((pos = SetFilePointer(ref, 0L, NULL, FILE_CURRENT)) == ERROR_RETURN ||
		(size = GetFileSize(ref, NULL)) == ERROR_RETURN)
		return GetLastError();

	if (pos > size) {
		static char zeroes[32] = { 0 };
		long        fill;

		if (SetFilePointer(ref, size, NULL, FILE_BEGIN) == ERROR_RETURN)
			return GetLastError();

		while (pos > size) {
			unsigned long wrote;

			fill =
				(pos - size > sizeof(zeroes)) ? sizeof(zeroes) : pos - size;
			if (WriteFile(ref, zeroes, fill, &wrote, NULL) == 0)
				return GetLastError();
			if (wrote < fill) {
				*length = 0;
				return OS_NOERR;
			}
			size += fill;
		}
	}

	if (WriteFile(ref, buffer, *length, length, NULL) == 0)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	read binary data, up to length bytes;
	update length;
	error indicates serious failure.  */
OSError
OS_Read(OSRef ref, void *buffer, OSSize * length)
{
	if (ReadFile(ref, buffer, *length, length, NULL) == 0)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	seek a file;
	illegal seek is revealed by next write or read;
	error indicates serious failure.  */
OSError
OS_Seek(OSRef ref, OSSeekMode how, OSPos offset)
{
	static int  howtrans[] = { FILE_CURRENT, FILE_BEGIN, FILE_END };

	if (SetFilePointer(ref, offset, NULL, howtrans[how]) == ERROR_RETURN)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	return file pointer */
OSError
OS_Tell(OSRef ref, OSPos * offset)
{
	if ((*offset = SetFilePointer(ref, 0L, NULL, FILE_CURRENT)) ==
		ERROR_RETURN) return GetLastError();
	else
		return OS_NOERR;
}


/*	close a file */
OSError
OS_Close(OSRef ref)
{
	if (CloseHandle(ref) == 0)
		return GetLastError();
	else
		return OS_NOERR;
}

/*  get length of a file;
	return error if directory or not found */
OSError
OS_GetSize(OSRef ref, OSSize * length)
{
	*length = GetFileSize(ref, NULL);
	if (*length == ERROR_RETURN)
		return GetLastError();
	else
		return OS_NOERR;
}

/*  set length of a file;
	return error if directory or not found */
OSError
OS_SetSize(OSRef ref, OSSize length)
{
	DWORD       orig;

	if ((orig = SetFilePointer(ref, 0L, NULL, FILE_CURRENT)) == ERROR_RETURN
		|| SetFilePointer(ref, length, NULL, FILE_BEGIN) == ERROR_RETURN
		|| !SetEndOfFile(ref)
		|| SetFilePointer(ref, orig, NULL, FILE_BEGIN) == ERROR_RETURN)
		return GetLastError();
	else
		return OS_NOERR;
}


/**************************************/
#if 0
#pragma mark -
#endif


/*	delete a file */
OSError
OS_Delete(const OSSpec * spec)
{
	GETSPECSTR(spec, intbuf);
	if (DeleteFile(intbuf) == 0)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	rename a file */
OSError
OS_Rename(const OSSpec * oldspec, const OSSpec * newspec)
{
	char        newfn[OS_PATHSIZE];

	GETSPECSTR(oldspec, intbuf);
	GETSPECSTR(newspec, newfn);

	if (MoveFile(intbuf, newfn) == 0)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	make directory */
OSError
OS_Mkdir(const OSSpec * spec)
{
	GETSPECSTR(spec, intbuf);
	if (CreateDirectory(intbuf, NULL) == 0)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	remove directory */
OSError
OS_Rmdir(const OSPathSpec * spec)
{
	GETPATHSPECSTR(spec, intbuf);
	if (RemoveDirectory(intbuf) == 0)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	change directory */
OSError
OS_Chdir(const OSPathSpec * spec)
{
	GETPATHSPECSTR(spec, intbuf);
	if (SetCurrentDirectory(intbuf) == 0)
		return GetLastError();
	else
		return OS_NOERR;
}

static void
CanonDir(char *s)
{
	s += strlen(s);
	if (*(s - 1) != '\\') {
		*s++ = '\\';
		*s = 0;
	}
}

/*	get current working directory */
OSError
OS_GetCWD(OSPathSpec * spec)
{
	OSError     err;

	if ((err = GetCurrentDirectory(OS_PATHSIZE, spec->s)) == 0)
		return GetLastError();
	else {
		CanonDir(spec->s);
		return OS_NOERR;
	}
}

static      OSError
RedirectStdHandle(HANDLE * savedStdHandle, long which,
				  const char *outfilename)
{
	SECURITY_ATTRIBUTES saAttr;
	HANDLE      hSaveStdHandle;
	HANDLE      newfile;

	/* Set the bInheritHandle flag so pipe handles are inherited. */

	saAttr.nLength = sizeof(SECURITY_ATTRIBUTES);
	saAttr.bInheritHandle = TRUE;
	saAttr.lpSecurityDescriptor = NULL;

	/* Save the handle to the current STDOUT/STDERR. */

	hSaveStdHandle = GetStdHandle(which);

	/* Create a pipe for the child process's STDOUT/STDERR. */

	newfile = CreateFile(outfilename, GENERIC_WRITE, FILE_SHARE_READ,
						 &saAttr, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
	if (newfile == INVALID_HANDLE_VALUE)
		return GetLastError();

	/* Set a write handle to the pipe to be STDOUT. */

	if (!SetStdHandle(which, newfile))
		return GetLastError();

	*savedStdHandle = hSaveStdHandle;
	return OS_NOERR;
}

/*	spawn a subprocess */
OSError
OS_Execute(const OSSpec * spec, char **argv, char **envp,
		   const char *stdoutfile, const char *stderrfile, int *exitcode)
{
	char       *cmdline;
	int         len = 0;
	char      **ptr;
	HANDLE      savedStdout, savedStderr;
	BOOL        success;

	STARTUPINFO si;
	PROCESS_INFORMATION pi;

	/*  Construct command line. */

	/*  Get maximum length... */
	ptr = argv;
	while (*ptr) {
		len += strlen(*ptr) + 3;	/* space, possible quotes */
		ptr++;
	}
	cmdline = (char *) malloc(len);
	if (cmdline == NULL)
		return OS_MEMERR;

	ptr = argv;
	len = 0;
	while (*ptr) {
		if (strchr(*ptr, ' ') != NULL) {
			cmdline[len++] = '\"';
			strcpy(cmdline + len, *ptr);
			len += strlen(*ptr);
			cmdline[len++] = '\"';
		} else if (strchr(*ptr, '\"') != NULL) {
			char       *f = *ptr;

			while (*f) {
				if (*f == '\"') {
					*(cmdline + len) = '\\';
					*(cmdline + len + 1) = '\"';
					len++;
				} else
					*(cmdline + len) = *f;
				f++;
				len++;
			}
		} else {
			strcpy(cmdline + len, *ptr);
			len += strlen(*ptr);
		}
		cmdline[len++] = ' ';
		ptr++;
	}
	cmdline[len] = 0;

	/*  Set up output handles */
	if (stdoutfile) {
		OSError     err =

			RedirectStdHandle(&savedStdout, STD_OUTPUT_HANDLE, stdoutfile);
		if (err != OS_NOERR)
			return err;
	}

	if (stderrfile) {
		OSError     err =

			RedirectStdHandle(&savedStderr, STD_ERROR_HANDLE, stderrfile);
		if (err != OS_NOERR) {
			if (stdoutfile)
				SetStdHandle(STD_OUTPUT_HANDLE, savedStdout);
			return err;
		}
	}

	memset((void *) &si, 0, sizeof(si));
	si.cb = sizeof(si);
	si.lpTitle = "Linking";

	GETSPECSTR(spec, intbuf);

	// ignoring environment block; if used, use GetLogicalDrives and
	// GetFullPathName 'X:' to add the mandatory CWDs of each directory
	// to the new block

	success =
		CreateProcess(intbuf, cmdline, NULL, NULL, true, 0, NULL, NULL, &si,
					  &pi);

	if (stdoutfile)
		SetStdHandle(STD_OUTPUT_HANDLE, savedStdout);
	if (stderrfile)
		SetStdHandle(STD_ERROR_HANDLE, savedStderr);

	if (!success)
		return GetLastError();
	else {

		free(cmdline);
		WaitForSingleObject(pi.hProcess, INFINITE);
		if (GetExitCodeProcess(pi.hProcess, (unsigned long *) exitcode)) {
			if (*exitcode == STILL_ACTIVE) {
				fprintf(stderr, "??? OS_Exec: process still active ???\n");
				return ERROR_UNKNOWN;
			} else
				return OS_NOERR;
		} else {
			return GetLastError();
		}
	}
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	skip a volume in a fullpath */
const char *
OS_GetDirPtr(const char *path)
{
	/*  These tests are redundant since we can have 
	   a full path and can distinguish the type
	   based on whether path[0] is '\\', but it's
	   easier to read this way.
	 */
	if (path[0] == '\\' && path[1] == '\\') {	/* network */
		const char *ptr = strchr(path + 2, '\\');

		if (ptr == NULL)
			ptr = path + strlen(path);	/* only a node */
		return ptr;
	} else if (isalpha(path[0]) && path[1] == ':')	/* drive */
		return path + 2;
	else if (path[0] == '\\')
		return path;
	else {
		ASSERT(!"Cannot discern type of fullpath");
		return path;
	}
}


/*	canonicalize a filepath for host; if dst is NULL, overwrite src in place */
static      OSError
OS_CanonPath(const char *src, char *dst)
{
	const char *ptr;
	char       *dptr;

	if (strlen(src) > OS_MAXPATHLEN)
		return OS_FNTLERR;

	/*  We can do this since dst is at most the length of src */
	if (dst == NULL)
		dst = (char *) src;

	ptr = src;
	dptr = dst;

	/*  First, check for weird Cygnus usage  */
	if (ptr[0] == '/' && ptr[1] == '/' &&
		isalpha(ptr[2]) && (ptr[3] == '/' || ptr[3] == 0)) {
		*dptr++ = ptr[2];
		*dptr++ = ':';
		ptr += 3;
	} else
		/*  Check for drive */
	if (isalpha(ptr[0]) && ptr[1] == ':') {
		*dptr++ = ptr[0] & ~0x20;	/* uppercase */
		*dptr++ = ':';
		ptr += 2;
	}

	/*  Convert slashes to backslashes */

	while (*ptr) {
		if (*ptr == '/')
			*dptr++ = '\\';
		else
			*dptr++ = *ptr;
		ptr++;
	}

	*dptr = 0;

	return OS_NOERR;
}


/*	tell if a filepath is legal for filesystem;
	call after OS_CanonPath if necessary */
OSError
OS_IsLegalPath(const char *path)
{
	const char *scan = path;
	int         pthlen = 0, fnlen = 0;

	/*  Do NOT check for legal characters;
	   we're too dumb to know this, re:
	   \\?\C:\ ... and \\?\UNC\...  */

	while (*scan) {
		if (*scan == '\\')
			fnlen = 0;
		else
			fnlen++;

		pthlen++;

		if (fnlen > OS_MAXNAMELEN || pthlen > OS_MAXPATHLEN)
			return OS_FNTLERR;

		scan++;
	}
	return OS_NOERR;
}

/*	tell if a filepath represents a full path */
int
OS_IsFullPath(const char *path)
{
	/*  can be network, \\node\..., or X:\... */
	return (path[0] == '\\' && path[1] == '\\') ||
		(isalpha(path[0]) && path[1] == ':' && path[2] == '\\');
}

/*	compact a canonical full path; if dst is NULL, overwrite src in place */
static void
OS_CompactPath(char *src)
{
	char       *bptr;
	char       *to;
	char       *from, *start;

	ASSERT(OS_IsFullPath(src));

	start = (char *) OS_GetDirPtr(src);

	bptr = start;
	from = start;
	to = bptr;

	while (*from) {
		char       *brk;

		brk = from + 1;
		while (*brk && *brk != '\\')
			brk++;

		if (brk - from == 1)	/* eliminate "\\" */
			from = brk;			/* skip path break */
		else {
			if (brk - from == 2 && from[1] == '.')
				from = brk;
			else /* eliminate ".." and previous directory */ if (brk - from ==
																 3
																 && from[1] ==
																 '.'
																 && from[2] ==
																 '.') {
				if (to > bptr) {
					do
						to--;
					while (to >= bptr && *to != '\\');
				}
				from = brk;
			} else				/* copy */
				while (from < brk)
					*to++ = *from++;

		}
	}

	if (to == bptr || *(from - 1) == '\\')
		*to++ = '\\';			/* ended at directory */

	*to = 0;					/* end string */
}


/*	compare paths */
int
OS_EqualPath(const char *a, const char *b)
{
	int         offs = 0;

	while (a[offs] && b[offs] &&
		   (a[offs] >= 'A' && a[offs] <= 'Z' ? a[offs] + 32 : a[offs]) ==
		   (b[offs] >= 'A' && b[offs] <= 'Z' ? b[offs] + 32 : b[offs])) {
		offs++;
	}
	return (a[offs] == 0 && b[offs] == 0);
}

/*************************************/
#if 0
#pragma mark -
#endif

/*	make OSSpec from a path and return
	what kind it is */
OSError
OS_MakeSpec(const char *path, OSSpec * spec, bool * isfile)
{
	OSError     err;
	DWORD       attr;
	char        tmp[OS_PATHSIZE];
	char        full[OS_PATHSIZE];
	char       *ptr, *eptr;
	const char *nptr;
	int         len;

	if ((err = OS_CanonPath(path, tmp)) != OS_NOERR)
		return err;

	if (GetFullPathName(tmp, OS_PATHSIZE, full, &ptr) == 0)
		return GetLastError();

	/*  Restore stolen trailing '.' if needed */
	eptr = full + strlen(full) - 1;
	nptr = strrchr(path, OS_PATHSEP);
	if (nptr)
		nptr++;
	else
		nptr = path;
	if (strcmp(nptr, "..") != 0 && strcmp(nptr, ".") != 0 &&
		(nptr[strlen(nptr) - 1] == '.' && *eptr != '.')) {
		*++eptr = '.';
		*++eptr = 0;
	}

	/*  Tell if it's a directory  */
	if ((attr = GetFileAttributes(full)) != ERROR_RETURN) {
		if (attr & FILE_ATTRIBUTE_DIRECTORY) {
			if (isfile)
				*isfile = false;
			ptr = NULL;
		} else {
			if (isfile)
				*isfile = true;
		}
	} else {
		err = GetLastError();
		if (err != OS_NOERR && err != OS_FNFERR)
			return err;

		if (isfile)
			*isfile = true;
	}

	if (ptr == NULL) {
		ptr = eptr + 1;
		if (*(ptr - 1) != OS_PATHSEP) {
			*ptr++ = OS_PATHSEP;
			*ptr = 0;
		}
	}

	len = ptr - full;
	if (len >= OS_PATHSIZE) {
		*spec->path.s = 0;
		return OS_FNTLERR;
	}

	memcpy(spec->path.s, full, len);
	spec->path.s[len] = 0;		// truncate

	len = strlen(ptr);
	if (len >= OS_NAMESIZE) {
		*spec->name.s = 0;
		return OS_FNTLERR;
	}

	memcpy(spec->name.s, ptr, len + 1);

	return OS_NOERR;
}

/*	make OSSpec from a path;
	must resolve to a file */
OSError
OS_MakeFileSpec(const char *path, OSSpec * spec)
{
	bool        isfile;
	OSError     err;

	err = OS_MakeSpec(path, spec, &isfile);
	if (err != OS_NOERR)
		return err;

	if (!isfile)
		return OS_FIDERR;

	return OS_NOERR;
}

/*	make OSPathSpec from a volume and directory */
OSError
OS_MakePathSpec(const char *vol, const char *dir, OSPathSpec * spec)
{
	OSSpec      tmp;
	OSError     err;
	bool        isfile;
	char        path[OS_PATHSIZE + OS_VOLSIZE];
	int			len;
	
	if ((vol ? strlen(vol) : 0) + (dir ? strlen(dir) : 0) + 2 > sizeof(path))
		return OS_FNTLERR;

	if (vol != NULL) {
		if (*vol == 0)			/* non-drive root "\dl\" */
			strcpy(path, dir ? dir : "\\");
		else if (*(vol + 1) == 0)	/* assume this is a drive */
			sprintf(path, "%s:%s", vol, dir ? dir : "\\");
		else if (dir)
			sprintf(path, "%s%s%s", vol, *dir == '\\' ? "" : "\\", dir);
		else
			sprintf(path, "%s", vol);
	} else if (dir != NULL) {
		strcpy(path, dir);
	} else
		strcpy(path, ".");

	err = OS_MakeSpec(path, &tmp, &isfile);
	len = strlen(tmp.path.s);

	if (err != OS_NOERR) {
			/* ensure that path is legal */
		if (len+1 < OS_PATHSIZE)
			memcpy(spec->s+len, "\\", 2);
		else 
			memcpy(spec->s+len-1, "\\", 2);
		return err;
	} else {
		memcpy(spec->s, tmp.path.s, len + 1);
		spec->s[OS_PATHSIZE-1] = 0;
	}

	if (isfile)
		return OS_FNIDERR;

	return OS_NOERR;
}

/*	make OSNameSpec from a filename */
OSError
OS_MakeNameSpec(const char *name, OSNameSpec * spec)
{
	if (strchr(name, '\\') != NULL)
		return OS_FIDERR;

	if (strlen(name) > OS_MAXNAMELEN)
		return OS_FNTLERR;

	strcpy(spec->s, name);

	return OS_NOERR;
}


/*	return FS root spec */
OSError
OS_GetRootSpec(OSPathSpec * spec)
{
	strcpy(spec->s, "\\");
	return OS_NOERR;
}


/********************************************/
#if 0
#pragma mark -
#endif


/*	make a full pathname from OSSpec */
char       *
OS_SpecToString(const OSSpec * spec, char *path, int size)
{
	if (size == 0)
		size = OS_PATHSIZE;

	if (path == NULL && (path = (char *) malloc(size)) == NULL)
		return NULL;
	else {
		int         plen, nlen;

		plen = strlen(spec->path.s);
		nlen = strlen(spec->name.s);
		if (plen + nlen >= size) {
			if (plen >= size) {
				nlen = 0;
				plen = size - 1;
			} else
				nlen = size - plen - 1;
		}
		memcpy(path, spec->path.s, plen);
		memcpy(path + plen, spec->name.s, nlen);
		path[plen + nlen] = 0;
		return path;
	}
}

/*	make a path from OSPathSpec */
char       *
OS_PathSpecToString(const OSPathSpec * pspec, char *path, int size)
{
	if (size == 0)
		size = OS_PATHSIZE;

	if (path == NULL && (path = (char *) malloc(size)) == NULL)
		return NULL;
	else {
		int         plen;

		plen = strlen(pspec->s);
		if (plen >= size)
			plen = size - 1;
		memcpy(path, pspec->s, plen);
		path[plen] = 0;
		return path;
	}
}

/*	make a name from OSNameSpec */
char       *
OS_NameSpecToString(const OSNameSpec * nspec, char *name, int size)
{
	if (size == 0)
		size = OS_NAMESIZE;

	if (name == NULL && (name = (char *) malloc(size)) == NULL)
		return NULL;
	else {
		int         nlen = strlen(nspec->s);

		if (nlen >= size)
			nlen = size - 1;
		memcpy(name, nspec->s, nlen);
		name[nlen] = 0;
		return name;
	}
}

/*	return the size of an OSPathSpec, for duplication purposes */
int
OS_SizeOfPathSpec(const OSPathSpec * spec)
{
	return (strlen(spec->s) + 1);
}

/*	return the size of an OSNameSpec, for duplication purposes */
int
OS_SizeOfNameSpec(const OSNameSpec * spec)
{
	return (strlen(spec->s) + 1);
}

/*	compare OSSpecs */
int
OS_EqualSpec(const OSSpec * a, const OSSpec * b)
{
	return OS_EqualPathSpec(&a->path, &b->path) &&
		OS_EqualNameSpec(&a->name, &b->name);
}

/*	compare OSPathSpecs */
int
OS_EqualPathSpec(const OSPathSpec * a, const OSPathSpec * b)
{
	return (OS_EqualPath(a->s, b->s));
}

/*	compare OSNameSpecs */
int
OS_EqualNameSpec(const OSNameSpec * a, const OSNameSpec * b)
{
	return (OS_EqualPath(a->s, b->s));
}

#if 0
#pragma mark -
#endif


/*	tell if OSSpec is a directory */
int
OS_IsDir(const OSSpec * spec)
{
	DWORD       attr;
	int         len;

	GETSPECSTR(spec, intbuf);
	len = strlen(intbuf);
	if (intbuf[len - 1] == OS_PATHSEP)
		intbuf[len - 1] = 0;

	attr = GetFileAttributes(intbuf);
	if (attr == ERROR_RETURN)
		return 0;
	else
		return (attr & FILE_ATTRIBUTE_DIRECTORY) != 0;
}

/*	tell if OSSpec is a file */
int
OS_IsFile(const OSSpec * spec)
{
	DWORD       attr;
	int         len;

	GETSPECSTR(spec, intbuf);
	len = strlen(intbuf);
	if (intbuf[len - 1] == OS_PATHSEP)
		intbuf[len - 1] = 0;

	attr = GetFileAttributes(intbuf);
	if (attr == ERROR_RETURN)
		return 0;
	else
		return (attr & FILE_ATTRIBUTE_DIRECTORY) == 0;
}

/*	tell if OSSpec is a softlink */
int
OS_IsLink(const OSSpec * spec)
{
	return 0;
}

/*	resolve a [soft] link / alias */
OSError
OS_ResolveLink(const OSSpec * link, OSSpec * target)
{
	*target = *link;
	return OS_NOERR;
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	open a directory for reading */
OSError
OS_OpenDir(const OSPathSpec * spec, OSDirRef * ref)
{
	char        wildcard[OS_PATHSIZE];

	ref->dir.ffd = (WIN32_FIND_DATA *) malloc(sizeof(WIN32_FIND_DATA));
	if (ref->dir.ffd == NULL)
		return OS_MEMERR;

	ref->path = *spec;
	strcpy(wildcard, spec->s);
	strcat(wildcard, "*");		/* must specify wildcard */

	ref->dir.handle =
		FindFirstFile(wildcard, (WIN32_FIND_DATA *) ref->dir.ffd);
	if (ref->dir.handle == INVALID_HANDLE_VALUE)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	read an entry from a directory;
	don't return "." or "..";
	return error when end-of-directory reached */
OSError
OS_ReadDir(OSDirRef * ref, OSSpec * entry, char *filename, bool * isfile)
{
	WIN32_FIND_DATA *ffd = (WIN32_FIND_DATA *) ref->dir.ffd;
	char        newname[OS_PATHSIZE];
	char        fn[OS_PATHSIZE];
	OSError     err;
	int         len;

	do {
		if (ref->dir.handle == INVALID_HANDLE_VALUE)
			return OS_FNFERR;
		else {
			/*  we already have one entry cached */
			if (strlen(ffd->cFileName) < OS_NAMESIZE)
				strncpy(newname, ffd->cFileName, OS_NAMESIZE);
			else
				strncpy(newname, ffd->cAlternateFileName, OS_NAMESIZE);
			newname[OS_PATHSIZE - 1] = 0;

			/*  cache next value */
			if (FindNextFile(ref->dir.handle, ffd) == 0)
				OS_CloseDir(ref);
		}
	} while (strcmp(newname, ".") == 0 || strcmp(newname, "..") == 0 ||
			 strlen(ref->path.s) + strlen(newname) >= OS_PATHSIZE);

	/* the following is so we properly create directory specs */

	len = strlen(ref->path.s);
	strncpy(fn, ref->path.s, OS_PATHSIZE - 1);
	if (len < OS_PATHSIZE) {
		strncpy(fn + len, newname, OS_PATHSIZE - 1 - len);
		fn[OS_PATHSIZE - 1] = 0;
	} else
		return OS_FNTLERR;

	strncpy(filename, newname, OS_NAMESIZE - 1);
	filename[OS_NAMESIZE - 1] = 0;

	err = OS_MakeSpec(fn, entry, isfile);
	return err;
}

/*	close directory */
OSError
OS_CloseDir(OSDirRef * ref)
{
	if (ref->dir.handle != INVALID_HANDLE_VALUE) {
		if (FindClose(ref->dir.handle) == 0)
			return GetLastError();
		if (ref->dir.ffd)
			free(ref->dir.ffd);
		ref->dir.ffd = NULL;
		ref->dir.handle = INVALID_HANDLE_VALUE;
	}
	return OS_NOERR;
}

#if 0
#pragma mark -
#endif

/*	return time in milliseconds */
unsigned long
OS_GetMilliseconds(void)
{
	return GetTickCount();
}

/*	return current time */
void
OS_GetTime(OSTime * tm)
{
	SYSTEMTIME  stm;
	FILETIME    ftm;

	GetSystemTime(&stm);
	SystemTimeToFileTime(&stm, &ftm);	/* may return error, ignored */
	*tm = *(OSTime *) & ftm;
}

#if 0
#pragma mark -
#endif

enum { OSMemDelta = 4096 };

/*	allocate a memory handle  */
OSError
OS_NewHandle(OSSize size, OSHandle * hand)
{
	hand->glob = GlobalAlloc(GMEM_FIXED, size);	/* size may be zero */
	hand->used = size;
	if (hand->glob == NULL)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	resize handle  */
OSError
OS_ResizeHandle(OSHandle * hand, OSSize size)
{
	/*  reallocating a GMEM_FIXED object returns another GMEM_FIXED object
	   in possibly a new location */
	HGLOBAL     nglob = GlobalReAlloc(hand->glob, size, GMEM_MOVEABLE);

	if (nglob == NULL) {
		hand->glob = NULL;		// assume it's hosed
		hand->used = 0;
		return GetLastError();
	} else {
		hand->glob = nglob;
		hand->used = size;
		return OS_NOERR;
	}
}

/*	lock handle  */
void       *
OS_LockHandle(OSHandle * hand)
{
	if (GlobalFlags(hand->glob) != GMEM_INVALID_HANDLE)
		return (void *) hand->glob;
	else
		return NULL;
}

/*	unlock handle  */
void
OS_UnlockHandle(OSHandle * hand)
{
}

/*	free handle  */
OSError
OS_FreeHandle(OSHandle * hand)
{
	if (GlobalFree(hand->glob) != NULL)
		return GetLastError();
	else {
		hand->glob = NULL;
		hand->used = 0;
		return OS_NOERR;
	}
}

/*	get handle size */
OSError
OS_GetHandleSize(OSHandle * hand, OSSize * size)
{
	if (GlobalFlags(hand->glob) != GMEM_INVALID_HANDLE) {
		*size = (OSSize) hand->used;	/* don't use GlobalSize since it rounds up */
		return OS_NOERR;
	} else {
		*size = 0;
		return OS_MEMERR;
	}
}

/*	invalidate handle */
void
OS_InvalidateHandle(OSHandle * hand)
{
	hand->glob = NULL;
	hand->used = 0;
}

/*	tell whether a handle is valid */
bool
OS_ValidHandle(OSHandle * hand)
{
	return hand != NULL && hand->glob != NULL;
}

/*********************************************/
#if 0
#pragma mark -
#endif


/*	Shared library routines.  */

/*	open a shared library  */
OSError
OS_OpenLibrary(const OSSpec * spec, OSLibrary * lib)
{
	GETSPECSTR(spec, intbuf);

	*lib = LoadLibrary(intbuf);
	if (*lib == NULL)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	find a symbol in the library */
OSError
OS_GetLibrarySymbol(OSLibrary lib, char *name, void **sym)
{
	*(FARPROC *) (*sym) = GetProcAddress(lib, name);
	if (*sym == NULL)
		return GetLastError();
	else
		return OS_NOERR;
}

/*	close a shared library */
OSError
OS_CloseLibrary(OSLibrary lib)
{
	if (!FreeLibrary(lib))
		return GetLastError();
	else
		return OS_NOERR;
}

/*************************************/

/*	Thread management */

/*	create a thread  */
OSError
OS_CreateThread(OSThread *thread, OSThreadEntry routine, void *arg)
{
	*thread = CreateThread(NULL, 65536, routine, NULL, 
						   CREATE_SUSPENDED, NULL);
	return (*thread == 0) ? GetLastError() : 0;
}

/*	suspend a thread */
OSError
OS_SuspendThread(OSThread thread)
{
	return SuspendThread(thread) ? 0 : GetLastError();
}

/*	resume a thread */
OSError
OS_ResumeThread(OSThread thread)
{
	return ResumeThread(thread) ? 0 : GetLastError();
}

/*	wait for a thread to complete */
OSError
OS_JoinThread(OSThread thread, void **ret)
{
	//???
	*ret = 0L;
	return CloseHandle(thread) ? GetLastError() : 0;
}

/*	kill a thread */
OSError
OS_KillThread(OSThread thread, bool force)
{
	return CloseHandle(thread) ? GetLastError() : 0;
}

/*	create a mutex */
OSError
OS_CreateMutex(OSMutex *mx)
{
	*mx = CreateMutex(NULL /*security*/, FALSE /*owner*/, NULL /*name*/);
	return *mx ? 0 : GetLastError();
}

/* 	lock a mutex */
OSError
OS_LockMutex(OSMutex *mx)
{
	return WaitForSingleObject(*mx, INFINITE) ? 0 : GetLastError();
}

/*	unlock a mutex */
OSError
OS_UnlockMutex(OSMutex *mx)
{
	return ReleaseMutex(*mx) ? 0 : GetLastError();
}

/*	try to lock a mutex */
OSError
OS_TryLockMutex(OSMutex *mx)
{
	return WaitForSingleObject(*mx, 0) ? 0 : GetLastError();
}

/*	destroy a mutex */
OSError
OS_KillMutex(OSMutex *mx)
{
	return CloseHandle(*mx) ? 0 : GetLastError();
}


/*	create a semaphore */
OSError
OS_CreateSemaphore(OSSemaphore *cd, int value)
{
	#warning args?
	*cd = CreateSemaphore(NULL, value, value, NULL);
	return *cd ? 0 : GetLastError();
}

/* 	increment a semaphore */
OSError
OS_PostSemaphore(OSSemaphore *cd)
{
	return ReleaseSemaphore(*cd, 1, NULL) ? 0 : GetLastError();
}

/*	wait on a semaphore and decrement it */
OSError
OS_WaitSemaphore(OSSemaphore *cd)
{
	return WaitForSingleObject(*cd, INFINITE) ? 0 : GetLastError();
}

/*	try to wait on a semaphore and decrement it */
OSError
OS_TryWaitSemaphore(OSSemaphore *cd)
{
	return WaitForSingleObject(*cd, 0) ? 0 : GetLastError();
}

/*	destroy a semaphore */
OSError
OS_KillSemaphore(OSSemaphore *cd)
{
	return CloseHandle(*cd) ? OS_NOERR : GetLastError();
}

/*	create a condition */
OSError
OS_CreateCondition(OSCondition *cd)
{
	*cd = CreateEvent(NULL, FALSE, FALSE, NULL);
	return *cd ? 0 : GetLastError();
}

/* 	trigger a condition */
OSError
OS_TriggerCondition(OSCondition *cd)
{
	return SetEvent(*cd) ? 0 : GetLastError();
}

/*	wait on a condition */
OSError
OS_WaitCondition(OSCondition *cd)
{
	return WaitForSingleObject(*cd, INFINITE) == WAIT_OBJECT_0 ? 0 : GetLastError();
}

/*	try to wait on a condition for usec microseconds */
OSError
OS_TimedWaitCondition(OSCondition *cd, int usec)
{
	return WaitForSingleObject(*cd, usec / 1000) == WAIT_OBJECT_0 ? 0 : GetLastError();
}

/*	done with condition handling (pair with OS_WaitCondition) */
OSError
OS_HandledCondition(OSCondition *cd)
{
	return ResetEvent(*cd) ? OS_NOERR : GetLastError();
}

/*	destroy a condition */
OSError
OS_KillCondition(OSCondition *cd)
{
	return CloseHandle(*cd) ? 0 : GetLastError();
}
