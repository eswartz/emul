/*
  configfile.h						-- header for configuration utilities

  (see config.h for ./configure generated header)

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

#ifndef __CONFIG_H__
#define __CONFIG_H__

#include "command.h"

/*
 *	Escape an outgoing quoted string
 */
char *escape(const char *str, int quote);

DECL_SYMBOL_ACTION(load_config);
DECL_SYMBOL_ACTION(save_config);
DECL_SYMBOL_ACTION(load_session);
DECL_SYMBOL_ACTION(save_session);

int 
config_load_spec(const OSSpec *spec, bool session);
int 
config_save_spec(const OSSpec *spec, bool session);
int 
config_load_file(const char *path, const char *filename, bool session);
int 
config_save_file(const char *path, const char *filename, bool session);

#endif
