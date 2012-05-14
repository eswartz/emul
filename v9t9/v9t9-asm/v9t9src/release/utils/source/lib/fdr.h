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