/*
  datafiles.c					-- utilities for manipulation of files

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

#include <stdarg.h>
#include "v9t9_common.h"
#include "fiad.h"
#include "memory.h"

#define _L LOG_ROMS


/* user directory lists */
char       *modulespath, *romspath, *ramspath, *demospath, *datapath;

#if BEWORKS_FS
OSFileType  osBinaryType = { 0666, "x/v9t9-memory-image" };
#elif POSIX_FS
OSFileType  osBinaryType = { 0666 };
#elif WIN32_FS
OSFileType  osBinaryType = 0;
#elif MAC_FS
OSFileType  osBinaryType = 'BIN ';
#endif

/*
 *	Find a file in the given path, returning 1 for success,
 *	or 0 for failure.  
 *
 *	Policy is:  look only in the path, unless it is NULL,
 *	in which case look at CWD.
 */
int
data_find_file(const char *path, const char *filename, OSSpec *spec)
{
	OSError err;

	if (!filename) 
		logger(_L|LOG_ABORT, _("NULL argument passed to data_find_file()"));

	if (path && (err = OS_FindFileInPath(filename, path, spec)) == OS_NOERR)
		return 1;
	else if ((err = OS_FindFileInPath(filename, datapath, spec)) == OS_NOERR)
		return 1;
	else
		return 0;

/*
	// look first for literal name
	if (!creating
		&& (err = OS_MakeFileSpec(filename, spec)) == OS_NOERR 
		&& OS_Status(spec) == OS_NOERR) {
		return 1;
		// look in provided path
	} else if (path 
		   && (err = OS_FindFileInPath(filename, path, spec, creating)) 
			   == OS_NOERR) {
		return 1;
		// look in v9t9's own directory
	} else if ((err = OS_MakeSpecWithPath(&v9t9_datadir, filename, 
										  mswp_noRelative, spec)) == OS_NOERR
			   && OS_Status(spec) == OS_NOERR) {
		return 1;
		// give up
	} else {
		return 0;
	}
*/

}


/*
 *	Find a file in the given path, returning 1 for success,
 *	or 0 for failure.  
 *
 *	Policy is:  look only in the path, unless it is NULL,
 *	in which case use the CWD.
 */
int
data_create_file(const char *path, const char *filename,
				   OSSpec *spec, OSFileType *type)
{
	OSError err;

	if (!filename) 
		logger(_L|LOG_ABORT, _("NULL argument passed to data_create_file()"));

	if (path && (err = OS_CreateFileInPath(filename, path, spec, type)) == OS_NOERR)
		return 1;
	else if ((err = OS_CreateFileInPath(filename, datapath, spec, type)) == OS_NOERR)
		return 1;
	else
		return 0;
}

/*************************************************************/
#if 0
#pragma mark -
#endif

/*
    Find the location for a binary image and return size.
 */
int  
data_find_binary(const char *path, const char *filename, OSSpec *spec)
{
	OSRef       ref;
	OSSize      size;
	OSError     err;

	if (!filename)
		return 0;

#if 0
	if (OS_GetFileNamePtr(filename) != filename &&
		(err = OS_MakeFileSpec(filename, spec)) == OS_NOERR &&
		OS_Status(spec) == OS_NOERR) {
		// found it
	} else if ((err = OS_FindFileInPath(filename, path, spec)) != OS_NOERR &&
			   (err = OS_FindFileInPath(filename, syspath, spec)) != OS_NOERR) {
		return 0;
	}
#endif

	if (!data_find_file(path, filename, spec))
		return 0;

	if ((err = OS_Open(spec, OSReadOnly, &ref)) != OS_NOERR) {
		OSerror(err, "\n");
		return 0;
	}

	OS_GetSize(ref, &size);

	OS_Close(ref);

	return size;
}


/*
	Load a binary image, return # bytes read.
*/
int  
data_load_binary(const char *type, const char *path, const char *filename,
				 u8 * loadat, int swap, int fileoffs, int imagesize, 
				 int maxsize)
{
	OSRef       ref;
	OSSize      size;
	OSError     err;
	OSSpec      spec;

	size = data_find_binary(path, filename, &spec);

	if (!size) {
		if (*filename) {
			command_logger(_L |LOG_USER|LOG_ERROR, 
						   _("Cannot find '%s' in path:\n'%s'\n"), 
						   filename, path);
		}
		return 0;
	}
	logger(_L|L_0, _("Loading %s image %s... "), type, OS_SpecToString1(&spec));

	if (imagesize)
		size = imagesize;
	else if (size - fileoffs > maxsize) {
		command_logger(_L | LOG_WARN | LOG_USER, _("%s too long, only %d bytes of %d read... "),
			 OS_SpecToString1(&spec), maxsize, size);
		size = maxsize - fileoffs;
	}

	if ((err = OS_Open(&spec, OSReadOnly, &ref)) != OS_NOERR) {
		OSerror(err, "\n");
		return 0;
	}

	if ((err = OS_Seek(ref, OSSeekAbs, fileoffs)) != OS_NOERR ||
		(err = OS_Read(ref, loadat, &size)) != OS_NOERR) {
		OSerror(err, _("could not read\n"));
		return 0;
	}

	if (swap) {
		logger(_L, _("swapping bytes... "));
		swab((const void *) loadat, (void *) loadat, size);
	}

	logger(_L| L_0, _("done\n"));

	OS_Close(ref);
	return size;
}

/*
	Save a binary image, return # bytes written.
*/

/*
 *	Save data to an image found somewhere in 'path'
 *	(if not existing, placed in first writeable entry of 'path').
 *	Existing file is not overwritten.
 *
 *	path:			search
 *	type:			string emitted in error, i.e., "RAM image"
 *	filename:		name of image
 *	fileoffs:		offset into file to write
 *	saveat:			start of memory to write 
 *	swap:			if true, byteswap word data to TI format
 *	memsize:		size of memory to write
 */
int  
data_save_binary(const char *type, const char *path, const char *filename, 
				 int fileoffs, u8 * saveat, int swap, int memsize)
{
	OSRef       ref;
	OSSize      size;
	OSError     err;
	OSSpec      spec;

#if 0
	if ((size = data_find_binary(path, syspath, filename, &spec)) == 0) {
		if ((err = OS_CreateFileInPath(filename, 
									   *path ? path : syspath, 
									   &spec, 
									   &osBinaryType)) != OS_NOERR) {
			OSerror(err, _("Couldn't create %s image %s anywhere in path: %s\n"),
					type, filename, path);
			return 0;
		}
	}
#endif

	if ((size = data_find_binary(path, filename, &spec)) == 0) {
		if (!data_create_file(path, filename, &spec, &osBinaryType)) {
			logger(_L|LOG_USER|LOG_ERROR, _("Couldn't create %s image '%s' anywhere in path:\n'%s'\n"),
					type, filename, path);
			return 0;
		}
		size = memsize;
	}

	logger(_L |L_0, _("Writing %s image %s... "), type, OS_SpecToString1(&spec));

	if ((err = OS_Open(&spec, OSReadWrite, &ref)) != OS_NOERR) {
		OSerror(err, "\n");
		return 0;
	}

	//saveat += offset;

	if (swap) {
		logger(_L, _("swapping bytes to save... "));
		swab((const void *) saveat, (void *) saveat, size);
	}

	err = OS_Seek(ref, OSSeekAbs, fileoffs);

	size = memsize;
	err = OS_Write(ref, saveat, &size);

	if (swap) {
		logger(_L, _("swapping bytes back... "));
		swab((const void *) saveat, (void *) saveat, size);
	}

	if (err != OS_NOERR) {
		OSerror(err, _("could not write\n"));
		return 0;
	}

	logger(_L|L_0, _("done\n"));

	OS_Close(ref);
	return size;
}

/*
	Return 1 if loaded.
*/
int
data_load_dsr(const char *path, const char *filename, const char *name, 
			  void *mem)
{
	return data_load_binary(name, romspath, filename, 
							mem, 1, 0, 8192, 8192);
}

/********************************************************/
#if 0
#pragma mark -
#endif

/*

Each GRAM Kracker stores an 8K ROM or GROM bank.  The first six bytes
are an extension of the memory image program standard.  Here is what the
bytes mean:

Byte 0: "More to load" flag, values:
>FF = There's another file to load
>80 = There's a "UTIL" file to load (I don't think this is ever used).
>00 = This is the last file

Byte 1: Bank of GRAM/RAM to load file into, values:
>01 = GROM/GRAM bank 0 (>0000)
>02 = GROM/GRAM bank 1 (>2000)
>03 = GROM/GRAM bank 2 (>4000)
>04 = GROM/GRAM bank 3 (>6000)
>05 = GROM/GRAM bank 4 (>8000)
>06 = GROM/GRAM bank 5 (>A000)
>07 = GROM/GRAM bank 6 (>C000)
>08 = GROM/GRAM bank 7 (>E000)
>09 = ROM bank 1 (>6000)
>0A = ROM bank 2 (second >6000)
>00 or >FF = Assembly language program, not a cartridge file

Bytes 2 and 3: Number of bytes to load (normally 8192)
Bytes 4 and 5: Actual address to start loading at (e.g. >6000)

As an example, Extended BASIC gets saved as files "XB" to "XB5", which
have headers as follows:
XB      FF0A 2000 6000 (ROM bank 2)
XB1     FF09 2000 6000 (ROM bank 1)
XB2     FF07 2000 C000 (GRAM bank 6)
XB3     FF06 2000 A000 (GRAM bank 5)
XB4     FF05 2000 8000 (GRAM bank 4)
XB5     0004 2000 6000

Even though TI GROMS only use 6K of each 8K bank, all 8K is stored so people
can add their own GPL code as extensions to modules.  Extended BASIC has
a number of enhancements available.
*/

/*
 *	Search paths for a file matching 'filename'.
 *	If found, and it is a valid GRAM-Kracker file,
 *	set 'spec' to point to it, and return its header info in 'header'
 *	
 */
int data_find_gram_kracker(const char *path, const char *filename, 
						   OSSpec *spec, gram_kracker_header *header)
{
	int size;
	fiad_logger_func old;
	int old_unknownfileistext;
	fiad_tifile tf;
	OSError err;
	OSRef ref;
	bool is_v9t9_file;

	memset(header, 0, sizeof(gram_kracker_header));

	/* Can we find the file? */
	size = data_find_binary(path, filename, spec);
	if (!size)
		return 0;

	/* See if it's a V9t9/TIFILES file or perhaps a plain binary */
	old = fiad_set_logger(0L);
	old_unknownfileistext = unknownfileistext;
	unknownfileistext = 0;

	if (fiad_tifile_setup_spec_with_spec(&tf, spec) != OS_NOERR) {
		goto error_exit;
	}

	/* Is it a V9t9 file? */
	if (fiad_tifile_get_info(&tf)) {
		if (!(tf.fdr.flags & ff_program)) {
			command_logger(_L|LOG_ERROR|LOG_USER, _("GRAM Kracker segment '%s' is not a PROGRAM file (it's %s)\n"),
				   OS_SpecToString1(spec), fiad_catalog_get_file_type_string(&tf.fdr));
			goto error_exit;
		}
		// allow enough space for the data, header, and extra sector
		if (FDR_FILESIZE(&tf.fdr) > 8198+256) {
			command_logger(_L|LOG_ERROR|LOG_USER, _("GRAM Kracker segment '%s' is too long\n"
				   "to be a segment (%d bytes > 8192+6 bytes)\n"),
				   OS_SpecToString1(spec), FDR_FILESIZE(&tf.fdr));
			goto error_exit;
		}
		is_v9t9_file = true;
	} else {
		is_v9t9_file = false;
	}

	/* Read header from first sector */
	if (is_v9t9_file) {
		u16 headersize = 6;
		if (fiad_tifile_open_file(&tf, newfileformat, 
								   false /*create*/,
								   false /*always*/,
								   true /*readonly*/) != OS_NOERR 
			||
			fiad_tifile_read_binary_image(&tf,
										  (u8 *)header,
										  headersize,
										  &headersize) != 0
			||
			headersize < 6)
		{
			command_logger(_L|LOG_ERROR|LOG_USER, _("Could not read signature for GRAM Kracker segment in V9t9 file '%s' (%s)\n"),
				   OS_SpecToString1(spec), OS_GetErrText(tf.error));
			goto error_exit;
		}
		header->absolute_image_file_offset = sizeof(v9t9_fdr) + 6;
	} else {
		OSSize headersize = 6;

		if ((err = OS_Open(spec, OSReadOnly, &ref)) != OS_NOERR ||
			(err = OS_Read(ref, (void *)header, &headersize)) != OS_NOERR ||
			(err = OS_Close(ref)) != OS_NOERR)
		{
			command_logger(_L|LOG_ERROR|LOG_USER, _("Could not read signature for GRAM Kracker segment in binary file '%s' (%s)\n"),
				   OS_SpecToString1(spec), OS_GetErrText(err));
			goto error_exit;
		}
		header->absolute_image_file_offset = 6;
	}

	/* Make header suitable for v9t9 */
	header->address = TI2HOST(header->address);
	header->length = TI2HOST(header->length);

	if (header->more_to_load == 0x80)
	{
		command_logger(_L|LOG_ERROR|LOG_USER, _("GRAM Kracker segment '%s' is a 'UTIL' segment (not supported)\n"),
			   OS_SpecToString1(spec));
		goto error_exit;
	}

	if (header->gk_type == 0x00 ||
		header->gk_type == 0xff ||
		header->gk_type > GK_TYPE_ROM_2)
	{
		command_logger(_L|LOG_ERROR|LOG_USER, _("GRAM Kracker segment '%s' is not a ROM file (got >%02X)\n"),
			   OS_SpecToString1(spec), header->gk_type);
		goto error_exit;
	}

	fiad_set_logger(old);
	return size;

error_exit:
	fiad_set_logger(old);
	unknownfileistext = old_unknownfileistext;
	return 0;
}
