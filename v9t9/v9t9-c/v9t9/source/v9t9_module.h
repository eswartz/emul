
/*
  v9t9_module.h					-- public interface for a v9t9 module.

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
 
#ifndef __v9t9_module_h__
#define __v9t9_module_h__

#include "centry.h"

/*
	History:

	20001201  	-- removed author tag from modules
	20000703:	-- added vmSoundModule.flush() as a means to
				   turn off sound without messing around with
				   cached sound info.
	20000625:	-- removed vmVideoModule.update().  No reason to
	                have this, since it's always a call into vdp.c.
					Also removed updatetextlist.  Our bitmap has
					enough info to safely update 8x6 blocks
					by drawing 8x8 blocks instead.
	20000409:	-- changed keyboard getkeynames into getextendedkeys,
					keeping enums and names constant for keys and
				    making modules only define which they support
	19990725:	-- bumped vmModule version to 3 and changed
					'install' to 'enable' and added 'disable' and
					a tag field
	19990601:	-- bumped all version numbers to 2.
				-- removed vmContext parameter from all callbacks 
					(the idea of a loadable plugin module is currently 
					absurd)

*/

/*	These enums are accepted error returns from module functions. */
typedef enum
{
	vmOk 			= 0,
	vmNotAvailable,
	vmConfigError,
	vmDisabled,
	vmInternalError
}	vmResult;

/*	module types */
typedef enum
{
	vmTypeCPU, 
	vmTypeVideo,
	vmTypeSound,
	vmTypeKeyboard,
	vmTypeDSR
}	vmType;

/*	module flags */
typedef enum
{
	vmFlagsNone = 0,
	vmFlagsExclusive = 1,		/* 	module must be only one of its type;
									usually used when it consumes all the
									information; module may be used exclusively
									nonetheless, due to emulator restrictions */
	vmFlagsOneShotEnable = 2,	/*	module can only be enabled once */
	vmFlagsOneShotDisable = 4,	/*	module can only be disabled once */
	vmFlagsUnused
}	vmFlags;

/*	runtime flags */
enum
{
	/* these flags correspond to the vmFlags for switching to a module  */
	vmRTEnabledOnce = 0x1,		/*	we installed the module (once) */
	vmRTDisabledOnce = 0x2,		/*	we installed the module (once) */

	/* these flags correspond to the startup/shutdown process */
	vmRTUndoInit = 0x10,		/* 	we inited the module (and must term) */
	vmRTUndoEnable = 0x20,		/* 	we enabled the module (and must disable) */
	vmRTUndoRestart = 0x40,		/* 	we restarted the module (and must restop) */

	vmRTInUse = 0x100,			/*	currently using this module */
	vmRTUnselected = 0x200,		/*	intentionally unselected this one */
	vmRTUnavailable = 0x400,	/*	->detect() failed */

	vmRTUserModified = 0x800,	/*	status modified by user (i.e., save it) */
	vmRTUnused
};

/************************/

/*	This struct is passed from a module to v9t9 and
	serves to define its capabilities and callbacks. */

typedef struct
{
	int			version;
}	vmGenericModule;

/**********************************************/

#define VMCPU_CURRENT_VERSION 2

typedef struct
{
	u16		pc, wp, st;
}	cpuContext;

enum
{
	em_KeepGoing = 0,	// nothing big
	em_Interrupted = 1,	// return to system
	em_TooFast = 2,		// idle
	em_Quitting = 3,	// normal termination
	em_Dying = 4		// fatal termination
};

/*	A CPU module, currently, means nothing special */
typedef struct
{	
	int			version;
			
	/* execute until interrupted, return an em_XXXX value */
	int			(*execute)(void);
			
	/* interrupt execution (results in vmCPUModule->execute() returning) */
	void		(*stop)(void);

}	vmCPUModule;	/* vmTypeCPU */

		
/**********************************************/

#define VMSOUND_CURRENT_VERSION	4

typedef enum
{
	vms_Tv0		= 0x01,
	vms_Tv1		= 0x02,
	vms_Tv2		= 0x04,
	vms_Tn		= 0x08,
	vms_Vv0		= 0x10,
	vms_Vv1		= 0x20,
	vms_Vv2		= 0x40,
	vms_Vn		= 0x80,

}	vmsUpdateMask;

typedef enum
{
	vms_Speech	= 0x01,		// speech synthesizer
	vms_AGw		= 0x02		// audio gate
}	vmsPlayMask;

typedef enum
{
	vms_AGr		= 0x02		// audio gate [cassette]
}	vmsReadMask;



/*	A sound module expresses the current state of sound, including
	the three-voice-plus-noise sound chip, and all digital sounds
	produced from other sources (speech, cassette, etc).
	
	Currently, the state of the sound chip is in the voices[]
	array (defined in sound.h).  Read from that array to determine
	what the 'updated' bitmask is referring to.  
	
	As for digital sounds, they are presented directly to the 'play'
	function.  The mix_server.c file is intended to be a somewhat portable
	mixer for digital data.  Feel free to use it.
*/
typedef struct
{
	int			version;

  	/* update sound according to bitmask */
	void		(*update)(vmsUpdateMask updated);

  	/* flush tone/noise sound */
	void		(*flush)(void);

  	/* schedule digital data for playing (immediately if possible,
  		else after all other sound of this type);
  		kind is *one* element of vmsPlayMask;
  		data/size/hz describe the sample */
	void		(*play)(vmsPlayMask kind, s16 *data, int size, int hz);

	/* read digital data from input; smooth input data if hardware
	 	is not capable of reading at a low 'hz' rate (for cassette). */
	void		(*read)(vmsReadMask kind, u16 *data, int size, int hz); 	

	/* test if digital data is done playing */
	int			(*playing)(vmsPlayMask kind);
}	vmSoundModule;		/* vmTypeSound */

/**********************************************/

#define VMVIDEO_CURRENT_VERSION 3

/* 

    A video module is expected to work by copying from an offscreen
    bitmap of the 99/4A screen (maintained in vdp.c), the changed
    parts of which are announced through callbacks to updatelist.
    This routine is sent lists of updateblocks, which contain pointers
    to the upper-left corners of 8x8 blocks in the bitmap.
    UPDATE_ROW_STRIDE is the width of a row. The extent of the bitmap
    is greater than that of the 99/4A video screen for use in clipping
    sprites.
	
	Never touch the bitmap!  It is expected to maintain history between
	updates.
	
	The bitmap is arranged using one byte per color, range 0 to 16.  Bytes
	with values 1 through 15 correspond to ordinary TI colors.  Entry 0 is
	used for "clear", the background.  The video mode you use should allow
	palette flipping for best performance, since entry 0 represents a color
	that changes often (the setfgbg callback).  Entry 16 is used for the
	foreground color in text mode.  
*/
	
struct updateblock;

typedef struct
{
	int			version;

	/* update screen (or offscreen page) from blocks in list */
	vmResult	(*updatelist)(struct updateblock *first, int num);
	
	/* resize the screen to this size in pixels
		(usually 256x192 or 240x192 for text mode) */
	vmResult	(*resize)(u32 x, u32 y);

	/* update color 0 and color 16 (as appearing in the main
		bitmap) to these TI colors (which may be 0, interpret as black) */
	vmResult	(*setfgbg)(u8 bg, u8 fg);

	/* blank the screen; background color is 'bg' 
		(maybe set all palette entries to color bg) */
	vmResult	(*setblank)(u8 bg);
	
	/* unblank the screen */
	vmResult	(*resetfromblank)(void);
}	vmVideoModule;		/* vmTypeVideo */

/**********************************************/

#define VMKEYBOARD_CURRENT_VERSION 3

typedef enum {
	SK_FIRST = 128,
	SK_PAUSE = SK_FIRST,
	SK_F1, SK_F2, SK_F3, SK_F4,
	SK_F5, SK_F6, SK_F7, SK_F8,
	SK_F9, SK_F10, SK_F11, SK_F12,
	SK_ESC, SK_TAB,
	SK_WIN_LEFT, SK_WIN_RIGHT, SK_MENU,
	SK_LAST
}	SpecialKey;

typedef struct
{
	int			version;
			
	/*	scan keyboard and update crukeyboardmap[] */
	vmResult	(*scan)(void);
	
	/*	return list of supported extended keys from
		the SK_xxx enum list; terminate with 0. */
	vmResult	(*getspecialkeys)(SpecialKey **list);
}	vmKeyboardModule;	/* vmTypeKeyboard */

/**********************************************/

/*	DSR module. 
	"install" routine should load ROM and set up CRU handlers.
	The >0 bit handler should call dsr_set_active(). */
#define VMDSR_CURRENT_VERSION 1
		
typedef struct
{
	int			version;
	
	/*	Return CRU base module is currently active on. 
		It will only be called if a program jumps into the DSR
		memory area (>4000...>5FFF) and the DSR is active.
		This allows one module to emulate a series of
		devices, if necessary.  */
	vmResult	(*getcrubase)(u32 *base);
	
	/*	Handle file-level DSR callbacks;
		code is either a device number or a subroutine number,
		as enumerated in the DSR's device name list.
		This routine is only called if the DSR ROM for the
		active device uses the OP_DSR opcode (0xC00..0xC1F). */
	vmResult	(*filehandler)(u32 code);
}	vmDSRModule;	/* vmTypeDSR */

/**********************************************/

/*	current version of module struct */
#define VM_CURRENT_VERSION	3

typedef struct vmModule
{
	/* ------ common to all modules ------ */
	
	int			version;
	
	const char	*name;		/* screen name */
	const char	*tag;		/* tag for id */
	
	vmType		type;
	vmFlags		flags;
	
	/* detect whether module is usable; called before
		any configuration variables are known */
	vmResult	(*detect)(void);
	
	/* [one-time] initialization; called before any
		configuration variables are known; insert your
		own variables here and initialize them */
	vmResult	(*init)(void);

	/* [one-time] termination at program start */
	vmResult	(*term)(void);
	
	/*  multi-time installation; configuration
		variables are known; called when module is
		selected */
	vmResult	(*enable)(void);

	/*  multi-time uninstallation; called when module is
		unselected */
	vmResult	(*disable)(void);

	/* multi-time restart; done in context switches
		into 9900 mode from non-9900 mode (or when
		pausing) */
	vmResult	(*restart)(void);
	
	/* multi-time restop; done in context switches
		from non-9900 mode to 9900 mode. */
	vmResult	(*restop)(void);

	union 
	{
		vmGenericModule	*generic;
		vmCPUModule		*cpu;
		vmVideoModule	*video;
		vmSoundModule	*sound;
		vmKeyboardModule *kbd;
		vmDSRModule		*dsr;
	}	m;

	/* set to zero; used during runtime */
	u32				runtimeflags;
	struct vmModule *next;		/* of this type */
	
}	vmModule;

/***********************************/

int			vmModulesInit(void);
int			vmModulesTerm(void);
void		vmAddModuleCommands(void);

int			vmRegisterModules(vmModule **module);

vmResult	vmDetectModule(vmModule *module);
vmResult	vmEnableModule(vmModule *module);
vmResult	vmDisableModule(vmModule *module);
vmResult	vmInitModule(vmModule *module);
vmResult	vmTermModule(vmModule *module);
vmResult	vmInstallModule(vmModule *module);
vmResult	vmRestartModule(vmModule *module);
vmResult	vmRestopModule(vmModule *module);

vmModule *vmLookupModule(char *tag);

int	vmDetectModules(void);
int	vmSelectStartupModules(void);
int	vmValidateModuleSelection(bool startup, bool incremental);
int	vmInitModules(void);
int	vmTermModules(void);
int	vmEnableModules(void);
int	vmDisableModules(void);
int	vmRestartModules(void);
int	vmRestopModules(void);
int vmClearUserFlagsOnModules(void);

void
module_logger(vmModule *module, int flags, const char *format, ...);

extern vmModule *vmCPU, *vmVideo, *vmSound,
				*vmKeyboard, *vmDSR;
extern vmModule *vmCPUMain, *vmVideoMain, *vmSoundMain,
				*vmKeyboardMain;
extern int vmCPUCount, vmVideoCount, vmSoundCount,
			vmKeyboardCount, vmDSRCount;

INLINE vmResult MODULE_ITERATE(vmModule *start, vmResult (*routine)(vmModule *))
{
	vmModule *ptr = start; 
	vmResult res, ret = vmOk;
	while (ptr) {
		res = routine(ptr);
		if (res != vmOk)
			ret = res;
		ptr = ptr->next;
	}
	return ret;
}

#include "cexit.h"

#endif
