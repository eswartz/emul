;   video_text.inc
; 
;   (c) 1996-2014 Ed Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 
  
vtextsetup
    PUSH    SP,11
    li      1,vtxt
    movb    #M_text,@vidmode
    bl      @vstdmode
vtextsetup0:
	bl	    @vsetupregs
	bl	    @vsetupaddrs
	mov    1,@vtermptr
    
	POP    SP,11
	rt
	
vtext2setup
    PUSH    SP,11
    bl      @venhmode
    movb    #M_text2,@vidmode
    
    ; set inverted bg/fg for blink color
    clr     0
    movb    @vfgbg,0
    swpb    0
    movb    @vfgbg,0
    src     5, 0
    movb    #>C,0
    bl      @vwreg
    
    li      1,vtxt2
    jmp     vtextsetup0


vgraphsetup
    movb    #M_graph,@vidmode
	li	1,vgfx
vgraphsetup0:	
    PUSH    SP,11
	bl	@vsetupregs
	bl	@vsetupaddrs
	mov 1,@vtermptr
    ;bl  @vtermsetup
    bl  @vstdmode
    POP     SP,11
	rt

vmultisetup
    movb    #M_multi,@vidmode
    li  1,vmulti
    jmp vgraphsetup0
    
;   (Called internally.  Preserve R2)
;    
;   Figure screen addr for a text mode
;
;   hi(R0) = X in window
;   lo(R0) = Y in window
;
;   Returns R0=addr

vtextaddr PUSH SP, 2
    
    mov     0,2              ; save X
    swpb    0

    ab      @vwy,0
    srl     0,8
    mpy     @vwidth,0        ; get row offset in R1
    
    movb    2,0
    ab      @vwx,0
    srl     0,8              ; get column offset

    a       1,0                ; column+row
    a       @vscreen,0         ; R0=addr
    a       @vtextpage,0

    POP     SP, 2
    rt


;   (Called internally.  Preserve R2)
;
;   Figure screen addr for gfx.
;
;   hi(R0) = X in window
;   lo(R0) = Y in window
;
;   Returns R0=addr

vgraphaddr PUSH SP, 2
    mov     0,2             ; save X
    swpb    0

    ab      @vwy,0
    srl     0,8
    sla     0,5

    movb    2,1
    ab      @vwx,1
    srl     1,8                 ; get column offset

    a       1,0             ; column+row
    a       @vscreen,0          ; R0=addr

    POP     SP, 2
    rt


;   Called from XOP [preserve 0, 3]
;
;   Clear some text line.
;   
;   R0=coord
;   R2=length

vtextclearline
    PUSH    SP,0,3,11

    mov     @vcoordaddr,3
    bl      *3
    bl      @vsetbank
    li      1,>2000
    ;movb    @vwxs, 2
    ;srl     2,8 
    bl      @vclr               ; clear out line
    
    cb      #M_text2, @vidmode
    jne     vtextclearlineout
    
    ; clear blinks
    clr     1
    movb    @vblinkflag, 3
    jeq     vtextclearline0
    seto    1    
vtextclearline0:
    srl     2, 3                ; NOTE: only works on 8-char aligned windows
    bl      @vtxt2blink
    bl      @vclr               ; set blink 

vtextclearlineout:         
    POP     SP,0,3,11
    rt


;--------------------------------------------------------------------------
;	Text mode window functions.
;--------------------------------------------------------------------------


;	Draw a char in the window.
;
;	VCH=char
;	VX+VWX=screen coord X
;	VY+VWY=screen coord Y
;	VFGBG=color


 Vector vtextchar, VidWS
	limi   0
	li	   SP,vstack + vstacksize

	mov	   @vx,0
	mov	   @vcoordaddr,1
	bl	   *1				; get address
    
    bl     @vsetbank            ; set bank / page
	bl	   @vwaddr				; set VDP addr
	movb   @vch,@VDPWD			; draw

    cb     #M_text2, @vidmode	
	jeq    vtextchar0
	rtwp
	
vtextchar0:
    ; set blink flag
    bl     @vtxt2blink
	bl     @vraddr
	movb   @VDPRD, 2
	bl     @vwaddr
	li     1, >8000
    andi   0, 7
    jeq    vtextchar1
	srl    1, 0
vtextchar1:	
	szcb   1, 2
	movb   @vblinkflag, 0     
	jeq    vtextchar2
	socb   1, 2
vtextchar2:	   
    movb   2, @VDPWD
	rtwp

vtxt2blink
    s      @vscreen, 0
    srl    0, 3
    andi   0, >1ff
    a       @vtextpage, 0
    a      @vcolors, 0
    andi   0, >3fff
    rt

;	
;	Blink the cursor.
;
;	Expects "vcursunder" to be valid.
;
;	Called from interrupt.

  Vector vtextcursor, VidWS
	limi	0
	li	    SP,vstack+  vstacksize

	mov	   @vx,0
	mov	   @vcoordaddr,1
	bl	    *1
	
	bl     @vsetbank
	ab	    #>80,@vcurs
	jeq	   vtcoff

	bl	    @vraddr			; read char under cursor
	movb	@VDPRD,@vcursunder

	bl	    @vwaddr
	movb	@vcurschar,@VDPWD		; draw cursor

	jmp	vtcout

vtcoff:
	bl	    @vwaddr
	movb	@vcursunder,@VDPWD		; restore char under cursor

vtcout:
	rtwp


vnopchar:
vnopcursor:
    data VidWS, vtcout
vnopclearline:
    rt    
