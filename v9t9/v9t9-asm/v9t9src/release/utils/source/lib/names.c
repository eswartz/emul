/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	NAMES
	-----


	FOAD routines to translate filenames back and forth from TI
	and DOS, ad infinitum.



	ti2dos(char *in, char *out)	will make out a DOS-legal
					filename from 10-char "in".


	dos2ti(char *in, char *out)	will make a 10-char TI filename
					from an 8-char DOS name.

	int split(char *filename, char *fpath, char *fname) splits stuff.

*/

#include <ctype.h>
#include <dir.h>
#include <stdlib.h>
#include <string.h>

#include "error.h"
#include "names.h"

static 	char	illegal[]="<>=,;:*?[]/\\";


void	_ti2dos(char *in, char *out,int mangle)
{
	int	pos;
	char	c;
	char	*ptr;
	char	dotted;


	pos=0;
	dotted=0;
	while ((c=*in++)!=0 && c!=' ')
	{
		ptr=strchr(illegal,c);
		if (ptr)
			c=(mangle? c+128 : '_');
		*out++=c;
		pos++;
		if (pos==8)
		{
			*out++='.';
			dotted=1;
		}
	}
	if (!dotted)
		*out++='.';
	*out=0;
}


/*
	Convert a 99/4A filename to DOS format, and mangle to do so.
*/
void	ti2dos(char *in, char *out)
{
	_ti2dos(in,out,1);
}


/*
	Convert a 99/4A filename to DOS format, and
	change illegal characters to underlines.
*/
void	ti2Dos(char *in, char *out)
{
	_ti2dos(in,out,0);
}


void	dos2ti(char *in, char *out)
{
	int	pos,writ;
	char	c;

	pos=writ=0;
	while ((c=*in++)!=0)
	{
		if (pos!=8)			// ignore period
		{
			if (c<0)
				c-=128;
			if (islower(c))
				c=_toupper(c);
			*out++=c;
			writ++;
		}
		pos++;
	}
	while (writ++<10)
		*out++=' ';			// space it out
	*out=0;
}


int	split(char *filename, char *fpath, char *fname)
{
	char	fullpath[80];

	char	*nameptr;

	if (_fullpath(fullpath,filename,80)==NULL)
	{
		Error=BADPATH;
		return 0;
	}
	else
	{
		nameptr=strrchr(fullpath,'\\');
		if (nameptr==NULL)
		{
			Error=BADPATH;
			return 0;
		}

		nameptr++;

		strcpy(fname,nameptr);
		*nameptr=0;

		strcpy(fpath,fullpath);

		return 1;
	}
}


void	makedirof(char *dir)
{
	if (dir[strlen(dir)-1]!='\\')
		strcat(dir,"\\");
}


void	fix10(char *in, char *out)
{
	int	x;

	for (x=9; x>=0; x--)
		if (x>=strlen(in))
			out[x]=0x20;
		else
			out[x]=in[x];
}


/*
#include <stdio.h>

void	main(void)
{
	char	in[14],out[14];

	char	path[80],name[14];

	strcpy(in,"ti­write.r");
	dos2ti(in,out);
	printf("%s => %s\t",in,out);
	ti2dos(out,in);
	printf("=> %s\n",in);

	strcpy(in,"dos10[me/s");
	ti2dos(in,out);
	printf("%s => %s\t",in,out);
	dos2ti(out,in);
	printf("%s\n",in);

	split(out,path,name);
        printf("%s = %s and %s\n",out,path,name);

}
*/
