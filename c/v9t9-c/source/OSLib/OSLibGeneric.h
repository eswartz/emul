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

#ifndef __INSIDE_OSLIB_H__
#error Only include this file in OSLib.h
#endif

/*************************************/
/*	Generic routines implementation	 */
/*************************************/

/*	perform wildcard matching on a path; 
	if path is non-NULL, start a search
	else continue searching; 
	return NULL if no match found;
	only matches filenames  		 */
OSSpec *
OS_MatchPath(const char *path);

/*	Return pointer to filename part of path */
const char *
OS_GetFileNamePtr(const char *path);

/*	Compress path + name into buf, which size bytes long,
	by inserting '...' in the middle */
char *
OS_CompactPaths(char *buf, const char *path, const char *name, int size);

/*	make OSSpec from a path and filename */
OSError
OS_MakeSpec2(const char *path, const char *filename, OSSpec *spec);

/*	make OSSpec given a path and possibly relative path (which may be NULL);
	if filename is relative and noRelative is set, or if filename is full path, 
	then ignore 'path'. */
enum { mswp_noRelative = true };
OSError
OS_MakeSpecWithPath(const OSPathSpec *path, const char *filename, bool noRelative, OSSpec *spec);

/*	change extension of a name; do not exceed OS_NAMESIZE */
OSError
OS_NameSpecChangeExtension(OSNameSpec *spec, char *ext, bool append);

/*	set the extension of a name; if ext begins with '.', append the extension, else replace it */
OSError
OS_NameSpecSetExtension(OSNameSpec *spec, char *ext);

/*	make a relative filepath from the spec from cwd;
	if cwd is NULL, use actual cwd */
char *
OS_SpecToStringRelative(const OSSpec *spec, const OSPathSpec *cwd, char *path, int size);

#define OS_SpecToStringRelative1(spec) \
	OS_SpecToStringRelative(spec, NULL, STSbuf, OS_PATHSIZE)
#define OS_SpecToStringRelative2(spec,buf) \
	OS_SpecToStringRelative(spec, NULL, buf, OS_PATHSIZE)

/*	Search for a file in a list; if not found, returns error.
	If plist is null, searches in current working directory.
*/
OSError	
OS_FindFileInPath(const char *filename, const char *plist, OSSpec *spec);

/*	Search for a place to create file in a list; 
	if not possible, returns error */
OSError	
OS_CreateFileInPath(const char *filename, const char *plist, OSSpec *spec, OSFileType *type);

/*	Search for an executable using the OS standards;
	filename should have appropriate extension if necessary;
	if relative path or not found, make spec in CWD  */
OSError	
OS_FindProgram(const char *filename, OSSpec *spec);

/*	Expand a string by replacing '~' with the home directory
	and $xxx with the value of environment variable 'xxx' */
char *
OS_PathExpand(const char *path);

/*	Copy a handle. */
OSError
OS_CopyHandle(OSHandle *hand, OSHandle *copy);

/*	Append data to a handle. */
OSError
OS_AppendHandle(OSHandle *hand, void *data, OSSize len);

typedef struct
{
	OSSpec		spec;
	OSHandle	hand;
	bool		loaded,changed,writeable;
}	OSFileHandle;

/*	Create a new file handle from a given spec;
	if src is non-NULL, copy this handle (don't link) */
OSError	
OS_NewFileHandle(OSSpec *spec, OSHandle *src, 
				bool writeable, OSFileHandle *hand);

/*	Lock a file handle into memory  */
OSError	
OS_LockFileHandle(OSFileHandle *hand, void **ptr, OSSize *size);

/*	Unlock file handle */
OSError	
OS_UnlockFileHandle(OSFileHandle *hand, void *ptr);

/*	Dispose file handle; 
	this guarantees that changes are flushed */
OSError	
OS_FreeFileHandle(OSFileHandle *hand);

/*	Get spec from the file handle */
void	
OS_GetFileHandleSpec(OSFileHandle *hand, OSSpec *spec);


