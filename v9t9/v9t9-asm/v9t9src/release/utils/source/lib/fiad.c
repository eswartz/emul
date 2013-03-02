/*
  fiad.c

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
	fiad
	----


	Files-in-a-directory routines.

*/

#include <ctype.h>
#include <dir.h>
#include <dos.h>
#include <fcntl.h>
#include <io.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys\stat.h>

#include "error.h"
#include "fdr.h"
#include "fiad.h"		// refs fdr.h
#include "files.h"
#include "names.h"
#include "utypes.h"



void	noansi(char *name)
{
	int 	index;

	for (index=0; index<10; index++)
        	name[index]&=0x7f;

}

int	openfiad(char *filename, int *handle)
{
	struct  tifile *ff;
	char	path[80];
	char	dosname[14];
	char	fdrname[12];
	char	tiname[12];
	int	ti;
	struct	fdrstruc *fdr;


	_fmode=O_BINARY;

	if ((ti=*handle=getfreeti(0))!=-1)
	{
	ff=&tifiles[ti];
	fdr=&ff->fdr;

	if (split(filename,path,tiname))
	{
		memset(fdrname,0x20,10);
		memcpy(fdrname,tiname,strlen(tiname));
		ti2dos(tiname,dosname);
		strcat(path,dosname);
		if ((_dos_open(path,O_RDWR|O_BINARY,&ff->doshandle))==0)
		{
			if (read(ff->doshandle,fdr,128)==128)
			{
				noansi(fdr->name);
				if (memicmp(fdr,fdrname,10)==0 &&
				    ((fdr->flags&F_VARIABLE) ? fdr->reclen : 1))
				{
					tifiles[ti].inuse=1;
						Error=0;
						return 1;
				}
			}
			Error=NOTTIEMUL;
			close(ff->doshandle);
			return 0;
		}
		else
		{
			Error=BADFILE;
			close(ff->doshandle);
			return 0;
		}
	}
	}
	return 0;			// split sets Error
}


int	createfiad(char *filename, byte type, byte reclen, int *handle)
{
	struct tifile *ff;
	char	path[80];
	char	dosname[14];
	char	fdrname[12];
	char	tiname[12];
	int	ti;

	_fmode=O_BINARY;

	if ((*handle=ti=getfreeti(0))!=-1)
	{
	ff=&tifiles[ti];

	if (split(filename,path,tiname))
	{
		memset(fdrname,0x20,10);
		memcpy(fdrname,tiname,strlen(tiname));
		ti2dos(tiname,dosname);
		strcat(path,dosname);
		if ((ff->doshandle=open(path,
			      O_CREAT|O_TRUNC|O_RDWR|O_BINARY,
			      S_IWRITE|S_IREAD))!=-1)
		{
			ff->inuse=1;
			createtifdr(&ff->fdr,fdrname,type,reclen);
			if (writetifdr(ti))
			{
				Error=0;
				return 1;
			}
			else
				return 0;
		}
		Error=NOSPACE;
		return 0;
	}
	}
	return 0;
}


int	deletefiad(char *filename)
{
	char	path[80];
	char	dosname[14];
	char	tiname[12];

	if (split(filename,path,tiname))
	{
		ti2dos(tiname,dosname);
		strcat(path,dosname);
		unlink(path);
		return 1;
	}
	else
	{
		Error=BADPATH;
		return 0;
	}
}



struct	ffblk ff;
char	ffpath[80];
char	ffwild[14];



int	wilddos2ti(char *name)
{
	char	temp[14];
	char	*back;

	dos2ti(ff.ff_name,temp);
	back=strchr(temp,' ');
	if (back)
		*back=0;

	strcpy(name,ffpath);
	strcat(name,temp);

	return 	0;


}
/*
	Initiate a wildcard search.
	Only ONE can be occurring at one time.

	Return 1 if the search is finished.
*/
int	wildfiadinit(char *path, char *wildcard, char *first)
{
	char	temp[80];
	byte	*trk;

	trk=wildcard;
	while (*trk)
	{
		if (*trk=='?' || *trk=='*')
			(*trk)+=0x80;
		trk++;
	}

	strcpy(temp,wildcard);
	ti2dos(temp,wildcard);		  // make legal wildcard

	trk=wildcard;
	while (*trk)
	{
		if (*trk==('?'+0x80) || *trk==('*'+0x80))
			(*trk)-=0x80;
		trk++;
	}

	strcpy(temp,path);
	strcpy(ffpath,path);
	strcat(temp,wildcard);
	if (strchr(wildcard,'.')==NULL)
		strcat(temp,".*");
	if (*(wildcard+strlen(wildcard)-1)=='.')
		strcat(temp,"*");


	if (findfirst(temp,&ff,0))
		return 1;

	return wilddos2ti(first);
}


int	wildfiad(char *name)
{
	if (findnext(&ff))
		return 1;

	return wilddos2ti(name);
}


