/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
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