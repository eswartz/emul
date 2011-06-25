
#ifndef __COMPILER_INTERNALS_H__
#define __COMPILER_INTERNALS_H__

/*	This info is used internally by compiled code */

#include "v9t9_types.h"
#define FOR_COMPILER 1
#include "memory.h"

/*	info the compiled code uses for emulation */
typedef struct CompilerState
{
	int			executing;
	u16			(*read_word)(u32 addr);
	s8			(*read_byte)(u32 addr);
	void		(*write_word)(u32 addr, u16 val);
	void		(*write_byte)(u32 addr, s8 val);
	u64 		cycles, instrs;			/* cumulative counts */
	mrmap		*map;
	u16			pc, wp, stat;
}	CompilerState;

/*	entry point type for "native" */
typedef int (*CompiledCodeEntry)(CompilerState *state);

/*	info we retrieve from compiled code query */
typedef struct CompiledCode
{
	u32			logicalsize;			/* amount of 9900 code represented */
	u16			startaddr;				/* start 9900 address */
}	CompiledCode;

#endif /*__COMPILER_INTERNALS_H__*/
