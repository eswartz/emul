#if _WIN32

#ifdef HAVE_CONFIG_H
# include <config.h>
#endif

#ifndef HAVE_OPENDIR

#include <sys/stat.h>
#include <errno.h>
#include <stdlib.h>

#include "dirent.h"

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

DIR *opendir (const char *spec)
{
	char        wildcard[MAXNAMLEN + 4], *wptr;
	int			len;
	unsigned long err;
	DIR			*ref;
	
	if (!spec)
	{
		errno = EINVAL;
		return NULL;
	}

	len = strlen(spec);
	if (len >= MAXNAMLEN)
	{
		errno = ENAMETOOLONG;
		return NULL;
	}

	ref = (DIR *)malloc(sizeof(DIR));
	if (ref == NULL)
	{
		errno = ENOMEM;
		return NULL;
	}

	ref->_d__dirent = (struct dirent *)malloc(sizeof(struct dirent));
	if (ref->_d__dirent == NULL)
	{
		free(ref);
		errno = ENOMEM;
		return NULL;
	}

	/* get data structure for directory walk */
	ref->_d__ffd = (void *) malloc(sizeof(WIN32_FIND_DATA));
	if (ref->_d__ffd == NULL)
	{
		free(ref->_d__dirent);
		free(ref);
		errno = ENOMEM;
		return NULL;
	}

	/* form wildcard */
	strcpy(wildcard, spec);
	wptr = wildcard + len;
	
	/* ensure trailing backslash */
	if (*(wptr-1) != '\\')
	{
		*wptr++ = '\\';
	}

	*wptr++ = '*';
	*wptr++ = 0;

	/* remember wildcard so we can form full path */
	ref->_d__wildcard = _strdup(wildcard);

	/* open directory */
	ref->_d__handle = FindFirstFile(ref->_d__wildcard, (WIN32_FIND_DATA *) ref->_d__ffd);
	if (ref->_d__handle == INVALID_HANDLE_VALUE)
	{
		err = GetLastError();
		if (err == ERROR_FILE_NOT_FOUND
			|| err == ERROR_PATH_NOT_FOUND)
			errno = ENOENT;
		else if (err == ERROR_INVALID_PARAMETER)
			errno = EINVAL;
		else
			errno = EACCES;

		free(ref->_d__dirent);
		free(ref->_d__ffd);
		free(ref->_d__wildcard);
		free(ref);
		return NULL;
	}
	else
	{
		errno = ENOERR;
		return ref;
	}
}

struct dirent *readdir (DIR *ref)
{
	WIN32_FIND_DATA *ffd = (WIN32_FIND_DATA *) ref->_d__ffd;
	struct dirent *ent = ref->_d__dirent;

	do
	{
		if (ref->_d__handle == INVALID_HANDLE_VALUE)
		{
			errno = ENOENT;
			return NULL;
		}
		else 
		{
			/*  we already have one entry cached */
			if (strlen(ffd->cFileName) < MAXNAMLEN)
				strncpy(ent->d_name, ffd->cFileName, MAXNAMLEN);
			else
				strncpy(ent->d_name, ffd->cAlternateFileName, MAXNAMLEN);
			ent->d_name[MAXNAMLEN - 1] = 0;

			/*  cache next value */
			if (FindNextFile(ref->_d__handle, ffd) == 0)
			{
				/* end of list, close and signal next iteration to fail */
				FindClose(ref->_d__handle);
				ref->_d__handle = INVALID_HANDLE_VALUE;
			}
		}
	} while (strcmp(ent->d_name, ".") == 0 || strcmp(ent->d_name, "..") == 0);

	ent->d_ino = 0;
	return ent;
}

void rewinddir (DIR *ref)
{
	if (ref->_d__handle != INVALID_HANDLE_VALUE)
		FindClose(ref->_d__handle);
	ref->_d__handle = FindFirstFile(ref->_d__wildcard, (WIN32_FIND_DATA *) ref->_d__ffd);	
}

int closedir (DIR *ref)
{
	if (ref->_d__handle != INVALID_HANDLE_VALUE)
		FindClose(ref->_d__handle);
	free(ref->_d__dirent);
	free(ref->_d__ffd);
	free(ref->_d__wildcard);
	free(ref);
	errno = ENOERR;
	return 0;
}

#endif	/* HAVE_OPENDIR */

#endif	/* _WIN32 */

