/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Format a DOAD.
*/


#include <alloc.h>
#include <conio.h>
#include <ctype.h>
#include <dos.h>
#include <io.h>
#include <memory.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "doad.h"
#include "dosfiles.h"
#include "error.h"
#include "fdr.h"
#include "fiad.h"
#include "files.h"
#include "names.h"
#include "param.h"



int	help(void)
{
	printf("\n"
	       "TIFORMAT V9t9 Disk Image Formatter v1.0\n"
	       "\n"
	       "Usage:  TIFORMAT [options] <disk image filename>\n"
	       "\n"
	       "TIFORMAT will create a new disk image for use as a DOAD under V9t9.\n"
	       "\n"
	       "Options:\n"
	       "\n"
	       "\t\t/Fxxx\t-- format size (90k, 180k, 360k)\n"
	       "\t\t/C\t-- using CUSTOM settings (specify before using\n"
	       "\t\t\t   nonstandard values with the options below)\n"
	       "\t\t/Txxx\t-- number of tracks (40,80)\n"
	       "\t\t/Sxxx\t-- number of sides (1,2) (V9t9 only supports 1 side)\n"
	       "\t\t/Nxxx\t-- number of sectors per track (9,15,18)\n"
	       "\t\t/Vxxx\t-- volume label (by default it is the filename)\n"
	       "\n"
	       "\tWhen specifying custom drive geometries, the /F option is \n"
	       "\tunnecessary.\n"
	       "\n"
	       );

	exit(1);
}


int	main(int argc, char **argv)
{
	char	temp[80];
	char	doadname[80];
	char	opt;
	char	volume[80];
	char	voldrive[4],volpath[64],volname[10],volext[4];

	int	custom=0;
	int	tracks,sectors,size,sides;
	int	usersize;

	int	h;

	size=90;
	tracks=40;
	sectors=9;
	size=90;
	usersize=0;

	if (paraminit(1,doadname)!=0)
		help();


	fnsplit(doadname,voldrive,volpath,volname,volext);
	memset(volume,0,80);
	strcpy(volume,volname);
	volume[10]=0;

	while ((opt=getopt())!=0)
	{
	switch (opt)
	{
	case	'?':
	case	'H':	help();
			break;
	case	'C':	custom=1;
			break;
	case	'F':	size=atoi(getoptstr(temp,1));
			switch (size)
			{
			case	90:	tracks=40;
					sides=1;
					sectors=9;
					break;
			case	180:	tracks=40;
					sides=2;
					sectors=9;
					break;
			case	360:	tracks=40;
					sides=2;
					sectors=18;
					break;
			default:	printf("Invalid size specified\n");
					exit(1);
			}
			usersize=1;
			break;

	case	'S':	sides=atoi(getoptstr(temp,1));
			if (!custom && (sides<0 || sides>2))
			{
				printf("Invalid # of sides specified\n");
				exit(1);
			}
			break;

	case	'T':	tracks=atoi(getoptstr(temp,1));
			if (!custom && (tracks!=40 && tracks!=80))
			{
				printf("Invalid # of tracks specified\n");
				exit(1);
			}
			break;

	case	'N':	sectors=atoi(getoptstr(temp,1));
			if (!custom &&
			(sectors!=9 && sectors!=15 && sectors!=18))
			{
				printf("Invalid # of sectors specified\n");
				exit(1);
			}
			break;


	case	'V':	getoptstr(volume,1);
			volume[10]=0;
			break;

	default:	printf("Unknown option '%c'\n",opt);
			exit(1);
	}
	}


	strupr(volume);
	if (usersize && sectors*tracks*sides/4!=size)
	{
		printf("Specified size and drive geometry do not match.\n"
		       "(%dk <> %d tracks * %d sides * %d sectors /4)\n",
		       size,tracks,sides,sectors);
		exit(1);
	}

	if (!createdisk(doadname,volume,tracks,sectors,sides,1,&h))
	{
		printf("Create failed!\n");
		tierror(doadname);
	}
	else
	{
		printf("Successful.\n");
		close(h);
	}

	return 0;
}

