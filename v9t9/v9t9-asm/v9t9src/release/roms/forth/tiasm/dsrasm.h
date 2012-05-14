#ifdef	__TIASM__
#define	S
#else
#define	S	extern
#endif

#include <stdio.h>

S	char	*inname,*outname,*listname;
S	FILE	*in,*out,*list;
