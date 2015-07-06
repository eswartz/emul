;   int_video.inc
; 
;   (c) 2014 Ed Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 

;==========================================================================
;	The VDP interrupt routine.
;
;	This handles clock, keyboard, etc.
;
;   This lives in the standard INT1 workspace, where R3 R4 R5 R6 are free
;   and R0 is a random seed ... just leave it so
;==========================================================================

M_VDP		equ		>0400

int_1
	szcb	#<M_VDP, @INTSP	; acknowledge VDP interrupt
	
    movb    @vintflags,3    ; check our commands
	coc		#(256*vhorizint), 3	; check for horizontal scanning interrupt?
	jne		intvnorm
	
	mov		#>8f01, @VDPWA	; set for status #1
	movb    @VDPST,3 	    
	coc		#>0100, 3		; is FH set?
	jne		intvnorm
	
	movb	#>ff, @vhorizint  ; in horizontal interrupt
	mov		#>87B1, @VDPWA
	
	mov		#>8f00, @VDPWA	; set for status #0
	b		@Int_Return
	
intvnorm0: 
	li		3, >8717
	mov		3, @VDPWA
	
intvnorm:	
	movb	#0, @vhorizint	; not in horizontal interrupt
	
 	inc		@uptime + 2	; time in 1/60 seconds
 	jnc		intv00		; overflow?
   	inc		@uptime	 	; more time accuracy

intv00:	
    movb    @vintflags,3    ; check our commands
    sla     3,1             ; nvblnk: suppress blinking/blanking?
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
    sla     3,1             ; nvkeyscan: suppress keyboard scan?
	joc     intv02
 
    ; ----------------------- keyboard scan
    
    ab      #1,@kbdtimer    ; inc repeat delay
    bl      @scankbd        ; get keyboard, save char       TRASHES REGS
 
intv02:
    movb    @vintflags, 3   ; kbd trashed, so reread
    sla     3,3			 ; nvsprmot: suppress sprite motion?
    joc     intv03          
     
    mov     @vsprmot, 0
    jeq     intv03          ; skip if no sprite motion table
     
    movb    @nsprmot, 0     ; or no sprites
    jeq     intv03
     
    blwp    @vspritemotion
     
intv03:
    sla     3,1			 ; nsoundlist: play sound list/synth?
    joc     intv04          
 
    ; ------------------------ play sound list
     
    mov     @sndlist, 0
    jeq     intv04b
     
    blwp    @soundlist

intv04b:     
	blwp    @sound_sequencer

intv04:    
    ; ----------------------- FORTH tick
    
   ; bl		@tick_forth	; FIXME: trashes R11
     
;     ; ----------------------- user interrupt?
;     
; 	mov		@userint,0		
; 	jeq		intv05
; 
; 	bl		*0				; execute user interrupt routine
; 	lwpi	intws
; 
intv05:

int1out:
	mov		#>8f00, @VDPWA	; set for status #0
	movb	@VDPST, @vstatus ; clear interrupt

	b		@Int_Return
