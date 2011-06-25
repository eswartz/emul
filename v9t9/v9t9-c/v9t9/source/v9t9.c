
/*
  V9t9.c					-- main common module

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

#define NEW_CONFIG 1

#define __V9t9__

#define _GNU_SOURCE
#include <getopt.h>

#include "v9t9_common.h"

#include "memory.h"
#include "9900.h"
#include "9901.h"
#include "vdp.h"
#include "video.h"
#include "sound.h"
#include "speech.h"
#include "emulate.h"
#include "keyboard.h"
#include "dsr.h"
#include "v9t9.h"
#include "timer.h"
#include "cru.h"
#include "moduledb.h"
#include "system.h"
#include "command.h"
#include "moduleconfig.h"
#include "debugger.h"
#include "configfile.h"
#ifdef EMU_DISK_DSR
#include "fiad.h"
#endif
#include "demo.h"
#include "compiler.h"

DECL_SYMBOL_ACTION(dump_config);
static
DECL_SYMBOL_ACTION(v9t9_die);

OSPathSpec  v9t9_datadir;
OSSpec		v9t9_progspec;
int			v9t9_argc;
char		**v9t9_argv;

int         v9t9_preconfiginit(void);

static	int         preconfiginit(void);
static	int         postconfiginit(void);
static	void        shutdown(void);

char *sessionspath, *configspath;

#include "v9t9_common.h"

static int  dying = 0;

static void
__die(void)
{
	v9t9_term(0);
}

void v9t9_sigint(int ignored)
{	
	stateflag |= ST_STOP | ST_TERMINATE;
}

void v9t9_sigterm(int ignored)
{
	TM_Stop();
	v9t9_restop();
	v9t9_term(ignored);
	exit(ignored);
}

#define _L	 LOG_INTERNAL | LOG_INFO

static void v9t9_help(void)
{
#if NEW_CONFIG
	logger(LOG_USER|LOG_ERROR,
		   _("\n"
		   "V9t9: TI Emulator! v7.0\n"
		   "\n"
		   "v9t9 {options} {file|commands}...\n"
		   "\n"
		   "-h:       display help\n"
		   "-c:       load alternate config file (default 'v9t9.cnf')\n"
		   "-m:       load alternate module database (default 'modules.inf' if\n"
		   "          'ReadModuleDatabase' command not present in config)\n"
		   "-r:       reboot instead of loading previous 'session.cnf'\n"
		   "-d <dir>: set v9t9 data directory to <dir>\n"
		   "          (default: '%s',\n"
		   "          '%s', or '.')\n"
		   "file |    try to load session file; if not found, execute as\n"
		   "commands: commands instead\n"
		   "\n"),
		   SHAREDIR, TOPSRCDIR);
#else
	logger(LOG_USER|LOG_ERROR,
		   _("\n"
		   "V9t9: TI Emulator! v7.0\n"
		   "\n"
		   "v9t9 {options} {file|commands}...\n"
		   "\n"
		   "-h:       display help\n"
		   "-c:       load alternate config file (default '~/.v9t9/v9t9.cnf')\n"
		   "-r:       reboot instead of loading previous 'session.cnf'\n"
		   "-d <dir>: set v9t9 data directory to <dir> (default '~/.v9t9')\n"
		   "file |    try to load session file; if not found, execute as\n"
		   "commands: commands instead\n"
			 "\n"));

#endif
}

int 
v9t9_config(int argc, char **argv)
{
	OSSpec spec;
	const char *dir;

	command_init();

	v9t9_argc = argc;
	v9t9_argv = argv;

	OS_InitProgram(&v9t9_argc, &v9t9_argv);

//	sessionspath = xstrdup(".");
//	configspath = xstrdup(".");
	xminit();

	OS_FindProgram(v9t9_argv[0], &v9t9_progspec);

#if NEW_CONFIG
	/* get config directory */
	if (
#if _WIN32
		(dir = getenv("USERPROFILE")) ||
#endif
		(dir = getenv("HOME"))
		)
	{
		if (OS_MakePathSpec(0L, dir, &v9t9_datadir) != OS_NOERR)
			dir = 0L;
		else
		{
			OSSpec spec;
			if (OS_MakeSpecWithPath(&v9t9_datadir, ".v9t9", !mswp_noRelative, &spec) == OS_NOERR)
			{
				if (OS_Status(&spec) != OS_NOERR)
				{
					OS_Mkdir(&spec);
					OS_MakeSpecWithPath(&v9t9_datadir, ".v9t9", !mswp_noRelative, &spec);
				}
				v9t9_datadir = spec.path;
			}
		}
	}
	if (!dir)
	{
		logger(_L|LOG_USER, _("could not locate home directory, using current directory\n"));
		OS_GetCWD(&v9t9_datadir);
	}

#else
	/* search for landmark of v9t9 installation */
	if (OS_MakeSpecWithPath(&v9t9_progspec.path, "../share/v9t9/v9t9.gtkrc", 
							false, &spec) == OS_NOERR
		&& OS_Status(&spec) == OS_NOERR) 
	{
		v9t9_datadir = spec.path;
	}
	else if (OS_MakeSpecWithPath(&v9t9_progspec.path, "../v9t9.gtkrc", 
								 false, &spec) == OS_NOERR
			 && OS_Status(&spec) == OS_NOERR) 
	{
		v9t9_datadir = spec.path;
	}
	else if (OS_MakePathSpec(0L, SHAREDIR, &v9t9_datadir) != OS_NOERR) {
		if (OS_MakePathSpec(0L, TOPSRCDIR, &v9t9_datadir) != OS_NOERR) 
		{
			OS_GetCWD(&v9t9_datadir);
		}
	}
#endif

	return 1;
}

static bool filespec_has_extension(OSSpec *spec, const char *extension)
{
	const char *name, *ext;
	name = OS_NameSpecToString1(&spec->name);
	ext = strrchr(name, '.');
	return (ext && strcasecmp(ext, extension) == 0);
}

/*
 *	Parse arguments, get configuration files, etc.
 *
 *	When no arguments are specified, the config file is v9t9.cnf,
 *	and the modules database is modules.inf. 
 *	Any other file on the cmdline is taken to be a session file,
 *	which is loaded after config files are found.
 *
 *	If no modules database is read in the config file, modules.inf
 *	is read.
 */
static int get_args_and_configs(void)
{
	static struct option long_options[] =
	{
		{ "help", 			0, 0L, '?' },
		{ "config", 		1, 0L, 'c' },
#if !NEW_CONFIG
		{ "modules",		1, 0L, 'm' },
#endif
		{ "restart",		0, 0L, 'r' },
#if !NEW_CONFIG
		{ "install-path",	1, 0L, 'i' },
#endif
		{ "timeout",		1, 0L, 't' },
		{ 0, 0, 0, 0 }
	};

	int option_index = 0;
	int c;
	OSError err;

	int restart = 0;
	int loaded_config = 0;
	int loaded_session = 0;
	int loaded_modules = 0;
	int executed_command = 0;
	int timeout = 0;

	while ((c = getopt_long(v9t9_argc, v9t9_argv, "c:m:?rd:", 
							long_options, &option_index)) != -1)
	{
		switch (c)
		{
			/* set data directory in DataDir */
		case 'd':
			err = OS_MakePathSpec(0L, optarg, &v9t9_datadir);
			if (err != OS_NOERR) {
				logger(_L|LOG_ERROR|LOG_USER, 
					   _("directory '%s' not found\n"), optarg);
				return 0;
			}
			break;

			/* config file */
		case 'c':
			loaded_config = 1;
			if (!config_load_file(0L, optarg, false /*session*/)) {
				logger(_L|LOG_FATAL, _("config file '%s' not found\n"), optarg);
			}
			break;

#if !NEW_CONFIG
			/* modules db */
		case 'm':
			if (!loaded_modules) {
				// initialize and add
				loaded_modules = 1;
				if (!modules_init_db(0L, optarg)) {
					logger(LOG_FATAL, _("modules db '%s' not found\n"), optarg);
				}
			} else {
				// add modules
				if (!config_load_file(0L, optarg, false /*session*/)) {
					logger(LOG_FATAL, _("modules db '%s' not found\n"), optarg);
				}
			}
			break;
#endif

#if 0
			/* load session file */
		case 's':
			loaded_session = 1;
			if (!config_load_file(".", optarg, true /*session*/)) {
				logger(_L|LOG_FATAL, _("session file '%s' not found\n"), optarg);
			}
			break;
#endif

#if 0
			/* execute these commands */
		case 'e':
			executed_command = 1;
			if (restart) {
				logger(_L|LOG_USER|LOG_WARN, _("executing commands may have no apparent effect when '-r' is used;\n"
											   "specify '-e <cmds>' before '-r' if necessary\n"));
			}
			command_exec_text(optarg);
			break;
#endif

			/* restart instead of starting where session file says */
		case 'r':
			restart = 1;
			break;

		case 't':
			timeout = atoi(optarg);
			break;

			/* show help */
		case '?':
			v9t9_help();
			exit(0);
			break;
		}
	}

#if NEW_CONFIG

	/* load default config file and modules database */
	if (!loaded_config) {
		if (!config_load_file(OS_PathSpecToString1(&v9t9_datadir), "v9t9.cnf", false /*session*/)) {
			logger(_L|LOG_USER|LOG_FATAL, _("config file 'v9t9.cnf' not found\n"));
		}
	}

#else
	/* load default config file and modules database */
	if (!loaded_config) {
		if (!config_load_file(0L, "v9t9.cnf", false /*session*/)
			&& !config_load_file(OS_PathSpecToString1(&v9t9_progspec.path), "v9t9.cnf", false)) {
			logger(_L|LOG_USER|LOG_FATAL, _("default config file 'v9t9.cnf' not found\n"));
		}
	}

	if (moddb == NULL && !loaded_modules) {
		if (!modules_init_db(0L, "modules.inf") &&
			!modules_init_db(OS_PathSpecToString1(&v9t9_progspec.path), "modules.inf")) {
			logger(_L|LOG_ERROR|LOG_USER, _("default modules db 'modules.inf' not found\n"));
		}
	}
#endif

	/* load session file[s], demo, or execute commands */

	/* We do this here, instead of through options, since getopt()
	   doesn't seem to have a way to filter options, i.e., to look
	   at only a subset at one point, and at the rest, later. 
	   These types of files and commands need to be executed after
	   we have the config file loaded, for instance; it would not be
	   right to always load one config file before parsing the options,
	   and then read another, and then do a third thing.
	*/
	if (optind < v9t9_argc)
	{
		while (optind < v9t9_argc)
		{
			OSSpec spec;
			if (data_find_file(sessionspath, v9t9_argv[optind], &spec)
				&& filespec_has_extension(&spec, ".cnf"))
			{
				/* load session */
				loaded_session = 1;
				if (!config_load_spec(&spec, true /*session*/)) {
					logger(_L|LOG_FATAL, _("could not load session file '%s'\n"), 
						   v9t9_argv[optind]);
				}
			} 
			else if (data_find_file(demospath, v9t9_argv[optind], &spec)
				&& filespec_has_extension(&spec, ".dem"))
			{
				/* load demo */
				execution_pause(true);
				if (!demo_start_playback(&spec)) {
					logger(_L|LOG_FATAL, _("could not execute demo '%s'\n"),
						   OS_SpecToString1(&spec));
				}
			} 	
			else
			{
				executed_command = 1;
				if (!command_exec_text(v9t9_argv[optind]))
				{
					logger(_L|LOG_FATAL, _("could not execute commands '%s'\n"), 
						   v9t9_argv[optind]);
				}
			}
			optind++;
		}
	}

	/* load default session */
	if (!executed_command && !restart && !loaded_session) {
		if (!config_load_file(sessionspath, "session.cnf", true /*session*/)) {
			logger(_L|LOG_ERROR|LOG_USER, _("No 'session.cnf' found; restarting\n"));
			restart = 1;
		}
	}

	/* restart or continue? */
	if (restart) {
		command_exec_text("ResetComputer\n");
	} else {
		emulate_setup_for_restore();
	}

	/* set timeout timer */
	if (timeout)
		TM_SetEvent(TM_UniqueTag(), TM_HZ*100*timeout, 0, TM_FUNC, v9t9_die);
	return 1;
}

int        
v9t9_init(void)
{
/*
	int load_std_session = 1, do_reset = 0;
	int argc; char **argv;
	char *last_command;
*/
	log_add_commands();

	TM_Init();

	vdpinit();
	cruinit();
	if (!setup_9901())
		return 0;

	sound_init();
	modules_init();
	debugger_init();
	compiler_init();
#ifdef EMU_DISK_DSR
	fiad_set_logger(_logger);
#endif

	if (!preconfiginit())
		return 0;

	demo_init();

	vmClearUserFlagsOnModules();

	if (!postconfiginit())
		return 0;

	get_args_and_configs();

	atexit(__die);

	dying = 0;
	return 1;
}

int
v9t9_execute(void)
{
	int ret;

	if (demo_playing)
		ret = demo_playback_loop();
	else
		ret = vmCPU->m.cpu->execute();

	return ret;
}

void
v9t9_term(int exitcode)
{
	if (dying)
		return;

	dying = 1;

	TM_Stop();
	
	v9t9_restop();
	shutdown();

	termlog();
}

static int
preconfiginit(void)
{
	if (!vmModulesInit())
		return 0;

	if (!vmRegisterModules(installed_modules))
		return 0;

	vmAddModuleCommands();

	if (!vmDetectModules())
		return 0;

	if (!v9t9_preconfiginit()) 
		return 0;
	if (!keyboard_preconfiginit()) 
		return 0;
	if (!speech_preconfiginit())
		return 0;

	if (!vmInitModules())
		return 0;

	if (!vmSelectStartupModules())
		return 0;

	return 1;
}

static int
postconfiginit(void)
{
	int ret = 1;

	if (!vmEnableModules())
		ret = 0;

	if (!speech_postconfiginit())
		ret = 0;

	if (!keyboard_postconfiginit())
		ret = 0;

	return ret;
}

int         restarted = 0;

#define	RS_VIDEO 0x1
//#define RS_EMULATE 0x2
#define RS_KEYBOARD 0x4
#define RS_SOUND 0x8
#define RS_DSR 0x10
#define RS_SPEECH 0x20

int
v9t9_restart(void)
{
	static struct {
		int         (*func) (void);
		int         mask;
		char       *desc;
	} restartlist[] = {
		{
		video_restart, RS_VIDEO, "Video"}, {
		keyboard_restart, RS_KEYBOARD, "Keyboard"}, {
		sound_restart, RS_SOUND, "Sound"}, {
		speech_restart, RS_SPEECH, "Speech"},	/* after sound_... */
		{
		NULL, 0, ""}
	}, *ptr;
	int ret = 1;

	if (!vmRestartModules())
		ret = 0;

	ptr = restartlist;

	while (ptr->func) {
		if ((ptr->func) () == 0)
			ret = 0;
		else {
			restarted |= ptr->mask;
			ptr++;
		}
	}


	vdpcompleteredraw();
	TM_Start();
	return ret;
}


void
v9t9_restop(void)
{
	static struct {
		void        (*func) (void);
		int         mask;
		char       *desc;
	} restoplist[] = {
		{ video_restop, RS_VIDEO, "Video"},
		{ keyboard_restop, RS_KEYBOARD, "Keyboard"}, 
		{ sound_restop, RS_SOUND, "Sound"}, 
		{ speech_restop, RS_SPEECH, "Speech"},	/* after sound_... */
		{
		NULL, 0, ""}
	}, *ptr;

	TM_Stop();
	vmRestopModules();

	ptr = restoplist;
	while (ptr->func) {
		if (restarted & ptr->mask) {
			ptr->func();
		}
		ptr++;
	}

	restarted = 0;
}


static void
shutdown(void)
{
	vmDisableModules();

	TM_Kill();
	vmTermModules();

	compiler_term();
	demo_term();
	speech_shutdown();
}

/****************************************/

static
DECL_SYMBOL_ACTION(v9t9_display_help)
{
	char *var;
	if (command_arg_get_string(SYM_ARG_1st, &var) && var)
		command_help_symbol(var);
	else
		command_help();
	return 1;
}

#include "gnu_gpl.h"

static
DECL_SYMBOL_ACTION(v9t9_display_license)
{
	logger(LOG_USER, gnu_gpl_license);

	return 1;
}

static
DECL_SYMBOL_ACTION(v9t9_go_interactive)
{
	int val;
	if (task == csa_READ) 
		return (stateflag & ST_INTERACTIVE) != 0;
	
	command_arg_get_num(SYM_ARG_1st, &val);
	if (val) {
		stateflag |= ST_INTERACTIVE;
		system_getcommands();
	} else {
		stateflag &= ~ST_INTERACTIVE;
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(v9t9_exit_interactive)
{
	stateflag &= ~ST_INTERACTIVE;
	return 1;
}

static
DECL_SYMBOL_ACTION(v9t9_die)
{
	stateflag |= ST_TERMINATE;
	stateflag &= ~ST_INTERACTIVE;
	return 1;
}

static
DECL_SYMBOL_ACTION(v9t9_quit)
{
	stateflag |= ST_TERMINATE;

	//	Save session on a positive exit
	config_save_file(sessionspath, "session.cnf", true /*session*/);

	stateflag &= ~ST_INTERACTIVE;

	return 1;
}

int
v9t9_preconfiginit(void)
{
	command_symbol_table *v9t9commands =
		command_symbol_table_new(_("Major Emulator Commands"),
								 _("These are general commands to control the emulator"),

		 command_symbol_new("Help",
							_("Display command help"),
							c_DONT_SAVE|c_OPTIONAL_ARGS,
							v9t9_display_help,
							NULL /* ret */ ,
							 command_arg_new_string(_("option"),
								_("option (or prefix) to get help for") /* help */,
								NULL /* action */,
								NEW_ARG_NEW_STRBUF,
								NULL)	/* args */
							,

		command_symbol_new("License",
							_("Display GPL license and terms of use"),
							c_DONT_SAVE,
							v9t9_display_license,
							NULL /* ret */ ,
							NULL	/* args */
							,

		command_symbol_new("Interactive",
							 _("Control whether emulator waits for user commands"),
							 c_DONT_SAVE,
							 v9t9_go_interactive /* action */ ,
							 RET_FIRST_ARG,
							 command_arg_new_enum
							 ("off|on",
							  _("if 'on', emulation will halt; "
							  "if 'off', emulation continues at end of command list"),
							  NULL /* action */ ,
							  NEW_ARG_NUM(int),
							  NULL /* next */ )
							 ,

	  command_symbol_new("Exit",
							  _("Exit from interactive mode (same as 'Interactive=off')"),
							  c_DONT_SAVE,
							  v9t9_exit_interactive,
							  NULL /* ret */ ,
							  NULL	/* args */
							  ,

	  command_symbol_new ("Die",
							   _("Exit V9t9 without saving session"),
							   c_DONT_SAVE,
							   v9t9_die,
							   NULL /* ret */ ,
							   NULL	/* args */
							   ,

	  command_symbol_new ("Quit|Bye",
							   _("Exit V9t9 and save session"),
							   c_DONT_SAVE,
							   v9t9_quit,
							   NULL /* ret */ ,
							   NULL	/* args */
							   ,

	  command_symbol_new("InstallDirectory",
						 _("Set V9t9 installation directory"),
						 c_DONT_SAVE,
						 NULL /* action*/,
						 RET_FIRST_ARG,
						 command_arg_new_pathspec
						 (_("directory"),
						  _("directory root where V9t9 was installed "
							"(i.e., /usr/share/v9t9)"),
						  NULL	/* action */,
						  &v9t9_datadir,
						  NULL /* next */ )
						 ,

	  command_symbol_new("DataPath",
						 _("Set fallback directory list for loading and storing data files"),
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
						  NEW_ARG_STRBUF(&datapath),
						  NULL /* next */ )
						 ,

	  command_symbol_new
         ("ConfigsPath",
		  _("Set directory list for searching and saving configuration files"),
		  c_STATIC|c_CONFIG_ONLY,
		  NULL /* action*/,
		  RET_FIRST_ARG,
		  command_arg_new_string
		    (_("path"),
			 _("list of directories "
			 "separated by one of these characters: '"
			 OS_ENVSEPLIST "'"),
			 NULL	/* action */,
			 NEW_ARG_STRBUF(&configspath),
			 NULL /* next */ )
		  ,

	  command_symbol_new
         ("SessionsPath",
		  _("Set directory list for searching and saving session files"),
		  c_STATIC|c_CONFIG_ONLY,
		  NULL /* action*/,
		  RET_FIRST_ARG,
		  command_arg_new_string
		    (_("path"),
			 _("list of directories "
			   "separated by one of these characters: '"
			 OS_ENVSEPLIST "'"),
			 NULL	/* action */,
			 NEW_ARG_STRBUF(&sessionspath),
			 NULL /* next */ )
		  ,

	 command_symbol_new("SaveConfigFile",
			_("Save current configuration settings to 'file'"),
		  c_DONT_SAVE,
		  save_config  /* action */ ,
		  NULL	/* ret */,
		  command_arg_new_string
		  (_("file"),
		   _("name of config file"),
		   NULL  /* action */ ,
		   NEW_ARG_STR(256),
		   NULL	/* next */
		  ),

	 command_symbol_new("LoadConfigFile|ReadModuleDatabase",
		  _("Load configuration settings from 'file', "
		  "or merge module list from 'file'"),
		  c_DONT_SAVE,
		  load_config  /* action */ ,
		  NULL	/* ret */,
		  command_arg_new_string
		  (_("file"),
		   _("name of config file or modules.inf file"),
		   NULL  /* action */ ,
		   NEW_ARG_STR(256),
		   NULL	/* next */
		  ),

	 command_symbol_new("SaveSessionFile",
		  _("Save current configuration settings and machine state to 'file'"),
		  c_DONT_SAVE,
		  save_session  /* action */ ,
		  NULL	/* ret */,
		  command_arg_new_string
		  (_("file"),
		   _("name of config file"),
		   NULL  /* action */ ,
		   NEW_ARG_STR(256),
		   NULL	/* next */
		  ),

	 command_symbol_new("LoadSessionFile",
		  _("Load configuration settings and machine state from 'file'"),
		  c_DONT_SAVE,
		  load_session  /* action */ ,
		  NULL	/* ret */,
		  command_arg_new_string
		  (_("file"),
		   _("name of config file"),
		   NULL  /* action */ ,
		   NEW_ARG_STR(256),
		   NULL	/* next */
		  ),

#warning optional argument
	 command_symbol_new("SaveScreenShot",
		  _("Take a screenshot and save to 'file' or an autogenerated name"),
		  c_DONT_SAVE,
		  vdp_take_screenshot  /* action */ ,
		  NULL	/* ret */,
		  command_arg_new_string
		  (_("file"),
		   _("name of file to write, or \"\" to use an automatic name "
		   "in the current directory"),
		   NULL  /* action */ ,
		   NEW_ARG_STR(256),
		   NULL	/* next */
		  ),

	NULL /* next */ ))))))))))))))),

	NULL /* sub */ ,

	NULL	/* next */
	);

//	configspath = xstrdup(OS_CWDSTR);
//	sessionspath = xstrdup(OS_CWDSTR);
	command_symbol_table_add_subtable(universe, v9t9commands);
	return 1;
}
