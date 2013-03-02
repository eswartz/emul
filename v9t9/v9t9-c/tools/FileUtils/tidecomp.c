
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
  tidecomp.c 								-- decompile binary images
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

#include <OSLib.h>
#include "xmalloc.h"
#include "fiad.h"
#include "dis9900.h"
#include "dcbitvector.h"

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
struct symtab_list { struct symtab_list *next; u16 addr; } *symtabs;

/* instruction flags */
#define fIsBranch		1	/* changes PC */
#define fIsCondBranch	2	/* conditional branch */
#define fIsCall			4	/* a branch we expect to return */
#define fIsReturn		8	/* return from function */
#define fCheckLater		16	/* guessed flags */
#define fIsLink			32	/* call returns */

/* block flags */
#define fIsData			1	/* not called or branched, probably data */
#define fIsMarked		2	/* worklist */
#define fIsFuncEntry	4	/* called as function */

/* structs */

typedef struct Object Object;
struct Object {
	enum { OBJ_NONE, OBJ_LABEL, OBJ_FUNCTION } type;
	union {
		struct Label *label;
		struct Function *function;
	} u;
	char *name;
};

typedef struct Block Block;

struct Block {
	Block *next, 	*prev;
	struct Label 	*label;
	struct BlockList *succ, *pred;
	u32 			id;
	Instruction 	*first, *last;
	struct Function *function;
	u32 			flags;
};

typedef struct BlockList BlockList;

struct BlockList {
	BlockList 		*next;
	Block 			*block;
};

typedef struct Function Function;

struct Function {
	Function *next, *prev;
	//Block *first, *last;
	BlockList *blocks;
	char *name;
	u32 id;
	struct mem_range *range;
	BitVector blockbv;
	u16 reads, writes, kills;	// register masks
	struct LabelList *entries;
	struct CGNode *node;
};

typedef struct CGNode CGNode;

struct CGNode {
	Function *function;
	struct CGEdge *callers, *callees;
};

typedef struct CGEdge CGEdge;
struct CGEdge {
	CGEdge *next;
	CGNode *node;
	Instruction *inst;		// calling instruction
};

typedef struct CGNodeList {
	struct CGNodeList *next;
	CGNode *node;
}	CGNodeList;

typedef struct CallGraph {
	CGNodeList *roots;
	CGNodeList *leaves;
	CGNodeList *nodes;
}	CallGraph;

//	ranges of memory and how to treat them
typedef struct mem_range mem_range;
struct mem_range {
	u32 from, size;
	int is_code;
	u8	*memory;		    // contents of block (read-only)
	Instruction *code, *codetail;	// if code
	Block *block, *blocktail;	// list of basic blocks
	Function *func, *functail;	// list of functions 
	struct mem_range *next;
};

#define FOR_EACH_RANGE(mr) \
	for (mr = ranges; mr; mr = mr->next)		{ \
		if (!mr->is_code) continue;

#define END_EACH_RANGE(mr) \
	}

#define FOR_EACH_RANGE_BLOCK(mr, block)							\
	for (block = mr->block; block; block = block->next)	{
#define END_EACH_RANGE_BLOCK(mr, block)			\
		if (block == mr->blocktail) break;			\
	}

#define FOR_EACH_RANGE_BLOCK_REV(mr, block)							\
	for (block = mr->blocktail; block; block = block->prev)	{
#define END_EACH_RANGE_BLOCK_REV(mr, block)			\
		if (block == mr->block) break;			\
	}

#define FOR_EACH_FUNC(mr, func)					\
	for (func = mr->func; func; func = func->next)	{
#define END_EACH_FUNC(mr, func)				\
		if (func == mr->functail) break;			\
	}

#define FOR_EACH_BLOCK(func, block)							\
	{ BlockList *__feb_bl = func->blocks; while (__feb_bl) { \
			block = __feb_bl->block; 
#define END_EACH_BLOCK(func, block)			\
	__feb_bl = __feb_bl->next;			   }	\
	}

#define FOR_EACH_INST(block, inst)									\
	for (inst = block->first; inst; inst = inst->next)	{
#define END_EACH_INST(block, inst)					\
		if (inst == block->last) break;			\
	}

typedef struct Label Label;
struct Label {
	Block *block;	// block owning label
	u32 id;		// unique label
	u16 addr;	// address of label
	char *name;	// real or unique name
	int rel;	// definite relocatable label?
	int rels;	// actual relocatable references (heuristic)
	bool func;	// referenced from BL or BLWP?
	bool named; // actually a real name
};

typedef struct LabelList LabelList;
struct LabelList {
	Label *label;
	LabelList *next;
};

/* globals */
u8 mymem[65536];
FILE *file;

mem_range *ranges;
GTree *labels;
int next_label;
CallGraph *callgraph;

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
	range->memory = mymem+from;
	range->code = range->codetail = 0L;
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
		u8 *ptr = r->memory + (addr - r->from);
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

static Label *add_label(u16 addr, int rel, u16 pc, char *name)
{
	Label *l;
	int key = addr | (!rel ? pc*0x10000 : 0);
	l = (Label *)g_tree_lookup(labels, (gpointer)key);
	if (l) {
		// don't change named label, or give a new unique name
		// to an already unique name
		if (l->named || !name) {
			return l;
		}
		l->addr = addr;
		l->rel = rel;
		l->rels += rel;
		fprintf(stderr, _("renaming label >%04X from %s to "),
				l->addr, l->name);
		xfree(l->name);

		l->name = name ? xstrdup(name) : unique_name(l->addr);
		l->named = name != 0L;
		fprintf(stderr, _("%s\n"), l->name);
	} else {
		l = (Label *)xmalloc(sizeof(Label));
		l->block = 0L;
		l->addr = addr;
		l->name = name ? xstrdup(name) : unique_name(l->addr);
		l->named = name != 0L;
		l->rel = rel;
		l->rels = 0;
		l->func = false;
		g_tree_insert(labels, (gpointer)key, l);
	}
	return l;
}

static Label *find_rel_label(u16 addr)
{
	Label *l;
	int key;

	key = addr;
	l = (Label*)g_tree_lookup(labels, (gpointer)key);
	return l;
}

static Label *find_label(u16 addr, int rel, u16 pc)
{
	Label *l;
	int key;

	key = addr | (!rel ? pc*0x10000 : 0);
	l = (Label*)g_tree_lookup(labels, (gpointer)key);
	if (l) return l;
	return l;
}

static Label *found_label;
static gint label_search(gpointer _key, gpointer value, gpointer data)
{
	int key = (int)_key;
	u16 addr = (u16)(int)data;
	if ( (key & 0xffff) == addr) {
		found_label = (Label *)value;
		return 1;
	}
	return 0;
}

static Label *find_any_label(u16 addr)
{
	Label *l;
	int key;

	key = addr;
	l = (Label*)g_tree_lookup(labels, (gpointer)key);
	if (l) return l;

	found_label = 0L;
	g_tree_traverse(labels, label_search, G_IN_ORDER, (gpointer)(unsigned)addr);
	return found_label;
}

static gint dump_label_trav(gpointer key, gpointer value, gpointer data)
{
	Label *l = (Label *)value;
	fprintf(stderr, ">%04X: %s (%s)\n", l->addr, l->name, l->rel ? "rel" : "???");
	return 0;
}

static void dump_labels(void)
{
	g_tree_traverse(labels, dump_label_trav, G_IN_ORDER, 0L);
}

//	Disassemble code into higher level
static int get_code(void)
{
	mem_range *r;
	u32 	addr;

	for (r = ranges; r; r = r->next) {
		if (!r->is_code) continue;

		r->code = r->codetail = 0L;
		for (addr = r->from; addr < r->from + r->size; ) {
			Instruction *inst;
			u32 newaddr;
			Label *l;

			inst = (Instruction *)xmalloc(sizeof(Instruction));

			addr = dis9900_decode(read_word(addr),
									 addr, 0x0, 0x0,
									 read_word, inst);

			if (inst->jump) {
				if (inst->inst == Ibl || inst->inst == Iblwp)
					inst->flags |= fIsCall+fIsBranch+fIsLink;
				else if (inst->inst == Irtwp)
					inst->flags |= fIsReturn+fIsBranch; /* B *R11 detected later */
				else if (inst->jump == OP_JUMP_COND)
					inst->flags |= fIsCondBranch+fIsBranch;
				else 
					if (inst->inst == Ib && inst->op1.type == OP_ADDR)
						inst->flags |= fIsBranch+fIsCall+fCheckLater;
					else
						inst->flags |= fIsBranch;
			}

			inst->prev = r->codetail;
			if (r->code)
				r->codetail->next = inst;
			else
				r->code = inst;
			r->codetail = inst;
			inst->next = 0L;
			
		}
	}
	return 0;

}

//	Get entry points into each module.

static void add_prog_list(mem_range *r, u16 list)
{
	u16 addr, link;
	u8 name[256], len;
	Label *l;
	fprintf(stdout, "Scanning program list at >%04X\n", list);
	while (list) {
		link = (mymem[list]<<8)|mymem[list+1];
		addr = (mymem[list+2]<<8)|mymem[list+3];
		len = mymem[list+4];
		memcpy(name, &mymem[list+5], len);
		name[len] = 0;
		fprintf(stdout, "Adding label %s at >%04X\n", name, addr);
		l = add_label(addr, 0, addr, *name ? name : 0L);
		l->func = true;
		list = link;
	}
}

static int get_entries(void)
{
	mem_range *r;
	struct symtab_list *sl;
	u8 *ptr;
	u16 addr;
	u8 name[8];
	int i;
	Label *l;

	// Get explicit symbol tables
	for (sl = symtabs; sl; sl = sl->next) {
		r = addr_to_range(sl->addr-1);
		if (!r) {
			fprintf(stderr, "!!! Can't find range containing >%04X\n", sl->addr-1);
			continue;
		}
		ptr = r->memory + (sl->addr - r->from);
		while (1) {
			ptr -= 2;
			addr = (ptr[0]<<8)|(ptr[1]);
			if (!addr) break;
			name[6] = 0;
			for (i=0; i<6; i++) {
				name[5-i] = *--ptr;
				if (name[5-i] == 0x20) name[5-i] = 0;
			}
			
			// now, these are almost always vectors, so take the PC
			addr = (mymem[addr+2]<<8) | mymem[addr+3];
			fprintf(stdout, "Adding label %s at >%04X\n", name, addr);
			l = add_label(addr, 0, addr, name);
			l->func = true;
		}
	}

	// Get standard entries
	FOR_EACH_RANGE(r) {
		if (r->memory[0] == 0xaa) {
			fprintf(stdout, "Scanning standard header at >%04X\n", r->from);
			addr = (mymem[r->from+4]<<8)|mymem[r->from+5];
			if (addr) add_prog_list(r, addr);
			addr = (mymem[r->from+6]<<8)|mymem[r->from+7];
			if (addr) add_prog_list(r, addr);
			addr = (mymem[r->from+8]<<8)|mymem[r->from+9];
			if (addr) add_prog_list(r, addr);
			addr = (mymem[r->from+10]<<8)|mymem[r->from+11];
			if (addr) add_prog_list(r, addr);
		}


		if (r->from == 0) {
			int xop;

			// reset
			addr = (mymem[2]<<8) | mymem[3];
			if (addr > 0 && addr < 0x2000 && !(addr&1)) { 
				fprintf(stdout, "Adding RESET vector at >%04X\n", addr);
				l = add_label(addr, 0, addr, "RESET");
				l->func = true;
			}

			// int1
			addr = (mymem[6]<<8) | mymem[7];
			if (addr > 0 && addr < 0x2000 && !(addr&1)) {
				fprintf(stdout, "Adding INT1 vector at >%04X\n", addr);
				l = add_label(addr, 0, addr, "INT_1");
				l->func = true;
			}

			// int2
			addr = (mymem[10]<<8) | mymem[11];
			if (addr > 0 && addr < 0x2000 && !(addr & 1)) {
				fprintf(stdout, "Adding INT2 vector at >%04X\n", addr);
				l = add_label(addr, 0, addr, "INT_2");
				l->func = true;
			}

			for (xop = 0; xop < 16; xop++) {
				// XOP
				char buf[16];
				addr = (mymem[0x42+xop*4]<<8) | mymem[0x43+xop*4];
				if (addr && addr < 0x2000 && !(addr&1)) {
					fprintf(stdout, "Adding XOP %d vector at >%04X\n", xop, addr);
					snprintf(buf, sizeof(buf), "XOP_%d", xop);
					l = add_label(addr, 0, addr, buf);
					l->func = true;
				}
			}
		}
	}
	END_EACH_RANGE(r);
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

//	Simple pass to check for branch/bl/blwp targets

static int get_labels(void)
{
	mem_range *r;
	Instruction *inst;
	Object *obj;

	for (r = ranges; r; r = r->next) {
		if (!r->is_code) continue;

		for (inst = r->code; inst; inst = inst->next) {
			int r;
			Label *l;

			if (inst->inst == Ibl || inst->inst == Ib || inst->inst == Iblwp
				|| inst->inst == Ijmp || inst->jump == OP_JUMP_COND) 
			{
				l = 0L;
				if (op_isa_label(&inst->op1)) {
					if (inst->inst != Iblwp) {
						l = add_label(op_ea(&inst->op1), 
									  op_is_rel(inst, &inst->op1), 
									  inst->pc,
									  0L);
						
						if (inst->inst == Ibl)
							l->func = true;
					}
					else {
						// need to read vector
						if (inst->op1.type == OP_ADDR) {
							u16 vecaddr = op_ea(&inst->op1);
							u16 addr = (mymem[vecaddr+2]<<8)|mymem[vecaddr+3];
						
							fprintf(stdout, "Adding BLWP vector at >%04X\n", addr);
							l = add_label(addr, 
										  false,
										  inst->pc,
										  0L);
						}
					}
				}

				if (l) {
					obj = (Object*)xcalloc(sizeof(Object));
					obj->type = OBJ_LABEL;
					obj->u.label = l;
					obj->name = l->name;

					inst->op1.obj = obj;
				}

#if 0
				if (op_isa_label(&inst->op2)) {
					l = add_label(op_ea(&inst->op2), 
								  op_is_rel(inst, &inst->op2), 
								  inst->pc,
								  0L);

					if (inst->inst == Ibl || inst->inst == Iblwp)
						l->func = true;

					obj = (Object*)xcalloc(sizeof(Object));
					obj->type = OBJ_LABEL;
					obj->u.label = l;
					obj->name = l->name;
					inst->op2.obj = obj;
				}
#endif
			}
		}
	}
	return 0;
}


//	Get labels in the code.  We look for operands that
//	reference direct addresses or jump targets and
// 	also those that take an immediate which might be an
//	address.  

//  To determine more accurately if an immediate
//	is a label (is relocatable), we see if the register
//	loaded with an immediate is indirected.

static int get_labels_flow(void)
{
	mem_range *r;
	Label 	*reglabels[16];
	Instruction *inst;
	Object *obj;
#define CLEAR_REG_LABELS() 	memset(reglabels,0,sizeof(reglabels))

	CLEAR_REG_LABELS();
	for (r = ranges; r; r = r->next) {
		if (!r->is_code) continue;

		for (inst = r->code; inst; inst = inst->next) {
			int r;
			Label *l;

			// don't consider labels in data, unless it's an illegal
			// instruction which may actually be an address
			if (inst->inst == Idata || inst->opcode >= 0x200) {
				// look for labels
				if (op_isa_label(&inst->op1)) {
					l = add_label(op_ea(&inst->op1), 
								  op_is_rel(inst, &inst->op1), 
								  inst->pc,
								  0L);

					if (inst->inst == Ibl || inst->inst == Iblwp)
						l->func = true;

					obj = (Object*)xcalloc(sizeof(Object));
					obj->type = OBJ_LABEL;
					obj->u.label = l;
					obj->name = l->name;

					inst->op1.obj = obj;
				}
				// kill label refs
				else if (inst->op1.type == OP_REG && inst->op1.dest == OP_DEST_KILLED)
					reglabels[op_reg(&inst->op1)] = 0;
					

				if (op_isa_label(&inst->op2)) {
					l = add_label(op_ea(&inst->op2), 
								  op_is_rel(inst, &inst->op2), 
								  inst->pc,
								  0L);

					if (inst->inst == Ibl || inst->inst == Iblwp)
						l->func = true;

					obj = (Object*)xcalloc(sizeof(Object));
					obj->type = OBJ_LABEL;
					obj->u.label = l;
					obj->name = l->name;
					inst->op2.obj = obj;

					if (!l->rel && inst->op1.type == OP_REG) {
						reglabels[op_reg(&inst->op1)] = l;
					}
				}

				// look for indirects of pending labels
				if (op_is_reg_ind(&inst->op1) 
					&& (l=reglabels[(r = op_reg(&inst->op1))])
					) 
				{
					l->rel = 1;
					add_label(l->addr, 1 /*rel*/, 0, l->name);
					reglabels[r] = 0L;
				}
				if (op_is_reg_ind(&inst->op2) 
					&& (l=reglabels[(r = op_reg(&inst->op2))])
					) 
				{
					l->rel = 1;
					add_label(l->addr, 1 /*rel*/, 0, l->name);
					reglabels[r] = 0L;
				}
			}

			// assumed end of BB
			if (inst->jump == true) {
				CLEAR_REG_LABELS();
			}
		}
	}
	return 0;
}

//	Traverse the instructions and make a blocklist
//	(using instructions from mem_range.code[tail] and
//	adding to mem_range.block[tail])

static int get_blocks(void)
{
	mem_range *r;
	u32 	addr;
	Instruction *inst;
	Block *curblock;
	u32 blockid;

	for (r = ranges; r; r = r->next) {
		if (!r->is_code) continue;

		r->block = r->blocktail = 0L;	//!!! clean
		curblock = 0L;
		blockid = 0;

		for (inst = r->code; inst; inst = inst->next) {
			Label *l = find_any_label(inst->pc);
			if (l || !curblock 
				|| (inst->prev && inst->prev->jump && inst->prev->inst != Ibl && inst->prev->inst != Iblwp)) {
				// new block
				Block *n = (Block*)xcalloc(sizeof(Block));

				n->id = blockid++;
				n->prev = curblock;
				if (curblock) {
					curblock->next = n;
				} else {
					r->block = n;
				}
				r->blocktail = n;
				curblock = n;
				curblock->first = inst;
			}

			if (l) {
				// this address is presumably branched to
				l->block = curblock;
				curblock->label = l;
			}

			inst->block = curblock;
			curblock->last = inst;
			if (inst == r->codetail) break;
		}
		curblock->last = r->codetail;
	}

	return 0;
}


//	For each block, figure out the 
//	successors/predecessors lists 
static void AddSucc(Instruction *inst, Block *block)
{
	BlockList *bl;

	for (bl = inst->block->succ; bl; bl = bl->next)
		if (bl->block == block) break;
	if (!bl) {
		bl = (BlockList*)xmalloc(sizeof(BlockList));
		bl->block = block;
		bl->next = inst->block->succ;
		inst->block->succ = bl;
	}

	for (bl = block->pred; bl; bl = bl->next)
		if (bl->block == inst->block) break;
	if (!bl) {
		bl = (BlockList*)xmalloc(sizeof(BlockList));
		bl->block = inst->block;
		bl->next = block->pred;
		block->pred = bl;
	}
}

static int get_flow(void)
{
	mem_range *r;
	Function *func;
	Block *block;
	Instruction *inst;

	// !!!clean
	FOR_EACH_RANGE(r) {
		FOR_EACH_RANGE_BLOCK(r, block) {
			block->succ = block->pred = 0L;
		}
		END_EACH_RANGE_BLOCK(r, block);
	}
	END_EACH_RANGE(r);

	FOR_EACH_RANGE(r) {
		FOR_EACH_RANGE_BLOCK(r, block) {
			inst = block->last;
			if (inst->flags & fIsBranch+fIsCondBranch) {
				if (!(inst->flags & fIsCall)) {
					// jump?
					if (inst->op1.obj && inst->op1.obj->type == OBJ_LABEL)
					{
						if (inst->op1.obj->u.label->block)
							AddSucc(inst, inst->op1.obj->u.label->block);
						else
							fprintf(stdout, "??? Ignoring branch to label %s from >%04X\n", inst->op1.obj->u.label->name, inst->pc);
					}
					if (inst->op2.obj && inst->op2.obj->type == OBJ_LABEL)
					{
						if (inst->op2.obj->u.label->block)
							AddSucc(inst, inst->op2.obj->u.label->block);
						else
							fprintf(stdout, "??? Ignoring branch to label %s from >%04X\n", inst->op2.obj->u.label->name, inst->pc);
					}
				}

				// fallthrough?
				if ((inst->flags & fIsCondBranch) 
					|| (inst->flags & fIsCall+fIsLink) == fIsCall+fIsLink)
				{
					if (block->next)
						AddSucc(inst, block->next);
					else
						fprintf(stdout, "??? Ignoring fallthrough after >%04X\n", inst->pc);
				}
			}
			else {
				// normal fall through
				if (block->next)
					AddSucc(inst, block->next);
				else
					fprintf(stdout, "??? Ignoring fallthrough after >%04X\n", inst->pc);
			}
		}
		END_EACH_RANGE_BLOCK(r, block);
	}
	END_EACH_RANGE(r);

	return 0;
}

//	Look for chains of branches and infer functions from these

static void get_block_set(Function *func, Block *block)
{
	BlockList *bl;
	if (bv_BitSet(block->id, &func->blockbv)) return;

	bv_SetBit(block->id, &func->blockbv);
	block->flags |= fIsMarked;

	for (bl = block->succ; bl; bl = bl->next) {
		get_block_set(func, bl->block);
	}
	for (bl = block->pred; bl; bl = bl->next) {
		get_block_set(func, bl->block);
	}
}

static Function *add_function(mem_range *r, Block *entry, u32 fid)
{
	Function *func = XMNEWCLEAR(Function);
	char buf[256];
	LabelList *ll;

	if (entry->label && !entry->label->named) {
		snprintf(buf, sizeof(buf), "F%04X", entry->first->pc);
		func->name = xstrdup(buf);
	}
	else {
		func->name = xstrdup(entry->label->name);
	}
	func->id = fid++;
	func->range = r;
	func->prev = func;
	bv_InitVector(&func->blockbv);

	ll = XMNEW(LabelList);
	ll->label = entry->label;
	ll->next = 0L;
	func->entries = ll;

	if (r->functail) r->functail->next = func;
	else r->func = func;
	func->prev = r->functail;
	r->functail = func;

	entry->flags |= fIsFuncEntry;
	//entry->block->flags |= fIsMarked;

	get_block_set(func, entry);
	fprintf(stdout, "Dump of function bitvector: ");
	bv_Dump(fprintf, stdout, func->name, &func->blockbv);

	return func;
}

static LabelList *sort_labellist(LabelList *list)
{
	LabelList *ll, *ll2;
	for (ll = list; ll; ll = ll->next) {
		for (ll2 = ll->next; ll2; ll2 = ll2->next) {
			if (ll->label->block->first->pc > ll2->label->block->first->pc) {
				Label *tmp = ll->label;
				ll->label = ll2->label;
				ll2->label = tmp;
			}
		}
	}
	return list;
}

static int get_functions(void)
{
	bool changed;
	Block *block;
	mem_range *r;
	Function *func = 0;
	uint32 fid;
	Instruction *inst;

	FOR_EACH_RANGE(r) {
		r->func = r->functail = 0L;
		FOR_EACH_RANGE_BLOCK(r, block) {
			block->flags &= ~fIsMarked;
		}
		END_EACH_RANGE_BLOCK(r, block);
	}
	END_EACH_RANGE(r);

	fid = 0;
	while (true) {
	call_retry:
		// find called functions
		FOR_EACH_RANGE(r) {
			FOR_EACH_RANGE_BLOCK(r, block) {
				FOR_EACH_INST(block, inst) {
					Block *entry = 0L;
					if (inst->flags & fIsCall 
						&& inst->op1.obj 
						&& inst->op1.obj->type == OBJ_LABEL
						&& (entry = inst->op1.obj->u.label->block)
						&& !(entry->flags & fIsFuncEntry))
					{
						add_function(r, entry, fid++);
						goto call_retry;
					}
				}
				END_EACH_INST(block, inst);
			}
			END_EACH_RANGE_BLOCK(r, block);
		}
		END_EACH_RANGE(r);

	label_retry:
		// no more calls; look for unclaimed labeled blocks
		FOR_EACH_RANGE(r) {
			FOR_EACH_RANGE_BLOCK(r, block) {
				if (block->label 
				&& !(block->flags & fIsFuncEntry)
				&& (!(block->flags & fIsMarked) 
					|| (block->label->func) ))
				{
					block->label->func = false;
					add_function(r, block, fid++);
					goto label_retry;
				}
			}
			END_EACH_RANGE_BLOCK(r, block);
		}
		END_EACH_RANGE(r);

		break;	// no block found
	}

	// Now, see which functions overlap and combine them
	do {
		Function *func2;

		changed = false;
		FOR_EACH_RANGE(r) {
			FOR_EACH_FUNC(r, func) {
				Function *func2next;
//				fprintf(stderr, "func is %s\n", func->name);
				if (func->entries) for (func2 = func->next; func2; func2 = func2next) {
//					fprintf(stderr, "func2 is %s\n", func2->name);
					if (func2->entries && bv_BitsInCommon(&func->blockbv, &func2->blockbv)) {
						// they overlap; make func2 an entry into func,
						// and remove func2 from list
						LabelList *ll;

						fprintf(stdout, "Combining %s with %s\n",
								func->name, func2->name);
						for (ll = func->entries; ll->next; ll = ll->next) /**/;
						ll->next = func2->entries;
						func2->entries = 0L;
						bv_Clear(&func2->blockbv);

/*
						// remove from list
						func2->prev->next = func2->next;
						if (func2->next)
							func2->next->prev = func2->prev;
						if (func2 == r->functail) r->functail = func2->prev;
						if (func2 == r->func) r->func = func2->next;
						func2next = func2->next;
						xfree(func2);
*/
						func2next = func2->next;
						changed = true;
					}
					else
						func2next = func2->next;
					if (func2 == r->functail) break;
				}
			}
			END_EACH_FUNC(r, func);
		}
		END_EACH_RANGE(r);
		
	} while (changed);

	// Remove dead functions
	FOR_EACH_RANGE(r) {
		Function *next;
		for (func = r->func; func; func = next) {
			if (!func->entries) {
				// remove from list

				if (func->prev)
					func->prev->next = func->next;
				else
					r->func = func->next;
				if (func->next)
					func->next->prev = func->prev;
				else
					r->functail = func->prev;
				next = func->next;
				xfree(func);
			}
			else
				next = func->next;
			if (func == r->functail) break;
		}
	}
	END_EACH_RANGE(r);

	// Assign block lists
	FOR_EACH_RANGE(r) {
		Function *next;
		FOR_EACH_RANGE_BLOCK_REV(r, block) {
			FOR_EACH_FUNC(r, func) {
				if (bv_BitSet(block->id, &func->blockbv)) {
					BlockList *bl;
					bl = XMNEW(BlockList);
					bl->block = block;
					bl->next = func->blocks;
					func->blocks = bl;
					block->function = func;
				}
			}
			END_EACH_FUNC(r, func);
		}
		END_EACH_RANGE_BLOCK_REV(r, block);
	}
	END_EACH_RANGE(r);

	// Now, sort the entries and give the best name

	FOR_EACH_RANGE(r) {
		FOR_EACH_FUNC(r, func) {
			func->entries = sort_labellist(func->entries);
			if (func->entries->label->named) {
				xfree(func->name);
				func->name = xstrdup(func->entries->label->name);
			}
		}
		END_EACH_FUNC(r, func);
	}
	END_EACH_RANGE(r);

	return 0;
}

//	Get the call graph
static int get_callgraph(void)
{
	bool changed;
	Block *block;
	mem_range *r;
	Function *func;
	Instruction *inst;
	CGNode *node;
	CGNodeList *nl;

	callgraph = XMNEWCLEAR(CallGraph);

	// find called functions
	FOR_EACH_RANGE(r) {
		FOR_EACH_FUNC(r, func) {

			// create function node if necessary
			if (!func->node) {
				node = XMNEWCLEAR(CGNode);
				nl = XMNEWCLEAR(CGNodeList);
				node->function = func;
				func->node = node;
				nl->node = node;
				nl->next = callgraph->nodes;
				callgraph->nodes = nl;
			}

			FOR_EACH_BLOCK(func, block) {
				FOR_EACH_INST(block, inst) {
					Block *entry;
					if (inst->flags & fIsCall 
						&& inst->op1.obj 
						&& inst->op1.obj->type == OBJ_LABEL
						&& (entry = inst->op1.obj->u.label->block)
						&& (entry->flags & fIsFuncEntry))
					{
						CGEdge *e = XMNEWCLEAR(CGEdge);

						// create function node if necessary
						if (!(node = entry->function->node)) {
							node = XMNEWCLEAR(CGNode);
							nl = XMNEWCLEAR(CGNodeList);
							node->function = entry->function;
							nl->node = node;
							nl->next = callgraph->nodes;
							callgraph->nodes = nl;
							entry->function->node = node;
						}

						// add caller edge
						e->node = func->node;
						e->inst = inst;
						e->next = node->callers;
						node->callers = e;

						// add callee edge
						e = XMNEWCLEAR(CGEdge);
						e->node = node;
						e->inst = inst;
						e->next = func->node->callees;
						func->node->callees = e;
					}	
				}	
				END_EACH_INST(block, inst);
			}
			END_EACH_BLOCK(func, block);
		}
		END_EACH_FUNC(r, func);
	}
	END_EACH_RANGE(r);

	// finish side structures
	for (nl = callgraph->nodes; nl; nl = nl->next) {
		node = nl->node;
		if (!node->callees) {
			CGNodeList *nl = XMNEW(CGNodeList);
			nl->node = node;
			nl->next = callgraph->leaves;
			callgraph->leaves = nl;
		}
		if (!node->callers) {
			CGNodeList *nl = XMNEW(CGNodeList);
			nl->node = node;
			nl->next = callgraph->roots;
			callgraph->roots = nl;
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


//	LOW-LEVEL
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
		Label *l;
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
		if (op_isa_label(&inst.op2) 
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

///////////////////////////////////////////////////////

//	HIGH-LEVEL


static char *hl_operand_print(Instruction *inst, Operand *op,
						   char *buf, int bufsize, 
						   char *comment, int commentsize)
{
	if (op->obj)
	{
		if (op->obj->type == OBJ_LABEL && !op->obj->u.label->rel)
		{
			snprintf(comment+strlen(comment), commentsize-strlen(comment), 
					 "%s?, ", op->obj->name);
			goto normal;
		}

		switch (op->type) 
		{
		case OP_JUMP:
		case OP_IMMED:
			if (op->obj->type == OBJ_LABEL && op->obj->u.label->block) {
				if (op->obj->u.label->named)
					snprintf(buf, bufsize, "%s(B%d)", op->obj->name, op->obj->u.label->block->id);
				else
					snprintf(buf, bufsize, "B%d", op->obj->u.label->block->id);
				return buf;
			}
			else
				return op->obj->name;

		case OP_ADDR:
			if (!op->val)
				snprintf(buf, bufsize, "@%s", op->obj->name);
			else
				snprintf(buf, bufsize, "@%s(R%d)", op->obj->name, op->val);
			return buf;

		default:
			fprintf(stderr, "???\n");
			return op->obj->name;
		}
	}

normal:
	return dis9900_operand_print(op, buf, bufsize);
}

static Function *get_function(Instruction *inst, Operand *op)
{
	if (op->obj && op->obj->type == OBJ_FUNCTION) return op->obj->u.function;
	return 0L;
}

static void format_instruction(Instruction *inst, bool highlevel,
							   char *ops, long opssize, char *comment, long commentsize)
{
	char op1[32], op2[32], *op1ptr, *op2ptr;
	Label *l;

	*comment = 0;
	*ops = 0;

	if (highlevel) {
		op1ptr = hl_operand_print(inst, &inst->op1, op1, sizeof(op1), comment, sizeof(comment));
		op2ptr = hl_operand_print(inst, &inst->op2, op2, sizeof(op2), comment, sizeof(comment));
	}
	else {
		op1ptr = dis9900_operand_print(&inst->op1, op1, sizeof(op1));
		op2ptr = dis9900_operand_print(&inst->op2, op2, sizeof(op2));

	}

	snprintf(ops, opssize, "%s%s%s",
			 op1ptr ? op1ptr : "",
			 op2ptr ? "," : "",
			 op2ptr ? op2ptr : "");

	if (verbose) {
		if (inst->jump && 
			(inst->op1.type == OP_ADDR || inst->op1.type == OP_JUMP)) {
			sprintf(comment+strlen(comment), "=>%04X  ", inst->op1.ea);
		}
	}
	sprintf(comment+strlen(comment), "; (>%04X: %04X)", inst->pc, inst->opcode);
}

static int
dump_instruction(FILE *file, Instruction *inst)
{
	char ops[64], comment[64];
	format_instruction(inst, true, ops, sizeof(ops), comment, sizeof(comment));
	if (show_opcode_addr) {
		fprintf(file, ">%04X: %04X    ", inst->pc, inst->opcode);
	} else {
		fprintf(file, "\t\t");
	}
	fprintf(file, "%-8s ", inst->name);
	fprintf(file, verbose ? "%-24s" : "%-32s", ops);
	if (*comment) {
		fprintf(file, "%s", comment);
	}
	fprintf(file, "\n");
	return 0;
}

static int
dump_block(FILE *file, Block *block)
{
	Instruction *inst = block->first;
	BlockList *bl;

	fprintf(file, "B%d: ", block->id);
	if (block->pred || block->succ) {
		fprintf(file, "\tpred: ");
		for (bl = block->pred; bl; bl = bl->next) {
			fprintf(file, "B%d ", bl->block->id);
		}
		fprintf(file, " ");
		fprintf(file, "succ: ");
		for (bl = block->succ; bl; bl = bl->next) {
			fprintf(file, "B%d ", bl->block->id);
		}
	}
	fprintf(file, "\n");
	for (inst = block->first; inst; inst = inst->next) {
		if (inst == block->first && block->label) {
			fprintf(file, "%-8s", block->label->name);
		} else {
			fprintf(file, "\t");
		}

		dump_instruction(file, inst);

		if (inst == block->last) break;
	}
	return 0;
}

//	Dump block list
static void dump_blocks(FILE *file, Block *first, Block *last)
{
	Block *b;

	for (b = first; b; b = b->next) {
		dump_block(file, b);
		if (b == last) break;
	}
}

//	Dump block list
static void dump_blocklist(FILE *file, BlockList *bl)
{
	while (bl) {
		dump_block(file, bl->block);
		bl = bl->next;
	}
}

//	Dump blocklist
static void dump_all_blocks(FILE *file)
{
	mem_range *r;
	Block *b;

	for (r = ranges; r; r = r->next) {
		if (!r->is_code) continue;

		fprintf(file, "Dump of mem_range >%04X:\n", r->from);
		dump_blocks(file, r->block, r->blocktail);
	}
}


//	Dump results of flowgraph
static void dump_flow(FILE *file)
{
	mem_range *r;
	Block *b;

	for (r = ranges; r; r = r->next) {
		if (!r->is_code) continue;

		fprintf(file, "Dump of mem_range >%04X:\n", r->from);
		dump_blocks(file, r->block, r->blocktail);
	}
}

//	Dump a function
static void dump_function(FILE *file, Function *f)
{
	LabelList *ll;
	CGEdge *e;
	fprintf(file, "\nFunction %d: %s\n", f->id, f->name);
	fprintf(file, "\tbitvector: ");
	bv_Dump(fprintf, file, f->name, &f->blockbv);
	fprintf(file, "\tentries: ");
	for (ll = f->entries; ll; ll = ll->next)
		fprintf(file, "%s ", ll->label->name);
	fprintf(file, "\n");
	if (f->node) {
		fprintf(file, "\tcallers: ");
		for (e = f->node->callers; e; e = e->next) {
			fprintf(file, "%s ", e->node->function->name);
		}
		fprintf(file, "\n");
		fprintf(file, "\tcallees: ");
		for (e = f->node->callees; e; e = e->next) {
			fprintf(file, "%s ", e->node->function->name);
		}
		fprintf(file, "\n");
	}
	dump_blocklist(file, f->blocks);
}

//	Dump results of function gathering
static void dump_functions(FILE *file)
{
	mem_range *r;
	Function *f;

	for (r = ranges; r; r = r->next) {
		if (!r->is_code) continue;

		fprintf(file, "Dump of mem_range >%04X:\n", r->from);
		for (f = r->func; f; f = f->next) {
			dump_function(file, f);
			if (f == r->functail) break;
		}
	}
}

//	Dump block graph
static void dump_block_graph(FILE *file)
{
	mem_range *r;
	Function *func;
	Block *block;
	BlockList *bl;

	fprintf(file, "digraph program {\n");
	FOR_EACH_RANGE(r) {
		fprintf(file, "subgraph cluster_R%04X {\n", r->from);
		FOR_EACH_FUNC(r, func) {
			fprintf(file, "subgraph cluster_%s {\n", func->name);
			FOR_EACH_BLOCK(func, block) {
				for (bl = block->succ; bl; bl = bl->next) {
					char ops[64], comment[64];
					char labbuf[64], *labptr;
					char *size;

					fprintf(file, "B%d -> B%d [", block->id, bl->block->id);

					fprintf(file, "weight=%d",
							bl->block->first->pc - block->first->pc);
					if ((block->last->flags & fIsBranch) 
						&& block->last->next
						&& block->last->next->block != bl->block) 
					{
						format_instruction(block->last, false, ops, sizeof(ops), comment, sizeof(comment));
						fprintf(file, ",label=\"%s %s\"", block->last->name, ops);
					}
					fputs("];\n", file);

					if (!block->label)
						snprintf(labbuf, sizeof(labbuf), "L%04X", block->first->pc), labptr = labbuf;
					else
						labptr = block->label->name;

					if (block->last->pc - block->first->pc >= 0x80)
						size = "16";
					else if (block->last->pc - block->first->pc < 0x80)
						size = "14";
					else if (block->last->pc - block->first->pc < 0x40)
						size = "12";
					else if (block->last->pc - block->first->pc < 0x20)
						size = "10";
					else
						size = "8";

					fprintf(file, "B%d [label=\"B%d\\n%s\"];\n", 
							block->id, block->id, labptr
							
							//,size
						);
				}
			}
			END_EACH_BLOCK(func, block);
			fprintf(file, "}\n");
		}
		END_EACH_FUNC(r, func);
		fprintf(file, "}\n");
	}
	END_EACH_RANGE(r);

	fprintf(file, "}\n");
}

//	Dump call graph
static void dump_call_graph(FILE *file)
{
	mem_range *r;
	Function *func;
	Block *block;
	BlockList *bl;
	CGNode *node;
	CGNodeList *nl;
	CGEdge *e;

	fprintf(file, "digraph program {\n");
	for (nl = callgraph->nodes; nl; nl = nl->next) {
		func = nl->node->function;
		fprintf(file, "subgraph %s {\n", func->name);
		for (e = nl->node->callees; e; e = e->next) {
			fprintf(file, "%s -> %s\n", func->name, e->node->function->name);
		}
		fprintf(file, "}\n");
	}

	fprintf(file, "}\n");
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
	Label *l = 0;
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
		   "tidecomp 9900 Disassembler v1.0\n"
		   "\n"
		   "Usage:   tidecomp [options] { -b <addr> -a <file> } { -r from:to -d from:to }\n"
		   "\n"
		   "tidecomp will 'decompile' 99/4A files or binaries\n"
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
		   "\t\t-s <addr> -- add new symbol table address\n"
		   "\n"),
			 ext
		);
}

int	main(int argc, char **argv)
{
	int		opt;
	int		failed = 0;
	char	*ptr;
	u16		base_addr = 0;
	u16		size = 0;
	FILE	*graph;

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
	symtabs = 0;

	init_ranges();
	init_labels();

	while ((opt = getopt(argc, argv, "?e:o:nb:a:r:d:hcvs:")) != -1)
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
		case 's':
		{
			u16 addr = strtol(optarg, &ptr, 0);
			struct symtab_list *sl = XMNEW(struct symtab_list);
			sl->addr = addr;
			sl->next = symtabs;
			symtabs = sl;
			break;
		}
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

	get_code();
	get_entries();
	get_labels();
//	dump_labels();
	get_blocks();
//	dump_all_blocks(stdout);
//	get_functions();
//	dump_functions(stdout);
	get_flow();
//	dump_flow(stdout);
	get_functions();

	graph = fopen("blockgraph.dotty", "w");
	dump_block_graph(graph);
	fclose(graph);

	get_callgraph();

	graph = fopen("callgraph.dotty", "w");
	dump_call_graph(graph);
	fclose(graph);

	dump_functions(stdout);

//	dump_ranges();
//	failed |= disassemble();

	return failed;
}

