/*
  StringUtils.c					-- infinite-buffer vprintf() routines

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
#include <stdlib.h>
#include <assert.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include "StringUtils.h"
#include "xmalloc.h"

/*	Print to a memory buffer and allocate a larger one as needed.
	If return is != buf, we allocated, and the caller must free. */
char       *
mvprintf(char *mybuf, unsigned len, const char *format, va_list va_)
{
	int         maxlen;
	int         ret;
	char       *buf;

	assert(mybuf != NULL);
	maxlen = len;
	buf = mybuf;

	/*  return of -1 indicates buffer is in old standards;
	   return of val >= maxlen indicates buffer is too small in C9X */
	va_list va;
	va_copy(va, va_);
	ret = vsnprintf(buf, maxlen, format, va);
	if (ret < 0) {
		do {
			if (buf != mybuf)
				free(buf);
			maxlen <<= 1;
			if (maxlen >= 65536)      /* prolly an error */
			{
				buf = (char*) xmalloc(strlen(format)+1);
				strcpy(buf, format);
				return buf;
			}
			buf = (char *) xmalloc(maxlen);
			va_copy(va, va_);
		} while ((ret = vsnprintf(buf, maxlen, format, va)) < 0);
	} else if (ret > maxlen) {
		maxlen = ret + 1;
		buf = (char *) xmalloc(maxlen);
		va_copy(va, va_);
		ret = vsnprintf(buf, maxlen, format, va);
	}

	return buf;
}

char       *
mprintf(char *mybuf, unsigned len, const char *format, ...)
{
	va_list     va;
	char       *ret;

	va_start(va, format);
	ret = mvprintf(mybuf, len, format, va);
	va_end(va);

	return ret;
}
