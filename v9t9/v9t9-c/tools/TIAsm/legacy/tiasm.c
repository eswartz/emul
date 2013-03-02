/*
  tiasm.c						-- 9900 assembler

  The main weird feature is that this assembler requires reverse
  polish notation for expressions.  Caveat emptor!

  (c) 1994-2011 Edward Swartz

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

/*
  $Id$
 */

/*
	TI-99/4A Assembler


	For now, file formats are ordinary text/binary files.
*/

#if HAVE_CONFIG_H
#include "config.h"
#endif

#if !HAVE_GETTEXT
#define _(string)	(string)
#define N_(string)	(string)
#define textdomain(Domain)
#define bindtextdomain(Package, Directory)
#else
#include <libintl.h>
#define _(string)	gettext(string)
#endif

#ifdef __BORLANDC__
extern unsigned int _stklen = 32768;
#endif

#if defined(__BORLANDC__)
#include <alloc.h>
#else
#include <malloc.h>
#endif
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <locale.h>

#ifndef HAVE_STRUPR
char       *
strupr(char *s)
{
	char       *p = s;

	while (*p) {
		if (islower(*p))
			*p = toupper(*p);
		p++;
	}
	return s;


}
#endif

#ifndef HAVE_SWAB
void
swab(unsigned char *a, unsigned char *b, int bytes) 
{
	while (bytes > 0) {
		unsigned char t = a[0], u = a[1];

		b[0] = u;
		b[1] = t;
		a += 2;
		b += 2;
		bytes -= 2;
	}
}
#endif 

/*	We're defining the 'word' size solely based on efficiency */

#if !defined(__GNUC__)
typedef	unsigned int	word;
#else
typedef	unsigned short	word;
#endif
typedef unsigned char	byte;

// current input filename
char        inname[256];

// list file
FILE       *list;

int         pass;
int         errors;
int         warnings;

#define	DEBUG if (1)

struct symbolrec {
	char        name[16];
	int         line;
	int         addr;
	int			equ;

	struct symbolrec *left, *right;
}          *symtable;


int         assemble(char *filename);
void        breakup(char *line, char *label, char *inst, char *src,

					char *dest);
char       *skipspaces(char *line);
char       *copychars(char *from, char *to, int max);

void        dolisting(word start, word end, char *line,

					  struct symbolrec *sym);
int         doaline(char *line, struct symbolrec **sym);

int         addsymbol(char *name, int defining, struct symbolrec **nw);

int         decode(char **pline, struct symbolrec *label, word orgaddr,

				   byte optype);

int         getregister(char **pline, word orgaddr, byte start);
int         getjump(char **pline, word orgaddr);
int         getgen(char **pline, word orgaddr, byte tbit, byte bit);
int         getcnt(char **pline, word orgaddr, byte bit, char *type);
int         getbit(char **pline, word orgaddr);

int         getnumber(char **pline, word orgaddr, long *ret, byte * neither);
int         getexpr(char **pline, word orgaddr, byte size, long *ret);



word        addr;
byte        memory[0x8000];

char        em[256];			// error message


struct opcoderec {
	char        name[6];
	word        opcode;
	byte        src;
	byte        dst;
};

int
opsort(struct opcoderec *a, struct opcoderec *b)
{
	return (strcmp(a->name, b->name));
}

enum { 
	NIL, REG, REGH, REG4, GEN, GENH, JMP, BIT, CNT, CNTH,
	BYT, WRD, DWD, EQU, IMM, XOP, BSS, AORG, INC, NIMM, AGAIN,
	EVEN
};

static struct opcoderec
            opcodes[] = {
	{"DB", 0x0000, BYT, AGAIN},
	{"BYTE", 0x0000, BYT, AGAIN},
	{"TEXT", 0x0000, BYT, AGAIN},
	{"DW", 0x0000, WRD, AGAIN},
	{"DATA", 0x0000, WRD, AGAIN},
	{"DD", 0x0000, DWD, AGAIN},
	{"EQU", 0x0000, EQU, NIL},
	{"BSS", 0x0000, BSS, NIL},
	{"AORG", 0x0000, AORG, NIL},
	{"INCL", 0x0000, INC, NIL},
	{"EVEN", 0x0000, EVEN, NIL},

	{"LI", 0x0200, REG, IMM},
	{"AI", 0x0220, REG, IMM},
	{"SI", 0x0220, REG, NIMM},
	{"ANDI", 0x0240, REG, IMM},
	{"ORI", 0x0260, REG, IMM},
	{"CI", 0x0280, REG, IMM},

	{"STWP", 0x02a0, REG, NIL},
	{"STST", 0x02c0, REG, NIL},

	{"LWPI", 0x02e0, IMM, NIL},

	{"LIMI", 0x0300, IMM, NIL},

	{"IDLE", 0x0340, NIL, NIL},
	{"RSET", 0x0360, NIL, NIL},
	{"RTWP", 0x0380, NIL, NIL},
	{"CKON", 0x03a0, NIL, NIL},
	{"CKOF", 0x03c0, NIL, NIL},
	{"LREX", 0x03e0, NIL, NIL},

	{"BLWP", 0x0400, GEN, NIL},
	{"B", 0x0440, GEN, NIL},
	{"RT", 0x045B, NIL, NIL},
	{"X", 0x0480, GEN, NIL},
	{"CLR", 0x04c0, GEN, NIL},
	{"NEG", 0x0500, GEN, NIL},
	{"INV", 0x0540, GEN, NIL},
	{"INC", 0x0580, GEN, NIL},
	{"INCT", 0x05c0, GEN, NIL},
	{"DEC", 0x0600, GEN, NIL},
	{"DECT", 0x0640, GEN, NIL},
	{"BL", 0x0680, GEN, NIL},
	{"SWPB", 0x06c0, GEN, NIL},
	{"SETO", 0x0700, GEN, NIL},
	{"ABS", 0x0740, GEN, NIL},

	{"SRA", 0x0800, REG, CNT},
	{"SRL", 0x0900, REG, CNT},
	{"SLA", 0x0a00, REG, CNT},
	{"SRC", 0x0b00, REG, CNT},

	// v9t9 internal opcodes
	{"DSR", 0x0c00, NIL, NIL},
	{"KEY", 0x0d80, NIL, NIL},
	{"EMIT", 0x0dc0, NIL, NIL},	// R0 implicit
	{"DBG", 0x0de0, NIL, NIL},
	{"-DBG", 0x0de1, NIL, NIL},

	{"JMP", 0x1000, JMP, NIL},
	{"NOP", 0x1000, NIL, NIL},
	{"JLT", 0x1100, JMP, NIL},
	{"JLE", 0x1200, JMP, NIL},
	{"JBE", 0x1200, JMP, NIL},
	{"JEQ", 0x1300, JMP, NIL},
	{"JE", 0x1300, JMP, NIL},
	{"JHE", 0x1400, JMP, NIL},
	{"JAE", 0x1400, JMP, NIL},
	{"JGT", 0x1500, JMP, NIL},
	{"JG", 0x1500, JMP, NIL},
	{"JNE", 0x1600, JMP, NIL},
	{"JNC", 0x1700, JMP, NIL},
	{"JOC", 0x1800, JMP, NIL},
	{"JC", 0x1800, JMP, NIL},
	{"JNO", 0x1900, JMP, NIL},
	{"JL", 0x1A00, JMP, NIL},
	{"JB", 0x1A00, JMP, NIL},
	{"JH", 0x1B00, JMP, NIL},
	{"JA", 0x1B00, JMP, NIL},
	{"JOP", 0x1C00, JMP, NIL},

	{"SBO", 0x1d00, BIT, NIL},
	{"SBZ", 0x1e00, BIT, NIL},
	{"TB", 0x1f00, BIT, NIL},

	{"COC", 0x2000, GEN, REGH},
	{"CZC", 0x2400, GEN, REGH},
	{"XOR", 0x2800, GEN, REGH},
	{"XOP", 0x2c00, GEN, XOP},
	{"LDCR", 0x3000, GEN, CNTH},
	{"STCR", 0x3400, GEN, CNTH},
	{"MPY", 0x3800, GEN, REGH},

	{"MUL", 0x3800, GEN, REGH},
	{"DIV", 0x3c00, GEN, REGH},

	{"SZC", 0x4000, GEN, GENH},
	{"SZCB", 0x5000, GEN, GENH},
	{"S", 0x6000, GEN, GENH},
	{"SB", 0x7000, GEN, GENH},
	{"C", 0x8000, GEN, GENH},
	{"CB", 0x9000, GEN, GENH},
	{"A", 0xa000, GEN, GENH},
	{"AB", 0xb000, GEN, GENH},
	{"MOV", 0xc000, GEN, GENH},
	{"MOVB", 0xd000, GEN, GENH},
	{"SOC", 0xe000, GEN, GENH},
	{"SOCB", 0xf000, GEN, GENH},
	{"", 0x0000, NIL, NIL}
};

#define	OPCOUNT (sizeof(opcodes)/sizeof(opcodes[0]))

typedef int sorter(const void *, const void *);

enum {
	A_rom = 1 << (0x0000 / 0x2000),
	A_module = 1 << (0x6000 / 0x2000),
	A_grom = 7 << (0x0000 / 0x2000),
	A_dsr = 1 << (0x4000 / 0x2000)
} addrmask, warnmask;

#define BANKUSED(a) (1 << ((a) / 0x2000))
#define INRANGE(a) (addrmask & BANKUSED(a))

int
main(int argc, char *argv[])
{
	int         ap;
	char       *romout = 0, *modout = 0, *dsrout = 0, *gromout = 0;
	char       *listout = 0;

	setlocale(LC_ALL, "");
	bindtextdomain(PACKAGE, LOCALEDIR);
	textdomain(PACKAGE);

	if (argc < 3) {
		printf
			(_("TIASM <input file> [-r <console ROM output>] [-m <module ROM output>]\n"
			 "[-d <DSR ROM output>] [-g <console GROM output>] [<list file>]\n"
			 "\n" "-r saves the 8k memory block at >0000.\n"
			 "-m saves the 8k memory block at >6000.\n"
			 "-d saves the 8k memory block at >4000.\n"
			 "-g saves the 24k memory block at >0000.  This can only be used with -m.\n"));
		return 1;
	}

	addrmask = 0;
	warnmask = 0;
	ap = 1;
	strcpy(inname, argv[ap++]);

	while (ap < argc) {
		if (strcmp(argv[ap], "-r") == 0) {
			romout = argv[++ap];
			if (!(addrmask & A_rom))
				addrmask |= A_rom;
			else {
				printf(_("-r specified twice, or -g also specified"));
				return 1;
			}
		} else if (strcmp(argv[ap], "-m") == 0) {
			modout = argv[++ap];
			if (!(addrmask & A_module))
				addrmask |= A_module;
			else {
				printf(_("-m specified twice"));
				return 1;
			}
		} else if (strcmp(argv[ap], "-d") == 0) {
			dsrout = argv[++ap];
			if (!(addrmask & A_dsr))
				addrmask |= A_dsr;
			else {
				printf(_("-d specified twice, or -g also specified"));
				return 1;
			}
		} else if (strcmp(argv[ap], "-g") == 0) {
			gromout = argv[++ap];
			if (!(addrmask & A_grom))
				addrmask |= A_grom;
			else {
				printf(_("-g specified twice, or -r|-d also specified"));
				return 1;
			}
		} else if (argv[ap][0] == '-') {
			printf(_("Unknown option %s\n"), argv[ap]);
			return 1;
		} else
			listout = argv[ap];
		ap++;
	}

	qsort((void *) opcodes, OPCOUNT - 1, sizeof(struct opcoderec),

		  (sorter *) opsort);

	if (listout) {
		list = fopen(listout, "w");
		if (list == NULL) {
			printf(_("Could not open listing file '%s'\n"), listout);
			return 1;
		}
	}


	pass = 0;
	errors = 0;
	strcpy(inname, argv[1]);
	symtable = NULL;
	addr = 0;
	if (assemble(inname) == 0) {
		pass++;
		addr = 0;
		if (assemble(inname) == 0) {
			FILE       *out;

			swab(memory, memory, 0x8000);

			if (romout) {
				out = fopen(romout, "wb");
				if (out == NULL || fwrite(memory, 1, 8192, out) != 8192) {
					printf(_("main: couldn't create ROM file %s\n"), romout);
					return 1;
				}
				fclose(out);
			}
			if (dsrout) {
				out = fopen(dsrout, "wb");
				if (out == NULL ||
					fwrite(memory + 0x4000, 1, 8192, out) != 8192) {
					printf(_("main: couldn't create DSR file %s\n"), romout);
					return 1;
				}
				fclose(out);
			}
			if (modout) {
				out = fopen(modout, "wb");
				if (out == NULL ||
					fwrite(memory + 0x6000, 1, 8192, out) != 8192) {
					printf(_("main: couldn't create module file %s\n"), romout);
					return 1;
				}
				fclose(out);
			}
			if (gromout) {
				out = fopen(gromout, "wb");
				if (out == NULL || fwrite(memory, 1, 24576, out) != 24576) {
					printf(_("main: couldn't create GROM file %s\n"), gromout);
					return 1;
				}
				fclose(out);
			}
		} else
			printf(_("PASS 2:  %d errors.\n"), errors);
	} else {
		printf(_("PASS 1:  %d errors.\n"), errors);
	}

	if (list)
		fclose(list);

	return errors != 0;
}




#define	WARNING(s) {	\
			fprintf(stderr,_("Warning:  %s (%d): %s\n"),inname,linenum,s); \
			if (list) \
			fprintf(list,_("Warning:  %s (%d): %s\n"),inname,linenum,s); \
			warnings++; \
		   }

#define	ERROR(s,v,r) {	\
			sprintf(em,s,v); \
			fprintf(stderr,_("Error:  %s (%d): %s\n"),inname,linenum,em); \
			if (list) \
			fprintf(list,_("Error:  %s (%d): %s\n"),inname,linenum,em); \
			errors++; \
			return r; \
		   }



#define	FATAL(s) {	\
			fprintf(stderr,_("Fatal:  %s (%d): %s\n"),inname,linenum,s); \
			if (list) \
			fprintf(list,_("Fatal:  %s (%d): %s\n"),inname,linenum,s); \
			exit(1); \
		 }


#define	FIXADDR addr=(addr+1)&0xfffe
#define	WORD(a) (*(word *)(memory+(a)))
#define	BYTE(a) (memory[(a)^1])


int         linenum;


int
assemble(char *filename)
{
	FILE       *in;
	char        line[256];
	char        orgline[256];
	int         err;
	word        orgaddr;
	struct symbolrec *sym;
	int         oldline;
	char        oldname[256];

	int         last = 0;


	in = fopen(filename, "r");
	if (in == NULL) {
		printf(_("parse: couldn't open input file %s\n"), filename);
		return 1;
	}

	oldline = linenum;
	strcpy(oldname, inname);

	linenum = 1;
	strcpy(inname, filename);

	while (!feof(in) && !last) {
		sym = NULL;
		em[0] = 0;
		if (fgets(line, 256, in) != NULL) {
			if (line[strlen(line) - 1] != '\n')
				last = 1;		// buggy fgets
			strcpy(orgline, line);

			orgaddr = addr;
			err = doaline(line, &sym);

			if (pass && list)
				dolisting(orgaddr, addr, orgline, sym);

			if (err)
				break;
			linenum++;
		} else
			last = 1;
	}
	fclose(in);
	if (pass && list)
		fprintf(list, _("\n"));

	linenum = oldline;
	strcpy(inname, oldname);

	err |= errors;

	return err;
}




void
dolisting(word start, word end, char *line, struct symbolrec *sym)
{
	int         first, printed;
	int         trailing;

	if (start == end || !INRANGE(start))
		if (sym == NULL)
			fprintf(list, "%05d						  \t%s", linenum, line);
		else if (sym->addr == start)
			fprintf(list, "%05d >%04X					\t%s", linenum,
					sym->addr, line);
		else
			fprintf(list, "%05d	   >%04X			  \t%s", linenum,
					sym->addr, line);
	else {
		first = 1;
		trailing = 0;
		while (start < end && trailing < 5) {
			printed = 0;
			if (first)
				fprintf(list, "%05d >%04X=", linenum, start);
			else {
				fprintf(list, "			");
				trailing++;
			}

			while (printed < 3 && start < end) {
				if (start & 1) {
					fprintf(list, "  >%02X ", BYTE(start));
					start++;
				} else {
					fprintf(list, ">%04X ", WORD(start));
					start += 2;
				}
				printed++;
			}

			while (printed < 3) {
				fprintf(list, "	  ");
				printed++;
			}

			if (first) {
				fprintf(list, " \t%s", line);
				first = 0;
			} else
				fprintf(list, "\n");
		}
	}
}



char       *
skipspaces(char *line)
{
	while (isspace(*line))
		line++;
	return line;
}

char       *
copychars(char *from, char *to, int max)
{
	while ((!isspace(*from)) && *from)
		if (max) {
			*to++ = *from++;
			max--;
		} else if (*from)
			from++;
	*to++ = 0;
	return from;
}





void
losecomments(char *line)
{
	char        temp[256];
	char        ch;
	char       *tp;

//  char    *lst,*fst;

	strcpy(temp, line);
	tp = temp;
//  fst=line;
	while (*tp) {
		ch = *tp++;
		if (ch == '\n' || ch == ';')
			break;
		*line++ = ch;
	}

/*	lst=line-1;
	while (lst>fst && (*lst==' ' || *lst=='\t'))
		lst--;
	line=lst;*/

	*line++ = 0;
	*line++ = 0;
}


int
skipcommas(char **pline, int must)
{
	char       *line = *pline;

	line = skipspaces(line);
	if (*line++ != ',')
		if (must)
			ERROR(_("Comma expected"), "", 1)
				else
		{
			*pline = line;
			return 1;
		}


	line = skipspaces(line);
	*pline = line;
	return 0;
}

int
pushw(word w)
{
	FIXADDR;

	if (!INRANGE(addr)) {
		if (!(warnmask & BANKUSED(addr))) {
			WARNING(_("PUSHW:  Writing to non-stored memory bank"));
			warnmask |= BANKUSED(addr);
		}
	}

	if (addr >= sizeof(memory))
		FATAL(_("Address larger than memory\n"));

	WORD(addr) = w;

// DEBUG fprintf(stderr,_("pushw: @>%04X := >%04X\n"),addr,w);

	addr += 2;
	return 0;
}

int
pushb(byte b)
{
	if (!INRANGE(addr)) {
		if (!(warnmask & BANKUSED(addr))) {
			WARNING(_("PUSHB:  Writing to non-stored memory bank"));
			warnmask |= BANKUSED(addr);
		}
	}

	if (addr >= sizeof(memory))
		FATAL(_("Address larger than memory\n"));

	BYTE(addr) = b;

//DEBUG fprintf(stderr,_("pushb: @>%04X := >%02X\n"),addr,b);

	addr++;
	return 0;
}


#if defined(__BORLANDC__)
word
swap(word a)
{
	asm {
	mov         ax, a 
	ror 		ax, 8
	} 
	return _AX;
}

#else
word swap(word a)
{
	return ((a & 0xff) << 8) | ((a & 0xff00) >> 8);
}

#endif

int         quoting, quotesize;
char        quoted[256];




int
doaline(char *line, struct symbolrec **sym)
{



//  struct instrec inst;
//  struct symbolrec *sym;

	char        ch;
	char        labelname[17];
	char        opcode[6];

	struct opcoderec *om;
	byte        optype;
	word        orgaddr;


	orgaddr = addr;
	quoting = 0;

	ch = *line;

	losecomments(line);

//DEBUG fprintf (stderr,"(%d) %s\n",linenum,line);

	//  Get a label

	if (*line == 0)
		return 0;

	*sym = NULL;
	if (!isspace(ch)) {
		memset(labelname, 0, 16);
		line = copychars(line, labelname, 16);

		if (labelname[strlen(labelname) - 1] == ':')
			labelname[strlen(labelname) - 1] = 0;

		if (labelname[15]) {
			WARNING(_("Label name truncated"));
			labelname[15] = 0;
		}

		strupr(labelname);
		if (definesymbol(labelname, sym))
		{
			return 0;
		}
	} else
		*labelname = 0;

	line = skipspaces(line);
	if (*line) {
		line = copychars(line, opcode, 5);
		line = skipspaces(line);
		strupr(opcode);


		om = (struct opcoderec *)
			bsearch(opcode, opcodes, OPCOUNT - 1, sizeof(struct opcoderec),
					(sorter *) opsort);

		if (om != NULL) {
			orgaddr = addr;
			if (om->opcode)
				pushw(om->opcode);
			optype = om->src;
			if (optype == NIL || decode(&line, *sym, orgaddr, optype));
			else
				do {
					if (om->dst == NIL)
						break;

					if (om->dst != AGAIN) {
						optype = om->dst;
						if (skipcommas(&line, 1))
							return 0;
						if (decode(&line, *sym, orgaddr, optype))
							return 0;
					} else {
						if (skipcommas(&line, 0))
							break;
						if (decode(&line, *sym, orgaddr, om->src))
							return 0;
					}

				} while (om->dst == AGAIN && *line);
		} else
			ERROR(_("Invalid mnemonic '%s'"), opcode, 0);
	}

	return 0;
}

/////////////////////////////////////////////////////////////////////////



int
findsymbol(char *buffer, struct symbolrec **ret)
{
	struct symbolrec *s;
	char        name[16];
	int         cmp;

	s = symtable;
	while (s != NULL) {
		strcpy(name, s->name);
		name[0] &= 0x7f;
		if ((cmp = strcmp(name, buffer)) == 0) {
			*ret = s;
			return 0;
		}
		if (cmp < 0)
			s = s->right;
		else
			s = s->left;
	}

	*ret = NULL;

	return 1;
}


struct symbolrec *insertsymbol(const char* name, int addr, int linenum)
{
	struct symbolrec *s, *n, **l;
	int         cmp;
	char        compare[18];

	n = malloc(sizeof(struct symbolrec));

	if (n == NULL)
		FATAL(_("Memory full!"));

	strcpy(n->name, name);

	if ((strchr("0123456789>$", n->name[0]) != NULL) ||
		(strpbrk(n->name, "+-=, ;*/<>") != NULL))
		ERROR(_("Illegal character(s) in label '%s'"), n->name, NULL);

	n->addr = addr;
	n->line = linenum;
	n->left = n->right = NULL;
	n->equ = 0;
//DEBUG     fprintf(stderr,_("Adding symbol '%s' on line %d at >%04X\n"),name,linenum,addr);

	if (symtable == NULL)
		symtable = s = n;
	else {
		s = symtable;
		while (s != NULL) {
			strcpy(compare, s->name);
			compare[0] &= 0x7f;
			cmp = strcmp(compare, n->name);
			if (!pass && cmp == 0)
				ERROR(_("Multiple symbol '%s'"), n->name, 0);
			if (cmp < 0) {
				l = &s->right;
				s = s->right;
			} else {
				l = &s->left;
				s = s->left;
			}

		}

		*l = n;
	}
	return n;
}

/*
	Define a symbol to the symbol table.

	Returns FATAL error if memory full,
		ERROR if multiple symbols.
*/
int
definesymbol(char *name, struct symbolrec **nw)
{
	struct symbolrec *n, *m;

	if (findsymbol(name, &m) == 0) {	// found alreddy
		int was_undef = (m->name[0] & 0x80) != 0;
		if (!was_undef && !pass)	// redefine?
		{
			ERROR(_("Multiple definition of '%s'"), name, 1);
		}
		m->name[0] &= 0x7f;

		// debug
		if (!was_undef && !m->equ && m->addr != addr)
		{
			fprintf(stderr, _("Moving '%s' from >%04X to >%04X\n"), m->name, m->addr, addr);
			exit(125);
		}

		m->addr = addr;
		*nw = m;
		return 0;
	 }

	// new symbol
	*nw = insertsymbol(name, addr, linenum);
	return 0;
}


/*
	Add a forward symbol in the symbol table.

	Returns FATAL error if memory full,
		ERROR if multiple symbols.
*/

int
forwardsymbol(char *name, struct symbolrec **nw)
{
	name[0] |= 0x80;		// undefined
	*nw = insertsymbol(name, 0, linenum);

	return 0;
}

/*
	This routine will return address of the name.

	If the symbol not found, a blank entry will be added to the
		symbol table and zero returned.
	If it's undefined, a new link will be made using record->addr
		and the address of the last link returned.

	The special symbol "$" is handled here.

*/
int
getsymboladdress(word orgaddr, char *name, int *ret)
{
	struct symbolrec *s;


	if (strcmp(name, "$") == 0) {
		*ret = orgaddr;
		return 0;
	}

	if (findsymbol(name, &s) == 0)	// found, defined
	{
		if (pass && s->name[0] & 0x80)
			ERROR(_("Unresolved symbol '%s'"), name, 1)
		else
		{
			*ret = s->addr;
			return 0;
		}
	} 
	else if (pass)
		ERROR(_("Undefined symbol '%s'"), name, 1)
	else
	{
		if (forwardsymbol(name, &s) == 0)	// adding, undefined
		{
			*ret = 0;
			return 0;
		} else
			return 1;			// ouch!
	}
}


//////////////////////////////////////////////////////////////////////

/*
	GETNUMBER will decode a number or an address as a long.

	*pline is non-null
*/

int
getnumber(char **pline, word orgaddr, long *ret, byte * neither)
{
	static char *abasestr = "0123456789ABCDEF";
	char        basestr[18];
	char        name[18];
	char       *line = *pline;
	char       *start;
	char       *where;
	long        val;
	int         base;
	byte        dig;
	byte        not;
	byte        smt;
	char        ch;
	char       *format;
	int         err;
	struct symbolrec *s;
	int         addr;

	ch = *line;

	not = (ch == '~');
	if (not)
		line++;


	if (ch == '>') {
		base = 16;
		line++;
	} else
		base = 10;

	start = line;

	memcpy(basestr, abasestr, base);
	basestr[base] = 0;

	val = 0;
	err = 0;
	smt = 0;
	while (!err) {
		where = strchr(basestr, toupper(*line));
		if (where == NULL || *where == 0) {
			if (!smt)
				err = 1;
			else if (not)
				val = ~val;
			break;
		}

		dig = where - basestr;
		val = (val * base) + dig;
		line++;
		smt = 1;
		*neither = 0;
	}

	if (err) {
		line = start;
		dig = 0;
		while (strchr(", \t\n+-*/()&|[]^", *line) == NULL) {
			if (dig < 15)
				name[dig++] = (*line++);
			else
				line++;
		}
		name[dig] = 0;

		if (dig) {
			*neither = 0;
			strupr(name);
			if (getsymboladdress(orgaddr, name, &addr) == 0)
				val = addr;
			else
				return 1;
		} else {
			*neither = 1;
			return 1;
		}
	}

	*pline = line;
	*ret = val;
	return 0;
}


/*
	GETEXPR will decode an expression as a long.
*/
int
getexpr(char **pline, word orgaddr, byte size, long *ret)
{
#define	STMAX	32
	static long stack[STMAX];
	int         sp = 0;
	char       *line;
	long        val;
	char        name[16];
	byte        neither;

	line = *pline;
	line = skipspaces(line);

	if (*line == 0 && !quoting) {
		ERROR(_("Missing argument"), "", 1);
	}

	do {
		if (sp >= STMAX)
			ERROR(_("Expression stack overflow!"), "", 1);
		if (*line == '"') {
			quoting = 0;
			do					// copy into quoted
				quoted[quoting++] = *line++;
			while (*line != '"' && *line);
			if (!*line)
				ERROR(_("Unterminated string"), "", 1);
			line++;				// skip last quote
			while ((quoting - 1) % size)
				quoted[quoting++] = 0;
			quotesize = quoting;
			quoting = 1;
		}

		if (quoting)			// quoting?
		{
			if (size == 1)
				stack[sp++] = quoted[quoting] & 255;
			else if (size == 2)
				stack[sp++] = ((quoted[quoting] & 255) << 8) +
					(quoted[quoting + 1] & 255);
			else if (size == 4)
				stack[sp++] = ((quoted[quoting] & 255) << 24) +
					((quoted[quoting + 1] & 255) << 16) +
					((quoted[quoting + 2] & 255) << 8) +
					(quoted[quoting + 3] & 255);
			else
				ERROR(_("Invalid size passed to getexpr (%hd)!"), size, 1);
			quoting += size;

			if (quoting >= quotesize)
				quoting = 0;
		} else if (getnumber(&line, orgaddr, &val, &neither) == 0) {
			if (sp >= 32)
				ERROR(_("Expression stack overflow at '%s'"), line, 1)
					else
				stack[sp++] = val;
		} else if (!neither)
			return 1;
		else if (strchr("+-*/%|&^[]", *line) == NULL)
			break;
		else if (sp > 1)
			switch (*line) {
			case '+':
				sp--;
				line++;
				stack[sp - 1] += stack[sp];
				break;
			case '-':
				sp--;
				line++;
				stack[sp - 1] -= stack[sp];
				break;
			case '*':
				sp--;
				line++;
				stack[sp - 1] *= stack[sp];
				break;
			case '/':
				sp--;
				line++;
				if (stack[sp])
					stack[sp - 1] /= stack[sp];
				else if (pass)
					ERROR(_("Divide by zero"), "", 1);
				break;
			case '%':
				sp--;
				line++;
				stack[sp - 1] %= stack[sp];
				break;
			case '|':
				sp--;
				line++;
				stack[sp - 1] |= stack[sp];
				break;
			case '&':
				sp--;
				line++;
				stack[sp - 1] &= stack[sp];
				break;
			case '^':
				sp--;
				line++;
				stack[sp - 1] ^= stack[sp];
				break;
			case ']':
				sp--;
				line++;
				stack[sp - 1] >>= stack[sp];
				break;
			case '[':
				sp--;
				line++;
				stack[sp - 1] <<= stack[sp];
				break;
			default:
				ERROR(_("Illegal operator '%c'"), *line, 1);
		} else
			ERROR(_("Stack underflow at '%s'"), line, 1);

		line = skipspaces(line);
	} while (*line && *line != ',');

	if (sp > 1)
		ERROR(_("Unterminated expression"), "", 1);


	*ret = stack[0];
	*pline = line;
	return 0;
}


/////////////////////////////////////////////////////////

/*
	Decode will encode one operand of an instruction.

	The optype will be used to limit the type.

	All operands are separated by commas.

	When all parameters are read, return *line==0
*/

int
decode(char **pline, struct symbolrec *label, word orgaddr, byte optype)
{
	char       *line;
	long        val;
	long        val1, val2, val3, val4;
	byte        reg, ts, td;
	word        s, d, sa, da;

	line = *pline;
	if (*line == 0 && optype != EVEN)
		ERROR(_("Missing argument"), "", 1);

	if (optype != BYT && optype != BSS) {
		FIXADDR;
		if (label && (label->addr & 1)) {
			label->addr = addr;
			//   DEBUG   fprintf(stderr,_("decode:bumping label addr to %04X\n"),label->addr);
		}
	}

	switch (optype) {
	case WRD:
		do {
			if (getexpr(&line, orgaddr, 2, &val))
				return 1;

			if (val < -32768 || val > 65535)
				ERROR(_("Integer out of range (%ld)"), val, 1);
			if (pushw(val))
				return 1;
		} while (quoting);
		break;

	case BYT:
		do {
			if (getexpr(&line, orgaddr, 1, &val) == 0) {
				if (val < -128 || val > 255)
					ERROR(_("Byte out of range (%ld)"), val, 1);
				if (pushb(val))
					return 1;
			} else
				return 1;
		} while (quoting);
		break;

	case DWD:
		do {
			if (getexpr(&line, orgaddr, 4, &val) == 0) {
				if (pushw(val >> 16) || pushw(val & 0xffff))
					return 1;
			} else
				return 1;
		} while (quoting);
		break;

	case EQU:
		if (getexpr(&line, orgaddr, 2, &val) == 0) {
			if (label != NULL)
				if (val >= -32768 && val < 65535) {
					label->addr = val;
					label->equ = 1;
				} else
					ERROR(_("Equate too large (%ld)"), val, 1);
		} else
			return 1;
		break;

	case AORG:
		if (getexpr(&line, orgaddr, 2, &val) == 0)
			addr = val;
		else
			return 1;
		break;

	case BSS:
		if (getexpr(&line, orgaddr, 2, &val) == 0)
			addr += val;
		else
			return 1;
		break;

	case INC:
		line = skipspaces(line);
		return assemble(line);

	case EVEN:
		addr = (addr + 1) & 0xfffe;
		break;

////////////////////////////////////////////////////////////////////

	case IMM:
		if (getexpr(&line, orgaddr, 2, &val))
			return 1;

		if (val < -32768 || val > 65535)
			ERROR(_("Integer out of range (%ld)"), val, 1);
		if (pushw(val))
			return 1;
		break;

	case NIMM:
		if (getexpr(&line, orgaddr, 2, &val))
			return 1;

		if (val < -32768 || val > 65535)
			ERROR(_("Integer out of range (%ld)"), val, 1);
		if (pushw(-val))
			return 1;
		break;

	case REG:
		if (getregister(&line, orgaddr, 0))
			return 1;
		break;

	case REGH:
		if (getregister(&line, orgaddr, 6))
			return 1;
		break;

	case REG4:
		if (getregister(&line, orgaddr, 4))
			return 1;
		break;

	case JMP:
		if (getjump(&line, orgaddr))
			return 1;
		break;

	case BIT:
		if (getbit(&line, orgaddr))
			return 1;
		break;

	case CNT:
		if (getcnt(&line, orgaddr, 4, _("Shift count")))
			return 1;
		break;

	case CNTH:
		if (getcnt(&line, orgaddr, 6, _("bit count")))
			return 1;
		break;

	case XOP:
		if (getcnt(&line, orgaddr, 6, _("XOP vector")))
			return 1;
		break;

	case GEN:
		if (getgen(&line, orgaddr, 4, 0))
			return 1;
		break;

	case GENH:
		if (getgen(&line, orgaddr, 10, 6))
			return 1;
		break;

	default:
		FATAL(_("Operand type not implemented!"));
	}

	*pline = line;
	return 0;
}





/////////////////////////////////////////////////////////////////////

/*
	Get a register at pline and change the word at start.
*/
int
getregister(char **pline, word orgaddr, byte start)
{
	byte        neither;
	long        regval;

	if (getexpr(pline, orgaddr, 1, &regval) == 0)
		if (regval >= 0 && regval < 16) {
			WORD(orgaddr) |= (regval << start);
		} else
			ERROR(_("Illegal register specified (%d)"), regval, 1)
				else
			return 1;

	return 0;
}

/*
	Get a jump offset and set.
*/
int
getjump(char **pline, word orgaddr)
{
	long        val;
	long        offs;

	if (getexpr(pline, orgaddr, 2, &val) == 0) {
		if (pass) {
			offs = (val - (signed) addr) / 2;
			if (offs < -127 || offs >= 128)
				ERROR(_("Jump range exceeded by %d words"),
					  (offs < 0 ? offs + 127 : offs - 128), 1);
			WORD(orgaddr) |= (offs & 255);
		}
		return 0;
	} else
		return 1;
}


/*
	Get a bit offset and set.
*/
int
getbit(char **pline, word orgaddr)
{
	long        val;
	long        offs;

	if (getexpr(pline, orgaddr, 1, &val) == 0) {
		if (pass) {
			offs = val;
			if (offs < -127 || offs >= 128)
				ERROR(_("Bit offset out of range (%d)"), offs, 1);
			WORD(orgaddr) |= (offs & 255);
		}
		return 0;
	} else
		return 1;
}


/*
	Get a count.
*/
int
getcnt(char **pline, word orgaddr, byte bit, char *type)
{
	long        val;
	long        offs;
	char        err[256];

	if (getexpr(pline, orgaddr, 1, &val) == 0) {
		if (pass) {
			offs = val;
			if (offs < 0 || offs > 16) {
				sprintf(err, _("%s illegal (%%d)"), type);
				ERROR(err, offs, 1);
			}
			WORD(orgaddr) |= (offs & 15) << bit;
		}
		return 0;
	} else
		return 1;
}

/*
	Get a general thingie.
*/

int
getgen(char **pline, word orgaddr, byte tbit, byte bit)
{
	long        val;
	byte        t;
	char        ch;


	ch = **pline;
	if (ch == '*')				// indirect?  ts=1
	{
		t = 1;
		WORD(orgaddr) |= t << tbit;

		(*pline)++;

		if (getregister(pline, orgaddr, bit))
			return 1;

	} else if (ch == '+')		// autoincrement?  ts=3
	{
		t = 3;
		WORD(orgaddr) |= t << tbit;

		(*pline)++;

		ch = **pline;
		if (ch == '*')			// better be this
			(*pline)++;
		else
			ERROR(_("Syntax error at '%s'"), *pline, 1);

		if (getregister(pline, orgaddr, bit))
			return 1;

	} else if (ch == '@')		// symbolic or indexed?  ts=2
	{
		t = 2;
		WORD(orgaddr) |= t << tbit;

		(*pline)++;

		if (getexpr(pline, orgaddr, 2, &val))
			return 1;

		if (val < -32768 || val > 65535)
			ERROR(_("Index/addr too large (%ld)"), val, 1);

		if (pushw(val))
			return 1;

		*pline = skipspaces(*pline);
		ch = **pline;
		if (ch == '(')			// indexed memory?  ts=2
		{
			(*pline)++;
			if (getregister(pline, orgaddr, bit))
				return 1;
			ch = **pline;
			if (ch != ')')
				ERROR(_("Unterminated index register"), "", 1)
					else
				(*pline)++;

		}

	} else						// register?  ts = 0
	if (getregister(pline, orgaddr, bit))
		return 1;

	return 0;

}
