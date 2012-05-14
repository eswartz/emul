
; ***************************************
; GRTEXT.ASM  V9t9 text-only update routines
; ***************************************
; by Edward Swartz  2/1996
; ***************************************


textonlyhandler proc near
	clc
	ret
	endp


;===========================================================================

;	Graphics mode text emulation.
;

;	(1)  Take the changes from the patterns and apply them.
;	(2)  Change characters on the screen.
;

textgraphicsscreen proc near

	call	mapcharchanges

	call	updatepatterns

	mov	es,textseg
	mov	di,8			; start at top, 4 cols in

	lea	bx,changes		; point to 32x24 screen o' changes
	mov	cx,screensize
	mov	si,screen
	mov	dx,screenwidth

tgschar:
	mov	al,[bx]
	or	al,al
	jz	tgsnext

	push	bx

	mov	al,[si]
	mov	es:[di],al		; put char there
	xor	ah,ah
	shr	ax,3
	
	mov	bx,ax
	add	bx,colors		; get offset
	mov	al,[bx]			; get color

	mov	ah,al
	shr	ax,4			; swap nybbles

	mov	es:[di+1],al

	pop	bx

tgsnext:
	inc	si
	add	di,2
	inc	bx
	dec	dx
	jz	tgsnextrow
	loop	tgschar
	jmp	tgsoutloop

tgsnextrow:
	add	di,16			; next row
	mov	dx,screenwidth
	loop	tgschar

tgsoutloop:
	call	gcleanup
	ret
	endp



textonlyscreen proc near

	call	mapcharchanges

	call	updatepatterns

	mov	es,textseg
	xor	di,di			; start at top, 4 cols in

	lea	bx,changes		; point to 32x24 screen o' changes
	mov	cx,screensize
	mov	si,screen
	mov	dx,screenwidth

toschar:
	mov	al,[bx]
	or	al,al
	jz	tosnext

	push	bx

	mov	al,[si]
	mov	es:[di],al		; put char there

	mov	al,textbg
	shl	al,4
	or	al,0fh			; always

	mov	es:[di+1],al

	pop	bx

tosnext:
	inc	si
	add	di,2
	inc	bx
	dec	dx
	jz	tosnextrow
	loop	toschar
	jmp	tosoutloop

tosnextrow:
	mov	dx,screenwidth
	loop	toschar

tosoutloop:
	call	gcleanup
	ret
	endp



;	Map "charchanges" to the "changes" array.
;
;
mapcharchanges proc near
	mov	si,screen
	lea	di,changes
	mov	cx,screensize
mccloop:
	xor	bh,bh
	mov	bl,[si]
	cmp	byte ptr [charchanges+bx],0
	jz	mccnext
	mov	byte ptr [di],0ffh
mccnext:
	inc	si
	inc	di
	loop	mccloop
	ret
	endp



;	Update text patterns.
;
;
updatepatterns proc near
	pusha
	push	es

	mov	si,screen
	mov	cx,screensize
uploop:
	xor	bh,bh
	mov	bl,[si]
	cmp	byte ptr [charchanges+bx],0
	jnz	updo
upnext:
	inc	si
	loop	uploop
	jmp	upout

updo:
	mov	ax,1110h
	mov	bx,0800h
	mov	cx,256
	xor	dx,dx
	push	ds
	pop	es
	mov	bp,patterns
	int	10h

	mov	dx,3d4h
	mov	al,9
	out	dx,al
	inc	dx
	in	al,dx
	or	al,80h
	out	dx,al

upout:
	pop	es
	popa

	ret
	endp


drawled	proc	near
	ret
	endp

drawrs232led	proc	near
	ret
	endp


drawphrase proc near
	ret
	endp

erasephrase proc near
	ret
	endp




;	Update pattern in CHANGES
;
graphicsonlypattern	proc	near
	mov	bx,vaddr
	sub	bx,patterns
	shr	bx,3			; BL=char # that changed
	or	byte ptr [charchanges+bx],080h

	ret
	endp


;	Update color in CHANGES
;
graphicsonlycolor	proc	near
	mov	bx,vaddr
	sub	bx,colors
	shl	bx,3			; BX=low char to change

	or	word ptr [charchanges+bx],07f7fh
	or	word ptr [charchanges+bx+2],07f7fh
	or	word ptr [charchanges+bx+4],07f7fh
	or	word ptr [charchanges+bx+6],07f7fh

	ret
	endp	




;	Update text pattern in CHANGES
;
textonlypattern	proc	near
	mov	bx,vaddr
	sub	bx,patterns
	shr	bx,3			; BL=char # that changed
	or	BYTE PTR [charCHANGES+BX],080H
	ret
	endp
