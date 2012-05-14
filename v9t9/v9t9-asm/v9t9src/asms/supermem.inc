; ******************************************************
; SUPERMEM.INC include file for super-fast memory access
; ******************************************************
; by Edward Swartz  1995
; ******************************************************

;==========================================================================
;	Super-fast internal memory routines
;==========================================================================

;--------------------------------------------------------------------------
;	Read a Word from memory MACRO.
;
;	ADDR=address, returns VAL=value.
;--------------------------------------------------------------------------


;	SUPER readword routine!
;
;	Uses FS: segment as addr map.

READWORD macro addr,val
	local	isram,isnotram

	.386
	cmp	word ptr fs:[addr],7f7fh
	.286
	jne	isram

	IFDIFI	<addr>,<di>
		push	di
		mov	di,addr
	ENDIF

	IFDIFI	<val>,<ax>
		push	ax
		call	_readword
		mov	val,ax
		pop	ax
	ELSE
		call	_readword
	ENDIF

	IFDIFI	<addr>,<di>
	     	pop	di
	ENDIF
      	jmp	isnotram
isram:
	mov	val,es:[addr]
isnotram:
	endm



;--------------------------------------------------------------------------
;	Read a Byte from memory MACRO.
;
;	ADDR=address, returns VAL=value.
;--------------------------------------------------------------------------

;	SUPER readbyte routine!
;
;	Uses FS: segment as addr map.

READBYTE macro addr,val
	local	isram,isnotram

	.386
	cmp	byte ptr fs:[addr],7fh
	.286
	jne	isram

	IFDIFI	<addr>,<di>
		push	di
		mov	di,addr
	ENDIF

	IFDIFI	<val>,<al>
		push	ax
		call	_readbyte
		mov	val,al
		pop	ax
	ELSE
		call	_readbyte
	ENDIF

	IFDIFI	<addr>,<di>
	     	pop	di
	ENDIF
      	jmp	isnotram
isram:
	mov	val,es:[addr]
isnotram:
	endm



;--------------------------------------------------------------------------
;	Write a Word to memory MACRO.
;
;	ADDR=address, VAL=value
;--------------------------------------------------------------------------

;	SUPER writeword routine!
;
;	Uses FS: segment as addr map.

WRITEWORD macro addr,val
	local	isram,isnotram

	.386
	cmp	word ptr fs:[addr],0		; 0?  (ROM)
	.286
	je	isnotram

	jl	isram				; -1 < 0

	IFDIFI	<addr>,<di>
		push	di
		mov	di,addr
	ENDIF

	IFDIFI	<val>,<ax>
		push	ax
		mov	ax,val
		call	_writeword
		pop	ax
	ELSE
		call	_writeword
	ENDIF

	IFDIFI	<addr>,<di>
	     	pop	di
	ENDIF
      	jmp	isnotram
isram:
	mov	word ptr es:[addr],val
isnotram:
	endm



;--------------------------------------------------------------------------
;	Write a Byte to memory MACRO.
;
;	ADDR=address, VAL=value
;--------------------------------------------------------------------------

;	SUPER writebyte routine!
;
;	Uses FS: segment as addr map.

WRITEBYTE macro addr,val
	local	isram,isnotram

	.386
	cmp	byte ptr fs:[addr],0		; 0?  (ROM)
	.286
	je	isnotram

	jl	isram				; -1 < 0

	IFDIFI	<addr>,<di>
		push	di
		mov	di,addr
	ENDIF

	IFDIFI	<val>,<al>
		push	ax
		mov	al,val
		call	_writebyte
		pop	ax
	ELSE
		call	_writebyte
	ENDIF

	IFDIFI	<addr>,<di>
	     	pop	di
	ENDIF
      	jmp	isnotram
isram:
	mov	byte ptr es:[addr],val
isnotram:
	endm


