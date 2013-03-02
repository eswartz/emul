/*
  fdr.h

  (c) 1991-2012 Edward Swartz

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
#include "utypes.h"

struct dskstruc
{
	char	name[10];
	word	totsecs;
	byte	secspertrack;
	byte	id[4];
	byte	tracksperside;
	byte	sides;
	byte	density;
	byte	res[36];
	byte	abm[200];
};


struct	fdrstruc
{
	char	name[10];
	word	res10;
	byte	flags;
	byte    recspersec;
	word    secsused;  				// swapped
	byte    eof;
	byte    reclen;
	word    fixrecs;				// native order
	byte	res20[8];
	byte	lnks[256-28];
};


#define	F_PROGRAM	1
#define F_INTERNAL	2
#define	F_WRITEPROTECT	8
#define	F_VARIABLE	128

#define	F_FIXED		0
#define F_TEXT		0
#define	F_UNPROTECT	0
#define	F_FIXED		0

word	swapbytes(word a);
void	createtifdr(struct fdrstruc *fdr, char *filename,
		  byte type, byte reclen);
