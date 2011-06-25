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

#ifndef __MODULEDB_H__
#define __MODULEDB_H__

/*	Flags for DefineModule */
enum
{
	MOD_PART_GROM	= 1,	// GROM at >6000 of an indefinite size
	MOD_PART_ROM	= 2,	// ROM at >6000 of size >2000
	MOD_PART_BANKED	= 4,	// like ROM, but two banks switched through 
							// write to >6000 or >6002
	MOD_PART_MINIMEM= 8,	// Mini-Memory RAM at >7000 of size >1000

	MOD_PART_GRAMKRACKER = 16 // all segments are in GRAM KRACKER format
};

/*	internal module database entry */
typedef struct ModuleEntry {
	char	*name;		/* screen name */
	char    *tag;		/* abbreviation for module */
	char	*commands;	/* commands to define/load memory */
	struct ModuleEntry *next;	/* next entry in database */
} ModuleEntry;

extern ModuleEntry *moddb, *loaded_module;

int		modules_init(void);
int		modules_init_db(const char *path, const char *dbfilename);
int 	module_load(ModuleEntry *ent);
int		module_load_by_name(const char *tag);

#endif
