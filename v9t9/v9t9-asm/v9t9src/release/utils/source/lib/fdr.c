/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
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