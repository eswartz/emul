;   slowmem.inc
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

;==========================================================================
;	"SLOW" external write/read memory routines.
;==========================================================================



;--------------------------------------------------------------------------
;	Read a Word from memory MACRO.
;
;	ADDR=address, returns VAL=value.
;--------------------------------------------------------------------------

READWORD macro	addr,val
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
	call	_readword		; read the word,
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


MREADWORD macro addr,val
	READWORD addr,val
	endm



;--------------------------------------------------------------------------
;	Read a Byte from memory MACRO.
;
;	ADDR=address, returns VAL=value.
;--------------------------------------------------------------------------

READBYTE macro	addr,val

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
	call	_readbyte
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
	call	_writeword
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


MWRITEWORD macro addr,val
	WRITEWORD addr,val
	endm




;--------------------------------------------------------------------------
;	Write a Byte to memory MACRO.
;
;	ADDR=address, VAL=value
;--------------------------------------------------------------------------

WRITEBYTE macro	addr,val

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
	call	_writebyte
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