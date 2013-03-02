/*
  chop.c

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
#include <alloc.h>
#include <conio.h>
#include <ctype.h>
#include <dos.h>
#include <fcntl.h>
#include <io.h>
#include <memory.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys\stat.h>

#include "param.h"

void	help(void)
{
	printf("\n"
	"CHOP file chopper\n"
	"\n"
	"CHOP will truncate a file to a specified length.\n"
	"\n"
	"Usage:   CHOP <filename> <size>\n"
	"\n"
	);

	exit(1);
}

void	main(int argc, char **argv)
{
	char	temp[80];
	char	name[80];
	longint	size;
	int	h;
	int	written;

	if (argc<3)
		help();

	strcpy(name,argv[1]);
	size=atol(argv[2]);

	h=open(name,O_RDWR|O_BINARY);
	if (h==-1)
	{
		printf("Couldn't open %s!\n",name);
		exit(1);
	}
	lseek(h,size,SEEK_SET);
	_dos_write(h,temp,0,&written);
	close(h);

        printf("Done.\n");
}
