/*
  swap.c

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
	Swap bytes in console ROMs.
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
	"SWAP Byte-Swap Utility\n"
	"\n"
	"SWAP will simply swap every two bytes in the given file.\n"
	"This is most useful when you have 99/4A console ROMs or cartridge ROMs\n"
	"which have been changed to Intel byte order.  V9t9 expects all files to\n"
	"be in 9900 (TI) byte order, which is what you should usually have.\n"
	"\n"
	"Don't use this program on GROM images since they are not affected by\n"
	"byte order.\n"
	"\n"
	"Usage:\n"
	"\n"
	"SWAP <infile> <outfile>\n"
	"\n"
	);

	exit(1);
}


void	main(int argc, char **argv)
{
	char	temp[80];
	char	infilename[80];
	char	outfilename[80];
	char	opt;

	int	from,to;
	char   	buff[1024];
	longint	left;
	int	size;

	struct	stat st;


	if (paraminit(0,temp)!=2)
		help();


	getparam(infilename);
	getparam(outfilename);

	from=open(infilename,O_RDONLY|O_BINARY);
	if (from==-1)
	{
		printf("Couldn't open %s!\n",infilename);
		exit(1);
	}

	to=open(outfilename,O_BINARY|O_CREAT|O_TRUNC|O_WRONLY,S_IREAD|S_IWRITE);
	if (to==-1)
	{
		printf("Couldn't create %s!\n",outfilename);
		exit(1);
	}

	fstat(from,&st);

	left=st.st_size&0xfffffffe;

	while (left)
	{
		if (left>1024)
			size=1024;
		else
			size=left;

		if (read(from,buff,size)!=size)
		{
			printf("Read error on %s!\n",infilename);
			exit(1);
		}

		swab(buff,buff,size);

		if (write(to,buff,size)!=size)
		{
			printf("Write error on %s!\n",outfilename);
			exit(1);
		}

		left-=size;
	}

	close(to);
	close(from);

	printf("Successful!\n");
}
