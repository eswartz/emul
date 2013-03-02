/*
  moduleconfig.h				-- external declarations for V9t9 modules
  
  Rename me!

  And fix this awful hard-coded architecture!

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

#ifndef __MODULECONFIG_H__
#define __MODULECONFIG_H__

#include "v9t9_module.h"

#include "centry.h"

extern vmModule emulate9900CPU;

#if defined(EMU_DISK_DSR)
extern vmModule emuDiskDSR;
#endif

#if defined(REAL_DISK_DSR)
extern vmModule realDiskDSR;
#endif

#if defined(REAL_RS232_DSR)
extern vmModule realRS232DSR;
#endif

#if defined(EMU_PIO_DSR)
extern vmModule emuPIODSR;
#endif

#if defined(LINUX_SVGA_VIDEO)
extern vmModule svgaVideo;
#endif

#if defined(X_WIN_VIDEO)
extern vmModule X_Video;
#endif

#if defined(GTK_VIDEO) /*&& defined(WIN32_VIDEO)a*/
extern vmModule gtkVideo;
#endif

#if defined(QTE_VIDEO)
extern vmModule QTE_Video;
#endif

#if defined(WIN32_VIDEO)
extern vmModule win32DrawDibVideo;
extern vmModule win32DirectDrawVideo;
#endif

#if defined(NULL_VIDEO)
extern vmModule nullVideo;
#endif

#if defined(LINUX_SVGA_KEYBOARD)
extern vmModule linuxKeyboard;
#endif

#if defined(X_WIN_KEYBOARD)
extern vmModule X_Keyboard;
#endif

#if defined(GTK_KEYBOARD) //&& !defined(WIN32_KEYBOARD)
extern vmModule gtkKeyboard;
#endif

#if defined(QTE_KEYBOARD)
extern vmModule QTE_Keyboard;
#endif

#if defined(WIN32_KEYBOARD)
extern vmModule	win32Keyboard;
#endif

#if defined(NULL_KEYBOARD)
extern vmModule nullKeyboard;
#endif

#if defined(LINUX_SPEAKER_SOUND)
extern vmModule linuxSpeakerSound;
#endif

#if defined(OSS_SOUND)
extern vmModule ossSound;
#endif

#if defined(ALSA_SOUND)
extern vmModule alsaSound;
#endif

#if defined(ESD_SOUND)
extern vmModule esdSound;
#endif

#if defined(WIN32_SOUND)
extern vmModule win32Sound;
#endif

#if defined(NULL_SOUND)
extern vmModule nullSound;
#endif

/******************************/

#ifdef __V9t9__

static vmModule *installed_modules[] =
{
	&emulate9900CPU,

#if defined(REAL_DISK_DSR)
	&realDiskDSR,
#endif
#if defined(EMU_DISK_DSR)
	&emuDiskDSR,
#endif
#if defined(REAL_RS232_DSR)
	&realRS232DSR,
#endif
#if defined(EMU_PIO_DSR)
	&emuPIODSR,
#endif
	
#if defined(GTK_VIDEO) //&& !defined(WIN32_VIDEO)
	&gtkVideo,
#endif
#if defined(QTE_VIDEO)
	&QTE_Video,
#endif
#if defined(X_WIN_VIDEO)
	&X_Video,
#endif
#if defined(LINUX_SVGA_VIDEO)
	&svgaVideo,
#endif		
#if defined(WIN32_VIDEO)
	&win32DrawDibVideo,
	&win32DirectDrawVideo,
#endif
#if defined(NULL_VIDEO)
	&nullVideo,
#endif

#if defined(GTK_KEYBOARD) //&& !defined(WIN32_KEYBOARD)
	&gtkKeyboard,
#endif
#if defined(QTE_KEYBOARD)
	&QTE_Keyboard,
#endif
#if defined(X_WIN_KEYBOARD)
	&X_Keyboard,
#endif
#if defined(LINUX_SVGA_KEYBOARD)
	&linuxKeyboard,
#endif
#if defined(WIN32_KEYBOARD)
	&win32Keyboard,
#endif
#if defined(NULL_KEYBOARD)
	&nullKeyboard,
#endif

	// order is important here; the first
	// registered module matched is used by default
#if defined(ALSA_SOUND)
	&alsaSound,
#endif
#if defined(OSS_SOUND)
	&ossSound,
#endif
#if defined(ESD_SOUND)
	&esdSound,
#endif
#if defined(LINUX_SPEAKER_SOUND)
	&linuxSpeakerSound,
#endif
#if defined(WIN32_SOUND)
	&win32Sound,
#endif
#if defined(NULL_SOUND)
	&nullSound,
#endif		

	NULL
};

#endif

#include "cexit.h"

#endif
