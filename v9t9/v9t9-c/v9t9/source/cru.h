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
  $Id$
 */

#ifndef __CRU_H__
#define __CRU_H__

#include "v9t9_types.h"

#include "9901.h"

#include "centry.h"

void	cruwrite(u32 addr,u32 val,u32 num);

u32		cruread(u32 addr,u32 num);

typedef	u32	(crufunc)(u32 addr,u32 data,u32 num);

#define	CRU_READ 1
#define	CRU_WRITE 2

/*
	range is in BITS, not address units.  base - base+range*2
*/
int		cruadddevice(int rw, u32 base, u32 range, crufunc *func);
int		crudeldevice(int rw, u32 base, u32 range, crufunc *func);

void	cruinit(void);

DECL_SYMBOL_ACTION(dump_cru_list);

#include "cexit.h"

#endif
