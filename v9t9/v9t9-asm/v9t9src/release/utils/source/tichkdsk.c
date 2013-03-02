/*
  tichkdsk.c

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
	This utility will check the structure of FIADs or DOADs.

	Mainly, the errors to fix are v5.01 bugs:

	1)	All xxx/FIX files created by the emulator have
		a 0 in their sectors-used field.

	2)	All xxx/VAR files created have double the sectors
		in the field.

	3)	Filenames.  The characters -, ~, !, (, and ) are
		legal in DOS.  Fix these DOS filenames.

	4)	Some goofy files have the extended characters in
		their FDRs.  Maybe an early-development bug, but
		may still affect people.  No extended characters
		may exist inside a FIAD FDR.

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
char	fix=0;

#define	SAY if (verbose)


/*
	Validatename will check that the name in the FDR is legal.
	If not, and "fix==1", we can fix it.
*/
int	validatename(byte *buf)
{
	int	bad=0;
	int	index;
	byte	temp[12];

	memcpy(temp,buf,10);
	temp[10]=0;
	index=9;
	while (index>=0 && temp[index]==0x20)
		temp[index--]=0;

	if (strchr(temp,' ')!=NULL || strchr(buf,'\0')<buf+10)
	{
		bad=1;
		printf("Illegal spaces in name (%10s).\n",temp);
	}

	memcpy(temp,buf,10);
	index=0;
	while (index<10 && temp[index]<0x80)
		index++;

	if (index<10)
	{
		printf("Illegal characters in name (%10s).\n",temp);
		bad=1;
	}

	if (strpbrk(temp,"abcdefghijklmnopqrstuvwxyz")!=NULL)
	{
		printf("Name has lower-case characters (%10s).\n"
		       "V9t9 can stand it, but unexplainable errors may arise.\n",temp);
		strupr(buf);
	}

	return bad==0;
}


int	getname(char *old, char *nw)
{
	int	index;
	do
	{
		printf("Enter a correct name for \"%10s\", up to 10 characters:\n\n: ",
			old);
		nw[0]=10;
		cgets(nw);
		strupr(nw+2);
		printf("\n");
		for (index=strlen(nw+2); index<10; index++)
			nw[index+2]=0x20;
		nw[12]=0;
	}	while (!validatename(nw+2));

        return	1;
}


int	scandisk(char *filename)
{
	char	diskname[64];
	char	name[14];
	int	doad;
	struct	dskstruc buf0;
	char	buf[256];
	struct	tifile dsk;

	if (!fiadordoad(filename,&doad,diskname,name))
	{
		printf("Cannot access directory/disk image %s\n",filename);
		return 0;
	}

	if (doad)
	{
		printf("Scanning disk image %s...\n",diskname);

		//  Fatal errors

		if (!opendisk(diskname,&dsk))
		{
			tierror(diskname);
			return 0;
		}

SAY		printf("\nChecking volume header...\n\n");
		if (!readsector(dsk.doshandle,0,(byte *)&buf0))
		{
			tierror(diskname);
			return 0;
		}
		else
SAY			printf("Okay header.\n");

		//  Recoverable errors

		if (!validatename(buf0.name))
		{
			if (fix)
			{
			getname(buf0.name,name);
			memcpy(buf0.name,name+2,10);
			if (!writesector(dsk.doshandle,0,(byte *)&buf0))
				die(filename);
			}
		}

		//  No more useful testing for DOADs yet!!
	}
	else
		printf("Scanning directory %s...\n",filename);

	return	1;
}


/*
	Get yes or no (Y/N), return 1 for Y.
*/
int	getyesorno(void)
{
	char	ch;

	do	ch=toupper(getch());  while (ch!='Y' && ch!='N');
	return	ch=='Y';
}


/*
	Truncate a FIAD file at secsused.
*/
int	trunc(int handle, word len)
{
	struct	tifile *ff=&tifiles[handle];
	unsigned temp;

	lseek(ff->doshandle,len*256L+128,SEEK_SET);
	_dos_write(ff->doshandle,0l,0,&temp);

        return	1;
}


int	scan(char *filename)
{
	struct	tifile *ff;

	int	handle;
	longint	fdrtotalbytes;
	longint	totalbytes;
	longint	databytes;
	struct	fdrstruc *fdr;
	char	name[14];
	word	oldsize;

	struct	ftime ftime;

	byte	type,len;

SAY	printf("Scanning file %s\n",filename);

	if (!openti(filename,&handle))
	{
		tierror(filename);
		return 0;
	}


	ff=&tifiles[handle];
	getftime(ff->doshandle,&ftime);

	//  Check FDR structure.

	fdr=&ff->fdr;
	type=fdr->flags;
	len=fdr->reclen;

	if (!validatename(fdr->name))
	{
		if (fix)
		{
		getname(fdr->name,name);
		memcpy(fdr->name,name+2,10);
		}
	}


	if (!(type&F_PROGRAM) && len==0)
	{
		printf("Invalid record length (0)\n");

		if (fix)
		{

		    if (fdr->recspersec==1)
			    len=255;	// good guess for xxx/FIX
		    else
		    if (fdr->recspersec>1)
			    len=256/fdr->recspersec; // very approx
		    else
			    len=80;		// pure guess

		    //  For fixed files, we can tell what the record size is:
		    //  *  EOF offset is exact multiple
		    //  *  (256*secsused-EOF)/fixrecs is almost it
		    if (!(type&F_VARIABLE) && fdr->fixrecs)
		    {
			len=(256L*(swapbytes(fdr->secsused)+1)-fdr->eof) /
			   fdr->fixrecs;
			if (fdr->eof)
				while (len%fdr->eof)
					len--;
		    }

		    if (!len)
			len=80;

		    printf("\tAssume %d bytes?\n",len);
		    if (getyesorno())
			    fdr->reclen=len;

		}

	}


	fdrtotalbytes=tigetfilesize(handle);
	totalbytes=tigetrealfilesize(handle);

	if ((fdrtotalbytes+255)/256!=totalbytes/256)
	{
		printf("File size mismatch in %s\n",filename);
		printf("TI Emulator! v5.0- probably made this error.\n");

		if (fix)
		{
		if ((type&F_VARIABLE) && !(type&F_PROGRAM))	// xxx/VAR
		{
			fdr->fixrecs=totalbytes/256;
			fdr->secsused=swapbytes(fdr->fixrecs);
		}
		else if (type&F_PROGRAM)			// program
		{
		//  On a program file, most likely the FDR is
		//  correct while the filesize is wrong.  Sheesh!
		//
		    if (fdrtotalbytes<totalbytes)
		    if (!ff->doad)
			trunc(handle,swapbytes(fdr->secsused));

		    else ;
		    else
		    {
			fdr->secsused=swapbytes(totalbytes/256);
			fdr->eof=totalbytes&255;
		    }
		}
		else if (!(type&F_VARIABLE))			// xxx/FIX
		{
		//	reclen is assumed to be legal from above
		//
			oldsize=fdr->secsused;
			fdr->secsused=swapbytes(
			    (fdr->fixrecs+fdr->recspersec-1)/fdr->recspersec);
			if (oldsize==fdr->secsused && !ff->doad)
				trunc(handle,swapbytes(oldsize));
		}
		}
	}

	if (fix)
	{
//		printf("Fixing...\n");
		if (!writetifdr(handle))
			printf("Error!  Couldn't write header!\n");
		else
			setftime(ff->doshandle,&ftime);
	}
	closeti(handle);

	return	1;
}



void	help(void)
{
		printf("TICHKDSK Directory/Disk Checker v1.0\n"
		       "\n"
		       "Usage:   TICHKDSK [options] { <directory> | <disk image:> }\n"
		       "\n"
		       "TICHKDSK will scan directories or disk image files for errors and fix them\n"
		       "if the /F option is provided.\n"
		       "\n"
		       "Options:\n"
		       "\t\t/F\t-- fix errors as well as reporting them\n"
		       "\t\t/V\t-- verbose operation\n"

		       );
		exit(0);

}


int	main(int argc, char **argv)
{
	char	temp[80];
	char	filetocheck[80];
	char	opt;

	if (paraminit(0,temp)<=0)
		help();


	while ((opt=getopt())!=0)
	{
	switch (opt)
	{
	case	'F':	fix=1;
			break;
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
	{
		if (isdir(temp))
		{
			makedirof(temp);
			scandisk(temp);
		}
		while (getfilename(temp,filetocheck,1))
			scan(filetocheck);
	}

	return 0;
}

