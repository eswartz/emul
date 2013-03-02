/*
  files.c

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
	FILES
	-----


	Generalized file routines for either FIADs or DOADs.

*/

#include <alloc.h>
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
#include "doad.h"
#include "files.h"
#include "names.h"
#include "utypes.h"



struct	tifile tifiles[MAXTIFILES];






int	getfreeti(int isdoad)
{
	int	ind=0;
	struct  tifile *tf;

	while	(ind<MAXTIFILES && tifiles[ind].inuse)
		ind++;

	if (ind>=MAXTIFILES)
	{
		Error=NOMEMORY;
		return -1;
	}
	else
	{
		tf=&tifiles[ind];
		tf->inuse=0;
		tf->changed=0;
		tf->secnum=0;
		tf->secoffs=0;
		tf->doad=isdoad;
		tf->fdrsecsize=(isdoad ? 256 : 128);
		if ((tf->links=farcalloc(8192,1))==NULL)
		{
			Error=NOMEMORY;
			return -1;
		}
		return ind;			// don't mark as used until
						// opened
	}
}






int	writetifdr(int handle)
{
	struct tifile *ff=&tifiles[handle];

//	ff->changed=0;
	lseek(ff->doshandle,
			(ff->doad ? ff->fdrsec*256L : 0),
			SEEK_SET);

	if (ff->doad)
		writedoadptrs(handle);

	if (write(ff->doshandle,&ff->fdr,ff->fdrsecsize)==ff->fdrsecsize)
		return 1;
	else
	{
		Error=NOSPACE;
		return 0;
	}
}


int	writetisector(int handle)
{
	struct tifile *ff=&tifiles[handle];
	word	sec;
	longint offs;

	sec=(ff->doad==0  ? ff->secnum : ff->links[ff->secnum]);

	offs=sec*256L + (ff->doad ? 0 : 128);

	if (lseek(ff->doshandle,offs,SEEK_SET)!=offs)
	{
		Error=BADSEEK;
		return 0;
	}

	if (write(ff->doshandle,ff->cursec,256)==256)
	{
		ff->changed=0;
		return 1;
	}
	else
	{
		Error=NOSPACE;
		return 0;
	}
}




int	updateti(int handle)
{
	struct tifile *ff=&tifiles[handle];

	if (ff->changed)
	{
		if (!writetifdr(handle))
			return 0;

		if (!writetisector(handle))
			return 0;
	}

	return	1;
}




int	readtisector(int handle)
{
	struct tifile *ff=&tifiles[handle];
	word	sec;
	longint offs;

	sec=(ff->doad==0) ? ff->secnum : ff->links[ff->secnum];

	offs=sec*256L + (ff->doad ? 0 : 128);

	if (lseek(ff->doshandle,offs,SEEK_SET)!=offs)
	{
		Error=BADSEEK;
		return 0;
	}


	if (read(ff->doshandle,ff->cursec,256)==256)
	{
		Error=0;
		return 1;
	}
	else
	{
		Error=BADREAD;
		return 0;
	}
}


//	Execute before reading...
//
//
int	getnexttisector(int handle)
{
	return readtisector(handle);
}


//	Execute before writing...
//
//
int	getnewtisector(int handle)
{
	struct tifile *ff=&tifiles[handle];
	word	next;

	if (ff->doad==0)
	{
		return 1;
	}
	else
	{
		next=getfreetisector(ff,0);
		if (next)
		{
			ff->links[ff->secnum]=next;
		}
		return	next;
	}
}



int	writenewtiblock(int handle, byte *buf)
{
	struct tifile *ff=&tifiles[handle];

	if (ff->changed)
	{
		if (!updateti(handle))
			return 0;

		ff->secnum++;
	}

	if (!getnewtisector(handle))
		return 0;

	ff->fdr.secsused=swapbytes(swapbytes(ff->fdr.secsused)+1);
	memcpy(ff->cursec,buf,256);
	ff->changed=1;
}



int	writeti(int handle, byte *buf, byte len)
{
	struct tifile *ff=&tifiles[handle];

	if ((ff->fdr.flags&F_VARIABLE)!=0)
	{
		if ((word)ff->secoffs+(word)len+1 >= 255)
		{
			if (!updateti(handle))
				return 0;

			ff->secoffs=0;
			ff->secnum++;
		}

		if (ff->secoffs==0)
		{
			if (getnewtisector(handle))
			{
				ff->fdr.fixrecs++;
				ff->fdr.secsused=swapbytes(swapbytes(ff->fdr.secsused)+1);
			}
			else
				return 0;
		}

		ff->cursec[ff->secoffs]=len;
		memcpy(ff->cursec+ff->secoffs+1,buf,len);
		ff->secoffs+=len+1;
		ff->fdr.eof=ff->secoffs;
		ff->cursec[ff->secoffs]=0xff;
		ff->changed=1;
	}
	else
	{

		if (ff->secoffs/ff->fdr.reclen>=ff->fdr.recspersec)
		{
			if (!updateti(handle))
				return 0;
			ff->secoffs=0;
			ff->secnum++;
		}

		if (ff->secoffs==0)
		{
			if (getnewtisector(handle))
				ff->fdr.secsused=swapbytes(swapbytes(ff->fdr.secsused)+1);
			else
				return 0;
		}

		len=ff->fdr.reclen;
		memcpy(ff->cursec+ff->secoffs,buf,len);
		ff->secoffs+=len;
		ff->fdr.fixrecs++;
		ff->fdr.eof=ff->secoffs;
		ff->changed=1;
	}
	return	1;
}


int	readnexttiblock(int handle, byte *buf)
{
	struct tifile *ff=&tifiles[handle];

	if (!getnexttisector(handle))
		return 0;

	memcpy(buf,ff->cursec,256);
	ff->secnum++;
}



int	readti(int handle, byte *buf, byte len)
{
	struct tifile *ff=&tifiles[handle];
	byte	reallen;
	byte	*ptr;
	byte	ch;

	*buf=0;
	if (tieof(handle))
		return 0;

	if ((ff->fdr.flags&F_VARIABLE)!=0)
	{
		if (ff->secoffs==0)
			if (!getnexttisector(handle))
				return 0;

		reallen=ff->cursec[ff->secoffs];
		if (reallen>=len)
		{
			Error=BADREAD;
			return 0;
		}
		memcpy(buf,ff->cursec+ff->secoffs+1,reallen);
		ptr=buf;
		while (ptr<buf+reallen)
		{
			ch=*ptr;
			*ptr++=(ch ? ch : 0x20);
		}
		*ptr=0;

//		if (strcmp(buf," ")==0)
//			*buf=0;

		ff->secoffs+=reallen+1;
		if (ff->secoffs>255)		/* read an overlong record? */
		{
			Error=BADREAD;
			return 0;
		}

		if (ff->cursec[ff->secoffs]==0xff)
		{
			ff->secoffs=0;
			ff->secnum++;
		}
	}
	else
	{
		if (ff->secoffs==0)
			if (!getnexttisector(handle))
				return 0;

		len=ff->fdr.reclen;
		memcpy(buf,ff->cursec+ff->secoffs,len);
		buf[len]=0;
		ff->secoffs+=len;
		if (ff->secoffs/ff->fdr.reclen>=ff->fdr.recspersec)
		{
			ff->secoffs=0;
			ff->secnum++;
		}

	}
	return	1;
}


int	tieof(int handle)
{
	unsigned long offs,len;

	offs=tigetcurpos(handle);
	len=tigetfilesize(handle);

	return (offs>=len);
}


int	closeti(int handle)
{
	struct tifile *ff=&tifiles[handle];

	if (ff->changed)
	{
		if (!updateti(handle))
			return 0;
		if (writetifdr(handle)==0)
			return 0;
	}
	ff->inuse=0;
	if (ff->links)
	{
		farfree(ff->links);
		ff->links=NULL;
	}
	close(ff->doshandle);
	return 1;
}


void	tigettype(int handle, byte *type, byte *len)
{
	struct tifile *ff=&tifiles[handle];

	*type=ff->fdr.flags;
	*len=ff->fdr.reclen;
}


longint	tigetfilesize(int handle)
{
	struct tifile *ff=&tifiles[handle];
	longint	len;

	len=(longint)(swapbytes(ff->fdr.secsused) - (ff->fdr.eof!=0))*256L
		+ff->fdr.eof;
	return len;

}

longint	tigetcurpos(int handle)
{
	struct tifile *ff=&tifiles[handle];
	longint	offs;

	offs=(ff->secnum*256L)+ff->secoffs;
	return offs;
}


longint tigetrealfilesize(int handle)
{
	struct tifile *ff=&tifiles[handle];
	int	index;

	if (ff->doad)
	{
		index=0;
		while (ff->links[index])
			index++;
		return (index*256L);
	}
	else
		return filelength(ff->doshandle)-128;
}


/////////////////////////////////////////////////////////////////


/*
	Decide whether a given filename refers to a FIAD or a DOAD.
	It is assumed that "filename" is _not_ a directory, because
	the function will return "name" as the directory name.
*/
int	fiadordoad(char *filename,
		   int *doad,
		   char *pathordisk,
		   char *name)
{
	char	temp[128];
	char	path[80];
	char	dname[14];
	char	fname[14];
	char	*tmp;
	char	dummy[6];
	struct	stat st;

	strcpy(temp,filename);
	dummy[0]=temp[0];
	dummy[1]=0;
	strcat(dummy,".DSK");

	if (
	    ((tmp=strchr(temp,':'))!=NULL &&
	    strchr(tmp+1,':')!=NULL)			// two colons:
							// a:\disk:file
	    ||
	    (temp[1]==':' && !stat(dummy,&st))		// a:file
							// "a" exists
	    ||
	    ((tmp=strchr(temp,':'))!=NULL &&
	     tmp!=temp+1)				// disk:file
	   )
	{
		*doad=1;
		tmp=strrchr(temp,':');			// get last colon
		if (*(tmp+1)!='\\')
			strcpy(fname,tmp+1);		// get TI filename
		else
			strcpy(fname,tmp+2);		// ignore disk:\wow

		fname[10]=0;
		fix10(fname,fname);			// make FDR-able
		*tmp=0;
		if (!split(temp,path,dname))
			return 0;
		strcat(path,dname);			// stickemtogether
		tmp=strrchr(path,'\\');
		if (tmp && strrchr(path,'.')<tmp)
			strcat(path,".DSK");		// attach default .DSK

		if (strpbrk(fname,"\\:"))
		{
			Error=BADPATH;
			return 0;
		}
	}
	else
	{
		*doad=0;				// a FIAD
		if (split(temp,path,fname)==0)
			return 0;
		if (temp[strlen(temp)-1]=='\\')		// just a directory?
		{
			strcat(path,fname);
			strcpy(fname,"");
		}
	}

	strupr(path);
	strupr(fname);
	strcpy(pathordisk,path);
	strcpy(name,fname);

	return 1;

}


int	openti(char *filename, int *handle)
{
	char	path[80];
	char	name[14];
	int	doad;

	if (fiadordoad(filename,&doad,path,name))
	{
		if (!doad)
		{
			strcat(path,name);
			return openfiad(filename,handle);
		}
		else
			return opendoad(path,name,handle);
	}
	else
		return 0;

}


int	createti(char *filename, byte type, byte reclen, int *handle)
{
	char	path[80];
	char	name[14];
	int	doad;

	if (fiadordoad(filename,&doad,path,name))
	{
		if (!doad)
		{
			strcat(path,name);
			return createfiad(filename,type,reclen,handle);
		}
		else
			return createdoad(path,name,type,reclen,handle);
	}
	else
		return 0;

}





int	whichwild;
struct	ffblk ffdos;
char	ffdospath[64];

int	wilddosinit(char *pathname, char *wildcard, char *first)
{
	char	temp[80];

	strcpy(ffdospath,pathname);
	strcpy(temp,pathname);
	strcat(temp,wildcard);

	if (findfirst(temp,&ffdos,0))
		return 1;

	strcpy(first,ffdospath);
	strcat(first,ffdos.ff_name);
	return 0;
}


int	wilddos(char *first)
{
	if (findnext(&ffdos))
		return 1;

	strcpy(first,ffdospath);
	strcat(first,ffdos.ff_name);
	return 0;
}


int	wildinit(char *filename, char *first, int ti)
{
	char	pathname[64];
	char	wildcard[14];

	char	temp[80];
	char	dir[64];
	char	ext[4];

	if (!ti)
	{
		if (!_fullpath(temp,filename,80))
			return 1;

                whichwild=-1;
		fnsplit(temp,pathname,dir,wildcard,ext);
		strcat(pathname,dir);
		strcat(wildcard,ext);
		return wilddosinit(pathname,wildcard,first);
	}

	if (filename[strlen(filename)-1]=='\\')
		strcat(filename,"*");

	if (!fiadordoad(filename,&whichwild,pathname,wildcard))
		return 1;

	if (whichwild)
		return wilddoadinit(pathname,wildcard,first);
	else
		return wildfiadinit(pathname,wildcard,first);
}


int	wild(char *next)
{
	if (whichwild<0)
		return wilddos(next);
	else
	if (whichwild)
		return wilddoad(next);
	else
		return wildfiad(next);

}




int	tiexists(char *filename)
{
	int	handle;

	if (openti(filename,&handle))
	{
		closeti(handle);
		return 1;
	}
	else
		return 0;

}


int	deleteti(char *filename)
{
	int	doad;
	char	diskname[64];
	char	name[14];

	if (!fiadordoad(filename,&doad,diskname,name))
		return 0;

	if (doad)
		return deletedoad(diskname,name);
	else
		return deletefiad(filename);

}

/*
#include "dosfiles.h"


void	main(int argc, char **argv)
{
	int	handle;
	byte	type,reclen;
	int	arg;
	char	fullname[80];

	for (arg=1; arg<argc; arg++)
	{
		while (getfilename(argv[arg],fullname)!=NULL)
		{
			printf("%s\n",fullname);
		}

	}
}


*/
