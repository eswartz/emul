; ********************************************
; JOYSTICK.INC  V9t9 joystick routine
; ********************************************
; by Edward Swartz.  4/10/1995
; ********************************************


	.data


isjoystick db	0			; &=1 if #1, &=2 if #2


joy1c	joyvals	<0,0>			; uncentered
joy2c	joyvals	<0,0>			; uncentered

joy1max	dw      0			; maximum count on joystick #1
joy2max	dw	0			; #2


joy1	joyvals	<0,0>			; just-read coords

joy2	joyvals	<0,0>			; just-read coords


	.code



;	To be called at the beginning of the program, to set
;	ISJOYSTICK, and JOY1C and JOY2C, and JOY1MAX, and JOY2MAX,
;	assuming the user has the joysticks at center.
;
findjoysticks proc near
	pusha

	mov	isjoystick,0

	mov	dx,40000
	mov	cx,4			; check four times
fjs1:
	mov	joy1max,dx		; maximum timeout possible (right?)
	lea	di,joy1c		; assume centered
	call	readjoystick1
     	jnc	fjsgot1
	loop	fjs1
	
	mov	joy1max,0		; no joystick
	jmp	fjstest2

fjsgot1:
	or	isjoystick,1		; got joystick #1
	mov	ax,joy1c.xx
	add	ax,joy1c.yy
	add	ax,ax
	mov	joy1max,ax		; set arbitrary JOY1MAX

fjstest2:
	mov	cx,4
fjs2:
	mov	joy2max,dx
	lea	di,joy2c
	call	readjoystick2
	jnc	fjsgot2

	mov	joy2max,0		; no joystick
       	jmp	fjsout

fjsgot2:
	or	isjoystick,2
	mov	ax,joy2c.xx
	add	ax,joy2c.yy
	add	ax,ax
	mov	joy2max,ax

fjsout:
	popa
	ret
	endp



;	To be used in the centering routine.
;
;	The joystick is centered.  So we'll save the values and
;	set JOYxC.
;
;	Assumes joystick exists.
;
;	AL = joystick #  (1 or 2)
;
savejoystickcenter proc near
	push	di

	cmp	al,1
	jne	sjc2

	mov	ah,2
	mov	cl,0
	call	getbutton

	mov	joy1max,65535		; reset maximum timeout
	lea	di,joy1c
	call	readjoystick1		; get center
	jmp	sjcout

sjc2:
	mov	ah,2
	mov	cl,2
	call	getbutton

	mov	joy2max,65535		; reset maximum timeout
	lea	di,joy2c
	call	readjoystick2		; get center

sjcout:
	pop	di
	ret
	endp



;	To be used in the centering routine.
;
;	The joystick is extreme lower-right.  So we'll calculate JOYxMAX.
;
;	Assumes joystick exists.
;
;	AL = joystick #  (1 or 2)
;
savejoystickmax proc near
	push	di

	cmp	al,1
	jne	sjc2

	mov	ah,1
	mov	cl,0
	call	getbutton

	mov	joy1max,65535		; reset maximum timeout
	lea	di,joy1
	call	readjoystick1		; get center

	mov	ax,joy1.xx
	add	ax,joy1.yy
	mov	joy1max,ax		; save this as max
	jmp	sjmout

sjm2:
	mov	ah,1
	mov	cl,2
	call	getbutton

	mov	joy2max,65535		; reset maximum timeout
	lea	di,joy2
	call	readjoystick2		; get center

	mov	ax,joy2.xx
	add	ax,joy2.yy
	mov	joy2max,ax		; save this as max

sjmout:
	pop	di
	ret
	endp


;	Wait for user to press a specific button.
;
;	AH=1 means STICK, AH=2 means BASE.
;	CL=0 means JOY#1, CL=2 means JOY#2.
;
getbutton proc near
	push	ax
	push	dx
	
	mov	dx,201h
gbloop:
	push	ax
	mov	ah,11h			; press a key to abort
	int	16h
	pop	ax
	jnz	gbabort

	in	al,dx
	not	al
	shr	al,4
	shr	al,cl
	test	al,ah
	jz	gbloop
	jmp	gbout

gbabort:
	mov	ah,10h
	int	16h
	mov	ah,11h
	int	16h
	jnz	gbabort

gbout:
	pop	dx
	pop	ax
	ret
	endp


;	Read joystick #1
;
;	Inputs:  DI=>JOYVALS
;
;	Returns C=1 if no joystick.
;	Else JOY1 is just-read coords, and AL=button status.
;
readjoystick1 proc near
	push	cx
	push	bp

	xor	cl,cl
	mov	bp,joy1max
	call	readjoystick

	pop	bp
	pop	cx
	ret
	endp


;	Read joystick #2
;
;	Inputs:  DI=>JOYVALS
;
;	Returns C=1 if no joystick.
;	Else JOY2 is just-read coords, and AL=button status.
;
readjoystick2 proc near
	push	cx
	push	bp

	mov	cl,2
	mov	bp,joy2max
	call	readjoystick

	pop	bp
	pop	cx
	ret
	endp



;	Read joystick.
;
;	[DI] is a JOYVALS record.
;	CL=0 if joystick #1, 2 if #2
;	BP=maximum
;
;	If no joystick connected or timeout, returns C=1,
;	else [DI].xx and [DI].yy are set, and AL=button status
;
;	The timing method used here is SOFTWARE DEPENDENT.
;	This is GOOD, because I don't have to mess with the
;	erratic DMA refresh bit, the PC speaker's timer channel,
;	or a hyped-up INT 8 interrupt -- ALL of which would
;	give less accurate results.

readjoystick 	proc near
	push	bx
	push	cx
	push	dx
	push	si

	mov	[di].xx,0 		; clear coordinates for read
	mov     [di].yy,0

	cli				; turn off interrupts

	xor	bx,bx			; clear "got x and y"
	xor	si,si			; clear counter

	mov	dx,201h			
	xor	al,al
	out	dx,al			; initialize joystick read

rjloop:
	jmp	$+2
	jmp	$+2
	jmp	$+2
	jmp	$+2			; waste time for Pentium geeks

	in	al,dx			; get status
	in	al,dx
	in	al,dx
	in	al,dx			; waste more time

	not	al			; invert
	shr	al,cl			; isolate correct joystick bits

	inc	si			; increment counter

	shr	al,1			; test X flag
	jnc	rjloopy
	test	bl,1
	jnz	rjloopy			; if not already set, save coord
	or	bl,1
	mov	[di].xx,si
rjloopy:
	shr	al,1			; test Y flag
	jnc	rjloopnext
	test	bl,2
	jnz	rjloopnext		; if not already set, save coord
	or	bl,2
	mov	[di].yy,si
rjloopnext:
	cmp	bl,3			; all bits read?
	je	rjloopout
	cmp	si,bp			; maximum time
	jb	rjloop

	stc
	jmp	rjout

rjloopout:
	in	al,dx			; get buttons
	not	al
	shr	al,4			; skip unused bits
	shr	al,cl			; isolate correct bits
	and	al,3			; mask
	clc		

rjout:
	sti				; turn on interrupts

	pop	si
	pop	dx
	pop	cx
	pop	bx
	ret
	endp


