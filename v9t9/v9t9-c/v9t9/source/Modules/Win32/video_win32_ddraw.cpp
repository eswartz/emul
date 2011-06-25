/*
  video_win32_ddraw.cpp			-- V9t9 module for Win32 video interface

  (c) 1994-2000 Edward Swartz

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
 *	DirectDraw module for V9t9
 *
 *	This module uses the DirectDraw library to make a
 * 	full-screen interface to v9t9.
 *
 */
 
#include "winv9t9.h"
#include <malloc.h>
//#define HMONITOR_DECLARED
#include <ddraw.h>

#include "v9t9_common.h"
#include "9900.h"
#include "memory.h"
#include "vdp.h"
#include "timer.h"
#include "v9t9.h"
#include "video.h"

#include "log.h"
#define _L LOG_VIDEO | LOG_INFO
#include "resource.h"

#include "video_win32.h"
#include "video_win32_ddraw.h"


static	BOOL	bActive;					// is application active?

//HWND	hWndScreen;							// the DD window

static	LPDIRECTDRAW2		lpDD;			// DirectDraw object
static	LPDIRECTDRAWSURFACE	lpDDSPrimary;	// DirectDraw primary surface
static	LPDIRECTDRAWSURFACE	lpDDSBack;		// DirectDraw back buffer
static	LPDIRECTDRAWSURFACE	lpDDSBitmap;	// Surface of memory bitmap
static	LPDIRECTDRAWPALETTE	lpDDPal;		// The primary surface palette

static	UINT	fsx = 320, fsy = 200;		// full-screen resolution
static	RECT	screenrect;					// client part of window for fullscreen

//	this restores memory when surface memory is lost
static int	dd_restore(void)
{
	HRESULT ret;
	
	if (!lpDDSPrimary)
		return 1;
	ret = lpDDSPrimary->Restore();
	if (ret == DD_OK)
	{
		if (lpDDSBitmap)
		{
			ret = lpDDSBitmap->Restore();
			if (ret == DD_OK)
				return 1;
			else
			{
				module_logger(&win32DirectDrawVideo, _L|LOG_ERROR|L_0, _("video_restore: could not restore bitmap %8X\n"), ret);
				return 0;
			}
		}
		return 1;
	}
	else
	{
		module_logger(&win32DirectDrawVideo, _L|LOG_ERROR|L_0, _("video_restore: could not restore primary %8X\n"), ret);
		return 0;
	}
}

static void	win_SetFullScreenMode(void)
{
	HRESULT	ret;

//	log("win_SetFullScreenMode\n");
	if (lpDD)
	{
		/* get exclusive mode (for fullscreen and memory locking access) */
		ret = lpDD->SetCooperativeLevel(hWndScreen,
				DDSCL_EXCLUSIVE | DDSCL_FULLSCREEN | DDSCL_ALLOWREBOOT |
				DDSCL_ALLOWMODEX);
		if (ret != DD_OK)
			DDFAIL(_("SetCooperativeLevel failed (%d)\n"), ret);
		
		/*  Set our video mode */
		ret = lpDD->SetDisplayMode(fsx, fsy, 8, 0, DDSDM_STANDARDVGAMODE);
		if (ret != DD_OK)
			DDFAIL(_("SetDisplayMode\n"), ret);
		
		/*  Use our palette */
		if (lpDDPal)
		{
			ret = lpDDPal->SetEntries(0 /*flags */, 0, 256, pals);
			if (ret != DD_OK)
				DDFAIL(_("SetEntries failed (%d)\n"), ret);
		}
		
		#warning fix this later
		// adjust rect for text mode!
		screenrect.left = 0;
		screenrect.top = 0;
		screenrect.right = 256;
		screenrect.bottom = 192;

	}
}

/*	Reset fullscreen mode */
static void	win_ResetFullScreenMode(void)
{
	HRESULT ret;

//	log("win_ResetFullScreenMode\n");
	if (lpDD)
	{
		/*  Take down the main screen */	
		ret = lpDD->SetCooperativeLevel(hWndScreen, DDSCL_NORMAL);
		if (ret != DD_OK)
			DDFAIL(_("SetCooperativeLevel (DDSCL_NORMAL)"), ret);
		lpDD->RestoreDisplayMode();
	}
	
}


////////////////

static	char 	szScreenClassName[] = "WinV9t9 DirectDraw Fullscreen";
static 	LRESULT CALLBACK ScreenWndProc(HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam);

static BOOL	win_registerDirectDrawWindowClass(HINSTANCE hInst, HINSTANCE hPreInst)
{
	WNDCLASS wc;

	/* 	DirectDraw window class */
	wc.lpszClassName 	= szScreenClassName;
	wc.hInstance 		= hInst;
	wc.lpfnWndProc		= ScreenWndProc;		
	wc.hCursor			= (HICON) NULL;
	wc.hIcon			= LoadIcon( (HINSTANCE)NULL, IDI_APPLICATION );
	wc.lpszMenuName		= (char *)NULL;
	wc.hbrBackground	= (HBRUSH)GetStockObject(BLACK_BRUSH);
	wc.style			= CS_OWNDC | CS_HREDRAW | CS_VREDRAW;
	wc.cbClsExtra		= 0;
	wc.cbWndExtra		= 0;

	if( !RegisterClass( &wc ) )
	{
		FAIL(_("RegisterClass szVideoClassName"));
		return FALSE;
	}
	
	return TRUE;
}

extern "C" vmModule win32DirectDrawVideo;

/*	Create a window containing the full-screen V9t9 */
static BOOL	win_createv9t9screen(HINSTANCE hInst, int nCmdShow)
{
	//log("win_createV9t9screen\n");
	
	/*  The DirectDraw screen window covers the whole screen */
	hWndScreen = CreateWindow( szScreenClassName,		
							_("V9t9-on-a-Monitor"),
							WS_VISIBLE | WS_POPUP | WS_CAPTION | WS_SYSMENU,
							0, 0,							/* position */
							GetSystemMetrics(SM_CXSCREEN),
							GetSystemMetrics(SM_CYSCREEN),	/* width/height */
							(HWND)NULL,						/* parent */
							(HMENU)NULL,					/* menubar */
							hInst,
							(LPSTR)NULL	);

	if (!hWndScreen)
	{
		module_logger(&win32DirectDrawVideo, _L|LOG_USER|LOG_ERROR, _("could not create window\n"));
		return FALSE;
	}

	return TRUE;
}

static	BOOL	win_createDirectDrawWindows(HINSTANCE hInst, int nCmdShow)
{
	if (!win_createv9t9screen(hInst, nCmdShow))
		return FALSE;
		
	ShowWindow(hWndScreen, SW_HIDE);
	hWndApp = hWndScreen;

	return TRUE;
}

/*************************************/
/*	Prototypes for routines common to window and full screen */

static void	video_paint(void);				// start repainting
static void	screen_invalidate(LPRECT rect);	// indicate need to repaint later

static 	u8 *screen_lock(u32 *pitch)
{
	HRESULT ret;
	DDSURFACEDESC ddsc;

	dd_restore();
	ddsc.dwSize = sizeof(ddsc);
	ret = lpDDSPrimary->Lock(&screenrect, &ddsc, DDLOCK_WAIT, 0);
	if (ret != DD_OK)
	{
		module_logger(&win32DirectDrawVideo, _L|LOG_ERROR|LOG_USER, _("error in Lock with %8X\n"),ret);
		return NULL;
	}

	/* locked */
	//log("lpSurface = %p\n", ddsc.lpSurface);

	*pitch = ddsc.lPitch;
	return (u8 *)ddsc.lpSurface;
}

static	void screen_unlock(u8 *screen)
{
	lpDDSPrimary->Unlock(screen);
}


/*	Handle update. */
static	bool screen_clearsides;
static void	screen_paint(LPRECT logrect, LPRECT physrect)
{
	if (!bActive /* || !fullscreen*/)
		return;
	
	if (!IsRectEmpty(logrect))
	{
		u8 	*video_buffer, *orig, *bmp;
		u32	pitch;
		int xbord, ybord;
		
		orig = video_buffer = screen_lock(&pitch);
		if (!orig)
			return;
	
		xbord = (fsx - win_logxsize) / 2;
		ybord = (fsy - win_logysize) / 2;
				
//		log("screen_paint (%d,%d)\n", xbord, ybord);	
	
		if (screen_clearsides)
		{
			for (int idx = 0; idx < ybord; idx++)
			{
				memset(video_buffer + idx*pitch, BG, fsx);
				memset(video_buffer + (fsy-idx-1)*pitch, BG, fsx);
			}
			
			for (int idx = 0; idx < fsy; idx++)
			{
				memset(video_buffer, BG, xbord);
				memset(video_buffer + fsx - xbord, BG, xbord);
				video_buffer += pitch;
			}
			screen_clearsides = false;
		}
	
/*		for (int idx = 0; idx<192; idx++)
		{
			bmp -= 256;
			memcpy(video_buffer + xbord, bmp, win_logxsize);
			video_buffer += pitch;
		}*/

		bmp = win_bitmap + (256 - logrect->top) * 256 + logrect->left;
		video_buffer = orig + (ybord + logrect->top) * pitch + logrect->left + xbord;

		for (int idx = logrect->top; idx < logrect->bottom; idx++)
		{
			bmp -= 256;
			memcpy(video_buffer, bmp, logrect->right - logrect->left);
			video_buffer += pitch;
		}
		
		screen_unlock(orig);
	}
}

static void screen_resize(u32 newxsize, u32 newysize)
{
	u8	*video_buffer, *orig;
	u32	pitch;
	int xbord,ybord;
	
	orig = video_buffer = screen_lock(&pitch);
	if (!orig)
		return;
	
	xbord = (fsx - newxsize) / 2;
	ybord = (fsy - newysize) / 2;
	
//	log("screen_resize [%d,%d]\n", xbord, ybord);
	video_buffer = orig;
	for (int idx = 0; idx < ybord; idx++)
	{
		memset(video_buffer, BG, fsx);
		video_buffer += pitch;
	}

	video_buffer = orig + (fsy - ybord) * pitch;
	for (int idx = 0; idx < ybord; idx++)
	{
		memset(video_buffer, BG, fsx);
		video_buffer += pitch;
	}
	
	video_buffer = orig + ybord*pitch;
	for (int idx = 0; idx < newysize; idx++)
	{
		memset(video_buffer, BG, xbord);
		memset(video_buffer + fsx - xbord, BG, xbord);
		video_buffer += pitch;
	}
	
	screen_unlock(orig);
}

static void screen_updatepalette(void)
{
	HRESULT ret;
	int idx;
	if (!lpDDPal)
		return;
	//log("screen_updatepalette\n");
	
//	for (idx = 1; idx < 17; idx ++)
//		DD_RGBTOPAL(idx, win_rgbmap[idx]);
/*	ret = lpDDPal->SetEntries(0, 0, 17, pals+rand()%16);
	if (ret != DD_OK)
		error(_("videoupdatepalette:  SetPalette failed (%d)\n"), ret);*/
	for (idx = 0; idx < 17; idx ++)
		DD_RGBTOPAL(idx, win_rgbmap[idx]);
	ret = lpDDPal->SetEntries(0, 0, 17, pals);
	if (ret != DD_OK)
		module_logger(&win32DirectDrawVideo, _L|LOG_ERROR|LOG_USER, _("videoupdatepalette:  SetPalette failed (%d)\n"), ret);
}

/*	Translate rectangles */
static void	screen_phys_to_log(LPRECT physrect, LPRECT logrect)
{
	RECT rect = screenrect;
	OffsetRect(&rect, -screenrect.left, -screenrect.top);
	SetRect(logrect, 
		((physrect->left - screenrect.left) * win_logxsize + rect.right - 1) / rect.right, 
		((physrect->top - screenrect.top) * win_logysize + rect.bottom - 1) / rect.bottom,
		((physrect->right - screenrect.left) * win_logxsize + rect.right - 1) / rect.right,
		((physrect->bottom - screenrect.top) * win_logysize + rect.bottom - 1)/ rect.bottom);
}

static void	screen_log_to_phys(LPRECT logrect, LPRECT physrect)
{
	RECT rect = screenrect;
	
	OffsetRect(&rect, -screenrect.left, -screenrect.top);
	SetRect(physrect, 
		(logrect->left * rect.right ) / win_logxsize + screenrect.left, 
		(logrect->top *  rect.bottom) / win_logysize + screenrect.top,
		(logrect->right * rect.right ) / win_logxsize + screenrect.left,
		(logrect->bottom * rect.bottom ) / win_logysize + screenrect.top);
}

static void	screen_invalidate(LPRECT rect)
{
	RECT physrect, logrect;

	if (rect == NULL)
	{
		screen_clearsides = true;
		logrect = screenrect;
		screen_updatepalette();
	}
	else
		logrect = *rect;
		
	screen_log_to_phys(&logrect, &physrect);

	// for fullscreen, repaint it immediately
	screen_paint(&logrect, &physrect);
}

LRESULT CALLBACK ScreenWndProc( HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam )
{
	if (win32DirectDrawVideo.runtimeflags & vmRTUnselected)
		return DefWindowProc(hWnd, messg, wParam, lParam);
	switch (messg)
	{
		case WM_ACTIVATEAPP:
			bActive = (BOOL)wParam;
			break;
		 
		case WM_SETCURSOR:
			SetCursor((HICON)NULL);
			return TRUE;

		// this message is sent very rarely,
		// notably when using ALT-TAB to switch back.
		case WM_PAINT:
		{
			// make Windows happy
			RedrawWindow(hWnd, NULL, NULL, RDW_VALIDATE);
			
			// make user happy
			screen_invalidate(NULL);
			return 0;
		}
		
		case WM_SYSKEYDOWN:
		case WM_SYSKEYUP:
		case WM_KEYDOWN:
		case WM_KEYUP:
			return KeyboardMessageHandler(hWnd, messg, wParam, lParam);
			break;

		case WM_CLOSE:
		case WM_DESTROY:
			command_exec_text("Die\n");
			break;
			
		default:
			return DefWindowProc(hWnd, messg, wParam, lParam);
	}

	return 0;
}

/******************************************/

#if 0
#pragma mark -
#endif

/*	Redraw entire window. */
static void	screen_complete_redraw(void)
{
	screen_updatepalette();
	screen_invalidate(NULL);
}

/*******************************************************/

static vmResult	win32ddraw_resize(u32 newxsize,u32 newysize)
{
	u8	*a;
	int		y,b;

	if (newxsize != win_logxsize)
	{
		b = 256 - (newxsize/2);
		if (b > 0)
		{
			a = win_bitmap;
			for (y=0; y < newysize; y ++)
			{
				memset(a, BG, b);
				memset(a + 256 - b, BG, b);
				a += 256;
			}
		}
		
		win_logxsize = newxsize;
		win_logysize = newysize;
		
		screen_resize(newxsize, newysize);
	}
	return vmOk;
}


static vmResult	win32ddraw_setfgbg(u8 fg, u8 bg)
{
	win_rgbmap[0] = bg ? bg : 1;
	win_rgbmap[16] = fg ? fg : 1;
	screen_updatepalette();
	return vmOk;
}

static vmResult	win32ddraw_setblank(u8 bg)
{
	int x;
#ifdef USEPALETTESWITCHING
	for (x=1; x<16; x++)
		win_rgbmap[x] = bg;
	screen_updatepalette();
#else
	memset(bitmap, BG, 256*256);
	screen_invalidate(NULL);
#endif
	return vmOk;
}

static vmResult win32ddraw_resetfromblank(void)
{
	int x;
	for (x=1; x<16; x++)
		win_rgbmap[x] = x;
	screen_updatepalette();
	screen_invalidate(NULL);
	return vmOk;
}


/***********************************************************/

static void ddraw_updatelist(struct updateblock *ptr, int num)
{
	int width = 8;
	int	i;
	int offs;
	u8 *scrn;
	
	RECT rect, updaterect;
	
	if (!num)
		return;
		
	SetRectEmpty(&updaterect);
	
	offs=(win_logxsize/2)-128;
	while (num--)
	{	
		// Windows bitmaps are upside down...
		scrn = win_bitmap + ((256 - ptr->r) * 256) + ptr->c + offs;
		for (i=0; i<8; i++)
		{
			scrn -= 256;
			memcpy(scrn,ptr->data,width);
			ptr->data+=UPDATEBLOCK_ROW_STRIDE;
		}
		
		SetRect(&rect, ptr->c, ptr->r, ptr->c + width, ptr->r + 8);
		UnionRect(&updaterect, &updaterect, &rect);
		
		ptr++;
	}
	
	screen_invalidate(&updaterect);
	
}

static vmResult	win32ddraw_updatelist(struct updateblock *ptr, int num)
{
	ddraw_updatelist(ptr, num);
	return vmOk;
}

/****************************************/
#if 0
#pragma mark -
#endif

static vmResult	win32ddraw_detect(void)
{
	/* we would crash in WinMain if we didn't detect */
	return vmOk;
}

static vmResult	win32ddraw_init(void)
{
	LPDIRECTDRAW	oldDraw;
	int ret;

	int	x;
	
	if (!win_registerDirectDrawWindowClass(myHInst, myHPreInst) ||
		!win_createDirectDrawWindows(myHInst, mynCmdShow))
		return vmInternalError;

//	log("init\n");

	for (x=0; x<16; x++)
		win_rgbmap[x]=x;
	win_rgbmap[16] = 15;

	/*	DirectDraw initialization */
	ret = DirectDrawCreate((GUID *)NULL, &oldDraw, NULL);
	if (ret != DD_OK)
		DDFAIL("DirectDrawCreate", ret);

	if (!SUCCEEDED(oldDraw->QueryInterface(IID_IDirectDraw2,
		        reinterpret_cast<void**>(&lpDD))))
		DDFAIL("QueryInterface", -1);

// --
		
	features|=FE_SHOWVIDEO|FE_VIDEO;
	
	return vmOk;
}

static vmResult	win32ddraw_enable(void)
{
	DDSCAPS			ddsCaps;
	DDSURFACEDESC	ddsd;
	int ret;
	int	idx;

//	log("enable\n");
	/*	Enable fullscreen mode so we can
		properly create the surfaces. */

	win_SetFullScreenMode();

	/*	Create primary surface */
	memset((void *)&ddsd, 0, sizeof(ddsd));
	ddsd.dwSize = sizeof(ddsd);
	ddsd.dwFlags = DDSD_CAPS | DDSD_BACKBUFFERCOUNT;
	ddsd.ddsCaps.dwCaps = DDSCAPS_PRIMARYSURFACE | DDSCAPS_COMPLEX | DDSCAPS_FLIP;
	ddsd.dwBackBufferCount = 1;
	
	ret = lpDD->CreateSurface(&ddsd, &lpDDSPrimary, NULL);
	if (ret != DD_OK)
	{
		module_logger(&win32DirectDrawVideo, _L|LOG_ERROR, _("CreateSurface (%d)\n"), ret);
		DDFAIL(_("CreateSurface (screen)"), ret);
	}

	/*	Create back buffer, for page flipping */
	ddsCaps.dwCaps = DDSCAPS_BACKBUFFER;
	ret = lpDDSPrimary->GetAttachedSurface(&ddsCaps, &lpDDSBack);
	if (ret != DD_OK)
		DDFAIL(_("GetAttachedSurface (screen)"), ret);

#if 0
	/*	Create offscreen surface */
	ddsd.dwFlags = DDSD_CAPS | DDSD_WIDTH | DDSD_HEIGHT;
	ddsd.ddsCaps.dwCaps = DDSCAPS_OFFSCREENPLAIN;
	ddsd.dwWidth = fsx;	
	ddsd.dwHeight = fsy;
	
	ret = lpDD->CreateSurface(&ddsd, &lpDDSBitmap, NULL);
	if (ret != DD_OK)
		DDFAIL(_("CreateSurface (offscreen)"), ret);
#endif

	/*	Set up the palette. */
	for (idx = 0; idx < 256; idx++)
	{
		DD_RGBTOPAL(idx,
			idx == 0 ? 1 :
			idx == 16 ? 15 :
			idx > 16 ? 8 : idx);
	}
	
/*	for (idx = DD_PALBASE+17; idx < 246; idx++)
	{
		pals[idx].peFlags = PC_NOCOLLAPSE;
		pals[idx].peRed = pals[idx].peGreen = pals[idx].peBlue = idx;
	}
*/
/*	for (idx = 0; idx < 256; idx++)
	{
		pals[idx].peFlags = PC_NOCOLLAPSE;
		pals[idx].peRed = pals[idx].peGreen = pals[idx].peBlue = rand();
	}
*/

	/*	Create the palette */
	ret = lpDD->CreatePalette(DDPCAPS_8BIT | DDPCAPS_ALLOW256, 
								pals, &lpDDPal, (IUnknown FAR *)NULL);
	if (ret != DD_OK)
		DDFAIL(_("CreatePalette failed"), ret);

	/*  attach the palette [this may be redundant if we do it twice] */	
	if (lpDDPal)
	{
		ret = lpDDSPrimary->SetPalette(lpDDPal);
		if (ret != DD_OK)
			DDFAIL(_("deo failed (%d)\n"), ret);
		
//		/*	turn it on [it should already be so, but make sure] */
//		ret = lpDDPal->SetEntries(0 /*flags */, 0, 256, pals);
//		if (ret != DD_OK)
//			DDFAIL(_("SetEntries failed (%d)\n"), ret);
	}

	/*	Reset screen if we might not be primary */
	win_ResetFullScreenMode();

//	win_video_event_tag = TM_UniqueTag();
//	TM_SetEvent(win_video_event_tag, TM_HZ*100/30, 0,
//				TM_REPEAT|TM_FUNC, win_video_update);


	return vmOk;
}

static vmResult	win32ddraw_disable(void)
{
//	log(_("disable\n"));
	return vmOk;
}

static vmResult	win32ddraw_restart(void)
{
//	log(_("restart\n"));
	if (features & FE_SHOWVIDEO)
	{
		win_SetFullScreenMode();
		screen_complete_redraw();
	}
	//ShowWindow(hWndScreen, SW_RESTORE);
	return vmOk;
}

static vmResult	win32ddraw_restop(void)
{
//	log(_("restop\n"));
	if (features & FE_SHOWVIDEO)
	{
//		win_wasfullscreen = fullscreen;
		win_ResetFullScreenMode();
		ShowWindow(hWndScreen, SW_HIDE);
	}
	return vmOk;
}

static vmResult	win32ddraw_term(void)
{
	if (lpDD)
	{
		if (lpDDSPrimary)
		{
			// releases lpDDSBack also
			lpDDSPrimary->Release();
			lpDDSPrimary = NULL;
		}
		if (lpDDSBitmap)
		{
			lpDDSBitmap->Release();
			lpDDSBitmap = NULL;
		}
		lpDD = NULL;
	}
	return vmOk;
}

static vmVideoModule win32ddraw_DDrawVideoModule =
{
	3,
	win32ddraw_updatelist,
	win32ddraw_resize,
	win32ddraw_setfgbg,
	win32ddraw_setblank,
	win32ddraw_resetfromblank
};

extern "C" vmModule win32DirectDrawVideo =
{
	3,
	"Win32 DirectDraw video",
	"vidWin32DirectDraw",
	
	vmTypeVideo,
	(vmFlags) (vmFlagsExclusive | vmFlagsOneShotEnable | vmFlagsOneShotDisable),
	
	win32ddraw_detect,
	win32ddraw_init,
	win32ddraw_term,
	win32ddraw_enable,
	win32ddraw_disable,
	win32ddraw_restart,
	win32ddraw_restop,
	{ (vmGenericModule *)&win32ddraw_DDrawVideoModule }
};

