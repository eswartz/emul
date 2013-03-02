/*
  xmalloc.c						-- instant-fail memory allocators

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

#include "v9t9_common.h"

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <config.h>
#include "sysdeps.h"
#include "xmalloc.h"

#if defined __GLIBC__ || defined MALLOC_HOOKS
void
xminit(void)
{
}

void
xminfo(void)
{
	malloc_stats();
}

void
xmterm(void)
{
}
#else
void
xminit(void)
{
}

void
xminfo(void)
{
}
void
xmterm(void)
{
}
#endif

#define _L	 LOG_INTERNAL | LOG_INFO

void *
xmalloc(size_t sz)
{
	void       *ret;

	ret = malloc(sz);
	if (!ret)
	{
		fprintf(stderr, _("xmalloc:  failed in allocation of %d bytes\n"), sz);
		exit(23);
	}
	return ret;
}

void *
xcalloc(size_t sz)
{
	void       *ret;

	ret = calloc(sz, 1);
	if (!ret)
	{
		fprintf(stderr, _("xcalloc:  failed in allocation of %d bytes\n\n"), sz);
		exit(23);
	}
	return ret;
}

void *
xrealloc(void *ptr, size_t sz)
{
	void       *ret = realloc(ptr, sz);

	if (!ret)
	{
		fprintf(stderr, _("xrealloc:  failed in reallocation to %d bytes\n\n"),
			 sz);
		exit(23);
	}
	return ret;
}

void
xfree(void *ptr)
{
	if (ptr)
		free(ptr);
}

char *
xstrdup(const char *str)
{
	char       *ret;
	 
	if (str)
	{
		ret = (char *) strdup(str);
		if (!ret)
		{
			fprintf(stderr, _("xstrdup:  failed to copy string\n\n"));
			exit(23);
		}
		return ret;
	}
	else
		return 0L;
}

char *
xintdup(int x)
{
	char       *ret = (char *) xmalloc(sizeof(int));

	if (!ret)
	{
		fprintf(stderr, _("xintdup:  failed to copy int\n\n"));
		exit(23);
	}
	memcpy(ret, &x, sizeof(int));

	return ret;
}
