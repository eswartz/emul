; ******************************************
; MEMORY.INC include file for memory stuff
; ******************************************
; by Edward Swartz  1/9/1995
; ******************************************

	.data

IFDEF	_TIEMUL_

;
;	Addresses for memory segments
;
	public	ms_rom,ms_lram,ms_dsrrom,ms_cartmem
	public	ms_pad,ms_hram

	public	_IP

	public	vaddr,vwrite,gaddr,gwrite,vdpramsize,vwriteoffset
	public	vdpstat,vregs
	public	gplseg,moduleseg,speechseg,cpuseg,moduleoffset

	public	dsrwritebyte,dsrreadbyte

	public	stateflag

ELSE

	extrn	ms_rom:word,ms_lram:word,ms_dsrrom:word
	extrn	ms_cartmem:word,ms_pad:word,ms_hram:word

	extrn	_IP:word

	extrn	vaddr:word,vwrite:byte,gaddr:word,gwrite:byte
	extrn	vdpramsize:word,vdpstat:byte,vregs:byte,vwriteoffset:word
	extrn	gplseg:word,moduleseg:word,speechseg:word,cpuseg:word
	extrn	moduleoffset:word

	extrn	dsrwritebyte:word,dsrreadbyte:word

	extrn	stateflag:word


ENDIF


ms_ptrs	equ	ms_rom
ms_bptrs equ	ms_brom


	.code

;**************************************************************************
;
;	The READ WORD routines.
;
;	These are all supposed to be called with ES:DI being the valid
;	pointer to the memory.  They return AX as the data, and do not
;	alter DI (as far as destroying it for a WRITE WORD routine to
;	the same address).
;
;**************************************************************************

;---------------------------------------------------------------------------
;	Read a Word from memory.
;	
;	DI=address.  Returns AX=value.
;---------------------------------------------------------------------------

_readword proc	near
	push	di				; save address
	and	di,0E000h
	shr	di,12
	mov	ax,[cs:word ptr ms_reads+di]	; get read routine
	pop	di				; DI=address
	call	ax				; read it
	ret
	endp


_freadword proc	far
	push	di				; save address
	and	di,0E000h
	shr	di,12
	mov	ax,[cs:word ptr ms_reads+di]	; get read routine
	pop	di				; DI=address
	call	ax				; read it
	retf
	endp



;==========================================================================
IFNDEF	_tiemul_

	extrn	ms_reads:word

ELSE

;-------------------------------------------------------------------------
;	Pointers to word read routines.
;-------------------------------------------------------------------------

	public	ms_reads

ms_reads	dw	mr_rom,mr_lram,mr_dsrrom,mr_cartmem
		dw	mr_pad,mr_hram,mr_hram2,mr_hram3

ENDIF
;========================================================================



;************************************************************************
;
;	The WRITE WORD routines.
;
;	All accept ES:DI as the address to be written to.
;	DI will not be destroyed.
;
;	However, routines should NOT use ES:DI after these routines are
;	called.  MW_CARTMEM, for instance, could change ms_cartmem,
;	rendering ES:DI invalid.
;
;************************************************************************

;---------------------------------------------------------------------------
;	Write a Word to memory.
;	
;	DI=address, AX=value.
;---------------------------------------------------------------------------

_writeword proc	near
	push	bx				; used as temp

	push	DI
	and	DI,0E000h
	shr	DI,12
	mov	bx,[cs:word ptr ms_writes+DI]	; get write routine
	pop	DI
	call	bx				; write it

	pop	bx				; restore temp
	ret
	endp

_fwriteword proc	far
	push	bx				; used as temp

	push	DI
	and	DI,0E000h
	shr	DI,12
	mov	bx,[cs:word ptr ms_writes+DI]	; get write routine
	pop	DI
	call	bx				; write it

	pop	bx				; restore temp
	retf
	endp




;========================================================================
IFNDEF	_tiemul_

	extrn	ms_writes:word

ELSE

	public	ms_writes

ms_writes	dw	mw_rom,mw_lram,mw_dsrrom,mw_cartmem
		dw	mw_pad,mw_hram,mw_hram2,mw_hram3


ENDIF
;==========================================================================




;**************************************************************************
;
;	The READ BYTE routines.
;
;	These are all supposed to be called with ES:DI being the proper
;	pointer to the memory. 
;
;
;	** ADDRESSES are ASSumed to be set up for an 8086 READ! **
;
;	(i.e., pre-complement the ones bit!)
;
;
;	They return AL as the data read.
;
;**************************************************************************


;---------------------------------------------------------------------------
;	Read a Byte from memory.
;	
;	DI=address.  Returns AL=value.
;---------------------------------------------------------------------------

_readbyte proc	near
	push	DI
	and	DI,0E000h
	shr	DI,12
	mov	ax,[cs:word ptr msb_reads+DI]	; get read routine
	pop	DI				; DI=offset
	call	ax				; read it
	ret
	endp


_freadbyte proc	far
	push	DI
	and	DI,0E000h
	shr	DI,12
	mov	ax,[cs:word ptr msb_reads+DI]	; get read routine
	pop	DI				; DI=offset
	call	ax				; read it
	retf
	endp


;==========================================================================
IFNDEF	_tiemul_

	extrn	msb_reads:word,mrb_null:near
	extrn	nulldsrread:near
	extrn	dskdsrread:near,rs232dsrread:near

ELSE

;-------------------------------------------------------------------------
;	Pointers to byte read routines.
;-------------------------------------------------------------------------

	public	msb_reads,mrb_null
	public	nulldsrread
	public	dskdsrread,rs232dsrread

msb_reads	dw	mrb_rom,mrb_lram,mrb_dsrrom,mrb_cartmem
		dw	mrb_pad,mrb_hram,mrb_hram2,mrb_hram3

ENDIF
;========================================================================



;************************************************************************
;
;	The WRITE BYTE routines.
;
;	All accept ES:DI as the address to be written to.  
;	DI will not be destroyed.
;
;	** THE ADDRESS is ASSumed to be PRE-COMPLEMENTED! **
;
;	However, routines should NOT use ES:DI after these routines are
;	called.  MW_CARTMEM, for instance, could change ms_cartmem,
;	rendering ES:DI invalid.
;
;************************************************************************

;---------------------------------------------------------------------------
;	Write a Byte to memory.
;	
;	DI=address, AL=value.
;---------------------------------------------------------------------------

_writebyte proc	near
	push	bx				; used as temp

	push	DI
	and	DI,0E000h
	shr	DI,12
	mov	bx,[cs:word ptr msb_writes+DI]	; get write routine
	pop	DI

	call	bx				; write it
	pop	bx				; restore temp
	ret
	endp


_fwritebyte proc	far
	push	bx				; used as temp

	push	DI
	and	DI,0E000h
	shr	DI,12
	mov	bx,[cs:word ptr msb_writes+DI]	; get write routine
	pop	DI

	call	bx				; write it
	pop	bx				; restore temp
	retf
	endp



;========================================================================
IFNDEF	_tiemul_

	extrn	msb_writes:word
	extrn	nulldsrwrite:near
	extrn	dskdsrwrite:near,rs232dsrwrite:near

ELSE

	public	msb_writes
	public	nulldsrwrite
	public	dskdsrwrite,rs232dsrwrite

msb_writes	dw	mwb_rom,mwb_lram,mwb_dsrrom,mwb_cartmem
		dw	mwb_pad,mwb_hram,mwb_hram2,mwb_hram3


ENDIF
;========================================================================


;========================================================================

;------------------------------------------------------------------------
;	VALIDATEWP will be sure WP is legal.
;
;	Changes no registers.
;------------------------------------------------------------------------

IFNDEF	_tiemul_

	extrn	validateWP:near

ELSE

	public	validateWP


ENDIF
;=======================================================================




;------------------------------------------------------------------------
;	FETCH a word from the current instruction pointer and adjust IP.
;
;	Sets ES:DI to the address of the current word (the one just read)
;
;------------------------------------------------------------------------

FETCH	macro	reg
	mov	di,IP
	mov	reg,es:[di]
	add	IP,2
	endm


;------------------------------------------------------------------------
;	Get a word from ES:DI and adjust IP.
;
;	(Use this after FETCH, before ES:DI changes.)
;------------------------------------------------------------------------

FETCH@	macro	reg
	mov	di,IP
	add	IP,2
	mov	reg,es:[di]
	endm


;------------------------------------------------------------------------
;	READREG -- read a register
;
;	OFFS must be a valid offset to a register (0-30),
;	or register SI or DI.
;
;	Uses WPSEG.
;
;	This routine assumes that no one tried to be funny and
;	install WP in the middle of memory-mapped registers.
;
;------------------------------------------------------------------------

READREG	macro	offs,reg

	IFIDNI		<offs>,<AX>
	 IFIDNI		<offs>,<CX>
	  IFIDNI	<offs>,<DX>
	   IFIDNI	<offs>,<BX>
		.err	"Illegal register used"
	   ENDIF
	  ENDIF
	 ENDIF
	ENDIF

	IFIDNI		<reg>,<IP>
		.err	"Can't use IP"
	ENDIF

	mov	reg,word ptr es:[wp+offs]

	endm


;------------------------------------------------------------------------
;	READREGBYTE -- read the high byte of a register
;
;	OFFS must be a valid offset to a register (0-30),
;	or register SI or DI.
;
;	Uses WPSEG.
;
;	This routine assumes that no one tried to be funny and
;	install WP in the middle of memory-mapped registers.
;
;------------------------------------------------------------------------

readregbyte	macro	offs,reg

	IFIDNI		<offs>,<AX>
	 IFIDNI		<offs>,<CX>
	  IFIDNI	<offs>,<DX>
	   IFIDNI	<offs>,<BX>
		.err	"Illegal register used"
	   ENDIF
	  ENDIF
	 ENDIF
	ENDIF

	mov	reg,byte ptr es:[wp+offs+1]
	endm



;------------------------------------------------------------------------
;	WRITEREG -- write a register
;
;	OFFS must be a valid offset to a register (0-30),
;	or register SI or DI.
;
;	Uses WPSEG.
;
;	This routine assumes no one tried to be funny and install
;	the workspace over memory-mapped registers.
;	
;------------------------------------------------------------------------

writereg macro	offs,reg

	IFIDNI		<offs>,<AX>
	 IFIDNI		<offs>,<CX>
	  IFIDNI	<offs>,<DX>
	   IFIDNI	<offs>,<BX>
		.err	"Illegal register used"
	   ENDIF
	  ENDIF
	 ENDIF
	ENDIF


	IFIDNI	<reg>,<IP>
		.err	"Can't use IP"
	ENDIF

	mov	word ptr es:[wp+offs],reg
	endm


;------------------------------------------------------------------------
;	WRITEREGBYTE -- write a register
;
;	OFFS must be a valid offset to a register (0-30),
;	or register SI or DI.
;
;	Uses WPSEG.
;
;	This routine assumes no one tried to be funny and install
;	the workspace over memory-mapped registers.
;	
;------------------------------------------------------------------------

writeregbyte macro	offs,reg

	IFIDNI		<offs>,<AX>
	 IFIDNI		<offs>,<CX>
	  IFIDNI	<offs>,<DX>
	   IFIDNI	<offs>,<BX>
		.err	"Illegal register used"
	   ENDIF
	  ENDIF
	 ENDIF
	ENDIF

	mov	byte ptr es:[wp+offs+1],reg
	endm




;--------------------------------------------------------------------------
;	For instructions which use the same address TWICE,
;	(like INC and stuff, which read and write), figure
;	out the ES:DI address, and return the addresses telling
;	how to read and write (CALL these).
;
;	For WORDS only.
;---------------------------------------------------------------------------

GETPTR	macro	addr,readreg,writereg
	mov	di,addr
	push	di
	and	di,0E000h
	shr	di,12
	mov	readreg,[cs:word ptr ms_reads+di]	; got read address
	mov	writereg,[cs:word ptr ms_writes+di]	; got write addr
	pop	di
	endm


;--------------------------------------------------------------------------
;	For instructions which use the same address TWICE,
;	(like INC and stuff, which read and write), figure
;	out the ES:DI address, and return the addresses telling
;	how to read and write (CALL these).
;
;	Destroys DI.
;
;	For BYTES only.
;---------------------------------------------------------------------------

GETPTRB	macro	addr,readreg,writereg
	mov	di,addr
	push	di
	and	di,0E000h
	shr	di,12
	mov	readreg,[cs:word ptr msb_reads+di]	; got read address
	mov	writereg,[cs:word ptr msb_writes+di]	; got write addr
	pop	di
	endm


IFNDEF	_TIEMUL_
	include	slowmem.inc		; external routines must use this
ELSE
	IFDEF	SUPER
		include	supermem.inc
	ELSE
		IFNDEF	FAST
		include	slowmem.inc    	; if fast not defined, use slow
		ELSE
		include	fastmem.inc    	; else use fast stuff
		ENDIF
	ENDIF
ENDIF




