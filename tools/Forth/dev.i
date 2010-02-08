;
;	dev.inc						-- peripheral device access
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
;	$Id: dev.i,v 1.14 2009-02-13 02:02:11 ejs Exp $

;==========================================================================
;	Device init.
;
;	Execute each DSR's initialization routine.
;==========================================================================

haa	byte	>aa
	byte	0

dinit	li	0,>3ffe        ; steal an extra byte to avoid having DSRs wraparound
	mov	0,@vdplimit			; set up 16k VDP RAM

	lwpi	cpurambase + >e0  				; be standard
	li	13,GPLRD
	li	14,>100
	li	15,VDPWA

	li	12,>0f00			; start below first DSR
di00	sbz	0
	ab	#1,12				; point to next DSR
	ci	12,>2000
	jhe	diout				; done

	sbo	0					; turn on ROM
	cb	@>4000,@haa			; legal rom?
	jne	di00				; nope

	mov	@>4004,1			; get init ptr
	jeq	di00				; none
di01	mov	@2(1),11			; get addr for init
	mov	12,@cpurambase + >d0
	bl	*11				; call powerup routine
	mov	*1,1				; get next powerup
	jne	di01				; if it exists
	jmp	di00

diout	lwpi	mainws				; restore WP
	rt


;--------------------------------------------------------------------------
;	Disk init.
;
;	We want to limit to two buffers, since bitmap mode would otherwise
;	overwrite it.
;
;	With this setup we will ONLY use the emulated disk DSR for stuff.
;	So, we'll simply write the new info for the real DSR, if installed,
;	and probe the emulated disk DSR.
;--------------------------------------------------------------------------

noemuerr db	"No emulated disk DSR found!",>ff
	db	"Please install the emulated disk DSR",>ff
	db	"by adding 'EmuDisk' to the DSRCombo",>ff
	db	"variable in FORTH.CNF.",>ff,>ff
	db	"(See DISKS.TXT for info.)",>ff,>ff
	db	"Press Ctrl+Break to halt."
	db	0

forthdskdef db	"FORTHDSK  "
	even

diskinit dect	SP
	mov	11,*SP

    lwpi    cpurambase + >E0           ; be standard and avoid having the ROM clobber our workspace ;)
    
	li	12,>1000			; DSR base for emulated DSR
	sbo	0				; turn on
	cb	@haa,@>4000			; installed?
	jeq	dskiokay

	;li	2,noemuerr			; print error message
	;b	@dieerr
	jmp dskiignore

dskiokay:
	sbz	0

    lwpi    mainws          
    
	li	0,forthdskdef
	li	1,forthdsk
	li	2,10
dskifn	movb	*0+,*1+
	dec	2
	jgt	dskifn

dskiignore:
	mov	*SP+,11
	rt

vsbw	dect	SP
	mov	11,*SP
	bl	@vwaddr
	movb	1,@VDPWD
	mov	*SP+,11
	rt


vsbr	dect	SP
	mov	11,*SP
	bl	@vraddr
	movb	@VDPRD,1
	mov	*SP+,11
	rt


equals	db	>20
period	db	"."

;	DSRLNK
;
;	Caller's R1 contains 8 or 10 for device/subroutine calls
;	Returns result in caller's R0 (low)
;
;   This is adjusted to work with V9938 by forcing the vbank
;   to >3 (>C000).  This, combined with a slightly dishonest
;   setting in cpurambase + >70 of >3FFE, allows the DSRs to use the
;   approximately >800 bytes free in the most memory-hungry
;   mode. 

DSRVBASE equ >C000

dsrlnk	data	dskws,dsrlnk+4
	limi   0
	mov    @mainws + 20, SP    ; use same stack as main

    li      0, DSRVBASE
    movb    @vpob, 2
    sb      2, @vpob
    bl      @vsetbank          ; set VDP bank for DSR operations
    
    li      8,cpurambase         ; cpuram base
    
	mov	   @2(13),5			; get offset
	szcb   @equals,15			; no error
	mov	   @>56(8),0			; get ptr to name
	mov	   0,9				
	ai     9,-8				; point to error code

	bl     @vsbr				; get len
	movb   1,3				; save
	srl    3,8				
	seto   4				; # chars

	li     2,cpurambase + >4a				; buffer
dsr00	
    inc     0    				; move device name
	inc    4
	c      4,3
	jeq    dsr01
	bl     @vsbr
	movb   1,*2+
	cb	   1,@period
	jne	   dsr00
dsr01	
    mov	   4,4				; any chars read?
	jeq	   dsr09
	ci	   4,7
	jgt	   dsr09				; too many?
	clr	   @>d0(8)
	mov    4,@>54(8)			; # chars in device name
	inc	   4
	a      4,@>56(8)			; point to '.' in name
	lwpi   cpurambase + >e0				; GPLWS
	clr    1				; init card counter
	li     12,>f00
dsr03	
    sbz     0
	ai     12,>100				; start scan at >1000
	clr    @>d0(8)
	ci     12,>2000			; last base?
	jeq    dsr08
	mov    12,@>d0(8)			; store CRU
	sbo    0				; turn on rom
	li     2,>4000
	cb     *2,@haa				; legal rom?
	jne    dsr03
	a      @dskws+10,2			; add offset
	jmp    dsr05
dsr04	
    mov     @>d2(8),2
	sbo    0
dsr05	
    mov	    *2,2				; any devices?
	jeq	   dsr03		    		; nope... next rom pleez
	mov    2,@>d2(8)			; save next link
	inct   2
	mov    *2+,9				; get routine addr
	movb   @>55(8),5			; get len of caller
	jeq    dsr07				; ??? no length?
	cb     5,*2+				; match name
	jne    dsr04
	srl    5,8
	li     6,>4a + cpurambase
dsr06	
    cb      *6+,*2+
	jne    dsr04
	dec    5
	jne    dsr06
dsr07	
    inc     1	    			; increment card #
	bl     *9				; run it
	jmp    dsr04				; if no error, skip this word
	sbz    0				; turn off rom
	lwpi   dskws				
	mov    9,0				; get error code (cpurambase + >F2)
	bl     @vsbr
	srl    1,13				; any error?
	jmp	   dsr10
dsr08	
    lwpi   dskws
dsr09
	clr	   1
dsr10	
    ;swpb   1
	mov	    1,*13
	socb	@equals,15
	
    clr     0
    bl      @vsetbank       ; back to bank 0 for std modes
	
	movb 2, @vpob
	rtwp

;   Write multiple bytes, using 0->3FFF address and pre-set bank
vmbw    
    dect    SP
    mov     11,*SP
    bl      @vwaddr
vmbw0   
    movb    *1+,@VDPWD
    dec     2
    jgt     vmbw0
    mov     *SP+,11
    rt

;   Read multiple bytes, using 0->3FFF address and pre-set bank
vmbr    
    dect    SP
    mov     11,*SP
    bl      @vraddr
vmbr0   
    movb    @VDPRD,*1+
    dec     2
    jgt     vmbr0
    mov     *SP+,11
    rt

;--------------------------------------------------------------------------
;   This routine will READ or WRITE one block (1k) to CPU RAM.
;
;   VDPTOP-256 is used as a sector buffer.
;   VDPTOP-256-16 is used as a PAB.
;
;   *R12=r/w flag (0=write, !=read)
;   @2(R12)=block #
;   @4(R12)=addr
;
;   BLWP @
;
;   Returns R0=0 for no error
;--------------------------------------------------------------------------

rblockpab db    >01,>14
wblockpab db    >01,>15

rwblock  data    dskws,rwblock + 4
    limi    0
    li      SP,dskstack + dskstacksize

    dect    SP
    movb    @vpob, *SP
    
    sb      *SP, @vpob
    li      0, DSRVBASE
    bl      @vsetbank          ; set VDP bank for DSR operations

    li      8, cpurambase            ; CPU RAM base
    
    mov     @24(13), 12
    
    mov     @>70(8), 0
    ai      0,-1024
    mov     @vfree, 1
    andi    1, >3FFF
    c       0, 1
    jhe     $0+
    
    movb    #4, *13             ; not enough memory!
    jmp     $1+
     
$0:
    mov     0, 7
    
    ai      0,->10
    li      1,forthdsk
    li      2,10
    bl      @vmbw               ; set filename
    mov     0,@>4e(8)

    li      3,4                 ; # secs to read/write
    mov     @4(12),1            ; CPU addr
    
    mov     @2(12),5            ; block #
    sla     5,2                 ; sector #
    
    clr     4
    div     #360, 4             ; sector -> disk
    inc     4                   ; 0-based to 1-based
    swpb    4
    movb    4, @>4C(8)          ; disk #
    
    li      6,wblockpab
    mov     *12,4
    jeq     rblks
    li      6,rblockpab
    
rblks 
    movb    #4, @>4D(8)         ; four sectors
    clr     @>50(8)              ; parms @>00 + rambase

    mov     7,*8             ; VDP buff addr 
    mov     5,@2(8)          ; sector #

    li      2,1024
    
    mov     4,4
    jne     rblks0

    mov     7,0
    bl      @vmbw               ; move block to VDP for write
    
rblks0:
    bl      @dodsr              ; do op
    
    clr     *12
    movb    @>50(8),@1(12)
    jne     rwblkerr

    mov     4,4
    jeq     wblks0
    
    mov     7,0
    bl      @vmbr               ; copy block from VDP for read

wblks0:
    jmp     $1+

rwblkerr movb @>50(8),*13
$1:
    clr     0
    bl      @vsetbank       ; back to bank 0 for std modes
    movb    *SP+, @vpob
    
    rtwp

;==========================================================================
;
;   Do a "generic" block read or write.
;
;   Called with VDP bank DSRVBASE
;
;   R6 -> device name
;   R8 -> CPU RAM base
;   Does not modify any registers, but might change vaddr/gaddr/...
;
dodsr   mov 6,@>e0+12(8) 
    lwpi    cpurambase + >e0               ; be standard
    li      13,GPLRD
    li      14,>100
    li      15,VDPWA

    li      8,cpurambase             ; CPU RAM base
    li      12,>1000            ; our CRU base
    sbo     0               ; turn it on

    li      1,>400A             ; subprograms
    mov     *1,1
    jeq     dodsrerr
dodsrfnd 
    mov     @4(1),2             ; complete name
    c       2,*6                ; same name?
    jeq     dodsrrun
    mov     *1,1                ; get next
    jne     dodsrfnd

dodsrerr 
    movb    #>ff,@>50(8)         ; fake error
    jmp     dodsrout

dodsrrun 
    mov     @2(1),11                ; get addr
    mov     12,@>d0(8)           ; save CRU addr
    bl      *11             ; call routine
    nop                 ; error 

dodsrout 
    li      12,>1000 
    sbz     0               ; turn off ROM
    
    lwpi    dskws
    rt
	