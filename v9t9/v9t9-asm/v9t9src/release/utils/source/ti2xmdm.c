/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Convert V9t9 files into XMODEM TIFILES-types.
*/

#include <alloc.h>
#include <conio.h>
#include <ctype.h>
#include <dos.h>
#include <fcntl.h>
#include <io.h>
#include <memory.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys\stat.h>

#include "doad.h"
#include "dosfiles.h"
#include "error.h"
#include "fdr.h"
#include "fiad.h"
#include "files.h"
#include "names.h"
#include "param.h"


char	verify=0;
char	verbose=0;

/*
	Get yes or no (Y/N), return 1 for Y.
*/
int	getyesorno(void)
{
	char	ch;

	do	ch=toupper(getch());  while (ch!='Y' && ch!='N');
	return	ch=='Y';
}

int     convert(char *from, char *todir)
{
	char	to[80];
	char	fromdir[80];
	char	fromtiname[14];
	char	fromname[14];
	int	doad;
	char	sec[256];

	int	i,o;
	byte	flags,reclen;
	word	secs;

	struct	xmodemtype
	{
		char 	tifiles[8];
		word	secsused;
		byte	flags;
		byte	recspersec;
		byte	eof;
		byte	reclen;
		word	fixrecs;
		byte	unused[127-14+1];
	}	xmod;

	struct	fdrstruc fdr;

	if (!fiadordoad(from,&doad,fromdir,fromtiname))
	{
		printf("%s is an invalid filename.\n",from);
		return 1;
	}


	if (!openti(from,&i))
	{
		printf("Couldn't open %s!\n",from);
		tierror(from);
		return 1;
	}

	memcpy(xmod.tifiles+1,"TIFILES",7);
	xmod.tifiles[0]=7;

	ti2Dos(fromtiname,fromname);

	strcpy(to,todir);
	strcat(to,fromname);

	if (verbose)
		printf("Converting %s to %s...\n",from,to);

	if (exists(to))
	{
		if (verify)
		{
			printf("%s already exists.  Overwrite (y/n)? \n",to);
			if (!getyesorno())
				return 1;
		}
	}

	o=open(to,O_CREAT|O_TRUNC|O_BINARY|O_RDWR,S_IWRITE|S_IREAD);
	if (o==-1)
	{
		printf("Couldn't create %s!\n",to);
		return 1;
	}

	memcpy(&fdr,&tifiles[i].fdr,128);

	memset(&xmod,0,128);

	memcpy(xmod.tifiles+1,"TIFILES",7);
	xmod.tifiles[0]=7;

	xmod.flags=fdr.flags;
	xmod.secsused=fdr.secsused;
	xmod.eof=fdr.eof;
	xmod.reclen=fdr.reclen;
	xmod.fixrecs=swapbytes(fdr.fixrecs);
        xmod.recspersec=fdr.recspersec;

	if (write(o,&xmod,128)!=128)
	{
		printf("%s: disk full or write error\n!",to);
		return 0;
	}

	secs=swapbytes(fdr.secsused);
	while (secs)
	{
		if (!readnexttiblock(i,sec))
		{
			printf("Read error on %s!\n",from);
			closeti(i);
			close(o);
			return 1;
		}
		if (write(o,sec,256)!=256)
		{
			printf("%s:  device probably full\n",to);
			closeti(i);
			close(o);
			deleteti(to);
			return 0;
		}
		secs--;
	}

	close(o);
	closeti(i);

	return	1;
}




void	help(void)
{
	printf("\n"
	"TI2XMDM V9t9 to TIFILES Converter v1.0\n"
	"\n"
	"XMDM2TI will convert V9t9 emulated files into the TIFILES format for\n"
	"transferring via XMODEM to a 99/4A.\n"
	"\n"
	"Usage:\n"
	"\n"
	"TI2XMDM [options] { <filename> } <destination>\n"
	"\n"
	"Options:\t/V\t-- verbose operation\n"
	"\t\t/C\t-- confirm overwrites of existing files\n"
	"\n"
	);

	exit(1);
}

void	main(int argc, char **argv)
{
	char	temp[80];
	char	filetocopy[80];
	char	destdir[80];
	char	opt;

	if (paraminit(1,destdir)<=0)
		help();

	if (!isdir(destdir))
	{
		printf("Last parameter should be destination directory.\n");
		exit(1);
	}
	else
		makedirof(destdir);

	while ((opt=getopt())!=0)
	{
	switch (opt)
	{
	case	'?':
	case	'H':	help();
			break;
	case	'C':	verify=1;
			break;
	case	'V':	verbose=1;
			break;

	default:	printf("Unknown option '%c'\n",opt);
			exit(1);
	}
	}


	while (getparam(temp)!=NULL)
	{
		if (isdir(temp))
			makedirof(temp);

		while (getfilename(temp,filetocopy,0))
			if (!convert(filetocopy,destdir))
				break;
	}


}
