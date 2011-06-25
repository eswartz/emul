/*
  (c) 1994-2001 Edward Swartz

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

#ifndef __V9t9_ENDIAN__
#define __V9t9_ENDIAN__

#include "v9t9_types.h"

#include "centry.h"

#ifdef HAVE_ENDIAN_H

#	include <endian.h>

#	if __BYTE_ORDER == __LITTLE_ENDIAN
#	define SWAPPED_ENDIAN	1
#	else
#	define SWAPPED_ENDIAN 0
#	endif

#else	// !HAVE_ENDIAN_H

#	if __INTEL__ || __i386__
#	define SWAPPED_ENDIAN 1
#	else
#	error Unknown endianness on this machine
#	define SWAPPED_ENDIAN 0
#	endif

#endif

/*	macros to reverse words */

#if SWAPPED_ENDIAN
#	define TI2HOST(x) ( (((u16)(x) & 0xff) << 8) | (((u16)(x) >> 8) & 0xff) )
#	define HOST2TI TI2HOST
#	define SWAPTI(x) ((u16)(x))
#else
#	define TI2HOST(x)	((u16)(x))
#	define HOST2TI TI2HOST
#	define SWAPTI(x) ( (((u16)(x) & 0xff) << 8) | (((u16)(x) >> 8) & 0xff) )
#endif


#include "cexit.h"

#endif
