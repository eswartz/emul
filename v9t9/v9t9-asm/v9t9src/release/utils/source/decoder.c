/*
  decoder.c

  (c) 1991-2012 Edward Swartz

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
	This file will define the "decoder" (and the encoder) for
	encoded files.

*/

#include <io.h>
#include <fcntl.h>
#include <stdio.h>
#include <sys\stat.h>

int	decoder(char *inname, char *outname, char *key)
{
	int	handle;
	char	buff[1024];
	struct	stat st;
	long	left;
	unsigned len;
	unsigned i;
	char	* index;
	long	filepos;


	remove(outname);

	rename(inname,outname);
	if (stat(outname,&st))
		return 1;

	handle=open(outname,O_RDWR|O_BINARY);
	if (handle==-1)
		return 1;

	index=key;
	left=st.st_size;
	while (left)
	{
		len=(left < 1024 ? left : 1024);

		filepos=lseek(handle,0,SEEK_CUR);

		if (read(handle,buff,len)!=len)
		{
			close(handle);
			return 1;
		}

		for (i=0; i<len; i++)
		{
			buff[i]^=(*index)^(index-key);
			index++;
			if (!*index)
				index=key;
		}

		lseek(handle,filepos,SEEK_SET);

		if (write(handle,buff,len)!=len)
		{
			close(handle);
			return 1;
		}

		left-=len;
	}

	close(handle);
	return 	0;
}
