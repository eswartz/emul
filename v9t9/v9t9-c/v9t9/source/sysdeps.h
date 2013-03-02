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

#ifndef __SYSDEPS_H__
#define __SYSDEPS_H__

#if __MWERKS__ && defined(_WIN32) && defined(_MSL_IMP_EXP)
#include <size_t.h>
typedef long ssize_t;
#endif

#if !HAVE_STRCASECMP
extern int strcasecmp(const char *a, const char *b);
#endif

#if !HAVE_STRNCASECMP
extern int strncasecmp(const char *a, const char *b, size_t max);
#endif

#if !HAVE_STRDUP
extern char *strdup(const char *s);
#endif

#if !HAVE_SWAB
extern void swab(const void *src, void *dst, ssize_t num);
#endif

#if !HAVE_STRUPR
extern char *strupr(char *s);
#endif

#if __GNUC__
#define INLINE 	static inline
#elif __MWERKS__
#define INLINE	inline
#else
#define INLINE	static
#endif

#endif
