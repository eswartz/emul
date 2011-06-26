/*
  ti2txt.c						-- convert TI file to text

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

#include <locale.h>
#include "v9t9_common.h"
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>
#include <errno.h>

#include "fiad.h"

/* options */
char *progname = 0L;
char *ext = ".txt";
int binary = 0;
int write_stdout = 0;

static void my_fiad_logger(u32 mask, const char *format, ...)
{
	va_list va;
	int type;
	
	type = mask & LOG_TYPE_MASK;
	if (type == LOG_DEBUG || type == LOG_INFO) return;
//	if (!(mask & LOG_USER)) return;

	va_start(va, format);
	fprintf(stderr, "%s: ", progname);
	vfprintf(stderr, format, va);
	va_end(va);
}

static int
convert_file(const char *name)
{
	OSError err;
	fiad_tifile tf;
	OSSpec spec;
	u8 buffer[256]; 
	int filesize;
	u16 maxread, curread;
	OSSpec outspec;
	FILE *file;
	int ret = 0;

	/* get the TI file */
	if ((err = OS_MakeFileSpec(name, &spec)) != OS_NOERR) {
		/* except for this one, my_fiad_logger should be sending errors to stderr */
		fprintf(stderr, _("%s: cannot access file '%s' (%s)\n"),
				progname, name, OS_GetErrText(err));
		return err;
	}

	/* make sure it's valid */
	fiad_tifile_clear(&tf);
	err = fiad_tifile_setup_spec_with_spec(&tf, &spec);

	/* get the FDR for file type */
	if (err != OS_NOERR || !fiad_tifile_get_info(&tf)) {
		fprintf(stderr, _("%s: cannot access file '%s' (%s)\n"),
				progname, name, OS_GetErrText(tf.error));
		return -1;
	}

	/* decide how to treat it */
	if ((err = fiad_tifile_open_file(&tf, newfileformat, false /*create*/,
									 false /*always*/, true /*readonly*/)) != OS_NOERR)
		return -1;

	/* get name for output file */
	err = OS_MakeFileSpec(OS_NameSpecToString1(&spec.name), &outspec);
	if (err != OS_NOERR) {
		fprintf(stderr, _("%s: cannot access output file './%s' (%s)\n"),
				progname, OS_NameSpecToString1(&spec.name), OS_GetErrText(err));
		return err;
	}
	OS_NameSpecChangeExtension(&outspec.name, ext, true /*append*/);

	/* make output file */
	if (write_stdout) {
		file = stdout;
	}
	else if ((file = fopen(OS_SpecToString1(&outspec), "wb")) == 0L) {
		fprintf(stderr, _("%s: cannot open output file '%s' (%s)\n"),
				progname, OS_SpecToString1(&outspec), strerror(errno));
		return -1;
	}

	/* cache first sector */
	fiad_tifile_read_sector(&tf);

	/* now translate */
	if (tf.format == F_TEXT) {
		/* already native file */
		FILE *native = fopen(OS_SpecToString1(&spec), "rb");
		if (!native) {
			fprintf(stderr, _("%s: cannot reopen input file '%s' (%s)\n"),
					progname, OS_SpecToString1(&spec), OS_GetErrText(err));
			ret = -1;
		}
		else {
			while ((maxread = fread(buffer, 1, 256, native)) > 0) {
				fwrite(buffer, 1, maxread, file);
			}
		}
	}
	else {
		/* real TI file */
		if (tf.fdr.flags & ff_program)
		{
			int ret;
			/* program file: dump binary contents */

			while ((ret = fiad_tifile_read_binary_image(&tf, buffer, 256, &maxread)) >= 0)
			{
				if (!maxread) break;
				if (fwrite(buffer, maxread, 1, file) != 1) {
					fprintf(stderr, _("%s: cannot write %d bytes to output file '%s' (%s)\n"),
							progname, maxread, OS_SpecToString1(&outspec), strerror(errno));
					ret = -1;
					break;
				}
			}
		}
		else {
			/* record-oriented file: write record as line unless in binary mode */
			u8  len;
			while (fiad_tifile_read_record(&tf, buffer, &len) == 0) {
				if (binary) {
					if (((tf.fdr.flags & ff_variable) && fwrite(&len, 1, 1, file) != 1) 
						|| fwrite(buffer, 1, len, file) != len) {
						fprintf(stderr, _("%s: cannot write %d bytes to output file '%s' (%s)\n"),
								progname, len, OS_SpecToString1(&outspec), strerror(errno));
						ret = -1;
						break;
					}
				} else {
					if (fwrite(buffer, 1, len, file) != len 
						|| fwrite("\n", 1, 1, file) != 1) {
						fprintf(stderr, _("%s: cannot write %d bytes to output file '%s' (%s)\n"),
								progname, len, OS_SpecToString1(&outspec), strerror(errno));
						ret = -1;
						break;
					}
				}
			}
		}
	}

	if (file != stdout)
		fclose(file);
	fiad_tifile_close_file(&tf);
	return ret;
}

static void	
help(void)
{
	printf(_("\n"
		   "TI2TXT TI-to-Native File Converter v1.1\n"
		   "\n"
		   "Usage:   TI2TXT [options] { <file> }\n"
		   "\n"
		   "TI2TXT will convert files into text (or binary) for use on the local systemm\n"
		   "\n"
		   "Options:\n"
		   "\t\t-?\t\t-- this help\n"
		   "\t\t-b\t\t-- convert binary\n"
		   "\t\t-e <ext>\t-- modify extension added to native files (default=.txt)\n"
			 "\t\t-s\t\t-- write files to stdout\n"
			 "\n")
		);
}

int	main(int argc, char **argv)
{
	int		opt;
	int		failed = 0;

	setlocale(LC_ALL, "");
	bindtextdomain(PACKAGE, LOCALEDIR);
	textdomain(PACKAGE);

	if (argc <= 1)
	{
		help();
		return 0;
	}

	progname = argv[0];
	ext = xstrdup(".txt");
	binary = 0;
	write_stdout = 0;

	while ((opt = getopt(argc, argv, "?HhBbE:e:Ss")) != -1)
	{
		switch (opt)
		{
		case '?':
		case 'H':
		case 'h':	
			help();
			break;
		case 'b':
		case 'B':
			binary = 1;
			break;
		case 'E':	
		case 'e':
			xfree(ext);
			ext = xstrdup(optarg);
			break;
		case 's':
		case 'S':
			write_stdout = 1;
			break;
		}
	}

	fiad_set_logger(my_fiad_logger);

	while (argv[optind])
	{
		if (convert_file(argv[optind]) != OS_NOERR)
			failed = 1;
		optind++;
	}

	return failed;
}

