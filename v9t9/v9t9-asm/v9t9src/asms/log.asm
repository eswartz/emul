; ==========================================
; LOG.ASM -- V9t9 diagnostics logging
; ==========================================
; by Edward Swartz  5/30/95
; ==========================================


_LOG_	=	1

	include	standard.h
	include	int.h
	include	support.h
	include	log.h


	comment	\

	This module provides logging functions to provide diagnostics
	support for functions of the emulator.

	The "startlogging" procedure will, if "islogging" is set, open
	a file and prepare to print messages to it.
	The filename for logging is given on the command line.

	"islogging" is a word bitmap of modules from which to accept
	logging; the modules themselves will test the word to decide
	if logging is necessary.
	
	All logs will be in "printf" format:

	<word>	pointer to format string
	<word>  # of arguments (for auto stack cleaning)
	[<word>  each argument]

	\


	.data


logfilename 	db	80 dup (0)
loghandle	dw	0
islogging		dw	0			; bitmap of LG_xxxx constants


TEMPSIZE	equ	64
tempout		db	TEMPSIZE dup (?)


	.code

;===========================================================================
;	LOG:	Pre-config init.
;
;	If the "/L" option was specified:
;
;		þ  Set up log file
;===========================================================================

log_preconfiginit proc near
	call	startlogging
	ret
	endp


;===========================================================================
;	LOG:	Post-config init.
;===========================================================================

log_postconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	LOG:	Restart.
;===========================================================================

log_restart proc near
	clc
	ret
	endp


;===========================================================================
;	LOG:	Restop.
;===========================================================================

log_restop proc near
	clc
	ret
	endp


;===========================================================================
;	LOG:	Shutdown.
;
;	þ  Close log file.
;===========================================================================

log_shutdown proc near
	call	stoplogging
	clc
	ret
	endp






;--------------------------------------------------------------------------
;	Start logging to the device.
;
;	The file will be opened in APPEND mode, so this can be 
;	turned on and off throughout the program, if necessary.
;
;	"logfilename" contains the filename,
;	if loghandle is nonzero, an open file is assumed, and
;	it is closed first.
;
;	Returns C=1 if couldn't open log file.
;--------------------------------------------------------------------------

	.data

beginlogging	db	'***%tStarted logging at %w%n',0
logerror 	db	'Couldn''t create log file %',0

	.code

startlogging proc near
	push	ax
	push	bx
	push	cx
	push	dx
	push	si

	mov	bx,loghandle
	or	bx,bx
	jz	slclosed

	mov	ah,3eh
	int	21h			; close open log file
	mov	loghandle,0		; closed

slclosed:
	cmp	byte ptr logfilename,0	; defined?
	jz	slout

	mov	ax,3d02h		; read/write access
	lea	dx,logfilename
	int	21h			; open file
	jnc	slseekend

	mov	ah,3ch
	xor	cx,cx
	int	21h			; create
	jnc	slseekend

	lea	dx,logerror		; couldn't... make error message
	lea	si,logfilename
	call	setuperror

	stc
	jmp	slout			; couldn't create... no logging

slseekend:
	mov	loghandle,ax		; save handle
	mov	bx,ax

	mov	ah,42h
	mov	al,2
	xor	cx,cx
	xor	dx,dx			
	int	21h			; seek to end of file

	push	elapsed
	push	offset beginlogging
	call	printf
	add	sp,4

	clc				; open!
slout:
	pop	si
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;---------------------------------------------------------------------------
;	Stop logging to log file.
;
;	Adds a "end of logging" line to file, then simply closes file.
;---------------------------------------------------------------------------

	.data

endlogging db	'***%tStopped logging at %w%n',0

	.code

stoplogging proc near
	push	ax
	push	bx

	cmp	loghandle,0
	jz	stlno

	push	offset endlogging
	call	printf			; print end-of-file message
	add	sp,2

	mov	bx,loghandle
	mov	ah,3eh
	int	21h			; close file

stlno:
	pop	bx
	pop	ax
	ret
	endp


;---------------------------------------------------------------------------
;	Add a message to the log file.
;
;	Arguments are passed on the stack:
;
;		[<word argument values>]
;		<word ptr to format string>	
;		<return addr>			<-- SP
;
;	CALLER cleans up stack.
;---------------------------------------------------------------------------

	.data

loghead	db	'**** <%d>%t : ',0

	.code

logout	proc	near
	cmp	loghandle,0
	jnz	lodo			; logging?

	jmp	loout

lodo:
	push	elapsed
	push	offset loghead
	call	printf 			; print header part
	add	sp,4

	jmp	printf			; print rest
loout:
	ret
	endp


;	PRINTF!
;
;	BP -> pointer to arguments
;	AL -> temp
;	SI -> string input
;	DI -> string output
;	BX -> number routines
;
printf	proc	near
	pusha

	cmp	loghandle,0
	jnz	prgo

	jmp	prout
prgo:
	mov	bp,sp
	add	bp,18			; 8 regs, ret

	mov	si,[bp]			; input string
	add	bp,2			; point to param #1
	lea	di,tempout	 	; output string

prloop:
	mov	al,[si]			; examine a char
	inc	si
	or	al,al			; end-of-string?
	jnz	prlook
	call	prdump			; yup, dump it
	jmp	prout

prlook:
	xor	cx,cx			; # digits required
	cmp	al,'%'			; special modifier?
	je	prspec

pradd:
	call	prpush			; no
	jmp	prloop

prspec:
	mov	al,[si]			; get special char
	inc	si
	cmp	al,'%'
	je	pradd

	cmp	al,'0'
	jb	prnotnum
	cmp	al,'9'
	ja	prnotnum

	mov	cl,al
	sub	cl,'0'
	jmp	prspec			; got a fieldwidth

prnotnum:
	cmp	al,'d'			; decimal number?
	jne	pr00

	lea	bx,nm_dec
	mov	ch,5
	jmp	prdonumber

pr00:
	cmp	al,'h'			; hex number?
	jne	pr01

	lea	bx,nm_hex
	mov	ch,4
	jmp	prdonumber

pr01:
	cmp	al,'b'			; binary?
	jne	pr02

	lea	bx,nm_bin
	mov	ch,16
	jmp	prdonumber

pr02:
	cmp	al,'s'			; string?
	jne	pr03

	mov	bx,[bp]
	add	bp,2
	call	prstring
	jmp	prloop

pr03:
	cmp	al,'c' 			; char?
	jne	pr04

	mov	ax,[bp]
	jmp	pradd

pr04:
	cmp	al,'n'			; newline?
	jne	pr05
     	
	mov	al,13
	call	prpush
	mov	al,10
	jmp	pradd

pr05:
	cmp	al,'t'			; tab?
	jne	pr06

	mov	al,9
	jmp	pradd

pr06:
	cmp	al,'w'			; date and time?
	jne	pr07

	call	prdate
	jmp	prloop

pr07:
	push	ax    			; unrecognized sequence
	mov	al,'%'
	call	prpush
	pop	ax
	jmp	pradd

prdonumber:
	mov	ax,[bp]
	add	bp,2
	call	prnumber
	jmp	prloop


prout:
	popa
	ret
	endp


;	Push a character out.
;
;	Mildly buffered.

prpush	proc	near
	mov	[di],al
	inc	di
	cmp	di,offset tempout+TEMPSIZE
	jb	prpout
	call	prdump
prpout:
	ret
	endp


;	Dump buffer to disk.
;
;

prdump	proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	mov	ah,40h
	mov	bx,loghandle
	mov	cx,di
	sub	cx,offset tempout
	lea	dx,tempout
	int	21h
	lea	di,tempout

	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	Print a number.
;
;	BX =>base divisors
;	AX = number
;	DI = output
;	CX = min # digits
;
	.data

baseconv db	'0123456789ABCDEF'
nm_bin	dw	32768,16384,8192,4096,2048,1024,512,256,128,64,32,16,8,4,2,1,0
nm_dec	dw	10000,1000,100,10,1,0
nm_hex	dw	1000h,100h,10h,1,0

	.code

prnumber proc	near
	push	dx
	push	cx
	push	si
	push	bp

	mov	si,di
prnloop:
	xor	dx,dx			; dx:ax = #
	mov	bp,[bx]			; cx = divisor
	add	bx,2
	or	bp,bp
	jz	prnout
	div	bp			; ax= digit val, dx=rest


	or	ax,ax			; leading zeros?
	jnz	prndo			; no
	cmp	ch,cl
	jbe	prndo
	cmp	si,di			; first digit still?
	je	prnskip			; oh, yeah.

prndo:
	push	ax
	push	bx
	lea	bx,baseconv
	xlat
	call	prpush
	pop	bx
	pop	ax

prnskip:
	or	ch,ch
	jz	prnnodecch
	dec	ch

prnnodecch:
	mov	ax,dx
	jmp	prnloop

prnout:
	cmp	si,di
	jnz	prnnotzero

	mov	al,'0'
	call	prpush

prnnotzero:
	pop	bp
	pop	si
	pop	cx
	pop	dx
	ret
	endp


prstring proc	near
	
prsloop:
	mov	al,[bx]
	inc	bx
	or	al,al
	jz	prsout
	cmp	al,'$'
	je	prsout

	call	prpush
	jmp	prsloop
prsout:
	ret
	endp


	.data

datetime db	'%d/%d/%d  %2d:%2d',0

	.code

prdate	proc	near
	pusha

	call	prdump			; dump buffer (printf overwrites)

	mov	ah,2ch			; get time
	int	21h

	xor	ax,ax
	mov	al,cl
	push	ax			; MINUTES
	mov	al,ch
	push	ax			; HOURS

	mov	ah,2ah
	int	21h			; get date

	push	cx			; YEAR
	xor	ax,ax
	mov	al,dl
	push	ax			; DAY
	mov	al,dh
	push	ax			; MONTH

	push	offset datetime
	call	printf
	add	sp,12

	call	prdump

	popa
	lea	di,tempout

	ret
	endp


	end
