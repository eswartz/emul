; ********************************************
; COMPROM.INC include file for compiled ROM
; ********************************************
; by Edward Swartz  1995
; ********************************************

	comment	\
;========================================================================
;	Branch macro:  are we in compiled rom?
;========================================================================

CHECKROM macro	
	local	not
	mov	ax,IP
	cmp	ax,2000h
	jae	not
	cmp	compiledromseg,0
	jz	not
	jmp	branchtorom
not:
	endm	 


;========================================================================
;	Branch to ROM routine.
;========================================================================
;
;	Calling routine expects:
;
;	BP -> stack structure:
;
;params	struc
;	f_return 	dd	0		; how to get back (EXECUTE)
;	f_writeword 	dd 	0
;	f_writebyte 	dd	0
;	f_readword 	dd 	0
;	f_readbyte 	dd	0
;
;	datasg		dw	0
;	cpusg		dw	0
;
;	lastval	dw	0
;	lastcmp	dw	0			; pass back between envirs
;	val_dx	dw	0
;	ip	dw	0
;	ends
;
;	BX -> WP
;	DS -> CPUSEG
;	SS -> stack
;	ES -> CPUSEG
;
romparams struc
	f_return 	dd	0		; how to get back (EXECUTE)
	f_writeword 	dd 	0
	f_writebyte 	dd	0
	f_readword 	dd 	0
	f_readbyte 	dd	0

	datasg		dw	0
	cpusg		dw	0

	rlastval	dw	0    
	rlastcmp	dw	0     		; pass back between envirs
	rval_dx		dw	0
	rip		dw	0
	ends


	.code

	include	slowmem.inc

;	extrn	_writeword:near
;	extrn	_writebyte:near
;	extrn	_readword:near
;	extrn	_readbyte:near


setupROMpointers proc	near
	push	ax
	push	bx

	mov	ax,@data
	mov	romstruc.datasg,ax
	mov	ax,cpuseg
	mov	romstruc.cpusg,ax

	mov	ax,@code
	lea	bx,return
	mov	word ptr romstruc.f_return,bx
	mov	word ptr romstruc.f_return+2,ax

	lea	bx,_fwriteword
	mov	word ptr romstruc.f_writeword,bx
	mov	word ptr romstruc.f_writeword+2,ax

	lea	bx,_fwritebyte
	mov	word ptr romstruc.f_writebyte,bx
	mov	word ptr romstruc.f_writebyte+2,ax

	lea	bx,_freadword
	mov	word ptr romstruc.f_readword,bx
	mov	word ptr romstruc.f_readword+2,ax

	lea	bx,_freadbyte
	mov	word ptr romstruc.f_readbyte,bx
	mov	word ptr romstruc.f_readbyte+2,ax
	pop	bx
	pop	ax
	ret
	endp


branchtorom proc	near
;	push	bp
	mov	curwp,WP

	mov	ax,lastval
	mov	romstruc.rlastval,ax

	mov	ax,lastcmp
	mov	romstruc.rlastcmp,ax

	mov	romstruc.rval_dx,dx

	mov	ax,IP
	mov	romstruc.rip,ax

	mov	ax,ss
	mov	es,ax
	lea	si,romstruc
	sub	sp,size romstruc
	mov	di,sp

	cld
	mov	cx,size romstruc/2
	rep	movsw
	mov	bp,sp


;	Then, get the pointer for the code.
;
;	It's at COMPILEDROMSEG:(10h+IP)

	mov	bx,IP
	and	bx,not 1
	mov	es,compiledromseg
;	mov	bx,es:[bx+10h-6000h]
	mov	bx,es:[bx+10h]

	push	es					; push jump addr
	push	bx

	mov	bx,curWP				; get WP
	mov	dx,romstruc.rval_dx			; get STAT
	mov	es,cpuseg
	mov	ds,cpuseg
	retf

	endp


return	proc	near
	mov	ax,ss
	mov	ds,ax

	cld
	mov	si,sp
	lea	di,romstruc
	mov	ax,@data
	mov	es,ax
	mov	cx,size romstruc/2
	rep	movsw

	add	sp,size romstruc	

	mov	ax,@data
	mov	ds,ax
	mov	es,cpuseg

	mov	ax,romstruc.rlastval
	mov	lastval,ax
	mov	ax,romstruc.rlastcmp
	mov	lastcmp,ax
	mov	dx,romstruc.rval_dx

	mov	ax,romstruc.rip

	mov	IP,ax

	mov	WP,curwp

;;;	or	stateflag,romming
	jmp	executefromrom
	endp
	\


