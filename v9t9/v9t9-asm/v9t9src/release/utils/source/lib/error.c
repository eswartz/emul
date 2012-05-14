/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
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