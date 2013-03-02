/*
  swapbyte.c

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

int	main(int argc, char *argv[])
{
	FILE	*romfile;
	char	rom[8192],romti[8192];

	if (argc<2)
	{
		printf("swapbyte [rom image]\n");
		exit(1);
	}

	if ((romfile=fopen(argv[1],"rb"))==NULL)
	{
		perror(argv[1]);
		exit(1);
	}

	if (!(fread(rom,8192,1,romfile)))
	{
		printf("Need 8192 bytes\n");
		fclose(romfile);
		exit(1);
	}
	fclose(romfile);

	swab(rom,romti,8192);

	if ((romfile=fopen(argv[1],"wb"))==NULL)
	{
		perror(argv[1]);
		exit(1);
	}

	if (!(fwrite(romti,8192,1,romfile)))
	{
		printf("Couldn't write 8192 bytes: ");
		perror(argv[1]);
		exit(1);
	}
	fclose(romfile);

	printf("Successful.\n");

	return 0;
}