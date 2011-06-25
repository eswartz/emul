/*
  compiler.c					-- just-in-time compiler

  (c) 1994-2003 Edward Swartz

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

#include <malloc.h>
#include <setjmp.h>

#include "v9t9_common.h"
#include "timer.h"
#include "9900.h"
#include "demo.h"
#include "command.h"
#include "memory.h"
#include "v9t9.h"
#include "dis9900.h"

#include "compiler.h"
#include "compiler_internals.h"

#define _L	 LOG_COMPILER | LOG_INFO

#ifndef CAN_COMPILE
#if __linux__
#define CAN_COMPILE 1
#else
#define CAN_COMPILE 0
#endif
#endif

///////////

int			compile_mode = 0;		// 1: compile 9900 code in real time

#if !CAN_COMPILE

void compiler_init(void)
{
}

void compiler_term(void)
{
}

extern struct CodeBlock *compiler_add_cache(u16 pc)
{
	return 0L;
}

extern int compiler_build(struct CodeBlock *cb)
{
	return 0;
}

int compiler_execute(struct CodeBlock *data, u16 pc, u32 *executed, u32 *instcycles)
{
	*executed = 0;
	*instcycles = 0;
	return 0;
}

extern void compiler_flush_for_wp(u16 addr)
{
}

#else

CompilerState compile_state;		// data shared with compiled code
OSPathSpec	compiledatadir;

/*	info about compiled code blocks */
typedef struct CodeBlock
{
	struct CodeBlock 	*next;
	MemoryEntry 		*ent;		/* memory we're handling */
	u32 				addr, size;	/* dimensions */
	char			   	*name;		/* filename */
	bool				compiled;	/* true if block is compiled successfully */
	bool				enabled;	/* true if block is loaded and in use */
	bool				ignore;		/* something bad happened; don't bother in the future */
	OSSpec				spec;		/* shared lib */
	OSLibrary			library;	/* open handle */
	CompiledCodeEntry 	entry;	   	/* entry point (!= 0L --> loaded) */
	OSThread			compile_thread;	/* current compiler thread */
}	CodeBlock;

static CodeBlock *codeblocks;

#define MAX_COMPILE_THREADS 2
static int compile_threads;

static void compiler_start(void);
static void compiler_stop(void);
static void compiler_trap_mem_write(const mrstruct *mr, u32 addr, s8 val);

#if POSIX_FS
OSFileType OSBinCodeType = { 0777 };
#endif

//////////////////////////////////////////////////

#if __INTEL__ || __i386__

void OS_ExecuteCode(u8 *addr, void *data)
{
	void (*fptr)(void *) = (void (*)(void *))addr;
	fptr(data);
}

#else


#endif

//////////////////////////////////////////////////

static
DECL_SYMBOL_ACTION(set_compile_mode)
{
	int num;
	command_arg_get_num(SYM_ARG_1st, &num);
	if (compile_mode != num)
	{
		if (num)
			compiler_start();
		else
			compiler_stop();
		compile_mode = num;
	}	
	return 1;
}

//	decide how much to compile.
//	For ROMs, we want to take the whole thing.
//	For RAM, we should restrict it to AREASIZE since parts are
//	likely to change often.

#define BLOCKSHIFT AREASHIFT
#if BLOCKSHIFT<AREASHIFT
#error // rely on having at least one mrstruct per block
#endif
#define BLOCKSIZE (1<<BLOCKSHIFT)
#define BLOCKCLIP(addr)	((addr) & (~0<<BLOCKSHIFT) & 0xffff)

static void pick_code_range(MemoryEntry *ent, u16 pc,
							u32 *addr, u32 *size, char *name)
{
	if (ent->filename)
	{
		if (ent->addr == AREACLIP(pc) && ent->realsize <= AREASIZE)
		{
			// take it all
			*addr = ent->addr;
			*size = ent->realsize;
			snprintf(name, OS_NAMESIZE, "code_%s.dll", ent->filename);
		}
		else
		{
			// take a part
			*addr = BLOCKCLIP(pc);
			*size = BLOCKSIZE;
			snprintf(name, OS_NAMESIZE, "code_%s_%04x_%04x.dll", 
					 ent->filename, *addr, *size);
		}
	}
	else
	{
		// take a chunk
		*addr = BLOCKCLIP(pc);
		*size = BLOCKSIZE;
		snprintf(name, OS_NAMESIZE, "code_%04x_%04x_%08x.dll", 
				 *addr, *size, ent->flags);
	}
	logger(_L|L_4, "choosing code block (%04x...%04x): %s\n", 
		   *addr, *addr + *size, name);
}

static CodeBlock *create_codeblock(MemoryEntry *ent, u16 pc)
{
	CodeBlock *cb;
	u32 addr; u32 size; 
	char name[OS_NAMESIZE];
	OSSpec spec;

	pick_code_range(ent, pc, &addr, &size, name);

	cb = xcalloc(sizeof(CodeBlock));
	cb->ent = ent;
	cb->addr = addr;
	cb->size = size;
	cb->name = xstrdup(name);

	return cb;
}

/////////////////////////////////////////////////
#if 0
#pragma mark -
#endif

typedef struct CInst
{
	Instruction ins;
	u16 stat_set;
	u16 stat_live;
	char size;
	bool label;
}	CInst;


/*
  9900 native code compiler.

  We operate by generating C source and compiling this into a shared
  library.  Oh, the insanity!

  The compiler_internals.h file describes the interface to the 
  native code.  The compiler_state struct is the parameter block to
  this code.  It provides accessors to the memory of the emulator.
  Note that PC, WP, and ST, are changed in this block and updated
  upon return.
*/

static OSHandle codetext, prefixtext, suffixtext;
static int codefailed;

static void printf_hv(OSHandle *hand, const char *format, va_list va)
{
	char sbuf[1024], *buf = sbuf;
	int max, ret;
	max = sizeof(sbuf);
	do 
	{
		ret = vsnprintf(buf, max, format, va);
		if (ret == -1) 
		{
			max <<= 1;
			if (buf == sbuf)
				buf = xmalloc(max);
			else
				buf = xrealloc(buf, max);
		}
		else if (ret >= max)
		{
			max = ret+1;
			buf = xmalloc(ret+1);
		}
		else
			break;
	}	while (1);

	codefailed |= OS_AppendHandle(hand, buf, ret) != OS_NOERR;
	if (buf != sbuf) xfree(buf);

	va_end(va);
}

static void printf_h(OSHandle *hand, const char *format, ...)
{
	va_list va;
	va_start(va, format);
	printf_hv(hand, format, va);
	va_end(va);
}

static void Cprintf(const char *format, ...)
{
	va_list va;
	va_start(va, format);
	printf_hv(&codetext, format, va);
	va_end(va);
}

static void Pprintf(const char *format, ...)
{
	va_list va;
	va_start(va, format);
	printf_hv(&prefixtext, format, va);
	va_end(va);
}

static void Sprintf(const char *format, ...)
{
	va_list va;
	va_start(va, format);
	printf_hv(&suffixtext, format, va);
	va_end(va);
}


static u16 flat_read_word(u16 addr)
{
	return MEMORY_READ_WORD(addr);
}

static void read_operand(Instruction *ins, Operand *op, const char *var)
{
	const char *sz = op->byteop ? "BYTE" : "WORD";
	switch (op->type)
	{
	case OP_REG:
		Cprintf("\t%s = REGREAD%s(%d);\n", var, sz, op->val); 
//		Cprintf("printf(\"r R%d=%%04X\\n\",%s);\n", op->val, var);
		if (ins->inst == Idiv && op->dest)
			Cprintf("\tlow = REGREAD%s(%d);\n", sz, op->val+1);
		break;
	case OP_IND:
//		Cprintf("printf(\"r R%d=%%04X\\n\",REGREADWORD(%d));\n", op->val, op->val);
		Cprintf("\t%s = READ%s(REGREADWORD(%d));\n", var, sz, op->val); 
		break;
	case OP_ADDR:
		if (op->val)
		{
//			Cprintf("printf(\"r R%d=%%04X\\n\",REGREADWORD(%d));\n", op->val, op->val);
			Cprintf("\t%s = READ%s(REGREADWORD(%d)+%d);\n", var, sz, op->val, op->immed);
		}
		else
			Cprintf("\t%s = READ%s(%d);\n", var, sz, op->immed);
		break;
	case OP_INC:
//		Cprintf("printf(\"r R%d=%%04X\\n\",REGREADWORD(%d));\n", op->val, op->val);
		Cprintf("\t%s = READ%s(REGREADWORD(%d));\n", var, sz, op->val); 
		Cprintf("\tREGINC(%d,%d);\n", op->val, op->byteop ? 1 : 2); 
		break;

	case OP_IMMED:
		Cprintf("\t%s = %d;\n", var, op->immed);
		break;

	case OP_CNT:
		Cprintf("\t%s = %d;\n", var, op->val);
		break;

	case OP_JUMP:
		Cprintf("\t%s = %d;\n", var, (s16)op->val);
		break;

	case OP_OFFS:
		Cprintf("\t%s = %d;\n", var, op->val);
		break;

	case OP_STATUS:
	case OP_INST:
		// ignore; these are real-time
		break;

	default:
		Cprintf("#error unknown read operand %d\n", op->type);
		break;
	}
}

static void read_address(Instruction *ins, Operand *op, const char *var)
{
	switch (op->type)
	{
	case OP_REG:
		Cprintf("\t%s = REGADDR(%d);\n", var, op->val); 
		break;
	case OP_IND:
		Cprintf("\t%s = REGREADWORD(%d);\n", var, op->val); 
		break;
	case OP_ADDR:
		if (op->val)
			Cprintf("\t%s = REGREADWORD(%d)+%d;\n", var, op->val, op->immed);
		else
			Cprintf("\t%s = %d;\n", var, op->immed);
		break;
	case OP_INC:
		Cprintf("\t%s = REGREADWORD(%d);\n", var, op->val); 
		Cprintf("\tREGINC(%d,%d);\n", op->val, op->byteop ? 1 : 2); 
		break;
	default:
		Cprintf("#error unknown read address operand %d\n", op->type);
		break;
	}
}

static void write_operand(Instruction *ins, Operand *op, const char *var)
{
	const char *sz = op->byteop ? "BYTE" : "WORD";
	switch (op->type)
	{
	case OP_REG:
		Cprintf("\tREGWRITE%s(%d,%s);\n", sz, op->val, var); 
//		Cprintf("printf(\"w R%d=%%04X\\n\",%s);\n", op->val, var);
		if (ins->inst == Impy || ins->inst == Idiv)
			Cprintf("\tREGWRITE%s(%d,low);\n", sz, op->val+1); 
		break;
	case OP_IND:
		Cprintf("\tWRITE%s(REGREADWORD(%d),%s);\n", sz, op->val, var); 
		break;
	case OP_ADDR:
		if (op->val)
			Cprintf("\tWRITE%s(REGREADWORD(%d)+%d,%s);\n", sz, op->val, op->immed, var);
		else
			Cprintf("\tWRITE%s(%d,%s);\n", sz, op->immed, var);
		break;
	case OP_INC:
		Cprintf("\tWRITE%s(REGREADWORD(%d),%s);\n", sz, op->val, var); 
		Cprintf("\tREGINC(%d,%d);\n", op->val, op->byteop ? 1 : 2); 
		break;

	case OP_STATUS:
	case OP_INST:
		// ignore; these are real-time
		break;

	case OP_IMMED:
	case OP_CNT:
	case OP_JUMP:
	case OP_OFFS:
	default:
		Cprintf("#error unknown write operand %d\n", op->type);
		break;
	}
}

static int handle_op(Instruction *ins)
{
	switch (ins->inst)
	{
	case Idata:
	case Ilimi:
	case Iidle:
	case Irset:
	case Ickon:
	case Ickof:
	case Ilrex:
	case Ix:
	case Isbo:
	case Isbz:
	case Itb:
	case Ildcr:
	case Istcr:
		return 0;
	}
	return 1;
}

#define INST_MAINTENANCE \
				"\tstate->cycles += %d;\n"  \
				"\tstate->instrs++;\n"

static void do_op(Instruction *ins, int do_status, u16 next_pc, int cycles)
{
	switch (ins->inst)
	{
	case Ili:
		Cprintf("\ta = b;\n"); 
		if (do_status) Cprintf("\tsetst_lae(a);\n");
		break;
	case Iai:
		// note: add set status first
		if (do_status) Cprintf("\tsetst_add_laeco(a, b);\n");
		Cprintf("\ta += b;\n"); 
		break;
	case Iandi:
		Cprintf("\ta &= b;\n"); 
		if (do_status) Cprintf("\tsetst_lae(a);\n");
		break;
	case Iori:
		Cprintf("\ta |= b;\n"); 
		if (do_status) Cprintf("\tsetst_lae(a);\n");
		break;
	case Ici:
		if (do_status) Cprintf("\tsetst_cmp(a,b);\n");
		break;
	case Istwp:
		Cprintf("\ta = state->wp;\n"); 
		break;
	case Istst:
		Cprintf("\ta = read_stat();\n"); 
		break;
	case Ilwpi:
		Cprintf("\tstate->wp = a & 0xfffe;\n"); 
		break;
	case Irtwp:
		Cprintf("\twrite_stat(REGREADWORD(15));\n"
				"\tstate->pc = REGREADWORD(14);\n"
				"\tstate->wp = REGREADWORD(13);\n"
				INST_MAINTENANCE
				"\tgoto retry;\n",
				cycles); 
		break;

	case Iblwp:
		Cprintf("\tREGWRITEWORD(15, read_stat());\n"
				"\tREGWRITEWORD(14, 0x%04x);\n"
				"\tREGWRITEWORD(13, state->wp);\n"
				"\tstate->wp = READWORD(a);\n"
				"\tstate->pc = READWORD(a+2);\n"
				INST_MAINTENANCE
				"\tgoto retry;\n", 	
				next_pc, cycles); 
		break;
	case Ib:
		Cprintf("\tstate->pc = a;\n"
				INST_MAINTENANCE
				"\tgoto retry;\n",
				cycles); 
		break;
	case Iclr:
		Cprintf("\ta = 0;\n"); 
		break;
	case Ineg:
		Cprintf("\ta = -a;\n");
		if (do_status)
		{
			Cprintf("\tsetst_o(a==0x8000);\n");
			Cprintf("\tsetst_lae(a);\n");
		}
		break;
	case Iinv:
		Cprintf("\ta = ~a;\n"); 
		if (do_status) Cprintf("\tsetst_lae(a);\n");
		break;
	case Iinc:
		// note: add set status first
		if (do_status) Cprintf("\tsetst_add_laeco(a, 1);\n");
		Cprintf("\ta += 1;\n"); 
		break;
	case Iinct:
		if (do_status) Cprintf("\tsetst_add_laeco(a, 2);\n");
		Cprintf("\ta += 2;\n"); 
		break;
	case Idec:
		if (do_status) Cprintf("\tsetst_add_laeco(a, 0xffff);\n");
		Cprintf("\ta += 0xffff;\n"); 
		break;
	case Idect:
		if (do_status) Cprintf("\tsetst_add_laeco(a, 0xfffe);\n");
		Cprintf("\ta += 0xfffe;\n");
		break;
	case Ibl:
		Cprintf("\tREGWRITEWORD(11, 0x%04x);\n"
				"\tstate->pc = a;\n"
				INST_MAINTENANCE
				"\tgoto retry;\n", 
				next_pc, cycles); 
		break;
	case Iswpb:
		Cprintf("\ta = (a>>8)|(a<<8);\n"); 
		break;
	case Iseto:
		Cprintf("\ta = 0xffff;\n"); 
		break;
	case Iabs:
		if (do_status) 
			Cprintf("\tsetst_lae(a);\n"
					"\tif (a >= 0x8000) { setst_o(a==0x8000); a = -a; }\n");
		else
			Cprintf("\tif (a >= 0x8000) a = -a;\n");
		break;
	case Isra:
		if (do_status) Cprintf("\tsetst_shift_right_c(a,b);\n");
		Cprintf("\ta = ((s16)a) >> b;\n");
		if (do_status) Cprintf("\tsetst_lae(a);\n");
		break;
	case Isrl:
		if (do_status) Cprintf("\tsetst_shift_right_c(a,b);\n");
		Cprintf("\ta = ((u16)a) >> b;\n");
		if (do_status) Cprintf("\tsetst_lae(a);\n");
		break;
	case Isla:
		if (do_status) Cprintf("\tsetst_sla_co(a,b);\n");
		Cprintf("\ta <<= b;\n");
		if (do_status) Cprintf("\tsetst_lae(a);\n");
		break;
	case Isrc:
		if (do_status) Cprintf("\tsetst_shift_right_c(a,b);\n");
		Cprintf("\ta = (a >> b) | (a << (16-b));\n");
		if (do_status) Cprintf("\tsetst_lae(a);\n");
		break;
	case Ijmp:
		Cprintf("\tstate->pc = 0x%04x + a;\n"
				INST_MAINTENANCE
				"\tgoto retry;\n", 
				ins->pc, cycles+2);
		break;

#define CprintfJcc(type,not)	 \
		Cprintf("\tif (" # not "test_%s()) {\n"	\
				"\t\tstate->pc = 0x%04x + a;\n"		\
				INST_MAINTENANCE					\
				"\t\tgoto retry;\n"					\
				"\t}\n", #type, ins->pc, cycles+2)
	case Ijlt:
		CprintfJcc(lt,);
		break;
	case Ijle:
		CprintfJcc(le,);
		break;
	case Ijeq:
		CprintfJcc(eq,);
		break;
	case Ijhe:
		CprintfJcc(he,);
		break;
	case Ijgt:
		CprintfJcc(gt,);
		break;
	case Ijne:
		CprintfJcc(ne,);
		break;
	case Ijnc:
		CprintfJcc(c,!);
		break;
	case Ijoc:
		CprintfJcc(c,);
		break;
	case Ijno:
		CprintfJcc(o,!);
		break;
	case Ijl:
		CprintfJcc(l,);
		break;
	case Ijh:
		CprintfJcc(h,);
		break;
	case Ijop:
		CprintfJcc(p,);
		break;
	case Icoc:
		if (do_status) Cprintf("\tsetst_e(a&b, a);\n");
		break;
	case Iczc:
		if (do_status) Cprintf("\tsetst_e(a&~b, a);\n");
		break;
	case Ixor:
		Cprintf("\tb ^= a;\n");
		if (do_status) Cprintf("\tsetst_lae(b);\n");
		break;
	case Ixop:
		Cprintf("\tREGWRITEWORD(15, read_stat());\n"
				"\tREGWRITEWORD(14, 0x%04x);\n"
				"\tREGWRITEWORD(13, state->wp);\n"
				"\tREGWRITEWORD(11, a);\n"
				"\tsetst_x();\n"
				"\tstate->wp = READWORD(0x40+b*4);\n"
				"\tstate->pc = READWORD(0x42+b*4);\n"
				INST_MAINTENANCE
				"\tgoto retry;\n", 
				next_pc, cycles); 
		break;
	case Impy:
		Cprintf("\tlow = prod = a*b;\n"
				"\tb = prod>>16;\n");
		break;
	case Idiv:
		if (do_status) Cprintf("\tsetst_o(a <= b);\n");
		Cprintf("\tif (a > b) {\n"
				"\t\tprod = (b << 16) | low;\n"
				"\t\tb = prod / a;\n"
				"\t\tlow = prod %% a;\n"
				"\t}\n");
		break;
	case Iszc:
		Cprintf("\tb &= ~a;\n");
		if (do_status) Cprintf("\tsetst_lae(b);\n");
		break;
	case Iszcb:
		Cprintf("\tb &= ~a;\n");
		if (do_status) Cprintf("\tsetst_byte_laep(b);\n");
		break;
	case Is:
		if (do_status) Cprintf("\tsetst_sub_laeco(b,a);\n");
		Cprintf("\tb -= a;\n");
		break;
	case Isb:
		if (do_status) Cprintf("\tsetst_sub_byte_laecop(b,a);\n");
		Cprintf("\tb -= a;\n");
		break;
	case Ic:
		if (do_status) Cprintf("\tsetst_cmp(a,b);\n");
		break;
	case Icb:
		if (do_status) Cprintf("\tsetst_cmp((s8)a,(s8)b);\n");
		break;
		
	case Ia:
		// note: add set status first
		if (do_status) Cprintf("\tsetst_add_laeco(b,a);\n");
		Cprintf("\tb += a;\n");
		break;
	case Iab:
		if (do_status) Cprintf("\tsetst_add_byte_laecop(b,a);\n");
		Cprintf("\tb += a;\n");
		break;
	case Imov:
		Cprintf("\tb = a;\n");
		if (do_status) Cprintf("\tsetst_lae(b);\n");
		break;
	case Imovb:
		Cprintf("\tb = a;\n");
		if (do_status) Cprintf("\tsetst_byte_laep(b);\n");
		break;
	case Isoc:
		Cprintf("\tb |= a;\n");
		if (do_status) Cprintf("\tsetst_lae(b);\n");
		break;
	case Isocb:
		Cprintf("\tb |= a;\n");
		if (do_status) Cprintf("\tsetst_byte_laep(b);\n");
		break;
	default:
		logger(_L|LOG_FATAL, _("unhandled instruction %s\n"), ins->name);
		break;
	}
}

static void get_status_flags(CInst *ci)
{
	switch (ci->ins.stset)
	{
	case st_NONE:			
		ci->stat_set = 0; break;
	case st_ALL:			
		ci->stat_set = 0xffff;	break;
	case st_INT:			
		ci->stat_set = 0xf; break;
	case st_XOP:			
		ci->stat_set = ST_X; break;
	case st_LAE:			
		ci->stat_set = ST_L|ST_A|ST_E; break;
	case st_BYTE_LAEP:
		ci->stat_set = ST_L|ST_A|ST_E|ST_P; break;
	case st_LAEO:			
		ci->stat_set = ST_L|ST_A|ST_E|ST_O; break;
	case st_O:				
		ci->stat_set = ST_O; break;
	case st_SHIFT_LAEC:		
		ci->stat_set = ST_L|ST_A|ST_E|ST_C; break;
	case st_SHIFT_LAECO:	
	case st_SUB_LAECO:
	case st_ADD_LAECO:
		ci->stat_set = ST_L|ST_A|ST_E|ST_C|ST_O; break;
	case st_SUB_BYTE_LAECOP:
	case st_ADD_BYTE_LAECOP:
		ci->stat_set = ST_L|ST_A|ST_E|ST_C|ST_O|ST_P; break;
	case st_CMP:
		ci->stat_set = ST_L|ST_A|ST_E; break;
	case st_BYTE_CMP:		
		ci->stat_set = ST_L|ST_A|ST_E|ST_P; break;
	case st_E:				
		ci->stat_set = ST_E; break;
	default:
		my_assert(!"invalid status flags"); break;
	}
}

//	Normally we trap self-modifying code at branches.
//	Either of these defines will add extra checks
#define ULTRA_PARANOID	0	// trap self-modifying code after operand reads
							// (i.e. *R3+ when PC~~WP+6)
#define PARANOID		0	// trap self-modifying code after operand writes

static int compile(u8 *base, u32 pc, u32 size)
{
	u32 start = pc;
	u32 end = pc + size;
	OSError err;
	CInst *code = xcalloc(sizeof(CInst) * size/2);
	u16 stat_live;

	Pprintf("// AUTOGENERATED CODE from v9t9 (CompileMode on)\n"	
			"\n"
			"#include <stdlib.h>\n"
			"#include \"compiler_internals.h\"\n"
			"#include \"compiler_macros.h\"\n"
			"\n"
			"int native(CompilerState *state)\n"
			"{\n"
			"u16 a,b,low; //temps\n"
			"u32 sum,prod;\n"
			"s16 lastcmp, lastval;\n"
			);

	// execute code if state, else return info
	Cprintf("if (state) {\n"
//			"printf(\"pc=%%04x, wp=%%04x, st=%%04x\\n\", state->pc, state->wp, state->stat);\n"
			"write_stat(state->stat);\n"
			"retry:\n"
#if !PARANOID
			// check at branches
			"if (!state->executing) goto unhandled;\n"
#endif
			// be more careful when interrupts may be enabled
			"if (state->stat & 0xf && state->instrs >= 65536) goto unhandled;\n"

			"switch (state->pc) {\n");

	// disassemble
	stat_live = 0xffff;
	while (pc < end)
	{
		CInst *ci = &code[(pc-start)/2];
		Instruction *ins = &ci->ins;

		pc = dis9900_decode(MEMORY_READ_WORD(pc), pc, 0, 0, flat_read_word, 
							&ci->ins);
		ci->size = pc - ci->ins.pc;
		get_status_flags(ci);

		pc = ins->pc + 2;
	}

	// emit
	pc = start;
	while (pc < end)
	{
		CInst *ci = &code[(pc-start)/2];
		Instruction *ins = &ci->ins;
		int do_status = 1;

		pc += ci->size;

		// emit label and case entry
		Cprintf("L%04x:\n"
				"case 0x%04x:\n", ins->pc, ins->pc); 

		// dump comment
		if (log_level(LOG_COMPILER) > 2)
		{
			char buf[64],buf2[64];
			char *op1ptr, *op2ptr;

			Cprintf("\n\t// PC=%04X (%04X) ==> %s", ins->pc, ins->opcode, ins->name);
			op1ptr = dis9900_operand_print(&ins->op1, buf, sizeof(buf));
			op2ptr = dis9900_operand_print(&ins->op2, buf2, sizeof(buf2));
			if (op1ptr && op2ptr) {
				Cprintf(" %s,%s", op1ptr, op2ptr);
			} else if (op1ptr) {
				Cprintf(" %s", op1ptr);
			} else if (op2ptr) {
				op1ptr = op2ptr;
				op2ptr = 0L;
				Cprintf(" %s", op2ptr);
			}
			Cprintf("\n");

			if (log_level(LOG_COMPILER) > 3)
			{
				Cprintf("\tprintf(\"%04X=%04X  %s %s%s%s\\n\");\n",
						ins->pc, ins->opcode, ins->name, op1ptr?op1ptr:"",
						op2ptr?",":"", op2ptr?op2ptr:"");
			}
		}

		// don't waste time with ones we can't deal with
		if (!handle_op(ins))
		{
			Cprintf("\tgoto unhandled;\n");
			goto skip;
		}

#if PARANOID
		Cprintf("\tif (!state->executing) goto unhandled;\n");
#endif

		//Cprintf("\tprintf(\"reading operands %04X\\n\");\n", ins->pc);

		// emit operand reads
		if (ins->op1.type != OP_NONE)
		{
			if (ins->op1.dest != OP_DEST_KILLED)
				if (ins->op1.addr)
					read_address(ins, &ins->op1, "a");
				else
					read_operand(ins, &ins->op1, "a");
			if (ins->op2.type != OP_NONE)
				if (ins->op2.dest != OP_DEST_KILLED)
					if (ins->op2.addr)
						read_address(ins, &ins->op2, "b");
					else
						read_operand(ins, &ins->op2, "b");
		}

#if ULTRA_PARANOID
		Cprintf("\tif (!state->executing) goto unhandled;\n");
#endif
		//Cprintf("\tprintf(\"running %04X\\n\");\n", ins->pc);

		// emit code and status fixup
		do_op(ins, do_status, pc, ins->cycles);

		// emit operand writes
		if (ins->inst != Idata && ins->op1.type != OP_NONE)
		{
			//Cprintf("\tprintf(\"writing results %04X\\n\");\n", ins->pc);

			if (ins->op1.dest)
			{
				write_operand(ins, &ins->op1, "a");
#if ULTRA_PARANOID
				Cprintf("\tif (!state->executing) goto unhandled;\n");
#endif
			}
			if (ins->op2.type != OP_NONE)
				if (ins->op2.dest)
				{
					write_operand(ins, &ins->op2, "b");
#if ULTRA_PARANOID
					Cprintf("\tif (!state->executing) goto unhandled;\n");
#endif
				}
		}

		// standard maintenance (when instruction finishes without branch)
		Cprintf(INST_MAINTENANCE
				"\tstate->pc = 0x%04x;\n", 
				ins->cycles, pc);

	skip:
		if (pc != ins->pc + 2)
		{
			// this instruction consumes possible 
			// other instructions (we can't tell)
			// go have a policy of jumping explicitly
			// to the next instruction and decoding
			// every single word.

			if (pc >= start && pc < start + size)
				Cprintf("\tgoto L%04x;\n", ci->ins.pc+ci->size);
			else
				Cprintf("\tgoto leave;\n");
			pc = ins->pc + 2;
		}

		Cprintf("\n");
	}

	// last instruction falls through
	Cprintf("\tgoto leave;\n");

	// default case (probably jumped or branched away)
	Cprintf("default: goto leave;\n");

	// end of switch
	Cprintf("}\n"
		"goto retry;\n");

	// handle errors/exceptional conditions
	Cprintf("unhandled:\n"
			"\tstate->stat = read_stat();\n"
			"\treturn 1; // emulate this one\n"
			"\n");

	// handle normal exit
	Cprintf("leave:\n"
			"\tstate->stat = read_stat();\n"
			"\treturn 0; // continue\n"
			"\n");

	// provide info to caller in query mode
	Cprintf("} else {\n"
			"\tstatic CompiledCode code = {\n"
			"\t\t%d, %d\n"
			"\t\t};\n"
			"\t\treturn (int)&code;\n"
			"\t}\n"
			"}\n", 
			start, size);

	xfree(code);
	return 0;
}

static int compiler_compile(CodeBlock *cb)
{
	OSError err;
	OSRef ref;
	void *ptr;
	OSSize sz;
	OSSpec tempspec;
	OSSpec srcspec;
	OSSpec makespec;
	char *argv[16]; int argc;
	char *outname, *errname;
	char buf[256];
	int exitcode;
	int i;

	if (data_find_binary(OS_PathSpecToString1(&compiledatadir),
						 cb->name, &cb->spec))
	{
		// validate!!!
		logger(_L|LOG_INFO|L_1, "using prebuilt binary\n");
		cb->compiled = true;
		return 1;
	}

	if (!data_create_file(OS_PathSpecToString1(&compiledatadir),
						  cb->name, &cb->spec, &OSBinCodeType))
	{
		logger(_L|LOG_ERROR|LOG_USER, _("could not create %s in %s"), 
			   cb->name, OS_PathSpecToString1(&compiledatadir));
		return 0;	
	}

	if ((err = OS_NewHandle(0, &codetext)) != OS_NOERR) 
	{
		logger(_L|LOG_ERROR|LOG_USER, _("out of memory"));
		return 0;
	}
	if ((err = OS_NewHandle(0, &prefixtext)) != OS_NOERR) 
	{
		logger(_L|LOG_ERROR|LOG_USER, _("out of memory"));
		OS_FreeHandle(&codetext);
		return 0;
	}
	if ((err = OS_NewHandle(0, &suffixtext)) != OS_NOERR) 
	{
		logger(_L|LOG_ERROR|LOG_USER, _("out of memory"));
		OS_FreeHandle(&codetext);
		OS_FreeHandle(&prefixtext);
		return 0;
	}

	codefailed = 0;

	// generate code
	codefailed |= compile(cb->ent->memact.arearead - cb->ent->addr,
						  cb->addr, cb->size);

	if (codefailed) {
		logger(_L|LOG_USER|LOG_ERROR, _("compiler failed to build segment\n"));

	}

	// dump code to disk
	if (!codefailed)
	{
		OSSize writ;
		ref = 0;
		srcspec = cb->spec;
		if ((err = OS_NameSpecChangeExtension(&srcspec.name, ".c", false)) != OS_NOERR
			|| (err = OS_Create(&srcspec, &OS_TEXTTYPE)) != OS_NOERR
			|| (err = OS_Open(&srcspec, OSWrite, &ref)) != OS_NOERR)
		{
			logger(_L|LOG_USER|LOG_ERROR, _("compiler failed to open file '%s' (%s)\n"),
				   OS_SpecToString1(&srcspec), OS_GetErrText(err));
			codefailed = 1;
			goto endwrite;
		}

		// write prefix
		if ((ptr = OS_LockHandle(&prefixtext)) == 0
			|| (err = OS_GetHandleSize(&prefixtext, &sz)) != OS_NOERR
			|| (writ=sz, err = OS_Write(ref, ptr, &writ)) != OS_NOERR
			|| writ != sz)
		{
			logger(_L|LOG_USER|LOG_ERROR, _("compiler failed to write text(1) to file '%s' (%s)\n"),
				   OS_SpecToString1(&srcspec), OS_GetErrText(err));
			codefailed = 1;
			goto endwrite;

		}

		if ((ptr = OS_LockHandle(&codetext)) == 0
			|| (err = OS_GetHandleSize(&codetext, &sz)) != OS_NOERR
			|| (writ=sz, err = OS_Write(ref, ptr, &writ)) != OS_NOERR
			|| writ != sz)
		{
			logger(_L|LOG_USER|LOG_ERROR, _("compiler failed to write text(2) to file '%s' (%s)\n"),
				   OS_SpecToString1(&srcspec), OS_GetErrText(err));
			codefailed = 1;
			goto endwrite;

		}

		if ((ptr = OS_LockHandle(&suffixtext)) == 0
			|| (err = OS_GetHandleSize(&suffixtext, &sz)) != OS_NOERR
			|| (writ=sz, err = OS_Write(ref, ptr, &writ)) != OS_NOERR
			|| writ != sz)
		{
			logger(_L|LOG_USER|LOG_ERROR, _("compiler failed to write text(3) to file '%s' (%s)\n"),
				   OS_SpecToString1(&srcspec), OS_GetErrText(err));
			codefailed = 1;
			goto endwrite;
		}
	}
endwrite:
	OS_FreeHandle(&prefixtext);
	OS_FreeHandle(&codetext);
	OS_FreeHandle(&suffixtext);
	if (ref && (err = OS_Close(ref)) != OS_NOERR)
		codefailed = 1;

	if (codefailed)
	{
		logger(_L|LOG_ERROR|LOG_USER, _("did not write source text"));
		OS_Delete(&srcspec);
		return 0;
	}

	// try to compile the code
	argc = 0;
	if ((err = OS_FindProgram("make", &makespec)) != OS_NOERR
		|| (err = OS_Status(&makespec)) != OS_NOERR)
	{
		logger(_L|LOG_USER|LOG_ERROR, "compiler cannot find 'make'\n");
		return 0;
	}

	argv[argc++] = xstrdup(OS_SpecToString1(&makespec));
	argv[argc++] = xstrdup("-C");

	snprintf(buf, sizeof(buf), "%s/../compiler", OS_PathSpecToString1(&v9t9_progspec.path));
	argv[argc++] = xstrdup(buf);
	
	snprintf(buf, sizeof(buf), "SRC=\"%s\"", OS_SpecToString1(&srcspec));
	argv[argc++] = xstrdup(buf);

	snprintf(buf, sizeof(buf), "DLL=\"%s\"", OS_SpecToString1(&cb->spec));
	argv[argc++] = xstrdup(buf);

	argv[argc] = 0L;

	snprintf(buf, sizeof(buf), "%s.out", cb->name);
	OS_MakeSpecWithPath(&compiledatadir, buf, false, &tempspec);
	outname = xstrdup(OS_SpecToString1(&tempspec));

	snprintf(buf, sizeof(buf), "%s.err", cb->name);
	OS_MakeSpecWithPath(&compiledatadir, buf, false, &tempspec);
	errname = xstrdup(OS_SpecToString1(&tempspec));

	err = OS_Execute(&makespec, argv, 0L, outname, errname, &exitcode);
	if (err != OS_NOERR)
	{
		int len;
		char *cmdline;
		for (len = 0, i = 0; i < argc; i++)
			len += strlen(argv[i]+1);
		cmdline = xmalloc((len+1)*sizeof(char));
		*cmdline = 0;
		for (len = 0, i = 0; i < argc; i++)
		{
			strcat(cmdline, argv[i]);
			strcat(cmdline, " ");
			xfree(argv[i]);
		}
		
		logger(_L|LOG_ERROR|LOG_USER, _("could not execute 'make' (%s)\n"),
			   cmdline);

		xfree(cmdline);
		xfree(outname);
		xfree(errname);
		return 0;
	}

	for (i = 0; i < argc; i++)
		xfree(argv[i]);

	if (exitcode != 0)
	{
		logger(_L|LOG_ERROR|LOG_USER, _("error compiling code (see %s)\n"), errname);
		xfree(outname);
		xfree(errname);

		return 0;
	}

	xfree(outname);
	xfree(errname);

	if (OS_Status(&cb->spec) != OS_NOERR)
	{
		logger(_L|LOG_ERROR|LOG_USER, _("could not locate '%s'\n"),
			   OS_SpecToString1(&cb->spec));
		return 0;
	}

	cb->compiled = true;
	return 1;
}

/////////////////////////////////////////////////
#if 0
#pragma mark -
#endif

/*	Fast lookup for PC --> compiled code block  */

#define COMPHASH	123

typedef struct CBHash
{
	struct CBHash *next;
	CodeBlock 		*cb;
}	CBHash;

CBHash	*cbhash[COMPHASH];

static inline u32 get_comphash(u16 addr)
{
	addr = BLOCKCLIP(addr);
	return ((addr>>1) ^ (addr >> BLOCKSHIFT)) % COMPHASH;
}

static CodeBlock *lookup_codeblock(u16 addr)
{
	CBHash *h;
	u32 hc;
//	addr = BLOCKCLIP(addr);
	hc = get_comphash(addr);
	for (h = cbhash[hc]; h; h=h->next)
		if (h->cb->addr <= addr && h->cb->addr+h->cb->size > addr)
		{
			MemoryEntry *ent = h->cb->ent;
			if (!(ent->flags & MEMENT_BANKING)
				|| (memory_module_bank == 0 && (ent->flags & MEMENT_BANK_1))
				|| (memory_module_bank == 1 && (ent->flags & MEMENT_BANK_2)))
			{
				return h->cb;
			}
		}
	return 0L;
}

static void register_codeblock(CodeBlock *cb)
{
	CBHash *h;
	u32 hc;
	u32 a;
	u16 addr;
	for (a = cb->addr; a < cb->addr+cb->size; a+=AREASIZE)
	{
//		addr = BLOCKCLIP(a);
		addr = a;
		hc = get_comphash(addr);

		h = xmalloc(sizeof(CBHash));
		h->cb = cb; 
		h->next = cbhash[hc];
		cbhash[hc] = h;
	}
}

static void unregister_codeblock(CodeBlock *cb)
{
	CBHash *h, *prev=0L, *next;
	u32 hc;
	u32 a;
	for (a = cb->addr; a < cb->addr+cb->size; a+=AREASIZE)
	{
//		u16 addr = BLOCKCLIP(a);
		u16 addr = a;
		hc = get_comphash(addr);
		for (h = cbhash[hc]; h; h = next)
		{
//			if (h->cb->addr == addr)
			if (h->cb->addr <= addr && h->cb->addr+h->cb->size > addr)
			{
				if (prev)
					prev->next = next = h->next;
				else
					cbhash[hc] = next = h->next;
				xfree(h);
			}
			else
			{
				prev = h;
				next = h->next;
			}
		}
	}
}

static void unregister_codeblocks_for_entry(const MemoryEntry *ent)
{
	int i;
	for (i = 0; i < COMPHASH; i++)
	{
		CBHash *hash, *prev = 0L, *next;
		for (hash = cbhash[i]; hash; hash = next)
		{
			if (hash->cb->ent == ent)
			{
				if (prev)
					prev->next = next = hash->next;	
				else
					cbhash[i] = next = hash->next;
				xfree(hash);
			}
			else
			{
				prev = hash;
				next = hash->next;
			}
		}
	}
}

static void init_comphash(void)
{
	memset(cbhash, 0, sizeof(cbhash));
}

static void free_comphash(void)
{
	int i;
	for (i = 0; i < COMPHASH; i++)
	{
		CBHash *hash = cbhash[i];
		CBHash *next;
		while (hash)
		{
			next = hash->next;
			xfree(hash);
			hash = next;
		}
		cbhash[i] = 0L;
	}
}

static int load_codeblock(CodeBlock *cb)
{
	OSError err;
	mrstruct newact;

	if (cb->entry) return 1;

	if ((err = OS_OpenLibrary(&cb->spec, &cb->library)) != OS_NOERR)
	{
		logger(_L|LOG_ERROR|LOG_USER, _("Could not load code block '%s' (%s)\n"),
			   OS_SpecToString1(&cb->spec), OS_GetErrText(err));
		return 0;
	}

	if ((err = OS_GetLibrarySymbol(cb->library, "native", (void*)&cb->entry)) != OS_NOERR)
	{
		logger(_L|LOG_ERROR|LOG_USER, _("Could not resolve symbol 'native' in '%s' (%s)\n"),
			   OS_SpecToString1(&cb->spec), OS_GetErrText(err));
		OS_CloseLibrary(cb->library);
		return 0;
	}

	return 1;
}

static void enable_codeblock(CodeBlock *cb)
{
	if (!cb->enabled)
	{
		u32 addr;

		/* change behavior so we can track writes to area */
		/*
		  newact = cb->ent->memact;
		  newact.areawrite = 0L;
		  newact.write_byte = compiler_trap_mem_write;
		  newact.write_word = 0L;
		  set_area_handler(md_cpu, cb->addr, cb->size, &newact);
		*/
/*
		for (addr = cb->addr; addr < cb->addr + cb->size; addr += AREASIZE)
		{
			struct mrstruct *area = THE_AREA(md_cpu, addr);
			area->areawrite = 0L;
			area->write_byte = compiler_trap_mem_write;
			area->write_word = 0L;
		}
*/

		cb->enabled = 1;
	}
}

static void disable_codeblock(CodeBlock *cb)
{
	if (cb->enabled)
	{
		u32 addr;
		/* restore memory behavior */
		//set_area_handler(md_cpu, cb->addr, cb->size, &cb->ent->memact);
		/*
		for (addr = cb->addr; addr < cb->addr + cb->size; addr += AREASIZE)
		{
			struct mrstruct *area = THE_AREA(md_cpu, addr);
			if (cb->ent->memact.areawrite)
				area->areawrite = cb->ent->memact.areawrite + (addr - cb->ent->addr);
			else
				area->areawrite = 0L;
			area->write_byte = cb->ent->memact.write_byte;
			area->write_word = cb->ent->memact.write_word;
		}
		*/
		cb->enabled = 0;
	}
}

static void unload_codeblock(CodeBlock *cb)
{
	CodeBlock *ptr, *prev = 0L;

	disable_codeblock(cb);
	if (cb->entry)
	{
		OS_CloseLibrary(cb->library);
		cb->entry = 0L;
	}

/*
	for (ptr = codeblocks; ptr; ptr = ptr->next)
	{
		if (ptr == cb)
		{
			if (prev)
				prev->next = ptr->next;
			else
				codeblocks = ptr->next;
			xfree(cb->name);
			xfree(cb);
			return;
		}
		prev = ptr;
	}
*/
}

static void flush_comphash(void)
{
	int i;
	for (i = 0; i < COMPHASH; i++)
	{
		CBHash *hash;
		for (hash = cbhash[i]; hash; hash = hash->next)
		{
			//hash->cb->enabled = 0;
			disable_codeblock(hash->cb);
		}
	}
}

static void dirty_codeblock(CodeBlock *cb)
{
	disable_codeblock(cb);
	cb->compiled = false;
}

static void disable_codeblocks_for_entry(const MemoryEntry *ent)
{
	int i;
	for (i = 0; i < COMPHASH; i++)
	{
		CBHash *hash;
		for (hash = cbhash[i]; hash; hash = hash->next)
		{
			if (hash->cb->ent == ent)
			{
				disable_codeblock(hash->cb);
			}
		}
	}
}


//	run executed code at PC=addr
static int compiler_execute_code(CodeBlock *cb)
{
	int emul;

	my_assert(cb && cb->entry);

//	printf("entry=%p (%d) >%04x\n", cb->entry, *(u8 *)cb->entry, compile_state.pc);

	// execute
	compile_state.executing = 1;
	emul = cb->entry(&compile_state);
	compile_state.executing = 0;

//	printf("exit(%d) >%04x\n", emul, compile_state.pc);

	return emul;
}

//	emulator wants to know if code is compiled
extern CodeBlock *compiler_search_cache(u16 pc)
{
	return lookup_codeblock(pc);
}

//	emulator wants to compile code
extern CodeBlock *compiler_add_cache(u16 pc)
{
	CodeBlock *cb = lookup_codeblock(pc);

	if (!cb)
	{
		char name[OS_NAMESIZE];
		MemoryEntry *ent;

		/* find memory block */
		ent = memory_lookup_entry(pc, md_cpu, 1);
		if (!ent) 
			return 0;	/* shouldn't happen! */

		/* don't compile weird memory */
		if (!ent->memact.arearead)
//			|| (ent->memact.write_byte && !(ent->flags & MEMENT_BANKING)))
			return 0;

		/* for now */
		if (ent->flags & MEMENT_RAM) return 0;

//		logger(_L|LOG_INFO|L_1, "registering code block at >%04x\n", pc);
		/* get a block to compile */
		if (!(cb = create_codeblock(ent, pc)))
			return 0;
		
		register_codeblock(cb);
	}

	return cb;
}

extern int compiler_built(CodeBlock *cb)
{
	return cb->compiled;
}

static void *compiler_thread(void *data)
{
	CodeBlock *cb = (CodeBlock *)data;

	/* unmap */
	disable_codeblock(cb);

	/* unload previous incarnation */
	unload_codeblock(cb);

	/* be sure we won't waste time */
	if (cb->addr < wp && cb->addr + cb->size >= wp + 32)
		return 0;

	logger(_L|LOG_USER, "compiling code block at >%04x (%s)\n", 
		   cb->addr, cb->ent->filename);

	/* compile code block */
	cb->ignore = !compiler_compile(cb);

	logger(_L|LOG_USER, "... done >%04X (%s)\n", 
		   cb->addr, cb->ent->filename);
	
	/* allow future calls to use compiled code */
	cb->compile_thread = 0;
	compile_threads--;
	return 0;
}

//	compile code block
extern int compiler_build(CodeBlock *cb)
{
	if (!cb) return 0;
	if (!cb->compiled)
	{
		int ret;

		/* check to see if this is happening now */
		if (cb->compile_thread || cb->ignore || compile_threads >= MAX_COMPILE_THREADS)
			return 0;

		/* create worker thread (or just do it now) */
		compile_threads++;
		if (OS_CreateThread(&cb->compile_thread, compiler_thread, cb) != OS_NOERR)
			compiler_thread(cb);

		return 0;
	}
	if (!cb->entry)
	{
		/* load into emulator */
		if (!load_codeblock(cb))
			return 0;
	}
	if (!cb->enabled)
	{
		/* map it */
		enable_codeblock(cb);
	}
	return 1;
}

//	execute code, return 0 if instruction must be emulated
int compiler_execute(CodeBlock *cb, u16 pc, u32 *executed, u32 *instcycles)
{
//	u64 startcycles, startinstrs;
	int emul;

	*instcycles = 0;

	/* watch out for error condition */
	if (!cb || !cb->compiled || !cb->entry || !cb->enabled)
		return 1;

//	startcycles = compile_state.cycles;
//	startinstrs = compile_state.instrs;
	compile_state.cycles = 0;
	compile_state.instrs = 0;
	
	compile_state.stat = statusto9900();
	compile_state.pc = pc;
	compile_state.wp = wp;

	emul = compiler_execute_code(cb);

	T9900tostatus(compile_state.stat);
	setandverifypc(compile_state.pc);
	setandverifywp(compile_state.wp);

//	*instcycles = compile_state.cycles - startcycles;
//	*executed = compile_state.instrs - startinstrs;
	*instcycles = compile_state.cycles;
	*executed = compile_state.instrs;

	return emul;
}

extern void compiler_flush_for_wp(u16 addr)
{
	CodeBlock *cb = lookup_codeblock(addr);
	if (!cb || !cb->enabled) return;
	logger(_L|LOG_USER, _("flushing code block at >%04X due to workspace overlap\n"), addr);
	compile_state.executing = 0;
	dirty_codeblock(cb);
}

//	callback when memory map changes
static void compiler_mem_map_reset(void)
{
	if (compile_mode)
	{
		CodeBlock *cb;

		logger(_L|L_1, "flushing compiler cache due to memory map change\n");
		//compiler_stop();
		//compiler_start();
		flush_comphash();
	}
}

//	callback when memory entry changes
static void compiler_mem_entry_reset(MemoryEntry *ent)
{
	if (compile_mode && (ent->flags & MEMENT_DOMAIN) == MEMENT_CONSOLE)
	{
		CodeBlock *cb = lookup_codeblock(ent->addr);

		logger(_L|L_1, _("flushing compile cache for %s(%04x) due to memory content change\n"),
			   ent->filename ? ent->filename : "???", ent->addr);

		compile_state.executing = 0;
		//disable_codeblock(cb);

		disable_codeblocks_for_entry(ent);
		//if (cb) unload_codeblock(cb);
		//if (cb) cb->dirty = true;
	}
}

// callback when memory contents change
static void compiler_trap_mem_write(const mrstruct *mr, u32 addr, s8 val)
{
	CodeBlock *cb = lookup_codeblock(addr);
//	my_assert(cb);
	if (!cb)
	{
		MEMORY_WRITE_BYTE(addr,val);
		return;
	}
	if (cb->enabled && cb->ent->flags & MEMENT_RAM)
	{
		logger(_L|L_1, "flushing compiler cache for %s due to write at %04x\n", 
			   cb->name, addr);

		printf("unloading\n");
		compile_state.executing = 0;
		dirty_codeblock(cb);
	}

	/* forward the write */
	if (cb->ent->memact.write_byte)
		cb->ent->memact.write_byte(mr, addr, val);
	else
		MEMORY_WRITE_BYTE(addr, val);
}

static void compiler_start(void)
{
	int i;
	
	logger(_L|L_1, "Starting compilation\n");

	compile_threads = 0;
	compile_state.executing = 0;

	FOREACH_AREA(md_cpu, area)
	{
		area->exec_hits = 0;
		area->read_hits = 0;
		area->write_hits = 0;
	}
	END_FOREACH_AREA;

	init_comphash();
	codeblocks = 0L;
	compile_state.cycles = compile_state.instrs = 0;
	compile_state.map = &__areahandlers;

	//printf("md_cpu[0x83fa] = %p\n", THE_AREA(md_cpu, 0x83fa));

	compile_state.read_word = memory_read_word;
	compile_state.read_byte = memory_read_byte;
	compile_state.write_word = memory_write_word;
	compile_state.write_byte = memory_write_byte;

	memory_register_callback(compiler_mem_map_reset);
	memory_register_entry_callback(compiler_mem_entry_reset);
}

static void compiler_stop(void)
{
	CodeBlock *c, *n;
	int i;
	logger(_L|L_1, "Stopping compilation\n");

	compile_state.executing = 0;
	compile_threads = 0;
		
	for (c = codeblocks; c; c = n)
	{
		n = c->next;
		if (c->compile_thread)
		{
			OS_KillThread(c->compile_thread, true);
			c->compile_thread = 0;
		}

		unload_codeblock(c);
	}

//	free_comphash();
}

void compiler_init(void)
{
	command_symbol_table *compilercommands =
		command_symbol_table_new(_("Compiler Options"),
								 _("These are commands for controlling real-time compilation"),

			command_symbol_new
							("CompileMode",
							 NULL /* help */,
							 c_STATIC|c_DONT_SAVE,
							 set_compile_mode,
							 RET_FIRST_ARG,
							 command_arg_new_enum("off|on",
										   _("enable or disable real-time compilation of 9900 code"),
										   NULL /* action */ ,
										   NEW_ARG_NUM(int),
										   NULL /* next */ )
					   ,

	  command_symbol_new("CompileDirectory",
						 _("Set compiler temporary directory"),
						 c_STATIC,
						 NULL /* action*/,
						 RET_FIRST_ARG,
						 command_arg_new_pathspec
						 (_("directory"), 
						  _("temporary directory"),
						  NULL	/* action */,
						  &compiledatadir,
						  NULL /* next */ )
						 ,

	NULL /* next */ )),

    NULL /* sub */ ,

	NULL	/* next */
	);

	OSError err;
	OSSpec dir;

	command_symbol_table_add_subtable(universe, compilercommands);

	if (OS_MakeSpecWithPath(&v9t9_datadir, "compilertmp", false, &dir) != OS_NOERR
		|| (OS_Mkdir(&dir) != OS_NOERR && OS_Status(&dir) != OS_NOERR)
		|| OS_MakeSpecWithPath(&v9t9_datadir, "compilertmp", false, &dir) != OS_NOERR)
	{
		OSPathSpec cwd;
		OS_GetCWD(&cwd);
		if (OS_MakeSpecWithPath(&cwd, "compilertmp", false, &dir) != OS_NOERR
			|| (OS_Mkdir(&dir) != OS_NOERR && OS_Status(&dir) != OS_NOERR)
			|| OS_MakeSpecWithPath(&cwd, "compilertmp", false, &dir) != OS_NOERR)
			dir.path = cwd;
	}
	compiledatadir = dir.path;
}

void compiler_term(void)
{
	compiler_stop();
}

#endif
