/*
  error.c

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
#include <stdio.h>
#include <stdlib.h>

#define	__ERROR__


int	Error=0;

char	*Errors[]=
{       "",
	"Bad directory specified.",
	"Not a V9t9-compatible file, or a damaged V9t9 file.",
	"Bad file structure.",
	"No space left on device.",
	"File not found to delete.",
	"Seek failed.",
	"Error on read (possibly file is short).",
	"Bad disk image.",
	"The file already exists.",
	"Out of memory.",
	"V9t9 v6.0 can handle a maximum disk size of 400K.",
	"With double-sided disk images, V9t9 v6.0 supports only 40 tracks.",
	"File not found."
};


void	tierror(char *file)
{
	if (Error)
	{
		fprintf(stderr,"\n"
		       "** Error on file %s:\n",file);
		fprintf(stderr,"%s\n\n",Errors[Error]);
	}
	else
		fprintf(stderr,"\nUnknown error on file %s\n\n",file);
}

void	die(char *file)
{
	if (Error)
	{
		tierror(file);
		exit(1);
	}
	else
		exit(0);

}
