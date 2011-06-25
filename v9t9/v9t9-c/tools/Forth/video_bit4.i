;	video_bit.i	 				-- MSX bitmap routines 
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
;	$Id: video_bit4.i,v 1.10 2009-02-13 02:02:11 ejs Exp $



; Standard V9938 palette
; Red|Blue  0|Green
vstdpalette
    db >00, >00 ;0
    db >00, >00 ;1
    db >11, >06 ;2
    db >33, >07 ;3
    db >17, >01 ;4
    db >27, >03 ;5
    db >51, >01 ;6
    db >27, >06 ;7
    db >71, >01 ;8
    db >73, >03 ;9
    db >61, >06 ;A
    db >64, >06 ;B
    db >11, >04 ;C
    db >65, >02 ;D
    db >55, >05 ;E
    db >77, >07 ;F
    

venhregs    
    dw  >808        ; 64 kvideo RAM
    dw  >e00        ; set bank 0, page 0, etc
    dw  >980        ; 212-line mode, color, etc
    dw  >c00        ; turn off alt colors
    dw  >d00        ; turn off blink/pageswap
    dw  >f00        ; point to sr0
    dw  0
             
; Set enhanced registers if on a V9938
;
venhmode
    push SP,R0,R1,R11
    
    li      1, venhregs
venhmode0:  mov *1+, 0
    jeq     venhmode1
    bl      @vwreg
    jmp     venhmode0
venhmode1:    
    pop     SP,R0,R1,R11
    rt

;   Reset status register
;
vstatus0
    li      0, >f00
    b       @vwreg
    
vsetpalette
    push    SP,R0,R11
    ; set std palette
    bl      @vwregnext
    data    >1000
    li      0, vstdpalette
vsetpal1 movb *0+, @VDPCL
    ci      0, vstdpalette + 16*2
    jne     vsetpal1
    pop     SP,R0,R11
    rt

;   Setup for bitmap modes 4,5,6,7
;
;
vbitmap4setup
    li  1, vbit4
vbit4sentr:
    PUSH    SP,11
    movb    #M_bit4,@vidmode

    bl  @vsetupregs
    bl  @vsetupaddrs
    bl  @venhmode
    
    mov *1+,@vbit4stride
    mov *1+,@vbit4shift
    mov *1+,@vbit4mask

    mov 1,@vtermptr
    bl  @vsetpalette
    
    POP SP,11
    rt

vbitmap5setup
    li  1, vbit5
    jmp vbit4sentr

vbitmap6setup
    li  1, vbit6
    jmp vbit4sentr

vbitmap7setup
    li  1, vbit7
    jmp vbit4sentr


;   (Called internally.  Preserve R2)
;
;   Figure screen addr for bitmap modes 4, 5, 6, 7.
;
;   hi(R0) = X in window
;   lo(R0) = Y in window
;
;   Returns R0=addr, R1=shift

vbit4xaddr 
    PUSH    SP,2

    a       @vwx, 0
    
    ; scale Y by vbsize
    mov     0,1
    sb      1,1
    movb    @vbsize+1,2
    srl     2,8
    mpy     1,2             ; 2=xxx, 3=row#
    
    mov     3,1
    mpy     @vbit4stride, 1 ; 2=addr
    
    ; scale X by vbsize
    srl     0,8
    movb    @vbsize,1
    srl     1,8    
    mpy     1,0             ; 0=xxx, 1=X scaled 
    
    mov     1,3
    mov     @vbit4shift,0
    jeq     vb4xnoshift
    srl     1,0             ; R1=offset
vb4xnoshift:
    a       1,2             ; R2=full addr
    mov     2,0             ; R0=full addr
    
    mov     3,1             ; preshifted X
    szc     @vbit4mask,1    ; R1=portion
    
    POP     SP,2
    rt



;	Send a coordinate to the indirect data port
;
;	R4 = VDPWI
;	R0 = data
vcoordsend:
	swpb	0
	movb	0, *4
	swpb	0
	movb	0, *4
	rt

;   Setup a MSX command
;
;   *R11 -> command
;   Leaves R4 = VDPWI
vcmdsetup
    mov     *11+, 0  
    PUSH    SP, 11
    
    ori     0,>1100
    bl      @vwreg
    
    li      4, VDPWI
    POP     SP,11
    rt

;   Setup the DX and DY operands of a MSX command targeting the current cursor position.
;
;   R0 = cursor position
;   Leaves	
;         R4 = VDPWI
;
;   Uses R0, R1, R2, R3
vbit4xsetupcursorDXDY
    PUSH    SP, 11
    a       @vwx, 0
    
    ; scale Y by vbsize
    mov     0,1
    sb      1,1
    movb    @vbsize+1,2
    srl     2,8
    mpy     1,2             ; 2=xxx, 3=row#
    
    ; account for the "big page"
    a       @vpgrow, 3
    
    srl     0,8
    movb    @vbsize,1
    srl     1,8    
    mpy     1,0             ; 0=xxx, 1=X scaled 

    bl      @vcmdsetup
    data    >24
    
    mov     1, 0
    bl      @vcoordsend     ; send DX
    mov     3, 0
    bl      @vcoordsend     ; send DY
    
    POP     SP,11
    rt
    
;   Setup the NX and NY operands of a MSX command targeting one line in
;   the current window.
;
;   R4 = VDPWI
;
;   Uses R0, R1, R2, R3
vbit4xsetupwindowlineNXNY
    push    SP, 11
    
    clr     1
    movb    @vbsize,1       ; X size (hi)
    clr     2
    movb    @vwxs,2         ; X (hi)
    mpy     1,2             ; 2=col #  (3 = 0)
    mov     2, 0
    dec     0
    bl      @vcoordsend     ; NX
    
    ; height is vbsize
    movb    @vbsize+1,0
    srl     0,8
    dec     0      
    bl      @vcoordsend     ; NY
    
    POP     SP,11
    rt

;   Setup the NX and NY operands of a MSX command targeting one charcter in
;   the current window.
;
;   R4 = VDPWI
;
;   Uses R0, R1, R2, R3
;   Leaves R2 = X size, R3 = Y size
vbit4xsetupwindowcharNXNY
    push    SP, 11
    
    movb    @vbsize,2      
    srl     2,8
    mov     2,0
    dec     0
    bl      @vcoordsend     ; NX
    
    movb    @vbsize+1,3
    srl     3,8
    mov     3,0
    dec     0
    bl      @vcoordsend     ; NY
    
    POP     SP,11
    rt
    
;   Setup an MSX memory move command that expects its work to 
;   be done through the CLR port.
;
;   -- point to status reg 2
;   -- wait for ready
;   -- write command
;   -- point to CLR register
;   
;   In:
;       R9 = command byte
;       R4 = VDPWI
;   Out:
;       R0 = xxx
vbit4xsetupMMMcommand
    push    SP,11
    
    bl      @vwregnext
    data    >0f02        ; set status reg for testing command finished & transfer ready
    
    ; wait for ready
vb4xclr:
    movb    @VDPST,0
    andi    0, >0100
    jne     vb4xclr

    ; write the command
    movb    9, *4 
    
    ; back to the CLR reg, no autoincrement
    bl      @vwregnext
    data    >11AC
    
    pop     SP,11
    rt
    
    
;   Called from XOP [preserve 0, 3]
;
;   Clear some bitmapped line in modes 4, 5, 6, 7.
;   
;   Uses the HMMV command to quickly blit out a rectangle,
;   with the assumption that the font size will force the window
;   to be properly aligned.
;
;   R0=coord in window (X=0, Y=...)
;   R2=length in chars

vbit4xclearline
    PUSH    SP,0,3,11

    bl      @vbit4xsetupcursorDXDY
    bl      @vbit4xsetupwindowlineNXNY

    movb    @vfgbg+1, *4    ; CLR

    movb    #>00, *4        ; ARG (dix=0, diy=0, mxc=0)

    li      9, >8000        ; HMMV  (terminal is usually aligned)
    bl      @vbit4xsetupMMMcommand
    
    ; don't wait :)
    ;li      2, >0100
    ;li      3, VDPST
;vb4xcl:
	; see if done
	;movb	*3,0
	;coc		2, 0
	;jeq		vb4xcl

vb4xclout:			
    POP		SP,0,3,11
	rt
	



;   Set the VDP RAM bank.
;
;   This directly sets VR14 without saving it off (unlike vwreg).
;   It does honor the page offset, though.
vsetbank
    dect    SP
    mov     0,*SP
    srl     0, 6
    ab      @vpob, 0
    movb    0, @VDPWA
    mov     *SP+,0
    movb    #>8e,@VDPWA
    andi    0,>3fff
    rt

;   Send a pixel from R7, shifting it out
;   R4 = VDPWI
;   R12 = BG|FG
    
vbit4xsendcharpixel
    sla     7,1
    jnc     vbit4xscpon
    movb    @vidws+25, *4       ; CLR
    rt
vbit4xscpon:    
    movb    12, *4              ; CLR
    rt
    
;   Draw a char in the window (modes 4, 5, 6, 7)
;
;   VCH=char
;   VX+VWX=char coord X
;   VY+VWY=char coord Y
;   VFGBG=color

 Vector vbit4xchar, vidws
    limi    0
    li      SP,vstack + vstacksize

    mov     @vx, 0
    bl      @vbit4xsetupcursorDXDY
    ; R4 = VDPWI
    
    bl      @vbit4xsetupwindowcharNXNY
    ; R2 = X size, R3 = Y size
    
    bl      @vfetchfontchar

    mov     @vfgbg,12
    swpb    12
    movb    *1+, 7               ; fetch first row
    bl      @vbit4xsendcharpixel  ; send first CLR

    movb    #0, *4              ; ARG (dix=0, diy=0, mxc=0)
    
    li      9, >B800            ; LMMC + TINP
    bl      @vbit4xsetupMMMcommand

    mov     2, 8                ; R8 = ctr for column
    dec     8                   ; from first pixel above
    
vbit4xchar_row
    ; R7 holds char, R8 is column ctr
    
vb4xchp:
    ; see if ready
    movb    @VDPST,0
    jlt     vbit4xchar_pixel
    jmp     vb4xchp

vbit4xchar_pixel:    
    bl      @vbit4xsendcharpixel
    
    dec     8
    jgt     vb4xchp
    
    mov     2, 8
    movb    *1+, 7
    dec     3
    jgt     vbit4xchar_row
        
    bl      @vstatus0
    rtwp



; Blink a cursor in graphics 4-7 modes
;
; Use LMMV to XOR a rectangle in the cursor shape
;
 Vector vbit4xcursor, vidws

    limi   0
    li     SP,vstack + vstacksize

    mov     @vx, 0
    bl      @vbit4xsetupcursorDXDY
    ; R4 = VDPWI
    
    li      0, 1            ; two cols
    bl      @vcoordsend         ; NX
    
    movb    @vbsize+1, 0
    srl     0,8
    dec     0
    bl      @vcoordsend         ; NY

    movb    #>ff, *4            ; CLR

    movb    #0, *4              ; ARG (dix=0, diy=0, mxc=0)
    
    li      9, >8300            ; LMMV + EOR
    bl      @vbit4xsetupMMMcommand

    ab  #>80,@vcurs

    bl      @vstatus0
    ; no need to wait!
    
    rtwp



; Set up the DX, DY, NX, NY arguments, 
; handling negative NX and NY to set DIX and DIY bits,
; and return updated argument
;
; In:
;   VR17 should point to >24
;   R1 = pointer to NY, NX, DY, DX values
;   R2 = template ARG (low byte), optionally OR'ed with $8000 to apply LINE semantics
;       to DX, DY and MAJ bit
;   R4 = VDPWI
;
; Out:
;   may modify values *R1
;   R2 is updated (high byte)
;   R0, R3 = xxx

vbitsetupDXDYNXNYsigned
    PUSH    SP,11
    
    ; R2 is original ARG byte, but has $8000 mask for LINE
    
    ; adjust dx and dix
    mov @2(1) , r0   ; nx
    jgt vbsdns0
    
    ori R2 , 4     ; DIX
    abs r0
vbsdns0:
    
    ; adjust dy and diy
    
    mov *1 , r3     ; ny
    jgt vbsdns1
    
    ori R2 , 8     ; DIY
    abs r3
vbsdns1:
    
    ; -------------
    
    mov R2 , R2       ; is this a LINE command?
    jgt vbsdns2
    jeq vbsdns2
    
    c r0 , r3           ; compare X and Y lengths
    jh vbsdns2
    ori R2 , 1          ; Y is major
    
    mov r3 , 11         ; swap DX / DY
    mov r0 , r3
    mov 11 , r0
    
    ; -------------

vbsdns2:    
    mov r0 , @2(1)
    mov r3 , *1

    jmp vbsdn0

; Set up the NX, NY, DX, DY arguments 
;
; In:
;   VR17 should point to >24
;   R1 = pointer to DX, DY, NX, NY values
;   R4 = VDPWI
;   R2 = arg word (low byte)
;
; Out:
;   R0, R3 = xxx
;   R2 = arg byte (upper byte)

vbitsetupDXDYNXNY:
    PUSH    SP, 11
    
vbsdn0:
    li      3, vcoordsend
    
    mov     @6(1) , 0
    bl      *3        ; DX

    mov     @4(1) , 0
    a       @vpgrow , 0
    bl      *3         ; DY

    mov     @2(1) , 0
    bl      *3         ; NX

    mov     *1 , 0
    bl      *3        ; NY

    swpb    2
    
    POP     SP,11
    rt
