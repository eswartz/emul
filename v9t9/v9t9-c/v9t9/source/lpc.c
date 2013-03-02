#include <stdio.h>
#include <unistd.h>
#include <math.h>
#include <string.h>
#ifndef STANDALONE
#include "v9t9_common.h"
#define _L	 LOG_SPEECH | LOG_INFO
#else
#include "v9t9_types.h"
#define _(x) x
//#define logger(flags, format, ...) fprintf(stderr, format, __VA_ARGS__)
#define logger(flags, format, ...) 
#define _L 0
#define log_level(x) 0
#endif

#define DUMP_DATS 1

#include "lpc.h"
#include "tms5220r.c"
//#include "tms5220.h"
/*
lpc.c

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
struct	LPC
{
	int		rpt;				/* repeat */
	int		pnv,env;			/* pitch, energy new value */
	int		pbf,ebf;			/* pitch, energy buffer */
	int		knv[12],kbf[12];	/* K interp values, new values, old values */

	u8		decode;			/* speech flags */
#define FL_unvoiced 1		/* unvoiced? */
#define FL_nointerp 2		/* no interpolation */
#define FL_first	4		/* first frame? */
#define FL_last		8		/* stop frame seen */

	int		b[12],y[12];	/* lattice filter */
	u32		ns1,ns2;		/* unvoiced hiss registers */
	int		ppctr;			/* pitch counter */
};



static struct LPC lpc;		/* LPC info */


void
LPCinit(void)
{
	memset((void *) &lpc, 0, sizeof(lpc));
	lpc.decode |= FL_first;
	/*  Reset excitation filters  */
	lpc.ns1 = 0xaaaaaaaa;
	lpc.ns2 = 0x1;
}


void
LPCstop(void)
{

}


/***************************************************************
	This section handles decoding one LPC equation into the 'lpc'
	structure.
****************************************************************/

#define ONE (32768>>6)

//#define KTRANS(x) ((((x) & 0x8000) ? (x) ^ 0x7fff : (x)) >> 6)
#define KTRANS(x) ((x) >> 6)
#define ZERO(x)	   0

static void
LPCclearToSilence(void)
{
	lpc.pnv = 12;
	lpc.env = 0;
	memset(lpc.knv, 0, sizeof(lpc.knv));

	lpc.knv[0] = ZERO(KTRANS(k1table[0]));
	lpc.knv[1] = ZERO(KTRANS(k2table[0]));
	lpc.knv[2] = ZERO(KTRANS(k3table[0]));
	lpc.knv[3] = ZERO(KTRANS(k4table[0]));
	lpc.knv[4] = ZERO(KTRANS(k5table[0]));
	lpc.knv[5] = ZERO(KTRANS(k6table[0]));
	lpc.knv[6] = ZERO(KTRANS(k7table[0]));
	lpc.knv[7] = ZERO(KTRANS(k8table[0]));
	lpc.knv[8] = ZERO(KTRANS(k9table[0]));
	lpc.knv[9] = ZERO(KTRANS(k10table[0]));
	
	// if the previous frame was unvoiced,
	// it would sound bad to interpolate.
	// just clear it all out.
	if (lpc.decode & FL_unvoiced) {
		lpc.pbf = 12;
		lpc.ebf = 0;
		memset(lpc.kbf, 0, sizeof(lpc.kbf));

		lpc.kbf[0] = ZERO(KTRANS(k1table[0]));
		lpc.kbf[1] = ZERO(KTRANS(k2table[0]));
		lpc.kbf[2] = ZERO(KTRANS(k3table[0]));
		lpc.kbf[3] = ZERO(KTRANS(k4table[0]));
		lpc.kbf[4] = ZERO(KTRANS(k5table[0]));
		lpc.kbf[5] = ZERO(KTRANS(k6table[0]));
		lpc.kbf[6] = ZERO(KTRANS(k7table[0]));
		lpc.kbf[7] = ZERO(KTRANS(k8table[0]));
		lpc.kbf[8] = ZERO(KTRANS(k9table[0]));
		lpc.kbf[9] = ZERO(KTRANS(k10table[0]));
		
		lpc.decode &= ~FL_unvoiced;
	}
}

/**
 *	Read an equation from the bit source.
 */
void
LPCreadEquation(u32 (*LPCfetch)(int), int forceUnvoiced)
{
	char        txt[256], *tptr = txt;

	/* 	Copy now-old 'new' values into 'buffer' values */
	lpc.ebf = lpc.env;
	lpc.pbf = lpc.pnv;
	memcpy(lpc.kbf, lpc.knv, sizeof(lpc.kbf));

	/*  Read energy  */
	lpc.env = LPCfetch(4);
	tptr += sprintf(tptr, "E: %d ", lpc.env);
	if (lpc.env == 15) {
		lpc.decode |= FL_last;
		LPCclearToSilence();	/* clear params */
	} else if (lpc.env == 0) {	/* silent frame */
		if (lpc.decode & FL_unvoiced)	/* unvoiced before? */
			lpc.decode |= FL_nointerp;
		else
			lpc.decode &= ~FL_nointerp;
		LPCclearToSilence();	/* clear params */
	} else {
		/*  Repeat bit  */
		lpc.rpt = LPCfetch(1);
		tptr += sprintf(tptr, "R: %d ", lpc.rpt);

		/*  Pitch code  */
		lpc.pnv = LPCfetch(6);
		tptr += sprintf(tptr, "P: %d ", lpc.pnv);

		if (lpc.pnv == 0) {		/* unvoiced */
			if (lpc.decode & FL_unvoiced)	/* voiced before? */
				lpc.decode |= FL_nointerp;	/* don't interpolate */
			else
				lpc.decode &= ~FL_nointerp;
			lpc.decode |= FL_unvoiced;
			lpc.pnv = 12;		/* set some pitch */

			if (lpc.ebf == 0)	/* previous frame silent? */
				lpc.decode |= FL_nointerp;
		} else {				/* voiced */

			lpc.pnv = pitchtable[lpc.pnv] >> 8;

			if (lpc.decode & FL_unvoiced)	/* unvoiced before? */
				lpc.decode |= FL_nointerp;	/* don't interpolate */
			else
				lpc.decode &= ~FL_nointerp;

			lpc.decode &= ~FL_unvoiced;
		}

		/* translate energy */
		//lpc.env = KTRANS(energytable[lpc.env]);
		lpc.env = energytable[lpc.env] >> 6;

		/*  Get K parameters  */

		if (!lpc.rpt) {			/* don't repeat previous frame? */
			u32         tmp;

			tmp = LPCfetch(5);
			lpc.knv[0] = KTRANS(k1table[tmp]);
			tptr += sprintf(tptr, "K0: %d ", tmp);

			tmp = LPCfetch(5);
			lpc.knv[1] = KTRANS(k2table[tmp]);
			tptr += sprintf(tptr, "K1: %d ", tmp);

			tmp = LPCfetch(4);
			lpc.knv[2] = KTRANS(k3table[tmp]);
			tptr += sprintf(tptr, "K2: %d ", tmp);

			tmp = LPCfetch(4);
			// bug in pre-TMS5220, according to MAME
			//lpc.knv[3] = KTRANS(k4table[tmp]);
			lpc.knv[3] = KTRANS(k3table[tmp]);
			tptr += sprintf(tptr, "K3: %d ", tmp);

			if (!(lpc.decode & FL_unvoiced)) {	/* unvoiced? */
				tmp = LPCfetch(4);
				lpc.knv[4] = KTRANS(k5table[tmp]);
				tptr += sprintf(tptr, "K4: %d ", tmp);

				tmp = LPCfetch(4);
				lpc.knv[5] = KTRANS(k6table[tmp]);
				tptr += sprintf(tptr, "K5: %d ", tmp);

				tmp = LPCfetch(4);
				lpc.knv[6] = KTRANS(k7table[tmp]);
				tptr += sprintf(tptr, "K6: %d ", tmp);

				tmp = LPCfetch(3);
				lpc.knv[7] = KTRANS(k8table[tmp]);
				tptr += sprintf(tptr, "K7: %d ", tmp);

				tmp = LPCfetch(3);
				lpc.knv[8] = KTRANS(k9table[tmp]);

				tptr += sprintf(tptr, "K8: %d ", tmp);

				tmp = LPCfetch(3);
				lpc.knv[9] = KTRANS(k10table[tmp]);
				tptr += sprintf(tptr, "K9: %d ", tmp);
			} else {
				lpc.knv[4] = ZERO(KTRANS(k5table[0]));
				lpc.knv[5] = ZERO(KTRANS(k6table[0]));
				lpc.knv[6] = ZERO(KTRANS(k7table[0]));
				lpc.knv[7] = ZERO(KTRANS(k8table[0]));
				lpc.knv[8] = ZERO(KTRANS(k9table[0]));
				lpc.knv[9] = ZERO(KTRANS(k10table[0]));
			}
		}
	}

	if (forceUnvoiced) {
		lpc.decode |= FL_unvoiced;
		lpc.knv[4] = ZERO(KTRANS(k5table[0]));
		lpc.knv[5] = ZERO(KTRANS(k6table[0]));
		lpc.knv[6] = ZERO(KTRANS(k7table[0]));
		lpc.knv[7] = ZERO(KTRANS(k8table[0]));
		lpc.knv[8] = ZERO(KTRANS(k9table[0]));
		lpc.knv[9] = ZERO(KTRANS(k10table[0]));
	}

	logger(_L | L_1, _("Equation: %s\n"), txt);

	logger(_L | L_1, _("ebf=%d, pbf=%d, env=%d, pnv=%d\n"),
		   lpc.ebf, lpc.pbf, lpc.env, lpc.pnv);
}

/*******************************************************************
	This section handles the 'execution' of an LPC equation.
********************************************************************/

/*
	Interpolate "new" values and "buffer" values.
*/
static void
LPCinterpolate(int period)
{
	int         x;

	if (!(lpc.decode & FL_nointerp)) {
		lpc.ebf += (lpc.env - lpc.ebf) / interp_coeff[period];
		if (lpc.pbf != 0)
			lpc.pbf += (lpc.pnv - lpc.pbf) / interp_coeff[period];
		for (x = 0; x < 11; x++)
			lpc.kbf[x] += (lpc.knv[x] - lpc.kbf[x]) / interp_coeff[period];
	} else {
	}

	//logger(_L|L_1, "[%d] ebf=%d, pbf=%d\n", period, lpc.ebf, lpc.pbf);
}


// this is safe since our values are ~14 bits
#define MD(a,b) (((a)*(b))/ONE)
#define B(i) lpc.b[i]
#define Y(i) lpc.y[i]
#define K(i) lpc.kbf[i]
//#define U Y(10)

#define LPC_TO_PCM(s) LPCtoPCM(s)

static int LPCtoPCMinitialized;
static int lpctopcm[1024];

static int LPCtoPCM(int val) 
{
	if (val < -512)
		return -0x8000;
	if (val > 511)
		return 0x7fff;

	return val << 6;

///
	if (!LPCtoPCMinitialized) {
		double mu = 255.;
		double logOnePlusMu = log(1+mu);
		int vv;
		for (vv = -512; vv < 512; vv++) {
			double v = (fabs(vv) / 512.0);
			//lpctopcm[vv+512] = (vv<0?-1:1)*(pow(1+mu, v)-1);
			lpctopcm[vv+512] = ((vv<0?-1:1)*127.0*log(1+mu*v)/logOnePlusMu) / 4;
		}
		LPCtoPCMinitialized = 1;
	}

	return lpctopcm[val+512];
}

/*
 *	Generate PCM data for one LPC frame.
 *
 */
static void
LPCcalc(s16 *data, u32 length)
{
	int         frame, framesize, bytes;
	int			stage;
	s16         *ptr;
#if DUMP_DATS
	FILE		*f = 0L;
#endif
	int			U;

	ptr = data;
	if (!ptr) 
		return;

#if DUMP_DATS
	if (log_level(_L) >= 3)
	{
		f = fopen("lpcdats.txt", "at");
		if (f)
		{
			int i;
			fprintf(f,"K: ");
			for (i = 0; i < 10; i++)
				fprintf(f, "%d|%d ", lpc.knv[i], K(i));
			fprintf(f,"\n");
		}
	}
#endif

	/* excitation data */
	U = 0;

	bytes = length;
	frame = 0;
	framesize = (length + 7) / 8;

	while (bytes--) {
		s32         samp;

		/* interpolate parameters? */
		if (framesize && (bytes % framesize) == 0) {
			LPCinterpolate(frame);
			frame++;
		}

		/*  Update excitation data in U? */
		if ((lpc.decode & FL_unvoiced)) {
			U = (lpc.ns1 & 1) ? lpc.ebf / 4 : -lpc.ebf / 4;

			/* noise generator */
			lpc.ns1 = (lpc.ns1 << 1) | (lpc.ns1 >> 31);
			lpc.ns1 ^= lpc.ns2;
			if ((lpc.ns2 += lpc.ns1) == 0)
				lpc.ns2++;
		} else {
			/* get next chirp value */
			int cptr = lpc.ppctr * 200 / length;
			U = cptr < sizeof(chirptable)/sizeof(chirptable[0]) ? chirptable[cptr] : 0;
			U = (U * lpc.ebf + 128) / 256;

			if (lpc.pbf) 
				lpc.ppctr = (lpc.ppctr + 1) % lpc.pbf;
			else	
				lpc.ppctr = 0;

		}

		/*  -----------------------------------------
			   10-stage lattice filter.

			   range 1..10 here, 0..9 in our arrays

			   Y10(i) = U(i) - K10*B10(i-1) U(i)=excitation
			   ----
			   Y9(i) = Y10(i) - K9*B9(i-1)
			   B10(i)= B9(i-1) + K9*Y9(i)
			   ----
			   ...
			   Y1(i) = Y2(i) - K1*B1(i-1)
			   B2(i) = B1(i-1) + K1*Y1(i)
			   ----
			   B1(i) = Y1(i)
			   ----------------------------------------- */

			/*  Stage 10 is different than the others.
			   Instead of calculating B11, we scale the excitation by
			   the energy.

			 */

#if DUMP_DATS
		if (f)
		{
			fprintf(f, "%d / ", U);
		}
#endif

		Y(10) = U;
		for (stage = 9; stage >= 0; stage--) {
			Y(stage) = Y(stage + 1) - MD(K(stage), B(stage));
		}
		for (stage = 9; stage >= 1; stage--) {
			B(stage) = B(stage - 1) + MD(K(stage - 1), Y(stage - 1));
		}

		samp = Y(0);
		B(0) = samp;

		//if (samp > 511 || samp < -512)
		//	logger(LOG_USER,"samp[%d]=%d\n", ptr-speech_data, samp);

#if DUMP_DATS
		if (f)
		{
			fprintf(f, "%d\n", samp);
		}
#endif

		*ptr++ = LPC_TO_PCM(samp);
	}

#if DUMP_DATS
	if (f)
	{
		fclose(f);
	}
#endif
}

/*
 *	Setup and generate PCM data for one LPC frame
 */
void
LPCexec(s16 *data, u32 length)
{
	if (lpc.decode & (FL_nointerp | FL_first))
		lpc.decode &= ~FL_first;

	lpc.ppctr = 0;

	memset(lpc.y, 0, sizeof(lpc.y));
	memset(lpc.b, 0, sizeof(lpc.b));

	LPCcalc(data, length);
}

/*	
	One LPC frame consists of decoding one equation (or repeating,
	or stopping), and calculating a speech waveform and outputting it.
	
	This happens during an interrupt.
	
	If we're here, we have enough data to form any one equation.
	@return 1 to continue, 0 if end of frame
*/
int
LPCframe(u32 (*LPCfetch)(int count), s8 *data, u32 length)
{
	if ((lpc.decode & FL_last) == 0) {
		LPCreadEquation(LPCfetch, 0);
		LPCexec(data, length);
		return (lpc.decode & FL_last) == 0;	/* not last frame */
	}
	else
		return 0;
}

int 
LPCavailable() 
{
	return (lpc.decode & FL_last) == 0;
}

extern void* LPCallocState() 
{
	return (void*)xmalloc(sizeof(lpc));
}
extern void LPCgetState(void *data) 
{
	memcpy(data, &lpc, sizeof(lpc));
}
extern void LPCsetState(void *data)
{
	memcpy(&lpc, data, sizeof(lpc));
}
extern void LPCfreeState(void *data) 
{
	xfree(data);
}
extern int LPCstateSize() 
{
	return sizeof(lpc);
}
