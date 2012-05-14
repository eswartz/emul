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