/*
  recvd.c

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
#include <bios.h>
#include <conio.h>
#include <io.h>
#include <fcntl.h>
#include <sys\stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys\types.h>

#include "serial.h"

#define DATA_READY 0x100
int	baudrate;
int	port;
int	irq;

#define	ERR	"Unknown code received.\n"

void	getbasename(void);
void	closemodule(void);
int	gs(char *s);
int	dump(void);


void	ps(char *st)
{
	unsigned char len=strlen(st);

	delay(100);

	com_send(len);
	while (len--)
		com_send(*st++);

}


void	Err(void)
{
	printf(ERR);
//	bioscom(_COM_SEND,255,port);
	com_send(0xff);
	buf_init();
}

int	main(void)
{
	int	ch;
	char	comm[256];
	struct	stat st;
	int	okay=0;

	printf("RECV -- 99/4A ROM dump receiver by Edward Swartz.\n\n"
	       "Companion to TRANS on the 99/4A.\n\n");

	if (stat("MODULES",&st) || stat("ROMS",&st) || stat("MODULES.INF",&st))
	{
		printf("This program needs to be run from the directory where V9t9.EXE,\n"
		       "MODULES.INF, etc., are located.  (Use RECV.BAT from that directory.)\n");
		exit(1);
	}

	printf("In order to use this program, TRANS must be running on a\n"
	       "99/4A system connected to this PC by a serial cable.\n"
	       "It's best to start the 99/4A TRANS before running this program.\n\n"
	       "Press <Enter> to continue, or <Esc> to exit RECV:");
	do
		ch=getch();
	while	(ch!=13 && ch!=27);

	if (ch==27)
		exit(1);


	do
	{
	do
	{
		printf("\n\n\nEnter the DOS serial port you are using to connect\n"
		       "to the 99/4A.  Valid values are 1-4.\n\n: ");
		comm[0]=2;
		port=atoi(cgets(comm));
	}	while (port<1 || port>4);

	do
	{
		printf("\n\n\nEnter the IRQ of COM%d.  Typical value is %d.\n\n: ",
			port, ((port==1 || port==3) ? 4 : 3));
		comm[0]=2;
		irq=atoi(cgets(comm));
	}	while (irq<0 || irq>7);

	port--;			// fix port to 0-3

	do
	{
		printf("\n\n\nEnter the baud rate you used to initialize the RS232\n"
		       "in TRANS.  Valid baud rates are\n"
		       "\t110 300 600 1200 2400 4800 9600.\n\n: ");
		comm[0]=5;
		baudrate=atoi(cgets(comm));
	}	while (baudrate<110 || baudrate>9600);

	if (com_init(port,baudrate,irq))
	{
		printf("\n\nIllegal initialization options.\n\n");
	}
	else
		okay=1;
	}
	while (!okay);

	printf("\n\nRECV is ready to go.\n\n"
	       "Press a key at any time to abort.\n\n");
	while (!kbhit())
	{
//		ch=bioscom(_COM_RECEIVE,0,port);
		buf_init();
		if (!gs(comm))
		switch(comm[0])
		{
			case	'S':	ps("G");
					if (dump())
						Err();
					break;
			case	'M':    getbasename();
					ps("N");
					break;
			case	'N':	closemodule();
					ps("M");
					break;
			default:	Err();
					break;
		}
		else
			Err();
	}

	com_off();
	return	0;
}


/*
	Get a string.  Return 0 if okay.
*/
int	gs(char *s)
{
	int	ch;
	int	p;
	unsigned len;



//	len=bioscom(_COM_RECEIVE,0,port);
	len=com_read();
	if (len>=256)
		return 1;

	p=0;
	while (len)
	{
		if (kbhit())
		{
			printf("\nUser break\n");
			return 1;
		}
//		ch=bioscom(_COM_RECEIVE,0,port);
		ch=com_read();
		if (len>=256)
			return 1;
		s[p]=ch;
		p++;
		len--;
	}
	s[p]=0;

	return	0;
}


/*	Do a checksum on the string:
	:aaaa:xx:yy:zzzzzzzzzzzzzzzzzzz;

	Return *bytes=xx,
	       *buff=zzzzz,
	       0 if checksum of zzzz is yy,
	       1 if not.

*/

unsigned src(unsigned x,char c)
{
asm	{
	mov	ax,x
	mov	cl,c
	ror	ax,cl
	}
	return	_AX;
}

int	cs(unsigned char *in, unsigned char *out, unsigned *rlen, unsigned addr)
{
	unsigned check,mcheck;
	unsigned len,pos;
	unsigned maddr;


	if (sscanf(in,":%4X:%2X:%2X:",&maddr,&len,&check)!=3)
		return 1;

	*rlen=len;
	if (maddr!=addr)
		return 1;

	mcheck=0;
	pos=0;
	in+=12;
	while (len)
	{
		mcheck+=(src(*in,pos&15));
		pos++;
		*out++=*in++;
		len--;
	}

	if ((mcheck&255)==check && *in==';')
		return 0;
	else
		return 1;


}


char	diskname[14];

char	basename[12];
char	title[32];
unsigned parts;

#define	P_ROM1		1
#define	P_ROM2		2
#define	P_BANKED	(P_ROM1+P_ROM2)
#define	P_GROM		4
#define	P_MINIMEM	8

void	getbasename(void)
{
	printf("\n\nReceiving a module.\n"
	       "Enter the base name for this module.  This is a 7-letter word used\n"
	       "in the filename to organize all the binary segments of the module.\n"
	       "(Example for Extended BASIC:  extbas)\n\n: ");
	basename[0]=8;
	cgets(basename);

	printf("\n\nEnter the name of the module.\n\n: ");
	title[0]=31;
	cgets(title);
}


void	getdiskname(void)
{
	printf("\n\nReceiving a disk image.\n"
	       "Enter the filename for the disk, as an 8-letter word.\n"
	       "The extension \".DSK\" will automatically be added.\n"
	       "(The disk image will be stored in the DISKS\ subdirectory.)"
	       "\n\n: ");
	diskname[0]=9;
	cgets(diskname);

}


void	closemodule(void)
{
	char	ch;
	FILE	*mi;
	struct 	stat st;
	char	entry[80];

	while (kbhit()) ch=getch();

	printf("\n\nComplete module received.\n"
	       "Do you wish to add an entry for '%s' in your\n"
	       "MODULES.INF file (y/n)?",title+2);

	do
	{
		ch=toupper(getch());
	}	while (ch!='Y' && ch!='N');

	printf("%c\n\n",ch);

	sprintf(entry,"%c%s%c,%s",34,title+2,34,basename+2);
	if ((parts&P_BANKED)==P_BANKED)
		sprintf(entry+strlen(entry),",BANKED");
	else
	if (parts&P_ROM1)
		sprintf(entry+strlen(entry),",ROM");

	if (parts&P_GROM)
		sprintf(entry+strlen(entry),",GROM");
	if (parts&P_MINIMEM)
		sprintf(entry+strlen(entry),",MMRAM");

	if (ch=='Y')
	{
		if (stat("MODULES.INF",&st))
		{
			printf("MODULES.INF does not exist.  Creating it anyway.\n");
		}
		mi=fopen("MODULES.INF","w+");
		if (mi==NULL)
		{
			printf("Couldn't create MODULES.INF!\n");
			return;
		}
		fprintf(mi,"%s\n",entry);
		fclose(mi);
	}
	else
	{
		printf("All right then.  Be sure to add this line to MODULES.INF sometime\n"
		       "to be able to add this module to your startup selection list:\n\n");
		printf("%s\n",entry);
	}
}



int	dump(void)
{
	unsigned ch;
	unsigned char	str[100];
	unsigned code;
	struct 	matchup
	{
		unsigned code;
		char *fn;
		unsigned saddr;

	}	matches[]=
	{{'CR',"994AROM.BIN",0x0},
	 {'CG',"994AGROM.BIN",0x0},
	 {'SR',"SPCHROM.BIN",0x0},
	 {'MC',"?C.BIN",0x6000},
	 {'MM',"?C.BIN",0x6000},
	 {'MD',"?D.BIN",0x6000},
	 {'MG',"?G.BIN",0x6000},
	 {'DR',"DISK.BIN",0x4000},
	 {'RR',"RS232.BIN",0x4000},
	 {'D0',"*.DSK",0x0},
	 {'D1',"*.DSK",0x0},
	 {'D2',"*.DSK",0x0},
	 {'D3',"*.DSK",0x0},
	 {'D4',"*.DSK",0x0},
	 {'D5',"*.DSK",0x0},
	 {'D6',"*.DSK",0x0},
	 {'D7',"*.DSK",0x0},
	 {0,"UNKNOWN.BIN",0x0}
	 };

	int	match;
	char	filename[66];
	char	*fn;

	struct	stat st;
	int	handle;

	unsigned left;
	unsigned char buff[80];
	unsigned bytes;
	unsigned addr;

	unsigned barlen,totlen;
	unsigned nbl;

	int	diskrest;


	printf("\n\nReceiving an image...\n");

	if (gs(str))
		return 1;			/* get header */

	if (str[0]!='#')
		return 1;

	code=(str[1])+(str[2]<<8);
	diskrest=0;
	if (sscanf(str+3,"#%4X#",&left)!=1)
	{
		printf("Bad header\n");
		return 1;
	}

	match=0;
	while (matches[match].code)
	{
		if (code==matches[match].code)
			break;
		match++;
	}

	if (matches[match].code==0)
		printf("Invalid image type received (%c%c).  Saving UNKNOWN.BIN.\n"
			,str[1],str[2]);

	if (matches[match].fn[0]=='?')
	{
		strcpy(filename,"MODULES\\");
		strcat(filename,basename+2);
		strcat(filename,matches[match].fn+1);
		fn=filename;

		if (code=='MC')	parts|=P_ROM1;
		if (code=='MM')	parts|=P_ROM1|P_MINIMEM;
		if (code=='MD') parts|=P_ROM2;
		if (code=='MG') parts|=P_GROM;
	}
	else
	if (matches[match].fn[0]=='*')
	{
		strcpy(filename,"DISKS\\");
		if (code=='D0')
			getdiskname();
		strcat(filename,diskname+2);
		strcat(filename,matches[match].fn+1);
		fn=filename;
		diskrest=code!='D0';
	}
	else
	{
		strcpy(filename,"ROMS\\");
		strcat(filename,matches[match].fn);
		fn=filename;
	}


	while ((!stat(fn,&st)) && !diskrest)
	{
		printf("\n\n%s already exists.  Enter a new filename\n"
		       "(or press <Enter> if you want to overwrite it):\n\n: "
		       ,fn);
		str[0]=62;
		cgets(str);
		if (str[2]==0)
			break;
		strcpy(filename+2,str+2);
		fn=filename+2;
	}


	if (!diskrest)
	do
	{
		printf("\nCreating %s...\n",fn);
		handle=open(fn,O_CREAT|O_TRUNC|O_BINARY|O_RDWR,S_IREAD|S_IWRITE);
		if (handle==-1)
		{
			printf("%s could not be created.  Give a new filename for it.\n"
			       "(The disk may be full.)\n\n: ",fn);
			filename[0]=62;
			fn=cgets(filename);
		}
	}	while (handle==-1);
	else
	{
		printf("\nAdding to %s...\n",fn);
		handle=open(fn,O_BINARY|O_RDWR);
		if (handle==-1)
		{
			printf("%s could not be reopened!  The disk may be full.\n\n");
			return 1;
		}
		lseek(handle,(str[2]-'0')*45*1024L,SEEK_SET);
	}

//	bioscom(_COM_SEND,1,port);		/* ack */
//	bioscom(_COM_SEND,'H',port);		/* ack */
	ps("H");

	printf("\nWaiting for %u bytes...\n",left);
	addr=matches[match].saddr;
	barlen=0;
	totlen=left;
	while (left)
	{
		if (gs(str))
		{
			if (kbhit())
				return 1;
//			bioscom(_COM_SEND,1,port);
//			bioscom(_COM_SEND,'R',port);
			ps("R");
			printf("\010r");
		}
		else
		if (cs(str,buff,&bytes,addr))
		{
//			bioscom(_COM_SEND,1,port);
//			bioscom(_COM_SEND,'R',port);
			ps("R");
			printf("\010r");
		}
		else
		{
//			bioscom(_COM_SEND,1,port);
//			bioscom(_COM_SEND,'O',port);
			if (write(handle,buff,bytes)!=bytes)
			{
				printf("\nDisk full!\n");
				ps("");
				return 1;
			}
			left-=bytes;
			addr+=bytes;
			nbl=(long)(addr-matches[match].saddr)*80/totlen;
			while (nbl>barlen)
			{
				printf(".");
				barlen++;
			}
			ps("O");
		}
	}

	close(handle);

	if (!gs(str) && str[0]=='E')
	{
//		bioscom(_COM_SEND,1,port);
//		bioscom(_COM_SEND,'F',port);
		ps("F");
		printf("\n\nSuccessfully received image.\n\n");
		return 0;
	}
	else
	{
		printf("\n\nEnd-of-file not received... assuming success.\n");
		return 1;
	}

}
