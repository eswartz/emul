/*
  fdr.c

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
/*
	FDR
	---


	createfdr(byte *fdr, char *filename, byte type, byte reclen);



*/

#include <io.h>
#include <memory.h>

#include "fdr.h"


word	swapbytes(word a)
{
asm	{
	mov	ax,a
	xchg	al,ah
	}
	return	_AX;
}



void	createtifdr(struct fdrstruc *fdr, char *filename,
		  byte type, byte reclen)
{
	memset(fdr,0,256);

	memcpy(fdr->name,filename,10);
	fdr->reclen=reclen;
	fdr->flags=type & (F_PROGRAM|F_INTERNAL|F_VARIABLE);
	fdr->recspersec=(reclen==0 ? 0 : 256/reclen);

}

