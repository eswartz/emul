/*
  sysdeps.c						-- definitions of undefined library routines

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

#include <config.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include "sysdeps.h"

#if !HAVE_STRCASECMP
int
strcasecmp(const char *a, const char *b) 
{
	int        x;

	while ((x = tolower(*a) - tolower(*b)) == 0 && *a) {
		a++;
		b++;
	}
	return x;
}
#endif

#if !HAVE_STRNCASECMP
int
strncasecmp(const char *a, const char *b, size_t max)
{
	int        x = 0;

	while (max-- && (x = tolower(*a) - tolower(*b)) == 0 && *a) {
		a++;
		b++;
	}
	return x;
}
#endif 
   
#if !HAVE_STRDUP
char *
strdup(const char *s)
{
	int        len = strlen(s) + 1;
	char      *ret = (char *) malloc(len);

	if (ret)
		strcpy(ret, s);
	return ret;
}

#endif

#if !HAVE_SWAB
void
swab(const void *_src, void *_dst, ssize_t num)
{
	const char *src = (const char *)_src;
	char *dst = (char *)_dst;

	while (num > 2) {
		char       tmp;

		tmp = src[0];
		dst[0] = src[1];
		dst[1] = tmp;
		dst += 2;
		src += 2;
		num -= 2;
	}
} 
#endif 

#if !HAVE_STRUPR
char *
strupr(char *s)
{
	char      *orig = s;

	while (*s) {
		*s = toupper(*s);
		s++;
	}
	return orig;
}

#endif

