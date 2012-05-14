/*
	DECODE(*addr,*op,*s,*d,*ts,*td)


	Disassemble instruction at ADDR, print out disassembly.

	Return OP = defining bits of instruction (minus s/d/ts/td)
	     ADDR = new address after 'executing'
		S = source value (address or offset)
		D = destination value (address or offset)
	       TS = type of source
	       TD = type of dest
*/

#include <stdio.h>
#include <stdlib.h>


typedef	unsigned int	word;
typedef	unsigned char	byte;

extern	word rom [4096];

#define	ROM(x)	(rom[(x)/2])
#define	FETCH  (rom[addr++]);

#define	REG	0
#define	IND	1
#define	ADDR	2
#define	INC	3

#define	IMMED	4
#define	CNT	5
#define	JUMP	6
#define	OFFS	7

word	decode(word addr, word *rop, word *rs, word *rd, byte *rts, byte *rtd,
		word *rsa, word *rda)
{
	word	oop,op,s,d,sa,da;
	byte	ts,td;
	char	line[80];
	char	*inst;


	addr>>=1;

	oop=op=FETCH;

	fprintf(stdout,";\t%04X=%04X ",addr+addr-2,op);

	if (op<0x200)	      	// data
	{
		op=0;
		inst="DATA";
		fprintf(stdout,"\t\t%s\n",inst);
	}
	else
	if (op<0x2a0)
	{
		ts=REG;
		s=op&15;
		sa=FETCH;
		op&=0x1e0;
		switch (op>>5)
		{
		case 0 :inst="LI  "; break;
		case 1 :inst="AI  "; break;
		case 2 :inst="ANDI"; break;
		case 3 :inst="ORI "; break;
		case 4 :inst="CI  "; break;
		}
		fprintf(stdout,"%04X\t\t%s R%d,>%04X\n",sa,inst,s,sa);
	}
	else
	if (op<0x2e0)
	{
		ts=REG;
		s=op&15;
		op&=0x1e0;
		switch (op>>5)
		{
		case 5 :inst="STWP"; break;
		case 6 :inst="STST"; break;
		}
		fprintf(stdout,"\t\t%s R%d\n",inst,s);
	}
	else
	if (op<0x320)
	{
		ts=IMMED;
		sa=FETCH;
		op&=0x1e0;
		switch (op>>5)
		{
		case 7 :inst="LWPI"; break;
		case 8 :inst="LIMI"; break;
		}
		fprintf(stdout,"%04X\t\t%s >%04X\n",sa,inst,sa);
	}
	else
	if (op<0x400)
	{
		op&=0x1e0;
		switch (op>>5)
		{
		case 9  :inst="DATA"; break;
		case 10 :inst="IDLE"; break;
		case 11 :inst="RSET"; break;
		case 12 :inst="RTWP"; break;
		case 13 :inst="CKON"; break;
		case 14 :inst="CKOF"; break;
		case 15 :inst="LREX"; break;
		}
		fprintf(stdout,"\t\t%s\n",inst);
	}
	else
	if (op<0x800)
	{
		ts=(op&0x30)>>4;
		s=op&15;
		if (ts==2)
		{
			sa=FETCH;
			fprintf(stdout,"%04X\t\t",sa);
		}
		else
			fprintf(stdout,"\t\t");
		op&=0x3c0;
		switch (op>>6)
		{
		case 0  :inst="BLWP"; break;
		case 1  :inst="B   "; break;
		case 2  :inst="X   "; break;		/* aaack! */
		case 3  :inst="CLR "; break;
		case 4  :inst="NEG "; break;
		case 5  :inst="INV "; break;
		case 6  :inst="INC "; break;
		case 7  :inst="INCT"; break;
		case 8  :inst="DEC "; break;
		case 9  :inst="DECT"; break;
		case 10 :inst="BL  "; break;
		case 11 :inst="SWPB"; break;
		case 12 :inst="SETO"; break;
		case 13 :inst="ABS "; break;
		case 14 :
		case 15 :inst="DATA"; break;
		}

		switch (ts)
		{
		case REG  : fprintf(stdout,"%s R%d\n",inst,s); break;
		case IND  : fprintf(stdout,"%s *R%d\n",inst,s); break;
		case ADDR : if (s==0)
				fprintf(stdout,"%s @>%04X\n",inst,sa);
			    else
				fprintf(stdout,"%s @>%04X(R%d)\n",inst,sa,s);
			    break;
		case INC  : fprintf(stdout,"%s *R%d+\n",inst,s); break;
		}


	}
	else
	if (op<0xc00)
	{
		ts=REG;
		s=op&15;
		td=CNT;
		d=(op&0xf0)>>4;
		op&=0x700;
		switch (op>>8)
		{
		case 0 :inst="SRA "; break;
		case 1 :inst="SRL "; break;
		case 2 :inst="SLA "; break;
		case 3 :inst="SRC "; break;
		}
		fprintf(stdout,"\t\t%s R%d,%d\n",inst,s,d);
	}
	else
	if (op<0x1000)
	{
		op&=0x7e0;
		fprintf(stdout,"\t\tDATA ...\n");
	}
	else
	if (op<0x2000)
	{
		ts=(op<0x1d00 ? JUMP : OFFS);
		s=op&255;
		op&=0x0f00;
		switch (op>>8)
		{
		case 0  :inst="JMP "; break;
		case 1  :inst="JLT "; break;
		case 2  :inst="JLE "; break;
		case 3  :inst="JEQ "; break;
		case 4  :inst="JHE "; break;
		case 5  :inst="JGT "; break;
		case 6  :inst="JNE "; break;
		case 7  :inst="JNC "; break;
		case 8  :inst="JOC "; break;
		case 9  :inst="JNO "; break;
		case 10 :inst="JL  "; break;
		case 11 :inst="JH  "; break;
		case 12 :inst="JOP "; break;
		case 13 :inst="SBO "; break;
		case 14 :inst="SBZ "; break;
		case 15 :inst="TB  "; break;
		}
		fprintf(stdout,"\t\t%s >%04X\n",inst,
				(op<0x0d00 ? (addr+(signed char)s)*2 : s));
	}
	else
	if (op<0x4000 && !(op>=0x3000 && op<0x3800))
	{
		ts=(op&0x30)>>4;
		s=(op&15);
		if (ts==2)
		{
			sa=FETCH;
			fprintf(stdout,"%04X\t\t",sa);
		}
		else
			fprintf(stdout,"\t\t");
		td=REG;
		d=(op&0x3c0)>>6;
		op&=0x1c00;
		switch (op>>10)
		{
		case 0 :inst="COC "; break;
		case 1 :inst="CZC "; break;
		case 2 :inst="XOR "; break;
		case 3 :inst="XOP "; break;
		case 6 :inst="MPY "; break;
		case 7 :inst="DIV "; break;
		}
		switch (ts)
		{
		case REG  : fprintf(stdout,"%s R%d",inst,s); break;
		case IND  : fprintf(stdout,"%s *R%d",inst,s); break;
		case ADDR : if (s==0)
				fprintf(stdout,"%s @>%04X",inst,sa);
			    else
				fprintf(stdout,"%s @>%04X(R%d)",inst,sa,s);
			    break;
		case INC  : fprintf(stdout,"%s *R%d+",inst,s); break;
		}
		fprintf(stdout,",R%d\n",d);
	}
	else
	if (op>=0x3000 && op<0x3800)
	{
		ts=(op&0x30)>>4;
		s=(op&15);
		if (ts==2)
		{
			sa=FETCH;
			fprintf(stdout,"%04X\t\t",sa);
		}
		else
			fprintf(stdout,"\t\t");
		td=REG;
		d=(op&0x3c0)>>6;
		if (d==0) d=16;
		op&=0x1c00;

		inst=(addr<0x3400 ? "LDCR" : "STCR");
		switch (ts)
		{
		case REG  : fprintf(stdout,"%s R%d",inst,s); break;
		case IND  : fprintf(stdout,"%s *R%d",inst,s); break;
		case ADDR : if (s==0)
				fprintf(stdout,"%s @>%04X",inst,sa);
			    else
				fprintf(stdout,"%s @>%04X(R%d)",inst,sa,s);
			    break;
		case INC  : fprintf(stdout,"%s *R%d+",inst,s); break;
		}
		fprintf(stdout,",%d\n",d);
	}
	else
	{
		ts=(op&0x30)>>4;
		s=(op&15);
		if (ts==2)
			sa=FETCH;
		td=(op&0x0c00)>>10;
		d=(op&0x3c0)>>6;
		if (td==2)
			da=FETCH;
		if (ts==2 && td==2)
			fprintf(stdout,"%04X %04X\t",sa,da);
		else
		if (ts==2 || td==2)
			fprintf(stdout,"%04X\t\t",(ts==2 ? sa : da));
		else
			fprintf(stdout,"\t\t");
		op&=0xf000;
		switch (op>>12)
		{
		case 4  :inst="SZC "; break;
		case 5  :inst="SZCB"; break;
		case 6  :inst="S   "; break;
		case 7  :inst="SB  "; break;
		case 8  :inst="C   "; break;
		case 9  :inst="CB  "; break;
		case 10 :inst="A   "; break;
		case 11 :inst="AB  "; break;
		case 12 :inst="MOV "; break;
		case 13 :inst="MOVB"; break;
		case 14 :inst="SOC "; break;
		case 15 :inst="SOCB"; break;
		}
		switch (ts)
		{
		case REG  : fprintf(stdout,"%s R%d",inst,s); break;
		case IND  : fprintf(stdout,"%s *R%d",inst,s); break;
		case ADDR : if (s==0)
				fprintf(stdout,"%s @>%04X",inst,sa);
			    else
				fprintf(stdout,"%s @>%04X(R%d)",inst,sa,s);
			    break;
		case INC  : fprintf(stdout,"%s *R%d+",inst,s); break;
		}
		switch (td)
		{
		case REG  : fprintf(stdout,",R%d\n",d); break;
		case IND  : fprintf(stdout,",*R%d\n",d); break;
		case ADDR : if (d==0)
				fprintf(stdout,",@>%04X\n",da);
			    else
				fprintf(stdout,",@>%04X(R%d)\n",da,d);
			    break;
		case INC  : fprintf(stdout,",*R%d+\n",d); break;
		}
	}

	*rop=oop;
	*rs=s;
	*rd=d;
	*rts=ts;
	*rtd=td;
	*rsa=sa;
	*rda=da;
	return	(addr+addr);
}
