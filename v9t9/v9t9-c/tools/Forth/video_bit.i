;	video_bit.i	 				-- classic bitmap mode routines 
;
;	(c) 1996-2008 Edward Swartz
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
;	$Id: video_bit.i,v 1.13 2009-02-13 02:02:11 ejs Exp $


vbitmapsetup
	movb	#00,@vmono
	li 	   1, vbit
vbitsentr:
    PUSH    SP,11
    movb    #M_bit,@vidmode

	bl	    @vsetupregs
	bl	    @vsetupaddrs
	mov    1,@vtermptr
    bl      @vstdmode
    
    POP     SP,11
	rt

vmonosetup
	movb	#1,@vmono
	li     1, vmonobit
	jmp	   vbitsentr

vbitmap3setup
    li      1, vbit3
    jmp     vbitsentr

;   (Called internally)
;
;   Figure the address and shift for a pixel on the screen
;
;   R0=Y, R2=X
;
;   Yields R0=address, R1=shift
;
vbitpixeladdr
    mov    0,1
    sla    0,5              ; R0=Y offset (row8) ++
    soc    1,0              ; R0=Y offset (row) ++
    andi   0,>ff07          ; R0=complete Y offset

    mov    2,1              ; copy of X
    andi   1,7              ; complete X bit offs
    a      2,0              ; Y-X offset ++
    s      1,0              ; complete Y-X offset
    rt
    

;   (Called internally.  Preserve R2)
;
;   Figure screen addr for bitmap.
;
;   hi(R0) = X in window
;   lo(R0) = Y in window
;
;   Returns R0=addr, R1=shift

vbitaddr 
    PUSH    SP,2,11

    a       @vwx, 0
    
    ; scale by vbsize
    clr     1
    movb    @vbsize,1       ; X size (hi)
    clr     2
    movb    0,2             ; X (hi)
    mpy     1,2             ; R2=col #  (3 = 0)
    
    sla     0,8             ; Y
    movb    @vbsize+1,3
    mpy     0,3             ; 3=row#
    
    mov     3,0
    sb      0,0             ; R0=Y

    bl      @vbitpixeladdr
    
    POP     SP,2,11
    rt


;   Called from XOP [preserve 0, 3]
;
;   Clear some bitmapped line.
;   
;   R0=coord in window (X=0, Y=...)
;   R2=length in chars
;
vbitclearline
    PUSH    SP,0,3,11

    ; get start address and shift in R0/R1
    mov     @vcoordaddr,3
    bl      *3
    mov     0,4

    ; scale width by vbsize
    mov     1,8
    
    clr     1
    movb    @vbsize,1       ; X size (hi)
    ;clr     2
    ;movb    @vwxs,2         ; X (hi)
    sla     2,8
    mpy     1,2             ; 2=col #  (3 = 0)
    
    mov     8,1
    
    ; height is vbsize
    movb    @vbsize+1,8
    srl     8,8      

    bl      @vgetcolorbyte
    movb    0,12
    srl     12,8
    
    bl      @vbitfillrect

    POP     SP,0,3,11
    rt

;   Fetch the current char from GROM (for now) into a memory buffer.
;   Eventually this will be expanded to allow us to store fonts in
;   a pattern table in VDP RAM so the user can define custom fonts.
;
;   Input:
;       vch = char
;       vfont = table base
;   Returns:
;       R1 = ptr to char
vfetchfontchar
    PUSH    SP,11,0
    movb    @vch,0
    srl     0,8
    sla     0,3
    a       @vfont,0
    bl      @gwaddr             ; set GROM addr
    
    movb    @vbsize+1,11
    srl     11,8
    
    mov     SP, 1
    s       11, 1               ; get space on stack for char
    mov     1, 0
$1:
    movb    @GPLRD, *0+
    movb    @GPLRD, *0+
    dect    11
    jgt     $1-
         
    POP     SP,11,0
    rt

;	Draw a char in the window (8x8)
;
;	VCH=char
;	VX+VWX=char coord X
;	VY+VWY=char coord Y
;	VFGBG=color


 Vector vbitchar, vidws
	limi	0
	li		SP,vstack + vstacksize

	c       @vbsize,#>0808
	jeq		vbitfast
	b		@vbitcharsmall+4

vbitfast:
    mov     @vx,0
	mov		@vcoordaddr,1
	bl		*1				; get address in 0, shift in 1

	mov		0,4              
	
	bl      @vfetchfontchar
	
	a		@vpatts,0
	bl		@vwaddr				; set VDP addr for patt

	li		3,VDPWD
    li      2, 8
$1: movb    *1+,*3
    dec     2
    jgt     $1-
	
	movb	@vmono,@vmono
	jne		vbcnocol

	mov		4,0
	a		@vcolors,0			; draw color
	bl		@vwaddr

    bl      @vgetcolorbyte
    movb    0,1

    li      2, 8
$1: movb    1,*3
    dec     2
    jgt     $1-    

vbcnocol:
	rtwp


vbitcursorset
    mov     11,10 
    bl     @vraddr
    movb   @VDPRD, 1
    movb    1,*6+
    socb   3,1
    b       *10
    
vbitcursorreset 
    movb *6+,1
    rt
 
; Blink cursor in bitmapped modes
;
;
 Vector vbitcursor, vidws

	limi   0
	li	   SP,vstack + vstacksize

	mov    @vx,0
	mov	   @vcoordaddr,1
	bl	   *1
	
	a	   @vpatts,0		; only change patt
    bl     @vsetbank
	mov    0,4
    
    mov    1,0
    li     3,>c0c0
    src    3,0
    
    ; based on the cursor mode, we either save + modify or restore the bits under the cursor
    li      5,vbitcursorset
    ab      #>80,@vcurs
    jne     vbc1
    li      5,vbitcursorreset
vbc1:    
    li     6,vcursunder
    movb   @vbsize+1, 2
    srl    2,8
    
    li      7, 7
    li      12, 1
vbc0:
    mov     4,0
    bl      *5      ; read or write pixel
    bl      @vwaddr
    movb    1,@VDPWD
    
    a       12,4    ; next row
    czc     7,4     ; end of block?
    jne     vbc2
    ai      4,>ff   
    andi    4,>3ff8
vbc2:
    dec     2
    jgt     vbc0
    
	rtwp


;	This routine will draw a line on the screen (XOP routine)
;
;	R12 = -> op|c y2 x2 y x 
;
;
h14         data 14

vbitline
    PUSH   SP, 13, 14
    
    mov    @8(12), 1   ; X
    mov    @6(12), 2   ; Y
    mov    @4(12), 3   ; X2
    mov    @2(12), 4   ; Y2
    
	s	   1,3          ; X direction = R3-R1
	mov    3,7        
	abs	   7            ; X distance 

	s	   2,4          ; Y direction = R4-R2
	mov    4,8        
	abs	   8            ; Y distance

	c	   7,8			; which axis is longer?
						; R6>=0 means Y is longer
						; R6<0  means X is longer
	jgt	   vbl_x

;	Y is the longer axis.  We will reorient the line to enforce
;   downward movement.   For this case, Y will step by 1 and
;	X will step +-(dX/dY).

    li      14, vbl_ymajor
    
    mov     4, 4        ; going up?
    jgt     $1+
   
    a       4, 2
    neg     4           ; make it go the other way
    
    mov     @4(12), 1
    neg     3           ; minor direction swapped too
     
$1:    
    
    jmp     vbl_plotline
    
vbl_x:
;   X is the longer axis.  We will reorient the line to enforce
;   rightward movement.   For this case, X will step by 1 and
;   Y will step +-(dY/dX).

    li      14, vbl_xmajor

    mov     3, 3        ; going up/left?
    jgt     $1+
   
    a       3, 1        ; make it go the other way
    neg     3           
    
    mov     @2(12), 2
    neg     4           ; minor direction swapped too
    
     
$1:    
    
    ; Swap major and minor
    mov     3, 11
    mov     4, 3
    mov     11, 4
    
vbl_plotline:    
    inc     4
    
    mov    *12, 12
    
    ; get op
    bl      @vbitgetdrawfuncandcolor
    

    bl      @vbl_getaddr       ; get start addr (R1, R2) -> (R0, R1)
    a       @vpatts, 0
    
    bl      @vbl_getfracmags
    
    ; R7=minor increment
    ; R6=minor fraction 
    ; R3=minor magnitude

    clr    2                ; so we have a clean check for COC
    
    li     13, >2420        ; CZC <addr>, 0
    mov    3, 9
    
    jgt    $2+
    li     13, >2020        ; COC <addr>, 0
$2:    
    ci     14, vbl_ymajor
    jne    $3+
    
    ai     13, >40          ; COC|CZC ..., 1
    a      3, 3             ; X shift change is in increments of 2
    sla    9, 3             ; adjust direction for X byte change (8 bytes)
    jmp    vbl_linepixel

$3:
    mov    9, 11
    sla    11, 3
    sla    9, 8
    s      11, 9             ; adjust direction for Y block change (256-8)    
    
vbl_linepixel:    
;  Construct the line. 
;          R4 is                 the MAJOR axis
;          R3 is                 the MINOR axis
;          R0 is the address
;          R1 is the bit offset * 2
;          R2 is the current byte
;          R5 is the color
;          For Y-major, R13 is an instruction to test for X / R1 crossing a byte
;          R14 is the major incrementer BL@ address
;          R15 is the color modifier

    clr     2
    li      8, VDPWA

    bl      @vbitreadpixel_
        
vbl_lineloop:
    andi   1, >E            ; keep shift in range
   ;bl     @vbitdrawpixel_
    
    b      *14              ; next major 

vbl_lineloopret:    
    dec    4                ; one pixel consumed
    jgt    vbl_lineloop

    bl     @vbitwritepixel_
         
	clr    15               ; clr out what we did
	POP    SP, 13, 14
	rtwp


;   Handle the line moving in a Y-major orientation
;
vbl_ymajor:
     x       5
     data    vbl_shifts
     
     bl     @vbitwritepixel_
    
    inc    0                ; new Y
    czc    #7, 0            ; new block?
    jne    $1+              ; nope

    ai     0,256-8          ; next block
    andi   0, >1fff         ; keep in range

$1:    
    a      6, 7
    jnc    $1+              ; new X yet?
    
    a      3, 1             ; yup, adjust shift
    x      13               ; test for going left or right and crossing the byte
    data   h14
    
    jne    $1+              ; not yet
    
    mov    0, 2
    a      9, 0             ; move across a row (8 bytes)
    movb   2, 0             ; keep in range
$1:    
    bl     @vbitreadpixel_
    jmp     vbl_lineloopret
    
;   Handle the line moving in a X-major orientation
;   We cache the byte in R2 until we know the address has changed.
;
vbl_xmajor: 
    x       5
    data    vbl_shifts
    
    inct   1                ; adjust shift
    czc    @h14, 1          ; test for going right and crossing the byte
    
    jne    $1+              ; not yet
    
    bl     @vbitwritepixel_
    
    ab     #8, @vidws+1     ; move across a row (8 bytes), keeping in range
    
    bl     @vbitreadpixel_
    
$1:
    
    a      6, 7
    jnc    vbl_lineloopret   ; new Y yet?

    bl     @vbitwritepixel_
    
    mov    0, 2             ; remember original address
    
    a      3, 0             ; yup
    x      13               ; new block?
    data   h07
    jne    $2+              ; nope

    a      9, 0             ; next/previous block

$2:    
    xor    0, 2             ; check if wrapped
    jgt    $3+
    
    andi   0, >1fff         ; keep in range
    ai     0, ->800
$3:
    bl     @vbitreadpixel_
    jmp    vbl_lineloopret

;          R4 is                 the MAJOR axis (+)
;          R3 is                 the MINOR axis (+/-)
vbl_getfracmags:
    mov     3, 6       
    abs     6         
    clr     7
    div     4, 6            ; R6=minor increment
    clr     7               ; R7=minor fraction 
    jno     $1+
    
    seto    6               ; close enough to straight
    seto    7               ; force immediate carry    
$1:    
    
    ; Convert R3 to the magnitude for shift adjust (-2, 0, 2)

    mov     3, 3
    seto    3
    jlt     $1+
    clr     3
    jeq     $1+
    inc     3
    
$1:
    rt


;	Get the starting VDP offset, moving coords into range
;
;	(R1,R2) = coord.
;
;	Return R0=offset, R1=shift*2

vbl_getaddr
    PUSH    SP, 2, 11

	sb      1,1    				; scale down X
    mov     1, 11
    
    clr     1
    div     #192, 1              ; scale down Y (R2=mod)
    mov     2, 0

    ; R0=Y R2=X
    mov     11, 2
    bl     @vbitpixeladdr
    
    a      1,1                ; shift in increments of 2 
	
    POP    SP,2,11
	rt

;   Get the drawing function
;
;   hi(R12) = op, lo(R12) = fg color
;
;   yields  R5 = X for pattern: xxxB @addr(R1), R2
;           R12 = color byte
;
vbitgetdrawfuncandcolor
    
    clr     5
    movb    12, 5
    andi    5, >F00 
    srl     5, 7
    mov     @vdrawops(5), 5    ; R5 = pattern operation

    sla     12, 12
    socb    @vfgbg+1, 12 ; R12 = color
    
    rt
    
;	Draw a pixel.
;
;	R1=X, R2=Y
;	R12=op|color byte
;
vbl_drawpixel PUSH SP, 5, 8, 11, 12
    bl     @vbitgetdrawfuncandcolor

    bl     @vbl_getaddr

    li     8,VDPWA    
    bl     @vbitreadpixel_
    
    x      5
    data   vbl_shifts
 
    bl     @vbitwritepixel_ 
    
    POP    SP, 5, 8, 11, 12
    rt

;
;   R0=address
;   R8=VDPWA
;   yields R2=patt|0
;
vbitreadpixel_ 
    movb   @vidws+1, *8
    movb   0, *8
    
    movb   @VDPRD, 2            ; get patt byte
    rt

;
;   R0=address
;   R2=patt|0
;   R12=color
;   R8=VDPWA
;
vbitwritepixel_ 
    ori    0, >4000
    movb   @vidws+1, *8
    movb   0, *8
    
    movb   2, @VDPWD            ; put patt byte

    movb   @vmono,@vmono
    jne    $1+
        
    xor    #>2000, 0            ; pattern -> color table

    movb   @vidws+1, *8
    movb   0, *8
    
    movb   12, @VDPWD          ; put color byte
    
    xor    #>2000, 0            ; pattern -> color table
$1:
    andi   0, >3fff            ; turn off write bit    
    rt
    
;   To correspond with V9938:   SET, OR, AND, XOR, NOT, xx, xx, xx
vdrawops: 
    dw  >F0A1, >F0A1
    dw  >50A1, >28A1            ; nop is CB
    dw  >90A1, >90A1, >90A1, >90A1

vbl_shifts byte >80,0,>40,0,>20,0,>10,0,>8,0,>4,0,>2,0,>1,0

;	Draw a smaller than 8x8 char in the window.
;	This reads the character's destination in one or two strips
;	then blits the pattern onto it.
;
;	VCH=char
;	VX+VWX=screen coord X
;	VY+VWY=screen coord Y
;	VFGBG=color

 Vector vbitcharsmall, vidws
	limi	0
	li		SP,vstack + vstacksize

	mov		@vx,0
	mov		@vcoordaddr,1
	bl		*1				; get address in 0, shift in 1

	mov		0,3
	andi	3,7
	neg		3
	ai		3,8				; R3=# pixels before row block


	mov		0, 4

	; first, draw with R0 shift and the left 8-shift bit
	
	seto	5
	movb	@vbsize, 0
	srl		0,8
	jeq		vbscs0				; avoid losing all bits (=16)
	srl		5, 0				; bitmask
vbscs0:
	inv		5				; e.g. FF00 or FC00

	mov		1,0
	src		5,0				; e.g. 03F0
	bl		@vbitcharstrip

	; then draw the next strip over one block
	li		1,8
	a		1,0
	ci		0,8
	jle		vbcsout
	ai		4,8
	swpb	5
	bl		@vbitcharstrip
vbcsout:

	rtwp

	
;	Draw one character strip inside a column of 8x8 blocks
;
;	R3 = height before next 32x8 strip (preserved)
;	R4 = offset	(preserved)
;	R5 = mask	(preserved)
;	R0 = shift	(preserved)
;	VCH = char
;	VBSIZE+1 = height

vbitcharstrip
    PUSH    SP, 0, 1, 3, 4, 7, 8, 9, 11, 12
	mov		0,9

	; read video memory into RAM
	li		2,vbitbuf

	; read all the current char bytes and mask off bits to modify
	movb 	@vbsize+1, 8
	srl		8,8

	a		@vpatts,4
	mov		4,7

	bl		@vbitreadstrip

    bl      @vfetchfontchar

	mov		7,4
	mov		4,0
	bl		@vwaddr				; set VDP addr for patt

	mov		9,0
	mov		8,3
vbsblit:
	clr		12
	movb	*1+,12
	src		12,0
	socb	*2+,12
	movb	12,@VDPWD
	dec		3
	jeq 	vbsblit_0
	inc		4
	czc		@h07,4
	jne		vbsblit
	dec		4
	bl		@vbsnextblock
	data	vwaddr
	mov		9,0
	jmp		vbsblit

vbsblit_0:
    bl      @vgetcolorbyte
    movb    0,1
	mov		7,4
	bl		@vbitsetcolorstrip

    POP     SP, 0, 1, 3, 4, 7, 8, 9, 11, 12
	rt

;	Move the VWADDR to the next block under the place
;	where we leave an 8x8 block.  
;
;	R4 = original VWADDR (updated)
;	Uses R0
;	vraddr or vwaddr follows *R11
vbsnextblock ; PUSH   SP, 1, 11
	ai 		SP,-4
	mov		1,@2(SP)
	mov		*11+,1
	mov 	11,*SP
	ai		4,>100
	andi	4,>3ff8
	mov		4,0
	bl		*1
	mov 	*SP+, 11
	mov 	*SP+, 1
	rt

;	Read a strip of bitmapped data (0-8 pixels wide,
;	0-8 pixels high).  Caller should ensure that
;	the height won't leak into the next 8x8 block.
;
;	R4 = pattern table offset (preserved)
;	R2 = buffer (preserved)
;	R5 = mask pixels (preserved)
;	R8 = height (preserved)
;
;	Destroys R0
;
vbitreadstrip PUSH  SP,4, 6, 9, 11
	mov		4,0
	bl		@vraddr				; set VDP addr for patt

	; read video memory into RAM

	; read all the current char bytes and mask off bits to modify

	mov 	8,9
vbrsrd:
	movb 	@VDPRD,6
	szcb 	5, 6
	movb 	6,*2+
	dec 	9
	jeq 	vbrsrd_1
	inc		0
	czc		@h07,0
	jne		vbrsrd
	dec		0
	bl		@vbsnextblock
	data	vraddr
	jmp		vbrsrd
vbrsrd_1:
	
	s 		8, 2			; reset buf ptr

    POP     SP, 4, 6, 9, 11 
	rt

;	Set the color of a strip of a column of pixels. 
;	Ignored in mono modes.
;
;	R4 = pattern table address (preserved)
;	R8 = height (preserved)
;	R1 = color|0
;	

vbitsetcolorstrip
    PUSH    SP, 4, 8, 11

	movb	@vmono,@vmono
	jne	vbcsnocol

	s		@vpatts,4
	a		@vcolors,4

	mov		4,0				; draw color
	bl		@vwaddr

vbsclr:
	movb	1,@VDPWD
	dec		8
	jeq		vbcsnocol
	inc		0
	czc		@h07,0
	jne		vbsclr
	dec		0
	bl		@vbsnextblock
	data	vwaddr
	jmp		vbsclr

vbcsnocol:
    POP     SP, 4, 8, 11
	rt

;	Fill a bitmapped rectangle.
;	
;	R4=addr
;	R1=shift 
;	R2=size in pixels X
;   R8=size in pixels Y 
;	R12=patt|color
;
vbitfillrect
    PUSH    SP,11

	; check for a work in the right part of an 8x8 column

	mov		1,1			; any shift?
	jeq		vbfr_mid

vbfr_left:
	; calc the shift right (for left side)
	li		5,>FF00

	ci		2,8
	jhe		vbfr_fulltoleft

	; not full to the left of the next 8x8 block
	mov    	2,0
	andi	0,7
	sla		5,0

	s		2,2				; full width taken care of

vbfr_fulltoleft:

	mov		1,0
	andi	0,7
	srl		5,0				; bitmask

	bl		@vbitunalignedfill

	ai		4,8				; skip that partial column

	mov    	2,2				; already done?
	jeq		vbfr_out

	ai		2,8
	s		1,2				; remove lhs strip from width to use

;;;;;; middle, with solid 8x8 blocks   TODO: optimize this!  We can fill Nx8 blocks more quickly

vbfr_mid:
	ci		2,8
	jl		vbfr_right

	; fill a column of 8xR8 blocks
	bl		@vbitalignedfill
	ai		4,8
	ai		2,-8
	jmp		vbfr_mid

;;;;;; right side, only handling flush left

vbfr_right:
	mov		2,1           	; # pixels remaining
	andi	1,7				; # pixels on right
	jeq		vbfr_out

	seto	5

	li		0,7
	s		1,0
	jeq		vbfr_r1
	srl		5,0				; bitmask
vbfr_r1:

	; adjust patt addr
	
	bl		@vbitunalignedfill

vbfr_out:

    POP     SP,11
	rt


;	Fill a column of pixels in a 1x8 - 7x8 block of some height
;
;	R4 = pattern table offset (preserved)
;	R5 = mask  (preserved)
;   R8 = pixel height
;	R12 = patt|color (preserved)
;
;	Destroys R0
;
vbitunalignedfill
    PUSH    SP, 1, 2, 7, 9, 11, 12

	mov		4,7
	; read video memory into RAM
	li		2,vbitbuf

	bl		@vbitreadstrip

	mov		7,4
	mov		4,0
	bl		@vwaddr				; set VDP addr for patt

	mov		8,9
vbufblit:
	socb	12,*2
	movb	*2+,@VDPWD
	dec		9
	jeq 	vbufblit_0
	inc		4
	czc		@h07,4
	jne		vbufblit
	dec		4
	bl		@vbsnextblock
	data	vwaddr
	jmp		vbufblit

vbufblit_0:
	mov		12,1
	swpb	1
	mov		7,4
	bl		@vbitsetcolorstrip

    POP     SP, 1, 2, 7, 9, 11, 12
	rt

;	Fill a column of pixels in an 8x8 block of some height
;
;	R4 = pattern table offset (preserved)
;	R8 = height in rows (preserved)
;	R5 = mask  (preserved)
;	R12 = patt|color (preserved)
;
;	Destroys R0
;
vbitalignedfill
    PUSH    SP, 1, 2, 7, 9, 11    

	mov		4,7
	mov		4,0
	bl		@vwaddr				; set VDP addr for patt

	mov		8,9
	li		1,7
vbafblit:
	movb	12,@VDPWD
	dec		9
	jeq 	vbafblit_0
	inc		0
	czc		1,0
	jne		vbafblit
	bl		@vbsnextblock
	data	vwaddr
	jmp		vbafblit

vbafblit_0:
	mov		12,1
	swpb	1
	mov		7,4
	bl		@vbitsetcolorstrip

    POP     SP, 1, 2, 7, 9, 11    
	rt
