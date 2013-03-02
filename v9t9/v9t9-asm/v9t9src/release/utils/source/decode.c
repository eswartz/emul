/*
  decode.c

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
#include "decoder.h"

int	main(int argc,char **argv)
{
	if (argc<4)
	{
		printf("DECODE <filename-in> <filename-out> <key>\n"
		       "\n"
		       "Use this program to decode encrypted files sent to you by the\n "
		       "author.\n\nSee BINARIES.TXT for more information.");
		exit(1);
	}

	if (decoder(argv[1],argv[2],argv[3]))
	{
		printf("There was a decoding error.  Make sure the file exists.\n");
		exit(1);
	}

	return	0;





}
