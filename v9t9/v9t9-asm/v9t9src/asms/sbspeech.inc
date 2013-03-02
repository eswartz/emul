;   sbspeech.inc
; 
;   (c) 1991-2012 Edward Swartz
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