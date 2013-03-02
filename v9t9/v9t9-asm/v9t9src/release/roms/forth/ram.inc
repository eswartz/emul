;   ram.inc
; 
;   (c) 1991-2012 Edward Swartz
; 
;   This program is free software; you can redistribute it and/or modify
;   it under the terms of the GNU General Public License as published by
;   the Free Software Foundation; either version 2 of the License, or
;   (at your option) any later version.
;  
;   This program is distributed in the hope that it will be useful, but
;   WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;   General Public License for more details.
;  
;   You should have received a copy of the GNU General Public License
;   along with this program; if not, write to the Free Software
;   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
;   02111-1307, USA.
; 

;==========================================================================
;	RAM definitions for V9t9 FORTH ROM.
;
;==========================================================================

	aorg	>8000

sysstack bss	>40			; system stack
sysstacksize equ $ sysstack -

mainws	bss	>20
intws	bss	>20
vidws	bss	>20			; video & term workspace
dskws	bss	>20			; disk workspace

;---------------------------------

uptime	bss	4			; time in 1/60 seconds
timeout	bss	2			; timeout counter
userint bss	2			; user int routine (bl @xxxx)

vregs	bss	2			; VDP registers 0 and 1
vscreen	bss	2			; VDP addr of screen
vpatts	bss	2			; VDP addr of patterns
vcolors bss	2			; VDP addr of colors
vsprites bss	2			; VDP addr of sprites
vsprpat bss	2			; VDP addr of sprite patterns
vsprmot	bss	2			; VDP addr of sprite motion
vfree	bss	2			; usable space

vwidth	bss	1			; width in chars
vheight	bss	1			; height in chars
vdrawchar bss	2			; draw char in window (BLWP @)
vscroll	bss	2			; scroll window up a line (BLWP @)
vclear	bss	2			; clear window (BLWP @)

vcoordaddr bss	2			; get SIT addr of R0 coord

vcrstimer bss	1			; timer for blink
vcrsblink bss	1			; limit in 1/60 s
vcursor	bss	2			; cursor blinker (BLWP @)
vcurs	bss	1			; cursor blink state (0 or >80)
vcursunder bss	1			; char under cursor

vstacksize equ 	>20
vstack	bss	vstacksize		; video stack

;-----------------------------------

@kbdstart equ	$

kbdgradr bss	2			; saved GROM addr
kbdlast	bss	1			; last char pressed (or 0)
kbdtimer bss	1			; timer (1/60 s) since last repeat
kbdscan bss	1			; most recent scancode	   (0-47)
kbdshft	bss	1			; most recent shift status (>70)
kbdhead	bss	1			; head of kbd buffer
kbdtail	bss	1			; tail of kbd buffer  
					; head==tail => empty

kbdbufsize equ	32
kbdbuf	bss	kbdbufsize			

kbdlimit bss	1			; 1/60s before repeating
kbddelay bss	1			; delay between keyscans
kbdflag	bss	1			; flag:  0=waiting to repeat, 1=r'ing
kbdmode	bss	1			; how we interpret keyboard
					; | >80 = no translation to ASCII
					; | >40 = no buffering

@kbdlength equ	$ @kbdstart -

kbdreturn bss	2			; return addr

;---------------------------------------

vfg	bss	1			; foreground color
vbg	bss	1			; background color
vch	bss	1			; current char

vcurschar bss	1			; char of cursor

vwx	bss	1			; window left
vwy	bss	1			; window right
vwxs	bss	1			; width of window
vwys	bss	1			; height of window
vwcy	bss	1			; last cleared row
	bss	1

vx	bss	1			; x-coord of cursor in window
vy	bss	1			; y-coord of cursor in window

vmono	bss	1
vidmode	bss	1			; what mode are we in?  (M_xxxx)

M_text	equ	0
M_graph	equ	1
M_bit	equ	2			; both mono and color


vlinex	bss	2
vliney	bss	2


;--------------------------------------

dskstacksize 	equ	>20
dskstack bss	dskstacksize

forthdsk bss	10			; filename for FORTH disk


;--------------------------------------
;	FORTH vars
;--------------------------------------

_S0	bss	2			; start addr of stack
_R0	bss	2			; start addr of return stack
_DP	bss	2			; start addr of dictionary
_Tib	bss	2			; start addr of terminal input buffer
_Width	bss	2			; width of name
_Warning bss	2			; warning
_CL	bss	2			; characters per line (EDIT)
_Fence	bss	2			; fence on forgetting words
_In	bss	2
_Out	bss	2
_Blk	bss	2			; block.  if 0, KBD, else GROM
_Scr	bss	2
_AtScr	bss	2
_Context bss	2
_Current bss	2
_State	bss	2
_Base	bss	2
_Dpl	bss	2
_Csp	bss	2
_Hld	bss	2

_Forth_Link bss	2
_Voc_Link bss	2

_Use	bss	2
_Prev	bss	2

_ErrPtr bss	2

;--------------------------------------

temp	equ	$			; COMPLETELY temporary workspace.
					; not to store ANYTHING useful.



	aorg	>8370

vdplimit bss	2			; VDP memory size


