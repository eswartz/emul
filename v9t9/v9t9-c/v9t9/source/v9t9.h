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
  $Id$
 */

#ifndef __V9t9_H__
#define __V9t9_H__

//#include "OSLib.h"

#include "centry.h"

extern struct OSPathSpec v9t9_datadir;
extern struct OSSpec v9t9_progspec;

// frontend must set these up
extern int v9t9_argc;
extern char **v9t9_argv;

extern char *sessionspath, *configspath;

//	call to set up globals
int		v9t9_config(int argc, char **argv);
//	call after system init
int		v9t9_init(void);

int		v9t9_restart(void);
void	v9t9_restop(void);

//	returns em_xxx flag
int		v9t9_execute(void);
void	v9t9_term(int exitcode);

// terminate via SIGINT (ctrl-c, etc)
void 	v9t9_sigint(int exitcode);
// terminate via SIGTERM (OS shutdown)
void 	v9t9_sigterm(int exitcode);

#include "cexit.h"

#endif
