/*
  tirename.c

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
	This utility will rename FIADs.

*/

#include <conio.h>
#include <ctype.h>
#include <dos.h>
#include<io.h>
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
	 printf("TIRENAME V9t9 FIAD File Renamer v1.0\n"
		"\n"
		"Usage:   TIRENAME [options] <file> <new filename>\n"
		"\n"
		"TIRENAME will rename a FIAD file.\n"
		"\n"
		"Options:\t/V\t-- verbose operation\n"

		);
	 exit(1);

}


int	main(int argc, char **argv)
{
	char	temp[80];
	char	fromname[80];
	char	toname[80];
	char	fulltoname[80];
	char	fulldosname[80];

	char	opt;

	int	handle;
	int	doad;
	struct	tifile *ff;
	char	path[80];
	char	name[14];
	char	dosname[14];

	if (paraminit(0,temp)!=2)
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


	getparam(fromname);
	getparam(toname);

	if (strpbrk(toname,"\\. "))
	{
		printf("Illegal characters in destination name (%s).\n"
		"Only specify the filename, not the path, as the new name.\n",
		toname);
		exit(1);

	}
	fiadordoad(toname,&doad,path,name);

	if (!fiadordoad(fromname,&doad,path,temp))
	{
		printf("Couldn't access %s!\n",fromname);
		exit(1);
	}

	if (doad)
	{
		printf("TIRENAME only works with FIAD files.\n");
		exit(1);
	}

	strcpy(fulltoname,path);
	strcat(fulltoname,name);

	if (tiexists(fulltoname))
	{
		printf("%s already exists!\n",fulltoname);
		exit(1);
	}

	if (!openti(fromname,&handle))
	{
		printf("Couldn't find %s!\n",fromname);
		exit(1);
	}

	ff=&tifiles[handle];

	strupr(name);
	name[10]=0;
	memset(ff->fdr.name,0x20,10);
	memcpy(ff->fdr.name,name,strlen(name));

	writetifdr(handle);
	closeti(handle);

	strcpy(fulltoname,path);
	ti2dos(name,dosname);
	strcat(fulltoname,dosname);

	rename(fromname,fulltoname);

	return 0;
}

