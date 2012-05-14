/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
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