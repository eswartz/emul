/*
  fiad.c						-- file-in-a-directory emulation routines

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
 *	File-in-a-directory library routines
 *
 */

#include "v9t9_common.h"
#include "v9t9_endian.h"
#include "fiad.h"

#define _L	LOG_EMUDISK
#define _LL	_L | LOG_USER

#if BEWORKS_FS
OSFileType  osV99Type = { 0666, "x/v9t9-file-image" };
OSFileType  osTIFILESType = { 0666, "x/tifiles-image" };
#elif POSIX_FS
OSFileType  osV99Type = { 0666 };
OSFileType  osTIFILESType = { 0666 };
#elif WIN32_FS
OSFileType  osV99Type = 0;
OSFileType  osTIFILESType = 0;
#elif MAC_FS
OSFileType  osV99Type = 'FI99';
OSFileType  osTIFILESType = 'TI99';
#endif

int         keepfileformat = 1;
int         unknownfileistext = 0;
int			repairbadfiles = 0;
int			fixupoldv9t9filenames = 1;
int			generateoldv9t9filenames = 0;
int         newfileformat = F_V9t9;


/*
 *	Suitable logger for no-nag situations
 */
static void 
fiad_no_logger(u32 srcflags, const char *format, ...)
{
	if (srcflags & LOG_FATAL)	exit(234);
}

static void (*fiad_logger)(u32, const char *, ...) = fiad_no_logger;

/*
 *	Install a new function to log warnings and errors
 *	from the fiad_xxx routines.  Passing 'NULL' uses
 *	no logging (the default).
 */
fiad_logger_func
fiad_set_logger(fiad_logger_func nw)
{
	fiad_logger_func old = fiad_logger;
	fiad_logger = nw == 0L ? fiad_no_logger : nw;
	return old;
}

/*	Convert a TI filename to a DOS 8.3 filename. */
void
fiad_filename_ti2dos(const char *tiname, int len, char *dosname)
{
	int         max = 10;
	int         ptr = 0;
	int         dptr = 0;

	while (len-- && max--) {
		char        cur;

		cur = tiname[ptr];

		/* forced end-of-filename? */
		if (cur == ' ' || cur == 0)
			break;

		if (ptr == 8)
			dosname[dptr++] = '.';

		/* offset illegal chars */
		if (strchr(DOS_illegalchars, cur) != NULL)
			cur |= 0x80;

		/* force uppercase */
		if (cur >= 'a' && cur <= 'z')
			cur -= 0x20;

		dosname[dptr++] = cur;
		ptr++;
	}

	dosname[dptr] = 0;
	fiad_logger(_L | L_2, _("fiad_filename_ti2dos:  incoming = '%.*s', outgoing = '%s'\n"), 10 - max, tiname,
		 dosname);
}

/* Convert a TI filename to the host OS.  

   We convert illegal chars in FIAD_illegalchars into HTML-like
   encodings (&#xx;) so all possible filenames can be stored.
*/
void
fiad_filename_ti2host(const char *tiname, int len, char *hostname)
{
	int         max = 10;
	int         hptr = 0, tptr = 0;

	while (len-- && max--) {
		char        cur = tiname[tptr++];

		/*  force lowercase  */
		if (cur >= 'A' && cur <= 'Z')
			cur += 0x20;
		else
			// illegal chars
		if (cur == FIAD_esc || strchr(FIAD_illegalchars, cur) != NULL) {
			u8          hex;

			hostname[hptr++] = '&';
			hostname[hptr++] = '#';
			hex = (cur & 0xf0) >> 4;
			if (hex > 9)
				hex += 'A' - 10;
			else
				hex += '0';
			hostname[hptr++] = hex;
			hex = (cur & 0xf);
			if (hex > 9)
				hex += 'A' - 10;
			else
				hex += '0';
			hostname[hptr++] = hex;
			cur = ';';
			//hostname[hptr++] = ';';
		}

		hostname[hptr++] = cur;
	}
	hostname[hptr] = 0;
	fiad_logger(_L | L_2, _("fiad_filename_ti2host:  incoming = '%.*s', outgoing = '%s'\n"), 10 - max, tiname,
		 hostname);
}

/*	Rename an old DOS-mangled V9t9 filename to the new format 
	
	name, len:  TI-format name 
	spec:		spec for existing file in an old format

	If successful, OS_NOERR is returned and 'spec' is transformed
	into the current filename.
*/
OSError
fiad_filename_fixup_old_filename(const char *name, int len,
								 OSSpec *spec)
{
	char        newname[OS_NAMESIZE];
	OSSpec      newspec;
	OSError		err;

	fiad_filename_ti2host(name, len, newname);
	if ((err = OS_MakeSpecWithPath(&spec->path, 
								   newname,
								   !mswp_noRelative,
								   &newspec)) == OS_NOERR
		&& (err = OS_Status(&newspec)) != OS_NOERR) 
	{
		fiad_logger(_LL | 0, _("FIAD server:  renaming old-style file '%s' to '%s'\n"),
					OS_NameSpecToString1(&spec->name), newname);
		if ((err = OS_Rename(spec, &newspec)) != OS_NOERR)
			fiad_logger(_LL | 0, _("FIAD server:  could not rename file (%s)\n"),
						OS_GetErrText(err));
		else
			*spec = newspec;
	}
	return err;
}

/*	Create a full path given a TI filename and an OS path. */
OSError
fiad_filename_to_spec(const OSPathSpec * path, const char *name, int len, 
					  OSSpec * spec)
{
	OSError     err;
	char        osname[OS_NAMESIZE];

	/*  Try the old-style 8.3 name first */
	fiad_filename_ti2dos(name, len, osname);
	if ((err = OS_MakeSpecWithPath(path, osname,
								   !mswp_noRelative, spec)) != OS_NOERR
		|| (err = OS_Status(spec)) != OS_NOERR
		|| generateoldv9t9filenames) 
	{
		/* if they wants it, they gots it */
		if (generateoldv9t9filenames) {
			return err;
		}

		/*  Now try the new-style name */
		fiad_filename_ti2host(name, len, osname);

		/*  if it fails, it fails... */
		return OS_MakeSpecWithPath(path, osname, !mswp_noRelative, spec);
	} 

		/* rename it to the new format? */
	else if (fixupoldv9t9filenames) {
		/* 
		 *	We can be reasonably sure this is a v9t9 file.
		 */
		fiad_filename_fixup_old_filename(name, len, spec);
	}
	return OS_NOERR;
}

/*	Convert a filename to TI format,
	return length of filename */
int
fiad_filename_host2ti(const char *hostname, char *tiname)
{
	int         hptr = 0, tptr = 0, max = 10;

	memset(tiname, ' ', 10);
	while (hostname[hptr] && max) {
		char        cur = hostname[hptr];

		if (cur != '.') {
			/* force uppercase */
			if (islower(cur))
				cur = toupper(cur);
			else
				if (cur == '&' && hostname[hptr + 1] == '#' &&
					isxdigit(hostname[hptr + 2])
					&& isxdigit(hostname[hptr + 3])
					&& hostname[hptr + 4] == ';') {
				u8          val;

				val = hostname[hptr + 2] - '0';
				if (val > 9)
					val -= 7;
				cur = hostname[hptr + 3] - '0';
				if (cur > 9)
					cur -= 7;
				cur |= val << 4;
				hptr += 4;
			} else if ((cur & 0x80)
					   && strchr(DOS_illegalchars, (cur & 0x7f)) != NULL)
			{
				cur ^= 0x80;
			}

			tiname[tptr] = cur;
			tptr++;
			max--;
		}
		hptr++;
	}
	fiad_logger(_L | L_2, _("fiad_filename_host2ti:  incoming: '%s', outgoing: '%.10s'\n"), hostname,
		 tiname);

	return 10 - max + strlen(hostname + hptr);
}

/*	Return length of filename in FDR */
int
fiad_filename_strlen(const char *tf)
{
	int len = 0;
	char *ptr = (char *)tf;
	while (len < 10 && ptr[len] != ' ') {
		len++;
	}
	return len;
}

/*	Convert a directory leaf to a TI name at 'name' and return length. */
int
fiad_path_disk2ti(const OSPathSpec *path, char *name)
{
	char        pth[OS_PATHSIZE];
	const char 	*nptr;
	int         len;

	OS_PathSpecToString2(path, pth);
	pth[strlen(pth) - 1] = 0;
	nptr = OS_GetFileNamePtr(pth);
	len = 0;

	memset(name, ' ', 10);		// clear field

	while (len < 10 && *nptr) {
		char ch = *nptr++;

		if (ch == '.')
			ch = '_';			// no periods allowed
		else if (ch >= 'a' && ch <= 'z')
			ch -= 0x20;			// no lowercase either

		*name++ = ch;
		len++;
	}
	return len;
}

/*********************************/

/*	Verify a V9t9 FDR as a real v9t9 FDR.
	Perform sanity checks to assert that a v9t9 file
	is really a v9t9 file and not a text file. */
bool
fiad_fdr_matches_v9t9_fdr(struct v9t9_fdr *v9f, const char *filename, OSSize filesize)
{
	// check for invalid filetype flags
	if (v9f->flags & ~FF_VALID_FLAGS) {
			fiad_logger(_L | LOG_ERROR, _("FIAD server:  invalid flags %02x "
						"for file '%s'\n"),
						v9f->flags,
						filename);
			return false;
	}

	// check for invalid file size:
	// do not allow file to be more than one sector larger than FDR says,
	// but allow it to be up to 64 sectors smaller:  
	// this is a concession for files copied with "direct output to file", 
	// which must write FDR changes before writing data.
	else if ((long)TI2HOST(v9f->secsused) < (long)((filesize - FDRSIZE) / 256 - 1)
			 || TI2HOST(v9f->secsused) > (filesize - FDRSIZE) / 256 + 64) {
			fiad_logger(_L | LOG_ERROR, _("FIAD server:  invalid number of sectors %d "
						"for data size %d in file '%s'\n"),
						TI2HOST(v9f->secsused), 
						filesize - FDRSIZE,
						filename);
			return false;
	}

	// fixed files have 256/reclen records per sector
	else if (!(v9f->flags & ff_program)
		&& !(v9f->flags & ff_variable)) {
		if (!v9f->reclen ||
			(256 / v9f->reclen != v9f->recspersec)) 
		{
			fiad_logger(_L | LOG_ERROR, _("FIAD server:  record length %d / records per sector %d invalid\n"
						"for FIXED file '%s'\n"),
						v9f->reclen,
						v9f->recspersec,
						filename);
			return false;
		}
	}
	// variable files have 255/(reclen+1) records per sector
	else if (!(v9f->flags & ff_program)) {
		if (!v9f->reclen || 
			(255 / (v9f->reclen + 1) != v9f->recspersec) 
			 // known problem that older v9t9s used this calculation
			&& (256 / v9f->reclen != v9f->recspersec))
		{
			fiad_logger(_L | LOG_ERROR, _("FIAD server:  record length %d / records per sector %d invalid\n"
						"for VARIABLE file '%s'\n"),
						v9f->reclen,
						v9f->recspersec,
						filename);
			return false;
		}
	}

	// program files have 0
	else if (v9f->reclen != 0 && v9f->recspersec != 0) {
		fiad_logger(_LL | LOG_ERROR, _("FIAD server:  record length %d / records per sector %d invalid\n"
					"for PROGRAM file '%s'\n"),
					v9f->reclen,
					v9f->recspersec,
					filename);
		return false;
	}

	return true;
}

/*	Setup the various flags in an FDR
	according to the minimum filetype info */
void
fiad_fdr_setup(fdrrec *fdr, bool program, u8 flags, u8 reclen, u32 size)
{
	if (program) {
		fdr->flags = ff_program;
		fdr->recspersec = 0;
		fdr->reclen = 0;
	} else {
		fdr->reclen = reclen ? reclen : 80;
		fdr->flags = flags & FF_VALID_FLAGS;
		fdr->recspersec = 
			(flags & ff_variable) ?
			(255 / (fdr->reclen + 1)) :
			(256 / fdr->reclen);
	}
	fdr->secsused = (size + 255) >> 8;
	fdr->byteoffs = size & 0xff;
	if (fdr->flags & ff_variable) {
		fdr->numrecs = fdr->secsused;
	} else if (!(fdr->flags & ff_program)) {
		fdr->numrecs = size / fdr->reclen;
	} else {
		fdr->numrecs = 0;
	}
}

/*	Repair various fields of the FDR according to
	known bugs or common file-closing problems.

	Returns true if fdr was changed.
*/
bool
fiad_fdr_repair(fdrrec *fdr, OSSize datasize)
{
	fdrrec orig = *fdr;

	// fix invalid record length...
	if (!(fdr->flags & ff_program)
		&& fdr->reclen == 0) 
	{
		fdr->reclen = 80;
	}

	// fixup for buggy older versions
	// which didn't calculate recspersec right
	// for variable or fixed files
	if (fdr->flags & ff_variable) {
		fdr->recspersec = 256 / (fdr->reclen + 1);	// was 256 / reclen
	} else if (!(fdr->flags & ff_program)) {
		fdr->recspersec = 255 / fdr->reclen;		// was 256 / reclen
	} else if (fdr->flags & ff_program) {
		fdr->recspersec = 0;
		fdr->reclen = 0;
	}

	fdr->secsused = (datasize + 255) / 256;

	// fix for number of records used in variable file
	if (fdr->flags & ff_variable) {
		fdr->numrecs = fdr->secsused;
	}

	return (memcmp((void *)&orig, (void *)fdr, sizeof(fdrrec)) != 0);
}

/*********************************/

/*	Initialize a tifile */
void
fiad_tifile_clear(fiad_tifile *tf)
{
	tf->open = false;
	tf->readonly = false;
	tf->changed = false;
	tf->changedfdr = false;
	tf->handle = 0;
	tf->error = OS_NOERR;
	memset((void *)&tf->fdr, 0, sizeof(fdrrec));
}

/*	Setup the FDR with the file path. */
OSError
fiad_tifile_setup_spec_with_file(fiad_tifile *tf, 
								 const OSPathSpec *path, 
								 const char *fname, int len)
{
	if (len > 10) {
		return OS_FNTLERR;
	}

	// copy filename into FDR
	memset(tf->fdr.filenam, ' ', 10);
	memcpy(tf->fdr.filenam, fname, len);
	fiad_logger(_L | L_2, _("setup FDR name as '%.10s'\n"), tf->fdr.filenam);

	return fiad_filename_to_spec(path, fname, len, &tf->spec);
}

/*	Setup the FDR with the OSSpec. */
OSError
fiad_tifile_setup_spec_with_spec(fiad_tifile *tf, 
								 OSSpec *spec)
{
	char tiname[10];
	int len;

	len = fiad_filename_host2ti(OS_NameSpecToString1(&spec->name), tiname);
	if (len > 10) {
		return OS_FNTLERR;
	}

	tf->spec = *spec;

	/* Since that was successful, now backtrack and see if the
	   file was in the old format */
	if (fixupoldv9t9filenames) {
		/*		
		 * We can't assume that 'tiname' is the actual name
		 * of the file due to weird extended ASCII mangling
		 * by the filesystem or unzip, so, if the file really exists,
		 * read its FDR and get the name from there.
		 */

		if (fiad_tifile_get_info(tf)) {
			if (tf->format == F_V9t9) {
				memcpy(tiname, tf->fdr.filenam, 10);
				len = fiad_filename_strlen(tf->fdr.filenam);
				fiad_filename_fixup_old_filename(tiname, len, &tf->spec);
			} else if (tf->format == F_TIFILES) {
				/*
				 *	There's no information about the original filename.
				 *	Blast!  Hope/pray/assume nothing screwed up the
				 *	extended characters.
				 */
				fiad_filename_fixup_old_filename(tiname, len, &tf->spec);
			}
		}
	}

	// copy filename into FDR
	memset(tf->fdr.filenam, ' ', 10);
	memcpy(tf->fdr.filenam, tiname, len);
	fiad_logger(_L | L_2, _("setup FDR name as '%.10s'\n"), tf->fdr.filenam);

	return OS_NOERR;
}

/*	Read the FDR from a file,
	return 0 if it's bad. */
int
fiad_tifile_read_fdr(fiad_tifile * tf)
{
	OSError     err;
	u8          fdrsec[FDRSIZE];
	OSSize      len = FDRSIZE, flen;
	fdrrec		*fdr = &tf->fdr;

	/* can we read the FDR? */
	if ((err = OS_GetSize(tf->handle, &flen)) != OS_NOERR ||
		(err = OS_Seek(tf->handle, OSSeekAbs, 0)) != OS_NOERR ||
		(err = OS_Read(tf->handle, (void *) fdrsec, &len)) != OS_NOERR) {
		tf->error = err;
		fiad_logger(_L|LOG_ERROR, _("%s: could not read FDR\n"), OS_SpecToString1(&tf->spec));
		return 0;
	} 
	/* is it big enough? */
	else if (len < FDRSIZE && !unknownfileistext) {
		fiad_logger(_L|LOG_ERROR, _("%s: FDR is short\n"), OS_SpecToString1(&tf->spec));
		return 0;
	} 
	/* is it a TIFILES or V9t9 file, or text? */
	else {
		v9t9_fdr   *v9f = (v9t9_fdr *) fdrsec;
		tifiles_fdr *tif = (tifiles_fdr *) fdrsec;
		char        filename[OS_NAMESIZE];

		/*  Figure out what kind it is and convert. */
		if (memcmp(tif->sig, "\007TIFILES", 8) == 0) {
			// TIFILES fdr has no filename
			tf->format = F_TIFILES;
			OS_NameSpecToString2(&tf->spec.name, filename);
			fiad_filename_host2ti(filename, fdr->filenam);
			fdr->secsused = TI2HOST(tif->secsused);
			fdr->flags = tif->flags;
			fdr->recspersec = tif->recspersec;
			fdr->byteoffs = tif->byteoffs;
			fdr->reclen = tif->reclen;
			fdr->numrecs = SWAPTI(tif->numrecs);
			fiad_logger(_L | L_3, _("TIFILES read> secsused=%04X, flags=%x, recspersec=%x, byteoffs=%x, reclen=%d, numrecs=%04X\n"),
				   tif->secsused, tif->flags, tif->recspersec,
				   tif->byteoffs, tif->reclen, tif->numrecs);
		} else if (len == FDRSIZE 
				   && fiad_fdr_matches_v9t9_fdr(v9f, OS_SpecToString1(&tf->spec), flen)) {
			// assume V9t9

			tf->format = F_V9t9;
			memcpy(fdr->filenam, v9f->filenam, 10);
			fdr->flags = v9f->flags;
			fdr->recspersec = v9f->recspersec;
			fdr->secsused = TI2HOST(v9f->secsused);
			fdr->byteoffs = v9f->byteoffs;
			fdr->reclen = v9f->reclen;
			fdr->numrecs = SWAPTI(v9f->numrecs);
			fiad_logger(_L | L_3, _("V9t9 read> secsused=%04X, flags=%x, recspersec=%x, byteoffs=%x, reclen=%d, numrecs=%04X\n"),
				   v9f->secsused, v9f->flags, v9f->recspersec,
				   v9f->byteoffs, v9f->reclen, v9f->numrecs);
		} else if (unknownfileistext) {
			// treat file as text

			fiad_logger(_L | L_2, _("Treating '%s' as text\n"), OS_SpecToString1(&tf->spec));
			tf->format = F_TEXT;
			OS_NameSpecToString2(&tf->spec.name, filename);
			fiad_filename_host2ti(filename, fdr->filenam);
			fiad_fdr_setup(fdr, false /*program*/, ff_variable, 80, flen);
		} else {
			fiad_logger(_L | LOG_ERROR, _("File '%s' does not appear to be a V9t9 file\n"), OS_SpecToString1(&tf->spec));
			return 0;
			
		}
		tf->changedfdr = false;
		return 1;
	}
}

/*	Write FDR to file, 
	return 0 and report error if failed. */
int
fiad_tifile_write_fdr(fiad_tifile * tf)
{
	OSError     err;
	u8          fdrsec[FDRSIZE];
	OSSize      len = FDRSIZE;
	fdrrec		*fdr = &tf->fdr;

	if (tf->readonly || !tf->open) {
		fiad_logger(_LL|LOG_ERROR, _("%s: Trying to write FDR on a closed or read-only file\n"), OS_SpecToString1(&tf->spec));
		return 0;
	}

	memset(fdrsec, 0, FDRSIZE);

	//  Decide what format to write the FDR in.

	// 	don't write one for text or for an unknown file
	if (tf->format == F_TEXT || tf->format == F_UNKNOWN) {
		tf->changedfdr = false;
		return 1;
	}

	// 	write FDR in given format
	if (tf->format == F_V9t9) {
		v9t9_fdr   *v9f = (v9t9_fdr *) fdrsec;

		tf->format = F_V9t9;
		memcpy(v9f->filenam, fdr->filenam, 10);
		v9f->flags = fdr->flags;
		v9f->recspersec = fdr->recspersec;
		v9f->secsused = HOST2TI(fdr->secsused);
		v9f->byteoffs = fdr->byteoffs;
		v9f->reclen = fdr->reclen;
		v9f->numrecs = SWAPTI(fdr->numrecs);
	} else {
		tifiles_fdr *tif = (tifiles_fdr *) fdrsec;

		tf->format = F_TIFILES;
		memcpy(tif->sig, "\007TIFILES", 8);
		tif->flags = fdr->flags;
		tif->recspersec = fdr->recspersec;
		tif->secsused = HOST2TI(fdr->secsused);
		tif->byteoffs = fdr->byteoffs;
		tif->reclen = fdr->reclen;
		tif->numrecs = SWAPTI(fdr->numrecs);
/*			fiad_logger(_LL, _("TIFILES write> secsused=%04X, flags=%x, recspersec=%x, byteoffs=%x, reclen=%d, numrecs=%04X\n"),
				   tif->secsused, tif->flags, tif->recspersec,
				   tif->byteoffs, tif->reclen, tif->numrecs);
*/
	}

	if ((err = OS_Seek(tf->handle, OSSeekAbs, 0)) != OS_NOERR ||
		(err = OS_Write(tf->handle, (void *) fdrsec, &len)) != OS_NOERR) {
		tf->error = err;
		fiad_logger(_LL|LOG_ERROR, _("%s: could not write FDR\n"), OS_SpecToString1(&tf->spec));
		return 0;
	} else if (len < FDRSIZE) {
		fiad_logger(_LL|LOG_ERROR, _("%s: wrote short FDR\n"), OS_SpecToString1(&tf->spec));
		return 0;
	} else {
		tf->changedfdr = false;
		return 1;
	}
}


/*	Verify a file, by classifying its type and checking invariants */
int
fiad_tifile_verify(fiad_tifile * tf, bool fixup)
{
	OSSize      sz;
	OSError     err;

	if (!fiad_tifile_read_fdr(tf)) {
		fiad_logger(_LL | LOG_ERROR, _("FIAD server: can't read FDR from file '%s'\n"),
			  OS_SpecToString1(&tf->spec));
		return 0;
	}

	if (tf->format == F_TEXT)
		return 1;

	if (tf->format == F_UNKNOWN)
		return 0;

	err = OS_GetSize(tf->handle, &sz);

	if (err != OS_NOERR || sz < FDRSIZE) {
		fiad_logger(_LL | LOG_ERROR, _("FIAD server:  FDR is short in file '%s'\n"),
			  OS_SpecToString1(&tf->spec));
		return 0;
	}

	if (tf->format == F_V9t9) {
		sz -= FDRSIZE;
	}

	if (fixup && repairbadfiles) {
		tf->changedfdr = fiad_fdr_repair(&tf->fdr, sz);

		fiad_logger(_LL, _("FIAD server:  repaired fields of FDR in '%s'\n"),
			  OS_SpecToString1(&tf->spec));
	}

	return 1;
}

OSFileType *
fiad_get_file_type(int newfileformat)
{
	return 	
		newfileformat == F_V9t9 ? &osV99Type :
		newfileformat == F_TIFILES ? &osTIFILESType :
		&OS_TEXTTYPE;
}

/*	Initialize file pointers which keep track of current record */
int
fiad_tifile_init_file_pointers(fiad_tifile * tf)
{
	tf->cursec = tf->curoffs = tf->curnrecs = tf->currec = 0;
	tf->changed = false;
	tf->changedfdr = false;
	return 1;
}

/*	Read a sector from the file at tf->cursec. 
	If tf points to the last empty sector of file, 
	this is not an error.
	Return 0 if sector not found.  */
int
fiad_tifile_read_sector(fiad_tifile * tf)
{
	OSPos       pos;
	OSSize      sz = 256;
	OSError		err;

	if (!fiad_tifile_flush(tf))
		return 0;

	pos = tf->cursec * 256 + (tf->format != F_TEXT ? FDRSIZE : 0);

	// Try to read it...
	if ((err = OS_Seek(tf->handle, OSSeekAbs, pos)) != OS_NOERR ||
		(err = OS_Read(tf->handle, tf->sector, &sz)) != OS_NOERR) 
	{
		// bad error
		tf->error = err;
		return 0;
	} 
	else if (sz < 256) {
		if (tf->cursec == tf->fdr.secsused && sz == 0) {
			// last sector can be empty
			return 1;
		} else if (tf->format != F_TEXT || tf->cursec != tf->fdr.secsused - 1) {
			// last sector can be short, but not the other ones
			return 0;
		} else
			return 1;
	} else {
		return 1;
	}
}

/*	Write a sector to the file at tf->cursec. 
	Return 0 if sector not written.  */
int
fiad_tifile_write_sector(fiad_tifile * tf)
{
	OSPos       pos;
	OSSize      sz = 256;
	OSError		err;

	pos = tf->cursec * 256 + (tf->format != F_TEXT ? FDRSIZE : 0);

	if ((err = OS_Seek(tf->handle, OSSeekAbs, pos)) != OS_NOERR ||
		(err = OS_Write(tf->handle, tf->sector, &sz)) != OS_NOERR) 
	{
		// bad error
		tf->error = err;
		return 0;
	} else if (sz < 256) {
		// no sector can be written short
		tf->error = OS_MEMERR;		// "out of space"
		return 0;
	} else {
		tf->changed = false;
		return 1;
	}
}

/*  Set file size according to cursec and curoffs */
int
fiad_tifile_set_file_size(fiad_tifile *tf)
{
       OSPos       pos;
       OSSize      sz = 0;
       OSError         err;

       pos = tf->cursec * 256 + (tf->format != F_TEXT ? FDRSIZE : 0);

       if ((err = OS_SetSize(tf->handle, pos)) != OS_NOERR)
       {
               // bad error
               tf->error = err;
               return 0;
       } else {
               return 1;
       }
}

/*
  Open or create the file, either r/w or r/o.
  We expect the spec to have been set up.

  create && always means, delete existing file.
  create means, create file if not existing.
 */
OSError
fiad_tifile_open_file(fiad_tifile *tf, int newfileformat,
					  bool create, bool always, bool readonly)
{
	OSError     err;
	bool		creating = false;

	fiad_logger(_L | L_3, _("fiad_tifile_open_file: '%s', create: %d, always: %d, readonly: %d\n"), 
		   OS_SpecToString1(&tf->spec), create, always, readonly);

	tf->open = false;
	tf->readonly = readonly;
	tf->format = F_UNKNOWN;
	fiad_tifile_init_file_pointers(tf);

	if (create) {
		// go ahead and delete if we want a clean slate
		if (always) {
			OS_Delete(&tf->spec);
		}

		// see if file exists
		err = OS_Status(&tf->spec);

		// if not (or the file is broken), try to create it
		if (err != OS_NOERR) {
			err = OS_Create(&tf->spec, fiad_get_file_type(newfileformat));

			// if we couldn't create, error
			if (err != OS_NOERR) {
				return err;
			}
			if (err == OS_NOERR) {
				fiad_logger(_L | L_2, _("created FIAD file '%s'\n"), OS_SpecToString1(&tf->spec));
			}
			tf->format = newfileformat;
			creating = true;
		}
	}

	/* Try to open file; it should exist by now */
	err = OS_Open(&tf->spec, readonly ? OSReadOnly : OSReadWrite, 
				  &tf->handle);

	tf->open = (err == OS_NOERR);
	tf->changed = false;
	tf->changedfdr = false;

	/* Permission error? */
	if (!readonly && err == OS_PERMERR) {
		return err;
	}

	/* It was opened, verify it.
	   If verification fails, but we can create, then do so. */
	if (!creating && err == OS_NOERR) {
		if (!fiad_tifile_verify(tf, !readonly /*fixup*/)) {
			if (create) {
				fiad_logger(_L|L_2, _("Failed to verify, creating new file\n"));
				creating = true;
				tf->format = newfileformat;
				err = OS_NOERR;
			} else {
				fiad_tifile_close_file(tf);
				err = OS_FNFERR;
			}
		}
	}

	/* Existing but wanting to create? */
	if (create && creating && err == OS_NOERR) {
		fiad_logger(_L|L_2, _("Truncating file\n"));
		err = OS_SetSize(tf->handle, 0);

		/* don't write FDR here; caller uses this to determine
		   if we created the file or not */
		tf->open = (err == OS_NOERR);
		tf->changed = true;
		tf->changedfdr = true;
	}

	/* Change FDR format? */
	if (tf->format != newfileformat &&
		!keepfileformat && 
		tf->format != F_TEXT &&
		newfileformat != F_TEXT)
	{
		tf->format = newfileformat;
		tf->changedfdr = true;
	}

	return err;
}

/*
 *	Reopen a file, keeping open file information intact.
 */
OSError
fiad_tifile_reopen_file(fiad_tifile *tf, int newfileformat,
						bool readonly)
{
	OSError err;
	fiad_tifile copy = *tf;
	err = fiad_tifile_open_file(tf, 
								newfileformat, 
								false /*create*/,
								false /*always*/,
								readonly);

	tf->cursec = copy.cursec;
	tf->curoffs = copy.curoffs;
	tf->curnrecs = copy.curnrecs;
	tf->currec = copy.currec;
	fiad_tifile_read_sector(tf);

	return err;
}

/*
 *	Close a tifile.
 */
void
fiad_tifile_close_file(fiad_tifile * tf)
{
	if (!tf || !tf->open)
		return;

	/*	ignore errors */
	fiad_tifile_flush(tf);
	OS_Close(tf->handle);

	tf->open = false;
	tf->handle = (OSRef) -1;

	/* if FDR is still different, file was either readonly
	   or we changed something we should write now */
	if (tf->changedfdr) {
		fiad_tifile_rewrite_fdr(tf);
	}
}

/*
  Rewrite FDR of closed file 

  This may be used to change the format (change tf->format)
  or rewrite a fixed FDR on a readonly file.
*/
void
fiad_tifile_rewrite_fdr(fiad_tifile *tf)
{
	int newfileformat = tf->format;

	if (tf->open) 
		fiad_logger(LOG_FATAL, _("fiad_tifile_rewrite_fdr: File is open!\n"));

	if (fiad_tifile_open_file(tf, 
							  F_UNKNOWN, 
							  false /*create*/, 
							  false /*always*/, 
							  false /*readonly*/) == OS_NOERR)
	{
		if (fiad_tifile_read_fdr(tf)) {

			if ((newfileformat == F_V9t9 || newfileformat == F_TIFILES) &&
				newfileformat != tf->format) {
				fiad_logger(_L|L_2, _("%s: changing file format to %s\n"),
					   OS_SpecToString1(&tf->spec),
					   newfileformat == F_V9t9 ? "V9t9" : "TIFILES");

				tf->format = newfileformat;
			} else if (!fiad_tifile_verify(tf, true /*fixup*/) 
					   && repairbadfiles) {
				fiad_logger(_L|L_2, _("%s: rewriting damaged FDR\n"),
					   OS_SpecToString1(&tf->spec));
			} else {
			    goto dont_write;
			}
			fiad_tifile_write_fdr(tf);
		}
	dont_write:
		fiad_tifile_close_file(tf);
	}
}

/*	Flush dirty buffers */
int
fiad_tifile_flush(fiad_tifile *tf)
{
	if (!tf->open)
		return 0;

	if (tf->readonly)
		return 1;

	if (tf->changed) {
		if (!fiad_tifile_write_sector(tf))
			return 0;
	}
	if (tf->changedfdr) {
		if (!fiad_tifile_write_fdr(tf))
			return 0;
	}
	return 1;
}

/*	Change the current sector */
int
fiad_tifile_seek_to_sector(fiad_tifile *tf, int secnum)
{
	if (!fiad_tifile_flush(tf))
		return 0;
	tf->cursec = secnum;
	tf->curoffs = 0;
	return 1;
}

/*	Seek, logically, to the end of file */
int
fiad_tifile_seek_to_end(fiad_tifile * tf)
{
	if (!fiad_tifile_seek_to_sector(tf, FDR_LASTSEC(&tf->fdr)))
		return 0;
	tf->curoffs = tf->fdr.byteoffs;
	tf->curnrecs = 0;
	tf->currec = 0;
	return fiad_tifile_read_sector(tf);
}

/*	Read a record from a file (all but PROGRAM files)

	Return 0 for success, 1 for EOF, and -1 for hardware failure
*/
int fiad_tifile_read_record(fiad_tifile *tf, u8 *data, u8 *reclen)
{
	if ((tf->fdr.flags & ff_variable) && tf->format != F_TEXT) {

		/* read variable data record */
		u8          len;

	  retry_var_read:
		//  Get a new sector?
		if (!tf->curoffs) {
			// any more sectors?
			if (tf->cursec < tf->fdr.secsused) {
				if (!fiad_tifile_read_sector(tf)) {
					return -1;
				}
			} else {
				return 1;	// EOF
			}
		}

		len = tf->sector[tf->curoffs];

		// 0xff means end of sector
		if (len == 0xff) {
			if (!fiad_tifile_seek_to_sector(tf, tf->cursec+1)) {
				return -1;
			}
			goto retry_var_read;
		}

		if (len > tf->fdr.reclen) {
			//fiad_logger(_L | _("FIAD ERROR:  length of record on disk is longer than maximum"));
			// this is legal; we only set tf->fdr.reclen bytes,
			// but advance the pointer 'len' bytes.
		}

		if ((u32) tf->curoffs + (u32) len >= 256) {
			fiad_logger(_LL | LOG_ERROR,
				  _("FIAD ERROR:  file appears corrupt [curoffs=%d, len=%d]\n\n\n"),
				  tf->curoffs, len);
			len = 255 - tf->curoffs;
			tf->cursec++;
		}

		*reclen = len > tf->fdr.reclen ? tf->fdr.reclen : len;
		memcpy(data, tf->sector + tf->curoffs + 1, *reclen);
		tf->curoffs += len + 1;	// length byte
	
	} else if ((tf->fdr.flags & ff_variable) && tf->format == F_TEXT) {

		/* read variable data from text file */
		int         len;
		int         lastbyte, strln;
		char        str255[256];
		bool		at_eoln, at_eof;

		fiad_logger(_L | L_3, "Variable read from F_TEXT: #%d/%d %d/%d\n", tf->cursec,
			 tf->fdr.secsused, tf->curoffs, tf->fdr.byteoffs);
		*str255 = 0;
		strln = 0;
		at_eoln = at_eof = false;

		// Keep reading from the file, bringing in new sectors, 
		// until we hit an eoln.
		// Do not fail at EOF; let this stop the next read.

		lastbyte = (tf->cursec < tf->fdr.secsused - 1 ? 256 : tf->fdr.byteoffs);

		fiad_logger(_L | L_3, "lastbyte=%d\n", lastbyte);

		while (!at_eoln && !at_eof) {

			if (!tf->curoffs) {
				// get next sector
				if (tf->cursec < tf->fdr.secsused) {
					if (!fiad_tifile_read_sector(tf)) {
						at_eof = true;
						break;	
					}
				} else {
					at_eof = true;
					break;
				}
				lastbyte = (tf->cursec < tf->fdr.secsused - 1 
							? 256 : tf->fdr.byteoffs);
			}

			if (tf->sector[tf->curoffs] == '\r' ||
				tf->sector[tf->curoffs] == '\n') 
			{
				if (tf->sector[tf->curoffs] == '\r' &&
					(tf->curoffs+1 < lastbyte && tf->sector[tf->curoffs+1] == '\n'))
					tf->curoffs++;
				at_eoln = true;
			}
			else
			{
				if (strln < 255)
					str255[strln++] = tf->sector[tf->curoffs];
			}

			if (tf->curoffs + 1 >= lastbyte) {
				tf->curoffs = 0;
				if (!fiad_tifile_seek_to_sector(tf, tf->cursec+1)) {
					at_eof = true;
				}
			}
			else
				tf->curoffs++;
		}

		if (at_eof && !strln) {
			return 1;	// eof
		}

		/*
		 * 	We can only copy up to 255 bytes into a record (and even then,
		 *	it will be truncated according to the record size in the PAB).
		 *  Perhaps add an option to split long lines into smaller records?
		 */
		str255[strln] = 0;

		fiad_logger(_L | L_2, "F_TEXT: '%s'\n", str255);

		len = strln;
		if (len > tf->fdr.reclen) {
			//fiad_logger(_L | _("FIAD ERROR:  length of record on disk is longer than maximum"));
			// this is legal; we only set tf->fdr.reclen bytes,
			// but advance the pointer 'len' bytes.
		}

		*reclen = len > tf->fdr.reclen ? tf->fdr.reclen : len;
		memcpy(data, str255, *reclen);

	} else {

		/* read fixed data record */
		u16         newsec;

		fiad_logger(_L | L_3, _("fixed read, wanted %d, max is %d\n\n"), tf->currec,
			 tf->fdr.numrecs);
		if (tf->currec >= tf->fdr.numrecs) {
			return 1; 	// EOF
		}

		newsec = tf->currec / tf->fdr.recspersec;

		// different sector?
		if (newsec != tf->cursec) {
			if (!fiad_tifile_seek_to_sector(tf, newsec)) {
				return -1;
			}
			//  Get a new sector
			if (!fiad_tifile_read_sector(tf)) {
				return -1;
			}
		}

		tf->curnrecs = tf->currec % tf->fdr.recspersec;
		tf->curoffs = tf->curnrecs * tf->fdr.reclen;

//		fiad_logger(_L|LOG_USER, "%p, %04X, %04X, %d\n", tf->pab, PABTOVDP(tf->pab), tf->pab.addr, tf->fdr.reclen);

		*reclen = tf->fdr.reclen;
		memcpy(data, tf->sector + tf->curoffs, *reclen);

		tf->currec++;
	}
	return 0;
}

/*	Write a record to the file (all but PROGRAM files)

	Return 0 for success, -1 for hardware failure, 1 for disk full 
*/
int
fiad_tifile_write_record(fiad_tifile *tf, u8 *data, u8 reclen)
{
	if ((tf->fdr.flags & ff_variable) && tf->format != F_TEXT) {

		/* write variable data record */
		u8          len;

	  retry_var_write:
		// write a new sector?
		if (!tf->curoffs) {
			// clip file to current position
			if (tf->cursec + 1 != tf->fdr.secsused) {
				tf->fdr.secsused = tf->cursec + 1;
				tf->fdr.numrecs = tf->cursec + 1;
				if (!fiad_tifile_set_file_size(tf)) {
					return -1;		// hardware failure
				}
			}
		}

		len = reclen;

		// I think this is standard
		if (len > tf->fdr.reclen)
			len = tf->fdr.reclen;

		// need room for record, length, and 0xff eos byte
		if (len + 1 + tf->curoffs >= 255) {
			if (!fiad_tifile_seek_to_sector(tf, tf->cursec+1)) {
				return tf->error == OS_MEMERR /*disk full*/ ? 1 : -1;
			}
			goto retry_var_write;
		}

		tf->sector[tf->curoffs] = len;
		memcpy(tf->sector + tf->curoffs + 1, data, len);
		tf->changed = true;
		tf->changedfdr = true;

		tf->curoffs += len + 1;	// update EOF ptr

		tf->sector[tf->curoffs] = 0xff;	// end of sector marker

		tf->fdr.byteoffs = tf->curoffs;	// update FDR

	} else if ((tf->fdr.flags & ff_variable) && tf->format == F_TEXT) {

		/* write variable data to text file */
		u8          len;
		int         strln, cpyln;

		len = reclen;

		// I think this is standard
		if (len > tf->fdr.reclen)
			len = tf->fdr.reclen;

		strln = len + 1;		// to copy

	  retry_textvar_write:
		// write a new sector?
		if (!tf->curoffs) {
			// clip file to current position
			if (tf->cursec + 1 != tf->fdr.secsused) {
				tf->fdr.secsused = tf->cursec + 1;
				tf->fdr.numrecs = tf->cursec + 1;
				if (!fiad_tifile_set_file_size(tf)) {
					return -1;		// hardware failure
				}
			}
		}

		// fill up sector to the end,
		// keep one byte for newline
		cpyln = tf->curoffs + strln < 256 ? strln : 255 - strln;
		memcpy(tf->sector + tf->curoffs, data + len - strln, cpyln);
		tf->changed = true;
		tf->changedfdr = true;

		strln -= cpyln;
		if (!strln)
#ifndef UNDER_MACOS
			tf->sector[tf->curoffs + cpyln - 1] = '\n';
#else
			tf->sector[tf->curoffs + cpyln - 1] = '\r';
#endif

		tf->curoffs += cpyln;

		if (!tf->curoffs) {
			if (!fiad_tifile_seek_to_sector(tf, tf->cursec+1)) {
				return tf->error == OS_MEMERR /*disk full*/ ? 1 : -1;
			}
			goto retry_textvar_write;
		}

		tf->fdr.byteoffs = tf->curoffs;	// update FDR

	} else {

		/* write fixed data record */
		u16         newsec;

		fiad_logger(_L | L_3, _("fixed write, at %d, cur is %d\n"), tf->currec,
			 tf->fdr.numrecs);

		newsec = tf->currec / tf->fdr.recspersec;

		// different sector?
		if (newsec != tf->cursec || tf->fdr.secsused == 0) {
			if (!fiad_tifile_seek_to_sector(tf, newsec)) {
				return tf->error == OS_MEMERR /*disk full*/ ? 1 : -1;
			}

			//  Get the current sector, growing file if needed
			if (tf->fdr.secsused <= tf->cursec) {
				// update sector count
				tf->fdr.secsused = tf->cursec + 1;
				tf->changedfdr = true;
			} else {
				// read old sector
				if (!fiad_tifile_read_sector(tf)) {
					return -1;
				}
			}
		}

		tf->curnrecs = tf->currec % tf->fdr.recspersec;
		tf->curoffs = tf->curnrecs * tf->fdr.reclen;

		memcpy(tf->sector + tf->curoffs, data, reclen);
		memset(tf->sector + tf->curoffs + reclen, 0, tf->fdr.reclen - reclen);
		tf->changed = true;

		tf->currec++;

		// update # records if needed
		if (tf->currec > tf->fdr.numrecs) {
			tf->fdr.numrecs = tf->currec;
			tf->fdr.byteoffs = 0;	// for fixed files
			tf->changedfdr = true;
		}
	}
	return 0;
}

/*	Seek to a given record.  For variable files, only 0 is accepted. 

	Return 0 for success, -1 for hardware failure, 1 for disk full
*/
int
fiad_tifile_seek_to_record(fiad_tifile *tf, u16 recnum)
{
	/* Variable files always RESTORE to beginning of file */
	if (tf->fdr.flags & ff_variable) {
		if (!fiad_tifile_seek_to_sector(tf, 0)) {
			return tf->error == OS_MEMERR /*disk full*/ ? 1 : -1;
		}
		fiad_tifile_init_file_pointers(tf);

	} else {
		if (!fiad_tifile_flush(tf)) {
			return tf->error == OS_MEMERR /*disk full*/ ? 1 : -1;
		}
		tf->currec = recnum;
	}
	return 0;
}

/*	Read binary data from file at current position.

	Returns 0 for success, -1 for hardware failure, 1 for EOF.
*/
int
fiad_tifile_read_binary_image(fiad_tifile *tf, u8 *data, u16 maxread, u16 *gotread)
{
	u16 toread;

	/* take minimum size */
	toread = maxread;

	fiad_logger(_L | L_2, _("file has >%04X or %d sectors\n\n"), tf->fdr.secsused,
		   tf->fdr.secsused);
	fiad_logger(_L | L_2, _("going to read at most %d bytes from file with size %d\n"), toread,
			 FDR_FILESIZE(&tf->fdr));
	
	/* bytes read */
	*gotread = 0;

	while (toread) {
		u16 copy = toread < 256 ? toread : 256;

		if (tf->cursec >= tf->fdr.secsused) {
			// end of file
			return 1;
		}

		if (!fiad_tifile_read_sector(tf)) {
			return -1;
		}

		memcpy(data, tf->sector, copy);
		data += copy;
		*gotread += copy;
		toread -= copy;

		if (!fiad_tifile_seek_to_sector(tf, tf->cursec+1)) {
			return -1;
		}
	}
	return 0;
}


/*	Write binary data to a file at the current position 

	Return 0 for success, -1 for hardware failure, 1 for disk full
*/
int
fiad_tifile_write_binary_image(fiad_tifile *tf, u8 *data, u16 towrite, u16 *written)
{
	u16 secs = 0;

	fiad_logger(_L | L_2, _("trying to write %d bytes to file\n\n"), towrite);

	*written = 0;
	while (*written < towrite) {
		u16 copy = towrite - (*written) < 256 ? towrite - (*written) : 256;

		if (tf->cursec >= tf->fdr.secsused) {
			tf->fdr.secsused = tf->cursec + 1;
			tf->fdr.byteoffs = 0;
			tf->changedfdr = true;
		}

		memcpy(tf->sector, data, copy);
		tf->changed = true;

		if (!fiad_tifile_seek_to_sector(tf, tf->cursec+1))
			return tf->error == OS_MEMERR /*disk full*/ ? 1 : -1;

		data += copy;
		*written += copy;
	}

	// last sector may be partial
	tf->fdr.byteoffs = *written & 0xff;
	return 0;
}

/*	Get info about a file in tf,
	return 1 for success
*/
int
fiad_tifile_get_info(fiad_tifile *tf)
{
	OSError err;
	int ret;

	err = fiad_tifile_open_file(tf, 
								F_UNKNOWN,
								false /*create*/, 
								false /*always*/, 
								true /*readonly*/);
	if ((tf->error = err) != OS_NOERR)
		return 0;
	
	// don't return right away in case we're
	// repairing the FDR
	ret = fiad_tifile_verify(tf, false /*fixup*/);

	fiad_tifile_close_file(tf);
	return ret;
}

/*	Free a catalog */
void
fiad_catalog_free_catalog(fiad_catalog *cat)
{
	int x;

	if (!cat->filenames) {
		return;
	}

	for (x = 0; x < cat->entries; x++) {
		xfree(cat->filenames[x]);
	}
	xfree(cat->filenames);

	cat->filenames = 0L;

	xfree(cat->fdrs);
	cat->fdrs = 0L;

	xfree(cat->index);
	cat->index = 0L;
}

static int sort_filename(const void *a, const void *b)
{
	return strcasecmp(*(const char **)a, *(const char **)b);
}

/*	Read a catalog from a directory */
OSError
fiad_catalog_read_catalog(fiad_catalog *cat, const char *wildcard)
{
	char wildpath[OS_PATHSIZE];
	OSSpec spec, *sptr;
	OSError err;
	OSSize	blocksize, totalblocks, freeblocks;
	bool	iswild;
	int         x = 0;
	fiad_tifile	tf;


	fiad_catalog_free_catalog(cat);

	/* Get disk info */

	err = OS_MakeSpec(wildcard, &spec, &iswild);
	if (err != OS_NOERR)
		return err;

	cat->path = spec.path;

	if ((err = OS_GetDiskStats(&spec.path, &blocksize, 
							   &totalblocks, &freeblocks)) != OS_NOERR) {
		blocksize = 256;
		totalblocks = 360;
		freeblocks = 0;
	}
	cat->total_sectors = totalblocks / 256 * blocksize;
	cat->free_sectors = freeblocks / 256 * freeblocks;

	/* Get file list */

	cat->entries_max = cat->entries = 0;
	cat->filenames = 0;

	/* If 'iswild' is true, use directory matching helper */
	if (!iswild) {
		OS_PathSpecToString2(&spec.path, wildpath);
		strcat(wildpath, "*");
	} else {
		strcpy(wildpath, wildcard);
	}

	/* Any matches? */

	if ((sptr = OS_MatchPath(wildpath)) == NULL) {
		return OS_FNFERR;
	}

	fiad_logger(_L | L_2, _("reading dir\n"));

	do {
		/* validate file and get info */
		if (!OS_IsFile(sptr) ||
			fiad_tifile_setup_spec_with_spec(&tf, sptr) != OS_NOERR ||
			!fiad_tifile_get_info(&tf) ||
			tf.format == F_UNKNOWN)
		{
			fiad_logger(_L|L_1, _("Skipping '%s' which doesn't appear to be a V9t9 file\n"), 
						OS_NameSpecToString1(&sptr->name));
			continue;
		}

		/* more memory needed? */
		if (x >= cat->entries_max) {
			char **names;
			fdrrec *fdrs;
			int *index;
				
			names = (char **) xrealloc(cat->filenames,
									   (cat->entries_max + 128) * sizeof(char *));
			fdrs = (fdrrec *) xrealloc(cat->fdrs,
									   (cat->entries_max + 128) * sizeof(fdrrec));
			index = (int *) xrealloc(cat->index,
									 (cat->entries_max + 128) * sizeof(int));
			cat->entries_max += 128;
			cat->filenames = names;
			cat->fdrs = fdrs;
			cat->index = index;
		}

		cat->filenames[x] = xstrdup(OS_NameSpecToString1(&sptr->name));
		fiad_logger(_L | L_2, _("got file '%s'\n\n"), cat->filenames[x]);
		cat->fdrs[x] = tf.fdr;
		cat->index[x] = x;
		x++;
		cat->entries++;
	} while ((sptr = OS_MatchPath(0L)) != 0L);

	return OS_NOERR;
}

/*	Sort a catalog */

static fiad_catalog *_sort_catalog;
static bool _sort_order;

static int _sort_disk(const void *a, const void *b)
{
	// disk order
	return _sort_order ? ((char *)a - (char *)b) :
		((char *)b - (char *)a);
}

static int _sort_filename(const void *a, const void *b)
{
	const char *a1 = _sort_catalog->filenames[*(const int *)a];
	const char *b1 = _sort_catalog->filenames[*(const int *)b];

	return _sort_order ? strcasecmp(a1, b1) : strcasecmp(b1, a1);
}

static int _sort_type(const void *a, const void *b)
{
	const fdrrec *a1 = &_sort_catalog->fdrs[*(const int *)a];
	const fdrrec *b1 = &_sort_catalog->fdrs[*(const int *)b];
	int diff;

	diff = (a1->flags & (ff_internal | ff_program | ff_variable)) -
		   (b1->flags & (ff_internal | ff_program | ff_variable));

	if (!_sort_order) diff = -diff;

	if (!diff)
	{
		diff = _sort_order ? a1->reclen - b1->reclen :
			b1->reclen - a1->reclen;
	}
	return diff;
}

static int _sort_size(const void *a, const void *b)
{
	const fdrrec *a1 = &_sort_catalog->fdrs[*(const int *)a];
	const fdrrec *b1 = &_sort_catalog->fdrs[*(const int *)b];

	return _sort_order ? (FDR_FILESIZE(a1) - FDR_FILESIZE(b1)) :
		(FDR_FILESIZE(b1) - FDR_FILESIZE(a1));
}

void
fiad_catalog_sort_catalog(fiad_catalog *cat, int sort_by, bool ascending)
{
	int (*func)(const void *a, const void *b);

	switch (sort_by)
	{
	case FIAD_CATALOG_SORT_BY_DISK:	func = _sort_disk; break;
	case FIAD_CATALOG_SORT_BY_NAME:	func = _sort_filename; break;
	case FIAD_CATALOG_SORT_BY_TYPE:	func = _sort_type; break;
	case FIAD_CATALOG_SORT_BY_SIZE:	func = _sort_size; break;
	default:	fiad_logger(_L|LOG_FATAL, _("Invalid catalog sort '%d'\n"), sort_by); 
	}

	_sort_catalog = cat;
	_sort_order = ascending;
	qsort(cat->index, cat->entries, sizeof(int), func);
	_sort_catalog = NULL;
}

/*	Get file information from catalog */
int
fiad_catalog_get_file_info(fiad_catalog *cat, int index, fdrrec *fdr)
{
	if (index < 0 || index >= cat->entries)
		return 0;

	*fdr = cat->fdrs[cat->index[index]];
	return 1;
}

/*
 *	Return string giving standard name for type of file, i.e., "DIS/VAR 80"
 */

static struct {
	u8 fdr_flags;
	const char *name;
}	fiad_tifile_file_types[5] =
{
	{0, 						"DIS/FIX"},
	{ff_program,				"PROGRAM"},
	{ff_internal,				"INT/FIX"},
	{ff_variable,				"DIS/VAR"},
	{ff_variable+ff_internal,	"INT/VAR"}
};

const char *
fiad_catalog_get_file_type_string(fdrrec *fdr)
{
	int i;
	static char type_string[12];

	for (i = 0; i < 5; i++) {
		if ((fdr->flags & (ff_program | ff_internal | ff_variable)) ==
			fiad_tifile_file_types[i].fdr_flags) 
		{
			// program files don't have record length
			if (fdr->flags & ff_program) {
				strcpy(type_string, 
					   fiad_tifile_file_types[i].name);
			} else {
				sprintf(type_string, "%s %d", 
						fiad_tifile_file_types[i].name,
						fdr->reclen);
			}
			return type_string;
		}
	}
	return "???";
}
