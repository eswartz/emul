
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
	; clr		r12			; point to 9901
	; tb		2			; VDP interrupt?
	movb	@INTSP, 0		; check interrupts
	coc		#M_VDP, 0		; M_VDP set?
	jeq		intvdp		; yup.

;    tb      3           ; timer interrupt?
;    jeq     $1+         ; nope, must be device
;
;    sbo     3           ; acknowledge
;    
;    mov     @timerisr, 3 ; does user hook the timer?
;    jeq     $1+
;    
;    bl      *3          ; call user handler
;    jmp     int1out
;
; $1:     
;    ; device interrupt (some other bit)
;
;	lwpi	>83e0      	
;	lwpi	intws
	jmp		int1out		; don't handle device interrupts yet

intvdp:
	szcb	#<M_VDP, @INTSP	; acknowledge VDP interrupt
	
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
	movb	@VDPST, @vstatus ; clear interrupt

	b		@Int_Return
