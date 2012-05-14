; *****************************************
; FLOPPY.INC  V9t9 Diskette Routines
; *****************************************
; by Edward Swartz  12/3/1994
; *****************************************


	comment	\


This file will will act as the complete floppy disk DSR unit, which
at the beginning will support the pseudo-floppy storage of FDR-files
and disk images (both stored on MS-DOS drives).



Important stuff to think about constantly:

1)	TI byte-order VS Intel byte-order
2)	Updating VDP changes with HANDLEVDP
3)	PAB and FDR compliance
4)	All the routines MUST act like they're writing
	to a real floppy; FDR-files are a subset.


This is FIAD -- all emulated files have a 128-byte header which is a copy
of the File Descriptor Record.

	\



ex_outofspace	equ	81h	     	; MS-DOS error codes
ex_badpath	equ	3
ex_readonly	equ	5

F_READFDR	equ	1	     	; "open" routines return this to
F_MAKEFDR	equ	2		; disk routines

MAXFILES	equ	9		; maximum open files

MAXDRIVE	equ	5		; maximum floppy drive (DSKx)

FDRSIZE		equ	128		; bytes in File Descriptor Record

ff_variable	equ	80h		; FDR masks
ff_internal	equ	2
ff_program	equ	1

fp_variable	equ	10h
fp_internal	equ	8
fp_relative	equ	1


fdrrec	struc
	filenam	db	10 dup (32)
	res10		dw	0
	flags		db	0
	recspersec 	db	0
	secsused	dw	0	; TI-order!  
	eof		db	0
	reclen		db	0
	fixrecs		dw	0	; intel-order  (same as VARSECS)
	res20		db	8 dup (0)
	dcpb		db	100 dup (0)
	ends

tifile	struc
	handle		dw	0
	pabaddress	dw	0
	open		db	0	; 0=never, 1=open, 2=closed
	device		db	0	; which device this is on
	openmode	db	0
	pab		db	10 dup (0)	
	changed		db	0
	cursec		dw	0
	cureof		db	0
	curnrecs	db	0	; fixed use (# recs in sector)
	currec		dw	0	; fixed use (rec # in file)

	info		db	384 dup (0)
					; info for the device using this
					; DSK:  an FDR record and curr sec
tifile	ends


	.data

files	tifile	MAXFILES	dup(<>)


O_FDR	equ	0  	; offset to FDR part of INFO
O_SEC	equ	FDRSIZE	; offset to SEC part of INFO


DrcTrans	db	0,1,1,5,2,3,80h,2,82h,5		; for catalog

DSK1path	equ	tidiskpathname
DSK2path	equ	DSK1path+64
DSK3path	equ	DSK2path+64
DSK4path	equ	DSK3path+64
DSK5path	equ	DSK4path+64


D_NUMDRIVES equ 5

illegalchars    db      '<>=,;:*?[]/\'  ; illegal chars complemented by 80h
illegallen	equ	$-illegalchars

fname	db	96 dup (0)
fname2	db	96 dup (0)
FDRfilename	db	10 dup (0)


searchrec	struc
	res0	db	15h dup (?)
	sattr	db	?
	stime	dw	?
	sdate	dw	?
	ssize	dd	?
	sname	db	13 dup (?)
	ends
DTAbuff		searchrec <>
maxdirectoryrecord dw	0			; max sector # to read

even




	.code


	comment	\

This DSR emulation wil operate like the old one, except for that
all files will be stored in an FDR-ized format, where the first FDRSIZE
bytes of the FDR is stored as the header for the file.




To remember:  	update VARSECS!
		DON'T switch byte order of SECSUSED when read/write,
		because V9t9 has words in the right order!


	\




;********************************************
;*********** DISK DSR ***********************
;********************************************


dodiskdsr proc	near
	pusha
	push	es

	push	wp

;	First, see what device we are called as.

	mov	bx,pc
	mov	bx,es:[bx]
	
;	This SHOULD be a DSROP opcode...
;	So, we need to subtract DSROP,
;	and point into the device table...

	cmp	bx,DSROP
	jb	ddderr
	cmp	bx,DSROP+20h
	jae	ddderr

;	If the TI DISK DSR is installed, we should 
;	pass control to it if the function references
;	drives 1-2.

	cmp	dsrseg_realdisk,0
	jz	dddnoconflict		; no TI DISK DSR installed

	cmp	bx,DSROP+D_DSK
	je	dddnoconflict

	cmp	bx,DSROP+D_DSK2
	jbe	ddderr			; we're doing files on DSK1/2

	cmp	bx,DSROP+D_DSKSUB	; are we looking at subroutines?
	jb	dddnoconflict		; nope
	
	mov	al,es:[834ch+1]
	and	al,15			; format uses high nybble for DSR vers
	cmp	al,3			; yeah.. what drive?  DSK3 or above?
	jae	dddnoconflict		; yup... we're okay

	jmp	ddderr			; error code passes control to next
					; CRU base

dddnoconflict:
	sub	bx,DSROP
	add	bx,bx


;	Handle a subprogram or do file operations

        jmp	[word ptr cs:diskdsrtable+bx]
	jmp	dddout
diskdsrtable:
	dw	dddDSK
	dw	dddDSK1
	dw	dddDSK2
	dw	dddDSK3		
	dw	dddDSK4
	dw	dddDSK5

	dw	dddD_INIT

				; each of these loads pointers 
				; then goes to generic routines

	dw	dddD_10		; each of these receives drive #
	dw	dddD_11		; in >834X
	dw	dddD_12
	dw	dddD_13
	dw	dddD_14
	dw	dddD_15
	dw	dddD_16

	dw	dddDFILES

ddderr:
	jmp	dddout	

dddout:
	pop	wp
	pop	es
	popa
	ret
	endp


;==============================================================

dddD_INIT:
	
;	Set up memory and stuff for disk access

	lea	si,files
	mov	cx,MAXFILES

dddDI1:
	mov	[si].open,0
	add	si,size tifile
	loop	dddDI1

	jmp	dddout


;==============================================================


dddDFILES:

;	Call Files
;

	add	word ptr es:[832Ch],12
	mov	byte ptr es:[8342h+1],0
	mov	byte ptr es:[8350h+1],0
	add	word ptr es:[WP+R11],2		; good return
	jmp	dddout


;==============================================================


dddDSK:

;	We need to search all the "volumes" for the
;	diskname pointed to by >8356.

;	After this, we jump to a routine which will
;	set up pointers for the correct "drive."

;	After extracting the volume name and using it,
;	ADD LENGTH TO >8354 and >8356!

;	Volume name starts at [8356h]

	mov	bx,es:[8356h]
	mov	al,[bx]		; check to be sure it's a period
	cmp	al,'.'
	je	dddFoundPer

	jmp	dddErr

dddFoundPer:
	inc	bx

;	Match now, with BX=>volume name

	mov	di,bx			; DI=input parm
	call	matchdrive
	mov	bx,di			; DI=outparm at filename
	or	al,al
	jnz	dddFoundit

	jmp	dddErr

dddFoundit:

;	BX was the last loc checked...

	mov	di,es:[8356h]
	neg	di
	add	di,bx			; DI = # chars moved into filename

	mov	es:[8356h],bx
	add	es:[8354h],di

	lea	bx,dddDSKVectors

	dec	al   			; drive # returned
	xor	ah,ah
	add	ax,ax
	add	bx,ax

	jmp	cs:[bx]

dddDSKVectors	dw	dddDSK1,dddDSK2,dddDSK3,dddDSK4,dddDSK5

dddDSK1:

;	Load appropriate pointers to DSK1.

	mov	curdev,D_DSK1

	lea	ax,DSK1path
	mov	dskpath,ax

	jmp	DSKFiles


dddDSK2:

;	Load pointers for DSK2

	mov	curdev,D_DSK2

	lea	ax,DSK2path
	mov	dskpath,ax

	jmp	DSKFiles


dddDSK3:

;	Load pointers for DSK3

	mov	curdev,D_DSK3

	lea	ax,DSK3path
	mov	dskpath,ax

	jmp	DSKFiles

dddDSK4:

;	Load pointers for DSK4

	mov	curdev,D_DSK4

	lea	ax,DSK4path
	mov	dskpath,ax

	jmp	DSKFiles

dddDSK5:

;	Load pointers for DSK5

	mov	curdev,D_DSK5

	lea	ax,DSK5path
	mov	dskpath,ax

	jmp	DSKFiles



;******************************************
;** DISK DSR FILE MANAGEMENT ENTRY POINT **
;******************************************
;** NO SECTOR/ETC ROUTINES WILL COME HERE**
;******************************************
;
;

dskseterror	macro	num
	push	di
	mov	di,pabaddr
	and	byte ptr [di+1],not 0e0h
	or	byte ptr [di+1],num
	pop	di
IF	num EQ 0
ELSE
	call	DSKClose		; only close if error
ENDIF
	endm

getfiletype	macro	where
	push	di
	mov	di,pabaddr
	mov	al,[di+1]
	pop	di
	endm


DSKFiles:

;	Now, branch off according to the opcode

	mov	ax,es:[8356h]
	sub	ax,es:[8354h]
	sub	ax,10

;	AX points to start of PAB.

	mov	pabaddr,ax
	mov	di,ax			

	mov	bl,[di]
	cmp	bl,9
	jbe	DSKOkOp

	dskseterror e_illegal
	jmp	DSKOut

DSKOkOp:
	xor	bh,bh
	add	bx,bx
	call	[word ptr cs:DSKOpcodes+bx]

DSKOut:
	pop	wp				; from dodiskdsr
	add	word ptr es:[wp+r11],2		; no error
	push	wp
	jmp	dddOut


DSKOpcodes:	
	dw	DSKOpen,DSKClose,DSKRead,DSKWrite
	dw	DSKSeek,DSKLoad,DSKSave,DSKDelete
	dw	DSKScratch,DSKGetStatus


;**************************************
;********* OPEN A FILE ****************
;**************************************
DSKOpen	proc	near

;	If this pab is already open, close it

	call	ispabopen
	jnc	DOTryOpen		; C= found, and is open

	call	DSKClose

DOTryOpen:

;	Get a FILES record set up (SI)

	call	getfreefile
	jnc	DOGotRec

	dskseterror e_outofspace
	jmp	DSKOOut

DOGotRec:
	
;	Now, try to access the file through DOS

	mov	di,curfile
	lea	di,[di].info
	add	di,O_FDR

;	Set up FNAME to be the DOS path to the file
;	and at FDRFILENAME will be put the TI filename

	call	getfilename
	jnc	DONormalFile

;	This is a catalog request.

;*******************************
;****** CATALOG
;*******************************

	mov	bx,pabaddr
	mov	al,[bx].pflags
	and	al,not 1
	cmp	al,0ch			; int/fix, input, relative
	jne	DONCatErr

	mov	al,38
	mov	[bx].preclen,al

	mov	si,curfile

	call	DSKinitsecptrs
	mov	[si].open,4		; Catalog open!
	jmp	DSKOOut

DONCatErr:
	dskseterror	e_badopenmode
	jmp	DSKOOut


DONormalFile:

;	Now, according to type in PAB, open file

	getfiletype	al
	and	al,6
	shr	al,1

	cmp	al,m_update
	jne	DONMode1

;***********************	Update mode

	call	openfileupdate	
	jnc	DONUOpened
	jmp	DSKOOut

;	SI = curfile
;	AX = how to get FDR...

DONUOpened:
	cmp	ax,F_READFDR
	jne	DONUMake
	
	call	DSKreadfdr

;	DI = FDR
;	Returns error if too short

	jc	DONUMakeFDR		; error, just make a new file

	call	DSKcomparefdrandpab

;	CompareFDRandPAB will only check:
;	RECLEN, var/fixed, program/data
;	If PAB says APPEND and type is fixed, error

	jnc	DONUMatch

	dskseterror e_badopenmode
	jmp	DSKOOut

DONUMatch:
	dskseterror 0

	call	DSKinitsecptrs
	call	DSKreadsector
	jmp	DSKOOut

DONUMake:
	cmp	ax,F_MAKEFDR
	je	DONUMakeFDR

	int	3			; invalid code

DONUMakeFDR:
	call	DSKmakefdr   		; will write it, too
	jnc	DONUMadeFDR

	dskseterror e_outofspace
	jmp	DSKOOut
	
DONUMadeFDR:
	call	DSKcomparefdrandpab
	jnc	DONUMatch

	int	3			; makefdr or comparefdrandpab is wrong
	jmp	DSKOOut




;******************************  Output mode

DONMode1:
	cmp	al,m_output
	jne	DONMode2

	call	openfilewrite
	jnc	DONOOpened
	jmp	DSKOOut

;	SI = curfile
;	AX = how to open

DONOOpened:
	cmp	ax,F_MAKEFDR
	je	DONOMakeFDR

	int	3			; invalid code

DONOMakeFDR:
	call	DSKmakefdr
	jnc	DONOMadeFDR
	
	dskseterror e_outofspace
	jmp	DSKOOut

DONOMadeFDR:

	call	DSKcomparefdrandpab
	jnc	DONOMatch

	int	3			; makefdr or other is invalid

DONOMatch:
	dskseterror 0

	call	DSKinitsecptrs

	jmp	DSKOOut


;***************************	 Input mode

DONMode2:
	cmp	al,m_input
	jne	DONMode3

	call	openfileread
	jnc	DONIOpened
	jmp	DSKOOut

;	SI = curfile
;	AX = how to open

DONIOpened:
	cmp	ax,F_READFDR
	je	DONIReadFDR

	int	3			; invalid code

DONIReadFDR:

	call	DSKreadfdr
	jnc	DONIGoodFDR

	dskseterror e_hardwarefailure
	jmp	DSKOOut

DONIGoodFDR:
	call	DSKcomparefdrandpab
	jnc	DONIMatch

	dskseterror e_badopenmode
	jmp	DSKOOut

DONIMatch:
	dskseterror 0

	call	DSKinitsecptrs
	call	DSKreadsector
	jmp	DSKOOut

;***************************	Append

DONMode3:
;	cmp	al,m_append	; must be this

	call	openfileappend
	jnc	DONAOpened
	jmp	DSKOOut

DONAOpened:
	cmp	ax,F_MAKEFDR
	jne	DONACheckIfReadFDR


;	Append -- create the file

DONANewFile:
	call	DSKinitsecptrs	
	call	DSKmakefdr
	jnc	DONAMadeFDR

	dskseterror e_outofspace
	jmp	DSKOOut

DONAMadeFDR:
	call	DSKcomparefdrandpab
	jnc	DONAMatch

	int	3   			; mismatch is invalid

DONAMatch:
	dskseterror 0	
	
	jmp	DSKOOut


DONACheckIfReadFDR:	
	cmp	ax,F_READFDR
	je	DONAReadFDR
	
	int	3			; error

;	Append -- open the file

DONAReadFDR:
	call	DSKreadfdr
	jc	DONANewFile

	call	DSKcomparefdrandpab
	jc	DONANewFile

	mov	bx,pabaddr
	mov	al,[bx].pflags
	mov	ah,al
	and	ah,6
	shr	ah,1
	cmp	ah,m_append
	jne	DONANoVarErr

	test	al,fp_variable
	jnz	DONANoVarErr	; can't append fixed

	dskseterror e_badopenmode
	jmp	DSKOOut

DONANoVarErr:
;	Point to the end of the file

	call	DSKseekend

DSKOOut:

;	Even if error, who cares, BUT
;	save the PAB.

	lea	di,[si].pab
	mov	si,pabaddr
	push	es
	push	ds
	pop	es
	mov	cx,9
	rep	movsb
	pop	es

	ret
	endp




;	The following functions open files.
;	There is nothing disk-specific, except
;	the return code, which can be ignored.

;	Open a file in UPDATE mode.
;	SI = curfile
;	Sets error if necessary, and sets carry.
;	Returns AX how to get FDR
openfileupdate	proc	near
	push	bx
	push	cx
	push	dx
	mov	ah,3dh			; try to open first
	mov	al,2h			; read/write access
	lea	dx,fname
	int	21h			

	mov	si,curfile
	mov	[si].handle,ax		; save handle (or error)
	mov	[si].openmode,m_update
	jnc	ofuokay		

;	File does not exist so create it.
	mov	ah,3ch
	mov	cx,0
	int	21h
	mov	[si].handle,ax		; save real handle
	jnc	ofuwriteattribs
	;	Error:  no disk space or bad directory
	call	getextendederror
	cmp	al,ex_outofspace
	jne	ofubadpath
	dskseterror	e_outofspace
	jmp	ofuout
ofuokay:
	mov	[si].open,01
	mov	ax,F_READFDR
	clc
	jmp	ofuout

ofuwriteattribs:
	mov	[si].open,01
	mov	ax,F_MAKEFDR
	clc
	jmp	ofuout

ofubadpath:
	cmp	al,ex_badpath
	jne	ofuhardware
	dskseterror	e_badopenmode
	stc
	jmp	ofuout

ofuhardware:
	dskseterror	e_hardwarefailure
	stc
	jmp	ofuout

ofuout:
	pop	dx
	pop	cx
	pop	bx
	; don't reset SI=CURFILE.
	ret
openfileupdate	endp



;	Open a file in write-only mode.
;	SI = curfile
;	Sets error if necessary, and sets carry.
;	Returns AX = how to get FDR
openfilewrite	proc	near
	push	bx
	push	cx
	push	dx
	mov	ah,3ch
	mov	cx,0
	lea	dx,fname
	int	21h			; open file read-only
	mov	si,curfile
	mov	[si].handle,ax		; save handle
	mov	[si].openmode,m_output
	jnc	ofwokay
	dskseterror	e_outofspace	; file does not exist
	stc
	jmp	ofwout
ofwokay:
	mov	[si].open,01
	mov	ax,F_MAKEFDR
	clc
ofwout:
	pop	dx
	pop	cx
	pop	bx
	ret
openfilewrite	endp



;	Open a file in read-only mode.
;	SI = curfile
;	Sets error if necessary, and sets carry.
;	Returns AX = how to get FDR
openfileread	proc	near
	push	bx
	push	cx
	push	dx
	mov	ah,3dh
	mov	al,0
	lea	dx,fname
	int	21h			; open file read-only
	mov	si,curfile
	mov	[si].handle,ax		; save handle
	mov	[si].openmode,m_input
	jnc	oftrokay
	dskseterror	e_badopenmode	; file does not exist
	stc
	jmp	oftrout
oftrokay:
	mov	[si].open,01
	mov	ax,F_READFDR
	clc
oftrout:
	pop	dx
	pop	cx
	pop	bx
	ret
openfileread	endp



;	Open a file in APPEND mode.
;	SI = curfile
;	Sets error if necessary, and sets carry.
;	Returns AX is way to get FDR
openfileappend	proc	near
	push	bx
	push	cx
	push	dx
	mov	ah,3dh			; try to open first
	mov	al,2h			; write access
	lea	dx,fname
	int	21h			
	mov	si,curfile
	mov	[si].handle,ax		; save handle (or error)
	mov	[si].openmode,m_append
	jnc	ofaokay		
	;	File does not exist so create it.
	mov	ah,3ch
	mov	cx,0
	int	21h
	mov	[si].handle,ax		; save real handle
	jnc	ofawriteattribs
	;	Error:  no disk space or bad directory
	call	getextendederror
	cmp	al,ex_outofspace
	jne	ofabadpath
	dskseterror	e_outofspace
	stc
	jmp	ofaout

ofabadpath:
	cmp	al,ex_badpath
	jne	ofahardware
	dskseterror	e_badopenmode
	stc
	jmp	ofaout

ofahardware:
	dskseterror	e_hardwarefailure
	stc
	jmp	ofaout

ofaokay:
	mov	[si].open,01
	mov	ax,F_READFDR
	clc
	jmp	ofaout

ofawriteattribs:
	mov	[si].open,01
	mov	ax,F_MAKEFDR
	clc
ofaout:
	pop	dx
	pop	cx
	pop	bx
	ret
openfileappend	endp








;**********************************************
;************ CLOSE A FILE ********************
;**********************************************

DSKClose	proc	near
	pusha		; only because this is called from dskseterror.

;	Close the file.

;	It must be open!

	call	getfile	
	jnc	DCClose

	call	restorepab

DCErr:
	push	bx
	mov	bx,pabaddr
	and	byte ptr [bx+1],not 0e0h
	or	byte ptr [bx+1],e_badfiletype
	pop	bx
	jmp	DCOut

DCClose:
	cmp	[si].open,4
	je	DCOk

	cmp	[si].open,0
	je	DCOk
	cmp	[si].open,1
	jne	DCErr

	cmp	[si].openmode,m_input
	je	DCNoFDRUpdate

	cmp	[si].changed,0
	jz	DCNoSave
	call	DSKwritesector		; has been changed
DCNoSave:
	call	DSKwritefdr
	jc	DCspaceerror

DCNoFDRUpdate:
	mov	ah,3eh
	mov	bx,[si].handle
	or	bx,bx
	je	DCErr
	int	21h
	jnc	DCOk
	call	getextendederror
	cmp	al,ex_outofspace
	je	DCspaceerror

	push	bx
	mov	bx,pabaddr
	and	byte ptr [bx+1],not 0e0h
	or	byte ptr [bx+1],e_hardwarefailure
	pop	bx
	jmp	DCOk

DCspaceerror:
	push	bx
	mov	bx,pabaddr
	and	byte ptr [bx+1],not 0e0h
	or	byte ptr [bx+1],e_outofspace
	pop	bx

DCOk:
	mov	[si].open,2
	mov	[si].pabaddress,0ffffh
DCOut:
	popa
	ret
	endp


;*********************************************
;************** READ FROM A FILE *************
;*********************************************

DSKRead	proc	near
;	Make sure file exists	

	call	getfile
	jnc	DRFileHere

DRNotOpen:
	dskseterror e_badfiletype
	jmp	DROut
DRIllegal:
	dskseterror e_illegal
	jmp	DROut

DRFileHere:
	call	restorepab

        cmp     [si].open,4                     ; 4 means catalog read
	jne	DRTestNorm

	call	DSKreadcatalog
	jmp	DROut
DRTestNorm:

	cmp	[si].open,1
	jne	DRNotOpen

        cmp     [si].openmode,m_append          ; bad modes for a read
	je	DRIllegal
	cmp	[si].openmode,m_output
	je	DRIllegal

;	Depending on file type, read rec

	test	[di].flags,ff_variable
	jnz	DRVar
	jmp	DRFixed

;---------------------------------------

DREOF:
	mov	bx,pabaddr
	mov	[bx].charcount,0		; nothing read

	dskseterror e_endoffile
	jmp	DROut

;----------------------------------------

;       Variable read

DRVar:

;	With a variable record size:
;
;	Read length byte.  If >ff, go to next sector.
;

DRVLoop:
        cmp     [si].cureof,0                   ; ptr into current sector
	jnz	DRVGotARec

	call	DSKreadsector	; get CURSEC
	jne	DREOF

DRVGotARec:
	mov	bx,pabaddr
	lea	bp,ds:[si].info+O_SEC

	mov	al,[si].cureof
	xor	ah,ah
        add     bp,ax                    ; BP is addr for sector's record

	mov	cl,ds:[bp]
	inc	bp
	cmp	cl,0ffh
	jnz	DRVNotEOF

;	Well, we need to read a new sector
;

	cmp	[si].changed,0		; er... has this sector changed?
        jz      DRVNoSaveSector         ; can happen in UPDATE mode

	call	DSKwritefdr		
	call	DSKwritesector		; write old sector to file

DRVNoSaveSector:

	inc	[si].cursec	
	mov	[si].cureof,0

        mov     ax,[di].secsused        ; read past EOF?
	xchg	al,ah
	cmp	ax,[si].cursec
	jbe	DREOF

	jmp	DRVLoop

DRVNotEOF:
;	cl,bx,bp better be defined

	add	[si].cureof,cl
	inc	[si].cureof		; for length byte
	mov	[bx].charcount,cl

	mov	bx,[bx].addr
	xchg	bl,bh

	xor	ch,ch
	push	bx
	push	cx

DRVStuffRec:
	or	cl,cl
	jz	DRVDone

        mov     al,ds:[bp]              ; copy record
	and	bx,maxvdp
	mov	[bx],al
	inc	bp
	inc	bx
	dec	cl

	jmp	DRVStuffRec

DRVDone:

        call    VDPUpdate               ; notify VIDEO.ASM

	jmp	DROut

DRFEOF:
	jmp	DREOF

;-------------------------------------------------

;	Fixed

DRFixed:
	
;	With a fixed record size:
;	Need to check curnrec to read a new sector
;	and write the old one if changed
;


	mov	bx,pabaddr
	mov	ax,[bx].recnum
	xchg	al,ah
        and     ax,7fffh                ; only 32768 records
	mov	[si].currec,ax		; set record number

;	Figure the sector # and rec #

	mov	ax,[si].currec
	xor	dx,dx

	cmp	ax,[di].fixrecs
	jae	DRFEOF			; EOF!

	mov	cl,[di].recspersec
	xor	ch,ch

	div	cx

;	AX = sector #, DX = rec#

	cmp	ax,[si].cursec
	je	DRFSameSector		; same sector to read?

        cmp     [si].changed,0          ; er... has old sector changed?
	jz	DRFNoSaveSector

	call	DSKwritefdr		
	call	DSKwritesector		; write old sector to file

DRFNoSaveSector:
	mov	[si].cursec,ax		; update current sector
	call	DSKreadsector
	jne	DRFEOF

DRFSameSector:
	mov	[si].curnrecs,dl


;	Get the address for the sector buffer.

	lea	bp,ds:[si].info+O_SEC

	mov	al,[di].reclen
	mul	[si].curnrecs

	add	bp,ax			; bp -> current record in sector


;	Put the record there...

	mov	bx,pabaddr

	mov	cl,[di].reclen
	mov	[bx].charcount,cl	; get record length

	mov	bx,[bx].addr
	xchg	bl,bh			; get addr

	push	bx
	xor	ch,ch
	push	cx

DRFStuffRec:
        mov     al,ds:[bp]                      ; copy record
	and	bx,maxvdp
	mov	[bx],al
	inc	bx
	inc	bp
	dec	cl
	cmp	cl,0
	ja	DRFStuffRec

	call	VDPUpdate

;       Increment  record #

	inc	[si].currec
	mov	bx,pabaddr
	mov	ax,[si].currec
	xchg	al,ah
	mov	[bx].recnum,ax
	
DROut:

	ret
	endp


;=====================================================================
;	Read the current sector from the file
;
;	SI=curfile
;	DI=fdr
;
;	Returns C=1 if error
DSKreadsector	proc	near
       	
;	Assume nothing about the file type.
;
;	Seek to the cursec position and read the sector.

	push	ax
	push	bx
	push	cx
	push	dx

	mov	ax,256
	mul	[si].cursec
	add	ax,FDRSIZE
	adc	dx,0

	mov	cx,dx
	mov	dx,ax

	mov	ah,42h
	mov	al,00
	mov	bx,[si].handle
		
	int	21h			; seek to current sector
	jc	DrsOut

	mov	ah,3fh
	mov	bx,[si].handle
	mov	cx,256
	lea	dx,[si].info+O_SEC

	int	21h			; read sector
	cmp	ax,256			; returns error if not 256 bytes

	mov	[si].changed,0		; read==!changed

DrsOut:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp




;*********************************************
;************** WRITE TO FILE ****************
;*********************************************

DSKWrite	proc near

;	Make sure file exists	

	call	getfile
	jnc	DWFileHere

DWNotOpen:
	dskseterror e_badfiletype
	jmp	DWOut
DWIllegal:
	dskseterror e_illegal
	jmp	DWOut
DWOutofSpace:
	dskseterror e_outofspace
	jmp	DWOut
DWFileHere:
	call	restorepab

        cmp     [si].open,4                ; catalog WRITE?
	je	DWIllegal

DWTestNormal:
	cmp	[si].open,1
	jne	DWNotOpen

        cmp     [si].openmode,m_input           ; only bad mdoe for writing
	je	DWIllegal

;	Depending on file type, write rec

	test	[di].flags,ff_variable
	jnz	DWVar
	jmp	DWFixed

DWVar:

;	With variable, everything is sequential.

;	curfile.cursecs, curfile.cureof
;	are incremented along with fdr.secsused and fdr.fixrecs.

;	Empty sector = curfile.eof=0

;       See if there's space for the record

DWVLoop:
        cmp     [si].cureof,0           ; sector used up?
	jnz	DWVAllcd

	inc	[di].fixrecs		; increment # sectors written on
	mov	ax,[di].secsused
	add	ah,1
	adc	al,0
	mov	[di].secsused,ax

DWVAllcd:

	lea	bp,ds:[si].info+O_SEC
	mov	al,[si].cureof
	xor	ah,ah
	add	bp,ax		; bp -> where to put new record

	mov	bx,pabaddr
	mov	cl,[bx].charcount

        cmp     cl,[di].reclen  ; record longer than RECLEN?
	jbe	DWVLenOk

        mov     cl,[di].reclen  ; this may actually be **WRONG**
                                ; -- an error may occur instead

DWVLenOk:
        inc     cl              ; length byte
	add	al,cl		; al=new eof if no overflow

	jnc	DWVNoNewSec

;	This sector is full...

;	First time this is called, info is correct

	call	DSKwritefdr
	jc	DWOutOfSpace

	call	DSKwritesector		; always writes CURSEC
	jc	DWOutOfSpace

	inc	[si].cursec
	mov	[si].cureof,0
	jmp	DWVLoop


DWVNoNewSec:
	dec	cl		; # chars
	mov	ds:[bp],cl		; length byte
	inc	bp
	mov	bx,[bx].addr	; get VDP rec ptr
	xchg	bl,bh
DWVPushRec:
	or	cl,cl
	jz	DWVDone

        and     bx,maxvdp       ; write
	mov	ah,[bx]
	mov	ds:[bp],ah
	inc	bx
	inc	bp
	dec	cl
	jmp	DWVPushRec

DWVDone:
        mov     byte ptr ds:[bp],0ffh           ; EOF marker (really EOSector)

	mov	[si].cureof,al	; adjust EOF pointer
	mov	[di].eof,al	; adjust FDR
      	jmp	DWOut

;---------------------------------------------------

;	FIXED RECORDS

DWFixed:
	mov	bx,pabaddr
	mov	ax,[bx].recnum
	xchg	al,ah
	and	ax,7fffh
	mov	[si].currec,ax		; set record number

DWFixedLoop:

;	Here, it's important to update:
;
;	curfile.cursec, curfile.curnrecs
;   and	fdr.totsecs, fdr.fixrecs WHEN passing the EOF!



;	Calculate the correct sector for this record.

	mov	ax,[si].currec
	xor	dx,dx

	mov	cl,[di].recspersec
	xor	ch,ch

	div	cx

;	AX = # sectors,  DX = rec# in sector

	cmp	ax,[si].cursec
	je	DWFSameSector

	cmp	[si].changed,0
	jz	DWFNoSaveSector

        call    DSKwritefdr             ; update last changed sector
	call	DSKwritesector

DWFNoSaveSector:
	mov	[si].cursec,ax

;	READ the old sector now.

	call	DSKreadsector
	je	DWFSameSector			

;	Uh... it doesn't exist.  Extend the file.  (Add current sector.)

        call    DSKwritesector          ; this will extend ALL the gaps,
                                        ; not just add one sector to the file

DWFSameSector:
	mov	cx,[di].secsused
	xchg	cl,ch
	inc	cx				; #sectors = sec offs+1
	cmp	ax,cx
	jbe	DWFnoupdate

	mov	cx,ax
	inc	cx
	xchg	cl,ch
	mov	[di].secsused,cx
DWFnoupdate:

	mov	[si].curnrecs,dl


;	Get the address for the sector buffer.

	lea	bp,ds:[si].info+O_SEC

	mov	al,[di].reclen
	mul	[si].curnrecs

	add	bp,ax			; bp -> new buffer


;	Put the record there...

	mov	bx,[bx].addr
	xchg	bl,bh
	mov	cl,[di].reclen
DWFStuffRec:
	and	bx,maxvdp
	mov	al,[bx]
        mov     ds:[bp],al              ; copy
	inc	bx
	inc	bp
	dec	cl
	cmp	cl,0
	ja	DWFStuffRec

;	Increment fixrecs if this is the end of file

	inc	[si].currec		; current record increments
	mov	ax,[si].currec		; and may be = to total # of recs

	xchg	al,ah			; swap for TI-order
	mov	bx,pabaddr
	mov	[bx].recnum,ax
	xchg	al,ah			; intel-order

	cmp	ax,[di].fixrecs
	jbe	DWFDone

	mov	[di].fixrecs,ax

DWFDone:

DWOut:
	mov	[si].changed,1		; changed!

	ret
	endp

;===========================================================

;	Write the current sector to the file
;
;	SI=curfile
;	DI=fdr
;
;	Returns C=1 if error
DSKwritesector	proc	near
       	
;	Assume nothing about the file type.
;
;	Seek to the cursec position and write the sector.

	push	ax
	push	bx
	push	cx
	push	dx

	mov	ax,256
	mul	[si].cursec
	add	ax,FDRSIZE
	adc	dx,0

	mov	cx,dx
	mov	dx,ax

	mov	ah,42h
	mov	al,00
	mov	bx,[si].handle
		
	int	21h			; seek to current sector
	jc	DwsOut

	mov	ah,40h
	mov	bx,[si].handle
	mov	cx,256
	lea	dx,[si].info+O_SEC

	int	21h			; write sector

	mov	[si].changed,0		; writ==!changed

DwsOut:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp






;*********************************************
;************* SEEK TO RECORD ****************
;*********************************************

DSKSeek	proc	near

;	Make sure file exists	

	call	getfile
	jnc	DSkFileHere

DSkNotOpen:
	dskseterror e_badfiletype
	jmp	DSeekOut
DSkIllegal:
	dskseterror e_illegal
	jmp	DSeekOut

DSkFileHere:
	call	restorepab

        cmp     [si].open,4             ; catalog SEEK?
	je	DSkIllegal

	cmp	[si].open,1
	jne	DSkNotOpen

	test	[di].flags,ff_variable
	jz	DSkSeekRec

;	Can only restore a readable file

	cmp	[si].openmode,m_append
	je	DSkIllegal
	cmp	[si].openmode,m_output
	je	DSkIllegal

	mov	bx,pabaddr
	mov	[bx].recnum,0

	cmp	[si].changed,0
	jz	DSkNoSave

	call	DSKwritefdr
	call	DSKwritesector

DSkNoSave:
	mov	[si].currec,0
	mov	[si].curnrecs,0
	mov	[si].cursec,0
	mov	[si].cureof,0
	jmp	DSeekOut

DSkSeekRec:

;	Get record # and remove high bit.

	mov	bx,pabaddr
	mov	ax,[bx].recnum		; reversed order
	xchg	al,ah
	and	ax,not 8000h
	mov	[si].currec,ax		; read/write will save if necc.
	xchg	al,ah
	mov	[bx].recnum,ax

	lea	ax,[bx].recnum
	push	ax
	push	2
	call	VDPUpdate
DSeekOut:
	ret
	endp



;*********************************************
;************ LOAD MEMORY IMAGE **************
;*********************************************

DSKLoad	proc	near

;	Need to open a file 

;	If this pab is already open, close it

	call	ispabopen
	jnc	DLTryOpen		; C= found, and is open

	call	DSKClose

DLTryOpen:

;	Get a FILES record set up (SI)

	call	getfreefile
	jnc	DLGotRec

	dskseterror e_outofspace
	jmp	DLOut

DLGotRec:
	
;	Now, try to access the file through DOS

	mov	di,curfile
	lea	di,[di].info
	add	di,O_FDR

	mov	word ptr [di],2020h
	mov	word ptr [di+2],2020h
	mov	word ptr [di+4],2020h
	mov	word ptr [di+6],2020h
	mov	word ptr [di+8],2020h	; clear fname


;	Set up FNAME to be the DOS path to the file
;	and at DI will be put the TI filename

	call	getfilename
	jnc	DLNormalFile

;	This is a catalog request.
;	can't do this binary!

	dskseterror	e_badfiletype
	jmp	DLOut


DLNormalFile:

;	Try to open it

	call	openfileread
	jnc	DLOpened

	dskseterror	e_badfiletype
	jmp	DLOut

;	SI = curfile
;	AX = how to open

DLOpened:
	lea	di,[si].info+O_FDR

	cmp	ax,F_READFDR
	je	DLGetFDR

	int	3			; invalid code

DLGetFDR:
	call	DSKreadfdr
	jnc	DLReadFDR
	
	dskseterror e_hardwarefailure
	jmp	DLOut

DLReadFDR:

	call	DSKcomparefdrandpab
	jnc	DLMatch

	dskseterror e_badfiletype
	jmp	DLOut

DLMatch:
	dskseterror 0

	call	DSKinitsecptrs

	mov	bx,pabaddr

	mov	ax,[bx].recnum		; AX = max wanted bytes
	xchg	al,ah

	mov	dx,[di].secsused
	xchg	dl,dh
	cmp	[di].eof,0
	je	DLNoLessSec
	dec	dx	   
DLNoLessSec:
	shl	dx,8
	add	dl,[di].eof		; DX = # bytes in file

	cmp	dx,ax			; compare real num and max num
	jbe	DLSaveVal

 	mov	dx,ax			; take minimum

DLSaveVal:
	xchg	dl,dh
	mov	[bx].recnum,dx
	xchg	dl,dh

	mov	bx,[bx].addr
	xchg	bl,bh

DLGetSecs:
	or	dx,dx
	jz	DLDone

	call	DSKreadsector
	inc	[si].cursec

	mov	cx,256
	cmp	dx,256
	jae	DLCopyIt

	mov	cx,dx

DLCopyIt:				; CX= # bytes to copy
	sub	dx,cx

	and	bx,maxvdp		; keep range in VDP

	mov	ax,bx
	add	ax,cx
	cmp	ax,4000h
	jbe	DLNoVDPOverflow

	sub	ax,4000h		; get enough to up to edge
	sub	cx,ax			; take away from copy count
	add	dx,ax			; add back to total count

DLNoVDPOverflow:
	push	bx
	push	cx

	push	si
	lea	si,[si].info+O_SEC
DLStuffData:
	or	cx,cx
	jz	DLSecDone

	mov	al,[si]
	mov	[bx],al
	inc	si
	inc	bx
	dec	cx
	jmp	DLStuffData
DLSecDone:
	pop	si

	call	VDPUpdate

	jmp	DLGetSecs

DLDone:
	call	DSKclose	
DLOut:
	ret
	endp



;*********************************************
;************* SAVE MEMORY IMAGE *************
;*********************************************

DSKSave	proc	near
;	Need to open a file 

;	If this pab is already open, close it

	call	ispabopen
	jnc	DSTryOpen		; C= found, and is open

	call	DSKClose

DSTryOpen:

;	Get a FILES record set up (SI)

	call	getfreefile
	jnc	DSGotRec

	dskseterror e_outofspace
	jmp	DSOut

DSGotRec:
	
;	Now, try to access the file through DOS

	mov	di,curfile
	lea	di,[di].info
	add	di,O_FDR

	mov	word ptr [di],2020h
	mov	word ptr [di+2],2020h
	mov	word ptr [di+4],2020h
	mov	word ptr [di+6],2020h
	mov	word ptr [di+8],2020h	; clear fname


;	Set up FNAME to be the DOS path to the file
;	and at DI will be put the TI filename

	call	getfilename
	jnc	DSNormalFile

;	This is a catalog request.
;	can't do this binary!

	dskseterror	e_badopenmode
	jmp	DSOut


DSNormalFile:

;	Try to open it

	call	openfilewrite
	jnc	DSOpened
	jmp	DSOut

;	SI = curfile
;	AX = how to open

DSOpened:
	lea	di,[si].info+O_FDR

	cmp	ax,F_MAKEFDR
	je	DSGetFDR

	int	3			; invalid code

DSGetFDR:
	call	DSKmakefdr
	jnc	DSMadeFDR
	
	dskseterror e_outofspace
	jmp	DSKOOut

DSMadeFDR:

	call	DSKcomparefdrandpab
	jnc	DSMatch

	int	3			; should work

DSMatch:
	dskseterror 0

	call	DSKinitsecptrs

	mov	bx,pabaddr

	mov	dx,[bx].recnum		; AX = bytes to write
	xchg	dl,dh

	mov	[di].eof,dl

	mov	ax,dx
	shr	ax,8			; get # sectors
	cmp	dl,0
	je	DSNoExtSec		; extra?
	inc	ax			; one more sector
DSNoExtSec:
	xchg	al,ah
	mov	[di].secsused,ax


DSSaveVal:
	mov	bx,[bx].addr
	xchg	bl,bh

DSGetSecs:
	or	dx,dx			; # bytes left to write
	jz	DSDone

	mov	cx,256
	cmp	dx,256
	jae	DSCopyIt

	mov	cx,dx

DSCopyIt:				; CX= # bytes to copy
	sub	dx,cx

	and	bx,maxvdp		; keep range in VDP

	mov	ax,bx
	add	ax,cx
	cmp	ax,4000h
	jbe	DSNoVDPOverflow

	sub	ax,4000h		; get enough to up to edge
	sub	cx,ax			; take away from copy count
	add	dx,ax			; add back to total count

DSNoVDPOverflow:

	push	si
	lea	si,[si].info+O_SEC
DSStuffData:
	or	cx,cx
	jz	DSSecDone

	mov	al,[bx]
	mov	[si],al
	inc	si
	inc	bx
	dec	cx
	jmp	DSStuffData
DSSecDone:
	pop	si

	call	DSKwritesector
	inc	[si].cursec

	jmp	DSGetSecs

DSDone:
	call	DSKClose	
DSOut:
	ret
	endp




;********************************************
;*************** DELETE FILE ****************
;********************************************

;       !!! Doesn't return error for trying to delete read-only file !!!
;
;

DSKDelete	proc	near

;	If this pab is already open, close it

	call	ispabopen
	jnc	DDTryOpen		; C= found, and is open

	call	DSKClose

DDTryOpen:

;	Set up FNAME to be the DOS path to the file
;	and at DI will be put the TI filename

	lea	di,fname+96-10

	call	getfilename
	jnc	DDNormalFile

;	This is a catalog request.
;	can't delete the catalog!

	dskseterror	e_illegal
	jmp	DDOut


DDNormalFile:

;	We have the filename, so delete it.

	mov	ah,41h
	lea	dx,fname
	int	21h		; otherwise no error

DDOut:
	ret
	endp


;*********************************************
;************** SCRATCH RECORD ***************
;*********************************************

;       
;       Not implemented on 99/4A
;

DSKScratch	proc	near
	call	getfile
	jnc	DScHere
	dskseterror e_badfiletype
	jmp	DScOut

DScHere:
	dskseterror e_illegal
DScOut:
	ret
	endp



;*********************************************
;**************** STATUS *********************
;*********************************************

DSKGetStatus	proc	near
;	Make sure file exists	

	mov	dh,0				; we didn't open the file
						; to get this info

	call	getfile
	jc	DStOpenFile
	cmp	[si].open,1
	jne	DStOpenFile
	jmp	DStFileHere

DStOpenFile:

;	Need to open the file and get the FDR...

	call	getfreefile
	jnc	DStGotRec

	dskseterror e_outofspace
	jmp	DStDontClose

DStGotRec:
	
;	Now, try to access the file through DOS

	mov	di,curfile
	lea	di,[di].info
	add	di,O_FDR

	mov	word ptr [di],2020h
	mov	word ptr [di+2],2020h
	mov	word ptr [di+4],2020h
	mov	word ptr [di+6],2020h
	mov	word ptr [di+8],2020h	; clear fname


;	Set up FNAME to be the DOS path to the file
;	and at DI will be put the TI filename

	call	getfilename
	jnc	DStNormalFile

;	This is a catalog request.

	mov	dl,01010000b
	jmp	DStNoEOFStats

DStNormalFile:

;	Try to open it

	call	openfileread
	jnc	DStOpened
	jmp	DStFileHere


DStOpened:
	lea	di,[si].info+O_FDR

	cmp	ax,F_READFDR
	je	DStGetFDR

	int	3			; invalid code

DStGetFDR:
	call	DSKreadfdr
	jnc	DStReadFDR
	
	dskseterror e_hardwarefailure
	jmp	DStOut


DStNotOpen:
	dskseterror e_badfiletype
	jmp	DStOut
DStIllegal:
	dskseterror e_illegal
	jmp	DStOut

DStReadFDR:
	mov	dh,0ffh

DStFileHere:

	xor	dl,dl			; status byte

	push	dx
	mov	ah,43h
	mov	al,0
	lea	dx,fname
	int	21h			; get file attributes
	pop	dx
	jnc	DStFileExists

	or	dl,80h			; file does not exist

DStFileExists:
	test	cx,1		
	jz	DStNotProtected

	or	dl,40h			; read-only

DStNotProtected:
	test	[di].flags,ff_internal
	jz	DStNotInternal

	or	dl,10h			; internal file

DStNotInternal:
	test	[di].flags,ff_program
	jz	DStNotProgram

	or	dl,8			; program file

DStNotProgram:
	test	[di].flags,ff_variable
	jz	DStNotVariable

	or	dl,4			; variable length

DStNotVariable:
	cmp	dh,0ffh
	je	DStNoEOFStats

;	Get disk free space to tell this

	push	dx			; save EOF flag

	mov	bx,dskpath
	mov	dl,[bx]
	sub	dl,64

	mov	ah,36h			; get disk info

	int	21h
	pop	dx
	cmp	ax,0ffffh
	je	DStEnough
	cmp	bx,0
	ja	DStEnough		; space left on disk
	or	dl,2

DStEnough:

;	Now see if file pointer is at EOF

	test	[di].flags,ff_variable
	jnz	DStEOFVarTest
	jmp	DStEOFFixTest

DStEOFVarTest:

;	VAR file's at EOF if cureof points to >ff
;	AND cursec is = secsused.

	mov	ax,[di].secsused
	xchg	al,ah

	mov	bp,[si].cursec
	cmp	[si].cureof,0
	jz	DStEVTNoIncSec

	inc	bp

DStEVTNoIncSec:
	cmp	ax,bp
	ja	DStNotEOF

	lea	bp,ds:[si].info+O_SEC
	mov	al,[si].cureof
	xor	ah,ah
	add	bp,ax

	cmp	byte ptr ds:[bp],0ffh
	je	DStEOF
	jmp	DStNotEOF


DStEOFFixTest:

;	FIX file's at EOF is currec is equal to fixrecs

	mov	ax,[si].currec
	cmp	ax,[di].fixrecs
	jae	DStEOF
	jmp	DStNotEOF

DStEOF:
	or	dl,1

DStNotEOF:

DStNoEOFStats:	

	mov	bx,pabaddr
	mov	[bx].scrnoffs,dl

DStOut:
	cmp	dh,0ffh
	jne	DStDontClose

	call	DSKClose
	dskseterror 0
	mov	bx,pabaddr
	mov	[bx].opcode,9

DStDontClose:
	ret
	endp


;*************************************************************
;*************************************************************

;*********************************************************************
;	Read the catalog
;
;	currec indicates what to read
;
;	SI = curfile
;
;

DSKreadcatalog	proc	near

	mov	ax,[si].currec
	
	or	ax,ax
	je	DrcGetVolumeRecord
	jmp	DrcIsFirstQ


DrcGetVolumeRecord:
;	On the first record, return volume info
;	For this emulator, volume is simply the
;	name of the directory.

	push	si
	mov	ax,4300h
	mov	si,dskpath
	lea	di,filename
DrcCopyPath:
	mov	al,[si]
	or	al,al
	jz	DrcCopyCheck
	mov	[di],al
	inc	si
	inc	di
	jmp	DrcCopyPath

DrcCopyCheck:
	pop	si
	xor	al,al
	mov	[di-1],al		; clear last backslash

	mov	ax,4300h
	lea	dx,filename
	int	21h			; get info on dir
	jnc	DrcDirExists
	
	dskseterror e_hardwarefailure
	jmp	DrcOut

DrcDirExists:
	mov	bx,pabaddr
	mov	di,[bx].addr
	ror	di,8

	inc	di			; skip length byte
	mov	bx,dskpath
	call	dir2ti			; put volume name in record
	dec	di
	call	countlen

;;	lea	bx,[di+11]		; point to rest of record
  	mov	bx,di

;	Now, take the # secs on the disk and BCD it

;	Use DOS # secs

	push	bx    		; save VDP ptr

	mov	ah,36h
	mov	bx,dskpath
	mov	dl,[bx]
	cmp	dl,'Z'
	jbe	DrcNoLower
	sub	dl,32
DrcNoLower:
	sub	dl,64

	int	21h		; get drive allocation information

	inc	ax		; =FFFFh means no drive
	mov	bp,0		; available
	jz	DrcNoDrive	; no drive?

	mov	bp,bx		; save avail

	push	di
	inc	ax		; restore 
	mov	di,ax		; store secs/clust

	mul	dx
	push	ax		; save secs/drive

	mov	ax,cx		; ax=bytes/sec
	mov	cx,256
	xor	dx,dx 		
	div	cx		; ax=secsize/256
	mov	cx,ax		; cx=divisor

	pop	ax		; ax=secs/drive
	xor	dx,dx
	div	cx   		; div to get 256-byte sectors

	xchg	bp,ax

	mul	di
	xor	dx,dx
	div	cx   		; do same for avail

	pop	di
	xchg	bp,ax


;	AX = # TI-sized sectors

	pop	bx		; Bx = vdp

DrcNoDrive:
	push	ax

	xor	ax,ax	     	; first value is 0
	call	bcd

	pop	ax 		; then total
	call	bcd

	mov	ax,bp		; AX =avail
	call	bcd
	

	mov	bp,pabaddr
	mov	cx,ds:[bp].addr
	xchg	cl,ch
	add	cl,ds:[bp].preclen	; supposed to be this long
	adc	ch,0

DrcBlankRest:
	cmp	bx,cx
	jae	DrcBlanked
	mov	byte ptr [bx],0
	inc	bx
	jmp	DrcBlankRest

DrcBlanked:
	mov	al,ds:[bp].preclen
	mov	ds:[bp].charcount,al


;	Execute FINDFIRST to get the first filename

	call	setupdta


	mov	[si].cureof,0		; 0=more files avail
	jc	DrcNoFiles
	jmp	DrcOut

DrcNoFiles:
;;;	mov	[si].cureof,0ffh	; no error HERE...  return 128 blank 
					; records.
	jmp	DrcOut

DrcIsFirstQ:

;	One file has already been found.  Return it and find a new one.

	mov	bx,pabaddr
	mov	bx,[bx].addr
	xchg	bl,bh

	cmp	[si].cureof,0
	je	DrcMore

	cmp	[si].currec,128		; can't have EOF on DOS...
	jb	DrcEmptyNotEOF		; can have >128 files...

	dskseterror e_endoffile
	jmp	DrcReallyOut

DrcEmptyNotEOF:
	mov	byte ptr [bx],0
	inc	bx
	xor	ax,ax
	call	bcd
	call	bcd
	call	bcd
	jmp	DrcOut


DrcMore:
	mov	di,bx			; DI pts to record
	inc	di
	lea	bx,DTAbuff.sname

	call	dos2ti
	dec	di
	call	countlen

;	Next is the TYPE.  Must open the file to check

;	Move the path into FNAME

;;	lea	bx,[di+11]		; point to rest of record
	mov	bx,di

	lea	di,fname
	mov	si,dskpath
	mov	cx,64
	call	concatasciiz

;	Move the filename into FNAME

	lea	si,DTAbuff.sname
	call	concatasciiz

	mov	byte ptr [di],0

	mov	si,curfile

	push	bx
	mov	ah,3dh
	mov	al,0
	lea	dx,fname
	int	21h
	mov	cx,ax
	pop	bx
	jc	DrcGetNext		; error??!

	push	bx
	mov	bx,cx
	mov	bp,cx
	mov	ah,3fh
	mov	cx,128
        lea     dx,[si].info+O_FDR      ; read FDR
	int	21h
	pop	bx
	jc	DrcGetNext		; error?!?!

	cmp	ax,128
	jb	DrcGetNext		; error!??!

	push	bx
	mov	bx,bp
	mov	ah,3eh
	int	21h			; close file
	pop	bx


;	Now check protection

	mov	ah,43h
	mov	al,0
	lea	dx,fname
	int	21h			; get file attribs

	and	cl,1
	mov	dh,cl			; protection flag

;	Now figure out what type to call this

	lea	di,[si].info+O_FDR
	mov	al,[di].flags
	and	al,not 8
	lea	bp,DrcTrans
	mov	cx,5
DrcCmp:
	cmp	ds:[bp],al
	je	DrcMat
	add	bp,2
	loop	DrcCmp
	mov	al,1
	jmp	DrcSetType
DrcMat:
	mov	al,ds:[bp+1]

DrcSetType:
	push	bx
	xor	ah,ah
	call	bcd
	pop	bp
	cmp	dh,0
	je	DrcNotProtSet
	mov	dx,ds:[bp+1]
	xchg	dl,dh
	neg	dx
	xchg	dl,dh
	mov	ds:[bp+1],dx

DrcNotProtSet:

;	Now send the length

	mov	ax,[di].secsused
	xchg	al,ah
	inc	ax			; for sector header
	call	bcd

;	Now send the record length

	mov	al,[di].reclen
	xor	ah,ah
	call	bcd


DrcGetNext:

;	Get next file

	mov	ah,4fh
	int	21h

	mov	si,curfile
	mov	[si].cureof,0
	jnc	DrcOut

	mov	[si].cureof,0ffh


DrcOut:
	mov	bp,pabaddr
	
	mov	ax,ds:[bp].addr
	xchg	al,ah
	push	ax
	mov	al,ds:[bp].preclen
	xor	ah,ah
	push	ax
	call	VDPUpdate

	inc	[si].currec
	mov	ax,[si].currec
	xchg	al,ah
	mov	ds:[bp].recnum,ax

DrcReallyOut:
	ret
	endp


;	COUNT the length of the filename following.
;	
;	Inputs:		DI=ptr to length byte, filename (10 char max) follows
;	Outputs:	[ORG di]=length, DI points past filename
;
countlen proc	near
	push	ax
	push	bx
	push	cx

	xor	cx,cx			; current length
	lea	bx,[di+1]		; pt to filename
ctlloop:
	mov	al,[bx]
	cmp	al,' '			; only legal terminator
	je	ctldone			
	inc	bx
	inc	cx
	cmp	cx,10
	jb	ctlloop
ctldone:
	mov	[di],cl
	mov	di,bx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;       Convert AX into BCD (BCD???  RADIX-100, floating-point.)
;
;	AX=val
;	BX=string addr


bcd	proc	near
	push	ax
	push	dx
	push	cx

	mov	byte ptr [bx],8
	inc	bx

	cmp	ax,10000
	jae	bcd10000
	cmp	ax,1000
	jae	bcd1000
	cmp	ax,100
	jae	bcd100
	cmp	ax,10
	jae	bcd10
	or	ax,ax
	jne	bcd1

	mov	word ptr [bx],0
	mov	word ptr [bx+2],0
	mov	word ptr [bx+4],0
	mov	word ptr [bx+6],0
	add	bx,8
	jmp	bcdout

bcd1:
bcd10:
	mov	byte ptr [bx],40h
	mov	[bx+1],al
	mov	word ptr [bx+2],0
	mov	word ptr [bx+4],0
	mov	word ptr [bx+6],0
	add	bx,8
	jmp	bcdout
bcd100:
bcd1000:
	mov	byte ptr [bx],41h

	xor	dx,dx
	mov	cx,100
	div	cx

;	AX = 100's, DX=rem

	mov	[bx+1],al
	mov	[bx+2],dl
	mov	word ptr [bx+3],0
	mov	word ptr [bx+5],0
	mov	byte ptr [bx+7],0
	add	bx,8
	jmp	bcdout

bcd10000:
	mov	byte ptr [bx],42h

	xor	dx,dx
	mov	cx,100
	div	cx

;	AX = 100's, DX=rem

	mov	[bx+3],dl
	xor	dx,dx
	div	cx

;	AX = 100's, DX=rem

	mov	[bx+1],al
	mov	[bx+2],dl
	mov	word ptr [bx+4],0
	mov	word ptr [bx+6],0
	add	bx,8

bcdout:
	pop	cx
	pop	dx
	pop	ax
	ret
	endp




;*******************************************************
;********** LOW-LEVEL access routines!!!
;*******************************************************

;  Need to save no registers.

H834c	equ	byte ptr 0834dh
H834d	equ	byte ptr 0834ch
H8350	equ	byte ptr 08351h

OKAY		equ	0
OUTOFSPACE	equ	080h
CANTOPENFILE	equ	021h
FILENOTFOUND	equ	021h
BADFUNCERR	equ	007h		; format
BADVALERR	equ	021h


dddd_goodexit:
	pop	wp
	add	word ptr es:[wp+R11],2
	push	wp
	jmp	dddOut

dddd_badexit:
	jmp	dddOut

DDDD_10:
;	Sector read/write

;	Only allow access to the volume name

	mov	bl,es:H834C
	cmp	bl,MAXDRIVE
	jbe	D10gooddrive
	jmp	d10err
d10gooddrive:
	dec	bl
	xor	bh,bh
	shl	bx,6
	add	bx,offset tidiskpathname	; BX points to holder
						; of volume name
	mov	dskpath,bx

	READWORD 8350H,ax			; sector #
	or	ax,ax
	jz	d10getvol
	cmp	ax,1
	je	d10getind
	jmp	d10_2

d10getvol:
	call	createsector0
	jmp	d10okay

d10getind:
	call	createsector1
	jmp	d10okay

d10_2:
	cmp	ax,maxdirectoryrecord
	jae	d10err
	call	createfdrsector
	jmp	d10okay

d10err:
	call	createsector0
	jmp	d10okay

d10okay:
	READWORD 834eh,ax
	push	ax
	mov	ax,256
	push	ax
	call	VDPUpdate

	READWORD 8350h,ax
	mov	word ptr es:[834ah],ax	; returned sector # for verify
	mov	es:[H8350],OKAY
	jmp	dddd_goodexit


;	Create Sector 0 for a fake directory read.
;
;
createsector0 proc near

	mov	di,es:[834eh]		; fill in "volume" name
	mov	bx,dskpath
	call	dir2ti

d10dofake:
	mov	si,es:[834eh]
	mov	ax,168h
	xchg	al,ah
	mov	word ptr [si+10],ax	; fill in info
	mov	byte ptr [si+12],9	; for a 90k disk
	mov	byte ptr [si+13],'D'
	mov	byte ptr [si+14],'S'
	mov	byte ptr [si+15],'K'
	mov	byte ptr [si+17],40
	mov	byte ptr [si+18],1
	mov	byte ptr [si+19],1

	mov	cx,256-56
	add	si,56			; point to allocation bit map
	mov	al,0ffh
d10fillused:
	mov	[si],al
	inc	si
	loop	d10fillused

	ret
	endp


;	Setup a DTA to search for files, and execute FINDFIRST.
;
;	Expects DSKPATH, FNAME, and DTABUFF to be used.
;
setupdta proc	near
	push	ax
	push	bx
	push	cx
	push	dx
	push	di

	mov	ah,1ah
	lea	dx,DTAbuff
	int	21h			; set the DTA to a buffer

	lea	bx,fname
	mov	di,dskpath
sudmovepath:
	mov	al,[di]
	or	al,al
	mov	[bx],al
	jz	sudmoved
	inc	di
	inc	bx
	jmp	sudmovepath

sudmoved:
	mov	ah,'*'
	mov	[bx],ah
	mov	[bx+2],ah
	mov	ah,'.'
	mov	[bx+1],ah
	xor	ah,ah
	mov	[bx+3],ah

	mov	ah,4eh
	xor	cx,cx
	lea	dx,fname
	int	21h

	pop	di
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp




;	Create the File Index sector for a fake directory read.
;
;	Will simply scan the directory for files, and count how
;	many there are.
;	
createsector1 proc near
	call	setupDTA			; do a FINDFIRST

	READWORD 834Eh,ax			; VDP pointer to buffer
	mov	di,ax
	xor	bx,bx
	mov	si,2				; sector number to start with

	mov	ah,4eh
	xor	cx,cx
	lea	dx,fname
	int	21h
	jc	cs1fillnull			; last one, clear buffer

cs1loop:
	ror	si,8
	mov	[bx+di],si
	add	bx,2
	ror	si,8
	inc	si
	cmp	bx,127*2			; 127 files max
	jae	cs1fillnull

	mov	ah,4fh				; find next
	int	21h
	jnc	cs1loop				; CY=1 means no more files

cs1fillnull:
	mov	maxdirectoryrecord,si		; save last legal sector
cs1fillnullloop:
	xor	si,si
	mov	[bx+di],si
	add	bx,2
	cmp	bx,256*2
	jb	cs1fillnullloop

	call	setupDTA

	ret
	endp


;	This routine expects SETUPDTA to have been executed by the
;	user reading sector 1.
;
createfdrsector proc	near
	push	ax

	mov	si,dskpath			; point to current path
	lea	di,fname
	mov	cx,64
	call	concatasciiz			; put pathname into record
	lea	si,DTAbuff.sname		; point to name FINDxxx gave
	call	concatasciiz
	xor	al,al
	mov	[di],al				; clear '$'

	mov	ah,3dh
	mov	al,0
	lea	dx,fname
	int	21h
	jc	cfserr				; can't open file??

	mov	bx,ax
	mov	ah,3fh
	mov	cx,FDRSIZE
	READWORD 834Eh,dx			; get ptr to buffer
	int	21h				; read
	cmp	ax,cx
	pushf

	mov	ah,3eh
	int	21h				; close file


	mov	ah,4fh
	int	21h				; get next file in wildcard

	popf
	jb	cfserr				; error!  (put here, because
						; a non-TI file needs to be
						; skipped over in any case

	clc
	jmp	cfsout
cfserr:
	stc
cfsout:
	pop	ax
	ret
	endp



;------------------------------------------

;	Format disk
;
DDDD_11:
	mov	es:[H8350],OKAY
	jmp	dddd_goodexit

;--------------------------------------------

;	Modify file protection
DDDD_12:
	cmp	es:H834c,MAXDRIVE
	jbe	d12gooddrive
	
	jmp	dddd_badexit

d12gooddrive:
	mov	bx,es:[834eh]
	lea	di,fname
	call	get10charname

	mov	ah,43h
	mov	al,1
	mov	cx,1			; set to read-only
	cmp	es:[h834d],0
	jnz	d12doprotect
	xor	cx,cx
d12doprotect:
	lea	dx,fname
	int	21h
	jnc	d12out
d12err:
	mov	es:[H8350],FILENOTFOUND
	jmp	dddd_goodexit
d12out:
	mov	es:[H8350],OKAY
	jmp  	dddd_goodexit


;*****************************************
;	Rename
;*****************************************
DDDD_13:
	cmp	es:H834c,MAXDRIVE
	jbe	d13gooddrive
	jmp	dddd_badexit
d13gooddrive:
	lea	di,fname
	mov	bx,es:[8350h]		; old
	call	get10charname

	call	getfreefile
	jnc	d13gotrec

	mov	es:[H8350],CANTOPENFILE
	jmp	dddd_goodexit

D13GotRec:
	lea	di,[si].info+O_FDR
	call	openfileupdate
	jc	d13notopen
	call	DSKreadfdr
	jnc	d13havefile
d13notopen:
	mov	es:[H8350],FILENOTFOUND
	jmp	dddd_goodexit
d13havefile:
	mov	bx,es:[834eh]		; new
	mov	ax,[bx]			; move filename
	mov	[di],ax
	mov	ax,[bx+2]
	mov	[di+2],ax
	mov	ax,[bx+4]
	mov	[di+4],ax
	mov	ax,[bx+6]
	mov	[di+6],ax
	mov	ax,[bx+8]
	mov	[di+8],ax
	call	uppercase
	call	DSKwritefdr

	mov	bx,es:[834eh]
	lea	di,fname2
	call	get10charname

	push	es
	push	ds
	pop	es

	mov	ah,56h
	lea	dx,fname
	lea	di,fname2
	int	21h
	pop	es
	jnc	D13Out

d13err:
	mov	es:[H8350],FILENOTFOUND
	jmp	dddd_goodexit
d13out:
	mov	es:[h8350],OKAY
	jmp	dddd_goodexit

;---------------------------------------------------

;	Access direct input file
;
DDDD_14:
	cmp	es:h834c,MAXDRIVE
	jbe	d14gooddrive
	jmp	dddd_badexit
d14gooddrive:
	lea	di,fname
	mov	bx,es:[834eh]
	call	get10charname

;	Need to open a file 
;	Get a FILES record set up (SI)

	call	getfreefile
	jnc	D14GotRec

	mov	es:[h8350],CANTOPENFILE
	jmp	dddd_goodexit

D14GotRec:
	lea	di,[si].info+O_FDR
	call	openfileread
	jc	D14NotOpen
	call	DSKreadfdr
	jnc	D14Havefile
D14NotOpen:
	mov	es:[H8350],FILENOTFOUND
	jmp	dddd_goodexit

D14HaveFile:
	mov	al,es:[H834d]
	or	al,al
	je	D14GetFDR
	jmp	D14GetSectors

;	Request for FDR info

D14GetFdr:
	mov	bl,es:[H8350]
	mov	bh,83h

	mov	ax,[di].secsused
	xchg	al,ah
	mov	es:[bx+2],ax		

	mov	al,[di].flags
	mov	es:[bx+5],al
	mov	al,[di].recspersec
	mov	es:[bx+4],al
	mov	al,[di].eof
	mov	es:[bx+7],al
	mov	al,[di].reclen
	mov	es:[bx+6],al	     ; offset for XOR-1
	mov	ax,[di].fixrecs
	mov	es:[bx+8],ax		

	mov	es:[H8350],OKAY
	jmp	D14CloseFile

D14GetSectors:

	mov	bl,es:[H8350]
	mov	bh,83h

;	BX points to read info
;	834d is the # of sectors to read

	xor	ah,ah		; already got AL; # to read
	xor	cx,cx		; actual # sectors read	

	mov	dx,es:[bx+2]	; start sector #
	mov	[si].cursec,dx	; set it for DSKreadsector

	mov	bx,es:[bx]	; get buffer start address

D14SecLoop:
	call	DSKreadsector	; into SI.info+O_SEC
	jc	D14GotAll

	lea	bp,[si].info+O_SEC

	push	cx

	push	bx		; for VDPUpdate
	mov	cx,256
	push	cx		; for VDPUpdate

D14MoveBytes:
	mov	dl,ds:[bp]
	and	bx,maxvdp
	mov	[bx],dl
	inc	bp
	inc	bx
	loop	D14MoveBytes

	call	VDPUpdate

	pop	cx

	inc	cx		; one more read
	inc	[si].cursec
	dec	ax		; # read counter
	jg	D14SecLoop

D14GotAll:

	mov	es:[H834D],cl
	jmp	D14CloseFile

D14CloseFile:
	mov	ah,3eh
	mov	bx,[si].handle
	int	21h
	
	mov	[si].open,0
	mov	[si].pabaddress,0ffffh	; forget file
					; (no close function)

	mov	es:[H8350],OKAY
	jmp	dddd_goodexit

;---------------------------------------------------

;	Access direct output file
;
DDDD_15:
	cmp	es:h834c,MAXDRIVE
	jbe	d15gooddrive
	jmp	dddd_badexit
d15gooddrive:
	lea	di,fname
	mov	bx,es:[834eh]
	call	get10charname

;	Need to open a file 
;	Get a FILES record set up (SI)

	call	getfreefile
	jnc	D15GotRec

	mov	es:[h8350],CANTOPENFILE
	jmp	dddOut

D15GotRec:
	lea	di,[si].info+O_FDR

	mov	al,es:[H834D]
	or	al,al
	je	D15SetFDR
	jmp	D15SetSectors

;	Set FDR info

D15SetFdr:

;	Create a file

	call	openfilewrite
	jnc	D15MadeFile

D15CantWrite:
	mov	byte ptr es:[H8350],OUTOFSPACE
	jmp	dddOut

D15MadeFile:
	mov	bl,es:[H8350]
	mov	bh,83h

;	Move the filename!

	push	si
	push	di
	push	cx

	lea	di,[si].info+O_FDR
	mov	si,es:[834eh]

	push	es
	push	ds
	pop	es

	cld
	mov	cx,5
	rep	movsw

	pop	es
	pop	cx
	pop	di
	pop	si
	

	mov	ax,es:[bx+2]	
	xchg	al,ah
	mov	[di].secsused,ax
	mov	al,es:[bx+5]
	mov	[di].flags,al
	mov	al,es:[bx+4]
	mov	[di].recspersec,al
	mov	al,es:[bx+7]
	mov	[di].eof,al
	mov	al,es:[bx+6]
	mov	[di].reclen,al
	mov	ax,es:[bx+8]
	mov	[di].fixrecs,ax
	call	uppercase
	call	DSKwritefdr
	jc	D15CantWrite

	mov	es:[H8350],OKAY

	jmp	D15CloseFile

D15CantReOpenFile:
	mov	es:[H8350],FILENOTFOUND
	jmp	dddd_goodexit


D15SetSectors:
;	Must open the file updatedly

	call	openfileupdate	; don't overwrite the FDR there...
	jc	D15CantReOpenFile


	mov	bl,es:[H8350]
	mov	bh,83h

;	BX points to write info
;	834d is the # of sectors to write

	mov	al,es:[H834D]
	xor	ah,ah		; AX=# to write
	xor	cx,cx		; actual # sectors written

	mov	dx,es:[bx+2]	; start sector #
	mov	[si].cursec,dx	; set it for DSKwritesector

	mov	bx,es:[bx]  	; get buffer start address

D15SecLoop:

	lea	bp,[si].info+O_SEC
	push	cx
	mov	cx,256
D15MoveBytes:
	and	bx,maxvdp
	mov	dl,[bx]
	mov	ds:[bp],dl
	inc	bp
	inc	bx
	loop	D15MoveBytes

	pop	cx

	call	DSKwritesector	; write it
	jc	D15GotAll

	inc	cx		; one more read
	inc	[si].cursec
	dec	ax		; # read counter
	jg	D15SecLoop

D15GotAll:
	mov	es:[H834D],cl

D15CloseFile:
	mov	ah,3eh
	mov	bx,[si].handle
	int	21h
	
	mov	[si].open,0
	mov	[si].pabaddress,0ffffh	; forget file
					; (no close function)

	mov	es:[H8350],OKAY
	jmp	dddd_goodexit

DDDD_16:
	mov	es:[H8350],0		; no error... whatever
	jmp	dddd_goodexit

;============================================================

;	Uppercase the FDR filename at [di]
;
;
uppercase proc near
	push	bx
	push	ax

	xor	bx,bx
ucloop:
	mov	al,[di+bx]
	cmp	al,'a'
	jb	ucup
	cmp	al,'z'
	ja	ucup
	sub	al,32
	mov	[di+bx],al
ucup:
	inc	bx
	cmp	bx,10
	jb	ucloop

	pop	ax
	pop	bx
	ret
	endp


get10charname proc	near
	push	ax
	mov	al,es:[H834C]
	mov	cx,10		; assume the person cleared out the rest
	call	fullname
	pop	ax
	ret
	endp

;===================================================================

;	Set up the file for reading/writing
;	from the beginning of the file
;
;	SI = curfile
;	DI = fdr
DSKinitsecptrs	proc	near

	mov	[si].cursec,0
	mov	[si].cureof,0
	mov	[si].curnrecs,0
	mov	[si].changed,0
	mov	[si].currec,0

	ret
	endp


;	Seek to end of file for Append
;	(Of course, like SEEK, this is only 
;	symbolic.  Read/Write sets dos fileptr.)
;
;	SI = curfile
;	DI = fdr
DSKseekend	proc	near
	push	bx
	mov	bl,[di].eof
	mov	[si].cureof,bl

	mov	ax,[di].secsused
	xchg	al,ah

	or	bl,bl
	jz	Dsenot
	dec	ax	      		; continue on last sector
Dsenot:
	mov	[si].cursec,ax
	mov	[si].curnrecs,0		; ignore for var
	mov	[si].currec,0

	call	DSKreadsector
	pop	bx
	ret
	endp


;====================================================================

;	DISK ONLY
;	Create an FDR  (SI=curfile) from PABADDR.
;
;	Write to file, too.  Truncate file at FDRSIZE.
;	Will handle reclen 0=>80 bytes

;	Returns DI = fdr, SI = curfile

DSKmakefdr	proc	near

	push	bx
	push	dx


;	Put the filename here

	call	DSKfdrfilename

	mov	bx,pabaddr
	mov	di,curfdr

	cmp	[bx].opcode,6		; program file
	jne	Dmfnoprog
	mov	dl,ff_program
	mov	[di].flags,dl
	mov	[di].recspersec,0
	mov	[di].reclen,0
	jmp	Dmfisprog
Dmfnoprog:
	

;	Fix FDR reclen

	mov	al,[bx].preclen
	or	al,al
	jne	Dmfnot80
	mov	al,80
	mov	[bx].preclen,al

Dmfnot80:
	mov	[di].reclen,al

;	Fix FDR flags

	mov	al,[bx].pflags
	xor	dl,dl

	test	al,fp_variable
	jz	Dmfnovar
	or	dl,80h
Dmfnovar:

	test	al,fp_internal
	jz	Dmfnoint
	or	dl,2
Dmfnoint:

	mov	[di].flags,dl

;	Initialize header

	mov	[di].secsused,0
	mov	[di].eof,0
	mov	[di].fixrecs,0

	mov	ax,256
	mov	dl,[di].reclen
	or	dl,dl
	jz	Dmfbad

	div	dl		; AL = #recs/sec

Dmfbad:
	mov	[di].recspersec,al

Dmfisprog:

;	Now, write the FDR

	call	DSKwritefdr
	jc	DmfErr

;	Truncate file

	mov	ah,40h
	mov	bx,[si].handle
	mov	cx,0		; will trunc
	int	21h

DmfErr:
	pop	dx
	pop	bx
	ret
	endp


;	Take filename from FILENAME and put it in the FDR
;
DSKfdrfilename	proc	near

	push	bx
	push	di
	lea	bx,FDRfilename
	mov	di,curfdr

	mov	cx,10
Dffn:
	mov	al,[bx]
	cmp	al,'a'			; we don't want lowercase...
	jb	Dffncap			; ambiguous in dos
	cmp	al,'z'
	ja	Dffncap
	sub	al,20h
Dffncap:
	mov	[di],al
	inc	bx
	inc	di
	loop	Dffn

	pop	di
	pop	bx
	ret
	endp



;	Write the FDR to the file...  that's all
;	SI = curfile
;	DI = fdr
;
;	Return C=1 if write error
DSKwritefdr	proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	mov	ah,42h
	mov	al,0
	mov	bx,[si].handle
	mov	cx,0
	mov	dx,cx
	int	21h		; to beginning
	jc	DSKwfErr

	mov	bx,[si].handle
	mov	ah,40h
	mov	cx,FDRSIZE
	mov	dx,di
	int	21h
	jc	DSKwfErr
	cmp	ax,FDRSIZE
	jmp	DSKwfOut

DSKwfErr:
DSKwfOut:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	Read an FDR from file
;
;	Returns C=1 if invalid (<FDRSIZE bytes)
;	Return DI=FDR
DSKreadfdr	proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	mov	ah,42h
	mov	al,0
	mov	bx,[si].handle
	mov	cx,0
	mov	dx,cx
	int	21h		; to beginning
	jc	DrfErr

	mov	di,curfdr
;	lea	di,[si].info+O_FDR 	; pt to FDR

	mov	ah,3fh
	mov	bx,[si].handle
	mov	cx,FDRSIZE
	mov	dx,di
	int	21h
	jc	DrfErr
	cmp	ax,FDRSIZE		; C=1 if not =FDRSIZE
	jc	DrfErr
	clc
	jmp	DrfOut
DrfErr:
	stc
DrfOut:

	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	Compare the FDR and PAB to make sure
;	they are compatible 
;
;	SI = curfile
;	DI = fdr
;
;	C=1 if error
DSKcompareFDRandPAB	proc	near
	push	ax
	push	bx

;	Make fixed/var attribs are the same

	mov	bx,pabaddr
	mov	al,[bx].pflags
	mov	ah,[di].flags

	test	ah,ff_program
	jz	DcfapData

	cmp	[bx].opcode,5
	je	DcfapSuccess
	cmp	[bx].opcode,6
	jne	DcfapError
	jmp	DcfapSuccess

DcfapData:
	test	al,fp_variable
	jz	DcfapFDRShouldBefixed

;	PAB says VARIABLE

	test	ah,ff_variable
	jz	DcfapError	; var<>fix

	jmp	DcfapTestReclen

DcfapFDRShouldBeFixed:
	test	ah,ff_variable
	jnz	DcfapError     	; fix<>var

DcfapTestReclen:
	mov	al,[bx].preclen
	or	al,al
	jnz	DcfapNot80
	mov	al,[di].reclen	; if 0, set to filesize
	mov	[bx].preclen,al
DcfapNot80:
	mov	ah,[di].reclen
	cmp	ah,al
	jnz	DcfapError	; reclens wrong

DcfaptestRelative:
	clc
	test	[bx].pflags,fp_relative
	jz	DcfapOk		; tests passed

	test	[bx].pflags,fp_variable
	jz	DcfapOk	
				; no var relative files
DcfapSuccess:
	clc
	jmp	DcfapOk
DcfapError:
	stc
DcfapOk:
	pop	bx
	pop	ax
	ret
	endp


;=====================================================================

;	GETFILENAME
;
;	Gets filename from VDP and concatenates it with TIDISKPATHNAME
;	in FNAME.
;
;	Sets C=1 if catalog

getfilename	proc	near
	push	ax
	push	bx
	push	cx
	push	dx
	push	di

	mov	bx,pabaddr		; point to the PAB filename
	add	bx,9
	mov	cl,[bx]
	sub	ch,ch			; get length of filename

	mov	bx,es:[8356h]		; get filename ptr

	inc	bx			; skip period (DSK1.)
	sub	cx,es:[8354h]		; lose length of device

	dec	cx			; and period

	cmp	cx,1
	jge	gfokayfilename

;	Catalog request... if applicable
	stc
	jmp	gfncout	

gfokayfilename:

	mov	al,curdev
	sub	al,D_DSK1-1		; get drive number

	lea	di,fname
	call	fullname		; create the filename

	lea	di,fdrfilename
	mov	ax,2020h
	mov	[di],ax
	mov	[di+2],ax
	mov	[di+4],ax
	mov	[di+6],ax
	mov	[di+8],ax

	mov	cx,bx			; where to stop
gffindstart:
	mov	al,[bx-1]
	cmp	al,'.'			; find the start
	je	gffoundstart
	dec	bx
	jmp	gffindstart

gffoundstart:
	cmp	bx,cx
	jae	gfgoodout
	mov	al,[bx]			; read TI filename from PAB...
	mov	[di],al			; fill FDRFILENAME
	inc	bx
	inc	di
	jmp	gffoundstart

gfgoodout:
	clc
gfncout:
	pop	di
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp






;==============================================================
;	Filename conversion routines
;==============================================================


;	TI2DOS takes a simple 10-char (or more, with paths)
;	TI filename and converts it into a valid DOS name.
;
;	Inputs:		BX points to TI filename
;			CX=length of this filename
;			DI points to destination for DOS-translated part.
;	Outputs:	BX pts to 1+last char processed

ti2dos	proc	near
	push	ax
	push	cx
	push	dx
	push	di
	push	bp

	xor	dx,dx		; period counter
	mov	bp,10		; max # chars
t2dputname:
	jcxz	t2dendname

	mov	al,[bx]
	cmp	al,' '
	je	t2dendname
	or	al,al
	je	t2dendname

	cmp	dx,8
	jne	t2dNoPer

	mov	ah,'.'	       	; insert DOS period
	mov	[di],ah
	inc	di

t2dNoPer:
	push	di
	push	es
	push	ds
	pop	es
	PUSH	CX

	lea	di,illegalchars
	mov	cx,illegallen

	repne	scasb
	jne	t2dNoBad     	; any invalid DOS chars?

	add	al,80h
	
t2dNoBad:
	POP	CX
	pop	es
	pop	di

	cmp	al,'a'
	jb	t2dnotLower
	cmp	al,'z'
	ja	t2dnotLower
	sub	al,20h		; make uppercase
t2dnotLower:
	mov	[di],al	
	inc	di
	inc	bx
	inc	dx
	dec	cx
	dec	bp
	jg	t2dputName
	
t2dendname:
	xor	al,al
	mov	[di],al		; null byte

	pop	bp
	pop	di
	pop	dx
	pop	cx
	pop	ax
	ret
	endp


;	DOS2TI takes a DOS filename and translates it into a TI name.
;
;	Only 8.3 length filenames allowed.
;
;
;	Inputs:		BX=input DOS filename
;			DI=destination for TI name
;	Outputs:	no regs modified

dos2ti	proc	near
	push	ax
	push	bx
	push	cx
	push	dx
	push	di

	xor	cx,cx			; # chars read
	xor	dx,dx			; # chars written
d2tloop:
	cmp	cx,11
	jae	d2tout			; ignore 11th char

	mov	al,[bx]
	inc	bx
	or	al,al
	jz	d2tout

	cmp	al,'\'			; pathname here... must be in DIR2TI
	je	d2tout

	cmp	al,'.'
	je	d2tgoloop		; ignore . for TI names

	cmp	al,'a'
	jb	d2tup
	cmp	al,'z'
	ja	d2tup
	sub	al,32			; uppercase it
d2tup:
	cmp	al,80h
	jb	d2tleg
	sub	al,80h			; make illegal into TI char
d2tleg:
	mov	[di],al			; shove char into buffer
	inc	di
	inc	dx
d2tgoloop:	
	inc	cx
	jmp	d2tloop

d2tout:
	mov	al,20h			; space out the rest

d2tclearout:
	cmp	dx,10
	jae	d2tdone

	mov	[di],al
	inc	di
	inc	dx
	jmp	d2tclearout

d2tdone:

	pop	di
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	FULLNAME creates the fully-qualified DOS name given
;	a legal TI drive number and a pointer to the TI filename.
;
;	Inputs:		AL=drive #
;			BX=pointer to TI filename
;			CX=length
;			DI=result location
;	Outputs: 	[DI] holds result.
;			BX=pts to char after last one processed

fullname proc	near
	push	ax
	push	si
	push	di

	xor	ah,ah
	dec	al
	shl	ax,6
	lea	si,tidiskpathname
	add	si,ax			; SI points to pathname spec

flncopy:
	mov	al,[si]			; copy pathname into buffer
	inc	si
	or	al,al
	jz	flncopied		; but not trailing null
	mov	[di],al
	inc	di
	jmp	flncopy

flncopied:
	call	ti2dos			; takes care of the rest

	pop	di
	pop	si
	pop	ax
	ret
	endp

;===========================================================================

;	MATCHDRIVE --	match a TI floppy volume name to one of the
;			directories in TIDISKPATHNAME.
;
;	Inputs:		DI=ptr to volume name (terminated with ' ',0,'.')
;	Outputs:	AL=drive number (0 if not matched)
;			DI=new ptr to filename

matchdrive	proc	near
	push	cx
	push	dx
	push	bx
	push	bp

	mov	bp,di			; save init ptr

	mov	dl,3			; act like DSK3-DSK5 are pathed
	test	features,FE_realdisk	; real DSR loaded?
	jnz	mdloop			; yup...

	mov	dl,1			; nope... all drives valid
mdloop:
	mov	cl,dl
	dec	cl
	xor	ch,ch
	shl	cx,6
	lea	bx,tidiskpathname
	add	bx,cx			; point to current path

	lea	di,fname
	call	dir2ti			; get a TI volume name in FNAME

	mov	di,bp

	mov	cx,10			; maximum possible length
	lea	bx,fname		; comparison buffer;  DI=pab
mdcomp:
	mov	al,[di]			; look at TI name (in a pab)
	or	al,al
	je	mdmatch			; end?
	cmp	al,' '
	je	mdmatch			; end?
	cmp	al,'.'
	je	mdmatch			; end?

	cmp	al,'a'
	jb	mdcompup
	cmp	al,'z'
	ja	mdcompup
	sub	al,32

mdcompup:
	cmp	al,[bx]
	jne	mdnomatch		; match?

	inc	di
	inc	bx
	loop	mdcomp

mdmatch:
	mov	al,[bx]			; be sure we don't match
	cmp	al,' '			; DSK.D.wow and xxx\DISK\
	je	mdrealmatch
	or	al,al
	jnz	mdnomatch

mdrealmatch:
	clc
	mov	al,dl
	jmp	mdout			; got it!  

mdnomatch:
	inc	dl
	cmp	dl,MAXDRIVE
	jbe	mdloop

	xor	al,al
	stc
mdout:
	pop	bp
	pop	bx
	pop	dx
	pop	cx
	ret
	endp


;	DIR2TI --	Take last directory out of TIDISKPATHNAME at DI
;			and DOS2TI it in FNAME.
;
;	Inputs:		BX=TIDISKPATHNAME
;			DI=dest for filename
;	Outputs:	[DI] is set (10-char block)

dir2ti	proc	near
	push	ax
	push	bx
	push	si
	push	di

dr2tfindend:
	cmp	byte ptr [bx],0		; find the end of the string
	je	dr2tgotend
	inc	bx
	jmp	dr2tfindend

dr2tgotend:
	dec	bx			; don't want null...

dr2tfindstart:
	mov	al,[bx-1]		
	cmp	al,'\'
	je	dr2tfoundstart
	cmp	al,':'
	je	dr2tfoundstart
	dec	bx
	jmp	dr2tfindstart

dr2tfoundstart:
	call	dos2ti			; TI-ize it

	pop	di
	pop	si
	pop	bx
	pop	ax
	ret
	endp
