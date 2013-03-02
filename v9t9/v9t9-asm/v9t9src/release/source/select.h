/*
  select.h

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
#ifdef	__SELECT__
#define	W
#else
#define	W extern
#endif


#define	M_ROM1	1
#define	M_ROM2	2
#define	M_GROM	4
#define	M_MINI	8

#define	MAXMODS	256

#include "utypes.h"

struct	modrec
{
	char	title[33];
	char	basename[9];
	byte	opts;
	byte	exist;
};

W struct modrec far *mods;

W int	nummods;

W word	selected[33];

W int	defaultmodule;