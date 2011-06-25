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

#include <stdarg.h>

#include "centry.h"

/*	Print to a memory buffer and allocate a larger one as needed.
	If return is != buf, we allocated, and the caller must free. */
char	*mvprintf(char *mybuf, unsigned len, const char *format, va_list va);
char	*mprintf(char *mybuf, unsigned len, const char *format, ...);

#include "cexit.h"
