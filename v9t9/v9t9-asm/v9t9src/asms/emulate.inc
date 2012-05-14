; *************************************************
; EMULATE.INC  Assembly language V9t9 macros
; *************************************************
; by Edward Swartz.  1/10/1995
; *************************************************


IFNDEF	_tiemul_
	.err	"You can't use EMULATE.INC outside of TIEMUL!"
ENDIF

st_l	equ	80h
st_a	equ	40h
st_e	equ	20h

st_c	equ	10h
st_o	equ	08h
st_p	equ	04h
st_x	equ	02h



	.code


;	Jump to the NEXT instruction.
;
;
NEXT	macro
	jmp	execute
	endm




;	GETSTAT converts from 9900 ST into emulated lastcmp/lastval/STAT?
;

GETSTAT	macro	
	call	_getstat
	endm


_getstat proc	near
	push	ax
	push	bx

	xor	ax,ax
	xor	bx,bx
	test	STATHI,ST_E
	jnz	gsend
	inc	bx				; 0  ?  1
	test	STATHI,ST_L+ST_A
	jz	gsend				; less than arith & logical
	xchg	ax,bx				; 1  ?  0

	test	STATHI,ST_L			
	jnz	gs00				; otherwise ONLY arith!
	dec	bx				; 1  ? -1
	jmp	gsend
gs00:	
	test	STATHI,ST_A
	jnz	gsend				; arith >  &|  logical >
	neg	ax				; -1 ?  0
gsend:						; only logical >
	mov	lastval,ax
	mov	lastcmp,bx

	mov	ax,STAT
	and	ax,15
	mov	intmask,ax

	pop	bx
	pop	ax
	ret
	endp


;	Convert from emulated status bits to 9900 ST.
;
;
SETSTAT	macro	
	call	_setstat
	endm


_setstat proc	near
	push	ax

	and	STATHI,not (ST_L+ST_A+ST_E)	; clear status bits
	and	STAT,not 15			; clear interrupt mask

	mov	ax,lastval
	cmp	ax,lastcmp
	jz	c0eq
	jbe	c0na
	or	STATHI,ST_L
	cmp	ax,lastcmp
c0na:
	jle	c0out
     	or	STATHI,ST_A
	jmp	c0out
c0eq:
	or	STATHI,ST_E
c0out: 

	mov	ax,intmask
	and	ax,15
	or	STAT,ax

	pop	ax
	ret
	endp



;========================================================================
;	Macros for decoding and executing instructions.
;========================================================================

CLEAR	macro	how
	and	STATHI,not how
	endm


CARRY	macro
	or	STATHI,ST_C
	endm

OVERFLOW macro
	or	STATHI,ST_O
	endm

NOCARRY macro
	and	STATHI,not ST_C
	endm

NOOVERFLOW macro
	and	STATHI,not ST_O
	endm

XOP	macro
	or	STATHI,st_x
	endm

OPARITY	macro
	or	STATHI,ST_P
	endm

NOPARITY macro
	and	STATHI,not ST_P
	endm


CARRY?	macro	
	local	not
	jnc	not
	CARRY
not:	endm

OVERFLOW? macro	
	local	not
	jno	not
	OVERFLOW
not:	endm

CARRYOVERFLOW? macro
	pushf
	CARRY?
	popf
	OVERFLOW?
	endm



;	Test the EQUAL flag.
;
EQUAL? macro	
	local	not,yup
	mov	lastcmp,0
	je	yup
	or	lastval,1
	jmp	not
yup:
	mov	lastval,0
not:	endm
	


ISCARRY?	macro	where
	test	STATHI,ST_C
	jnz	where
	endm


ISOVERFLOW?	macro	where
	test	STATHI,ST_O
	jnz	where
	endm


ISNOTCARRY?	macro	where
	test	STATHI,ST_C
	jz	where
	endm


ISNOTOVERFLOW?	macro	where
	test	STATHI,ST_O
	jz	where
	endm



ISODDPARITY?	macro	where
	test	STATHI,ST_P
	jnz	where
	endm



;	Compare WHAT to VALUE and set
;	LASTVAL and LASTCMP.
;
CMPTO	macro	what,value
	mov	lastval,what
	mov	lastcmp,value
	endm	


CMP0	macro	val
	mov	lastval,val
	mov	lastcmp,0
	endm


;===========================================================================

CMP0B	macro	val
	local	cmp0bs

IFDIFI	<val>,<AL>
	push	ax
	mov	al,val
ENDIF
	or	al,al
	jpe	cmp0bs
	OPARITY
cmp0bs:
	cbw
	mov	lastval,ax
	mov	lastcmp,0

IFDIFI	<val>,<AL>
	pop	ax
ENDIF
	endm



;	Compare WHAT to VALUE (byte)
;	and set LASTVAL and LASTCMP
;
;	VALUE=al
;
CMPTOB	macro	what,value
	local	ctb0

	push	ax
	mov	al,value
	
	or	what,what
	jpe	ctb0
	OPARITY

ctb0:
	cbw
	mov	lastcmp,ax
	mov	al,what
	cbw
	mov	lastval,ax
	pop	ax
	endm



;------------------------------------------------------------------------
;	FAST macros
;
;------------------------------------------------------------------------

CHANGEREG macro reg,how
	how	es:[WP+reg],cx
	mov	ax,es:[WP+reg]
	endm


;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
;
;	Undocumented (except in TIEMUL.TXT) and broken and unused


	IFDEF	COMPROM

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
;	rstatc		db	0
;	rstato		db	0
;	rstatp		db	0
;	rstatx		db	0
	ends


	.code

	include	slowmem.inc


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


;	mov	al,statc
;	mov	romstruc.rstatc,al
;	mov	al,stato
;	mov	romstruc.rstato,al
;	mov	al,statp
;	mov	romstruc.rstatp,al
;	mov	al,statx
;	mov	romstruc.rstatx,al

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

;	mov	al,romstruc.rstatc
;	mov	statc,al
;	mov	al,romstruc.rstato
;	mov	stato,al

;	mov	al,romstruc.rstatp
;	mov	statp,al

;	mov	al,romstruc.rstatx
;	mov	statx,al


	mov	ax,romstruc.rip

	mov	IP,ax

	mov	WP,curwp

;;;	or	stateflag,romming
	jmp	executefromrom
	endp


	ENDIF

;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX



;=========================================================================


;------------------------------------------------------------------------
;	Execute an instruction.
;
;	Interrupts (checkstate) are honored.
;------------------------------------------------------------------------

execute	equ	$

	IFDEF	COMPROM
	mov	ax,IP

	cmp	ax,2000h
	jae	executefromrom

	mov	ax,compiledromseg
	or	ax,ax
	jz	executefromrom

	jmp	branchtorom
	ENDIF


executefromrom:
	mov	ax,stateflag
	test	ax,checkable		; check state flag first
	jne	dostate

nodelay:
nostate:
getinst:
	inc	cips
	FETCH	si			; fetch instruction in SI


executeX:				; entry point for X
	mov	bx,si
	mov	bl,bh
	and	bx,00feh		
	jmp	word ptr [opcodetable+bx]

dostate:
	jmp	checkstate

	.data
	even

opcodetable equ	$
	dw	h0000,h0200,h0400,h0400,h0800,h0800,h0800,h0800
	dw	h1000,h1000,h1000,h1000,h1000,h1000,h1000,h1000
	dw	h2000,h2000,h2000,h2000,h2000,h2000,h2000,h2000
	dw	h2000,h2000,h2000,h2000,h2000,h2000,h2000,h2000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000
	dw	h4000,h4000,h4000,h4000,h4000,h4000,h4000,h4000

	.code

;--------------------------------------------------------------------------
;	This define tells READBYTE and WRITEBYTE where we are.
;	Since we're in EMULATE.INC, the instructions pre-complement
;	the addresses.  
;	But, external routines won't expect this, so READBYTE and WRITEBYTE
;	will do it for them.
;--------------------------------------------------------------------------

_emulate_	equ	1

;==========================================================================
;	Data instructions, 					>0000->01FF
;==========================================================================

	even
h0000:	
dat:
	next

;==========================================================================
;	Immediate, Control instructions, 			>0200->03FF
;--------------------------------------------------------------------------
;
;     	  0 1 2 3-4 5 6 7+8 9 A B-C D E F 		LI, AI, ANDI, ORI,
;	----------------------------------		CI, STWP, STST,
;	|      o p c o d e     |0| reg # |		LIMI, LWPI, IDLE,
;	----------------------------------		RSET, RTWP, CKON,
;							CKOF, LREX
;	Inputs:		SI=opcode
;	Outputs:	SI=register offset (0-30)
;			Destroys BX
;
;==========================================================================

	even
h0200:
	mov	bx,si				; save opcode
	
	and	si,15
	add	si,si				; register offset

	and	bx,01e0h
	shr	bx,4
	jmp	word ptr [h0200table+bx]

	.data
	even

h0200table equ	$
	dw	li,ai,andi,ori,ci,stwp,stst,lwpi
	dw	limi,dat,idle,rset,rtwp,ckon,ckof,lrex

	.code

;------------------------------------------------------------------------
;	LI -- 		Load Immediate
;
;	Inputs:		SI=register offset
;
;	Outputs:	*Reg = *IP+
;
;------------------------------------------------------------------------

	even
li:
	FETCH@	ax				; get immediate data
	WRITEREG SI,ax				; set the register
	CMP0	ax				; set status flags
	NEXT


;------------------------------------------------------------------------
;	AI -- 		Add Immediate
;
;	Inputs:		SI=register offset
;
;	Outputs:	*Reg += *IP+
;
;------------------------------------------------------------------------

	even
ai:
	CLEAR	ST_C+ST_O

	FETCH@	cx				; get immediate data

IFDEF	FAST
	CHANGEREG si,add
	CARRYOVERFLOW?

ELSE

	READREG	SI,ax				; get AX=register

	add	ax,cx				; add

	CARRYOVERFLOW?

	WRITEREG SI,ax				; set the register
ENDIF
	CMP0	ax				; set status flags
	NEXT


;------------------------------------------------------------------------
;	ANDI --   	AND Immediate
;
;	Inputs:		SI=register offset
;
;	Outputs:	*Reg &= *IP+
;
;------------------------------------------------------------------------

	even
andi:
	FETCH@	cx				; get immediate data

IFDEF	FAST
	CHANGEREG si,and

ELSE

	READREG	SI,ax				; get AX=register

	and	ax,cx				; and

	WRITEREG SI,ax				; set the register
ENDIF

	CMP0	ax				; set status flags
	NEXT
	

;------------------------------------------------------------------------
;	ORI --   	OR Immediate
;
;	Inputs:		SI=register offset
;
;	Outputs:	*Reg |= *IP+
;
;------------------------------------------------------------------------

	even
ori:
	FETCH@	cx				; get immediate data

IFDEF	FAST
	CHANGEREG si,or

ELSE
	READREG	SI,ax				; get AX=register

	or	ax,cx				; and

	WRITEREG SI,ax				; set the register
ENDIF
	CMP0	ax				; set status flags
	NEXT
	

;------------------------------------------------------------------------
;	CI --   	Compare Immediate
;
;	Inputs:		SI=register offset
;
;	Outputs:	status = (*Reg-*IP+)
;
;------------------------------------------------------------------------

	even
ci:
	FETCH@	cx				; get immediate data
	READREG	SI,ax				; get AX=register

	CMPTO	ax,cx				; set status flags
	NEXT


;------------------------------------------------------------------------
;	STWP --   	STore Workspace Pointer
;
;	Inputs:		SI=register offset
;
;	Outputs:	*Reg = WP
;
;------------------------------------------------------------------------

	even
stwp:
	WRITEREG SI,WP
	NEXT


;------------------------------------------------------------------------
;	STST --   	STore STatus register
;
;	Inputs:		SI=register offset
;
;	Outputs:	*Reg = STAT
;
;------------------------------------------------------------------------

	even
stst:
	SETSTAT
	WRITEREG SI,STAT
	NEXT


;------------------------------------------------------------------------
;	LWPI --   	Load Workspace Pointer Immediate
;
;	Outputs:	WP = *IP+
;
;------------------------------------------------------------------------

	even
lwpi:
	FETCH@	WP				; get immediate data
	call	validateWP			; set WPSEG
	NEXT


;------------------------------------------------------------------------
;	LIMI --   	Load Interrupt Mask Immediate
;
;	Outputs:	status&15 |= *IP+&15
;
;------------------------------------------------------------------------

	even
limi:
	FETCH@	ax				; get immediate data
	and	ax,15				; mask off

	and	intmask,not 15			; lose current mask
	and	STAT,not 15
	or	intmask,ax     			; add new mask
	or	STAT,ax

	call	handle9901			; interrupt state changed
	NEXT


;------------------------------------------------------------------------
;	IDLE --   	IDLE (unsupported on 9900)
;------------------------------------------------------------------------

	even
idle:
	NEXT


;------------------------------------------------------------------------
;	RSET --   	Reset (unsupported on 9900)
;------------------------------------------------------------------------

	even
rset:
	NEXT



;------------------------------------------------------------------------
;	RTWP --   	Return with Workspace Pointer
;
;	Outputs:	WP = R13, IP = R14, STAT = R15
;------------------------------------------------------------------------

	even
rtwp:
	READREG	R15,STAT			; restore STAT
	GETSTAT

	READREG	R14,ax
	and	ax,not 1
	mov	IP,ax				; restore IP
	
	READREG	R13,WP				; restore WP
	and	WP,not 1
	call	validateWP			; set WPSEG

	call	handle9901			; IM changed

	jmp	nostate
	NEXT


;------------------------------------------------------------------------
;	IRTWP --   	Return with Workspace Pointer
;
;	Outputs:	WP = R13, IP = R14, STAT = R15
;------------------------------------------------------------------------

	even
irtwp:
;;	mov	intwaiting,0

	READREG	R15,STAT			; restore STAT
	GETSTAT

	READREG	R14,ax
	and	ax,not 1
	mov	IP,ax				; restore IP
	
	READREG	R13,WP				; restore WP
	and	WP,not 1
	call	validateWP			; set WPSEG

	call	handle9901			; IM changed

	NEXT



;------------------------------------------------------------------------
;	CKON --   	ClocK ON (unsupported on 9900)
;------------------------------------------------------------------------

	even
ckon:
	NEXT

;------------------------------------------------------------------------
;	CKOF --   	ClocK OFF (unsupported on 9900)
;------------------------------------------------------------------------

	even
ckof:
	NEXT

;------------------------------------------------------------------------
;	LREX --   	Load, Restart, and Execute
;------------------------------------------------------------------------
	even

lrex:
	NEXT



;==========================================================================
;	Single-operand instructions, 				>0400->07FF
;--------------------------------------------------------------------------
;
;     	  0 1 2 3-4 5 6 7+8 9 A B-C D E F 		BLWP, B, X, CLR,     
;	----------------------------------		NEG, INV, INC, INCT,
;	|      o p c o d e   |TS |   S   |		DEC, DECT, BL, SWPB,
;	----------------------------------		SETO, ABS
;							
;	Inputs:		SI=opcode
;	Outputs:	SI=address of operand
;			AX,BX,CX destroyed
;
;==========================================================================

	even
h0400:
	mov	cx,si				; save opcode

	mov	bx,si				; copy opcode

	and	si,15				; single out S
	add	si,si				; times two	(S)

	shr	bx,4				; isolate the
	and	bx,3				; TS field	(TS)

	jnz	h0400_1				; TS=0?

;
;	TS=0 --	return register address
;

	jmp	h0400_reg
	jmp	h0400_go

h0400_1:
	dec	bx				; TS=1?				
	jnz	h0400_2

;
;	TS=1 --	return value in register		
;

	READREG	si,si				; Get SI=*reg
	jmp	h0400_go

h0400_2:
	dec	bx				; TS=2?
	jnz	h0400_3

;
;	TS=2 --	return value in register plus address at IP
;

	FETCH@	bx				; get address at IP
	or	si,si
	jz	h0400_2go			; S=0 so BX is full addr

	READREG	si,si				; else get the register
	add	bx,si				; and add it to the address

h0400_2go:
	mov	si,bx
	jmp	h0400_go

h0400_3:
;
;	TS=3 --	return value at address, and increment register by 2.
;

	READREG	si,ax				; get AX=value of register
	add	word ptr es:[wp+si],2	    	; increment register
	mov	si,ax				; get back value

h0400_go:
	and	si,not 1			; fix to word boundary

	mov	bx,cx
	and	bx,03c0h
	shr	bx,5
	jmp	word ptr [h0400table+bx]

h0400_reg:
	mov	bx,cx
	and	bx,03c0h
	shr	bx,5
	jmp	word ptr [h0400regtable+bx]

	.data
	even

h0400table equ	$
	dw	blwp,b,x,clr,nneg,inv,iinc,inct
	dw	ddec,ddect,bbl,swpb,sseto,aabs,dat,dat

h0400regtable equ $
	dw	blwpr,br,xr,clrr,nnegr,invr,iincr,inctr
	dw	ddecr,ddectr,bblr,swpbr,ssetor,aabsr,dat,dat

	.code

;------------------------------------------------------------------------
;	BLWP --   	Branch with Workspace Pointer
;
;	Result:		WP = *SI+, IP = *SI
;			New R13=old WP, New R14=Old IP, New R15=Old STAT 
;------------------------------------------------------------------------

	even
blwpr:
	add	si,WP
	and	si,not 1

	even
blwp:
	READWORD si,bx	       			; get new WP address
	and	bx,not 1			; force word boundary

	add	si,2
	READWORD si,cx				; get new IP address
	and	cx,not 1			; force word boundary

	mov	ax,WP
	mov	WP,bx				; change
	call	validateWP			; get new WPSEG

	WRITEREG R13,ax	  			; save old WP
	mov	ax,IP
	WRITEREG R14,ax				; save return IP
	SETSTAT
	WRITEREG R15,STAT			; save STAT

	mov	IP,cx
	jmp	nostate

;;;	CHECKROM
	NEXT


;------------------------------------------------------------------------
;	B --	   	Branch
;
;	Results:	IP = SI
;------------------------------------------------------------------------

	even
br:
	add	si,WP
	and	si,not 1
	even
b:
	mov	IP,si				 
;;;	CHECKROM
	NEXT


;------------------------------------------------------------------------
;	X --	   	eXecute
;
;	Results:	Executes instruction at SI
;------------------------------------------------------------------------

	even
xr:
	add	si,WP
	and	si,not 1
	even
x:
	READWORD si,si			; get instruction at SI
	jmp	executeX


;------------------------------------------------------------------------
;	CLR --	   	CLeaR
;
;	Results:	*SI = 0
;
;	This instruction is often incorrectly used to write to
;	memory-mapped ports, so optimizations are omitted.
;------------------------------------------------------------------------

	even
clr:
	WRITEWORD si,0
	NEXT

	even
clrr:
	mov	word ptr es:[WP+si],0
	NEXT

;------------------------------------------------------------------------
;	NEG --	   	NEGate
;
;	Results:	*SI = -*SI
;------------------------------------------------------------------------

	even
nneg:
	CLEAR	ST_O


IFDEF	FAST

	neg	word ptr es:[si]
	OVERFLOW?
	mov	ax,es:[si]
	CMP0	ax
	NEXT

ELSE

	READWORD si,ax
	
	neg	ax				; negate it

	OVERFLOW?				; did we negate 8000h?

	WRITEWORD si,ax

	CMP0	ax				; set other flags
	NEXT

ENDIF


	even
nnegr:
	CLEAR	ST_O

	neg	word ptr es:[si+WP]		; negate it
	OVERFLOW?				; did we negate 8000h?
	mov	ax,es:[WP+si]

	CMP0	ax				; set other flags
	NEXT




;------------------------------------------------------------------------
;	INV --	   	INVert
;
;	Results:	*SI = ~*SI
;------------------------------------------------------------------------

	even
inv:
IFDEF	FAST
	not	word ptr es:[si]
	mov	ax,es:[si]
	CMP0	ax	
	NEXT

ELSE

	READWORD si,ax

	not	ax				; invert it

	WRITEWORD si,ax

	CMP0	ax				; set other flags
	NEXT
ENDIF

	even
invr:
	not	word ptr es:[WP+si]		; invert it
	mov	ax,es:[WP+si]
	CMP0	ax				; set other flags
	NEXT



;------------------------------------------------------------------------
;	INC --	   	INCrement
;
;	Results:	*SI++
;------------------------------------------------------------------------

	even
iinc:
	CLEAR	ST_C+ST_O

IFDEF	FAST
	
	add	word ptr es:[si],1

	CARRYOVERFLOW?

	mov	ax,es:[si]
	CMP0	ax
	NEXT

ELSE


	READWORD si,ax

	add	ax,1				; increment it

	CARRYOVERFLOW?

	WRITEWORD si,ax

	CMP0	ax				; set other flags
	NEXT
ENDIF


	even
iincr:
	CLEAR	ST_C+ST_O

	add	word ptr es:[WP+si],1 		; increment it

	CARRYOVERFLOW?

	mov	ax,es:[WP+si]
	CMP0	ax				; set other flags
	NEXT



;------------------------------------------------------------------------
;	INCT --	   	INCrement by Two
;
;	Results:	*SI+=2
;------------------------------------------------------------------------

	even
inct:
	CLEAR	ST_C+ST_O

IFDEF	FAST

	add	word ptr es:[si],2

	CARRYOVERFLOW?

	mov	ax,es:[si]
	CMP0	ax
	NEXT

ELSE

	READWORD si,ax

	add	ax,2				; increment it by 2

	CARRYOVERFLOW?

	WRITEWORD si,ax
	CMP0	ax				; set other flags
	NEXT
ENDIF


	even
inctr:
	CLEAR	ST_C+ST_O

	add	word ptr es:[WP+si],2 		; increment it by 2

	CARRYOVERFLOW?

	mov	ax,es:[WP+si]
	CMP0	ax				; set other flags
	NEXT



;------------------------------------------------------------------------
;	DEC --	   	DECrement
;
;	Results:	*SI--
;------------------------------------------------------------------------

	even
ddec:
	CLEAR	ST_C+ST_O

IFDEF	FAST

	add	word ptr es:[si],-1

	CARRYOVERFLOW?

	mov	ax,es:[si]
	CMP0	ax
	NEXT

ELSE

	READWORD si,ax

	add	ax,-1				; decrement it

	CARRYOVERFLOW?

	WRITEWORD si,ax

	CMP0	ax				; set other flags
	NEXT
ENDIF


	even
ddecr:
	CLEAR	ST_C+ST_O

	add	word ptr es:[WP+si],-1		; decrement it

	CARRYOVERFLOW?

	mov	ax,es:[WP+si]
	CMP0	ax				; set other flags
	NEXT



;------------------------------------------------------------------------
;	DECT --	   	DECrement by Two
;
;	Results:	*SI-=2
;------------------------------------------------------------------------

	even
ddect:
	CLEAR	ST_C+ST_O

IFDEF	FAST
	
	add	word ptr es:[si],-2
	CARRYOVERFLOW?
	mov	ax,es:[si]
	CMP0	ax
	NEXT

ELSE

	READWORD si,ax

       	add	ax,-2				; decrement it by 2

	CARRYOVERFLOW?

	WRITEWORD si,ax

	CMP0	ax				; set other flags
	NEXT
ENDIF

	even
ddectr:
	CLEAR	ST_C+ST_O

       	add	word ptr es:[WP+si],-2		; decrement it by 2

	CARRYOVERFLOW?

	mov	ax,es:[WP+si]
	CMP0	ax				; set other flags
	NEXT



;------------------------------------------------------------------------
;	BL --	   	Branch and Link
;
;	Results:	IP=SI, R11=old IP
;------------------------------------------------------------------------

	even
bblr:
	add	si,WP
	and	si,not 1
	even
bbl:
	mov	ax,IP
	WRITEREG R11,ax	  			; save old IP
	mov	IP,si				; set new one
;;;	CHECKROM
	NEXT


;------------------------------------------------------------------------
;	SWPB --	   	SWaP Bytes
;
;	Results:	*SI = swab(*SI)
;------------------------------------------------------------------------

	even
swpb:

IFDEF	FAST
     	
	ror	word ptr es:[si],8
	NEXT

ELSE

	READWORD si,ax

	xchg	al,ah				; swap bytes

	WRITEWORD si,ax

	NEXT
ENDIF


	even
swpbr:
	ror	word ptr es:[WP+si],8
	NEXT


;------------------------------------------------------------------------
;	SETO --	   	SET Ones
;
;	Results:	*SI = 0FFFFh
;
;	This instruction is used incorrectly to write to memory-
;	mapped ports, so optimizations are omitted.
;------------------------------------------------------------------------

	even
sseto:
	WRITEWORD si,0FFFFh
	NEXT


	even
ssetor:
	mov	word ptr es:[WP+si],-1
	NEXT



;------------------------------------------------------------------------
;	ABS --	   	ABSolute value
;
;	Results:	*SI = |*SI|
;------------------------------------------------------------------------

	even
aabs:
	CLEAR	ST_O

IFDEF	FAST

	READWORD si,ax
	CMP0	ax
	or	ax,ax
	jns	absnot
	neg	word ptr es:[si]
	OVERFLOW?
absnot:
	NEXT

ELSE

	READWORD si,ax

	CMP0	ax				; set other flags BEFORE abs!

	or	ax,ax				; positive?
	jns	absnot				; yup, nothing changes...

	neg	ax				; negate it
	OVERFLOW?				; did we negate 8000h?

	WRITEWORD si,ax

absnot:
ENDIF

	NEXT


	even
aabsr:
	CLEAR	ST_O

	mov	ax,es:[WP+si]
	CMP0	ax				; set other flags BEFORE abs!

	or	ax,ax				; positive?
	jns	absnotr				; yup, nothing changes...

	neg	word ptr es:[WP+si]		; negate it

	OVERFLOW?				; did we negate 8000h?

absnotr:

	NEXT




;==========================================================================
;	Shift instructions, 					>0800->0BFF
;	AND my own instructions, 				>0C00->0FFF
;--------------------------------------------------------------------------
;
;     	  0 1 2 3-4 5 6 7+8 9 A B-C D E F 		SRA, SRL, SLA, SRC     
;	----------------------------------		------------------
;	|  o p c o d e   |   C   |   W   |		DSR, KEYS, SPRI,
;	----------------------------------		TRAN, INT1, BRK,
;							TIDSR, KEYSLOW,
;	Inputs:		SI=opcode			SCREEN
;	Outputs:	SI=address of operand
;			CL=shift count
;			Destroys BX
;
;==========================================================================

	even
h0800:
	mov	bx,si				; copy opcode

	mov	cx,si				; copy opcode
	shr	cl,4
	and	cl,15				; isolate shift count
	jnz	h0800_go			; <>0, absolute shift

	READREG R0,cx				; relative shift in R0
	and	cl,15				; limit
	jnz	h0800_go

	mov	cl,16

h0800_go:
	and	si,15				; isolate W
	add	si,si				; make a word access

	and	bx,07e0h
	shr	bx,4
	jmp	word ptr [h0800table+bx]

	.data
	even

h0800table equ	$
	dw	ssra,ssra,ssra,ssra,ssra,ssra,ssra,ssra
	dw	ssrl,ssrl,ssrl,ssrl,ssrl,ssrl,ssrl,ssrl
	dw	ssla,ssla,ssla,ssla,ssla,ssla,ssla,ssla
	dw	ssrc,ssrc,ssrc,ssrc,ssrc,ssrc,ssrc,ssrc

	dw	dsr,dsr,dsr,dsri,keys,spritemove,transfer,irtwp	; >0C00
	dw	int1,tidsr,keyslow,screen,bmove,dat,dat,dat 	; >0D00
	dw	break,dat,dat,dat,dat,dat,dat,dat		; >0E00
	dw	dat,dat,dat,dat,dat,dat,dat,dat			; >0F00

	.code

;------------------------------------------------------------------------
;	SRA --	   	Shift Right Arithmetic
;
;	Results:	*SI>>=CL
;------------------------------------------------------------------------

	even
ssra:
	CLEAR	ST_C

	sar	word ptr es:[WP+si],cl	   	; shift right arithmetic

	CARRY?					; carry?

	mov	ax,es:[WP+si]
	CMP0	ax
	NEXT


;------------------------------------------------------------------------
;	SRL --	   	Shift Right Logical
;
;	Results:	*SI>>=CL
;------------------------------------------------------------------------

	even
ssrl:
	CLEAR	ST_C

	shr	word ptr es:[WP+si],cl		; shift right logical

	CARRY?					; carry?

	mov	ax,es:[WP+si]
	CMP0	ax
	NEXT


;------------------------------------------------------------------------
;	SLA --	   	Shift Left Arithmetic
;
;	Results:	*SI<<=CL
;
;	The 80x86 processor doesn't set the OV flag correctly
;	unless ONE bit is shifted.  The unoptimized code takes
;	this fact into account.
;------------------------------------------------------------------------

	even
ssla:
	CLEAR	ST_C+ST_O

	mov	ax,es:[WP+si]			; save original

	sal	word ptr es:[WP+si],cl

	CARRY?

;	The overflow status bit is NOT SET for multiple shifts on an 80x86.
;	So, we'll simply check the sign bit for a change, which sets the
;	overflow bit.

	xor	bx,es:[WP+si]			; test sign bit for change
	jns	sslanot		

	OVERFLOW				; any change means OV=1 
						; on 9900

sslanot:
	mov	ax,es:[WP+si]
	CMP0	ax
	NEXT


;------------------------------------------------------------------------
;	SRC --	   	Shift Right Circular
;
;	Results:	*SI=rightcircularshift(*SI,CL)
;------------------------------------------------------------------------

	even
ssrc:
	CLEAR	ST_C

	ror	word ptr es:[WP+si],cl		; rotate right

	CARRY?					; carry?

	mov	ax,es:[WP+si]
	CMP0	ax
	NEXT



;------------------------------------------------------------------------
;	DSR --	   	Branch to file handler for emulated file access
;------------------------------------------------------------------------

	even
dsr:					; DSR jump....
	cmp	IP,4000h
	jb	dsrnot
	cmp	IP,6000h
	jae	dsrnot			; is it really IT?!
	sub	IP,2			; point to opcode that called
	call	handlefileoperations	; will set PC to return
dsrnot:
	NEXT


;------------------------------------------------------------------------
;	DSRI --		Interrupt DSR routine.
;------------------------------------------------------------------------

	even
dsri:
	cmp	IP,4000h
	jb	dsrinot
	cmp	IP,6000h
	jae	dsrinot
	sub	IP,2
	call	handlefileinterrupt
dsrinot:
	NEXT


;------------------------------------------------------------------------
;	KEYS --	   	Read keyboard faster
;
;	This is activated by the PT_keyboard mask in Patches
;------------------------------------------------------------------------

	even
keys:
	cmp	IP,02b2h+2
	jne	keysnot
	call	handlekeyboard
keysnot:
	NEXT


;------------------------------------------------------------------------
;	SPRITEMOVE --  	Optimized sprite move routine.
;
;	Called during INT 1.
;------------------------------------------------------------------------

	even
spritemove:
	cmp	IP,0962h+2
	jne	spritemovenot
	call	movesprites
spritemovenot:
	NEXT


;------------------------------------------------------------------------
;	TRANSFER --	Optimized memory block move.
;
;	Called from GPL interpreter.
;------------------------------------------------------------------------

	even
transfer:
	cmp	IP,065eh+2
	jne	transfernot
	call	dotransfer
transfernot:
	NEXT


;------------------------------------------------------------------------
;	INT1 --	   	Optimized Interrupt handler.
;------------------------------------------------------------------------

	even
int1:
	cmp	IP,094ch+2
	jne	int1not
	call	doint1
int1not:
	NEXT


;------------------------------------------------------------------------
;	BREAK --   	Breakpoint
;------------------------------------------------------------------------

	even
break:
        or      stateflag,debugrequest
	add	PC,-2
        NEXT


;------------------------------------------------------------------------
;	TIDSR --	Optimized sector, track read/write loops
;------------------------------------------------------------------------

	even
tidsr:
	cmp	usediskdsr,0
	jz	tidsr_i
	cmp	IP,4214h+2		; read sector loop (only)
	je	tidsr_rs
	cmp	IP,426ch+2		; write sector loop (only)
	je	tidsr_ws
	cmp	IP,4440h+2		; write track (only)
	jne	tidsr_i			; ignore
	mov	IP,4458h		; return here
	jmp	tidsr_w
tidsr_ws:
	mov	IP,4284h		; return from write sector
tidsr_w:
	call	DSKoptwrite		; uses VADDR and R6
	jmp	tidsr_i
tidsr_rs:
	mov	IP,422ch		; return from read sector
tidsr_r:	
	call	DSKoptread
tidsr_i:
	NEXT


;--------------------------------------------------------------------------
;	KEYSLOW --	Pace keyboard response
;--------------------------------------------------------------------------

keyslow:
	cmp	IP,484h+2		; only reached from ROM routine
	jne	keyslownot

	call	kscandelay		; handlekeyboard calls directly

keyslownot:
	NEXT


;--------------------------------------------------------------------------
;	SCREEN --	Fill screen with character
;--------------------------------------------------------------------------

screen:
	cmp	IP,5ach+2
	jne	screennot

	call	screenfill

screennot:
	NEXT


;--------------------------------------------------------------------------
;	BMOVE --	Moves ROM from >A000 to >0000 and restarts
;--------------------------------------------------------------------------

bmove:
	push	cx
	push	si
	push	di

	mov	cx,4096
	mov	si,0a000h
	xor	di,di
bmove0:
	mov	ax,es:[si]
	mov	es:[di],ax
	add	si,2
	add	di,2
	loop	bmove0

	or	stateflag,reboot

	pop	di
	pop	si
	pop	cx
	NEXT



;==========================================================================
;	Jump, CRU bit instructions,				>1000->1FFF
;--------------------------------------------------------------------------
;
;     	  0 1 2 3-4 5 6 7+8 9 A B-C D E F 		JMP, JLT, JLE, JEQ,
;  	----------------------------------		JHE, JGT, JNE, JNC,
;	|   o p c o d e  | signed offset |		JOC, JNO, JL,JH,JOP
;	----------------------------------		---------------------
;							SBO, SBZ, TB
;	Inputs:		SI=opcode
;	Outputs:	AX=signed word offset
;			Destroys SI
;
;==========================================================================

	even
h1000:
	mov	ax,si				; save opcode
	
	cbw	     				; signed byte --> integer
	add	ax,ax				; word offset

	and	si,0f00h
	shr	si,7

	jmp	[cs:word ptr h1000table+si]

	even

h1000table equ	$
	dw	jjmp,jjlt,jjle,jjeq,jjhe,jjgt,jjne,jjnc
	dw	jjoc,jjno,jjl,jjh,jjop,sbo,sbz,tb


;------------------------------------------------------------------------
;	JMP --  	Unconditional jump
;
;	Results:	IP+=AX
;------------------------------------------------------------------------

	even
jjmp:	jmp		jjump


	even
jjumpb:
	add	IP,ax
	NEXT


;------------------------------------------------------------------------
;	JLT --  	Jump if Less Than (arithmetic)
;
;	Results:	IF (A>=0 && EQ==0), IP+=AX
;------------------------------------------------------------------------

	even
jjlt:
	mov	bx,lastval
	cmp	bx,lastcmp

	jl		jjumpb
	NEXT


;------------------------------------------------------------------------
;	JLE --  	Jump if Lower or Equal (logical)
;
;	Results:	IF (L>=0 || EQ==1), IP+=AX
;------------------------------------------------------------------------

	even
jjle:
	mov	bx,lastval
	cmp	bx,lastcmp

	jbe		jjumpb
	NEXT


;------------------------------------------------------------------------
;	JEQ --  	Jump if Equal
;
;	Results:	IF (EQ==1), IP+=AX
;------------------------------------------------------------------------

	even
jjeq:
	mov	bx,lastval
	cmp	bx,lastcmp

	je		jjumpb
	NEXT


;------------------------------------------------------------------------
;	JHE --  	Jump if Higher or Equal (logical)
;
;	Results:	IF (L>=1 || EQ==1), IP+=AX
;------------------------------------------------------------------------

	even
jjhe:
	mov	bx,lastval
	cmp	bx,lastcmp

	jae		jjumpb
	NEXT


;------------------------------------------------------------------------
;	JGT --  	Jump if Greater Than (arithmetic)
;
;	Results:	IF (A>=1 && EQ==0), IP+=AX
;------------------------------------------------------------------------

	even
jjgt:
	mov	bx,lastval
	cmp	bx,lastcmp

	jg		jjumpb
jjgts:
	NEXT


;------------------------------------------------------------------------
;	JNE --  	Jump if Not Equal
;
;	Results:	IF (EQ==0), IP+=AX
;------------------------------------------------------------------------

	even
jjne:
	mov	bx,lastval
	cmp	bx,lastcmp

	jne		jjumpb
	NEXT


;------------------------------------------------------------------------
;	JNC --  	Jump if No Carry
;
;	Results:	IF (C==0), IP+=AX
;------------------------------------------------------------------------

	even
jjnc:
	ISNOTCARRY?	jjump
	NEXT


;------------------------------------------------------------------------
;	JOC --  	Jump On Carry
;
;	Results:	IF (C==1), IP+=AX
;------------------------------------------------------------------------

	even
jjoc:
	ISCARRY?	jjump
	NEXT


;------------------------------------------------------------------------
;	JNO --  	Jump if No Overflow
;
;	Results:	IF (OV==0), IP+=AX
;------------------------------------------------------------------------

	even
jjno:
	ISNOTOVERFLOW?	jjump
	NEXT


;------------------------------------------------------------------------
;	JL --  		Jump if Lower (logical)
;
;	Results:	IF (L>=0 && EQ==0), IP+=AX
;------------------------------------------------------------------------

	even
jjl:
	mov	bx,lastval
	cmp	bx,lastcmp

	jb		jjump
	NEXT


;------------------------------------------------------------------------
;	JH --    	Jump if Higher (logical)
;
;	Results:	IF (L>=1), IP+=AX
;------------------------------------------------------------------------

	even
jjh:
	mov	bx,lastval
	cmp	bx,lastcmp

	ja		jjump
	NEXT


;------------------------------------------------------------------------
;	JOP --  	Jump On Parity
;
;	Results:	IF (P==1), IP+=AX
;------------------------------------------------------------------------

	even
jjop:
	ISODDPARITY?	jjump
	NEXT


	even
jjump:
	add	IP,ax
	NEXT



;------------------------------------------------------------------------
;	SBO --  	Set Bit to One
;
;	Results:	CRU Bit==1
;------------------------------------------------------------------------

	even
sbo:
	push	dx			; modified (DX=STAT!)

	READREG	R12,dx			; get CRU base
	add	dx,ax	       		; create CRU address
	mov	cl,1			; setup for writeseveralCRU
	mov	ax,100h			; turn it on
	call	writeseveralCRU		; do it

	pop	dx			; restore DX (STAT!)
	NEXT


;------------------------------------------------------------------------
;	SBZ --  	Set Bit to Zero
;
;	Results:	CRU Bit==0
;------------------------------------------------------------------------

	even
sbz:
	push	dx			; modified (DX=STAT!)

	READREG	R12,dx			; get CRU base
	add	dx,ax	       		; create CRU address
	mov	cl,1			; setup for writeseveralCRU
	xor	ax,ax			; turn it off
	call	writeseveralCRU		; do it

	pop	dx			; restore DX (STAT!)
	NEXT


;------------------------------------------------------------------------
;	TB --	  	Test Bit
;
;	Results:	EQ=CRU Bit
;------------------------------------------------------------------------

	even
tb:
	push	dx			; modified (DX=STAT!)

	READREG	R12,dx			; get CRU base
	add	dx,ax	       		; create CRU address
	mov	cl,1			; setup for readeseveralCRU
	call	readseveralCRU		; read it
	pop	dx			; restore DX (STAT!)

	not	ax
	test	ax,100h			; set?

	EQUAL?

tbnot:
	NEXT


;==========================================================================
;	General and One-Register instructions			>2000->3FFF
;--------------------------------------------------------------------------
;
;     	  0 1 2 3-4 5 6 7+8 9 A B-C D E F 		COC, CZC, XOR,     
;	----------------------------------		LDCR, STCR, XOP,
;	|   opcode   |   D   |TS |   S   |		MPY, DIV
;	----------------------------------		
;							
;	Inputs:		SI=opcode
;	Outputs:	BX=source address 		(S)
;			SI=destination register offset	(D)
;
;==========================================================================

	even
h2000:
	cmp	si,3000h			; check if LDCR/STCR
	jb	h2000_notcru
	cmp	si,3800h
	jae	h2000_notcru			

	cmp	si,3400h			; yup... LDCR or STCR?
	jb	h2000ldcr

	jmp	stcr

h2000ldcr:
	jmp	ldcr

h2000_notcru:
	mov	cx,si				; save opcode

	mov	bx,si				; copy opcode

	and	si,15				; single out S
	add	si,si				; times two	(S)

	shr	bx,4				; isolate the
	and	bx,3				; TS field	(TS)

	jnz	h2000_1				; TS=0?

;
;	TS=0 --	return register address
;

	add	si,WP	       
	jmp	h2000_go

h2000_1:
	dec	bx				; TS=1?				
	jnz	h2000_2

;
;	TS=1 --	return value in register		
;

	READREG	si,si				; Get SI=*reg
	jmp	h2000_go

h2000_2:
	dec	bx				; TS=2?
	jnz	h2000_3

;
;	TS=2 --	return value in register plus address at IP
;

	FETCH@	bx				; get address at IP
	or	si,si
	jz	h2000_2go			; S=0 so BX is full addr

	READREG	si,si				; else get the register
	add	bx,si				; and add it to the address

h2000_2go:
	jmp	h2000_go2

h2000_3:
;
;	TS=3 --	return value at address, and increment register by 2.
;

	READREG	si,ax				; get AX=value of register
	add	word ptr es:[wp+si],2		; increment register
	mov	si,ax				; get back value

h2000_go:
	mov	bx,si
h2000_go2:
	and	bx,not 1			; fix to word boundary

	mov	si,cx				; restore opcode
	shr	si,6				; isolate
	and	si,15				; D
	add	si,si				; make word access

	mov	di,cx
	and	di,1c00h
	shr	di,9
	jmp	word ptr [h2000table+di]

	.data
	even

h2000table equ	$
	dw	coc,czc,xxor,xxop,ldcr,stcr,mpy,ddiv

	.code

;------------------------------------------------------------------------
;	COC --	   	Compare Ones Corresponding
;
;	Results:	stat = (S&D == S)
;------------------------------------------------------------------------

	even
coc:
	READWORD BX,cx				; get S
	READREG	SI,ax				; get D

	and	ax,cx				; mask
	cmp	ax,cx				; compare

	EQUAL?					; same?

	NEXT


;------------------------------------------------------------------------
;	CZC --	   	Compare Zeroes Corresponding
;
;	Results:	stat = (S&~D == S)
;------------------------------------------------------------------------

	even
czc:
	READWORD BX,cx				; get S
	READREG	SI,ax				; get D

	not	ax				; 0's <--> 1's
	and	ax,cx				; mask
	cmp	ax,cx				; compare

	EQUAL?					; same?

	NEXT


;------------------------------------------------------------------------
;	XOR --	   	eXclusive OR
;
;	Results:	D ^= S
;------------------------------------------------------------------------

	even
xxor:
IFDEF	FAST

	READWORD bx,cx
	CHANGEREG si,xor

ELSE

	READWORD bx,cx				; get S in BX
	READREG	si,ax				; get D in AX

	xor	ax,cx				; XOR

	WRITEREG si,ax				; restore register

ENDIF

	CMP0	ax				; set other bits	

	NEXT


;------------------------------------------------------------------------
;	XOP --	   	eXtended OPeration
;
;	Result:		WP = *(40h+D)+, IP = *(42h+D)
;			New R13=old WP, New R14=Old IP, New R15=Old STAT 
;			New R11=S
;			Xop bit set
;------------------------------------------------------------------------

	even
xxop:
	CLEAR	ST_X

	push	bx				; save new R11

	add	si,si				; word --> vector
	add	si,40h				; XOP base
	READWORD si,bx	       			; get new WP address
	and	bx,not 1			; force word boundary

	add	si,2
	READWORD si,cx				; get new IP address
	and	cx,not 1			; force word boundary

	mov	ax,WP
	mov	WP,bx				; change
	call	validateWP			; get new WPSEG

	WRITEREG R13,ax	  			; save old WP
	mov	ax,IP
	WRITEREG R14,ax				; save return IP
	SETSTAT
	WRITEREG R15,STAT			; save STAT

	mov	IP,cx

	pop	bx
	WRITEREG R11,bx				; pass the operand
	
	XOP					; set XOP flag

	jmp	nostate
	NEXT


;------------------------------------------------------------------------
;	LDCR --	   	LoaD into CRu
;
;	Decode instruction!
;
;	This is necessary because LDCR can be both a BYTE or a WORD
;	instruction, and all bets are off.
;
;
;
;	Results:	CRU R12--CRU R12+D-1 set to S
;------------------------------------------------------------------------

	even
ldcr:
	mov	cx,si				; CX=# bits
	and	cx,03c0h
	shr	cx,6
	jnz	ldcr00
	mov	cx,16

ldcr00:
	mov	bx,si				; copy opcode

	and	si,15				; single out S
	add	si,si				; times two	(S)

	shr	bx,4				; isolate the
	and	bx,3				; TS field	(TS)

	jnz	ldcr_1				; TS=0?

;
;	TS=0 --	return register address
;

	add	si,WP	       
	jmp	ldcr_go

ldcr_1:
	dec	bx				; TS=1?				
	jnz	ldcr_2

;
;	TS=1 --	return value in register		
;

	READREG	si,si				; Get SI=*reg
	jmp	ldcr_go

ldcr_2:
	dec	bx				; TS=2?
	jnz	ldcr_3

;
;	TS=2 --	return value in register plus address at IP
;

	FETCH@	bx				; get address at IP
	or	si,si
	jz	ldcr_2go			; S=0 so BX is full addr

	READREG	si,si				; else get the register
	add	bx,si				; and add it to the address

ldcr_2go:
	jmp	ldcr_go2

ldcr_3:
;
;	TS=3 --	return value at address, and increment register by 2.
;

	READREG	si,ax				; get AX=value of register
	cmp	cx,8
	jbe	ldcr_31
	inc	word ptr es:[wp+si]	   	; increment register
ldcr_31:
        inc	word ptr es:[wp+si] 		; increment register
	mov	si,ax				; get back value

ldcr_go:
	mov	bx,si
ldcr_go2:

	cmp	cx,8
	ja	ldcrw
	
	xor	bx,1				; fix byte addressing
	READBYTE bx,ah
	CMP0B	ah
	jmp	ldcrd

ldcrw:
	and	bx,not 1			; fix to word boundary
	READWORD bx,ax
	CMP0	ax

ldcrd:
	push	dx
	READREG R12,dx
	call	writeseveralCRU
	pop	dx

	NEXT


;------------------------------------------------------------------------
;	STCR --	   	Store from CRu
;
;	Decode instruction!
;
;	This is necessary because STCR can be both a BYTE or a WORD
;	instruction, and all bets are off.
;
;
;	Results:	S = CRU R12--CRU R12+D-1
;------------------------------------------------------------------------

	even
stcr:
	mov	cx,si				; CX=# bits
	and	cx,03c0h
	shr	cx,6
	jnz	stcr00
	mov	cx,16

stcr00:
	mov	bx,si				; copy opcode

	and	si,15				; single out S
	add	si,si				; times two	(S)

	shr	bx,4				; isolate the
	and	bx,3				; TS field	(TS)

	jnz	stcr_1				; TS=0?

;
;	TS=0 --	return register address
;

	add	si,WP	       
	jmp	stcr_go

stcr_1:
	dec	bx				; TS=1?				
	jnz	stcr_2

;
;	TS=1 --	return value in register		
;

	READREG	si,si				; Get SI=*reg
	jmp	stcr_go

stcr_2:
	dec	bx				; TS=2?
	jnz	stcr_3

;
;	TS=2 --	return value in register plus address at IP
;

	FETCH@	bx				; get address at IP
	or	si,si
	jz	stcr_2go			; S=0 so BX is full addr

	READREG	si,si				; else get the register
	add	bx,si				; and add it to the address

stcr_2go:
	jmp	stcr_go2

stcr_3:
;
;	TS=3 --	return value at address, and increment register by 2.
;

	READREG	si,ax				; get AX=value of register
	cmp	cx,8
	jbe	stcr_31
	inc	word ptr es:[wp+si]	  	; increment register
stcr_31:
	inc	word ptr es:[wp+si] 		; increment register
	mov	si,ax				; get back value

stcr_go:
	mov	bx,si
stcr_go2:

	push	cx

	cmp	cx,8
	ja	stcraw
	
	xor	ah,ah
	jmp	stcrdo
stcraw:
	xor	ax,ax
stcrdo:
	push	dx
	READREG R12,dx
	call	readseveralCRU
	pop	dx
	pop	cx

	cmp	cx,8
	ja	stcrw

	CMP0B	AH
	xor	bx,1
	WRITEBYTE bx,ah
	jmp	stcrd

stcrw:
	CMP0	AX
	and	bx,not 1
	WRITEWORD bx,ax
stcrd:

	NEXT


;------------------------------------------------------------------------
;	MPY --	   	MultiPlY
;
;	GAS=BX,  WAD=SI
;
;	Results:	D:D+1 = D*S
;------------------------------------------------------------------------

	even
mpy:
	READWORD BX,bx				; get source number
	READREG	SI,ax				; get D in SI

	push	dx				; save DX (STAT!)

	mul	bx

	WRITEREG SI,dx				; save high word
	WRITEREG si+2,ax			; save low word

	pop	dx				; restore DX (stat!)

	NEXT


;------------------------------------------------------------------------
;	DIV --	   	DIVide
;
;	GAS=BX,  WAD=SI
;
;	Results:	D = D/S  D+1 = D%S
;------------------------------------------------------------------------

	even
ddiv:
	CLEAR	ST_O

	push	dx				; save DX (STAT!)

	READWORD BX,bx				; get source number
	READREG	SI,dx				; get high word
	READREG SI+2,ax				; get low word

	cmp	bx,dx
	jbe	ddiverr

	div	bx
	jmp	ddivskip

ddiverr:
	pop	dx				; restore DX (STAT!)
	OVERFLOW				; overflow!
	NEXT					; continue on...

ddivskip:
	WRITEREG si,ax	 			; save quotient
	WRITEREG si+2,dx			; save remainder

	pop	dx				; restore DX (stat!)

	NEXT



;==========================================================================
;	Two-Register instructions				>4000->FFFF
;--------------------------------------------------------------------------
;
;     	  0 1 2 3-4 5 6 7+8 9 A B-C D E F 		SZC, SZCB, S, SB,
;	----------------------------------		C, CB, A, AB, MOV,
;	|opcode|B|TD |   D   |TS |   S   |		MOVB, SOC, SOCB
;	----------------------------------		
;							
;	Inputs:		SI=opcode
;	Outputs:	SI=address of source		(S)
;			BX=address of destination	(D)
;
;==========================================================================

	even

h4000:
	mov	cx,si				; save opcode

	mov	bx,si				; copy opcode

	test	bx,0c30h
	jnz	h4000_some
	jmp	h4000_reg

h4000_some:
	and	si,15				; single out S
	add	si,si				; times two	(S)

	and	bx,030h
	shr	bx,3
	jmp	[word ptr h4000ts+bx]


	.data

	even
h4000ts	dw	h4000_0,h4000_1,h4000_2,h4000_3

	.code

;
;	TS=0 --	return register address
;

h4000_0:
	add	si,WP	       
	jmp	h4000_go


;
;	TS=1 --	return value in register		
;

h4000_1:
	READREG	si,si				; Get SI=*reg
	jmp	h4000_go


;
;	TS=2 --	return value in register plus address at IP
;

h4000_2:

	FETCH@	bx				; get address at IP
	or	si,si
	jz	h4000_2go			; S=0 so BX is full addr

	READREG	si,si				; else get the register
	add	bx,si				; and add it to the address

h4000_2go:
	jmp	h4000_go2


;
;	TS=3 --	return value at address, and increment register by 2.
;

h4000_3:
	READREG	si,ax				; get AX=value of register

	test	cx,1000h			; byte instruction?
	jz	h4000_3w			; no

	inc	word ptr es:[wp+si]		; increment by 1
	jmp	h4000_3n

h4000_3w:
	add	word ptr es:[wp+si],2		; increment by 2

h4000_3n:
	mov	si,ax				; get back value

h4000_go:
	mov	bx,si
h4000_go2:

	push	bx				; ****  SAVE S!

	mov	si,cx				; restore opcode
	shr	si,6				; shift over to D
	mov	bx,si				; copy opcode

	and	si,15				; single out S
	add	si,si				; times two	(S)

	and	bx,30h
	shr	bx,3
	jmp	[word ptr h4001td+bx]


	.data

	even
h4001td	dw	h4001_0,h4001_1,h4001_2,h4001_3

	.code

;
;	TS=0 --	return register address
;
h4001_0:
	add	si,WP	       
	jmp	h4001_go

;
;	TS=1 --	return value in register		
;

h4001_1:
	READREG	si,si				; Get SI=*reg
	jmp	h4001_go

;
;	TS=2 --	return value in register plus address at IP
;

h4001_2:
	FETCH	bx				; restore ES:DI, get *IP+
	or	si,si
	jz	h4001_2go			; S=0 so BX is full addr

	READREG	si,si				; else get the register
	add	bx,si				; and add it to the address

h4001_2go:
	jmp	h4001_go2


;
;	TS=3 --	return value at address, and increment register by 2.
;
h4001_3:
	READREG	si,ax				; get AX=value of register

	test	cx,1000h			; byte instruction?
	jz	h4001_3w			; nope

	inc	word ptr es:[wp+si]		; increment by 1
	jmp	h4001_3n			

h4001_3w:
	add	word ptr es:[wp+si],2		; increment by 2

h4001_3n:
	mov	si,ax				; get back value

h4001_go:
	mov	bx,si
h4001_go2:

	pop	si				; ****  RETRIEVE S


	mov	DI,cx
	and	DI,0f000h
	shr	DI,11
	jmp	word ptr [h4000table+DI]

h4000_reg:
	mov	di,cx
	and	di,03c0h
	shr	di,5
	mov	si,cx
	and	si,15
	add	si,si

	mov	bx,cx
	and	bx,0f000h
	shr	bx,11
	jmp	word ptr [h4000regtable+bx]

	.data

	even

h4000table equ	$
	dw	dat,dat,dat,dat,szc,szcb,s,sb
	dw	c,cb,a,ab,mmov,movb,soc,socb

h4000regtable equ $
	dw	dat,dat,dat,dat,szcr,szcbr,sr,sbr
	dw	cr,cbr,ar,abr,mmovr,movbr,socr,socbr

	.code


;------------------------------------------------------------------------
;	SZC --	   	Set Zeros Corresponding
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D &= ~S
;------------------------------------------------------------------------

	even
szc:
	and	si,not 1			; fix to word
	and	bx,not 1			; fix to word

IFDEF	FAST

	READWORD si,ax
	not	ax
	and	es:[bx],ax
	mov	ax,es:[bx]

ELSE

	READWORD si,cx				; get source in CX

	READWORD bx,ax

	not	cx				; 0's <---> 1's
	and	ax,cx				; mask

	WRITEWORD bx,ax

ENDIF

	CMP0	ax				; set other bits	

	NEXT


	even
szcr:
	mov	cx,es:[WP+si]
	not	cx				; 0's <---> 1's
	and	es:[WP+di],cx 			; mask
	mov	ax,es:[WP+di]
	CMP0	ax				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	SZCB --	   	Set Zeros Corresponding, Byte
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D &= ~S
;------------------------------------------------------------------------

	even
szcb:
	CLEAR	ST_P

	xor	si,1				; complement
	xor	bx,1				; complement

	READBYTE si,cl				; get source in CX

	READBYTE bx,al

	not	cl				; 0's <---> 1's
	and	al,cl				; mask

	WRITEBYTE bx,al


	CMP0B	al				; set other bits	

	NEXT


	even
szcbr:
	CLEAR	ST_P

	mov	cl,es:[WP+si+1]
	not	cl				; 0's <---> 1's
	and	es:[WP+di+1],cl	   		; mask
	mov	al,es:[WP+di+1]
	CMP0B	al				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	S --	   	Subtract
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D -= S
;------------------------------------------------------------------------

	even
s:
	CLEAR	ST_C+ST_O

	and	si,not 1			; fix to word
	and	bx,not 1			; fix to word

IFDEF	FAST

	READWORD si,ax

	neg	ax				; negate subtrahend
	jnz	s0				; if non-zero, no carry
	CARRY					; (TI does INV, INC, ADD)
s0:
	add	es:[bx],ax

	CARRYOVERFLOW?

	mov	ax,es:[bx]

ELSE

	READWORD si,cx				; get source in CX

	READWORD bx,ax

	neg	cx
	jnz	s0
	CARRY
s0:
	add	ax,cx				; subtract

	CARRYOVERFLOW?

	WRITEWORD bx,ax

ENDIF

	CMP0	ax				; set other bits	

	NEXT


	even
sr:
	CLEAR	ST_C+ST_O

	mov	cx,es:[WP+si]
	
	neg	cx
	jnz	s0r
	CARRY
s0r:
	add	es:[WP+di],cx			; subtract

	CARRYOVERFLOW?

	mov	ax,es:[WP+di]
	CMP0	ax				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	SB --	   	Subtract, Byte
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D -= S
;------------------------------------------------------------------------

	even
sb:
	CLEAR	ST_C+ST_O+ST_P

	xor	si,1				; complement
	xor	bx,1				; complement

	READBYTE si,cl				; get source in CX

	READBYTE bx,al

	neg	cl
	jnz	sb0
      	CARRY
sb0:
	add	al,cl				; subtract

	CARRYOVERFLOW?

	WRITEBYTE bx,al

	CMP0B	al				; set other bits	

	NEXT


	even
sbr:
	CLEAR	ST_C+ST_O+ST_P

	mov	cl,es:[WP+si+1]

	neg	cl
	jnz	sb0r
      	CARRY
sb0r:
	add	es:[WP+di+1],cl			; subtract

	CARRYOVERFLOW?

	mov	al,es:[WP+di+1]
	CMP0B	al				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	C --	   	Compare
;
;	SI=GAS
;	BX=GAD
;
;	Results:	status = (D - S)
;------------------------------------------------------------------------

	even
c:
	and	si,not 1			; fix to word
	and	bx,not 1			; fix to word

	READWORD si,cx				; get source in CX
	READWORD bx,ax    			; get destination in AX

	CMPTO	cx,ax				; set other bits	

	NEXT


	even
cr:
	mov	cx,es:[WP+si]
	mov	ax,es:[WP+di]
	CMPTO	cx,ax				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	CB --	   	Compare Bytes
;
;	SI=GAS
;	BX=GAD
;
;	Results:	stat = (D - S)
;------------------------------------------------------------------------

	even
cb:
	CLEAR	ST_P

	xor	si,1				; complement
	xor	bx,1				; complement

	READBYTE si,cl				; get source in CL
	READBYTE bx,al    			; get destination in AL

	CMPTOB	cl,al				; set other bits	

	NEXT


	even
cbr:
	CLEAR	ST_P

	mov	cl,es:[WP+si+1]
	mov	al,es:[WP+di+1]

	CMPTOB	cl,al				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	A --	   	Add
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D += S
;------------------------------------------------------------------------

	even
a:
	CLEAR	ST_C+ST_O

	and	si,not 1			; fix to word
	and	bx,not 1			; fix to word

IFDEF	FAST

	READWORD si,ax
	add	es:[bx],ax

	CARRYOVERFLOW?

	mov	ax,es:[bx]

ELSE

	READWORD si,cx				; get source in CX

	READWORD bx,ax

	add	ax,cx				; subtract

	CARRYOVERFLOW?

	WRITEWORD bx,ax

ENDIF

	CMP0	ax				; set other bits	

	NEXT


	even
ar:
	CLEAR	ST_C+ST_O

	mov	cx,es:[WP+si]

	add	es:[WP+di],cx			; subtract

	CARRYOVERFLOW?

	mov	ax,es:[WP+di]
	CMP0	ax				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	AB --	   	Add, Byte
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D -= S
;------------------------------------------------------------------------

	even
ab:	
	CLEAR	ST_C+ST_O+ST_P

	xor	si,1				; complement
	xor	bx,1				; complement

	READBYTE si,cl				; get source in CX

	READBYTE bx,al

	add	al,cl				; subtract

	CARRYOVERFLOW?

	WRITEBYTE bx,al

	CMP0B	al				; set other bits	

	NEXT


	even
abr:	
	CLEAR	ST_C+ST_O+ST_P

	mov	cl,es:[WP+si+1]

	add	es:[WP+di+1],cl			; subtract

	CARRYOVERFLOW?

	mov	al,es:[WP+di+1]
	CMP0B	al				; set other bits	

	NEXT




;------------------------------------------------------------------------
;	MOV --	   	MOVe
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D = S
;------------------------------------------------------------------------

	even
mmov:
	and	si,not 1			; fix to word
	and	bx,not 1			; fix to word

	READWORD si,ax				; get source in CX
	WRITEWORD bx,ax    			; get destination in AX

	CMP0	ax				; set other bits	

	NEXT


	even
mmovr:
	mov	ax,es:[WP+si]
	mov	es:[WP+di],ax
	CMP0	ax				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	MOVB --	   	MOVe Bytes
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D = S
;------------------------------------------------------------------------

	even
movb:
	CLEAR	ST_P

	xor	si,1				; complement
	xor	bx,1				; complement

	READBYTE si,al				; get source in CL
	WRITEBYTE bx,al    			; get destination in AL

	CMP0B	al				; set other bits	

	NEXT


	even
movbr:
	CLEAR	ST_P

	mov	al,es:[WP+si+1]
	mov	es:[WP+di+1],al
	CMP0B	al				; set other bits	

	NEXT




;------------------------------------------------------------------------
;	SOC --	   	Set Ones Corresponding
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D |= S
;------------------------------------------------------------------------

	even
soc:
	and	si,not 1			; fix to word
	and	bx,not 1			; fix to word

IFDEF	FAST

	READWORD si,ax
	or	es:[bx],ax
	mov	ax,es:[bx]

ELSE

	READWORD si,cx				; get source in CX

	READWORD bx,ax

	or	ax,cx				; mask

	WRITEWORD bx,ax

ENDIF

	CMP0	ax				; set other bits	

	NEXT


	even
socr:
	mov	cx,es:[WP+si]
	or	es:[WP+di],cx	 		; mask
	mov	ax,es:[WP+di]
	CMP0	ax				; set other bits	

	NEXT



;------------------------------------------------------------------------
;	SOCB --	   	Set Ones Corresponding, Byte
;
;	SI=GAS
;	BX=GAD
;
;	Results:	D |= S
;------------------------------------------------------------------------

	even
socb:
	CLEAR	ST_P

	xor	si,1				; complement
	xor	bx,1				; complement

	READBYTE si,cl				; get source in CX

	READBYTE bx,al

	or	al,cl				; mask

	WRITEBYTE bx,al

	CMP0B	al				; set other bits	

	NEXT


	even
socbr:
	CLEAR	ST_P

	mov	cl,es:[WP+si+1]
	or	es:[WP+di+1],cl			; mask
	mov	al,es:[WP+di+1]
	CMP0B	al				; set other bits	

	NEXT



