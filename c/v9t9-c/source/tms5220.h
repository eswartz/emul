/*
  tms5220.h						-- lookup tables for speech synthesizer

  This file is no longer used.  The information is in a different
  format in tms5220r.c.

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

#include "centry.h"

u16	energytable[16]=
	{ 	0 << 6,  4 << 6,  5 << 6,  7 << 6, 
		10 << 6, 14 << 6, 20 << 6, 29 << 6,
		40 << 6, 57 << 6, 81 << 6, 114 << 6,
		161 << 6, 227 << 6, 321 << 6, 0 << 6
	};

u32 rmsTrans[16] = { 0, 52, 87, 123, 174, 246, 348,	491, 
				694, 981, 1385, 1957, 2764, 3904, 5514, 7789 };

				
u16 pitchtable[64] = { 0 << 8, 15 << 8, 16 << 8, 17 << 8, 18 << 8, 19 << 8, 20 << 8, 21 << 8, 22 << 8, 23 << 8, 24 << 8, 25 << 8,
				26 << 8, 27 << 8, 28 << 8, 29 << 8, 30 << 8, 31 << 8, 32 << 8, 33 << 8, 34 << 8, 35 << 8, 36 << 8, 37 << 8,
				38 << 8, 39 << 8, 40 << 8, 41 << 8, 42 << 8, 44 << 8, 46 << 8, 48 << 8, 50 << 8, 52 << 8, 53 << 8, 56 << 8,
				58 << 8, 60 << 8, 62 << 8, 65 << 8, 68 << 8, 70 << 8, 72 << 8, 76 << 8, 78 << 8, 80 << 8, 84 << 8, 86 << 8,
				91 << 8, 94 << 8, 98 << 8, 101 << 8, 105 << 8, 109 << 8, 114 << 8, 118 << 8, 122 << 8, 127 << 8,
				132 << 8, 137 << 8, 142 << 8, 148 << 8, 153 << 8, 159 << 8};

#define BASE 32768

s16 k1table[32] = {
	-0.97850 * BASE,
	-0.97270 * BASE,
	-0.97070 * BASE,
	-0.96680 * BASE,
	-0.96290 * BASE,
	-0.95900 * BASE,
	-0.95310 * BASE,
	-0.94140 * BASE,
	-0.93360 * BASE,
	-0.92580 * BASE,
	-0.91600 * BASE,
	-0.90620 * BASE,
	-0.89650 * BASE,
	-0.88280 * BASE,
	-0.86910 * BASE,
	-0.85350 * BASE,
	-0.80420 * BASE,
	-0.74058 * BASE,
	-0.66019 * BASE,
	-0.56116 * BASE,
	-0.44286 * BASE,
	-0.30708 * BASE,
	-0.15735 * BASE,
	-0.00005 * BASE,
	 0.15725 * BASE,
	 0.30698 * BASE,
	 0.44288 * BASE,
	 0.65109 * BASE,
	 0.66013 * BASE,
	 0.74054 * BASE,
	 0.80416 * BASE,
	 0.85350 * BASE
};

s16 k2table[32] =
{
	-0.64000 * BASE,
	-0.59000 * BASE,
	-0.53500 * BASE,
	-0.47507 * BASE,
	-0.41039 * BASE,
	-0.34129 * BASE,
	-0.26830 * BASE,
	-0.19209 * BASE,
	-0.11350 * BASE,
	-0.03345 * BASE,
	 0.04702 * BASE,
	 0.12690 * BASE,
	 0.20515 * BASE,
	 0.28067 * BASE,
	 0.35325 * BASE,
	 0.42163 * BASE,
	 0.48553 * BASE,
	 0.54464 * BASE,
	 0.59878 * BASE,
	 0.64796 * BASE,
	 0.69227 * BASE,
	 0.73190 * BASE,
	 0.76714 * BASE,
	 0.79828 * BASE,
	 0.82567 * BASE,
	 0.84965 * BASE,
	 0.87057 * BASE,
	 0.88875 * BASE,
	 0.90451 * BASE,
	 0.91813 * BASE,
	 0.92988 * BASE,
	 0.98830 * BASE
};

s16 k3table[16] =
{
	-0.86000 * BASE,
	-0.75467 * BASE,
	-0.64933 * BASE,
	-0.54400 * BASE,
	-0.43867 * BASE,
	-0.33333 * BASE,
	-0.22800 * BASE,
	-0.12267 * BASE,
	-0.01733 * BASE,
	 0.08800 * BASE,
	 0.19333 * BASE,
	 0.29867 * BASE,
	 0.40400 * BASE,
	 0.50933 * BASE,
	 0.61487 * BASE,
	 0.72000 * BASE
};

s16 k4table[16] =
{
	-0.64000 * BASE,
	-0.53145 * BASE,
	-0.42289 * BASE,
	-0.31434 * BASE,
	-0.20579 * BASE,
	-0.09723 * BASE,
	 0.01132 * BASE,
	 0.11987 * BASE,
	 0.22843 * BASE,
	 0.33698 * BASE,
	 0.44553 * BASE,
	 0.55409 * BASE,
	 0.66264 * BASE,
	 0.77119 * BASE,
	 0.87975 * BASE,
	 0.98830 * BASE
};

s16 k5table[16] =
{
	-0.64000 * BASE,
	-0.54933 * BASE,
	-0.45867 * BASE,
	-0.38800 * BASE,
	-0.27733 * BASE,
	-0.18667 * BASE,
	-0.09600 * BASE,
	-0.00533 * BASE,
	 0.08533 * BASE,
	 0.17600 * BASE,
	 0.26867 * BASE,
	 0.35733 * BASE,
	 0.44800 * BASE,
	 0.53867 * BASE,
	 0.62933 * BASE,
	 0.72000 * BASE
};

s16 k6table[16] =
{
	-0.50000 * BASE,
	-0.41333 * BASE,
	-0.32667 * BASE,
	-0.24000 * BASE,
	-0.15333 * BASE,
	-0.06667 * BASE,
	 0.02000 * BASE,
	 0.10667 * BASE,
	 0.19333 * BASE,
	 0.28000 * BASE,
	 0.36687 * BASE,
	 0.45333 * BASE,
	 0.54000 * BASE,
	 0.62667 * BASE,
	 0.71333 * BASE,
	 0.80000 * BASE
};

s16 k7table[16] =
{
	-0.60000 * BASE,
	-0.50667 * BASE,
	-0.41333 * BASE,
	-0.32000 * BASE,
	-0.22667 * BASE,
	-0.13333 * BASE,
	-0.04000 * BASE,
	 0.05333 * BASE,
	 0.14867 * BASE,
	 0.24000 * BASE,
	 0.33333 * BASE,
	 0.42687 * BASE,
	 0.52000 * BASE,
	 0.61333 * BASE,
	 0.70667 * BASE,
	 0.80000 * BASE
};

s16 k8table[8] =
{
	-0.50000 * BASE,
	-0.31429 * BASE,
	-0.12857 * BASE,
	 0.05714 * BASE,
	 0.24286 * BASE,
	 0.42857 * BASE,
	 0.61429 * BASE,
	 0.80000 * BASE
};

s16 k9table[8] =
{
	-0.50000 * BASE,
	-0.34286 * BASE,
	-0.18571 * BASE,
	-0.02857 * BASE,
	 0.12857 * BASE,
	 0.28571 * BASE,
	 0.44286 * BASE,
	 0.60000 * BASE
};

s16 k10table[8] =
{
	-0.40000 * BASE,
	-0.25714 * BASE,
	-0.11429 * BASE,
	 0.02857 * BASE,
	 0.17143 * BASE,
	 0.31429 * BASE,
	 0.45714 * BASE,
	 0.60000 * BASE
};

///////

/* chirp table */

static signed char chirptable[41] = {
	0x00, 0x2a, (char) 0xd4, 0x32,
	(char) 0xb2, 0x12, 0x25, 0x14,
	0x02, (char) 0xe1, (char) 0xc5, 0x02,
	0x5f, 0x5a, 0x05, 0x0f,
	0x26, (char) 0xfc, (char) 0xa5, (char) 0xa5,
	(char) 0xd6, (char) 0xdd, (char) 0xdc, (char) 0xfc,
	0x25, 0x2b, 0x22, 0x21,
	0x0f, (char) 0xff, (char) 0xf8, (char) 0xee,
	(char) 0xed, (char) 0xef, (char) 0xf7, (char) 0xf6,
	(char) 0xfa, 0x00, 0x03, 0x02,
	0x01
};

static char interp_coeff[8] = {
	8, 8, 8, 4, 4, 2, 2, 1
};


#include "cexit.h"
