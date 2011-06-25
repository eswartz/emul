
/*
  speech.c						-- TMS5220 speech synthesizer emulation

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

#define __SPEECH__

#define DUMP_DATS 1

#include <unistd.h>
#include "v9t9_common.h"
#include "v9t9.h"
#include "sound.h"
#include "memory.h"
#include "timer.h"
#include "demo.h"
#include "speech.h"
#include "lpc.h"

#define _L	 LOG_SPEECH | LOG_INFO

#ifndef STANDALONE
char	speechromfilename[64]="spchrom.bin";
u8         speechrom[65536];
static struct tms5200 sp;
#endif

static int speech_hertz = 8000;
static int speech_length = 200;
static s16 *speech_data;


/****************************************************/

static int  speech_event_tag;
static int  in_speech_intr = 0;	// mutex on speech_intr

#define SPEECH_TIMEOUT (25+9)

/****************************************************/


static u8   swapped_nybbles[16] = 
{ 
	0x0, 0x8, 0x4, 0xc,
	0x2, 0xa, 0x6, 0xe,
	0x1, 0x9, 0x5, 0xd,
	0x3, 0xb, 0x7, 0xf
};

static      u8
swapbits(u8 in)
{
	return (swapped_nybbles[in & 0xf] << 4) |
		(swapped_nybbles[(in & 0xf0) >> 4]);
}

/*
// "neat" but slower way
static u8
swapbits(u8 in)
{
	in = ((in >> 1) & 0x55) | ((in << 1) & 0xaa);
	in = ((in >> 2) & 0x33) | ((in << 2) & 0xcc);
	in = ((in >> 4) & 0x0f) | ((in << 4) & 0xf0);
	return in;
}
*/

/******************************************************/

#ifndef STANDALONE
static void
SpeechOn(void)
{
	int hz;

	if (!speech_event_tag)
		speech_event_tag = TM_UniqueTag();

	hz = speech_hertz / speech_length;
	TM_SetEvent(speech_event_tag, TM_HZ * 100 / (hz ? hz : 1), 0,
				TM_REPEAT | TM_FUNC, speech_intr);
}

static void
SpeechOff(void)
{
	TM_ResetEvent(speech_event_tag);
}
#endif

static void
tms5200speakExternal(void);

void speech_demo_init(void)
{
	LPCinit();
	tms5200speakExternal();
}


void speech_demo_stop(void)
{
	logger(_L | L_1, _("Done with speech phrase\n"));
	SpeechOff();			/* stop interrupting */
	demo_record_event(demo_type_speech, demo_speech_stopping);
	sp.status &= ~(SS_TS | SS_SPEAKING);
	sp.gate = (sp.gate & ~GT_WDAT) | GT_WCMD;
}

static void speech_wait_complete(int seconds)
{
	int ret;
	int tm;

	// wait for existing sample to finish
	tm = TM_GetTicks();
	while (TM_GetTicks() < tm + seconds * TM_HZ)
	{
		SPEECHPLAYING(ret, vms_Speech);
		if (!ret) break;
	}

	// allow other threads to run for a while
	// in case we're speaking many words in a row.
	stateflag |= ST_STOP;
}

static void 
tms5200purgeFIFO(void)
{
	sp.bit = sp.bits = sp.out = sp.in = sp.len = 0;
}

static void
tms5200reset(void)
{
	logger(_L | L_1, _("Speech reset\n"));
	sp.status = SS_BE | SS_BL;
	tms5200purgeFIFO();
	sp.command = 0x70;
	sp.data = 0;
	sp.addr = 0; sp.addr_pos = 0;
	sp.gate = GT_RSTAT | GT_WCMD;
	SpeechOff();
	sp.status &= ~SS_SPEAKING;
	sp.timeout = SPEECH_TIMEOUT;
	LPCinit();
	in_speech_intr = 0;

	// flush existing sample
	SPEECHPLAY(vms_Speech, NULL, 0L, speech_hertz);
}

void speech_demo_term(void)
{
	tms5200reset();
}

static      u8
tms5200read(void)
{
	sp.addr_pos = 0;
	if (sp.addr >= 32768)
		sp.data = 0;
	else
		sp.data = speechrom[sp.addr];
	sp.addr++;
	sp.gate = (sp.gate & ~GT_RSTAT) | GT_RDAT;
	logger(_L | L_2, _("Speech read: %02X\n\n"), sp.data);
	return sp.data;
}

static      u8
tms5200peek(void)
{
	sp.addr_pos = 0;
	if (sp.addr >= 32768)
		sp.data = 0;
	else
		sp.data = speechrom[sp.addr];
	sp.gate = (sp.gate & ~GT_RSTAT) | GT_RDAT;
	return sp.data;
}

static void
tms5200loadAddress(u32 nybble)
{
	sp.addr_pos = (sp.addr_pos + 1) % 5;
	sp.addr = (sp.addr >> 4) | (nybble << 16);
	logger(_L | L_2, _("Speech addr = %04X\n"), sp.addr);
}

static void
tms5200readAndBranch(void)
{
	u32         addr;

	sp.addr_pos = 0;
	addr = (tms5200read() << 8) + tms5200read();
	sp.addr = (sp.addr & 0xc000) | (addr & 0x3fff);
	sp.gate = (sp.gate & ~GT_RDAT) | GT_RSTAT;
}

static void
tms5200speak(void)
{
	logger(_L | L_1, _("Speaking phrase at %04X\n"), sp.addr);
	demo_record_event(demo_type_speech, demo_speech_starting);

	// wait for previous sample to end, or else we
	// can end up stacking tons of digitized data
	speech_wait_complete(1);

//	SPEECHPLAY(vms_Speech, NULL, 0L, speech_hertz);

	sp.gate = (sp.gate & ~GT_WDAT) | GT_WCMD;	/* just in case */
	sp.bit = 0;					/* start on byte boundary */
	sp.status |= SS_SPEAKING | SS_TS;

	while (sp.status & SS_SPEAKING)
		speech_intr();			// not scheduled

	LPCinit();
}

static void
tms5200speakExternal(void)
{
	logger(_L | L_1, _("Speaking external data\n"));
	demo_record_event(demo_type_speech, demo_speech_starting);

	sp.gate = (sp.gate & ~GT_WCMD) | GT_WDAT;	/* accept data from I/O */
	tms5200purgeFIFO();
	SpeechOn();					/* call speech_intr every 25 ms */
	sp.status |= SS_SPEAKING;
	sp.timeout = SPEECH_TIMEOUT;
}

/*	Undocumented and unknown function... */
static void
tms5200loadFrameRate(u8 val)
{
	if (val & 0x4) {
		/* variable */
	} else {
		/* frameRate = val & 0x3; */
	}
}

static void
tms5200command(u8 cmd)
{
	sp.command = cmd & 0x70;
	logger(_L | L_3, _("Cmd=%02X  Status: %02X\n\n"), cmd, sp.status);
	switch (sp.command) {
	case 0x00:
	case 0x20:
		tms5200loadFrameRate(cmd & 0x7);
		break;
	case 0x10:
		tms5200read();
		break;
	case 0x30:
		tms5200readAndBranch();
		break;
	case 0x40:
		tms5200loadAddress(cmd & 0xf);
		break;
	case 0x50:
		tms5200speak();
		break;
	case 0x60:
		tms5200speakExternal();
		break;
	case 0x70:
		tms5200reset();
		break;
		/* default: ignore */
	}
}

static void
tms5200writeFIFO(u8 val)
{
	sp.fifo[sp.in] = swapbits(val);
	sp.in = (sp.in + 1) & 15;
	logger(_L | L_3, _("FIFO write: %02X  len=%d\n"), val, sp.len);
	if (sp.len < 16)
		sp.len++;
	sp.timeout = SPEECH_TIMEOUT;
	sp.status &= ~SS_BE;
	if (sp.len > 8) {
		sp.status &= ~SS_BL;
		speech_intr();
	}
}

static      u8
tms5200readFIFO(void)
{
	u8          ret = sp.fifo[sp.out];

	logger(_L | L_3, _("FIFO read: %02X  len=%d\n"), ret, sp.len);

	if (sp.len == 0) {
		sp.status |= SS_BE;
		sp.status &= ~SS_TS;
		tms5200reset();
		SpeechOff();
		logger(_L | L_1, _("Speech timed out\n"));
	}

	if (sp.len > 0) {
		sp.out = (sp.out + 1) & 15;
		sp.len--;
	}
	if (sp.len < 8)
		sp.status |= SS_BL;
	if (sp.len == 0) {
		sp.status |= SS_BE;
		sp.status &= ~SS_TS;
	}
	return ret;
}

static      u8
tms5200peekFIFO(void)
{
	return sp.fifo[sp.out];
}


/*
	Fetch so many bits.
	
	This differs if we're reading from vocabulary or FIFO, in terms
	of where a byte comes from, but that's all.
	
	When reading from the FIFO, we only execute ...readFIFO when we
	are finished with the byte.
*/
static      u32
LPCfetch(int bits)
{
	u32         cur;

	if (sp.gate & GT_WDAT) {	/* from FIFO */
		if (sp.bit + bits >= 8) {	/* we will cross into the next byte */
			cur = tms5200readFIFO();
			demo_record_event(demo_type_speech, demo_speech_adding_byte, cur);

			cur <<= 8;
			cur |= tms5200peekFIFO();	/* we can't read more than 6 bits,
										   so no poss of crossing TWO bytes */
		} else
			cur = tms5200peekFIFO() << 8;
	} else {					/* from vocab */

		if (sp.bit + bits >= 8) {
			cur = tms5200read();
			demo_record_event(demo_type_speech, demo_speech_adding_byte, cur);

			cur <<= 8;
			cur |= tms5200peek();
		} else
			cur = tms5200peek() << 8;
	}

	/*  Get the bits we want.  */
	cur = (cur << (sp.bit + 16)) >> (32 - bits);

	/*  Adjust bit ptr  */
	sp.bit = (sp.bit + bits) & 7;

	sp.bits += bits;
	return cur;
}


INLINE int speech_sample_playing(void)
{
	int playing;

	SPEECHPLAYING(playing, vms_Speech);
	return playing;
}

void
speech_demo_push(u8 val)
{
	tms5200writeFIFO(swapbits(val));
}

void
speech_intr(void)
{
	int do_frame = 0;
	if (in_speech_intr)
		return;
	in_speech_intr = 1;

	logger(_L | L_2, _("Speech Interrupt\n"));

	if (sp.gate & GT_WDAT) {	/* direct data */
		if (!(sp.status & SS_TS)) {	/* not talking yet... we're waiting for */
			if (!(sp.status & SS_BL)) {	/* enough data in the buffer? */
				sp.status |= SS_TS;		/* whee!  Start talking */
				do_frame = 1;
			}
		}
		else {
			if (sp.status & SS_BL) {
				if (sp.timeout-- <= 0) {
					speech_wait_complete(1);

					tms5200reset();
					demo_record_event(demo_type_speech, demo_speech_terminating);

					// this apparently happens in normal cases
					logger(_L|L_1, "Speech timeout\n");
				}
			}
			else
				do_frame = 1;
		}
	}
	else {		/* vocab data */
		if (sp.status & SS_TS)
			do_frame = 1;
	}

	if (do_frame) {
		sp.bits = 0;

		int last = !LPCframe(LPCfetch, speech_data, speech_length);
		/* spit it out */
		if (speech_data)
			SPEECHPLAY(vms_Speech, speech_data, speech_length, speech_hertz);	
		if (last) {
			//if (sp.status & SS_SPEAKING) {	/* didn't get empty condition */

			speech_demo_stop();
		}
	}


  out:
	logger(_L | L_4, _("Out of speech interrupt\n"));
	in_speech_intr = 0;
}


/********************/

u16
speech_mmio_get_addr(void)
{
	return sp.addr;
}

void
speech_mmio_set_addr(u16 addr)
{
	sp.addr = addr;
}

bool
speech_mmio_addr_is_complete(void)
{
	return sp.addr_pos == 0 || sp.addr_pos == 5;
}

void
speech_mmio_write(u8 val)
{
	if (sp.gate & GT_WCMD)
		tms5200command(val);
	else
		tms5200writeFIFO(val);
}

s8 speech_mmio_read(void)
{
	if (sp.gate & GT_RSTAT) {
		u8 stat = sp.status | (speech_sample_playing() ? SS_TS : 0);
		return stat;
	} else {
		sp.gate = (sp.gate & ~GT_RDAT) | GT_RSTAT;
		return sp.data;
	}
}

mrstruct speech_handler =
{
	speechrom, speechrom, 0L,		// cannot write
	NULL, NULL,
	NULL, NULL
};

void
speech_memory_init(void)
{
	memory_insert_new_entry(MEMENT_SPEECH, 0x0000, 0x10000, 
						   _("Speech ROM"),
							//	0L /*filename*/, 0L /*fileoffs*/, 
							speechromfilename, 0 /*fileoffs*/,
							&speech_handler);

}

/************************/

static void
speech_initLPC(void)
{
	if (!(features & FE_SPEECH)) {
		features &= ~(FE_PLAYSPEECH | FE_SPEECH);
		logger(_L | LOG_USER, _("Sound module cannot play speech, speech disabled.\n\n"));
	} else
		logger(_L | L_0, _("Setting up LPC speech...\n\n"));
}


/***********************************************/

static
DECL_SYMBOL_ACTION(speech_set_sample_length)
{
	if (task == csa_WRITE) {
		xfree(speech_data);
		if (speech_length <= 0) speech_length = 1;
		speech_data = (s16*) xmalloc(speech_length * sizeof(s16));
		if (sp.status & SS_SPEAKING) {
			SpeechOff();
			SpeechOn();
		}
	}
	return 1;
}

static 
DECL_SYMBOL_ACTION(update_speech_hertz)
{
	if (task == csa_WRITE) {
		SpeechOff();
		SpeechOn();
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(speech_define_speech_rom)
{
	char cmdbuf[1024];
	char *fname;

	if (task == csa_READ) {
		command_arg_get_string(SYM_ARG_1st, &fname);
		if (!fname || !*fname)
			return 0;
		else 
			return (iter == 0);
	}

	command_arg_get_string(SYM_ARG_1st, &fname);
	if (!*fname)
		fname = "spchrom.bin";
	sprintf(cmdbuf, "DefineMemory \"RS\" 0x0000 -0x10000 \"%s\" 0x0 \"Speech ROM\"\n", fname);
	return command_exec_text(cmdbuf);
}

int
speech_preconfiginit(void)
{
	command_symbol_table *speechcommands =
		command_symbol_table_new(_("Speech Options"),
								 _("These are commands for controlling speech synthesis"),

		 command_symbol_new("PlaySpeech",
							_("Control whether speech is played"),
							c_STATIC,
							NULL /*action*/,
							RET_FIRST_ARG,
							command_arg_new_toggle
							("off|on",
							 _("toggle speech on or off"),
							 NULL /* action */ ,
							 ARG_NUM(features),
							 FE_PLAYSPEECH,
							 NULL /* next */ )
							,

		 command_symbol_new("SpeechROMFileName",
							_("Name of speech ROM"),
							c_DYNAMIC,
							speech_define_speech_rom /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_string
							(_("file"),
							 _("name of binary image"),
							 NULL /* action */,
							 NEW_ARG_NEW_STRBUF,
							 NULL /* next */ )
							,

		 command_symbol_new("SpeechHertz",
							_("Set sample rate for speech"),
							c_STATIC,
							update_speech_hertz /*action*/,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("hertz"),
							 _("normal value is 8000"),
							 NULL /* action */ ,
							 ARG_NUM(speech_hertz),
							 NULL /* next */ )
							,

		 command_symbol_new("SpeechSampleLength",
							_("Set sample length for a unit of speech"),
							c_STATIC,
							speech_set_sample_length,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("length"),
							 _("in bytes, normal value is 200"),
							 NULL /* action */ ,
							 ARG_NUM(speech_length),
							 NULL /* next */ )
							,

		NULL /*next*/)))),
		NULL /*sub*/,
		NULL /*next*/
	);								 

	command_symbol_table_add_subtable(universe, speechcommands);
	speech_length = 200;
	speech_data = (s16*) xmalloc(speech_length * sizeof(s16));
	LPCinit();
	features |= FE_SPEECH|FE_PLAYSPEECH;
	return 1;
}

int
speech_postconfiginit(void)
{
/*	if (!loadspeech(romspath, systemromspath, 
					speechromfilename, speechrom)) {
	      features &= ~FE_PLAYSPEECH;
	}
*/
//	speech_memory_init();
	speech_initLPC();
	tms5200reset();
	return 1;
}

int
speech_restart(void)
{
	speech_memory_init();
	return 1;
}

void
speech_restop(void)
{
}

void
speech_shutdown(void)
{
	xfree(speech_data);
}

/*
 *	The least possible work we can do to enable
 *	saving and loading speech -- hardcoded binary data!
 */
DECL_SYMBOL_ACTION(speech_machine_state)
{
	char *str;
	if (task == csa_READ) {
		char tmp[2*(sizeof(sp)+LPCstateSize())+1];
		void *lpc;
		if (iter)
			return 0;
		emulate_bin2hex((u8 *)&sp, tmp, sizeof(sp));
		command_arg_set_string(SYM_ARG_1st, tmp);
		lpc = LPCallocState();
		LPCgetState(lpc);
		emulate_bin2hex(lpc, tmp, LPCstateSize());
		command_arg_set_string(SYM_ARG_2nd, tmp);
		LPCfreeState(lpc);
		return 1;

	}

	if (command_arg_get_string(SYM_ARG_1st, &str))
		emulate_hex2bin(str, (u8 *)&sp, sizeof(sp));

	//if (command_arg_get_string(SYM_ARG_2nd, &str))
	//	emulate_hex2bin(str, (u8 *)&lpc, sizeof(lpc));

	// we have to restart speech if necessary,
	// since it's nearly impossible to reset the speech
	// synthesizer if it was in direct mode without
	// setting the timeout function.
	in_speech_intr = 0;
	if (sp.timeout && (sp.gate & GT_WDAT)) {
		SpeechOn();
	} else {
		sp.timeout = 0;
	}

	// fix bug
	if (sp.status & SS_BE)
		sp.status &= ~SS_TS;

	return 1;
}

/*************************************/
