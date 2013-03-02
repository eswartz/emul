/*
  timatch.c

  (c) 1991-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
/*
	This utility will fix the names of FIADs.

*/

#include <conio.h>
#include <ctype.h>
#include <dos.h>
#include<io.h>
#include <memory.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

#include "doad.h"
#include "dosfiles.h"
#include "error.h"
#include "fdr.h"
#include "fiad.h"
#include "files.h"
#include "names.h"
#include "param.h"

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



void	help(void)
{
	 printf("TIMATCH V9t9 FIAD Filename Fixer v1.0\n"
		"\n"
		"Usage:   TIMATCH [options] { file }\n"
		"\n"
		"TIMATCH will match up the internal filename with the external\n"
		"DOS filename if you have accidentally used DOS RENAME on a\n"
		"file and can no longer access it.\n"
		"\n"
		"DANGER:  do not use on non-V9t9 DOS files!\n"
		"\n"
		"Options:\t/V\t-- verbose operation\n"

		);
	 exit(1);

}


int	main(int argc, char **argv)
{
	char	temp[80];
	char	fullname[80];
	char	tocheck[80];

	char	opt;

	int	handle;
	int	doad;
	char	path[80];
	char	name[14];
	char	dosname[14];

	struct	fdrstruc fdr;

	if (paraminit(0,temp)<=0)
		help();

	while ((opt=getopt())!=0)
	{
	switch (opt)
	{
	case	'V':	verbose=1;
			break;
	case	'H':
	case	'?':	help();
			break;
	default:	printf("Unknown option '%c'\n",opt);
			exit(1);
	}
	}


	while (getparam(temp)!=NULL)
	while (getfilename(temp,tocheck,1))
	{
		if (!fiadordoad(tocheck,&doad,path,name))
		{
			printf("Cannot acccess %s!\n",tocheck);
			tierror(tocheck);
			exit(1);
		}

		if (doad)
		{
			printf("%s:  TIMATCH only works with FIAD files.\n",
			tocheck);
		}
		else
		{

		memset(name+strlen(name),0x20,10-strlen(name));

		ti2dos(name,dosname);
		strcpy(fullname,path);
		strcat(fullname,dosname);

		handle=open(fullname,O_RDWR|O_BINARY);
		if (handle==-1)
		{
			printf("Couldn't open %s!\n",tocheck);
		}
		else

		if (read(handle,&fdr,128)!=128)
		{
			printf("%s is not a V9t9 file!\n",fullname);
		}
		else

		if (memcmp(fdr.name,name,10)==0)
		{
			if (verbose)
				printf("%s: filenames match.\n",tocheck);
		}
		else
		{
			strupr(name);
			name[10]=0;
			memset(fdr.name,0x20,10);
			memcpy(fdr.name,name,strlen(name));

			lseek(handle,0,SEEK_SET);

			if (write(handle,&fdr,128)!=128)
			{
				printf("Couldn't update %s!\n",tocheck);
				exit(1);
			}
			else
			if (verbose)
				printf("Fixed %s.\n",tocheck);
		}

		close(handle);
		}
	}

	return 0;
}

