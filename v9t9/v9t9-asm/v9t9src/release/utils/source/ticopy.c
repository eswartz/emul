/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	This program will be used simply to copy files.

	Obviously, it will be most useful when one of the files
	is a DOAD.
*/


#include <alloc.h>
#include <conio.h>
#include <ctype.h>
#include <dos.h>
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
char	retry=0;
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



int	copy(char *from, char *todir)
{
	char	to[80];
	char	fromdir[80];
	char	fromname[14];
	int	doad;
	char	buf[256];

	int	i,o;
	byte	flags,reclen;
	word	secs;


	if (!fiadordoad(from,&doad,fromdir,fromname))
	{
		printf("%s is an invalid filename.\n",from);
		return 1;
	}

	strcpy(to,todir);
	strcat(to,fromname);

	if (verbose)
		printf("Copying %s to %s...\n",from,to);

	if (tiexists(to))
	{
		if (verify)
		{
			printf("%s already exists.  Overwrite (y/n)? \n",to);
			if (!getyesorno())
				return 1;
		}
	}

	if (!openti(from,&i))
	{
		printf("Couldn't open %s!\n",from);
		return 1;
	}

	tigettype(i,&flags,&reclen);
	if (!createti(to,flags,reclen,&o))
	{
		printf("Couldn't create %s!\n",to);
		return retry;
	}

	memcpy(&tifiles[o].fdr,&tifiles[i].fdr,256);
	memset(tifiles[o].fdr.lnks,0,sizeof(tifiles[o].fdr.lnks));

	tifiles[o].fdr.secsused=0;
	secs=swapbytes(tifiles[i].fdr.secsused);
	while (secs)
	{
		if (!readnexttiblock(i,buf))
		{
			printf("Read error on %s!\n",from);
			break;
		}
		if (!writenewtiblock(o,buf))
		{
			printf("%s:  device full\n",todir);
			closeti(i);
			closeti(o);
			deleteti(to);
			return retry;
		}
		secs--;
	}

	closeti(i);
	closeti(o);

	return	1;
}




void	help(void)
{
		printf("TICOPY V9t9 File Copier v1.0\n"
		       "\n"
		       "Usage:   TICOPY [options] { <filename> } [<directory> | <disk image:>] \n"
		       "\n"
		       "TICOPY will copy each of the indicated source files into the destination,\n"
		       "which is either a directory (FIAD) or a disk image (DOAD).  The source\n"
		       "files may be from either FIADs or DOADs as well.\n"
		       "\n"
		       "Options:\n"
		       "\t\t/C\t-- confirm overwrites of existing files\n"
		       "\t\t/R\t-- if dest becomes full, delete current file and continue.\n"
		       "\t\t/V\t-- verbose operation\n"
		       "\n"
		       );
		exit(0);

}


int	main(int argc, char **argv)
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
	case	'R':	retry=1;
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

		while (getfilename(temp,filetocopy,1))
			if (!copy(filetocopy,destdir))
				break;
	}

	return 0;
}

