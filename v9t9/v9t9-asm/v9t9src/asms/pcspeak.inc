; ********************************************
; PCSPEAK.INC include file for PC speaker
; ********************************************
; by Edward Swartz  1995
; ********************************************

;	PCSPEAK.INC
;
;
;	Routines for controlling the PC speaker
;

	.data

pcspkon	db	0
pcspeakersilence db 15
pclasthertz dw 	0

	.code



;	Play a tone of AX hertz through PC speaker
;	
pchertz	proc	near
	push	ax
	push	dx
	push	bx
	mov	pclasthertz,ax
	mov	bx,ax
	mov	ax,34ddh
	mov	dx,0012h
	cmp	dx,bx
	jnb	phoverflow
	div	bx
	mov	bx,ax
	mov	al,010110110b
	out	43h,al
;;	in	al,61h
;;	or	al,3
;;	out	61h,al
ph1:
	mov	al,bl
	out	42h,al
	mov	al,bh
	out	42h,al
phoverflow:
	pop	bx
	pop	dx
	pop	ax
	ret
	endp


pcspeakerspeech proc	near
	ret
	endp


pcspeakernoise proc	near
	ret
	endp


;	PCSOUNDOFF --	Turn off sound to PC speaker
;


pcsoundoff	proc	near
	push	ax
	in	al,61h
	and	al,0fch
	out	61h,al
	pop	ax
	mov	pcspkon,0
	ret
	endp


;	PCSOUNDON --	Turn on sound to PC speaker
;
pcsoundon	proc	near
	push	ax
	in	al,61h
	and	al,0fch
	or	al,3
	out	61h,al
	pop	ax
	mov	pcspkon,1
	ret
	endp



initpcspeaker proc	near
	call	pcsoundoff
	ret
	endp


clearpcspeaker proc	near
	call	pcsoundoff
	ret
	endp

cleanpcspeaker proc	near
	call	pcsoundoff
	and	playsound,not pcspeakermask
	and	soundcard,not pcspeakermask
	ret
	endp



;	Set the TIMER 2 to the proper frequency
;
;	BX==> voiceinfo, AH=voice
;
pcspeakertone	proc	near
	ret
	endp





	push	ax

;	cmp	ah,0
;	jne	pstno
	mov	pvoice,ah
	mov	ax,[bx].hertz
	call	pchertz
pstno:
	pop	ax
	ret
	endp


;	If volume<>0fh, then turn on speaker, else turn it off.
;	(Proper frequency should be loaded.)
;
pcspeakervol	proc	near
	ret
	endp

	push	ax
;	mov	al,voice1.volume
;	add	al,voice2.volume
;	add	al,voice3.volume
;	cmp	al,15*3
;	je	psvoff0
;	cmp	pcspkon,0
;	jnz	psvout0
;	call	pcsoundon
;	jmp	psvout0
;psvoff0:
;	cmp	pcspkon,0
;	jz	psvout0
;	call	pcsoundoff
;psvout0:
;	pop	ax
;	ret
	


	push	ax
;	cmp	ah,0
;	jne	psvno
	cmp	ah,pvoice
	jne	psvno
	mov	al,[bx].volume
	cmp	al,0fh
	je	psvoff
	cmp	pcspkon,0
	jnz	psvno
	call	pcsoundon
	jmp	psvout
psvoff:
	cmp	pcspkon,0
	jz	psvno
	call	pcsoundoff
psvout:
psvno:
	pop	ax
	ret
	endp


	.data

pcspeech spchstruc <pcspinit,pcsaybyte,pcspshutdown,pcsayonebyte>

	.code


pcspinit proc	near
	call	pcsoundoff
	clc
	ret
	endp


pcspshutdown proc near
	cmp	pcspkon,0
	jz	pcspsdout
	call	pcsoundon
pcspsdout:
	ret
	endp


	.data

lastbyte	db	0

	.code

;	Say one byte.  AH=value.
;
;
;
pcsayonebyte proc near
	push	ax
	mov	al,ah
	and	al,0f0h

	mov	ah,lastbyte
	mov	lastbyte,al

	cmp	al,80h
	jae	pcsbtestdown
	cmp	ah,80h
	jb	pcsbout
	jmp	pcsbon

pcsbtestdown:
	cmp	ah,80h
	jae	pcsbout
	jmp	pcsbon

pcsbon:
	in 	al,061h
	xor 	al,02
	out 	061h,al
;;	xor	al,02
;;	out	61h,al

pcsbout:
	pop	ax
	ret
	endp


pcsaybyte proc	near
	push	ax
	mov	al,20h
	out	20h,al
	pop	ax
	iret
	endp


	comment	\
pcsaybyte proc	near
	push	ax
	push	ds
	push	es
	push	bx

	mov	ax,@data
	mov	ds,ax
	mov	bx,soffs
	mov	es,saddr
	mov	ah,es:[bx]

	call	pcsayonebyte

	dec	slen
	inc	soffs

	mov	al,20h
	out	20h,al

	pop	bx
	pop	es
	pop	ds
	pop	ax
	iret
	endp
	\



	.data

pvoice	db	0


	.code

pctoggle proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	cmp	queueing,0		; speech going?
	jnz	pctout			; whoops!

	mov	ah,pvoice
	mov	cx,3			; tries left
pctloop:
	xchg	ax,bx
	mov	al,size voiceinfo
	mul	bh
	add	ax,offset voice1
	xchg	ax,bx
	mov	dl,pcspeakersilence
	cmp	[bx].volume,dl
	jae	pctnot
	push	ax
	mov	ax,[bx].hertz
	cmp	ax,pclasthertz
	je	pctnope
	call	pchertz
pctnope:
	pop	ax
	cmp	pcspkon,0
	jnz	pctsave
	call	pcsoundon
pctsave:
	inc	ah
	cmp	ah,3
	jb	pctsave0
	xor	ah,ah
pctsave0:
	mov	pvoice,ah

	jmp	pctout
pctnot:
	inc	ah
	cmp	ah,3
	jb	pctloop0
	xor	ah,ah
pctloop0:
	loop	pctloop
pctoff:
	call	pcsoundoff
pctout:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp






