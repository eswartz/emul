/*
  config.c

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
	This module will do configuration-file functions.


	int changevar(char *configfilename, char *varname, char *value);
*/

#include <ctype.h>
#include <dir.h>
#include <dos.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>



void	stripln(char *ln)
{
	char	*ptr;

	ptr=ln+strlen(ln)-1;
	while (ptr>=ln && (strchr("\n\r \t",*ptr)))
		*ptr--=0;

}

int	comment(char *ln)
{
	while (*ln==' ' || *ln==9)
		ln++;

	if (!*ln || strchr("[;!#",*ln))
		return 1;
	else
		return 0;
}


int	notvar(char *ln, char *var)
{
	while (*ln==' ' || *ln==9)
		ln++;

	while (*var)
	{
		if (toupper(*var)==toupper(*ln))
		{
			var++;  ln++;
		}
		else
			return 1;
	}

	if (*ln==' ' || *ln==9 || *ln=='=')
		return 0;
	else
		return 1;

}


/*
	Here's how we do it.

	Rename CONFIGFILENAME to *.CBK.  Then, read line-by-line
	from it, and write line-by-line to the new file, except
	for changing the line which contains VARNAME.

	Returns 1 for success.
*/
int	changevar(char *configfilename, char *varname, char *value)
{
	char	drive[4];
	char	path[64];
	char	name[10];
	char	ext[6];

	char	oldname[80];

	FILE	*i,*o;

	char	linebuf[256];
	int	found;

	fnsplit(configfilename,drive,path,name,ext);
	strcpy(oldname,drive);
	strcat(oldname,path);
	strcat(oldname,name);
	strcat(oldname,".CBK");

	remove(oldname);

	if (rename(configfilename,oldname))
		return 0;

	i=fopen(oldname,"r");
	if (i==NULL)
		return 0;

	o=fopen(configfilename,"w");
	if (o==NULL)
		return 0;

	found=0;
	while (!feof(i))
	{
		fgets(linebuf,256,i);
		if (feof(i))
			break;
		stripln(linebuf);
		if (comment(linebuf) || notvar(linebuf,varname))
			fprintf(o,"%s\n",linebuf);
		else
		{
			fprintf(o,"%s = %s\n",varname,value);
			found=1;
		}
	}
	if (!found)
		fprintf(o,"%s = %s\n",varname,value);

	fclose(i);
	fclose(o);

	return	1;
}

/*
	Return the value of VARNAME from CONFIGFILENAME.
	If not found, return 0.

*/

int	getvar(char *configfilename, char *varname, char *buf)
{
	FILE	*i;
	char	*ptr;

	char	linebuf[256];
	int	found;

	i=fopen(configfilename,"r");
	if (i==NULL)
		return 0;

	found=0;
	*buf=0;
	while (!feof(i))
	{
		fgets(linebuf,256,i);
		stripln(linebuf);
		if (!(comment(linebuf) || notvar(linebuf,varname)))
		{
			ptr=strchr(linebuf,'=');
			if (ptr)
			{
				ptr++;
				while (*ptr==' ' || *ptr==9)
					ptr++;
				strcpy(buf,ptr);
				found=1;
			}
		}
	}

	fclose(i);

	return found;
}
