/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Convert V9t9 files into XMODEM TIFILES-types.
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

#include "doad.h"
#include "dosfiles.h"
#include "error.h"
#include "fdr.h"
#include "fiad.h"
#include "files.h"
#include "names.h"
#include "param.h"

#include "config.h"

struct	xmodemtype
{
	char 	tifiles[8];
	word	secsused;
	byte	flags;
	byte	recspersec;
	byte	eof;
	byte	reclen;
	word	fixrecs;
	byte	unused[127-14+1];
};


/*
Each GRAM Kracker stores an 8K ROM or GROM bank.  The first six bytes
are an extension of the memory image program standard.  Here is what the
bytes mean:

Byte 0: "More to load" flag, values:
>FF = There's another file to load
>80 = There's a "UTIL" file to load (I don't think this is ever used).
>00 = This is the last file

Byte 1: Bank of GRAM/RAM to load file into, values:
>01 = GROM/GRAM bank 0 (>0000)
>02 = GROM/GRAM bank 1 (>2000)
>03 = GROM/GRAM bank 2 (>4000)
>04 = GROM/GRAM bank 3 (>6000)
>05 = GROM/GRAM bank 4 (>8000)
>06 = GROM/GRAM bank 5 (>A000)
>07 = GROM/GRAM bank 6 (>C000)
>08 = GROM/GRAM bank 7 (>E000)
>09 = ROM bank 1 (>6000)
>0A = ROM bank 2 (second >6000)
>00 or >FF = Assembly language program, not a cartridge file

Bytes 2 and 3: Number of bytes to load (normally 8192)
Bytes 4 and 5: Actual address to start loading at (e.g. >6000)

As an example, Extended BASIC gets saved as files "XB" to "XB5", which
have headers as follows:
XB      FF0A 2000 6000 (ROM bank 2)
XB1     FF09 2000 6000 (ROM bank 1)
XB2     FF07 2000 C000 (GRAM bank 6)
XB3     FF06 2000 A000 (GRAM bank 5)
XB4     FF05 2000 8000 (GRAM bank 4)
XB5     0004 2000 6000

Even though TI GROMS only use 6K of each 8K bank, all 8K is stored so people
can add their own GPL code as extensions to modules.  Extended BASIC has
a number of enhancements available.



*/


word	addrsfor[]=
	{0x0,     				/* type 0 is not used */
	 0x0,0x2000,0x4000,			/* CPU GROM segments, 1-3 */
	 0x0,0x2000,0x4000,0x6000,0x8000,	/* 4-8, GROM */
	 0x0,0x0};				/* 9-10, CPU ROM module */

struct	gramheader
{
	byte	moretoload;
	byte	bank;
	word	length;
	word	addr;
};

char	configfilename[80];
char	modulesinffilename[80];
char	verbose=0;
char	bare=0;
char	overwrite=0;

char	modpath[80];
char	rompath[80];
char	modext[80];

char	cpuromfilename[80];
char	gplromfilename[80];

char	basename[80];


int	programfile(int i, char *fullname)
{
	struct	xmodemtype xmod;
	struct	fdrstruc fdr;
	byte	flags;
	byte	reclen;

	if (read(i,&fdr,128)!=128)
	{
		printf("Read error on %s!\n",fullname);
		close(i);
		return 0;
	}

	memcpy(&xmod,&fdr,128);

	if (memcmp(xmod.tifiles,"\07TIFILES",8)==0)
	{
		flags=xmod.flags;
		reclen=xmod.reclen;
	}
	else
	{
		flags=fdr.flags;
		reclen=fdr.reclen;
	}

	if ((flags&0x83)!=1 || reclen)
	{
		printf("%s is not a PROGRAM-type file.\n"
		"Be sure there is a TIFILES or V9t9\n"
		"header on all segments of the module image, or use the /B option.\n",
		fullname);

		close(i);
		return 0;
	}

	return	1;
}


int	anyexist(char *basename)
{
	char	fullname[80];
	char	*exts[]=
	{"C.","D.","G."};
	int	ext;
	struct	stat st;

	for (ext=0; ext<3; ext++)
	{
		strcpy(fullname,modpath);
		strcat(fullname,basename);
		strcat(fullname,exts[ext]);
		strcat(fullname,modext);
		if (!stat(fullname,&st))
			return 1;
	}

	return 0;

}


#define	G_ROM1	1
#define	G_ROM2	2
#define	G_GROM	4


int	convert(char *basefile)
{
	char	pathname[64];
	char	filename[14];

	char	fullname[80];
	char	outname[80];

	char	temptiname[14];
	char	tempdosname[14];

	char	title[34];

	int	doad;

	int	segment;
	struct	gramheader gram;
	char	segchar[2];

	int	i,o;
	word	size;
	char	buff[8192];

	int	cpugrom=0;
	byte	parts=0;

	FILE	*modinf;
	char	modline[80];
	char	*modptr;

	struct	stat st;

	if (!fiadordoad(basefile,&doad,pathname,filename))
	{
		printf("Couldn't access %s!\n",basefile);
		return 1;
	}

	if (doad)
	{
		printf(
		"Please copy the GRAM Kracker files from the disk image into a temporary\n"
		"directory using TICOPY, and then run GRAM2TI on them.\n"
		"\n"
		"Example:\n"
		"\n"
		"C:\\V9t9> TICOPY %s* \tmp\n"
		"C:\\V9t9> GRAM2TI \tmp\%s*\n"
		"\n",basefile,filename
		);
		return 1;
	}


	if (!*basename)
	{
		ti2dos(filename,tempdosname);
		memset(basename,0,8);
		memcpy(basename,tempdosname,7);
		if (basename[strlen(basename)-1]=='.')
			basename[strlen(basename)-1]=0;
	}

	if (verbose)
		printf("Using %s as the basename.\n",basename);


	while (anyexist(basename) && !overwrite)
	{
		printf("Module files that will be created by GRAM2TI already exist.\n"
		"Enter a new basename (different from %s)\n\n: ",basename);
		basename[0]=8;
		cgets(basename);
		printf("\n\n");
		strcpy(basename,basename+2);
	}


	segment=0;
	do
	{
		strcpy(fullname,pathname);
//		strcat(fullname,filename);
		strcpy(temptiname,filename);
		if (segment)
		{
			segchar[0]=segment+'0';
			segchar[1]=0;
			if (strlen(filename)<10)
				strcat(temptiname,segchar);
			else
				*(temptiname+strlen(temptiname)-1)=*segchar;
		}

		ti2dos(temptiname,tempdosname);
		strcat(fullname,tempdosname);

		if (verbose)
			printf("Adding segment %s...\n",fullname);

		i=open(fullname,O_RDONLY|O_BINARY);
		if (i==-1)
		{
			printf("Couldn't open the segment %s!\n",fullname);
			if (segment)
				printf("Be sure you have all the segments of the file available.\n");
			return 1;
		}

		if (!bare)
			if (!programfile(i,fullname))
				return 1;

		if (read(i,&gram,6)!=6)
		{
			printf("Read error on %s!\n",fullname);
			return 1;
		}
		gram.length=swapbytes(gram.length);
		gram.addr=swapbytes(gram.addr);

		if (gram.moretoload==0x80)
		{
			printf("%s is a utility file (not ROM).\n",fullname);
			close(i);
			return 1;
		}

		if ((gram.moretoload!=0 && gram.moretoload!=0xff) ||
		    (gram.bank<1 || gram.bank>0xa) ||
		    (gram.length>0x2000))
		{
			printf("%s doesn't appear to be a GRAM-Kracker segment.\n",
			fullname);
			close(i);
			return 1;
		}

		if (gram.bank<0x4)
		{
			cpugrom=1;
			printf("This looks like a console GROM segment.\n"
			"Switching to GROM mode.\n");
		}
		else
			cpugrom=0;

		strcpy(outname,(cpugrom ? rompath : modpath));
		if (cpugrom)
			strcat(outname,gplromfilename);
		else
		if (gram.bank<0x9)
		{
			parts|=G_GROM;
			strcat(outname,basename);
			strcat(outname,"G.");
			strcat(outname,modext);
		}
		else
		if (gram.bank==0x9)
		{
			parts|=G_ROM1;
			strcat(outname,basename);
			strcat(outname,"C.");
			strcat(outname,modext);
		}
		else
		{
			parts|=G_ROM2;
			strcat(outname,basename);
			strcat(outname,"D.");
			strcat(outname,modext);
		}

		if (verbose)
			printf("Creating/adding to %s...\n",outname);

		o=open(outname,O_CREAT|O_BINARY|O_RDWR,S_IREAD|S_IWRITE);
							/* no O_TRUNC! */
		if (o==-1)
		{
			printf("Couldn't create %s!\n",outname);
			close(i);
			return 1;
		}

		if (read(i,buff,gram.length)!=gram.length)
		{
			printf("Read error on %s!\n",fullname);
			close(i);
			return 1;
		}

		lseek(o,addrsfor[gram.bank],SEEK_SET);
		if (write(o,buff,gram.length)!=gram.length)
		{
			printf("Write error on %s!\n",outname);
			close(o);
			close(i);
			return 1;
		}

		close(o);
		close(i);

		segment++;
	} 	while (gram.moretoload);

	if (parts)
	{
		if (verbose)
			printf("Adding entry for module to %s...\n",
			modulesinffilename);

		do
		{
			printf("\n\nPlease enter the title for the module, that will appear\n"
			"in the startup selection list.  (No quotes, max 32 chars)\n\n: ");
			title[0]=32;
			cgets(title);
		}	while (strchr(title+2,'"'));

		printf("\n\n");
		modptr=modline;
		modptr+=sprintf(modline,"\"%s\",%s",title+2,basename);

		if ((parts&(G_ROM1|G_ROM2))==(G_ROM1|G_ROM2))
			modptr+=sprintf(modptr,",BANKED");
		else
		if (parts&G_ROM1)
			modptr+=sprintf(modptr,",ROM");
		else
		if (parts&G_ROM2)
			modptr+=sprintf(modptr,",ROM2");

		if (parts&G_GROM)
			modptr+=sprintf(modptr,",GROM");

		modinf=fopen(modulesinffilename,"a+t");
		if (modinf==NULL)
		{
			printf("Couldn't open/create %s!\n",modulesinffilename);
			printf("Module entry not added!  Please add this line to your modules\n"
			"database to be able to use the module:\n\n\t%s\n",
			modline);
			return 1;
		}
		else
		{
			fprintf(modinf,"%s\n",modline);
			fclose(modinf);
			printf("Module entry added to %s\n",
			modulesinffilename);
			printf("Please run MODULES to add it to the startup selection list.\n");
		}
	}

	printf("\nSuccessful!\n");
	return	0;
}







void	help(void)
{
	printf("\n"
	"GRAM2TI GRAM-Kracker to V9t9 Module Converter v1.0\n"
	"\n"
	"GRAM2TI will convert GRAM-Kracker format files into module files that are\n"
	"compatible with V9t9.  The source files may be either TIFILES or V9t9\n"
	"format, or pure PROGRAM data.\n"
	"\n"
	"The V9t9 module files will be created and an entry for the module\n"
	"will be automatically added to MODULES.INF.\n"
	"\n"
	"GRAM2TI can also create console GROM images from GRAM-Kracker files.\n"
	"\n"
	"Usage:\n"
	"\n"
	"GRAM2TI [options] <first GRAM-Kracker segment>\n"
	"\n"
	"Options:\t/Mxxx\t-- specify module database (default MODULES.INF)\n"
	"\t\t/Cxxx\t-- specify configuration file (default V9t9.CNF)\n"
	"\t\t/Nxxx\t-- specify V9t9 module base name\n"
	"\t\t/B\t-- the files are bare (no TIFILES or V9t9 header info)\n"
	"\t\t/O\t-- overwrite existing targets\n"
	"\t\t/V\t-- verbose operation\n"
	"\n"
	"(See TRANSFER.TXT and UTILS.TXT.)\n"
	"\n"
	);

	exit(1);
}


int	main(int argc, char **argv)
{
	char	temp[80];
	char	basefile[80];
	char	opt;
	struct	stat st;

	if (paraminit(1,basefile))
		help();


	strcpy(modulesinffilename,"MODULES.INF");
	strcpy(configfilename,"V9t9.CNF");


	while ((opt=getopt())!=0)
	{
	switch (opt)
	{
	case	'?':
	case	'H':	help();
			break;
	case	'M':	getoptstr(modulesinffilename,1);
			break;
	case	'C':	getoptstr(configfilename,1);
			break;
	case	'V':	verbose=1;
			break;
	case	'B':	bare=1;
			break;
	case	'O':	overwrite=1;
			break;
	case	'N':	getoptstr(basename,1);
			break;

	default:	printf("Unknown option '%c'\n",opt);
			exit(1);
	}
	}

	if (stat(modulesinffilename,&st) || stat(configfilename,&st))
	{
		printf("Cannot find configuration files\n%s or %s.\n",
		modulesinffilename,configfilename);
		printf("Either specify them with /C and /M, or run GRAM2TI directly from\n"
		"your V9t9 directory.\n");
		exit(1);
	}


	if (!getvar(configfilename,"ModulesPath",modpath))
		strcpy(modpath,"MODULES\\");
	makedirof(modpath);

	if (!getvar(configfilename,"ROMSPath",rompath))
		strcpy(rompath,"ROMS\\");
	makedirof(rompath);

	if (!getvar(configfilename,"DefaultModuleExtension",modext))
		strcpy(modext,"HEX");

	if (!getvar(configfilename,"CPUROMFileName",cpuromfilename))
		strcpy(cpuromfilename,"994AROM.BIN");

	if (!getvar(configfilename,"GPLROMFileName",gplromfilename))
		strcpy(gplromfilename,"994AGROM.BIN");

	return convert(basefile);
}
