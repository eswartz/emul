

/*
	ENCODE(addr,op,s,d,ts,td)


	Create 80x86 code, print to stdout.

@0900:
	xxx
	xxx
	xxx
	xxx


*/

#include <conio.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


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

char *	add(char *bf,char *ad)
{
	do
	{
		*bf++=*ad++;
	}	while (*ad);
	return	bf;
}

char *	prep(char *bf, char *ad)
{
static	char	temp[4096];
	strcpy(temp,bf);
	strcpy(bf,ad);
	return	add(bf+strlen(bf),temp);
}

int	lasthadcmp=0;
int	curhascmp=0;

void	subst(char *bf, word addr, word newaddr, word s,word d,word sa,word da)
{
static	char	*clrall="\tCLEAR\tST_O+ST_C\n";
static	char	*clrallb="\tCLEAR\tST_O+ST_C+ST_P\n";
//static 	char	*clrall0= "\tCLEAR\tST_O\n";
//static	char	*clrallb0="\tCLEAR\tST_O+ST_P\n";
static	char	*equ="\tCLEAR\tST_E\n";
static	char	*clr="";
static	char	*clrb="\tCLEAR\tST_P\n";
static	char	*ovr="\tCLEAR\tST_O\n";
static	char	*car="\tCLEAR\tST_C\n";

	char	c;
	int	nl,tbd;
static	char	outstring[4096];
static	char	temp[20];
	char	*osp;
	char	tok[4];

	word	ca;

//	curhascmp=(strstr(bf,"%cm0")!=NULL);

//	if (lasthadcmp && !curhascmp)
//		fprintf(stdout,"\tCMP0\tAX\n");



	for (ca=addr; ca<newaddr; ca+=2)
		fprintf(stdout,"@%04X:\n",ca);

	memset(outstring,0,4096);
	nl=1;
	tbd=0;
	osp=outstring;
	while ((c=*bf++)!=0)
	{
	if (nl)
	{
		if (c!='.')
			*osp++='\t';
		nl=0;
		tbd=0;
	}

	if (c!='%')
	{
		if (c=='.' && (*bf)!='.') ;
		else
		{
		if (c=='~')
			break;
		if (c!=' ' || (c==' ' && tbd))
			*osp++=c;
		else
		{
			*osp++='\t';
			tbd=1;
		}
		if (c=='\n')
			nl=1;
		}
	}
	else
	{
	tok[0]=*bf++;
	tok[1]=*bf++;
	tok[2]=*bf++;
	tok[3]=0;


	if 	(strcmp(tok,"imm")==0)
		sprintf(temp,"0%04Xh",sa);
	else if	(strcmp(tok,"src")==0)
		sprintf(temp,"0%04Xh",s);
	else if (strcmp(tok,"srg")==0)
		if (s)
			sprintf(temp,"[WP+R%d]",s);
		else
			sprintf(temp,"[WP]");
	else if (strcmp(tok,"sim")==0)
		sprintf(temp,"0%04Xh",sa&0xfffe);
	else if (strcmp(tok,"six")==0)
		sprintf(temp,"0%04Xh",sa^1);
	else if (strcmp(tok,"sia")==0)
		sprintf(temp,"0%04Xh",sa);
	else if (strcmp(tok,"srb")==0)
		sprintf(temp,"[WP+R%d+1]",s);

	else if (strcmp(tok,"des")==0)
		sprintf(temp,"0%04Xh",d);
	else if (strcmp(tok,"drg")==0)
		if (d)
			sprintf(temp,"[WP+R%d]",d);
		else
			sprintf(temp,"[WP]");
	else if (strcmp(tok,"dim")==0)
		sprintf(temp,"0%04Xh",da&0xfffe);
	else if (strcmp(tok,"dix")==0)
		sprintf(temp,"0%04Xh",da^1);
	else if (strcmp(tok,"dia")==0)
		sprintf(temp,"0%04Xh",da);
	else if (strcmp(tok,"dr2")==0)
		sprintf(temp,"[WP+R%d+2]",d);
	else if (strcmp(tok,"drb")==0)
		sprintf(temp,"[WP+R%d+1]",d);

	else if (strcmp(tok,"cmi")==0)
	{
		osp=prep(outstring,clr);
		sprintf(temp,"CMPTO\tax,0%04Xh",sa);

	}
	else if (strcmp(tok,"coc")==0)
	{
		osp=prep(outstring,clrall);
		sprintf(temp,"CARRYOVERFLOW?\n\tCMP0\tax");
//		sprintf(temp,"CARRYOVERFLOW?");
//		curhascmp=1;
	}
	else if (strcmp(tok,"cob")==0)
	{
		osp=prep(outstring,clrallb);
		sprintf(temp,"CARRYOVERFLOW?\n\tCMP0B\tal");
	}
	else if (strcmp(tok,"cm0")==0)
	{
		osp=prep(outstring,clr);
		sprintf(temp,"CMP0\tax");
//		strcpy(temp,"");
//		curhascmp=1;
	}
	else if (strcmp(tok,"cb0")==0)
	{
		osp=prep(outstring,clrb);
		sprintf(temp,"CMP0B\tal");
	}
	else if (strcmp(tok,"cmp")==0)
	{
		osp=prep(outstring,clr);
		sprintf(temp,"CMPTO\tcx,ax");
	}
	else if (strcmp(tok,"cmb")==0)
	{
		osp=prep(outstring,clrb);
		sprintf(temp,"CMPTOB\tcl,al");
	}
	else if (strcmp(tok,"crs")==0)
	{
		osp=prep(outstring,car);
		sprintf(temp,"CARRY?\n\tCMP0\tax");
//		sprintf(temp,"CARRY?\n");
//		curhascmp=1;
	}

	else if (strcmp(tok,"ov?")==0)
	{
		osp=prep(outstring,ovr);
		sprintf(temp,"OVERFLOW?");
	}
	else if (strcmp(tok,"car")==0)
	{
//		osp=prep(outstring,car);
		sprintf(temp,"CARRY");
	}
	else if (strcmp(tok,"eq?")==0)
	{
		osp=prep(outstring,equ);
		sprintf(temp,"EQUAL?");
	}
	else if (strcmp(tok,"ovr")==0)
	{
		osp=prep(outstring,ovr);
		sprintf(temp,"OVERFLOW");
	}

	else if (strcmp(tok,"ins")==0)
		sprintf(temp,"0%04Xh",addr);

	else if (strcmp(tok,"nxt")==0)
		sprintf(temp,"0%04Xh",newaddr);

	else if (strcmp(tok,"jmp")==0)
		sprintf(temp,"@%04X",d);

	else if (strcmp(tok,"jnx")==0)
		sprintf(temp,"@%04X",addr+2);

	else if (strcmp(tok,"ret")==0)
		sprintf(temp,"mov\tax,0%04Xh\n\tjmp\treturn",addr);

	else
	{
		fprintf(stderr,"Unrecognized token %s!\n",tok);
		exit(2);
	}

	osp=add(osp,temp);

	}
	}


	*osp=0;
	fprintf(stdout,outstring);
	fprintf(stdout,"\n");

//	lasthadcmp=curhascmp;

}

#define	IN(x) 	strcpy(bf,(x))
#define	AD(x)   strcat(bf,(x))

#define	MM(x)	( ((x)>=0x8400 && (x)<0xa000) || \
		  ((x)>=0x6000 && (x)<0x8000) )

#define	MMB(x)	( ((x)>=0x5ff0 && (x)<0x6000) || \
		  ((x)>=0x8400 && (x)<0xa000) || \
		  ((x)>=0x6000 && (x)<0x8000) )


void	encode(word addr,word newaddr,word op,word s,word d,
		byte ts,byte td,word sa,word da)
{
	char	bf[2048];
	word	ca;

	strcpy(bf,"");

	if (op<0x200)	      	// data
	{			// nothing!
	}
	else
	if (op<0x2a0)
	{
		/*  all read from immed, no addressing calucated */

		op&=0x1e0;
		switch (op>>5)
		{
		case 0 :
//			inst="LI  ";
			IN(	"mov ax,%imm\n"
				"mov %srg,ax\n"
				"%cm0");
			break;
		case 1 :
		//	inst="AI  ";
			IN(     "add word ptr %srg,%imm\n"
				"mov ax,%srg\n"
				"%coc\n");
			break;
		case 2 :
		//	inst="ANDI";
			IN(	"and word ptr %srg,%imm\n"
				"mov ax,%srg\n"
				"%cm0\n");
			break;
		case 3 :
		//	inst="ORI ";
			IN(	"or word ptr %srg,%imm\n"
				"mov ax,%srg\n"
				"%cm0\n");
			break;
		case 4 :
		//	inst="CI  ";
			IN(	"mov ax,%srg\n"
				"%cmi\n");
			break;
		}

		/*  all write to regs, no writeback */
	}
	else
	if (op<0x2e0)
	{
		op&=0x1e0;
		switch (op>>5)
		{
		case 5 :
		//	inst="STWP";
			IN(	"%ret\n");
			break;
		case 6 :
		//	inst="STST";
			IN(	"%ret\n");
			break;
		}
	}
	else
	if (op<0x320)
	{
		op&=0x1e0;
		switch (op>>5)
		{
		case 7 :
		//	inst="LWPI";
			IN(	"%ret\n");
			break;
		case 8 :
		//	inst="LIMI";
			IN(	"%ret\n");
			break;
		}
	}
	else
	if (op<0x400)
	{
		op&=0x1e0;
		switch (op>>5)
		{
		case 9  :
		//	inst="DATA";
			break;
		case 10 :
		//	inst="IDLE";
			break;
		case 11 :
		//	inst="RSET";
			break;
		case 12 :
		//	inst="RTWP";
			IN(	"%ret\n");
			break;
		case 13 :
		//	inst="CKON";
			break;
		case 14 :
		//	inst="CKOF";
			break;
		case 15 :
		//	inst="LREX";
			break;
		}
	}
	else
	if (op<0x800)
	{
		op&=0x3c0;
		//	Get the VALUE OF THE OPERAND in AX
		//
/*		if (((op>>6)==1) || ((op>>6)==10))
		switch (ts)
		{
		case	REG:
			IN(	"lea ax,%srg\n");
			break;
		case	IND:
			IN(	"mov ax,%srg\n");
			break;
		case	ADDR:
			if (s==0)
			IN(	"mov ax,%sim\n");
			else
			IN(	"mov di,%srg\n"
				"add di,%sia\n"
				"call readword\n");
			break;
		case	INC:
			IN(	"mov ax,%srg\n"
				"add %srg,word ptr 2\n");
			break;
		}

		else*/
		switch (ts)
		{
		case	REG:
			IN(	"mov ax,%srg\n");
			break;
		case	IND:
			IN(	"mov di,%srg\n"
				"call readword\n");
			break;
		case	ADDR:
			if (s==0)
				if (MM(sa))
					IN(	"mov di,%sim\n"
						"call readword\n");
				else
					IN(	"mov ax,word ptr [bot+offs+%sim]\n");
			else
			IN(	"mov di,%srg\n"
				"add di,%sia\n"
				"call readword\n");
			break;
		case	INC:
			IN(	"mov di,%srg\n"
				"add %srg,word ptr 2\n"
				"call readword\n");
			break;
		}


		switch (op>>6)
		{
		case 0  :
		//	inst="BLWP";
			IN(	"%ret\n~");
			break;
		case 1  :
		//	inst="B   ";
			IN(	"%ret\n~");
/*			AD(	"mov di,ax\n"
				"and di,not 1\n"
				"mov di,cs:[di]\n"
				"jmp di\n~");*/
			break;
		case 10 :
		//	inst="BL  ";
			IN(	"%ret\n~");
/*			AD(    	"mov di,ax\n"
				"mov word ptr [WP+R11],%nxt\n"
				"and di,not 1\n"
				"mov di,cs:[di]\n"
				"jmp di\n~");*/
			break;
		case 2  :
		//	inst="X   ";
			IN(	"%ret\n~");
			break;
		case 3  :
		//	inst="CLR ";
			AD(	"xor ax,ax\n");
			break;
		case 4  :
		//	inst="NEG ";
			AD(      "neg ax\n"
				"%ov?\n"
				"%cm0\n");
			break;
		case 5  :
		//	inst="INV ";
			AD(	"not ax\n"
				"%cm0\n");
			break;
		case 6  :
		//	inst="INC ";
			AD(    	"add ax,1\n"
				"%coc\n");
			break;
		case 7  :
		//	inst="INCT";
			AD(	"add ax,2\n"
				"%coc\n");
			break;
		case 8  :
		//	inst="DEC ";
			AD(	"add ax,-1\n"
				"%coc\n");
			break;
		case 9  :
		//	inst="DECT";
			AD(	"add ax,-2\n"
				"%coc\n");
			break;
		case 11 :
		//	inst="SWPB";
			AD(	"xchg al,ah\n");
			break;
		case 12 :
		//	inst="SETO";
			AD(	"mov ax,-1\n");
			break;
		case 13 :
		//	inst="ABS ";
			AD(	"CMP0 ax\n"
				"or ax,ax\n"
				"jns @%ins0\n"
				"neg ax\n"
				"overflow?\n"
				".@%ins0:\n");
			break;
		case 14 :
		case 15 :
		//	inst="DATA";
			IN( "");
			break;
		}
		/*  write value */
		if (ts==0)
			AD(	"mov %srg,ax\n");
		else
			if (ts!=2 || s || MM(sa))
				AD(	"call writeword\n");
			else
				AD(	"mov word ptr [bot+offs+%sim],ax\n");
	}
	else
	if (op<0xc00)
	{

		/*  all read from regs */

		op&=0x700;
		if (d!=0)
		switch (op>>8)
		{
		case 0 :
		//	inst="SRA ";
			IN(	"sar word ptr %srg,%des\n"
				"mov ax,%srg\n"
				"%crs\n");
			break;
		case 1 :
		//	inst="SRL ";
			IN(	"shr word ptr %srg,%des\n"
				"mov ax,%srg\n"
				"%crs\n");
			break;
		case 2 :
		//	inst="SLA ";
			IN(	"sal word ptr %srg,%des\n"
				"mov ax,%srg\n"
				"%coc\n");
			break;
		case 3 :
		//	inst="SRC ";
			IN(	"ror word ptr %srg,%des\n"
				"mov ax,%srg\n"
				"%crs\n");
			break;
		}
		else
		{
			IN(	"mov cx,[WP]\nand cl,15\n");
			switch (op>>8)
			{
			case 0 :
			//	inst="SRA ";
				AD(	"sar word ptr %srg,cl\n"
					"mov ax,%srg\n"
					"%crs\n");
				break;
			case 1 :
			//	inst="SRL ";
				AD(	"shr word ptr %srg,cl\n"
					"mov ax,%srg\n"
					"%crs\n");
				break;
			case 2 :
			//	inst="SLA ";
				AD(	"sal word ptr %srg,cl\n"
					"mov ax,%srg\n"
					"%coc\n");
				break;
			case 3 :
			//	inst="SRC ";
				AD(	"ror word ptr %srg,cl\n"
					"mov ax,%srg\n"
					"%crs\n");
				break;
			}
		}

		/*  all write to regs */
	}
	else
	if (op<0x1000)
	{
		op&=0x7e0;
		IN("");
	}
	else
	if (op<0x2000)
	{
		op&=0x0f00;
		d=addr+2+((signed char)s*2);
		if (d==addr)
			IN("%ret\n");
		else
		{
		IN(	"mov ax,[bp]..lastval\n"
			"cmp ax,[bp]..lastcmp\n");
		switch (op>>8)
		{
		case 0  :
		//	inst="JMP ";
			IN(	"jmp %jmp\n");
			break;
		case 1  :
		//	inst="JLT ";
			AD(	"jnl %jnx\n"
				"jmp %jmp\n");
			break;
		case 2  :
		//	inst="JLE ";
			AD(	"jnbe %jnx\n"
				"jmp %jmp\n");
			break;
		case 3  :
		//	inst="JEQ ";
			AD(	"jne %jnx\n"
				"jmp %jmp\n");
			break;
		case 4  :
		//	inst="JHE ";
			AD(	"jnae %jnx\n"
				"jmp %jmp\n");
			break;
		case 5  :
		//	inst="JGT ";
			AD(	"jng %jnx\n"
				"jmp %jmp\n");
			break;
		case 6  :
		//	inst="JNE ";
			AD(	"je %jnx\n"
				"jmp %jmp\n");
			break;
		case 7  :
		//	inst="JNC ";
			IN(	"ISCARRY? %jnx\n"
				"jmp %jmp\n");
			break;
		case 8  :
		//	inst="JOC ";
			IN(	"ISNOTCARRY? %jnx\n"
				"jmp %jmp\n");
			break;
		case 9  :
		//	inst="JNO ";
			IN(	"ISOVERFLOW? %jnx\n"
				"jmp %jmp\n");
			break;
		case 10 :
		//	inst="JL  ";
			AD(	"jnb %jnx\n"
				"jmp %jmp\n");
			break;
		case 11 :
		//	inst="JH  ";
			AD(	"jna %jnx\n"
				"jmp %jmp\n");
			break;
		case 12 :
		//	inst="JOP ";
			AD(	"ISNOTODDPARITY? %jnx\n"
				"jmp %jmp\n");
			break;
		case 13 :
		case 14 :
		case 15 :
			IN(	"%ret\n");
		}
		}
	}
	else
	if (op<0x4000)
	{
		//	Get the VALUE OF THE "DEST" (src!) (second val) in CX
		//
		/*  all read from addr, write to REG.  Only need to get
		    the value from that addr. */


		switch (ts)
		{
		case	REG:
			IN(	"mov cx,%srg\n");
			break;
		case	IND:
			IN(	"mov di,%srg\n"
				"call readwordc\n");
			break;
		case	ADDR:
			if (s==0)
				if (MM(sa))
					IN(	"mov di,%sim\n"
						"call readwordc\n");
				else
					IN(	"mov cx,word ptr [bot+offs+%sim]\n");
			else
			IN(	"mov di,%srg\n"
				"add di,%sia\n"
				"call readwordc\n");
			break;
		case	INC:
			IN(	"mov di,%srg\n"
				"add %srg,word ptr 2\n"
				"call readwordc\n");
			break;
		}



		op&=0x1c00;
		switch (op>>10)
		{
		case 0 :
		//	inst="COC ";
			AD(	"mov ax,%drg\n"
				"and ax,cx\n"
				"cmp ax,cx\n"
				"%eq?\n");
			break;
		case 1 :
		//	inst="CZC ";
			AD(	"mov ax,%drg\n"
				"not ax\n"
				"and ax,cx\n"
				"cmp ax,cx\n"
				"%eq?\n");
			break;
		case 2 :
		//	inst="XOR ");
			AD(	"xor %drg,cx\n"
				"mov ax,%drg\n"
				"%cm0\n");
			break;
		case 3 :
		case 4 :
		case 5 :
		//	inst=ldcr, stcr, XOP;
			IN(	"%ret\n~");
			break;
		case 6 :
		//	inst="MPY ";
			AD(	"push dx\n"
				"mov ax,%drg\n"
				"mul cx\n"
				"mov %drg,dx\n"
				"mov %dr2,ax\n"
				"pop dx\n");
			break;
		case 7 :
		//	inst="DIV ";
			AD(	"push dx\n"
				"mov dx,%drg\n"
				"mov ax,%dr2\n"
				"cmp cx,dx\n"
				"ja @%ins0\n"
				"pop dx\n"
				"%ovr\n"
				"jmp @%ins1\n"
			     ".@%ins0:\n"
				"div cx\n"
				"mov %drg,ax\n"
				"mov %dr2,dx\n"
				"pop dx\n"
			     ".@%ins1:\n");
			break;
		}
		/*  no writeback -- all register dests */
	}
	else if (op<0xc000 || op>=0xe000)
	{
/////////////////////////////////////////////////////////////////////////////////


		if ((op&0x1000)!=0x1000)
		{
		//	Get the VALUE OF THE src! in CX
		//
		switch (ts)
		{
		case	REG:
			IN(	"mov cx,%srg\n");
			break;
		case	IND:
			IN(	"mov di,%srg\n"
				"call readwordc\n");
			break;
		case	ADDR:
			if (s==0)
				if (MM(sa))
					IN(	"mov di,%sim\n"
						"call readwordc\n");
				else
					IN(	"mov cx,word ptr [bot+offs+%sim]\n");
			else
			IN(	"mov di,%srg\n"
				"add di,%sia\n"
				"call readwordc\n");
			break;
		case	INC:
			IN(	"mov di,%srg\n"
				"add %srg,word ptr 2\n"
				"call readwordc\n");
			break;
		}

		//	Get the value of the dest in AX.
		//
		switch (td)
		{
		case	REG:
			AD(	"mov ax,%drg\n");
			break;
		case	IND:
			AD(	"mov di,%drg\n"
				"call readword\n");
			break;
		case	ADDR:
			if (d==0)
				if (MM(da))
					AD(	"mov di,%dim\n"
						"call readword\n");
				else
					AD(	"mov ax,word ptr [bot+offs+%dim]\n");
			else
			AD(	"mov di,%drg\n"
				"add di,%dia\n"
				"call readword\n");
			break;
		case	INC:
			AD(	"mov di,%drg\n"
				"add %drg,word ptr 2\n"
				"call readword\n");
			break;
		}
		}
		else
		{
		//	Get the VALUE OF THE src! in CL
		//
		switch (ts)
		{
		case	REG:
			IN(	"mov cl,%srb\n");
			break;
		case	IND:
			IN(	"mov di,%srg\n"
				"call readbytec\n");
			break;
		case	ADDR:
			if (s==0)
				if (MMB(sa))
//					IN(	"mov di,%sim\n"
//						"call readbytec\n");
					IN(	"mov di,%sia\n"
						"call readbytec\n");
				else
					IN(	"mov cl,byte ptr [bot+offs+%six]\n");
			else
			IN(	"mov di,%srg\n"
				"add di,%sia\n"
				"call readbytec\n");
			break;
		case	INC:
			IN(	"mov di,%srg\n"
				"add %srg,word ptr 1\n"
				"call readbytec\n");
			break;
		}


		//	Get the value of the dest in AX.
		//
		switch (td)
		{
		case	REG:
			AD(	"mov al,%drb\n");
			break;
		case	IND:
			AD(	"mov di,%drg\n"
				"call readbyte\n");
			break;
		case	ADDR:
			if (d==0)
				if (MMB(da))
//					AD(	"mov di,%dim\n"
//						"call readbyte\n");
					AD(	"mov di,%dia\n"
						"call readbyte\n");
				else
					AD(	"mov al,byte ptr [bot+offs+%dix]\n");
			else
			AD(	"mov di,%drg\n"
				"add di,%dia\n"
				"call readbyte\n");
			break;
		case	INC:
			AD(	"mov di,%drg\n"
				"add %drg,word ptr 1\n"
				"call readbyte\n");
			break;
		}

		}







		op&=0xf000;
		switch (op>>12)
		{
		case 4  :
		//	inst="SZC ";
			AD(	"not cx\n"
				"and ax,cx\n"
				"%cm0\n");
			break;
		case 5  :
		//	inst="SZCB";
			AD(	"not cl\n"
				"and al,cl\n"
				"%cb0\n");
			break;
		case 6  :
		//	inst="S   ";
			AD(	"neg cx\n"
				"jc @%ins0\n"
				"%car\n"
			      ".@%ins0:\n"
				"add ax,cx\n"
				"%coc\n");
			break;
		case 7  :
		//	inst="SB  ";
			AD(	"neg cl\n"
				"jc @%ins0\n"
				"%car\n"
			      ".@%ins0:\n"
				"add al,cl\n"
				"%cob\n");
			break;
		case 8  :
		//	inst="C   ";
			AD(	"%cmp\n~");
			break;
		case 9  :
		//	inst="CB  ";
			AD(	"%cmb\n~");
			break;
		case 10 :
		//	inst="A   ";
			AD(	"add ax,cx\n"
				"%coc\n");
			break;
		case 11 :
		//	inst="AB  ");
			AD(	"add al,cl\n"
				"%cob\n");
			break;

		case 14 :
		//	inst="SOC ";
			AD(	"or ax,cx\n"
				"%cm0\n");
			break;
		case 15 :
		//	inst="SOCB";
			AD(	"or al,cl\n"
				"%cb0\n");
			break;
		}





		if ((op&0x1000)!=0x1000)
		{
		if (td==0)
			AD(	"mov %drg,ax\n");
		else
			if (td!=2 || d || MM(da))
				AD(	"call writeword\n");
			else
				AD(	"mov word ptr [bot+offs+%dim],ax\n");
		}
		else
		{
		if (td==0)
			AD(	"mov %drb,al\n");
		else
			if (td!=2 || d || MMB(da))
				AD(	"call writebyte\n");
			else
				AD(	"mov byte ptr [bot+offs+%dix],al\n");
		}

	}
	else
	{
/////////////////////////////////////////////////////////////////////////////////
		if ((op&0x1000)!=0x1000)
		{
		//	Get the VALUE OF THE src! in CX
		//
		switch (ts)
		{
		case	REG:
			IN(	"mov ax,%srg\n");
			break;
		case	IND:
			IN(	"mov di,%srg\n"
				"call readword\n");
			break;
		case	ADDR:
			if (s==0)
				if (MM(sa))
					IN(	"mov di,%sim\n"
						"call readword\n");
				else
					IN(	"mov ax,word ptr [bot+offs+%sim]\n");
			else
			IN(	"mov di,%srg\n"
				"add di,%sia\n"
				"call readword\n");
			break;
		case	INC:
			IN(	"mov di,%srg\n"
				"add %srg,word ptr 2\n"
				"call readword\n");
			break;
		}

		//	Put the value for the dest from AX.
		//
		switch (td)
		{
		case	REG:
			AD(	"mov %drg,ax\n");
			break;
		case	IND:
			AD(	"mov di,%drg\n"
				"call writeword\n");
			break;
		case	ADDR:
			if (d==0)
				if (MM(da))
					AD(	"mov di,%dim\n"
						"call writeword\n");
				else
					AD(	"mov word ptr [bot+offs+%dim],ax\n");
			else
			AD(	"mov di,%drg\n"
				"add di,%dia\n"
				"call writeword\n");
			break;
		case	INC:
			AD(	"mov di,%drg\n"
				"add %drg,word ptr 2\n"
				"call writeword\n");
			break;
		}

			AD(	"%cm0\n");
		}
		else
		{
		//	Get the VALUE OF THE src! in AL
		//
		switch (ts)
		{
		case	REG:
			IN(	"mov al,%srb\n");
			break;
		case	IND:
			IN(	"mov di,%srg\n"
				"call readbyte\n");
			break;
		case	ADDR:
			if (s==0)
				if (MMB(sa))
//					IN(	"mov di,%sim\n"
//						"call readbyte\n");
					IN(	"mov di,%sia\n"
						"call readbyte\n");
				else
					IN(	"mov al,byte ptr [bot+offs+%six]\n");
			else
			IN(	"mov di,%srg\n"
				"add di,%sia\n"
				"call readbyte\n");
			break;
		case	INC:
			IN(	"mov di,%srg\n"
				"add %srg,word ptr 1\n"
				"call readbyte\n");
			break;
		}


		//	Put the value for the dest from AL.
		//
		switch (td)
		{
		case	REG:
			AD(	"mov %drb,al\n");
			break;
		case	IND:
			AD(	"mov di,%drg\n"
				"call writebyte\n");
			break;
		case	ADDR:
			if (d==0)
				if (MMB(da))
//					AD(	"mov di,%dim\n"
//						"call writebyte\n");
					AD(	"mov di,%dia\n"
						"call writebyte\n");
				else
					AD(	"mov byte ptr [bot+offs+%dix],al\n");
			else
			AD(	"mov di,%drg\n"
				"add di,%dia\n"
				"call writebyte\n");
			break;
		case	INC:
			AD(	"mov di,%drg\n"
				"add %drg,word ptr 1\n"
				"call writebyte\n");
			break;
		}
			AD(	"%cb0\n");

		}


	}


	/*  now create the string!  */

/*	IN("%ret\n~");*/
	subst(bf,addr,newaddr,s,d,sa,da);
}


