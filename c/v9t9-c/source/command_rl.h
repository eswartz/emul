/*
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

#ifndef __COMMAND_RL_H__
#define __COMMAND_RL_H__

#include <stdio.h>

#include "centry.h"

void	readline_getcommands(FILE *in, FILE *out);
void	readline_getcommand(FILE *in, FILE *out);
char **	readline_completion(const char *text, int start, int end);

#include "cexit.h"

#endif
