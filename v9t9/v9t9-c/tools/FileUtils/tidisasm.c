
/*
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
  tidisasm.c 								-- disassemble binary images.
*/

/*
  $Id$
 */

#include <locale.h>
#include "v9t9_common.h"
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>
#include <errno.h>
#include <glib.h>

#include "fiad.h"
#include "dis9900.h"

/* options */
const char *progname;
char *ext = ".dump";
int write_stdout;
const char *outfilename;
int force_nonbinary;
int show_opcode_addr;
int show_comments;
int verbose;
int native_file;

/* structs */

//	ranges of memory and how to treat them
typedef struct mem_range mem_range;
struct mem_range {
	u32 from, size;
	int is_code;
	struct mem_range *next;
};

typedef struct label label;
struct label {
	u16 addr;	// address of label
	char *name;	// real or unique name
	int rel;	// definite relocatable label?
	int rels;	// actual relocatable references (heuristic)
};


/* globals */
u8 mymem[65536];
FILE *file;

mem_range *ranges;
GTree *labels;
int next_label;

static void my_fiad_logger(u32 mask, const char *format, ...)
{
	va_list va;
	int type;
	
	type = mask & LOG_TYPE_MASK;
	if (type == LOG_DEBUG || type == LOG_INFO) return;
//	if (!(mask & LOG_USER)) return;

	va_start(va, format);
	fprintf(stderr, "%s: ", progname);
	vfprintf(stderr, format, va);
	va_end(va);
}

static void init_ranges(void)
{
	ranges = 0L;
}

static mem_range *add_range(u16 from, u16 size, int is_code)
{
	mem_range *range = (mem_range *)xmalloc(sizeof(mem_range));
	mem_range **prev;

	range->from = from;
	range->size = size;
	range->is_code = is_code;
	for (prev = &ranges; (*prev); prev = &(*prev)->next)
	{
		if ((*prev)->from > from)
			break;
	}
	range->next = (*prev) ? (*prev) : 0L;
	(*prev) = range;

	return range;
}

static mem_range *addr_to_range(u16 addr)
{
	mem_range *r;
	for (r = ranges; r; r = r->next) {
		if (addr >= r->from && addr < r->from + r->size)
			return r;
	}
	return 0L;
}

static mem_range *addr_to_code_range(u16 addr)
{
	mem_range *r;
	for (r = ranges; r; r = r->next) {
		if (addr >= r->from && addr < r->from + r->size && r->is_code)
			return r;
	}
	return 0L;
}


static void dump_ranges(void)
{
	mem_range *r = ranges;
	while (r) {
		fprintf(stderr, "%s:  >%04X - >%04X\n", 
				r->is_code ? "C" : "D", r->from, r->from + r->size);
		r = r->next;
	}
}

static u16 read_word(u16 addr)
{
	// find range
	mem_range *r = addr_to_range(addr);
	if (r) {
		u8 *ptr = mymem + addr;
		return (ptr[0]<<8)|ptr[1];
	}
//	fprintf(stderr, "read_word: unknown >%04X\n", addr);
	return 0;
}

static gint label_compare(gconstpointer a, gconstpointer b)
{
	return (u32)a - (u32)b;
}

static void init_labels(void)
{
	labels = g_tree_new(label_compare);
	next_label = 0;
}

static char *unique_name(u16 addr)
{
	char buf[16];
	snprintf(buf, sizeof(buf), "L%04X", addr);
	return xstrdup(buf);
}

static label *add_label(u16 addr, int rel, u16 pc, char *name)
{
	label *l;
	int key = addr | (!rel ? pc*0x10000 : 0);
	l = (label *)g_tree_lookup(labels, (gpointer)key);
	if (l) {
		// don't change named label, or give a new unique name
		// to an already unique name
		if (*l->name != '@' || !name) {
			return l;
		}
		l->addr = addr;
		l->rel = rel;
		l->rels += rel;
		fprintf(stderr, _("renaming label >%04X from %s to "),
				l->addr, l->name);
		xfree(l->name);
		l->name = name ? xstrdup(name) : unique_name(l->addr);
		fprintf(stderr, _("%s\n"), l->name);
	} else {
		l = (label *)xmalloc(sizeof(label));
		l->addr = addr;
		l->name = name ? xstrdup(name) : unique_name(l->addr);
		l->rel = rel;
		l->rels = 0;
		g_tree_insert(labels, (gpointer)key, l);
	}
	return l;
}

static label *find_rel_label(u16 addr)
{
	label *l;
	int key;

	key = addr;
	l = g_tree_lookup(labels, (gpointer)key);
	return l;
}

static label *find_label(u16 addr, int rel, u16 pc)
{
	label *l;
	int key;

	key = addr | (!rel ? pc*0x10000 : 0);
	l = g_tree_lookup(labels, (gpointer)key);
	if (l) return l;
	return l;
}

static label *found_label;
static gint label_search(gpointer _key, gpointer value, gpointer data)
{
	int key = (int)_key;
	u16 addr = (u16)(int)data;
	if ( (key & 0xffff) == addr) {
		found_label = (label *)value;
		return 1;
	}
	return 0;
}

static label *find_any_label(u16 addr)
{
	label *l;
	int key;

	key = addr;
	l = g_tree_lookup(labels, (gpointer)key);
	if (l) return l;

	found_label = 0L;
	g_tree_traverse(labels, label_search, G_IN_ORDER, (gpointer)addr);
	return found_label;
}

static gint dump_label_trav(gpointer key, gpointer value, gpointer data)
{
	label *l = (label *)value;
	fprintf(stderr, ">%04X: %s (%s)\n", l->addr, l->name, l->rel ? "rel" : "???");
	return 0;
}

static void dump_labels(void)
{
	g_tree_traverse(labels, dump_label_trav, G_IN_ORDER, 0L);
}

static u16 op_ea(Operand *op)
{
	if (op->type == OP_IMMED || op->type == OP_ADDR)
		return op->immed;
	else
		return op->ea;
}

static int op_isa_label(Operand *op)
{
	return addr_to_range(op_ea(op))
		&& (op->type == OP_ADDR
			|| op->type == OP_IMMED
			|| op->type == OP_JUMP);
}

//	operand is relocatable if it's in our memory
// 	and is a direct address, a jump target, or
//	a nontrivial register indirect (a likely lookup table)
static int op_is_rel(Instruction *inst, Operand *op)
{
	if (!strcmp(inst->name, "LWPI")) return 1;
	return addr_to_range(op_ea(op))
		&& ((op->type == OP_ADDR && (!op->val || op->immed >= 0x20)) 
			|| op->type == OP_JUMP);
}

//	operand is a register indirect?
static int op_is_reg_ind(Operand *op)
{
	return (op->type == OP_IND || op->type == OP_INC
			|| (op->type == OP_ADDR && op->val));
}

//	register portion of register indirect
static int op_reg(Operand *op)
{
	return op->val;
}

//	Get labels in the code.  We look for operands that
//	reference direct addresses or jump targets and
// 	also those that take an immediate which might be an
//	address.  

//  To determine more accurately if an immediate
//	is a label (is relocatable), we see if the register
//	loaded with an immediate is indirected.

static int gather_labels(void)
{
	mem_range *r;
	u32 	addr;
	label 	*reglabels[16];

#define CLEAR_REG_LABELS() 	memset(reglabels,0,sizeof(reglabels))

	CLEAR_REG_LABELS();
	for (r = ranges; r; r = r->next) {
		if (!r->is_code) continue;

		for (addr = r->from; addr < r->from + r->size; ) {
			Instruction inst;
			u32 newaddr;
			int r;
			label *l;

			newaddr = dis9900_decode(read_word(addr),
									 addr, 0x0, 0x0,
									 read_word, &inst);

			// don't consider labels in data, unless it's an illegal
			// instruction which may actually be an address
			if (strcmp(inst.name, "DATA") || inst.opcode >= 0x200) {
				// look for labels
				if (op_isa_label(&inst.op1)) {
					l = add_label(op_ea(&inst.op1), 
								  op_is_rel(&inst, &inst.op1), 
								  addr,
								  0L);
				}
				// kill label refs
				else if (inst.op1.type == OP_REG && inst.op1.dest == OP_DEST_KILLED)
					reglabels[op_reg(&inst.op1)] = 0;
					

				if (op_isa_label(&inst.op2)) {
					l = add_label(op_ea(&inst.op2), 
								  op_is_rel(&inst, &inst.op2), 
								  addr,
								  0L);
					if (!l->rel && inst.op1.type == OP_REG) {
						reglabels[op_reg(&inst.op1)] = l;
					}
				}

				// look for indirects of pending labels
				if (op_is_reg_ind(&inst.op1) 
					&& (l=reglabels[(r = op_reg(&inst.op1))])
					) 
				{
					l->rel = 1;
					add_label(l->addr, 1 /*rel*/, 0, l->name);
					reglabels[r] = 0L;
				}
				if (op_is_reg_ind(&inst.op2) 
					&& (l=reglabels[(r = op_reg(&inst.op2))])
					) 
				{
					l->rel = 1;
					add_label(l->addr, 1 /*rel*/, 0, l->name);
					reglabels[r] = 0L;
				}
			}

			// assumed end of BB
			if (inst.jump == true) {
				CLEAR_REG_LABELS();
			}

			addr = newaddr;
		}
	}
	return 0;
}

static char *operand_print(Operand *op, char *name, char *buf, int bufsize)
{
	switch (op->type)
	{
	case OP_IMMED:
	case OP_JUMP:
		return name;

	case OP_ADDR:
		if (!op->val)
			snprintf(buf, bufsize, "@%s", name);
		else
			snprintf(buf, bufsize, "@%s(R%d)", name, op->val);
		return buf;

	default:
		fprintf(stderr, "???\n");
		return name;
	}
}

static int
disassemble_code_range(mem_range *r)
{
	u32 addr;

	for (addr = r->from; addr < r->from + r->size; )
	{
		Instruction inst;
		u32 newaddr;
		char op1[32], op2[32], *op1ptr, *op2ptr;
		char ops[64];
		label *l;
		char comment[64];

		newaddr = dis9900_decode(read_word(addr),
										  addr, 0x0, 0x0,
										  read_word, &inst);

		*comment = 0;

		// get operand text
		op1ptr = dis9900_operand_print(&inst.op1, op1, sizeof(op1));

		// try to replace with label
		if (op_isa_label(&inst.op1) 
			&& (l = find_label(op_ea(&inst.op1), 
							   op_is_rel(&inst, &inst.op1), 
							   addr)))
		{
			if (l->rel) {
				op1ptr = operand_print(&inst.op1, l->name, op1, sizeof(op1));
			} else {
				sprintf(comment+strlen(comment), "%s?, ", l->name);
			}
		}

		// get operand text
		op2ptr = dis9900_operand_print(&inst.op2, op2, sizeof(op2));
		if (op2ptr 
			&& op_isa_label(&inst.op2) 
			&& (l = find_label(op_ea(&inst.op2), 
							   op_is_rel(&inst, &inst.op2), 
							   addr)))
		{
			if (l->rel) {
				op2ptr = operand_print(&inst.op2, l->name, op2, sizeof(op2));
			} else {
				sprintf(comment+strlen(comment), "%s? ", l->name);
			}
		}

		if ((l = find_any_label(addr))) {
			fprintf(file, "%-8s", l->name);
		} else {
			fprintf(file, "\t");
		}

		if (show_opcode_addr) {
			fprintf(file, ">%04X: %04X    ", addr, inst.opcode);
		} else {
			fprintf(file, "\t\t");
		}
		fprintf(file, "%-8s ", inst.name);

		snprintf(ops, sizeof(ops), "%s%s%s",
				 op1ptr ? op1ptr : "",
				 op2ptr ? "," : "",
				 op2ptr ? op2ptr : "");

		fprintf(file, verbose ? "%-24s" : "%-32s", ops);

		if (show_comments) {
			if (verbose) {
				if (inst.jump && 
					(inst.op1.type == OP_ADDR || inst.op1.type == OP_JUMP)) {
					sprintf(comment+strlen(comment), "=>%04X  ", inst.op1.ea);
				}
			}
			sprintf(comment+strlen(comment), "; (>%04X: %04X)", addr, inst.opcode);
		}
		if (*comment) {
			fprintf(file, "%s", comment);
		}
		fprintf(file, "\n");

		addr = newaddr;
	}
	return 0;
}

static int
disassemble_data_range(mem_range *r)
{
	u32 addr;
	u16 val;
	int online = 0;
	u8 hex[64];
	char line[80] = {0};
	int lidx = 0;
	label *l = 0;
	int ww = show_opcode_addr ? 5 : 6;
	int mx = (80 - 8 - 8) / (ww+3);

	for (addr = r->from; addr < r->from + r->size || online; addr += 2) {
		l = find_any_label(addr);
		if (l || addr >= r->from + r->size 
			|| online>=mx*2 
			|| (online/2)*(ww+2) + 8 + 3 >= 80) 
		{
			hex[online] = 0;
			line[lidx-1] = 0;
			if (online) {
				fprintf(file, "\t%-*s", 
						(ww)*mx+8, line);
				fprintf(file, " '%-*s'", 
						online, hex);
				fprintf(file, " ; %04X\n", 
						addr - online);
			}
			if (l) {
				fprintf(file, "%s\n", l->name);
			
			}
			online = 0;
			*line = 0;
			lidx = 0;
		}
		if (addr >= r->from + r->size) 
			break;
		if (!online) {
			if (show_opcode_addr) {
				sprintf(line+lidx, ">%04X:  ", addr);
			} else {
				sprintf(line+lidx, "  DATA  ");
			}
			lidx += 8;
		}
		val = read_word(addr);
		if (show_opcode_addr) {
			sprintf(line+lidx, "%04X ", val);
			lidx += 5;
		} else {
			sprintf(line+lidx, ">%04X,", val);
			lidx += 6;
		}

		hex[online++] =  (isprint((val>>8)&0xff)) ? (val>>8) : '.';
		hex[online++] =  (isprint((val)&0xff)) ? (val) : '.';
	}
	return 0;
}

/*	Disassemble the known memory */
static int
disassemble(void)
{
	mem_range *r;
	int ret = 0;

	if (outfilename) {
		file = fopen(outfilename, "wt");
		if (!file) {
			fprintf(stderr, _("%s: cannot write '%s' (%s)\n"),
					progname, outfilename, strerror(errno));
			return 1;
		}
	} else {
		file = stdout;
	}

	// gather info

	for (r = ranges; r; r = r->next) {
		if (show_opcode_addr) {
			fprintf(file, "; range: >%04X->%04X\n", r->from, r->from + r->size);
		} else {
			fprintf(file, "\tAORG >%04X\n", r->from);
		}
		if (r->is_code) {
			if (disassemble_code_range(r)) {
				ret = 1;
				break;
			}
			
		} else {
			if (disassemble_data_range(r)) {
				ret = 1;
				break;
			}
		}
		fprintf(file, "\n");
	}

	if (outfilename)
		fclose(file);
	return ret;
}

static void add_mem(u16 from, int memsize, u8 *mem)
{
	if (from + memsize > 65536) {
		fprintf(stderr, _("%s: range >%04X - >%04X is too large, clipping\n"),
				progname, from, from+memsize);
		memsize = 65536 - from;
	}

	memcpy(mymem + from, mem, memsize);
}


static int
add_file(const char *name, u16 base_addr, u16 *filesize)
{
	OSError err;
	fiad_tifile tf;
	OSSpec spec;
	u16 maxread;
	u8 *mem;
	int memsize;

	/* get the file */
	if ((err = OS_MakeFileSpec(name, &spec)) != OS_NOERR) {
		/* except for this one, my_fiad_logger should be sending errors to stderr */
		fprintf(stderr, _("%s: cannot access file '%s' (%s)\n"),
				progname, name, OS_GetErrText(err));
		return err;
	}

	/* make sure it's valid */
	if (!native_file) {
		fiad_tifile_clear(&tf);
		err = fiad_tifile_setup_spec_with_spec(&tf, &spec);
		if (err != OS_NOERR) {
			fprintf(stderr, _("%s: cannot access v9t9 file '%s' (%s)\n"),
					progname, name, OS_GetErrText(tf.error));
			goto native;
		}

		/* get the FDR for file type */
		if (!fiad_tifile_get_info(&tf)) {
			fprintf(stderr, _("%s: cannot access v9t9 file '%s' (%s)\n"),
					progname, name, OS_GetErrText(tf.error));
			goto native;
		}

		/* decide how to treat it */
		if ((err = fiad_tifile_open_file(&tf, newfileformat, false /*create*/,
										 false /*always*/, true /*readonly*/)) != OS_NOERR)
			return -1;

		/* cache first sector */
		fiad_tifile_read_sector(&tf);

		/* now translate */
		if (force_nonbinary || tf.format == F_TEXT || tf.fdr.flags & ff_program) {
			/* binary file */
			int ret;
	   
			memsize = FDR_FILESIZE(&tf.fdr);
			mem = (u8 *)xmalloc(memsize);
			ret = fiad_tifile_read_binary_image(&tf, mem, memsize, &maxread);
			if (ret != 0 || maxread < memsize) {
				fprintf(stderr, _("%s: file appears short (only read %d of %d)\n"),
						progname, memsize, maxread);
			}
			memsize = maxread;
			add_mem(base_addr, memsize, mem);
			*filesize = memsize;
			xfree(mem);
		}
		else {
			fprintf(stderr, _("%s: I can't deal with non-binary files.  Specify --force to continue.\n"),
					progname);
		}
		fiad_tifile_close_file(&tf);
	}
	else {
		/* try a real file */
		OSError err;
		OSSize sz;
		OSRef ref;

	native:
		if ((err = OS_Open(&spec, OSReadOnly, &ref)) != OS_NOERR
			|| (err = OS_GetSize(ref, &sz)) != OS_NOERR
			|| !(mem = (u8 *)xmalloc(sz))	
			|| (err = OS_Read(ref, mem, &sz)) != OS_NOERR)
		{
			fprintf(stderr, _("%s: can't read '%s'\n"), 
					progname, OS_SpecToString1(&spec));
			OS_Close(ref);
			return errno;
		}
		memsize = sz;
		add_mem(base_addr, memsize, mem);
		*filesize = memsize;
		xfree(mem);
		OS_Close(ref);
	}
	return 0;
}

static int add_range_from_argv(char *str, int is_code)
{
	char *ptr = strchr(str, ':'), *end;
	u32 addrf,addrt;

	if (!ptr) {
		fprintf(stderr, _("%s: bad range (%s), expected 'addr-from:addr-to'\n"),
				progname, str);
		return 1;
	}
	*ptr++ = 0;

	addrf = strtoul(str, &end, 0);
	if (*end || addrf >= 0x10000) {
		fprintf(stderr, _("%s: invalid start address (%s)\n"), 
				progname, str);
		return 1;
	}
	addrt = strtoul(ptr, &end, 0);
	if (*end || addrt > 0x10000) {
		fprintf(stderr, _("%s: invalid end address (%s)\n"), 
				progname, ptr);
		return 1;
	}

	if (addrt < addrf) {
		fprintf(stderr, _("%s: invalid range (%04X - %04X)\n"), 
				progname, addrf, addrt);
		return 1;
	}

	add_range(addrf, addrt - addrf, is_code);
	return 0;
}

static void	
help(void)
{
	printf(_("\n"
		   "tidisasm 9900 Disassembler v1.0\n"
		   "\n"
		   "Usage:   tidisasm [options] { -b <addr> -a <file> } { -r from:to -d from:to }\n"
		   "\n"
		   "tidisasm will disassemble 99/4A files or binaries\n"
		   "\n"
		   "Options:\n"
		   "\t\t-?        -- this help\n"
		   "\t\t-e <ext>  -- specify extension (default: %s)\n"
		   "\t\t-o <file> -- send output to <file> (else stdout)\n"
		   "\t\t-n        -- treat file as native binary (raw dump)\n"
		   "\t\t-f        -- force disassembly of non-PROGRAM v9t9 files\n"
		   "\t\t-b <addr> -- specify logical base address of next -a binary\n"
		   "\t\t-a <file> -- specify file to incorporate\n"
		   "\t\t-r <addr>:<addr> -- specify range to disassemble\n"
		   "\t\t-d <addr>:<addr> -- specify range to treat as data\n"
		   "\t\t-h        -- show opcode and address\n"
		   "\t\t-c        -- show comments\n"
		   "\t\t-v        -- verbose output\n"
		   "\n"),
			 ext
		);
}

int	main(int argc, char **argv)
{
	int		opt;
	int		failed = 0;
	char	*ptr;
	u16		base_addr;
	u16		size = 0;

	setlocale(LC_ALL, "");
	bindtextdomain(PACKAGE, LOCALEDIR);
	textdomain(PACKAGE);

	if (argc <= 1)
	{
		help();
		return 0;
	}

	progname = argv[0];
	outfilename = 0;
	force_nonbinary = 0;
	show_opcode_addr = 0;
	show_comments = 0;
	verbose = 0;
	native_file = 0;

	init_ranges();
	init_labels();

	while ((opt = getopt(argc, argv, "?e:o:nb:a:r:d:hcv")) != -1)
	{
		switch (opt)
		{
		case '?':
			help();
			break;
		case 'e':
			ext = optarg;
			break;
		case 'o':
			outfilename = optarg;
			break;
		case 'f':
			force_nonbinary = 1;
			break;
		case 'b':
			base_addr = strtol(optarg, &ptr, 0);
			if (*ptr) {
				fprintf(stderr, _("%s: invalid base addr (%s)\n"),
						progname, optarg);
				exit(1);
			}
			break;
		case 'a':
			if (add_file(optarg, base_addr, &size)) {
				exit(1);
			}
			break;
		case 'r':
			if (add_range_from_argv(optarg, 1 /*code*/))
				exit(1);
			break;
		case 'd':
			if (add_range_from_argv(optarg, 0 /*code*/))
				exit(1);
			break;
		case 'h':
			show_opcode_addr = 1;
			break;
		case 'c':
			show_comments = 1;
			break;
		case 'v':
			verbose++;
			break;
		case 'n':
			native_file = 1;
			break;
		}
	}

	fiad_set_logger(my_fiad_logger);

	if (argv[optind])
	{
		fprintf(stderr, _("%s: '%s' unexpected\n"), progname, argv[optind]);
	}

	if (!ranges) {
		add_range(base_addr, base_addr + size, 1);
	}

	gather_labels();
	dump_labels();

	dump_ranges();
	failed |= disassemble();

	return failed;
}

