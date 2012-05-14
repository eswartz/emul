; *************************************************
; FILES.ASM  V9t9 file management routines
; *************************************************
; by Edward Swartz  5/24/1993
; *************************************************

_FILES_	 = 1

	include	standard.h
	include	tiemul.h
	include	video.h
	include	hardware.h
	include	keyboard.h		; for FCTN4 in SERIAL.INC!
	include	support.h
	include	files.h
	
	include	memory.inc
	include	registers.inc

	include	files.inc


	.data


f_program	equ	80h
f_variable	equ	10h
f_internal	equ	8h
f_relative	equ	1h

f_open		equ	0
f_close		equ	1
f_read		equ	2
f_write		equ	3
f_seek		equ	4
f_load		equ	5
f_save		equ	6
f_delete	equ	7
f_scratch	equ	8
f_status	equ	9

m_update	equ	0
m_output	equ	1
m_input		equ	2
m_append	equ	3

m_error		equ	0e0h

e_baddevice	equ	0h
e_readonly	equ	020h
e_badopenmode	equ	040h
e_illegal	equ	060h
e_outofspace	equ	080h
e_endoffile	equ	0a0h
e_hardwarefailure equ	0c0h
e_badfiletype	equ	0e0h

pabrec	struc
	opcode		db	0
	pflags		db	0
	addr		dw	0
	preclen		db	0
	charcount	db	0
	recnum		dw	0
	scrnoffs	db	0
	namelen		db	0
	ends




	even
dskpath	dw	0			; ptr to DOS path name
pabaddr	dw	0			; VDP loc of current PAB
curfile	dw	0			; current file
curfdr	dw	0
fnend	dw	0			; end of filename in VDP


diskdsrname	db	'DISK.BIN',0,0,0,0,0
emudiskdsrname	db	'EMUDISK.BIN',0,0
shareddiskdsrname db	'EMU2DISK.BIN',0
rs232dsrname	db	'RS232.BIN',0,0,0,0
emurs232dsrname	db	'EMURS232.BIN',0

curdev	db	0			; current device

	even

dsrseg_realdisk	dw	0			; segment for disk dsr
dsrseg_emudisk	dw	0			; segment for EMU disk dsr
dsrseg_emurs232	dw	0			; segment for emulated rs232 dsr
dsrseg_realrs232 dw	0			; segment for rs232 dsr

	even





	.code

;===========================================================================
;	FILES:	Pre-config init.
;
;	
;===========================================================================

files_preconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	FILES:	Post-config init.
;
;	þ  Get memory for DSR ROMs, if specified in V9t9.CNF.
;	þ  Bits in "features" (set from DSRCombo) tell which.
;===========================================================================

files_postconfiginit proc near
	
	call	allocatedsrs

	ret
	endp


;===========================================================================
;	FILES:	Restart.
;===========================================================================

files_restart proc near
	clc
	ret
	endp


;===========================================================================
;	FILES:	Restop.
;===========================================================================

files_restop proc near
	clc
	ret
	endp


;===========================================================================
;	FILES:	Shutdown.
;
;	DOS frees memory for us, so we won't do it here.
;===========================================================================

files_shutdown proc near
	clc
	ret
	endp


;
;	ALLOCATEDSRS --	Get memory for wanted DSR ROMs and set
;			bits in features
;

	.data

ad_err	db	'Out of memory for DSR images.',0

	.code

allocatedsrs proc near
	lea	bx,diskdsrname
	lea	si,dsrseg_realdisk
	mov	ax,FE_realdisk
	call	allocadsr
	jc	aderr

	lea	bx,emudiskdsrname		; assume full emulation

	mov	ax,features
	not	ax
	test	ax,FE_emudisk+FE_realdisk
	jnz	adnotboth

	lea	bx,shareddiskdsrname		; we have both -- use
						; fixed emulated DSR

adnotboth:
	lea	si,dsrseg_emudisk
	mov	ax,FE_emudisk
	call	allocadsr
	jc	aderr

	lea	bx,rs232dsrname
	lea	si,dsrseg_realrs232
	mov	ax,FE_realrs232
	call	allocadsr
	jc	aderr

	lea	bx,emurs232dsrname
	lea	si,dsrseg_emurs232
	mov	ax,FE_emurs232
	call	allocadsr
	jnc	adout

aderr:
	lea	dx,ad_err
	call	setuperror
	stc
		
adout:
	ret
	endp


;	ALLOCADSR --	Get 8k memory if the filename at [bx] is non-null,
;			and store segment at [si].  If allocated, set
;			AX in features.

allocadsr proc	near
	test	features,ax
	jz	aadout

	cmp	byte ptr [bx],0
	jz	aadout

	push	ax
	push	bx
	mov	ah,48h
	mov	bx,8192/16
	int	21h
	mov	[si],ax				; assume success
	pop	bx
	pop	ax
	jc	aaderr				; error?

	or	features,ax			; nope
	jmp	aadout

aadnot:
	not	ax
	and	features,ax			; uh-oh

aaderr:
	mov	word ptr [si],0			; clear segment
	stc

aadout:
	ret
	endp




;	LOADDSRS --	Read DSR images from disk
;
;	Inputs:		EMUDISKDSRNAME, DISKDSRNAME, and RS232DSRENAME 
;			with ROMPATH are filenames.
;
;			If any feature bits are reset, we act as if device
;			doesn't exist.  
;
;	Outputs:	C=1 if error
;

loaddsrs proc	near
	push	ax
	push	bx
	push	si

	lea	bx,diskdsrname			; get 99/4A DISK DSR
	lea	si,dsrseg_realdisk
	mov	ax,FE_realdisk
	call	loadadsr
	jc	ldsout

	lea	bx,emudiskdsrname		; get emulated DISK DSR
	lea	si,dsrseg_emudisk		
	mov	ax,FE_emudisk
	call	loadadsr
	jc	ldsout

	lea	bx,rs232dsrname			; get 99/4A RS232 DSR
	lea	si,dsrseg_realrs232
	mov	ax,FE_realrs232
	call	loadadsr
	jc	ldsout

	lea	bx,emurs232dsrname	     	; get emulated RS232 DSR
	lea	si,dsrseg_emurs232
	mov	ax,FE_emurs232
	call	loadadsr


ldsout:
	pop	si
	pop	bx
	pop	ax
	ret
	endp


;	LOADADSR
;
;	Load one DSR ROM.
;
;	SI = ptr to segaddr
;	BX = filename
;	AX = mask for FEATURES.
;
;	If features&AX, then
;		Loads file at <rompath>\filename into [si]:0
;		Sets error messages if needed.

	.data

lad_ferr db	'DSR ROM image % not found or invalid size.',0dh,0ah,0

	.code

loadadsr proc	near
	push	dx
	push	cx

	test	features,ax
	jz	ladout				; don't load me

	mov	es,[si]				; point to segment
	lea	si,rompath			; path for DSR ROMs
	mov	cx,8192				; 8k
	call	readROM				; read the ROM
	jnc	ladout

	lea	dx,lad_ferr			; error message
	lea	si,filename			; ROM not found
	call	setuperror
	stc

ladout:
	pop	cx
	pop	dx
	ret
	endp


;============================================================================
;       HANDLEFILEOPERATIONS
;
;       This routine handles any jump into the DSR ROM space, >4000 to >5000
;       where a "DSR" opcode appears.  The "DSR" opcode appears only in
;       emulated DSR ROMs to trap system calls.
;
;       The assumption is that DSRLNK called into the DSR ROM, so all
;       the necessary variables in the CPU PAD are set up correctly.
;
;============================================================================

;       Only emulated disk DSR and emulated RS232 are checked here.
;       The other ROMs are complete and do not need software emulation.
;

handlefileoperations proc near
	push	ax

;	Get the CRU base for "me"

	READWORD 83D0h,ax

	and	ax,1f00h
	cmp	ax,1000h		; >1000 is emulated disk dsr
	jne	hfocheckcru1

	call	dodiskdsr
	jmp	hfoout

hfocheckcru1:
	cmp	ax,1300h
	jne	hfocheckcru2

	test	features,FE_emurs232		; only one at a time
	jz	hfocheckcru2			; is supported, and if
						; it's not emulated,
						; it's not this one.
	call	dors232dsr
	jmp	hfoout

hfocheckcru2:
	jmp	hfoerrorout

hfoout:
hfoerrorout:
	
	READREG	R11,ax
	mov	IP,ax

	pop	ax
	ret
	endp


;	Handle file interrupt.
;
;	This is currently only used for the RS232.
;
handlefileinterrupt proc near
	push	ax

	test	features,FE_emurs232
	jz	hfinotrs232

	call	dors232int

hfinotrs232:
	READREG R11,ax
	mov	IP,ax

	pop	ax
	ret
	endp
	

;	Restore the important bytes of the PAB
;	so that crank changes won't mess this up
;
;	SI = curfile
restorepab	proc	near
	push	bx
	push	si
	push	di

	mov	bx,pabaddr
	lea	si,[si].pab

	mov	al,[si].pflags
	mov	[bx].pflags,al

	lea	di,[bx].pflags+1

	mov	vaddr,di
	mov	al,[di]
	call	handlevdp1


	mov	al,[si].preclen
	mov	[bx].preclen,al

	lea	di,[bx].preclen+1

	mov	vaddr,di
	mov	al,[di]
	call	handlevdp1

	pop	di
	pop	si
	pop	bx
	ret
	endp



;	Check if the PAB is open
;       Set C=1 if so.
;
ispabopen	proc	near
	push	si
	push	cx
	push	ax
	lea	si,files
	mov	cx,MAXFILES
	mov	ax,pabaddr
ipo1:
	cmp	[si].pabaddress,ax
	je	ipofound
	add	si,size tifile
	dec	cx
	jg	ipo1
iponotfoundopen:
	clc
	jmp	ipoout
ipofound:
	cmp	[si].open,1
	jne	iponotfoundopen
	stc
ipoout:
	pop	ax
	pop	cx
	pop	si
	ret
	endp



;	Return in SI pointer to free FILES record
;
getfreefile	proc	near
	push	cx
	push	ax

	lea	si,files
	mov	cx,MAXFILES
	mov	ax,pabaddr

gff1:
	cmp	[si].open,4		; catalog
	je	gffnotfound
	cmp	[si].open,1
	jne	gfffound
gffnotfound:
	add	si,size tifile
	dec	cx
	jg	gff1
	stc
	xor	si,si
	jmp	gffout
gfffound:
	clc
	mov	curfile,si
	mov	[si].open,0
	mov	[si].pabaddress,ax
	mov	al,curdev
	mov	[si].device,al
	mov	[si].handle,0
	lea	di,[si].info+O_FDR
	mov	curfdr,di
gffout:
	pop	ax
	pop	cx
	ret
	endp


;	Return the FILES record for PABADDR
;
getfile	proc	near
	push	cx
	push	ax

	lea	si,files
	mov	cx,MAXFILES
	mov	ax,pabaddr

gf1:
	cmp	[si].pabaddress,ax
	je	gffound
	add	si,size tifile
	dec	cx
	jg	gf1
	stc
	xor	si,si
	jmp	gfout
gffound:
	clc
	mov	curfile,si
	lea	di,[si].info+O_FDR
	mov	curfdr,di
gfout:
	pop	ax
	pop	cx
	ret
	endp



;	Get extended error code from DOS.
;
getextendederror	proc	near
	push	bx
	push	cx
	push	dx
	push	di
	push	si
	push	bp
	push	ds
	push	es
	mov	ah,59h
	mov	bx,0
	int	21h
	cmp	bh,1
	jne	geeout
	mov	al,ex_outofspace
geeout:
	pop	es
	pop	ds
	pop	bp
	pop	si
	pop	di
	pop	dx
	pop	cx
	pop	bx
	ret
        endp 


;	Update bytes in VDP
;
;	On stack -- top word=len, then addr.
;
;       The DSR handler has changed VDP bytes; need to call
;       "handlevdp" in case this changes the graphics.
;

VDPUpdate	proc	near
	push	bp
	mov	bp,sp

	push	cx
	push	bx
	push	ax

	mov	cx,[bp+4]
	mov	bx,[bp+6]

VULoop:
	or	cx,cx
	jz	VUDone

	mov	al,[bx]
	mov	vaddr,bx
	call	handlevdp1
	inc	bx
	dec	cx
	jmp	VULoop

VUDone:
	pop	ax
	pop	bx
	pop	cx

	pop	bp
	ret	4
	endp


;	Without any knowledge of anything, close all the files.
;

closeallfiles	proc	near
	pusha

	lea	si,files
	mov	cx,MAXFILES
cafloop:
	cmp	[si].open,1
	jne	cafnotopen
	mov	ah,3eh
	mov	bx,[si].handle
	or	bx,bx
	jz	cafnotafile
	int	21h
cafnotafile:
	mov	[si].open,0
	mov	[si].pabaddress,0ffffh
cafnotopen:
	add	si,size tifile
	loop	cafloop

	popa
	ret
	endp

	

;	Handle a critical error
;
handlecriticalerror	proc	far
	mov	al,3			; FAIL!!!  ** Dos 3.1 + ***
	iret
	endp


	include	FLOPPY.INC
	include	SERIAL.INC
	include FDC.INC


	end
