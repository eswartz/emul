; *************************************************************
; COMPILE.INC  Assembly language TI Emulator macros for COMPILE
; *************************************************************
; by Edward Swartz.  2/22/1995
; *************************************************************


	.data

	org	0

bot	db	8192 dup (?)

offs	equ	-4

	.code

	org	(4096*2)

params	struc
	f_return 	dd	0		; how to get back (EXECUTE)
	f_writeword 	dd 	0
	f_writebyte 	dd	0
	f_readword 	dd 	0
	f_readbyte 	dd	0

	datasg	dw	0
	cpusg	dw	0

	lastval	dw	0
	lastcmp	dw	0			; pass back between envirs
	val_dx	dw	0
	ip	dw	0
	statc	db	0
	stato	db	0
	statp	db	0
	statx	db	0
	
	ends


;	BP points to the above struc in the STACK SEGMENT



R0	equ	0
R1	equ	2
R2	equ	4
R3	equ	6
R4	equ	8
R5	equ	10
R6	equ	12
R7	equ	14
R8	equ	16
R9	equ	18
R10	equ	20
R11	equ	22
R12	equ	24
R13	equ	26
R14	equ	28
R15	equ	30

;
;	Registers used in the emulator.
;

WP	equ	BX		;  DIFFERENT FROM TIEMUL!
STATHI	equ	DH
STAT	equ	DX



st_l	equ	80h
st_a	equ	40h
st_e	equ	20h

st_c	equ	10h
st_o	equ	08h
st_p	equ	04h
st_x	equ	02h





;	CLEAR several bits in the status word.
;	Use by adding together ST_* equates.
;
CLEAR	macro mask
	and	STATHI,not mask
	endm


CARRY	macro
	or	STATHI,ST_C
	endm

OVERFLOW	macro
	or	STATHI,ST_O
	endm


;	Test the CARRY flag.
;
CARRY?	macro
	local	not
	jnc	not
	CARRY
not:	endm


;	Test the OVERFLOW flag.
;
OVERFLOW?	macro
	local	not
	jno	not
	OVERFLOW
not:	endm


;	Test the EQUAL flag.
;
EQUAL?	macro
	local	not,yup
	mov	[bp].lastcmp,0
	je	yup
	or	[bp].lastval,1
	jmp	not
yup:
	mov	[bp].lastval,0
not:	endm


ISCARRY?	macro where
	test	STATHI,st_c
	jnz	where
	endm


ISOVERFLOW? macro	where
	test	STATHI,st_o
	jnz	where
	endm


ISNOTCARRY?	macro where
	test	STATHI,st_c
	jz	where
	endm


ISNOTOVERFLOW? macro	where
	test	STATHI,st_o
	jz	where
	endm



ISODDPARITY? macro where
	test	STATHI,st_p
	jnz	where
	endm

ISCARRY?	macro where
	test	STATHI,st_c
	jnz	where
	endm




ISNOTODDPARITY? macro where
	test	STATHI,st_p
	jz	where
	endm



CARRYOVERFLOW? macro
	pushf
	CARRY?
	popf
	OVERFLOW?
	endm



CMP0	macro val
	mov	[bp].lastval,val
	mov	[bp].lastcmp,0
	endm


CMP0B	macro val
	local	cmp0bs

	or	al,al
	jpe	cmp0bs
	or	STATHI,ST_P

cmp0bs:
	cbw
	mov	[bp].lastval,ax
	mov	[bp].lastcmp,0

	endm




;	Compare WHAT to VALUE and set
;	LASTVAL and LASTCMP.
;
CMPTO	macro what,value
	mov	[bp].lastval,what
	mov	[bp].lastcmp,value
	endm


;	Compare WHAT to VALUE (byte)
;	and set LASTVAL and LASTCMP
;
;	VALUE=al
;
CMPTOB	macro what,value
	local	ctb0

	push	ax
	mov	al,value

	or	what,what
	jpe	ctb0
	or	STATHI,ST_P
ctb0:
	cbw
	mov	[bp].lastcmp,ax
	mov	al,what
	cbw
	mov	[bp].lastval,ax
;	mov	equals,0
	pop	ax
	endm



return:
	mov	[bp].IP,ax
	mov	[bp].val_dx,dx
	jmp	dword ptr [bp].f_return



readword:
	and	di,not 1
	mov	ds,[bp].datasg
	call	dword ptr [bp].f_readword
	mov	ds,[bp].cpusg
	ret

readbyte:
	xor	di,1
	mov	ds,[bp].datasg
	call	dword ptr [bp].f_readbyte
	mov	ds,[bp].cpusg
	xor	di,1
	ret

readwordc:
	and	di,not 1
	mov	ds,[bp].datasg
	call	dword ptr [bp].f_readword
	mov	ds,[bp].cpusg
	mov	cx,ax
	ret

readbytec:
	xor	di,1
	mov	ds,[bp].datasg
	call	dword ptr [bp].f_readbyte
	mov	ds,[bp].cpusg
	mov	cl,al
	xor	di,1
	ret



writeword:
	and	di,not 1
	mov	ds,[bp].datasg
	call	dword ptr [bp].f_writeword
	mov	ds,[bp].cpusg
	ret

writebyte:
	xor	di,1
	mov	ds,[bp].datasg
	call	dword ptr [bp].f_writebyte
	mov	ds,[bp].cpusg
	ret







