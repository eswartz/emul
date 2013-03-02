;   video.inc
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




;	Initialize video.  Defaults to text mode.
;

vinit	si	SP,4
	mov	11,@2(SP)
	mov	0,*SP

	li	0,15
	mov	0,@vcrstimer			; set up standard blink
	sb	@vcurs,@vcurs			; it's off

	li	1,>0107
	mov	1,@vfg

	blwp	@vtextsetup
	blwp	@vscreenon

	mov	+*SP,0
	mov	+*SP,11
	rt



;=========================================================================
;	Internally called routines
;	
;	(IM=0)
;=========================================================================


;	Set GROM write addr in R0
;

gwaddr	movb	0,@>9c02
	swpb	0
	movb	0,@>9c02
	swpb	0
	rt



;	Set VDP write/read addr in R0
;

vwaddr	ori	0,>4000
vraddr	swpb	0
	movb	0,@>8c02
	swpb	0
	movb	0,@>8c02
	andi	0,>3fff
	rt


;	Video clear.
;
;	R0=addr
;	R1=char
;	R2=#

vclr	si	SP,4
	mov	11,@2(SP)
	mov	2,*SP

	bl	@vwaddr
vclr0	movb	1,@>8c00
	dec	2
	jgt	vclr0
	
	mov	+*SP,2
	mov	+*SP,11
	rt



;	Figure screen addr for text.
;
;	hi(R0) = X in window
;	lo(R0) = Y in window
;
;	Returns R0=addr

d40	data	40

vtextaddr si	SP,4
	mov	1,@2(SP)
	mov	2,*SP
	
	mov	0,2				; save X
	swpb	0

	ab	@vwy,0
	srl	0,8
	mpy	@d40,0				; get row offset in R1

	movb	2,0
	ab	@vwx,0
	srl	0,8   				; get column offset

	a	1,0				; column+row
	a	@vscreen,0			; R0=addr

	mov	+*SP,2
	mov	+*SP,1
	rt



;	Figure screen addr for gfx.
;
;	hi(R0) = X in window
;	lo(R0) = Y in window
;
;	Returns R0=addr

vgraphaddr si	SP,4
	mov	1,@2(SP)
	mov	2,*SP
	
	mov	0,2				; save X
	swpb	0

	ab	@vwy,0
	srl	0,8
	sla	0,5

	movb	2,1
	ab	@vwx,1
	srl	1,8   				; get column offset

	a	1,0				; column+row
	a	@vscreen,0			; R0=addr

	mov	+*SP,2
	mov	+*SP,1
	rt


;	Figure screen addr for bitmap.
;
;	hi(R0) = X in window
;	lo(R0) = Y in window
;
;	Returns R0=addr

vbitaddr si	SP,4
	mov	1,@2(SP)
	mov	2,*SP
	
	mov	0,2				; save X

	andi	0,>ff				; get >00YY
	swpb	0				; >yy * bitoffs = >yy00
	ab	@vwy,0

	movb	2,1
	ab	@vwx,1
	srl	1,8   				; get column offset
	sla	1,3

	a	1,0				; column+row
						; R0 = offs only

	mov	+*SP,2
	mov	+*SP,1
	rt




;	Clear some text line.
;	
;	R0=coord
;	R1=char
;	R2=length

vtextclearline
	si	SP,6
	mov	0,@4(SP)
	mov	3,@2(SP)
	mov	11,*SP

	mov	@vcoordaddr,3
	bl	*3
	bl	@vclr				; clear out line

	mov	+*SP,11
	mov	+*SP,3
	mov	+*SP,0
	rt


;	Clear some bitmapped line.
;	
;	R0=coord
;	R1=char
;	R2=length

vbitclearline
	si	SP,8
	mov	0,@6(SP)
	mov	1,@4(SP)
	mov	3,@2(SP)
	mov	11,*SP

	clr	1

	mov	@vcoordaddr,3
	bl	*3

	mov	0,3
	a	@vpatts,0
	bl	@vclr				; clear out line

	movb	@vmono,@vmono
	jne	vbclbl

	mov	3,0
	a	@vcolors,0

	clr	1
	movb	@vfg,1
	sla	1,4
	socb	@vbg,1
	bl	@vclr				; clear out line

vbclbl:
	mov	+*SP,11
	mov	+*SP,3
	mov	+*SP,1
	mov	+*SP,0
	rt




;==========================================================================
;	Externally called video functions.
;
;	IM=1, so we must turn off interrupts to use VDP.
;
;	Use BLWP @ for new workspace AND assurance of correct
;	IM on exit.
;==========================================================================


;	Turn the screen on or off.
;
;	NOTE:	TIMER & KBD interrupt call these.  
;	Don't change any registers or use the stack!
;
;	NOTE:  
;

vscreenoff data	vidws,vscreenoff 4+
	szcb 	@h40,@vregs 1+
	jmp	vreg1set
vscreenon data 	vidws,vscreenon 4+
	socb	@h40,@vregs 1+
vreg1set:
	limi	0
	movb	@vregs 1 +,@>8c02
	movb	@h81,@>8c02
	rtwp



;-------------------------------------------------------------------------
;		0=graphics mode
;			>0000 = screen
;			>0300 = sprites
;			>0380 = colors
;			>03A0 = sprite motion
;			>0420 = sprite patterns (really 0->800)
;			>0800 = char patts
;			>1000+= free
;
;		1=text mode
;			>0000 = screen
;			>0800 = patts
;			>1000+= free
;
;		2=bitmap mode
;			>0000 = patts
;			>1800 = screen
;			>1B00 = sprites
;			>1B80 = sprite motion
;			>1C00 = sprite patts
;			>2000 = colors
;			>3800+= free
;
;		3=monochrome bitmap mode
;			>0000 = patts
;			>1800 = screen
;			>1B00 = sprites
;			>1B80 = sprite motion
;			>1C00 = sprite patts
;			>2000 = colors
;			>2040+= free
;
;
;-------------------------------------------------------------------------

vtmap	dw	vscreen,vsprites,vcolors,vsprpat,vpatts,vfree
	dw	vdrawchar,vscroll,vclear,vwidth,vcursor,vcoordaddr

vgfx 	db	>0,>A0,>0,>E,>1,>6,>0,>f	; last byte*>80 = motion tbl
vgfxt	dw	>0,>300,>380,>0,>800,>1000
	dw	vtextchar,vgraphscroll,vtextclear,>2018,vtextcursor,vgraphaddr

vtxt 	db	>0,>B0,>0,>0,>1,>0,>0,0
vtxtt	dw	>0,>0,>0,>0,>800,>1000
	dw	vtextchar,vtextscroll,vtextclear,>2818,vtextcursor,vtextaddr

vbit 	db	>2,>A0,>6,>ff,>03,>36,>3,>37
vbitt	dw	>1800,>1b00,>2000,>1800,>0,>3800
	dw	vbitchar,vbitscroll,vbitclear,>2018,vbitcursor,vbitaddr


vsetupregs:
	li	12,>8c02
	li	0,>8000
	mov	*1,@vregs

	li	2,7
vts0	movb	+*1,*12
	movb	0,*12
	ai	0,>100
	dec	2
	jgt	vts0

	movb	+*1,2
	srl	2,9
	mov	2,@vsprmot

	clr	1
	movb	@vfg,1
	sla	1,4
	socb	@vbg,1
	movb	1,*12
	movb	0,*12

	rt


vsetupaddrs:
	li	0,vtmap
	li	2,12
vts1	mov	+*1,3
	mov	+*0,4
	mov	3,*4
	dec	2
	jgt	vts1
	rt


vtextsetup 	data vidws,vtextsetup 4+
	limi	0
	li	SP,vstack vstacksize +

	li	1,M_text 256 *
	movb	1,@vidmode

	li	1,vtxt
	bl	@vsetupregs

	li	1,vtxtt
	bl	@vsetupaddrs

	clr	0
	li	1,>2000
	li	2,960
	bl	@vclr

	clr	0
	bl	@vgetfont

	movb	@hunder,@vcurschar

	rtwp


vgraphsetup 	data vidws,vgraphsetup 4+
	limi	0
	li	SP,vstack vstacksize +

	li	1,M_graph 256 *
	movb	1,@vidmode

	li	1,vgfx
	bl	@vsetupregs

	li	1,vgfxt
	bl	@vsetupaddrs

	mov	@vsprites,0
	clr	1
	li	2,128
	bl	@vclr

	mov	@vcolors,0
	li	1,>1000
	li	2,32
	bl	@vclr

	clr	0
	li	1,>2000
	li	2,768
	bl	@vclr

	mov	@vsprmot,0
	clr	1
	li	2,128
	bl	@vclr


	movb	@hunder,@vcurschar


	clr	0
	bl	@vgetfont

	rtwp


vbitmapsetup 	data vidws,vbitmapsetup 4+
	limi	0
	li	SP,vstack vstacksize +

	movb	@h00,@vmono

vbitsentr:
	li	1,M_bit 256 *
	movb	1,@vidmode

	li	1,vbit
	bl	@vsetupregs

	li	1,vbitt
	bl	@vsetupaddrs

	movb	@Hff,@vcurschar

	mov	@vpatts,0
	clr	1
	li	2,>1800
	bl	@vclr

	mov	@vcolors,0
	clr	1
	movb	@vfg,1
	sla	1,4
	socb	@vbg,1
;	li	2,>1800
	bl	@vclr

	mov	@vsprites,0
	clr	1
	li	2,128
	bl	@vclr

	mov	@vsprmot,0
	clr	1
;	li	2,128
	bl	@vclr



	mov	@vscreen,0
	bl	@vwaddr
	clr	1
	li	2,768
vbs2	movb	1,@>8c00
	ai	1,>100
	dec	2
	jgt	vbs2


	rtwp


vmonosetup 	data vidws,vmonosetup 4+
	limi	0
	li	SP,vstack vstacksize +

	movb	@h01,@vmono
	jmp	vbitsentr


;	Get a font from GROM and load into pattern table.
;
;	(All) R0= font #
;
;
vgetfont si	SP,6
	mov	0,@4(SP)
	mov	2,@2(SP)
	mov	11,*SP

	sla	0,11				; * >800
	ai	0,grom_fonts
	
	bl	@gwaddr

	li	0,>800
	bl	@vwaddr

	li	2,>800
vgf1	movb	@>9800,@>8c00
	dec	2
	jgt	vgf1

	mov	+*SP,11
	mov	+*SP,2
	mov	+*SP,0
	rt



;========================================================================




;	Turn off the cursor.
;
;	Call before moving it or changing the char under the cursor.
;
;	NOT a BLWP @ function because it's unnecessary.
;

vcursoroff
	cb	@vcurs,@h00
	jeq	vcursisoff
	dect	SP
	mov	0,*SP
	movb	@h80,@vcurs		; force an "off" next time
	mov	@vcursor,0
	blwp	*0
	mov	+*SP,0
vcursisoff	rt



;--------------------------------------------------------------------------
;	Text mode window functions.
;--------------------------------------------------------------------------


;	Draw a char in the window.
;
;	VCH=char
;	VX+VWX=screen coord X
;	VY+VWY=screen coord Y
;	VFG/VBG=color


vtextchar data vidws,vtextchar 4+
	limi	0
	li	SP,vstack vstacksize +

	mov	@vx,0
	mov	@vcoordaddr,1
	bl	*1				; get address

	bl	@vwaddr				; set VDP addr
	movb	@vch,@>8c00			; draw
	rtwp


;	Draw a char in the window.
;
;	VCH=char
;	VX+VWX=screen coord X
;	VY+VWY=screen coord Y
;	VFG/VBG=color


vbitchar data vidws,vbitchar 4+
	limi	0
	li	SP,vstack vstacksize +


	mov	@vx,0
	mov	@vcoordaddr,1
	bl	*1				; get address

	mov	0,4
	a	@vpatts,0
	bl	@vwaddr				; set VDP addr for patt

	li	0,grom_fonts
	movb	@vch,1
	srl	1,8
	sla	1,3
	a	1,0
	bl	@gwaddr				; set GROM addr
	li	1,>9800
	li	3,>8c00

	movb	*1,*3
	movb	*1,*3
	movb	*1,*3
	movb	*1,*3
	movb	*1,*3
	movb	*1,*3
	movb	*1,*3
	movb	*1,*3
	
	movb	@vmono,@vmono
	jne	vbcnocol

	mov	4,0
	a	@vcolors,0			; draw color
	bl	@vwaddr

	movb	@vfg,1
	sla	1,4
	andi	1,>f000
	movb	@vbg,2
	andi	2,>0f00
	soc	2,1

	movb	1,*3
	movb	1,*3
	movb	1,*3
	movb	1,*3
	movb	1,*3
	movb	1,*3
	movb	1,*3
	movb	1,*3

vbcnocol:
	rtwp






;	Clear the text window.
;
;	VWX/VWXS & VWY/VWYS are window coords
;	
;	VFG/VBG = fill color
;	
vtextclear data	vidws,vtextclear 4+
	limi	0
	li	SP,vstack vstacksize +

	movb	@h20,@vcursunder

	li	1,>2000
	movb	@vwxs,2
	srl	2,8
	li	6,vtextclearline

vtcentr	clr	0
	movb	@vwys,3
	srl	3,8


vtcloop	bl	*6
	inc	0				; next 'Y'
	dec	3
	jgt	vtcloop

	clr	@vx

	rtwp


;	Clear the text window.
;
;	VWX/VWXS & VWY/VWYS are window coords
;	
;	VFG/VBG = fill color
;	
vbitclear data	vidws,vbitclear 4+

	limi	0
	li	SP,vstack vstacksize +

	sb	@vcursunder,@vcursunder

	clr	1
	movb	@vwxs,2
	srl	2,8
	sla	2,3
	li	6,vbitclearline

	jmp	vtcentr


;	
;	Blink the cursor.
;
;	Expects "vcursunder" to be valid.
;
;	Called from interrupt.

vtextcursor	data vidws,vtextcursor 4+
	limi	0
	li	SP,vstack vstacksize +

	li	12,>8c00
	mov	@vx,0
	mov	@vcoordaddr,1
	bl	*1

	ab	@h80,@vcurs
	jeq	vtcoff

	bl	@vraddr			; read char under cursor
	movb	@>8800,@vcursunder

	bl	@vwaddr
	movb	@vcurschar,*12		; draw cursor

	jmp	vtcout

vtcoff:
	bl	@vwaddr
	movb	@vcursunder,*12		; restore char under cursor

vtcout:
	rtwp



;	
;	Blink the cursor.
;
;	Expects "vcursunder" to be valid.
;
;	Called from interrupt.

vbitcursor	data vidws,vbitcursor 4+

	limi	0
	li	SP,vstack vstacksize +

	li	12,>8c00
	mov	@vx,0
	mov	@vcoordaddr,1
	bl	*1
	ai	0,7			; point to bottom of char
	a	@vpatts,0		; only change patt

	ab	@h80,@vcurs
	jeq	vbcoff

	bl	@vraddr			; read char under cursor
	movb	@>8800,@vcursunder

	bl	@vwaddr
	movb	@vcurschar,*12		; draw cursor

	jmp	vbcout

vbcoff:
	bl	@vwaddr
	movb	@vcursunder,*12		; restore char under cursor

vbcout:
	rtwp




;---------------------------------------------------------------

;	Scroll the text cursor down.
;
;	VWX/VWXS & VWY/VWYS are window coords
;	
;	VFG/VBG = fill color

vbitscroll data	vidws,vbitscroll 4+

	limi	0
	li	SP,vstack vstacksize +

	li	0,13
	clr	1
	jmp	vtext0

vgraphscroll data vidws,vtextscroll 4+
vtextscroll data vidws,vtextscroll 4+
	limi	0
	li	SP,vstack vstacksize +

	clr	0
	li	1,>2000				; clear char

vtext0 	movb	@vwxs,2				; length
	srl	2,8
	src	2,0

	mov	@vx,0
	andi	0,>ff				; get coord for lower-left
	bl	@vtextclearline
	inc	0
	swpb	0
	cb	0,@vwys
	jl	vtext1
      	sb	0,0
vtext1	swpb	0
	bl	@vtextclearline
	
	rtwp


;	This routine will draw a line on the screen.
;
;	(R1,R2) - (R3,R4)  (x,y)   VFG/VBG

vbitline data	vidws,vbitline 4+
	limi	0
	li	SP,vstack vstacksize +

	li	1,M_bit 256 *
	cb	1,@vidmode
	jeq	vblcont

	rtwp

vblcont:
	mov	@2(13),1
	mov	@4(13),2
	mov	@6(13),3
	mov	@8(13),4

	mov	1,5
	s	3,5
	abs	5

	mov	2,6
	s	4,6
	abs	6

	c	5,6				; which axis is longer?
						; R6>=0 means Y is longer
						; R6<0  means X is longer
	jgt	vbl_x

;	Y is the longer axis.  For this case, Y will step by +-1 and
;	X will step +-(dX/dY).
;
;	Set up R7 to be a pointer to a MINOR incrementer (+-X) and
;	       R8 to                 a MAJOR incrementer (+-Y).
;	       R9 to                 the MINOR increment value (+)
;	       R0 to be the address and bit offset. 

	bl	@vbl_getaddr			; get start addr

	mov	6,12				; R12 = # pixels

	li	7,vbl_xincs
	li	8,vbl_yincs			; both point to positives

	mov	5,9
	clr	10
	div	6,9				; R9 = increment
	clr	6
	jno	vbl_ymajnostr

	inct	6
	clr	9				; use force-carry

vbl_ymajnostr:
	c	1,3
	jlt	vbl_ymajxord
	inct	7				; R7 -> decrementer

vbl_ymajxord:
	c	2,4
	jlt	vbl_ymajyord
	inct	8				; R8 -> decrementer

vbl_ymajyord:
	jmp	vbl_draw


vbl_x:
;	X is the longer axis.  For this case, X will step by +-1 and
;	Y will step +-(dY/dX).
;
;	Set up R7 to be a pointer to a MINOR incrementer (+-Y) and
;	       R8 to                 a MAJOR incrementer (+-X).
;	       R9 to                 the MINOR increment value (+)
;	       R0 to be the address and bit offset. 

	bl	@vbl_getaddr			; get start addr
	
	mov	5,12				; R12 = # pixels
	
	li	7,vbl_yincs
	li	8,vbl_xincs

	mov	6,9
	clr	10
	div	5,9				; R9 = incrementer
	clr	6
	jno	vbl_xmajnostr

	clr	9				; use another routine
	inct	6

vbl_xmajnostr:
	c	1,3
	jlt	vbl_xmajxord
	inct	8

vbl_xmajxord:
	c	2,4
	jlt	vbl_xmajyord
	inct	7

vbl_xmajyord:


;	During drawing:
;
;	R0 = vdp addr offset
;	R1 = shift index (0-7)
;	R2 = color byte
;	
;	With incremental increment (fractional):
;		R9  = increment (unsigned)
;		R3 = incrementer
;		when C=1, call *R9
;
;	*R6 = straight-line fix
;	*R7 = minor increment
;	*R8 = major increment/decrement
;	R12 = # pixels to draw
;

vbl_draw:
	mov	*7,7
	mov	*8,8

	mov	6,6				; diagonal?
	jeq	vbl_dno6			; nope

	mov	7,6
	jmp	vbl_draw0			; inc every time

vbl_dno6 li	6,vbl_dinc

vbl_Draw0:
	mov	0,1
	andi	1,7				; R1 = bit offs
	srl	0,3				; R0 = vdp offset

	clr	2
	movb	@vfg,2
	sla	2,4
	socb	@vbg,2				; R2= color byte

	clr	3				; clear incrementer
vbl_loop:
	bl	@vbl_drawpixel			; draw a pixel

	bl	*8				; major increment
	bl	*6				; minor
	dec	12				; go through all the pixels
	jgt	vbl_loop
	rtwp


vbl_dinc a	9,3
	jnc	vbl_dinc0
vbl_nop	b	*7				; major increment
vbl_dinc0 rt


;	Get the starting VDP offset, scaling the coords 
;	and saving in "vlinex" and "vliney"
;
;	(R1,R2) = coord.
;
;	Return R0=offset*8+bit offset (0-7)
;	Destroys R9, R12

h256	data	256
h192	data	192
hneg1	data	>ffff

vbl_getaddr

;-----------------------------

	si	SP,8
	mov	1,@6(SP)
	mov	2,@4(SP)
	mov	3,@2(SP)
	mov	4,*SP

	andi	1,>ff				; scale down X
	mov	1,@vlinex

	clr	3
	mov	2,4
	jgt	vbl_posy

	ai	4,32640				; make positive
vbl_posy:
	div	@h192,3
	mov	4,@vliney
	mov	4,2				; scale down Y

	mov	+*SP,4
	mov	+*SP,3
	mov	+*SP,2
	mov	+*SP,1

;-----------------------------

	mov	@vliney,0 			; copy Y coord

	sla	0,5				; R0=Y offset (row8) ++
	soc	@vliney,0 			; R0=Y offset (row) ++
	andi	0,>ff07				; R0=complete Y offset

	mov	@vlinex,12
	mov	12,9			      	; R2=copy of X
	andi	12,7				; R1=complete X bit offs
	a	9,0				; R0=Y-X offset ++
	s	12,0				; R0=complete Y-X offset
	sla	0,3				; shift up
	soc	12,0				; R0=offset & bit val
	rt


;	Draw a pixel.
;
;	R0=addr, @vbl_shifts(r1) = bit mask.
;	R2=color byte
;
;	Destroys R4.

vbl_drawpixel
	mov	11,10
	bl	@vraddr				; patts always at 0
	movb	@>8800,4			; get old byte
	socb	@vbl_shifts(1),4		; set bit
	bl	@vwaddr				
	movb	4,@>8c00			; write back
	movb	@vmono,@vmono
	jne	vbl_dpnocol

	a	@vcolors,0			; point to colors
	bl	@vwaddr				
	movb	2,@>8c00			; set color
	s	@vcolors,0
vbl_dpnocol:
	b	*10			


vbl_shifts byte	>80,>40,>20,>10,>8,>4,>2,>1
vbl_xincs data	vbl_xpos,vbl_xneg
vbl_yincs data	vbl_ypos,vbl_yneg


h8	data	8

;----------------------------
vbl_xpos:					; increment X
	inc	1
	andi	1,7
	jne	vbl_xpos1			; rollover?
	ai	0,8				; next X cell
vbl_xpos0:
	a	@h8,@vlinex
	c	@vlinex,@h256
	jl	vbl_xpos1
	s	@h256,@vlinex
	si	0,256
vbl_xpos1:
	rt

;----------------------------

vbl_xneg:					; increment Y
	dec	1
	joc	vbl_xneg1			; rollover?
	si	0,8				; previous X cell
vbl_xneg0:
	andi	1,7
	s	@h8,@vlinex
	c	@vlinex,@hneg1
	jgt	vbl_xneg1
	a	@h256,@vlinex
	ai	0,256
vbl_xneg1:
	rt

;----------------------------

vbl_ypos:
	inc	0				; down one row
	czc	@h7,0				; out of cell block?
	jne	vbl_ypos1
	ai	0,248				; next cell row
vbl_yposmnot:
	a	@h8,@vliney
	c	@vliney,@h192
	jl	vbl_ypos1
	s	@h192,@vliney
	si	0,>1800
vbl_ypos1:
	rt

;----------------------------

vbl_yneg:
	dec	0    				; up one row
	coc	@h7,0				; out of cell block?
	jne	vbl_yneg1
	si	0,248				; previous cell row
vbl_ynegmnot:
	s	@h8,@vliney
	c	@vliney,@hneg1
	jgt	vbl_yneg1
	a	@h192,@vliney
	ai	0,>1800
vbl_yneg1:
	rt
	

