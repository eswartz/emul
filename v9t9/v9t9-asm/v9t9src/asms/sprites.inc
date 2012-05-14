; ************************************************
; SPRITES.INC  TI Emulator sprite drawing routines
; ************************************************
; 6/1993-2/19/1995 by Edward Swartz
; ************************************************

	.code

;************************************************************************
;	Go through changes, charchanges, spritechanges, spritecharchanges
;	and completely define CHANGES
;

gcoordchanges	proc	near
	
;	Patterns & colors

	lea	di,changes
	mov	si,screen

	xor	ax,ax
	mov	cx,screensize

gcchloop1:
	mov	al,[si]
	mov	bx,ax
	test	byte ptr [bx+charchanges],0ffh
	jz	gcchnope
	mov	byte ptr [di],0ffh
gcchnope:
	inc	si
	inc	di
	loop	gcchloop1


;	Sprites

	call	handlespritechanges

	ret
	endp


;************************************
;	Global sprite change routine
;
;	Nothing is preserved
;

handlespritechanges	proc	near

;	Sprite patterns

	mov	ax,ds
	mov	es,ax
	mov	cx,32/2
	xor	ax,ax
	lea	di,spritestodraw
	rep	stosw

	cmp	check5sprites,0
	je	hscdontcheck
	call	setaschanged5thsprites
	call	check5spritesonaline
	jmp	hscskipfake
hscdontcheck:
	call	fakecheck
hscskipfake:

	lea	di,spritechanges
	mov	si,sprites

	mov	bl,spritechars
	neg	bl			; mask for comparisons

	mov	cx,32
	xor	ah,ah
hschloop3:
	mov	al,[si+2]		; pattern
	and	al,bl			; mask for correct pattern
	mov	bp,ax

	mov	bh,byte ptr ds:[bp+spritecharchanges]
	cmp	bl,0fch
	jne	hschnotbigcmp

	or	bh,byte ptr ds:[bp+spritecharchanges+1]
	or	bh,byte ptr ds:[bp+spritecharchanges+2]
	or	bh,byte ptr ds:[bp+spritecharchanges+3]
hschnotbigcmp:
	or	bh,bh
	jz	hschnope3

	mov	byte ptr [di],0ffh
hschnope3:
	add	si,4
	inc	di
	loop	hschloop3


	call	comparesprites			; sets SPRITESTODRAW
						; and disqualifies clear
						; sprites

;	Go through unchanged sprites, and if any of their
;	chars are changed, set this sprite as changed, too.

	mov	si,sprites
	lea	di,spritechanges
	lea	bx,spritestodraw
	mov	cx,32
hschloop4:
	cmp	si,deleted
	jae	hschnope4

	cmp	byte ptr [BX],0			; changed?  
	jne	hschloopit			; already handled
	cmp	byte ptr [DI],0			; changed?  
	jne	hschloopit			; already handled

	call	checkspritelocs			; returns C=1 if changed
			
	jnc	hschnope4
	mov	byte ptr [bx],0ffh	; draw it!
	jmp	hschloopit

hschnope4:

hschloopit:
	inc	di
	inc	bx
	add	si,4
	loop	hschloop4


	cmp	checkspritecoinc,0
	jz	hscnocheck
	call	checkcoinc
hscnocheck:
	call	losecopies
	ret
	endp

	
;	Figure out if any sprites coincide.
;
;	Ignore sprites off bottom of screen, unlike E/A manual says.
;
;	The actual TI doesn't look at offscreen sprites.
;
checkcoinc	proc	near
	push	ax
	push	bx
	push	cx
	push	dx
	push	si
	push	di

	mov	bl,totspritesize
	shl	bl,3
	xor	bh,bh
	mov	si,sprites
	mov	ch,31
ccloop1:
	mov	al,[si]
	cmp	al,0d0h			; end of SI list?
	je	ccout			; well, this is the major, so leave
	inc	al
	cmp	al,192
	jae	ccdonedi 		; >=192 doesn't count, skip all these

	mov	di,si
	add	di,4
	mov	cl,32
ccloop2:
	mov	al,[di]
	cmp	al,0d0h
	je	ccdonedi		; end of DI list, so leave this loop
	inc	al
	cmp	al,192
	jae	ccskip			; >=192 doesn't count

	xor	ah,ah
	mov	al,[si]
	mov	dl,[di]
	xor	dh,dh
	sub	ax,dx
	jns	ccloop22
	neg	ax
ccloop22:
	cmp	ax,bx
	jge	ccskip

	xor	ah,ah
	mov	al,[si+1]
	xor	dh,dh
	mov	dl,[di+1]
	sub	ax,dx
	jns	ccloop23
	neg	ax
ccloop23:
	cmp	ax,bx
	jl	ccgotit
ccskip:
	add	di,4
	dec	cl
	jg	ccloop2
ccdonedi:
	add	si,4
	dec	ch
	jg	ccloop1
	jmp	ccout
ccgotit:
	or	vdpstat,coincidenceflag
ccout:
	pop	di
	pop	si
	pop	dx
	pop	cx
	pop	bx
	pop	ax

	ret
	endp


;	Lose copies of sprites which are sitting on top
;	of each other
;

losecopies	proc	near
	push	si
	push	di
	push	bp
	push	bx
	push	cx
	push	ax

	lea	si,spritestodraw
	mov	cl,32
	mov	bx,sprites
lcloop:
	cmp	byte ptr [si],0
	jz	lcskip

	mov	di,4			; init 'next sprite in table' offs
	mov	bp,1 			; init 'next spritetodraw' offs
	mov	ch,cl			
	dec	ch			; # sprites 'under' this one

lccomp:
	mov	ax,[bx]
	cmp	ax,[bx+di]
	jne	lcnot			; different at all?
	mov	ax,[bx+2]
	cmp	ax,[bx+di+2]		; hmm?
	jne	lcnot

	mov	byte ptr ds:[si+bp],0	; yup... clear this identical sprite

lcnot:
	inc	bp
	add	di,4
	dec	ch
	jg	lccomp

lcskip:
	add	bx,4
	inc	si
	dec	cl
	jg	lcloop

	pop	ax
	pop	cx
	pop	bx
	pop	bp
	pop	di
	pop	si
	ret
	endp


;	Get the current deleted sprite ptr
;
getdeletedptr	proc	near
	mov	si,sprites
	mov	cx,32
gdploop:
	mov	al,[si]
	cmp	al,0d0h
	je	gdpgotit
	add	si,4
	loop	gdploop
gdpgotit:
	mov	deleted,si
	ret
	endp


;	Get the old deleted sprite ptr
;
getolddeletedptr	proc	near
	lea	si,oldsprites
	mov	cx,32
godploop:
	mov	al,[si]
	cmp	al,0d0h
	je	godpgotit
	add	si,4
	loop	godploop
godpgotit:
	mov	olddeleted,si
	ret
	endp


switch	macro	a,b
	mov	al,a
	xchg	b,al
	mov	a,al
	endm

copy	macro	a,b
	mov	al,b
	mov	a,al
	endm


;	COMPARESPRITES:  point is to track deleted sprite ptr
;	and set spritestodraw accordingly.
;
;
;	Nothing is preserved
;
comparesprites	proc	near

;	Spritestodraw is all zeroes originally

	call	getolddeletedptr
	call	getdeletedptr


;	Update locations changed by movement of oldsprites.
;
;	Look at all sprites up to the old deleted sprite pointer.
;	If the sprite is currently changed, set its old locations.
;	If the sprite is past the current deleted sprite pointer,
;	update it too because it needs to be overwritten.

	switch	spritesize,ospritesize
	switch	spritemag,ospritemag
	switch	spritechars,ospritechars
	switch	totspritesize,ototspritesize

	lea	si,oldsprites
	lea	di,spritechanges
	mov	bx,sprites		; only for comp
	mov	cx,32
csploop00:
	cmp	si,olddeleted
	jae	csploop00out

	cmp	bx,deleted
	jae	cspdoset00		; pos's changed to CHARS!

	cmp	byte ptr [di],0
	je	cspdoloop00
	mov	al,[si+3]
	and	al,0fh
	jz	cspdoloop00		; don't consider clear sprite

cspdoset00:
	call	setspritelocs

cspdoloop00:
	add	si,4
	add	bx,4
	inc	di
	loop	csploop00
csploop00out:


;	Now go through current sprites.  If different, set locs.
;	Also set spritestodraw.

	copy	spritesize,ospritesize
	copy	spritemag,ospritemag
	copy	spritechars,ospritechars
	copy	totspritesize,ototspritesize
	
	lea	bp,oldsprites
	mov	si,sprites
	lea	di,spritechanges
	lea	bx,spritestodraw
csploop01:
	cmp	si,deleted
	jae	csploop01out

	cmp	bp,olddeleted
	jae	csploopdo		; undeleted!

	cmp	byte ptr [di],0
	je	cspdoloop01

csploopdo:
	call	setspritelocs
	mov	byte ptr [bx],0ffh

cspdoloop01:
	add	si,4
	add	bp,4
	inc	bx
	inc	di
	jmp	csploop01

csploop01out:



	ret
	endp


;	SETSPRITELOCS:
;	SI = offset into sprite tables
;
;	Sets CHANGES for all the positions covered by a sprite.
;

setspritelocs	proc	near
	push	bx
	lea	bx,scaset		; set CHANGES for all legal locs
	call	spritecharaction	; SI = sprite table ptr
	pop	bx
	ret
	endp


;	From SPRITECHARACTION:
;		SCASET : set CHANGED at coords
;
;	SI = char offset
;	BX = pixels offset
;	DI = sprite table
;
;	Save everything changed

scaset	proc	near
	mov	byte ptr [si+changes],0feh
	clc
	ret
	endp


;       CHECKSPRITELOCS:
;	SI = sprite table ptr
;	SAVE SI, DI, and CX
;
;
;	If other chars are set in CHANGES overlapping this unchanged
;	sprite, return C=1 to change it, too

checkspritelocs	proc	near
	push	bx
	lea	bx,scacheck
	call	spritecharaction
	pop	bx
	ret
	endp



;	From SPRITECHARACTION:
;		SCACHECK : check if CHANGED is set at coords
;
;	SI = char offset

scacheck proc	near
	push	ax
	mov	al,[si+changes]
	add	al,0ffh			; will set C=1 if AL<>0
	pop	ax
	ret
	endp



;	SET5SPRITELOCS:
;	SI = offset into sprite tables
;	Only set chars that intersect row in ROW5

set5spritelocs	proc	near
	push	bx
	lea	bx,sca5set		; set CHANGES for all legal locs
	call	spritecharaction	; SI = sprite table ptr
	pop	bx
	ret
	endp


;	From SPRITECHARACTION:
;		SCA5SET : set CHANGED at coords
;
;	SI = char offset
;	BX = pixels offset
;	DI = sprite table
;
;	Save everything changed

sca5set	proc	near
	cmp	bl,row5
	jne	sca5nope
	mov	byte ptr [si+changes],0feh
sca5nope:
	clc
	ret
	endp




;	Big-ass generic procedure which will do something
;	for all the chars affected by a sprite
;	SI = sprite
;	BX = procedure to call
;	Save everything
;
;	Stops checking all coords when C=1
spritecharaction	proc	near
	pusha

	mov	bp,bx		; BP is the real procedure to call

;	Get coords

	mov	bl,[si]		; bl=y
	inc	bl

	mov	bh,[si+1]	; bh=x
	
;	BL, BH both are coords
;	The values will always remain byte-sized.

;	Get size  (CH=X, CL=Y)

	mov	cl,totspritesize
	test	bl,7
	jz	scaloopy
	inc	cl		; # rows, 1-5
scaloopy:
	push	bx
	mov	ch,totspritesize
	test	bh,7
	jz	scaloopx
	inc	ch		; # cols, 1-5
scaloopx:
;	Test legality!

	cmp	bl,192
	jae	scaskipcheck  	; don't draw below bottom of screen

	mov	al,bl
	shr	al,3		; byte offs (ROW)
	xor	ah,ah		; AX=row (0-23)
	shl	ax,5		; AX=char position (row,0) (0-767)

	mov	dl,bh		; copy x
	test	byte ptr [si+3],80h
	jz	scanoclock
	sub	dl,32	  	; adjust for early clock
scanoclock:
	shr	dl,3		; column (0-31)
	xor	dh,dh		; into word
	add	ax,dx		; AX=char position (row,col)

	mov	di,si		; save sprite ptr
	mov	si,ax		; SI is screen char pos (0-767)

;	SI = char offset
;	BX = byte offset (adjusted for early clock)

	push	bx		; save "real" value
	test	byte ptr [di+3],80h
	jz	scanosendclock
	sub	bh,32
scanosendclock:
	call	bp
	pop	bx
	jc	scaterm		; stop?
	mov	si,di		; restore sprite ptr

scaskipcheck:
	add	bh,8
	dec	ch
	jnz	scaloopx

	pop	bx		; get org coords
	add	bl,8
	dec	cl
	jnz	scaloopy
	clc
	jmp	scaout
scaterm:
	pop	bx
scaout:
	popa
	ret
	endp






;	FAKECHECK
;
;	Fakes a CHECK5ONALINE
;
fakecheck	proc	near
	push	ax
	push	cx
	push	di

	push	es
	mov	ax,@data
	mov	es,ax

	xor	ax,ax

	lea	di,spritenums
	mov	cx,256/2
	rep	stosw			; clear out SPRITENUMS
	lea	di,fifthsprites
	dec	ax
	mov	cx,256/2
	rep	stosw
	pop	es
	pop	di
	pop	cx
	pop	ax
	ret
	endp




setaschanged5thsprites proc	near
	push	bx
	push	cx
	push	ax

	mov	cx,32
	xor	bx,bx
sac5sloop:
	mov	al,[bx+obliterated]
	or	[bx+spritechanges],al
	inc	bx
	loop	sac5sloop

	pop	ax
	pop	cx
	pop	bx
	ret
	endp
	







;	This procedure will set the FIFTHSPRITES
;	and SPRITENUMS with information about all the
;	active sprites.
;
check5spritesonaline	proc	near
	pusha
	push	es
	push	ds
	pop	es

	mov	cx,256/2
	mov	ax,-1
	lea	di,fifthsprites
	rep	stosw			; set FIFTHSPRITES to be none

	mov	ax,0505h
	lea	di,spritenums		
	mov	cx,256/2
	rep	stosw			; # visible sprites+1 left on a row

	xor	ax,ax
	lea	di,obliterated
	mov	cx,32/2
	rep	stosw

	pop	es

	mov	si,sprites		; point to sprite table
	mov	ch,32
	xor	ah,ah			; sprite #
sspbone:
	cmp	si,deleted
	jae	sspbout

	mov	bl,[si]
	inc	bl			; row

	xor	bh,bh

	mov	di,ax
	shr	di,8

	mov	cl,totspritesize
	shl	cl,3
sspbrows:
	mov	al,[spritenums+bx]
	or	al,al
	jz	sspbsetchange		; already got 5?
	dec	al
	jnz	sspbnot			; the fifth?

	mov	[fifthsprites+bx],ah	; the fifth sprite on this line
	mov	[spritechanges+di],0ffh	

sspbnot:
	mov	[spritenums+bx],al 	; reset # sprites left on this row
	jmp	sspbskiprow

sspbsetchange:
	mov	[spritechanges+di],0ffh

sspbskiprow:
	inc	bl
sspbnotop:
	dec	cl
	jg	sspbrows

	add	si,4
	inc	ah
	dec	ch
	jg	sspbone

sspbout:
	popa
	ret
	endp



;*********************************************************************
;	DRAWSPRITES
;
;

drawsprites	proc	near


	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax
	mov	al,8
	out	dx,al
	inc	dx
	mov	es,graphseg

;	1)  CHANGES has info about which chars are different, i.e.,
;	    what will be affected by a sprite at any time.
;	2)  SPRITESTODRAW reflects intersections.
;	4)  All the 5th+ sprites on a line have been erased in part
;	    by the CHECK5SPRITESONALINE procedure.

;	So, do this:
;	For each sprite to be drawn, set up a LEGALROWS buffer
;	(size 192 bytes, but only TOTSPRITESIZE*8 elements filled in)
;	that DRAWSPRITE will look at.

	mov	si,deleted
	mov	bx,si
	sub	bx,sprites
	shr	bx,2
	mov	dx,bx				; DX = # of sprite
dsploop:
	sub	si,4
	dec	bx
	dec	dx
	cmp	si,sprites
	jl	dspout

	test	byte ptr [bx+spritestodraw],0ffh
	jz	dspnot

	mov	al,[si]
	inc	al
	xor	ah,ah


	lea	bp,fifthsprites
	lea	di,legalrows
	add	bp,ax
	add	di,ax
	mov	ah,totspritesize
	shl	ah,3
	
dspsetrows:
	mov	byte ptr [di],0ffh		; sure, draw...
	cmp	dl,ds:[bp]
	jb	dspokay
	mov	byte ptr [di],0			; wait, this row's out...
	mov	byte ptr [bx+obliterated],0ffh	; this one was obliterated
dspokay:
	inc	al
	jnz	dspnottop
	sub	di,256
	sub	bp,256
dspnottop:
	inc	di
	inc	bp
	dec	ah
	jg	dspsetrows


	call	drawsprite
dspnot:
	jmp	dsploop

dspout:

	ret
	endp


;	Draw the sprite on page #1.
;	SI = sprite data
;	Preserve BX, CX, DX, BP and SI.
drawsprite	proc	near
	push	bx
	push	cx
	push	dx
	push	si
	push	bp

	;	figure screen address for sprite and shift count.
	test	byte ptr [si+3],15
	jnz	dsnotblank
	jmp	dsnotonscreen
dsnotblank:
	xor	bh,bh
	mov	bl,[si]
	inc	bl
	cmp	bl,192
	jb	dsonscreen
	cmp	bl,224
	jae	dsonscreen
	jmp	dsnotonscreen
dsonscreen:
	mov	bp,bx
	add	bp,offset legalrows	; BP points to legal rows
	mov	cl,[si+1]
	xor	ch,ch
	test	byte ptr [si+3],128	; early clock on?
	jz	dsnoclock
	sub	cx,32
dsnoclock:
	mov	di,cx
	sar	di,3			; get byte #
	mov	ax,rowwidth
	mul	bx
	add	ax,di
	add	ax,screen1
	mov	di,ax			; DI = screen address
	mov	bl,[si+2]		; BH=0 already
	cmp	spritechars,4
	jne	dsnotbig
	and	bl,not 3		; big sprites start on 4-char boundaries
dsnotbig:
	shl	bx,3
	add	bx,sprpat
	mov	ax,bx			; AX = temp pattern address
	mov	bl,[si+3]
	and	bl,15			; BL = color
	and	cx,7			; CL = shift count
	mov	si,ax			; SI = pattern
	mov	dx,3cfh
	cmp	spritemag,2
	je	dsbigadjust

;	Small size sprite.
;

	cmp	spritesize,8
	jne	ds16x16
	call	spritechar8		; 8x8
	jmp	dsnotonscreen
ds16x16:
	call	spritechar16		; 16x16
	jmp	dsnotonscreen

dsbigadjust:
	cmp	spritesize,8
	jne	ds32x32
	call	spritemag8		; 8x8 == 16x16
	jmp	dsnotonscreen
ds32x32:
	call	spritemag16		; 16x16 == 32x32

dsnotonscreen:
	pop	bp
	pop	si
	pop	dx
	pop	cx
	pop	bx
	ret
drawsprite	endp


;	Draw an 8x8 sprite character
;	SI = pattern
;	DI = EGA address
;	BL = color
;	CL = shift right
;	BP = legalrows
;
;	Preserve BH & DX & BP
;	Returns DI as bottom of sprite
spritechar8	proc	near
	push	bx
	push	bp
	mov	dsccount,8
sc8loop:
	cmp	byte ptr ds:[bp],0
	jz	sc8skipblank
	mov	ah,[si]
	or	ah,ah
	jz	sc8skipblank
	xor	al,al
	shr	ax,cl			; AH=left byte, AL=right byte
	out	dx,al
	mov	bh,es:[di+1]		; load regs
	mov	es:[di+1],bl		; set RIGHT BYTE
	xchg	al,ah
	out	dx,al
	mov	bh,es:[di]
	mov	es:[di],bl		; set LEFT BYTE
sc8skipblank:
	add	di,rowwidth		; next row
	cmp	di,256*rowwidth+screen1
	jb	sc8notop
	sub	di,256*rowwidth
	sub	bp,256
sc8notop:
	inc	bp
	inc	si			; next byte
	dec	dsccount
	jg	sc8loop

	pop	bp
	pop	bx

	ret
	endp




;	Draw a 16x?? sprite character
;	SI = pattern
;	DI = EGA address
;	BL = color
;	CL = shift right
;	BP = legalrows
;
;	Preserve BH & DX & BP
;	Returns DI as bottom of sprite
spritechar16	proc	near
	push	bx
	push	bp
	mov	dsccount,16
sc16loop:
	cmp	byte ptr ds:[bp],0
	jz	sc16skipblank
	mov	ah,[si]
	mov	al,[si+16]
	or	ax,ax
	jz	sc16skipleftblank
	shr	ax,cl			; AH=left byte, AL=right byte
	out	dx,al
	mov	bh,es:[di+1]		; load regs
	mov	es:[di+1],bl		; set RIGHT BYTE, LEFT of right byte
	xchg	al,ah
	out	dx,al
	mov	bh,es:[di]
	mov	es:[di],bl		; set LEFT BYTE, left
sc16skipleftblank:
	mov	ah,[si+16]
	or	ah,ah
	jz	sc16skipblank
	xor	al,al
	shr	ax,cl			; AH=left byte, AL=right byte
	out	dx,al
	mov	bh,es:[di+2]
	mov	es:[di+2],bl		; set LEFT BYTE, right
sc16skipblank:
	add	di,rowwidth		; next row
	cmp	di,256*rowwidth+screen1
	jb	sc16notop
	sub	di,256*rowwidth
	sub	bp,256
sc16notop:
	inc	bp
	inc	si			; next byte
	dec	dsccount
	jg	sc16loop

	pop	bp
	pop	bx

	ret
	endp


;	Draw a 16x16 magnified 8x8 sprite character
;	SI = pattern
;	DI = EGA address
;	BL = color
;	CL = shift right
;	BP = legalrows
;
;	Preserve BH & DX & BP
;	Returns DI as bottom of sprite

spritemag8	proc	near
	push	bx
	push	dx
	push	bp
	push	si

	push	bx
	push	si
	push	di
	lea	di,magpatt
	mov	ch,8
sm8row:
	mov	ah,[si]

	xor	bh,bh
	mov	bl,ah
	and	bl,15			; get RIGHT NYBBLE
	mov	al,[magconvert+bx]
	mov	bl,ah
	shr	bl,4			; get LEFT NYBBLE
	mov	ah,[magconvert+bx]

	mov	[di],ah
	mov	[di+16],al
	mov	[di+1],ah
	mov	[di+17],al		; magpatt will hold full 16x16 patt
	add	di,2
	inc	si
	dec	ch
	jg	sm8row

	pop	di
	pop	si
	lea	si,magpatt
	pop	bx

	call	spritechar16

	pop	si
	pop	bp
	pop	dx
	pop	bx
	ret
	endp



;	Draw a 32x32 magnified 16x16 sprite character
;	SI = pattern
;	DI = EGA address
;	BL = color
;	CL = shift right
;	BP = legalrows
;
;	Preserve BH & DX & BP
;	Returns DI as bottom of sprite
spritemag16	proc	near
	push	di
	push	si
	push	bp

	push	di
	call	spritemag8
	add	si,8
	add	bp,16
	cmp	bp,offset legalrows+256
	jb	sm160
      	sub	bp,256
sm160:
	call	spritemag8
	pop	di
	add	di,2
	add	si,8
	sub	bp,16
	cmp	bp,offset legalrows
	jae	sm161
      	add	bp,256
sm161:
	call	spritemag8
	add	si,8
	add	bp,16
	cmp	bp,offset legalrows+256
	jb	sm162
      	sub	bp,256
sm162:
	call	spritemag8

	pop	bp
	pop	si
	pop	di
	ret
	endp




;========================================================================
;	Optimization of 99/4A ROM routine.
;
;	Automatic sprite motion.
;========================================================================

movesprites	proc	near
	push	es
	pusha

	mov	bx,780h
	mov	si,300h
	READBYTE 837Ah,cl

	xor	ch,ch
msploop:
	jcxz	mspout

	mov	ah,[si]	 		; y
	mov	al,[bx+2]		; y-little
	mov	dh,[bx]			; y-mot
	xor	dl,dl
	sar	dx,4			; scale >> 16x
	add	ax,dx
	cmp	ax,0c0ffh
	jb	msp1
	cmp	ax,0e000h
	ja	msp1
	cmp	dx,0
	jg	msp01
	add	ah,0c0h
msp01:
	add	ah,020h

msp1:
	mov	temploc,ah
	mov	tempmot+2,al

	mov	ah,[si+1]
	mov	al,[bx+3]
	mov	dh,[bx+1]
	xor	dl,dl
	sar	dx,4
	add	ax,dx
	
	mov	temploc+1,ah
	mov	tempmot+3,al

	mov	vaddr,si
	mov	al,temploc
	call	handlevdp
	inc	vaddr
	mov	al,temploc+1
	call	handlevdp
	mov	vaddr,bx
	add	vaddr,2
	mov	al,tempmot+2
	call	handlevdp
	inc	vaddr
	mov	al,tempmot+3
	call	handlevdp

	add	si,4
	add	bx,4
	dec	cx
	jmp	msploop
mspout:
	popa
	pop	es
	mov	ip,09e8h		; exit
	ret
	endp
