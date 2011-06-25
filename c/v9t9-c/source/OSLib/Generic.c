/*
  Generic.c						-- generic operating system utilities

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
 *	Operating system library generic utilities.
 *
 */

#include "OSLib.h"

#include <string.h>
#include <stdio.h>
#include <string.h>
#if __MWERKS__ && !__INTEL__
#include <alloca.h>
#else
#include <malloc.h>
#endif
#include <ctype.h>
#if HAVE_GETPWNAM
#include <pwd.h>
#endif

#include <stdlib.h>
#include <assert.h>

char        STSbuf[OS_PATHSIZE];

/*	perform wildcard matching on a path; 
	if path is non-NULL, start a search
	else continue searching; 
	return NULL if no match found  		 */
static OSSpec wildmatch;
static OSDirRef wilddirref;
static char wilddir[OS_MAXPATHLEN];
static char wildname[OS_MAXNAMELEN];

#if defined(OS_IS_CASE_INSENSITIVE)
#define EQU(x,y) (tolower(x)==tolower(y))
#else
#define EQU(x,y) ((x)==(y))
#endif

/*	Simple wildcard matcher.  Fails if '*' is not followed by a character or EOS. */
static int
WildCardMatch(const char *wild, const char *name)
{
	if (*name == 0)
		return 0;

	while (*wild) {
		if (*wild == '*') {
			const char  next = *++wild;
			const char *prev = NULL;

			while (*name) {
				if (EQU(*name, next))
					prev = name;
				name++;
			}
			if (prev != NULL)
				name = prev;
			if (!EQU(*name, next))	/* allows matching next==0 */
				return 0;
		} else if (*wild == '?' && *name) {
			++wild;
			++name;
			if (!*wild && *name)
				return 0;
		} else if (EQU(*wild, *name)) {
			++wild;
			++name;
		} else
			return 0;
	}
	return *name == 0 || (*name == OS_PATHSEP && *(name + 1) == 0);
}

/*	This routine matches a patch with a wildcard at the end.
	It fails if the wildcard is in the directory part.
	Only matches files, not directories.
*/
OSSpec     *
OS_MatchPath(const char *path)
{
	OSError     err;
	char        filename[OS_NAMESIZE];
	bool        isfile;

	if (path != NULL) {
		OSSpec      spec;
		const char *nptr;

		/*  Note: this fails in WinNT if the wildcard
		   characters are present in path.  We need
		   to break the path at the wildcard and match
		   from there on.
		 */
		nptr = strrchr(path, OS_PATHSEP);
		if (nptr == NULL) {
			nptr = path;
			strcpyn(wilddir, OS_CWDSTR, -1, OS_PATHSIZE);
		} else {
			nptr++;
			strcpyn(wilddir, path, nptr - path, OS_PATHSIZE);
		}

		err = OS_MakePathSpec(NULL, wilddir, &spec.path);
		if (err != OS_NOERR)
			return NULL;

		strcpyn(wildname, nptr, -1, OS_NAMESIZE);
		if ((err = OS_MakeNameSpec(wildname, &spec.name)) != OS_NOERR)
			return NULL;

		if ((err = OS_OpenDir(&spec.path, &wilddirref)) != OS_NOERR)
			return NULL;
	}

	while ((err = OS_ReadDir(&wilddirref, &wildmatch, filename, &isfile)) ==
		   OS_NOERR) {
		/*  This inherently matches only filenames. */
		if (isfile && WildCardMatch(wildname, filename))
			return &wildmatch;
	}

	OS_CloseDir(&wilddirref);
	return NULL;
}

/*	Return pointer to filename part of path */
const char *
OS_GetFileNamePtr(const char *path)
{
	const char *ptr = strrchr(path, OS_PATHSEP);

	if (ptr == NULL)
		ptr = path;
	else
		ptr++;

	return ptr;
}

/*	make OSSpec from a path and filename */
OSError
OS_MakeSpec2(const char *path, const char *filename, OSSpec * spec)
{
	char        bpath[OS_PATHSIZE], *eptr;
	int         pthlen, fnlen;

	if (path == NULL)
		path = "";
	if (filename == NULL)
		filename = "";

	fnlen = strlen(filename);
	pthlen = strlen(path);
	if (fnlen + pthlen + 1 > OS_MAXPATHLEN)	// 1 for OS_PATHSEP
		return OS_FNTLERR;

	strncpy(bpath, path, pthlen);

	eptr = bpath + pthlen;
	if (*(eptr - 1) != OS_PATHSEP)
		*eptr++ = OS_PATHSEP;

	strcpy(eptr, filename);

	return OS_MakeSpec(bpath, spec, NULL);
}

/*	make OSSpec given an OSPathSpec (which may be NULL) and 
	filename, relative path, or full path (which may be NULL);
	if 'noRelative' and filename is relative, or if filename is a full path, 
	ignore 'path'.
*/
OSError
OS_MakeSpecWithPath(const OSPathSpec * path, const char *filename, bool noRelative,
					OSSpec * spec)
{
	if (filename == NULL) {
		if (path)
			spec->path = *path;
		else
			OS_GetCWD(&spec->path);
		return OS_MakeNameSpec("", &spec->name);
	}
		else
		if ((!noRelative || strpbrk(filename, "/\\:") == NULL) &&
			!OS_IsFullPath(filename)) {
		char        buf[OS_PATHSIZE];
		char       *mptr, *eptr;

		if (path)
			OS_PathSpecToString2(path, buf);
		else
			*buf = 0;
			
		mptr = buf + OS_PATHSIZE - strlen(filename) - 1;

		eptr = buf + strlen(buf);
		if (eptr > mptr)
			strcpy(mptr, filename);
		else
			strcpy(eptr, filename);

		return OS_MakeSpec(buf, spec, NULL);
	} else
		return OS_MakeSpec(filename, spec, NULL);
}

/*	change extension of a name */
OSError
OS_NameSpecChangeExtension(OSNameSpec * spec, char *ext, bool append)
{
	char        tmp[OS_NAMESIZE];
	char       *per;

	OS_NameSpecToString2(spec, tmp);
	if (!append) {
		per = strrchr(tmp, '.');
		if (per == NULL)
			per = tmp + strlen(tmp);
	} else
		per = tmp + strlen(tmp);

	if (strlen(tmp) + strlen(ext) > OS_NAMESIZE)
		per = tmp + OS_NAMESIZE - strlen(ext) - 1;

	strcpy(per, ext);
	return OS_MakeNameSpec(tmp, spec);
}

/*	set the extension of a name; if ext begins with '.', append the extension, else replace it */
OSError
OS_NameSpecSetExtension(OSNameSpec * spec, char *ext)
{
	char        tmp[OS_NAMESIZE];
	char       *per;

	OS_NameSpecToString2(spec, tmp);
	if (*ext != '.') {
		per = strrchr(tmp, '.');
		if (per == NULL)
			per = tmp + strlen(tmp);

		if (*ext)				// allow deleting extension
			if (strlen(tmp) + 1 >= OS_NAMESIZE)
				*(per - 1) = '.';
			else
				*per++ = '.';
	} else {
		per = tmp + strlen(tmp);
	}

	if (strlen(tmp) + strlen(ext) > OS_NAMESIZE)
		per = tmp + OS_NAMESIZE - strlen(ext) - 1;

	strcpy(per, ext);
	return OS_MakeNameSpec(tmp, spec);
}

/*	This silly routine shortens an overlong concatenation by
	inserting "..." in the middle of the string. 
*/
char       *
OS_CompactPaths(char *buf, const char *p, const char *n, int size)
{
	int         plen = p ? strlen(p) : 0;
	int         nlen = n ? strlen(n) : 0;

	if (plen + nlen + 1 <= size) {
		sprintf(buf, "%s%s", p ? p : "", n ? n : "");
		return buf;
	} else {
		char       *ptr = buf;
		int         bidx = 0;
		int         pnidx = 0;
		int         diff = (plen + nlen) - (size);

		while (plen > 0 && nlen > 0) {
			if (plen > 0) {
				*ptr++ = *p++;
				plen--;
			} else if (nlen > 0) {
				*ptr++ = *n++;
				nlen--;
			}

			bidx++;
			if (bidx == size / 2 - 2) {
				*ptr++ = '.';
				*ptr++ = '.';
				*ptr++ = '.';
				bidx += 3;

				if (plen > 0) {
					plen -= diff / 2;
					if (plen < 0) {
						n -= plen;
						nlen += plen;
						plen = 0;
					}
				} else {
					n += diff / 2;
					nlen -= diff / 2;
				}
			}
		}
		*ptr = 0;
		return buf;
	}
}

char       *
OS_SpecToStringRelative(const OSSpec * spec, const OSPathSpec * cwdspec,
						char *path, int size)
{
	char        fullbuf[OS_PATHSIZE], *full = fullbuf;
	char        cwdbuf[OS_PATHSIZE], *cwd = cwdbuf;
	OSPathSpec  mycwdspec;

	OS_SpecToString(spec, fullbuf, OS_PATHSIZE);

	if (size == 0)
		size = OS_PATHSIZE;
	if (path == NULL && (path = (char *) malloc(size)) == NULL)
		return NULL;

	if (cwdspec == NULL) {
		OS_GetCWD(&mycwdspec);
		cwdspec = &mycwdspec;
	}

	if (OS_PathSpecToString(cwdspec, cwdbuf, OS_PATHSIZE) == NULL) {
		memcpy(path, fullbuf, size - 1);
		path[size - 1] = 0;
		return path;
	}

	/*  find longest common prefix of full and cwd  */
	while (*cwd && EQU(*full, *cwd)) {
		full++;
		cwd++;
	}

	/*  if prefix is less than a third the length of the fullpath, abort */
	if (cwd - cwdbuf < strlen(fullbuf) / 2 &&
		strlen(cwd) > strlen(fullbuf) / 2) {
		memcpy(path, fullbuf, size - 1);
		path[size - 1] = 0;
		return path;
	}

	/*  backtrack, if necessary, up to previous OS_PATHSEP
	   [don't match foo with fool]
	 */
#if !defined(OS_REL_PATH_HAS_SEP)
	while (cwd > cwdbuf) {
		full--;
		cwd--;
		if (*cwd == OS_PATHSEP)
			break;
	}
#else
	while (cwd > cwdbuf) {
		if (*(cwd - 1) == OS_PATHSEP)
			break;
		full--;
		cwd--;
	}
#endif

	/*  complete mismatch --> out  */
	if (cwd == cwdbuf) {
		strncpy(path, full, size - 1);
		path[size - 1] = 0;
	} else {
#if !defined(OS_REL_PATH_HAS_SEP)
		/*  skip OS_PATHSEP (relative dirs don't have 'em)  */
		cwd++;
		full++;
#endif

		/*  if cwd has more than full, need OS_PDSTR's and the rest of full... */
		if (*cwd) {
			char       *pptr = path;

			while (*cwd) {
#if defined(OS_REL_PATH_HAS_SEP)
				cwd++;
				if (*cwd == OS_PATHSEP)
					pptr += sprintf(pptr, OS_PDSTR);
#else
				if (*cwd == OS_PATHSEP)
					pptr += sprintf(pptr, OS_PDSTR);
				cwd++;
#endif
			}
			strcpy(pptr, full);
		}
		/*  if cwd has less, then use the part of full that doesn't match  */
		else {
			strncpy(path, full, size - 1);
			path[size - 1] = 0;
		}
	}

	return path;
}

/*	Search for a file in a list; if not found, returns error and
	creates OSSpec in first directory in plist. */
OSError
OS_FindFileInPath(const char *filename, const char *plist, OSSpec * spec)
{
	const char *next;
	char        path[OS_PATHSIZE];
	OSError     err;
	OSSpec      first;
	bool        madefirst = false;

	while (plist && *plist) {
		next = strchr(plist, OS_ENVSEP);
		if (next == NULL)
			next = strpbrk(plist, OS_ENVSEPLIST);
		if (next == NULL)
			next = plist + strlen(plist);

		strcpyn(path, plist, next - plist, OS_MAXPATHLEN);

//      printf("searching '%s' for '%s'\n",path,filename);
		if ((err = OS_MakeSpec2(path, filename, spec)) == OS_NOERR) {
			err = OS_FIDERR;
			if (*filename && (err = OS_Status(spec)) == OS_NOERR)
				return OS_NOERR;
			if (!madefirst) {
//				printf("%s\n", OS_SpecToString1(spec));
				first = *spec;	// use this if all else fails
				madefirst = true;
			}
		}
		plist = (*next) ? next + 1 : NULL;
	}

	if (madefirst) {
		*spec = first;
	} else {
		err = OS_MakeFileSpec(filename, spec);
		if (err == OS_NOERR) {
			if (!plist)
				err = OS_Status(spec);
			else
				err = OS_FNFERR;	
					/* we just wanted a legal spec,
					   and it wasn't really found here */
		}
	}

	return err;
}

/*	Search for a place to create file in a list; 
	if not possible, returns error */
OSError
OS_CreateFileInPath(const char *filename, const char *plist, 
					OSSpec * spec, OSFileType *type)
{
	const char *next;
	char        path[OS_PATHSIZE];
	OSError     err;

	while (plist && *plist) {
		next = strchr(plist, OS_ENVSEP);
		if (next == NULL)
			next = strpbrk(plist, OS_ENVSEPLIST);
		if (next == NULL)
			next = plist + strlen(plist);

		strcpyn(path, plist, next - plist, OS_MAXPATHLEN);

		if ((err = OS_MakeSpec2(path, filename, spec)) == OS_NOERR) {
			if ((err = OS_Create(spec, type)) == OS_NOERR)
				return OS_NOERR;
		}

		plist = (*next) ? next + 1 : NULL;
	}

	if (!plist) {
		if ((err = OS_MakeFileSpec(filename, spec)) == OS_NOERR
			&& (err = OS_Create(spec, type)) == OS_NOERR)
			return OS_NOERR;
	}

	return err;
}

/*	Search for an executable using the OS standards;
	if relative path or not found, make spec in CWD  */

//#define SRCH_DEBUG 1
OSError
OS_FindProgram(const char *filename, OSSpec * spec)
{
	char       *plist;
	OSError     err;
	char        temp[OS_PATHSIZE];

	strncpy(temp, filename, OS_PATHSIZE);
	temp[OS_PATHSIZE - 1] = 0;
#if defined(WIN32_FS)
	if (strlen(temp) < 4 || strcasecmp(temp + strlen(temp) - 4, ".exe") != 0)
		strcatn(temp, ".exe", OS_PATHSIZE);
#endif

#if SRCH_DEBUG
	printf("Searching for '%s'\n", temp);
#endif

	if (strchr(temp, OS_PATHSEP) == NULL) {
		/*  Windows wants us to search its directories first.
		 */

#if defined(WIN32_FS)
		{
			char        path[OS_PATHSIZE];
			int         len;

			#ifndef GetSystemDirectory
			extern      __declspec(dllimport) int __stdcall
			            GetSystemDirectoryA(char *path, int size);
			extern      __declspec(dllimport) int __stdcall
			            GetWindowsDirectoryA(char *path, int size);
			#endif

			len = GetSystemDirectoryA(path, OS_PATHSIZE);
			if (len != 0) {
#if SRCH_DEBUG
				printf("Searching Windows system directory '%s' for '%s'\n",
					   path, temp);
#endif
				if ((err = OS_MakeSpec2(path, temp, spec)) == OS_NOERR) {
					err = OS_Status(spec);
					if (err != OS_NOERR) {
#if SRCH_DEBUG
						printf("Not found at '%s', error '%s'\n",
							   OS_SpecToString1(spec), OS_GetErrText(err));
#endif
					} else {
#if SRCH_DEBUG
						printf("Found '%s' at '%s'\n", temp,
							   OS_SpecToString1(spec));
#endif
						return err;
					}
				}
			}

			len = GetWindowsDirectoryA(path, OS_PATHSIZE);
			if (len != 0) {
#if SRCH_DEBUG
				printf("Searching Windows directory '%s' for '%s'\n", path,
					   temp);
#endif
				if ((err = OS_MakeSpec2(path, temp, spec)) == OS_NOERR) {
					err = OS_Status(spec);
					if (err != OS_NOERR) {
#if SRCH_DEBUG
						printf("Not found at '%s', error '%s'\n",
							   OS_SpecToString1(spec), OS_GetErrText(err));
#endif
					} else {
#if SRCH_DEBUG
						printf("Found '%s' at '%s'\n", temp,
							   OS_SpecToString1(spec));
#endif
						return err;
					}
				}
			}
		}
#endif

#if defined(POSIX_FS) || defined(WIN32_FS)
		plist = getenv("PATH");
#elif defined(MAC_FS)
		plist = getenv("Commands");
#else
#error
#endif

#if SRCH_DEBUG
		printf("Searching path '%s' for '%s'\n", plist, temp);
#endif
		err = OS_FindFileInPath(temp, plist, spec);
		if (err == OS_NOERR) {
#if SRCH_DEBUG
			printf("Found '%s' at '%s'\n", temp, OS_SpecToString1(spec));
#endif
			return OS_NOERR;
		}
	}

	/*  Default to CWD  */
#if SRCH_DEBUG
	printf("Using '%s'\n", temp);
#endif

	if ((err = OS_MakeFileSpec(temp, spec)) == OS_NOERR)
		err = OS_Status(spec);
#if SRCH_DEBUG
	if (err != OS_NOERR) {
		printf("Not found at '%s', error '%s'\n", temp, OS_GetErrText(err));
	}
#endif
	return err;
}

/*	Expand a string by replacing '~' with the home directory
	and $xxx with the value of environment variable 'xxx' */
char *
OS_PathExpand(const char *path)
{
	static char buf[OS_PATHSIZE];
	char *bufptr = buf;
	char name[OS_PATHSIZE];
	char *var;
	int len;

	if (*path == '~')
	{
		if (path[1] != '/')
		{
#if HAVE_GETPWNAM
			/* fill in user's home directory */
			struct passwd *pwd;
			char *nptr;
			
			nptr = path;
			while (*nptr && *nptr != '/' 
				   && *nptr != '\\' && *nptr != ':') 
				nptr++;
			len = nptr-path;
			if (len >= sizeof(name)) len = sizeof(name)-1;
			strncpy(name, path, len);
			name[len] = 0;
			
			pwd = getpwnam(name);
			if (pwd)
			{
				strncpy(bufptr, pwd->pw_dir, OS_PATHSIZE-1);
				bufptr[OS_PATHSIZE-1] = 0;
				bufptr += strlen(bufptr);
				path = nptr;
			}
#endif
		}
		else	/* "~/..." */
		{
			/* put value of $HOME in, or leave ~ */
			var = getenv("HOME");
			if (var)
			{
				strncpy(bufptr, var, OS_PATHSIZE-1);
				bufptr[OS_PATHSIZE-1] = 0;
				bufptr += strlen(bufptr);
				path++;
			}
		}
	}

	while (*path)
	{
		if (bufptr >= buf + OS_PATHSIZE)
		{
			*(bufptr-1) = 0;
			return buf;
		}

		if (*path == '\\' && path[1] == '$')
		{
			/* don't replace \$ */
			path += 2;
			*bufptr++ = '$';
		}
		else if (*path == '$')
		{
			/* replace variable $[A-Za-z_]([A-Za-z_0-9]*) with value 
			   from getenv(), or leave it in place */
			const char *nptr = ++path;
			while (*nptr &&
				   (isalpha(*nptr) || *nptr == '_' 
					|| (nptr > path && isdigit(*nptr))))
				nptr++;

			len = nptr - path;
			if (len >= sizeof(name)) len = sizeof(name)-1;

			strncpy(name, path, len);
			name[len] = 0;
			var = getenv(name);

			if (var)
			{	
				len = strlen(var);
				if (len > sizeof(buf) - (bufptr - buf) - 1)
					len = sizeof(buf) - (bufptr - buf) - 1;
				strncpy(bufptr, var, len);
				bufptr[len] = 0;
				bufptr += len;
				path = nptr;
			}
		}
		else
		{
			*bufptr++ = *path++;
		}
	}
	*bufptr = 0;
	return buf;
}


/****************************************/

/*	Handle routines.  */

/*	Copy a handle. */
OSError
OS_CopyHandle(OSHandle * hand, OSHandle * copy)
{
	OSError     err;
	OSSize      sz;
	void       *f, *t;

	if ((err = OS_GetHandleSize(hand, &sz)) == OS_NOERR &&
		(err = OS_NewHandle(sz, copy)) == OS_NOERR) {
		f = OS_LockHandle(hand);
		t = OS_LockHandle(copy);
		memcpy(t, f, sz);
		OS_UnlockHandle(hand);
		OS_UnlockHandle(copy);
		return OS_NOERR;
	} else {
		OS_FreeHandle(copy);
		return err;
	}
}

/*	Append data to a handle. */
OSError
OS_AppendHandle(OSHandle * hand, void *data, OSSize len)
{
	OSError     err;
	OSSize      sz;
	void       *buffer;

	if ((err = OS_GetHandleSize(hand, &sz)) == OS_NOERR &&
		(err = OS_ResizeHandle(hand, sz + len)) == OS_NOERR &&
		(buffer = OS_LockHandle(hand)) != NULL) {
		memcpy((char *) buffer + sz, data, len);
		OS_UnlockHandle(hand);
		return OS_NOERR;
	} else
		return err;
}
