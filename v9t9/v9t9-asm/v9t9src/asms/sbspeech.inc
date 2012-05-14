; ***********************************************
; SPEECHSB.INC  V9t9 SoundBlaster speech routines
; ***********************************************
; by Edward Swartz.  6/1996
; ***********************************************


;---------------------------------------------------------------------
;	Speak a sample to the SB via DMA.
;---------------------------------------------------------------------

sbdmaspeech proc near
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
sds0:
	pop	si
	pop	ds
	pop	cx

	mov	speechrec.sampleoffs,0
	mov	speechrec.sampleseg,es
	mov	speechrec.sampleleng,cx
	mov	speechrec.samplespeed,speechSBspeed

	or	LPCstat,LPC_audioing

	lea	si,speechrec
	call	sbdmaplay

	pop	es
	pop	di
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;------------------------------------------------------------------------
;	SBSPEECHSAMPLEEND	
;
;	Speech segment has finished audioing
;
;	Called from interrupt.
;	DMAcurr=0, DMAgoing=0, nothing playing
;
;
;	This routine SHOULD start the next sample.
;
;

sbspeechsampleend proc near

	jmp	speechintr

	ret
	endp





;----------------------------------------------------------------------
;	SBspeechabort
;
;	Done with speech and the speaker.
;
;	Let noise play.
;----------------------------------------------------------------------

sbspeechabort	proc near

	call	sbdmaabort

	mov	speechactive,0
	cmp	noiseactive,0
	jz	sbsaout

	call	sbnoiseplay			; turn on noise again

sbsaout:
	ret
	endp






