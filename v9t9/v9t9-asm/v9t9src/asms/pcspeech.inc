
; ***********************************************
; PCSPEECH.INC  V9t9 PC speaker speech routines
; ***********************************************
; by Edward Swartz.  6/1996
; ***********************************************


;---------------------------------------------------------------------
;	Speak a sample to the PC speaker via a horrible interrupt.
;---------------------------------------------------------------------


pcspeechplaysample proc near
	push	ax
	push	bx
	push	cx
	push	si
	push	di
	push	es

	mov	si,lpcoutbuffer
	mov	cx,[si].spchleng

	mov	playinbuffer.spchleng,cx


	push	cx
	push	ds

	mov	es,playinbuffer.spchseg
	mov	ds,[si].spchseg

	push	si
	xor	si,si
	xor	di,di


IFDEF	T386
	shr	cx,2
	.386
	rep	movsd
	.286
ELSE
	shr	cx,1
	rep	movsw
ENDIF
	pop	si
	pop	ds
	pop	cx

	mov	speechrec.sampleoffs,0
	mov	speechrec.sampleseg,es
	mov	speechrec.sampleleng,cx
	mov	speechrec.samplespeed,speechSBspeed

	or	LPCstat,LPC_audioing

	mov	currentfunc,offset pcspeakbyte
	mov	ax,speechspeed
	call	setcurrentspeed

	pop	es
	pop	di
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;------------------------------------------------------------------------
;	PCSPEECHSAMPLEEND	
;
;	Speech segment has finished audioing
;
;	Called from currentfunct.
;
;	This routine SHOULD start the next sample.
;

pcspeechsampleend proc near
	push	ax
	xor	ax,ax
	call	setcurrentspeed
	pop	ax

	jmp	speechintr

	endp





;----------------------------------------------------------------------
;	PCspeechabort
;
;	Done with the speaker.
;
;----------------------------------------------------------------------

pcspeechabort	proc near
	push	ax
	xor	ax,ax
	call	setcurrentspeed

	mov	speechactive,0
	pop	ax
	ret
	endp



;	Called 8000 times a second to speak one byte to the PC speaker.
;
;	Please, don't let your children grow up to write high-rate
;	interrupts.
;

	.data

pcprev	db	80h			; previous byte played
pclast	db	80h			; last byte played

	even

	.code

pcspeakbyte proc near
	push	ax
	push	bx
	push	es

	mov	bx,speechrec.sampleoffs
	cmp	bx,speechrec.sampleleng
	jb	psbmore

;	Done...

	call	pcspeechsampleend
	jmp	psbout

psbmore:
	mov	es,speechrec.sampleseg
	mov	al,es:[bx]
	inc	speechrec.sampleoffs

	and	al,0f0h
	mov	es:[bx],al

	mov	ah,pclast
	sub	ah,pcprev

	sub	al,pclast

	xor	ah,al
	jns	psbnochange		; jns = jump if no sign change

	in	al,61h
	xor	al,2
	out	61h,al

psbnochange:
	mov	al,pclast
	mov	pcprev,al
	mov	al,es:[bx]
	
	mov	pclast,al

psbout:
	pop	es
	pop	bx
	pop	ax
	ret
	endp
