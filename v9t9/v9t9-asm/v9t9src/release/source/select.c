/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Module for MODULES.EXE which lets user select and tag files.
*/

#define __SELECT__

#include <types.h>
#include <conio.h>
#include <stdlib.h>
#include <stdio.h>
#include <memory.h>
#include <string.h>

#include "select.h"

typedef	unsigned char byte;
typedef	unsigned int word;
typedef unsigned long longint;


int	screen(void)
{
	char	scrn[80*25*2];
static	char	*mess="V.9.t.9. .ù. .";
	int	count;
	char	*ptr,*eptr;
static	char	*footer="Arrows: Move  <Space>: Tag/Untag  <F1>: Default  <Enter>: Save  <Esc>: Abort";

	for (count=1; count<strlen(mess); count+=2)
		mess[count]=BLUE*16+LIGHTBLUE;

	ptr=mess;
	eptr=mess+strlen(mess);
	for (count=0; count<80*25*2; count++)
	{
		scrn[count]=*ptr++;
		if (ptr>=eptr)
			ptr=mess;
	}

	textmode(C80);
	window(1,1,80,25);
	textbackground(LIGHTGRAY);
	textcolor(WHITE);

	puttext(1,1,80,25,scrn);

	textbackground(LIGHTGRAY);
	textcolor(BLACK);
	gotoxy(1,25);
	for (count=0; count<40-strlen(footer)/2; count++)
		cprintf(" ");
	cprintf(footer);
	clreol();

	return 1;
}

int	selectwindow(void)
{
	int	row,col;
	char	scrn[61-19+1][2];
	struct	text_info t;

	window(19,1,61,24);
	textbackground(CYAN);
	textcolor(BLACK);
	clrscr();

	memset(scrn,0x20,sizeof(scrn));
	for (col=0; col<43; col++)
		scrn[col][1]=CYAN*16+BLACK;

	scrn[0][0]=scrn[42][0]='º';
	scrn[0][1]=scrn[42][1]=CYAN*16+BLUE;

	gettextinfo(&t);
	for (row=t.wintop; row<=t.winbottom; row++)
		puttext(t.winleft,row,t.winright,row,scrn);

	window(20,1,60,24);

	return	1;
}


int	isselected(int num)
{
	int	index;

	index=0;
	while (selected[index])
	{
		if (selected[index]==num)
			return index+1;
		index++;
	}

	return 0;
}

byte	tb,tc;

char *	addto(char *ptr, char *mess)
{
	byte	cl;

	cl=(tb<<4)+tc;
	while (*mess)
	{
		*ptr++=*mess++;
		*ptr++=cl;
	}
        *ptr=0;
	return ptr;
}


void	wmemset(byte *what, word i, word len)
{
asm	{
	push	es
	mov   	ax,ds
	mov	es,ax
	mov	di,what
	mov	cx,len
	mov	ax,i
	rep	stosw
	pop	es
	}
}

int	drawlist(int sel)
{
	int 	toprow;
	int	i;
	char	line[90];
	char	temp[10];
	char	*lptr;
	int	which;
	int	num;


	struct	text_info t;
	struct	modrec m;

	if (nummods<24)
		toprow=0;
	else
	{
		toprow=sel-12;
		if (toprow<0)
			toprow=0;
		if (toprow+24>nummods)
			toprow=nummods-24;
	}

	gettextinfo(&t);
	for (i=0; i<(nummods>=24 ? 24 : nummods); i++)
	{
		num=i+toprow;

		_fmemcpy(&m,&mods[num],sizeof(struct modrec));

		if (num==sel)
		{
			if (defaultmodule &&
			    defaultmodule==isselected(num+1))
				tc=YELLOW;
			else
				tc=(WHITE);
			tb=(BLACK);
		}
		else
		{
			tb=(CYAN);
			if (isselected(num+1))
				if (defaultmodule==isselected(num+1))
					tc=YELLOW;
				else
					tc=(WHITE);
			else
				tc=(BLACK);
		}

		wmemset(line,(((tb<<4)+tc)<<8)+0x20,40);

		addto(line+16,m.title);

		line[40*2]=' ';
		line[40*2+1]=CYAN*16+YELLOW;
		if (toprow>0 && i==0)
			line[40*2]='';

		if (toprow+24<nummods && i==23)
			line[40*2]='';


		lptr=line;

		/* Tag location */

		tb=CYAN;
		tc=WHITE;

		if ((which=isselected(num+1))!=0)
			sprintf(temp,"%2d ",which);
		else
			strcpy(temp,"   ");

		lptr=addto(lptr,temp);

		/* Statistics */

		for (which=1; which<8; which+=which)
			if (m.opts&which)
			{
				if (m.exist&which)
					tc=(LIGHTCYAN);
				else
					tc=(DARKGRAY);

				lptr=addto(lptr,
				(which==1 ? "C" :
				(which==2 ? "D" : "G")));

			}
			else
				lptr=addto(lptr," ");

		lptr=addto(lptr," ");

		puttext(t.winleft,t.wintop+i,t.winright,t.wintop+i,line);
	}
	return	1;
}



int	removeitem(int i)
{
	int	index;

	for (index=i; index<32; index++)
		selected[index]=selected[index+1];

	selected[31]=0;

	return	1;
}


int	additem(int i)
{
	int	index;

	index=0;
	while (selected[index])
		index++;

	if (index>=32)
		index=31;

	selected[index]=i;

	return	1;
}


int	toggle(int sel)
{
	int	which;

	if ((which=isselected(sel+1))!=0)
	{
		if (which<=defaultmodule)
			defaultmodule--;
		return removeitem(which-1);
	}
	else
		return additem(sel+1);
}


int	select(void)
{
	int	sel;
	char	ch;
	int	quit;


	screen();
	selectwindow();

	_setcursortype(_NOCURSOR);

	sel=0;
	quit=0;
	while (!quit)
	{
		drawlist(sel);
		ch=getch();
		if (ch==0)
			ch=getch();
		switch (ch)
		{
		case	72:  	if (sel>=1) sel--;
				break;
		case	80:	if (sel<nummods-1) sel++;
				break;
		case	73:	if (sel>=6) sel-=6; else sel=0;
				break;
		case	81:	if (sel<nummods-6) sel+=6; else sel=nummods-1;
				break;
		case	71:	sel=0;
				break;
		case	79:	sel=nummods-1;
				break;
		case	59:     if (!isselected(sel+1))
					toggle(sel);
				defaultmodule=isselected(sel+1);
				break;
		case	32:	toggle(sel);
				break;
		case	13:	quit=2;
				break;
		case	27:	quit=1;
				break;
		}
	}

	_setcursortype(_NORMALCURSOR);
	window(1,1,80,25);
	textbackground(BLACK);
	textcolor(LIGHTGRAY);
	clrscr();
	return	quit-1;
}
