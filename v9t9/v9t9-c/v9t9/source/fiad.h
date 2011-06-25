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
 *	File-in-a-directory utility routines
 *
 */

#ifndef __FIAD_H__
#define __FIAD_H__

#include "centry.h"

extern int newfileformat, unknownfileistext, keepfileformat;
extern int unknownfileistext;
extern int repairbadfiles;
extern int fixupoldv9t9filenames, generateoldv9t9filenames;

/*	In V9t9 6.0, we used 8.3 filenames, and these chars
	were converted by adding 0x80 to the name on disk. 
	In this version, we still have illegal chars, but
	they are replaced with the escape sequence '&#xx;' as
	in HTML. */
#define DOS_illegalchars "<>=,;:*?[]/\\"

#define FIAD_esc '&'
#define FIAD_illegalchars "<>,:*?/\\"

typedef void (*fiad_logger_func)(u32, const char *, ...);

/*
 *	Install a new function to log warnings and errors
 *	from the fiad_xxx routines.  NULL means no logging.
 *	Returns the old logger function.
 */
fiad_logger_func
fiad_set_logger(fiad_logger_func nw);


/*	Convert a TI filename to a DOS 8.3 filename. */
void
fiad_filename_ti2dos(const char *tiname, int len, char *dosname);

/* Convert a TI filename to the host OS. */
void
fiad_filename_ti2host(const char *tiname, int len, char *hostname);

/*	Create a full path given a TI filename and an OS path. */
OSError
fiad_filename_to_spec(const OSPathSpec * path, const char *name, int len, 
					  OSSpec * spec);

/*	Convert a filename to TI format,
	return length of filename */
int
fiad_filename_host2ti(const char *hostname, char *tiname);

/*	Return length of filename in FDR */
int
fiad_filename_strlen(const char *name);

/*	Convert a directory leaf to a TI name at 'name' and return length. */
int
fiad_path_disk2ti(const OSPathSpec *path, char *name);

/*	Rename an old DOS-mangled V9t9 filename to the new format 
	
	name, len:  TI-format name 
	spec:		spec for existing file in an old format

	If successful, OS_NOERR is returned and 'spec' is transformed
	into the current filename.
*/
OSError
fiad_filename_fixup_old_filename(const char *name, int len,
								 OSSpec *spec);

/*	Format of FDR on disk  */
enum
{
	F_V9t9 = 0,			// V9t9 format
	F_TIFILES = 1,		// TIFILES format
	F_TEXT = 2,			// treating as text
	F_UNKNOWN = -1		// unknown
};

extern int unknownfileistext;
extern OSFileType	osV99Type, osTIFILESType;

#define	F_READFDR	1
#define F_MAKEFDR	2
#define	FDRSIZE		128

/*	FDR masks for file type */
enum {
	ff_variable = 0x80,
	ff_backup = 0x10,		// set by MYARC HD
	ff_protected = 0x8,
	ff_internal = 0x2,
	ff_program = 0x1
};

#define FF_VALID_FLAGS	(ff_variable|ff_backup|ff_protected|ff_internal|ff_program)

typedef struct v9t9_fdr {
	char        filenam[10];/* filename, padded with spaces */
	u8          res10[2];	/* reserved */
	u8          flags;		/* filetype flags */
	u8          recspersec;	/* # records per sector, 
							   256/reclen for FIXED,
							   255/(reclen+1) for VAR,
							   0 for program */
	u16         secsused;	/* [big-endian]:  # sectors in file */
	u8          byteoffs;	/* last byte used in file 
								   (0 = no last empty sector) */
	u8          reclen;		/* record length, 0 for program */
	u16         numrecs;	/* [little-endian]:  # records for FIXED file,
							   # sectors for VARIABLE file,
							   0 for program */
	u8          rec20[8];	/* reserved */
	u8          dcpb[100];	/* sector layout of file, ignored for v9t9 */
} v9t9_fdr;

typedef struct tifiles_fdr {
	u8          sig[8];		/* '\007TIFILES' */
	u16         secsused;	/* [big-endian]:  # sectors in file */
	u8          flags;		/* filetype flags */
	u8          recspersec;	/* # records per sector, 
							   256/reclen for FIXED,
							   255/(reclen+1) for VAR,
							   0 for program */
	u8          byteoffs;  	/* last byte used in file 
							   (0 = no last empty sector) */
	u8          reclen;		/* record length, 0 for program */
	u16         numrecs;	/* [little-endian]:  # records for FIXED file,
							   # sectors for VARIABLE file,
							   0 for program */
	u8          unused[112];	/* zero */
} tifiles_fdr;

typedef struct fdrrec {
	char        filenam[10];/* the filename the 9900 sees */
	u8          flags;		/* filetype flags */
	u8          recspersec;	/* # records per sector, 
							   256/reclen for FIXED,
							   255/(reclen+1) for VAR,
							   0 for program */
	u16         secsused;	/* [HOST ORDER]:  # sectors in file */
	u8          byteoffs;	/* last byte used in file 
								   (0 = no last empty sector) */
	u8          reclen;		/* record length, 0 for program */
	u16         numrecs;	/* [little-endian]:  # records for FIXED file,
							   # sectors for VARIABLE file,
							   0 for program */
} fdrrec;

#define FDR_LASTSEC(fdr)	( (fdr)->secsused - 1 + \
								((fdr)->byteoffs ? 0 : 1))

// file size for var/program files
#define FDR_PGMVARFILESIZE(fdr)	( (((fdr)->secsused - 1) << 8) + \
								((fdr)->byteoffs ? (fdr)->byteoffs : 256))
// file size for fixed files
#define FDR_FIXFILESIZE(fdr)	( ((fdr)->numrecs + (fdr)->recspersec - 1) / \
								((fdr)->recspersec ? (fdr)->recspersec : 1) * 256)
#define FDR_FILESIZE(fdr)	(((fdr)->flags & (ff_variable|ff_program)) ? \
						FDR_PGMVARFILESIZE(fdr) : FDR_FIXFILESIZE(fdr))

/*	Verify a V9t9 FDR as a real v9t9 FDR.
	Perform sanity checks to assert that a v9t9 file
	is really a v9t9 file and not a text file. */
bool
fiad_fdr_matches_v9t9_fdr(struct v9t9_fdr *v9f, const char *filename, OSSize filesize);

/*	Setup the various flags in an FDR
	according to the minimum filetype info */
void
fiad_fdr_setup(fdrrec *fdr, bool program, u8 flags, u8 reclen, u32 size);

/*	Repair various fields of the FDR according to
	known bugs or common file-closing problems.
	'filesize' should be the physical size of the file.

	Returns true if fdr was changed.
*/
bool
fiad_fdr_repair(fdrrec *fdr, OSSize filesize);

/*	Layout for our idea of an open file */

typedef struct fiad_tifile {
	OSSpec      spec;		/* OS spec for file */
	OSRef       handle;		/* OS handle for file */
	bool		open;		/* file is open */
	bool		readonly;	/* if we didn't or couldn't open file read/write */
	bool		changed;	/* has file been dirtied? */
	bool		changedfdr;	/* was FDR format or data changed when opened? */

	OSError		error;		/* last OSError we got */

	u16         cursec;		/* current sector offset in file */
	u8          curoffs;	/* current offset into sector */
	u8          curnrecs;	/* record offset into sector */
	u16         currec;		/* current record # in file */

	int         format;		/* FDR format on disk (F_V9t9, F_TIFILES, F_TEXT) */
	fdrrec      fdr;		/* essential info in FDR */

	u8          sector[256]; /* sector buffer */
} fiad_tifile;

/*	Initialize a tifile */
void
fiad_tifile_clear(fiad_tifile *tf);

/*	Setup the FDR with the file path. */
OSError
fiad_tifile_setup_spec_with_file(fiad_tifile *tf, 
								 const OSPathSpec *path, 
								 const char *fname, int len);

/*	Setup the FDR with the OSSpec. */
OSError
fiad_tifile_setup_spec_with_spec(fiad_tifile *tf, 
								 OSSpec *spec);

/*	Read the FDR from a file,
	return 0 if it's bad. */
int
fiad_tifile_read_fdr(fiad_tifile * tf);

/*	Write FDR to file, 
	return 0 and report error if failed. */
int
fiad_tifile_write_fdr(fiad_tifile * tf);

/*	Verify a file, by classifying its type and checking invariants */
int
fiad_tifile_verify(fiad_tifile * tf, bool check_size);

OSFileType *
fiad_get_file_type(int newfileformat);

/*	Initialize file pointers which keep track of current record */
int
fiad_tifile_init_file_pointers(fiad_tifile * tf);

/*	Read a sector from the file at tf->cursec. 
	If tf points to the last empty sector of file, 
	this is not an error.
	Return 0 if sector not found.  */
int
fiad_tifile_read_sector(fiad_tifile * tf);

/*	Write a sector to the file at tf->cursec. 
	Return 0 if sector not written.  */
int
fiad_tifile_write_sector(fiad_tifile * tf);

/*  Set file size according to cursec and curoffs */
int
fiad_tifile_set_file_size(fiad_tifile *tf);

/*
  Open or create the file, either r/w or r/o.
  We expect the spec to have been set up.

  create && always means, delete existing file.
  create means, create file if not existing.

  create, if 2, means the same as create==true, 
  with the added hint that the file size might
  not match, so don't check this.
 */
OSError
fiad_tifile_open_file(fiad_tifile *tf, int newfileformat,
					  bool create, bool always, bool readonly);

/*
 *	Reopen a file, keeping open file information intact.
 */
OSError
fiad_tifile_reopen_file(fiad_tifile *tf, int newfileformat,
						bool readonly);

/*
 *	Close a tifile.
 */
void
fiad_tifile_close_file(fiad_tifile * tf);

/*
  Rewrite FDR of closed file 

  This may be used to change the format (change tf->format)
  or rewrite a fixed FDR on a readonly file.
*/
void
fiad_tifile_rewrite_fdr(fiad_tifile *tf);

/*	Flush dirty buffers */
int
fiad_tifile_flush(fiad_tifile *tf);

/*	Change the current sector */
int
fiad_tifile_seek_to_sector(fiad_tifile *tf, int secnum);

/*	Seek, logically, to the end of file */
int
fiad_tifile_seek_to_end(fiad_tifile * tf);

/*	Read a record from a file 

	Return 0 for success, 1 for EOF, and -1 for hardware failure
*/
int fiad_tifile_read_record(fiad_tifile *tf, u8 *data, u8 *reclen);

/*	Write a record to the file.

	Return 0 for success, -1 for hardware failure, 1 for disk full 
*/
int
fiad_tifile_write_record(fiad_tifile *tf, u8 *data, u8 reclen);

/*	Seek to a given record.  For variable files, only 0 is accepted. 

	Return 0 for success, -1 for hardware failure, 1 for disk full
*/
int
fiad_tifile_seek_to_record(fiad_tifile *tf, u16 recnum);

/*	Read binary data from file at current position.

	Returns 0 for success, -1 for hardware failure, 1 for EOF.
*/
int
fiad_tifile_read_binary_image(fiad_tifile *tf, u8 *data, u16 maxread, u16 *gotread);

/*	Write binary data to a file at the current position 

	Return 0 for success, -1 for hardware failure, 1 for disk full
*/
int
fiad_tifile_write_binary_image(fiad_tifile *tf, u8 *data, u16 towrite, u16 *written);

/*	Get info (read FDR) about a closed file in tf,
	return 1 for success
*/
int
fiad_tifile_get_info(fiad_tifile *tf);


/*	A struct for catalog info */

typedef struct fiad_catalog
{
	OSPathSpec	path;
	u32			total_sectors;			// for 'disk' (256-byte)
	u32			free_sectors;			// for 'disk' (256-byte)
	u32			entries, entries_max;
	int		   	*index;					// index for filenames[] and fdrs[]
	char		**filenames;			// in host format
	fdrrec		*fdrs;
} fiad_catalog;

/*	Free a catalog */
void
fiad_catalog_free_catalog(fiad_catalog *cat);

/*	Read a catalog from a directory */
OSError
fiad_catalog_read_catalog(fiad_catalog *cat, const char *wildcard);

/*	Sort a catalog */

enum
{
	FIAD_CATALOG_SORT_BY_DISK,	// i.e., disk directory order (none!)
	FIAD_CATALOG_SORT_BY_NAME,
	FIAD_CATALOG_SORT_BY_SIZE,
	FIAD_CATALOG_SORT_BY_TYPE
};
void
fiad_catalog_sort_catalog(fiad_catalog *cat, int sort_by, bool ascending);

/*	Get file information from catalog:  index is the sorted index */
int
fiad_catalog_get_file_info(fiad_catalog *cat, int index, fdrrec *fdr);

/*	Useful utilities */
const char *
fiad_catalog_get_file_type_string(fdrrec *fdr);

#include "cexit.h"

#endif
