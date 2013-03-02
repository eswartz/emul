/*
  moduledb.c					-- database of memory segments

  Rename me!

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

/*
	This module provides a layer of abstraction to the memory.c
	module by defining a way for the user to redefine parts of
	the 99/4A memory map (usually by filling them in with binary
	images).

	"Module" here means, basically, any >2000 byte area of ROM or RAM,
	GROM or GRAM, including console ROMs and GROMs, volatile and
	non-volatile RAM.  
*/

#include "v9t9_common.h"
#include "memory.h"
#include "command.h"
#include "configfile.h"
#include "moduledb.h"

#define _L	 LOG_ROMS

ModuleEntry *moddb;

ModuleEntry *loaded_module;	/* last successful module loaded */

/*	Free entries in modules database */
static void 
modules_free_db(void)
{
	ModuleEntry *lst = moddb;

	while (lst) {
		ModuleEntry *nxt = lst->next;

		if (lst->name)
			xfree(lst->name);
		if (lst->tag)
			xfree(lst->tag);
		if (lst->commands)
			xfree(lst->commands);
		xfree(lst);
		lst = nxt;
	}

	moddb = NULL;
}

/*	Initialize modules database */
int
modules_init_db(const char *path, const char *dbfilename)
{
	loaded_module = NULL;
	modules_free_db();
	return config_load_file(path, dbfilename, false /*session*/);
}

static ModuleEntry *
modules_find_tag_in_db(const char *tag)
{
	ModuleEntry *lst = moddb;

	while (lst && strcasecmp(lst->tag, tag))
		lst = lst->next;
	return lst;
}

static ModuleEntry *
modules_search_str_in_db(char *str)
{
	ModuleEntry *lst = moddb;

	while (lst && stristr(lst->name, str) == NULL)
		lst = lst->next;
	return lst;
}

static void
modules_add_to_db(ModuleEntry * nw)
{
	ModuleEntry **lst = &moddb;

	while (*lst && strcasecmp((*lst)->tag, nw->tag))
		lst = &(*lst)->next;
	if (*lst)
		nw->next = (*lst)->next;
	else
		nw->next = NULL;

	*lst = nw;
}

/*	Free modules loaded in memory  */
static int
module_unload_entries(void)
{
	MemoryEntry *lst = mementlist, *prev = 0L;

	while (lst) {
		if (lst->flags & MEMENT_CART) {
			memory_unmap_entry(lst);
			memset(lst->memact.areamemory, 0, lst->realsize);
			lst = memory_remove_entry_from_list(prev, lst);
		} else {
			prev = lst;
			lst = lst->next;
		}
	}
	memory_module_bank_handlers[0] = memory_module_bank_handlers[1] = NULL;
	loaded_module = NULL;
	return 1;
}

/*	Free module loaded in memory  */
static int
module_unload_entry(int mement)
{
	MemoryEntry *lst = mementlist, *prev = 0L;

	while (lst) {
		if ((lst->flags & MEMENT_CART) &&
			(lst->flags & mement) == mement) 
		{
			memory_unmap_entry(lst);
			memset(lst->memact.areamemory, 0, lst->realsize);
			lst = memory_remove_entry_from_list(prev, lst);
		} else {
			prev = lst;
			lst = lst->next;
		}
	}
	if (mement & MEMENT_BANKING) {
		memory_module_bank_handlers[0] = memory_module_bank_handlers[1] = NULL;
	}
	return 1;
}

static int module_change_entry(const char *tag, const char *name, const char *base, const char *cmdbuf)
{	
	ModuleEntry *nw, *it;

	it = modules_find_tag_in_db(tag);
	nw = 0L;

	// replace existing entry?
	if (it) {
		xfree(it->tag);
		xfree(it->name);
		xfree(it->commands);
	} else {
		nw = it = (ModuleEntry *) xmalloc(sizeof(ModuleEntry));
	}

	it->name = xstrdup(name);
	it->tag = xstrdup(tag);
	it->commands = (char *)xmalloc(strlen(cmdbuf)+2);
	sprintf(it->commands, "%s\n", cmdbuf);

	if (nw) {
		modules_add_to_db(nw);
	}
	return 1;
}

static char *
appendstr(char *strbuf, char *format, ...)
{
	char linebuf[1024];
	char *buf;

	va_list va;
	va_start(va, format);
	vsnprintf(linebuf, sizeof(linebuf), format, va);
	va_end(va);

	buf = (char *)xmalloc((strbuf ? strlen(strbuf) : 0) + strlen(linebuf) + 1);
	if (strbuf) strcpy(buf, strbuf); else *buf = 0;
	strcat(buf, linebuf);

	if (strbuf) xfree(strbuf);
	return buf;
}

static bool 
module_find_gram_kracker_segment(char *namebuf, OSSpec *spec, 
								 gram_kracker_header *header,
								 bool *had_rom_segment, bool *is_banked,
								 char **cmdptr)
{
	if (data_find_gram_kracker(modulespath, namebuf, 
							   spec, header))
	{	
		if (header->gk_type == GK_TYPE_ROM_2 || *had_rom_segment)
		{
			// assume so... we go backwards through the types,
			// so we could see #2 before #1
			*is_banked = true;
			*had_rom_segment = true;
		}

		// note the extra space, this is to patch the string later
		*cmdptr = appendstr(*cmdptr, "; DefineMemory \"RM%s\"  0x%04x 0x%04x "
						   "\"%s\" %d \"GRAM Kracker segment type %d\"",
						   header->gk_type == GK_TYPE_ROM_1 ? 
							(*is_banked ? "C1" : "C") :
							header->gk_type == GK_TYPE_ROM_2 ? "C2" 
							: "G",
						   header->address,
						   header->length,
						   namebuf, 
						   header->absolute_image_file_offset,
						   header->gk_type);
		return true;
	}
	else
	{
		return false;
	}
}

/*
 *	Search for the pieces of a GRAM Kracker file.
 *
 *	"cmdptr" is a pointer to a string filled in with commands used
 *		to load the module later.
 *	"base" is the base filename for all the GRAM kracker files.
 *		This may be "foo" where "foo", "foo1", "foo2", ... exist,
 *		or "foo%dbar" where %d is replaced with ""/"0"/"1", "2", ...
 */
static char *
module_lookup_gram_kracker(char *cmdptr, const char *base)
{
	char *orig = cmdptr;
	char namebuf[OS_NAMESIZE];
	OSSpec spec;
	int part;
	gram_kracker_header header;
	bool had_rom_segment = false, is_banked = false;

	const char *percent, *comma;

	/* assume 'base' is a printf-style pattern wherein we
	   can substitute 'part'. */
	percent = strstr(base, "%d");
	if (percent) {
		if (strstr(percent+1, "%d")) {
			command_logger(LOG_ERROR|LOG_USER, 
				   _("GRAMKRACKER base filename should have at most one '%%d' (got '%s')\n"),
				   base);
			return 0L;
		}
	}

	/* or, if 'base' has commas in it, we take these to be the actual names */
	comma = strchr(base, ',');
	if (comma && percent) {
		command_logger(_L|LOG_ERROR|LOG_USER, 
			   _("GRAMKRACKER base filename cannot have both a ',' and '%' (got '%s')\n"),
			   base);
		return 0L;
	}

	/* given a list of files? */
	if (comma) {
		const char *start = base;
		bool got_any = false;
		do
		{
			strncpy(namebuf, start, comma - start);
			namebuf[comma - start] = 0;

			if (module_find_gram_kracker_segment(namebuf, &spec, &header,
												 &had_rom_segment, &is_banked,
												 &cmdptr))
			{
				got_any = true;

				// no, the user may be loading them out of order
//				if (!header.more_to_load) {
//					return cmdptr;
//				}
			}
			else
			{
				command_logger(_L|LOG_ERROR|LOG_USER, _("Could not find GRAM Kracker segment '%s' (in '%s')\n"),
					   namebuf, base);

				if (!got_any) 
					return 0L;
			}

			if (!*comma)
				break;
			start = comma + 1;
			comma = strchr(start, ',');
			if (!comma)	
				comma = start + strlen(start);
		} while (1);

		return cmdptr;
	}

	/* iterate 0 through n finding these files named
			 say, 'base', 'base2', ... or
			'base0', 'base1', ... or
			'base1', 'base2' */
	else {
		for (part = -1; part < 10; part++) {
			if (part >= 0)
				if (percent) 
					snprintf(namebuf, sizeof(namebuf), base, part);
				else {
					// if there's no '%d', append number to end
					strncpy(namebuf, base, sizeof(namebuf));
					snprintf(namebuf + strlen(namebuf), 
							 sizeof(namebuf) - strlen(namebuf),
							 "%d", part);
				}
			else {
				// remove '%d' for the -1 iteration ('base%d' -> 'base')
				if (percent) {
					strcpy(namebuf, base);
					memmove((char *)((percent - base) + namebuf), 
							((percent - base) + namebuf + 2), 
							strlen(percent + 2) + 1);
				} else {
					strcpy(namebuf, base);
				}
			}

//			g_print("namebuf=%s\n", namebuf);
			if (module_find_gram_kracker_segment(namebuf, &spec, &header,
												 &had_rom_segment, &is_banked,
												 &cmdptr))
			{
				if (!header.more_to_load)
					return cmdptr;
			}
			else if (part > 1)
			{
				command_logger(_L|LOG_USER|LOG_ERROR, _("Could not find expected remaining GRAM Kracker segments\n"
					   "numbered starting at %d"), part);
				return cmdptr;
			}
			else if (part == 1 && orig == cmdptr)
			{
				command_logger(_L|LOG_USER|LOG_ERROR, _("Could not find first segment of GRAM Kracker file with base '%s'\n"),
					   base);
				return 0L;
			}	
		}
		return cmdptr;
	}
}

static
DECL_SYMBOL_ACTION(module_define_entry)
{
	char       *tag, *name, *base;
	int         parts;
	char		*cmdptr = 0L, *retstr;
	int			ret;

	command_arg_get_string(SYM_ARG_1st, &tag);
	command_arg_get_string(SYM_ARG_2nd, &name);
	command_arg_get_string(SYM_ARG_3rd, &base);
	command_arg_get_num(SYM_ARG_4th, &parts);

	/* Define commands that will load this module */

	cmdptr = appendstr(cmdptr, "UnloadModule");

	if (parts & MOD_PART_GRAMKRACKER) {
		/* Do the work to find all the parts */

		retstr = module_lookup_gram_kracker(cmdptr, base);
		if (!retstr) {
			xfree(cmdptr);
			return 0;
		}
		cmdptr = retstr;
	}

	if (parts & MOD_PART_GROM) {
		cmdptr = appendstr(cmdptr, "; DefineMemory \"RMG\" 0x6000 0x0000 "
						  "\"%sg.bin\" 0x0 \"GROM for %s\"",
						  base, name);
	}

	if (parts & MOD_PART_BANKED) {
		cmdptr = appendstr(cmdptr, "; DefineMemory \"RM1C\" 0x6000 0x2000 "
						  "\"%sc.bin\" 0x0 \"Bank 1 for %s\"",
					  base, name);

		cmdptr = appendstr(cmdptr, "; DefineMemory \"RM2C\" 0x6000 0x2000 "
						  "\"%sd.bin\" 0x0 \"Bank 2 for %s\"",
						  base, name);

	} else if (parts & MOD_PART_ROM) {
		cmdptr = appendstr(cmdptr, "; DefineMemory \"RMC\" 0x6000 0x2000 "
						  "\"%sc.bin\" 0x0 \"ROM for %s\"",
					  base, name);
	}

	if (parts & MOD_PART_MINIMEM) {
		cmdptr = appendstr(cmdptr, "; DefineMemory \"SMC\" 0x7000 0x1000 "
						  "\"%sr.bin\" 0x0 \"Mini-Memory RAM for %s\"",
						  base, name);
	}

	ret = module_change_entry(tag, name, base, cmdptr);
	xfree(cmdptr);

	return ret;
}

static
DECL_SYMBOL_ACTION(module_define_entry_memory)
{
	char       *tag, *name, *base;
	char		*commands;

	command_arg_get_string(SYM_ARG_1st, &tag);
	command_arg_get_string(SYM_ARG_2nd, &name);
	command_arg_get_string(SYM_ARG_3rd, &base);
	command_arg_get_string(SYM_ARG_4th, &commands);

	return module_change_entry(tag, name, base, commands);
}

static
DECL_SYMBOL_ACTION(modules_list_db)
{
	ModuleEntry *lst = moddb;

	logger(LOG_USER, _("List of installed modules:"));
	while (lst) {
		logger(LOG_USER, _("Tag: '%s', Name: '%s'\n"), lst->tag, lst->name);
		logger(LOG_USER, _("\tCommands:  '%s'\n"), lst->commands);
		lst = lst->next;
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(modules_clear_db)
{
	loaded_module = NULL;
	modules_free_db();
	return 1;
}

int module_load(ModuleEntry *ent)
{
	int ret;
	my_assert(ent->commands);
	logger(_L | LOG_USER, _("Loading module '%s'\n"), ent->name);
	logger(_L | L_1, "\t%s", ent->commands);
	ret = command_exec_text(ent->commands);
	if (ret) loaded_module = ent;
	return ret;
}

static
DECL_SYMBOL_ACTION(load_module_by_name)
{
	ModuleEntry *ent;
	char       *str;

	if (task == csa_READ) {  
		if (!iter && loaded_module) {
			command_arg_set_string(sym->args, loaded_module->name);
			return 1;
		}
		return 0;
	}

	command_arg_get_string(sym->args, &str);
	if (!str || !*str)
	{
		module_unload_entries();
		return 1;
	}

	ent = modules_find_tag_in_db(str);
	if (ent == NULL)
		ent = modules_search_str_in_db(str);

	if (ent == NULL) {
		command_logger(_L | LOG_ERROR | LOG_USER, _("No module matches '%s'\n"), str);
		return 0;
	}

	return module_load(ent);
}

static
DECL_SYMBOL_ACTION(load_module_by_name_and_reset)
{
	if (load_module_by_name(sym, task, 0))
		return command_exec_text("ResetComputer\n");
	else
		return 0;
}

static
DECL_SYMBOL_ACTION(module_unload)
{
	return module_unload_entries();
}

static
DECL_SYMBOL_ACTION(module_unload_and_reset)
{
	if (module_unload_entries())
		return command_exec_text("ResetComputer\n");
	else
		return 0;
}


static
DECL_SYMBOL_ACTION(do_memory_ram_init)
{
	memory_ram_init();
	return 1;
}

static
DECL_SYMBOL_ACTION(do_memory_complete_load)
{
	return memory_complete_load();
}

static
DECL_SYMBOL_ACTION(do_memory_volatile_load)
{
	return memory_volatile_load();
}

static
DECL_SYMBOL_ACTION(do_memory_volatile_save)
{
	memory_volatile_save();
	return 1;
}

static
DECL_SYMBOL_ACTION(module_define_console_rom)
{
	char cmdbuf[1024];
	char *fname;
	command_arg_get_string(SYM_ARG_1st, &fname);
	if (!fname || !*fname)
		return 1;
	sprintf(cmdbuf, "DefineMemory \"RC\" 0x0000 (-0x2000) \"%s\" 0x0 \"Console ROM\"\n", fname);
	return command_exec_text(cmdbuf);
}

static
DECL_SYMBOL_ACTION(module_define_console_grom)
{
	char cmdbuf[1024];
	char *fname;
	command_arg_get_string(SYM_ARG_1st, &fname);
	if (!fname || !*fname)
		return 1;
	sprintf(cmdbuf, "DefineMemory \"RG\" 0x0000 (-0x6000) \"%s\" 0x0 \"Console GROM\"\n", fname);
	return command_exec_text(cmdbuf);
}

static
DECL_SYMBOL_ACTION(module_define_module_grom)
{
	char cmdbuf[1024];
	char *fname;

	if (task == csa_READ) {
		command_arg_get_string(SYM_ARG_1st, &fname);
		if (!fname || !*fname)
			return 0;
		else 
			return (iter == 0);
	}

	command_arg_get_string(SYM_ARG_1st, &fname);
	if (!fname || !*fname) {
		module_unload_entry(MEMENT_GRAPHICS);
		return 1;
	}
	sprintf(cmdbuf, "DefineMemory \"RMG\" 0x6000 (-0xA000) \"%s\" 0x0 \"Module GROM\"\n", fname);
	loaded_module = NULL;
	return command_exec_text(cmdbuf);
}

static
DECL_SYMBOL_ACTION(module_define_module_rom)
{
	char cmdbuf[1024];
	char *fname;

	if (task == csa_READ) {
		command_arg_get_string(SYM_ARG_1st, &fname);
		if (!fname || !*fname)
			return 0;
		else 
			return (iter == 0);
	}

	command_arg_get_string(SYM_ARG_1st, &fname);
	if (!fname || !*fname) {
		module_unload_entry(MEMENT_CONSOLE);
		return 1;
	}
	sprintf(cmdbuf, "DefineMemory \"RMC\" 0x6000 (-0x2000) \"%s\" 0x0 \"Module ROM\"\n", fname);
	loaded_module = NULL;
	return command_exec_text(cmdbuf);
}

static
DECL_SYMBOL_ACTION(module_define_module_rom_1)
{
	char cmdbuf[1024];
	char *fname;

	if (task == csa_READ) {
		command_arg_get_string(SYM_ARG_1st, &fname);
		if (!fname || !*fname)
			return 0;
		else 
			return (iter == 0);
	}

	command_arg_get_string(SYM_ARG_1st, &fname);
	if (!fname || !*fname) {
		module_unload_entry(MEMENT_BANK_1 | MEMENT_CONSOLE);
		return 1;
	}
	sprintf(cmdbuf, "DefineMemory \"RM1C\" 0x6000 (-0x2000) \"%s\" 0x0 \"Module ROM bank 1\"\n", fname);
	loaded_module = NULL;
	return command_exec_text(cmdbuf);
}

static
DECL_SYMBOL_ACTION(module_define_module_rom_2)
{
	char cmdbuf[1024];
	char *fname;

	if (task == csa_READ) {
		command_arg_get_string(SYM_ARG_1st, &fname);
		if (!fname || !*fname)
			return 0;
		else 
			return (iter == 0);
	}

	command_arg_get_string(SYM_ARG_1st, &fname);
	if (!fname || !*fname) {
		module_unload_entry(MEMENT_BANK_2 | MEMENT_CONSOLE);
		return 1;
	}
	sprintf(cmdbuf, "DefineMemory \"RM2C\" 0x6000 (-0x2000) \"%s\" 0x0 \"Module ROM bank 2\"\n", fname);
	loaded_module = NULL;
	return command_exec_text(cmdbuf);
}

static
DECL_SYMBOL_ACTION(module_set_module_bank)
{
	int val;
	command_arg_get_num(SYM_ARG_1st, &val);
	if (val < 0 || val > 1) {
		command_logger(_L|LOG_ERROR|LOG_USER, _("memory bank must be 0 or 1 (got %d)\n"), val);
		return 0;
	}
	memory_set_module_bank(val);
	return 1;
}

int
modules_init(void)
{
	command_symbol_table *modulecommands =
		command_symbol_table_new(_("Memory Map / ROM / RAM / Module Options"),
								 _("These are commands for dealing with the layout of memory in the virtual 99/4A"),


#if 0
	  command_symbol_new
         ("SystemModulesPath",
		  _("Set secondary directory list to search for module ROM images"),
		  c_STATIC|c_CONFIG_ONLY,
		  NULL /* action*/,
		  RET_FIRST_ARG,
		  command_arg_new_string
		    (_("path"),
			 _("list of directories "
			 "separated by one of these characters: '"
			 OS_ENVSEPLIST "'"),
			 NULL	/* action */,
			 NEW_ARG_STRBUF(&systemmodulespath),
			 NULL /* next */ )
		  ,

	  command_symbol_new
		 ("SystemROMSPath",
		  _("Set secondary directory list to search for console ROM and GROM images"),
		  c_STATIC|c_CONFIG_ONLY,
		  NULL /* action*/,
		  RET_FIRST_ARG,
		  command_arg_new_string
		    (_("path"),
			 _("list of directories "
			 "separated by one of these characters: '"
			 OS_ENVSEPLIST
			 "'"),
			 NULL	/* action */,
			 NEW_ARG_STRBUF(&systemromspath),
			 NULL /* next */ )
		  ,

	  command_symbol_new
		 ("SystemRAMSPath",
		  _("Set secondary directory list to search for nonvolatile RAM images"),
		  c_STATIC|c_CONFIG_ONLY,
		  NULL /* action*/,
		  RET_FIRST_ARG,
		  command_arg_new_string
		    (_("path"),
			 _("list of directories "
			 "separated by one of these characters: '"
			 OS_ENVSEPLIST
			 "'"),
			 NULL	/* action */,
			 NEW_ARG_STRBUF(&systemramspath),
			 NULL /* next */ )
		  ,
#endif

	  command_symbol_new
         ("ModulesPath||SystemModulesPath",
		  _("Set initial directory list to search for module ROM images"),
		  c_STATIC|c_CONFIG_ONLY,
		  NULL /* action*/,
		  RET_FIRST_ARG,
		  command_arg_new_string
		    (_("path"),
			 _("list of directories "
			 "separated by one of these characters: '"
			 OS_ENVSEPLIST "'"),
			 NULL	/* action */,
			 NEW_ARG_STRBUF(&modulespath),
			 NULL /* next */ )
		  ,

	  command_symbol_new
		 ("ROMSPath||SystemROMSPath",
		  _("Set initial directory list to search for console ROM and GROM images"),
		  c_STATIC|c_CONFIG_ONLY,
		  NULL /* action*/,
		  RET_FIRST_ARG,
		  command_arg_new_string
		    (_("path"),
			 _("list of directories "
			 "separated by one of these characters: '"
			 OS_ENVSEPLIST
			 "'"),
			 NULL	/* action */,
			 NEW_ARG_STRBUF(&romspath),
			 NULL /* next */ )
		  ,

	  command_symbol_new
		 ("RAMSPath||SystemRAMSPath",
		  _("Set initial directory list to search for nonvolatile RAM images"),
		  c_STATIC|c_CONFIG_ONLY,
		  NULL /* action*/,
		  RET_FIRST_ARG,
		  command_arg_new_string
		    (_("path"),
			 _("list of directories "
			 "separated by one of these characters: '"
			 OS_ENVSEPLIST
			 "'"),
			 NULL	/* action */,
			 NEW_ARG_STRBUF(&ramspath),
			 NULL /* next */ )
		  ,

	  command_symbol_new
		("DefineMemory",
		 _("Specify existence of a memory area"),
		 c_DYNAMIC|c_SESSION_ONLY,
		 memory_define_entry	/* action */,
		 NULL /* ret */,
		 command_arg_new_string
		   (_("flags"),
			_("string of characters defining memory characteristics:\n"
			"first, 'R' for ROM, 'W' for RAM, and 'S' for stored RAM;\n"
			"then, 'M' for a module, or nothing;\n"
			"then, '1' or '2' for banks of a banked module, or nothing;\n"
			"then, one of 'C'onsole, 'G'raphics, 'V'ideo, 'S'peech"),
			NULL /* action */,
			NEW_ARG_STR(32),

		 command_arg_new_num
		   (_("address"),
			_("starting address of image"),
			NULL /* action */,
			NEW_ARG_NUM(u16),
		 command_arg_new_num
			(_("size"),
			 _("size of ROM in bytes; "
			 "except for 0, which indicates an unknown size, "
			 "and a negative number (enclose in parentheses!), "
			   "which indicates the magnitude "
			   "of the maximum size allowed "),
			 NULL /* action */,
			 NEW_ARG_NUM(u32),
		 command_arg_new_string
		   (_("file"),
			_("name of binary image to load and/or store, "
			"searched in the ROMSPath or ModulesPath; "
			"if blank, memory is read as zeroes"),
			NULL /* action */ ,
			NEW_ARG_NEW_STRBUF,
		 command_arg_new_num
		   (_("offset"),
			_("byte offset of image, if stored in larger file"),
			NULL /* action */,
			NEW_ARG_NUM(u32),
		 command_arg_new_string
		   (_("name"),
			_("text name of memory area"),
			NULL /* action */ ,
			NEW_ARG_NEW_STRBUF,
			NULL	/* next */))))))
	 ,

	  command_symbol_new
		("DefaultMemoryMap",
		 _("Setup defaults for a 99/4A memory map"),
		 c_DONT_SAVE,
		 memory_default_list /* action */ ,
		 NULL /* ret */,
		 NULL /* args */
	 ,	
		 
	  command_symbol_new
		("MemoryExpansion32K",
		 _("Use 32K expansion memory (may be overridden by a DefineRAM command)"),
		 c_STATIC,
		 do_memory_ram_init /* action */ ,
		 RET_FIRST_ARG,
		 command_arg_new_enum
		    ("off|on", 
		    _("toggle"),
			 NULL /* action */ ,
			 ARG_NUM(isexpram),
			 NULL /* next */ )
		 ,	

	  command_symbol_new
		   ("ExtraConsoleRAM",
			_("Set up >8000->82FF range as real RAM (like in the Geneve) "
			"instead of mirroring >8300->83FF (the default)"),
			c_STATIC,
			do_memory_ram_init /* action */ ,
			RET_FIRST_ARG,
			command_arg_new_enum
			  ("off|on", _("toggle"),
			   NULL /* action */ ,
			   ARG_NUM(isenhconsoleram),
			   NULL /* next */ )
		,

	  command_symbol_new
		  ("ConsoleROMFileName",
		   _("Name of console ROM which starts at address >0000"),
		   c_DONT_SAVE/*|c_SESSION_ONLY*/,
		   module_define_console_rom /* action */,
		   RET_FIRST_ARG,
		   command_arg_new_string
		     (_("file"),
			  _("name of binary image"),
			  NULL /* action */ ,
			  NEW_ARG_NEW_STRBUF,
			  NULL	/* next */)
	   ,

	  command_symbol_new
		  ("ConsoleGROMFileName",
		   _("Name of console GROM which starts at address G>0000"),
		   c_DONT_SAVE/*|c_SESSION_ONLY*/,
		   module_define_console_grom /* action */,
		   RET_FIRST_ARG,
		   command_arg_new_string
		     (_("file"),
			  _("name of binary image"),
			  NULL  /* action */ ,
			  NEW_ARG_NEW_STRBUF,
			  NULL	/* next */ )
	   ,

	  command_symbol_new
		  ("ModuleGROMFileName",
		   _("Name of module GROM which starts at address G>6000"),
		   c_DYNAMIC|c_DONT_SAVE /*c_SESSION_ONLY*/,
		   module_define_module_grom /* action */,
		   RET_FIRST_ARG,
		   command_arg_new_string
		     (_("file"),
			  _("name of binary image"),
			  NULL  /* action */ ,
			  NEW_ARG_NEW_STRBUF,
			  NULL	/* next */ )
	   ,

	  command_symbol_new
		  ("ModuleROMFileName|ModuleROM",
		   _("Name of module ROM (non-banked) which starts at CPU address >6000"),
		   c_DYNAMIC|c_DONT_SAVE /*SESSION_ONLY*/,
		   module_define_module_rom  /* action */ ,
		   RET_FIRST_ARG,
		   command_arg_new_string
			 (_("file"),
			  _("name of binary image"),
			  NULL /* action */,
			 NEW_ARG_NEW_STRBUF,
			 NULL /* next */ )
	   ,

	  command_symbol_new
		  ("ModuleROM1FileName|ModuleROM1",
		   _("Name of module ROM (first bank) which starts at CPU address >6000"),
		   c_DYNAMIC|c_DONT_SAVE /*c_SESSION_ONLY*/,
		   module_define_module_rom_1  /* action */ ,
		   RET_FIRST_ARG,
		   command_arg_new_string
			 (_("file"),
			  _("name of binary image"),
			  NULL /* action */,
			 NEW_ARG_NEW_STRBUF,
			 NULL /* next */ )
	   ,

	  command_symbol_new
		  ("ModuleROMBank2FileName|ModuleROM2FileName",
		   _("Name of module ROM (second bank) which starts at CPU address >6000"),
		   c_DYNAMIC|c_DONT_SAVE /*c_SESSION_ONLY*/,
		   module_define_module_rom_2 /* action */,
		   RET_FIRST_ARG,
		   command_arg_new_string
			 (_("file"),
			  _("name of binary image"),
			  NULL  /* action */,
			  NEW_ARG_NEW_STRBUF,
			  NULL /* next */)
	   ,

	  command_symbol_new
		  ("LoadAllMemory",
		   _("Load all memory images (ROMs and RAMs) into the emulator"),
		   c_DONT_SAVE,
		   do_memory_complete_load,
		   NULL  /* ret */ ,
		   NULL	/* args */
	   ,

	  command_symbol_new
		  ("LoadMemory",
		   _("Load the volatile memory images (RAMs) into the emulator"),
		   c_DONT_SAVE,
		   do_memory_volatile_load,
		   NULL  /* ret */ ,
		   NULL	/* args */
	   ,

	  command_symbol_new
		  ("SaveMemory",
		   _("Save the volatile memory images (RAMs) to disk"),
		   c_DONT_SAVE,
		   do_memory_volatile_save,
		   NULL  /* ret */ ,
		   NULL	/* args */
	   ,

	  command_symbol_new
			("ListMemory",
			 _("List memory map"),
			 c_DONT_SAVE,
			 memory_dump,
			 NULL /* ret */ ,
			 NULL	/* args */
	   ,

	  command_symbol_new
		   ("DefineModule",
			_("Define or redefine a standard module/cartridge in the database"),
			c_DONT_SAVE,
			module_define_entry,
			NULL /* ret */ ,
			command_arg_new_string
			 	(_("tag"),
				 _("short tag for easy reference"),
				 NULL /* action */ ,
				 NEW_ARG_STR(5),
		    command_arg_new_string
				 (_("name"),
				  _("full name of module"),
				  NULL /* action */ ,
				  NEW_ARG_STR(64),
			command_arg_new_string
				  (_("base"),
				   _("base of module file name"),
				   NULL /* action */ ,
				   NEW_ARG_STR(64),
			command_arg_new_enum
				   (_("GROM=1|ROM=2|BANKED=4|MINIMEM=8|GRAMKRACKER=16"),
					_("sections present (sum of one or more "
				        "of GROM, ROM, BANKED, MINIMEM)"),
					NULL /* action */ ,
					NEW_ARG_NUM(long),
					NULL /* next */ ))))
			,

	  command_symbol_new
		   ("DefineModuleMemory",
			_("Define or redefine a module/cartridge in the database, "
			"giving commands to define its memory configuration"),
			c_DONT_SAVE,
			module_define_entry_memory,
			NULL /* ret */,
			command_arg_new_string
				(_("tag"),
				 _("short tag for easy reference"),
				 NULL /* action */ ,
				 NEW_ARG_STR(5),
			command_arg_new_string
				 (_("name"),
				  _("full name of module"),
				  NULL /* action */ ,
				  NEW_ARG_STR(64),
			command_arg_new_string
				  (_("base"),
				   _("base of module file name"),
				   NULL /* action */ ,
				   NEW_ARG_STR(64),
			command_arg_new_string
				   (_("commands"),
					_("commands used to define module memory map, e.g., "
					"'DefineMemory \"RWMC\" 0x6000 0x2000 \"module_rom.bin\" 0x0 \"Module ROM file\"; "
					"DefineMemory \"RMG\" 0x6000 0x6000 \"module_grom.bin\" 0x0 \"Module GRAM file\"'"),
					NULL /* action */ ,
					NEW_ARG_NEW_STRBUF,
					NULL /* next */ ))))
			,

	  command_symbol_new
			("ListModules",
			 _("List modules in database"),
			 c_DONT_SAVE,
			 modules_list_db,
			 NULL /* ret */ ,
			 NULL /* args */
	   ,

	  command_symbol_new
			("InitModuleDatabase",
			 _("Initialize current module list to empty "
			 "(use 'ReadModuleDatabase <file>' or LoadConfigFile <file>' "
			 "to add entries)"),
			 c_DONT_SAVE,
			 modules_clear_db,
			 NULL /* ret */ ,
			 NULL /* args */
	   ,

	  command_symbol_new
			 ("UnloadModule||UnloadModuleOnly",
			  _("Unload currently loaded module(s); you may want to 'ResetComputer' next!"),
			  c_DONT_SAVE,
			  module_unload,
			  NULL /* ret */,
			  NULL /* args */
	   ,

	  command_symbol_new
			 ("LoadModule||LoadModuleOnly",
			  _("Load a module by tag or name"),
			  c_DONT_SAVE,
			  load_module_by_name_and_reset,
			  NULL /* ret */ ,
			  command_arg_new_string
			      (_("tag|name"),
				  _("tag or title substring"),
				  NULL /* action */ ,
				  NEW_ARG_STR(64),
				   NULL /* next */ )
	  ,

	  command_symbol_new
			  ("ReplaceModule",
			   _("Replace current module but do not reset computer"),
			   c_DYNAMIC|c_SESSION_ONLY,
			   load_module_by_name,
			   NULL /* ret */ ,
			   command_arg_new_string
				   (_("tag|name"),
					_("tag or title substring"),
					NULL /* action */ ,
					NEW_ARG_STR(64),
					NULL /* next */ )
	  ,

	  command_symbol_new
			  ("ChangeModuleBank",
			   _("Change active ROM bank of module (only applies to banked modules)"),
			   c_STATIC|c_SESSION_ONLY,
			   module_set_module_bank,
			   RET_FIRST_ARG /* ret */ ,
			   command_arg_new_num
				   (_("bank"),
					_("0 or 1"),
					NULL /* action */ ,
					ARG_NUM(memory_module_bank),
					NULL /* next */ )
	  ,

	   NULL /* next */ )))))))))))))))))))))))))/*)))*/,

	  NULL /* sub */ ,

	  NULL	/* next */
);

	command_symbol_table_add_subtable(universe, modulecommands);

	modulespath = xstrdup(OS_CWDSTR);
	ramspath = xstrdup(OS_CWDSTR);
	romspath = xstrdup(OS_CWDSTR);

	return 1;
}
