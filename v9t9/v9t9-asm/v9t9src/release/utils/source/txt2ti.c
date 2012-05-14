/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Convert DOS/UNIX files into D/V 80 format.
*/

#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "dosfiles.h"
#include "fdr.h"
//#include "fiad.h"
#include "files.h"
#include "names.h"
#include "param.h"


byte	type=F_VARIABLE;
byte	reclen=80;
char	ignore=0;
char	verbose=0;
char	overwrite=0;
//char	unixstyle=0;
char	expandtabs=0;
byte	tabsize=8;
char	chop=0;
char	screen=0;
char	ext[128];



#define	END(x)  (*((x)+strlen(x)-1))

void	dochop(char *buf)
{
	char	*end;

	end=buf+strlen(buf)-1;
	while (*buf && *end==' ')
		*end--=0;
}


void	doexpand(char *buf)
{
	char	temp[256];
	char	*in,*out;
	int	pos;
	char	ch;

	strcpy(temp,buf);
	in=temp; out=buf;
	pos=0;
	while ((ch=*in++) && out<buf+256)
	{
		if (ch!=9)
		{
			*out++=ch;
			pos++;
		}
		else
		{
			do
			{
				*out++=' ';
				pos++;
			}	while ((pos%tabsize)!=0 && out<buf+256);
		}
	}
	*out++=0;
}


void	dotranslate(char *from, char *topath)
{
	char	frompath[128],fromname[14];
	char	to[128],toname[14];
	char	*nptr;
	char	ans;

	char	buf[256];

	int	handle;
	FILE	*infile;

	int	doad;


	if (!split(from,frompath,fromname))
	{
		fprintf(stderr,"Couldn't parse filename %s.\n",from);
		return;
	}

	ans=0;

	nptr=fromname+strlen(fromname)-1;     	// lose the extension
	while (nptr>fromname)
		if (*nptr=='.')
		{
			*nptr=0;
			break;
		}
		else
			nptr--;


	if (!fiadordoad(topath,&doad,to,toname))
	{
		fprintf(stderr,"Can't access %s\n",topath);
		return;
	}
	else
	if (!doad)
	{
		dos2ti(fromname,toname);		// get 10-char name
		dochop(toname);
		strcat(toname,ext);
//		strcpy(fromname,toname);		// copy
//		ti2dos(fromname,toname);		// make DOS name
	}
	else
	{
		dos2ti(fromname,toname);		// get 10-char name
		dochop(toname);
		strcat(toname,ext);
		strcpy(fromname,toname);
	}

	strcpy(to,topath);
	strcat(to,toname);

	    while ((!overwrite) && tiexists(to) && ans!='O' && ans!='S')
	    {
		    fprintf(stderr,"File %s exists.  (O)verwrite, (R)ename, or (S)kip? ",to);
		    buf[0]=4;
		    cgets(buf);
                    fprintf(stderr,"\n");
		    ans=toupper(*(buf+2));
		    if (ans=='R')
		    {
			    fprintf(stderr,"Enter new filename in the form %s:  ",toname);
			    buf[0]=14;
			    cgets(buf);
			    fprintf(stderr,"\n");

			    strcpy(toname,buf+2);
			    strcpy(to,topath);
			    strcat(to,toname);
		    }
	    }


	if (ans!='S')
	{
		if ((infile=openbufferread(from))==NULL)
//		if (!openfoad(from, &handle))
		{
		    if (!ignore)
			fprintf(stderr,"File %s does not exist (?), skipping.\n",from);
		}
		else
		{
		    {
			if (!createti(to,type,reclen,&handle))
			{
			    fprintf(stderr,"Could not create %s !\n",to);
			    tierror(to);
			}
			else
			{
			    if (verbose)
				fprintf(stderr,"Converting %s to %s\n",from,to);

			    while (!feof(infile))
			    {
				getstring(buf,reclen,infile,!(type&F_VARIABLE),
					(type&F_INTERNAL) ? 0 : 0x20);
				if (chop)
				    dochop(buf);
				if (expandtabs)
				    doexpand(buf);

				if (!writeti(handle,buf,strlen(buf)))
				{
				    fprintf(stderr,"\tWrite error on %s !\n",to);
				    break;
				}
			    }
			    closeti(handle);
			}
		    }
		    closebufferread(infile);
		}
	}
}


void	help(void)
{
	printf( "\nTXT2TI  -- UNIX/DOS Text to 99/4A File Converter v1.0\n"
		"\n"
		"Usage:"
		"\n"
		"TXT2TI options { <filename> }  [<destination directory> | <disk image:>]\n"
		"\n"
		"This program will convert DOS text files into the 99/4A format.\n"
		"By default, the DIS/VAR 80 format is the destination, but the options\n"
		"below allow for any output type.\n"
		"\n"
		"Options:\n"
		"\t\t/I\t-- ignore failed conversions of illegal files\n"
		"\t\t/Exxx\t-- provide custom extension (default none)  (ex: /T)\n"
		"\t\t/O\t-- recklessly overwrite existing files\n"
		"\t\t/V\t-- verbose operation\n"
		"\t\t/C\t-- chop extra spaces off ends of lines\n"
		"\t\t/T[xxx]\t-- expand tabs to spaces, optionally set tabsize to xxx."
		"\n"
		"\t\t/F\t-- make output files xxx/FIX format.\n"
		"\t\t/N\t-- make output files INT/xxx format.\n"
		"\t\t/Rxxx\t-- set record size other than 80 bytes.\n"
	      );

	exit(1);
}


int	main(int argc, char *argv[])
{
	int	np;
	char	last[128],path[128],filename[128];
	char	opt;
	char	temp[64];


	strcpy(ext,"");

	np=paraminit(1,last);

	if (np<0)
		help();

	if (np==0 || !isdir(last))
	{
		printf("The last parameter should be the destination directory.\n");
		exit(1);
	}

	makedirof(last);

	while ((opt=getopt())!=0)
	{
		switch	(opt)
		{
		case 'I':
			ignore=1;
			break;
		case 'E':
			memset(ext,0,4);
			getoptstr(ext,0);
			ext[4]=0;
			break;
		case 'O':
			overwrite=1;
			break;
		case 'V':
			verbose=1;
			break;
//		case 'U':
//			unixstyle=1;
//			break;
		case 'C':
			chop=1;
			break;
/*		case 'S':
			if (!screen)
			{
				screen=1;
				paraminit(0,last);
			}
			break;*/
		case 'T':
			expandtabs=1;
			if (getoptstr(temp,0)!=NULL)
			{
				tabsize=atoi(temp);
				if (tabsize==0)
					tabsize=8;
			}
			break;
		case 'F':
			type&=~F_VARIABLE;
			break;
		case 'N':
			type|=F_INTERNAL;
			break;
		case 'R':
			getoptstr(temp,1);
			reclen=atoi(temp);
			if (reclen==0)
				reclen=80;
			break;
		case '?':
		case 'H':
			help();
			break;
		default:
			printf("Illegal option:  /%c\n",opt);
			exit(1);
		}
	}

	while (getparam(path)!=NULL)
	{
		while (getfilename(path,filename,0)!=NULL)
			dotranslate(filename,last);
	}

	return	0;
}
