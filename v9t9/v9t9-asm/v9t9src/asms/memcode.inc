; ******************************************
; MEMCODE.INC include file for memory code
; ******************************************
; by Edward Swartz  1/9/1995
; ******************************************

;
;	Include this file after MEMORY.INC
;


IFNDEF	_tiemul_
	.err	"Only include MEMCODE.INC in TIEMUL.ASM"
ENDIF

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



;-------------------------------------------------------------------------
;	Null reader.  (No memory)
;-------------------------------------------------------------------------

mr_null:
	xor	ax,ax
	ret


;-------------------------------------------------------------------------
;	Generic reads.
;-------------------------------------------------------------------------
mr_rom:
mr_lram:
mr_cartmem:
mr_hram:
mr_hram2:
mr_hram3:
	mov	ax,es:[di]
	ret


;-------------------------------------------------------------------------
;	DSR ROM read.
;	(Memory-mapped in TI DISK DSR.)
;	HOWEVER, it's supposed to be used with bytes.
;-------------------------------------------------------------------------
mr_dsrrom:
	mov	ax,es:[di]
	ret


;-------------------------------------------------------------------------
;	RAM PAD read.
;	Could be memory-mapped.
;-------------------------------------------------------------------------
mr_pad:
	cmp	di,8400h
	jb	mr_pad0

	inc	di			; point to "high byte"
	call	readmemorymapped	; only "high bytes" active
	shl	ax,8			; other byte is zero
	dec	di			; restore addr
	ret

mr_pad0:
;;	and	di,80ffh		; only 256 bytes active
	or	di,300h
	mov	ax,es:[di]
	ret

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

;------------------------------------------------------------------------
;	NULL writer  (ROM!)
;------------------------------------------------------------------------

mw_nullwrite:
mw_rom:
	ret


;------------------------------------------------------------------------
;	Generic RAM writer.
;------------------------------------------------------------------------

mw_lram:
mw_hram:
mw_hram2:
mw_hram3:
	mov	es:[di],ax
	ret


;------------------------------------------------------------------------
;	DSR ROM write.
;
;	We have only byte memory-mapped ports.
;------------------------------------------------------------------------

mw_dsrrom:
	ret


;------------------------------------------------------------------------
;	Cartridge write.
;------------------------------------------------------------------------

mw_cartmem:
	test	selected.memtype,mod_banked	; bankable?
	jnz	mw_cmisbankable			; yep... let's switcheroo

	test	selected.memtype,mod_minimem	; Mini Memory?
	jz	mw_cmnot			; nope... ROM!

	cmp	di,7000h			; only >7000->7FFF is ram...
	jb	mw_cmnot

	mov	es:[di],ax			; RAM, write it

mw_cmnot:
	ret

mw_cmisbankable:				; we have a banked cartridge
	test	di,2				; no RAM possible (right??)

	push	si
	push	di
	push	ds
	push	cx
	push	ax

	mov	di,6000h
	mov	si,0
	jz	mw_cmnothigh
	mov	si,2000h/16
mw_cmnothigh:
	add	si,moduleseg
	mov	ds,si
	xor	si,si

	IFDEF	T386

	mov	cx,8192/4
	.386
	rep	movsd
	.286

	ELSE

	mov	cx,8192/2
	rep	movsw

	ENDIF

	pop	ax
	pop	cx
	pop	ds
	pop	di
	pop	si

	ret


;------------------------------------------------------------------------
;	RAM PAD write
;
;	(OR memory-mapped write)
;------------------------------------------------------------------------

mw_pad:
	cmp	di,8400h
	jb	mw_pad0

	inc	di				; point to "high byte"
	shr	ax,8				; "low bytes" ignored
	call	writememorymapped		; only "high bytes" active
	dec	di				; restore addr
	ret

mw_pad0:
;;	and	di,80ffh
	or	di,300h
	mov	es:[di],ax
	ret

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

;==========================================================================

;-------------------------------------------------------------------------
;	Null reader.  (No memory)
;-------------------------------------------------------------------------

mrb_null:
	xor	al,al
	ret


;-------------------------------------------------------------------------
;	Generic reads.
;-------------------------------------------------------------------------
mrb_rom:
mrb_lram:
mrb_cartmem:
mrb_hram:
mrb_hram2:
mrb_hram3:
	mov	al,es:[di]
	ret


;-------------------------------------------------------------------------
;	DSR ROM read.
;	(Memory-mapped in TI DISK DSR.)
;-------------------------------------------------------------------------
mrb_dsrrom:
	call	word ptr dsrreadbyte
	ret

;---------------------------------------------------

nulldsrread:
	mov	al,es:[di]
	ret

;---------------------------------------------------

dskdsrread:
	cmp	di,5FF0h
	jb	ddr_dr0

	call	handlefdcread		
	ret

ddr_dr0:
	mov	al,es:[di]
	ret

;---------------------------------------------------

rs232dsrread:
	cmp	di,5001h
	jne	rdr_dr0

	call	handlers232read
	ret

rdr_dr0:
	mov	al,es:[di]
	ret


;-------------------------------------------------------------------------
;	RAM PAD read.
;	Could be memory-mapped, tho.
;-------------------------------------------------------------------------
mrb_pad:
	cmp	di,8400h
	jb	mrb_pad0

	call	readmemorymapped
	ret

mrb_pad0:
;;;;;;;	and	di,80ffh		; only 256 bytes active
	or	di,300h
	mov	al,es:[di]
	ret

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

;========================================================================

;------------------------------------------------------------------------
;	NULL writer  (ROM!)
;------------------------------------------------------------------------

mwb_nullwrite:
mwb_rom:
	ret


;------------------------------------------------------------------------
;	Generic RAM writer.
;------------------------------------------------------------------------

mwb_lram:
mwb_hram:
mwb_hram2:
mwb_hram3:
	mov	es:[di],al
	ret


;------------------------------------------------------------------------
;	DSR ROM write.
;
;	We have memory-mapped ports in TI DSR.
;------------------------------------------------------------------------

mwb_dsrrom:
	call	word ptr dsrwritebyte
	ret

;-----------------------------------------------------

nulldsrwrite:
	ret

;-----------------------------------------------------

dskdsrwrite:
	cmp	di,5FF0h
	jb	ddw_dr0

	call	handlefdcwrite		

ddw_dr0:   			
	ret

;-----------------------------------------------------

rs232dsrwrite:
	cmp	di,5001h
	jne	rdw_dr0

	call	handlers232write

rdw_dr0:
	ret

	


;------------------------------------------------------------------------
;	Cartridge write.
;------------------------------------------------------------------------

	.data

mw_cartlog db	'Switched module to segoffs >%h%n',0

	.code

mwb_cartmem:
	test	selected.memtype,mod_banked	; bankable?
	jnz	mwb_cmisbankable     		; yep... let's switcheroo

	test	selected.memtype,mod_minimem	; Mini Memory?
	jz	mwb_cmnot			; nope... ROM!

	cmp	di,7000h			; only >7000->7FFF is ram...
	jb	mwb_cmnot

	mov	es:[di],al			; RAM, write it

mwb_cmnot:
	ret

mwb_cmisbankable:				; we have a banked cartridge

	test	di,2				; no RAM possible (right??)

	push	si
	push	di
	push	ds
	push	cx
	push	ax

	mov	di,6000h
	mov	si,0
	jz	mwb_cmnothigh
	mov	si,2000h/16
mwb_cmnothigh:
	mov	moduleoffset,si

	LOG1	LG_tiemul,mw_cartlog,si

	add	si,moduleseg
	mov	ms_cartmem,si

	mov	ds,si
	xor	si,si

	IFDEF	T386

	mov	cx,8192/4
	.386
	rep	movsd
	.286

	ELSE

	mov	cx,8192/2
	rep	movsw

	ENDIF

	pop	ax
	pop	cx
	pop	ds
	pop	di
	pop	si

	ret


;------------------------------------------------------------------------
;	RAM PAD write
;
;	(OR memory-mapped write)
;------------------------------------------------------------------------

mwb_pad:
	cmp	di,8400h
	jb	mwb_pad0

	call	writememorymapped
	ret

mwb_pad0:
;;;;;;;	and	di,80ffh
	or	di,300h
	mov	es:[di],al
	ret

;========================================================================


;========================================================================

;------------------------------------------------------------------------
;	VALIDATEWP will be sure WP is legal.
;
;	Changes no registers.
;------------------------------------------------------------------------

	.data

valWPlog db	'Got an illegal workspace pointer >%h at >%h!%n',0
;vWP_err	db	'Emulated program lockup!',0dh,0ah,0

	.code

validateWP proc	near
	push 	ax

	mov	ax,WP				; address to test
	cmp	ax,2000h
	jb	vWPstop				; >2000->FFFF
	cmp	ax,0a000h
	jae	vWPout				; >2000->9FFF
	cmp	ax,8400h
	jae	vWPstop				; >2000->83FF
	cmp	ax,8000h
	jae	vWPout				; >2000->7FFF
	cmp	ax,4000h
	jb	vWPout				; >4000->7FFF
	cmp	ax,7000h
	jae	vWPout				; >4000->6FFF

vWPstop:
;	push	dx
;	lea	dx,vWP_err
;	call	setuperror
;	pop	dx
	or	stateflag,reboot

	LOG2	LG_tiemul,valWPlog,WP,IP
vWPout:
	pop	ax
	ret
	endp

;=======================================================================



;-----------------------------------------------------------------------
;	_GETPTR will return an offset into the 
;	MS_READS, MS_WRITES, and MS_PTRS tables.
;
;	Inputs:		DI=address
;	Outputs:	DI is an offset in the tables
;
;-----------------------------------------------------------------------

_getptr	proc	near
	and	di,0E000h
	shr	di,12
	ret
	endp


;	Read from memory-mapped device
;	DI = address
;
readmemorymapped	proc	near
	push	bx
	push	di

;	even (odd) accesses not allowed

	shr	di,1
	jc	rmmokay
	mov	al,0
	jmp	rmmout
rmmokaY:
	mov	bx,di
	and	bx,00e00h
	xchg	bl,bh
	jmp	[word ptr cs:readmap+bx]
	even
readmap	dw	rwsnd,rwsnd,rrvdp,rwvdp,rrspeech,rwspeech,rrgpl,rwgpl

;================================================
rwsnd:
	mov	al,0
	jmp	rmmout

;================================================
rrvdp:
	test	di,1
	jnz	rrvdpstatus
	mov	di,vaddr		; read VDP byte
	and	di,3fffh		
	mov	al,[di]
	inc	di
	and	di,3fffh		
	mov	vaddr,di
	mov	vwrite,0
	jmp	rmmout

;-----------------------------------------------
rrvdpstatus:
	mov	al,vdpstat		; read VDP status
	mov	vwrite,0
	jmp	rmmout		

;==============================================
rwvdp:
	mov	al,0
	jmp	rmmout

;==============================================
rrspeech:
	call	handlespeechread
	jmp	rmmout			

;==============================================
rwspeech:
	mov	al,0
	jmp	rmmout	

;==============================================
rrgpl:
	test	di,1
	jnz	rrgpladdress

	mov	di,gaddr		; read GPL byte

	mov	es,gplseg
	mov	al,es:[di]
	mov	es,cpuseg

	mov	bx,di			; GPL wraps in 8k segments!
	and	bx,0e000h
	inc	di
	and	di,01fffh
	or	bx,di
	mov	gaddr,bx
	jmp	rmmout

;---------------------------------------------
rrgpladdress:
	mov	ax,gaddr
	mov	bx,ax
	and	bx,0e000h
	inc	ax
	and	ax,01fffh
	or	ax,bx

	mov	bx,ax
	shl	bx,8
	mov	gaddr,bx
	shr	ax,8

	jmp	rmmout

rwgpl:
	mov	al,0
	jmp	rmmout
rmmout:
	pop	di
	pop	bx
	ret
	endp	
	

;	Write to memory-mapped device
;	DI = address
;	AL = value
writememorymapped	proc	near
	push	ax
	push	bx
	push	di
	push	bp

	shr	di,1
	jc	wmmokay
	jmp	wmmout			; no odd (even) accesses allowed
wmmokay:
	mov	bx,di
	and	bx,0e00h
	xchg	bl,bh
	jmp	[word ptr cs:writemap+bx]
	even
writemap	dw	wwsnd,wwsnd,wrvdp,wwvdp,wrspeech,wwspeech,wrgpl,wwgpl
	;		WWSND above will never be reached.

;===========================================
wwsnd:
	call	handlesound		
	jmp	wmmout

;===========================================
wwvdp:
	lea	bx,vaddr

	test	di,1
	jnz	wwvdpaddr

	mov	bp,[bx]			; write VDP byte
	mov	di,bp
	and	di,3fffh

;IFNDEF	FAST

;	A little note about this above (12/7/94) --
;	Just figgered out that without setting the >4000 mask when 
;	sending the address for a VDP write, the VDP processor
;	unwittingly does a read-before-write when writing, thereby
;	incrementing the write address temporarily.  BUT the
;	VDP address only increments by one byte overall.  WHEN
;	it does this matters.

	mov	di,bp
	add	di,vwriteoffset
	and	di,3fffh
;ENDIF

	mov	[bx],di
	call	handlevdp	
wwdont:
	inc	bp
	and	bp,3fffh
	mov	[bx],bp
	mov	vwrite,0
	jmp	wmmout

;---------------------------------------
wwvdpaddr:
	shr	word ptr [bx],8
	mov	[bx+1],al

	mov	al,80h
	add	vwrite,al
	jnz	wwvalsb			; LSB?  If so, don't worry yet
	test	word ptr [bx],8000h	; register write?
	jz	wwvagoon
	call	handlevdpreg		; yup
wwvagoon:	

;IFNDEF	FAST
	mov	vwriteoffset,1
	test	word ptr [bx],4000h
	jz	wwvnowrite
	mov	vwriteoffset,0
wwvnowrite:
;ENDIF
	and	word ptr [bx],3fffh	; lose write-bit and reg-bit

wwvalsb:
	jmp	wmmout

;======================================
wrvdp:
	jmp	wmmout

;======================================
wrspeech:
	jmp	wmmout

;======================================
wwspeech:
	call	handlespeechwrite
	jmp	wmmout		       
	
;======================================
wwgpl:
	lea	bx,gaddr
	test	di,1
	jz	wwgpldata

wwgpladdress:
	shl	word ptr [bx],8
	mov	[bx],al
	jmp	wmmout

;--------------------------------------
wwgpldata:
	mov	di,[bx]			; write GPL byte

;;	mov	es,gplseg		; not implemented
;;	mov	es:[di],al
;;	mov	es,cpuseg

	push	ax
	mov	ax,di
	and	ax,0e000h
	inc	di
	and	di,01fffh
	or	ax,di
	mov	[bx],ax
	pop	ax			; GPL wraps!
	jmp	wmmout

;=====================================
wrgpl:
	jmp	wmmout

wmmout:
	pop	bp
	pop	di
	pop	bx
	pop	ax
	ret
	endp	

