/*
  video_win32_drawdib.c			-- V9t9 module for Win32 video interface

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
 *	DrawDibs module for V9t9
 *
 *	This module uses the DrawDIB library which enables direct
 *	bitmap drawing to video memory.  This is much faster than
 *	anything else Win32 has to offer.
 *
 */
 
#include "winv9t9.h"
#include <malloc.h>

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
#include "video_win32_drawdib.h"

#if MIXED_GTK
#include <gtk/gtk.h>
#include <gdk/win32/gdkwin32.h>
#include "gtkloop.h"
#endif

#if STATUS_WINDOW
HWND hWndStatus;
#endif

HWND hWndWindow;

//static	BOOL	windowed;					// running in windowed mode?
static	HDRAWDIB dib;						// drawdib handle for window
static HPALETTE pal;		/* my palette */

///////////

static	char 	szWindowClassName[] = "WinV9t9";
#if STATUS_WINDOW
static	char 	szStatusClassName[] = "WinV9t9 Status";
#endif

static LRESULT CALLBACK WindowWndProc(HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam);
#if STATUS_WINDOW
static LRESULT CALLBACK StatusWndProc(HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam);
#endif

static BOOL	win_registerDrawDibWindowClass(HINSTANCE hInst, HINSTANCE hPreInst)
{
	WNDCLASS wc;

#if MIXED_GTK
	if (win_frontend != FE_GTK)
#endif
	{
	
	/*	DrawDibs-window window class */
	wc.lpszClassName 	= szWindowClassName;
	wc.hInstance 		= hInst;
	wc.lpfnWndProc		= WindowWndProc;
	wc.hCursor			= LoadCursor( (HINSTANCE)NULL, IDC_ARROW );
	wc.hIcon			= LoadIcon( (HINSTANCE)NULL, IDI_APPLICATION );
	wc.lpszMenuName		= win_frontend != FE_GTK ? MAKEINTRESOURCE(IDR_MENU1) : NULL;
	wc.hbrBackground	= (HBRUSH)NULL;		/* I will redraw it all */
	wc.style			= CS_BYTEALIGNCLIENT;
	wc.cbClsExtra		= 0;
	wc.cbWndExtra		= 0;

	if( !RegisterClass( &wc ) )
	{
		FAIL(_("RegisterClass szWindowClassName"));
		return FALSE;
	}

#if STATUS_WINDOW		
	/*	Status window class */
	wc.lpszClassName 	= szStatusClassName;
	wc.hInstance 		= hInst;
	wc.lpfnWndProc		= StatusWndProc;
	wc.hCursor			= LoadCursor((HINSTANCE)NULL, IDC_ARROW);
	wc.hIcon			= LoadIcon((HINSTANCE)NULL, IDI_APPLICATION);
	wc.lpszMenuName		= (char *)NULL;
	wc.hbrBackground	= (HBRUSH)GetStockObject(LTGRAY_BRUSH);
	wc.style			= 0;
	wc.cbClsExtra		= 0;
	wc.cbWndExtra		= 0;

	if( !RegisterClass( &wc ) )
	{
		FAIL(_("RegisterClass szStatusClassName"));
		return FALSE;
	}
#endif

	}
	return TRUE;
}

/*	Create the window containing a windowed V9t9 */
static BOOL	win_createv9t9window(HINSTANCE hInst, int nCmdShow)
{
	RECT rect;

	//log("win_createv9t9window\n");
	
	/*	Create video-screen window */
	SetRect(&rect, 0, 0, 256*2, 192*2);
	AdjustWindowRect(&rect, WS_OVERLAPPEDWINDOW, 
				win_frontend != FE_GTK /* menubar */);
	hWndWindow = CreateWindow(szWindowClassName,		
							_("V9t9-in-a-Box"),
							WS_OVERLAPPEDWINDOW,
							CW_USEDEFAULT, CW_USEDEFAULT,	/* position */
							rect.right-rect.left, 
							rect.bottom-rect.top,			/* width/height */
							(HWND)NULL,						/* parent */
							(HMENU)NULL,					/* use class menubar */
							hInst,
							(LPSTR)NULL	);

	if (!hWndWindow)
	{
		module_logger(&win32DrawDibVideo, _L|LOG_ERROR|LOG_USER,_("could not create window\n"));
		return FALSE;
	}
	
	return TRUE;
}

#if STATUS_WINDOW
/*	Create the status window accompanying the window */
static BOOL	win_createv9t9status(HINSTANCE hInst, int nCmdShow)
{
	RECT rect;

	/*  Place status window under the video window 
		(it's a little wider, though) */

	//log("win_createv9t9status\n");
	
	GetWindowRect(hWndWindow, &rect);
	rect.top = rect.bottom + 16;
	rect.bottom = rect.top + 16*4;
	AdjustWindowRect(&rect, WS_OVERLAPPEDWINDOW, 0);
	
	hWndStatus = CreateWindow(szStatusClassName,		
							_("V9t9 Status"),
							WS_OVERLAPPEDWINDOW,
							rect.left, rect.top,				/* position */
							rect.right-rect.left, 
							rect.bottom-rect.top,				/* width/height */
							(HWND)NULL,							/* parent */
							(HMENU)NULL,						/* menubar */
							hInst,
							(LPSTR)NULL	);

	if (!hWndStatus)
	{
		module_logger(&win32DrawDibVideo, _L|LOG_ERROR|LOG_USER,_("could not create window\n"));
		return FALSE;
	}
	else
		return TRUE;
}		
#endif

static BOOL	win_createDrawDibWindows(HINSTANCE hInst, int nCmdShow)
{
	if (!win_registerDrawDibWindowClass(myHInst, myHPreInst))
		return FALSE;

	if (!win_createv9t9window(hInst, nCmdShow) 
#if STATUS_WINDOW	
		|| !win_createv9t9status(hInst, nCmdShow)
#endif		
		)
		return FALSE;
	
	ShowWindow(hWndWindow, SW_HIDE);

	hWndApp = hWndWindow;
	return TRUE;
}

/*************************************/
/*	Prototypes for routines common to window and full screen */

static void	video_paint(void);				// start repainting
static void	window_invalidate(LPRECT rect);	// indicate need to repaint later

#if 0
#pragma mark -
#endif

#if STATUS_WINDOW

static UINT lastinfo, lastchar, lastsys;

static void	status_redraw(void)
{
	InvalidateRect(hWndStatus, (RECT *)NULL, TRUE);
	UpdateWindow(hWndStatus);
}

/*	Redraw entire status window. */
static void	status_complete_redraw(void)
{
	status_redraw();
}

static void	status_paint(HWND hWnd)
{
	char keyval[32];
	RECT rect;
	HBRUSH hbr;
	PAINTSTRUCT pstruct;
	HDC hdc;

	hdc = BeginPaint(hWnd, &pstruct);

	GetClientRect(hWnd, &rect);
	hbr = (HBRUSH)GetStockObject(DKGRAY_BRUSH);
	FillRect(hdc, &rect, hbr);

	sprintf(keyval, "%02X %02X %08X %d", lastchar, lastsys, lastinfo, TM_GetTicks());
	TextOut(hdc, 0, rect.top, keyval, strlen(keyval));
	sprintf(keyval, "%04X %04X %04X", pc, wp, vdp_mmio_get_addr());
	TextOut(hdc, 0, rect.top + 16, keyval, strlen(keyval));
	
//	TextOut(hdc, 0, rect.top + 32, statuslines[0], strlen(statuslines[0]));
//	TextOut(hdc, 0, rect.top + 48, statuslines[1], strlen(statuslines[1]));
//	TextOut(hdc, 0, rect.top + 64, statuslines[2], strlen(statuslines[2]));
	
	ReleaseDC(hWnd, hdc);
	EndPaint(hWnd, &pstruct);
}

LRESULT CALLBACK StatusWndProc( HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam )
{
	switch (messg)
	{
		case WM_PAINT:
			if (IsIconic(hWnd))
				return 1;
		
			status_paint(hWnd);
			return 0;

		case WM_DESTROY:
			hWndStatus = 0;
			return TRUE;

		
		default:
			return DefWindowProc(hWnd, messg, wParam, lParam);
	}

	return 0;
}

#endif	// STATUS_WINDOW

/**********************************/


/**************************************************/

#if 0
#pragma mark -
#endif

/*	Translate rectangles */
static void	window_phys_to_log(LPRECT physrect, LPRECT logrect)
{
	RECT rect;

	// we fix the client size at a good multiple of win_logxsize x win_logysize
	GetClientRect(hWndWindow, &rect);
	SetRect(logrect, 
		(physrect->left * win_logxsize + rect.right - 1) / rect.right, 
		(physrect->top * win_logysize + rect.bottom - 1) / rect.bottom,
		(physrect->right * win_logxsize + rect.right - 1) / rect.right,
		(physrect->bottom * win_logysize + rect.bottom - 1)/ rect.bottom);
}

static void	window_log_to_phys(LPRECT logrect, LPRECT physrect)
{
	RECT rect;
	
	GetClientRect(hWndWindow, &rect);
	SetRect(physrect, 
		(logrect->left * rect.right ) / win_logxsize, 
		(logrect->top *  rect.bottom) / win_logysize,
		(logrect->right * rect.right ) / win_logxsize,
		(logrect->bottom * rect.bottom ) / win_logysize);
}

#if 0
#pragma mark -
#endif

static bool	force_draws=true;	/* true: paint window directly, false: invalidate and wait for messages */

static int window_update_counter_tag;
static long long window_update_pixels;
static int window_total_seconds,  window_updates;
static int window_updates_second; //, window_max_updates_second = 60;

static void window_update_counter(int x)
{
	return;
	
	window_total_seconds++;
	if (window_total_seconds % 10 == 0)
	{
		module_logger(&win32DrawDibVideo, _L|LOG_USER, _("Refresh rate: %d pixels/frame, %1.2f (%d) frames/second\n"), 
			window_updates ? (long)(window_update_pixels / window_updates) : 0,
			window_updates / (float)window_total_seconds,
			window_updates_second / 10);
		window_updates_second = 0;
		window_updates /= 2;
		window_update_pixels /= 2;
	}
}

static	BOOL window_clearsides;


/*
 *	Force a paint of the window, 
 */
static void	window_paint_force_region(HRGN thergn)
{
//	RECT rect;
	RECT updaterect, logrect;
	HDC hdc;

	hdc = GetDC(hWndWindow);
	SelectClipRgn(hdc, thergn);
	GetRgnBox(thergn, &updaterect);

	if (!IsRectEmpty(&updaterect))
	{
		window_updates++;
		window_updates_second++;
	
		window_phys_to_log(&updaterect, &logrect);
		if (dib)
		DrawDibDraw(dib, hdc,
			updaterect.left,		
			updaterect.top, 						/* client coords, upper-left of dest rect */
			(updaterect.right - updaterect.left), 
			(updaterect.bottom - updaterect.top),	/* width and height of dest rect, -1 == use bitmap size */
			&win_bm->bmiHeader,
			win_bitmap,
			logrect.left, 
			logrect.top,
				/* source upper-left of bitmap rect */
			logrect.right - logrect.left,
			logrect.bottom - logrect.top,
				/* size of bitmap rect */
			0			/* DDF_xxx flags.  don't use DDF_HURRYUP! */
		);
	}
	
	ReleaseDC(hWndWindow, hdc);
}

/*
 *	Force a paint of the window, using given hdc
 */
static void	window_paint_force_rect(HDC hdc, LPRECT therect)
{
	RECT updaterect, logrect;

	logrect = *therect;
	window_log_to_phys(&logrect, &updaterect);

	if (dib)
		DrawDibDraw(dib, hdc,
			updaterect.left,		
			updaterect.top, 						/* client coords, upper-left of dest rect */
			(updaterect.right - updaterect.left), 
			(updaterect.bottom - updaterect.top),	/* width and height of dest rect, -1 == use bitmap size */
			&win_bm->bmiHeader,
			win_bitmap,
			logrect.left, 
			logrect.top,
				/* source upper-left of bitmap rect */
			logrect.right - logrect.left,
			logrect.bottom - logrect.top,
				/* size of bitmap rect */
			0			/* DDF_xxx flags.  don't use DDF_HURRYUP! */
		);
}

/*
 *	Paint according to a WM_PAINT message. 
 */
static void	window_paint_message(void)
{
//	RECT rect;
	RECT updaterect, logrect;
	PAINTSTRUCT pstruct;
	HDC hdc;

	hdc = BeginPaint(hWndWindow, &pstruct );
	updaterect = pstruct.rcPaint;
	window_phys_to_log(&updaterect, &logrect);
	window_update_pixels+=(updaterect.right-updaterect.left) *
						(updaterect.bottom-updaterect.top);

	if (!IsRectEmpty(&updaterect))
	{
		window_updates++;
		window_updates_second++;
	
		if (dib)
		DrawDibDraw(dib, hdc,
			updaterect.left,		
			updaterect.top, 						/* client coords, upper-left of dest rect */
			(updaterect.right - updaterect.left), 
			(updaterect.bottom - updaterect.top),	/* width and height of dest rect, -1 == use bitmap size */
			&win_bm->bmiHeader,
			win_bitmap,
			logrect.left, 
			logrect.top,
				/* source upper-left of bitmap rect */
			logrect.right - logrect.left,
			logrect.bottom - logrect.top,
				/* size of bitmap rect */
			0			/* DDF_xxx flags.  don't use DDF_HURRYUP! */
		);
	}
	
	ReleaseDC(hWndWindow, hdc);
	EndPaint(hWndWindow, &pstruct);
}

#if MIXED_GTK
extern void gtk_window_paint(RECT *rect)
{
	HDC hdc;
	RECT logrect;
	RECT updaterect = *rect;
	PAINTSTRUCT pstruct;
	
	GdkColor color;
	GdkGC *gc = gdk_gc_new(v9t9_drawing_area->window);
	color.red = 0xff;
	color.green = 0x40;
	color.blue = 0x10;
	gdk_colormap_alloc_color(NULL, &color, false, true);
	gdk_gc_set_foreground(gc, &color);
	gdk_draw_rectangle(v9t9_drawing_area->window, gc, true, rect->left, rect->top, 
		rect->right-rect->left, rect->bottom-rect->top);
	gdk_gc_unref(gc);
		return;
	
	window_phys_to_log(&updaterect, &logrect);
	
	hdc = GetDC(hWndWindow);
//	hdc = BeginPaint(hWndWindow, &pstruct );
//	updaterect = pstruct.rcPaint;
	window_phys_to_log(&updaterect, &logrect);

	module_logger(&win32DrawDibVideo, LOG_USER, _("Update: %d,%d %d,%d\n"), 
					updaterect.left,		
			updaterect.top, 						/* client coords, upper-left of dest rect */
			(updaterect.right - updaterect.left), 
			(updaterect.bottom - updaterect.top));	/* width and height of dest rect, -1 == use bitmap size */

//	ValidateRect(hWndWindow, rect);
	if (!IsRectEmpty(&updaterect))
	{
#if 1
		IntersectClipRect(hdc,
					updaterect.left,		
			updaterect.top, 						/* client coords, upper-left of dest rect */
			(updaterect.right - updaterect.left), 
			(updaterect.bottom - updaterect.top));	/* width and height of dest rect, -1 == use bitmap size */
#endif
		if (dib)
		DrawDibDraw(dib, hdc,
			updaterect.left,		
			updaterect.top, 						/* client coords, upper-left of dest rect */
			(updaterect.right - updaterect.left), 
			(updaterect.bottom - updaterect.top),	/* width and height of dest rect, -1 == use bitmap size */
			&win_bm->bmiHeader,
			bitmap,
			logrect.left, 
			logrect.top,
				/* source upper-left of bitmap rect */
			logrect.right - logrect.left,
			logrect.bottom - logrect.top,
				/* size of bitmap rect */
			0			/* DDF_xxx flags.  don't use DDF_HURRYUP! */
		);
#if 1
		ExcludeClipRect(hdc,
					updaterect.left,		
			updaterect.top, 						/* client coords, upper-left of dest rect */
			(updaterect.right - updaterect.left), 
			(updaterect.bottom - updaterect.top));	/* width and height of dest rect, -1 == use bitmap size */
#endif
	}
	
	ReleaseDC(hWndWindow, hdc);
//	EndPaint(hWndWindow, &pstruct);
	ValidateRect(hWndWindow, NULL);
}
#endif

static void	window_resize(u32 newxsize, u32 newysize)
{
	RECT wrect,rect;
	int wx,wy;
	
	GetWindowRect(hWndWindow,&wrect);
	GetClientRect(hWndWindow,&rect);

	wx = (wrect.right-wrect.left) - rect.right;
	wy = (wrect.bottom-wrect.top) - rect.bottom;
	
	//log("window_resize = %d,%d %d,%d [%d,%d]\n", rect.left,rect.top, rect.right,rect.bottom,wx,wy);
	if (rect.right % 256 == 0 && newxsize == 240)
		rect.right = rect.right * 240 / 256;
	else if (rect.right % 240 == 0 && newxsize == 256)
		rect.right = rect.right * 256 / 240;
	else
		return;
	//log("window_resize => %d,%d\n", rect.right, rect.bottom);
	
	// don't change z-order or activate the window...
	// this wreaks havoc with trying to use the command dialog
	if (SetWindowPos(hWndWindow, HWND_TOP, wrect.left, wrect.top, 
			rect.right+wx, 
			rect.bottom+wy, 
			SWP_NOZORDER|SWP_NOOWNERZORDER|SWP_NOACTIVATE) == 0)
		module_logger(&win32DrawDibVideo, _L | LOG_ERROR|LOG_USER,_("could not do SetWindowPos (%d)\n"), GetLastError());
}


static void	window_invalidate(LPRECT rect)
{
	RECT physrect;
	RECT updrect;
	
	if (rect == NULL)
		SetRect(&updrect, 0, 0, win_logxsize, win_logysize);
	else
		updrect = *rect;
		
	window_log_to_phys(&updrect, &physrect);

#if MIXED_GTK
	if (win_frontend != FE_GTK)
#endif	
	{
		InvalidateRect(hWndWindow, &physrect, FALSE);
		UpdateWindow(hWndWindow);
	}
#if MIXED_GTK	
	else
	{
	//	InvalidateRect(hWndWindow, &physrect, FALSE);
	//	gtk_window_paint(&physrect);
	}
#endif
}

static void	window_updatepalette(void)
{
	if (dib)
	{
#if MIXED_GTK	
			if (win_frontend == FE_GTK)
				return;
#endif
				
	#ifdef USEPALETTESWITCHING
		int idx;
		HDC hdc;
		for (idx = 0; idx < 17; idx ++)
			DB_RGBTOPAL(idx, win_rgbmap[idx]);
/*		if (!DrawDibChangePalette(dib, 0, 1, pals+win_rgbmap[0]))
		{
		}
		if (!DrawDibChangePalette(dib, 16, 1, pals+win_rgbmap[16]))
		{
		}*/
		for (idx = 0; idx < 17; idx++)
			DrawDibChangePalette(dib, idx, 1, pals+win_rgbmap[idx]);
		hdc = GetDC(hWndWindow);
		//DrawDibRealize(dib, hdc, 0);
		ReleaseDC(hWndWindow, hdc);
		
		window_invalidate(NULL);
	#else
		vdpcompleteredraw(); // vdpdirtyall();
	#endif
	}
}


/*	Handle WM_SIZING message. */
static void	window_sizing(HWND hWnd, int which, LPRECT rect)
{
	RECT wcur, cur;
	int wx, wy;
	
	//log("window_sizing\n");
	GetWindowRect(hWnd, &cur);
	wcur = cur;
	//if (win_frontend != FE_GTK)
		AdjustWindowRect(&wcur, WS_OVERLAPPEDWINDOW, 0);
	wx = (wcur.right - wcur.left) - (cur.right - cur.left);
	wy = (wcur.bottom - wcur.top) - (cur.bottom - cur.top);
	
	if (which == WMSZ_BOTTOM || which == WMSZ_BOTTOMLEFT || which == WMSZ_BOTTOMRIGHT)
	{
		rect->bottom = cur.top + ((rect->bottom - rect->top) / 192) * 192 + wy;
	}
	if (which == WMSZ_TOP || which == WMSZ_TOPLEFT || which == WMSZ_TOPRIGHT)
	{
		rect->top = cur.bottom - ((rect->bottom - rect->top + 192) / 192) * 192 + wy;
		if (rect->top > rect->bottom) rect->top = rect->bottom;
	}
	if (which == WMSZ_RIGHT || which == WMSZ_TOPRIGHT || which == WMSZ_BOTTOMRIGHT)
	{
		rect->right = cur.left + ((rect->right - rect->left) / 256) * 256 + wx;
	}
	if (which == WMSZ_LEFT || which == WMSZ_TOPLEFT || which == WMSZ_BOTTOMLEFT)
	{
		rect->left = cur.right - ((rect->right - rect->left + 256) / 256) * 256 + wx;
		if (rect->left > rect->right) rect->left = rect->right;
	}
	
	window_invalidate(NULL);
}

extern LRESULT CALLBACK WindowWndProc( HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam )
{
	if (win32DrawDibVideo.runtimeflags & vmRTUnselected)
		return DefWindowProc(hWnd, messg, wParam, lParam);
	
	switch (messg)
	{
		case WM_ACTIVATE:
			window_resize(win_logxsize, win_logysize);
			break;
			
		// don't use a cursor in fullscreen mode
		case WM_SETCURSOR:
			SetCursor(LoadCursor((HINSTANCE)NULL, IDC_ARROW));
			return TRUE;

		case WM_PAINT:
			module_logger(&win32DrawDibVideo, _L | L_2, _("drawdib paint\n"));
			if (IsIconic(hWnd))
				return 1;		/* let Windows draw icon */
		
			module_logger(&win32DrawDibVideo, _L | L_2, _("drawdib painting\n"));
			window_paint_message();
			return 0;

		case WM_SIZING:
			window_sizing(hWnd, (int)wParam, (LPRECT)lParam);
			return TRUE;

		case WM_SIZE:
			window_invalidate(NULL);
			return 0;
			break;
		
	#ifdef USEPALETTESWITCHING
			/*	DrawDib-related; must redraw palette */
		case WM_PALETTECHANGED:
		{
			HWND changer = (HWND) wParam;
			//log("WM_PALETTECHANGED\n");
			if (changer == hWnd)
				break;
			/* 	else fall through */
		}
				
			/*  DrawDib-related; must set up palette and return true. */
		case WM_QUERYNEWPALETTE:
		{
			HDC hdc = GetDC(hWnd);
			//log("WM_QUERYNEWPALETTE\n");
			if (DrawDibRealize(dib, hdc, FALSE) > 0)
			{
				ReleaseDC(hWnd, hdc);
				window_invalidate(NULL);
				return TRUE;
			}
			else
			{
				ReleaseDC(hWnd, hdc);
				return FALSE;
			}
			break;
		}
	#endif
		
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

		case WM_COMMAND:
			return win_command(hWnd, LOWORD(wParam));
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
static void	window_complete_redraw(void)
{
	window_updatepalette();
	window_invalidate(NULL);
}

/*******************************************************/

static vmResult	win32ddib_resize(u32 newxsize,u32 newysize)
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
		
		window_resize(newxsize, newysize);
	}
	return vmOk;
}


static vmResult	win32ddib_setfgbg(u8 fg, u8 bg)
{
	win_rgbmap[0] = bg ? bg : 1;
	win_rgbmap[16] = fg ? fg : 1;
	window_updatepalette();
	return vmOk;
}

static vmResult	win32ddib_setblank(u8 bg)
{
	int x;
#ifdef USEPALETTESWITCHING
	for (x=1; x<16; x++)
		win_rgbmap[x] = bg;
	window_updatepalette();
#else
	memset(bitmap, BG, 256*256);
	window_invalidate(NULL);
#endif
	return vmOk;
}

static vmResult win32ddib_resetfromblank(void)
{
	int x;
	window_invalidate(NULL);
	for (x=1; x<16; x++)
		win_rgbmap[x] = x;
	window_updatepalette();
	return vmOk;
}


/***********************************************************/

static void	ddib_update_paint(struct updateblock *ptr, int num)
{
	int width=8;
	int	i;
	int offs;
	u8 *scrn;
	
	RECT rect, updaterect, physrect;
	
	if (!num)
		return;
		
	SetRectEmpty(&updaterect);
	
	offs=(256-win_logxsize)/2;
	while (num--)
	{	
		// Windows bitmaps are upside down...
		scrn = win_bitmap + ((256 - ptr->r) * 256) + ptr->c + offs;
		for (i=0; i<8; i++)
		{
			scrn -= 256;
			//memcpy(scrn,ptr->data,width);
			// even text modes work with this
			*(unsigned long long *)scrn = *(unsigned long long *)ptr->data;
			ptr->data+=UPDATEBLOCK_ROW_STRIDE;
		}
		
		SetRect(&rect, ptr->c + offs, ptr->r, ptr->c + offs + width, ptr->r + 8);
		
		window_log_to_phys(&rect, &physrect);
		InvalidateRect(hWndWindow, &physrect, false);
		
		ptr++;
	}
}

#define TRY_THIS 0

static void	ddib_update_force(struct updateblock *ptr, int num)
{
	int width = 8;
	int	i;
	int offs;
	u8 *scrn;
	
	HRGN updatergn, rgn;
	RECT rect, physrect;
	
	if (!num)
		return;
	
	updatergn = CreateRectRgn(0, 0, 0, 0);
	
	offs=(256-win_logxsize)/2;
	while (num--)
	{	
		// Windows bitmaps are upside down...
		scrn = win_bitmap + ((256 - ptr->r) * 256) + ptr->c + offs;
		for (i=0; i<8; i++)
		{
			scrn -= 256;
			//memcpy(scrn,ptr->data,width);
			// even text modes work with this
			*(unsigned long long *)scrn = *(unsigned long long *)ptr->data;
			ptr->data+=UPDATEBLOCK_ROW_STRIDE;
		}
		
		SetRect(&rect, ptr->c + offs, ptr->r, ptr->c + width + offs, ptr->r + 8);
		
		window_log_to_phys(&rect, &physrect);

		rgn = CreateRectRgn(physrect.left, physrect.top, physrect.right, physrect.bottom);
		CombineRgn(updatergn, updatergn, rgn, RGN_OR);
		DeleteObject((HGDIOBJ)rgn);
		
		window_update_pixels+=(physrect.right-physrect.left) *
							(physrect.bottom-physrect.top);
		
		ptr++;
	}

	window_paint_force_region(updatergn);
	DeleteObject((HGDIOBJ)updatergn);
}

static void ddib_update(struct updateblock *ptr, int num)
{
	if (force_draws)
		ddib_update_force(ptr, num);
	else
		ddib_update_paint(ptr, num);
}

static vmResult	win32ddib_updatelist(struct updateblock *ptr, int num)
{
	ddib_update(ptr, num);
	return vmOk;
}


/****************************************/
#if 0
#pragma mark -
#endif

static vmResult	win32ddib_detect(void)
{
	/* we would crash in WinMain if we didn't detect */
	return vmOk;
}

static vmResult	win32ddib_init(void)
{
	command_symbol_table *cmds = 
		command_symbol_table_new(_("Win32 DrawDIB Commands"),
								 _("These options control the behavior of the V9t9-in-a-Window module"),
			command_symbol_new
			 ("DrawDIBForceDraw",
				  _("Toggle direct painting of windows versus waiting for a WM_PAINT message"),
				  c_STATIC,
				  NULL /* action */,
				  RET_FIRST_ARG,
				  command_arg_new_enum
				    ("off|on",
					 _("on:  draw as soon as possible; "
					 "off:  queue updates for WM_PAINT"),
					 NULL,
					 ARG_NUM(force_draws),
					 NULL /* next */ )			  
					 
					 ,
			
			NULL /* next */),
			
			NULL /* sub */,
			
			NULL /* next */
	);
			

	LOGPALETTE *lpal;
	int	x;
	int idx;

	command_symbol_table_add_subtable(universe, cmds);
	
#if MIXED_GTK
	if (win_frontend != FE_GTK)
#endif	
	{
		if (!win_createDrawDibWindows(myHInst, mynCmdShow))
			return vmInternalError;
	}
#if MIXED_GTK
	else
	{
		if (!v9t9_drawing_area)
		{
			module_logger(&win32DrawDibVideo, _L|LOG_ERROR|LOG_USER, _("Could not find GTK main window!\n"));
			return vmInternalError;
		}
		hWndWindow = hWndApp = (HWND)GDK_WINDOW_XWINDOW((GdkWindowPrivate *)(v9t9_drawing_area->window));
	}
#endif
		
	for (idx = 0; idx <= 16; idx++)
	{
		DB_RGBTOPAL(idx,idx);
	}
	
	lpal = (LOGPALETTE *)malloc(sizeof(LOGPALETTE) + sizeof(pals));
	lpal->palVersion = 0x300;
	lpal->palNumEntries = 17;
	memcpy((void *)lpal->palPalEntry, pals, sizeof(pals));
	if ((pal = CreatePalette(lpal)) == NULL)
	{
		FAIL("CreatePalette");
		return vmInternalError;
	}
	
	dib = DrawDibOpen();
	if (!dib)
	{
		FAIL("DrawDibOpen");
		return vmInternalError;
	}

	if (!DrawDibSetPalette(dib, pal))
	{
		FAIL("DrawDibSetPalette");
		return vmInternalError;
	}

	DrawDibProfileDisplay(&win_bm->bmiHeader);
	
	for (x=0; x<16; x++)
		win_rgbmap[x]=x;
	win_rgbmap[16] = 15;

//	win_video_event_tag = TM_UniqueTag();
//	TM_SetEvent(win_video_event_tag, TM_HZ*100/30, 0,
//				TM_REPEAT|TM_FUNC, win_video_update);

	features|=FE_SHOWVIDEO|FE_VIDEO;
	
	return vmOk;
}

static vmResult	win32ddib_enable(void)
{
	window_update_counter_tag = TM_UniqueTag();
	return vmOk;
}

static vmResult	win32ddib_disable(void)
{
//	wasfullscreen = false;
	return vmOk;
}

int reparented = 0;
static vmResult	win32ddib_restart(void)
{
	if (features & FE_SHOWVIDEO)
	{
		HDC	hdc;
		int idx;
		
		/*	Put up the window */
		if (dib)
		{
			hdc = GetDC(hWndWindow);
			if (!hdc)
			{
				FAIL("GetDC");
				v9t9_term(1);
			}
			
			if (!DrawDibBegin(dib, hdc, 
					-1, -1, 		/* dest rect, no stretching */
					&win_bm->bmiHeader, 
					256, 192, 		/* source rect */
				#ifdef USEPALETTESWITCHING
					DDF_ANIMATE
				#else
					DDF_BACKGROUNDPAL	/* keep static pal */
				#endif
					))
					
			{
				FAIL("DrawDibBegin");
				v9t9_term(1);
			}

			for (idx = 0; idx < 17; idx ++)
				DB_RGBTOPAL(idx, win_rgbmap[idx]);

			if (!DrawDibChangePalette(dib, 0, 17, pals))
			{
				FAIL("DrawDibChangePalette");
				v9t9_term(1);
			}

			ReleaseDC(hWndWindow, hdc);
			
			ShowWindow(hWndWindow, SW_RESTORE);
			SetFocus(hWndWindow);
		}
		
		window_updates = window_update_pixels = 0;
		window_total_seconds = 0;
		TM_SetEvent(window_update_counter_tag, TM_HZ*100/1, 0,
					TM_FUNC|TM_REPEAT, TM_EVENT_FUNC(window_update_counter));
		
		window_complete_redraw();
	}
	return vmOk;
}

static vmResult	win32ddib_restop(void)
{
	if (features & FE_SHOWVIDEO)
	{
		TM_ResetEvent(window_update_counter_tag);
		
		if (dib)	
		{
			DrawDibEnd(dib);
		}

		ShowWindow(hWndWindow, SW_HIDE);
#if STATUS_WINDOW		
		ShowWindow(hWndStatus, SW_HIDE);
#endif
	}
	return vmOk;
}

static vmResult	win32ddib_term(void)
{
	if (dib)
	{
		DrawDibEnd(dib);
		DrawDibClose(dib);
		dib = 0;
	}
	return vmOk;
}

static vmVideoModule win32ddib_DrawDibVideoModule =
{
	3,
	win32ddib_updatelist,
	win32ddib_resize,
	win32ddib_setfgbg,
	win32ddib_setblank,
	win32ddib_resetfromblank
};

extern vmModule win32DrawDibVideo =
{
	3,
	"Win32 DrawDIB video",
	"vidWin32DrawDIB",
	
	vmTypeVideo,
	(vmFlags) (vmFlagsExclusive | vmFlagsOneShotEnable | vmFlagsOneShotDisable),
	
	win32ddib_detect,
	win32ddib_init,
	win32ddib_term,
	win32ddib_enable,
	win32ddib_disable,
	win32ddib_restart,
	win32ddib_restop,
	{ (vmGenericModule *)&win32ddib_DrawDibVideoModule }
};


