;   video.i
; 
;   (c) 2010-2013 Edward Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 

;
;   Video routines typically use XOPWS or VIDWS.
;
;   Those using VIDWS may be called during interrupts, so
;   VDP interrupts must be disabled during video operations.
;   (This is good thinking anyway!)
;

h08	    equ	8
hunder	equ	'_'
h07	    equ	7

;	Initialize video.  Defaults to text mode.
;

vinit	  PUSH    r0
	
	mov    _VIDVARSTART, @vidvarstart
	mov    _VIDVARSIZE, @vidvarsize

	ldc	   #15,r0
	mov	   r0,@vcrstimer			 ; set up standard blink

	ldc	   >0107, r0
	mov	   r1,@vfgbg

    mov     #>808, @vbsize          ; set in case we lazily switch to bitmap mode

    clr    r1
	call   vsetmode
	call   vreset
	call   vscreenon

	POP    R0
	ret

; Set vfgbg to color in R0 word and update mono modes
vsetcolor
	mov    r0, @vfgbg
	
	cmp     M_text,@vidmode
	jeq    vsetcolor1
	cmp     M_text2,@vidmode
	jne    vsetcolor0

vsetcolor1:
    call   vgetcolorbyte
    swpb   r0
	or     >700, r0
	call   vwreg

	jmp    vsetcolor9

vsetcolor0:
	call   vcolorsetup
vsetcolor9:
	ret

;   Get vfgbg in single byte
vgetcolorbyte
    mov.b   @vfgbg,r0
    lsh     #4,R0
    or.b    @vfgbg+1,r0
    ret
    
;   Get vfg in single byte
vgetfgcolorbyte
    mov.b   @vfgbg,r0
    lsh     4, r0
    ret

;=========================================================================
;	Internally called routines
;	
;	(IM=0)
;=========================================================================

;	Set VDP write/read addr in R0.
;   These are legacy versions which assume that the 
;   address is pre-masked to the 0->3FFF range.

vwaddr	or	>4000, r0
vraddr
	mov.b	r0,&VDPWA
	swpb	r0
	mov.b	r0,&VDPWA
	swpb	r0
	and     >3fff, r0
	ret
	
;   Set a VDP register, remembering values stored to
;   VRs 0 through 14 for later restoration if needed.
vwreg  push r1
    mov     r0, r1
    and     >7FFF, r1
    or      >8000, r0
    cmp     >0f00, r1
    jge     vwreg0
    rsh     #8, r1
    mov.b   r0,@savedvregs(1)
vwreg0:    
    swpb    r0
    mov.b    r0,&VDPWA
    swpb    r0
    mov.b    r0,&VDPWA
    pop R1
    ret

;   Read a stored VDP register
;   Input:
;       R1=index (0-15)
;   Output:
;       R1=value (word)
vrreg
    mov.b    @savedvregs(r1),r1
    rsh     #8, r1
    ret
    
; Restore the video registers and turn on the screen
;
; Called from XOPWS, can modify any regs
vrestoremode
    ldc  VDPWA, r12
    ldc savedvregs, r1
    ldc >80, r0
vrm0 mov.b *r1+, *r12
    mov.b r0, *r12
    inc r0
    cmp >8f, r0
    jne  vrm0
    call vscreenon
    ret

;	Video clear.  Caller should set the bank first.
;
;	R0=addr
;	R1=char
;	R2=#

vclr	push R2
	call vwaddr
	loop R2: mov.b R1,&VDPWD
    pop R2
	ret



;==========================================================================
;	Externally called video functions.
;
;	IM=1, so we must turn off interrupts to use VDP.
;
;==========================================================================


;	Turn the screen on or off.
;
;	NOTE:	TIMER & KBD interrupt call these.  
;	Don't change any registers or use the stack!
;

vscreenoff
	nand 	#>40,@vregr1
	jmp	vreg1set
vscreenon
	or	    #>40,@vregr1
vreg1set:
	mov.b	@vregr1,&VDPWA
	mov.b	#>81,&VDPWA
	ret

;-------------------------------------------------------------------------

vtmap	dw	vscreen,vscreensz,vcolors,vcolorsz,vpatts,vpattsz,vsprpat,vsprites,vsprmot,vsprcol,vfree
	dw  vscrnx,vscrny
vtmapsize equ $-vtmap

;       0=text mode
;           >0000 = screen
;           >0800 = patts
;           >1000+= free
;
vtxt db 0,>0,1,>B0,2,>0,4,>1,-1
    dw  >0,960,>0,>0,>800,>800,0,0,0,0,>1000
    dw 256,192
    dw vtextterms, >2818, hunder

;       1=graphics mode
;           >0000 = screen
;           >0300 = sprites
;           >0380 = colors
;           >03A0 = sprite motion
;           >0420 = sprite patterns (really 0->800)
;           >0800 = char patts
;           >1000+= free
vgfx 	db	0,>0,1,>A0,2,>0,3,>E,4,>1,5,>6,6,>0,-1
	dw	>0,768,>380,>20,>800,>800,>000,>300,>3A0,0,>1000
	dw 256,192
	dw vtextterms, >2018, hunder

;       2=bitmap mode (graphics 2 mode)
;           >0000 = patts
;           >1800 = screen
;           >1B00 = sprites
;           >1B80 = sprite motion
;           >1C00 = sprite patts    (really >1800)
;           >2000 = colors
;           >3800+= free
;
vbit db	0,>2,1,>A0,2,>6,3,>ff,4,>03,5,>36,6,>3,-1
	dw	>1800,>300,>2000,>1800,>0000,>1800,>1800,>1b00,>1B80,0,>3800
    dw 256,192
    dw vbitterms, >2018, hff

;       3=graphics 3 mode (256x192x8+8), sprites 2
;           >0000 = patts
;           >1800 = sprite patts
;           >1900 = sprite colors
;           >1B00 = sprites
;           >1B80 = sprite motion
;           >1C00 = sprite patts ( really >1800)
;           >2000 = colors
;           >3800 = screen
;           >3B00+= free
;
vbit3  db  0,>4,1,>A0,2,>E,3,>ff,4,>03,5,>36,>B,0,6,>3,-1
    dw  >3800,768,>2000,>1800,>0,>1800,>1800,>1b00,>1b80,>1900,>3B00
    dw 256,192
    dw vbitterms, >2018, hff
    
    
;       4=graphics 4 mode (256x212x16)
;           >0000 = patts
;           >7000 = sprite patts
;           >7800 = sprite colors
;           >7A00 = sprites
;           >7A80 = sprite motion
;           >8000+= free
;
vbit4  db  0,>6,1,>A0,2,>1F,5,>F4,>B,0,6,>E,-1
    dw >0000,0,>0000,>0000,>0,>6400,>7000,>7A00,>7A80,>7800,>8000
    dw 256,212
    dw >80, 1, >FFFE
    dw vbit4terms, >201B, hff

;       5=graphics 5 mode (256x212x4)
;           >0000 = patts
;           >7000 = sprite patts
;           >7800 = sprite colors
;           >7A00 = sprites
;           >7A80 = sprite motion
;           >8000+= free
;
vbit5  db  0,>8,1,>A0,2,>1F,5,>F4,>B,0,6,>E,7,>FF,-1
    dw  >0000,0,>0000,>0000,>0,>6A00,>7000,>7A00,>7A80,>7800,>8000
    dw 512,212
    dw >80, 2, >FFFC
    dw vbit5terms, >401B, hff

;       6=graphics 6 mode (512x212x16)
;           >0000 = patts
;           >D400 = sprite colors
;           >D600 = sprites (1B0)
;           >D680 = sprite motion
;           >D800 = sprite patts (1B)
;           >E000+= free
;
vbit6  db  0,>A,1,>A0,2,>1F,5,>AC,>B,>1,6,>1B,-1
    dw  >0000,0,>0000,>0000,>0,>D400,>D800,>D600,>D680,>D400,>E000
    dw 512,212
    dw >100, 1, >FFFE
    dw vbit4terms, >401B, hff

;       7=graphics 7 mode (256x212x256)
;           >0000 = patts
;           >D400 = sprite colors
;           >D600 = sprites (1AC)
;           >D680 = sprite motion
;           >D800 = sprite patts (1B)
;           >E000+= free
;
vbit7  db  0,>E,1,>A0,2,>1F,5,>AC,>B,>1,6,>1B,-1
    dw  >0000,0,>0000,>0000,>0,>D400,>D800,>D600,>D680,>D400,>E000
    dw 256,212
    dw >100, 0, >FFFF
    dw vbit7terms, >201B, hff

;       8=text 2 mode
;           >0000 = screen (to >870 for 212-line mode)
;           >0A00 = colors (blinks)
;           >1000 = patts
;           >1110+= free
;
vtxt2  db  0,>4,1,>B0,2,>0,3,>2f,>A,0,4,>2,>D,>22,-1
    dw  >0,2160,>A00,2160/8,>1000,>800,0,0,0,0,>1110
    dw  512, 212
    dw vtextterms, >501B, hunder
    
;      9=monochrome bitmap mode (graphics 2 mode)
;           >0000 = patts
;           >1800 = screen
;           >1B00 = sprites
;           >1B80 = sprite motion
;           >1C00 = sprite patts    (really >1800)
;           >2000 = colors
;           >2040+= free (seems like 2800)
;
vmonobit db 0,>2,1,>A0,2,>6,3,>9f,4,>03,5,>36,6,>3,-1
    dw  >1800,>300,>2000,>800,>0000,>1800,>1800,>1B00,>1B80,0,>2800
    dw 256,192
    dw vbitterms, >2018, hff

;       10=multicolor mode
;           >0000 = screen
;           >0300 = sprites
;           >0380 = sprite motion
;           >0800 = char patts (colors)
;           >1000 = sprites patts
;           >1800+= free
vmulti    db  0,>0,1,>8,2,>0,3,>0,4,>1,5,>6,6,>0,-1
    dw  >0,768,0,0,>800,>800,>1000,>300,>380,0,>1800
    dw 256,192
    dw vmultiterms, >2018, hunder
    

hff equ >ff

vmodesetups	
    dw vtextsetup       ; 0
    dw vgraphsetup      ; 1
    dw vbitmapsetup     ; 2
    dw vbitmap3setup    ; 3
    dw vbitmap4setup    ; 4
    dw vbitmap5setup    ; 5
    dw vbitmap6setup    ; 6
    dw vbitmap7setup    ; 7
    dw vtext2setup      ; 8
    dw vmonosetup       ; 9
    dw vmultisetup      ; 10

;   Set video mode      (XOP)
;
;	caller R1 is mode #
vsetmode:
    pusha
    mov.b  r1, @vmode
    lsh    1, r1
	mov    @vmodesetups(r1), r1
    call    *r1

    popa
	ret

; leaves R1 pointing to the address and word table
vsetupregs:
    
vts0 cmp.b #>ff,*r1
    jeq  vts1
    mov   *r1+, r0
    call vwreg
	jmp	vts0

vts1:
    inct r1

    call vgetcolorbyte
    or >700, r0
    call vwreg

    clr.b    @vpob
    clr     @vpgrow
    clr     @vtextpage
	
	ret


vsetupaddrs:
	ldc	vtmap, r0
vsa1 mov *r1+,r3
	mov	*r0+,r4
	mov	r3,*r4
	cmp vtmap + vtmapsize, r0
	jne	vsa1
	ret

; Get the given table address
vgettab
    add r1,r1
    mov @vtmap(r1), r0
    ret
    
vcleartables:
    ; don't clear patterns... font routines and/or vtermclear does that
    jmp vct0
    
vcleartables4x:
vct0:
    call vcolorsetup

    call vsetpalette
    
vclrtabscr:    
    mov @vscreensz, r2
    jeq vclrtabspr
    mov @vscreen, r0
    ldc >2000, r1
    call vclr
    
vclrtabspr:
	mov	@vsprites, r0
	jeq vclrtabout
	
	; delete the sprites; $d0 for mode 1 and $d8 for mode 2
    mov @vsprcol, r1
    jne vclrtabspr2
	ldc #>d0,r1
    jmp vclrtabsprs
vclrtabspr2:

    ; clear sprite color table too
    mov r1, r0
    clr r1
    ldc >200, r2
    call vsetbank
    call vclr
    mov @vsprites, r0
	ldc #>d8, r1
vclrtabsprs:
    call vsetbank
    call vwaddr    
    mov.b r1,&VDPWA
	inc r0
	clr	r1
	ldc 127, r2
	call vclr

	mov	@vsprmot,r0
    ;call vsetbank       ; it'll be in the same page
	clr	r1
	ldc	128, r2
	call vclr

vclrtabout:
	ret

vsetbank:
    ret
    
;   Setup color table
;
vcolorsetup
    mov @vcolorsz, r2
    jeq vcolorsetup0
    clr r1
    cmp.b M_text2, @vidmode
    jeq vcolorsetup1
    call vgetcolorbyte
    mov.b r0,r1
vcolorsetup1:    
    mov @vcolors,r0
    add  @vtextpage, r0
    call vsetbank
    call vclr
vcolorsetup0:   
    ret

;   (Called internally)
; 
;   Set the terminal handlers for the mode and make the window full-screen
;
vtermsetup
    mov     @vtermptr,r1
    ldc     vtermptrs, r0
    mov     *r1+,r3
vtts0:
    mov     *r0+,r2
    mov     *r3+,*r2
    cmp     vtermptrsend, r0
    jne     vtts0
    
    ; force window to full screen
    mov     *r1, @vwxs
    
    mov.b   *r1+, r2
    rsh     8, r2
    mov     r2, @vwidth
    mov.b   *r1+, @vheight
    
    clr     @vwx
    clr     @vx

    ; get cursor char
    mov     *r1+,r2
    mov.b   *r2,@vcurschar
    
    ret

;   Reset video state
;
;   -- reset terminal bounds
;   -- clear memory
;   -- load font (if needed)
vreset
    pushn #2, r1
    
    call vtermsetup
    call vcleartables
    
    mov.b @vidmode,r1
    cmp.b  M_text,r1
    jeq  vresettextish
    cmp.b  M_text2,r1
    jeq  vresettextish
    cmp.b  M_multi,r1
    jeq  vresetmulti
    cmp.b  M_graph,r1
    jne  vreset0

vresettextish:    
    jmp     vresetout

vresetmulti:
    ; setup standard SIT for multi mode
    ; 0 1 2 3 ... 30 31  x  4
    ; 32 33 ... 62 63    x 4
    ; ... 190 191
    mov     @vscreen,r0      ; never banked
    call    vwaddr
    clr     r1
vbs4    ldc #4, r2
vbs3    mov.b r1,&VDPWD
    inc     r1
    tstn     #>1f, r1
    jne     vbs3
    add     ->20, R1
    dec     r2
    jne     vbs3
    add     #>20, r1
    cmp     #>c0, r1
    jnc      vbs4       ;??
    jmp     vresetout
    
vreset0:    
    cmp.b   M_bit,@vidmode
    jne     vreset1
    
    ; setup standard SIT for bitmap mode
    mov     @vscreen,r0      ; never banked
    call    vwaddr
    clr     r1
    ldc      768, r2
vbs2    mov.b    r1,&VDPWD
    inc     r1
    dec     r2
    jne     vbs2
    
    jmp     vresetbitcommon

vreset1:
    ; must be M_bit4
    
    call    vcleartables4x
    
vresetbitcommon:
        
vresetout:
    clr     r1
    call    vsetfont
    call    termclear
    
    POPN    #2, r1
    ret

vtermptrs dw vdrawchar,vclearline,vcursor,vcoordaddr
vtermptrsend equ $

vtextterms dw vtextchar,vtextclearline,vtextcursor,vtextaddr
vbitterms dw  vbitchar,vbitclearline,vbitcursor,vbitaddr
vbit4terms dw  vbit4xchar,vbit4xclearline,vbit4xcursor,vbit4xaddr
vbit5terms dw  vbit4xchar,vbit4xclearline,vbit4xcursor,vbit4xaddr
vbit7terms dw  vbit4xchar,vbit4xclearline,vbit4xcursor,vbit4xaddr
vmultiterms dw vnopchar,vnopclearline,vnopcursor,vtextaddr

; Setup for a standard mode
vstdmode
    clr     @vbit4stride
    ldc     >900, r0      ; 192 rows
    br      vwreg       

; Save/restore/query important state for the video mode (XOP)
;
;   R1 = query:  0 = size?  1 = save  2 = restore
;   R2 = addr for 1/2
;
;   R1 = result
;   Uses R1, R3, R4

vsaverestore_s mov *r1+, *r2+
    ret
vsaverestore_r mov *r2+, *r1+
    ret

vsaverestore 
    cmp     #0, r1
    jeq     vsvrssz

    push    r12
    ldc     vsaverestore_s, r4
    cmp     #1, r1
    movne   vsaverestore_r, r4

    ldc     _CPURAMSTART, R1
    ldc     (_CPURAMSIZE+1) / 2, R3
    loop    R3: call R4
    ldc     _VIDVARSTART, R1
    ldc     (_VIDVARSIZE+1) / 2, R3
    loop    R3: call R4
    
    cmp     vsaverestore_r, R4
    jne     vsvrsrst2
   
    call    vrestoremode
    
vsvrsrst2:    
    POP     r12
    ret
            
vsvrssz:
    ldc     _CPURAMSIZE+1, R1
    add     _VIDVARSIZE+1, R1
    ret
    

vfonts dw vf_8x8, vf_6x8, vf_5x6

vf_8x8 dw >0808, grom_font8x8
vf_6x8 dw >0608, grom_font8x8
vf_5x6 dw >0506, grom_font5x6

;   (Called from XOP or init)
;
;   Set a font from GROM, load into pattern table (text modes)
;   or set up terminal to fit (bitmap modes).
;   R1 = 0 for 8x8, 1 for 6x8, 2 for 5x6
;
vsetfont 
    add    r1,r1
    mov    @vfonts(r1), r1
    
    mov    *r1+, @vbsize
    mov    *r1, r0
    mov    r0,@vfont

    cmp.b  M_bit, @vidmode
    jeq    vsetfont1
    cmp.b  M_bit4, @vidmode
    jeq    vsetfont1
    
vsetfont0:
    call    gwaddr

    mov     @vpatts, r0
    add     @vtextpage, r0
    call    vsetbank
    call    vwaddr

    ldc     >800, R2
    loop    R2: mov.b   @GPLRD,&VDPWD

    jmp     vsetfont2

vsetfont1:    
    ; reset the maximum vwidth/vheight
    clr    r0
    mov    @vscrnx, r1
    mov.b  @vbsize, r2
    div    r2, r0
    mov    r0, @vwidth

    clr    r0
    mov    @vscrny, r1
    mov.b   @vbsize+1, r2
    div    r2, r0
    
    ; add extra row if we can see at least half the character (slack for 212-line modes)
    add    r1, r1
    cmp    r1, r2
    jnc    vsetfont1b   ; jl?
    inc    r0
vsetfont1b    
    mov.b   r0, @vheight
    
    call    treset

vsetfont2:    
    
    ret

    
;========================================================================
;	Turn off the cursor.
;
;	Call before moving it or changing the char under the cursor.
;

vcursoroff
	cmp.b   #00, @vcurs
	jeq	    vcursisoff
	push    r0
	mov	    @vcursor,r0
    call    *r0
    pop     r0
vcursisoff	ret

    
