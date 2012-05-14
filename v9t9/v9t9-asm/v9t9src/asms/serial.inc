; ********************************************
; SERIAL.INC include file for RS232 DSR emulation
; ********************************************
; by Edward Swartz  1995
; ********************************************

;       SERIAL.INC
;
;       RS232 file emulation routines
;

	.data




rsenders	dw	'EC',RSDEcho,'CR',RSDCR,'LF',RSDLF
		dw	'NU',RSDNU,'DA',RSDData,'BA',RSDBaud
		dw	'PA',RSDPar,'TW',RSDTW,'CH',RSDCH
		dw	'ec',RSDEcho,'cr',RSDCR,'lf',RSDLF
		dw	'nu',RSDNU,'da',RSDData,'ba',RSDBaud
		dw	'pa',RSDPar,'tw',RSDTW,'ch',RSDCH,0,0


RSBauds		dw	110,85AAh
		dw	300,849Ch
		dw	600,8271h
		dw	1200,01a1h
		dw	2400,809ch
		dw	4800,804eh
		dw	9600,8027h,-1,0

RSDatas		dw	7,0,8,1,-1,0

RSParities	db	'N',0,'O',30h,'E',20h
		db	'n',0,'o',30h,'e',20h,0,0

_CR	db	13
_LF	db	10


rsrec	struc
	baseaddr	dw	0	; CRU base
	cr		db	0	; whether to suppress CR's
	lf		db	0	; whether to suppress LF's
	null		db	0	; whether to output 4 nulls each line
	echo		db	0	; do not echo?

	setup		db	0	; setup byte
	baudrate	dw	0	; baud word
	ends

rsrec1	rsrec	<1340h,0,0,0>
rsrec2	rsrec	<1380h,0,0,0>
piorec1	rsrec	<0,0,0,0>

	.code


;********************************************
;********** RS232 DSR ***********************
;********************************************


doRs232int proc	near				; tackle RS232 interrupt	
	pusha
	lea	di,rsrec1
	mov	dx,16
	call	RS232tb
	jnz	DRIgoforit

	mov	dx,31
	call	RS232tb
	jnz	DRIreset

	lea	di,rsrec2
	mov	dx,16
	call	RS232tb
	jnz	DRIgoforit

	mov	dx,31
	call	RS232tb
	jnz	DRIreset
	jmp	DRIcont

DRIreset:
	lea	di,rsrec1
	mov	dx,31
	call	RS232sbo


	lea	di,rsrec2
	mov	dx,31
	call	RS232sbo

	jmp	DRIout

DRIgoforit:
	call	RS232checkavail			; char available?
	jnz	DRIavail			; yup
	jmp	DRIcont
DRIavail:
	READBYTE 8304h,bl			; get buffer pos
	inc	bl
	READBYTE 8302h,bh			; get limit
	cmp	bl,bh				; above yet?
	jbe	DRInowrap
      	xor	bl,bl				; yup, wraparound
DRInowrap:
	READBYTE 8303h,bh			; get current head 
	cmp	bl,bh
	je	DRIfull

	mov	cx,8
	call	RS232stcr			; get char in AL
	shr	ax,8

	mov	dx,9
	call	RS232tb				; error?
	jz	DRIstore

	mov	al,0ffh				; error
	jmp	DRIstore

DRIfull:
	mov	al,0feh
	READBYTE 8304h,bl			; get old pos

DRIstore:
	WRITEBYTE 8304h,bl			; store new pos
	xor	bh,bh
	READWORD 8300h,cx
	add	bx,cx				; point to VDP buffer
	and	bx,3fffh
	mov	[bx],al				; save
	push	bx
	push	1
	call	VDPupdate			; changed memory

DRIcont:					; tell RS232 thanks
	mov	dx,18
	call	RS232sbo

DRIout:
	popa
	ret
	endp



rsseterror	macro	num
	push	di
	mov	di,pabaddr
	and	byte ptr [di+1],not 0e0h
	or	byte ptr [di+1],num
	pop	di
IF	num EQ 0
ELSE
	call	RSClose
ENDIF
	endm




dors232dsr	proc	near
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
	jb	drderr
	cmp	bx,DSROP+20h
	jae	drderr

	sub	bx,DSROP
	add	bx,bx


;	Handle a subprogram or do file operations

        jmp	[word ptr cs:rs232dsrtable+bx]
	jmp	drdout
rs232dsrtable:
	dw	drdRS232
	dw	drdRS2321
	dw	drdRS2322
	dw	drdPIO

drderr:
	jmp	drdout	

drdout:
	pop	wp
	pop	es
	popa
	ret
	endp





drdRS232:

;	Assume RS232 by itself refers to RS232/1

	jmp	drdRS2321


drdRS2321:

;	Load appropriate pointers to RS232/1.

	mov	curdev,D_RS2321
	lea	di,rsrec1

	jmp	RSFiles


drdRS2322:

;	Load pointers for RS232/2

	mov	curdev,D_RS2322
	lea	di,rsrec2

	jmp	RSFiles


drdPIO:

;	Load pointers for PIO

	mov	curdev,D_PIO
	lea	di,piorec1

	jmp	RSFiles


RSFiles:

;	Now, branch off according to the opcode

	mov	ax,es:[8356h]
	sub	ax,es:[8354h]
	sub	ax,10

;	AX points to start of PAB.

	mov	pabaddr,ax
	mov	bx,ax			

	WRITEBYTE 835Dh,0
	mov	bl,[bx]
	test	bl,80h
	jz	RSNotInterruptable

	WRITEBYTE 835Dh,0ffh

RSNoTinterruptable:
	and	bl,7fh

	cmp	bl,9
	jbe	RSOkOp

	rsseterror e_illegal
	jmp	RSOut

RSOkOp:
	xor	bh,bh
	add	bx,bx
	call	[word ptr cs:RROpcodes+bx]

RSOut:
	pop	wp				; from dodiskdsr
	add	word ptr es:[wp+r11],2		; no error
	push	wp
	jmp	drdOut


RROpcodes:	
	dw	RROpen,RSClose,RSRead,RSWrite
	dw	RSSeek,RSLoad,RSSave,RSDelete
	dw	RSScratch,RSStatus


;**************************************************
;***************** OPEN
;**************************************************

RROpen	proc	near

	mov	[di].baudrate,049Ch	; 300 baud
	mov	[di].setup,0bah		; .DA=7.PA=O.CH 
					; (for default)
	READBYTE 835dh,ah
	mov	[di].cr,ah		; set supression of CRs
	mov	[di].lf,ah		; set supression of LFs
	mov	[di].null,0		; no nulls

RSOEndings:
	mov	al,es:[8354h]		; # chars taken out
	mov	bx,pabaddr
	add	bx,9
	cmp	al,[bx]			; same len (no ext)?
	jae	RSONoEnds

	mov	cl,[bx]
	xor	ch,ch

	mov	bx,es:[8356h]		; process extensions like .CR
	sub	cx,es:[8354h]		; and .BA=xxxx
	mov	bp,bx
	add	bp,cx
	dec	bp
	jmp	RSOEndLoop
RSONoEnds:
	jmp	RSOCheckOpenMode
RSOEndLoop:
	cmp	bx,bp
	jae	RSOCheckOpenMode
	inc	bx			; skip period
	mov	al,[bx]
	or	al,al
	je	RSOCheckOpenMode
	mov	ax,[bx]
	xchg	al,ah			; from VDP
	add	bx,2			; bump
	lea	si,rsenders		; SI cant be referenced
RSOFindEnd:
	cmp	word ptr [si],0
	je	RSOErr2

	cmp	[si],ax
	je	RSOEFound
	add	si,4
	jmp	RSOFindEnd

RSOErr2:
	rsseterror e_badopenmode
	jmp	RSOOut

RSOEFound:
	mov	ax,[si+2]		; get handler
	call	ax			; call it
	jc	RSOErr2
	jmp	RSOEndLoop

RSOCheckOpenMode:
	mov	bx,pabaddr
	mov	al,[bx].pflags		; check given flags

	test	al,fp_relative		; they want relative?
	jnz	RSOErr2			; haah!

	cmp	[bx].preclen,0		; no record length
	jne	RSOHaveLen		; defaults
					; to
	mov	[bx].preclen,80		; 80 bytes
	lea	ax,[bx].preclen
	push	ax
	push	1
	call	VDPUpdate		; we changed VDP data

RSOHaveLen:
	
;	Now, we dance

;	If it's a PIO, then skip this part
;	(Initialize communications port)

	cmp	curdev,D_PIO
	jae	RSOOut

	mov	dx,31
	call	RS232sbo			; reset our port

	mov	cx,8
	xor	ax,ax
	mov	ah,[di].setup
	call	RS232ldcr			; write setup byte

	mov	dx,13
	call	RS232sbz			; skip timer interval

	mov	cx,11
	mov	ax,[di].baudrate
	call	RS232ldcr			; set receive rate
	mov	cx,11				; ldcr ax,11
	call	RS232ldcr			; set transmit rate

	READBYTE 835Dh,al
	or	al,al
	jz	RSOOut

	mov	dx,18
	call	RS232sbo			; turn on interrupts

RSOOut:
	ret
	endp


;	Decipher a number (baud rate or databits)
;	Returns 0xffff if error
;
RSGetNum	proc	near
	push	cx
	push	dx

	xor	ax,ax
	mov	ch,0ffh		; no numbers found
	mov	cl,[bx]
	cmp	cl,'='
	jne	RSGNErr

RSGNLoop:
	inc	bx
	cmp	bx,bp
	ja	RSGNFind
	mov	cl,[bx]	
	cmp	cl,'.'
	je	RSGNFind
	or	cl,cl
	je	RSGNFind
	cmp	cl,'0'
	jb	RSGNErr
	cmp	cl,'9'
	ja	RSGNErr

	sub	cl,'0'
	xor	ch,ch		; number found
	mov	dx,10
	mul	dx
	add	ax,cx
	jmp	RSGNLoop
RSGNErr:
	mov	ax,-1
	stc
	jmp	RSGNOut

RSGNFind:
	or	ch,ch
	jne	RSGNErr

;	DI points to # values and replacements.
;	0xffff = end of table
RSGNFL:
	cmp	word ptr [di],0ffffh
	je	RSGNErr
	cmp	ax,[di]
	je	RSGNFound
	add	di,4
	jmp	RSGNFL
RSGNFound:
	mov	ax,[di+2]
	clc

RSGNOut:
			; leave pointing to error, '.'
	pop	dx
	pop	cx
	ret
	endp


RSGetLetter	proc	near
	cmp	byte ptr [bx],'='
	jne	RSGLErr
	inc	bx

	mov	al,[bx]
	inc	bx

RSGLFind:
	cmp	byte ptr [di],0
	je	RSGLErr
	cmp	[di],al
	je	RSGLFound
	add	di,2
	jmp	RSGLFind
RSGLErr:
	stc
	jmp	RSGLOut
RSGLFound:
	clc
	mov	al,[di+1]
RSGLOut:
	ret
	endp



RSDEcho	proc	near			; .EC means DON'T echo input
	mov	[di].echo,1
	ret
	endp

RSDCR	proc	near
	mov	[di].cr,1		; suppress CRs
	ret
	endp

RSDLF	proc	near
	mov	[di].lf,1		; suppress LFs
	ret
	endp

RSDNU	proc	near
	mov	[di].null,1		; output 6 nulls after CR
	ret
	endp


RSDData	proc	near
	push	di
	lea	di,RSdatas
	call	RSGetNum
	pop	di
	or	[di].setup,al		; by default set to 7 bits --
	ret				; this can upgrade to 8
	endp

RSDBaud	proc	near
	push	di
	lea	di,RSbauds
	call	RSGetNum
	pop	di
	mov	[di].baudrate,ax
	mov	al,[di].setup		; get setup byte
	or	al,80h			; set to one stop bit
	and	al,not 8   		; turn off CLK4M
	and	ah,80h			; get CLK4M from baudrate
	shr	ah,4			; position bit
	or	al,ah			; set it
	mov	[di].setup,al		; save byte
	ret
	endp

RSDPar	proc	near				
	push	di	    		; set parity to N, O, or E
	lea	di,RSParities
	call	RSGetLetter
	pop	di
	mov	ah,[di].setup
	and	ah,not 30h		; ODD and enabled by default
	or	ah,al
	mov	[di].setup,ah
  	ret
	endp

RSDCH	proc	near
	or	[di].setup,20h			; enable parity check
	ret
	endp


RSDTW	proc	near
	and	[di].setup,not 80h		; set two stop bits
	ret
	endp


;**************************************
;********* CLOSE
;**************************************

RSClose	proc	near
	ret	    				; sure, whatever
	endp



;********************************************
;************* READ
;********************************************

RSRead	proc	near
	rsseterror 0

	mov	bx,pabaddr

	cmp	curdev,D_PIO
	jb	RRIsCom

RRPIO:
	rsseterror 3				; can't read from PIO!
	jmp	RROut

RRIsCOM:

;	For RS232, simply read until we get the needed # of chars
;	(for fixed) or a CR/LF (for variable).
;
;	I'm not sure what it really does, but I'll take the .CR/.LF
;	flags to also mean suppress inputting them (for purposes of
;	terminating the string).
;
;	In any case, they won't be read into the input buffer unless
;	we have the internal mode set.

	mov	si,[bx].addr		; VDP buffer addr
	ror	si,8

	test	[bx].pflags,fp_internal
	jz	RRRVar

;
;	For internal reads, we receive the length byte in the
;	input stream, followed by the data.
;	
;	If length>reclen, assume error.

RRRInt:
	call	RS232getchar
	jc	RRFail

	test	[bx].pflags,fp_variable		; variable mode?
	jnz	RRRIV

	cmp	al,[bx].preclen			; no, we must receive
	jne	RRFail				; this byte
	jmp	RRRIOkLen

RRRIV:
	cmp	al,[bx].preclen			; variable len should be
	ja	RRFail				; at most the reclen

RRRIOkLen:
	xor	cx,cx
	mov	cl,al				; save total len
	mov	[bx].charcount,cl		; update PAB

RRRILoop:
	call	RS232getchar
	jc	RRFail

	mov	[si],al
	inc	si
	loop	RRRILoop

	mov	cl,[bx].charcount		; # chars read
	jmp	RREnd

RRRVar:
	xor	cx,cx
	mov	cl,[bx].preclen		; get maximum length
RRRC:
	call	RS232getchar
	jc	RRFail			; timeout (no DSR or RBL) or FCTN-4

	cmp	[di].echo,0
	jnz	RRNoEcho

	call	RS232putchar		; no .EC means, echo received char

RRNoEcho:
	test	[bx].pflags,fp_variable
	jz	RRRC01			; just add it

	cmp	al,_CR
	je	RRDVLen			; end of string

RRRC01:	
	mov	[si],al
	inc	si

	loop	RRRC

RRDVLen:
	neg	cl			; figure dis/var length
	add	cl,[bx].preclen
	mov	[bx].charcount,cl

RREnd:
	mov	si,[bx].addr		; update changed bytes
	ror	si,8
	push	si
	xor	ch,ch
	push	cx
	call	VDPUpdate

	jmp	RROut

RRFail:
       	rsseterror e_hardwarefailure
RROut:
	ret
	endp


;	Get a character from the RS232.
;
;	Set C=1 if Fctn-4 is pressed, or we time out waiting for 
;	the Data Set Ready pin and RBL to set.
;
;	Set Z=1 if we get a null character.
;
;	[DI]= rsrec
;	Return AL=char
;	CY=1 if hardware error
;
RS232getchar proc near
	push	cx
	push	dx
	push	si

	mov	dx,[di].baseaddr
	call	rselect
	jc	Rgcout

Rgcnotready:
	call	checkfctn4
	jc	Rgcout

	call	isbuffered
	jnc	Rgcnotready

	call	readbuffer
	mov	al,[si].char

	push	ax

	mov	dx,9
	call	RS232tb				; error of any sort?
	jz	Rgcgood

	stc

Rgcgood:
	pop	ax

Rgcout:
	pop	si
	pop	dx
	pop	cx
       	ret
	endp


	comment	\
;	Get a character from the RS232.
;
;	Set C=1 if Fctn-4 is pressed, or we time out waiting for 
;	the Data Set Ready pin and RBL to set.
;
;	Set Z=1 if we get a null character.
;
;	[DI]= rsrec
;	Return AL=char
;	CY=1 if hardware error
;
RS232getchar proc near
	push	cx
	push	dx

Rgcnotready:
	call	checkfctn4
	jc	Rgcout

	call	RS232checkavail
	jz	Rgcnotready

	mov	cx,8
	call	RS232stcr
	shr	ax,8				; sets Z if ax=0

;	push	ax

;	mov	dx,9
;	call	RS232tb				; error of any sort?
;	jz	Rgcgood

;	stc

;Rgcgood:
;	pop	ax
 	clc

Rgcout:
	pop	dx
	pop	cx
       	ret
	endp


	\

;	RS232 -- set a CRU bit.
;
;	DX = bit #

rs232sbo proc	near
	push	ax
	push	cx
	push	dx
	add	dx,dx
	add	dx,[di].baseaddr
	mov	ah,1
	mov	cx,1
	call	writeseveralCRU
	pop	dx
	pop	cx
	pop	ax
	ret
	endp


;	RS232 -- reset a CRU bit.
;
;	DX = bit #

rs232sbz proc	near
	push	ax
	push	cx
	push	dx
	add	dx,dx
	add	dx,[di].baseaddr
	xor	ah,ah
	mov	cx,1
	call	writeseveralCRU
	pop	dx
	pop	cx
	pop	ax
	ret
	endp


;	RS232 -- test bit
;
;	DX = bit
;	Returns Z=1 if off, Z=0 if on

rs232tb proc	near
	push	ax
	push	cx
	push	dx

	add	dx,dx
	add	dx,[di].baseaddr
	mov	cx,1
	call	readseveralCRU
	test	ah,1

	pop	dx
	pop	cx
	pop	ax
	ret
	endp


;	RS232 -- load CRU
;
;	AX = value
;	CX = # bits

rs232ldcr proc	near
	push	ax
	push	cx
	push	dx

	mov	dx,[di].baseaddr
	call	writeseveralCRU

	pop	dx
	pop	cx
	pop	ax
	ret
	endp


;	RS232 -- Store CRU
;
;	CX = # bits

rs232stcr proc	near
	push	cx
	push	dx

	mov	dx,[di].baseaddr
	call	readseveralCRU

	pop	dx
	pop	cx
	ret
	endp



;	RS232 -- check if char is available
;
;	Returns Z=1 if not.

rs232checkavail proc near
	push	ax
	push	cx
	push	dx

	mov	dx,27
	call	RS232tb				; data set ready?
	jz	rcano

	mov	dx,21	  			; receive buffer loaded?
	call	RS232tb

rcano:
	pop	dx
	pop	cx
	pop	ax

	ret
	endp



;	Check if Fctn-4 is set.
;
checkfctn4 proc near
	cmp	fctn4,0
	jz	cf4not
	stc
cf4not:	ret
	endp



;********************************************
;**************** WRITE
;********************************************

RSWrite	proc	near
	rsseterror 0

	mov	bx,pabaddr

	xor	cx,cx
	mov	cl,[bx].charcount
	test	[bx].pflags,fp_variable
	jnz	RWV
       	mov	cl,[bx].preclen
RWV:

	cmp	curdev,D_PIO
	jb	RWIsCom

RWPIO:
	mov	dx,[bx].addr
	xchg	dl,dh

	mov	ah,40h
	mov	bx,4			; standard printer
	int	21h  
	jc	RWHardError
  	
RWPCRLF:
	cmp	[di].cr,0
	jnz	RWPNoCR

	mov	ah,40h
	mov	cx,1
	lea	dx,_CR
	int	21h
	jc	RWHardError

RWPNoCR:
	cmp	[di].lf,0
	jnz	RWOut

	mov	ah,40h
	mov	cx,1
	lea	dx,_LF
	int	21h
	jc	RWHardError
	jmp	RWOut


RWIsCom:
	mov	si,[bx].addr
	ror	si,8

	test	[bx].pflags,fp_internal
	jz	RWVar

	mov	al,cl
	call	RS232putchar			; internal -- send len

RWVar:
	jcxz	RWCRLF

RWOneChar:
	mov	al,[si]
	inc	si
	call	RS232putchar
	jc	RWHardError

	loop	RWOneChar

RWCRLF:
	test	[bx].pflags,fp_internal
	jnz	RWOut				; no candy for internal

	test	[bx].pflags,fp_variable
	jz	RWOut				; only send cr/lf
						; with variable length
	cmp	[di].cr,0
	jnz	RWNoCR
	
	mov	al,_CR
	call	RS232PutChar
	jc	RWHardError

RWNoCR:
	cmp	[di].lf,0
	jnz	RWOut

	mov	al,_LF
	call	RS232PutChar
	jnc	RWOut

RWHardError:
	rsseterror e_hardwarefailure
	jmp	RWOut

RWOut:
	ret
	endp


;	Send one char (AL) to the RS232.
;
;	Return C=1 if timeout.
;	
;
RS232PutChar proc near
	push	cx
	push	dx

	push	ax				; save transmit char

	mov	dx,16
	call	RS232sbo			; set RTS

RPCNotReady:
	call	checkfctn4
	jc	RPCTimeOut

	mov	dx,27
	call	RS232tb				; check DSR
	jz	RPCNotReady

	mov	dx,22
	call	RS232tb				; transmit buffer empty?
	jz	RPCNotReady

	mov	cx,8
	pop	ax
	shl	ax,8	       			
	call	RS232ldcr			; send char

	mov	dx,16
	call	RS232sbz			; RTS off
	jmp	RPCOut

RPCTimeOut:
	pop	ax				; lose char

RPCOut:
	pop	dx
	pop	cx
	ret
	endp




;*******************************
;*********** SEEK... not
;*******************************

RSSeek	proc	near
	rsseterror e_illegal
	ret
	endp

;********** Load

RSLoad	proc	near
	jmp	RSSeek
	endp

;********** Binary

RSSave	proc	near
	jmp	RSSeek
	endp

;********** Delete
RSDelete	proc	near
	jmp	RSSeek
	endp

;********** Scratch
RSScratch	proc	near
	jmp	RSSeek
	endp

;********** Status
RSStatus	proc	near
	jmp	RSSeek
	endp


	
