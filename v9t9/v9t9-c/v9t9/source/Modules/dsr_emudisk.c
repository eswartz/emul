/*  dsr_emudisk.c					-- V9t9 module for file-in-a-directory DSR

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

#include <fcntl.h>
#include <stdio.h>
#include <errno.h>
#include <ctype.h>

#include "v9t9_common.h"
#include "v9t9.h"
#include "cru.h"
#include "command.h"
#include "memory.h"
#include "dsr.h"
#include "vdp.h"
#include "9900.h"
#include "moduleconfig.h"
#include "v9t9.h"
#include "timer.h"

#include "pab.h"
#include "fiad.h"

#define _L	 LOG_EMUDISK | LOG_INFO
#define _LL	 LOG_EMUDISK | LOG_INFO | LOG_USER

#define EMUDISKCRUBASE	0x1000

/******************************************/

#define VDP_PTR(x)	FLAT_MEMORY_PTR(md_video, (x)&0x3fff)
#define VDP_ADDR(p)	((p) - VDP_PTR(0))

/*	These are the codes we add to OP_DSR to specify
	which DSR function we're emulating. */

/*	If these numbers change, fix DSR ROM files */
enum {
	/* this first group doubles as device codes */
	D_DSK = 0,				// standard file operation on DSK.XXXX.[YYYY]
	D_DSK1 = 1,			// standard file operation on DSK1.[YYYY]
	D_DSK2 = 2,			// ...
	D_DSK3 = 3,			// ...
	D_DSK4 = 4,			// ...
	D_DSK5 = 5,			// ...

	D_INIT = 6,			// initialize disk DSR
	D_DSKSUB = 7,			// subroutines

	D_SECRW = 7,			// sector read/write    (10)
	D_FMTDISK = 8,			// format disk          (11)
	D_PROT = 9,			// file protection      (12)
	D_RENAME = 10,			// rename file          (13)
	D_DINPUT = 11,			// direct input file    (14)
	D_DOUTPUT = 12,		// direct output file   (15)
	D_16 = 13,				// set the VDP end of buffer (like call files) (16)

	D_FILES = 14			// setup # files (CALL FILES)
};


/*	Error codes for subroutines */
enum {
	es_okay = 0,
	es_outofspace = 0x4,
	es_cantopenfile = 0x1,
	es_filenotfound = 0x1,
	es_badfuncerr = 0x7,	// format
	es_fileexists = 0x7,	// rename
	es_badvalerr = 0x1,
	es_hardware = 0x6		// ??? made this up
};


#define	MAXFILES	9			/* max # open files */
#define	MAXDRIVE	5			/* max drive number */

/******************************************/

char        emudiskfilename[OS_NAMESIZE] = "emudisk.bin";
char        emu2diskfilename[OS_NAMESIZE] = "emu2disk.bin";

u8          emudiskdsr[8192];
OSPathSpec  emudiskpath[5];

int         allowlongcatalogs = 0;

u16			rambase;	   	// base of scratch-pad RAM
u16         vdpnamebuffer;	// location of VDP name buffer (for compatibility)

/*	Info maintained for open files  */
typedef struct fiad_pabfile
{
	u8			*fnptr;		// pointer to filename part of PAB in VDP RAM,
							// the key for distinguishing open files
	pabrec		pab;		// copy of PAB used for local operations
	fiad_tifile	tf;			// info about open file

	bool		is_catalog;	// does pab represent catalog?
}	fiad_pabfile;

/*	Our array of open files */
static fiad_pabfile files[MAXFILES];

/*	Our disk catalogs (via DSKx. or sector access) */
static fiad_catalog DskCat[MAXDRIVE];
/*	Flags set by timer, used to throttle directory reads */
static bool DskCatFlag[MAXDRIVE];
static int DskCatTag[MAXDRIVE];

// last sector read on each drive, used for catalog --
// = if we read a sector beyond the catalog limit
// or re-read sectors 0 or 1, we force a reread of catalog
// from local disk
// = set to 0xffff when known invalid

static u16 last_sector_read[MAXDRIVE];


/*	Translations for DSKx. files' catalog function
	from FDR type to catalog value (pairs) */
u8          DrcTrans[5][2] = { 
	{0, 1}, {ff_program, 5},
	{ff_internal, 3}, {ff_variable, 2},
	{ff_variable + ff_internal, 4}
};

/*	Dirty the VDP where we messed around */
static void
VDPUpdate(u16 addr, u16 len)
{
	while (len--)
		vdp_touch(addr++);
}

/*	Copy data safely to VDP */
static      u16
VDPCopy(u16 addr, u8 * data, u32 len)
{
	while (len--) {
		domain_write_byte(md_video, addr++, *data++);
	}
	return addr;
}

/*	Copy data safely from VDP */
static      u16
VDPRead(u8 * data, u16 addr, u32 len)
{
	while (len--) {
		*data++ = domain_read_byte(md_video, addr++);
	}
	return addr;
}



////////////////////////////////////

/*
  Set error in pabfile's PAB.
 */
static void
pab_set_error(fiad_pabfile *pf, u8 paberror)
{
	pab_set_vdp_error(pf->fnptr, paberror);
	pf->pab.pflags = (pf->pab.pflags & ~m_error) | (paberror & m_error);
}

static void
pab_vdp_error(u8 *fn, OSError err, char *str, u8 paberror)
{
	pab_set_vdp_error(fn, paberror);
	if (paberror) {
		module_logger(&emuDiskDSR, _L | L_1, _("%.*s: got PAB error %d [%s (%s)]\n"), 
			   *fn, fn+1,
			   paberror >> 5,
			   str, err ? OS_GetErrText(err) : "");
	}
}

static void
pab_error(fiad_pabfile *pf, OSError err, char *str, u8 paberror)
{
	pab_set_error(pf, paberror);
	pab_vdp_error(pf->fnptr, err, str, paberror);
}

/*
  Match an open pab file.

  We distinguish open files by their fnptr being set.
*/
static fiad_pabfile *
pab_find_open_file(u8 *fn)
{
	fiad_pabfile     *pf = files;

	while (pf < files + MAXFILES) {
		if (pf->fnptr == fn && (pf->tf.open || pf->is_catalog))
			return pf;
		pf++;
	}
	return NULL;
}

/*
  Allocate a new pab file.
*/
static fiad_pabfile *
pab_get_new_file(u8 *fn)
{
	fiad_pabfile	*pf = files;

	while (pf < files + MAXFILES) {
		if (pf->fnptr == fn) {
			// already open, close it
			if (pf->tf.open) {
				module_logger(&emuDiskDSR, _L|LOG_ERROR, _("%.*s:  existing PAB not closed\n"),
					   *fn, fn+1);
				fiad_tifile_close_file(&pf->tf);
			}
			pf->is_catalog = false;
			return pf;
		} else if (pf->fnptr == NULL) {
			pf->fnptr = fn;
			pf->is_catalog = false;
			fiad_tifile_clear(&pf->tf);
			return pf;
		}
		pf++;
	}
	return NULL;
}

/*
 *	Copy PAB from VDP into file struct before a file operation
 *
 *	If opening is true, allow flags/reclen to be updated
 */
static void
pab_fetch_from_vdp(fiad_pabfile *pf, bool opening)
{
	pabrec mypab;
	VDPRead((void *)&mypab, VDP_ADDR(pf->fnptr-9), PABREC_SIZE);

	pf->pab.opcode = mypab.opcode;
	pf->pab.addr = TI2HOST(mypab.addr);
	pf->pab.charcount = mypab.charcount;
	pf->pab.recnum = TI2HOST(mypab.recnum);
	pf->pab.scrnoffs = mypab.scrnoffs;
	pf->pab.namelen = mypab.namelen;

	/* 
	   TI BASIC appears to trash these bytes after
	   opening the file.  We stubbornly continue to
	   use them, however, so don't reread from the
	   real PAB after opening. 
	*/
	if (opening)
	{
		pf->pab.pflags = mypab.pflags;
		pf->pab.preclen = mypab.preclen;

		module_logger(&emuDiskDSR, _L | L_3, _("PAB contents: flags=>%02X, reclen=%d, addr=>%04X, charcount=%d, recnum=%d\n"),
			   pf->pab.pflags, pf->pab.preclen,
			   pf->pab.addr, pf->pab.charcount, pf->pab.recnum);
	}
	else
	{
		module_logger(&emuDiskDSR, _L | L_3, _("PAB contents: addr=>%04X, charcount=%d, recnum=%d\n"),
			   pf->pab.addr, pf->pab.charcount, pf->pab.recnum);

	}
}

/*
 *	Copy PAB from file struct into VDP after a file operation
 */
static void
pab_store_to_vdp(fiad_pabfile *pf)
{
	pabrec mypab;

	mypab.opcode = pf->pab.opcode;
	mypab.addr = HOST2TI(pf->pab.addr);
	mypab.charcount = pf->pab.charcount;
	mypab.recnum = HOST2TI(pf->pab.recnum);
	mypab.scrnoffs = pf->pab.scrnoffs;
	mypab.namelen =  pf->pab.namelen;

	mypab.pflags = pf->pab.pflags;
	mypab.preclen = pf->pab.preclen;

	VDPCopy(VDP_ADDR(pf->fnptr-9), 
			(u8 *)&mypab, 
			PABREC_SIZE);
}

///////////////

#if 0
static u8   sub_error_map[] = {
	es_outofspace,				// E_nobuffer
	es_hardware,				// E_cantmakespec [disk paths are bad]
	es_filenotfound,			// E_filenotfound
	es_badvalerr,				// E_cantmodifyfile
	es_hardware,				// E_formaterror
	es_hardware,				// E_shortread  [should find data]
	es_outofspace,				// E_shortwrite
	es_hardware,				// E_sectornotfound [should find data]
	es_hardware,				// E_unexpectederror [whatever]
	es_badfuncerr,				// E_illegalop
	es_hardware					// E_endoffile
};
#endif

static void
sub_set_error(u8 errcode)
{
	memory_write_byte(rambase+0x50, errcode);
	if (errcode) {
		module_logger(&emuDiskDSR, _L | L_1, _("got subroutine error %d\n"),
			  errcode);
	}
}

static void
sub_error(fiad_tifile * tf, OSError err, char *str, int errcode)
{
	sub_set_error( errcode);
	if (errcode) {
		module_logger(&emuDiskDSR, _L | L_1, _("got subroutine error %d [%s (%s)]\n"),
			  errcode, str, err ? OS_GetErrText(err) : "");
		fiad_tifile_close_file(tf);
	}
}

/*	Close a PAB file */
static void
pab_close_file(fiad_pabfile *pf)
{
	// not true for catalogs
	if (pf->tf.open) {
		fiad_tifile_close_file(&pf->tf);
	}
	pf->fnptr = 0L;
}

/*	Close all files */
static void
pab_close_all_files(void)
{
	fiad_pabfile *pf = files;
	int dsk;

	while (pf < files + MAXFILES) {
		pab_close_file(pf);
		pf++;
	}

	for (dsk = 0; dsk < MAXDRIVE; dsk++) {
		last_sector_read[dsk] = 0xffff;
	}
}


/////////////////////////////////////

/*	Read the FDR from a file,
	return 0 if it's bad. */
static int
pab_read_fdr(fiad_pabfile * pf)
{
	int	ret = fiad_tifile_read_fdr(&pf->tf);
	if (!ret) {
		pab_set_error(pf, e_hardwarefailure);
	}
	return ret;
}

/*	Write FDR to file, 
	return 0 and report error if error. */
static int
pab_write_fdr(fiad_pabfile * pf)
{
	int ret = fiad_tifile_write_fdr(&pf->tf);
	
	if (!ret) {
		pab_set_error(pf, e_hardwarefailure);
	}
	return ret;
}

/*
	Compare the FDR with the PAB, making sure the
	PAB operation is compatile with the FDR, and that record sizes and
	file types match.  

	If mismatching, report the error and return 0, else return 1. 
*/
static int
pab_compare_fdr_and_pab(fiad_pabfile * pf)
{
	u8          pflags, fflags;
	char       *pptr;
	u8          len;
	pabrec		*pab = &pf->pab;
	fiad_tifile	*tf = &pf->tf;

	/* compare name */
	len = fiad_filename_strlen(tf->fdr.filenam);

	pptr = (char *) pf->fnptr + pab->namelen;
	while (pptr > (char *) pf->fnptr && *(pptr - 1) != '.')
		pptr--;

	if (strncasecmp(tf->fdr.filenam, pptr, len))
		module_logger(&emuDiskDSR, _LL | LOG_WARN,
			  _("filename in FDR doesn't match filename ('%.*s' | '%.*s')\n"),
			  len, tf->fdr.filenam, pab->namelen - (pptr - (char *)pf->fnptr), pptr);

	pflags = pab->pflags;
	fflags = tf->fdr.flags;

	/* program files are easy */
	if (pab->opcode == f_load || pab->opcode == f_save)
		if (fflags & ff_program)
			return 1;

	/* both must be fixed or variable */
	if (!!(pflags & fp_variable) == !!(fflags & ff_variable)) {
		/*  fixup the PAB if it doesn't know record size */
		if (!pab->preclen)
			pab->preclen = tf->fdr.reclen;

		/* both must have same record length */
		if (pab->preclen != tf->fdr.reclen) {
			module_logger(&emuDiskDSR, _L | L_1, _("DSKcomparefdrandpab:  record length differs\n"));
		} else {
			/* and no "var,relative" files */
			if ((pflags & (fp_relative | fp_variable)) ==
				(fp_relative | fp_variable)) {
				module_logger(&emuDiskDSR, _L | L_1, _("DSKcomparefdrandpab:  var + relative file\n"));
			} else
				return 1;
		}
	} else {
		module_logger(&emuDiskDSR, _L | L_1, _("DSKcomparefdrandpab: fixed/variable flag differs\n"));
	}

	pab_error(pf, 0, _("file type mismatch on file open"), e_badfiletype);
	return 0;
}

/*	Compare the FDR with the ten-byte filename at fptr,
	making sure the filename matches.  Always return 1. */
static int
DSKcomparefdrandsub(fiad_tifile * tf, char *fptr)
{
	if (tf->format == F_V9t9 && strncasecmp(tf->fdr.filenam, fptr, 10)) {
		module_logger(&emuDiskDSR, _LL,
			 _("DSKcomparefdrandsub:  filenames don't match ('%.*s' | '%.*s')\n"),
			 10, tf->fdr.filenam, 10, fptr);
	}
	return 1;
}

/*
	Create an FDR from a PAB.
	The filename is in the FDR already.
*/
static void
pab_make_fdr(fiad_pabfile * pf)
{
	fiad_tifile *tf = &pf->tf;
	pabrec *pab = &pf->pab;

	fiad_fdr_setup(&tf->fdr, 
				   pab->opcode == f_save, 
				   ((pab->pflags & fp_variable) ? ff_variable : 0) |
				   ((pab->pflags & fp_internal) ? ff_internal : 0),
				   pab->preclen,
				   0 /*size*/);

	pab->preclen = tf->fdr.reclen;
}

/*	Read a sector from the PAB file */
static int
pab_read_sector(fiad_pabfile * pf)
{
	int ret = fiad_tifile_read_sector(&pf->tf);
	if (!ret) {
		char msg[64];
		sprintf(msg, _("reading sector %d"), pf->tf.cursec);
		pab_error(pf, pf->tf.error, msg, e_hardwarefailure);
	}
	return ret;
}

/*	Write current sector to PAB file */
static int
pab_write_sector(fiad_pabfile * pf)
{
	int ret = fiad_tifile_write_sector(&pf->tf);
	if (!ret) {
		char msg[64];
		sprintf(msg, _("writing sector %d"), pf->tf.cursec);
		pab_error(pf, 0, msg, e_hardwarefailure);
	}
	return ret;
}

////////////////////

/*	
	Routine to create the OSSpec for the file
	using the PAB and the current disk, and copy the
	name into the file's FDR.
	
	Return 0 for success,
		1 if the filename is empty, which indicates
			a catalog request on a disk, or
		-1 if the path was invalid.
*/
static int
pab_setup_file(fiad_pabfile *pf, u8 dev, u8 *fn)
{
	/*  0x8356 holds a pointer into VDP RAM to the end
	   of the device name (RS232|, DSK|., DSK1|.ed) */
	u8          len;
	u16         fnaddr;
	char       *fname;
	OSError     err;

	pf->fnptr = fn;
	fiad_tifile_clear(&pf->tf);

	len = *fn;					/* length of device+filename */
	module_logger(&emuDiskDSR, _L | L_2, _("getfilespec_pab:  pab len = %d\n"), len);
	fnaddr = memory_read_word(rambase+0x56) + 1;	/* addr of filename (skip period) */
	//module_logger(&emuDiskDSR, _L | L_2, _("getfilespec_pab:  fnaddr++ at 0x8356 = %20.20s       \n"),
	//VDP_PTR(0) + fnaddr);
	len -= memory_read_word(rambase+0x54) + 1;	/* minus length of device + period */
	module_logger(&emuDiskDSR, _L | L_2, _("getfilespec_pab:  length of device 0x8354 = %04X\n"),
		 memory_read_word(rambase+0x54));
	fname = (char *) VDP_PTR(fnaddr);

	if (memchr(fname, '.', len) != NULL) {
		pab_error(pf, 0, _("bad characters in filename"), e_badfiletype);
		return -1;
	}

	err = fiad_tifile_setup_spec_with_file(&pf->tf, 
										   &emudiskpath[dev - 1], 
										   fname, 
										   len);

	if (err != OS_NOERR) {
		pab_error(pf, err, _("couldn't make spec"), e_badfiletype);
		return -1;
	} else if (len == 0)
		return 1;				/* no filename means catalog */
	else
		return 0;
}

/*	Create the host OSSpec for the ten-character filename,
	as seen in a subroutine call. */
static int
sub_setup_file(fiad_tifile *tf, u8 dsknum, u16 fnaddr)
{
	u8          len = 0;
	const char	*chptr = (char *)VDP_PTR(fnaddr);

	while (len < 10 && (*chptr != ' ' &&
						*chptr != 0 &&
						*chptr != '.')) {
		len++;
		chptr++;
	}

	// a dot is illegal
	if (*chptr == '.') {
		sub_error(tf, 0, _("bad characters in filename"), es_filenotfound);
		return -1;
	}

	if (fiad_filename_to_spec(&emudiskpath[dsknum - 1], 
							  (char *) VDP_PTR(fnaddr), 
							  len,
							  &tf->spec) != OS_NOERR) 
		return -1;
	else if (len == 0)
		return 1;				/* no filename */
	else
		return 0;
}

////////////////////////////////////

/*
	Match a emulated disk by volume name.  
	Return length of volume plus dot, and disk device (>0) or 0 if not matched.  
	We're passed a name terminated by a period, space, or 0. 
*/
static u8
matchdrive(char *volume, int *len)
{
	u8          dev;

	*len = 1;
	while (volume[*len - 1] != '.' &&
		   volume[*len - 1] != ' ' && volume[*len - 1] != 0)
		(*len)++;

	/*  we don't match DSK1 and DSK2 if
	   the real disk is going */
	dev = dsr_is_emu_disk(1) ? 1 : 3;

	while (dev <= MAXDRIVE) {
		char        path[OS_PATHSIZE];
		char       *ptr;
		char        tivol[10];
		int         len;
		int         idx;
		int         matched;

		OS_PathSpecToString2(&emudiskpath[dev - 1], path);

		module_logger(&emuDiskDSR, _L | L_1, _("trying to match '%s'\n"), path);

		/* clear trailing slash, colon, etc */
		path[strlen(path) - 1] = 0;

		/* ptr has leaf name */
		ptr = (char *) OS_GetFileNamePtr(path);

		len = fiad_filename_host2ti(ptr, tivol);
		VDPCopy(vdpnamebuffer, (u8 *)tivol, 10);

		/* compare */
		idx = 0;
		matched = 1;
		while (idx < 10 && volume[idx] &&
			   volume[idx] != '.' && volume[idx] != 0) {
			/*  don't be picky about capitalization,
			   since these things are hard to figure out ;) */
			if (tolower(volume[idx]) != tolower(tivol[idx])) {
				matched = 0;
				break;
			}
			idx++;
		}
		if (matched)
			return dev;

		dev++;
	}
	return 0;
}

////////////////////////////////////

#if 0
#pragma mark -
#pragma mark "Catalog read"
#endif

/*	Convert and push an integer into a TI floating point record:
	[8 bytes] [0x40+log num] 9*[sig figs, 0-99]
	Return pointer past end of float.
*/
static u8  *
int_to_tifloat(unsigned x, u8 * buf)
{
	u8         *start;

	*buf++ = 8;					// bytes in length
	memset(buf, 0, 8);
	if (x == 0)
		return buf + 8;

	start = buf;
	buf[0] = 0x3F;
	while (x > 0) {
		buf[0]++;
		memmove(buf + 2, buf + 1, 6);
		buf[1] = x % 100;
		x /= 100;
	}

	return buf + 8;
}


/*
  Setup a pab file for use as a catalog iterator.
 */
static int
DSKinitcatalog(fiad_pabfile * pf, u8 dsk)
{
	OSError err;
	int max;
	char path[OS_PATHSIZE];
	OSPathSpec *spec;

	pf->is_catalog = dsk--;
	spec = &emudiskpath[dsk];

	/* Don't re-read catalog more often than necessary. */
	if (!OS_EqualPathSpec(spec, &DskCat[dsk].path) 
		|| DskCatFlag[dsk]) 
	{
		module_logger(&emuDiskDSR, _L|L_1, _("Re-reading catalog for DSK%d\n"),dsk+1);
		OS_PathSpecToString2(spec, path);
		if ((err = fiad_catalog_read_catalog(&DskCat[dsk], path)) != OS_NOERR) {
			pab_error(pf, err, _("couldn't open directory for catalog"),
					  e_hardwarefailure);
			return 0;
		}

		/* Sort by name */
		fiad_catalog_sort_catalog(&DskCat[dsk], 
								  FIAD_CATALOG_SORT_BY_NAME, 
								  true /*ascending*/);

		/* Set timer to prevent over-reading catalog
		   (Microsoft Multiplan does this) */
		DskCatFlag[dsk] = 0;
		TM_SetEvent(DskCatTag[dsk], TM_HZ*100, 0, 0 /*flags*/, 
					&DskCatFlag[dsk]);
	}

	/* Restrict number of entries we return */
	max = (allowlongcatalogs ? 32767 : 127);
	if (DskCat[dsk].entries > max) DskCat[dsk].entries = max;

	return 1;
}

/*	For a file-based catalog, we are guaranteed 128 records. 
	The first is the volume, and the rest are for files.  Each
	record is a maximum of 38 bytes long, but is fixed at
	the file's open record length by padding it with zeroes.
	
	For a volume record, the format is:
		[length of volume] "volume"
		[8] [float: 0]
		[8] [float: total # sectors] 
		[8] [float: remaining # sectors]
		[0...]
		
	For a file record, the format is:
		[length of filename] "filename"
		[8] [float: type]
		[8] [float: # sectors used + FDR]
		[8] [float: record length] 

	Assume that any errors are due to reading a non-V9t9
	file, and return 0 for those.
*/
static int
DSKreadcatalog(fiad_pabfile *pf, u8 dsk, u8 *cr)
{
	OSError     err;
	fdrrec		fdr;
	u8         *ptr;
	fiad_catalog *cat = &DskCat[dsk - 1];

	module_logger(&emuDiskDSR, _L | L_1, _("reading catalog entry\n"));
	memset(cr, 0, pf->pab.preclen);

	// volume record?
	if (pf->pab.recnum == 0) {

		/*  Get volume name from path. */
		ptr = cr;
		*ptr = fiad_path_disk2ti(&cat->path, (char *)ptr + 1);

		// copy disk name to name buffer
		VDPCopy(vdpnamebuffer, ptr+1, 10);

		ptr += *ptr + 1;

		// zero field
		ptr = int_to_tifloat(0, ptr);

		// total space
		ptr = int_to_tifloat(cat->total_sectors, ptr);

		// free space
		ptr = int_to_tifloat(cat->free_sectors, ptr);

		return 1;
	}

	// read file record; restrict it to 127 entries
	// in case naive programs will die...
	err = 0;
	if (pf->pab.recnum >= cat->entries) {
		module_logger(&emuDiskDSR, _L | L_1, _("DSKreadcatalog: reached end of directory\n"));

		// make an empty record     
		ptr = cr;
		*ptr++ = 0;
		ptr = int_to_tifloat(0, ptr);
		ptr = int_to_tifloat(0, ptr);
		ptr = int_to_tifloat(0, ptr);
		return 1;
	}
	
	// Get file info
	if (!fiad_catalog_get_file_info(cat, pf->pab.recnum, &fdr))
		return 0;

	// first field is the string representing the
	// file or volume name
	ptr = cr;
	*ptr = fiad_filename_strlen(fdr.filenam);
	memcpy(ptr+1, fdr.filenam, *ptr);
	VDPCopy(vdpnamebuffer, (u8 *)fdr.filenam, 10);
	ptr += *ptr + 1;

	// second field is file type
	{
		int         idx;

		for (idx = 0; idx < sizeof(DrcTrans) / sizeof(DrcTrans[0]); idx++)
			if (DrcTrans[idx][0] ==
				(fdr.flags & (ff_internal | ff_program | ff_variable))) {
				ptr = int_to_tifloat(DrcTrans[idx][1], ptr);
				break;
			}
		// no match == program
		if (idx >= sizeof(DrcTrans) / sizeof(DrcTrans[0])) {
			ptr = int_to_tifloat(1, ptr);
		}
	}

	// third field is file size, one sector for fdr
	ptr = int_to_tifloat(1 + fdr.secsused, ptr);

	// fourth field is record size
	ptr = int_to_tifloat(fdr.reclen, ptr);

	return 1;
}


////////////////////////////////////

/*	These DSKXxx routines handle file operations on PABs. */

static void
DSKClose(u8 dev, u8 *fn);

static void
DSKOpen(u8 dev, u8 *fn)
{
	fiad_pabfile	*pf;
	fiad_tifile	*tf;
	pabrec	*pab;

	int			ret;
	OSError     err;

	module_logger(&emuDiskDSR, _L | L_1, "DSKOpen\n");

	/* get a pabfile for PAB */
	if ((pf = pab_get_new_file(fn)) == NULL) {
		pab_vdp_error(fn, 0, _("no free files"), e_outofspace);
		return;
	}

	tf = &pf->tf;
	pab = &pf->pab;

	/* read PAB */
	pab_fetch_from_vdp(pf, true /*opening*/);

	/* sanity check */
	if (pab->preclen == 255 && (pab->pflags & fp_variable)) {
		pab_error(pf, 0, _("can't have variable record size of 255"),
					e_badopenmode);
		goto open_error;
	}

	/*  Fix PAB reclen to default if needed  */
//	if (!pab->preclen) {
//		pab->preclen = 80;
//	}

	/* get local file spec */
	ret = pab_setup_file(pf, dev, fn);

	/* failure? */
	if (ret < 0)
		goto open_error;

	/* catalog request? */
	if (ret > 0) {
		/* make sure it's a legal open mode */
		if ((pab->pflags & (fp_internal | m_input)) != (fp_internal | m_input)) {
			pab_error(pf, 0, _("bad catalog open flags"), e_badopenmode);
			goto open_error;
		}

		if (!DSKinitcatalog(pf, dev))
			goto open_error;
		fiad_tifile_init_file_pointers(tf);
		pf->pab.preclen = 38;
	
		/*  store the PAB */
		pab_store_to_vdp(pf);
		return;
	}

	/* normal file open */

	pab_set_error(pf, 0);

	switch (pab->pflags & m_openmode) {

		// update/append mode: either create new file, or
		// open existing one.

	case m_append:
		// can't append to FIXED file
		if (!(pab->pflags & fp_variable) /*&&
		   !(pab->pflags & fp_internal)*/) {
			pab_error(pf, 0, _("can't append to FIXED file"), e_badopenmode);
			goto open_error;
		}
		// fall through

	case m_update:
		err = fiad_tifile_open_file(tf, 
									newfileformat,
									true /*create*/, 
									false /*always*/,
									false /*readonly*/);
		if (err != OS_NOERR) {
			pab_error(pf, err, _("could not open for update"), 
					  err == OS_PERMERR ? e_readonly :
					  e_badopenmode);
			goto open_error;
		}

		// tf->changed set if we know we created it;
		// if not, and we can't read the FDR, create it anyway
		if (!tf->changed && pab_read_fdr(pf)) {
			module_logger(&emuDiskDSR, _L | L_1, _("read FDR successfully\n"));
			if (!pab_compare_fdr_and_pab(pf))
				goto open_error;
		} else {
			module_logger(&emuDiskDSR, _L | L_1, _("setting up new FDR\n"));
			pab_set_error(pf, 0);
			pab_make_fdr(pf);
			if (!pab_write_fdr(pf)) {
				pab_set_error(pf, e_hardwarefailure);
				goto open_error;
			}
			my_assert(pab_compare_fdr_and_pab(pf));
		}
		
		// when appending, start at end
		if ((pab->pflags & m_openmode) == m_append) {
			fiad_tifile_seek_to_end(tf);
		}

		// cache sector (may not exist for new file)
		if (!fiad_tifile_read_sector(tf)) {
			pab_set_error(pf, e_hardwarefailure);
			goto open_error;
		}
		break;

		// output mode: always create new file
	case m_output:
		err = fiad_tifile_open_file(tf,
									newfileformat,
									true /*create*/,
									true /*always*/,
									false /*readonly*/);
		if (err != OS_NOERR) {
			pab_error(pf, err, _("could not open for output"),
					  err == OS_PERMERR ? e_readonly :
					  e_badopenmode);
			goto open_error;
		}

		module_logger(&emuDiskDSR, _L | L_1, _("setting up new FDR\n"));
		pab_make_fdr(pf);
		my_assert(pab_compare_fdr_and_pab(pf));
		break;

		// input mode: always open existing file
	case m_input:
		err = fiad_tifile_open_file(tf, 
									F_UNKNOWN,
									false /*create*/, 
									false /*always*/,
									true /*readonly*/);
		if (err != OS_NOERR) {
			pab_error(pf, err, _("could not open for input"), 
					  e_badopenmode);
			goto open_error;
		}

		if (!pab_compare_fdr_and_pab(pf))
			goto open_error;
		
		// cache first sector
		if (!fiad_tifile_read_sector(tf)) {
			pab_set_error(pf, e_hardwarefailure);
			goto open_error;
		}
		break;
	}

	/*  store the PAB back to VDP */
	pab_store_to_vdp(pf);
	return;

open_error:
	pf->fnptr = NULL;
}

void
DSKClose(u8 dev, u8 *fn)
{
	fiad_pabfile     *pf;

	module_logger(&emuDiskDSR, _L | L_1, "DSKClose\n");

	// if the file isn't open, um...
	if ((pf = pab_find_open_file(fn)) == NULL)
		pab_set_vdp_error(fn, e_badfiletype);
	else
		pab_close_file(pf);
}


#if 0
#pragma mark "File read"
#endif

static void
DSKRead(u8 dev, u8 *fn)
{
	fiad_pabfile	*pf;
	fiad_tifile		*tf;
	pabrec 			*pab;
	u8				len;
	int				err;

	module_logger(&emuDiskDSR, _L | L_1, "DSKRead\n");

	if ((pf = pab_find_open_file(fn)) == NULL) {
		pab_vdp_error(fn, 0, _("DSKRead:  error due to not finding open file"), e_badfiletype);
		return;
	}

	tf = &pf->tf;
	pab = &pf->pab;

	/* check operation */
	if ((pab->pflags & m_openmode) == m_output ||
		(pab->pflags & m_openmode) == m_append) {
		pab_error(pf, 0, _("read from append/output file"), e_illegal);
		return;
	}

	/* get pab info */
	pab_fetch_from_vdp(pf, false /*opening*/);

	/* catalog read? */
	if (pf->is_catalog) {
		u8          cr[256];
		int         x;

		DSKreadcatalog(pf, dev, cr);
		VDPCopy(pab->addr, cr, pab->preclen);

		module_logger(&emuDiskDSR, _L | L_3, _("dump of catalog record:"));
		for (x = 0; x < pab->preclen; x++) {
			logger(_L |  0 | L_3, "%02X ", cr[x]);
		}
		logger(_L | L_3, "\n");

		tf->currec++;
		pab->charcount = pab->preclen;
		pab->recnum = tf->currec;

		pab_store_to_vdp(pf);
		return;
	}

	/* normal file read */

	/* for fixed file, limit record # and update */
	if (!(pab->pflags & fp_variable)) {
		pf->pab.recnum &= 0x7fff;
		tf->currec = pf->pab.recnum;
		err = fiad_tifile_read_record(tf, VDP_PTR(pab->addr), &len);
		pf->pab.recnum = tf->currec;
	} else {
		/* variable file */
		err = fiad_tifile_read_record(tf, VDP_PTR(pab->addr), &len);
	}

	if (err == 0) {
		VDPUpdate(pab->addr, len);
		pab->charcount = len;
	} else if (err < 0) {
		// hardware failure
		pab_set_error(pf, e_hardwarefailure);
	} else {
		// end of file
		pab_set_error(pf, e_endoffile);
		pab_close_file(pf);
	}

	/* save changed PAB info */
	pab_store_to_vdp(pf);
}

static void
DSKWrite(u8 dev, u8 *fn)
{
	fiad_pabfile	*pf;
	fiad_tifile		*tf;
	pabrec 			*pab;
	int				err;

	module_logger(&emuDiskDSR, _L | L_1, "DSKWrite\n");

	if ((pf = pab_find_open_file(fn)) == NULL) {
		pab_vdp_error(fn, 0, _("DSKWrite:  error due to not finding open file"), e_badfiletype);
		return;
	}

	tf = &pf->tf;
	pab = &pf->pab;

	/* check operation */
	if ((pab->pflags & m_openmode) == m_input) {
		pab_error(pf, 0, _("writing to input file"), e_illegal);
		return;
	}

	if (pf->is_catalog) {
		pab_error(pf, 0, _("writing to catalog"), e_illegal);
		return;
	}

	/* get pab info */
	pab_fetch_from_vdp(pf, false /*opening*/);

	/* for fixed file, limit record # and update */
	if (!(pab->pflags & fp_variable)) {
		pf->pab.recnum &= 0x7fff;
		tf->currec = pf->pab.recnum;
		err = fiad_tifile_write_record(tf, VDP_PTR(pab->addr), pab->preclen);
		pf->pab.recnum = tf->currec;
	} else {
		/* variable file */
		err = fiad_tifile_write_record(tf, VDP_PTR(pab->addr), pab->charcount);
	}

	if (err == 0) {
		// no error
	} else if (err < 0) {
		// hardware failure
		pab_set_error(pf, e_hardwarefailure);
	} else if (err > 0) {
		// disk full
		pab_set_error(pf, e_outofspace);
	}

	/* save changed PAB info */
	pab_store_to_vdp(pf);
}

static void
DSKSeek(u8 dev, u8 *fn)
{
	fiad_pabfile	*pf;
	fiad_tifile		*tf;
	pabrec 			*pab;
	int				err;

	module_logger(&emuDiskDSR, _L | L_1, "DSKSeek\n");

	if ((pf = pab_find_open_file(fn)) == NULL) {
		pab_vdp_error(fn, 0, _("DSKSeek:  error due to not finding open file"), e_badfiletype);
		return;
	}

	tf = &pf->tf;
	pab = &pf->pab;

	if (pf->is_catalog) {
		pab_error(pf, 0, _("DSKSeek:  error due to seeking catalog"), e_illegal);
		return;
	}

	/* get pab info */
	pab_fetch_from_vdp(pf, false /*opening*/);

	/* variable files are restored to record 0,
	   and this can only be done in update/input mode */
	if ((pab->pflags & fp_variable) && 	
		((pab->pflags & m_openmode) == m_append || 
		 (pab->pflags & m_openmode) == m_output)) {
			pab_error(pf, 0, _("append/output mode can't seek"), e_illegal);
			return;
	}

	if (!(pab->pflags & fp_variable)) {
		// maximum record # is 32767
		pab->recnum &= 0x7fff;
		err = fiad_tifile_seek_to_record(tf, pab->recnum);
	} else {
		pab->recnum = 0;
		err = fiad_tifile_seek_to_record(tf, 0);
	}

	if (err == 0) {
		// no error
	} else {
		pab_set_error(pf, (err > 0) ? e_outofspace : e_hardwarefailure);
	}

	/* save changed PAB info */
	pab_store_to_vdp(pf);
}

#if 0
#pragma mark "LOAD MEMORY IMAGE"
#endif

static void
DSKLoad(u8 dev, u8 *fn)
{
	fiad_pabfile pf;
	fiad_tifile	*tf;
	pabrec	*pab;

	int			ret;
	OSError     err;
	u16			len;

	module_logger(&emuDiskDSR, _L | L_0, "DSKLoad\n");

	tf = &pf.tf;
	pab = &pf.pab;

	/* get local file spec */
	ret = pab_setup_file(&pf, dev, fn);

	/* failure? */
	if (ret < 0)
		return;

	/* catalog request? */
	if (ret > 0) {
		pab_error(&pf, 0, _("can't open catalog as binary"), e_illegal);
		return;
	}

	module_logger(&emuDiskDSR, _L | L_1, _("trying to open binary image\n"));

	err = fiad_tifile_open_file(tf,
								F_UNKNOWN,
								false /*create*/, 
								false /*always*/,
								true /*readonly*/);
	if (err != OS_NOERR) {
		pab_error(&pf, err, _("could not open for input"), 
				  e_badfiletype);
		return;
	}

	/* read PAB */
	pab_fetch_from_vdp(&pf, true /*opening*/);

	if (!pab_compare_fdr_and_pab(&pf))
		return;
		
	pab_set_error(&pf, 0);

	ret = fiad_tifile_read_binary_image(tf, VDP_PTR(pab->addr), 
										pab->recnum, &len);
	VDPUpdate(pab->addr, len);

	if (ret >= 0) {
		// no error or EOF (which is okay for DSKLoad)

		// EJS 050221: nope, this isn't documented behavior
		// and it breaks TI Artist! when loading printer files.
		//pab->recnum = len;
	} else {
		// failure
		pab_set_error(&pf, e_hardwarefailure);
	}

	module_logger(&emuDiskDSR, _L | L_1, _("read %d bytes to >%04X\n"),
				  pab->recnum, pab->addr);
	
	/* save changed PAB info */
	pab_store_to_vdp(&pf);

	/* close file */
	pab_close_file(&pf);
}

#if 0
#pragma mark "SAVE MEMORY IMAGE"
#endif

static void
DSKSave(u8 dev, u8 *fn)
{
	fiad_pabfile pf;
	fiad_tifile	*tf;
	pabrec	*pab;

	int			ret;
	OSError     err;
	u16			len;

	module_logger(&emuDiskDSR, _L | 0, "DSKSave\n");

	tf = &pf.tf;
	pab = &pf.pab;

	/* get local file spec */
	ret = pab_setup_file(&pf, dev, fn);

	/* failure? */
	if (ret < 0)
		return;

	/* catalog request? */
	if (ret > 0) {
		pab_error(&pf, 0, _("can't save catalog as binary"), e_illegal);
		return;
	}

	module_logger(&emuDiskDSR, _L | L_1, _("trying to save binary image\n"));

	err = fiad_tifile_open_file(tf,
								newfileformat,
								true /*create*/,
								true /*always*/,
								false /*readonly*/);
	if (err != OS_NOERR) {
		pab_error(&pf, err, _("could not open for output"),
				  err == OS_PERMERR ? e_readonly :
				  e_badfiletype);
		return;
	}

	/* read PAB */
	pab_fetch_from_vdp(&pf, true /*opening*/);

	module_logger(&emuDiskDSR, _L | L_1, _("setting up new FDR\n"));
	pab_make_fdr(&pf);
	my_assert(pab_compare_fdr_and_pab(&pf));

	pab_set_error(&pf, 0);

	ret = fiad_tifile_write_binary_image(tf, VDP_PTR(pab->addr), 
										 pab->recnum, &len);

	if (ret == 0) {
		// no error
	} else {
		// failure
		pab_set_error(&pf, ret > 0 ? e_outofspace : e_hardwarefailure);
	}

	/* no pab changes */

	/* close file */
	pab_close_file(&pf);
}

static void
DSKDelete(u8 dev, u8 *fn)
{
	fiad_pabfile pf;
	fiad_tifile *tf;
	pabrec		*pab;
	int         ret;
	OSError     err;

	module_logger(&emuDiskDSR, _L | 0, "DSKDelete\n");

	tf = &pf.tf;
	pab = &pf.pab;

	/* read PAB */
	pab_fetch_from_vdp(&pf, true /*opening*/);

	/* get local file spec */
	ret = pab_setup_file(&pf, dev, fn);

	/* failure? */
	if (ret < 0)
		return;

	/* catalog request? */
	if (ret > 0) {
		pab_error(&pf, 0, _("can't delete catalog"), e_illegal);
		return;
	}

	module_logger(&emuDiskDSR, _L | L_1, _("trying to delete file\n"));

	pab_set_error(&pf, 0);

	err = OS_Delete(&tf->spec);
	if (err != OS_NOERR) {
		pab_error(&pf, err, _("deleting file\n"),
					err == OS_PERMERR ? e_readonly : e_badfiletype);
	}
}

static void
DSKScratch(u8 dev, u8 *fn)
{
	module_logger(&emuDiskDSR, _L | 0, "DSKScratch");

	// nothing supports this
	pab_set_vdp_error(fn, e_illegal);
}

/*	Get status bits for a file */
static u8
fiad_tifile_get_status(fiad_tifile *tf, u8 status)
{
	bool is_protected = true;
	OSSize total, free, blocksize;
	
	if ((tf->fdr.flags & ff_protected) ||
		OS_CheckProtection(&tf->spec, &is_protected) == OS_NOERR)
		status |= is_protected ? st_readonly : 0;

	if (tf->open) {
		if (tf->fdr.flags & ff_internal)
			status |= st_internal;

		if (tf->fdr.flags & ff_program)
			status |= st_program;

		if (tf->fdr.flags & ff_variable)
			status |= st_variable;
	}
	
	if (OS_GetDiskStats(&tf->spec.path, &blocksize, &total, &free) == OS_NOERR
		&& total == free)
		status |= st_diskfull;
		
	// check EOF
	if (status & st_variable) {

		if (tf->cursec + 1 >= tf->fdr.secsused &&
			tf->sector[tf->curoffs] == 0xff) {
			status |= st_eof;
			module_logger(&emuDiskDSR, _L | L_2, _("DSKStatus:  EOF on variable file"));
		}
	} else {
		if (tf->currec >= tf->fdr.numrecs) {
			status |= st_eof;
			module_logger(&emuDiskDSR, _L | L_2, _("DSKStatus:  EOF on fixed file\n"));
		}
	}
	return status;
}

static void
DSKStatus(u8 dev, u8 *fn)
{
	int file_was_open = 0;
	fiad_pabfile *pf, local;
	fiad_tifile *tf;
	pabrec *pab;
	u8 status = 0;
	int ret;
	OSError err;

	module_logger(&emuDiskDSR, _L | L_2, "DSKStatus\n");

	if ((pf = pab_find_open_file(fn)) == NULL) {

		/* file is not open, open it now */

		module_logger(&emuDiskDSR, _L | L_2, _("DSKStatus:  no PAB file yet...\n"));

		/* use local file */
		pf = &local;
		tf = &pf->tf;
		pab = &pf->pab;

		/* get local file spec */
		ret = pab_setup_file(pf, dev, fn);

		/* failure? */
		if (ret < 0) {
			module_logger(&emuDiskDSR, _L | L_2, _("DSKStatus:  file not found\n"));
			status = st_filenotfound;
			pab->scrnoffs = status;
			pab_store_to_vdp(pf);
			return;
		}

		/* catalog request? */
		if (ret > 0) {
			module_logger(&emuDiskDSR, _L | L_2, _("DSKStatus:  catalog status\n"));
			status = st_internal | st_readonly;
			pab->scrnoffs = status;
			pab_store_to_vdp(pf);
			return;
		}

		module_logger(&emuDiskDSR, _L | L_2, _("DSKStatus:  opening file\n"));

		/* open file to look at it */
		err = fiad_tifile_open_file(tf, 
									F_UNKNOWN,
									false /*create*/, 
									false /*always*/,
									true /*readonly*/);
		if (err != OS_NOERR) {
			module_logger(&emuDiskDSR, _L | L_2, _("DSKStatus:  file not found\n"));
			status |= st_filenotfound;
			pab->scrnoffs = status;
			pab_store_to_vdp(pf);
			return;
		}
	} else {

		/* file was already open */

		module_logger(&emuDiskDSR, _L | L_2, _("DSKStatus:  file was open\n"));

		tf = &pf->tf;
		pab = &pf->pab;

		/* get pab info */
		pab_fetch_from_vdp(pf, false /*opening*/);

		// they might have seeked on us
		if (!(pab->pflags & fp_variable)) {
			pab->recnum &= 0x7fff;
			fiad_tifile_seek_to_record(tf, pab->recnum);
		}

		file_was_open = 1;
	}

	status |= fiad_tifile_get_status(tf, status);

	module_logger(&emuDiskDSR, _L | L_1, _("DSKStatus:  flags=%02X\n\n"), status);

	pab->scrnoffs = status;
	pab_set_error(pf, 0);

	/* save changed PAB bits */
	pab_store_to_vdp(pf);

	/* close file if we just opened it */
	if (!file_was_open) {
		pab_close_file(pf);
	}
}

////////////////////////////////////

#if 0
#pragma mark -
#pragma mark "Fake catalog sectors routines"
#endif

static void 
setup_catalog(u8 dsk)
{
	char path[OS_PATHSIZE];
	OS_PathSpecToString2(&emudiskpath[dsk], path);
	fiad_catalog_read_catalog(&DskCat[dsk], path);
}

/*
 *	Don't always free the catalog, since fiad.c will 
 *	free it for us next time we read it
 */
static void
free_catalog(u8 dsk)
{
	fiad_catalog_free_catalog(&DskCat[dsk]);
}

static void
create_volume_sector(u8 dsk, u8 *sector)
{
	int         len;
	fiad_catalog *cat = &DskCat[dsk];

	module_logger(&emuDiskDSR, _L | L_1, _("creating catalog sector 0\n"));

	// reread catalog at sector 0
	if (!DskCat[dsk].entries ||
		!OS_EqualPathSpec(&emudiskpath[dsk], &DskCat[dsk].path) ||
		(last_sector_read[dsk] != 0 &&
		last_sector_read[dsk] != 1)) 
	{
		setup_catalog(dsk);
	}

	memset(sector, 0, 256);

	// use directory name as volume name
	len = fiad_path_disk2ti(&cat->path, (char *)sector);

	// copy to VDP name buffer
	VDPCopy(vdpnamebuffer, sector, 10);

	// fill in info for a 90k disk
	sector[10] = 0x1; sector[11] = 0x68;
	sector[12] = 9;			// secs per track
	sector[13] = 'D';
	sector[14] = 'S';
	sector[15] = 'K';
	sector[17] = 40;		// # tracks
	sector[18] = 1;			// # sides?  (may be density)
	sector[19] = 1;			// density?  (may be sides)

	// disk is full
	memset(sector + 56, 0xff, 256 - 56);
}

static void
create_index_sector(u8 dsk, u16 * sector)
{
	int x,l;

	module_logger(&emuDiskDSR, _L | L_1, _("creating catalog sector 1\n"));

	// reread catalog at sector 1
	if (!DskCat[dsk].entries ||
		!OS_EqualPathSpec(&emudiskpath[dsk], &DskCat[dsk].path) ||
		(last_sector_read[dsk] != 0 &&
		last_sector_read[dsk] != 1)) {
		setup_catalog(dsk);
	}

	memset(sector, 0, 256);

	/* entries were already sorted */
	l = DskCat[dsk].entries > 127 ? 127 : DskCat[dsk].entries;
	for (x = 0; x < l; x++) {
		u16 sec = x+2;
		sector[x] = HOST2TI(sec);
	}
}

static int
create_fdr_sector(u8 dsk, u16 ent, u8 * sector)
{
	fdrrec fdr;
	v9t9_fdr   *v9f;
	fiad_catalog *cat = &DskCat[dsk];

	module_logger(&emuDiskDSR, _L | L_1, _("creating catalog directory sector %d\n"), ent + 2);

	// reread catalog at unknown FDR sector
	if (!DskCat[dsk].entries ||
		!OS_EqualPathSpec(&emudiskpath[dsk], &DskCat[dsk].path) ||
		(last_sector_read[dsk] == 0xffff)) 
	{
		setup_catalog(dsk);
	}

	if (!fiad_catalog_get_file_info(cat, ent, &fdr))
		return 0;

	memset(sector, 0, 256);

	v9f = (v9t9_fdr *) sector;

	memcpy(v9f->filenam, fdr.filenam, 10);
	VDPCopy(vdpnamebuffer, (u8 *)fdr.filenam, 10);

	v9f->flags = fdr.flags;
	v9f->recspersec = fdr.recspersec;
	v9f->secsused = HOST2TI(fdr.secsused);
	v9f->byteoffs = fdr.byteoffs;
	v9f->reclen = fdr.reclen;
	v9f->numrecs = SWAPTI(fdr.numrecs);
	return 1;
}

#if 0
#pragma mark "Read/write sector"
#endif

/*	Read/write sector.
	Only allow access to the volume sector, index sector,
	and catalog sectors; all of by faking it.  The
	catalogs DskCat[] contain a list of files scanned
	in the directory when sector 1 is read.  If any sector
	greater than 1 is read without reading sector 1 first,
	there will be invalid sector info returned.

in:		byte 0x834C: drive
		byte 0x834d: 0xff=read, 0x00=write
		word 0x834E: vdp addr
		word 0x8350: sector #
	
out:	word 0x834A: sector read
		byte 0x8350: status
*/

static void
DskSectorRW(u8 dev, u8 rw, u16 addr, u16 secnum)
{
	u8          sector[256];

	module_logger(&emuDiskDSR, _L | L_1, "DskSectorRW\n");

	// don't write!
	if (!rw) {
		module_logger(&emuDiskDSR, _LL | 0, _("ignoring sector write request"));
		sub_set_error(0);
		return;
	}
	if (secnum == 0) {
		// make volume sector
		create_volume_sector(dev - 1, sector);
	} else if (secnum == 1) {
		// make index sector
		create_index_sector(dev - 1, (u16 *) sector);
	} else /* (secnum >= 2) */ {
		// make fdr sector
		if (!create_fdr_sector(dev - 1, secnum - 2, sector)) {
			free_catalog(dev-1);
			memset(sector, 0, 256);
			secnum = 0xffff;
			return;
		}
	}
	last_sector_read[dev-1] = secnum;

	VDPCopy(addr, sector, 256);
	sub_set_error(0);
}

#if 0
#pragma mark "Format disk"
#endif

/*	Format disk. 

in:		byte 0x834C: drive
		byte 0x834d: # tracks
		word 0x834e: track layout
		byte 0x8350: density (1,2)
		byte 0x8351: sides (1,2)

out:	byte 0x8350: status
*/
static void
DskFormatDisk(u8 dev, u8 tracks, u16 tinfo, u16 densid)
{
	module_logger(&emuDiskDSR, _L | 0, "DskFormatDisk\n");
	sub_set_error(e_illegal);
}

#if 0
#pragma mark "Modify file protection"
#endif

/*	Modify file protection 

in:		byte 0x834c: drive
		byte 0x834d: 0=none, <>0=locked
		word 0x834e: filename
*/
static void
DskProtect(u8 dev, u8 lock, u16 fname, u16 addr2)
{
	fiad_tifile    local, *tf = &local;
	OSError err;

	module_logger(&emuDiskDSR, _L | 0, "DskProtect\n");

	if (sub_setup_file(tf, dev, fname) != 0) {
		sub_set_error(es_badfuncerr /*es_filenotfound */ );
		return;
	}

	err = fiad_tifile_open_file(tf, 
								F_UNKNOWN,
								false /*create*/, 
								false /*always*/,
								false /*readonly*/);
	if (err != OS_NOERR) {
		sub_error(tf, err, _("could not open to modify protection"), 
				  err == OS_PERMERR ? es_hardware :
				  es_badfuncerr /* es_filenotfound */);
		return;
	}

	DSKcomparefdrandsub(tf, (char *) VDP_PTR(fname));

	// update FDR
	tf->fdr.flags = (tf->fdr.flags & ~ff_protected) | (lock ? ff_protected : 0);
	tf->changedfdr = true;

	fiad_tifile_close_file(tf);

	// do NOT modify the real file

	sub_set_error(es_okay);
}

#if 0
#pragma mark "Rename file"
#endif

/*	Rename file

in:		byte 0x834c: drive
		word 0x834e: new name
		word 0x8350: old name
		
out:	byte 0x8350: status		
*/
static void
DskRename(u8 dev, u8 unk, u16 newname, u16 oldname)
{
	fiad_tifile    local, *tf = &local;

	char        newpath[OS_PATHSIZE];
	OSSpec      orig;
	OSSpec      renamed;
	OSError     err;

	module_logger(&emuDiskDSR, _L | 0, "DskRename\n");

	if (sub_setup_file(tf, dev, oldname) != 0) {
		sub_set_error(es_badfuncerr /*es_filenotfound */ );
		return;
	}

	// save original spec
	orig = tf->spec;

	// rename real file
	OS_PathSpecToString2(&orig.path, newpath);
	fiad_filename_ti2host((char *) VDP_PTR(newname),
			strcspn((char *) VDP_PTR(newname), " ."),
			newpath + strlen(newpath));

	// can't fathom new name?
	if ((err = OS_MakeFileSpec(newpath, &renamed)) != OS_NOERR) {
		sub_set_error(es_hardware);
		return;
	}

	// destination already exists?
	if (OS_Status(&renamed) == OS_NOERR) {
		sub_set_error(es_fileexists);
		return;
	}

	if ((err = OS_Rename(&orig, &renamed)) != OS_NOERR) {
		sub_set_error(es_hardware);
		return;
	}
   
	// now open the file and rename the FDR
	if (sub_setup_file(tf, dev, newname) != 0) {
		sub_set_error(es_badfuncerr /*es_filenotfound */ );
		return;
	}

	err = fiad_tifile_open_file(tf, 
								F_UNKNOWN,
								false /*create*/, 
								false /*always*/,
								false /*readonly*/);
	if (err != OS_NOERR) {
		sub_error(tf, err, _("could not open to modify filename"), 
				  err == OS_PERMERR ? es_hardware :
				  es_badfuncerr /* es_filenotfound */);
		return;
	}

	// update FDR
	memcpy(tf->fdr.filenam, VDP_PTR(newname), 10);
	tf->changedfdr = true;

	module_logger(&emuDiskDSR, _L | 0, _("Renamed '%s' to '%s'\n\n\n"), OS_SpecToString1(&orig), newpath);

	fiad_tifile_close_file(tf);

	sub_set_error(es_okay);
}

#if 0
#pragma mark "Direct input file"
#endif

/*	Access direct input file 

in:		byte 0x834c: drive
		byte 0x834d: 0=get FDR info, <>0=get # sectors
		word 0x834e: filename
		byte 0x8350: 0x8300+# = ptr to:
		-- if get FDR info:
			word <unused>, word <# sectors>, 
			byte <flags>, byte <recspersrec>, 
			byte <eof>, byte <reclen>, word <numrecs>
		-- if read sectors:
			word <buffer addr>, word <sector #>

out:	byte 0x834D: # sectors read
		byte 0x8350: status
*/
static void
DskDirectInput(u8 dev, u8 secs, u16 fname, u16 parms)
{
	fiad_tifile    local, *tf = &local;
	OSError err;

	module_logger(&emuDiskDSR, _L | 0, "DskDirectInput\n");

	// error condition
	memory_write_byte(rambase+0x4d, 0);

	if (sub_setup_file(tf, dev, fname) != 0) {
		sub_set_error(es_filenotfound);
		return;
	}

	err = fiad_tifile_open_file(tf, 
								F_UNKNOWN,
								false /*create*/, 
								false /*always*/,
								true /*readonly*/);
	if (err != OS_NOERR) {
		sub_error(tf, err, _("could not open to read sectors"), 
				  es_badfuncerr /* es_filenotfound */);
		return;
	}

	DSKcomparefdrandsub(tf, (char *) VDP_PTR(fname));

	parms = rambase+0x00 + (parms >> 8);

	if (!secs) {
		// read FDR info
		memory_write_word(parms + 2, tf->fdr.secsused);
		memory_write_byte(parms + 4, tf->fdr.flags);
		memory_write_byte(parms + 5, tf->fdr.recspersec);
		memory_write_byte(parms + 6, tf->fdr.byteoffs);
		memory_write_byte(parms + 7, tf->fdr.reclen);
		memory_write_word(parms + 8, HOST2TI(tf->fdr.numrecs));

		sub_set_error(0);
	} else {
		// read sectors
		u16         vaddr = memory_read_word(parms);
		u16         secnum = memory_read_word(parms + 2);
		u16         red = 0;
		int			ret;

		module_logger(&emuDiskDSR, _L | L_1,
			 _("reading %d sectors from sector #%d from file, storing to >%04X\n\n"),
			 secs, secnum, vaddr);
//debugger_enable(true);

		sub_set_error(0);

		if (!fiad_tifile_seek_to_sector(tf, secnum)) {
			sub_error(tf, tf->error, _("DskDirectInput:  Could not seek to sector"), es_hardware);
		} else {
			ret = fiad_tifile_read_binary_image(tf, VDP_PTR(vaddr), secs * 256, &red);
			if (ret < 0) {
				sub_error(tf, tf->error, _("DskDirectInput:  Could not read sectors"), es_hardware);
				sub_set_error(es_hardware);
			}

			// error will be set if sector read failed
			memory_write_byte(rambase+0x4D, (red + 255) >> 8);
		}
	}
	fiad_tifile_close_file(tf);
}

#if 0
#pragma mark "Direct output file"
#endif

/*	Access direct output file

in:		byte 0x834c: drive
		byte 0x834d: 0=make FDR, <>0=write # sectors
		word 0x834e: filename
		byte 0x8350: +0x8300 = ptr to:
		-- if make FDR:
			word <unused>, word <# sectors>, 
			byte <flags>, byte <recspersrec>, 
			byte <eof>, byte <reclen>, word <numrecs>
		-- if write sectors:
			word <buffer addr>, word <sector #>

out:	byte 0x834d: # sectors written (or 0)
		byte 0x8350: status
*/
static void
DskDirectOutput(u8 dev, u8 secs, u16 fname, u16 parms)
{
	fiad_tifile    local, *tf = &local;
	OSError err;

	module_logger(&emuDiskDSR, _L | 0, "DskDirectOutput\n");

	if (sub_setup_file(tf, dev, fname) != 0) {
		sub_set_error(es_outofspace);
		return;
	}

	err = fiad_tifile_open_file(tf, 
								newfileformat,
								2 /*create*/, 
								false /*always*/,
								false /*readonly*/);
	if (err != OS_NOERR) {
		sub_error(tf, err, _("could not open to write sectors"), 
				  err == OS_PERMERR ? es_hardware :
				  es_badfuncerr /* es_filenotfound */);
		return;
	}

	DSKcomparefdrandsub(tf, (char *) VDP_PTR(fname));

	parms = rambase+0x00 + (parms >> 8);

	if (!secs) {
		// write FDR info
		tf->fdr.secsused = memory_read_word(parms + 2);
		tf->fdr.flags = memory_read_byte(parms + 4);
		tf->fdr.recspersec = memory_read_byte(parms + 5);
		tf->fdr.byteoffs = memory_read_byte(parms + 6);
		tf->fdr.reclen = memory_read_byte(parms + 7);
		tf->fdr.numrecs = TI2HOST(memory_read_word(parms + 8));

		// get filename
		memcpy(tf->fdr.filenam, VDP_PTR(fname), 10);
		tf->changedfdr = true;

		module_logger(&emuDiskDSR, _L|L_2, _("Setting size of '%s' to %d sectors\n"), 
			   OS_SpecToString1(&tf->spec), 
			   tf->fdr.secsused);

		// set file to given length
		if (!fiad_tifile_seek_to_sector(tf, tf->fdr.secsused)
		||	!fiad_tifile_set_file_size(tf))
		{
			sub_set_error(es_hardware);
		}
		else
			sub_set_error(0);

		// Reset any bogus flags in here
		tf->changedfdr = fiad_fdr_repair(&tf->fdr, tf->fdr.secsused * 256);

		//module_logger(&emuDiskDSR, _L|L_2, _("Number of sectors now %d\n"), tf->fdr.secsused);
	} else {
		// write sectors
		u16         vaddr = memory_read_word(parms);
		u16         secnum = memory_read_word(parms + 2);
		u16         wrt = 0;
		int			ret;

		module_logger(&emuDiskDSR, _L | L_1,
			 _("writing %d sectors at sector #%d to file, reading from >%04X\n\n"),
			 secs, secnum, vaddr);

		sub_set_error(0);

		if (!fiad_tifile_seek_to_sector(tf, secnum)) {
			sub_set_error(es_hardware);
		} else {
			ret = fiad_tifile_write_binary_image(tf, VDP_PTR(vaddr), secs*256, &wrt);
			if (ret < 0) {
				sub_set_error(es_hardware);
			}

			memory_write_byte(rambase+0x4D, (wrt + 255) >> 8);
		}
	}
	fiad_tifile_close_file(tf);
}

////////////////////////////////////


extern vmModule emuDiskDSR;

static int warned_direct_io = 0;

static void
bwdrom_emudisk(const mrstruct *mr, u32 addr, s8 val)
{
	if (addr >= 0x5ff0)
	{
		// this is the I/O area for the wd179x controller
		if (!warned_direct_io) 
		{
			module_logger(&emuDiskDSR,
						  _LL|LOG_ERROR, _("The current program is attempting to control the disk controller,\n"
								 "but this functionality does not work with the emulated disk DSR.\n"
								 "Consider running this application from a disk-on-a-disk (DOAD).\n"));
			warned_direct_io = 1;
		}
	}
}

static      s8
brdrom_emudisk(const mrstruct *mr, u32 addr)
{
	if (addr >= 0x5ff0)
		return 0x00;
	else
		return BYTE(mr->areamemory, addr - 0x5C00);
}

//  this is only used on the last area of the DSR ROM block */
mrstruct    dsr_rom_emudisk_io_handler = {
	emudiskdsr + 0x1C00, NULL, NULL,
	NULL,
	brdrom_emudisk,
	NULL,
	bwdrom_emudisk
};

mrstruct    dsr_rom_emudisk_handler = {
	emudiskdsr, emudiskdsr, NULL,			/* ROM */
	NULL,
	NULL,
	NULL,
	NULL
};

static      u32
cruwEmuDiskROM(u32 addr, u32 data, u32 num)
{
	if (data) {
		module_logger(&emuDiskDSR, _L | L_3, _("emu disk DSR on\n"));
		report_status(STATUS_DISK_ACCESS, 0, true);
//      debugger_enable(true);

		/*  load appropriate ROM (shared/lone) 
			depending on whether real disk ROM is also loaded */
/*
		if (0 == data_load_dsr(romspath,
#ifdef REAL_DISK_DSR
						 (realDiskDSR.runtimeflags & vmRTInUse) ? emu2diskfilename :
#endif
						 emudiskfilename, _("Emulated disk DSR ROM"), emudiskdsr))
			return 0;
*/

		dsr_set_active(&emuDiskDSR);
		SET_AREA_HANDLER(0x4000, 0x1C00, &dsr_rom_emudisk_handler);
		SET_AREA_HANDLER(0x5C00, 0x0400, &dsr_rom_emudisk_io_handler);
		module_logger(&emuDiskDSR, _L | L_3, _("DSR Read >4000 = >%2X\n\n"), 
					  memory_read_byte(0x4000)&0xff);
	} else {
		module_logger(&emuDiskDSR, _L | L_3, _("emu disk DSR off\n"));
		report_status(STATUS_DISK_ACCESS, 0, false);
//      debugger_enable(false);
		dsr_set_active(NULL);
		SET_AREA_HANDLER(0x4000, 0x2000, &zero_memory_handler);
	}
	return 0;
}

/********************************************/

static      vmResult
emudisk_getcrubase(u32 * base)
{
	*base = EMUDISKCRUBASE;
	return vmOk;
}

static      vmResult
emudisk_filehandler(u32 code)
{
	/*  If the real disk DSR is also installed, we limit
	   our functional drives from DSK3 to DSK5, and pass
	   references to DSK1 and DSK2 to the real DSR.
	   (The order of the CRU allows this, since we're EMUDISKCRUBASE
	   and the real DSR is 0x1100.) */

	module_logger(&emuDiskDSR, _L | L_3, "handlefileoperations\n");
#ifdef REAL_DISK_DSR
	module_logger(&emuDiskDSR, _L | L_3, _("code = %d\n\n"), code);

	/*  pass control when the code is for the real disk 
	   we didn't skip the return at *R11, so the TI ROM 
	   will continue to next DSR */

	/*  this shouldn't happen unless non-shared emulated
	   disk DSR was loaded. */
	if (dsr_is_real_disk(code - D_DSK1 + 1)) {
		module_logger(&emuDiskDSR, _L | L_3, _("passing control to real disk"));
		return vmOk;
	}

	/*  extended ops store drive [1-x] in low nybble */
	if (code >= D_DSKSUB && dsr_is_real_disk(memory_read_byte(rambase+0x4C) & 0xf)) {
		module_logger(&emuDiskDSR, _L | L_3, _("passing control to real disk\n"));
		return vmOk;
	}
#endif

	module_logger(&emuDiskDSR, _L | L_3, _("handling code %d\n\n"), code);

	/* get RAM base */
	rambase = wp - 0xe0;
	module_logger(&emuDiskDSR, _L | L_2, "RAMbase=%04x\n", rambase);

	/*
	   match any of the drives for the volume name at 
	   the VDP address in >8356.

	   After matching the volume, add the length of the name
	   to >8354 and >8356.
	 */
	if (code == D_DSK) {
		char       *vname = (char *) VDP_PTR(memory_read_word(rambase+0x56));
		u8          dev;
		int         len;

		if (*vname++ != '.')
			return vmOk;		/* bad device */

		dev = matchdrive(vname, &len);
		if (dev == 0) {
			module_logger(&emuDiskDSR, _L | L_1, _("did not match DSK.%.*s\n"), len, vname);
			return vmOk;		/* not matched */
		} else {
			module_logger(&emuDiskDSR, _L | L_1, _("matched DSK.%.*s on DSK%d\n"), len, vname, dev);
		}

		/*  Update ptrs  */
		memory_write_word(rambase+0x56, memory_read_word(rambase+0x56) + len);
		memory_write_word(rambase+0x54, memory_read_word(rambase+0x54) + len);

		/*  use this device  */

		code = dev;
	}

	switch (code) {
		/* PAB file operation on DSKx */
	case D_DSK1:
	case D_DSK2:
	case D_DSK3:
	case D_DSK4:
	case D_DSK5:
	{
		u16         pabaddr =
			memory_read_word(rambase+0x56) - 
			memory_read_word(rambase+0x54) -
			PABREC_SIZE;
		u8 opcode;
		u8 *fnptr;

		fnptr = VDP_PTR(pabaddr+9);

		opcode = *(fnptr-9);

		/* illegal opcode? */
		if (opcode > 9)
			pab_set_vdp_error(fnptr, e_illegal);
		else {
			static void (*opcodehandlers[]) (u8 dev, u8 *fn) = {
				DSKOpen, DSKClose, DSKRead, DSKWrite,
				DSKSeek, DSKLoad, DSKSave, DSKDelete,
				DSKScratch, DSKStatus
			};

			module_logger(&emuDiskDSR, _L | L_2, _("doing operation %d on DSK%d\n"), opcode, code);
			opcodehandlers[opcode] (code, fnptr);
		}

		/*  return, indicating that the DSR handled the operation */
		register(11) += 2;
		break;
	}

		/* init disk dsr */
	case D_INIT:
	{
		int         x;

		for (x = 0; x < MAXFILES; x++) {
			memset((void *)&files[x], 0, sizeof(files[0]));
		}

		/* Set up timer stuff for catalogs */
		for (x = 0; x < MAXDRIVE; x++) {
			DskCatFlag[x] = 1;
			if (!DskCatTag[x])
				DskCatTag[x] = TM_UniqueTag();
		}

		/*  also steal some RAM for the name compare buffer,
		   so dependent programs can function */
		vdpnamebuffer = memory_read_word(rambase+0x70) - 9;
		memory_write_word(rambase+0x70, vdpnamebuffer - 1);
		break;
	}

		/* ???? */
	case D_16:
	{
		memory_write_byte(rambase+0x50, 0);	/* no error */
		register(11) += 2;
		break;
	}

		/* call files(x) 
		   just ignore it */
	case D_FILES:
		memory_write_word(rambase+0x2C, memory_read_word(rambase+0x2C) + 12);
		memory_write_byte(rambase+0x42, 0);
		memory_write_byte(rambase+0x50, 0);
		register(11) += 2;
		break;


	case D_SECRW:
	case D_FMTDISK:
	case D_PROT:
	case D_RENAME:
	case D_DINPUT:
	case D_DOUTPUT:
	{
		static void (*handlers[]) (u8 dev, u8 opt, u16 addr1, u16 addr2) = {
			DskSectorRW, DskFormatDisk, DskProtect,
			DskRename, DskDirectInput, DskDirectOutput
		};
		u8          dev;
		u8          opt;
		u16         addr1, addr2;

		dev = memory_read_byte(rambase+0x4c);
		opt = memory_read_byte(rambase+0x4d);
		addr1 = memory_read_word(rambase+0x4e);
		addr2 = memory_read_word(rambase+0x50);

		module_logger(&emuDiskDSR, _L | L_2, _("doing operation %d on DSK%d [%d, %04X, %04X]\n"),
			 code, dev, opt, addr1, addr2);

		if (dev <= MAXDRIVE) {
			handlers[code - D_SECRW] (dev, opt, addr1, addr2);
			register(11) += 2;
		}
		break;
	}

	default:
		module_logger(&emuDiskDSR, _L | L_1, _("ignoring code = %d\n"), code);
		return vmNotAvailable;
	}
	return vmOk;
}

/*************************************************/

//	Called to load/save each of the files[].
//
//	Since we're using one variable for each of these fields,
//	the second argument must be a string.  Sigh.
//	Since the arg is a string buffer, we can use a simple routine
//	to format numeric strings for us.
//
//	A special config var should close all the file[] entries
//	so we can sanely open them up at fixed positions again.

static char *num2str(int x)
{
	static char buf[32];
	sprintf(buf, "%d", x);
	return buf;
}

static int str2num(const char *str)
{
	int x;

	if (!strcasecmp(str,"true") || !strcasecmp(str, "on"))
		return 1;
	else if (!strcasecmp(str,"false") || !strcasecmp(str, "off"))
		return 0;
	x = atoi(str);
	return x;
}

//	Number of fields to save:

//	fiad_pabfile.fnptr (VDP addr)
//	fiad_pabfile.pab (binary)
//	fiad_pabfile.is_catalog (0/1)
//	fiad_pabfile.tf:
//		spec (pathname)
//		open (0/1)
//		readonly (0/1)
//		error (number)
//		cursec (number)
//		cureof (number)
//		curnrecs (number)
//		currec (number)
//	
//		handle, changed, changedfdr, fdr, sector
//		are reconstructed at re-load time

enum
{
	fiad_tifile_vdp_filename_addr,
	fiad_tifile_vdp_pab_data,
	fiad_tifile_is_catalog,
	fiad_tifile_path,
	fiad_tifile_is_open,
	fiad_tifile_is_read_only,
	fiad_tifile_error,
	fiad_tifile_sector,
	fiad_tifile_sector_offset,
	fiad_tifile_sector_record_count,
	fiad_tifile_record_number,
	fiad_tifile_reopen,
	fiad_tifile_ENTS
};

//#define fiad_tifile_ENTS	12

static
DECL_SYMBOL_ACTION(emudisk_config_files)
{
	fiad_pabfile *f;
	char tmp[256];
	char *str;
	int val,addr;
	OSError err;

	if (task == csa_READ) {
		if (iter >= (fiad_tifile_ENTS * (sizeof(files)/sizeof(fiad_tifile))))
			return 0;

		f = &files[iter / fiad_tifile_ENTS];

		// first arg is file #
		command_arg_set_num(SYM_ARG_1st, iter / fiad_tifile_ENTS);
		// second arg is enum
		command_arg_set_enum(SYM_ARG_2nd, iter % fiad_tifile_ENTS);

		switch (iter % fiad_tifile_ENTS) {
		case fiad_tifile_vdp_filename_addr:
			// prework:  save changed stuff if necessary
			fiad_tifile_flush(&f->tf);
//			command_arg_set_string(SYM_ARG_2nd, "vdp_filename_addr");
			command_arg_set_string(SYM_ARG_3rd, 
								   !f->fnptr ? num2str(-1) :
								   num2str(f->fnptr - VDP_PTR(0)));
			break;

		case fiad_tifile_vdp_pab_data: // pab 
//			command_arg_set_string(SYM_ARG_2nd, "vdp_pab_data");
			emulate_bin2hex((u8 *)&f->pab, tmp, sizeof(f->pab));
			command_arg_set_string(SYM_ARG_3rd, tmp);
			break;

		case fiad_tifile_is_catalog: // is_catalog
//			command_arg_set_string(SYM_ARG_2nd, "is_catalog");
			command_arg_set_string(SYM_ARG_3rd, num2str(f->is_catalog));
			break;

			// start of fiad_pabfile stuff	

		case fiad_tifile_path:	// spec
//			command_arg_set_string(SYM_ARG_2nd, "path");
			command_arg_set_string(SYM_ARG_3rd, OS_SpecToString1(&f->tf.spec));
			break;

		case fiad_tifile_is_open: // open
//			command_arg_set_string(SYM_ARG_2nd, "is_open");
			command_arg_set_string(SYM_ARG_3rd, f->tf.open ? "true" : "false");
			break;

		case fiad_tifile_is_read_only:
//			command_arg_set_string(SYM_ARG_2nd, "is_read_only");
			command_arg_set_string(SYM_ARG_3rd, f->tf.readonly ? "true" : "false");
			break;

		case fiad_tifile_error: // error
//			command_arg_set_string(SYM_ARG_2nd, "error");
			command_arg_set_string(SYM_ARG_3rd, num2str(f->tf.error));
			break;

		case fiad_tifile_sector: // cursec
//			command_arg_set_string(SYM_ARG_2nd, "sector");
			command_arg_set_string(SYM_ARG_3rd, num2str(f->tf.cursec));
			break;

		case fiad_tifile_sector_offset: // curoffs
//			command_arg_set_string(SYM_ARG_2nd, "sector_offset");
			command_arg_set_string(SYM_ARG_3rd, num2str(f->tf.curoffs));
			break;

		case fiad_tifile_sector_record_count: // curnrecs
//			command_arg_set_string(SYM_ARG_2nd, "sector_record_count");
			command_arg_set_string(SYM_ARG_3rd, num2str(f->tf.curnrecs));
			break;

		case fiad_tifile_record_number: // currec
//			command_arg_set_string(SYM_ARG_2nd, "record_number");
			command_arg_set_string(SYM_ARG_3rd, num2str(f->tf.currec));
			break;

		case fiad_tifile_reopen: // reopen (trigger)
			// this tells the loader to reopen the file
//			command_arg_set_string(SYM_ARG_2nd, "reopen");
			command_arg_set_string(SYM_ARG_3rd, "");
			break;

		default:
			my_assert(!_("error in emudisk_config_files"));
		}
		return 1;
	}

	//	Load from config file
	//	spec should be the first thing we see

	command_arg_get_num(SYM_ARG_1st, &val);

	if (val < 0 || val >= sizeof(files) / sizeof(fiad_tifile)) {
		module_logger(&emuDiskDSR, _L | LOG_USER | LOG_ERROR, _("Invalid file number\n"));
		return 0;
	}

	f = &files[val];
//	command_arg_get_string(SYM_ARG_2nd, &field);
	command_arg_get_enum(SYM_ARG_2nd, &val);
	command_arg_get_string(SYM_ARG_3rd, &str);

	switch (val)
	{
	case fiad_tifile_vdp_filename_addr:
		addr = str2num(str);
		f->fnptr = (addr == -1) ? 0L : VDP_PTR(addr & 0x3fff);
		break;
	case fiad_tifile_vdp_pab_data:
		emulate_hex2bin(str, (u8 *)&f->pab, sizeof(f->pab));
		break;
	case fiad_tifile_is_catalog:
		f->is_catalog = str2num(str);
		break;
	case fiad_tifile_path:
		if (OS_MakeSpec(str, &f->tf.spec, NULL) != OS_NOERR) {
			module_logger(&emuDiskDSR, _L | LOG_USER | LOG_ERROR, _("Invalid file name '%s'\n"), str);
			return 0;
		}
		break;
	case fiad_tifile_is_open:
		f->tf.open = !!str2num(str);
		break;
	case fiad_tifile_is_read_only:
		f->tf.readonly = !!str2num(str);
		break;
	case fiad_tifile_error:
		f->tf.error = str2num(str);
		break;
	case fiad_tifile_sector:
		f->tf.cursec = str2num(str);
		break;
	case fiad_tifile_sector_offset:
		f->tf.curoffs = str2num(str) & 0xff;
		break;
	case fiad_tifile_sector_record_count:
		f->tf.curnrecs = str2num(str);
		break;
	case fiad_tifile_record_number:
		f->tf.currec = str2num(str);
		break;
	case fiad_tifile_reopen:
		if (f->is_catalog) {
			if (!DSKinitcatalog(f, f->is_catalog)) {
				module_logger(&emuDiskDSR, _L | LOG_ERROR | LOG_USER, _("Could not reopen catalog at '%s'\n"),
					   OS_PathSpecToString1(&f->tf.spec.path));
				return 0;
			}
		} else if (f->tf.open) {
			err = fiad_tifile_reopen_file(&f->tf, 
										  newfileformat,
										  f->tf.readonly /*readonly*/);

			if (err != OS_NOERR) {
				module_logger(&emuDiskDSR, _L | LOG_ERROR | LOG_USER, _("Could not reopen file '%s'\n"),
					   OS_SpecToString1(&f->tf.spec));
				return 0;
			}
		}

		// read fdr and current sector for normal file
		if (f->tf.open) {
			if (!fiad_tifile_read_fdr(&f->tf))
				return 0;
			if (!fiad_tifile_read_sector(&f->tf))
				return 0;
		}
		break;
	default:
		module_logger(&emuDiskDSR, _L | LOG_USER | LOG_ERROR, _("Unrecognized file field: %d\n"),
			   val);
		return 0;
	}

	return 1;
}

static 
DECL_SYMBOL_ACTION(emudisk_close_files)
{
	pab_close_all_files();
	return 1;
}

/*************************************************/
static
DECL_SYMBOL_ACTION(emudisk_check_disk)
{
	int         dsknum = sym->name[3] - '0';

	if (!dsr_is_emu_disk(dsknum))
		module_logger(&emuDiskDSR, _L | LOG_WARN | L_1,
			   _("DSK%d (%s) is inaccessible when real (DOAD) disk DSR is loaded\n"),
			   dsknum, OS_PathSpecToString1(&emudiskpath[dsknum - 1]));

	return 1;
}


/*************************************************/

static      vmResult
emudisk_detect(void)
{
	return vmOk;
}

static      vmResult
emudisk_init(void)
{
	command_symbol_table *emudiskcommands =
		command_symbol_table_new(_("Emulated Disk DSR Options"),
								 _("These commands control the emulated files-in-a-directory (FIAD) emulation"),

	 command_symbol_new("DSK1Path",
						_("Set DSK1 directory"),
						c_STATIC,
						emudisk_check_disk
						/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_pathspec
						(_("dir"),
						 _("directory containing V9t9 files"),
						 NULL /* action */ ,
						 &emudiskpath[0],
						 NULL /* next */ )
						,
	 command_symbol_new("DSK2Path",
						_("Set DSK2 directory"),
						c_STATIC,
						emudisk_check_disk
						/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_pathspec
						(_("dir"),
						 _("directory containing V9t9 files"),
						 NULL /* action */ ,
						 &emudiskpath[1],
						 NULL /* next */ )
						,
	 command_symbol_new("DSK3Path",
						_("Set DSK3 directory"),
						c_STATIC,
						emudisk_check_disk
						/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_pathspec
						(_("dir"),
						 _("directory containing V9t9 files"),
						 NULL /* action */ ,
						 &emudiskpath[2],
						 NULL /* next */ )
						,
	 command_symbol_new("DSK4Path",
						_("Set DSK4 directory"),
						c_STATIC,
						emudisk_check_disk
						/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_pathspec
						(_("dir"),
						 _("directory containing V9t9 files"),
						 NULL /* action */ ,
						 &emudiskpath[3],
						 NULL /* next */ )
						,
	 command_symbol_new("DSK5Path",
						_("Set DSK5 directory"),
						c_STATIC,
						emudisk_check_disk
						/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_pathspec
						(_("dir"),
						 _("directory containing V9t9 files"),
						 NULL /* action */ ,
						 &emudiskpath[4],
						 NULL /* next */ )
						,

	command_symbol_new("EmuDiskDSRFileName",
					   _("Name of emulated DSR ROM image which fits in the CPU address space >4000...>5FFF; "
					   "this DSR defines DSK1 through DSK5"),
					   c_STATIC,
					   NULL /* action */ ,
					   RET_FIRST_ARG,
					   command_arg_new_string(_("file"),
											  _("name of binary image"),
											  NULL /* action */ ,
											  ARG_STR(emudiskfilename),
											  NULL /* next */ )
					   ,
	command_symbol_new("EmuDiskSharedDSRFileName|Emu2DiskDSRFileName",
					   _("Name of emulated DSR ROM image which fits in the CPU address space >4000...>5FFF; "
					   "this DSR defines DSK3 through DSK5 and can share space with the real (DOAD) disk DSR"),
					   c_STATIC,
					   NULL /* action */ ,
					   RET_FIRST_ARG,
					   command_arg_new_string(_("file"),
											  _("name of binary image"),
											  NULL /* action */ ,
											  ARG_STR(emu2diskfilename),
											  NULL /* next */ )
					   ,

	command_symbol_new("KeepFileFormat",
						_("Toggle preservation of original file type (V9t9 or TIFILES)"),
						c_STATIC,
						NULL /* action */ ,
						RET_FIRST_ARG,
						command_arg_new_enum("off|on",
											_("on: don't change existing file's type; "
											"off: change type to NewFileFormat"),
											NULL	/* action */,
											ARG_NUM(keepfileformat),
											NULL /* next */ )
						,

    command_symbol_new("NewFileFormat",
						_("Select type for new files or converted files"),
						c_STATIC,
						NULL	/* action */,
						RET_FIRST_ARG,
						command_arg_new_enum("V9t9|TIFILES||f_V9t9=0|f_TIFILES=1",
											_("v9t9: original V9t9 file type; "
											"tifiles: TIFILES (XMODEM) format"),
											NULL	/* action */,
											 ARG_NUM(newfileformat),
											NULL /* next */ )
						,

	command_symbol_new("UnknownFileIsText",
						_("Toggle treatment of unknown (non-V9t9 and non-TIFILES) files as "
						"DOS/Unix/Mac text files"),
						c_STATIC,
						NULL  /* action */ ,
						RET_FIRST_ARG,
						command_arg_new_enum("off|on",
											_("on:  read unknown file as text; off:  generate error"),
											NULL
											/* action */ ,
											ARG_NUM(unknownfileistext),
											NULL /* next */ ),

	command_symbol_new ("AllowLongCatalogs",
						_("Allow catalogs read through DSKx. to return more than 127 records; "
						"some programs may depend on this limit"),
						c_STATIC,
						NULL/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_enum("off|on",
											_("on: allow up to 32767 entries, off: restrict to 127 entries"),
											NULL /* action */,
											ARG_NUM(allowlongcatalogs),
											NULL /* next */ ),

	command_symbol_new ("RepairDamagedFiles",
						_("Repair files with bad file sizes, even when opened read-only.  "
						"This is a bit dangerous if you try to open a non-V9t9 file, "
						"which will (obviously) appear damaged.  "
						"V9t9 will try to rule out files that don't pass enough sanity checks, though."),
						c_STATIC|c_CONFIG_ONLY,
						NULL/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_enum("off|on",
											_("on: repair damaged files, off: leave them alone"),
											NULL /* action */,
											ARG_NUM(repairbadfiles),
											NULL /* next */ ),

	command_symbol_new ("FixupOldV9t9Filenames",
						_("Rename older V9t9 files which were mangled to fit in "
						"the DOS 8.3 filename format.  These files were split at "
						"the 8th character with a '.' and all illegal DOS characters "
						"(" DOS_illegalchars ") were biased by 128 to make them "
						"representable on that filesystem.\n"
						"New V9t9 file mangling rules assume filesystems that "
						"allow long filenames, so there is no splitting at "
						"the 8th character, and illegal characters are translated "
						"HTML-like into '&#xx;' where 'xx' is the hexadecimal "
						"ASCII code for the characters.\n"
						"Files renamed to the new format will not be compatible "
						"with older versions of V9t9, unless, under Windows, "
						"you refer to the files with the short format "
						"(i.e., 'longfilenm' --> 'longfi~1')."),
						c_STATIC|c_CONFIG_ONLY,
						NULL/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_enum("off|on",
											_("on: rename old V9t9 files, "
											"off: leave them alone"),
											NULL /* action */,
											ARG_NUM(fixupoldv9t9filenames),
											NULL /* next */ ),

	command_symbol_new ("GenerateOldV9t9Filenames",
						_("Generate filenames that conform to the old V9t9 DOS-"
						"mangled format (see above) instead of the new format. "
						"Not recommended unless you actively use the DOS version."),
						c_STATIC|c_CONFIG_ONLY,
						NULL/* action */ ,
						RET_FIRST_ARG,
						command_arg_new_enum("off|on",
											_("on: generate old V9t9 filenames, "
											"off: generate current V9t9 filenames"),
											NULL /* action */,
											ARG_NUM(generateoldv9t9filenames),
											NULL /* next */ ),

	// these commands must precede EmuDiskFileInfo
	// so that reloading a session will do the right thing
	command_symbol_new("CloseAllFiles",
					   NULL /* help */,
					   c_STATIC|c_SESSION_ONLY,
					   emudisk_close_files,
					   NULL /* ret */,
					   NULL /* args */,

   command_symbol_new("EmuDiskTopOfRam||dsrEmuDiskTopOfRam",
					  _("Top address used in VDP RAM"),
					  c_STATIC,
					  NULL /* action */,
					  RET_FIRST_ARG,
					  command_arg_new_num(_("address"),
										  _("VDP RAM address, minus one"),
										  NULL /* action */,
										  ARG_NUM(vdpnamebuffer),
										  NULL /* next */)
    ,

	command_symbol_new("EmuDiskFileInfo",
					   NULL /* help */,
					   c_DYNAMIC|c_SESSION_ONLY,
					   emudisk_config_files,
					   NULL /* ret */,
					   command_arg_new_num(_("file number"),
											 _("index of files[] array"),
											 NULL /* action */,
											 NEW_ARG_NUM(u8),
					   command_arg_new_enum(//_("field name"),
"vdp_filename_addr|vdp_pab_data|is_catalog|path|is_open|is_read_only|"
"error|sector|sector_offset|sector_record_count|record_number|reopen",
												_("name of field in pab_tifile to save"),
												NULL /* action */,
												//	NEW_ARG_NEW_STRBUF,
												NEW_ARG_NUM(int),
					   command_arg_new_string(_("field contents"),
												_("contents of field"),
												NULL /* action */,
												NEW_ARG_NEW_STRBUF,

					   NULL /* next */))),

	NULL
	/* next */


		))))))))))))))))),

  NULL /* sub */ ,

  NULL	/* next */
  );

	command_symbol_table_add_subtable(universe, emudiskcommands);

	features |= FE_emudisk;
	return vmOk;
}

static      vmResult
emudisk_enable(void)
{
	module_logger(&emuDiskDSR, _L | L_1, _("setting up emulated disk DSR ROM\n"));

	if (cruadddevice(CRU_WRITE, EMUDISKCRUBASE, 1, cruwEmuDiskROM)) {
		return vmOk;
	} else
		return vmNotAvailable;
}

static      vmResult
emudisk_disable(void)
{
	crudeldevice(CRU_WRITE, EMUDISKCRUBASE, 1, cruwEmuDiskROM);
	return vmOk;
}

static      vmResult
emudisk_restart(void)
{
	/*  load appropriate ROM (shared/lone) 
		depending on whether real disk ROM is also loaded */
	if (0 == data_load_dsr(romspath,
#ifdef REAL_DISK_DSR
						 (realDiskDSR.runtimeflags & vmRTInUse) ? emu2diskfilename :
#endif
						 emudiskfilename, _("Emulated disk DSR ROM"), emudiskdsr))
			return vmNotAvailable;

	return vmOk;
}

static      vmResult
emudisk_restop(void)
{
	return vmOk;
}

static      vmResult
emudisk_term(void)
{
	return vmOk;
}

static vmDSRModule emuDiskModule = {
	1,
	emudisk_getcrubase,
	emudisk_filehandler
};

vmModule 	emuDiskDSR = {
	3,
	"Emulated disk DSR",
	"dsrEmuDisk",

	vmTypeDSR,
	vmFlagsNone,

	emudisk_detect,
	emudisk_init,
	emudisk_term,
	emudisk_enable,
	emudisk_disable,
	emudisk_restart,
	emudisk_restop,
	{(vmGenericModule *) & emuDiskModule}
};
