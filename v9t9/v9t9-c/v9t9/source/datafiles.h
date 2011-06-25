/*
  datafiles.h						-- utilities for manipulation of files

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

#ifndef __DATAFILES_H__
#define __DATAFILES_H__

#include "centry.h"

#include "OSLib.h"

extern	char *modulespath, *romspath, *ramspath, *demospath, *datapath;

extern OSFileType osBinaryType;

/*
 *	Find 'filename' in 'path', and if a match is found, set in 'spec'
 *	and return 1.  If 'path' is null, the search is repeated with 'datapath'.
 *	Returns 0 if no match is made.
 */
int
data_find_file(const char *path, const char *filename, OSSpec *spec);

/*
 *	Find place to create 'filename' in 'path', and set in 'spec'.
 *	'type' is the filetype of the file to create.  If 'path' is null,
 *	the search is repeated with 'datapath'.  Returns 0 if the file
 *	cannot be created anywhere.
 */
int
data_create_file(const char *path, const char *filename,
				   OSSpec *spec, OSFileType *type);

/*
    Find the location for a binary image and return its size,
	or 0 for failure.
 */
int 
data_find_binary(const char *path, const char *filename, OSSpec *spec);

/*
 *	Load a binary image, return # bytes read, or zero for failure.
 *	May emit error messages.
 *
 *	type:			string emitted in error, i.e., "RAM image"
 *	path:			search path
 *	filename:		name of image
 *	fileoffs:		offset into file to read
 *	loadat:			area in memory to store
 *	swap:			if true, byteswap word data from TI format
 *	imagesize:		expected size of file
 *	maxsize:		maximum size allowed to read
*/
int 
data_load_binary(const char *type, const char *path, const char *filename,
				 u8 * loadat, int swap, int fileoffs, int imagesize,
				 int maxsize);

/*
 *	Save data to an image found somewhere in 'path' 
 *	(if not existing, placed in first writeable entry of 'path').
 *	May emit error messages.
 *
 *	type:			string emitted in error, i.e., "RAM image"
 *	path:			search path
 *	filename:		name of image
 *	fileoffs:		offset into file to write
 *	saveat:			start of memory to write 
 *	swap:			if true, byteswap word data to TI format
 *	memsize:		size of memory to write
 */
int  
data_save_binary(const char *type, const char *path, const char *filename, 
				 int fileoffs, u8 * saveat, int swap, int memsize);

int	
data_load_dsr(const char *path, const char *filename, const char *name, void *mem);

/*	GRAM Kracker file segment types */
enum
{
	GK_TYPE_PROGRAM = 0x00,
	GK_TYPE_GROM_0	= 0x01,
	GK_TYPE_GROM_1	= 0x02,
	GK_TYPE_GROM_2	= 0x03,
	GK_TYPE_GROM_3	= 0x04,
	GK_TYPE_GROM_4	= 0x05,
	GK_TYPE_GROM_5	= 0x06,
	GK_TYPE_GROM_6	= 0x07,
	GK_TYPE_GROM_7	= 0x08,
	GK_TYPE_ROM_1	= 0x09,
	GK_TYPE_ROM_2	= 0x0A
};

typedef struct gram_kracker_header
{
	u8	more_to_load;				/* 0x00 means no */
	u8	gk_type;					/* GK_TYPE_xxx anove */
	u16	length;						/* translated to host-endian */
	u16	address;					/* translated to host-endian */
	int absolute_image_file_offset;	/* offset in containing host file */
}	gram_kracker_header;

/*
 *	Search paths for a file matching 'filename'.
 *	If found, and it is a valid GRAM-Kracker file,
 *	set 'spec' to point to it, and return its header info in 'header'.
 *
 *	Returns non-zero file size for success.
 *	
 */
int 
data_find_gram_kracker(const char *path, const char *filename, 
					   OSSpec *spec, gram_kracker_header *header);

#include "cexit.h"

#endif
