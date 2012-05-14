#include "utypes.h"

#define	MAXTIFILES 10

extern	struct	tifile
{
	int	doshandle;		// either FIAD or DOAD

	struct	fdrstruc fdr;

	byte	cursec[256];
	byte	changed;
	byte	inuse;

	word	fdrsecsize;

	word	secnum;			// offset in file, FIAD & DOAD
	word	secoffs;

	byte	doad;			// is this a DOAD?
	word	fdrsec;			// sector of FDR on a DOAD
	word far *links;		// all the sectors in the file, 0=end

	struct	dskstruc dsk;
}	tifiles[MAXTIFILES];





int	getfreeti(int doad);
void	createtifdr(struct fdrstruc *fdr, char *filename,
		  byte type, byte reclen);
int	writetifdr(int handle);
int	writetisector(int handle);
int	updateti(int handle);
int	readtisector(int handle);
int	getnexttisector(int handle);
int	getnewtisector(int handle);

int	openti(char *filename, int *handle);
int	createti(char *filename, byte type, byte reclen, int *handle);
int	writeti(int handle, byte *buf, byte len);
int	readti(int handle, byte *buf, byte len);
int	tieof(int handle);
int	closeti(int handle);
void	tigettype(int handle, byte *type, byte *len);
longint	tigetfilesize(int handle);
longint	tigetcurpos(int handle);
longint tigetrealfilesize(int handle);

int	fiadordoad(char *filename,
		   int *doad,
		   char *pathordisk,
		   char *name);
int	wild(char *next);
int	wildinit(char *filename, char *first,int ti);
int	tiexists(char *filename);

int	deleteti(char *filename);
int	readnexttiblock(int handle, byte *buf);
int	writenewtiblock(int handle, byte *buf);
