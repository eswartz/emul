;   term.inc
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

;	Terminal emulation.
;
;	VIDEO sets up "vdrawchar", "vwidth", "vheight"
;	for character drawing.
;
;	Call after video mode is initialized.

tinit 	dect	SP
	mov	0,*SP
	clr	@vx			; upper-left corner
	clr	@vwx			; window starts upper-left
	mov	@vwidth,@vwxs		; set dims to screen size
	mov	+*SP,0
	rt


;	Create a windowed area on the screen.
;
;	This only sets up limits; something else should "draw"
;	the window.
;
;	hi(R0) = left
;	lo(R0) = top
;	hi(R1) = width
;	lo(R1) = height
;
;	If window's left/top coordinates are out-of-bounds, they
;	will be moved to the very left/top
;
;	If the sizes are out-of-bounds, they will stick to the edge of
;	the screen.
;
window	si	SP,8
	mov	11,@6(SP)
	mov	0,@4(SP)
	mov	1,@2(SP)
	mov	2,*SP

	bl	@vcursoroff			; clear cursor stuff

	li	2,vwidth
	cb	0,*2				; check left coord
	jl	wxlookay			; lower, okay
	sb	0,0				; fix to edge
wxlookay:
	movb	0,@vwx				; save 
	ab	1,0				; get right coord
	jc	wxxover
	cb	0,*2				; lower or equal, okay
	jle	wxhiokay			
wxxover:
	movb	*2,1
	sb	@vwx,1				; fix R1 to that width
wxhiokay:
	movb	1,@vwxs

	swpb	0				; point to Y 
	swpb	1				; coordinates

	li	2,vheight
	cb	0,*2				; check top coord
	jl	wylookay			; lower, okay
	sb	0,0				; fix to edge
wylookay:
	movb	0,@vwy				; save 
	ab	1,0				; get bottom coord
	jc	wyyover
	cb	0,*2				; lower or equal, okay
	jle	wyhiokay			
wyyover:
	movb	*2,1
	sb	@vwy,1				; fix R1 to that height
wyhiokay:
	movb	1,@vwys

	clr	@vx				; clear coords

	mov	+*SP,2
	mov	+*SP,1
	mov	+*SP,0
	mov	+*SP,11
	rt


;	CALLED FROM FORTH!
;
;	Print a character.
;
;	hi(R1) = char

printchar si	SP,4
	mov	11,@2(SP)
	mov	1,*SP

	bl	@vcursoroff			; we're movin', buddy!

	movb	1,@vch
	mov	@vdrawchar,1
	blwp	*1  				; print char

	ab	@h01,@vx			; add a space
	cb	@vx,@vwxs			; edge of window?
	jl	prchout				; no, we're okay

	bl	@crlf

;	sb	@vx,@vx				; else next line
;	ab	@h01,@vy
;	cb	@vy,@vwys
;	jl	prchout
;
;	sb	@vy,@vy
prchout:
	mov	+*SP,1
	mov	+*SP,11
	rt


;	CALLED FROM FORTH!
;
;	Move cursor to X,Y in window
;
;	hi(R0) = X
;	lo(R0) = Y
;
gotoxy	si	SP,4
	mov	11,@2(SP)
	mov	0,*SP

	bl	@vcursoroff			; we're moving it!

	cb	0,@vwxs
	jl	gxyokayx
	movb	@vwxs,0
	sb	@h01,0
gxyokayx:
	swpb	0
	cb	0,@vwys
	jl	gxyokayy
	movb	@vwys,0
	sb	@h01,0
gxyokayy:
	swpb	0
	mov	0,@vx

	mov	+*SP,0
	mov	+*SP,11
	rt


;	CALLED FROM FORTH!
;
;	Do a carriage return.
;

crlf	si	SP,4
	mov	1,@2(SP)
	mov	11,*SP

	bl	@vcursoroff			; we're moving cursor

	sb	@vx,@vx				; reset X coord
	ab	@h01,@vy			; next line
	cb	@vy,@vwys			; bottom of window?
	jl	crlf0

	sb	@vy,@vy

crlf0:

	cb	@vy,@vwcy
	je	crlf1
	movb	@vy,@vwcy
	mov	@vscroll,1
	blwp	*1

crlf1:
	mov	+*SP,11
	mov	+*SP,1
	rt


;	CALLED FROM FORTH!
;
;	Do a backspace.
;

bksp	si	SP,6
	mov	1,@4(SP)
	mov	2,@2(SP)
	mov	11,*SP

	bl	@vcursoroff

	movb	@vx,1				; get x
	sb	@H01,1				; decrement
	jc	bksp0				; 0 -> -1?
;;;	movb	@vy,@vwcy			; curr row
	movb	@vwxs,1				; yup, move to other edge
	sb	@h01,1			
	movb	@vy,2				; and decrement
	sb	@h01,2				; Y
	jc	bksp1				; 0 -> -1?
	movb	@vwys,2				; move to bottom
	sb	@h01,2
bksp1	movb	2,@vy				; save
bksp0	movb	1,@vx				; save

	mov	+*SP,11
	mov	+*SP,2
	mov	+*SP,1
	rt


;	CALLED FROM FORTH!
;
;	Do a tab.
;
tab	si	SP,4
	mov	1,@2(SP)
	mov	11,*SP

tab0	li	1,>2000
	bl	@printchar  			; print a space
	movb	@vx,1
	andi	1,>0700				; if not on 8-char boundary
	jne	tab0				; repeat

	mov	+*SP,11
	mov	+*SP,1

	rt
