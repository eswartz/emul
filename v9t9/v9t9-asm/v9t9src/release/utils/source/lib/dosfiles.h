#include "utypes.h"

char	*getstring(char *buf, int len, FILE *fptr,int fixed,byte nul);

int	exists(char *filename);
int	isdir(char *filename);

FILE*	openbufferwrite(char *filename);
int	writeflush(FILE *fptr);
int     writebuffer(FILE *fptr, byte *buf, word len);
int	closebufferwrite(FILE *fptr);

int     readbuffer(FILE *fptr, byte *buf, word len);
int	readflush(FILE *fptr);
FILE*	openbufferread(char *filename);
int	closebufferread(FILE *fptr);
