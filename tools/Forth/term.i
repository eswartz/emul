;	term.inc					--	terminal emulation.
;
;	(c) 1996-2001 Edward Swartz
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
;	$Id: term.i,v 1.9 2009-02-25 02:18:25 ejs Exp $

;
;	VIDEO sets up "vdrawchar", "vwidth", "vheight"
;	for character drawing.  It's up to those routines to determine
;   how to map character coordinates to the screen (e.g.
;   fonts, number of pixels, etc).

;	Call after video mode is initialized.
;
treset  	
	clr	   @vx			    ; upper-left corner
	clr	   @vwx			    ; window starts upper-left
	movb   @vwidth+1,@vwxs	; set dims to screen size
	movb   @vheight,@vwys	; set dims to screen size
	rt

;   (Called from XOP)
;
;	Create a windowed area on the screen (in terms of characters).
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
window	PUSH    SP,11

	bl	    @vcursoroff			; clear cursor stuff

	movb   @vwidth+1,2
	cb     0,2				; check left coord
	jl     wxlookay			; lower, okay
	sb     0,0				; fix to edge
wxlookay:
	movb   0,@vwx			; save 
	ab	   1,0				; get right coord
	jc	   wxxover
	cb	   0,2				; lower or equal, okay
	jle    wxhiokay			
wxxover:
	movb	2,1
	sb	    @vwx,1			; fix R1 to that width
wxhiokay:
	movb	1,@vwxs

	swpb	0				; point to Y 
	swpb	1				; coordinates
	movb   @vheight, 2

	cb     0,2				; check top coord
	jle    wylookay			; lower, okay
	sb     0,0				; fix to edge
wylookay:
	movb   0,@vwy		    ; save 
	ab     1,0				; get bottom coord
	jc     wyyover
	cb     0,2				; lower or equal, okay
	jle    wyhiokay			
wyyover:
	movb   2,1
	sb     @vwy,1				; fix R1 to that height
wyhiokay:
	movb   1,@vwys

	clr	   @vx				; clear coords

    POP    SP,11
	rt


;	(Called from XOP)
;
;	Print a character.
;
;	hi(R1) = char

printchar PUSH  SP, 11
	bl	    @vcursoroff			; we're movin', buddy!

	movb	1,@vch
	mov	    @vdrawchar,1
	blwp	*1  				; print char

	ab	    #1,@vx	     		; add a space
	jc	    prchwr				; carried, thus wrapped
	cb	    @vx,@vwxs			; edge of window?
	jl	    prchout				; no, we're okay
prchwr:
	bl	    @crlf

prchout:
    POP SP, 11
	rt


;	(Called from XOP)
;
;	Move cursor to X,Y in window
;
;	hi(R0) = X
;	lo(R0) = Y
;
gotoxy	PUSH SP, 11

	bl	    @vcursoroff			; we're moving it!

	cb	    0,@vwxs
	jl	    gxyokayx
	movb	@vwxs,0
	sb 	    #1,0
gxyokayx:
	swpb	0
	cb	    0,@vwys
	jl	    gxyokayy
	movb	@vwys,0
	sb	    #1,0
gxyokayy:
	swpb	0
	mov	    0,@vx

    POP     SP, 11    
	rt


;---------------------------------------------------------------

;   (Called from XOP)
;
;   Scroll the text cursor down.
;
;   VWX/VWXS & VWY/VWYS are window coords
;   
;   VFGBG = fill color

termscroll
    PUSH    SP,11
    
    mov     @vclearline, 3

    movb    @vwxs,2             ; length
    srl     2,8

    mov     @vx,0
    sb      0,0                 ; get coord for lower-left
    
    dect    SP
    mov     2,*SP
    bl      *3
    
    inc     0
    swpb    0
    cb      0,@vwys
    jl      termscroll1
    sb      0,0
termscroll1
    swpb    0
    mov     *SP+,2     
    bl      *3

    POP     SP,11
    rt


termclear
    PUSH    SP,0,2,3,11
    clr     0
    mov     @vclearline,3
    dect    SP
    movb    @vwxs,2             ; length
    srl     2,8
termclear0
    mov     2,*SP
    bl      *3
    mov     *SP,2
    inc     0
    swpb    0
    cb      0,@vwys
    jeq     termclear1
    swpb    0
    jmp     termclear0
termclear1
    clr     @vx
    inct    SP
    POP     SP,0,2,3,11
    rt        
    
;	CALLED FROM FORTH!
;
;	Do a backspace.
;

bksp	PUSH   SP, 1, 2, 11

	bl	@vcursoroff

	movb	@vx,1				; get x
	sb	    #1,1				; decrement
	jc	    bksp0				; 0 -> -1?
;;;	movb	@vy,@vwcy			; curr row
	movb	@vwxs,1				; yup, move to other edge
	sb	    #1,1			
	movb	@vy,2				; and decrement
	sb	    #1,2				; Y
	jc	    bksp1				; 0 -> -1?
	movb	@vwys,2				; move to bottom
	sb	    #1,2
bksp1	movb	2,@vy				; save
bksp0	movb	1,@vx				; save

    POP SP, 1, 2, 11
	rt

;   Called from XOP
;
;   Do a carriage return.
;

crlf    PUSH    SP, 1, 11

    bl      @vcursoroff         ; we're moving cursor

    sb      @vx,@vx             ; reset X coord
    ab      #1,@vy               ; next line
    jc      crlfc               ; wrapped?
    cb      @vy,@vwys           ; bottom of window?
    jl      crlf0
crlfc:
    sb      @vy,@vy

crlf0:

    cb      @vy,@vwcy
    je      crlf1
    movb    @vy,@vwcy
    bl      @termscroll

crlf1:
    POP     SP, 1, 11
    rt



;	CALLED FROM FORTH!
;
;	Do a tab.
;
tab	PUSH    SP, 1, 11
	mov	1,@2(SP)
	mov	11,*SP

tab0	li	1,>2000
	bl	@printchar  			; print a space
	movb	@vx,1
	andi	1,>0700				; if not on 8-char boundary
	jne	tab0				; repeat

    POP     SP, 1, 11
	rt

;   Clear to end of line
clreol  PUSH    SP, 0, 2, 3, 11

    mov     @vclearline, 3

    movb    @vwxs,2             ; length
    srl     2,8

    mov     @vx,0
    sb      0,0                 ; get coord for lower-left
    
    bl      *3
    
    POP     SP, 0, 2, 3, 11
    rt
    

;   CALLED FROM FORTH!
;
;   Emit a character, intepreting
;
;   hi(R1) = char
emit
    cb      1,#>0d           ; enter?
    jeq     crlf

$1:  cb     1,#>07          ; bell?
    jeq     bell

$1:  cb     1,#>08          ; backspace?
    jeq     bksp

$1:  cb     1,#>09          ; tab?
    jeq     tab
    
$1: cb      1,#>0B          ; vertical tab ( == clear to end of line )
    jeq     clreol
        
    b       @printchar
    
;   Unhandled
bell:    
    rt

;  CALLED FROM FORTH!
;
;  Print a message, interpreting the chars
;   
;   R2 = message
;   R3 = length
;
type PUSH    SP,11
    ai      SP,-4
$1: mov     3,3
    jeq     $2+
    movb    *2+,1
    mov     2, *SP
    mov     3, @2(SP)
    bl      @emit
    mov     @2(SP), 3
    mov     *SP, 2
    dec     3
    jmp     $1-
$2: ai      SP,4
    POP     SP,11
    rt
	