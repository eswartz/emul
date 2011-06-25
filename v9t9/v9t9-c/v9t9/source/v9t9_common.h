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

#ifndef __V9t9_COMMON_H__
#define __V9t9_COMMON_H__

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#undef _L	/* stray from Cygwin ctype.h */

#include "config.h"

#ifndef SHAREDIR
#define SHAREDIR "."
#endif

#ifndef TOPSRCDIR
#define TOPSRCDIR "."
#endif

#if !HAVE_GETTEXT
#undef ENABLE_NLS
#define _(string)	(string)
#define N_(string)	(string)
#define textdomain(Domain)
#define bindtextdomain(Package, Directory)
#else
#include <libintl.h>
#define _(string)	gettext(string)
#define N_(string)	gettext(string)
#endif

#include "OSLib.h"
#include "StringUtils.h"
#include "xmalloc.h"
#include "v9t9_defs.h"
#include "v9t9_types.h"
#include "log.h"
#include "emulate.h"
#include "v9t9_module.h"
#include "moduleconfig.h"
#include "sysdeps.h"
#include "datafiles.h"

#endif
