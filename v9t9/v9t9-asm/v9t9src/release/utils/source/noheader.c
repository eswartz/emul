/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Lose the header from self-transferred ROMs.
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

#include "param.h"


void	help(void)
{
	printf("\n"
	"NOHEADER V9t9-Compatibility File Stripper Utility\n"
	"\n"
	"NOHEADER is used to lose headers from custom-transferred files (usually\n"
	"ROM images) to make the files usable with V9t9.  ROM images existing in\n"
	"TIFILES or GRAM-Kracker files are typical targets of NOHEADER.\n"
	"\n"
	"Note that the options below are ONLY used to tell NOHEADER the size of\n"
	"the header -- no checks are done to verify conformity with a specific\n"
	"file type.\n"
	"\n"
	"Usage:\n"
	"\n"
	"NOHEADER <options> <old file> <new file>\n"
	"\n"
	"Options:\t/T\t-- source file is in TIFILES or V9t9 format\n"
	"\t\t\t  (128-byte header only)\n"
	"\t\t/G\t-- source file is GRAM-Kracker format\n"
	"\t\t\t   (128-byte TIFILES/V9t9 header, plus 6-byte GK header)\n"
	"\t\t/Cxxx\t-- custom header size xxx.\n"
	"\n"
	);

	exit(1);
}


void	main(int argc, char **argv)
{
	char	temp[80];
	char	fromname[80],toname[80];
	char	opt;

	int	headsize;

	int	from,to;
	char   	buff[1024];
	longint	left;
	int	size;

	struct	stat st;


	if (paraminit(0,temp)!=2)
		help();

	headsize=0;

	while ((opt=getopt())!=0)
	{
	switch (opt)
	{
	case	'T':	headsize=128;
			break;
	case	'G':	headsize=128+6;
			break;
	case	'C':	headsize=atoi(getoptstr(temp,1));
			break;
	case	'H':
	case	'?':	help();
			break;
	default:	printf("Unknown option '%c'\n",opt);
			exit(1);
	}
	}

	if (!headsize)
	{
		printf("\nAn option is required to tell TIHEADER what kind of source file you have.\n");
		exit(1);
	}

	getparam(fromname);
	getparam(toname);

	from=open(fromname,O_RDONLY|O_BINARY);
	if (from==-1)
	{
		printf("Couldn't open %s!\n",fromname);
		exit(1);
	}

	to=open(toname,O_BINARY|O_CREAT|O_TRUNC|O_WRONLY,S_IREAD|S_IWRITE);
	if (to==-1)
	{
		printf("Couldn't create %s!\n",toname);
		exit(1);
	}

	fstat(from,&st);

	left=st.st_size;

	lseek(from,headsize,SEEK_SET);

	left-=headsize;

	while (left)
	{
		if (left>1024)
			size=1024;
		else
			size=left;

		if (read(from,buff,size)!=size)
		{
			printf("Read error on %s!\n",fromname);
			exit(1);
		}

		if (write(to,buff,size)!=size)
		{
			printf("Write error on %s!\n",toname);
			exit(1);
		}

		left-=size;
	}

	close(to);
	close(from);

	printf("Successful!\n");
}
