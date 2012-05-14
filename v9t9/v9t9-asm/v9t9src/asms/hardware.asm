; *******************************************
; HARDWARE.ASM  V9t9 CRU I/O routines
; *******************************************
; by Edward Swartz.  6/4/1993
; *******************************************


_HARDWARE_ = 1

	include	standard.h
	include	tiemul.h
	include	video.h			; for DRAWLED only!
	include	int.h
	include	files.h
	include	keyboard.h
	include	hardware.h

	include	memory.inc
	include	registers.inc


	.data

rs232led	db	0
diskled		db	0
emudiskled	db	0

;-------------------------------------
;	9901 variables
;-------------------------------------

mode9901	db	0			; 0=ints enabled, 
		even				; 1=programming clock

int9901		dw	0			; interrupt enable mask
currentints	dw	0			; pending ints
handledints	dw	0			; which ints have been 
						; handled?

clockinvl	dw	0			; clock interval
maxclockhertz	dw	3FFFh			; maximum (for computer)

clockhertz	dw	0			; speed of 9901 clock in Hz
latchedtimer	dw	0			; current val (inverted)

;-------------------------------------

diskromon	db	0			; is ROM selected for DISK?
usediskdsr	db	0			; use TI DISK dsr?
	even



;	Table of emulated bits
;
;	<bit addr>,<# bits max to read/write>,<address>
;

NULL	equ	0

CRUWtable dw	0000h,1,W9901_0
	dw	002h,14,W9901_A
	dw	0002h,1,W9901_S
	dw	0004h,1,W9901_S
 	dw	0006h,1,W9901_S
 	dw	0008h,1,W9901_S
 	dw	000ah,1,W9901_S
 	dw	000ch,1,W9901_S
 	dw	000eh,1,W9901_S
 	dw	0010h,1,W9901_S
 	dw	0012h,1,W9901_S
 	dw	0014h,1,W9901_S
 	dw	0016h,1,W9901_S
 	dw	0018h,1,W9901_S
 	dw	001ah,1,W9901_S
 	dw	001ch,1,W9901_S
 	dw	001eh,1,W9901_f

	dw	0024h,3,KeyWrite
	dw	0024h,1,KeyC2
	dw	0026h,1,KeyC1
	dw	0028h,1,KeyC0
	dw	002Ah,1,AlphaW
	
	dw	0030h,1,AudioW
	dw	0032h,1,CassetteW

	dw	1000h,1,emuDSKROM


	dw	1100h,1,DSKROM
;;	dw	1102h,1,DSKmotor		; don't need to emulate
	dw	1104h,1,dDSKhold
	dw	1106h,1,DSKheads
	dw	1108h,1,DSKsel1
	dw	110ah,1,DSKsel2
	dw	110ch,1,DSKsel3
	dw	110eh,1,dDSKside

	dw	1300h,1,RS232ROM

	dw	1302h,1,piosend
	dw	1304h,1,pioreset

	dw	1340h,8,rswregs			; RS232/1
	dw	1350h,3,rswrates
	dw	1356h,1,rsw0b
	dw	1358h,1,rsw0c
	dw	135ah,1,rsw0d
	dw	135ch,1,rsw0e
	dw	135eh,1,rsw0f

	dw	1360h,1,rsrts
	dw	1362h,1,rsbrk
	dw	1364h,1,rsrie
	dw	1366h,1,rstbie
	dw	1368h,1,rstie
	dw	136ah,1,rsdscie

	dw	137eh,1,rsreset


	dw	1380h,8,rswregs			; RS232/2
	dw	1390h,3,rswrates
	dw	1396h,1,rsw0b
	dw	1398h,1,rsw0c
	dw	139ah,1,rsw0d
	dw	139ch,1,rsw0e
	dw	139eh,1,rsw0f

	dw	13a0h,1,rsrts
	dw	13a2h,1,rsbrk
	dw	13a4h,1,rsrie
	dw	13a6h,1,rstbie
	dw	13a8h,1,rstie
	dw	13aah,1,rsdscie

	dw	13beh,1,rsreset

CRUWTableLen	equ ($-CRUWTable)/6
	dw	-1


CRURtable dw	0000h,1,R9901_0
	dw	0002h,1,R9901_1
	dw	0004h,1,R9901_2

	dw	0006h,8,R9901_K
	dw	0006h,1,R9901_S
	dw	0008h,1,R9901_S
	dw	000ah,1,R9901_S
	dw	000ch,1,R9901_S
	dw	000eh,1,R9901_S
	dw	0010h,1,R9901_S
	dw	0012h,1,R9901_S
	dw	0014h,1,R9901_S

	dw	0016h,4,R9901_L
	dw	0016h,1,R9901_S
	dw	0018h,1,R9901_S
	dw	001ah,1,R9901_S
	dw	001ch,1,R9901_S
	dw	001eh,1,R9901_S

	dw	002Ah,1,AlphaR

	dw	1102h,1,DSKget1
	dw	1104h,1,DSKget2
	dw	1106h,1,DSKget3

	dw	1304h,1,piobusy

	dw	1340h,8,rsreadbyte
	dw	1340h,7,rsreadbyte
	dw	1340h,6,rsreadbyte
	dw	1340h,5,rsreadbyte

	dw	1352h,1,rsre
	dw	1354h,1,rsrpe
	dw	1356h,1,rsore
	dw	1358h,1,rsfe
	dw	135ah,1,rsfbd
	dw	135ch,1,rssbd
	dw	135eh,1,rsrinp
	dw	1360h,1,rsrint
	dw	1362h,1,rstrai

	dw	1366h,1,rstimi
	dw	1368h,1,rsdssci
	dw	136ah,1,rsrbrl
	dw	136ch,1,rstbre
	dw	136eh,1,rstsre
	dw	1370h,1,rsterr
	dw	1372h,1,rstela
	dw	1374h,1,rsrrts
	dw	1376h,1,rsrdsr
	dw	1378h,1,rsrcts
	dw	137ah,1,rsdssc
	dw	137ch,1,rsflag		; **
	dw	137eh,1,rsint


	dw	1380h,8,rsreadbyte
	dw	1380h,7,rsreadbyte
	dw	1380h,6,rsreadbyte
	dw	1380h,5,rsreadbyte

	dw	1392h,1,rsre
	dw	1394h,1,rsrpe
	dw	1396h,1,rsore
	dw	1398h,1,rsfe
	dw	139ah,1,rsfbd
	dw	139ch,1,rssbd
	dw	139eh,1,rsrinp
	dw	13a0h,1,rsrint
	dw	13a2h,1,rstrai

	dw	13a6h,1,rstimi
	dw	13a8h,1,rsdssci
	dw	13aah,1,rsrbrl
	dw	13ach,1,rstbre
	dw	13aeh,1,rstsre
	dw	13b0h,1,rsterr
	dw	13b2h,1,rstela
	dw	13b4h,1,rsrrts
	dw	13b6h,1,rsrdsr
	dw	13b8h,1,rsrcts
	dw	13bah,1,rsdssc
	dw	13bch,1,rsflag		; **
	dw	13beh,1,rsint

	dw	1ffeh,1,Bogus
CRURTableLen	equ ($-CRURTable)/6
	dw	-1


	.code


;===========================================================================
;	HARDWARE:	Pre-config init.
;===========================================================================

hardware_preconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	HARDWARE:	Post-config init.
;
;	þ  Initialize the 9901 interrupt status.
;===========================================================================

hardware_postconfiginit proc near
	call	reset9901
	clc
	ret
	endp


;===========================================================================
;	HARDWARE:	Restart.
;===========================================================================

hardware_restart proc near
	clc
	ret
	endp


;===========================================================================
;	HARDWARE:	Restop.
;===========================================================================

hardware_restop proc near
	clc
	ret
	endp


;===========================================================================
;	HARDWARE:	Shutdown.
;===========================================================================

hardware_shutdown proc near
	clc
	ret
	endp





;	Reset 9901 state.
;

reset9901 proc	near
	mov	currentints,0
	mov	int9901,0
	mov	mode9901,0
	mov	clockinvl,0
	mov	clockhertz,0
	mov	handledints,0
	xor	ax,ax
	call	setcurrentspeed
	ret
	endp


;---------------------------------------------------------------------------
;	Handle changes to the 9901 state.
;
;	This includes changes to 
;		þ  the processor's Interrupt Mask,
;		þ  the int9901 mask, 
;	and	þ  the currentints mask.
;
;	The most this routine will do is set "interruptoccuring".
;
;	External interrupts can set "currentints" but nothing else!
;
;---------------------------------------------------------------------------

handle9901 proc	near
	push	ax

	and	stateflag,not interruptoccuring	; assume no action

	mov	ax,currentints			; pending interrupts
	and	ax,int9901			; mask out unenabled
	jz	h99none

	cmp	intmask,0			; processor accepting them?
	je	h99none				; hell no

	or	stateflag,interruptoccuring	; interrupt it, baby

h99none:
	pop	ax
	ret
	endp


;----------------------------------------------------------------------------
;	Reset an interrupt level.
;
;	This is usually called by the 99/4A as "SBO 2" or "SBO 3",
;	but must be called manually by external devices.
;
;	DX = mask for interrupt
;----------------------------------------------------------------------------

reset9901int proc near
	push	dx
	test	handledints,dx
	jz	r99iNoReset
	not	dx

	and	handledints,dx
	and	currentints,dx

r99iNoReset:
	pop	dx
	ret
	endp


;--------------------------------------------------------------------------
;	Latch 9901 timer output.
;
;	The timer goes at a fixed rate of 31250 Hz.  This timer will be
;	emulated by matching it with the PC's timer 0, which goes at
;	1193181 Hz.  Both the timers use the same sort of decrement-to-0
;	-and-interrupt technology so the conversion is relatively 
;	simple.  The 9901's timer is inverted from the 9900 bus, so
;	this will also be taken into account.
;
;	Since 1193181 is too large for a word, the ratio 31250/1193200,
;	or 625/23864, is used in the calculations.
;
;	Due to rollover of the PC timer, the "uptime" variable will
;	be used in conjunction with the latch of PC timer 0 to provide
;	an accurate count.
;
;	Due to the possible long time taken by this function, the timer
;	is latched only upon a read of the least significant bit.
;	
;--------------------------------------------------------------------------

	IFDEF	T386

	.386
latch9901 proc	near
	push	eax
	push	ebx
	push	ecx
	push	edx

	cli

	mov	al,40h
	out	43h,al			; latch timer
	in	al,40h
	mov	ah,al
	in	al,40h
	xchg	al,ah			; got timer 0 count
	and	eax,0ffffh
	neg	eax
	movzx	ebx,timer0interval
	add	ebx,eax			; get # ticks since rollover in EBX

	movzx	eax,timer0interval
	imul	eax,uptime		; figure # ticks since start

	add	eax,ebx			; carry indicates unrecoverable
					; 	overflow here
	imul	eax,625			; ratio up...
	mov	ebx,23864
	xor	edx,edx
	div	ebx			; ratio down.  

	movzx	ecx,clockhertz		; get modulator
	jcxz	l99skip			; uh... skip it

	xor	edx,edx
	div	ecx			; modulate
	mov	ax,dx			; get remainder as 9901 timer val

l99skip:
	not	ax			; invert
	mov	latchedtimer,ax		; save

	sti

	pop	edx
	pop	ecx
	pop	ebx
	pop	eax
	ret
	endp

	.286

	ELSE

latch9901 proc near
	push	ax
	mov	ax,latchedtimer
	inc	ax
	cmp	ax,timer0interval
	jb	l99out
	xor	ax,ax
l99out:
	mov	latchedtimer,ax
	pop	ax
	ret
	endp

	ENDIF



;	CL=# bits to get
;	AX=value to change (word)
;	DX=CRU addr
;
;	Returns AX=value!
;
readseveralCRU	proc	near
	push	di
	push	si
	push	bx
	push	dx

	and	dx,1ffeh
	cli

	mov	ch,cl				; CH = # bits to read
	cmp	ch,8
	ja	rsCWord
	mov	cl,8
	sub	cl,ch				; CL = #bits to SHR end result
	jmp	rsCStart
rsCWord:
	mov	cl,16
	sub	cl,ch
rsCStart:
	push	cx				; # end fix value

	lea	bx,CRURTable

	xor	ax,ax				; cosmetic

rsCLoop:
	mov	si,[bx]
	or	si,si				; end of table?  (-1)
	js	rsCDone				; went through all defined
						; bits
	cmp	si,dx				; same address?
	je	rsCRead
	jb	rsCWait

	add	dx,2
	shr	ax,1
	or	ax,8000h			; no CRU bit
	dec	ch
	jmp	rsCDoLoop			; look at entry again

rsCWait:
	add	bx,6				; size of entry
	jmp	rsCLoop

rsCRead:
	mov	cl,[bx+2]			; max # bits to do at once
	cmp	cl,ch
	jbe	rsCDoMulti

	add	bx,6				; skip entry
	jmp	rsCLoop				; try again

rsCDoMulti:
	shr	ax,cl				; make room for bits
	sub	ch,cl				; fix # bits left
						; ROUTINE WON'T REFERENCE DX
	call	[bx+4]				; call routine

	add	cl,cl				; -> address offset
	add	dl,cl
	adc	dh,0				; fix address 

	add	bx,6				; skip entry
;	jmp	rsCDoLoop

rsCDoLoop:
	or	ch,ch  				; all bits got?
	jnz	rsCLoop

rsCDone:
	pop	cx
	shr	ax,cl
	sti
	pop	dx
	pop	bx
	pop	si
	pop	di
	ret
	endp


;	CL=# bits to write
;	AX=value to change (word)
;	DX=CRU addr
;
writeseveralCRU	proc	near
	push	ax
	push	di
	push	si
	push	bx
	push	dx

	and	dx,1ffeh
	cli

	mov	ch,cl				; CH = # bits to write
	cmp	ch,8
	ja	wsCWord
	shr	ax,8				; put value in low byte
wsCWord:

	lea	bx,CRUWTable

wsCLoop:
	mov	si,[bx]
	or	si,si				; end of table?  (-1)
	js	wsCDone				; went through all defined
						; bits
	cmp	si,dx				; same address?
	je	wsCRead
	jb	wsCWait

	add	dx,2
	shr	ax,1
	dec	ch
	jmp	wsCDoLoop			; look again
wsCWait:
	add	bx,6				; size of entry
	jmp	wsCLoop

wsCRead:
	mov	cl,[bx+2]			; max # bits to do at once
	cmp	cl,ch
	jbe	wsCDoMulti

	add	bx,6				; skip entry
	jmp	wsCLoop				; try again

wsCDoMulti:
	sub	ch,cl				; fix # bits left
						; ROUTINE WON'T REFERENCE DX
	push	ax
	call	[bx+4]				; call routine
	pop	ax
	shr	ax,cl				; move bits up
	add	cl,cl				; -> address offset
	add	dl,cl
	adc	dh,0				; fix address 
	add	bx,6				; skip entry

wsCDoLoop:
	or	ch,ch  				; all bits got?
	jnz	wsCLoop

wsCDone:
	sti
	pop	dx
	pop	bx
	pop	si
	pop	di
	pop	ax
	ret
	endp




;	CRU BIT EMULATIONS
;
;	READ:  OR ax,8000h if ON
;
;	WRITE: shr ax,1;  jc zap to check
;


;------------------------------------------------
;	9901 timer program / interrupt enable bit
;
;	When 0, allow interrupts
;	When 1, we're programming the clock
;------------------------------------------------

W9901_0	proc	near
	mov	mode9901,0
	shr	ax,1
	adc	mode9901,0
	ret
	endp


;	Handle 14 bits in int9901 or clockinvl.
;
;	mode9901==0 ?	int9901
;		    :	clockinvl
;

W9901_A	proc	near
	push	bx

	mov	bx,ax
	and	bx,3fffh

	cmp	mode9901,0
	jnz	W99Aclock

	mov	int9901,bx			; sets all 14 bits
	call	handle9901			; interrupt state changed
	jmp	W99AOut

W99Aclock:
	mov	clockinvl,bx			; sets all 14 bits
						; ************** set clock
W99Aout:
	pop	bx
	ret
	endp


;	Change one bit in 9901
;
;	mode9901==0 ?	Disable/Enable an interrupt
;		    :	Bit in clock interval
;
W9901_S	proc	near
	push	bx
	push	cx
	push	dx

	mov	cx,dx				; get this CRU addr
	shr	cx,1				; bit addr
	dec	cx				; point to base

	lea	bx,clockinvl			; modify clock interval

	cmp	mode9901,0			; which mode?
	jnz	W99Schange
	
	lea	bx,int9901			; modify interrupt mask

W99Schange:
	mov	dx,1
	shl	dx,cl
	not	dx
	and	[bx],dx				; assume unset
	not	dx
	shr	ax,1
	jnc	W99SDone			; set?
	or	[bx],dx				; set bit

W99SDone:
	cmp	mode9901,0			; modified interrupt masks?
	jnz	W99SOut

	call	reset9901int			; try to reset interrupt
	call	int9901change			; notify change
	call	handle9901			; changed interrupt state

W99SOut:
	pop	dx
	pop	cx
	pop	bx
	ret
	endp


W9901_F	proc	near
	ret
	endp


;	Interrupt mask was changed, so change emulator-specific 
;	states.
;
;	Only the interval timer needs to react, as it is tied
;	to a program-slowing-down 80x86 interrupt.
;
;	CX = bit # (1-15)

int9901change proc near
	cmp	cx,2	 			; bit 2 = INT 3
	jne	i99cout

	xor	ax,ax	       			; turn off clock

	test	int9901,M_INT3			; off or on?
	jz	i99c03_set			; off

						; ******************
						; also reset interval counter

	call	figure9901clock
	mov	ax,clockhertz

;;	mov	ax,1000				; boing

	mov	currentfunc,offset doint3	; 9901 clock ticker

i99c03_set:
	call	setcurrentspeed			; set speed

i99cout:
	ret
	endp


;	Interrupt routine for INT3
;
;
doint3	proc near
	or	currentints,M_INT3
	call	handle9901
	ret
	endp



;	Calculate clockhertz from clockinvl.
;	
figure9901clock	proc	near
	push	ax
	push	dx

	mov	ax,clockinvl
	and	ax,3FFFh
	jnz	f99cok
      	inc	ax
f99cok:
	mov	clockinvl,ax

	xor	dx,dx
	mov	ax,31250
	div	clockinvl

	cmp	ax,maxclockhertz
	jbe	f99cnottoofast
	mov	ax,maxclockhertz
f99cnottoofast:

	mov	clockhertz,ax

	pop	dx
	pop	ax
	ret
	endp


;	Read ???
;
;
R9901_0	proc	near
	or	ax,8000h
	ret
	endp


;	Read INT1 status or lowest bit of timer.
;
;	mode9901==0 ? 	INT status
;		    :	timer value
;
R9901_1	proc	near
	cmp	mode9901,0		; 9901 or clock?
	jnz	R991Timer

	test	currentints,M_INT1	; 9901
	jnz	R991Out
	or	ax,8000h
	jmp	R991Out

R991Timer:
	call	latch9901		; lsb read => latch
	test	latchedtimer,1
	jz	R991Out
	
	or	ax,8000h		
	
R991Out:
	ret
	endp

	
;	Read INT2 status or second lowest bit of timer.
;
;	mode9901==0 ? 	INT status
;		    :	timer value
;
R9901_2	proc	near
	cmp	mode9901,0		; 9901 or clock?
	jnz	R992Timer

	push	ax
	mov	ax,currentints
	test	ax,M_INT1		; lower levels active?
	pop	ax
	jnz	R992Others

	test	currentints,M_INT2	; 9901
	jnz	R992Out

R992Others:
	or	ax,8000h
	jmp	R992Out

R992Timer:
	test	latchedtimer,2
	jz	R992Out

	or	ax,8000h
R992Out:
	ret
	endp
	

;	Bits 3 through 10 are used both by the 9901 timer and
;	the keyboard, depending on mode9901.
;



;	Read INT status or a keyboard bit.
;
;	mode9901==0 ? 	keyboard
;		    :	timer value

	.data

KeyRows dw	KeyR0,KeyR1,KeyR2,KeyR3,KeyR4,KeyR5,KeyR6,KeyR7

	.code

R9901_S	proc	near
	push	bx

	cmp	mode9901,0		; 9901 or clock?
	jnz	R99STimer

	mov	bx,dx			; get CRU addr
	shr	bx,1			; addr->bit
	neg	bx			; reverse
	add	bx,10			; CRU addr -> keyboard row
	add	bx,bx
	mov	bx,[KeyRows+bx]
	jmp	bx			; go to keyboard code

R99STimer:
	test	latchedtimer,4
	jz	R99SOut

	or	ax,8000h      
R99SOut:
	pop	bx
	ret
	endp

	
	
;--------------------------------------------------------------------------
;	READ all 8 keyboard bits or 8 timer bits.
;
R9901_K	proc	near
	push	bx
	push	cx

	cmp	mode9901,0
	jz	R99Keys

	mov	bx,latchedtimer
	shr	bx,3				; shift
	mov	ah,bl	
	jmp	R99KOut

R99Keys:
	cmp	KeyCol,6
	jb	KRdNormKey

	call	readjoysticks

KRdNormKey:
	mov	bl,KeyCol
	xor	bh,bh
	shl	bx,3
	add	bx,offset KeyScn

	mov	cl,8
	mov	ch,80h
KRdLoop:
	cmp	byte ptr [bx],0
	jnz	KRdOn
	or	ah,ch
KRdOn:
	inc	bx
	shr	ch,1
	dec	cl
	jg	KRdLoop


	cmp	AlphaLockLine,0
	jnz	KRd3On

	test	shift,s_capslock
	jnz	KRd3On
	or	ah,10h
KRd3On:

R99KOut:
	pop	cx
	pop	bx
	ret
	endp



GetKeyAddr	proc	near
	push	ax
	push	bx
	pop	ax
	mov	bl,KeyCol
	xor	bh,bh
	shl	bx,3
	add	bx,offset KeyScn
	add	bx,ax
	pop	ax
	ret
	endp



;	Process individual keyboard rows.
;	Called from R9901_S with BX pushed on the stack.
;
KeyR7:
	mov	bx,7
	jmp	KRow

KeyR6:
	mov	bx,6
	jmp	KRow

KeyR5:
	mov	bx,5
	jmp	KRow

KeyR4:
	mov	bx,4
	jmp	KRow

;	READ keyboard row 3 AND Alpha Lock!!
;
KeyR3:
	cmp	AlphaLockLine,0
	jnz	KR3Ok

	test	shift,s_capslock
	jnz	K3On
	jmp	K3Off

KR3Ok:
	mov	bx,3
	jmp	KRow
K3Off:
	or	ax,8000h
K3On:
	pop	bx
	ret


KeyR2:
	mov	bx,2
	jmp	KRow

KeyR1:
	mov	bx,1
	jmp	KRow

KeyR0:
	xor	bx,bx
	jmp	KRow
	
KRow:
	cmp	KeyCol,6
	jb	KRNormKey

	call	readjoysticks

KRNormKey:
	call	GetKeyAddr
	cmp	byte ptr [bx],0
	jnz	KROn
	or	ax,8000h
KROn:
	pop	bx
	ret
	endp


;	WRITE all 3 column bitz
;
KeyWrite proc	near
	and	ax,7
	mov	KeyCol,al
	ret
	endp


;	WRITE column number bit 2
;
KeyC2	proc	near
	and	KeyCol,not 1
	shr	ax,1
	jnc	KC2Off
	or	Keycol,1
KC2Off:
	ret
	endp

;	WRITE column number bit 1
;
KeyC1	proc	near
	and	KeyCol,not 2
	shr	ax,1
	jnc	KC1Off
	or	Keycol,2
KC1Off:
	ret
	endp

;	WRITE column number bit 0
;
KeyC0	proc	near
	and	KeyCol,not 4
	shr	ax,1
	jnc	KC0Off
	or	Keycol,4
KC0Off:

	ret
	endp

;-------------------------------------------------------------
;	Read highest four bits in timer, if mode9901==clock.
;-------------------------------------------------------------

R9901_L proc	near
	push	bx
	cmp	mode9901,0
	jz	R99LNothing

	mov	bx,latchedtimer
	shr	bx,11				; shift
	shl	bl,4
	or	ah,bl
	jmp	R99LOut

R99LNothing:
	or	ax,0f000h
R99LOut:
	pop	bx
	ret
	endp


;	READ alpha lock line
;
AlphaR	proc	near
	cmp	AlphaLockLine,0
	jz	AROn
	or	ax,8000h
AROn:
	ret
	endp


;	WRITE alpha lock line
;
AlphaW	proc	near
	mov	AlphaLockLine,0
	shr	ax,1
	jnc	AWOff
	mov	AlphaLockLine,1
AWOff:
	ret
	endp


;	WRITE Audio gate
;
AudioW	proc	near
CassetteW:
	shr	ax,1
	jc	AuWOff
	mov	ah,0
	jmp	AuWSet
AuWOff:
	mov	ah,2
AuWSet:
	in	al,61h
	and	al,not 2
	or	al,ah
	out	61h,al
	ret
	endp




;========================================================================


;	DODRAWLED --	Draw an LED thingie in the corner of the screen
;			when DSR ROM goes on or off
;
;	Inputs:		CH=on color, CL=off color, BX=^onflag, DI=row
;			AX=ROM toggle bit
dodrawled proc	near
	push	ax

	cmp	byte ptr [bx],0
	jz	ddlnot

	test	ax,1
	jnz	ddlon
	xchg	cl,ch
ddlon:
	mov	ah,ch
	call	drawled
	
ddlnot:
	pop	ax
	ret
	endp


;	WRITE to DISK DSR ROM bit
;
DSKROM	proc	near
	push	bx
	push	cx
	push	si
	push	di
	push	es

	test	features,FE_realdisk
	jz	DRnoled

	mov	cx,0a00h		; BH="on", BL=off, CX=var, DI=row
	lea	bx,diskled
	mov	di,21
	call	dodrawled

DRnoled:
	shr	ax,1
	jc	DROn

DRclear:
	call	DSRclear

	mov	diskromon,0		; not on
	mov	usediskdsr,0
	mov	dsrwritebyte,offset nulldsrwrite
	mov	dsrreadbyte,offset nulldsrread

	jmp	DROut

DROn:
	mov	es,cpuseg
	mov	ax,dsrseg_realdisk
	or	ax,ax
	jz	DRclear			; whoops!
	push	ds
	mov	ds,ax
	call	DSRfill
	pop	ds
	mov	diskromon,1
	mov	usediskdsr,1		; using REAL DISK DSR
	mov	dsrwritebyte,offset dskdsrwrite
	mov	dsrreadbyte,offset dskdsrread

DROut:
	pop	es
	pop	di
	pop	si
	pop	cx
	pop	bx
	ret
	endp



;	WRITE to emulated DISK DSR ROM bit
;
emuDSKROM	proc	near
	push	bx
	push	cx
	push	si
	push	di
	push	es

	test	features,FE_emudisk
	jz	edrnoled

	mov	cx,0300h		; CH="on", CL=off, BX=var, DI=row
	lea	bx,emudiskled
	mov	di,22
	call	dodrawled

edrnoled:
	shr	ax,1
	jc	eDROn

edrclear:
	call	DSRclear

	mov	diskromon,0		; not on
	mov	usediskdsr,0
	jmp	eDROut

eDROn:
	mov	es,cpuseg
	mov	ax,dsrseg_emudisk
	or	ax,ax
	jz	eDRclear			; whoops!
	push	ds
	mov	ds,ax
	call	DSRfill
	pop	ds

	mov	diskromon,1
	mov	usediskdsr,0
eDROut:
	mov	dsrwritebyte,offset nulldsrwrite
	mov	dsrreadbyte,offset nulldsrread

	pop	es
	pop	di
	pop	si
	pop	cx
	pop	bx
	ret
	endp



;	WRITE to RS232 DSR ROM bit
;
RS232ROM proc	near
	push	bx
	push	cx
	push	si
	push	di
	push	es

	test	features,FE_realrs232+FE_emurs232
	jz	RRnoled

	mov	cx,0a00h		; CH="on", CL=off, BX=var, DI=row
	lea	bx,rs232led
	mov	di,23
	call	dodrawled

RRnoled:
	mov	diskromon,0
	mov	usediskdsr,0

	shr	ax,1
	jc	RROn

RRclear:
	call	DSRclear

	mov	dsrwritebyte,offset nulldsrwrite
	mov	dsrreadbyte,offset nulldsrread

	jmp	RRout

RROn:
	mov	es,cpuseg
	mov	ax,dsrseg_realrs232
	test	features,FE_emurs232
	jz	RRNotEmu
	mov	ax,dsrseg_emurs232

RRNotEmu:
	or	ax,ax
	jz	RRclear				; whoops!
	push	ds
	mov	ds,ax
	call	DSRfill
	pop	ds

	mov	dsrwritebyte,offset rs232dsrwrite
	mov	dsrreadbyte,offset rs232dsrread

RRout:
	pop	es
	pop	di
	pop	si
	pop	cx
	pop	bx
	ret
	endp


DSRfill	proc	near
	push	si
	push	di
	push	cx
	
	xor	si,si
	mov	di,4000h
	mov	cx,8192/2
	rep	movsw

	pop	cx
	pop	di
	pop	si
	ret
	endp




DSRclear proc	near
	push	di
	push	ax
	push	cx

	xor	ax,ax
	mov	di,4000h
	mov	cx,8192/2
	rep	stosw

	pop	cx
	pop	ax
	pop	di
	ret
	endp


;	Set disk ready/hold
;
dDSKhold proc	near
	shr	ax,1
	jc	DSKhon
	
	mov	DSKhold,0
	call	DSKholdoff
	jmp	DSKhout

DSKhon:
	mov	DSKhold,1
DSKhout:
	ret
	endp


;	Load disk heads
;
;
DSKheads proc	near
	ret
	endp


;	Select drive X
;
DSKsel1 proc	near
DSKsel2:
DSKsel3:
	push	cx
	mov	cx,dx
	sub	cx,1106h
	shr	cx,1	  		; CX=drive # (1-3)

	shr	ax,1
	jc	DSKson			; turn on drive?
	
	cmp	cl,DSKnum
	jne	DSKsout			; geez... who cares?
	call	closedisk		; turn off drive
	mov	DSKnum,0		; no drive selected
	jmp	DSKsout

DSKson:				      	; turn on drive... already on?
	cmp	cl,DSKnum	      	
	je	DSKsopen
	call	closedisk
DSKsopen:
	mov	DSKnum,cl
	call	opendisk
DSKsout:
	pop	cx
	ret
	endp


;	Is drive X connected?
;
DSKget1 proc	near
DSKget2:
DSKget3:

;
;	yes, we do have these drives
;
;	(setting the bit means it's absent)
;
	ret
	endp


;	Select side of disk
;
dDSKside proc	near
	mov	DSKside,0
	shr	ax,1
	jnc	DSKsdout
	inc	DSKside
DSKsdout:
	call	seektotrack
	ret
	endp


Bogus	proc	near
	ret
	endp



 	include	rs232.inc


	end
