;	V9t9: the TI Emulator! v6.0 Source 
;	Copyright (c) 1996 by Edward Swartz


;--------------------------------------------------------------------------

kbd_scan equ	>80
kbd_poll equ	>40

kinit	si	10,6
	mov	0,@4(10)
	mov	1,@2(10)
	mov	2,*10

	li	1,@kbdlength		; init everything
	li	2,@kbdstart
kinit0	clr	+*2
	dect	1
	jgt	kinit0

	li	0,>1e00			; 1/2 s before repeat
	movb	0,@kbdlimit		
	li	0,>0300			; 1/20 s delay between repeat
	movb	0,@kbddelay

	mov	+*10,2
	mov	+*10,1
	mov	+*10,0
	rt


;	This is called during an interrupt!
;
;	(I.E., don't use the stack!)
;
;	WP = INTWS

scankbd	mov	11,10

	clr	12
	sbo	21		 	; clear alpha lock line

	clr	1		 	; read char
;;	sb	@kbdscan,@kbdscan	; clear scancode

	li	12,>24		 	; kbd select
	clr	3		 	; row * >100
	ldcr	3,3		 	; set

	li	12,>6		 	; kbd matrix
	stcr	4,8		 	; get row 0
	inv	4		 	; 0=off 1=on
	mov	4,2		 	; copy to R2=shifts
	andi	2,>7000		 	; save 0=off 1=on (shifts)
	movb	2,@kbdshft	 	; save shift
	andi	4,>0700		 	; mask =, space, enter
	jmp	skloop0

skloop:
	seto	4		 	; set low bits too	
	stcr	4,8		 	; read 8 bits
       	inv	4		 	; 0=off 1=on
skloop0:
	jne	skgotsome	 	; any bits set?
	ai	3,>100
	ci	3,>600		 	; stop at joystick
	jeq	skblank

	li	12,>24		 	; point to kbd select
	ldcr	3,3		 	; set new row #
	li	12,>6		 	; point to matrix
	jmp	skloop

skgotsome:
	srl	3,5		 	; entry into table
	swpb	4		 	; move to low byte so we can
skwhich:
	srl	4,1		 	; roll down
	joc	skdone		 	; this bit?
	inc	3		 	; next
	jmp	skwhich

skblank:
	movb	1,@kbdscan		; no key whatsoever
	jmp	sknone
skdone:	
	blwp	@vscreenon
	clr	@timeout

	bl	@kbdhandle
sknone:
	movb	1,@kbdlast	     	; update last char

	b	*10

;-------------------------------------------------------------------------

kbdhandle
	movb	@kbdmode,4
	sla	4,1		 	; kbd_scan set?
	jnc	khgetascii

	swpb	3
	ai	3,>100
	movb	3,@kbdscan	 	; save scancode
	movb	3,1		 	; R3=R1=scancode
	jmp	khtestbuffer
	
khgetascii:
	srl	2,11		 	; get shift state
	ai	2,grom_kbdlist	

	movb	@>9802,@kbdgradr	; save GROM addr
	movb	@>9802,@kbdgradr 1+
	dec	@kbdgradr

	li	12,>9c02

	movb	2,*12			; point to grom kbd list
	swpb	2
	movb	2,*12

	movb	@>9800,2		; get table entry
	swpb	2
	movb	@>9800,2		; it's flipped in GROM

	a	3,2		 	; get offset

	movb	2,*12			; point to char
	swpb	2
	movb	2,*12
	
	movb	@>9800,1 	 	; R1=ASCII code, 0-255

	movb	@kbdgradr,*12	   	; restore addr
	movb	@kbdgradr 1+,*12

	ci	1,>ff00
	jne	kbnobreak

	b	@break


kbnobreak:
	clr	12
	sbz	21		 	; turn on alpha lock line
	tb	7
	jeq	khnoalpha
		
	ci	1,>6100		 	; alpha lock on; 
	jl	khnoalpha	 	; test 'a'-'z'
	ci	1,>7b00
	jhe	khnoalpha
	ai	1,0 >2000 -	 	; uppercase

khnoalpha:
	sbo	21
;;;	movb	1,@kbdscan		; save scancode
	swpb	3			; put scancode in hi byte
	ai	3,>100

khtestbuffer:
	sla	4,1		 	; kbd_poll set?
	joc	khnone

	socb	1,1
	jne	khbuffer   	 	; got something

	jmp	khnone

;-------------------------------------------

khbuffer:	
	
	sla	4,1		 	; kbd_poll set?
	joc	khnone		 	; don't buffer

	cb	3,@kbdscan	 	; scancode the same?
	jne	khnew

	movb	@kbdflag,@kbdflag    	; repeating yet?
	jeq	khb4repeat

	cb	@kbdtimer,@kbddelay  	; time for new repeat?
	jl	khnone
	jmp	khstuff

khb4repeat:
	cb	@kbdtimer,@kbdlimit  	; repeated long enough yet?
	jl	khnone		     	; no
	movb	@hff,@kbdflag	     	; set flag
	jmp	khstuff

khnew:
	movb	@h00,@kbdflag	     	; clear repeat flag
	movb	3,@kbdscan		; save new scancode

khstuff:

	clr	2
	movb	@kbdtail,2	       	; get current pos in ring
	swpb	2
	movb	1,@kbdbuf(2)	     	; buffer it
	inc	2		     	; inc...
	andi	2,kbdbufsize 1-	     	; roll over if necc
	swpb	2
	cb	2,@kbdhead	     	; overflow if equal!
	jeq	khnone		     	; eeeer... don't update ptrs

	movb	2,@kbdtail	     	; update
	sb	@kbdtimer,@kbdtimer  	; restart timer

khnone:

khout:
	rt




;--------------------------------------------------

;	CALLED FROM FORTH.
;
;	If key available, return EQ=0.
;
;	Should be called with INTERRUPTS DISABLED.
;

kbdavail movb	@kbdmode,0
	sla	0,2		  	; kbd_poll set?
	jnc	katestbuff	  	; yup
	
	movb	@kbdscan,@kbdscan 	; kbdscan=0 means none
	rt

katestbuff:
	cb	@kbdhead,@kbdtail	; EQ=1 means none
	rt


;	CALLED FROM FORTH!
;
;	Read the last char from the keyboard.
;	Assumes that "kbdavail" returned positively.
;
;	Returns all r0=value (either scancode or ASCII code,
;	depending on kbdmode).
;
;	If R0=0, then no char is available.
;
;	This should be called WITH INTERRUPTS DISABLED, as the interrupt
;	routine WILL modify all these variables.

kbdread	movb	@kbdmode,1
	sla	1,2			; kbd_poll set?
	jnc	krbuff

	movb	@kbdscan,0		; read last polled char
	srl	0,8
	rt

krbuff:					; read from keyboard buffer
	clr	1
	cb	@kbdhead,@kbdtail
	jeq	krbempty
	movb	@kbdhead,1		; get head ptr
	ab	@h01,@kbdhead		; and inc...
	szcb	@kbdbufmask,@kbdhead	; mask...
	swpb	1			; and make offset
	movb	@kbdbuf(1),0		; and retrieve!
	srl	0,8
krbempty:
	rt
	
