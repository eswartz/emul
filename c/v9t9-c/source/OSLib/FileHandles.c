/*
  FileHandles.c				   	-- utilities for dealing with files in memory

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

#include "config.h"
#include "OSLib.h"

#include <string.h>
#include <stdio.h>
#include <string.h>
#if __MWERKS__ && !__INTEL__
#include <alloca.h>
#else
#include <malloc.h>
#endif
#include <ctype.h>

#include <stdlib.h>
#include <assert.h>


/*	File handle routines.  */

/*
	Currently implemented stupid 'n' simple.  
	Later, we will want to use purgeable handles on the Mac
	and mmap()ing on other OSes.
*/

static      OSError
OS_LoadFileHandle(OSFileHandle * hand)
{
	OSError     err;
	OSRef       ref;
	OSSize      sz;
	void       *buffer;

	hand->loaded = false;
	if ((err = OS_Open(&hand->spec, OSReadOnly, &ref)) != OS_NOERR)
		goto err_0;

	if ((err = OS_GetSize(ref, &sz)) != OS_NOERR)
		goto err_1;

	if ((err = OS_ResizeHandle(&hand->hand, sz)) != OS_NOERR)
		goto err_1;

	buffer = OS_LockHandle(&hand->hand);

	if ((err = OS_Read(ref, buffer, &sz)) != OS_NOERR)
		goto err_2;

	hand->loaded = true;
	hand->changed = false;

  err_2:
	OS_UnlockHandle(&hand->hand);
  err_1:
	OS_Close(ref);
  err_0:
	return err;

}

static      OSError
OS_WriteFileHandle(OSFileHandle * hand)
{
	OSError     err;
	OSRef       ref;
	OSSize      sz;
	void       *buffer;

	if (!hand->loaded && !hand->changed)
		return OS_NOERR;

	OS_Delete(&hand->spec);

	if ((err = OS_Create(&hand->spec, &OS_TEXTTYPE)) != OS_NOERR)
		goto err_0;

	if ((err = OS_Open(&hand->spec, OSReadWrite, &ref)) != OS_NOERR)
		goto err_0;

	if ((err = OS_GetHandleSize(&hand->hand, &sz)) != OS_NOERR)
		goto err_0;

	buffer = OS_LockHandle(&hand->hand);

	if ((err = OS_Write(ref, buffer, &sz)) != OS_NOERR)
		goto err_2;

	hand->changed = false;

  err_2:
	OS_UnlockHandle(&hand->hand);
  err_1:
	OS_Close(ref);
  err_0:
	return err;
}

/*	Create a new file handle from a given spec;
	if src is non-NULL, copy this handle (don't link) */
OSError
OS_NewFileHandle(OSSpec * spec, OSHandle * src, bool writeable,
				 OSFileHandle * hand)
{
	OSError     err;

	if (!writeable && src != NULL)
		return OS_PERMERR;

	hand->spec = *spec;
	hand->writeable = writeable;
	if (src == NULL) {
		err = OS_NewHandle(0, &hand->hand);

		if (err != OS_NOERR)
			return err;

		/* load once at outset */
		err = OS_LoadFileHandle(hand);
	} else {
		err = OS_CopyHandle(src, &hand->hand);

		if (err != OS_NOERR)
			return err;

		hand->changed = true;
		hand->loaded = true;
	}
	return err;
}

/*	Lock a file handle into memory  */
OSError
OS_LockFileHandle(OSFileHandle * hand, void **ptr, OSSize * size)
{
	/*  Currently, we give the actual image to the caller,
	   but in the future, depending on 'writeable', we should
	   protect the memory.
	 */
	*size = 0;

	if (!OS_ValidHandle(&hand->hand))
		return OS_MEMERR;

	*ptr = OS_LockHandle(&hand->hand);
	OS_GetHandleSize(&hand->hand, size);
	return OS_NOERR;
}

/*	Unlock file handle */
OSError
OS_UnlockFileHandle(OSFileHandle * hand, void *ptr)
{
	if (!OS_ValidHandle(&hand->hand))
		return OS_MEMERR;
	OS_UnlockHandle(&hand->hand);
	return OS_NOERR;
}

/*	Dispose file handle;
	this guarantees that changes are flushed */
OSError
OS_FreeFileHandle(OSFileHandle * hand)
{
	OSError     err;

	/*  In the future, this will have already been done. */
	if (hand->writeable && hand->changed) {
		if ((err = OS_WriteFileHandle(hand)) != OS_NOERR)
			return err;
	}

	if (!OS_ValidHandle(&hand->hand))
		return OS_MEMERR;

	if ((err = OS_FreeHandle(&hand->hand)) != OS_NOERR)
		return err;

	hand->loaded = 0;
	return OS_NOERR;
}

/*	Get spec from the file handle */
void
OS_GetFileHandleSpec(OSFileHandle * hand, OSSpec * spec)
{
	*spec = hand->spec;
}
