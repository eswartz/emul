/*
  seriald.c

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
#include <conio.h>
#include <dos.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>

#include "serial.h"

#define	TRCHAR	0
#define	DIVL	0
#define	INTENA	1
#define	DIVH	1
#define	INTID	2
#define	LNCTRL	3
#define	MDMCTRL	4
#define	LNSTAT	5
#define	MDMSTAT	6

unsigned base;

void	interrupt (*irqsave)();
void	interrupt (*irq4)();
void	interrupt (*irq5)();
char	org21;

char	serialirq;

void	com_die(int *reglist);

int	com_init(int port, unsigned baudrate, char irq)
{
	unsigned far *biosaddrs=(unsigned far *)0x400;
	unsigned sbaud;

	if (port<0 || port>3 || (base=biosaddrs[port])==0)
	{
		printf("\nPort %d is not supported by BIOS.\n",port+1);
		return 1;
	}

	printf("\n\n*** Initializing COM%d to BAUD=%d and IRQ=%d\n",port,baudrate,irq);

	outp(base+LNCTRL,inp(base+LNCTRL)|0x80);        /* access DLAB */

	if (baudrate!=110 && baudrate!=300 && baudrate!=600 &&
	    baudrate!=1200 &&  baudrate!=2400 && baudrate!=4800 &&
	    baudrate!=9600)
	{
		printf("\nInvalid baud rate.\n");
		return 2;
	}

	sbaud=115200l/baudrate;

	outp(base+DIVL,sbaud&255);
	outp(base+DIVH,sbaud/256);

//	outp(base+LNCTRL,inp(base+LNCTRL)&0x7f);
	outp(base+LNCTRL,0x03);				/* 8N1 */

	outp(base+MDMCTRL,0xb);			/* DTR and OUT2 */

	serialirq=irq;
	irqsave=getvect(0x8+irq);

	setvect(0x8+irq,com_int);

	inp(base+INTID);  inp(base+MDMSTAT);  inp(base);

	outp(base+MDMCTRL,inp(base+MDMCTRL)|0x83);  /* turn on interrupts */
	org21=inp(0x21);
	outp(0x21,inp(0x21)&(~(1<<irq)));	    /* turn on IRQ */

	outp(base+INTENA,(inp(base+INTENA)&0xf0)|0x1);
	outp(base+INTENA,(inp(base+INTENA)&0xf0)|0x1);

	buf_init();

	signal(SIGABRT,com_die);
	signal(SIGTERM,com_die);
	signal(SIGINT,com_die);

	return 0;
}


void	com_die(int *reglist)
{
	printf("Ctrl-Break\n");
	com_off();
	exit(0);


}


void	com_off(void)
{
	setvect(0x8+serialirq,irqsave);

	outp(0x21,org21);
	outp(base+INTENA,inp(base+INTENA)&0xf0);
	outp(base+MDMCTRL,inp(base+MDMCTRL)&0x7f);
}


int	com_send(unsigned char ch)
{
	delay(10);
	outp(base+MDMCTRL,inp(base+MDMCTRL)|0x2);	/* set RTS */

	while ((inp(base+MDMSTAT)&0x30)!=0x30 && !kbhit());	/* wait for DSR */

	while ((inp(base+LNSTAT)&0x20)==0 && !kbhit());	/* wait for empty */

	outp(base,ch);					/* send */

	outp(base+MDMCTRL,inp(base+MDMCTRL)&(~0x2));	/* reset RTS */



	return	0;
}


#define BUFSIZE 128
unsigned char 	buff[BUFSIZE];
volatile int	bufstart,bufend;

void	interrupt com_int(void)
{
static	unsigned char	theint,stat;

	asm sti;
	theint=inp(base+INTID);
	if ((theint&0x1)==0)
	{
		if ((theint&0xe)==4)
		{
		while (bufstart!=((bufend+1)&(BUFSIZE-1)) &&
		       (stat=(inp(base+LNSTAT)&1))==1)
//		stat=inp(base+LNSTAT);
		{
			if ((stat&0xe))
			{
				buff[bufend]=0xff;
				bufend=(bufend+1)&(BUFSIZE-1);
			}
			buff[bufend]=inp(base);
			cprintf("%d ",buff[bufend]);
			bufend=(bufend+1)&(BUFSIZE-1);
		}

		}
	}
	outp(0x20,0x20);
}





int	buf_init(void)
{
	bufstart=bufend=0;
}


unsigned com_read(void)
{
	unsigned char ch;

	while (bufstart==bufend && !kbhit()) ;

	if (bufstart==bufend)
	{
		printf("\nUser break\n");
		return 0xffff;
	}
	else
	{
		ch=buff[bufstart];
		bufstart=(bufstart+1)&(BUFSIZE-1);
		return ch;
	}

}
