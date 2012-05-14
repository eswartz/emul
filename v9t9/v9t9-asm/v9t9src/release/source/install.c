/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	This program will install V9t9: the TI Emulator! v6.0 on
	someone's system.

	This program DOES NOT CARE if a user has TI Emulator! v4.0
	or v5.01 installed already.  Its only purpose is to install
	the new version and create a config file.

	It will ask for some defaults, and fill in the V9t9.DFL
	skeleton with them.

*/

#include <conio.h>
#include <ctype.h>
#include <dir.h>
#include <dos.h>
#include <errno.h>
#include <io.h>
#include <memory.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys\types.h>
#include <fcntl.h>
#include <sys\stat.h>

#include "decoder.h"


void	footer(char *s);
void	screen(void);
void	header(char *s);
void	cleanup(void);
int	getescorenter(void);
int	intro(void);
int	getpath(void);
int	checkfiles(void);
int	checkage(void);

#define	E_HALT	1
#define	E_RETRY	2
#define	E_CONT	3

int	error(char *mess, int flags);
int	config(void);
int	decode(void);
int	readdocs(void);
int	congrats(void);


char	installpath[66];
int	installdemos;
char	installdemopath[66];
char	key[16];

int	main(int argc, char *argv[])
{
	if (argc>1)
	{
	printf("V9t9: TI Emulator! v6.0 Installation Program\n"
	       "\n"
	       "This program is used to do a fresh installation of V9t9.\n"
	       "It requires no command-line parameters and is interactive.\n"
	       "You can delete this program after installation.\n");
	exit(0);
	}

	textmode(C80);

	header("");
	footer("");

	if (!checkfiles())
	if (!intro())
	if (!checkage())
	if (!getpath())
	if (!decode())
	if (!config())
	{
		congrats();
		readdocs();
	}


	cleanup();
	return	0;
}


void	header(char *s)
{
	window(1,1,80,5);
	textbackground(MAGENTA);
	textcolor(YELLOW);
	clrscr();

	cprintf(" ษออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออป "
		" บ  V9t9:  TI Emulator! v6.0 Installation Program                     7/1995  บ "
		" บ  ฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤ        by Edward Swartz  บ "
		" บ  %-72s  บ "
		" ศออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออผ"
		,s);


}

void	screen(void)
{
	window(1,6,80,24);
	textbackground(BLUE);
	textcolor(WHITE);
}


void	footer(char *s)
{
	window(1,25,80,25);
	textbackground(LIGHTGRAY);
	textcolor(BLACK);
	clrscr();
	cprintf("    %s",s);
}

void	cleanup()
{
	textbackground(BLACK);
	textcolor(LIGHTGRAY);
	window(1,1,80,25);
	clrscr();
	_setcursortype(_NORMALCURSOR);
}


int	getescorenter(void)
{
	char	ch;

	do
	{
		ch=getch();
	}	while (ch!='\r' && ch!=27);

	return	ch==27;
}


int	getenter(void)
{
	char	ch;

	do
	{
		ch=getch();
	}	while (ch!='\r');

	return	0;
}


void	printwordwrap(char *st)
{
	char	*en;
	char	*nl;
	char	buff[80];

	while (st)
	{
	    en=st+70;			/* we want 70 chars at most */
	    nl=st+strcspn(st,"\n\0");	/* but we'll stop at newline or null */
	    if (nl<en)
		    en=nl;

	    if (*en)
	    while (*en!=' ' && en>st)	/* don't break words */
		    en--;

	    memcpy(buff,st,en-st);
	    buff[en-st]=0;

	    cprintf("     %s\r\n",buff);

	    if (*en)
	       st=en+1;
	    else
	       break;
	}
}


int	error(char *mess, int flags)
{
	int	ret;

	screen();
	textbackground(RED);
	textcolor(WHITE);
	clrscr();

	printwordwrap(mess);

	if (flags==E_RETRY)
		footer("Press <Enter> to retry or <Esc> to abort.");
	else
	if (flags==E_HALT)
		footer("Press <Enter> to abort installation.");
	else
		footer("Press <Enter> to continue.");

	if (flags==E_HALT)
	{
		getenter();
		cleanup();
		exit(100);
	}

	if (flags==E_RETRY)
		ret=getescorenter();
	else
	if (flags==E_CONT)
		ret=getenter();

	screen();
	clrscr();
	return	ret;
}


int	checkfiles(void)
{
	struct 	stat st;

	header("File check");
	screen();

	if (stat("V9t9_6.PKG",&st) ||
	    stat("VDEM_6.PKG",&st) ||
	    stat("INSTALL.EXE",&st) ||
	    stat("V9t9.DFL",&st))
	error("\n\n\n\n\n\n\n"
	      "Could not find one of the files V9t9_6.PKG, VDEM_6.PKG, INSTALL.EXE, "
	      "or V9t9.DFL.  Be sure you're running INSTALL from the "
	      "directory where you unzipped the 600V9t9.ZIP archive.",
	      E_HALT);

	return 0;
}



int	intro(void)
{

	header("General introduction to V9t9");
	strcpy(key,"V9t9");

	screen();
	clrscr();
	_setcursortype(_NOCURSOR);

printwordwrap("\n\n"
	"V9t9 is a nearly complete TI-99/4A emulator which runs on 286+ "
	"AT systems.  It implements the full 9900 instruction set, standard "
	"99/4A graphics, three-voice sound and noise, disk drives, "
	"keyboard, joysticks, serial and parallel ports, "
	"to name a few things. "
	"\n"
	"In addition, a demonstration capability, a debugger, and many "
	"run-time functions are built-in. "
	"\n"
	"The minimum system recommended to run this program is a "
	"386-DX/33 with VGA, though it will run on a 286 "
	"with EGA.  I develop the program on a 386-SX/20 and can "
	"withstand it.  :)  "
	"\n"
	"An AT-style 101-key keyboard is required."
	);

footer( "Press <Enter> to continue, or <ESC> to exit the INSTALL program.");

	if (getescorenter())
		return 1;

	screen();
	clrscr();


printwordwrap("\n\n"
	"With a PC speaker, you can have three-voice arpeggiated sound "
	"and speech.  With an Adlib card or Sound Blaster, you have real "
	"three-voice sound, speech, and periodic noise.  On SB's "
	"equipped with the CT-VOICE.DRV driver, you can have real "
	"noise. "
	"\n"
	"V9t9 supports hardware-level keyboard scans.  It allows "
	"patching to speed up the CPU ROM (for slow computers) and "
	"patching to delay the keyboard speed (for fast computers). "
	"\n"
	"New in this version is real speech synthesis.  BUT, until I get "
	"it perfect, it sounds a little sketchy. "
	"\n"
	"Also new is direct RS232 and PIO support. They appear to work "
	"nicely, "
	"but they are relatively new. "
	);


	if (getescorenter())
		return 1;

	screen();
	clrscr();


printwordwrap("\n\n"
	"Note that there is no 'registered version' of this program.  "
	"The executable in this archive supports all the functions I "
	"just mentioned.  This emulator can do almost anything a 99/4A can: "
	"\n"
	"þ  Runs all BASIC, Extended BASIC, and assembly programs \n"
	"þ  Handles two-bank (16k) Atarisoft cartridges \n"
	"þ  Allows use of Mini Memory's RAM (can't save it yet, tho) \n"
	"þ  Runs all those fun TI games \n"
	"þ  ... \n"
	"\n"
	"I don't need to point it ALL out, do I?"
	);

	if (getescorenter())
		return 1;

	screen();
	clrscr();

printwordwrap("\n"
	"For copyright reasons, the 99/4A console ROMs "
	"cannot "
	"be distributed with this emulator.  "
	"However, a FORTH-based ROM that I wrote has been included "
	"in this archive.  With it you can send a ROM transfer program over "
	"a serial line to your 99/4A (provided you have an RS232, memory expansion, "
	"a disk drive, and Editor/Assembler or Extended BASIC).  "
	"The transfer program will send your console ROMs, "
	"disk and RS232 ROMs, Editor/Assember and Extended BASIC, and even "
	"disk images to your PC for use with V9t9. "
	"\n"
	"Or, if you want the easy way out, the console ROMs are available "
	"from me through my software license with TI.  The package is $15.  "
	"In addition, copies of quite a few "
	"TI modules are also available for $1 each.  \n"
	"Unfortunately I can't distribute the disk, RS232, or speech ROMS "
	"under my license.  I have constructed emulated ROMs for all "
	"these devices, however, which allows some degree of usability. "
	);

	if (getescorenter())
		return 1;

	screen();
	clrscr();

printwordwrap("\n"
	"If your 99/4A system is dead or unable to use the ROM transfer "
	"program, you might have to send me money just to see the "
	"emulator in action, running familiar programs.  I dislike the "
	"'fee-before-you-see' racket that defines commerical software.  "
	"That's why this program is fairware. "
	"\n"
	"So, in an effort to "
	"compensate for the copyright laws which prevent me from freely "
	"distributing the ROMs, this package includes several "
	"demonstrations of V9t9 running popular 99/4A programs.  "
	"The demos accurately show how "
	"the emulator looks, sounds, and runs.  True "
	"emulation will most likely "
	"go much faster on your system, as most of the demos were "
	"recorded on my 386-SX/20. "
	"\n\n"
	"If this program interests you, press <Enter> to begin installation, "
	"or <Esc> to abort.");

footer("Do what the man says.");

	return getescorenter();
}




#define	MONTH 2678400

int	checkage(void)
{
	time_t	now;
	time_t	install;
	struct	stat st;

	strcat(key,"go");

	stat(_argv[0],&st);
	now=time(NULL);			/* get current time */
	install=st.st_mtime;

	header("Archive age check");
	screen();
	clrscr();
	footer("Press <Esc> to look for a newer version or <Enter> to continue anyway.");

	if (now>install+MONTH*36)	/* 3 years?! */
	{
	printwordwrap("\n\n\n\n\n\n\n"
		"If your clock is correct, then this archive is over three "
		"years old!  What are you, an antique collector?!  Wow.  "
		"Don't try to order or contact me because in all probability "
		"I have moved away and my e-mail addresses have expired. "
		"(Is DOS still alive...?)"
		);
		return getescorenter();
	}

	if (now>install+MONTH*12)	/* a year */
	{
	printwordwrap("\n\n\n\n\n\n\n"
		"If your clock is correct, then this archive is over a "
		"year old!  Consider looking for a new archive, because "
		"I likely do not support this version any longer.");
		return getescorenter();
	}

	if (now>install+MONTH*4)	/* four months */
	{
	printwordwrap("\n\n\n\n\n\n\n"
		"If your system clock is right, then this archive is over "
		"four months old.  Consider looking for a newer version, "
		"since I don't wholeheartedly support older archives.");
		return getescorenter();
	}
	else
		return 0;
}


int	input(char *buf, int len)
{
	char	buff[80];
	char	ch;
	int	quit=0;
	int	pos=0;
	int	oldx=wherex();
	int	oldy=wherey();

	_setcursortype(_NORMALCURSOR);

	memset(buff,32,80);
	buff[len]=0;
	cprintf("     ");

	textbackground(CYAN);
	textcolor(BLACK);

	cprintf(buff);

	memset(buff,0,80);
	strcpy(buff,buf);

	gotoxy(6,oldy);
	cprintf(buff);
	pos=strlen(buff);
	while (!quit)
	{
		gotoxy(pos+6,wherey());
		ch=getch();
		if (ch==27)
			return 1;
		if (ch==13)
			quit=1;
		else
		if (ch==0)
			ch=getch(); 		/* ignore functions */
		else
		if (ch==8)
			if (pos>0)
			if (buff[pos]!=0 && pos!=len)
			{
				cprintf("\010");
				pos--;
			}
			else
			{
				if (pos==len)
				{
				    buff[pos]=0;
				    cprintf(" \010");
				}
				cprintf("\010 \010");
				pos--;
				buff[pos]=0;
			}
			else ;
		else
		if (ch>32 && ch<127)
		{
			if (pos<len)
			{
				cprintf("%c",ch);
				buff[pos++]=ch;
			}
			else
			{
				cprintf("%c\010",ch);
				buff[pos]=ch;
			}
		}
	}

	strcpy(buf,buff);
	_setcursortype(_NOCURSOR);
	screen();
	gotoxy(oldx,oldy);
	cprintf("\r\n");

	return 0;
}


int	inputpath(char *path, char *ex)
{
	char	buff[80];
	char	newp[80];
	int	illegal;

	sprintf(buff,"\nEnter the directory (ex: %s): ",ex);
	printwordwrap(buff);

	strcpy(newp,path);
	do
	{
		if (input(newp,48))
			return	1;

		strupr(newp);

		illegal=(strlen(newp)<=3 || newp[1]!=':' ||
		       !isalpha(newp[0]) ||
		       newp[2]!='\\');

		if (illegal)
			printwordwrap("Please specify the drive and root directory (e.g. C:\\) in "
				      "the path. "
				      "\n");
		else
		if (strpbrk(newp+2,"-<>=,;:*?[]()/!~")!=NULL)
		{
			printwordwrap("The given path has illegal "
			"characters in it.  Try using alphanumerics. ");
			illegal=1;
		}
		else
		if (setdisk(getdisk())<newp[0]-65)
		{
			printwordwrap("The given drive is not recognized "
			"by DOS.");
			illegal=1;
		}

	}	while (illegal);


	_fullpath(path,newp,80);

	if (path[strlen(path)-1]!='\\')
		strcat(path,"\\");
	return 	0;
}


int	getyesorno(int *ans)
{
	char 	ch;

	_setcursortype(_NORMALCURSOR);

	do
	{
		ch=toupper(getch());
	}	while (ch!='Y' && ch!='N' && ch!=27);

	_setcursortype(_NOCURSOR);
	if (ch==27)
		return 1;

	*ans=ch;
	return	0;
}


int	drivefree(void)
{
	struct	diskfree_t dfree;
	int	drive,last;
	longint	avail;
	char	buff[80];

	last=setdisk(getdisk());

	for (drive=3; drive<last; drive++)
	{
		if (!_dos_getdiskfree(drive,&dfree))
		{
		avail = (long) dfree.avail_clusters
		      * (long) dfree.bytes_per_sector
		      * (long) dfree.sectors_per_cluster;
		sprintf(buff,"Drive %c has %10ld bytes free. ",drive+64,avail);
		printwordwrap(buff);
		}
	}

}


int	getpath(void)
{
	struct	stat st;
	char	buff[800];
	long	size;
	int	answer;

	strcat(key,"In");
	header("Installation directories");
	footer("Enter a string, or press <Esc> to abort.");
	screen();
	clrscr();

	printwordwrap("\n"
	"Glad you want to try V9t9 out. ");

	drivefree();

	stat("V9t9_6.PKG",&st);
	size=(st.st_size+999)/1000;
	sprintf(buff,"\nSelect the directory where you want to install "
		"V9t9.  (Do NOT install in the same directory as a previous "
		"version of TI Emulator!)   "
		"The archive will probably expand to %ld bytes, "
		"so select a drive with enough space. ",size*2000);

	printwordwrap(buff);

	printwordwrap("\n"
	"(If you're going to install the archive of "
	"demonstration files, they don't have to be on the same drive "
	"as the rest of V9t9.  So don't worry about its space requirements "
	"yet.) "
	);


	strcpy(installpath,"C:\\V9T9\\V6.0");
	if (inputpath(installpath,installpath))
		return 1;

	footer("Press 'Y' or 'N' to answer, or <Esc> to abort installation.");
	screen();
	clrscr();

	printwordwrap(" ");
	drivefree();

	stat("Vdem_6.PKG",&st);
	size=(st.st_size+999)/1000;
	sprintf(buff,"\nThe V9t9 demonstrations in this archive will probably "
	"take up anywhere from %ld to %ld bytes.  Do you want to install them now?  "
	"(If not, a VDEM_6.ZIP file will be left in the %sDEMOS\\ "
	"directory for you to unpack later.) "
	"\n",size*2000,size*4000,
	installpath);

	printwordwrap(buff);
	cprintf("     Your choice? ");

	if (getyesorno(&answer))
		return 1;

	installdemos=(answer=='Y');
	strcpy(installdemopath,installpath);
	strcat(installdemopath,"DEMOS\\");
	if (installdemos)
	{
		header("Demonstration files directory");
		footer("Enter a directory or press <Esc> to not install demos.");
		screen();
		clrscr();
		printwordwrap("\n\n"
		"Okay, cool.  Pick the directory where you want to install the "
		"demonstration "
		"files.  Again, this can be on any drive in your system with enough "
		"space.  Your configuration file will be set up to find demos here "
		"by default. "
		);

		if (inputpath(installdemopath,installdemopath))
			installdemos=0;			/* don't */
	}

	strcat(key,"G");
	return 0;
}


int	installerror(char *s)
{
	char	buffer[80];

	header("Install error");
	footer("Press <Enter> to exit installation.");
	screen();
	clrscr();


	printwordwrap("\n\n\n\n\n\n\n"
	"There was an error unzipping the archive.");

	if (errno==ENOMEM)
	printwordwrap("You do not have enough memory free.");
	else if (errno==ENOEXEC)
	printwordwrap("The archive has been corrupted.");
	else if (errno==ENOENT)
	{
		sprintf(buffer,"Could not find %s",s);
		printwordwrap(buffer);
	}
	else
	printwordwrap(sys_errlist[errno]);

	printwordwrap("\n"
	"Press <Enter>...");
	getenter();
	return	1;
}


int	copyover(char *fn)
{
	char	oldp[80];
	char	newp[80];

	struct	stat st;

	int	i,o;
	char	buff[1024];
	int	len;

	sprintf(oldp,"%s",fn);
	sprintf(newp,"%sDEMOS\\%s",installpath,fn);

	remove(newp);

	if (rename(oldp,newp)==0)
		return 	0;			/* easiest way */


	i=open(oldp,O_RDONLY|O_BINARY);
	if (i==-1)
	{
		error("Could not open the demo archive!",E_CONT);
		return 1;
	}
	o=open(newp,O_RDWR|O_BINARY|O_TRUNC|O_CREAT,S_IREAD|S_IWRITE);
	if (o==-1)
	{
		close(i);
		error("Could not move the demo archive!",E_CONT);
		return 1;
	}

	fstat(i,&st);
	while (st.st_size)
	{
		len=(st.st_size < 1024 ? st.st_size : 1024);
		if (read(i,buff,len)!=len)
		{
			close(i);
			close(o);
			error("Read error on the demo archive!",E_CONT);
			return 1;
		}
		if (write(o,buff,len)!=len)
		{
			close(i);
			close(o);
			error("Write error on the demo archive!",E_CONT);
			return 1;
		}
		st.st_size-=len;
	}

	close(o);
	close(i);

	remove(oldp);
	return	0;
}



int	decode(void)
{
	char	fn[100];
	int	err;

	header("Unpacking files");
	footer("");
	screen();
	clrscr();

	printwordwrap("\n\n\n\n\n"
	"INSTALL is now unencrypting the V9t9 archive... \n");

	if (decoder("V9t9_6.PKG","V9t9_6.EXE",key))
	{
		printwordwrap("\n\n??? Couldn't find V9t9_6.PKG! "
		"\n"
		"Press <Enter> to abort, and be sure you run INSTALL from "
		"the directory that contains this file.");
		getenter();
		return 1;
	}

	printwordwrap("About to unzip the archive...");
	delay(4000);

	cleanup();

	sprintf(fn,"V9t9_6 -d -o -s%s %s",key,installpath);
	err=system(fn);
//	err=system("editor.exe");


	if (err)
		return installerror("V9t9_6.EXE");

	delay(3000);
	remove("V9t9_6.EXE");

	header("Installing demos");
	footer("");
	screen();
	clrscr();

	printwordwrap("\n\n\n\n\n"
	"INSTALL is now unencrypting the demonstration archive... \n");

	if (decoder("Vdem_6.PKG","Vdem_6.EXE",key))
	{
		printwordwrap("\n\n??? Couldn't find VDEM_6.PKG! "
		"\n"
		"Press <Enter> to abort, and be sure you run INSTALL from "
		"the directory that contains this file.");
		getenter();
		return 1;
	}

	if (!installdemos)
	{
		printwordwrap("INSTALL is now moving the demonstration "
		"archive to the V9t9 \DEMOS directory for later unzipping. \n");
		if (copyover("VDEM_6.EXE"))
			printwordwrap("Could not move VDEM_6.EXE! "
			"Please do it manually "
			"or reinstall after you free some space. \n");
		printwordwrap("Press <Enter> to continue.");
		getenter();
		return	0;
	}


	printwordwrap("About to unzip the archive...");
	delay(4000);

	cleanup();

	sprintf(fn,"VDEM_6 -d -o %s",installdemopath);
	err=system(fn);

	if (err)
		return installerror("VDEM_6.EXE");

	delay(3000);
	remove("VDEM_6.EXE");

	header("");
	footer("");
	screen();
	clrscr();

	return	0;

}


int	getnumber(int low, int hi)
{
	char	buff[80];
	int	temp;

	do
	{
		sprintf(buff,"Enter your choice (%d-%d): ",low,hi);
		printwordwrap(buff);
		buff[0]=0;
		if (input(buff,5))
			return low;
		temp=atoi(buff);
	}
	while 	(temp<low || temp>hi);

	return	temp;
}


#define	END(x) (*((x)+strlen(x)-1))

char	*getstring(char *buf, int len, FILE *fptr, int fixed)
{

	char	ch;
	int	x;

	flushall();

	memset(buf,0x20,len);
	fgets(buf,len,fptr);

	while (  (*buf) &&
		 ( (ch=END(buf)) =='\n' || ch=='\r')  )
		if (*buf)
			END(buf)=0;

	if (fixed)
		for (x=0; x<len; x++)
			buf[x]=(buf[x]==0 ? 0x20 : buf[x]);


	return buf;
}


int	config(void)
{
	static	int videos[]={6,4,4,4,3,3,2,2,2,1,1,1};
	static	int ROMkeys[]={1,10,50,200,400,800,1600,3000,4000,5000,6000,7000};
	static  int noises[]={500,400,300,200,100,50,0,0,0,0,0,0};

	char	line[160];
	char	temp[80];
	int	proc;
	int	fill;
	FILE	*in,*out;
	int	which;

	header("V9t9 v6.0 Configuration");
	footer("");
	screen();
	clrscr();

	printwordwrap("\n"
	"V9t9 comes with a default configuration file "
	"which should suit most of your needs.  However, there are some "
	"items that depend on your particular system. "
	"\n"
	"Most of these depend directly on your processor's speed.  Choose "
	"the processor which most closely matches your setup: "
	"\n"
	"1) 286-12       2) 386-SX/16     3) 386-SX/33     4) 386-DX/16 \n"
	"5) 386-DX/33    6) 386-DX/40     7) 486-25        8) 486-33 \n"
	"9) 486-50      10) 486-66       11) Pentium-60   12) Pentium-90 \n");

	proc=getnumber(1,12)-1;

	if (proc>1)
		printwordwrap("Wow, you have a great system.");


	printwordwrap("Generating V9t9.CNF configuration file...");

	for (which=0; which<2; which++)
	{
	in=fopen("v9t9.dfl","r");
	if (in==NULL)
		error("\n\n\n\n\n??? Couldn't open V9t9.DFL. "
		"\n"
		"Be sure you are running from the directory where INSTALL "
		"is located.  You'll need to unzip 600V9t9.ZIP again.",
		E_HALT);

	sprintf(temp,(which==0 ? "%sV9t9.CNF" : "%sFORTH.CNF"),installpath);
	out=fopen(temp,"w");
	if (out==NULL)
		error("\n\n\n\n\n!!! Couldn't create configuration file. "
		"\n"
		"Probably your disk is full.  Later, copy V9t9.DFL from "
		"this directory, name it V9t9.CNF, and put in your own "
		"values.  Either that, or free some space, and run INSTALL "
		"again after "
		"re-unzipping 600V9t9.ZIP.",E_HALT);

	while (!feof(in))
	{
	getstring(line,160,in,0);
	if (line[0]=='%')
	{
		memcpy(temp,line+1,3);
		temp[3]=0;
		strcpy(line,line+4);
		fill=atoi(temp);
		switch (fill)
		{
		case	1:	sprintf(temp," %sDISK",installpath);
				break;
		case	2:	sprintf(temp," %sDISKS",installpath);
				break;
		case	3:	sprintf(temp," %sROMS",installpath);
				break;
		case	4:	sprintf(temp," %sMODULES",installpath);
				break;
		case	5:	sprintf(temp," %sSPEECH",installpath);
				break;
		case	6:	sprintf(temp," %d",videos[proc]);
				break;
		case	7:	sprintf(temp," %d",videos[proc]*3);
				break;
		case	8:	sprintf(temp," %s",proc>6 ? "True" : "False");
				break;
		case	9:	sprintf(temp," %d",ROMkeys[proc]);
				break;
		case	10:	sprintf(temp," %d",noises[proc]);
				break;
		case	11:	sprintf(temp," %s",proc>6 ?
					"+SlowDownKeyboard" :
					"+EmulateKeyboardROM");
				break;
		case	12:	sprintf(temp," %s",installdemopath);
				break;
		case	13:	sprintf(temp," %s",which==0 ?
							"994AROM.BIN" :
							"FORTH.ROM");
				break;
		case	14:	sprintf(temp," %s",which==0 ?
							"994AGROM.BIN" :
							"FORTH.GRM");
				break;
		case	15:	sprintf(temp," %s",which==0 ?
							"EMUSPCH.BIN" :
							"");
				break;

		case	16:	sprintf(temp," %s",which==0 ?
							"0":
							"1");
                		break;
		default:
				printwordwrap("Unknown value in V9t9.DFL!");
				break;

		}
		strcat(line,temp);
	}
	fprintf(out,"%s\n",line);

	}
	fclose(out);
	fclose(in);
	}
	return  0;
}


int	congrats(void)
{
	char	temp[400];

	header("Finished installation");
	footer("Press <Enter> to continue.");
	screen();
	clrscr();

	sprintf(temp,"\n\n\n\n\n\n"
	"There we go, all done.  V9t9 %s been installed in the "
	"directories you specified. \n",installdemos ?
	"and the demonstration files have" : "has");

	printwordwrap(temp);

/*	printwordwrap(
	"Two copies of the system and module configuration files "
	"(V9t9.CNF/FORTH.CNF and MODULES.INF/FORTH.INF) have been created.  "
	"V9t9.CNF and MODULES.INF are set up for generic emulation, "
	"and FORTH.INF and FORTH.CNF are set up to run the FORTH interpreter, "
	"using FORTH.BAT.  "
	"\n"
	"If you don't have any 99/4A ROMs yet, then you can run FORTH.BAT to use the "
	"FORTH interpreter that comes with V9t9, or just mess around with "
	"the demonstration files.  "
	"Otherwise, you can simply "
	"run V9t9.
	"\n"
	"See V9t9.TXT and TRANSFER.TXT for setup instructions in any case.  "
	"\n "
	);*/

	getenter();

	footer("Press <Enter> to read the documentation.");
	screen();
	clrscr();

	printwordwrap("\n\n"
	"After you press <Enter>, UTILS\\DOCS.EXE will be run to let you read "
	"and print the documentation.  Read V9t9.TXT carefully.  Most of "
	"the other documents describe specific aspects of V9t9, but some "
	"important ones are: "
	"\n"
	"þ  DEMOS.TXT    -- how to run (and record) demonstrations \n"
	"þ  TRANSFER.TXT -- how to transfer 99/4A ROMs to your PC \n"
	"þ  CONTACT.TXT  -- how to contact me \n"
	"þ  FAIRWARE.TXT -- fairware contract and ordering information \n"
	"þ  CONFIG.TXT   -- information on the V9t9.CNF configuration file \n"
	);

	getenter();
	return	0;
}


int	readdocs(void)
{
	installpath[strlen(installpath)-1]=0;	/* DOS doesn't like \ */
	chdir(installpath);
	setdisk(installpath[0]-65);

	cleanup();
	system("utils\\docs docs\\v9t9.txt");

	return	0;
}
