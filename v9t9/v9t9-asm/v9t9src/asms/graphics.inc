; ****************************************
; GRAPHICS.INC  V9t9 video routines
; ****************************************
; 6/1993-2/17/1995 by Edward Swartz
; ****************************************


;	Routines to redraw the screen for all four screen modes.
;
;	SPRITES.INC updates the sprites themselves.
;
;

	.code



;	Draw graphics-mode screen (24x32).
;	Each char represents a 8x8 pixel block on the screen.
;	Every 8 chars has a unique color (#0-#7, ... #248-#255).
graphicsscreen	proc	near

	call	gcoordchanges

	mov	dx,3ceh
	mov	ax,205h

	out	dx,ax			; set WM 2
	mov	dx,32			; 32 columns per row
	mov	di,screen1
					; first byte on screen
	mov	es,graphseg
	lea	bx,changes
	mov	cx,screensize
	mov	si,screen		; SI is VDP address
gsgraphics:				; DRAW GRAPHICS SCREEN
	mov	al,[bx]
	or	al,al
	jz	gsgnext

	push	bx
					; draw char
	mov	al,[si]			; al = char to draw
	sub	bh,bh
	mov	bl,al
	shl	bx,3
	add	bx,patterns		; BX = ptr to pattern table entry
	push	bx
	mov	bl,al
	sub	bh,bh
	shr	bl,3			; BL = color offset
	add	bx,colors
	mov	bl,[bx]			; BL = color from table

	mov	al,bl
	mov	ah,bl
	and	al,15
	shr	ah,4

	pop	bx			; BX = pattern address
	call	drawgraphicschar
	pop	bx

gsgnext:	
	inc	si
	inc	bx
	inc	di			; next cell
	dec	dx
	jz	gsgnextrow
	loop	gsgraphics
	jmp	gsgoutloop
gsgnextrow:
	add	di,(rowwidth-32)+rowwidth*8-rowwidth
					; skip sides, 4 on left & 4 on right,
					; and 8 rows (320 bytes)
					; minus the row it just entered
	mov	dx,32
	loop	gsgraphics
gsgoutloop:
	cld

;	Now do the SPRITES.

	call	drawsprites

	call	updateseenscreen
	
	call	gcleanup
	ret
	endp




;	Draw one character on the screen
;	DI = screen address
;	BX = ptr to pattern table entry for char
;	AL = BG color
;	AH = FG color
drawgraphicschar	proc	near
	push	cx
	push	dx

	mov	cx,ax			; save colors
	mov	dx,3ceh
	mov	ax,0ff08h
	out	dx,ax

	mov	ah,es:[di]
	mov	es:[di],cl		; BG color
	mov	ah,es:[di+rowwidth*1]
	mov	es:[di+rowwidth*1],cl
	mov	ah,es:[di+rowwidth*2]
	mov	es:[di+rowwidth*2],cl
	mov	ah,es:[di+rowwidth*3]
	mov	es:[di+rowwidth*3],cl
	mov	ah,es:[di+rowwidth*4]
	mov	es:[di+rowwidth*4],cl
	mov	ah,es:[di+rowwidth*5]
	mov	es:[di+rowwidth*5],cl
	mov	ah,es:[di+rowwidth*6]
	mov	es:[di+rowwidth*6],cl
	mov	ah,es:[di+rowwidth*7]
	mov	es:[di+rowwidth*7],cl

	inc	dx

	mov	cl,8
	mov	ah,ch

	mov	al,[bx]
	out	dx,al
	mov	al,es:[di]
	mov	es:[di],ah		; send FG color

	mov	al,[bx+1]
	out	dx,al
	mov	al,es:[di+rowwidth*1]
	mov	es:[di+rowwidth*1],ah
		
	mov	al,[bx+2]
	out	dx,al
	mov	al,es:[di+rowwidth*2]
	mov	es:[di+rowwidth*2],ah		

	mov	al,[bx+3]
	out	dx,al
	mov	al,es:[di+rowwidth*3]
	mov	es:[di+rowwidth*3],ah		

	mov	al,[bx+4]
	out	dx,al
	mov	al,es:[di+rowwidth*4]
	mov	es:[di+rowwidth*4],ah		

	mov	al,[bx+5]
	out	dx,al
	mov	al,es:[di+rowwidth*5]
	mov	es:[di+rowwidth*5],ah		

	mov	al,[bx+6]
	out	dx,al
	mov	al,es:[di+rowwidth*6]
	mov	es:[di+rowwidth*6],ah		

	mov	al,[bx+7]
	out	dx,al
	mov	al,es:[di+rowwidth*7]
	mov	es:[di+rowwidth*7],ah		

	pop	dx
	pop	cx
	ret
drawgraphicschar	endp


;=========================================================================


IFDEF	T386

;	Draw text-mode screen (40x24).
;	Each char in the screen image table is drawn 6x8 on the screen.
;	There are only two colors.
;
;	A group of 4 6x8 chars is drawn at once now.  (24x8)
;
textscreen	proc	near

	mov	bp,screen
	xor	di,di
	mov	cx,960
	xor	bh,bh
tscheckchanged:
	mov	bl,ds:[di+bp]
	cmp	byte ptr [bx+charchanges],0
	jz	tsnochange
	mov	byte ptr [changes+di],0ffh
tsnochange:
	inc	di
	loop	tscheckchanged

	mov	ax,ds
	mov	es,ax
	lea	di,charchanges
	xor	ax,ax
	mov	cx,256/2
	rep	stosw

	mov	es,graphseg
	lea	bx,changes
	mov	si,screen		; offset to screen table
	mov	cx,960/4

	mov	di,screen0+1
tsrow:
	mov	dx,40/4
tseachchar:
	mov	ax,[bx]
	or	ax,[bx+2]
	jz	tsnext

	xor	ax,ax
	mov	[bx],ax
	mov	[bx+2],ax

	call	drawfour
tsnext:
	add	di,3
	add	si,4
	add	bx,4
	dec	cx
	jz	tsout
	dec	dx
	jnz	tseachchar
	add	di,rowwidth*7+10
	jmp	tsrow
tsout:
	mov	dx,3ceh
	mov	ax,0205h
	out	dx,ax

	mov	dx,3c4h
	mov	ax,0f02h
	out	dx,ax

	ret
	endp


;	Draw four 6x8 characters side-by-side.
;
;	SI-> screen, [DI] -> addr
;

	.386

drawfour proc near
	push	bp
	push	bx
	push	cx
	push	dx
	push	di

	mov	dx,3c4h
	mov	al,2
	mov	ah,15
	out	dx,ax


	mov	dx,3ceh
	mov	ax,0205h
	out	dx,ax

	mov	ax,0ff08h
	out	dx,ax

	mov	al,textbg

	mov	cx,8
	push	di
dfclear:
	mov	es:[di],al
	mov	es:[di+1],al
	mov	es:[di+2],al
	add	di,rowwidth
	loop	dfclear
	pop	di

	mov	dx,3ceh
	mov	ax,005h
	out	dx,ax

	mov	dx,3c4h
	mov	al,2
	mov	ah,0fh			; always use color 15 to draw
	out	dx,ax

	movzx	ebp,patterns
	mov	cx,8

d4rowloop:

	xor	eax,eax

	movzx	bx,[si]			; get first character
	lea	ebx,[ebx*8+ebp]
	mov	dl,[bx]
	and	dl,0fch
	or	al,dl
	shl	eax,6

	movzx	bx,[si+1]		; get second character
	lea	ebx,[ebx*8+ebp]
	mov	dl,[bx]
	and	dl,0fch
	or	al,dl
	shl	eax,6

	movzx	bx,[si+2]		; get third character
	lea	ebx,[ebx*8+ebp]
	mov	dl,[bx]
	and	dl,0fch
	or	al,dl
	shl	eax,6

	movzx	bx,[si+3]		; get fourth character
	lea	ebx,[ebx*8+ebp]
	mov	dl,[bx]
	and	dl,0fch
	or	al,dl

	shr	eax,2

	mov	bl,es:[di+2]
	mov	es:[di+2],al
	mov	bl,es:[di+1]
	mov	es:[di+1],ah
	shr	eax,16
	mov	bl,es:[di]
	mov	es:[di],al

	inc	bp
	add	di,rowwidth
	dec	cx
	jcxz	d4out
	jmp	d4rowloop


d4out:	
	pop	di
	pop	dx
	pop	cx
	pop	bx
	pop	bp

	ret
	endp

	.286

ELSE

;	Draw text-mode screen (40x24).
;	Each char in the screen image table is drawn 6x8 on the screen.
;	There are only two colors.
textscreen	proc	near

	mov	bp,screen
	xor	di,di
	mov	cx,960
	xor	bh,bh
tscheckchanged:
	mov	bl,ds:[di+bp]
	cmp	byte ptr [bx+charchanges],0
	jz	tsnochange
	mov	byte ptr [changes+di],0ffh
tsnochange:
	inc	di
	loop	tscheckchanged

	mov	ax,ds
	mov	es,ax
	lea	di,charchanges
	xor	ax,ax
	mov	cx,256/2
	rep	stosw

	mov	dx,3ceh
	mov	ax,0005h
	out	dx,ax

	mov	dx,3c4h
	mov	ax,0f02h
	out	dx,ax

	mov	es,graphseg
	lea	di,textaddrs
	lea	bx,changes
	mov	si,screen		; offset to screen table
	mov	cx,960

tseachchar:
	cmp	byte ptr [bx],0
	je	tsnext

	mov	byte ptr [bx],0
	push 	bx
	mov	bl,[si]
	sub	bh,bh
	shl	bx,3			
	add	bx,patterns		; BX=offset to pattern
	push	si
	mov	si,bx
	push	cx
	mov	cl,[di+2]
	push	di
	mov	di,[di]
	call	drawtextchar
	pop	di
	pop	cx
	pop	si
	pop	bx
tsnext:
	add	di,3
	inc	si
	inc	bx
	loop	tseachchar

	mov	dx,3ceh
	mov	ax,0205h
	out	dx,ax

	ret
	endp

;	Draw a text-mode char
;	SI=pattern table offset
;	CL=shift value
;	DI=screen offset
;
drawtextchar	proc	near
	mov	dx,3ceh
	mov	al,8
	out	dx,al
	inc	dx

	mov	ax,0fch
	ror	ax,cl

	mov	bh,[si]
	and	bh,0fch
	xor	bl,bl
	shr	bx,cl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di+1]
	mov	es:[di+1],bl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di]
	mov	es:[di],bh



	lea	di,[di+rowwidth]
	mov	bh,[si+1]
	and	bh,0fch
	xor	bl,bl
	shr	bx,cl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di+1]
	mov	es:[di+1],bl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di]
	mov	es:[di],bh


	lea	di,[di+rowwidth]
	mov	bh,[si+2]
	and	bh,0fch
	xor	bl,bl
	shr	bx,cl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di+1]
	mov	es:[di+1],bl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di]
	mov	es:[di],bh

	lea	di,[di+rowwidth]
	mov	bh,[si+3]
	and	bh,0fch
	xor	bl,bl
	shr	bx,cl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di+1]
	mov	es:[di+1],bl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di]
	mov	es:[di],bh


	lea	di,[di+rowwidth]
	mov	bh,[si+4]
	and	bh,0fch
	xor	bl,bl
	shr	bx,cl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di+1]
	mov	es:[di+1],bl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di]
	mov	es:[di],bh


	lea	di,[di+rowwidth]
	mov	bh,[si+5]
	and	bh,0fch
	xor	bl,bl
	shr	bx,cl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di+1]
	mov	es:[di+1],bl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di]
	mov	es:[di],bh


	lea	di,[di+rowwidth]
	mov	bh,[si+6]
	and	bh,0fch
	xor	bl,bl
	shr	bx,cl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di+1]
	mov	es:[di+1],bl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di]
	mov	es:[di],bh


	lea	di,[di+rowwidth]
	mov	bh,[si+7]
	and	bh,0fch
	xor	bl,bl
	shr	bx,cl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di+1]
	mov	es:[di+1],bl

	xchg	al,ah
	out	dx,al
	mov	ch,es:[di]
	mov	es:[di],bh


	ret
	endp

ENDIF


;=========================================================================

;	Draw multicolor screen (64x48).
;	Each byte in the pattern table represents two
;	consecutive blocks on the screen.
multiscreen	proc	near

	mov	di,screen
	mov	cx,screensize
	lea	si,changes
	xor	ah,ah
mscheck:
	mov	al,[di]
	mov	bx,ax
	test	byte ptr [charchanges+bx],0ffh
	jz	msnotincluded
	mov	byte ptr [si],0ffh	; yup
msnotincluded:
	inc	di
	inc	si
	loop	mscheck


	call	handlespritechanges


	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax			; set WM 2
	mov	dx,32			; 32 cells per row
	mov	di,screen1
					; first byte on screen
	mov	es,graphseg
	lea	bx,changes
	mov	cx,screensize
	xor	ah,ah			; row #
	mov	si,screen		; SI is VDP address
msloop:					; DRAW MULTICOLOR SCREEN
	cmp	byte ptr [bx],0
	jz	msnext
	push	bx
	push	ax
	;	draw char
	mov	bl,[si]			; al = char to draw
	xor	bh,bh
	shl	bx,3			; pattern address +
	shr	ax,8			
	and	ax,3			; row AND 3*2 = pattern offset
	add	ax,ax
	add	bx,ax
	add	bx,patterns
	call	drawmultichar
	pop	ax
	pop	bx
msnext:	
	inc	si
	inc	bx
	inc	di			; next cell
	dec	dx
	jnz	msdoloop		
	add	di,(rowwidth-32)+rowwidth*8-rowwidth
					; skip sides, 4 on left & 4 on right,
					; and 8 rows (320 bytes)
					; minus the row it just entered
	inc	ah			; next row
	mov	dx,32
msdoloop:
	loop	msloop
	cld

	call	drawsprites
	call	updateseenscreen
	call	gcleanup

	ret
multiscreen	endp


;	Draw multicolor mode character (4x4 block).
;	BX = pattern table address (colors).
;
;	Preserve DX, DI, CX.
drawmultichar	proc	near
	push	cx
	push	di
	push	dx
	push	si
	mov	dx,03ceh
	mov	al,8
	out	dx,al
	inc	dx

	mov	ch,[bx]
	mov	cl,ch
	shr	ch,4
	jnz	dmctlnb
	mov	ch,textbg
dmctlnb:
	and	cl,15
	jnz	dmctrnb
	mov	cl,textbg
dmctrnb:
	mov	si,4			; draw 4 rows on screen for 2 blocks
dmctoprow:
	mov	al,0f0h			; mask for left block
	out	dx,al
	mov	al,es:[di]
	mov	es:[di],ch
	mov	al,00fh			; mask for right block
	out	dx,al
	mov	al,es:[di]
	mov	es:[di],cl
	add	di,rowwidth
	dec	si
	jg	dmctoprow


	mov	ch,[bx+1]
	mov	cl,ch
	shr	ch,4
	jnz	dmcblnb
	mov	ch,textbg
dmcblnb:
	and	cl,15
	jnz	dmcbrnb
	mov	cl,textbg
dmcbrnb:
	mov	si,4			; draw 4 rows on screen for 2 blocks
dmcbottomrow:
	mov	al,0f0h			; mask for left block
	out	dx,al
	mov	al,es:[di]
	mov	es:[di],ch
	mov	al,00fh			; mask for right block
	out	dx,al
	mov	al,es:[di]
	mov	es:[di],cl
	add	di,rowwidth
	dec	si
	jg	dmcbottomrow
	
	pop	si
	pop	dx
	pop	di
	pop	cx
	ret
drawmultichar	endp


;=========================================================================

;	Draw bitmap screen (256x192).
;	Each char in the screen image table represents a
;	8x8 pixel block with 2 colors each row.
bitscreen	proc	near

;	Move charchanges to changes in appropriate places.
;	Go through SIT... if charchanges corresp is set, set changes.

	lea	bx,changes
	MOV	si,screen
	mov	cx,768
	XOR	AH,AH
	mov	dx,00ffh
bsmchtc:
	mov	al,[si]
	inc	si
	mov	di,ax
	
	test	byte ptr [di+charchanges],dl
	jz	bsch0
	mov	byte ptr [bx],dl
bsch0:
	inc	bx
	dec	cx
	jz	bschout
	TEST	CX,dx
	jnz	bsmchtc
	inc	ah
	jmp	bsmchtc


bschout:
	push	ds
	pop	es
	lea	di,charchanges
	mov	cx,768/2
	xor	ax,ax
	rep	stosw

	call	handlespritechanges

	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax			; set WM 2
	mov	dx,32			; 32 columns per row
	mov	di,screen1
					; first byte on screen
	xor	ax,ax			; # char on screen
	mov	es,graphseg
	lea	bx,changes
	mov	cx,screensize
	mov	si,screen		; SI is VDP address
bssstart:				; DRAW BITMAP SCREEN
	cmp	byte ptr [bx],0
	jz	bsnext

	push	si
	push	bx
	push	ax

	mov	al,[si]			; AX = # char
	mov	bx,ax
	shl	bx,3			; BX = offset into both tables
	mov	si,bx

	and	si,bitpattmask
	add	si,patterns		; SI = pattern addr
	and	bx,bitcolormask
	add	bx,colors		; BX = color addr

	call	drawbitchar
	pop	ax
	pop	bx
	pop	si
bsnext:	
	inc	ax
	inc	si
	inc	bx
	inc	di			; next cell
	dec	dx
	jnz	bsdoloop		
	add	di,(rowwidth-32)+rowwidth*8-rowwidth
					; skip sides, 4 on left & 4 on right,
					; and 8 rows (320 bytes)
					; minus the row it just entered
	mov	dx,32
bsdoloop:
	loop	bssstart
	;	Now do the SPRITES.
	cld

	call	drawsprites
	call	updateseenscreen
	call	gcleanup

	ret
bitscreen	endp


;	SI= pattern;  BX= color
onerow	macro	offs
	local	notbg,notfg

	mov	al,[si+offs]
	mov	ah,[bx+offs]
	and	ah,15
	jnz	notbg
	mov	ah,textbg
notbg:
	not	al
	out	dx,al
	mov	ch,es:[di+offs*rowwidth]
	mov	es:[di+offs*rowwidth],ah
	not	al
	out	dx,al
	mov	ah,[bx+offs]
	shr	ah,4
	jnz	notfg
	mov	ah,textbg
notfg:
	mov	ch,es:[di+offs*rowwidth]
	mov	es:[di+offs*rowwidth],ah
	endm
	

;	Draw one character on the screen
;	DI = screen address
;	SI = ptr to pattern table entry for char
;	BX = ptr to color table entry
;
drawbitchar	proc	near
	push	cx
	push	dx
	mov	dx,03ceh
	mov	ax,0ff08h
	out	dx,ax
	inc	dx
					; clear background
	mov	al,[bx]			; no masking because EGA ignores
	mov	ah,es:[di]		; upper 4 bits
	mov	es:[di],al

	lea	di,[di+rowwidth]
	mov	al,[bx+1]
	mov	ah,es:[di]
	mov	es:[di],al

	lea	di,[di+rowwidth]
	mov	al,[bx+2]
	mov	ah,es:[di]
	mov	es:[di],al

	lea	di,[di+rowwidth]
	mov	al,[bx+3]
	mov	ah,es:[di]
	mov	es:[di],al

	lea	di,[di+rowwidth]
	mov	al,[bx+4]
	mov	ah,es:[di]
	mov	es:[di],al

	lea	di,[di+rowwidth]
	mov	al,[bx+5]
	mov	ah,es:[di]
	mov	es:[di],al

	lea	di,[di+rowwidth]
	mov	al,[bx+6]
	mov	ah,es:[di]
	mov	es:[di],al

	lea	di,[di+rowwidth]
	mov	al,[bx+7]
	mov	ah,es:[di]
	mov	es:[di],al

	sub	di,7*rowwidth

	mov	al,[si]
	out	dx,al
	mov	al,[bx]
	shr	al,4
	mov	ah,es:[di]
	mov	es:[di],al

	mov	al,[si+1]
	lea	di,[di+rowwidth]
	out	dx,al
	mov	al,[bx+1]
	shr	al,4
	mov	ah,es:[di]
	mov	es:[di],al

	mov	al,[si+2]
	lea	di,[di+rowwidth]
	out	dx,al
	mov	al,[bx+2]
	shr	al,4
	mov	ah,es:[di]
	mov	es:[di],al

	mov	al,[si+3]
	lea	di,[di+rowwidth]
	out	dx,al
	mov	al,[bx+3]
	shr	al,4
	mov	ah,es:[di]
	mov	es:[di],al

	mov	al,[si+4]
	lea	di,[di+rowwidth]
	out	dx,al
	mov	al,[bx+4]
	shr	al,4
	mov	ah,es:[di]
	mov	es:[di],al

	mov	al,[si+5]
	lea	di,[di+rowwidth]
	out	dx,al
	mov	al,[bx+5]
	shr	al,4
	mov	ah,es:[di]
	mov	es:[di],al

	mov	al,[si+6]
	lea	di,[di+rowwidth]
	out	dx,al
	mov	al,[bx+6]
	shr	al,4
	mov	ah,es:[di]
	mov	es:[di],al

	mov	al,[si+7]
	lea	di,[di+rowwidth]
	out	dx,al
	mov	al,[bx+7]
	shr	al,4
	mov	ah,es:[di]
	mov	es:[di],al

	sub	di,7*rowwidth
	
	pop	dx
	pop	cx
	ret
	endp

;=========================================================================

;	UPDATESEENSCREEN:
;	Copy chars in CHANGED from Screen 1 to Screen 0

updateseenscreen	proc	near
	mov	dx,3ceh
	mov	ax,105h
	out	dx,ax

	mov	es,graphseg

	lea	bp,changes
	xor	bx,bx
	mov	cx,768
uss1loop:
	mov	al,ds:[bp]
	or	al,al
	jz	uss1not

	mov	si,[bx+charoffsets]
	mov	di,si
	add	si,screen1
	add	di,screen0

	mov	al,es:[si]
	mov	es:[di],al
	mov	al,es:[si+rowwidth*1]
	mov	es:[di+rowwidth*1],al
	mov	al,es:[si+rowwidth*2]
	mov	es:[di+rowwidth*2],al
	mov	al,es:[si+rowwidth*3]
	mov	es:[di+rowwidth*3],al
	mov	al,es:[si+rowwidth*4]
	mov	es:[di+rowwidth*4],al
	mov	al,es:[si+rowwidth*5]
	mov	es:[di+rowwidth*5],al
	mov	al,es:[si+rowwidth*6]
	mov	es:[di+rowwidth*6],al
	mov	al,es:[si+rowwidth*7]
	mov	es:[di+rowwidth*7],al

uss1not:
	inc	bp
	add	bx,2
	loop	uss1loop

	ret
	endp



;	Clean up buffers and copy sprite table

gcleanup	proc	near
	mov	si,sprites
	lea	di,oldsprites
	push	es
	push	ds
	pop	es
	mov	cx,128/2
	rep	movsw

	xor	ax,ax
	lea	di,changes
	mov	cx,screensize
	shr	cx,1
	rep	stosw

	lea	di,charchanges
	mov	cx,768/2
	rep	stosw

	lea	di,spritechanges
	mov	cx,32/2
	rep	stosw

	lea	di,spritecharchanges
	mov	cx,256/2
	rep	stosw

	pop	es
	ret
	endp


;==========================================================================

;	Update video screen  (called externally)
;
updatevideoscreen	proc	near
	cmp	ontiscreen,0
	jz	updateout
	pusha
	push	es

	
	cmp	videodatachanged,0
	je	updatenot
	cmp	everupdate,0
	jz	updatenot
	cmp	blank,0
	jnz	updatenot

	and	vdpstat,vdpinterrupt	; lose all but this bit
	call	word ptr screenhandler
	mov	sinceredrawcounter,0
	mov	vdprestcounter,0
updatenot:
	mov	videodatachanged,0
	pop	es
	popa
updateout:
	ret
updatevideoscreen	endp	


;=====================================================================

setupdatesprites	proc	near
	cld
	push	ds
	pop	es
	lea	di,spritechanges
	mov	cx,32/2
	mov	ax,0ffffh
	rep	stosw
	ret
	endp
	

setcompleteupdate	proc	near
	cld
	push	es
	push	ds
	pop	es
	lea	di,changes
	mov	cx,screensize
	shr	cx,1
	mov	ax,0ffffh
	rep	stosw
	call	setupdatesprites
	pop	es
	ret
	endp

completeupdate	proc	near
	call	setcompleteupdate
	call	settiscreenmode
	call	clearsides
	call	erasephrase
	call	word ptr screenhandler
	mov	ontiscreen,1
	ret
	endp


;=========================================================================

;	For speech routines
;	"Draw" a phrase on the bottommost line of the screen
;
;	TIFONT in DEBUG has a TI font
drawphrase	proc	near
	pusha
	push	es
	mov	es,graphseg

	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax

	mov	cl,sphrase
	xor	ch,ch
	jcxz	dphout


;	Set whole bottom line to black but don't clear set chars

	push	cx
	mov	cx,32
	mov	di,speechline
dphclear:
	mov	ax,0101h
	lea	bx,tifont+' '*8
	inc	di
	call	drawgraphicschar
	loop	dphclear
	pop	cx

;	Center this

	mov	ax,cx
	shr	ax,1
	neg	ax
	add	ax,16
	mov	di,speechline
	add	di,ax


	lea	si,sphrase+1
dphloop:
	mov	bl,[si]
	xor	bh,bh
	shl	bx,3
	add	bx,offset tifont

	mov	ax,0f01h
	call	drawgraphicschar
	inc	di
	inc	si
	loop	dphloop

dphout:
	pop	es
	popa
	ret
	endp


;	ERASE a speech phrase
;
;
erasephrase	proc	near
	pusha
	push	es
	mov	es,graphseg
	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax
	mov	ax,0ff08h
	out	dx,ax

	mov	di,speechline
	mov	cx,8*rowwidth/2

	xor	al,al
	mov	ah,al
	rep	stosw

	pop	es
	popa
	ret
	endp


;=========================================================================

;	DRAWLED --	Draw led (signifying a device is on)
;
;	Inputs:		AH=color, DI=row(0-23)
;
drawled	proc	near
	push	bx
	push	di

	push	ax
	push	dx

	mov	ax,rowwidth
	shl	di,3
	sub	di,5
	mul	di
	mov	di,ax

	pop	dx
	pop	ax

	call	drawbitmap

	pop	di
	pop	bx
	ret
	endp


drawrs232led	proc	near
	push	di
	push	bx

	mov	di,(24*8-5)*rowwidth
	call	drawbitmap
	pop	bx
	pop	di
	ret
	endp


drawbitmap	proc	near
	cmp	ontiscreen,0
	jz	dbmout

	pusha
	push	es

	mov	es,graphseg
	push	ax

	mov	dx,3ceh
	mov	ax,205h
	out	dx,ax			; set WM 2

	mov	dx,03ceh
	mov	al,8
	out	dx,al
	inc	dx
	mov	cl,2

	pop	ax

	mov	al,0ffh
dbmloop:
	out	dx,al
	xchg	al,ah
	mov	ch,es:[di]
	mov	es:[di],al		; send BG color
	xchg	al,ah
	add	di,rowwidth
	dec	cl
	jg	dbmloop
	pop	es
	popa
dbmout:
	ret
	endp
