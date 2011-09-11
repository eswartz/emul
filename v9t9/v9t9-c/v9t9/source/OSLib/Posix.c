/*
  Posix.c						-- operating system interface for POSIX

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
 *	Operating system library for POSIX-type systems.
 *
 *	The define POSIX_FS must be defined.  Optionally, BEWORKS_FS
 *	and QNX_FS may modify behavior.
 *
 *	This module handles interaction with the operating-system specific
 *	intricacies with file manipulation, memory management, process
 *	spawning, etc.
 *
 */

#if defined(MAC_FS) || defined(WIN32_FS)
#error Wrong module!
#endif

#undef DEBUG

#include "OSLib.h"

#include <string.h>
#include <errno.h>
#include <sys/stat.h>
#include <stdio.h>
#include <unistd.h>
#ifdef __WATCOMC__
#include <process.h>
#endif
#include <signal.h>
#include <string.h>
#include <ctype.h>
#include <fcntl.h>

#include <fcntl.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <dirent.h>
#include <utime.h>

#include <stdlib.h>
#include <assert.h>

#if defined(UNDER_UNIX)
#include <sys/time.h>
#include <sys/times.h>
#endif

#if __linux__ || __CYGWIN32__
#include <sys/vfs.h>
#endif

#if defined(UNDER_BEWORKS)
#include <kernel/fs_attr.h>		/* file attributes */
#include <kernel/image.h>		/* shared library add-ons */
#include <kernel/OS.h>			/* OS utils, namely, areas */
#define MMAP_BE		1
#endif

#if defined(UNDER_QNX)
#include <sys/mman.h>			/* only shared-memory mmap()ing */
#define MMAP_QNX	1
#endif

#if defined(UNDER_UNIX)
#include <dlfcn.h>				/* shared libraries */
#include <sys/mman.h>			/* memory mapping */
#define MMAP_POSIX	1
#endif

#define OS_DLERR	(-ENOENT)	/* generic shared lib error */
static const char *lastdlerr = NULL;	/* buffer for message */

#if DEBUG
#define ASSERT(x) assert(x)
#else
#define ASSERT(x)
#endif

/*	get error text for an OSError */
char
           *
OS_GetErrText(OSError err)
{
	if (err == OS_DLERR) {
		if (lastdlerr == NULL) {	/* d'oh, never got error */
			return "Shared library function failed";
		} else
			return (char *) lastdlerr;
	} else
		return strerror(err);
}

/*********************/

/*	Initialize C program context  */
OSError
OS_InitProgram(int *argc, char ***argv)
{
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


static char intbuf[OS_PATHSIZE];

#define GETSPECSTR(sp,bf) if (OS_SpecToString(sp, bf, sizeof(bf))==NULL) return OS_FNTLERR
#define GETPATHSPECSTR(sp,bf) if (OS_PathSpecToString(sp, bf, sizeof(bf))==NULL) return OS_FNTLERR

#if !defined(BEWORKS_FS)
OSFileType  OS_TEXTTYPE = { 0666 };
#else
OSFileType  OS_TEXTTYPE = { 0666, "text/plain" };
#endif

/*	create a new file, overwrite an old one if existing */
OSError
OS_Create(const OSSpec * spec, OSFileType * type)
{
	int         h;

	GETSPECSTR(spec, intbuf);
	h = open(intbuf, O_CREAT | O_TRUNC, type->perm);
	if (h < 0)
		return errno;
	else {
		close(h);
		// don't return error if this fails
		// -- msdos fs can't change permissions and returns error here
		OS_SetFileType(spec, type);
		return OS_NOERR;
	}
}


/*	tell if a file exists */
OSError
OS_Status(const OSSpec * spec)
{
	struct stat st;

	GETSPECSTR(spec, intbuf);
	return stat(intbuf, &st) ? errno : OS_NOERR;
}

/*  get type of a file */

#if !defined(BEWORKS_FS)

OSError
OS_GetFileType(const OSSpec * spec, OSFileType * type)
{
	struct stat st;

	GETSPECSTR(spec, intbuf);
	if (stat(intbuf, &st) < 0)
		return errno;
	else {
		type->perm = st.st_mode;
		return OS_NOERR;
	}
}

#else

OSError
OS_GetFileType(const OSSpec * spec, OSFileType * type)
{
	struct stat st;
	int         h;
	char        attr[256];

	GETSPECSTR(spec, intbuf);
	if ((h = open(intbuf, O_RDWR)) < 0)
		return errno;

	if (fstat(h, &st) < 0)
		return errno;

	if (fs_read_attr(h, "BEOS:TYPE", 'MIMS', 0,
					 type->mime, sizeof(type->mime)) < 0)
		return errno;

	close(h);

	type->perm = st.st_mode;
	strcpy(type->mime, attr);
	return OS_NOERR;
}

#endif

/*  set type for a file */

#if !defined(BEWORKS_FS)

OSError
OS_SetFileType(const OSSpec * spec, OSFileType * type)
{
	int         oldmask;
	int         err;

	GETSPECSTR(spec, intbuf);
	oldmask = umask(0);
	err = chmod(intbuf, type->perm & (~oldmask));
	umask(oldmask);

	return err < 0 ? errno : OS_NOERR;
}

#else

OSError
OS_SetFileType(const OSSpec * spec, OSFileType * type)
{
	int         oldmask;
	int         err;
	int         ref;

	GETSPECSTR(spec, intbuf);

	/*  may be blank  */
	if (type->mime && *type->mime) {
		if ((ref = open(intbuf, O_RDWR)) < 0)
			return errno;

		fs_remove_attr(ref, "BEOS:TYPE");
		if (fs_write_attr(ref, "BEOS:TYPE", 'MIMS', 0,
						  type->mime, strlen(type->mime) + 1) < 0)
			return errno;

		close(ref);
	}

	oldmask = umask(0);
	err = chmod(intbuf, type->perm & (~oldmask)) < 0;
	umask(oldmask);

	return err ? errno : OS_NOERR;
}

#endif

/*  get timestamps of a file */
OSError
OS_GetFileTime(const OSSpec * spec, OSTime * crtm, OSTime * chtm)
{
	struct stat st;

	GETSPECSTR(spec, intbuf);
	if (stat(intbuf, &st) < 0)
		return errno;
	else {
		if (crtm)
			*crtm = st.st_ctime;
		if (chtm)
			*chtm = st.st_mtime;
		return OS_NOERR;
	}
}

/*  set timestamps of a file */
OSError
OS_SetFileTime(const OSSpec * spec, OSTime * crtm, OSTime * chtm)
{
	struct utimbuf buf;
	struct stat st;

	GETSPECSTR(spec, intbuf);

	if (stat(intbuf, &st) < 0)
		return errno;

#warning "Can't set creation time"
	buf.actime = chtm ? *chtm : st.st_atime;
	buf.modtime = crtm ? *crtm : st.st_mtime;

	if (utime(intbuf, &buf) < 0)
		return errno;
	else
		return OS_NOERR;
}

/*	modify protection on a file */
OSError
OS_ModifyProtection(const OSSpec * spec, bool protect)
{
	struct stat st;

	GETSPECSTR(spec, intbuf);
	if (stat(intbuf, &st) < 0)
		return errno;

	if (chmod(intbuf, (st.st_mode & ~S_IREAD) | (protect ? 0 : S_IREAD)) < 0)
		return errno;
	else
		return OS_NOERR;
}

/*	get protection on a file */
OSError
OS_CheckProtection(const OSSpec *spec, bool *is_protected)
{
	struct stat st;

	GETSPECSTR(spec, intbuf);
	if (stat(intbuf, &st) < 0)
		return errno;

	*is_protected = (st.st_mode & ~S_IREAD) == 0;
	return OS_NOERR;
}


/*	get disk space info */
OSError
OS_GetDiskStats(const OSPathSpec * spec,
				OSSize * blocksize, OSSize * total, OSSize * free)
{
#if __linux__ || __CYGWIN32__
	struct statfs stf;

	GETPATHSPECSTR(spec, intbuf);
	if (statfs(intbuf, &stf) < 0)
		return errno;

	*blocksize = stf.f_bsize;
	*total = stf.f_blocks;
	*free = stf.f_bfree;
	return OS_NOERR;
#else
#error
#endif
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	open an existing file */
OSError
OS_Open(const OSSpec * spec, OSOpenMode mode, OSRef * ref)
{
	static int  modetrans[] =
		{ O_RDONLY, O_WRONLY, O_RDWR, O_APPEND | O_WRONLY };

	GETSPECSTR(spec, intbuf);
	*ref = open(intbuf, modetrans[mode]);
	if (*ref < 0) {
		*ref = -1;				/* don't let someone blithely close this handle */
		return errno;
	} else
		return OS_NOERR;
}

/*	write binary data, up to length bytes;
	length==0 can extend file;
	update length;
	error indicates serious failure */
OSError
OS_Write(OSRef ref, void *buffer, OSSize * length)
{
	struct stat st;
	size_t      pos;

	if (fstat(ref, &st) < 0)
		return errno;

	pos = lseek(ref, 0, SEEK_CUR);
	if (pos > st.st_size && *length == 0) {
		lseek(ref, -1, SEEK_CUR);
		if (write(ref, "\0", 1) != 1) {
			*length = 0;
			return errno;
		}
	}

	*length = write(ref, buffer, *length);
	if ((signed) *length < 0) {
		return errno;
	} else
		return OS_NOERR;
}

/*	read binary data, up to length bytes;
	update length;
	error indicates serious failure.  */
OSError
OS_Read(OSRef ref, void *buffer, OSSize * length)
{
	*length = read(ref, buffer, *length);
	if ((signed) *length < 0)
		return errno;
	else
		return OS_NOERR;
}

/*	seek a file;
	illegal seek is revealed by next write or read;
	error indicates serious failure.  */
OSError
OS_Seek(OSRef ref, OSSeekMode how, OSPos offset)
{
	static int  howtrans[] = { SEEK_CUR, SEEK_SET, SEEK_END };

	return lseek(ref, offset, howtrans[how]) < 0 ? errno : OS_NOERR;
}

/*	tell file position */
OSError
OS_Tell(OSRef ref, OSPos * offset)
{
	*offset = lseek(ref, 0L, SEEK_CUR);
	return (*offset < 0) ? errno : OS_NOERR;
}

/*	close a file */
OSError
OS_Close(OSRef ref)
{
	if (ref == -1)
		return EBADF;
	else
		return close(ref) ? errno : OS_NOERR;
}

/*  get length of a file;
	return error if directory or not found */
OSError
OS_GetSize(OSRef ref, OSSize * length)
{
	struct stat st;

	if (fstat(ref, &st) < 0)
		return errno;
	else {
		*length = st.st_size;
		return OS_NOERR;
	}
}


/*  set length of a file;
	return error if directory or not found */
#if !defined(__WATCOMC__)

OSError
OS_SetSize(OSRef ref, OSSize length)
{
	OSSize sz;
	OSPos pos;
	OSError err;
	char foo[512] = {0};

	err = OS_GetSize(ref, &sz);
	if (err != OS_NOERR)
		return err;

	err = OS_Tell(ref, &pos);
	if (err != OS_NOERR)
		return err;

	if (sz >= length) {
		return ftruncate(ref, length) ? errno : OS_NOERR;
	}

	// extend file (ftruncate is not guaranteed to work)

	err = OS_Seek(ref, OSSeekEnd, 0);
	if (err != OS_NOERR)
		return err;

	while (sz < length) {
		OSSize len = sizeof(foo);
		err = OS_Write(ref, foo, &len);
		if (err != OS_NOERR || len < sizeof(foo))
			break;
		sz += len;
	}

	OS_Seek(ref, OSSeekAbs, pos);
	return err;
}

#else

OSError
OS_SetSize(OSRef ref, OSSize length)
{
	return chsize(ref, length) ? errno : OS_NOERR;
}

#endif



/**********************************/

/*	delete a file */
OSError
OS_Delete(const OSSpec * spec)
{
	GETSPECSTR(spec, intbuf);
	return unlink(intbuf) ? errno : OS_NOERR;
}

/*	rename a file */
OSError
OS_Rename(const OSSpec * oldspec, const OSSpec * newspec)
{
	char        newbuf[OS_PATHSIZE];

	GETSPECSTR(newspec, newbuf);
	GETSPECSTR(oldspec, intbuf);
	return rename(intbuf, newbuf) ? errno : OS_NOERR;
}

/*	make directory */
OSError
OS_Mkdir(const OSSpec * spec)
{
	GETSPECSTR(spec, intbuf);
	return mkdir(intbuf, 0777) ? errno : OS_NOERR;
}

/*	remove directory */
OSError
OS_Rmdir(const OSPathSpec * spec)
{
	GETPATHSPECSTR(spec, intbuf);
	return rmdir(intbuf) ? errno : OS_NOERR;
}

/*	change directory */
OSError
OS_Chdir(const OSPathSpec * spec)
{
	GETPATHSPECSTR(spec, intbuf);
	return chdir(intbuf) ? errno : OS_NOERR;
}

/*	get current working directory */
OSError
OS_GetCWD(OSPathSpec * spec)
{
	if (getcwd(spec->s, OS_PATHSIZE) == NULL)
		return errno;
	else {
		char       *ptr = spec->s + strlen(spec->s);

		if (*(ptr - 1) != '/')
			strcpy(ptr, "/");
		return OS_NOERR;
	}
}

/*	spawn a subprocess */
extern char **environ;

OSError
OS_Execute(const OSSpec * spec, char **argv, char **envp,
		   const char *stdoutfile, const char *stderrfile, int *exitcode)
{
	int         svstdout, svstderr;
	pid_t       kidpid;
	int         status;

	/*  Unix magic:  file descriptors for stdout/stderr are always
	   1 and 2, and are always allocated in order starting from the
	   lowest unopened descriptor.  This code duplicates the current
	   stdout/stderr (as needed), and immediately opens new files,
	   which become the new stdout/stderr for the child.  

	   dup2 duplicates a descriptor into a specific target
	   (probably not necessary, but safer) */

	if (stdoutfile) {
		svstdout = dup(1);
		close(1);
		if (open(stdoutfile, O_WRONLY | O_CREAT | O_TRUNC, 0666) < 0) {
			status = errno;
			dup2(svstdout, 1);
			close(svstdout);
			return status;
		}
	}

	if (stderrfile) {
		svstderr = dup(2);
		close(2);
		if (open(stderrfile, O_WRONLY | O_CREAT | O_TRUNC, 0666) < 0) {
			status = errno;
			dup2(svstderr, 2);
			close(svstderr);
			return status;
		}
	}

	kidpid = fork();
	if (!kidpid) {				/* kid running */
		if (execve(argv[0],
				   (char *const *) argv,
				   (char *const *) (envp && *envp ? envp : environ)) < 0)
			exit(-1);			/* signal failure */

		/* can't get here */
		return EINVAL;
	} else {
		OSError     err;

		if (stdoutfile) {
			dup2(svstdout, 1);
			close(svstdout);
		}

		if (stderrfile) {
			dup2(svstderr, 2);
			close(svstderr);
		}

		*exitcode = 0;

		err = waitpid(kidpid, &status, 0) <= 0 ? errno : OS_NOERR;

		if (WIFEXITED(status))
			*exitcode = WEXITSTATUS(status);

		if (WIFSIGNALED(status))
			*exitcode = -WTERMSIG(status);

		return err;
	}
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	tell if a filepath is legal for filesystem;
	call after OS_CanonPath if necessary */
OSError
OS_IsLegalPath(const char *path)
{
	const char *scan = path;
	int         pthlen = 0, fnlen = 0;

	while (*scan) {
		if (*scan == '/')
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
	return (*path == '/');
}

#if !defined(QNX_FS) && !defined(BEWORKS_FS)

/*	skip a volume in a fullpath */
const char *
OS_GetDirPtr(const char *path)
{
	ASSERT(*path == '/');
	return path;
}

#elif defined(QNX_FS)

/*	skip a volume in a fullpath '/' or '//node/[path/]' */
const char *
OS_GetDirPtr(const char *path)
{
	const char *ptr;

	ASSERT(*path == '/');
	if (*(path + 1) == '/' && *(path + 2) != '/') {	/*  network path?  */
		ptr = strchr(path + 3, '/');
		if (ptr == NULL)
			ptr = path + strlen(path);	/* just "//1" */
	} else
		ptr = path;

	return ptr;
}


#elif defined(BEWORKS_FS)

/*	skip a volume in a fullpath '/[vol]' or '/' */
const char *
OS_GetDirPtr(const char *path)
{
	struct stat st, stdotdot;
	char        vn[OS_VOLSIZE];
	const char *ptr;

	ptr = strchr(path + 1, '/');
	if (ptr != NULL && (ptr - path < OS_VOLSIZE)) {
		strncpy(vn, path, ptr - path);
		vn[ptr - path] = 0;

		/*  Compare devices of root and possible volume */
		stat("/", &st);
		if (stat(vn, &stdotdot) == 0) {
			if (st.st_dev != stdotdot.st_dev)
				return ptr;
		}
	}
	return path;
}

#endif

/*	compact a full path; if dst is NULL, overwrite src in place */
static int
OS_CompactPath(char *src, char *dst)
{
	char        buf[OS_PATHSIZE], *bptr;
	char       *to;
	const char *from, *start;

	ASSERT(OS_IsFullPath(src));

#if !defined(BEWORKS_FS)
	start = OS_GetDirPtr(src);
#else
	start = src;				/* it's okay to do /boot/../volume */
#endif

	if (dst == NULL)
		bptr = buf;
	else
		bptr = dst;

	strncpy(bptr, src, start - src);
	bptr += (start - src);

	from = start;
	to = bptr;

	while (*from) {
		const char *brk;

		brk = from + 1;
		while (*brk && *brk != '/')
			brk++;

		if (brk - from == 1)	/* eliminate '//' */
			from = brk;			/* skip path break */
		else {
			if (brk - from == 2 && from[1] == '.')
				from = brk;
			else /* eliminate ".." and previous directory */ 
				if (brk - from == 3
					&& from[1] == '.'
					&& from[2] == '.') {
				if (to > bptr) {
					do
						to--;
					while (to >= bptr && *to != '/');
				}
				from = brk;
			} else				/* copy */
				while (from < brk)
					*to++ = *from++;

		}
	}

	if (to == bptr || *(from - 1) == '/')
		*to++ = '/';			/* ended at directory */

	*to = 0;					/* end string */

#if 0

	// Bad idea.  User might want "-I/" and then use "#include "boot/home/..."

#if defined(BEWORKS_FS)
	/*
	   If this refers to "/", make it "/boot/"
	 */
	if (*bptr == '/' && *(bptr + 1) == 0)
		strcpy(bptr, "/boot/");

#endif
#endif

	if (dst == NULL)
		strcpy(src, buf);

	return OS_NOERR;
}


/*	compare paths */
int
OS_EqualPath(const char *a, const char *b)
{
	return (strcmp(a, b) == 0);
}

/*************************************/
#if 0
#pragma mark -
#endif

/*	canonicalize a filepath for host; if dst is NULL, overwrite src in place */
OSError
OS_CanonPath(char *src, char *dst)
{
	int idx, out;
		
	if (strlen(src) > OS_MAXPATHLEN)
		return OS_FNTLERR;

	/*  We don't change the size of the string, so this is okay  */
	if (dst == NULL)
		dst = src;

	/*	Probably the only weird thing we'll see is DOS paths  */	
	idx = out = 0;

	/*	assume C: is /C/ */
	if (isalpha(src[0]) && src[1] == ':') {
		dst[out++] = '/';
		dst[out++] = toupper(src[0]);
		dst[out++] = '/';
		idx += 2;
	}
	while (src[idx])
	{
		if (src[idx] == '\\')
			dst[out] = '/';
		else
			dst[out] = src[idx];
		idx++;
		out++;
	}			
	dst[out] = 0;
			
	return OS_NOERR;
}


/*	make OSSpec from a path; tell what kind it is */
OSError
OS_MakeSpec(const char *path, OSSpec *spec, bool *isfile)
{
	char tmp[OS_PATHSIZE];
	struct stat st;
	char *ptr;
	int len;
	OSError err;
	
	*spec->path.s = *spec->name.s = 0;
	
	if ((err=OS_CanonPath((char*)path, tmp))!=OS_NOERR)
		return err;
	
	/*	Prepend cwd if needed */	
	if (!OS_IsFullPath(tmp))
	{
		char *end;
		char orig[OS_PATHSIZE];
		
		strcpy(orig, tmp);
		if (getcwd(tmp, OS_PATHSIZE)==NULL)
			return errno;
		
		end = tmp + strlen(tmp) - 1;
		if (*end!='/')
		{
			*++end='/';
			*++end=0;
		}
		
		if (strlen(orig) + (end - tmp) >= OS_PATHSIZE)
			return OS_FNTLERR;
			
		strcpy(end, orig);
	}
	else
	{
		if (strlen(tmp) >= OS_PATHSIZE)
			return OS_FNTLERR;
	}

	if ((err=OS_CompactPath(tmp, NULL))!=OS_NOERR)
		return err;
	
	if ((err=OS_IsLegalPath(tmp))!=OS_NOERR)
		return err;
		
	/*  Tell if it's a directory reference  */
	if (stat(tmp, &st)==0)
	{
		ptr = tmp + strlen(tmp);
		
		if (*(ptr-1)=='/')
			ptr--;
			
		if (S_ISDIR(st.st_mode))
		{
			if (isfile) 
				*isfile = false;
			*ptr++ = '/';
		}
		else
		{
			if (isfile) 
				*isfile = true;
		}
				
		*ptr=0;
		
	}
	else
	if (errno != ENOENT)
		return errno;
	else
		if (isfile)
			*isfile = true;
	
	ptr = strrchr(tmp, '/')+1;
	
	len = ptr-tmp;
	if (len >= OS_PATHSIZE)
	{
		*spec->path.s = 0;
		return OS_FNTLERR;
	}
		
	memcpy(spec->path.s, tmp, len);
	spec->path.s[len] = 0;		// truncate
	
	len = strlen(ptr);
	if (len >= OS_NAMESIZE)
	{
		*spec->name.s = 0;
		return OS_FNTLERR;
	}
	memcpy(spec->name.s, ptr, len);
	spec->name.s[len] = 0;

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

/*	make OSPathSpec from a volume and dir */
OSError
OS_MakePathSpec(const char *vol, const char *dir, OSPathSpec * spec)
{
	bool        isfile;
	OSSpec      tmp;
	OSError     err;
	char        path[OS_PATHSIZE];
	int			len;
	
	if ((vol ? strlen(vol) : 0) + (dir ? strlen(dir) : 0) + 2 > sizeof(path))
		return OS_FNTLERR;

	sprintf(path, "%s%s", vol ? vol : "", dir ? dir : "");

	err = OS_MakeSpec(path, &tmp, &isfile);
	len = strlen(tmp.path.s);

	if (err != OS_NOERR) {
			/* ensure that path is legal */
		if (len+1 < OS_PATHSIZE)
			memcpy(spec->s+len, "\\", 2);
		else 
			memcpy(spec->s+OS_PATHSIZE-2, "\\", 2);
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
	if (strchr(name, '/') != NULL)
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
	strcpy(spec->s, "/");
	return OS_NOERR;
}


/********************************************/

/*
	For the OS_xxxToString functions, the string buffer
	and its maximum size are passed.  If the output is too
	big for the buffer, NULL is returned.  If the
	buffer is given as NULL, the buffer is malloc()ed.
*/

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
	return (strcmp(a->s, b->s) == 0);
}

/*	compare OSNameSpecs */
int
OS_EqualNameSpec(const OSNameSpec * a, const OSNameSpec * b)
{
	return (strcmp(a->s, b->s) == 0);
}

#if 0
#pragma mark -
#endif


/*	tell if OSSpec is a directory */
int
OS_IsDir(const OSSpec * spec)
{
	struct stat st;

	if (OS_SpecToString(spec, intbuf, sizeof(intbuf)) == NULL)
		return 0;

	if (stat(intbuf, &st) < 0)
		return 0;
	else
		return S_ISDIR(st.st_mode);
}

/*	tell if OSSpec is a file */
int
OS_IsFile(const OSSpec * spec)
{
	struct stat st;

	if (OS_SpecToString(spec, intbuf, sizeof(intbuf)) == NULL)
		return 0;

	if (stat(intbuf, &st) < 0)
		return 0;
	else
		return !S_ISDIR(st.st_mode);
}

/*	tell if OSSpec is a softlink */
int
OS_IsLink(const OSSpec * spec)
{
	struct stat st;
	char       *ptr;

	if (OS_SpecToString(spec, intbuf, sizeof(intbuf)) == NULL)
		return 0;

	ptr = intbuf + strlen(intbuf) - 1;

	if (*ptr == '/')
		*ptr = 0;

	if (lstat(intbuf, &st) < 0)
		return 0;
	else
		return S_ISLNK(st.st_mode);
}

/*	resolve a [soft] link / alias */
OSError
OS_ResolveLink(const OSSpec * link, OSSpec * target)
{
	char        fn[OS_NAMESIZE];
	char        path[OS_PATHSIZE];
	int         len;

	if (OS_SpecToString(link, intbuf, sizeof(intbuf)) == NULL)
		return OS_FNTLERR;

	/*  does not null-terminate string */
	len = readlink(intbuf, fn, sizeof(fn));

	if (len < 0)
		return errno;
	else
		fn[len] = 0;

	sprintf(path, "%s%s", (*fn != OS_PATHSEP) ? link->path.s : "", fn);

	return OS_MakeSpec(path, target, NULL);
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	open a directory for reading */
OSError
OS_OpenDir(const OSPathSpec * spec, OSDirRef * ref)
{
	DIR        *dptr;

	GETPATHSPECSTR(spec, intbuf);
	dptr = opendir(intbuf);
	if (dptr == NULL)
	{
		*ref->path.s = 0;
		ref->dir = (DIR *) 0L;
		return errno;
	}

	ref->path = *spec;
	ref->dir = (DIR *) dptr;
	return OS_NOERR;
}

/*	read an entry from a directory;
	don't return "." or "..";
	return error when end-of-directory reached */
OSError
OS_ReadDir(OSDirRef * ref, OSSpec * spec, char *filename, bool * isfile)
{
	struct dirent *de;
	char        fn[OS_PATHSIZE];
	OSError     err;
	int         len;

	if (ref->dir == 0L)
		return OS_FNFERR;

	do {
		de = readdir((DIR *) ref->dir);
		if (de == NULL)
			return OS_FNFERR;
	} while (strcmp(de->d_name, ".") == 0 || strcmp(de->d_name, "..") == 0 ||
			 strlen(ref->path.s) + strlen(de->d_name) >= OS_PATHSIZE);

	len = strlen(ref->path.s);
	strncpy(fn, ref->path.s, OS_PATHSIZE - 1);
	if (len < OS_PATHSIZE) {
		strncpy(fn + len, de->d_name, OS_PATHSIZE - 1 - len);
		fn[OS_PATHSIZE - 1] = 0;
	} else
		return OS_FNTLERR;

	strncpy(filename, de->d_name, OS_NAMESIZE - 1);
	filename[OS_NAMESIZE - 1] = 0;

	err = OS_MakeSpec(fn, spec, isfile);

	return err;
}

/*	close directory */
OSError
OS_CloseDir(OSDirRef * ref)
{
	if (ref->dir)
		return closedir((DIR *) ref->dir);
	else
		return OS_NOERR;
}


/*************************************/
#if 0
#pragma mark -
#endif

/*	return time in milliseconds */
unsigned long
OS_GetMilliseconds(void)
{
#if !defined(BUILDHOST_BEWORKS)
	struct tms  tms;

	return times(&tms) * 1000 / CLOCKS_PER_SEC;
#else
	return system_time() / 1000;
#endif
}

/*	return current time */
void
OS_GetTime(OSTime * tm)
{
	time(tm);
}

#if 0
#pragma mark -
#endif


/*	allocate a memory handle */

enum { OSMemDelta = 256 };

OSError
OS_NewHandle(OSSize size, OSHandle * hand)
{
	hand->addr = NULL;
	hand->used = size;

	/* allow easy growth; never allocate zero bytes */
	hand->size = (size + OSMemDelta) & ~(OSMemDelta - 1);
	hand->addr = (void *) malloc(hand->size);
	if (hand->addr == NULL)
		return OS_MEMERR;
	else
		return OS_NOERR;
}

/*	resize handle  */
OSError
OS_ResizeHandle(OSHandle * hand, OSSize size)
{
	/* never allocate zero bytes */
	OSSize      nsize = (size + OSMemDelta) & ~(OSMemDelta - 1);
	void       *naddr = (void *) realloc(hand->addr, nsize);

	if (naddr == NULL)
		return OS_MEMERR;
	else {
		hand->addr = naddr;
		hand->size = nsize;
		hand->used = size;
		return OS_NOERR;
	}
}

/*	lock handle  */
void       *
OS_LockHandle(OSHandle * hand)
{
	return hand->addr;
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
	if (hand->addr == NULL)
		return OS_MEMERR;

	free(hand->addr);
	hand->used = hand->size = 0;
	hand->addr = NULL;
	return OS_NOERR;
}

/*	get handle size */
OSError
OS_GetHandleSize(OSHandle * hand, OSSize * size)
{
	if (hand->addr != NULL) {
		*size = (OSSize) hand->used;
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
	hand->addr = NULL;
	hand->used = 0;
}

/*	tell whether a handle is valid */
bool
OS_ValidHandle(OSHandle * hand)
{
	return hand != NULL && hand->addr != NULL;
}

/*************************************/
#if 0
#pragma mark -
#endif


/*************************************/
#if 0
#pragma mark -
#endif

#if __linux__
#define SHLIB_DL
#endif

/*	Shared library / DLL routines  */

#if defined(SHLIB_DL)

/*	open a shared library  */
OSError
OS_OpenLibrary(const OSSpec * spec, OSLibrary * lib)
{
	GETSPECSTR(spec, intbuf);
	*lib = dlopen(intbuf, RTLD_NOW);
	lastdlerr = dlerror();
	if (*lib == NULL)
		return OS_DLERR;
	else
		return OS_NOERR;
}

/*	find a symbol in the library */

/*	dlsym() may return NULL for a symbol defined to be NULL...
	only dlerror() can tell if an error really happened.
 */
OSError
OS_GetLibrarySymbol(OSLibrary lib, char *name, void **sym)
{
	*sym = dlsym(lib, name);
	lastdlerr = dlerror();
	if (*sym == NULL && lastdlerr != NULL)
		return OS_DLERR;
	else
		return OS_NOERR;
}

/*	close a shared library */
OSError
OS_CloseLibrary(OSLibrary lib)
{
	int         st;

	st = dlclose(lib);
	lastdlerr = dlerror();
	if (st < 0)
		return OS_DLERR;
	else
		return OS_NOERR;
}


/*****************************/
#if 0
#pragma mark -
#endif


#elif defined(SHLIB_BE)

/*	open a shared library  */
OSError
OS_OpenLibrary(const OSSpec * spec, OSLibrary * lib)
{
	GETSPECSTR(spec, intbuf);
	*lib = load_add_on(intbuf);
	if (*lib >= 0) {
		lastdlerr = NULL;
		return OS_NOERR;
	} else {
		lastdlerr = NULL;
		return OS_FNFERR;
	}
}

/*	find a symbol in the library */
OSError
OS_GetLibrarySymbol(OSLibrary lib, char *name, void **sym)
{
	status_t    st;
	static char dlerrbuf[OS_PATHSIZE + 64];

	st = get_image_symbol(lib, name, B_SYMBOL_TYPE_ANY, sym);
	if (st != B_NO_ERROR) {
		image_info  info;

		if (get_image_info(lib, &info) == B_NO_ERROR) {
			sprintf(dlerrbuf,
					"Symbol '%s' not found in '%s'", name, info.name);
			lastdlerr = dlerrbuf;
		} else {
			sprintf(dlerrbuf,
					"Symbol '%s' not found in shared library", name);
			lastdlerr = dlerrbuf;
		}

		*sym = NULL;
		return OS_DLERR;
	} else {
		lastdlerr = NULL;
		return OS_NOERR;
	}
}

/*	close a shared library */
OSError
OS_CloseLibrary(OSLibrary lib)
{
	status_t    st;

	st = unload_add_on(lib);
	if (st != B_NO_ERROR) {
		lastdlerr = "Error closing shared library";
		return OS_DLERR;
	} else {
		lastdlerr = NULL;
		return OS_NOERR;
	}
}


/************************************/

#if 0
#pragma mark -
#endif

#else

/*	No support for shared libraries */

/*	open a shared library  */
OSError
OS_OpenLibrary(const OSSpec * spec, OSLibrary * lib)
{
	*lib = NULL;
	lastdlerr = "No support for shared libraries";
	return OS_DLERR;
}

/*	find a symbol in the library */
OSError
OS_GetLibrarySymbol(OSLibrary lib, char *name, void **sym)
{
	*sym = NULL;
	lastdlerr = "No support for shared libraries";
	return OS_DLERR;
}

/*	close a shared library */
OSError
OS_CloseLibrary(OSLibrary lib)
{
	lastdlerr = "No support for shared libraries";
	return OS_DLERR;
}

#endif

/*	Thread management */

/*	create a thread  */
OSError
OS_CreateThread(OSThread *th, void *(*routine)(void *), void *arg)
{
	int err;
	pthread_attr_t attr;
	
	if ((err = pthread_attr_init(&attr)) ||
		(err = pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE)) ||
		(err = pthread_attr_setschedpolicy(&attr, SCHED_OTHER)))
	{
		return err;
	}

	/* Somehow, this *always* appears to fail with EINTR
	   under some versions of glibc, though the thread has 
	   been properly created. */

	err = pthread_create(th, &attr, routine, arg);
	if (err != EINTR)
	{
		return err;
	}
	
	return 0;
}

/*	suspend a thread */
OSError
OS_SuspendThread(OSThread th)
{
//	pthread_kill(th, SIGSTOP);
	return 0;
}

/*	resume a thread */
OSError
OS_ResumeThread(OSThread th)
{
//	pthread_kill(th, SIGCONT);
	return 0;
}

/*	wait for a thread to complete */
OSError
OS_JoinThread(OSThread th, void **ret)
{
	return pthread_join(th, ret);
}

/*	kill a thread */
OSError
OS_KillThread(OSThread th, bool force)
{
	int ret;
	if (!force)
		ret = pthread_cancel(th);
	else
		ret = pthread_kill(th, SIGTERM);

	return ret;
}

/*	create a mutex */
OSError
OS_CreateMutex(OSMutex *mx)
{
	return pthread_mutex_init(mx, NULL);
}

/* 	lock a mutex */
OSError
OS_LockMutex(OSMutex *mx)
{
	return pthread_mutex_lock(mx);
}

/*	unlock a mutex */
OSError
OS_UnlockMutex(OSMutex *mx)
{
	return pthread_mutex_unlock(mx);
}

/*	try to lock a mutex */
OSError
OS_TryLockMutex(OSMutex *mx)
{
	return pthread_mutex_trylock(mx);
}

/*	destroy a mutex */
OSError
OS_KillMutex(OSMutex *mx)
{
	return pthread_mutex_destroy(mx);
}

/*	create a semaphore */
OSError
OS_CreateSemaphore(OSSemaphore *sem, int value)
{
	return sem_init(sem, 0 /*pshared*/, value);
}

/* 	increment a semaphore */
OSError
OS_PostSemaphore(OSSemaphore *sem)
{
	return sem_post(sem);
}

/*	wait on a semaphore and decrement it */
OSError
OS_WaitSemaphore(OSSemaphore *sem)
{
	int ret;
	while ((ret = sem_wait(sem))) {
		if (errno != EINTR)
			break;
	}
	return ret ? errno : 0;
}

/*	wait on a semaphore and decrement it */
OSError
OS_TryWaitSemaphore(OSSemaphore *sem)
{
	return sem_wait(sem);
}

/*	read the value of a semaphore */
OSError
OS_ReadSemaphore(OSSemaphore *sem, int *val)
{
	return sem_getvalue(sem, val);
}

/*	destroy a semaphore */
OSError
OS_KillSemaphore(OSSemaphore *sem)
{
	return sem_destroy(sem);
}

/*	create a condition */
OSError
OS_CreateCondition(OSCondition *cd)
{
	pthread_mutex_init(&cd->mutex, 0L);
	pthread_cond_init(&cd->cond, 0L);
	return 0;
}

/* 	trigger a condition */
OSError
OS_TriggerCondition(OSCondition *cd)
{
	int ret;
	if ((ret = pthread_mutex_lock(&cd->mutex))) return ret;
	if ((ret = pthread_cond_broadcast(&cd->cond))) return ret;
	if ((ret = pthread_mutex_unlock(&cd->mutex))) return ret;
	return 0;
}

/*	wait on a condition */
OSError
OS_WaitCondition(OSCondition *cd)
{
	int ret;
	if ((ret = pthread_mutex_lock(&cd->mutex))) return ret;

	while ((ret = pthread_cond_wait(&cd->cond, &cd->mutex))) {
		if (ret != EINTR)
			break;
	}
	return ret;
}

/*	try to wait on a condition for 'usec' microseconds */
OSError
OS_TimedWaitCondition(OSCondition *cd, int usec)
{
	struct timeval now;
	struct timespec timeout;
	int ret;

	if ((ret = pthread_mutex_lock(&cd->mutex))) return ret;

	gettimeofday(&now, 0L);
	timeout.tv_sec = now.tv_sec + usec / 1000000;
	timeout.tv_nsec = (now.tv_usec + usec % 1000000) * 1000;

	ret = pthread_cond_timedwait(&cd->cond, &cd->mutex, &timeout);
	return ret;
}

/*	done with condition handling (pair with OS_[Timed]WaitCondition) */
OSError
OS_HandledCondition(OSCondition *cd)
{
	int ret;
	if ((ret = pthread_mutex_unlock(&cd->mutex))) return ret;
	return 0;
}

/*	destroy a condition */
OSError
OS_KillCondition(OSCondition *cd)
{
	int ret;
	if ((ret = pthread_cond_destroy(&cd->cond))) return ret;
	if ((ret = pthread_mutex_destroy(&cd->mutex))) return ret;
	return 0;
}

