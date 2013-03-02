/*
  tidir.c						-- get directory listing for V9t9 files

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

#include <locale.h>
#include "v9t9_common.h"
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>

#include "fiad.h"

enum
{
	TOLIST_WIDE,
	TOLIST_NORMAL,
	TOLIST_LONG
};
int	tolist = TOLIST_NORMAL;
int	sort_by = FIAD_CATALOG_SORT_BY_DISK;
bool sort_ascending = true;
fiad_catalog cat;

static int	
get_catalog(char *name)
{
	OSError err;

	err = fiad_catalog_read_catalog(&cat, name);
	if (err != OS_NOERR)
	{
		fprintf(stderr, _("Could not read catalog for '%s' (%s)\n"),
				name, OS_GetErrText(err));
		return 0;
	}
	else
		return 1;
}

static void	
display_header(void)
{
	int	x;

	switch (tolist)
	{
	case TOLIST_WIDE:	break;
	case TOLIST_NORMAL:
//		for (x=0; x<2; x++)
		printf("%-10s  %-4s  %-11s  \t",
			   _("Filename"),_("Secs"), _("Type"));
		break;

	case TOLIST_LONG:
		printf("%-10s  %-4s  %-11s  %-8s  %-5s  %s",
				_("Filename"),_("Secs"),_("Type"),
			   _("Bytesize"),_("#recs"), _("Host name"));
		break;
	}
	printf("\n");
}

static void	
display_entry(char *filename, fdrrec *fdr, int index)
{
	int	on_line;
	int sec;

	switch (tolist)
	{
	case TOLIST_WIDE:
		printf("%.10s ",fdr->filenam);
		on_line = 7;
		break;
		
	case TOLIST_NORMAL:
		printf("%.10s  %4d  %-11s  ",
			   fdr->filenam,
			   fdr->secsused,
			   fiad_catalog_get_file_type_string(fdr));
		on_line = 2;
		break;

	case TOLIST_LONG:
		printf("%.10s  %4d  %-11s  ",
			   fdr->filenam,
			   fdr->secsused,
			   fiad_catalog_get_file_type_string(fdr));

		printf("%8ld  ",FDR_FILESIZE(fdr));
		if (!(fdr->flags&(ff_variable|ff_program)))
			printf("%5d  ",fdr->numrecs);
		else
			printf("%5s  ","");

		printf("%s", filename);

/*
        sec = cat->fdrsec[cat->index[index]];
		if (sec)
			printf("%5d",sec);
		else
			printf("%5s","");
*/
		on_line = 1;
		break;
	}

//	if ((index + 1) % on_line == 0)
		printf("\n");
//	else 
//		printf("\t");
}

static int	
display_catalog(void)
{
	int	index;
	int	lines;

//	First, sort the list.

	fiad_catalog_sort_catalog(&cat, sort_by, sort_ascending);

	index = 0;
	while (index < cat.entries)
	{
		display_entry(cat.filenames[cat.index[index]],
					  &cat.fdrs[cat.index[index]], 
					  index);
		index++;
	}
	printf("\n\n");

	return 1;
}

static int	
free_catalog(void)
{
	fiad_catalog_free_catalog(&cat);
	return 1;
}

static void	
help(void)
{
	printf(_("\n"
		   "TIDIR V9t9 Directory Lister v1.1\n"
		   "\n"
		   "Usage:   TIDIR [options] { <directory> }\n"
//		   "Usage:   TIDIR [options] { <file> | <directory> | <disk image:> }\n"
		   "\n"
		   "TIDIR will print the listing of files in emulated directories.\n"
//		   "or disk images.\n"
		   "\n"
		   "Options:\n"
		   "\t\t-?\t-- this help\n"
		   "\t\t-l\t-- long format\n"
		   "\t\t-w\t-- wide format\n"
		   "\t\t-o[-]x\t-- sort by Disk order, Name (default), Size, or Type.\n"
		   "\t\t\t   \"-\" means descending.\n"
			 "\n")
		);
}

static	char	*legalsorts="DNST";

int	main(int argc, char **argv)
{
	char	*whichsort;
	int		sortchar;
	int		opt;
	bool	printed_header;
	int		bad;

	setlocale(LC_ALL, "");
	bindtextdomain(PACKAGE, LOCALEDIR);
	textdomain(PACKAGE);

	if (argc <= 1)
	{
		help();
		return 0;
	}

	tolist = TOLIST_NORMAL;
	sort_by = FIAD_CATALOG_SORT_BY_NAME;
	sort_ascending = true;

	while ((opt = getopt(argc, argv, "?HhWwLlO:o:")) != -1)
	{
		switch (opt)
		{
		case '?':
		case 'H':
		case 'h':	
			help();
			break;
		case 'W':
		case 'w':
			tolist = TOLIST_WIDE;
			break;
		case 'L':	
		case 'l':
			tolist = TOLIST_LONG;
			break;
		case 'O':
		case 'o':
			if (optarg[0] == '-')
				sortchar = 1;
			else
				sortchar = 0;

			if (optarg[sortchar + 1])
			{
				fprintf(stderr, _("Too many letters in /O parameter (%s).\n"),
						optarg);
				return 1;
			}
			whichsort = strchr(legalsorts, toupper(optarg[sortchar]));
			if (whichsort == NULL)
			{
				fprintf(stderr, _("Unknown sort option in /O parameter (%s).\n"), 
					   optarg);
				return 1;
			}
			sort_by = whichsort - legalsorts + FIAD_CATALOG_SORT_BY_DISK;
			sort_ascending = (sortchar != 1);
			break;
		}
	}

	printed_header = false;

	while (argv[optind])
	{
		if (get_catalog(argv[optind]))
		{
			if (!printed_header)
			{
				display_header();
				printed_header = true;
			}

			display_catalog();
			free_catalog();
		}

		optind++;
	}

	return 0;
}

