/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Convert XMODEMed TIFILES files into V9t9 files.
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
char	assume=0;
char	retry=0;


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
	char	fromdosname[14];
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

	if (!fiadordoad(from,&doad,fromdir,fromdosname))
	{
		printf("%s is an invalid filename.\n",from);
		return 1;
	}

	if (doad)
	{
		printf("It doesn't make sense to convert files from a disk image, since these\n"
		"are already in V9t9 format.\n");
		exit(1);
	}


	i=open(from,O_RDONLY|O_BINARY);
	if (i==-1)
	{
		printf("Couldn't open %s!\n",from);
		return 1;
	}

	if (read(i,&xmod,128)!=128)
	{
		close(i);
		printf("%s:  invalid TIFILES file (too short)\n",from);
		return 1;
	}

	if (memcmp(xmod.tifiles+1,"TIFILES",7))
	{
		if (!assume)
		{
			printf("%s doesn't appear to be a TIFILES file.\n"
			"Assume DIS/FIX 128 format (y/n)?\n");
			if (!getyesorno())
			{
				close(i);
				return 1;
			}
		}
		xmod.secsused=swapbytes(filelength(i)/256);
		xmod.flags=0;
		xmod.eof=filelength(i)%256;
		xmod.reclen=80;
		xmod.fixrecs=swapbytes(xmod.secsused*2-(xmod.eof ? 1 : 0));
	}


	dos2ti(fromdosname,fromname);

	strcpy(to,todir);
	strcat(to,fromname);

	if (verbose)
		printf("Converting %s to %s...\n",from,to);

	if (tiexists(to))
	{
		if (verify)
		{
			printf("%s already exists.  Overwrite (y/n)? \n",to);
			if (!getyesorno())
				return 1;
		}
	}

	if (!createti(to,fdr.flags,fdr.reclen,&o))
	{
		printf("Couldn't create %s!\n",to);
		return 1;
	}

	memset(&fdr,0,128);
	memcpy(fdr.name,fromname,10);
	fdr.flags=xmod.flags;
	fdr.recspersec=xmod.recspersec;
/*	if (xmod.reclen!=0)
		fdr.recspersec=256/xmod.reclen;
	else
		fdr.recspersec=0;*/
	fdr.secsused=xmod.secsused;
	fdr.eof=xmod.eof;
	fdr.reclen=xmod.reclen;
	fdr.fixrecs=swapbytes(xmod.fixrecs);

	memcpy(&tifiles[o].fdr,&fdr,128);
	tifiles[o].fdr.secsused=0;

	secs=swapbytes(fdr.secsused);
	while (secs)
	{
		if (read(i,sec,256)!=256)
		{
			printf("Read error on %s!\n",from);
			return 1;
		}
		if (!writenewtiblock(o,sec))
		{
			printf("%s:  device probably full\n",todir);
			close(i);
			closeti(o);
			deleteti(to);
			return retry;
		}
		secs--;
	}

	closeti(o);
	close(i);

	return	1;
}




void	help(void)
{
	printf("\n"
	"XMDM2TI TIFILES to V9t9 File Converter v1.0\n"
	"\n"
	"XMDM2TI will convert 99/4A files which were transferred from a 99/4A with\n"
	"XMODEM (in the \"TIFILES\" format) to be compatible with V9t9.\n"
	"Usage:\n"
	"\n"
	"XMDM2TI [options] { <filename> } [ <destination> | <disk image:> ]\n"
	"\n"
	"Options:\t/V\t-- verbose operation\n"
	"\t\t/C\t-- confirm overwrites of existing files\n"
	"\t\t/R\t-- if dest becomes full, delete current file and continue.\n"
	"\t\t/A\t-- assume that files without headers are DIS/FIX 128\n"
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
	case	'A':	assume=1;
			break;
	case	'R':	retry=1;
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