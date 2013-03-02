/*
  MacOS.c						-- operating system interface for MacOS classic

  (c) 1994-2011 Edward Swartz

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
 *	Operating system library for Macintosh.
 *
 *	This module handles interaction with the operating-system specific
 *	intricacies with file manipulation, memory management, process
 *	spawning, etc.
 *
 */

#if (defined(POSIX_FS) || defined(WIN32_FS)) && !defined(MAC_FS)
#error Wrong module!
#endif

#include "OSLib.h"
#include "StringUtils.h"

#include <string.h>
#include <stdio.h>
#include <string.h>
#if __MWERKS__
#include <alloca.h>
#else
#include <malloc.h>
#endif
#include <stdlib.h>
#include <ctype.h>

#include <assert.h>

/* Universal headers */
#include <MacTypes.h>
#include <Files.h>
#include <Aliases.h>
#include <Errors.h>
#include <Script.h>
#include <TextUtils.h>
#include <Gestalt.h>
#include <LowMem.h>

#if defined(UNDER_MPW)

/*  MPW includes */
#include <ErrMgr.h>
#include <CursorCtl.h>
#include <IntEnv.h>
#endif

//#define DEBUG 1

#if DEBUG
#define ASSERT(x) 	assert(x)
#else
#define ASSERT(x)
#endif

/*	System state variables  */
static Boolean systemHandles;
static Boolean aliasAvail;


/*  I wrote these to learn the OS  */

static      OSErr
CanonicalizeIfDirectory(FSSpec * fss)
{
	CInfoPBRec  pb;
	OSErr       err;

	pb.hFileInfo.ioNamePtr = fss->name;
	pb.hFileInfo.ioVRefNum = fss->vRefNum;
	pb.hFileInfo.ioFDirIndex = 0;	/* get file info */
	pb.hFileInfo.ioDirID = fss->parID;
	err = PBGetCatInfoSync(&pb);
	if (err == noErr && (pb.hFileInfo.ioFlAttrib & 0x10)) {
		fss->parID = pb.dirInfo.ioDrDirID;
		*fss->name = 0;
	} else if (err == paramErr)
		err = noErr;

	return err;
}

#if !defined(UNDER_MPW)

static      OSErr
ResolveFSSpec(short vRefNum, long dirID, Str255 name,
			  Boolean resolveLeafAlias, FSSpec * fss,
			  Boolean * isFolder, Boolean * hadAlias, Boolean * leafIsAlias)
{
	int         idx;
	OSErr       err;

	*isFolder = 0;
	*hadAlias = 0;

	if (!aliasAvail) {
		*leafIsAlias = 0;
		*hadAlias = 0;
		*isFolder = 0;
		err = FSMakeFSSpec(vRefNum, dirID, name, fss);
		if (*fss->name) {
			err = CanonicalizeIfDirectory(fss);
			if (err == noErr && *fss->name == 0)
				*isFolder = 1;
		} else if (err == noErr)
			*isFolder = 1;
		return err;
	} else {
		idx = 0;
		while (idx < name[0]) {
			Str255      outname;

			*leafIsAlias = 0;
			outname[0] = 0;

			/*  Get next path component, either a name or a series of ':'s */
			if (name[idx + 1] == ':') {
				/*  Skip the first colon */
				idx++;
				while (idx < name[0] && name[idx + 1] == ':')
					outname[++outname[0]] = name[++idx];
			} else
				while (idx < name[0] && name[idx + 1] != ':')
					outname[++outname[0]] = name[++idx];

#ifdef DEBUG
			p2cstr(outname);
			printf("Component is '%s'\n", (char *) outname);
			c2pstr((char *) outname);
#endif

			err = FSMakeFSSpec(vRefNum, dirID, outname, fss);
			if (err != noErr) {
				/*  ugly: it we were passed a full path, this may have
				   failed on the volume.  Need to append ':'.  
				   Unfortunately, this couldn't be done above, because
				   aliased files would trigger an error if the colon
				   were present.
				 */

				if (idx < name[0]) {
					outname[++outname[0]] = ':';
					err = FSMakeFSSpec(vRefNum, dirID, outname, fss);
					if (err != noErr)
						return err;
				} else
					return err;
			}

			/*  See if component is an alias */
			err = ResolveAliasFile(fss, 1, isFolder, leafIsAlias);
			if (err != noErr)
				return err;


			if (*leafIsAlias)
				*hadAlias = 1;

#ifdef DEBUG
			if (*leafIsAlias) {
				p2cstr(fss->name);
				printf("Component was an alias (--> '%s')\n",
					   (char *) fss->name);
				c2pstr((char *) fss->name);
			} else
				printf("Component was not an alias.\n");
#endif

			/*  If it's a directory, go to it */
			if (outname[0]) {
				if (*isFolder && outname[1] != ':')
					err = CanonicalizeIfDirectory(fss);

				vRefNum = fss->vRefNum;
				dirID = fss->parID;
			}

			/*  we should be done if *isFolder was not set */
			if (!*isFolder && idx < name[0])
				return dirNFErr;
		}
	}
	return err;
}


static      OSErr
MakeResolvedFSSpec(short vRefNum, long dirID, Str255 name, FSSpec * fss,
				   Boolean * isFolder, Boolean * hadAlias,
				   Boolean * leafIsAlias)
{
	OSErr       err;
	CInfoPBRec  pb;

	*hadAlias = 0;
	*leafIsAlias = 0;

	/*  hope for easy case  */
	err = FSMakeFSSpec(vRefNum, dirID, name, fss);

	if (err == noErr) {
		if (aliasAvail) {
#ifdef DEBUG
			printf("FSMakeFSSpec succeeded, trying ResolveAliasFile\n");
#endif

			err = ResolveAliasFile(fss, 1, isFolder, leafIsAlias);
			if (err == noErr) {

#ifdef DEBUG
				if (*leafIsAlias)
					printf("Had a leaf alias\n");
#endif

				*hadAlias = *leafIsAlias;
			} else {
#ifdef DEBUG
				printf("Leaf had an invalid alias\n");
#endif
			}
		} else {
			*hadAlias = 0;
		}

		/*  See if it's a directory. */

		pstrcpy(name, fss->name);
		pb.hFileInfo.ioNamePtr = name;
		pb.hFileInfo.ioVRefNum = fss->vRefNum;
		pb.hFileInfo.ioFDirIndex = 0;	/* get file info */
		pb.hFileInfo.ioDirID = fss->parID;
		err = PBGetCatInfoSync(&pb);
		if (err == noErr) {
			if (pb.hFileInfo.ioFlAttrib & 0x10)
				*isFolder = 1;
		}

		return err;
	} else
		/*  try going up the chain... */
	{
		//char  fn[256];

#ifdef DEBUG
		printf("FSMakeFSSpec failed, resolving alias.\n");
#endif

		err =
			ResolveFSSpec(vRefNum, dirID, name, 1, fss, isFolder, hadAlias,
						  leafIsAlias);
		if (err == noErr) {
#ifdef DEBUG
//          printf("MakeResolvedPath returned '%s'\n", fn);
#endif

			//c2pstr(fn);
			//err = FSMakeFSSpec(0, 0, (unsigned char *)fn, fss);
		}
		return err;
	}
}

#endif // !defined(UNDER_MPW)

#if 0
#pragma mark -
#endif


/*	get error text for an OSError */

char
           *
OS_GetErrText(OSError err)
{
	static char errmsg[256];
	char       *ret;

#if defined(BUILDHOST_MPW)
	char       *rep;

	ret = GetSysErrText(err, errmsg);
	rep = strstr(ret, " (OS error");
	// get rid of this; we already append this 
	// to our error message
	if (rep != NULL) {
		*rep = 0;
	}
#else
#warning "Need error message..."
	sprintf(errmsg, "Error #%d", err);
	ret = errmsg;
#endif
	return ret;
}

/*********************/
#if 0
#pragma mark -
#endif


/*	Initialize C program context  */

OSError
OS_InitProgram(int *argc, char ***argv)
{
	long        gestaltReply;

#if !defined(UNDER_MPW)

	/*  Get some command-line arguments  */

	if (argc != NULL && argv != NULL) {
		ccommand(argv);
		*argc = 0;
		while ((*argv)[*argc])
			(*argc)++;
	}

	/*  Don't save the window...  */
	SIOUXSettings.asktosaveonclose = 0;

#else

	InitGraf(&qd.thePort);
	GetDateTime((unsigned long *) &qd.randSeed);

	InitCursorCtl(NULL);
	InitErrMgr("", "", true);

#endif

	systemHandles = false;
	aliasAvail = false;
	InitGraf(&qd.thePort);
	GetDateTime((unsigned long *) &qd.randSeed);

	if (Gestalt(gestaltOSAttr, &gestaltReply) == noErr &&
		(gestaltReply & (1L << gestaltRealTempMemory)))
		systemHandles = true;

	if (Gestalt(gestaltAliasMgrAttr, &gestaltReply) == noErr &&
		(gestaltReply & (1L << gestaltAliasMgrPresent)))
		aliasAvail = true;

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


OSFileType  OS_TEXTTYPE = 'TEXT';

#define FSS(sp) sp->path.vRefNum, sp->path.dirID, sp->name.name

/*	create a new file, overwrite an old one if existing */

OSError
OS_Create(const OSSpec * spec, OSFileType * type)
{
	OSErr       err = HDelete(FSS(spec));

	if (err == noErr || err == fnfErr)
		err = HCreate(FSS(spec), 'CWIE', *type);
	return err;
}

/*	tell if a file exists */

OSError
OS_Status(const OSSpec * spec)
{
	OSErr       err;
	FInfo       fi;

	err = HGetFInfo(FSS(spec), &fi);
	return err;
}


/*  get type of a file */

OSError
OS_GetFileType(const OSSpec * spec, OSFileType * type)
{
	OSErr       err;
	FInfo       fi;

	err = HGetFInfo(FSS(spec), &fi);
	if (err == noErr)
		*type = fi.fdType;
	else
		*type = '????';
	return err;
}

/*  set type for a file */

OSError
OS_SetFileType(const OSSpec * spec, OSFileType * type)
{
	OSErr       err;
	FInfo       fi;

	err = HGetFInfo(FSS(spec), &fi);
	if (err == noErr) {
		fi.fdType = *type;
		err = HSetFInfo(FSS(spec), &fi);
	}
	return err;
}

/*  get timestamps of a file */

OSError
OS_GetFileTime(const OSSpec * spec, OSTime * crtm, OSTime * chtm)
{
	OSErr       err;
	CInfoPBRec  pb;
	Str63       name;

	/* Think reference sez name is overwritten */
	pstrcpy(name, spec->name.name);
	pb.hFileInfo.ioNamePtr = name;
	pb.hFileInfo.ioVRefNum = spec->path.vRefNum;
	pb.hFileInfo.ioDirID = spec->path.dirID;
	pb.hFileInfo.ioFDirIndex = 0;

	if ((err = PBGetCatInfoSync(&pb)) != noErr)
		return err;

	if (crtm)
		*crtm = pb.hFileInfo.ioFlCrDat;
	if (chtm)
		*chtm = pb.hFileInfo.ioFlMdDat;

	return OS_NOERR;
}

/*  set timestamps of a file */

OSError
OS_SetFileTime(const OSSpec * spec, OSTime * crtm, OSTime * chtm)
{
	OSErr       err;
	CInfoPBRec  pb;
	Str63       name;

	/* Think reference sez name is overwritten */
	pstrcpy(name, spec->name.name);
	pb.hFileInfo.ioNamePtr = name;
	pb.hFileInfo.ioVRefNum = spec->path.vRefNum;
	pb.hFileInfo.ioDirID = spec->path.dirID;
	pb.hFileInfo.ioFDirIndex = 0;

	if ((err = PBGetCatInfoSync(&pb)) != noErr)
		return err;

	if (crtm)
		pb.hFileInfo.ioFlCrDat = *crtm;
	if (chtm)
		pb.hFileInfo.ioFlMdDat = *chtm;

	if ((err = PBSetCatInfoSync(&pb)) != noErr)
		return err;

	return OS_NOERR;
}

/*	modify protection on a file */
OSError
OS_ModifyProtection(const OSSpec * spec, bool protected)
{
#error not implemented yet
	return OS_NOERR;
}

/*	get disk space info (total, free are measured in units of blocksize bytes) */
OSError
OS_GetDiskStats(const OSPathSpec * spec,
				OSSize * blocksize, OSSize * total, OSSize * free)
{
#error not implemented yet
	*blocksize = 256;
	*total = 90;
	*free = 0;
	return OS_NOERR;
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	open an existing file */

OSError
OS_Open(const OSSpec * spec, OSOpenMode mode, OSRef * ref)
{
	static int  modetrans[] = { fsRdPerm, fsWrPerm,
		fsRdWrPerm, fsWrPerm
	};
	OSErr       err;

	err = HOpenDF(FSS(spec), modetrans[mode], ref);

	if (err != noErr) {
		*ref = -1;
		return err;
	} else {
		if (mode == OSAppend)
			err = SetFPos(*ref, fsFromLEOF, 0L);
		return err;
	}
}

/*	write binary data, up to length bytes;
	length==0 can extend file;
	update length;
	error indicates serious failure */

/*	On Mac, can't even seek past EOF.  
	OS_Seek handles extending the file. */

OSError
OS_Write(OSRef ref, void *buffer, OSSize * length)
{
	return FSWrite(ref, (long *) length, buffer);
}

/*	read binary data, up to length bytes;
	update length;
	error indicates serious failure.  */

OSError
OS_Read(OSRef ref, void *buffer, OSSize * length)
{
	OSErr       err;

	err = FSRead(ref, (long *) length, buffer);
	// ignore EOF; in Posix-land we just return length < requested
	return (err != eofErr) ? err : noErr;
}

/*	seek a file;
	illegal seek is revealed by next write or read;
	error indicates serious failure.  */

/*  Whoops, on the Mac this is different.  You can't seek past EOF,
	so we ask Posix-ly and extend the file as implied, returning
	an error only when something else happens. */

OSError
OS_Seek(OSRef ref, OSSeekMode how, OSPos offset)
{
	static int  howtrans[] = { fsFromMark, fsFromStart, fsFromLEOF };
	long        length, final, curpos;
	OSErr       err;

	if ((err = GetEOF(ref, &length)) != noErr ||
		(err = GetFPos(ref, &curpos)) != noErr)
		return err;

	switch (how) {
	case OSSeekRel:
		final = curpos + offset;
		break;
	case OSSeekAbs:
		final = 0 + offset;
		break;
	case OSSeekEnd:
		final = length + offset;
		break;
	default:
		return paramErr;
	}

	/*  Don't use SetEOF() since it doesn't provide zeroed blocks */
	while (final > length) {
		static char zeroes[32] = { 0 };
		long        fill;

		fill = final - length;
		if (fill > sizeof(zeroes))
			fill = sizeof(zeroes);

		if ((err = FSWrite(ref, &fill, zeroes)) != noErr || fill == 0)
			return err;

		length += fill;
	}

	return SetFPos(ref, fsFromStart, final);
}

/*	return file pointer */

OSError
OS_Tell(OSRef ref, OSPos * offset)
{
	return GetFPos(ref, offset);
}


/*	close a file */

OSError
OS_Close(OSRef ref)
{
	return FSClose(ref);
}

/*  get length of a file;
	return error if directory or not found */

OSError
OS_GetSize(OSRef ref, OSSize * length)
{
	return GetEOF(ref, (long *) length);
}

/*  set length of a file;
	return error if directory or not found */

OSError
OS_SetSize(OSRef ref, OSSize length)
{
	return SetEOF(ref, length);
}

/**************************************/
#if 0
#pragma mark -
#endif


/*	delete a file */

OSError
OS_Delete(const OSSpec * spec)
{
	return HDelete(FSS(spec));
}

/*	rename a file */

/*	for Mac, we need to allow moving through directories, 
	but not moving through volumes. */

OSError
OS_Rename(const OSSpec * oldspec, const OSSpec * newspec)
{
	/*  Move through directory first if necessary. */

	if (!OS_EqualPathSpec(&oldspec->path, &newspec->path)) {
		CMovePBRec  pb;
		OSErr       err;

		if (oldspec->path.vRefNum != newspec->path.vRefNum)
			return paramErr;

		pb.ioVRefNum = oldspec->path.vRefNum;

		pb.ioDirID = oldspec->path.dirID;
		pb.ioNamePtr = (unsigned char *) oldspec->name.name;
		pb.ioNewDirID = newspec->path.dirID;
		pb.ioNewName = NULL;	/* must specify directory */

		err = PBCatMoveSync(&pb);

		if (err != noErr)
			return err;
	}

	return HRename(newspec->path.vRefNum, newspec->path.dirID,
				   oldspec->name.name, newspec->name.name);
}

/*	make directory */

/*  The user passes the new directory name in the FSSpec. 
	FSMakeFSSpec will do this for us, unless the file already
	exists. */

OSError
OS_Mkdir(const OSSpec * spec)
{
	long        newdirID;
	FSSpec      fss;

	fss.vRefNum = spec->path.vRefNum;
	fss.parID = spec->path.dirID;
	pstrcpy(fss.name, spec->name.name);

	return FSpDirCreate(&fss, smSystemScript, &newdirID);
}

/*	remove directory */

/*	The directory should exist, else spec->name specifies a
	non-directory. */

OSError
OS_Rmdir(const OSPathSpec * spec)
{
	return HDelete(spec->vRefNum, spec->dirID, (unsigned char *) "");
}

/*	change directory */

OSError
OS_Chdir(const OSPathSpec * spec)
{
	WDPBRec     wd;

	wd.ioNamePtr = NULL;
	wd.ioVRefNum = spec->vRefNum;
	wd.ioWDDirID = spec->dirID;
	return PBHSetVolSync(&wd);
}

/*	get current working directory */

OSError
OS_GetCWD(OSPathSpec * spec)
{
	FSSpec      fss;
	OSError     err;

	err = FSMakeFSSpec(0, 0, (unsigned char *) "", &fss);
	if (err != noErr)
		return err;

	err = CanonicalizeIfDirectory(&fss);

	spec->vRefNum = fss.vRefNum;
	spec->dirID = fss.parID;
	return err;
}

/*	spawn a subprocess */

OSError
OS_Execute(const OSSpec * spec, char **argv, char **envp, const char *outname,
		   const char *errname, int *exitcode)
{
	int         idx;

	for (idx = 0; argv[idx]; idx++) {
		bool        esc = strpbrk(argv[idx], "\t\n\r|()\"\',= ") != NULL;

		if (esc)
			printf("\"%s\" ", argv[idx]);
		else
			printf("%s ", argv[idx]);
	}

	if (outname)
		printf("> \"%s\" ", outname);
	if (errname)
		printf("2> \"%s\" ", errname);
	printf("\n");

	*exitcode = 0;
	return noErr;
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	canonicalize a filepath for host; if dst is NULL, overwrite src in place */
static      OSError
OS_CanonPath(char *src, char *dst)
{
	char       *ptr, *dptr;
	char       *vptr;
	int         nds;
	int         looksunix, fnlen;

	if (strlen(src) >= OS_MAXPATHLEN)
		return OS_FNTLERR;

	/*  We can do this since dst is at most the length of src */
	if (dst == NULL)
		dst = src;

	ptr = src;
	dptr = dst;
	vptr = NULL;				/* volume ptr */
	nds = 0;

	/*  First, check for Unix usage as in
	   /dir/dir/name.c  -->  dir:dir:name.c and
	   //node/dir/dir/name.c --> node:dir:dir:name.c */

	looksunix = (strchr(ptr, '/') != NULL && strchr(ptr, ':') == NULL) ||
		strcmp(ptr, ".") == 0;

	if (looksunix) {
		if (*ptr == '/') {
			OSErr       err;
			short       volref;

			do
				ptr++;
			while (*ptr == '/');

			err = GetVol((unsigned char *) dptr, &volref);
			if (err != noErr)
				strcpy(dptr, "Desktop");
			else
				p2cstr((unsigned char *) dptr);

			dptr += strlen(dptr);
			vptr = dptr;
			nds = 0;
		}

		if (strcmp(ptr, ".") != 0)
			*dptr++ = ':';
	}

	/*  Convert slashes to colons and '..' to ':' */

	fnlen = 0;
	while (*ptr) {
		if (looksunix) {
			if (*ptr == '/') {
				if (fnlen > 0)
					*dptr++ = ':';
				fnlen = 0;
				nds++;
			}
				else
				if (fnlen == 0 &&
					*ptr == '.' && *(ptr + 1) == '.' &&
					(*(ptr + 2) == 0 || *(ptr + 2) == '/')) {
				/* some yahoos do "/.."; we can't have Ontology:: */
				if (vptr == NULL || nds > 0) {
					*dptr++ = ':';
					nds--;
				}
				fnlen = 0;
				if (*(ptr + 2))
					ptr += 2;
			}
				else
				if (fnlen == 0 &&
					*ptr == '.' && (*(ptr + 1) == 0 || *(ptr + 1) == '/')) {
				if (*(ptr + 1))
					ptr++;
				fnlen = 0;
			} else {
				*dptr++ = *ptr;
				fnlen++;
			}
		} else
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

	while (*scan) {
		if (fnlen == 0 && *scan == '.')
			return bdNamErr;	/* don't allow device name */

		if (*scan == ':')
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
	/*  a relative path begins with ':' but a bare filename is also relative */
	return (path[0] != ':') && (strchr(path, ':') != NULL);
}


/*	compact a canonical full path; if dst is NULL, overwrite src in place */
static int
OS_CompactPath(char *src, char *dst)
{
	char       *from, *to, *bptr;

	ASSERT(OS_IsFullPath(src));

	from = strchr(src, ':');
	bptr = to = (dst == NULL ? src : dst);

	if (from != NULL) {
		/* from always points to ':' */
		while (*from) {
			char       *brk;

			brk = from + 1;
			while (*brk && *brk != ':')
				brk++;

			if (brk == from + 1) {	/* eliminate "::" */
				if (to > bptr) {
					do
						to--;
					while (to >= bptr && *to != ':');
				}
				from = brk + 1;
			} else				/* copy */
				while (from < brk)
					*to++ = *from++;
		}
	}

	if (to == bptr || from == NULL || *(from - 1) == ':')
		*to++ = ':';			/* ended at volume */

	*to = 0;					/* end string */

	return OS_NOERR;
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	make OSSpec from a path; tell what kind it is */

OSError
OS_MakeSpec(const char *path, OSSpec * spec, bool * isfile)
{
	OSErr       err;
	char        tmp[OS_PATHSIZE];
	FSSpec      fss;
	Boolean     isFolder, hadAlias, leafIsAlias;

	spec->path.vRefNum = spec->path.dirID = 0;
	*spec->name.name = 0;

	/*  Get rid of bad chars */
	if ((err = OS_CanonPath((char *) path, tmp)) != OS_NOERR ||
		(err = OS_IsLegalPath(tmp)) != OS_NOERR)
		return err;
	else {
		c2pstr(tmp);
		err = MakeResolvedFSSpec(0, 0, (unsigned char *) tmp, &fss,
								 &isFolder, &hadAlias, &leafIsAlias);
		if (err == noErr) {
			if (fss.vRefNum == 0 && fss.parID == 0)
				return nsvErr;
			else if (isFolder) {
				/*  canonicalize this: directories always
				   have spec->name==""
				 */

				if (isfile)
					*isfile = false;
				if ((err = CanonicalizeIfDirectory(&fss)) != noErr)
					return err;
			} else if (isfile)
				*isfile = true;
		} else if (err == fnfErr) {
			if (isfile)
				*isfile = true;
			err = OS_NOERR;
		}

		spec->path.vRefNum = fss.vRefNum;
		spec->path.dirID = fss.parID;
		pstrcpy(spec->name.name, fss.name);
	}
	return err;
}

/*	make OSSpec from a path;
	must resolve to a file */
_DLL _PAS   OSError
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

/*	make OSPathSpec from a volume and dir; 
	guaranteed to be a directory */

OSError
OS_MakePathSpec(const char *vol, const char *dir, OSPathSpec * pspec)
{
	OSSpec      spec;
	OSErr       err;
	bool        isfile;
	char        path[OS_PATHSIZE + OS_VOLSIZE];

	if ((vol ? strlen(vol) : 0) + (dir ? strlen(dir) : 0) + 2 > sizeof(path))
		return OS_FNTLERR;

	sprintf(path, "%s%s", vol ? vol : "", dir ? dir : "");

	err = OS_MakeSpec(path, &spec, &isfile);
	*pspec = spec.path;

	if (err != OS_NOERR)
		return err;

	if (isfile)
		return OS_FNIDERR;

	return OS_NOERR;
}

/*	make OSNameSpec from a filename */

OSError
OS_MakeNameSpec(const char *name, OSNameSpec * nspec)
{
	if (strchr(name, ':') != NULL)
		return OS_FIDERR;

	if (strlen(name) > OS_MAXNAMELEN)
		return OS_FNTLERR;

	c2pstrcpy(nspec->name, name);

	return OS_NOERR;
}

/*	return FS root spec */

OSError
OS_GetRootSpec(OSPathSpec * spec)
{
	spec->vRefNum = -1;
	spec->dirID = 2;
	return OS_NOERR;
}

/*	make a full pathname from OSSpec */


char       *
OS_SpecToString(const OSSpec * spec, char *path, int size)
{
	char        thepath[OS_PATHSIZE], thename[OS_NAMESIZE];
	int         plen, nlen;

	if (size == 0)
		size = OS_PATHSIZE;
	if (path == NULL && (path = (char *) malloc(size)) == NULL)
		return NULL;

	if (OS_PathSpecToString(&spec->path, thepath, OS_PATHSIZE) == NULL)
		return NULL;
	if (OS_NameSpecToString(&spec->name, thename, OS_NAMESIZE) == NULL)
		return NULL;

	plen = strlen(thepath);
	nlen = strlen(thename);
	if (plen + nlen >= size) {
		if (plen >= size) {
			nlen = 0;
			plen = size - 1;
		} else {
			nlen = size - plen - 1;
		}
	}
	memcpy(path, thepath, plen);
	memcpy(path + plen, thename, nlen);
	path[plen + nlen] = 0;
	return path;
}

static      OSErr
VolDirToPath(short vRefNum, long dirID, char *path)
{
	CInfoPBRec  pb;
	Str255      name;

	if (dirID > 1) {			/* top of volume */
		OSErr       err;

		pb.dirInfo.ioNamePtr = name;	/* buffer */
		pb.dirInfo.ioVRefNum = vRefNum;
		pb.dirInfo.ioFDirIndex = -1;	/* get dir info */
		pb.dirInfo.ioDrDirID = dirID;

		err = PBGetCatInfoSync(&pb);
		if (err != noErr) {
			strcat(path, "\003???");	/* dead */
			return err;
		} else {
			pstrcharcat(name, ':');

			err = VolDirToPath(vRefNum, pb.dirInfo.ioDrParID, path);
			p2cstr(name);
			strcat(path, (char *) name);
			return err;
		}
	} else {
		*path = 0;
		return noErr;
	}
}

/*	make a path from OSPathSpec */

char       *
OS_PathSpecToString(const OSPathSpec * pspec, char *path, int size)
{
	char        thepath[OS_PATHSIZE];
	int         plen;

	if (size == 0)
		size = OS_PATHSIZE;
	if (path == NULL && (path = (char *) malloc(size)) == NULL)
		return NULL;

	VolDirToPath(pspec->vRefNum, pspec->dirID, thepath);
	plen = strlen(thepath);
	if (plen >= size)
		plen = size - 1;

	memcpy(path, thepath, plen);
	path[plen] = 0;
	return path;
}


char       *
OS_NameSpecToString(const OSNameSpec * spec, char *name, int size)
{
	int         nlen;

	if (size == 0)
		size = OS_NAMESIZE;
	if (name == NULL && (name = (char *) malloc(size)) == NULL)
		return NULL;

	nlen = spec->name[0];
	if (nlen >= size)
		nlen = size - 1;

	memcpy(name, spec->name + 1, nlen);
	name[nlen] = 0;
	return name;
}

/*	return the size of an OSPathSpec, for duplication purposes */

int
OS_SizeOfPathSpec(const OSPathSpec * spec)
{
	return sizeof(OSPathSpec);
}

/*	return the size of an OSNameSpec, for duplication purposes */

int
OS_SizeOfNameSpec(const OSNameSpec * spec)
{
	return *spec->name + 1;
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
	return a->vRefNum == b->vRefNum && a->dirID == b->dirID;
}

/*	compare OSNameSpecs */

int
OS_EqualNameSpec(const OSNameSpec * a, const OSNameSpec * b)
{
	unsigned char len;

	if (a->name[0] != b->name[0])
		return 0;

	len = a->name[0];
	while (len) {
		if (tolower(a->name[len]) != tolower(b->name[len]))
			return 0;
		len--;
	}

	return 1;
}

#if 0
#pragma mark -
#endif

/*	tell if OSSpec is a directory */

int
OS_IsDir(const OSSpec * spec)
{
	CInfoPBRec  pb;
	Str255      name;
	OSErr       err;

	pstrcpy(name, spec->name.name);
	if (!*name) {
		name[0] = 1;
		name[1] = ':';
	}
	pb.hFileInfo.ioNamePtr = name;
	pb.hFileInfo.ioVRefNum = spec->path.vRefNum;
	pb.hFileInfo.ioFDirIndex = 0;
	pb.hFileInfo.ioDirID = spec->path.dirID;
	if ((err = PBGetCatInfoSync(&pb)) != 0)
		return 0;
	else {
		return (pb.hFileInfo.ioFlAttrib & 0x10) == 0x10;
	}
}

/*	tell if OSSpec is a file */

int
OS_IsFile(const OSSpec * spec)
{
	CInfoPBRec  pb;
	Str255      name;
	OSErr       err;

	pstrcpy(name, spec->name.name);
	if (!*name) {
		name[0] = 1;
		name[1] = ':';
	}
	pb.hFileInfo.ioNamePtr = name;
	pb.hFileInfo.ioVRefNum = spec->path.vRefNum;
	pb.hFileInfo.ioFDirIndex = 0;
	pb.hFileInfo.ioDirID = spec->path.dirID;
	if ((err = PBGetCatInfoSync(&pb)) != 0)
		return 0;
	else {
		return (pb.hFileInfo.ioFlAttrib & 0x10) != 0x10;
	}
}

/*	tell if OSSpec is a [soft] link / alias */

/*	this only checks leaves */

int
OS_IsLink(const OSSpec * spec)
{
	FSSpec      alias;
	OSErr       err;
	Boolean     isFolder, leafIsAlias;

	err = FSMakeFSSpec(FSS(spec), &alias);
	if (err != noErr)
		return 0;

	err = ResolveAliasFile(&alias, 1, &isFolder, &leafIsAlias);
	if (err != noErr)
		return 0;
	else
		return leafIsAlias;
}

/*	resolve a [soft] link / alias */

/*	this only resolves leaves */

OSError
OS_ResolveLink(const OSSpec * link, OSSpec * target)
{
	FSSpec      alias;
	OSErr       err;
	Boolean     isFolder, leafIsAlias;

	err = FSMakeFSSpec(FSS(link), &alias);
	if (err != noErr)
		return err;

	err = ResolveAliasFile(&alias, 1, &isFolder, &leafIsAlias);

	target->path.vRefNum = alias.vRefNum;
	target->path.dirID = alias.parID;
	pstrcpy(target->name.name, alias.name);
	return err;
}


/*************************************/
#if 0
#pragma mark -
#endif


/*	open a directory for reading */

OSError
OS_OpenDir(const OSPathSpec * spec, OSDirRef * ref)
{
	CInfoPBRec  pb;
	OSErr       err;

	pb.dirInfo.ioNamePtr = NULL;
	pb.dirInfo.ioVRefNum = spec->vRefNum;
	pb.dirInfo.ioFDirIndex = -1;
	pb.dirInfo.ioDrDirID = spec->dirID;

	err = PBGetCatInfoSync(&pb);
	if (err != noErr) {			/* directory not found? */
		ref->vRefNum = ref->dirID = 0;
		return err;
	} else {
		ref->vRefNum = spec->vRefNum;
		ref->dirID = pb.dirInfo.ioDrDirID;
		ref->index = 1;
		return OS_NOERR;
	}
}

/*	read an entry from a directory;
	don't return "." or "..";
	return error when end-of-directory reached */

OSError
OS_ReadDir(OSDirRef * ref, OSSpec * entry, char *filename, bool * isfile)
{
	CInfoPBRec  pb;
	Str255      name;
	OSErr       err;

	*name = 0;
	pb.dirInfo.ioNamePtr = name;
	pb.dirInfo.ioVRefNum = ref->vRefNum;
	pb.dirInfo.ioDrDirID = ref->dirID;
	pb.dirInfo.ioFDirIndex = ref->index;

	err = PBGetCatInfoSync(&pb);
	if (err == noErr) {
		FSSpec      fss;

		pstrcpy(fss.name, name);
		p2cstrcpy(filename, name);
		fss.vRefNum = ref->vRefNum;
		fss.parID = ref->dirID;

		CanonicalizeIfDirectory(&fss);

		entry->path.vRefNum = fss.vRefNum;
		entry->path.dirID = fss.parID;
		pstrcpy(entry->name.name, fss.name);
		if (isfile)
			*isfile = (*fss.name != 0);

		ref->index++;
	}
	return err;
}

/*	close directory */

OSError
OS_CloseDir(OSDirRef * ref)
{
	ref->index = 0;
	ref->vRefNum = ref->dirID = 0;
	return OS_NOERR;
}

/***************************************/
#if 0
#pragma mark -
#endif

/*	return time in milliseconds */

unsigned long
OS_GetMilliseconds(void)
{
	return LMGetTicks() * 1000 / 60;
}


/*	return current time */

void
OS_GetTime(OSTime * tm)
{
	GetDateTime(tm);
}

#if 0
#pragma mark -
#endif


/*	allocate a memory handle  */

OSError
OS_NewHandle(OSSize size, OSHandle * hand)
{
	if (systemHandles) {
		OSErr       res;

		hand->h = TempNewHandle(size, &res);
		if (res == OS_NOERR)
			return OS_NOERR;
	}

	hand->h = NewHandle(size);
	if (hand->h != NULL)
		return OS_NOERR;

	return OS_MEMERR;
}

/*	resize handle  */

OSError
OS_ResizeHandle(OSHandle * hand, OSSize size)
{
	SetHandleSize(hand->h, size);
	if (GetHandleSize(hand->h) == size)
		return OS_NOERR;
	else
		return OS_MEMERR;
}

/*	lock handle  */

void       *
OS_LockHandle(OSHandle * hand)
{
	if (GetHandleSize(hand->h) >= 65536)
		HLockHi(hand->h);
	else
		HLock(hand->h);
	return (void *) *hand->h;
}

/*	unlock handle  */

void
OS_UnlockHandle(OSHandle * hand)
{
	HUnlock(hand->h);
}

/*	free handle  */

OSError
OS_FreeHandle(OSHandle * hand)
{
	DisposeHandle(hand->h);
	hand->h = NULL;
	return OS_NOERR;
}

/*	get handle size */

/*  a return of 0 may indicate error, 
	but we have no way to report this.
*/

OSError
OS_GetHandleSize(OSHandle * hand, OSSize * size)
{
	*size = GetHandleSize(hand->h);
	if (*size == 0)
		return MemError();
	else
		return OS_NOERR;
}

/*	invalidate handle */

void
OS_InvalidateHandle(OSHandle * hand)
{
	hand->h = NULL;
}

/*	tell whether a handle is valid */

bool
OS_ValidHandle(OSHandle * hand)
{
	return hand != NULL && hand->h != NULL;
}

/*************************************/
#if 0
#pragma mark -
#endif


/*	Code resource routines  */

/*	No support yet */

/*	open a shared library  */

OSError
OS_OpenLibrary(const OSSpec * spec, OSLibrary * lib)
{
	*lib = NULL;
	return fnfErr;
}

/*	find a symbol in the library */

OSError
OS_GetLibrarySymbol(OSLibrary lib, char *name, void **sym)
{
	*sym = NULL;
	return rfNumErr;			/* assume it wasn't open */
}

/*	close a shared library */

OSError
OS_CloseLibrary(OSLibrary lib)
{
	return rfNumErr;			/* assume it wasn't open */
}
