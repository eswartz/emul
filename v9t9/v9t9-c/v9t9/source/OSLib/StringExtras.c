/*
  StringExtras.c				-- various strXXX() extensions

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

#include "config.h"
#include "clstandardheaders.h"
#include <ctype.h>
#include "OSLib.h"


char       *
strcatn(char *d, const char *s, long max)
{
	char       *p = d + strlen(d);

	while (*s && p - d + 1 < max)
		*p++ = *s++;
	*p = 0;
	return d;
}

char       *
strcpyn(char *d, const char *s, long len, long max)
{
	char       *p = d;

	while (len-- && *s && p - d + 1 < max)
		*p++ = *s++;
	*p = 0;
	return d;
}

const char *
stristr(const char *hay, const char *nee)
{
	while (*hay) {
		int         idx = 0;

		while (hay[idx] && nee[idx]
			   && toupper(hay[idx]) == toupper(nee[idx]))
			idx++;
		if (!nee[idx])
			return hay;
		hay++;
	}
	return NULL;
}
