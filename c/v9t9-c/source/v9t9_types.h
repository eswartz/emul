/*
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

#ifndef __V9t9_TYPES_H__
#define __V9t9_TYPES_H__

#include "clstandardtypes.h"

#include "centry.h"

#if  (__MWERKS__  && __INTEL__)  || (__GNUC__ && __i386__) || (__APCS_32__)

typedef unsigned short u16;
typedef signed short s16;
typedef unsigned char u8;
typedef signed char s8;
typedef unsigned long u32;
typedef signed long s32;
typedef unsigned long long u64;
typedef signed long long s64;

#elif (__GNUC__ && __x86_64__)

typedef unsigned short u16;
typedef signed short s16;
typedef unsigned char u8;
typedef signed char s8;
typedef unsigned int u32;
typedef signed int s32;
typedef unsigned long u64;
typedef signed long s64;

#else

#error "add processor to v9t9_types.h"

#endif


/*	Optimal values by system/compiler.  */

/*	GCC always moves 16 bit math into ints anyway, 
	so save it the trouble. */
#if __GNUC__

typedef unsigned int uword;
typedef signed int sword;
typedef unsigned int uaddr;
typedef unsigned int uop;

/*	Codewarrior is smarter.  */
#elif __MWERKS__

typedef unsigned short uword;
typedef signed short sword;
typedef unsigned int uaddr;
typedef unsigned int uop;

#else
#error
#endif

#include "cexit.h"
#endif
