/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*

	Parameter-handling routines.


	int paraminit()		sets up for getopt and returns # of params.
	char getopt()		returns uppercase /xxx or -xxx param.
	char *getoptstr(char *s) returns the option with a string. (no spaces!)

	char* getparam(char *s) returns a non-option param , but not the last
	char* getlastparam(char *s) returns last param

*/


#include <ctype.h>
#include <dir.h>
#include <dos.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "fdr.h"
#include "files.h"
#include "names.h"
#include "param.h"


int	curoptarg;
int	curarg;
int	lastarg;

struct 	ffblk cursearch;
int	searchfinished=1;


int	paraminit(int needslast, char *last)
{
	char	ch;
	int	la;
	int	na;

	searchfinished=1;

	curarg=curoptarg=0;

	strcpy(last,"");

	if (!needslast)
	{
		lastarg=_argc;
		na=0;
		while	(getparam(last)!=NULL)
			na++;

		curarg=0;
		return	na;
	}
	else
	{
		lastarg=_argc;
		na=0;
		while 	(getparam(last)!=NULL)
		{
			 la=curarg;
			 na++;
		}

		lastarg=la;
		curarg=0;
		return na-1;
	}
}


char	getopt(void)
{
	char	ch;

	while (++curoptarg<=_argc)
	{
		if ((ch=*_argv[curoptarg])=='/' || ch=='-')
		{
			ch=_argv[curoptarg][1];
			if (ch==0)
			{
				fprintf(stderr,"Option letter expected in argument %d.\n",curoptarg);
				exit(1);
			}
			else
			return toupper(ch);
		}
	}
	return '\0';
}


char *getoptstr(char *buf, int required)
{
	char	ch;
	char	opt;

	opt=toupper(_argv[curoptarg][1]);
	ch=_argv[curoptarg][2];
	if (ch==0)
	{
		if (required)
		{
			fprintf(stderr,"/%c requires an argument in the form: /%cxxx\n",
				opt,opt);
			exit(1);
			return NULL;
		}
		else
			return NULL;
	}
	else
	{
		strcpy(buf,_argv[curoptarg]+2);
		return buf;
	}
}


char    *getparam(char *buf)
{
	char ch;

	while (++curarg<lastarg)
	{
		if (!((ch=*_argv[curarg])=='/' || ch=='-'))
		{
			strcpy(buf,_argv[curarg]);
			return buf;
		}
	}
	return NULL;
}



char	*getfilename(char *path,char *buf,int ti)
{
	char	thepath[80];
	char	wildcard[14];

	if (searchfinished)
	{
		searchfinished=wildinit(path,buf,ti);
		if (searchfinished)
			fprintf(stderr,"No files match %s, continuing\n",path);
	}
	else
		searchfinished=wild(buf);


	if (searchfinished)
			return NULL;
	else
		return buf;

}



/*
void	main(int argc,char *argv[])
{
	char	last[128];
	char	cur[128];
	char	path[128];
	char	opt;
	char	*par;

	if (paraminit(1,last)<=0)
	{
		fprintf(stderr,"Need some params here!\n");
		exit(1);
	}


	while ((opt=getopt())!=0)
	{
//		if ((opt&1)==1)
			getoptstr(cur);
//		else
//			strcpy(cur,"");

		printf("Option:\t%c\t%s\n",opt,cur);
	}

	printf("\n\n");

	while ((par=getparam(cur))!=NULL)
	{
		printf("Param:\t\t%s\n",par);
		while ((par=getfilename(cur,path))!=NULL)
			printf("\t\t\t%s\n",path);
	}

	printf("\n\nLast:\t\t%s\n\n",last);
}

*/
