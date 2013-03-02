/*
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
  $Id: romaddr.c,v 1.4 2008-10-19 19:37:36 ejs Exp $
 */

/*
 * Get ROM address from console ROM image
 */

#include <stdlib.h>
#include <stdio.h>

int main(int argc, char **argv)
{
	FILE *f;
	unsigned char addrbytes[2];

	if (argc < 2) {
		fprintf(stderr, "invoke as: romaddr [console rom image]\n");
		exit(1);
	}

	/* open ROM */
	f = fopen(argv[1], "rb");
	if (!f) {
		perror(argv[1]);
		exit(1);
	}

	/* the size of the console ROM is stored at offset 0x48 */
	fseek(f, 0x48, SEEK_SET);
	if (fread(addrbytes, 1, 2, f) != 2) {
		perror("could not read size bytes");
		exit(1);
	}

	printf("%d", (addrbytes[0] << 8) + addrbytes[1]);
	fclose(f);
}
