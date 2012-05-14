/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
#include <memory.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys\stat.h>

#include "fdr.h"
#include "files.h"

#define	END(x) (*((x)+strlen(x)-1))

char	*getstring(char *buf, int len, FILE *fptr, int fixed, byte nul)
{

	char	ch;
	int	x;

	flushall();

	memset(buf,0,len+1);
	fgets(buf,len,fptr);

	while (  (*buf) &&
		 ( (ch=END(buf)) =='\n' || ch=='\r')  )
		if (*buf)
			END(buf)=0;

	if (fixed)
		memset(buf+strlen(buf),nul,len-strlen(buf));

	return buf;
}




int	exists(char *filename)
{
	struct stat st;

	if (stat(filename,&st))
		return 0;
	else
		return 1;
}


int	isdir(char *filename)
{
	struct	stat st;
	char	path[64];
	char	name[14];
	int	doad;

	if (strchr(filename,'*')!=NULL)
		return 0;

	if (!stat(filename,&st) && (st.st_mode&S_IFDIR))
		return 1;

	if (!fiadordoad(filename,&doad,path,name))
		return 0;

	if (doad && *name==' ')
		return 1;

	return 0;

}


#define	BUFSIZE 2048
byte	buffer[BUFSIZE];
int	buflen,bufpos;

FILE*	openbufferwrite(char *filename)
{
	FILE* t;

	t=fopen(filename,"wb");

	if (t!=NULL)
		buflen=bufpos=0;

	return t;
}


int	writeflush(FILE *fptr)
{
	if (fwrite(buffer,1,buflen,fptr)==buflen)
	{
		buflen=bufpos=0;
		return 1;
	}
	else
	{
		buflen=bufpos=0;
		return 0;
	}
}


int     writebuffer(FILE *fptr, byte *buf, word len)
{
	int	totlen;

	totlen=len;

	if (buflen+len>BUFSIZE)
	{
		memcpy(buffer+buflen,buf,BUFSIZE-buflen);
		buf+=BUFSIZE-buflen;
		len-=BUFSIZE-buflen;

		buflen=BUFSIZE;
		if (!writeflush(fptr))
			return 0;
	}

	memcpy(buffer+buflen,buf,len);
	buflen+=len;

	return totlen;
}


int	closebufferwrite(FILE *fptr)
{
	if (writeflush(fptr))
	{
		fclose(fptr);
		return 1;
	}
	else
	{
		fclose(fptr);
		return 0;
	}
}








FILE*	openbufferread(char *filename)
{
	FILE* t;

	t=fopen(filename,"rb");

	if (t!=NULL)
		buflen=bufpos=0;

	return t;
}


int	readflush(FILE *fptr)
{
	buflen=fread(buffer,1,BUFSIZE,fptr);
	bufpos=0;
	return (buflen!=0);
}


int     readbuffer(FILE *fptr, byte *buf, word len)
{
	int	totlen;

	totlen=len;

	if (bufpos+len>buflen)
	{
		memcpy(buf,buffer+bufpos,buflen-bufpos);
		buf+=buflen-bufpos;
		len-=buflen-bufpos;

		bufpos=buflen;

		if (!readflush(fptr))
			return totlen-len;
	}

	memcpy(buf,buffer+bufpos,len);
	bufpos+=len;

	return totlen;
}


int	closebufferread(FILE *fptr)
{
	fclose(fptr);
	return 1;
}





