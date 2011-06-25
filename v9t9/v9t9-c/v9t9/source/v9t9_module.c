/*
  v9t9_module.c					-- device driver module handler

  (See moduledb.c, roms.c, and memory.c for 99/4A module routines.)

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

/*
 *	Handle v9t9 modules.
 *
 *	The module concept is used here to separate the guts of the emulator
 *	from the particularities of the host executing environment.  Without
 *	modules, the emulator is a silent, invisible, noniteractive state machine.
 *	Modules define the interface between the 99/4A state machine and the 
 *	display, sound, keyboard, and I/O devices of the host.
 *
 */

#include <assert.h>

#include "v9t9_common.h"

#include "command.h"
#include "v9t9_module.h"

/*	These are lists of modules of the given type.  Except (currently) for
	vmCPU and vmVideo, any of the types may have more than one active
	module at one time.  For instance, there can be two separate sound
	devices [maybe one for MIDI output of the sound generator, and another
	for digitized data output]. */

vmModule   *vmCPU, *vmVideo, *vmKeyboard, *vmSound, *vmDSR;

/*	Currently active main module */
vmModule   *vmCPUMain, *vmVideoMain, *vmKeyboardMain, *vmSoundMain,

	*vmDSRMain;

static char *vmErrors[] = {
	"no error",
	"not available",
	"configuration error",
	"module disabled",
	"internal error"
};

static char *vmTypeNames[] = {
	"CPU",
	"video",
	"sound",
	"keyboard",
	"DSR"
};

#define MAX_MODULES	64

static vmModule *modules[MAX_MODULES];
static int  nmodules;

#define _L	 LOG_MODULES | LOG_INFO
#define _LL	 LOG_MODULES | LOG_ERROR

static int  
AddModule(vmModule * module)
{
	if (nmodules >= MAX_MODULES) {
		logger(_LL | 0, _("AddModule:  too many modules defined"));
		return 0;
	}

	if (module->name == NULL) {
		logger(_LL | 0, _("AddModule:  module has no name\n"));
		return 0;
	}

	if (module->version != VM_CURRENT_VERSION) {
		module_logger(module, _LL | 0,
				_("AddModule:  module has wrong module version (%d != %d)\n\n"),
				module->version, VM_CURRENT_VERSION);
		return 0;
	}

	if (module->m.generic == NULL) {
		module_logger(module, _LL | 0, _("AddModule: module has no specific module pointer\n"));
		return 0;
	}

	switch (module->type) {
	case vmTypeCPU:
		if (module->m.cpu->version != VMCPU_CURRENT_VERSION) {
			module_logger(module, _LL | 0, _("AddModule: CPU module '%s' has wrong version (%d)\n"),
					module->name, module->m.cpu->version);
			return 0;
		}
		if (!module->m.cpu->execute || !module->m.cpu->stop) {
			module_logger(module, _LL | 0, _("AddModule: CPU module '%s' has NULL callback(s)\n"),
					module->name);
			return 0;
		}
		break;

	case vmTypeVideo:
		if (module->m.video->version != VMVIDEO_CURRENT_VERSION) {
			module_logger(module, _LL | 0, _("AddModule: video module '%s' has wrong version (%d)\n"),
					module->name, module->m.video->version);
			return 0;
		}
		if (!module->m.video->updatelist || !module->m.video->resize ||
			!module->m.video->setfgbg || !module->m.video->setblank ||
			!module->m.video->resetfromblank) {
			module_logger(module, _LL | 0, _("AddModule: Video module has NULL callback(s)\n"));
			return 0;
		}
		break;

	case vmTypeSound:
		if (module->m.sound->version != VMSOUND_CURRENT_VERSION) {
			module_logger(module, _LL | 0, _("AddModule: sound module has wrong version (%d)\n"),
				module->m.sound->version);
			return 0;
		}
		if (!(module->flags & vmFlagsExclusive) &&
			(module->m.sound->update == NULL &&
			 module->m.sound->flush == NULL && 
			 module->m.sound->play == NULL && 
			 module->m.sound->read == NULL && 
			 module->m.sound->playing == NULL)) {
			module_logger(module, _LL | 0,
					_("AddModule: invalid sound module (has no callbacks "
					"and is not exclusive), ignoring\n"));
			return 0;
		}
		break;

	case vmTypeKeyboard:
		if (module->m.kbd->version != VMKEYBOARD_CURRENT_VERSION) {
			module_logger(module, _LL | 0,
					_("AddModule: keyboard module has wrong version (%d)\n"),
					module->m.kbd->version);
			return 0;
		}
		if (!module->m.kbd->scan || !module->m.kbd->getspecialkeys) {
			module_logger(module, _LL | 0, _("AddModule: Keyboard module has NULL callback(s)\n"));
			return 0;
		}
		break;

	case vmTypeDSR:
		if (module->m.dsr->version != VMDSR_CURRENT_VERSION) {
			module_logger(module, _LL | 0, _("AddModule: DSR module has wrong version (%d)\n"),
					module->m.dsr->version);
			return 0;
		}
		if (!module->m.dsr->getcrubase || !module->m.dsr->filehandler) {
			module_logger(module, _LL | 0, _("AddModule: DSR module has NULL callback(s)\n"));
			return 0;
		}
		break;

		module_logger(module, _LL | 0, _("AddModule:  module has unknown type (%d)\n"),
				module->type);
		return 0;
	}

	if (module->detect == NULL ||
		module->init == NULL ||
		module->term == NULL ||
		module->enable == NULL ||
		module->disable == NULL ||
		module->restart == NULL || module->restop == NULL) {
		module_logger(module, _LL | 0, _("AddModule:  callback(s) missing in '%s'\n"), module->name);
		return 0;
	}

	modules[nmodules++] = module;
	return 1;
}

/*********************************/

int
vmModulesInit(void)
{
	nmodules = 0;
	return 1;
}

int
vmModulesTerm(void)
{
	vmTermModules();
	nmodules = 0;
	return 1;
}

int
vmRegisterModules(vmModule ** module)
{
	vmModule  **mptr;

	for (mptr = module; *mptr; mptr++)
		if (!AddModule(*mptr))
			return 0;
	return 1;
}

/*********************************/

vmResult
vmDetectModule(vmModule * module)
{
	vmResult    res;

	assert(module != NULL);
	module_logger(module, _L | L_1, "vmDetectModule");
	res = module->detect();
	if (res != vmOk) {
		// not _LL since a detection needn't succeed
		module_logger(module, _L | 0, "vmDetectModule:  %s\n", vmErrors[res]);
		module->runtimeflags |= vmRTUnavailable;
	}
	return res;
}

vmResult
vmInitModule(vmModule * module)
{
	vmResult    res;

	assert(module != NULL);

	if (module->runtimeflags & vmRTUnavailable)
		return vmOk;

	if (module->runtimeflags & vmRTUndoInit)
		return vmOk;

	module_logger(module, _L | L_1, "vmInitModule\n");
	res = module->init();
	if (res != vmOk) {
		module_logger(module, _LL | 0, "vmInitModule: %s\n", vmErrors[res]);
		module->runtimeflags |= vmRTUnavailable;
	} else {
		module->runtimeflags |= vmRTUndoInit;
	}
	return res;
}

vmResult
vmTermModule(vmModule * module)
{
	vmResult    res;

	assert(module != NULL);

	if (!(module->flags & vmRTUndoInit))
		return vmOk;

	module->runtimeflags &= ~vmRTUndoInit;
	module_logger(module, _L | L_1, "vmTermModule\n");
	res = module->term();
	if (res != vmOk)
		module_logger(module, _LL | 0, "vmTermModule: %s\n", vmErrors[res]);
	return res;
}

vmResult
vmEnableModule(vmModule * module)
{
	vmResult    res;

	assert(module != NULL);

	if (module->runtimeflags & vmRTUnavailable)
		return vmOk;

	if (!(module->runtimeflags & vmRTInUse))
		return vmOk;

	if ((module->flags & vmFlagsOneShotEnable) &&
		(module->runtimeflags & vmRTEnabledOnce)) return vmOk;

	if (module->runtimeflags & vmRTUndoEnable)
		return vmOk;

	module_logger(module, _L | L_1, "vmEnableModule\n");
	res = module->enable();
	if (res != vmOk)
		module_logger(module, _LL | 0, "vmEnableModule: %s\n", vmErrors[res]);
	else {
		module->runtimeflags |= vmRTEnabledOnce;
		module->runtimeflags |= vmRTUndoEnable;
	}

	return res;
}

vmResult
vmDisableModule(vmModule * module)
{
	vmResult    res;

	assert(module != NULL);

	if (!(module->runtimeflags & vmRTUndoEnable))
		return vmOk;

	module_logger(module, _L | L_1, "vmDisableModule\n");
	module->runtimeflags &= ~vmRTUndoEnable;
	res = module->disable();
	if (res != vmOk)
		module_logger(module, _LL | 0, "vmDisableModule: %s\n", vmErrors[res]);

	return res;
}

vmResult
vmRestartModule(vmModule * module)
{
	vmResult    res;

	assert(module != NULL);

	if (!(module->runtimeflags & vmRTInUse))
		return vmOk;

	if (!(module->runtimeflags & vmRTEnabledOnce))
		return vmOk;

	if (module->runtimeflags & vmRTUndoRestart)
		return vmOk;

	module_logger(module, _L | L_1, "vmRestartModule\n");
	res = module->restart();
	if (res != vmOk)
		module_logger(module, _LL | 0, "vmRestartModule: %s\n", vmErrors[res]);
	else
		module->runtimeflags |= vmRTUndoRestart;

	return res;
}

vmResult
vmRestopModule(vmModule * module)
{
	vmResult    res;

	assert(module != NULL);

	if (!(module->runtimeflags & vmRTInUse))
		return vmOk;

	if (!(module->runtimeflags & vmRTEnabledOnce))
		return vmOk;

	if (!(module->runtimeflags & vmRTUndoRestart))
		return vmOk;

	module_logger(module, _L | L_1, "vmRestopModule\n");
	res = module->restop();
	if (res != vmOk)
		module_logger(module, _LL | 0, "vmRestopModule: %s\n", vmErrors[res]);
	else
		module->runtimeflags &= ~vmRTUndoRestart;

	return res;
}

/********************/

static int
CollectModules(vmModule ** list, vmType type, bool required)
{
	int         found = 0;
	int         x;

	*list = NULL;
	for (x = 0; x < nmodules; x++)
		if (modules[x]->type == type && vmDetectModule(modules[x]) == vmOk) {
			*list = modules[x];
			list = &(*list)->next;
			found++;
		}
	*list = NULL;				/* terminate list */

	if (!found && required)
		logger(_L | LOG_FATAL, _("CollectModules:  Could not find a %s module\n"),
			 vmTypeNames[type]);

	return found;
}

/*	Detect all applicable modules */
int
vmDetectModules(void)
{
	logger(_L | L_1, "vmDetectModules");

	/*  CPU modules  */
	if (!CollectModules(&vmCPU, vmTypeCPU, true))
		return 0;

	/*  Video modules */
	if (!CollectModules(&vmVideo, vmTypeVideo, true))
		return 0;

	/*  Sound modules */
	if (!CollectModules(&vmSound, vmTypeSound, true))
		return 0;

	/*  Keyboard modules */
	if (!CollectModules(&vmKeyboard, vmTypeKeyboard, true))
		return 0;

	/*  DSR modules */
	CollectModules(&vmDSR, vmTypeDSR, false);

	return 1;
}

/*	Select startup (or re-startup) modules.  
	-- We are allowed to use more than one module of one type
	at once if none has the vmFlagsExclusive flag set.
	-- For sound modules, we also make the restriction that
	we only use as many modules as will satisfy both callbacks;
	if the first module selected defines one, that one will
	be ignored even if defined in a second selected one.
	-- For simplicity, we assume that the list of modules
	is ordered by priority and stop with the first workable
	combination.
*/

static void
SelectModules(vmModule * list, vmModule ** main, vmType type, 
			  bool startup, bool exclusive)
{
	vmModule   *ptr, *prev;
	int         chained = 0;	/* already using a module? */
	int         termed = 0;		/* not looking for new modules */

	for (ptr = list, prev = NULL; ptr; ptr = ptr->next) {
		/* clear flag */
		ptr->runtimeflags &= ~vmRTInUse;

		if (termed) {
			continue;
		}

		if (ptr->runtimeflags & vmRTUnavailable) {
			module_logger(ptr, _L | L_1, _("SelectModules: unavailable, skipping\n"));
			continue;
		}

		if (ptr->runtimeflags & vmRTUnselected) {
			module_logger(ptr, _L | L_1, _("SelectModules: is unselected, skipping\n"));
			continue;
		}

		if (ptr->flags & vmFlagsExclusive) {
			if (chained) {
				/* already using non-exclusive one */
				module_logger(ptr, _L | L_1,
					 _("SelectModules: not using since non-exclusive module already in use\n"));
				continue;
			}
			/*  else, select this one,
			   and one-by-one ignore each of the rest */
		} else
			/* sound has special semantics */
		if (type == vmTypeSound && prev != NULL) {
			/* if we're here, then the 'prev' module defines
			   one callback; only select a module that defines 
			   the other one */
			if ((prev->m.sound->update == NULL &&
				 ptr->m.sound->update == NULL)
				||
				((prev->m.sound->play == NULL) &&
				 (ptr->m.sound->play == NULL))
				||
				((prev->m.sound->read == NULL) &&
				 (ptr->m.sound->read == NULL))) {
				/* neither defines the missing callback */
				module_logger(ptr, _L | L_1,
					 _("SelectModules: not using since it doesn't satisfy missing "
					 "callbacks from module '%s'\n"), prev->name);
				continue;
			}
		}
		/* just use it */

		module_logger(ptr, _L|L_0, _("SelectModules: using\n"));

		if (!chained && main)
			*main = ptr;

		ptr->runtimeflags |= vmRTInUse;
		if (!startup) vmEnableModule(ptr);
		chained++;
		prev = ptr;

		/* did we get the only one we could use? */
		if (exclusive) {
			logger(_L | L_1,
				 _("SelectModules: terminating search, only one %s module may be active\n"),
				 vmTypeNames[type]);
			termed = 1;
			continue;
		}

		if (type == vmTypeSound &&
			ptr->m.sound->update != NULL && ptr->m.sound->play != NULL &&
			ptr->m.sound->read != NULL) {
			module_logger(ptr, _L | L_1, _("SelectModule: terminating search, all sound callbacks defined\n"));
			termed = 1;
			continue;
		}
	}

	if (!chained && prev != NULL)
		logger(_L | LOG_FATAL,
			 _("SelectModules:  did not find a single usable %s module?\n"),
			 vmTypeNames[type]);
}


/*	The user has opted to select a custom
	list of modules.  Validate each combination.
	If the combination fails, simply call SelectModules()
	again to return to the default state. 
	
	Valid list:  -- no two are vmTypeExclusive
	-- none has vmRTUnselected set
	-- for sound, no conflicting callbacks 
	
	AND, an empty list is valid if "incremental" is set.
*/

static int
ValidateModules(vmModule * list, vmType type, bool required,
				bool exclusive, bool incremental)
{
	vmModule   *ptr, *prev;
	int         chained = 0;	/* already using a module? */

	ptr = list;
	prev = NULL;
	while (ptr) {
		if (!(ptr->runtimeflags & (vmRTUnselected | vmRTUnavailable))) {
			/* did we get the only one we could use? */
			if (chained && exclusive) {
				module_logger(ptr, _LL | 0, _("ValidateModules: must not be selected since "
						"only one %s module may be active at once\n"),
						vmTypeNames[type]);
				return 0;
			}

			if (ptr->flags & vmFlagsExclusive) {
				if (chained) {
					/* already using non-exclusive one, fail */
					module_logger(ptr, _LL | 0,
							_("ValidateModules: must not be selected since "
							"other non-exclusive modules are already selected\n"));
					return 0;
				}
			} else
				/* sound has special semantics */
			if (type == vmTypeSound && prev != NULL) {
				if (((prev->m.sound->update != NULL) &&
					 (ptr->m.sound->update != NULL))
					||
					((prev->m.sound->play != NULL) &&
					 (ptr->m.sound->play != NULL))
					||
					((prev->m.sound->read != NULL) &&
					 (ptr->m.sound->read != NULL))) {
					/* both have the same callback defined */
					module_logger(ptr, _LL | 0,
							_("ValidateModules: must not be selected since module "
							"'%s' defines the same callbacks\n"), prev->name);
					return 0;
				}
			}
			// success, so far

//          // allow selection of disabled module
//          ptr->runtimeflags &= ~vmRTUnselected;

			chained++;
			prev = ptr;
		}

		ptr = ptr->next;

	}

	if (!chained && !incremental && required) {
		logger(_LL | 0, _("ValidateModules: no %s modules are selected; "
				"at least one is required\n"), vmTypeNames[type]);
		return 0;
	}

	return 1;
}

static vmResult
IterModuleList(vmModule * ptr, vmResult(*vmFunc) (vmModule *))
{
	vmResult    res = vmOk, ret = vmOk;

	while (ptr) {
		if (!(ptr->runtimeflags & (vmRTUnavailable | vmRTUnselected))) {
			if ((res = vmFunc(ptr)) != vmOk)
			{
				if (res == vmNotAvailable)
					ptr->runtimeflags |= vmRTUnavailable;
				else
					ret = res;
			}
		}
		ptr = ptr->next;
	}
	return ret;
}

static vmResult
IterModuleLists(vmModule ** list[], vmResult(*vmFunc) (vmModule *))
{
	vmResult    res = vmOk, ret = vmOk;
	int         step;

	step = 0;
	while (list && list[step]) {
		vmModule   *ptr = *(list[step]);

		if ((res = IterModuleList(ptr, vmFunc)) != vmOk)
			ret = res;
		step++;
	}

	return ret;
}


int
vmValidateModuleSelection(bool startup, bool incremental)
{
	logger(_L | L_1, "vmValidateModuleSelection\n");

	/*  CPU modules  */
	SelectModules(vmCPU, &vmCPUMain, vmTypeCPU, startup, true /*exclusive*/);

	/*  Video modules */
	SelectModules(vmVideo, &vmVideoMain, vmTypeVideo, startup, false /*exclusive*/);

	/*  Sound modules */
	IterModuleList(vmSound, vmRestopModule);
	SelectModules(vmSound, &vmSoundMain, vmTypeSound, startup, false /*exclusive*/);
	IterModuleList(vmSound, vmRestartModule);

	/*  Keyboard modules */
	SelectModules(vmKeyboard, &vmKeyboardMain, vmTypeKeyboard, startup, false /*exclusive*/);

	/*  DSR modules */
	SelectModules(vmDSR, NULL, vmTypeDSR, startup, false /*exclusive*/);

	/*  Reset our features mask. */
	features &= ~(FE_VIDEO | FE_KEYBOARD | FE_SOUND | FE_SPEECH);

	/*  These are always available. */
	features |= FE_VIDEO | FE_KEYBOARD;

	/*  These depend on the union of callbacks our sound modules define. */
	if (vmSound->m.sound->update != NULL ||
		(vmSound->next && vmSound->next->m.sound->update != NULL))
		features |= FE_SOUND;
	if (vmSound->m.sound->play != NULL ||
		(vmSound->next && vmSound->next->m.sound->play != NULL))
		features |= FE_SPEECH | FE_CSXWRITE;
	if (vmSound->m.sound->read != NULL ||
		(vmSound->next && vmSound->next->m.sound->read != NULL))
		features |= FE_CSXREAD;

	return 1;
}

int
vmSelectStartupModules(void)
{
	return vmValidateModuleSelection(true, false);
}

int
vmInitModules(void)
{
	static vmModule **initList[] = {
		&vmKeyboard, &vmSound, &vmVideo, &vmCPU, &vmDSR, NULL
	};

	logger(_L | L_1, "vmInitModules\n");
	return IterModuleLists(initList, vmInitModule) == vmOk;
}

int
vmTermModules(void)
{
	static vmModule **termList[] = {
		&vmDSR, &vmVideo, &vmCPU, &vmKeyboard, &vmSound, NULL
	};

	logger(_L | L_1, "vmTermModules\n");
	return IterModuleLists(termList, vmTermModule) == vmOk;
}

int
vmEnableModules(void)
{
	static vmModule **enableList[] = {
		&vmVideo, &vmKeyboard, &vmSound, &vmDSR, &vmCPU, NULL
	};

	logger(_L | L_1, "vmEnableModules\n");
	return IterModuleLists(enableList, vmEnableModule) == vmOk;
}

int
vmDisableModules(void)
{
	static vmModule **disableList[] = {
		&vmCPU, &vmDSR, &vmSound, &vmKeyboard, &vmVideo, NULL
	};

	logger(_L | L_1, "vmDisableModules\n");
	return IterModuleLists(disableList, vmDisableModule) == vmOk;
}

int
vmRestartModules(void)
{
	static vmModule **restartList[] = {
		&vmDSR, &vmCPU, NULL
	};
	int ret = 1;

	logger(_L | L_1, "vmRestartModules, features = %x\n", features);
	if (features & FE_VIDEO)
	        ret &= (IterModuleList(vmVideo, vmRestartModule) == vmOk);
	if (features & FE_KEYBOARD)
	        ret &= (IterModuleList(vmKeyboard, vmRestartModule) == vmOk);
	if (features & FE_SOUND)
	        ret &= (IterModuleList(vmSound, vmRestartModule) == vmOk);

	return ret && IterModuleLists(restartList, vmRestartModule) == vmOk;
}

int
vmRestopModules(void)
{
	static vmModule **restopList[] = {
		&vmCPU, &vmDSR, NULL
	};
	int ret = 1;

	logger(_L | L_1, "vmRestopModules, features = %x\n", features);
	if (features & FE_SOUND)
	  ret &= (IterModuleList(vmSound, vmRestopModule) == vmOk);
	if (features & FE_KEYBOARD)
	  ret &= (IterModuleList(vmKeyboard, vmRestopModule) == vmOk);
	if (features & FE_VIDEO)
	  ret &= (IterModuleList(vmVideo, vmRestopModule) == vmOk);
	return ret && IterModuleLists(restopList, vmRestopModule) == vmOk;
}

int
vmClearUserFlagsOnModules(void)
{
	static vmModule **allList[] = {
		&vmCPU, &vmDSR, &vmSound, &vmVideo, &vmKeyboard, 0L
	};
	int idx = 0;
	logger(_L | L_1, "vmClearUserFlagsOnModules\n");
	while (allList[idx]) {
		vmModule *list = *allList[idx];
		while (list) {
			list->runtimeflags &= ~vmRTUserModified;
			list = list->next;
		}
		idx++;
	}
	return 1;
}

/******************************/

vmModule   *
vmLookupModule(char *tag)
{
	int         x;

	for (x = 0; modules[x] && x < MAX_MODULES; x++) {
		if (strcasecmp(modules[x]->tag, tag) == 0)
			return modules[x];
	}
	return NULL;
}

static
DECL_SYMBOL_ACTION(vm_list_v9t9_modules)
{
	int         x;

	for (x = 0; modules[x] && x < MAX_MODULES; x++) {
		logger(LOG_USER, _("Tag: '%s', name: '%s', type: '%s'\n"),
			 modules[x]->tag, modules[x]->name, vmTypeNames[modules[x]->type]);
		logger(LOG_USER, _("\tFlags: "));
		if (modules[x]->flags & vmFlagsExclusive)
			logger(LOG_USER, _("exclusive "));
		if (modules[x]->flags & vmFlagsOneShotEnable)
			if (modules[x]->runtimeflags & vmRTEnabledOnce)
				logger(LOG_USER, _("[enable-once] "));
			else
				logger(LOG_USER, _("enable-once "));
		if (modules[x]->flags & vmFlagsOneShotDisable)
			if (modules[x]->runtimeflags & vmRTDisabledOnce)
				logger(LOG_USER, _("[disable-once] "));
			else
				logger(LOG_USER, _("disable-once "));
		if (modules[x]->runtimeflags & vmRTInUse)
			logger( LOG_USER, _("in-use "));
		if (modules[x]->runtimeflags & vmRTUnselected)
			logger( LOG_USER, _("unselected "));
		logger(LOG_USER, "\n");
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(vm_toggle_v9t9_module)
{
	char       *tag;
	int         onoff;
	vmModule   *mod;

	if (task == csa_READ)
	{
		static int idx;

		if (!iter)
			idx = 0;

		if (idx >= nmodules)
			return 0;

		while (idx < nmodules) {
			if (modules[idx]->runtimeflags & vmRTUserModified) {
				command_arg_set_string(sym->args, modules[idx]->tag);
				command_arg_set_num(sym->args->next, 
									!(modules[idx]->runtimeflags & vmRTUnselected));
				idx++;
				return 1;
			} else {
				idx++;
			}
		}
		return 0;
	}

	command_arg_get_string(sym->args, &tag);
	command_arg_get_num(sym->args->next, &onoff);
	mod = vmLookupModule(tag);
	if (mod == NULL) {
		logger(_LL | LOG_USER, _("No such v9t9 module '%s'\n\n"), tag);
		return 0;
	}

	if ((mod->runtimeflags & vmRTUnavailable) && onoff) {
		logger(_LL | LOG_USER, _("Module '%s' is unavailable, cannot toggle\n"), tag);
		return 0;
	}

	if (!onoff) {
		vmRestopModule(mod);
		vmDisableModule(mod);
	}

	if (onoff)
		mod->runtimeflags &= ~vmRTUnselected;
	else
		mod->runtimeflags |= vmRTUnselected;

	mod->runtimeflags |= vmRTUserModified;

	if (!vmValidateModuleSelection(false, true))
		return 0;

	if (onoff) {
		if (vmInitModule(mod) != vmOk)
			return 0;
		if (vmEnableModule(mod) != vmOk)
			return 0;
		if (vmRestartModule(mod) != vmOk)
			return 0;
	}

	return 1;
}

static
DECL_SYMBOL_ACTION(vm_setup_v9t9_modules)
{
	return vmValidateModuleSelection(false, false);
}

void
module_logger(vmModule *module, int flags, const char *format, ...)
{
	va_list va;
	char buf[256], *bptr;

	va_start(va, format);
	bptr = mvprintf(buf, sizeof(buf), format, va);
	if (module && module->name)
		logger(flags, "%s: %s", 
			   module->name,
			   bptr);
	else
		logger(flags, "%s", bptr);
	if (bptr != buf) xfree(bptr);
	va_end(va);
}


void
vmAddModuleCommands(void)
{
	command_symbol_table *modules =
		command_symbol_table_new(_("V9t9 Module Commands"),
								 _("These options affect the modules used to emulate V9t9"),

		 command_symbol_new("ListV9t9Modules",
							_("List available modules and current status"),
							c_DONT_SAVE,
							vm_list_v9t9_modules,
							NULL /* ret */ ,
							NULL	/* args */
							,

		command_symbol_new("ToggleV9t9Module",
				   _("Turn use of a module on or off"),
							 c_DYNAMIC,
							 vm_toggle_v9t9_module,
							 NULL /* ret */ ,
							 command_arg_new_string
							 (_("tag"),
							  _("tag name for module"),
							  NULL,
							  NEW_ARG_STR(64),
							  command_arg_new_enum
							  ("off|on",
							   _("whether to use or not use module"),
							   NULL,
							   NEW_ARG_NUM(bool),
							   NULL /* next */ ))
							 ,

	   command_symbol_new("SetupV9t9Modules",
							  _("Setup module gestalt"),
							  c_DONT_SAVE,
							  vm_setup_v9t9_modules,
							  NULL /* ret */ ,
							  NULL	/* args */
							  ,

							  NULL /* next */ ))),

		 NULL /* sub */ ,

		 NULL	/* next */
		);

	command_symbol_table_add_subtable(universe, modules);
}
