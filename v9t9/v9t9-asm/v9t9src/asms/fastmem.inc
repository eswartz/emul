; ************************************************************
; FASTMEM.INC include file for fast (inaccurate) memory access
; ************************************************************
; by Edward Swartz  1995
; ************************************************************

;==========================================================================
;	FAST internal memory routines
;==========================================================================

;--------------------------------------------------------------------------
;	Read a Word from memory MACRO.
;
;	ADDR=address, returns VAL=value.
;--------------------------------------------------------------------------

READWORD macro	addr,val
	local	rw0,rw1
	IFIDNI	<val>,<di>
		.err	"Can't return to di"
	ENDIF

	IFDIFI	<addr>,<di>		; if the address isn't already DI,
		IFNDEF	_emulate_
			push	DI  	; and we're not in emulate.inc, save DI,
			IFDIFI	<val>,<AX>     	; if the return value isn't ax,
				push	ax     	; save it,
			ENDIF
		ENDIF
		mov	DI,addr		; and make DI the address.
	ENDIF

	cmp	di,8400h
	jb	rw0
	cmp	di,0a000h
	jae	rw0

	inc	di
	call	readmemorymapped
	shl	ax,8
	dec	di
	jmp	rw1

rw0:
	mov	ax,es:[di]

rw1:




	IFDIFI	<val>,<AX>		; and if we weren't in ax,
		mov	val,ax		; move it there,
		IFNDEF	_emulate_
			pop	ax		; restore ax,
		ENDIF
	ENDIF
	IFDIFI	<addr>,<DI>		; and if the address wasn't DI,
		IFNDEF	_emulate_	; and we're not emulating,
			pop	DI	; restore it.
		ENDIF
	ENDIF
	endm


;--------------------------------------------------------------------------
;	Read a Byte from memory MACRO.
;
;	ADDR=address, returns VAL=value.
;--------------------------------------------------------------------------

READBYTE macro	addr,val
	local	rb0,rb1,rb00,rb2

IFDIFI	<addr>,<DI>
	IFNDEF	_emulate_
		push	DI
	ENDIF
	mov	DI,addr
ENDIF

IFNDEF	_emulate_		; if we're not emulating,
	xor	DI,1		; then complement the address.
ENDIF

IFNDEF	_emulate_
IFDIFI	<val>,<AL>
	IFDIFI	<val>,<AH>
		push	ax
	ENDIF
ENDIF
ENDIF


	lea	ax,[addr+6000h]
	cmp	ax,5ff0h+6000h
	jb	rb0
	cmp	ax,6000h+6000h
	jb	rb2

	cmp	addr,word ptr 8400h
	jb	rb0

	call	readmemorymapped
	jmp	rb1
rb2:
     	call	mrb_dsrrom
	jmp	rb1
rb0:
	mov	al,es:[di]
rb1:



IFDIFI	<val>,<AL>
	mov	val,al
	IFNDEF	_emulate_
		IFDIFI 	<val>,<AH>
			pop	ax
		ENDIF
	ENDIF
ENDIF
IFDIFI	<addr>,<DI>
	IFNDEF	_emulate_
		pop	DI
	ENDIF
ENDIF
IFNDEF	_emulate_		; if an external routine is calling this,
	IFIDNI	<addr>,<DI>	; and their routine used DI,
		xor	DI,1	; restore its value which was changed above.
	ENDIF
ENDIF
	endm



;--------------------------------------------------------------------------
;	Write a Word to memory MACRO.
;
;	ADDR=address, VAL=value
;--------------------------------------------------------------------------


WRITEWORD macro	addr,val
	local	ww0,ww1,ww2,ww3,ww4,ww5

IFIDNI	<val>,<DI>
	.err	"Can't write from DI"
ENDIF

IFDIFI	<addr>,<DI>
	IFNDEF	_emulate_
		push	DI
	ENDIF
	mov	DI,addr
ENDIF
IFDIFI	<val>,<AX>
	IFNDEF	_emulate_
		push	ax
	ENDIF
	mov	ax,val
ENDIF

	cmp	di,0a000h
	jae	ww1
	cmp	di,8400h
	jae	ww2
	cmp	di,8000h
	jae	ww1
	test	di,2000h		; 0000, 4000 will be skipped
	jz	ww4
	test	di,4000h		; 2000 will be ram
	jz	ww1

	call	mw_cartmem		; 6000 will be cart
	jmp	ww4
ww2:
	inc	di
	shr	ax,8
	call	writememorymapped
	dec	di
	jmp	ww4
ww1:
	mov	es:[di],ax
ww4:

IFDIFI	<val>,<AX>
	IFNDEF	_emulate_
		pop	ax
	ENDIF
ENDIF
IFDIFI	<addr>,<DI>
	IFNDEF	_emulate_
		pop	DI
	ENDIF
ENDIF
	endm


;
;	Write word macro for routines who are "known" not to
;	write to memory-mapped areas.
;
MWRITEWORD macro addr,val
IFDEF	FAST
	mov	es:[addr],val
ELSE
	WRITEWORD addr,val
ENDIF
	endm




;--------------------------------------------------------------------------
;	Write a Byte to memory MACRO.
;
;	ADDR=address, VAL=value
;--------------------------------------------------------------------------


WRITEBYTE macro	addr,val
	local	wb0,wb1,wb3,wb2,wb4,wb5

IFDIFI	<addr>,<DI>
	IFNDEF	_emulate_
		push	DI
	ENDIF
	mov	DI,addr
ENDIF
IFNDEF	_emulate_
	xor	DI,1
ENDIF
IFDIFI	<val>,<AL>
	IFDIFI	<val>,<AH>
		IFNDEF	_emulate_
		push	ax
		ENDIF
	ENDIF
	mov	al,val
ENDIF

	cmp	di,0a000h
	jae	wb1
	cmp	di,8400h
	jae	wb2
	cmp	di,8000h
	jae	wb1			; leave 0000-7fff

	test	di,4000h		; 0000, 2000 will be passed on
	jz	wb3
	cmp	di,6000h
	jae	wb5			; cart mem
	call	mwb_dsrrom		; dsr rom
	jmp	wb4

wb3:
	test	di,2000h		; 0000, 4000 will be skipped
	jz	wb4			; rom?
	jmp	wb1			; lo ram?
wb5:
	call	mwb_cartmem		; 6000 will be cart
	jmp	wb4
wb2:
	call	writememorymapped
	jmp	wb4
wb1:
	mov	es:[di],al
wb4:




IFDIFI	<val>,<AL>
	IFDIFI	<Val>,<ah>
		IFNDEF	_emulate_
		pop	ax
		ENDIF
	ENDIF
ENDIF
IFDIFI	<addr>,<DI>
	IFNDEF	_emulate_
		pop	DI
	ENDIF
ENDIF
IFNDEF	_emulate_
	IFIDNI	<addr>,<DI>
		xor	DI,1
	ENDIF
ENDIF
	endm




