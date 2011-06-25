/*
  dsr_pio.c					-- V9t9 module for PIO DSR

  (c) 1994-2005 Edward Swartz

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
#include <stddef.h>

#include "v9t9_common.h"
#include "v9t9.h"
#include "cru.h"
#include "command.h"
#include "9900.h"
#include "memory.h"
#include "dsr.h"
#include "timer.h"
#include "pab.h"

#include "dsr_emupio.h"

char        emupiofilename[OS_PATHSIZE] = { "printer_%03d.out" };
char        emupiodsrfilename[OS_PATHSIZE] = { "emupio.bin" };
u8	  		emupiodsr[8192];

#define _L	 LOG_PIO | LOG_INFO

enum
{
	D_PIO = 3	// c.f. emupio.dsr
};

/********************************************************************/

static void PIOSendByte(u16 crubase, u8 val);

static struct PIO
{
	int reading : 1;			// was CRU BIT 1 set?
	int handshakein : 1;		// CRU BIT 2
	int handshakeout : 1;		// CRU BIT 2
	int sparein : 1;			// CRU BIT 3
	int spareout : 1;			// CRU BIT 3
	int reflect : 1;			// CRU BIT 4

	u8 data;					// last transmitted byte
	int page;					// page # (starts after last existing on disk)
								// emupiofilename[] + page + ".out"
	OSSpec filespec;			// where data goes
	OSRef handle;				// if !=0, open handle for file
}	pio;

/*
 *	Set read/write status
 */
static      u32
PIO_1_w(u32 addr, u32 data, u32 num)
{
	pio.reading = data;
	pio.handshakein = 0;	// set ready right away
	return 0;
}

/*
 *	Handshake
 *
 */
static      u32
PIO_2_w(u32 addr, u32 data, u32 num)
{
	pio.handshakeout = data;
	pio.handshakein = 0;
	if (!data)
		PIOSendByte(addr & ~0xff, pio.data);
	pio.handshakein = 1;	// set acknowledged right away
	return 0;
}

/*
 *	Handshake
 *
 */
static      u32
PIO_2_r(u32 addr, u32 data, u32 num)
{
	return pio.handshakein;
}

/*
 *	Spare
 *
 */
static      u32
PIO_3_w(u32 addr, u32 data, u32 num)
{
	pio.spareout = data;
	return 0;
}

/*
 *	Spare
 *
 */
static      u32
PIO_3_r(u32 addr, u32 data, u32 num)
{
	return pio.sparein;
}

/*
 * 	Reflect
 *
 */
static      u32
PIO_4_w(u32 addr, u32 data, u32 num)
{
	pio.reflect = data;
	return 0;
}

/*
 *	Handshake
 *
 */
static      u32
PIO_4_r(u32 addr, u32 data, u32 num)
{
	return pio.reflect;
}

/*
 *	RS232 CTS bits
 *
 */
static      u32
PIO_5_6_w(u32 addr, u32 data, u32 num)
{
	return 0;
}

/*
 *	RS232 CTS bits
 *
 */
static      u32
PIO_5_6_r(u32 addr, u32 data, u32 num)
{
	return 0;
}

/*
 *	Card light (LED)
 *
 */
static      u32
PIO_7_w(u32 addr, u32 data, u32 num)
{
	report_status(STATUS_PIO_ACCESS, data);
	return 0;
}

/**************************************************/

//	High-level routines


/*
 *	Send a byte to the PIO 
 */
static void PIOSendByte(u16 crubase, u8 val)
{
	OSError err;
	if (!pio.handle)
	{
		if (strchr(emupiofilename, '%'))
		{
			char outname[OS_NAMESIZE];
			int cnt = 1000;
			while (cnt>0) {
				snprintf(outname, sizeof(outname), emupiofilename, pio.page);
				if (!data_find_file(datapath, outname, &pio.filespec))
					break;
				pio.page++;
				cnt--;
			}
			if (cnt == 0) {
				module_logger(&emuPIODSR, _L|LOG_ERROR|LOG_USER, _("could not find a spare page file for '%s' (page=%d)"),
							  emupiofilename, pio.page);
				return;
			}
			if (!data_create_file(datapath, outname, &pio.filespec, &OS_TEXTTYPE)) {
				module_logger(&emuPIODSR, _L|LOG_ERROR|LOG_USER, _("could not create page file '%s'"),
							  outname);
				return;
			}
		}
		else
		{
			if (!data_create_file(datapath, emupiofilename, &pio.filespec, &OS_TEXTTYPE)) {
				module_logger(&emuPIODSR, _L|LOG_ERROR|LOG_USER, _("could not create page file '%s'"),
							  emupiofilename);
				return;
			}
		}

		if ((err = OS_Open(&pio.filespec, OSReadWrite, &pio.handle)) != OS_NOERR)
		{
			module_logger(&emuPIODSR, _L|LOG_ERROR|LOG_USER, _("could not write file '%s' (%s)"),
						  OS_SpecToString1(&pio.filespec),
						  OS_GetErrText(err));
			return;	
		}
	}

	if (pio.handle)
	{
		OSSize size = 1;
		if ((err = OS_Write(pio.handle, (void *)&val, &size)) != OS_NOERR
			&& size != 1)
		{
			module_logger(&emuPIODSR, _L|LOG_ERROR, _("error writing to PIO (%s) (%s)"),
						  OS_SpecToString1(&pio.filespec),
						  OS_GetErrText(err));
		}
	}
	//module_logger(&emuPIODSR, _L | LOG_USER, "%c", val);
}


static void PIOCloseFile()
{
	if (pio.handle) {
		OS_Close(pio.handle);
		pio.handle = 0;
	}
}

static void
bwdrom_emupio(const mrstruct *mr, u32 addr, s8 val)
{
	pio.data = val;
}

/********************************************************************/

//  this is only used on the last area of the DSR ROM block */
mrstruct    dsr_rom_emupio_io_handler = {
	emupiodsr + 0x1000, NULL, NULL,
	NULL,
	NULL,
	NULL,
	bwdrom_emupio
};

mrstruct    dsr_rom_emupio_handler = {
	emupiodsr, emupiodsr, NULL,			/* ROM */
	NULL,
	NULL,
	NULL,
	NULL
};

static      u32
emuPIOROM_w(u32 addr, u32 data, u32 num)
{
	if (data) {
		dsr_set_active(&emuPIODSR);

		SET_AREA_HANDLER(0x4000, 0x1000, &dsr_rom_emupio_handler);
		SET_AREA_HANDLER(0x5000, 0x1000, &dsr_rom_emupio_io_handler);

	} else {
		report_status(STATUS_PIO_ACCESS, false);
		dsr_set_active(NULL);
		SET_AREA_HANDLER(0x4000, 0x2000, &zero_memory_handler);
	}
	return 0;
}

/**************************************/

/*	These PIOxxx routines handle file operations on PABs. */

/*	These bytes are mapped directly into scratch-pad RAM. */
typedef struct PioInfo
{
	u8 pab[10];			// >834A: copy of PAB
	u8 lenHi, lenLo;	// >8354: length of DSR name
	u8 namHi, namLo;	// >8356: ptr to filename in VDP
	u8 echoOff;			// >8358 (.EC)
	u8 noCRLF;			// >8359 (.CR)
	u8 noLF;			// >835A (.LF)
	u8 parity;			// >835B (.CH)
	u8 null;			// >835C (.NU)
	//u8 interrupts;		// >835D (>80 open)	 (RS232)

	// other stuff...
}	PioInfo;

static PioInfo *pioInfo = 0;

#define PAB_SET_ERROR(pab, error)	((pab)->pflags = (pab)->pflags & ~m_error | (error))
#define PAB_RESET_ERROR(pab)	((pab)->pflags = (pab)->pflags & ~m_error)

//	Parse flags in filename (i.e. PIO.CR.EC).
//	Return 0 for success.

static int PIOParse(pabrec* pab)
{
	int fnlen;
	u8 *fnend = pab->name + pab->namelen, 
		*fnstart = pab->name,
		*fn = fnstart;

	static struct { const char *ext; int offs; u8 value; }
	flags[] = 
		{
			".BA", -1, 0,
			".DA", -1, 0,
			".TW", -1, 0,
			".CH", offsetof(PioInfo, parity), 1,
			".PA=N", -1, 0,
			".PA=E", -1, 0,
			".PA=O", -1, 0,
			".CR", offsetof(PioInfo, noCRLF), 1,
			".LF", offsetof(PioInfo, noLF), 1,
			".NU", offsetof(PioInfo, null), 1,
			".EC", offsetof(PioInfo, echoOff), 1,
			0, 0, 0
		};

	if (!pioInfo)
		pioInfo = (PioInfo*)FLAT_MEMORY_PTR(md_cpu, 0x834A);

	// set flags from filename
	pioInfo->parity = 0;
	pioInfo->noCRLF = 0;
	pioInfo->noLF = 0;
	pioInfo->null = 0;
	pioInfo->echoOff = 0;

	fnlen = pab->namelen; //pioInfo->lenHi*256 + pioInfo->lenLo;
	fnend = fn + fnlen;
	while (fn < fnend) {
		// skip to '.'
		if (*fn == '.') {
			int idx;

			for (idx = 0; flags[idx].ext; idx++) {
				int flen = strlen(flags[idx].ext);
				if (fn + flen <= fnend 
					&& !strncasecmp(flags[idx].ext, fn, flen))
				{
					if (flags[idx].offs >= 0)
						*((u8*)pioInfo + flags[idx].offs) = flags[idx].value;
					fn += flen;
					break;
				}
			}
			if (!flags[idx].ext) {
				PAB_SET_ERROR(pab, e_badopenmode);
				return 1;
			}
		}
		else
			fn++;
	}
	return 0;
}

static void PIOSetHandshake(u16 crubase, int val)
{
	PIO_2_w(crubase + 2*2, val!=0, 1);
}

static void PIOSetLight(u16 crubase, int val)
{
	PIO_7_w(crubase + 7*2, val!=0, 1);
}

static void PIOWriteByte(u16 crubase, u8 val)
{
	PIO_1_w(crubase + 1*2, 0, 1);

	// wait to send
	while (PIO_2_r(crubase + 2*2, 0, 1) != 0)
		/* loop */;

	// set data
	pio.data = val;

	// flag data
	PIO_2_w(crubase + 2*2, 0, 1);

	// wait to finish
	while (PIO_2_r(crubase + 2*2, 0, 1) == 0)
		/* loop */;
}


static void
PIODSRClose(u8 dev, u16 crubase, u8 *fn);

static void
PIODSROpen(u8 dev, u16 crubase, u8 *fn)
{
	pabrec	*pab;

	int			ret;
	OSError     err;

	module_logger(&emuPIODSR, _L | L_1, "PIODSROpen\n");

	pab = (pabrec*)(fn-9);

	if (PIOParse(pab) == 0)
	{
		PIOSetHandshake(crubase, 1);

		if (pab->preclen == 0)
			pab->preclen = 80;

		if (pab->pflags & fp_relative) {
			PAB_SET_ERROR(pab, e_badopenmode);
			return;
		}

		pab->charcount = 0;
		PIOSetHandshake(crubase, 1);
		PIOSetLight(crubase, 0);

		PAB_RESET_ERROR(pab);
	}
}

static void
PIODSRClose(u8 dev, u16 crubase, u8 *fn)
{
	pabrec	*pab;

	module_logger(&emuPIODSR, _L | L_1, "PIODSRClose\n");

	pab = (pabrec*)(fn-9);

	if (PIOParse(pab) == 0) 
	{
		pab->charcount = 0;
		PAB_RESET_ERROR(pab);

		PIOSetHandshake(crubase, 1);
		PIOSetLight(crubase, 0);
	}

	PIOCloseFile();
}

static void
PIODSRRead(u8 dev, u16 crubase, u8 *fn)
{
	pabrec	*pab;

	module_logger(&emuPIODSR, _L | L_1, "PIODSRRead\n");

	pab = (pabrec*)(fn-9);

	if (PIOParse(pab) != 0)
		return;

	PAB_SET_ERROR(pab, e_illegal);	//!!! TODO
}

static void
PIODSRWrite(u8 dev, u16 crubase, u8 *fn)
{
	pabrec	*pab;
	int len;
	u8 *buffer;
	
	module_logger(&emuPIODSR, _L | L_1, "PIODSRWrite\n");

	pab = (pabrec*)(fn-9);

	if (PIOParse(pab) != 0)
		return;

	// send length if internal
	if (pab->pflags & fp_internal)
		PIOWriteByte(crubase, pab->charcount);
	else {
		// I think this is standard
		//if (pab->charcount > pab->preclen) {
		//	module_logger(&emuPIODSR, _L|L_0, "Shortening record %d to %d\n",
		//				  pab->charcount, pab->preclen);
		//	pab->charcount = pab->preclen;
		//}
	}

	// send data
	buffer = VDP_PTR(TI2HOST(pab->addr));
	for (len = 0; len < pab->charcount; len++)
		PIOWriteByte(crubase, *buffer++);

	// send end of record for DIS/VAR
	if (pab->pflags & fp_variable && !(pab->pflags & fp_internal))
	{
		if (!pioInfo->noCRLF) {
			printf("PIO: CR\n");
			PIOWriteByte(crubase, 0x0D);
		}
		if (pioInfo->null) {
			for (len = 0; len < 6; len++)
				PIOWriteByte(crubase, 0x0);
		}
		if (!pioInfo->noLF) {
			printf("PIO: LF\n");
			PIOWriteByte(crubase, 0x0A);
		}
	}
}


/************************************/


static      vmResult
emupio_getcrubase(u32 * base)
{
	*base = PIO_CRU_BASE;
	return vmOk;
}

static      vmResult
emupio_filehandler(u32 code)
{
	u16 rambase;

	module_logger(&emuPIODSR, _L | L_3, _("handling code %d\n\n"), code);

	/* get RAM base */
	rambase = wp - 0xe0;
	module_logger(&emuPIODSR, _L | L_2, "RAMbase=%04x\n", rambase);

	if (code == D_PIO)
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
			static void (*opcodehandlers[]) (u8 dev, u16 crubase, u8 *fn) = {
				PIODSROpen, PIODSRClose, PIODSRRead, PIODSRWrite,
				0 /*seek*/, 0 /*PIOLoad*/, 0 /*PIOSave*/, 0 /*delete*/,
				0 /*scratch*/, 0 /*status*/
			};

			if (opcodehandlers[opcode] == 0) {
				module_logger(&emuPIODSR, _L | L_2, _("unsupported operation %d on PIO/%d\n"), opcode, code);
				pab_set_vdp_error(fnptr, e_illegal);
			}
			else {
				module_logger(&emuPIODSR, _L | L_2, _("doing operation %d on PIO/%d\n"), opcode, code);
				opcodehandlers[opcode] (code, memory_read_word(rambase + 0xF8), fnptr);
			}
		}

		/*  return, indicating that the DSR handled the operation */
		register(11) += 2;
	}

	return vmOk;
}

static      vmResult
emupio_detect(void)
{
	return vmOk;
}

static      vmResult
emupio_init(void)
{
	command_symbol_table *emupiocommands =
		command_symbol_table_new(_("PIO DSR Options"),
								 _("These commands control the parallel port (PIO) emulation"),

		 command_symbol_new("PIO",
							_("Give local name for PIO output"),
							c_DONT_SAVE,
							NULL /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_string
							(_("filename"),
							 _("filename for printer pages under V9t9DataDir; use a printf-style specifier to number separate pages (e.g. 'page%03d.out')"),
							 NULL /* action */ ,
							 ARG_STR(emupiofilename),
							 NULL /* next */ )
							,

		   command_symbol_new("EmuPIODSRFileName",
							  _("Name of emulated PIO DSR ROM image which fits in the CPU address space >4000...>5FFF"),
							  c_STATIC,
							  NULL /* action */ ,
							  RET_FIRST_ARG,
							  command_arg_new_string
							  (_("file"),
							   _("name of binary image"),
							   NULL /* action */ ,
							   ARG_STR
							   (emupiodsrfilename),
							   NULL /* next */ )
							  ,

		  NULL /* next */ )),

		 NULL /* sub */ ,

		 NULL	/* next */
		);

	command_symbol_table_add_subtable(universe, emupiocommands);

	return vmOk;
}

static struct {
	int         bit;
	crufunc    *func;
} pio_writers[] = {
		1, PIO_1_w,
		2, PIO_2_w,
		3, PIO_3_w,
		4, PIO_4_w,
		5, PIO_5_6_w,
		6, PIO_5_6_w,
		7, PIO_7_w,
		-1, 0};

static struct {
	int         bit;
	crufunc    *func;
} pio_readers[] = {
	2, PIO_2_r,
	3, PIO_3_r,
	4, PIO_4_r,
	5, PIO_5_6_r,
	6, PIO_5_6_r,
	-1, 0};

static int
add_pio_device(int base)
{
	int         idx;

	for (idx = 0; pio_writers[idx].bit >= 0; idx++) {
		if (!cruadddevice(CRU_WRITE,
						  PIO_CRU_BASE + base + pio_writers[idx].bit * 2,
						  1, pio_writers[idx].func))
			return 0;
	}
	for (idx = 0; pio_readers[idx].bit >= 0; idx++) {
		if (!cruadddevice(CRU_READ,
						  PIO_CRU_BASE + base + pio_readers[idx].bit * 2,
						  1, pio_readers[idx].func))
			return 0;
	}
	return 1;
}

static int
del_pio_device(int base)
{
	int         idx;

	for (idx = 0; pio_writers[idx].bit >= 0; idx++) {
		if (!crudeldevice(CRU_WRITE,
						  PIO_CRU_BASE + base + pio_writers[idx].bit * 2,
						  1, pio_writers[idx].func))
			return 0;
	}
	for (idx = 0; pio_readers[idx].bit >= 0; idx++) {
		if (!crudeldevice(CRU_READ,
						  PIO_CRU_BASE + base + pio_readers[idx].bit * 2,
						  1, pio_readers[idx].func))
			return 0;
	}
	return 1;
}

static      vmResult
emupio_enable(void)
{
	vmResult    res;

	module_logger(&emuPIODSR, _L | L_1, _("setting up PIO DSR ROM\n"));

	if (cruadddevice(CRU_WRITE, PIO_CRU_BASE, 1, emuPIOROM_w) &&
		add_pio_device(0x0) ) {
		return vmOk;
	} else
		return vmNotAvailable;
}

static      vmResult
emupio_disable(void)
{
	PIOCloseFile();
	crudeldevice(CRU_WRITE, PIO_CRU_BASE, 1, emuPIOROM_w);
	del_pio_device(0x0);
	return vmOk;
}

static      vmResult
emupio_restart(void)
{
	if (0 == data_load_dsr(romspath, emupiodsrfilename,
					 _("PIO DSR ROM"), emupiodsr)) 
		return vmNotAvailable;
	else
		return vmOk;
}

static      vmResult
emupio_restop(void)
{
	return vmOk;
}

static      vmResult
emupio_term(void)
{
	PIOCloseFile();
	return vmOk;
}

static vmDSRModule emuPIOModule = {
	1,
	emupio_getcrubase,
	emupio_filehandler
};

vmModule    emuPIODSR = {
	3,
	"Emulated PIO DSR",
	"dsrEmuPIO",

	vmTypeDSR,
	vmFlagsNone,

	emupio_detect,
	emupio_init,
	emupio_term,
	emupio_enable,
	emupio_disable,
	emupio_restart,
	emupio_restop,
	{(vmGenericModule *) & emuPIOModule}
};
