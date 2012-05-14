;	V9t9: the TI Emulator! v6.0 Source 
;	Copyright (c) 1996 by Edward Swartz




;==========================================================================
;	The VDP interrupt routine.
;
;	This handles clock, keyboard, etc.
;
;==========================================================================

int	limi	0			; disable interrupts 

	clr	r12			; point to 9901
	tb	2			; VDP interrupt?
	jne	intvdp			; yup.
	jmp	intvdp
       	
	lwpi	>83e0			; they require this...
	lwpi	intws
	jmp	intout			; don't handle device interrupts yet

intvdp:
	sbo	2			; acknowledge VDP interrupt

	inc	@uptime 2+		; time in 1/60 seconds
	jnc	intv00			; overflow?
       	inc	@uptime	 		; more time accuracy

intv00:	
	ab	@h01,@kbdtimer		; inc repeat delay
	bl	@scankbd		; get keyboard, save char

@intpatch:	nop			; PATCH HERE for VDP int

;-----------------------------------

int2:
	inct	@timeout		; blank screen?
	jne	int200

	blwp	@vscreenoff

int200:
	li	0,vcrstimer
	ab	@h01,*0			; cursor timer
	cb	*0,@vcrsblink		; to blink or not to blink?
	jl	int201

	sb	*0,*0			; clear
	mov	@vcursor,0		; get ptr
	blwp	*0			; blink it

int201:
	mov	@userint,0		
	jeq	intout

	bl	*0			; execute user interrupt routine
	lwpi	intws

intout:
	RTWP
