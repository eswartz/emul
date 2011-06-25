/*
  dsr_realdisk.c				-- V9t9 module for disk image DSR

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

char        realdiskfilename[OS_NAMESIZE] = "disk.bin";
u8          realdiskdsr[8192];
char        diskname[3][OS_NAMESIZE] =
	{ "disk0001.dsk", "disk0002.dsk", "disk0003.dsk" };
char       *diskimagepath = NULL;

#define _L	LOG_REALDISK | LOG_INFO
#define _LS	LOG_REALDISK | LOG_INFO | LOG_USER

//#define DSKbuffersize (256*18)	/* maximum track size */
#define DSKtracksize_SD (3200)
#define DSKtracksize_DD (6420)

#define DSKbuffersize (16384)		/* maximum track size */

#if BEWORKS_FS
	 OSFileType  osDiskImageType = { 0666, "x/v9t9-disk-image" };
#elif POSIX_FS
	 OSFileType  osDiskImageType = { 0666 };
#elif WIN32_FS
	 OSFileType  osDiskImageType = 0;
#elif MAC_FS
	 OSFileType  osDiskImageType = 'DI99';
#endif

/*	Header for track (*.trk) files; also used internally for sector
	files, but not present in image: we guess the disk geometry from
	the size and sector 0 information. */

#define TRACK_MAGIC			"trak"
#define TRACK_MAGIC_SIZE 	4
#define TRACK_VERSION		1

typedef struct DSKheader
{
	u8			magic[4];	
	u8			version;	/* disk version */
	u8			tracks;		/* tracks per side */
	u8			sides;		/* 1 or 2 */
	u8			unused;
	u16			tracksize;	/* bytes per track */
	u16			track0offs;	/* offset for track 0 data */
}	DSKheader;

typedef struct DSKInfo
{
	u8   		num;		/* current drive #1-3 */
	u8   		hold;		/* holding for data? */
	u8   		lastbyte;	/* last byte written to WDDATA when hold off */
	u8   		buffer[DSKbuffersize];	/* track contents */
	u8	 		dirty;			/* is the buffer out of date with disk? */
	u32  		buflen;		/* max length of data expected for a write/read */
	u32  		bufpos;		/* offset into buffer */

	OSSpec 		spec;
	OSRef 		handle;	/* file descriptor for disk */
	DSKheader 	hdr;	/* info about disk */

	u8	 		fulltrk;		/* full track information used? */
	u8			readonly;		/* don't write! */

	u32  		trackoffset;		/* current track seek position into disk */

	u32  		trackbyteoffset;	/* offset into track, bytes */
	u32	 		idoffset;		/* offset of ID field (0xfe) in track (for sectors) */
	u32	 		dataoffset;	/* offset of data field (0xfb) in track (for sectors) */

	u8   		command;	/* command being executed */
	u8   		flags;		/* flags sent with command being executed */
	u8			addr;		/* last addr written */

	/* command-specified */
	u8			seektrack;	/* physically seeked track */
	u8			track;		/* desired track */
	u8			side;		/* current side */
	u8			sector;		/* desired sector */
	u16			crc;		/* current CRC */

	/* these are the logical values */
	u8   		trackid;	/* current track */
	u8   		sideid;	/* current side */
	u8   		sectorid;	/* current sector */
	u8	 		sizeid;	/* current sector size (1=128, 2=256, etc.) */
	u16	 		crcid;		/* expected CRC */

	u8   		status;	/* current status */
	u8	 		motor;		/* motor running? */

}	DSKInfo;

DSKInfo	DSK;

enum
{
	W_WTCMD = 4,
	W_WTADDR = 5,
	W_WSADDR = 6,
	W_WTDATA = 7
};

enum
{
	FDC_seekhome		= 0x00,
	FDC_seek			= 0x10,
	FDC_step			= 0x20,
	FDC_stepin			= 0x50,
	FDC_stepout			= 0x60,

		fl_head_load	= 0x08,
		fl_verify_track	= 0x04,	/* match track register with sector ID */
		fl_step_rate	= 0x03,

	FDC_readsector		= 0x80,
	FDC_writesector		= 0xA0,
		fl_multiple		= 0x10,
		fl_side_number	= 0x08,
		fl_side_compare	= 0x02,
		fl_deleted_dam	= 0x01,

	FDC_readIDmarker= 0xC0,
	FDC_readtrack	= 0xE0,
	FDC_writetrack	= 0xF0,

		fl_15ms_delay	= 0x04,	/* common to readsector...writetrack */

	FDC_interrupt	= 0xD0
};

#define fdc_READY		0x80
#define fdc_WRITEPROT	0x40
#define fdc_BADRECORD	0x10
#define fdc_CRCERR		0x08
#define	fdc_LOSTDATA	0x04
#define	fdc_TRACK0		0x04		// for seek home only
#define	fdc_BUSY		0x01

#define	fdc_WTDATA		0x5FFE
#define	fdc_RDDATA		0x5FF6

struct fdc_secrec {
	u8			track, side, sector, seclen;
	u16			crc;
};

static void
FDCclosedisk(void);
static int
FDCseektotrack(void);
static int
FDCfindIDmarker(void);

static int
FDCreadtrackdata(void);

/* calculate CRC for data address marks or sector data */
/* borrowed from xmess-0.56.2.  seems like this only works for MFM */
static u16 calc_crc(u16 crc, u8 value)
{
	u8 l, h;

	l = value ^ (crc >> 8);
	crc = (crc & 0xff) | (l << 8);
	l >>= 4;
	l ^= (crc >> 8);
	crc <<= 8;
	crc = (crc & 0xff00) | l;
	l = (l << 4) | (l >> 4);
	h = l;
	l = (l << 2) | (l >> 6);
	l &= 0x1f;
	crc = crc ^ (l << 8);
	l = h & 0xf0;
	crc = crc ^ (l << 8);
	l = (h << 1) | (h >> 7);
	l &= 0xe0;
	crc = crc ^ l;
	return crc;
}


static      u32
FDCgetfilesize(void)
{
	OSSize      sz;

	if (DSK.handle) {
		OS_GetSize(DSK.handle, &sz);
		return sz;
	} else
		return 0;
}

static int
FDCsetfilesize(void)
{
	OSSize      sz, len;
	OSError	err;

	if (!DSK.handle) return 0;

	OS_GetSize(DSK.handle, &len);

	sz = DSK.hdr.tracksize * DSK.hdr.tracks * DSK.hdr.sides;

	/* never shrink */
	if (sz > len && (err = OS_SetSize(DSK.handle, sz)) != OS_NOERR) {
		module_logger(&realDiskDSR, _LS|LOG_ERROR,
					  _("DOAD server:  cannot get %d bytes for disk image '%s' (%s)\n"),
					  sz, OS_SpecToString1(&DSK.spec), OS_GetErrText(err));
		return 0;
	}

	return 1;
}

static void
FDCgetstatus(void)
{
}

static int
FDCwriteheader(void)
{
	OSError err;
	DSKheader hdr;
	OSSize sz;

	if (!DSK.handle || DSK.readonly) {
		return 0;
	}

	if (DSK.fulltrk) {
		/* byteswap header for export */
		memcpy(hdr.magic, DSK.hdr.magic, sizeof(hdr.magic));
		hdr.version = DSK.hdr.version;
		hdr.tracks = DSK.hdr.tracks;
		hdr.sides = DSK.hdr.sides;
		hdr.tracksize = HOST2TI(DSK.hdr.tracksize);
		hdr.track0offs = HOST2TI(DSK.hdr.track0offs);
		sz = sizeof(hdr);

		if ((err = OS_Seek(DSK.handle, OSSeekAbs, 0)) != OS_NOERR
			|| (err = OS_Write(DSK.handle, &hdr, &sz)) != OS_NOERR) {
			module_logger(&realDiskDSR, _LS|LOG_ERROR,
						  _("DOAD server:  cannot write disk header for image '%s' (%s)\n"),
						  OS_SpecToString1(&DSK.spec), OS_GetErrText(err));
			return 0;
		}
	}

	/* maintain invariants */
	if (!FDCsetfilesize()) 
		return 0;

	return 1;
}

static int
FDCreadheader(void)
{
	OSError err;
	DSKheader hdr;
	OSSize sz,len;
	u8 sector[256];

	OS_CheckProtection(&DSK.spec, &DSK.readonly);

	if (DSK.fulltrk) {
		sz = sizeof(hdr);

		if ((err = OS_Seek(DSK.handle, OSSeekAbs, 0)) != OS_NOERR
			|| (err = OS_Read(DSK.handle, &hdr, &sz)) != OS_NOERR) {
			module_logger(&realDiskDSR, _LS|LOG_ERROR,
						  _("DOAD server:  cannot read disk header for image '%s' (%s)\n"),
						  OS_SpecToString1(&DSK.spec), OS_GetErrText(err));
			return 0;
		}

		/* byteswap imported header */
		memcpy(DSK.hdr.magic, hdr.magic, sizeof(hdr.magic));
		DSK.hdr.version = hdr.version;
		DSK.hdr.tracks = hdr.tracks;
		DSK.hdr.sides = hdr.sides;
		DSK.hdr.tracksize = TI2HOST(hdr.tracksize);
		DSK.hdr.track0offs = TI2HOST(hdr.track0offs);

		/* verify */
		if (memcmp(DSK.hdr.magic, TRACK_MAGIC, TRACK_MAGIC_SIZE)) {
			module_logger(&realDiskDSR, _LS|LOG_ERROR,
						  _("DOAD server:  disk image '%s' has unknown type (got '%*s', expected '%*s')\n"),
						  OS_SpecToString1(&DSK.spec),  
						  TRACK_MAGIC_SIZE, DSK.hdr.magic,
						  TRACK_MAGIC_SIZE, TRACK_MAGIC);
			return 0;
		}	

		if (DSK.hdr.version < 1 || DSK.hdr.version > TRACK_VERSION) {
			module_logger(&realDiskDSR, _LS|LOG_ERROR,
						  _("DOAD server:  disk image '%s' has too new version (%d > %d)\n"),
						  OS_SpecToString1(&DSK.spec), DSK.hdr.version, TRACK_VERSION);
			return 0;
		}
	} else {

		/* no header: guess */
		if ((err = OS_GetSize(DSK.handle, &sz)) != OS_NOERR || sz < 256) {
			module_logger(&realDiskDSR, _LS|LOG_ERROR,
						  _("DOAD server:  cannot get info about disk image '%s' (%s)\n"),
						  OS_SpecToString1(&DSK.spec), OS_GetErrText(err));
			return 0;
		}

		/* read sector 0 */
		OS_Seek(DSK.handle, OSSeekAbs, 0);
		len = 256;
		if ((err = OS_Read(DSK.handle, sector, &len)) != OS_NOERR) {
			module_logger(&realDiskDSR, _LS|LOG_ERROR,
						  _("DOAD server:  cannot read sector 0 for '%s' (%s)\n"),
						  OS_SpecToString1(&DSK.spec), OS_GetErrText(err));
			return err;
		}

		memset(DSK.hdr.magic, 0, TRACK_MAGIC_SIZE);
		DSK.hdr.version = 0;
		DSK.hdr.tracks = sector[17];
		DSK.hdr.sides = sector[18];
		DSK.hdr.tracksize = sector[12] * 256L;
		DSK.hdr.track0offs = 0;
		if (DSK.hdr.tracks == 0 || DSK.hdr.sides == 0 || DSK.hdr.tracksize == 0)
		{
			DSK.hdr.sides = 1;
			DSK.hdr.tracksize = 256*9;
			DSK.hdr.tracks = sz / DSK.hdr.tracksize;
			if (DSK.hdr.tracks >= 80) {
				DSK.hdr.tracks /= 2;
				DSK.hdr.sides++;
				if (DSK.hdr.tracks >= 80) {
					DSK.hdr.tracks /= 2;
					DSK.hdr.tracksize <<= 1;
					if (DSK.hdr.tracks > 40) {
						DSK.hdr.tracks = 40;
					}
				}
			}
		}
	}

	if (DSK.hdr.tracksize > DSKbuffersize) {
		module_logger(&realDiskDSR, _LS|LOG_ERROR, _("Disk image has too large track size (%d > %d)\n"),
					  DSK.hdr.tracksize, DSKbuffersize);
		return 0;
	}

	return 1;
}

static int
FDCcreatedisk(int fulltrk)
{
	OSError err;

	module_logger(&realDiskDSR, _LS|LOG_ERROR,
				  _("DOAD server:  creating new disk image at DSK%d (%s)\n"),
				  DSK.num, OS_SpecToString1(&DSK.spec));

	/* defaults */
	DSK.fulltrk = fulltrk;
	memcpy(DSK.hdr.magic, TRACK_MAGIC, TRACK_MAGIC_SIZE);
	DSK.hdr.version = TRACK_VERSION;
	DSK.hdr.tracks = 40;
	DSK.hdr.sides = 1;
	DSK.hdr.tracksize = DSK.fulltrk ? DSKtracksize_SD : 256*9;
	DSK.hdr.track0offs = sizeof(DSKheader);

	/* create file */
	if (!data_create_file(diskimagepath, diskname[DSK.num - 1], 
						  &DSK.spec, &osDiskImageType)
		|| (err = OS_Open(&DSK.spec, OSReadWrite, &DSK.handle)) != OS_NOERR) 
	{
		module_logger(&realDiskDSR, _LS|LOG_ERROR,
					  _("DOAD server:  could not create disk (%s)\n"),
					  OS_GetErrText(err));
		DSK.handle = 0;
		return 0;
	}

	return FDCwriteheader();
}

static void
FDCopendisk(void)
{
	OSError     err;
	const char	*name;
	const char	*ptr;

	module_logger(&realDiskDSR, _L|L_1, _("FDCopendisk\n"));
	if (DSK.handle)
		FDCclosedisk();

	//DSK.status = fdc_BADRECORD;
	//DSK.trackid = DSK.sectorid = DSK.track = DSK.sector = 0;
	DSK.status = 0;

	/* disk file */
	name = diskname[DSK.num-1];

	/* get disk type from extension */
	ptr = strrchr(name, '.');
	if (!ptr) ptr = name + strlen(name);
	DSK.fulltrk = (strcasecmp(ptr, ".trk") == 0);

	if (data_find_file(diskimagepath, name, &DSK.spec)) 
	{
		if ((err = OS_Open(&DSK.spec, OSReadWrite, &DSK.handle)) != OS_NOERR
		&&	(err = OS_Open(&DSK.spec, OSReadOnly, &DSK.handle)) != OS_NOERR)
		{
			module_logger(&realDiskDSR, _L|L_0, _("Cannot access disk '%s' (%s)\n"),
						  OS_SpecToString1(&DSK.spec), OS_GetErrText(err));
			DSK.handle = 0;
			return;
		}
	}
	else
	{
		DSK.readonly = 0;
		if (!FDCcreatedisk(DSK.fulltrk)) {
			FDCclosedisk();
			return;
		}
	}

	/* get disk info */
	if (!FDCreadheader()) {
		if (!FDCcreatedisk(DSK.fulltrk)) {
			FDCclosedisk();
			return;
		}
		FDCreadheader();
	}

	DSK.dirty = 1;
	FDCseektotrack();
	FDCreadtrackdata();

	module_logger(&realDiskDSR, _L|L_0, _("Opened %s disk '%s' #%d,\n#tracks=%d, tracksize=%d, sides=%d\n"),
				  DSK.fulltrk ? "track-image" : "sector-image",
				  OS_SpecToString1(&DSK.spec),
		 DSK.num, DSK.hdr.tracks, DSK.hdr.tracksize, DSK.hdr.sides);
	module_logger(&realDiskDSR, _L|L_2, _("DSK.handle=%d\n"), DSK.handle);
}

static void dump_buffer(int offs, int len)
{
	int x;
	if (log_level(LOG_REALDISK) <= 2) return;
	if (len)
		module_logger(&realDiskDSR, _L|L_2, _("Buffer contents:\n"));
	for (x = offs; len-- > 0; x+=16, len-=16) {
		int         y;

		logger(_L|L_2, "%04X=", x);
		for (y = 0; y < 16; y++)
			logger(_L|L_2, "%02X ", DSK.buffer[x + y]);
		logger(_L|L_2, " ");
		for (y = 0; y < 16; y++)
			logger(_L|L_2, "%c",
				   isprint(DSK.buffer[x + y]) ? DSK.buffer[x +
														   y] :
				   '.');
		logger(_L|L_2, "\n");
	}

}

static int 
FDCwritedataat(u32 diskoffset, u32 bufoffset, u32 size)
{
	OSError err;
	OSSize ret = size;
	OSSize diskoffs = DSK.trackoffset + diskoffset;

	module_logger(&realDiskDSR, _L|L_1, 
				  _("writing %d bytes of data on track %d, trackoffset = %d, offset = %d\n"),
				  size, DSK.seektrack, DSK.trackoffset, diskoffs);

	// dump contents
	dump_buffer(bufoffset, size);

	DSK.status &= ~(fdc_WRITEPROT | fdc_LOSTDATA | fdc_CRCERR);

	if ((err = OS_Seek(DSK.handle, OSSeekAbs, diskoffs)) != OS_NOERR) {
		DSK.status |= fdc_LOSTDATA;
		//DSK.status |= fdc_BADRECORD;

		module_logger(&realDiskDSR, _LS|LOG_ERROR,
					  _("DOAD server:  could not seek disk '%s' to byte %d (%s)\n"),
					  OS_SpecToString1(&DSK.spec), diskoffs,
					  OS_GetErrText(err));
		return 0;
	}
	else if ((err = OS_Write(DSK.handle, (char *) DSK.buffer + bufoffset, &ret)) != OS_NOERR
		|| ret != size) {
		DSK.status |= fdc_CRCERR | fdc_LOSTDATA;

		module_logger(&realDiskDSR, _LS|LOG_ERROR,
					  _("DOAD server:  could not write %d bytes of data to disk '%s' at byte %d (%s)\n"),
					  size,
					  OS_SpecToString1(&DSK.spec), 
					  diskoffset,
					  OS_GetErrText(err));
		return 0;
	}
	return 1;
}

static void 
format_sector_track(void)
{
	// interpret data 
	u8 *sec = DSK.buffer;
	int cnt = DSK.buflen;
	while (cnt > 0) {
		u8 *data = sec;
	retry:
		while (cnt > 0 && *data != 0xfe) { data++; cnt--; }
		if (cnt > 6) {
			u8 track, side, sector, size;
			u32 offs, sz;
			u16 crc = 0xffff, crcid;

			// ID marker
			data++; cnt--;

			crc = calc_crc(crc, 0xfe);
			track = *data; crc = calc_crc(crc, *data++);
			side = *data; crc = calc_crc(crc, *data++);
			sector = *data; crc = calc_crc(crc, *data++);
			size = *data; crc = calc_crc(crc, *data++);
			crcid = (*data++<<8); crcid += *data++;

			if (crcid == 0xf7ff) crcid = crc;
			if (0 && crcid != crc) {
				module_logger(&realDiskDSR, _L|L_3,
							  _("retrying for id marker (CRC=%04X != %04X)\n"),
							  crc, crcid);
				goto retry;
			}
			cnt -= 6;

			module_logger(&realDiskDSR, _L|L_2, _("Formatting sector track:%d, side:%d, sector:%d, size:%d, crc=%04X\n"),
						  track, side, sector, size, crc);

			sz = 128 << size;
			offs = sector * sz;
			if (offs >= DSK.hdr.tracksize) {
				module_logger(&realDiskDSR, _LS|LOG_ERROR,
							  _("Program is formatting track on '%s' with non-ordinary sectors;\n"
								"this does not work with sector-image disks"), 
							  OS_SpecToString1(&DSK.spec));
				offs = 0;
			}

		retry1:
			while (cnt > 0 && *data != 0xfb) { data++; cnt--; }
			data++; cnt--;

			crc = 0xffff;

			if (cnt >= sz + 2) {
				u8 *ptr = data;
				crc = calc_crc(crc, 0xfb);
				while (ptr < data + sz) {
					crc = calc_crc(crc, *ptr++);
				}
				crcid = *ptr++<<8; crcid += *ptr++;
				if (crcid == 0xf7ff) crcid = crc;
				if (0 && crc != crcid) {
					module_logger(&realDiskDSR, _L|L_3,
								  _("retrying for sector data (CRC=%04X != %04X)\n"),
								  crc, crcid);
					goto retry1;
				}

				if (!FDCwritedataat(offs, data - DSK.buffer, sz)) {
					return;
				}
				data += sz + 2;	// + crc
				cnt -= sz + 2;
			} else {
				module_logger(&realDiskDSR, _LS|LOG_ERROR,
							  _("Lost sector data in format of sector-image disk '%s'\n"), 
							  OS_SpecToString1(&DSK.spec));
				break;
			}
		}
	}
}

/*	Flush the sector or track contents to disk */
static void
FDCflush(void)
{
	if (!DSK.hold) return;

	if (DSK.handle && DSK.buflen && DSK.dirty) {
		OSSize      ret = DSK.buflen;
		OSSize		offs;

		//DSK.status &= ~fdc_LOSTDATA;

		if (DSK.readonly) {
			DSK.status |= fdc_WRITEPROT;

			module_logger(&realDiskDSR, _LS|LOG_ERROR,
						  _("DOAD server:  disk image '%s' is write-protected\n"),
						  OS_SpecToString1(&DSK.spec));
			return;
		}


		if (DSK.fulltrk) {
			if (DSK.command == FDC_writesector) {
				u8			*ptr, *end;

				// write new ID field
				offs = DSK.idoffset;
				if (DSK.buffer[offs] != 0xfe)
					module_logger(&realDiskDSR, _L|LOG_FATAL,
								  _("Inconsistent idoffset (%d)"), DSK.idoffset);

				DSK.buffer[offs+0] = 0xfe;
				DSK.buffer[offs+1] = DSK.trackid;
				DSK.buffer[offs+2] = DSK.sideid;
				DSK.buffer[offs+3] = DSK.sectorid;
				DSK.buffer[offs+4] = DSK.sizeid;
				DSK.buffer[offs+5] = DSK.crcid >> 8;
				DSK.buffer[offs+6] = DSK.crcid & 255;

				if (!FDCwritedataat(offs, offs, 7)) {
					return;
				}

				// write data with new CRC
				offs = DSK.dataoffset;
				ptr = DSK.buffer + offs;
				if (*ptr++ != 0xfb)
					module_logger(&realDiskDSR, _L|LOG_FATAL,
								  _("Inconsistent dataoffset (%d)"), DSK.dataoffset);

				end = ptr + (128 << DSK.sizeid);

				DSK.crcid = 0xffff;
				while (ptr < end)
					DSK.crcid = calc_crc(DSK.crcid, *ptr++);
				*ptr++ = DSK.crcid >> 8;
				*ptr++ = DSK.crcid & 0xff;
				
				if (!FDCwritedataat(offs, offs, ptr - DSK.buffer - offs)) {
					return;
				}
			} else if (DSK.command == FDC_writetrack) {
				// write entire track

				if (!FDCwritedataat(0, 0, DSK.hdr.tracksize)) {
					return;
				}
			}
		}
		else {
			// simple disks only write data

			if (DSK.command == FDC_writesector) {
				if (!FDCwritedataat(DSK.dataoffset, DSK.dataoffset, DSK.buflen)) {
					return;
				}
			} else if (DSK.command == FDC_writetrack) {
				format_sector_track();
			}
		}
	}

	DSK.dirty = 0;
}

/*	When hold changes, we dump written data to disk */
static void
FDChold(int onoff)
{
	if (onoff) {
		module_logger(&realDiskDSR, _L|L_1, _("FDChold on\n"));
		/* about to read or write */
	} else {
		module_logger(&realDiskDSR, _L|L_1, _("FDChold off\n"));
		if (DSK.hold 
			&& (DSK.command == FDC_writesector || DSK.command == FDC_writetrack)) 
		{
			FDCflush();
		}
	}
}

static void
FDCclosedisk(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDCclosedisk\n"));
	if (DSK.handle) {
		FDCflush();
		DSK.buflen = DSK.bufpos = 0;
		OS_Close(DSK.handle);
		DSK.handle = 0;
	}
}


/*
  Seek to the current DSK.seektrack.
  Returns 0 for error.
*/
static int
FDCseektotrack(void)
{
	u32         offs;
	OSError	err;

	if (!DSK.dirty) return 1;

	DSK.trackoffset = 0;
	DSK.trackbyteoffset = 0;
	DSK.idoffset = DSK.dataoffset = 0;
	DSK.dirty = 1;

	if (!DSK.handle) return 0;

	if (DSK.seektrack >= DSK.hdr.tracks || DSK.side >= DSK.hdr.sides) {
		module_logger(&realDiskDSR, _L|L_2, _("cannot seek past end of disk Tr%d(%d) Sd%d(%d)\n"), 
					  DSK.seektrack, DSK.hdr.tracks, DSK.side, DSK.hdr.sides);
		return 0;
	}

	offs = DSK.seektrack;
	if (DSK.side) offs += DSK.hdr.tracks;
	offs *= DSK.hdr.tracksize;
	offs += DSK.hdr.track0offs;

	module_logger(&realDiskDSR, _L|L_2, _("seeking to Tr%d Sd%d = byte %d of file\n"), 
				  DSK.seektrack, DSK.side, offs);

	if ((err = OS_Seek(DSK.handle, OSSeekAbs, offs)) != OS_NOERR) {
		module_logger(&realDiskDSR, _LS | LOG_ERROR, _("failed to seek!\n"));
		return 0;
	}

	DSK.trackoffset = offs;
	DSK.trackbyteoffset = 0;
	DSK.idoffset = DSK.dataoffset = 0;

	return 1;
}

static int
FDCreadtrackdata(void)
{
	OSSize sz;
	OSError err;

	if (!DSK.dirty) return 1;

	module_logger(&realDiskDSR, _L|L_2, _("Reading data from track %d\n"), 
				  DSK.seektrack);

	/* read track */
//	DSK.status &= ~fdc_LOSTDATA;

	sz = DSK.hdr.tracksize;
	if ((err = OS_Seek(DSK.handle, OSSeekAbs, DSK.trackoffset)) != OS_NOERR
		|| (err = OS_Read(DSK.handle, DSK.buffer, &sz)) != OS_NOERR
		|| sz < DSK.hdr.tracksize) 
	{
		module_logger(&realDiskDSR, _LS | LOG_ERROR, _("failed to read track %d data\n"),
					  DSK.seektrack, OS_GetErrText(err));

		//DSK.status |= fdc_LOSTDATA;
		DSK.dirty = 1;
		return 0;
	}

	DSK.dirty = 0;
	return 1;
}

/*
static void
FDCwritetrack(void)
{
	if (DSK.handle) {
		OSError     err;
		OSSize      ret = DSK.bufpos;

		err = OS_Write(DSK.handle, (void *) DSK.buffer, &ret);
		if (err != OS_NOERR)
			DSK.status |= fdc_BADRECORD;
		else if (ret != DSK.bufpos)
			DSK.status |= fdc_LOSTDATA;
	}
	DSK.bufpos = 0;
}
*/

static void
FDCseekhome(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDC seek home\n"));
	if (DSK.seektrack != 0) {
		DSK.seektrack = DSK.track = 0;
		DSK.dirty = 1;
		FDCseektotrack();
	}
	DSK.status |= fdc_TRACK0;

	DSK.status &= ~(fdc_BADRECORD|fdc_CRCERR);
	if (DSK.flags & fl_verify_track) {
		FDCfindIDmarker();
		if (DSK.trackid != 0)
			DSK.status |= fdc_BADRECORD;
	}
}

static void
FDCseek(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDC seek, T%d s%d\n"), DSK.lastbyte, DSK.side);

	/* current track written WTADDR, desired track written to WTDATA */
	//DSK.seektrack += DSK.lastbyte - DSK.track;
	DSK.seektrack = DSK.lastbyte;

	DSK.dirty = 1;
	FDCseektotrack();

	DSK.status &= ~(fdc_TRACK0);
	DSK.status &= ~(fdc_BADRECORD);

	DSK.track = DSK.seektrack;


	if (DSK.flags & fl_verify_track)
	{
		int tries, origoffs = DSK.idoffset;

		for (tries = 0; tries < DSK.hdr.tracksize / 8 ; tries++) {
			if (!FDCfindIDmarker()) 
				break;
			if (DSK.trackid == DSK.track) 
				break;
			if (DSK.idoffset == origoffs)
				break;
		}
	
		if (DSK.trackid != DSK.track) {
		   	DSK.status |= fdc_BADRECORD;
			module_logger(&realDiskDSR, _L|L_1, _("FDC seek, record mismatch (%d != %d) %d\n"), DSK.trackid, DSK.track, tries);
		}

	}
}

static void
FDCstepin(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDC step in, T%d s%d\n"), DSK.seektrack, DSK.side);

	DSK.seektrack++;
	DSK.track++;
	DSK.dirty = 1;

	FDCseektotrack();
	DSK.status &= ~fdc_BADRECORD;

	if (DSK.flags & fl_verify_track)
	{
		int tries;

		for (tries = 0; tries < 18; tries++) {
			if (!FDCfindIDmarker()) break;
			if (DSK.trackid == DSK.track) 
				break;
		}
	
		if (DSK.trackid != DSK.track) {
			DSK.status |= fdc_BADRECORD;
			module_logger(&realDiskDSR, _L|L_1, _("FDC seek, record mismatch (%d != %d) %d\n"), DSK.trackid, DSK.track, tries);
		}

	}
}

/*	Find a sector ID on the track */
static int
FDCfindIDmarker(void)
{
	module_logger(&realDiskDSR, _L|L_2, _("FDC find ID marker\n"));
	
	FDCreadtrackdata();
	if (DSK.fulltrk) {
		u8 *ptr, *start, *end, *cur, c;
		bool found = false;
		int cnt;

		/* scan track for markers */
		ptr = DSK.buffer + DSK.idoffset + 1;
		//ptr = DSK.buffer + DSK.trackbyteoffset;
		start = DSK.buffer;
		end = DSK.buffer + DSK.hdr.tracksize;
		cnt = DSK.hdr.tracksize;

#define NEXT() (c=*ptr, ptr=ptr+1<end?ptr+1:start, cnt--, c)
#define UP(x)	(ptr+(x)<end ? (ptr+(x)) : (ptr+(ptr-start+(x))%(end-start)))

		module_logger(&realDiskDSR, _L|L_3, _("FDCfindIDmarker: starting at %d, traversing [%d...%d) (track offset: %d)\n"), ptr-DSK.buffer, start-DSK.buffer, end-DSK.buffer, DSK.trackoffset);

		while (!found && cnt > 0) {
			// sync
			//while (cnt > 0 && *ptr == 0xff) NEXT();

			// scan for address field marker (account for broken disks)
			while (cnt > 0 && *ptr != 0xfe) NEXT();
			if (cnt < 7) break;

			// reset CRC
			cur = ptr;
			DSK.idoffset = ptr - start;
			DSK.crc = 0xffff;
			DSK.crc = calc_crc(DSK.crc, NEXT());

			// get ID
			DSK.trackid = NEXT();
			DSK.sideid = NEXT();
			DSK.sectorid = NEXT();
			DSK.sizeid = NEXT();
			DSK.crc = calc_crc(DSK.crc, DSK.trackid);
			DSK.crc = calc_crc(DSK.crc, DSK.sideid);
			DSK.crc = calc_crc(DSK.crc, DSK.sectorid);
			DSK.crc = calc_crc(DSK.crc, DSK.sizeid);

			DSK.crcid = (NEXT()<<8); DSK.crcid |= NEXT();

			// this algorithm does NOT WORK
			if (0 && DSK.crc != DSK.crcid)
			{
				module_logger(&realDiskDSR, _L|L_1, _("FDCfindIDmarker: failed ID CRC check (%04X != %04X)\n"),
							  DSK.crcid, DSK.crc);
				
				DSK.status |= fdc_CRCERR;
			}
			else // if ((DSK.command >= FDC_readsector && DSK.command != FDC_readIDmarker) || 
			{
				module_logger(&realDiskDSR, _L|L_3, _("FDCfindIDmarker: T%d, S%d, s%d\n"), DSK.trackid, DSK.sideid, DSK.sectorid);
				if (DSK.command != FDC_readIDmarker || DSK.sideid == DSK.side)
				{
					// found one; look somewhere else next time
					module_logger(&realDiskDSR, _L|L_3, _("FDCfindIDmarker: found marker at offset >%04X\n"),
								  cur - start);
					DSK.trackbyteoffset = cur - start + 1;
					found = true;
				}
			}
		}

#undef UP
#undef NEXT

		return found;
	}
	else {
		/* easy */
		module_logger(&realDiskDSR, _L|L_1, _("FDCfindIDmarker: noop success\n"));
		DSK.trackid = DSK.track;
		DSK.sectorid = DSK.sector;
		DSK.sideid = DSK.side;
		DSK.sizeid = 2;	//!!! hack
					  
		DSK.trackbyteoffset = (128<<DSK.sizeid) * DSK.sectorid;
		return 1;
	}
}

/*	Match the current ID with the desired track/sector id */
static int
FDCmatchIDmarker(void)
{

	module_logger(&realDiskDSR, _L|L_1, _("FDC match ID marker: looking for T%d, S%d\n"), DSK.track, DSK.sector);
	
	DSK.status &= ~(fdc_BADRECORD | fdc_CRCERR);

	if (DSK.fulltrk)
	{
		int tries, origoffs = DSK.idoffset;
		bool found = false;
		for (tries = 0; !found && tries < DSK.hdr.tracksize; tries++)
		{
			if (!FDCfindIDmarker())
				return 0;

			if (DSK.trackid == DSK.track
				//&& DSK.sideid == DSK.side
				&& DSK.sectorid == DSK.sector
				//&& DSK.crcid == DSK.crc
				)
			{
				found = true;
			}
			else
			{
				module_logger(&realDiskDSR, _L|L_2, _("unmatching track T%d S%d s%d z%d\n"), 
							  DSK.trackid, DSK.sectorid, DSK.sideid, DSK.sizeid);
			}
			//if (DSK.idoffset == origoffs) break;
		}

		if (!found) {
			module_logger(&realDiskDSR, _L|L_1, _("FDCmatchIDmarker failed\n"));
			DSK.status |= fdc_BADRECORD;
			return 0;
		}
		else {
			module_logger(&realDiskDSR, _L|L_1, _("FDCmatchIDmarker succeeded: track %d, sector %d, side %d, size %d\n"),
						  DSK.trackid, DSK.sectorid, DSK.sideid, DSK.sizeid);
			return 1;
		}
	}	/* else sector disk */
	else {
		/* easy */

		if (!FDCfindIDmarker())
			return 0;

		/* simple disks cannot renumber sectors */
		if (FDCseektotrack() == 0 
			|| DSK.track >= DSK.hdr.tracks 
			|| DSK.sector >= DSK.hdr.tracksize / 256) 
		{
			DSK.status |= fdc_BADRECORD;
			DSK.idoffset = 0;
	  
			module_logger(&realDiskDSR, _L|L_1, _("FDCmatchIDmarker failed\n"));
			return 0;
		} 
		else 
		{
			DSK.trackid = DSK.track;
			DSK.sectorid = DSK.sector;
			DSK.sideid = DSK.side;
			DSK.sizeid = 1;		// 256 bytes

			// ID marker lives virtually at end of track
			DSK.idoffset = DSK.hdr.tracksize;
			DSK.buffer[DSK.idoffset+0] = 0xfb;
			DSK.buffer[DSK.idoffset+1] = DSK.trackid;
			DSK.buffer[DSK.idoffset+2] = DSK.sideid;
			DSK.buffer[DSK.idoffset+3] = DSK.sectorid;
			DSK.buffer[DSK.idoffset+4] = DSK.sizeid;
			DSK.buffer[DSK.idoffset+5] = 0xf7;
			DSK.buffer[DSK.idoffset+6] = 0xff;

			DSK.idoffset = DSK.hdr.tracksize;

			module_logger(&realDiskDSR, _L|L_1, _("FDCmatchIDmarker succeeded: track %d, sector %d, side %d, size %d\n"),
						  DSK.trackid, DSK.sectorid, DSK.sideid, DSK.sizeid);

			return 1;
		}
	}
}

/*	Scan forward from the ID field to the data field */
static int
FDCfindDataMarker(void)
{
	if (DSK.fulltrk) {
		// search for data field

		u8 *ptr, *start, *end, c;
		bool found = false;
		int cnt;

		/* scan forward for sector data */
		DSK.status &= ~(fdc_CRCERR | fdc_BADRECORD);

		ptr = DSK.buffer + DSK.idoffset;
		start = DSK.buffer;
		end = DSK.buffer + DSK.hdr.tracksize;
		cnt = 8 + 12 + 8;	/* skip ID, id/data separator, sync bytes */

#define NEXT() (c=*ptr, ptr=ptr+1<end?ptr+1:start, cnt--, c)

		while (!found && cnt > 0) {
			// sync 
			//while (cnt > 0 && *ptr == 0x00) NEXT();

			// scan for data field marker
			while (cnt > 0 && *ptr != 0xfb) NEXT();

			// reset CRC
			DSK.crc = 0xffff;
			DSK.crc = calc_crc(NEXT(), DSK.crc);

			DSK.dataoffset = ptr - start;
			DSK.trackbyteoffset = ptr - start;
			found = true;
		}

		if (!found) {
			return 0;
		}

#undef NEXT
	}
	else {
		// simple: physical offset of sector

		DSK.dataoffset = 256 * DSK.sectorid;
		DSK.trackbyteoffset = DSK.dataoffset;
	}
	return 1;
}

/*	Set up to read sector contents */
static void
FDCreadsector(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDC read sector, T%d S%d s%d\n"), DSK.track, DSK.sector, DSK.side);
	if (!DSK.handle) {
		//DSK.status |= fdc_LOSTDATA;
		return;
	}

	if (!FDCreadtrackdata())
		return;

	if (!FDCmatchIDmarker()) 
		return;

	if (!FDCfindDataMarker())
		return;

	DSK.buflen = 128 << DSK.sizeid;
	DSK.bufpos = 0;
}

/*
	Set up to write sector contents.  If this looks just like a read,
	that's because it basically is. We have to find out where to write
	on the track, and then fill in the old data buffer.  This is why
	we can't write to unformatted disks.
*/
static void
FDCwritesector(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDC write sector, T%d S%d s%d\n"), DSK.track, DSK.sector, DSK.side);

	if (!DSK.handle) {
		return;
	}

	if (!FDCreadtrackdata())
		return;

	if (!FDCmatchIDmarker()) 
		return;

	if (!FDCfindDataMarker())
		return;

	DSK.buflen = 128 << DSK.sizeid;
	DSK.bufpos = 0;
}

static void
FDCwritetrack(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDC write track, #%d\n"), DSK.seektrack);

	DSK.dirty = 1;
	DSK.buflen = DSK.fulltrk ? DSK.hdr.tracksize : DSKbuffersize;
	DSK.bufpos = 0;
	DSK.trackbyteoffset = 0;
}

/*	Read contents of the track */
static void
FDCreadtrack(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDC read track, #%d\n"), DSK.seektrack);

	if (!FDCreadtrackdata())
		return;

	DSK.buflen = DSK.hdr.tracksize;
	DSK.bufpos = 0;
	DSK.trackbyteoffset = 0;
}

/*	Read the current ID marker */
static void
FDCreadIDmarker(void)
{
	struct fdc_secrec *fsr;

	/* since we can't tell what's a valid ID field,
	   always go to the beginning of the track to search. */

	//DSK.idoffset = 0;

	if (!FDCfindIDmarker()) {
		DSK.status |= fdc_LOSTDATA | fdc_CRCERR;
		return;
	}

	/* store data past end of track */
	fsr = (struct fdc_secrec *) (DSK.buffer + DSK.hdr.tracksize);

	fsr->track = DSK.trackid;
	fsr->side = DSK.sideid;
	fsr->sector = DSK.sectorid;
	fsr->seclen = DSK.sizeid;
	fsr->crc = HOST2TI(DSK.crcid);

	DSK.trackbyteoffset = DSK.hdr.tracksize;
	DSK.buflen = sizeof(*fsr);
	DSK.bufpos = 0;

	module_logger(&realDiskDSR, _L|L_1, _("FDC read ID marker: track=%d sector=%d side=%d size=%d\n"),
				  DSK.trackid, DSK.sectorid, DSK.sideid, DSK.sizeid);

}

static void
FDCinterrupt(void)
{
	module_logger(&realDiskDSR, _L|L_1, _("FDC interrupt\n"));
	FDCflush();

	DSK.buflen = DSK.bufpos = 0;
//	DSK.status = (DSK.seektrack == 0 ? fdc_TRACK0 : 0);
//	DSK.status = 0;
}

//////////////////

static      s8
FDCreadbyte(u32 addr)
{
	u8          ret;

	switch ((addr - 0x5ff0) >> 1) {
	case 0:					/* R_RDSTAT */
		FDCgetstatus();
		ret = DSK.status;
		module_logger(&realDiskDSR, _L|L_1, _("FDC read status >%04X = >%02X\n"), addr, (u8) ret);
		break;

	case 1:					/* R_RTADDR */
		ret = DSK.track;
		module_logger(&realDiskDSR, _L|L_1, _("FDC read track addr >%04X = >%02X\n"), addr, (u8) ret);
		break;

	case 2:					/* R_RSADDR */
		ret = DSK.sector;
		module_logger(&realDiskDSR, _L|L_1, _("FDC read sector addr >%04X = >%02X\n"), addr, (u8) ret);
		break;

	case 3:					/* R_RDDATA */
		/* read from circular buffer */

		if (DSK.bufpos == 0) {
			if (log_level(LOG_REALDISK) > 2) {
				// dump contents
				dump_buffer(DSK.trackbyteoffset, DSK.buflen);
			}
		}

		if (DSK.hold && DSK.buflen) {
			int offs = (DSK.trackbyteoffset+DSK.bufpos); //%DSK.hdr.tracksize;
			ret = DSK.buffer[offs];
			DSK.crc = calc_crc(DSK.crc, ret);
			if (++DSK.bufpos >= DSK.buflen) {
				DSK.bufpos = 0;
			}
		} else {
			ret = DSK.lastbyte;
		}

		module_logger(&realDiskDSR, _L|L_4, _("FDC read data (%d) >%02X (%d)\n"), 
					  DSK.trackbyteoffset, (u8) ret, DSK.bufpos);

		break;

	case 4:					/* R_WTCMD */
	case 5:					/* R_WTADDR */
	case 6:					/* R_WSADDR */
	case 7:					/* R_WTDATA */
		ret = 0x00;
		module_logger(&realDiskDSR, _L|L_1, _("FDC read write xxx >%04X = >%02X\n"), addr, (u8) ret);
		break;
	}
	return ret;

}

static void
FDCwritebyte(u32 addr, u8 val)
{
	switch ((addr - 0x5ff0) >> 1) {
	case 0:					/* W_RDSTAT */
	case 1:					/* W_RTADDR */
	case 2:					/* W_RSADDR */
	case 3:					/* W_RDDATA */
		module_logger(&realDiskDSR, _L|L_1, _("FDC write read xxx >%04X, >%02X\n"), addr, val);
		break;

	case 4:					/* W_WTCMD */
		FDCflush();
		DSK.buflen = DSK.bufpos = 0;

		module_logger(&realDiskDSR, _L|L_1, _("FDC command >%02X\n"), val);
		
		DSK.command = val & 0xf0;
		DSK.flags = val;
		switch (DSK.command) {
		case FDC_seekhome:
			FDCseekhome();
			break;
		case FDC_seek:
			FDCseek();
			break;
		case FDC_stepin:
			FDCstepin();
			break;
		case FDC_readsector:
			FDCreadsector();
			break;
		case FDC_writesector:
			FDCwritesector();
			break;
		case FDC_readIDmarker:
			FDCreadIDmarker();
			break;
		case FDC_interrupt:
			FDCinterrupt();
			break;
		case FDC_writetrack:
			FDCwritetrack();
			break;
		case FDC_readtrack:
			FDCreadtrack();
			break;
		default:
			module_logger(&realDiskDSR, _L|L_1, _("unknown FDC command >%02X\n"), val);
		}
		break;

	case 5:					/* W_WTADDR */
		DSK.track = val;
		//DSK.status &= ~fdc_LOSTDATA;
		DSK.addr = W_WTADDR;
		module_logger(&realDiskDSR, _L|L_1, _("FDC write track addr >%04X, >%02X\n"), addr, val);
		break;

	case 6:					/* W_WSADDR */
		DSK.sector = val;
		//DSK.status &= ~fdc_LOSTDATA;
		module_logger(&realDiskDSR, _L|L_1, _("FDC write sector addr >%04X, >%02X\n"), addr, val);
		break;

	case 7:					/* W_WTDATA */
		module_logger(&realDiskDSR, _L|(DSK.hold?L_4:L_1), _("FDC write data (%d,%d) >%02X\n"), 
					  DSK.trackbyteoffset, DSK.bufpos, (u8) val);
		if (DSK.hold == 0) {
			DSK.lastbyte = val;
			/*if (DSK.addr == W_WTADDR) {
				DSK.seektrack = val;
			}
			else if (DSK.addr == W_WSADDR)
			DSK.sector = val;*/
		} else {
			if (DSK.command == FDC_writesector || DSK.command == FDC_writetrack) {
				if (DSK.buflen) {
					/* fill circular buffer */
					int offs = (DSK.trackbyteoffset+DSK.bufpos); //%DSK.hdr.tracksize;
					DSK.buffer[offs] = val;
					DSK.crc = calc_crc(DSK.crc, val);
					if (++DSK.bufpos >= DSK.buflen) {
						/*	the NEXT byte is an overrun
						module_logger(&realDiskDSR, _LS|LOG_ERROR, 
									  _("Disk data write overrun (%d > %d)\n"),
									  DSK.bufpos, DSK.buflen);
						*/
						//FDCflush();
						DSK.bufpos = 0;
					}
				}
			} else {
				module_logger(&realDiskDSR, _L|L_2, _("Unexpected data write >%02X for command >%02X\n"),
							  val, DSK.command);
			}
		}
	}
}


static void
bwdrom_realdisk(const mrstruct *mr, u32 addr, s8 val)
{
	if (addr >= 0x5ff0)
		FDCwritebyte(addr, ~val);
}

static      s8
brdrom_realdisk(const mrstruct *mr, u32 addr)
{
	if (addr >= 0x5ff0)
		return ~FDCreadbyte(addr);
	else
		return BYTE(mr->areamemory, addr - 0x5C00);
}

extern vmModule realDiskDSR;

mrstruct    dsr_rom_realdisk_handler = {
	realdiskdsr, realdiskdsr, NULL,			/* ROM */
	NULL,
	NULL,
	NULL,
	NULL
};

//  this is only used on the last area of the DSR ROM block */
mrstruct    dsr_rom_realdisk_io_handler = {
	realdiskdsr + 0x1C00, NULL, NULL,
	NULL,
	brdrom_realdisk,
	NULL,
	bwdrom_realdisk
};

static      u32
cruwRealDiskROM(u32 addr, u32 data, u32 num)
{
	if (data) {
		module_logger(&realDiskDSR, _L|L_1, _("real disk DSR on\n"));
		report_status(STATUS_DISK_ACCESS, 1, true);
		dsr_set_active(&realDiskDSR);

		SET_AREA_HANDLER(0x4000, 0x1c00, &dsr_rom_realdisk_handler);
		SET_AREA_HANDLER(0x5c00, 0x400, &dsr_rom_realdisk_io_handler);

		module_logger(&realDiskDSR, _L|L_1, _("DSR Read >4000 = >%02X\n"), memory_read_byte(0x4000)&0xff);
	} else {
		module_logger(&realDiskDSR, _L|L_1, _("real disk DSR off\n"));
		report_status(STATUS_DISK_ACCESS, 1, false);
		dsr_set_active(NULL);

		SET_AREA_HANDLER(0x4000, 0x2000, &zero_memory_handler);
	}
	return 0;
}

static      u32
cruwRealDiskMotor(u32 addr, u32 data, u32 num)
{
	module_logger(&realDiskDSR, _L|L_1, _("CRU Motor %s\n"), data ? "on" : "off");
	DSK.motor = data;
	return 0;
}

static      u32
cruwRealDiskHold(u32 addr, u32 data, u32 num)
{
	module_logger(&realDiskDSR, _L|L_1, _("CRU hold %s\n"), data ? "on" : "off");

/*
	if (data)
		DSK.status |= fdc_READY;
	else
		DSK.status &= ~fdc_READY;
*/

	FDChold(data);
	DSK.hold = data;
	return 0;
}

static      u32
cruwRealDiskHeads(u32 addr, u32 data, u32 num)
{
	module_logger(&realDiskDSR, _L|L_1, _("CRU Heads %s\n"), data ? "on" : "off");

	if ((DSK.command == FDC_seekhome || DSK.command == FDC_seek) && data) {
//		if (FDCreadtrackdata()) {
//			FDCfindIDmarker();
//			DSK.status |= (DSK.command == FDC_seekhome && DSK.seektrack == 0 ? fdc_TRACK0 : 0);
//		}
	} else {
		DSK.status &= ~fdc_TRACK0;
	}

	return 0;
}

static      u32
cruwRealDiskSel(u32 addr, u32 data, u32 num)
{
	u8          newnum = (addr - 0x1106) >> 1;

	module_logger(&realDiskDSR, _L|L_1, _("CRU disk select, #%d\n"), newnum);

	if (data) {
		module_logger(&realDiskDSR, _L|L_1, _("opening (DSK.num=%d)\n"), DSK.num);
		if (newnum != DSK.num) {
			FDCclosedisk();
			DSK.num = newnum;
		}

		if (!DSK.handle) {
			FDCopendisk();
		}
	} else if (newnum == DSK.num) {
		module_logger(&realDiskDSR, _L|L_1, _("closing\n"));
		FDCclosedisk();
		DSK.num = 0;
	}
	return 0;
}

static      u32
cruwRealDiskSide(u32 addr, u32 data, u32 num)
{
	module_logger(&realDiskDSR, _L|L_1, _("CRU disk side #%d\n"), data);

	DSK.side = data;
//	FDCseektotrack();
	return 0;
}

static      u32
crurRealDiskPoll(u32 addr, u32 data, u32 num)
{
	return 0;
}

/*************************************/

static
DECL_SYMBOL_ACTION(realdisk_disk_change)
{
	int         dsknum = sym->name[9] - '0';

	if (task == caa_READ)
		return 0;

	if (!dsr_is_real_disk(dsknum))
		module_logger(&realDiskDSR, _L|LOG_WARN|L_1,
			  _("DOAD server:  DSK%d (%s) is inaccessible\n"
			   "when emulated (FIAD) disk DSR is loaded\n"),
			  dsknum, diskname[dsknum - 1]);

	if (DSK.num == dsknum) {
		module_logger(&realDiskDSR, _L|LOG_WARN, _("Changing disk %d while it's being accessed!\n"), DSK.num);
		FDCclosedisk();
	}
	return 1;
}

/************************************/


static      vmResult
realdisk_getcrubase(u32 * base)
{
	*base = 0x1100;
	return vmOk;
}

static      vmResult
realdisk_filehandler(u32 code)
{
	/* shouldn't ever get here */
	return vmOk;
}

static      vmResult
realdisk_detect(void)
{
	return vmOk;
}

static      vmResult
realdisk_init(void)
{
	command_symbol_table *realdiskcommands =
		command_symbol_table_new(_("TI Disk DSR Options"),
								 _("These commands control the TI 'real' disk-on-a-disk (DOAD) emulation"),

    	 command_symbol_new("DiskImagePath",
    						_("Set directory list to search for DOAD disk images"),
							c_STATIC,
    						NULL /* action */ ,
    						RET_FIRST_ARG,
    						command_arg_new_string
    						(_("path"),
    						 _("list of directories "
    						 "separated by one of these characters: '"
    						 OS_ENVSEPLIST "'"),
    						 NULL /* action */ ,
    						 -1, &diskimagepath,
    						 NULL /* next */ )
    						,
		  command_symbol_new("DiskImage1|DSK1Image",
    						 _("DOAD image in drive 1"),
							 c_STATIC,
    						 realdisk_disk_change,
    						 RET_FIRST_ARG,
    						 command_arg_new_string
    						 (_("file"),
    						  _("name of DOAD image"),
    						  NULL /* action */ ,
    						  ARG_STR(diskname[0]),
    						  NULL /* next */ )
    						 ,
		   command_symbol_new("DiskImage2|DSK2Image",
    						  _("DOAD image in drive 2"),
							  c_STATIC,
    						  realdisk_disk_change,
    						  RET_FIRST_ARG,
    						  command_arg_new_string
    						  (_("file"),
    						   _("name of DOAD image"),
    						   NULL /* action */ ,
    						   ARG_STR(diskname[1]),
    						   NULL /* next */ )
    						  ,
    		command_symbol_new("DiskImage3|DSK3Image",
    						   _("DOAD image in drive 3"),
							   c_STATIC,
    						   realdisk_disk_change,
    						   RET_FIRST_ARG,
    						   command_arg_new_string
    						   (_("file"),
    							_("name of DOAD image"),
    							NULL /* action */ ,
    							ARG_STR(diskname[2]),
    							NULL /* next */ )
    						   ,

    		 command_symbol_new("DiskDSRFileName",
    							_("Name of DSR ROM image which fits in the CPU address space >4000...>5FFF"),
								c_STATIC,
    							NULL /* action */ ,
    							RET_FIRST_ARG,
    							command_arg_new_string
    							(_("file"),
    							 _("name of binary image"),
    							 NULL /* action */ ,
    							 ARG_STR(realdiskfilename),
    							 NULL /* next */ )
    							,

			NULL /* next */ ))))),

    	 NULL /* sub */ ,

    	 NULL	/* next */
		);

	command_symbol_table_add_subtable(universe, realdiskcommands);

//  features |= FE_realdisk;
	return vmOk;
}

static      vmResult
realdisk_enable(void)
{
//  if (!(features & FE_realdisk))
//      return vmOk;

	module_logger(&realDiskDSR, _L|L_0, _("setting up TI real-disk DSR ROM\n"));

	if (cruadddevice(CRU_WRITE, 0x1100, 1, cruwRealDiskROM) &&
		cruadddevice(CRU_WRITE, 0x1102, 1, cruwRealDiskMotor) &&
		cruadddevice(CRU_WRITE, 0x1104, 1, cruwRealDiskHold) &&
		cruadddevice(CRU_WRITE, 0x1106, 1, cruwRealDiskHeads) &&
		cruadddevice(CRU_WRITE, 0x1108, 1, cruwRealDiskSel) &&
		cruadddevice(CRU_WRITE, 0x110A, 1, cruwRealDiskSel) &&
		cruadddevice(CRU_WRITE, 0x110C, 1, cruwRealDiskSel) &&
		cruadddevice(CRU_WRITE, 0x110E, 1, cruwRealDiskSide) &&
		cruadddevice(CRU_READ, 0x1102, 1, crurRealDiskPoll) &&
		cruadddevice(CRU_READ, 0x1104, 1, crurRealDiskPoll) &&
		cruadddevice(CRU_READ, 0x1106, 1, crurRealDiskPoll)) {
//      features |= FE_realdisk;
		return vmOk;
	} else
		return vmNotAvailable;
}

static      vmResult
realdisk_disable(void)
{
//  if (!(features & FE_realdisk))
//      return vmOk;

	crudeldevice(CRU_WRITE, 0x1100, 1, cruwRealDiskROM);
	crudeldevice(CRU_WRITE, 0x1102, 1, cruwRealDiskMotor);
	crudeldevice(CRU_WRITE, 0x1104, 1, cruwRealDiskHold);
	crudeldevice(CRU_WRITE, 0x1106, 1, cruwRealDiskHeads);
	crudeldevice(CRU_WRITE, 0x1108, 1, cruwRealDiskSel);
	crudeldevice(CRU_WRITE, 0x110A, 1, cruwRealDiskSel);
	crudeldevice(CRU_WRITE, 0x110C, 1, cruwRealDiskSel);
	crudeldevice(CRU_WRITE, 0x110E, 1, cruwRealDiskSide);

	crudeldevice(CRU_READ, 0x1102, 1, crurRealDiskPoll);
	crudeldevice(CRU_READ, 0x1104, 1, crurRealDiskPoll);
	crudeldevice(CRU_READ, 0x1106, 1, crurRealDiskPoll);

	return vmOk;
}

static      vmResult
realdisk_restart(void)
{
	if (0 == data_load_dsr(romspath, realdiskfilename,
					 _("TI disk DSR ROM"), realdiskdsr)) 
		return vmNotAvailable;

#if EMU_DISK_DSR
	if (emuDiskDSR.flags & vmRTInUse)
		vmRestartModule(&emuDiskDSR);
#endif

	return vmOk;
}

static      vmResult
realdisk_restop(void)
{
	return vmOk;
}

static      vmResult
realdisk_term(void)
{
	return vmOk;
}

static vmDSRModule realDiskModule = {
	1,
	realdisk_getcrubase,
	realdisk_filehandler
};

vmModule    realDiskDSR = {
	3,
	"TI real-disk DSR",
	"dsrRealDisk",

	vmTypeDSR,
	vmFlagsNone,

	realdisk_detect,
	realdisk_init,
	realdisk_term,
	realdisk_enable,
	realdisk_disable,
	realdisk_restart,
	realdisk_restop,
	{(vmGenericModule *) & realDiskModule}
};
