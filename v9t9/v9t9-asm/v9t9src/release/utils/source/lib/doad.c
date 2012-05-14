/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Disk-on-a-disk routines.

	These routines are only add-ons to FOAD.C.

	int opendisk(char *filename, int *handle);
	int createdisk(char *filename, byte tracks, byte secs, byte sides, int *handle);
	int readsector(int handle, int num, char *buf);
	int writesector(int handle, int num, char *buf);
	int getdiskinfo(int handle, int *totsecs, int *usedsecs);
	int allocatesector(int handle);
	int deallocatesector(int handle);


*/

#include <ctype.h>
#include <dos.h>
#include <fcntl.h>
#include <io.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys\stat.h>

#include "doad.h"
#include "error.h"
#include "fdr.h"
#include "files.h"
#include "names.h"
#include "utypes.h"


/*
	This routine is to be used when accessing any file on
	an existing disk, usually inside DOAD routines.

*/
int	opendisk(char *filename, struct tifile *ff)
{

	strupr(filename);
	if ((ff->doshandle=open(filename,O_RDWR|O_BINARY))==-1)
	{
		Error=NOFILE;
		return 0;
	}
	else
	{
		if (!readsector(ff->doshandle,0,(byte *)&ff->dsk))
		{
			Error=BADDISK;
			close(ff->doshandle);
			return 0;
		}
		else
		{
			ff->dsk.id[3]=0;
			if (strcmp(ff->dsk.id,"DSK")!=0)
			{
				Error=BADDISK;
				close(ff->doshandle);
				return 0;
			}
			else
			if (swapbytes(ff->dsk.totsecs)>400*4)
			{
				Error=INCOMPAT;
				close(ff->doshandle);
				return 0;
			}
			return	1;

		}

	}


}

/*
	This routine simply creates a new disk image, and the handle,
	for some reason.
*/

int	createdisk(char *filename, char *dn, byte tracks, byte secs,
		   byte sides, byte dens, int *handle)
{
	byte 	buf[256];
	byte    sector[256];
	struct dskstruc *dsk=(struct dskstruc *)buf;
	int	c,s;
	word	total;

	if ((*handle=open(filename,O_RDWR|O_BINARY|O_CREAT|O_TRUNC,
			  S_IWRITE|S_IREAD))==-1)
	{
		Error=NOSPACE;
		return 0;
	}
	else
	{
		total=tracks*secs*sides;
		if (total>2880)
		{
			Error=INCOMPAT;
			return 0;
		}

		if (sides==2 && tracks!=40)
		{
			Error=BADSIDE;
			return 0;
		}

		for (c=0; c<tracks; c++)
		   for(s=sides-1; s>=0; s--)
			if (!formattrack(*handle,s,c,secs))
			{
				Error=NOSPACE;
				close(*handle);
				remove(filename);
				return 0;
			}

		memset((byte *)dsk,0,256);
		memcpy(dsk->name,dn,10);
		dsk->totsecs=swapbytes((word)tracks * (word)secs * (word) sides);
		dsk->secspertrack=secs;
		dsk->tracksperside=tracks;
		dsk->sides=sides;
		dsk->density=dens;
		strcpy(dsk->id,"DSK");
		memset(dsk->abm,0xff,200);
		memset(dsk->abm,0,swapbytes(dsk->totsecs)/8);
		dsk->abm[0]=3;

		memset(sector,0,256);

		if (!(writesector(*handle,0,buf) &&
		      writesector(*handle,1,sector)))
		{
			Error=NOSPACE;
			close(*handle);
			remove(filename);
			return 0;
		}
		else
			return	1;
	}
}


int	formattrack(int handle, byte side, byte track, byte secs)
{
	char 	sec[256];
	longint	offs;
	int	c;

	memset(sec,0xe5,256);

	offs=(word)(track+(side ? 40 : 0)) * (word)secs * 256L;
	lseek(handle,offs,SEEK_SET);

	for (c=0; c<secs; c++)
		if (write(handle,sec,256)!=256)
			return 0;
	return 1;
}


void	closedisk(int handle)
{
	close(handle);
}


int	readsector(int handle, word secnum, byte *buf)
{
	lseek(handle, secnum*256L, SEEK_SET);
	return (read(handle,buf,256)==256);
}


int	writesector(int handle, word secnum, byte *buf)
{
	lseek(handle, secnum*256L, SEEK_SET);
	return (write(handle,buf,256)==256);
}




int	getfreetisector(struct tifile *ff,int fdr)
{
	word	ind;
	byte	found;
	byte	curbyte;
	word	sec;
	byte	searched,total;

	total=(word)(swapbytes(ff->dsk.totsecs)/8);

	found=searched=0;
	if (fdr)
		ind=0;			// for FDRs, look at 0
	else
		ind=4;             	// for file sectors, start at 32
	while (searched<total && !found)
	{
		if ((curbyte=~ff->dsk.abm[ind])!=0)
		{
			sec=0;
			while (curbyte)
			{
				if (curbyte&1)
				{
				    ff->dsk.abm[ind]|=(1<<sec);     // used

				    sec+=(ind*8);
				    curbyte=0;
				    found=1;
				}
				else
				{
				    curbyte>>=1;
				    sec++;
				}
			}
		}
		ind++;
		if (ind>=total)
			ind=0;
		searched++;
	}

	if (!found)
	{
		Error=NOSPACE;
		return 0;
	}
	else
	{
		if (writesector(ff->doshandle,0,(byte *)&ff->dsk)!=0)
			return sec;
		else
			return 0;
	}

}


int	freetisector(struct tifile *ff,word secnum)
{
	word	ind;
	byte	curbyte;

	if (secnum>=swapbytes(ff->dsk.totsecs))
		return 0;

	ind=secnum/8;
	ff->dsk.abm[ind]&=~(1<<(secnum&7));

	if (writesector(ff->doshandle,0,(byte *)&ff->dsk)!=0)
		return 1;
	else
		return 0;
}

/*
	finddoadfdr will search for a "filename" and set "fdr" to
	the file's fdr, returning the fdr's sector.

*/
int	finddoadfdr(int handle,struct fdrstruc *fdr,char *filename)
{
	word 	sec1[128];
	int	ind;
	int	sec;


	if (readsector(handle,1,(byte *)sec1)==0)
		return 0;

	ind=0;

	if (sec1[ind]==0) ind++;		// hidden files

	while ((sec=swapbytes(sec1[ind]))!=0 && ind<128)
	{
		readsector(handle,sec,(byte *)fdr);
		if (memicmp(fdr->name,filename,10)==0)
			return sec;
		ind++;
	}
	return 0;
}

/*
	adddoadentry will insert a FDR's "secnum" into sector 1 of disk
	"handle".  "infdr" is used to determine ordering.

*/
int	adddoadentry(struct tifile *ff, int secnum)
{
	word	sec1[128];
	struct fdrstruc fdr;
	int	ind,hi,lo;
	int	sec;
	int	found;

	if (readsector(ff->doshandle,1,(byte *)sec1)==0)
		return 0;

	if (sec1[127]!=0)
	{
		Error=NOSPACE;
		return	0;			// no space
	}

	for (ind=127; ind>=0 && sec1[ind]==0; ind--)	;

	if (ind<0)
	{
		lo=hi=0;
		found=1;
	}
	else
	{
	    lo=0; hi=ind+1;
	    while (lo!=hi)
	    {
		    ind=(lo+hi)/2;
		    sec=swapbytes(sec1[ind]);
		    if (readsector(ff->doshandle,sec,(byte *)&fdr))
		    {
			    if (memicmp(fdr.name,ff->fdr.name,10)==0)
			    {
				    Error=EXISTS;	// handler should
				    return 0;		// delete file first
			    }

			    found=memicmp(ff->fdr.name,fdr.name,10);
			    if (found<0)
				hi=ind;
			    else
				lo=ind+1;
		    }
		    else
			    return 0;
	    }
	}

	ind=lo;
	memmove(&sec1[ind+1],&sec1[ind],(127-ind)*2);
	sec1[ind]=swapbytes(secnum);

	if (writesector(ff->doshandle,1,(byte *)sec1)==0)
		return 0;
	else
		return 1;
}


int	removedoadentry(struct tifile *ff, int secnum)
{
	word	sec1[128];
	struct fdrstruc fdr;
	int	ind,hi,lo;
	int	sec;
	int	found;

	if (readsector(ff->doshandle,1,(byte *)sec1)==0)
		return 0;

	for (ind=0; ind<128 && swapbytes(sec1[ind])!=secnum; ind++)	;

	if (ind>=128)
	{
		Error = NOTIFILE;
		return 0;
	}
	else
	{
		memmove(&sec1[ind],&sec1[ind+1],(127-ind)*2);
		sec1[127]=0;

		if (writesector(ff->doshandle,1,(byte *)sec1)==0)
			return 0;
		else
			return 1;
	}
}








int	opendoad(char *diskname, char *filename, int *handle)
{
	struct  tifile *ff;
	char	path[80];
	char	dosname[14];
	char	fdrname[12];
	int	ti;
	struct	fdrstruc *fdr;


	strupr(filename);
	if ((ti=*handle=getfreeti(1))!=-1)
	{
		ff=&tifiles[ti];
		fdr=&ff->fdr;

		if (opendisk(diskname,ff))
		{
		    fix10(filename,fdrname);
		    if ((ff->fdrsec=finddoadfdr(ff->doshandle,
						fdr,fdrname))!=0)
		    {
			    if ((fdr->flags&F_VARIABLE) ? fdr->reclen : 1)
			    {
				    ff->inuse=1;
				    readdoadptrs(ti);
					    Error=0;
					    return 1;
			    }
			    else
				    Error=NOTTIEMUL;

			    close(ff->doshandle);
		    }
		    else
		    {
			    Error=BADFILE;
			    return 0;
		    }
		}
		else
		{
			Error=BADDISK;
			return 0;
		}
	}
	return	0;
}






int	createdoad(char *diskname, char *filename,
		   byte type, byte reclen, int *handle)
{
	struct tifile *ff;
	int	ti;
	char	fdrname[12];

	deletedoad(diskname,filename);

	strupr(filename);
	if ((*handle=ti=getfreeti(1))!=-1)
	{
		ff=&tifiles[ti];

		if (opendisk(diskname,ff))
		    if ((ff->fdrsec=getfreetisector(ff,1))!=0)
		    {
			    fix10(filename,fdrname);
			    createtifdr(&ff->fdr,fdrname,type,reclen);
			    initdoadptrs(ti);
			    if (writetifdr(ti))
			    {
				ff->inuse=1;
				if (adddoadentry(ff,ff->fdrsec))
				{
					Error=0;
					return 1;
				}
			    }
		    }
		    else
			    Error=NOSPACE;
		else
			Error=BADDISK;
	}
	return 0;
}


int     initdoadptrs(int handle)
{
	struct tifile *ff=&tifiles[handle];

	if (ff->doad==0)
		return 0;

	_fmemset(ff->links,0,8192);
	memset(ff->fdr.lnks,0,256-28);
	return 1;
}


int	readdoadptrs(int handle)
{
	struct tifile *ff=&tifiles[handle];
	byte	offs;
	word	left;
	word	index;
	byte	rec[3];
	word	offsec,offlen,totoffs,lastsec;

	if (!ff->doad)
		return 0;

	left=swapbytes(ff->fdr.secsused);
	offs=0;
	index=0;
	totoffs=0;
	while (left)
	{
		memcpy(rec,&ff->fdr.lnks[offs],3);
		if ((rec[0]|rec[1]|rec[2])==0)
		{
			Error=BADFILE;
			return 0;
		}
		offs+=3;
		offsec=((rec[1]&15)<<8)|rec[0];
		offlen=((rec[2]<<4)|(rec[1]>>4))-totoffs;
		totoffs+=offlen+1;
		lastsec=offsec+offlen;
		while (offsec<=lastsec)
		{
			if (index>=8192 || offsec>=2880)
			{
				Error=BADFILE;
				return 0;
			}
			ff->links[index++]=offsec++;
			left--;
		}
	}
	return	1;
}


int     writedoadptrs(int handle)
{
	struct tifile *ff=&tifiles[handle];

	byte	offs;
	word	left;
	word	index;
	byte	rec[3];
	word	offsec,offlen,totoffs,lastsec;

	if (!ff->doad)
		return 0;

	memset(ff->fdr.lnks,0,256-28);

	left=swapbytes(ff->fdr.secsused);
	offs=0;
	index=0;
	totoffs=0;
	offlen=0xffff;
	while (offlen+1!=left)
	{
		offsec=ff->links[index++];
		totoffs=1;
		offlen++;
		if (!offsec)
		{
			printf("EMULATE.LIB:  FILES.C:  Line 397:  Internal error\n");
			Error=BADFILE;
			return 0;
		}
		while (ff->links[index]==offsec+totoffs)
		{
			offlen++;
			index++;
                        totoffs++;
		}
		rec[0]=offsec&255;
		rec[1]=((offlen&15)<<4) | ((offsec&0xf00)>>8);
		rec[2]=(offlen&0xff0)>>4;

		if (offs>=256-28)
		{
			Error=NOSPACE;
			return 0;
		}

		memcpy(&ff->fdr.lnks[offs],rec,3);
		offs+=3;
	}
	return	1;
}



int	deletedoad(char *diskname, char *filename)
{
	struct 	tifile *ff;
	int	ti;
	int	sec;
	word	offs;

	if (opendoad(diskname,filename,&ti))
	{
		ff=&tifiles[ti];
		removedoadentry(ff,ff->fdrsec);
		freetisector(ff,ff->fdrsec);
		offs=0;
		while ((sec=ff->links[offs])!=0)
		{
			freetisector(ff,sec);
			ff->links[offs++]=0;
		}
		closeti(ti);
		Error=0;
		return 1;
	}
	else
		return 0;

}


word	ffsec1[128];
int	ffindex;
char	dffpath[80];
char	dffwild[14];

/*int	fiadordoad(char *filename,
		   int *doad,
		   char *pathordisk,
		   char *name)*/


int	readonesector(char *diskname, int sector, byte *buf)
{
	struct	tifile ffdisk;

	if (!opendisk(diskname,&ffdisk))
		return 0;

	if (!readsector(ffdisk.doshandle,sector,buf))
		return 0;

	closedisk(ffdisk.doshandle);

	return 1;
}

/*
	Initiate a wildcard search.
	Only ONE can be occurring at one time.

	Return 1 if the search is finished.
*/
int	wilddoadinit(char *diskname, char *wildcard, char *first)
{
	int	doad;

	strcpy(dffpath,diskname);
	strcat(dffpath,":");
	strcpy(dffwild,wildcard);

	if (*dffwild==' ')
		*dffwild='*';

	if (!readonesector(diskname,1,(byte *)ffsec1))
		return 1;

	ffindex=0;
	return	wilddoad(first);
}


int	match(word sector,char *from, char *to)
{
	struct	dskstruc dsk;
	byte	index;
	byte	unmatch;

	if (!readonesector(dffpath,sector,(byte *)&dsk))
		return 0;

	index=0;
	unmatch=0;
	while (index<10 && !unmatch)
	{
		if (from[index]!='?')
			if (from[index]=='*')
				index=10;
			else
				unmatch=toupper(from[index])!=
					toupper(dsk.name[index]);
		index++;
	}
	if (unmatch)
		return 0;

	to[10]=0;
	memcpy(to,dsk.name,10);
	return 1;

}


int	wilddoad(char *name)
{
	word	sec;
	char	filename[14];

	while ((sec=swapbytes(ffsec1[ffindex++])) && !match(sec,dffwild,filename))
		;

	if (!sec)
		return 1;

	strcpy(name,dffpath);
	strcat(name,filename);
	return 0;
}
