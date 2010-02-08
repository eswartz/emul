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
