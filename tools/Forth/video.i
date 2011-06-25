;	video.inc	 				-- video/text routines 
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
;	$Id: video.i,v 1.33 2009-02-13 02:02:11 ejs Exp $

;
;   Video routines typically use XOPWS or VIDWS.
;
;   Those using VIDWS may be called during interrupts, so
;   VDP interrupts must be disabled during video operations.
;   (This is good thinking anyway!)
;

h08	byte	8
hunder	byte	"_"
h07	data	7
	even

;	Initialize video.  Defaults to text mode.
;

vinit	  PUSH    SP, 0, 11
	
	mov    #_VIDVARSTART, @vidvarstart
	mov    #_VIDVARSIZE, @vidvarsize

	li	   0,15
	mov	   0,@vcrstimer			 ; set up standard blink
	;sb	   @vcurs,@vcurs		 ; it's off

	li	   1,>0107
	mov	   1,@vfgbg

    mov    #>808, @vbsize          ; set in case we lazily switch to bitmap mode

    clr     1
	blwp   @vsetmode
	bl     @vreset
	;bl    @vrestoremode
	bl     @vscreenon

	POP    SP, 0, 11
	rt

; Set vfgbg to color in R0 word and update mono modes
vsetcolor
	dect   SP
	mov    11, *SP
	mov    0, @vfgbg
	
	cb     #M_text,@vidmode
	jeq    vsetcolor1
	cb     #M_text2,@vidmode
	jne    vsetcolor0

vsetcolor1:
    bl     @vgetcolorbyte
    swpb   0
	ori    0,>700
	bl     @vwreg

	jmp    vsetcolor9

vsetcolor0:
	bl     @vcolorsetup
vsetcolor9:
	mov    *SP+, 11
	rt

;   Get vfgbg in single byte
vgetcolorbyte
    clr     0
    movb    @vfgbg,0
    sla     0,4
    socb    @vfgbg+1,0
    rt
    
;   Get vfg in single byte
vgetfgcolorbyte
    clr     0
    movb    @vfgbg,0
    sla     0,4
    rt
    
    

;   Set the r/w video page (XOP)
;
;   R1 = page #
vrwpage PUSH SP,11
    bl      @vcursoroff
    mov     r1 , r2
    cb     #M_bit4, @vidmode
    jeq     vrwpage0
    
    ; text/graphics mode allows page access on 0x2000 boundaries
    sla     r1, 13
    mov     r1, @vtextpage
    sla     r2, 7
    andi    r2, >400
    movb    r2, @vpob 
    jmp     vrwpageout
     
vrwpage0:
    sla     r1 , 8
    mov     r1 , @vpgrow       ; page row offset is 256 * page
    ; the page bank offset is this page row offset times bytes-per-line divided by 16k
    mpy     @vbit4stride , r2   ; e.g. 256 * 1 , 128 * 1 ...
    sla     r3 , 2
    movb    r3 , @vpob
vrwpageout:    
    POP SP,11
    rt
        
;   Set the visible video page (XOP)
;  
;   R1 = page #
vpage PUSH SP,11
    mov     r1 , r0
    movb    @vidmode, 11
    cb     #M_bit4, 11
    jeq     vpage0
    
    ; text/graphics mode allows page access on 0x2000 boundaries
    sla     0, 3
    sb      0, 0
    cb     #M_text2, 11
    jeq     vpage1
    ori     0,>200
    jmp     vpageout
    
vpage1:
    ; V9938 needs lower bits set
    ori     0,>203
    bl      @vwreg
    
    ; move color table too (A00 away from screen)
    mov     1, 0
    andi    0, 1
    sla     0, 7
    ori     0,>32f
    bl      @vwreg
    mov     1, 0
    srl     0, 1
    ori     0,>A00
    bl      @vwreg
    
    ; and move pattern table
    mov     1, 0
    sla     0, 2
    inct    0
    ori     0,>400
    jmp     vpageout
     
vpage0:    
    sla     r0 , 5
    ori     r0 , >821f
vpageout:    
    bl      @vwreg
    POP     SP,11
    rt

;=========================================================================
;	Internally called routines
;	
;	(IM=0)
;=========================================================================


;	Set GROM write addr in R0
;

gwaddr	movb	0,@GPLWA
	swpb	0
	movb	0,@GPLWA
	swpb	0
	rt



;	Set VDP write/read addr in R0.
;   These are legacy versions which assume that the 
;   address is pre-masked to the 0->3FFF range.

vwaddr	ori	0,>4000
vraddr
	swpb	0
	movb	0,@VDPWA
	swpb	0
	movb	0,@VDPWA
	andi    0,>3fff
	rt
	
;   Set a VDP register, remembering values stored to
;   VRs 0 through 14 for later restoration if needed.
vwreg   
    dect    SP
    mov     1, *SP
    mov     0, 1
    andi    1,>7FFF
    ori     0,>8000
    swpb    0
    ci      1,>0F00
    jhe     vwreg0
    srl     1,8
    movb    0,@savedvregs(1)
vwreg0:    
    movb    0,@VDPWA
    swpb    0
    movb    0,@VDPWA
    mov     *SP+,1
    rt

;   Set a VDP register from the following word
vwregnext 
    mov     *11+, 0
    jmp     vwreg
    
;   Read a stored VDP register
;   Input:
;       R1=index (0-15)
;   Output:
;       R1=value (word)
vrreg
    movb    @savedvregs(1),1
    srl     1,8
    rt
    
; Restore the video registers and turn on the screen
;
; Called from XOPWS, can modify any regs
vrestoremode
    dect SP
    mov 11,*SP
    li  12,VDPWA
    li  1, savedvregs
    li 0, >8000
vrm0 movb *1+, *12
    movb 0, *12
    ai 0, >100
    ci 0, >8F00    
    jne  vrm0
    bl @vscreenon
    mov *SP+,11
    rt

;	Video clear.  Caller should set the bank first.
;
;	R0=addr
;	R1=char
;	R2=#

vclr	dect SP
	mov	11,*SP
	dect SP
	mov	2,*SP

;    bl  @vsetbank  ; let caller do it
	bl	@vwaddr
vclr0	movb	1,@VDPWD
	dec	2
	jne	vclr0
	
	mov	*SP+,2
	mov	*SP+,11
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

vscreenoff
	szcb 	#>40,@vregr1
	jmp	vreg1set
vscreenon
	socb	#>40,@vregr1
vreg1set:
	movb	@vregr1,@VDPWA
	movb	#>81,@VDPWA
	rt

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
    dw vbitterms, >2018,hff

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
vbit4  db  0,>6,1,>A0,2,>0,5,>F4,>B,0,6,>E,-1
    dw >0000,0,>0000,>0000,>0,>6400,>7000,>7A00,>7A80,>7800,>8000
    dw 256,212
    dw >80, 1, >FFFE
    dw vbit4terms, >201B, hff

;       5=graphics 5 mode (512x212x4)
;           >0000 = patts
;           >7000 = sprite patts
;           >7800 = sprite colors
;           >7A00 = sprites
;           >7A80 = sprite motion
;           >8000+= free
;
vbit5  db  0,>8,1,>A0,2,>0,5,>F4,>B,0,6,>E,7,>FF,-1
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
vbit6  db  0,>A,1,>A0,2,>0,5,>AC,>B,>1,6,>1B,-1
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
vbit7  db  0,>E,1,>A0,2,>0,5,>AC,>B,>1,6,>1B,-1
    dw  >0000,0,>0000,>0000,>0,>D400,>D800,>D600,>D680,>D400,>E000
    dw 256,212
    dw >100, 0, >FFFF
    dw vbit7terms, >201B, hff

;       8=text 2 mode
;           >0000 = screen (to >870 for 212-line mode)
;           >0A00 = colors (blinks)
;           >1000 = patts
;           >1800+= free
;
vtxt2  db  0,>4,1,>B0,2,>0,3,>2f,>A,0,4,>2,>D,>22,-1
    dw  >0,2160,>A00,2160/8,>1000,>800,0,0,0,0,>1800
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
vmonobit db 0,>2,1,>B0,2,>6,3,>80,4,>03,5,>36,6,>3,-1
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
    

hff db >ff
    even
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
 Vector vsetmode, vidws
    li     SP,vstack + vstacksize 
    movb   @3(13), 1
    movb   1, @vmode
	srl    1, 7
	mov    @vmodesetups(1), 1
	bl     *1

	rtwp

; leaves R1 pointing to the address and word table
vsetupregs:
    dect SP
    mov 11,*SP
    
vts0 cb #>ff,*1
    jeq  vts1
    mov   *1+, 0
    bl  @vwreg
	jgt	vts0

vts1:
    inct 1

    bl  @vgetcolorbyte
    swpb 0
    ori 0,>700
    bl @vwreg

    sb      @vpob, @vpob
    clr     @vpgrow
    clr     @vtextpage
	
	mov *SP+,11
	rt


vsetupaddrs:
	li	0,vtmap
vsa1 mov *1+,3
	mov	*0+,4
	mov	3,*4
	ci 0,vtmap + vtmapsize
	jlt	vsa1
	rt

; Get the given table address
vgettab
    a 1,1
    mov @vtmap(1), 0
    rt
    
vcleartables:
	dect SP	
	mov 11,*SP

    ; don't clear patterns... font routines and/or vtermclear does that
    ;mov @vpatts, 0
    ;bl @vsetbank
    ;clr 1
    ;mov @vpattsz, 2
    ;bl @vclr
    jmp vct0
    
vcleartables4x:
    dect SP 
    mov 11,*SP
vct0:
    bl @vcolorsetup

    bl @vsetpalette
    
vclrtabscr:    
    mov @vscreensz, 2
    jeq vclrtabspr
    mov @vscreen, 0
    li 1,>2000
    bl @vclr
    
vclrtabspr:
	mov	@vsprites,0
	jeq vclrtabout
	
	; delete the sprites; $d0 for mode 1 and $d8 for mode 2
    mov @vsprcol, 1
    jne vclrtabspr2
	movb #>d0,1
    jmp vclrtabsprs
vclrtabspr2:

    ; clear sprite color table too
    mov 1, 0
    clr 1
    li 2,>200
    bl @vsetbank
    bl @vclr
    mov @vsprites, 0
	movb #>d8,1
vclrtabsprs:
    bl @vsetbank
    bl  @vwaddr    
    movb 1,@VDPWA
	inc 0
	clr	1
	li	2,127
	bl	@vclr

	mov	@vsprmot,0
    ;bl @vsetbank       ; it'll be in the same page
	clr	1
	li	2,128
	bl	@vclr

vclrtabout:
	mov *SP+,11
	rt


;   Setup color table
;
vcolorsetup
    mov @vcolorsz, 2
    jeq vcolorsetup0
    dect SP
    mov 11, *SP
    clr 1
    cb #M_text2, @vidmode
    jeq vcolorsetup1
    bl @vgetcolorbyte
    movb 0,1
vcolorsetup1:    
    mov @vcolors,0
    a   @vtextpage, 0
    bl  @vsetbank
    bl  @vclr
    mov *SP+, 11
vcolorsetup0:   
    rt

;   (Called internally)
; 
;   Set the terminal handlers for the mode and make the window full-screen
;
vtermsetup
    PUSH    SP,R11
    
    mov     @vtermptr,1
    li      0,vtermptrs
    mov     *1+,3
vtts0:
    mov     *0+,2
    mov     *3+,*2
    ci      0,vtermptrsend
    jne     vtts0
    
    ; force window to full screen
    mov     *1, @vwxs
    
    movb    *1+, 2
    srl     2,8
    mov     2,@vwidth
    movb    *1+, @vheight
    
    clr     @vwx
    clr     @vx

    ; get cursor char
    mov     *1+,2
    movb    *2,@vcurschar
    
    POP     SP,R11
    rt

;   Reset video state
;
;   -- reset terminal bounds
;   -- clear memory
;   -- load font (if needed)
vreset
    PUSH SP,1,2,11    
    
    bl @vtermsetup
    bl  @vcleartables
    
    movb @vidmode,1
    cb   #M_text,1
    jeq  vresettextish
    cb   #M_text2,1
    jeq  vresettextish
    cb   #M_multi,1
    jeq  vresetmulti
    cb   #M_graph,1
    jne  vreset0

vresettextish:    
    jmp     vresetout

vresetmulti:
    ; setup standard SIT for multi mode
    ; 0 1 2 3 ... 30 31  x  4
    ; 32 33 ... 62 63    x 4
    ; ... 190 191
    mov     @vscreen,0      ; never banked
    bl      @vwaddr
    clr     1
vbs4    li  2, 4 
vbs3    movb 1,@VDPWD
    ai      1,>0100
    czc     #>1f00, 1
    jne     vbs3
    ai      1,->2000
    dec     2
    jgt     vbs3
    ai      1,>2000
    ci      1,>C000
    jl      vbs4
    jmp     vresetout
    
vreset0:    
    cb      #M_bit,@vidmode
    jne     vreset1
    
    ; setup standard SIT for bitmap mode
    mov     @vscreen,0      ; never banked
    bl      @vwaddr
    clr     1
    li      2,768
vbs2    movb    1,@VDPWD
    ai      1,>100
    dec     2
    jgt     vbs2
    
    jmp     vresetbitcommon

vreset1:
    ; must be M_bit4
    
    bl      @vcleartables4x
    
vresetbitcommon:
        
vresetout:
    clr     1
    bl      @vsetfont
    bl      @termclear
    
    POP     SP,1,2,11
    rt

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
    ;clr     @vpob
    ;clr     @vpgrow
    ;clr     @vtextpage
    clr     @vbit4stride
    li      0, >900      ; 192 rows
    b       @vwreg       

; Save/restore/query important state for the video mode (XOP)
;
;   R1 = query:  0 = size?  1 = save  2 = restore
;   R2 = addr for 1/2
;
;   R1 = result
;   Uses R1, R3, R4

vsaverestore_s equ >ccb1    ; mov *1+, *2+
vsaverestore_r equ >cc72    ; mov *2+, *1+

vsaverestore 
    mov     1, 1
    jeq     vsvrssz

    PUSH    SP,11,12  
    li      4, vsaverestore_s
    ci      1, 1
    jeq     vsvrsrst
    li      4, vsaverestore_r
vsvrsrst:    
    li      1, _CPURAMSTART
    li      3, (_CPURAMSIZE+1) / 2
vsvrsrst0:  x 4
    dec     3
    jgt     vsvrsrst0
    li      1, _VIDVARSTART
    li      3, (_VIDVARSIZE+1) / 2
vsvrsrst1:  x 4
    dec     3
    jgt     vsvrsrst1
    
    ci      4, vsaverestore_r
    jne     vsvrsrst2

    ;movb    @vmode, 1
    ;srl     1, 7
    ;mov     @vmodesetups(1), 1
    ;bl      *1
    
    bl      @vrestoremode
    
vsvrsrst2:    
    POP     SP,11,12
    rt
            
vsvrssz:
    li      1, _CPURAMSIZE+1
    ai      1, _VIDVARSIZE+1
    rt
    

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
vsetfont PUSH SP,11
    a      r1,r1
    mov    @vfonts(r1), r1
    
    mov    *1+, @vbsize
    mov    *1, 0
    mov    0,@vfont

    cb     #M_bit, @vidmode
    jeq    vsetfont1
    cb     #M_bit4, @vidmode
    jeq    vsetfont1
    
vsetfont0:
    bl      @gwaddr

    mov     @vpatts, 0
    a       @vtextpage, 0
    bl      @vsetbank
    bl      @vwaddr

    li      2,>800
vgf1 movb   @GPLRD,@VDPWD
    dec     2
    jgt     vgf1

    jmp     vsetfont2

vsetfont1:    
    ; reset the maximum vwidth/vheight
    clr    0
    mov    @vscrnx, 1
    movb   @vbsize, 2
    srl    2, 8
    div    2, 0
    mov    0, @vwidth

    clr    0
    mov    @vscrny, 1
    movb   @vbsize+1, 2
    srl    2, 8
    div    2, 0
    
    ; add extra row if we can see at least half the character (slack for 212-line modes)
    a      1, 1
    c      1, 2
    jl     vsetfont1b
    inc    0
vsetfont1b    
    swpb   0
    movb   0, @vheight
    
    bl      @treset

vsetfont2:    
    POP     SP,11
    rt

    
;========================================================================
;	Turn off the cursor.
;
;	Call before moving it or changing the char under the cursor.
;
;	NOT a BLWP @ function because it's unnecessary.
;

vcursoroff
	cb	   @vcurs,#00
	jeq	   vcursisoff
	dect   SP
	mov	   0,*SP
	;movb   #>80,@vcurs		; force an "off" next time
	mov	   @vcursor,0
	blwp   *0
	mov	   *SP+,0
vcursisoff	rt

; =============================================================
;   This routine will draw a pixel on the screen (XOP)
;
;   caller R12 -> args:     op|color  y  x

 Vector pixel, vidws
    limi    0
    li      SP,vstack + vstacksize

    mov     @24(13), 12
    
    bl      @vpixel
    
    rtwp
    
;   R12 -> args:    op|color y x
vpixel:
    PUSH    SP,4,9,11
    
    cb      #M_bit4,@vidmode
    jeq     vbpcont4
    
    cb      #M_bit,@vidmode
    jne     vbpout

vbpcont:
    PUSH    SP, 12
    mov     @2(12), 2
    mov     @4(12), 1
    mov     *12, 12
    
    bl      @vbl_drawpixel

    POP     SP, 12
    jmp     vbpout

vbpcont4:
    bl      @vcmdsetup
    data    >24             ; -> DX
    
    mov     @4(12), 0
    bl      @vcoordsend
    mov     @2(12), 0
    bl      @vcoordsend
    
    bl      @vwregnext
    data    >112c        ; -> CLR
    movb    @1(12), *4     ; CLR

    movb    #00, *4         ; ARG 

    li      9, >5000        ; PSET
    szcb    #>f0, *12
    socb    *12, 9           ; OP
    bl      @vbit4xsetupMMMcommand

vbpout:    
    POP     SP,4,9,11
    rt
    
; =============================================================
;   Fill a rectangle (XOP)
;   
;   caller R12 -> op|color h w y x
 Vector fillrect, vidws
    limi    0
    li      SP,vstack + vstacksize

    mov     @24(13), 10
    
    cb      @vidmode, #M_bit4
    jeq     fr4x
    
    cb      @vidmode, #M_bit
    jne     frout

    mov     @6(10), 2       ; Y
    mov     @8(10), 1       ; X
    bl      @vbl_getaddr    ; R0= addr, R1=shift
    
    mov     @2(10), 8       ; H
    mov     @4(10), 2       ; W
    
    ; only fg honored, not op
    mov     *10, 12         ; op|c 
    sla     12, 12
    socb    @vfgbg+1, 12
    
    swpb    12
    movb    #>FF,12

    mov     0,4
    andi    1,7

    bl      @vbitfillrect
    rtwp
    
fr4x:
    bl      @vcmdsetup
    data    >24
    
    mov     10, 1
    inct    1               ; skip color
    dec     *1              ; convert end-coord relative offset to real width/height
    dec     @2(1)           ; ... again
    clr     2
    bl      @vbitsetupDXDYNXNYsigned

    movb    @1(10), *4      ; CLR

    movb    2, *4           ; ARG (dix=0, diy=0, mxc=0)

    li      9, >8000        ; LMMV
    szcb    #>f0, *10
    socb    *10, 9          ; OP
    bl      @vbit4xsetupMMMcommand
    
frout:    
    rtwp


; =============================================================
;   Draw a line (XOP)
;   
;   caller R12 -> op|color y2 x2 y x
 Vector line, vidws
    limi    0
    li      SP,vstack + vstacksize

    mov     @24(13), 12
    
    cb     @vidmode, #M_bit4
    jeq     ln4x
    
    cb     @vidmode, #M_bit
    jne     lnout
    
    b       @vbitline
    
ln4x:
    bl      @vcmdsetup
    data    >24
    
    mov     12, 1
    inct    1       ; skip color
    li      2, >8000    ; LINE mode
    
    ; convert X2,Y2 to X2-X1, Y2-Y1
    s       @4(1), *1
    s       @6(1), @2(1)
    bl      @vbitsetupDXDYNXNYsigned

    movb    @1(12), *4     ; CLR

    movb    2, *4           ; ARG (dix=0, diy=0, mxc=0)

    li      9, >7000        ; LINE
    szcb    #>f0, *12
    socb    *12, 9          ; OP
    bl      @vbit4xsetupMMMcommand
    
lnout:    
    rtwp

; =============================================================
;   Draw a circle (XOP)
;   http://www.cs.unc.edu/~mcmillan/comp136/Lecture7/circle.html
;   caller R12 -> op|color r y x
 Vector circle, vidws
    limi    0
    li      SP,vstack + vstacksize

    mov     @24(13), 12
    
    mov     12, 1
    
    mov     *12+, 8     ; color
    mov     *12+, 7     ; R / p / y
    mov     *12+, 5     ; cy
    mov     *12+, 4     ; cx
    
    li      9, 5
    sla     7, 2
    s       7, 9
    sra     9, 2        ; P = (5/4) R
    sra     7, 2
    
    clr     6           ; x

    bl      @circlePoints
    
ccloop:
    c       6, 7
    jhe     ccout

    inc     6
    mov     9, 9
    jlt     ccneg
    
    dec     7
    
    mov     6, 0
    s       7, 0
    a       0, 0
    a       0, 9
    
    jmp     ccnext

ccneg:
    a       6, 9
    a       6, 9

ccnext:
    inc     9
    
    bl      @circlePoints
    jmp     ccloop
    
ccout:
    rtwp    

;   R4: cx
;   R5: cy
;   R6: x
;   R7: y
;   R8: color
;
circlePoints 
    PUSH    SP,11,12,13,14,15
    
    STWP    12 
    ai      12, 13*2

    mov     8, 13       ; R13=color, R14=Y, R15=X    

; +x = 0
; +y = 0
; -x = 1
; -y = 1

    mov     4, 15
    a       6, 15
    mov     5, 14
    a       7, 14
    bl      @vpixel     ; cx+x, cy+y        ; 0 0

    mov     4, 15
    s       6, 15
    mov     5, 14
    s       7, 14
    bl      @vpixel     ; cx-x, cy-y        ; 1 1

    c       6, 7
    jeq     cptseq     ; x == y    
    
    mov     4, 15
    a       7, 15
    mov     5, 14
    a       6, 14
    bl      @vpixel     ; cx+y, cy+x        ; 0 0
    
    mov     4, 15
    s       7, 15
    mov     5, 14
    s       6, 14
    bl      @vpixel     ; cx-y, cy-x        ; 1 1
    
    mov     6, 6
    jeq     cptsout     ; x == 0
    
    mov     4, 15
    a       7, 15
    mov     5, 14
    s       6, 14
    bl      @vpixel     ; cx+y, cy-x        ; 0 1
    
    mov     4, 15
    s       7, 15
    mov     5, 14
    a       6, 14
    bl      @vpixel     ; cx-y, cy+x        ; 1 0

cptseq:
    
    mov     4, 15
    a       6, 15
    mov     5, 14
    s       7, 14
    bl      @vpixel     ; cx+x, cy-y        ; 0 1

    mov     4, 15
    s       6, 15
    mov     5, 14
    a       7, 14
    bl      @vpixel     ; cx-x, cy+y        ; 1 0
    
cptsout:    
    POP     SP,11,12,13,14,15
    rt

;   Handle sprite motion.
;
;   Like in the 99/4A ROM, we have a table with four bytes per sprite,
;   in this configuration:
;
;   [ Y motion ] [ X motion ] [ Y fraction ] [ X fraction ]
;
;   The motion is a signed byte in units of 1/16 pixel per 1/60 second.
;   The slowest, then, is 15/4 pixels per second, 
;   and the fastest is 476 pixels per second.
;
;   The fraction bytes represent 1/256 pixel increments for the
;   Y and X coordinates of a sprite.
;
;   Watch out for sprites moving to the "delete coordinate"
;   (>D0 or >D8).
;
 Vector  vspritemotion, vidws
    li     SP,vstack + vstacksize 

    movb    @nsprmot, 5
    jne     $1+
    
    rtwp

$1:    
    srl     5, 8
    
    mov     @vsprmot, 0
    mov     0, 6
    s       @vsprites, 6    ; R6 = delta to sprites
    
    bl      @vsetbank
    
    mov     0, 4
    
    li      12, VDPRD
    li      9, VDPWA
    li      8, >D000        ; boundary Y coord
    
    mov     @vbit4stride, 0
    jeq     vsm_loop
    
    li      8, >D800        ; ... for V9938 modes
    
vsm_loop:
    movb    @vidws+9, *9
    movb    4, *9       ; get sprite motion entry
    
    movb    *12, 1      ; Y-motion
    swpb    1
    movb    *12, 1      ; X-motion
    
    mov     1, 1        ; any motion?
    jeq     vsm_next
    
    movb    *12, 3      ; Y-frac
    movb    *12, 2      ; X-frac

    mov     4, 0
    s       6, 0
    movb    @vidws+1, *9
    movb    0, *9       ; get sprite coords
    
    swpb    3
    movb    *12, 3      ; Y|y
    swpb    2
    movb    *12, 2      ; X|x
    
    ; R2 = X|x coord, R3 = Y|y coord, R1 = x|y motion
    
    movb    1, 7
    sra     7, 8        ; sign extend
    sla     7, 4        ; 1/16 pixel per 1/60 second        
    a       7, 2        ; add motion for X

    swpb    1
    sra     1, 8        ; sign extend
    sla     1, 4        ; 1/16 pixel per 1/60 second        
    a       1, 3        ; add motion for Y
    
    ; Check for Y coordinate in bad range.
    ;
    ; The original idea was:
    ;
    ;   If we moved down to $D0 (or more), then bump by $10.
    ;   If we moved up to $DF (or less), then bump by -$10.
    ;   
    ; We need to apply consistent movement, though, so we
    ; can't just notice when a sprite happens to be in the
    ; range -- we may have a kind of movement that sometimes
    ; hits this range and sometimes doesn't.  Moving a whole 16
    ; pixels in some cases and not others would be too noticeable.
    ;
    ; Instead, just check for the same coordinate and 
    ; bump one pixel in the intended direction.
    ;
    ; TODO: further tweaks with the fraction
    
    cb      8, 3     
    jne     $2+         ; at delete coordinate? 
    
    mov     1, 1        ; which direction?
    jlt     $1+
    
    ab      #>1, 3      ; moved down, go further 
    jmp     $2+
    
$1:
    sb      #>1, 3      ; moved up, go further
    
$2:
    ori     0, >4000
    movb    @vidws+1, *9
    movb    0, *9       ; update sprite coord 
    
    movb    3, @-2(9)
    movb    2, @-2(9)
    
    a       6, 0
    inct    0
    movb    @vidws+1, *9
    movb    0, *9        ; update motion fractions
    
    swpb    3
    movb    3, @-2(9)
    swpb    2
    movb    2, @-2(9)
    
vsm_next:
    ai      4, 4
    dec     5
    jgt     vsm_loop

    rtwp
    
