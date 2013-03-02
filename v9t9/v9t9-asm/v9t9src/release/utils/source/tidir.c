/*
  tidir.c

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
	Simple stupid utility to get a directory of a FIAD or DOAD.
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


#define	LISTMAX	1024
struct	listrec
{
	byte	name[12];
	byte	flags;
	byte	reclen;
	word	fdrsec;
	word	secsused;
	longint	bytesused;
	word	fixrecs;
}	far *thelist[LISTMAX];

int	thelistlen;

char	tolist=1;
char	sort=0;


void	initlist(void)
{
	memset(thelist,0,sizeof(thelist));
	thelistlen=0;
}

void	header(void)
{
	int	x;

	if (tolist==0)
/*		for (x=0; x<7; x++)
			printf("%-10s ","Filename")*/
			;
	else
	if (tolist==1)
		for (x=0; x<2; x++)
		printf("%-10s  %-4s  %-7s  %-3s\t\t","Filename","Secs",
					     "Type",
					     "Len");
	else
	if (tolist==2)
		printf("%-10s  %-4s  %-7s  %-3s  %-8s  %-5s  %-5s",
				"Filename","Secs","Type",
				"Len","Bytesize","#recs","FDRsec");
	printf("\n");
}


int	list(char *filename)
{
	int	handle;
	struct	tifile *ff;
	struct	listrec far *l;
	struct	listrec lr;

	if (thelistlen>=LISTMAX)
	{
		printf("Too many files in directory!  Maximum %d accepted.\n",
			LISTMAX);
		return 0;
	}

	l=farmalloc(sizeof(struct listrec));
	if (l==NULL)
	{
		printf("Ran out of memory for filelist!\n");
		return 0;
	}

	thelist[thelistlen]=l;

	if (!openti(filename,&handle))
	{
		tierror(filename);
		return 1;			/* not terminal err */
	}

	ff=&tifiles[handle];
	memcpy(lr.name,ff->fdr.name,10);
	lr.name[10]=0;
	tigettype(handle,&lr.flags,&lr.reclen);
	lr.secsused=swapbytes(ff->fdr.secsused)+(ff->doad!=0);
	lr.bytesused=tigetfilesize(handle);
	if (!(lr.flags&F_VARIABLE))
		lr.fixrecs=ff->fdr.fixrecs;
	else
		lr.fixrecs=0;
	if (ff->doad)
		lr.fdrsec=ff->fdrsec;
	else
		lr.fdrsec=0;

	_fmemcpy(l,&lr,sizeof(struct listrec));
	thelistlen++;
	closeti(handle);
	return 1;
}


word	countfree(byte *bitmap, int bytes)
{
	int	index=0;
	word	total=0;
	byte	one;

	while (index<bytes)
	{
		one=~bitmap[index];
		while (one)
		{
			if (one&0x80)
				total++;
			one+=one;
		}
		index++;
	}
	return	total;
}


int	listdisk(char *diskname)
{
	int	doad;
	char	pathname[64];
	char	lastpart[16];
	char	volname[12];
	struct	tifile ff;
	word	free,total;

	if (!fiadordoad(diskname,&doad,pathname,lastpart))
	{
		printf("Couldn't access directory/disk image %s!\n",diskname);
		return 0;
	}

	printf("\nDirectory of %s\n",diskname);
	if (doad)
	{
	    if (!opendisk(pathname,&ff))
	    {
		printf("Couldn't open disk image %s!\n",pathname);
		return 0;
	    }
	    else
	    {
		total=swapbytes(ff.dsk.totsecs);
		free=countfree(ff.dsk.abm,total/8);
		memcpy(volname,ff.dsk.name,10);
		volname[10]=0;
		printf("Volume name is  %10s\n",volname);
		printf("Total: %5d\tFree: %5d\n\n",total,free);
		closedisk(ff.doshandle);
	    }
	}

	header();
	return	1;
}


/**********************************************/

int	uplistsortname(const void *a, const void *b)
{
	struct 	listrec far **l1=(struct listrec far **)a;
	struct 	listrec far **l2=(struct listrec far **)b;

	return (_fmemicmp((*l1)->name,(*l2)->name,10));
}

int	downlistsortname(const void *a, const void *b)
{
	struct 	listrec far **l1=(struct listrec far **)a;
	struct 	listrec far **l2=(struct listrec far **)b;

	return (_fmemicmp((*l2)->name,(*l1)->name,10));
}

int	uplistsortsize(const void *a, const void *b)
{
	struct 	listrec far **l1=(struct listrec far **)a;
	struct 	listrec far **l2=(struct listrec far **)b;

	return (*l1)->bytesused-(*l2)->bytesused;
}

int	downlistsortsize(const void *a, const void *b)
{
	struct 	listrec far **l1=(struct listrec far **)a;
	struct 	listrec far **l2=(struct listrec far **)b;

	return (*l2)->bytesused-(*l1)->bytesused;
}

int	uplistsorttype(const void *a, const void *b)
{
	struct 	listrec far **l1=(struct listrec far **)a;
	struct 	listrec far **l2=(struct listrec far **)b;

	return (*l1)->flags-(*l2)->flags;
}

int	downlistsorttype(const void *a, const void *b)
{
	struct 	listrec far **l1=(struct listrec far **)a;
	struct 	listrec far **l2=(struct listrec far **)b;

	return (*l2)->flags-(*l1)->flags;
}


int	gettypeindex(byte flags)
{
	int	conv;

	switch (flags&(F_VARIABLE|F_PROGRAM))
	{
	case	0 :
		conv=1;
		break;
	case	1 :
		conv=4;
		break;
	case	2 :
		conv=3;
		break;
	case	0x80 :
		conv=0;
		break;
	case	0x82 :
		conv=2;
		break;
	default :
		conv=5;
	}
	return	conv;
}


void	display(struct listrec far *l, int x)
{
static	char	*types[]=
       {"DIS/VAR","DIS/FIX","INT/VAR",
	"INT/FIX","PROGRAM","UNKNOWN"};

	int	online;

	if (tolist==0)
	{
		printf("%10Fs ",l->name);
		online=7;
	}
	else
	if (tolist==1)
	{
		printf("%10Fs  %4d  %7s  ",l->name,l->secsused,
					     types[gettypeindex(l->flags)]);
		if (!(l->flags&F_PROGRAM))
			printf("%3u\t",l->reclen);
		else
			printf("%3s\t","");
		online=2;
	}
	else
	if (tolist==2)
	{
		printf("%10Fs  %4d  %7s  ",l->name,l->secsused,
					     types[gettypeindex(l->flags)]);
		if (!(l->flags&F_PROGRAM))
			printf("%3u  ",l->reclen);
		else
			printf("%3s  ","");

		printf("%8ld  ",l->bytesused);
		if (!(l->flags&(F_VARIABLE|F_PROGRAM)))
			printf("%5d  ",l->fixrecs);
		else
			printf("%5s  ","");

		if (l->fdrsec)
			printf("%5d",l->fdrsec);
		else
			printf("%5s","");
		online=1;
	}

	if ((x+1)%online==0)
		printf("\n");
	else
		if (tolist)
			printf("\t");
}


int	displaylist(void)
{
	static	int (*upsorters[]) (const void *, const void *) =
	{
		NULL,
		uplistsortname,
		uplistsortsize,
		uplistsorttype
	};

	static	int (*downsorters[]) (const void *, const void *) =
	{
		NULL,
		downlistsortname,
		downlistsortsize,
		downlistsorttype
	};

	int	x;
	int	lines;

//	First, sort the list.

	if (sort)
	qsort(thelist,thelistlen,sizeof(thelist[0]),
		(sort>0 ? upsorters[sort] : downsorters[-sort]));

	x=0;
	while (x<thelistlen)
	{
		display(thelist[x],x);
		x++;
	}
	printf("\n\n");

	return 1;
}

int	loselist(void)
{
	int	x;

	for (x=0; x<thelistlen; x++)
	{
		farfree(thelist[x]);
		thelist[x]=NULL;
	}

	return 1;
}


void	help(void)
{
		printf("TIDIR V9t9 Directory Lister v1.0\n"
		       "\n"
		       "Usage:   TIDIR [options] { <file> | <directory> | <disk image:> }\n"
		       "\n"
		       "TIDIR will print the listing of files in emulated directories\n"
		       "or disk images.\n"
		       "\n"
		       "Options:\n"
		       "\t\t/L\t-- long format\n"
		       "\t\t/W\t-- wide format\n"
		       "\t\t/O[-]x\t-- sort by Name, Size, or Type.\n"
		       "\t\t\t   \"-\" means descending.\n"
		       "\n"
		       );
		exit(0);

}


int	main(int argc, char **argv)
{
	char	temp[80];
	char	filetolist[80];
	char	sortopts[80];
static	char	*legalsorts="NST";
	char	*whichsort;
	int	sortchar;
	char	opt;
	int	oneheader=0;
	int	bad;

	if (paraminit(0,temp)<=0)
		help();

	while ((opt=getopt())!=0)
	{
	switch (opt)
	{
	case	'?':
	case	'H':	help();
			break;
	case	'W':	tolist=0;
			break;
	case	'L':	tolist=2;
			break;
	case	'O':	getoptstr(sortopts,1);
			if (sortopts[0]=='-')
				sortchar=1;
			else
				sortchar=0;

			if (sortopts[sortchar+1])
			{
				printf("Too many letters in /O parameter.\n");
				exit(1);
			}
			whichsort=strchr(legalsorts,toupper(sortopts[sortchar]));
			if (whichsort==NULL)
			{
				printf("Unknown sort option in /O parameter.\n");
				exit(1);
			}
			sort=whichsort-legalsorts+1;
			if (sortchar==1)
				sort=-sort;
			break;

	default:	printf("Unknown option '%c'\n",opt);
			exit(1);
	}
	}


	while (getparam(temp)!=NULL)
	{
		bad=0;
		if (isdir(temp))
		{
			bad=!listdisk(temp);
			makedirof(temp);
			oneheader=1;
		}

		if (!bad)
		{
			if (!oneheader)
			{
			header();
			oneheader=1;
			}

			initlist();
			while (getfilename(temp,filetolist,1) && list(filetolist))
				;
			displaylist();
			loselist();
		}
	}

	return 0;
}

