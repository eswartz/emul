
#ifndef __COMPILER_INTERNALS_H__
#define __COMPILER_INTERNALS_H__
/*
compiler_internals.h

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
