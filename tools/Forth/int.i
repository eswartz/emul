;	int.inc						-- VDP interrupt routine 
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
;	$Id: int.i,v 1.15 2009-01-17 23:20:52 ejs Exp $

;==========================================================================
;	The VDP interrupt routine.
;
;	This handles clock, keyboard, etc.
;
;   This lives in the standard INT1 workspace, where R3 R4 R5 R6 are free
;   and R0 is a random seed ... just leave it so
;==========================================================================

int1 limi	0			; disable interrupts 

	clr		r12			; point to 9901
	tb		2			; VDP interrupt?
	jne		intvdp		; yup.

    tb      3           ; timer interrupt?
    jeq     $1+         ; nope, must be device

    sbo     3           ; acknowledge
    
    mov     @timerisr, 3 ; does user hook the timer?
    jeq     $1+
    
    bl      *3          ; call user handler
    jmp     int1out

$1:     
    ; device interrupt (some other bit)
      	
	lwpi	>83e0		; they require this...
	lwpi	intws
	jmp		int1out		; don't handle device interrupts yet

intvdp:
	sbo	    2			; acknowledge VDP interrupt
    movb    @VDPST, 3   ; acknowledge #2
    
	inc		@uptime + 2	; time in 1/60 seconds
	jnc		intv00		; overflow?
   	inc		@uptime	 	; more time accuracy

intv00:	
    movb    @vintflags,3    ; check our commands
    sla     3,1             ; suppress blinking/blanking?
    joc     intv01

    ; ----------------------- blink/blank
        
	inct	@timeout	    ; blank screen?
	jne		intv00b

	bl     	@vscreenoff 

intv00b:
	li		0,vcrstimer
	ab		#1,*0			; cursor timer
	cb		*0,@vcrsblink	; to blink or not to blink?
	jl		intv01

	sb		*0,*0			; clear
	mov		@vcursor,0		; get ptr
	blwp	*0				; blink it

intv01:
    sla     3,1             ; suppress keyboard scan?
    joc     intv02

    ; ----------------------- keyboard scan
    
    ab      #1,@kbdtimer    ; inc repeat delay
    bl      @scankbd        ; get keyboard, save char       TRASHES REGS

intv02:
    movb    @vintflags, 3   ; kbd trashed, so reread
    sla     3,3
    joc     intv03          ; suppress sprite motion?
    
    mov     @vsprmot, 0
    jeq     intv03          ; skip if no sprite motion table
    
    movb    @nsprmot, 0     ; or no sprites
    jeq     intv03
    
    blwp    @vspritemotion
    
intv03:
    sla     3,1
    joc     intv04          ; sound list?

    ; ------------------------ play sound list
    
    mov     @sndlist, 0
    jeq     intv04
    
    blwp    @soundlist
    
intv04:    
   blwp    @sound_sequencer
    
    ; ----------------------- user interrupt?
    
	mov		@userint,0		
	jeq		intv05

	bl		*0				; execute user interrupt routine
	lwpi	intws

intv05:
    ;movb    #>00, @VDPWA    ; point to SR0     ; the client must guarantee this
    ;movb    #>8f, @VDPWA
	movb	@VDPST, @vstatus ; clear interrupt

int1out:

	bl		@bankrtwp
	data	int1flag

int2
	bl		@bankrtwp
	data	int2flag


