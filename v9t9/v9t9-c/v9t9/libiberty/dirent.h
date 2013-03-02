/*
dirent.h

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
#ifndef _SYS_DIRENT_H
#define _SYS_DIRENT_H

#include <sys/types.h>

#ifndef MAXNAMLEN
#define MAXNAMLEN 256
#endif

struct dirent
{
  long __d_reserved[4];
  _ino_t d_ino; /* Just for compatibility, it's junk */
  char d_name[256];
};

typedef struct
{
	struct dirent 	*_d__dirent;
  	char 			*_d__wildcard;			/* "directory\\*" */
  	unsigned long 	*_d__handle;			/* for FindNextFile() */
	void 			*_d__ffd;				/* really WIN32_FIND_DATA */ 
} DIR;

DIR *opendir (const char *);
struct dirent *readdir (DIR *);
void rewinddir (DIR *);
int closedir (DIR *);

#endif
