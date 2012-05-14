; ********************************************
; ADLIB.INC include file for Adlib 
; ********************************************
; by Edward Swartz  1993-1995
; ********************************************


;	ADLIB.INC
;	
;
;	Routines for controlling the FM chips in Adlib/SoundBlaster cards
;

	.data


;
;	ADLIB variables
;

ins11		db	0,1,2,6,7,8,12,16,14,17,13
voiceoffs	db	0,1,2,3,4,5,8,9,10,11,12,13,16,17,18,19,20,21

scalers		dw	66,131,262,523,1047,2094,4188,8376

noisevoice	equ	3
speechvoice	equ	4

	.code

;	DETECT_ADLIB --	Detect if Adlib card is installed
;
;	Returns AL=1 if installed
;
detect_adlib    proc    near
	push	cx
	push    dx

	mov	bx,0100h		; clear test reg
	call	writefm
	mov	bx,0460h		; disable counter
	call	writefm
	mov	bx,0480h		; clear status reg
	call	writefm

	call	readfm			; get status
	mov	ah,al

	mov	bx,02ffh		; set counter 1 at 255
	call	writefm
	mov	bx,0421h		; start counter
	call	writefm

	mov	cx,200
	call	addelay

	call	readfm
	
	mov	bx,0460h		; disable counter
	call	writefm
	mov	bx,0480h		; clear status reg
	call	writefm

	and	ah,0e0h
	jnz	danot

	and	al,0e0h
	cmp	al,0c0h
	jnz	danot

dahere:
       	mov	al,1
	jmp	daout
danot:
	xor	al,al
daout:
	pop     dx
	pop	cx
	ret
	endp





;	ADDELAY --	Create a delay of CX cycles
;
;	Destroys CX
;
addelay	proc    near
	push    dx
	push	cx
	push    ax
	mov     dx,388h
addloop:
	in      al,dx
	loop   	addloop
	pop     ax
	pop	cx
	pop     dx
	ret
	endp


;	WRITEFM --	Send a command to the Adlib's FM chips
;	
;	BH = index
;	BL = data
;
writefm proc	near
	push	ax
        push    cx
	push	dx

        mov     al,bh
	mov	dx,388h
        out     dx,al
        mov     cx,6
        call    addelay
        mov	dx,389h
        mov     al,bl
        out     dx,al
        mov     cx,35
        call    addelay

	pop	dx
        pop     cx
	pop	ax
	ret
	endp



;	READFM --	Read data from Adlib's FM chips
;	
;	Returns	AL=data
;
readfm proc	near
	push	dx

	mov	dx,388h
	in	al,dx

	pop	dx
	ret
	endp


;	SETVOICE1 --	Set an operator for a voice
;
;	AH=voice #
;	VALUE must not be BH!
;
setvoice1 macro	prt,vlue
	local	not
	cmp	ah,8
	jae	not
	push	bx
	mov	bl,vlue
	mov	bh,prt
	push	ax
	push	bx
	push	di

	add	bh,ah
	call	writefm 

	pop	di
	pop	bx
	pop	ax


	pop	bx
not:
	endm


;	SETVOICE --	Set both operators for a voice
;
;	AH=voice #
;	VALUE must not be BH!
;
setvoice macro	prt,vlue
	push	bx
	mov	bh,prt
	mov	bl,vlue
	push	ax
	push	di

	shr	ax,8
	lea	di,ins11
	add	di,ax
	mov	al,[di]

	xor	ah,ah
	lea	di,voiceoffs
	add	di,ax

	push	bx
	add	bh,[di]
	call	writefm 
	pop	bx

	add	bh,[di+3]
	call	writefm 

	pop	di
	pop	ax

	pop	bx
	endm



;	CLEANADLIB --	Shutdown.
;
;
cleanadlib	proc    near
	call	wipeadlib

	and	soundcard,not adlibmask
	and	playsound,not adlibmask
	ret
	endp



;	WIPEADLIB --	Clear the Adlib card completely
;
;
wipeadlib	proc    near
	push	ax
	push	cx
	push	dx

	mov	bh,1 			; index
	xor	bl,bl			; data
waloop:
	call	writefm
	inc	bh
	cmp	bh,0f5h
	jbe	waloop

	pop	dx
	pop	cx
	pop	ax
	ret
	endp




;	CLEARADLIB --	Clear the playing voices
;
clearadlib	proc	near
	push	ax
	push	bx
	push	si

	mov	ah,0
caloop:
	setvoice1 0a0h,0
	setvoice1 0b0h,0
	inc	ah
	cmp	ah,4
	jb	caloop

	pop	si
	pop	bx
	pop	ax
	ret
	endp




;	INITADLIB --	Initialize Adlib ports to TI defaults.
;
;	Square waves, loud, no decay, etc...
;
initadlib proc	near
	push	ax
	push	bx

	call	wipeadlib
	
	mov	bx,0120h 		; waveform will be changed
	call	writefm
	mov	bx,0800h
	call	writefm
	mov	bx,0bd00h
	call	writefm

	mov	ah,0
iavoiceinit:
	setvoice 20h,02h		; set modulator's multiple to 2

	setvoice 40h,000h		; set volume to maximum,
	
	setvoice 60h,0f0h		; set attack:quick, decay:long
	
	setvoice 80h,0ffh		; long sustain, long release

	setvoice1 0a0h,00h		; set frequency LSB to 0
	setvoice1 0b0h,20h		; turn off voice

	setvoice1 0c0h,000001b		; select complex sounds

	setvoice 0e0h,03h		; select square wave

ianot:
	inc	ah
	cmp	ah,3
	jae	ianoiseinit
	jmp	iavoiceinit

ianoiseinit:
	mov	ah,noisevoice
	setvoice 20h,00h
	setvoice 40h,000h
	setvoice 60h,0f0h
	setvoice 80h,0ffh
	setvoice1 0a0h,00h
	setvoice1 0b0h,20h
	setvoice1 0c0h,00000b
	setvoice 0e0h,01h

iaout:
	pop	bx
	pop	ax
	ret
	endp


;	Set the tone of a voice (AH) to BX hertz.
;
;
adlibhertz	proc	near
	push	di
	push	ax
	push	bx
	push	cx
	inc	bx
	cmp	bx,110
	jae	ahvalid
	jmp	ahinvalid
ahvalid:
	cmp	bx,130
	ja	ahnot0
	mov	si,0
	jmp	ahcalc
ahnot0:
	cmp	bx,261
	ja	ahnot1
	mov	si,1
	jmp	ahcalc
ahnot1:
	cmp	bx,522
	ja	ahnot2
	mov	si,2
	jmp	ahcalc
ahnot2:
	cmp	bx,1046
	ja	ahnot3
	mov	si,3
	jmp	ahcalc
ahnot3:
	cmp	bx,2093
	ja	ahnot4
	mov	si,4
	jmp	ahcalc
ahnot4:
	cmp	bx,4187
	ja	ahnot5
	mov	si,5
	jmp	ahcalc
ahnot5:
	cmp	bx,8375
	ja	ahnot6
	mov	si,6
	jmp	ahcalc
ahnot6:
	cmp	bx,16363
	ja	ahinvalid
	mov	si,7
	jmp	ahcalc

ahinvalid:
	setvoice1	0b0h,0
	setvoice1	0a0h,0		; out of range, so turn off voice
	mov	[di].hertz,0
	jmp	ahout

ahcalc:
	push	di
	add	si,si
	mov	di,[scalers+si]
	sub	bx,di			; put all values in one octav

	push	ax			; save voice #

	mov	ax,bx			; figure out ADLIB value
	mov	cx,343
	mul	cx
	div	di
	add	ax,343
	and	ax,03ffh		
	mov	bx,ax


	pop	ax
	pop	di

	setvoice1	0a0h,bl

	add	si,si
	mov	cx,si
	add	cl,bh

	mov	[di].octave,cl		; save octave byte

	cmp	[di].volume,0fh
	je	ahnovol

	or	cl,20h 			; key-on bit

ahnovol:
	setvoice1	0b0h,cl

ahout:
	pop	cx
	pop	bx
	pop	ax
	pop	di
	ret
	endp




;	Set volume for voice AH to AL (0-63)
;
adlibsetvol proc	near
	push	ax
	push	bx
	push	cx

	mov	cl,[bx].octave		; get octave byte
	or	cl,20h			; assume it'll be on

	cmp	[bx].hertz,0		; too-high pitch?
	je	asvunset     		; turn off key-on, but set volume
	
	cmp	al,3fh
	jne	asvset

asvunset:
	and	cl,not 20h

asvset:
	setvoice 40h,al
	setvoice1 0b0h,cl

asvout:
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	Set an Adlib voice to the proper frequency
;
adlibtone proc	near
	push	ax
	push	bx
	push	di
	mov	di,bx			; adlibhertz needs voicex
	mov	bx,[bx].hertz
	call	adlibhertz
	pop	di
	pop	bx
	pop	ax
	ret
	endp



;	Set a voice's volume (called from HANDLESOUND)
;
;
adlibvol proc	near
	push	ax
	mov	al,[bx].volume
	shl	al,2
	add	al,3
	call	adlibsetvol
	pop	ax
	ret
	endp



;	Do Adlib noise.
;
;	Only periodic-type noise is emulated here.
;
;
adlibnoise proc	near
	push	ax
	push	bx
	push	dx
	push	di

	mov	ah,noisevoice

	test	voicen.stype,4
	jnz	adnwhite

adnper:
	push	ax
	mov	ax,voicen.hertz
	mov	bx,8
	mul	bx
	mov	bx,15
	div	bx
	mov	bx,ax
	pop	ax	 
	lea	di,voicen
	call	adlibhertz

	jmp	adnout

adnwhite:
	mov	al,63
	mov	ah,noisevoice
	lea	bx,voicen
	call	adlibsetvol

adnout:
	pop	di
	pop	dx
	pop	bx
	pop	ax
	ret
	endp


;	Set volume for Adlib noise.
;
;
adlibnoisevol proc near
	push	ax

	test	voicen.stype,4
	jnz	anvsetnone		; white noise

	mov	al,voicen.volume
	cmp	al,15
	je	anvsetnone
	shl	al,2
	jmp	anvcont
anvsetnone:
	mov	al,63
anvcont:
	mov	ah,noisevoice
	call	adlibsetvol
	pop	ax
	ret
	endp


;	The segment of code below is disabled. 
;
;	I'd hoped to emulate speech on the Adlib too, but I couldn't
;	figure out how to make a voice "click."

	.data

adlibspeech spchstruc	<adspinit,adsaybyte,adspshut,adsayonebyte>

advoice	voiceinfo	<334,334,15,20h>	
adlast	db	0

	.code

adspinit proc 	near
	pusha

	mov	ah,speechvoice
	setvoice 20h,00h
	setvoice 40h,0			; not-quite-off
	setvoice 60h,0f0h
	setvoice 80h,0ffh
	setvoice1 0a0h,0h
	setvoice1 0b0h,37h
	setvoice1 0c0h,00000b
	setvoice 0e0h,00h

	popa
	ret
	endp



adspshut proc	near
	pusha

	mov	ah,speechvoice
	setvoice1 0b0h,0

	popa
	ret
	endp


adsayonebyte proc near
	ret
	endp


adsaybyte proc near
	push	ax
	mov	al,20h
	out	20h,al
	pop	ax
	iret
	endp


	comment	\
adsaybyte proc	near
	pusha
	push	ds
	push	es

	mov	ax,@data
	mov	ds,ax
	mov	es,saddr
	mov	bx,soffs
	mov	al,es:[bx]
	or	al,al
	mov	ah,0
	jns	adsb0
	neg	al
	mov	ah,1
adsb0:
;	cmp	ah,adlast
;	je	adsb1

;	setvoice1 0b0h,0
;	setvoice1 0b0h,3ch
;	xor	al,7fh

adsb1:
;	mov	adlast,ah

	shr	al,1
	mov	ah,speechvoice
	setvoice 40h,al

;	setvoice1 0b0h,3ch
;	setvoice1 0b0h,20h

	inc	soffs
	dec	slen

	mov	al,20h
	out	20h,al

	pop	es
	pop	ds
	popa
	iret
	endp
	\


	endp
