;	kbd.inc						-- FORTH ROM keyboard scanner
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
;	$Id: kbd.i,v 1.13 2009-02-25 02:18:24 ejs Exp $

;--------------------------------------------------------------------------

kinit
    ;   reset clears all memory

    mov    #>1e03,@kbdlimit  ; 1/2 s before repeat, 1/20 s delay between repeat
	
	rt

;   Scan the keyboard.
;
;   We store the scancode, if any, in kbdscan, 
;   and the shift state in kbdshift (even uncombined).
;   The translated keycode (ASCII) is stored in a
;   buffer at kbdbuf.  Repeats and delays are controlled
;   by kbdlimit and kbddelay.
;
;	This is called during an interrupt!
;
;	(I.E., don't use the stack!)
;
;	WP = INTWS

scankbd	mov	11,10

	clr		12
	sbo		21		 	; clear alpha lock line

	clr		1		 	; read char

	li		12,>24	 	; kbd select
	clr		3			; row * >100
	ldcr	3,3		 	; set

	li		12,>6	 	; kbd matrix
	stcr	4,8		 	; get row 0
	inv		4			; 0=off 1=on
	
	clr     5
	cb      #>72, 4     ; ctrl+fctn+shift+space (abort)?
	jne     sknobreak
	
	seto    5           ; remember for later
	
sknobreak:	
	mov		4,2			; copy to R2=shifts
	andi	2,>7000		; save 0=off 1=on (shifts)
	movb	2,@kbdshft	; save shift
	andi	4,>0700		; mask =, space, enter
	jmp		skloop0

skloop:
	seto	4		 	; set low bits too	
	stcr	4,8		 	; read 8 bits
  	inv		4		 	; 0=off 1=on
skloop0:
	jne		skgotsome	; any bits set?
	ai		3,>100
	ci		3,>600		; stop at joystick
	jeq		skblank

	li		12,>24		; point to kbd select
	ldcr	3,3		 	; set new row #
	li		12,>6	 	; point to matrix
	jmp		skloop

skgotsome:
	srl		3,5		 	; entry into table
	swpb	4		 	; move to low byte so we can
skwhich:
	srl		4,1			; roll down
	joc		skdone	 	; this bit?
	inc		3			; next
	jmp		skwhich

skblank:
	movb	1,@kbdscan	; no key whatsoever

	movb	@kbdshft,3	; shifts?
	jeq		sknone

	jmp		sknone0

skdone:	
	bl		@kbdhandle
sknone0:
    a       @timeout,@randnoise
    clr     @timeout
	bl     	@vscreenon

sknone:
	movb	1,@kbdlast	   	; update last char
	a		1,@randnoise

	b		*10

;-------------------------------------------------------------------------

kbdhandle
;    movb    @kbdmode, 9     ; kbd_trans set?
;	jlt		khgetascii
;
;    inc     3
;	swpb	3
;	movb	3,@kbdscan	 	; save scancode
;	movb	3,1		 		; R3=R1=scancode
;	jmp		khtestbuffer
;	
;khgetascii:
	srl		2,11		 	; get shift state
	ai		2,grom_kbdlist	

    li      9,GPLRA
	movb	*9,8	      ; save GROM addr in R8
	swpb    8
	movb	*9,8
	swpb    8
	dec		8

	li		12,GPLWA
	movb	2,*12			; point to grom kbd list
	swpb	2
	movb	2,*12

    dect    9
	movb	*9,2		   ; get table entry
	swpb	2
	movb	*9,2		   ; it's flipped in GROM
	swpb	2				

	a		3,2		 		; get offset

	movb	2,*12			; point to char
	swpb	2
	movb	2,*12
	
	movb	*9,1      	 	; R1=key code, 0-255

	movb	8,*12	    	; restore GROM addr
	swpb    8
	movb	8,*12

	clr		12
	sbz		21		 	; turn on alpha lock line
	tb		7
	jeq		khnoalpha
		
	ci		1,>6100		; alpha lock on; 
	jl		khnoalpha	; test 'a'-'z'
	ci		1,>7b00
	jhe		khnoalpha
	ai		1,->2000	; uppercase

khnoalpha:
	sbo		21
	inc     3
	swpb	3			; put scancode in hi byte

khtestbuffer:
	sla		4,1		 	; kbd_poll set?
	joc		khnone

	; HACK!  Fctn-Shift-S is treated as Ctrl-H
	cb		#211, 1
	jne		$0+
	cb		#>30, @kbdshft
	jne		$0
	
	li		1,>0800
	
$0:
	socb	1,1
	jne		khbuffer   	; got something

	jmp		khnone

;-------------------------------------------

;   Buffer a key if it's new or the same as the last
;   one and a significant time has elapsed.
khbuffer:	
	cb		3,@kbdscan 	       ; scancode the same?
	jne		khnew

	movb	@kbdflag, 2      	; get flags
	ab      2,2
	jnc		khb4repeat          ; repeating yet?

	cb		@kbdtimer,@kbddelay ; time for new repeat?
	jl		khnone
	jmp		khstuff

khb4repeat:
	cb		@kbdtimer,@kbdlimit ; repeated long enough yet?
	jl		khnone		     	; no
	
    socb    #>80,@kbdflag       ; set repeat flag
	
	jmp		khstuff

khnew:
	szcb	#>80,@kbdflag	    ; clear repeat flag
	movb	3,@kbdscan			; save new scancode

khstuff:
	sb		@kbdtimer,@kbdtimer ; restart timer

    mov     5,5                 ; check abort flag
    jeq     khstuffit
    
    clr     @kbdhead
    clr     @kbdflag
    b       @ABORT

khstuffit:
	movb	@kbdtail,2	       	; get current pos in ring
    srl     2,8
	movb	1,@kbdbuf(2)	    ; buffer it
	inc		2		     		; inc...
	andi	2,kbdbufsize-1	    ; roll over if necc
	swpb	2
	cb		2,@kbdhead	     	; overflow if equal!
	jeq		khnone		     	; eeeer... don't update ptrs

	movb	2,@kbdtail	     	; update

khnone:

khout:
	rt




;--------------------------------------------------

;	If key available, return EQ=0.
;
;	Should be called with INTERRUPTS DISABLED.
;

kbdavail 
;    movb    @kbdmode, 0
;    sla     0, 2                ; is kbd_poll set?
;	jnc		katestbuff
;	
;	movb	@kbdscan,0         	; kbdscan=0 means none
;	rt
;
;katestbuff:
	cb		@kbdhead,@kbdtail	; EQ=1 means none
	rt

;	Read the last char from the keyboard buffer.
;	Assumes that "kbdavail" returned positively.
;
;	Returns all r0=ASCII-esque keycode.
;
;	If R0=0, then no char is available.
;
;	This should be called WITH INTERRUPTS DISABLED, as the interrupt
;	routine WILL modify all these variables.

kbdread	   
;	movb	@kbdmode,1
;	sla		1,2					; kbd_poll set?
;	jnc		krbuff
;
;	movb	@kbdscan,0			; read last polled char
;	srl		0,8
;	rt
;
;krbuff:							; read from keyboard buffer
	clr		1
	cb		@kbdhead,@kbdtail
	jeq		krbempty
	movb	@kbdhead,1			; get head ptr
	ab		#1,@kbdhead		; and inc...
	szcb	#-kbdbufsize,@kbdhead	; mask...
	swpb	1					; and make offset
	movb	@kbdbuf(1),0		; and retrieve!
	srl		0,8
krbempty:
	rt
