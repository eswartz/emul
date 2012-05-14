/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
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