/*
  video_null.c					-- V9t9 module for null video.

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

#include "v9t9_common.h"
#define _L LOG_USER|LOG_INFO

static      vmResult
nullvideo_detect(void)
{
	return vmOk;
}

static      vmResult
nullvideo_init(void)
{
	return vmOk;
}

static      vmResult
nullvideo_enable(void)
{
	return vmOk;
}

static      vmResult
nullvideo_disable(void)
{
	return vmOk;
}

static      vmResult
nullvideo_restart(void)
{
	module_logger(&nullVideo, LOG_WARN|LOG_USER, _("No video driver loaded\n"));
	return vmOk;
}

static      vmResult
nullvideo_restop(void)
{
	return vmOk;
}

static      vmResult
nullvideo_term(void)
{
	return vmOk;
}

/**************/

static      vmResult
nullvideo_updatelist(struct updateblock *ptr, int num)
{
	return vmOk;
}

static      vmResult
nullvideo_resize(u32 newxsize, u32 newysize)
{
	return vmOk;
}

static      vmResult
nullvideo_setfgbg(u8 fg, u8 bg)
{
	return vmOk;
}

static      vmResult
nullvideo_setblank(u8 bg)
{
	return vmOk;
}

static      vmResult
nullvideo_resetfromblank(void)
{
	return vmOk;
}

/***********************************************************/

static vmVideoModule nullvideo_videoModule = {
	3,
	nullvideo_updatelist,
	nullvideo_resize,
	nullvideo_setfgbg,
	nullvideo_setblank,
	nullvideo_resetfromblank
};

vmModule    nullVideo = {
	3,
	"Null video",
	"vidNull",

	vmTypeVideo,
	vmFlagsExclusive,

	nullvideo_detect,
	nullvideo_init,
	nullvideo_term,
	nullvideo_enable,
	nullvideo_disable,
	nullvideo_restart,
	nullvideo_restop,
	{(vmGenericModule *) & nullvideo_videoModule}
};
