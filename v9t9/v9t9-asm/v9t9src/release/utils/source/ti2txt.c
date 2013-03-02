/*
  ti2txt.c

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
	Convert V9t9-emulated files into DOS/UNIX *.TXT format.
*/

#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#include "fdr.h"
#include "dosfiles.h"
//#include "fiad.h"
//#include "doad.h"
#include "names.h"
#include "param.h"
#include "files.h"

char	ignore=0;
char	verbose=0;
char	overwrite=0;
char	unixstyle=0;
char	chop=0;
char	screen=0;
char	varonly=0;
char	ext[128];



int	dv(int handle)
{
	byte	type,len;

	tigettype(handle,&type,&len);

	return	((type&F_PROGRAM)==0) && (len);
}


#define	END(x)  (*((x)+strlen(x)-1))

void	dochop(char *buf)
{
	while (*buf && END(buf)==' ')
		END(buf)=0;
}




void	dotranslate(char *from, char *topath)
{
	char	frompath[128],fromname[14];
	char	to[128],toname[16];
	char	ans;

	char	buf[256];

	int	handle;
	FILE	*outfile;

	int	reclen;
	byte	type,len;

	int	doad;


	if (!fiadordoad(from,&doad,frompath,fromname))
	{
		fprintf(stderr,"Couldn't parse filename %s.\n",from);
		return;
	}

	ans=0;

	if (!screen)
	{
	    dos2ti(fromname,toname);		// get 10-char name
	    toname[8]=0;			// trunc to 8 chars
	    strcpy(fromname,toname);		// copy
	    ti2Dos(fromname,toname);		// make DOS name, no funny

	    strcat(toname,ext);			// DOS name always has .

	    strcpy(to,topath);
	    strcat(to,toname);

	    while ((!overwrite) && exists(to) && ans!='O' && ans!='S')
	    {
		    fprintf(stderr,"File %s exists.  (O)verwrite, (R)ename, or (S)kip? ",to);
		    buf[0]=4;
		    cgets(buf);
		    fprintf(stderr,"\n");
		    ans=toupper(*(buf+2));
		    if (ans=='R')
		    {
			    fprintf(stderr,"\nEnter new filename in the form %s:  ",toname);
			    toname[0]=14;
			    cgets(toname);
			    fprintf(stderr,"\n");

			    strcpy(to,topath);
			    strcat(to,toname+2);
		    }
	    }

	}

	if (ans!='S')
	{
		if (!openti(from, &handle))
		{
		    if (!ignore)
			fprintf(stderr,"File %s not a V9t9-compatible file, skipping.\n",from);
		}
		else
		{
		    tigettype(handle,&type,&len);

		    if ((type&F_PROGRAM) || (varonly && !(type&F_VARIABLE)))
		    {
			if (!ignore)
			    fprintf(stderr,"File %s is not a text file, skipping.\n",from);
		    }
		    else
		    {
			if (!screen && (outfile=openbufferwrite(to))==NULL)
			{
			    fprintf(stderr,"Could not create %s !\n",to);
			}
			else
			{
			    if (verbose && !screen)
				fprintf(stderr,"Converting %s to %s\n",from,to);

			    while (!tieof(handle))
			    {
				if (!readti(handle,buf,255))
				{
				    fprintf(stderr,"\tRead error on %s !\n",from);
//				    die(from);
				    break;
				}

				if (type&F_VARIABLE)
					reclen=strlen(buf);
				else
					reclen=len;

				if (chop)
				{
				    dochop(buf);
				    reclen=strlen(buf);
				}

				if (unixstyle)
				{
					strcpy(buf+reclen,"\n");
					reclen++;
				}
				else
				{
					strcpy(buf+reclen,"\r\n");
					reclen+=2;
				}

				if (screen)
				   fprintf(stdout,"%s",buf);
				else
				if (writebuffer(outfile,buf,reclen)!=reclen)
				{
				    fprintf(stderr,"\tWrite error on %s !\n",to);
				    break;
				}
			    }
			    if (!screen)
				    closebufferwrite(outfile);
			}
		    }
		    closeti(handle);
		}
	}
}


void	help(void)
{
	printf( "\nTI2TXT  -- translate V9t9 99/4A emulated files to DOS *.TXT format.\n"
		"\n"
		"Usage:"
		"\n"
		"TI2TXT options { [<filename> | <wildcard>] }  [<destination directory>]\n"
		"\n"
		"Options:\t/I\t-- ignore failed conversions of illegal files\n"
		"\t\t/Exxx\t-- provide custom extension (default TXT)\n"
		"\t\t/O\t-- recklessly overwrite existing files\n"
		"\t\t/V\t-- verbose operation\n"
		"\t\t/U\t-- output UNIX text file (LF instead of CR/LF)\n"
		"\t\t/C\t-- chop extra spaces off ends of lines\n"
		"\t\t/S\t-- print output to screen instead of files\n"
		"\t\t/D\t-- ONLY allow xxx/VAR files to be translated\n"
	      );

	exit(1);
}


int	main(int argc, char *argv[])
{
	int	np;
	char	last[128],path[128],filename[128];
	char	opt;


	strcpy(ext,"TXT");

	np=paraminit(1,last);

	if (np<0)
		help();


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
			break;
		case 'O':
			overwrite=1;
			break;
		case 'V':
			verbose=1;
			break;
		case 'U':
			unixstyle=1;
			break;
		case 'C':
			chop=1;
			break;
		case 'D':
			varonly=1;
			break;
		case 'S':
			if (!screen)
			{
				screen=1;
				np=paraminit(0,last);
			}
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

	if (!screen && !isdir(last))
	{
		fprintf(stderr,"You need to specify a target directory.\n");
		exit(1);
	}
	makedirof(last);

	while (getparam(path)!=NULL)
	{
		while (getfilename(path,filename,1)!=NULL)
			dotranslate(filename,last);
	}

	return	0;
}
