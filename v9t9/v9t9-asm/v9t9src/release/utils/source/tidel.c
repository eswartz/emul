/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	This program will be used simply to delete files.

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



int	dodelete(char *fn)
{
	char	dir[80];
	char	name[14];
	int	doad;

	int	f;
	byte	flags,reclen;


/*	if (!fiadordoad(fn,&doad,dir,name))
	{
		printf("%s is an invalid filename.\n",from);
		return 1;
	}

	if (!openti(fn,&f))
	{
		printf("Couldn't access %s!\n",fn);
		return 1;
	}

	tigettype(f,&flags,&reclen);*/

	if (tiexists(fn) && verify)
	{
		printf("Delete %s (y/n)?\n",fn);
		if (!getyesorno())
			return 1;
	}

	if (verbose)
		printf("Deleting %s...\n",fn);

	if (!deleteti(fn))
	{
		printf("Couldn't delete %s!\n",fn);
		tierror(fn);
	}


	return	1;
}




void	help(void)
{
		printf("TIDEL V9t9 File Deleter v1.0\n"
		       "\n"
		       "Usage:   TIDEL [options] { <filename> } \n"
		       "\n"
		       "TIDEL will delete the indicated files.  This program is most useful\n"
		       "with disk images.\n"
		       "\n"
		       "Options:\n"
		       "\t\t/C\t-- confirm deletion of each file\n"
		       "\t\t/V\t-- verbose operation\n"
		       "\n"
		       );
		exit(0);

}


int	main(int argc, char **argv)
{
	char	temp[80];
	char	filetodelete[80];
	char	opt;

	if (paraminit(0,temp)<=0)
		help();

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

		while (getfilename(temp,filetodelete,1))
			if (!dodelete(filetodelete))
				break;
	}

	return 0;
}

