; ***************************************
; INT.ASM  V9t9 interrupt manager
; ***************************************
; by Edward Swartz  6/4/1993
; ***************************************


_INT_	= 1

	include	standard.h
	include	tiemul.h
	include	video.h
	include	hardware.h
	include	files.h
	include	keyboard.h
	include	debug.h
	include	record.h
	include	log.h
	include	int.h

	include	memory.inc
	include	registers.inc


	.data

videodatachanges db	0	
		even

oldctrlbreak	dd	?		; ctrl-break (1bh)
oldctrlc	dd	?		; ctrl-c (23h)
oldint8		dd	?		; timer interrupt (8h)
oldcriterr	dd	?		; critical error (24h)
oldkeyboard	dd	?		; keyboard interrupt (9h)

spchcntr	db	0

elapsed		dw	0		; 1/60 seconds we've been going

delta60		dw	0
cntr60		dw	0
deltati		dw	0
cntrti		dw	0
delta8		dw	0
cntr8		dw	0

timer0interval	dw	0		; interval on timer 0
currentspeed	dw	0		; fastest interrupter, 18-10000?
currentfunc	dw	nullinterrupter

debugint	db	1		; let interrupts while debugging
	even

	.code

;===========================================================================
;	INT:	Pre-config init.
;===========================================================================

int_preconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	INT:	Post-config init.
;===========================================================================

int_postconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	INT:	Restart.
;
;	þ  Set timer and other interrupts.
;
;===========================================================================

int_restart proc near
	push	ax
	call	setrs232ints
	call	installtimerinterrupt
	call	installkeyboardinterrupt
	call	installcriticalerror
	pop	ax
	clc
	ret
	endp


;===========================================================================
;	INT:	Restop.
;
;	þ  Restore interrupt vectors.
;
;===========================================================================

int_restop proc	near
	call	resetrs232ints
	call	resettimerinterrupt
	call	resetkeyboardinterrupt
	call	resetcriticalerror

	clc
	ret
	endp


;===========================================================================
;	INT:	Shutdown.
;
;	þ  Reset Ctrl-C and Ctrl-Break handlers that point into V9t9.
;
;===========================================================================

int_shutdown proc near
	xor	ax,ax
	call	setspeed
	clc
	ret
	endp







;	Install the timer interrupt.
;
;
installtimerinterrupt proc near
	mov	ax,3508h
	int	21h
	mov	word ptr oldint8+2,es
	mov	word ptr oldint8,bx

	push	ds
	mov	ax,2508h
	mov	dx,seg cs:int8
	mov	ds,dx
	mov	dx,offset cS:int8
	int	21h
	pop	ds

	mov	ax,timer0interval
	call	setspeed

	ret
	endp


;	Install a critical error handler.
;
;
installcriticalerror proc near
	mov	ax,3524h
	int	21h
	mov	word ptr oldcriterr+2,es
	mov	word ptr oldcriterr,bx

	push	ds
	mov 	ax,02524h
	mov 	dx,seg cs:handlecriticalerror
	mov 	ds,dx
	mov 	dx,offset cs:handlecriticalerror
	int 	21h			; set CRITICAL ERROR interrupt
	pop	ds
	ret
	endp


;	SETUPCTRLC --	Install handler for ^C
;
;
installctrlc	proc	near
	mov	ax,3523h
	int	21h			; get old ctrl-c
	mov	word ptr oldctrlc+2,es
	mov	word ptr oldctrlc,bx	

	push	ds
	mov 	ax,2523h
	mov 	dx,seg cs:ctrlbreak
	mov 	ds,dx
	mov 	dx,offset cs:ctrlbreak
	int 	21h			; set CTRL-C interrupt
	pop	ds

	mov	ax,351bh
	int	21h			; get old ctrl-break
	mov	word ptr oldctrlbreak+2,es
	mov	word ptr oldctrlbreak,bx 

	push	ds
	mov 	ax,251bh
	mov 	dx,seg cs:ctrlbreak
	mov 	ds,dx
	mov 	dx,offset cs:ctrlbreak
	int 	21h			; set CTRL-BREAK interrupt
	pop	ds

	ret
	endp


;	Install the keyboard interrupt.
;
;
installkeyboardinterrupt proc near

	mov	ah,35h
	mov	al,09h
	int	21h
	mov	word ptr oldkeyboard+2,es
	mov	word ptr oldkeyboard,bx

	push	ds
	mov 	ax,02509h
	mov 	dx,seg cs:keyboard
	mov 	ds,dx
	mov 	dx,offset cs:keyboard
	int 	21h			; set keyboard interrupt
	pop	ds

	ret
	endp



;	Release the timer interrupt.
;
;
resettimerinterrupt proc near
	mov	bx,timer0interval	; preserve speed around
	xor	ax,ax			;
	call	setspeed		;
	mov	timer0interval,bx	; reset of timer

	push	ds
	mov	ax,2508h
	mov	dx,word ptr oldint8
	mov	ds,word ptr oldint8+2
	int	21h
	pop	ds

	ret
	endp


;	Release the critical error interrupt.
;
;
resetcriticalerror proc near
	push	ds
	mov	ax,2524h
	mov	dx,word ptr oldcriterr
	mov	ds,word ptr oldcriterr+2
	int	21h
	pop	ds
	ret
	endp




;	Release the two Ctrl-C vectors.
;
;
resetctrlc proc	near
	push	ax
	push	dx

	push	ds
	mov	ax,0251bh
	mov	dx,word ptr oldctrlbreak
	mov	ds,word ptr oldctrlbreak+2
	int	21h
	pop	ds
	push	ds
	mov	ax,02523h
	mov	dx,word ptr oldctrlc
	mov	ds,word ptr oldctrlc+2
	int	21h
	pop	ds

	pop	dx
	pop	ax
	ret
	endp


;	Set the old keyboard interrupt (for dos shell, pathname entering,
;	debugger, exit, etc.)
;
resetkeyboardinterrupt proc near
	push	ds
	mov 	ax,02509h
	mov 	dx,word ptr oldkeyboard
	mov	ds,word ptr oldkeyboard+2
	int 	21h
	pop	ds
	ret
	endp

	



;===========================================================================
;	This procedure changes the timer interrupt.
;	AX is the value written to the timer register.
;	Figure the Hz value by doing 1234DC ö AX.
;	18.2 Hz = FFFF
;	60   Hz = 4DAE
;
setspeed	proc	near
	push	ax
	push	bx
	mov	timer0interval,ax
	mov	bx,ax
	cli
	mov	al,036h
	out	043h,al
	mov	ax,bx
	out	040h,al
	mov	al,ah
	out	040h,al
	sti
	pop	bx
	pop	ax
	ret
	endp



;	New interrupt scheduling.  The maximumly interrupting process
;	controls the speed of INT8.  
;
;	V9t9 has a maximum of three timers -- one goes invariably at
;	60 Hz (for time-critical timing), one goes at TimerSpeed
;	(for 99/4A VDP interrupts), and the other is variable.
;
;	This last timer is used for cassette routines and speech
;	output.
;
;	CURRENTFUNC holds a near pointer to the fastest process.
;	When its speed (CURRENTSPEED) is changed (setcurrentspeed), this 
;	procedure is called to call setspeed and change the delta values.
;	(allow minimum of 19 currentspeed)
;
settimerspeed proc near
	push	ax
	push	bx
	push	dx

	mov	bx,currentspeed

	mov	dx,60			; figure delta for 60 times/sec int
	xor	ax,ax			; speed = currentspeed
	cmp	bx,dx
	jbe	stssame0		; faster than currentspeed???
	div	bx			; speed = 60 Hz
stssame0:
	mov	delta60,ax
	mov	cntr60,0

	mov	dx,timerdelay		; figure delta for TI interrupt
	xor	ax,ax			; speed = currentspeed
	cmp	bx,dx
	jbe	stssame1
	div	bx			; speed = timerspeed Hz
stssame1:
	mov	deltati,ax
	mov	cntrti,0

	mov	dx,18
	xor	ax,ax			; speed = currentspeed
	cmp	bx,dx
	jbe	stssame2
	mov	ax,13107		; dx:ax = 18.2
	div	bx			; speed = 18.2 Hz
stssame2:
	mov	delta8,ax
	mov	cntr8,0

	mov	dx,12h
	xor	ax,ax			; INT8 speed = 18.2 Hz
	cmp	bx,dx
	jbe	stssame3
	mov	ax,34DDh	
	div	bx			; INT8 speed = currentspeed
stssame3:
	call	setspeed

	pop	dx
	pop	bx
	pop	ax
	ret
	endp



;	Set current maximum delay.
;	Called before and after big-ass-fast interrupt routine executed.
;
;	Interrupt slower than maximum of timerspeed or 60 Hz assumed to 
;	mean, "no more fast ints", because it will be called at the previous
;	maximum hz.
;
;	AX=Hz of process.
;
setcurrentspeed proc near
	or	ax,ax
	jnz	scsnotnull

	mov	currentfunc,offset nullinterrupter

scsnotnull:
	cmp	ax,timerdelay		; is timerspeed > currentspeed?
	jae	scsgreater0

	mov	ax,timerdelay		; yup, currentspeed=timerspeed

scsgreater0:

	cmp	ax,60			; is 60 Hz > currentspeed?
	jae	scsgreater1

	mov	ax,60			; yup, currentspeed=60

scsgreater1:
	
	mov	currentspeed,ax
	call	settimerspeed
	ret
	endp


nullinterrupter proc	near
	ret
	endp





;===========================================================================
;	This is the Ctrl-Break interrupt.
;
;	(CALLED only within DOS-controlled keyboard routines!)
;
ctrlbreak	proc	far
	push	ax
	push	ds
	mov	ax,@data
	mov	ds,ax
	or	stateflag,ctrlbreakpressed+happymessage
	pop	ds
	pop	ax
	iret
	endp	


;==========================================================================
;	Interrupt 8 -- keep everything synchronized
;
int8	proc	far
	push	ax
	push	ds
	mov	ax,@data
	mov	ds,ax

	call	word ptr currentfunc

	mov	ax,delta60
	or	ax,ax
	jz	tick60

	add	cntr60,ax
	jnc	skip60

tick60:
	call	interrupt60

skip60:
	mov	ax,deltati
	or	ax,ax
	jz	tickti

	add	cntrti,ax
	jnc	skipti

tickti:
	call	interruptti

skipti:
	mov	ax,delta8
	or	ax,ax
	jz	tick8

	add	cntr8,ax
	jnc	skip8

tick8:
	pushf
	call	dword ptr oldint8
	jmp	int8out

skip8:
	mov	al,20h
	out	20h,al
int8out:
	pop	ds
	pop	ax
	iret
	endp



;===========================================================================
;	This routine handles stuff that happens along with TI interrupts.
;
;	Happens from 18-200 Hz?
;
interruptti proc near
	mov	ax,stateflag	; for reading only!

	cmp	debugint,0	; let interrupts happen during debugging?
	jnz	itvdp		; sure... but we do it anyway

	test	ax,debugrequest 	; debugging?
	jz	itvdp		     	; nope... allow it

	test	ax,intdebug		; intermittent?
	jz	itnovdp			; nope... don't allow

itvdp:

	or	stateflag,titick

	or	vdpstat,vdpinterrupt
	or	currentints,M_INT2	; signal INT2 level interrupt
	call	handle9901		; changed interrupt status

	
itnovdp:

itnovdpint:
	ret
	endp


;==========================================================================
;	This routine handles stuff that happens every 1/60 second.
;
;

interrupt60 proc near
	or	stateflag,sixtieth

	inc	elapsed

	inc	sinceredrawcounter
	mov	al,sinceredrawcounter
	cmp	al,videoredrawmaxdelay
	jae	i60update

	inc	vdprestcounter
	mov	al,vdprestcounter
	cmp	al,videoredrawlatency
	jb	i60noupdate

i60update:	
	or	stateflag,videointoccured
	
i60noupdate:
	mov	joysticktick,1		; time to read joystick/mouse again

	inc	kbtimer			; count up to kbdelay

	test	stateflag,intdebug
	jz	i60speech

	mov	al,debugcnt		; see if time for an interrupt
	inc	al
	cmp	al,debugdelay
	jb	i60nodebug

	or	stateflag,debugrequest
	xor	al,al

i60nodebug:
	mov	debugcnt,al

i60speech:

	mov	ax,cips			; keep track of instructions per sec
	cmp	ax,100
	jae	i60valid
	mov	ax,100

i60valid:

	mov	cips,0
	add	word ptr ips,ax
	adc	word ptr ips+2,0
	jc	i60clear

	add	word ptr uptime,1
	adc	word ptr uptime+2,0
	jnc	i60done

i60clear:
	xor	ax,ax			; overflow!
	mov	word ptr ips,ax
	mov	word ptr ips+2,ax
	mov	word ptr uptime,ax
	mov	word ptr uptime+2,ax

i60done:

	ret
	endp






	end
