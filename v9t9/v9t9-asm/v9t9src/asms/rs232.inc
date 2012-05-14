; ****************************
; TI Emulator! RS232 routines
; ****************************
; by Edward Swartz,  2/3/95
; ****************************

;       Directly emulate the RS232 via the CRU and PC serial ports.
;
;

	.data


TRCHAR	equ	0	; transmit/receive buffer
DIVL	equ	0	; divisor latch LSB
INTENA	equ	1	; interrupt enable register
DIVH	equ	1	; divisor latch MSB
INTID	equ	2	; interrupt ID reg
LNCTRL	equ	3	; line control register
MDMCTRL	equ	4	; modem control register
LNSTAT	equ	5	; line status
MDMSTAT	equ	6	; modem status




rs1	rsstruc	<0,0,0>
rs2	rsstruc	<0,0,0>

lastpio	dw	0				; last CRU base accessed
pio1	piostruc <0,0>
pio2	piostruc <0,0>

	.code


;	RSELECT -- Select the correct RSx structure based on the CRU
;	address in DX.
;
;	Returns SI -> RSx, or CY=1 if undefined
;
rselect	proc	near
	cmp	dx,1380h
	lea	si,rs1
	jb	rsgotit
	lea	si,rs2
rsgotit:
	cmp	[si].port,0
	jnz	rsselected
	stc
	jmp	rsselectout
rsselected:
	clc
rsselectout:
	ret
	endp


;-------------------------------------------------------------------------
;	BUFFERED DATA ROUTINES
;-------------------------------------------------------------------------



rs232isbuffered proc near
	clc
	ret


	push	si
	lea	si,rs1
	call	isbuffered
	jc	ribout
	lea	si,rs2
	call	isbuffered
ribout:
	jnc	ribnoset
	or	[si].intflag,RIEINT
	stc
ribnoset:
	pop	si
	ret
	endp


;	ISBUFFERED -- sets C=1 if there is something in the buffer.
;
;	Inputs:	SI -> RSx

isbuffered proc near
	push	ax
;	cmp	[si].intgoing,0
;	jz	ibdnot				; not even using buffers
	mov	ax,[si].bufst
	cmp	ax,[si].bufen
ibdnot:
	clc					; nope!
	jz	ibdout

	stc					; yup!
ibdout:
	pop	ax
	ret
	endp


;	ADDBUFFER -- 	adds AH (char), AL (lnstat) to buffer.
;
;	Inputs SI -> RSx
;	Outputs:  CY=1 if just became full

addbuffer proc near
	push	bx
	push	cx

	mov	bx,[si].bufen			; get end-of-buf
	mov	[si+bx].buffr,ah		; stuff values
	mov	[si+bx+1].buffr,al
	mov	cx,bx
	add	bx,2				; bump ptr
	and	bx,SERBUFMAX-1			; keep circular
	mov	[si].bufen,bx			; restore
	cmp	bx,[si].bufst			; full?
	jne	about

abfull:
	mov	[si].bufen,cx			; bump back
about:
	clc
	pop	cx
	pop	bx
	ret
	endp


;	READBUFFER --	reads char and lnstat from buffer
;			and sets CHAR and LNS with it.
;
;	If the reader calls this when ISBUFFERED is false, well,
;	we'll return the last char read.
;
;	The routine which calls this assumes that the CHAR is always
;	read BEFORE the LNSTAT (i.e., error codes).


readbuffer proc	near
	push	ax
	push	bx

	mov	bx,[si].bufst
	mov	al,[si+bx+1].buffr		; get lnstat
;	and	al,THREMP+TSREMP		; clear out bits
	mov	ah,[si].lns			; get current lnstat
	and	ah,60h
	or	al,ah				; get status w/ current
						; transmit holding bits
	mov	[si].lns,al

	mov	ah,[si+bx].buffr		; get char
	mov	[si].char,ah

	cmp	bx,[si].bufen
	je	rbempty

	add	bx,2				; bump pointer
	and	bx,SERBUFMAX-1

rbempty:
	mov	[si].bufst,bx

	pop	bx
	pop	ax
	ret
	endp





;=========================================================================
;	Set RS232 WRITE BITS.
;
;	All inputs in low bits of AX.
;=========================================================================


;=========================================================================
;	Handle RS232 registers (control, interval, receive rate, etc.)
;	with 8 bit (and 12-bit) inputs.
;
;	The control register has all its input in 8 bits.  When it is
;	written, clear the 8 bit in LFLAGS.
;
;	The interval register uses exactly 8 bits.  It is not implemented.
;
;	The receive and transmit rates use 12 bits.  Their definition is
;	not complete until RSWRATES is called.
;
;	The transmit buffer uses all 8 bits.
;=========================================================================

rswregs	proc	near
	push	si
	push	ax

	call	rselect
	jc	rswrout

	and	[si].flagset,not 1	; clear flag that LDCTRL was just set
	mov	ah,[si].lflags
	test	ah,0fh
	jnz	rswrnottb		; transmit buffer?

	call	rssettb
	jmp	rswrout

rswrnottb:
	test	ah,0eh
	jnz	rswrnottrrat

	call	rssettrrat
	jmp	rswrout

rswrnottrrat:
	test	ah,0ch
	jnz	rswrnotrerat

	call	rssetrerat
	jmp	rswrout

rswrnotrerat:
	test	ah,08h
	jnz	rswrnotinvl

	call	rssetinvl
	jmp	rswrout

rswrnotinvl:
	call	rssetctrl

rswrout:
	pop	ax
	shr	ax,8
	pop	si
	ret
	endp


;--------------------------------------------------------------------------
;	Set the RS232 CONTROL REGISTER.  
;
;	These eight bits in AH define, from bit values 1 to 128:
;
;	RCL0, RCL1, none, CLK4M, Podd, Penb, SBS2, SBS1
;
;
;	The bits will be set up in RSSTRUC to facilitate easy 
;	IBM serial port initialization.
;
;--------------------------------------------------------------------------

	.data

rsparityconv 	db	0,0,3,1
rsstopconv	db	1,1,0,0

	.code

rssetctrl proc	near
	push	ax
	push	bx
	push	dx

	mov	ah,al	
	and	ah,3			; get RCL1 and RCL0
	add	ah,5
	mov	[si].bits,ah		; save databits

	mov	bl,al
	and	bl,30h			; get PENB and PODD
	shr	bl,4
	xor	bh,bh
	mov	ah,[rsparityconv+bx]	; get converted parity val
	mov	[si].parity,ah		; set parity

	mov	bl,al
	and	bl,0c0h			; get SBS1 and SBS2
	shr	bl,6			
	xor	bh,bh
	mov	ah,[rsstopconv+bx]	; get converted stop bits
	mov	[si].stop,ah		; save stop bits (0 or 1 == 1 or 2)

	mov	ah,al
	shr	ah,3
	shr	ah,1			; shift one -- CY=bit
	mov	ah,3
	adc	ah,0
	mov	[si].clk4m,ah		; save CLK4M value (either 3 or 4)

	and	[si].lflags,not 8 	; clear LDCTRL bit

	call	rsfigurereceiverate	; CLK4M can change it
	call	rsfiguresendrate	; ditto

	call	pcsetlnctrl		; set PC serial port

	pop	dx
	pop	bx
	pop	ax
	ret
	endp


;	Set PC UART line control register.
;
;	Inputs:	SI -> RSx structure
;
pcsetlnctrl	proc	near
	push	ax
	push	dx
	
	mov	dx,[si].port		; set LNCTRL in the PC serial port
	add	dx,LNCTRL

	mov	al,[si].bits		; assemble byte in AL
	sub	al,5

	mov	ah,[si].stop
	shl	ah,2
	or	al,ah

	mov	ah,[si].parity
	shl	ah,3
	or	al,ah

	out	dx,al			; set PC 

	pop	dx
	pop	ax
	ret
	endp





;---------------------------------------------------------------------------
;	Set RS232 INTERVAL register.
;	Not implemented.
;---------------------------------------------------------------------------

rssetinvl proc	near
	mov	[si].timer,al

	and	[si].lflags,not 4		; clear LDIR flag
	ret
	endp


;---------------------------------------------------------------------------
;	Set RS232 RECEIVE RATE register.
;	
;	Only stores high 8 bits.
;---------------------------------------------------------------------------

rssetrerat proc	near
	mov	byte ptr [si].rbaud,al

	ret
	endp


;---------------------------------------------------------------------------
;	Set RS232 TRANSMIT RATE register.
;	
;	Only stores high 8 bits.
;---------------------------------------------------------------------------

rssettrrat proc near
	
	mov	byte ptr [si].sbaud,al
	
	ret
	endp


;---------------------------------------------------------------------------
;	SEND RS232 CHARACTER.
;---------------------------------------------------------------------------

rssettb proc	near
	push	dx

	mov	[si].char,al
	mov	dx,[si].port
	out	dx,al

;	add	dx,LNSTAT
;	in	al,dx
;	and	al,not THREMP
;	mov	[si].lns,al

	pop	dx
	ret
	endp


;===========================================================================
;	Set RS232 RATE bits.  This defines the highest two bits
;	and the divide-by-8 flag of the receive and transmit rate
;	buffers.
;===========================================================================

rswrates proc	near
	push	ax
	push	si

	call	rselect
	jc	rswtout

	mov	ah,[si].lflags
	test	ah,0fh
	jz	rswtout			; transmit buffer?

rswtnottb:
	test	ah,0eh
	jnz	rswtnottrrat

	call	rssetserat8		; set high bits of transmit rate
	and	[si].lflags,not 1	; clear LXDR
	jmp	rswtout

rswtnottrrat:
	test	ah,0ch
	jnz	rswtnotrerat

	call	rssetrerat8		; set high bits of receive rate
	and	[si].lflags,not 2	; clear LRDR
	jmp	rswtout

rswtnotrerat:

rswtout:
	pop	si
	pop	ax
	ret
	endp


;---------------------------------------------------------------------------
;	Set low 4 bits of RECEIVE RATE REGISTER.
;
;	Combine with high bits, and calculate correct PC baud rate
;	and store it.
;---------------------------------------------------------------------------

rssetrerat8 proc near
	push	ax

	mov	ah,1				; default to no divide-by-8
	test	al,4				; divide-by-8 bit
	jz	rsr8is1
	mov	ah,8				
rsr8is1:
	mov	[si].rdiv8,ah			; set divide-by-8 to 1 or 8

	and	al,3				; isolate RDR8 and RDR9
	mov	byte ptr [si+1].rbaud,al	; set high bits of rbaud

	call	rsfigurereceiverate		; calculate RPCRAUD

	pop	ax
	ret
	endp


;---------------------------------------------------------------------------
;	Set low 4 bits of SEND RATE REGISTER.
;
;	Combine with high bits, and calculate correct PC baud rate
;	and store it.
;---------------------------------------------------------------------------

rssetserat8 proc near
	push	ax

	mov	ah,1				; default to no divide-by-8
	test	al,4				; divide-by-8 bit
	jz	rss8is1
	mov	ah,8				
rss8is1:
	mov	[si].sdiv8,ah			; set divide-by-8 to 1 or 8

	and	al,3				; isolate XDR8 and XDR9
	mov	byte ptr [si+1].sbaud,al	; set high bits of rbaud

	call	rsfiguresendrate		; calculate SPCRAUD

	pop	ax
	ret
	endp




;--------------------------------------------------------------------------
;	Figure the BAUD RATE divisor used for the PC.
;
;	Since the 99/4A RS232 has TWO rates (send/receive) and the PC one,
;	we'll just use the maximum baud rate.
;
;
;	Baud = 3 MHz / ((CLK4M ? 4 : 3) * 2 * xBAUD x xDIV8)
;--------------------------------------------------------------------------

rsfiguresendrate:
rsfigurereceiverate:
	pusha


rsfigurerate:
	xor	ax,ax				; figure RECEIVE RATE
	mov	al,[si].clk4m			; CLK4M *
	add	ax,ax				; 2 *

	xor	cx,cx
	mov	cl,[si].rdiv8
	mov	bx,[si].rbaud

	mul	cx				; RDIV8 *
	mul	bx				; RBAUD
	
	mov	bp,ax
	mov	di,dx				; save RECEIVE DIVISOR

	xor	ax,ax				; figure SEND RATE
	mov	al,[si].clk4m			; CLK4M *
	add	ax,ax				; 2 *

	xor	cx,cx
	mov	cl,[si].sdiv8
	mov	bx,[si].sbaud

	mul	cx				; XDIV8 *
	mul	bx				; XBAUD
	
	xor	cx,cx				; shift count for below

	cmp	dx,di
	ja	rsfrsend			; take send rate
	jb	rsfrreceive
	cmp	ax,bp
	jae	rsfrsend		
rsfrreceive:
	mov	dx,di
	mov	ax,bp				; take send rate

rsfrsend:					; DX:AX is max rate
	or	dx,dx	 			; need a WORD
	jz	rsfrdodiv
	shr	ax,1				; divide DX:AX by 2
	shr	dx,1
	jns	rsfrnocarry
	add	ax,8000h
rsfrnocarry:
	inc	cl				; shift count
	jmp	rsfrsend

rsfrdodiv:
	mov	bx,ax
	mov	ax,0c6c0h			; DX:AX = 3000000 (Hz)
	mov	dx,2dh

rsfrshift:
	or	cl,cl				; shift down to match
	jz	rsfrshifted			; shifted maximum rate

	shr	ax,1				; divide DX:AX by 2
	shr	dx,1
	jns	rsfrshiftloop
	add	ax,8000h
rsfrshiftloop:
	dec	cl
	jmp	rsfrshift

rsfrshifted:
	cmp	bx,dx
	jbe	rsfrnodiv
	div	bx				; AX= baud rate!
	jmp	rsfrdiddiv
rsfrnodiv:		  
	xor	ax,ax				; invalid baud rate
	jmp	rsfrskipbaud
rsfrdiddiv:
	mov	bx,ax
	mov	dx,1
	mov	ax,0c200h			; DX:AX = 115200
	div	bx				; can't overflow
	shr	bx,1				; get half of divisor
	cmp	bx,dx				; round up?
	ja	rsfrskipbaud			; no
	inc	ax				; (at 9600 baud, this makes
						; huge difference!)
rsfrskipbaud:
	mov	[si].rpcbaud,ax
	mov	[si].spcbaud,ax			; save rates!

	call	pcsetbaudrate

	popa
	ret


;	Set PC UART baud rate.
;
;	Inputs:	SI -> RSx structure.

pcsetbaudrate proc near
	push	ax
	push	dx

	mov	dx,[si].port
	add	dx,LNCTRL

	in	al,dx				; get LNCTRL status
	nop
	or	al,80h				; set DLAB
	nop
	out	dx,al				; set LNCTRL

	add	dx,-LNCTRL+DIVL			; point to DIVL
	mov	al,byte ptr [si].spcbaud	; get low byte
	out	dx,al
	inc	dx				; point to high byte reg
	mov	al,byte ptr [si+1].spcbaud	; get high byte
	out	dx,al

	add	dx,-1+LNCTRL			; point to LNCTRL
	in	al,dx				; get LNCTRL status
	nop
	and	al,not 80h			; reset DLAB
	nop
	out	dx,al				; set LNCTRL status

	pop	dx
	pop	ax
	ret
	endp


rsw0b:
rsw0c:
rsw0d:
rsw0e:
	pusha
	call	rselect			; get proper RSx structure
	jc	rsw0lnone

	mov	[si].flagset,1		; we just set a load ctrl flag

	shr	dx,1			; get bit #
	and	dx,0fh			; isolate low nybble
	sub	dx,11			; get offset
	mov	cl,dl
	mov	ah,1
	shl	ah,cl			; get bit offset; 1=LXDR, 2=LRDR, ...
	not	ah
	and	[si].lflags,ah		; clear bit
	not	ah
	shr	al,1			; high bit is input
	jnc	rsw0lnone
	or	[si].lflags,ah		; set bit
rsw0lnone:
	popa
	ret


;==========================================================================
;	Define extra RS232 WRITE bits.
;	
;==========================================================================


;	Set test mode.
;
;	Not implemented.

rsw0f:	
	ret


;	BITSET macro.  
;
;	This macro will change a bit in the UART depending on 
;	the low bit of AL.
;
;	This will implement a whole routine.

BITSET	macro 	offs,val
	local	bs0,bss,bssnot

	push	ax
	push	dx
	push	si
	call	rselect
	jc	bssnot

	mov	dx,[si].port
	add	dx,offs

	shr	al,1			; CY = RTS

	in	al,dx
	
	jnc	bs0

	or	al,val
	jmp	bss

bs0:
	and	al,not val
bss:
	out	dx,al	
bssnot:
	pop	si
	pop	dx
	pop	ax
	endm



;	IBITSET macro.  
;
;	This macro will change a bit in the UART depending on 
;	the low bit of AL.
;
;	This will implement a whole routine.

IBITSET	macro 	offs,val
	local	bs0,bss,bssnot

	push	ax
	push	dx
	push	si
	call	rselect
	jc	bssnot

	mov	dx,[si].port
	add	dx,offs

	shr	al,1			; CY = RTS

	in	al,dx
	
	jc	bs0

	or	al,val
	jmp	bss

bs0:
	and	al,not val
bss:
	out	dx,al	
bssnot:
	pop	si
	pop	dx
	pop	ax
	endm




;	Set RTS  (16).
;

RTS	equ	2

rsrts:
	IBITSET	MDMCTRL,RTS
	ret


;	Set BRK  (17).
;

BRK	equ	64

rsbrk:
	BITSET	LNCTRL,BRK
	ret


;	Set RECEIVER INTERRUPT ENABLE  (18).
;
;	When interrupts are already set, this clears that condition.
;

RIEINT	equ	1
LNSTATINT equ	4

rsrie:
	push	ax
	push	si
	call   	rselect
	jc	rsrieout

	and	currentints,not M_INT1
	shr	al,1				; test bit
	mov	ah,RIEINT
	call	set99int

	mov	[si].intflag,0
	test	[si].intgoing,RIEINT
	jz	rsrieout

	call	isbuffered
	jnc	rsrieout

	or	[si].intflag,RIEINT

;	and	[si].lns,not RCDAVL

rsrieout:
	pop	si
	pop	ax
	ret


;	Set TRANSMIT BUFFER INTERRUPT ENABLE  (19).
;

THREINT	equ	2

rstbie:
	push	ax
	push	si
	call	rselect
	jc	rstbieout

	shr	al,1
	mov	ah,THREINT
	call	set99int

rstbieout:
	pop	si
	pop	ax
	ret


;	Set TIMER INTERRUPT ENABLE (20).
;
;	Not implemented.

rstie:
	ret


;	Set DATA SET CHANGE INTERRUPT ENABLE (21).
;
;	This is an approximation interrupt -- the PC actually
;	handles more cases.

MDMSTINT equ	8

rsdscie:
	push	ax
	push	si
	call	rselect
	jc	rsdscieout

	shr	al,1
	mov	ah,MDMSTINT
	call	set99int

	and	[si].intflag,not MDMSTINT

rsdscieout:
	pop	si
	pop	ax
	ret


;	RESET RS232  (31).
;
;	Will ignore the RS232 DSR "reset-at-will" bug.

rsreset:
	push	ax
	push	dx
	push	si

;	test	currentints,M_INT1
;	jnz	rsrtnot
;;	cmp	word ptr es:[83dch],0074h	; interrupt environment?
;;	je	rsrtnot

	call	rselect
	jc	rsrtnot

	mov	ah,15
	clc
	call	set99int		; clear interruptable bits

	mov	[si].lflags,0fh		; set all control flags high
	mov	[si].flagset,0

	xor	ax,ax
	mov	[si].bufst,ax
	mov	[si].bufen,ax		; empty buffer

	mov	[si].char,0		; make these actually READ THE REGS
	mov	[si].lns,60h		; *****
	mov	[si].mdm,0f0h

	mov	dx,[si].port
	add	dx,MDMCTRL
	in	al,dx
	or	al,RTS
	out	dx,al			; set RTS high

	call	pcsetlnctrl		; set status of line 
	call	pcsetbaudrate		; set baud rate

	call	setpcint

rsrtnot:
	pop	si
	pop	dx
	pop	ax
	ret


;===========================================================================
;	Set RS232 READ BITS.
;
;	All read routines return result in high part of AX.
;===========================================================================


;---------------------------------------------------------------------------
;	RS232 Transmitted data READ.
;
;	Read 5-8 bits.
;
;	CL = # bits
;---------------------------------------------------------------------------

rsreadbyte:
	push	cx
	push	dx
	push	si

	call	rselect
	jc	rsrbout
	call	isbuffered
	jnc	rsrbfix

	call	readbuffer			; get next char and lns
	and	[si].intflag,not RIEINT		; interrupt handled

	call	isbuffered			; any more?
	jnc	rsrbfix

	or	[si].intflag,RIEINT		; more to come

	or	currentints,M_INT1
	call	handle9901

rsrbfix:
	mov	dh,0ffh				; mask
	mov	ch,[si].char
	neg	cl
	add	cl,8
	shl	ch,cl				; zero out high bits
	shl	dh,cl

	and	ah,dh				; mask out
	or	ah,ch				; or in

rsrbout:
	pop	si
	pop	dx
	pop	cx
	ret


comment	\

	The TI's RS232 card handles error conditions differently from
	the PC's UART chips.  The PC will reset an error condition
	when the register is read, while the RS232 will only reset
	an error when another character is received correctly.

	To handle emulating this, we can do several things:

	1)  Keep a copy of all the UART error conditions, and only
	update it when a new byte is received.  We'll be able to
	do this by ENABLING INTERRUPTS and having the interrupt
	control all aspects of receiving/error checking.

	\



;	Read RS232 receive error  (9).
;
;	Reset when good char read.

OVRERR	equ	2
PARERR	equ	4
FRMERR	equ	8

rsre:	
	push	si
	call	rselect
	jc	rsre0
	call	isbuffered
	jnc	rsre1
      	call	readbuffer
rsre1:
	test	[si].lns,OVRERR+PARERR+FRMERR
	jz	rsre0
	or	ax,8000h
rsre0:
	pop	si
	ret


;	Read RS232 receive parity error  (10).
;
;	Reset when char w/good parity received.

rsrpe:
	push	si
	call	rselect
	jc	rsrpe0
	call	isbuffered
	jnc	rsrpe1
       	call	readbuffer
rsrpe1:
	test	[si].lns,PARERR
	jz	rsrpe0
	or	ax,8000h
rsrpe0:
	pop	si
	ret


;	Read RS232 overrun error (11).
;
;	Reset...?  I suppose when a char is read from the RS232.

rsore:
	push	si
	call	rselect
	jc	rsore0
	call	isbuffered
	jnc	rsore1
       	call	readbuffer
rsore1:
	test	[si].lns,OVRERR
	jz	rsore0
	or	ax,8000h
rsore0:
	pop	si
	ret


;	Read RS232 framing error (12).
;
;	Reset when char w/no framing error received.

rsfe:
	push	si
	call	rselect
	jc	rsfe0
	call	isbuffered
	jz	rsfe1
       	call	readbuffer
rsfe1:
	test	[si].lns,FRMERR
	jz	rsfe0
	or	ax,8000h
rsfe0:
	pop	si
	ret


;	Read RS232 full bit detect.
;	
;	Not implemented.

rsfbd:
	ret


;	Read RS232 start bit detect.
;
;	Not implemented.

rssbd:
	ret


;	Read RS232 receive input bit (15).
;
;	Not implemented.
;	
;	(This means something's coming in as we speak.)

rsrinp:
	ret


;	Read RS232 receive interrupt (16).
;	
;
;	Set ONLY when interrupts are enabled.

rsrint:
	push	si
	call	rselect
	jc	rsrint0

	test	[si].intflag,RIEINT
	jz	rsrint0

	or	ax,8000h

rsrint0:
	pop	si
	ret


;	Read RS232 transmitter interrupt (17).
;
;	Set when transmitter buffer is empty, AND interrupts are enabled.

rstrai:
	push	si
	call	rselect
	jc	rstrai0

	test	[si].intflag,THREINT
	jz	rstrai0

	or	ax,8000h
rstrai0:
	pop	si
	ret


;	Read RS232 timer interrupt  (19).
;
;	Not implemented.

rstimi:
	ret


;	Read RS232 DSR/CTS state change  (20).
;
;	Set ONLY when interrupts enabled.

rsdssci:
	push	si
	call	rselect
	jc	rsdssci0

	test	[si].intflag,MDMSTINT
	jz	rsdssci0

	or	ax,8000h
rsdssci0:

	pop	si
	ret


;	Read RS232 receive buffer register loaded (21).
;
;	Set upon character receipt; cleared by output to bit 18.

RCDAVL	equ	1

rsrbrl:
	push	dx
	push	si

	call	rselect
	jc	rstbrl0

	call	isbuffered
	jnc	rstbrl0

	or	ax,8000h		; yup
rstbrl0:
	pop	si
	pop	dx
	ret


;	Read RS232 transmit buffer register empty (22).
;
;	Set means no char waiting to be sent.
;	Reset upon write to transmit buffer.

THREMP	equ	32

rstbre:
	push	dx
	push	si

	call	rselect	
	jc	rstbre0

	mov	dx,[si].port
	add	dx,LNSTAT
	push	ax
	in	al,dx
	test	al,THREMP
	pop	ax

	jz	rstbre0	

	or	ax,8000h		

rstbre0:
	pop	si
	pop	dx
	ret


;	Read RS232 transmitter shift register empty (23).
;
;	Set when data is not being transmitted.
;	Reset when data is being sent.

;	On the UART, bit val 64 means THREMP _or_ TSREMP is set.
;	Figure out which with some fun math.

TSREMP	equ	64

rstsre:
	push	dx
	push	si

	call	rselect			; get port offset
	jc	rstsre0

	mov	dx,[si].port
	add	dx,LNSTAT
	push	ax
	in	al,dx
	mov	dl,al
	pop	ax


	and	dl,THREMP+TSREMP
	jz	rstsre0			; both unset means TSREMP unset
	and	dl,THREMP
	jz	rstsre0			; THREMP only set

	or	ax,8000h		; TSREMP only set

rstsre0:
	pop	si
	pop	dx

	ret


;	Read RS232 timer error.
;
;	Not implemented.

rsterr:
	ret


;	Read RS232 timer elapsed.
;
;	Not implemented.

rstela:
	ret


;	Read RS232 Request To Send pin (26).
;
;	RTS from remote device.  Inverted.

RRTS	equ	16			; really CTS

rsrrts:
	push	si
	call	rselect
	jc	rsrrts0

	test	[si].mdm,RRTS
	jz	rsrrts0
	or	ax,8000h
rsrrts0:
	pop	si
	ret


;	Read RS232 Data Set Ready pin  (27).
;
;	DSR from remote device.  Inverted.

RDSR	equ	32

rsrdsr:
	push	si
	call	rselect
	jc	rsrdsr0

	test	[si].mdm,RDSR
	jz	rsrdsr0
	or	ax,8000h
rsrdsr0:
	pop	si
	ret


;	Read RS232 Clear To Send pin  (28).
;
;	CTS from remote device.  Inverted.

RCTS	equ	16

rsrcts:
	push	si
	call	rselect
	jc	rsrcts0
	
	test	[si].mdm,RCTS
	jz	rsrcts0
	or	ax,8000h
rsrcts0:
	pop	si
	ret


;	Read RS232 data set status change (29).
;
;	Set when DSR or CTS changes.  
;	Cleared when bit 21 (DSCENB) is written.

RDDSR	equ	2

rsdssc:
	push	si
	call	rselect
	jc	rsdssc0

	test	[si].mdm,RDDSR
	jz	rsdssc0
	or	ax,8000h
rsdssc0:
	pop	si
	ret


;	Read RS232 flag (30).
;
;	Flag is set when any LOAD REGISTER commands are issued,
;	or the BREAK BIT is set.

BRKBIT	equ	16

rsflag:
	push	si
	call	rselect
	jc	rsflag0

	test	[si].lns,BRKBIT
	jnz	rsflag1

	cmp	[si].flagset,0
	jz	rsflag0

rsflag1:
	or	ax,8000h

rsflag0:
	pop	si
	ret


;	Read RS232 interrupt  (31).
;
;	Set when DSCINT, TIMINT, XBINT, RBINT happens.

rsint:
	push	si
	call	rselect
	jc	rsint0

	test	[si].intflag,0fh
	jz	rsint0

	or	ax,8000h
rsint0:

	pop	si
	ret


;==========================================================================
;	RS232 interrupts.
;==========================================================================

	.data

oldrs1int 	dd	?
oldrs2int 	dd	?


	.code



;	Set up the RS232 interrupts and the UART interrupt.
;
;	Only set the interrupts for which ports are defined.
;
setrs232ints proc near
	push	ax
	push	dx
	push	si
	push	es

	cmp	rs1.port,0
	jz	sriskip1

	mov	ah,35h				; get old interrupt vector
	mov	al,rs1.irq
	add	al,8
	int	21h

	mov	word ptr oldrs1int,bx		; save
	mov	word ptr oldrs1int+2,es

	push	ds
	mov	ah,25h
	mov	al,rs1.irq
	add	al,8
	push	cs
	pop	ds
	lea	dx,rs1int
	int	21h
	pop	ds				; set new interrupt

	lea	si,rs1
	xor	ax,ax
	mov	[si].bufst,ax
	mov	[si].bufen,ax
	mov	[si].intgoing,al
	mov	[si].intflag,0
	call	setpcint			; turn on UART
	mov	ah,RIEINT+LNSTATINT+THREINT+MDMSTINT
	call	setuartints			; init


sriskip1:
	cmp	rs2.port,0
	jz	sriskip2

	mov	ah,35h				; get old int
	mov	al,rs2.irq
	add	al,8
	int	21h

	mov	word ptr oldrs2int,bx		; save
	mov	word ptr oldrs2int+2,es

	push	ds				; set new int
	mov	ah,25h
	mov	al,rs2.irq
	add	al,8
	push	cs
	pop	ds
	lea	dx,rs2int
	int	21h
	pop	ds

	lea	si,rs2
	xor	ax,ax
	mov	[si].bufst,ax
	mov	[si].bufen,ax
	mov	[si].intgoing,al
	mov	[si].intflag,0
	call	setpcint			; turn on UART
	mov	ah,RIEINT+LNSTATINT+THREINT+MDMSTINT
	call	setuartints			; init

sriskip2:
	pop	es
	pop	si
	pop	dx
	pop	ax
	ret
	endp


;	Reset RS232 interrupts.
;
;	Reset UART interrupt.
;

resetrs232ints proc near
	push	ax
	push	dx
	push	si

	cmp	rs1.port,0
	jz	rriskip1

	lea	si,rs1
	mov	ah,0fh
	call	resetuartints
	call	resetpcint			; reset UART

	push	ds
	mov	ah,25h
	mov	al,rs1.irq
	add	al,8
	mov	dx,word ptr oldrs1int
	mov	ds,word ptr oldrs1int+2
	int	21h				; reset int
	pop	ds

rriskip1:
	cmp	rs2.port,0
	jz	rriskip2

	lea	si,rs2
	mov	ah,0fh
	call	resetuartints
	call	resetpcint			; reset UART

	push	ds
	mov	ah,25h
	mov	al,rs2.irq
	add	al,8
	mov	dx,word ptr oldrs2int
	mov	ds,word ptr oldrs2int+2
	int	21h				; restore int
	pop	ds

rriskip2:
	pop	si
	pop	dx
	pop	ax
	ret
	endp


;===========================================================================
;	Initialize hardware interrupt.
;
;	SI -> RSx
;===========================================================================

SETIRQ	equ	8

setpcint proc 	near
	push	ax
	push	cx
	push	dx

	mov	dx,[si].port
	or	dx,dx
	jz	spinot

	in	al,dx
	add	dx,INTID
	in	al,dx
	add	dx,-INTID+MDMSTAT
	in	al,dx			; read regs to clear them?

	mov	dx,[si].port
	add	dx,MDMCTRL		; point to modem ctrl reg
	in	al,dx			
	or	al,SETIRQ+3		; set UART interrupt
	out	dx,al

	add	dx,-MDMCTRL+LNSTAT
	in	al,dx
	mov	[si].lns,al		; setup line status

	add	dx,-LNSTAT+MDMSTAT
	in	al,dx
	mov	[si].mdm,al

	in	al,21h			; PIC IRQ mask
	mov	ah,1
	mov	cl,[si].irq
	shl	ah,cl			; get bit for IRQ
	not	ah
	and	al,ah			; mask out
	out	21h,al			; write back

spinot:
	pop	dx
	pop	cx
	pop	ax
	ret
	endp


;---------------------------------------------------------------------------
;	Set bits in the Interrupt Enable register.
;
;	SI -> RSx
;	AH -> mask
;---------------------------------------------------------------------------

setuartints proc near
	push	ax
	push	dx

	mov	dx,[si].port		; set bits in IER.
	or	dx,dx
	jz	suinot

	add	dx,INTENA
	in	al,dx
	and	al,not 0fh
	or	al,ah
	out	dx,al
	jmp	$+2
	out	dx,al			; write twice in case buggy 8250

	and	[si].intflag,ah		; clear interrupt flag
suinot:
	pop	dx
	pop	ax
	ret
	endp



;===========================================================================
;	Reset UART interrupt.
;
;	SI -> RSx
;===========================================================================

resetpcint proc near
	push	ax
	push	cx
	push	dx

	mov	dx,[si].port
	or	dx,dx
	jz	rpinot

	add	dx,MDMCTRL
	in	al,dx
	and	al,not SETIRQ
	out	dx,al			; turn off UART int

	add	dx,-MDMCTRL+INTENA
	xor	al,al
	out	dx,al			; turn off int types

	in	al,21h			; get PIC IRQ mask
	mov	ah,1
	mov	cl,[si].irq
	shl	ah,cl
	or	al,ah 			; mask out interrupt
	out	21h,al

	mov	[si].intgoing,0		; no more inting

rpinot:	pop	dx
	pop	cx
	pop	ax
	ret
	endp


;---------------------------------------------------------------------------
;	Reset bits in the Interrupt Enable register.
;
;	SI -> RSx
;	AH -> mask
;---------------------------------------------------------------------------

resetuartints proc near
	push	ax
	push	dx

	mov	dx,[si].port		; reset bits in IER.
	or	dx,dx
	jz	ruinot

	add	dx,INTENA
	in	al,dx
	not	ah
	and	al,ah
	out	dx,al

	and	[si].intflag,ah		; clear set int flag
ruinot:
	pop	dx
	pop	ax
	ret
	endp


;---------------------------------------------------------------------------
;	Set interrupt from 99/4A's point of view.
;
;	SI -> RSx
;	AH -> mask
;	C=0 to reset, =1 to set
;---------------------------------------------------------------------------

set99int proc	near
	push	ax
	push	dx

	jnc	s9iclear
	or	[si].intgoing,ah		; add bits
	jmp	s9iout
s9iclear:
	not	ah
	and	[si].intgoing,ah		; clear bits
s9iout:
	mov	dx,M_INT1
	call	reset9901int

	pop	dx
	pop	ax
	ret
	endp


;===========================================================================
;	RS232 interrupt routines.
;===========================================================================

	.data

rsintbits db	MDMSTINT,THREINT,RIEINT,LNSTATINT

	.code

rs1int:
	push	si
	lea	si,rs1
	jmp	rsintrout
rs2int:
	push	si
	lea	si,rs2

rsintrout:
	push	ax
	push	bx
	push	dx
	push	ds

	mov	ax,@data
	mov	ds,ax

	mov	dx,[si].port
	add	dx,INTID
	in	al,dx			; get interrupt identification

	test	al,1			; interrupt pending?
	jnz	rsintskip      		; hmmm...

	shr	al,1	      		; isolate int id bits 		
	and	ax,3			
	mov	bx,ax
	mov	al,[bx+rsintbits]	; get corresp. bit
	or	[si].intflag,al		; set it
	mov	bl,al			; save 

	add	dx,-INTID+MDMSTAT
	in	al,dx
	mov	[si].mdm,al		; save modem status

	add	dx,-MDMSTAT

rsintmore:
	in	al,dx			; get char
	mov	ah,al
	add	dx,LNSTAT
	in	al,dx			; get line status

	mov	[si].lns,al

	cmp	bl,RIEINT		; did we just get a character?
	jne	rsintnodata		; (NOT necessarily for the 9900!)

	call	addbuffer		; add char/lnstat to buffer
	jc	rsintnodata		; full buffer -> skip rest

	in	al,dx	 		; check LNSTAT again
	test	al,1			; still data?
	jz	rsintnodata		; nope
	add	dx,-LNSTAT		; point to received data
	jmp	rsintmore		; again

rsintnodata:
	test	[si].intgoing,bl	; any interruptible flags?
	jz	rsintskip

	or	currentints,M_INT1	; trigger interrupt
	call	handle9901

rsintskip:
	mov	al,20h
	out	20h,al			; done
rsintnoack:
	pop	ds
	pop	dx
	pop	bx
	pop	ax

	pop	si
	iret


;==========================================================================
;	PIO routines.
;
;==========================================================================


;	PSELECT -- Select the correct PIOx structure based on the CRU
;	address in DX.
;
;	Returns SI -> PIOxm or CY=1 if undefined
;
pselect	proc	near
	lea	si,pio1
        cmp     lastpio,1500h           ; just writing to >5000 cannot
        jb      psgotit                 ; distinguish WHICH PIO we are using
	lea	si,pio2
psgotit:
	cmp	[si].pport,0
	jnz	psselected
	stc
	jmp	psselectout
psselected:
	clc
psselectout:
	ret
	endp


;       Handle a read from the PIO.  Called from MEMORY.INC
;       via "handledsrread".
;
;       99/4A PIO is bi-directional???
;
;       We can't emulate reads on MOST parallel ports.
;       Leave as the PC has it -- return the last byte written.

handlers232read proc near
	push	dx
	push	si

	mov	al,es:[di]
	call	pselect
	jc	hrrout

	mov	dx,[si].pport
	in	al,dx
	
hrrout:
	pop	si
	pop	dx
	ret
	endp


;       Only for PIO data write.  Called from MEMORY.INC via
;       "handledsrwrite".
;
;	AL=byte
;
handlers232write proc near
	push	ax
	push	dx
	push	si

	call	pselect
	jc	hrwout

	mov	dx,[si].pport
      	out	dx,al

hrwout:
	pop	si
	pop	dx
	pop	ax
	ret
	endp


;--------------------------------------------------------------------------
;	Set SEND line (CRU bit 1).
;
;	Set to 0 indicates write, set to 1 indicates read.
;--------------------------------------------------------------------------

piosend:
	push	ax
	push	dx
	push	si

	mov	lastpio,dx
	call	pselect
       	jc	psdout

	mov	ah,al
	and	ah,1				; get the bit
	shl	ah,5
	mov	dx,[si].pport
	add	dx,2
	in	al,dx
	jmp	$+2
	and	al,not 32			; clear EMBC
	or	al,ah
	out	dx,al

psdout:
	pop	si
	pop	dx
	pop	ax
	ret


;--------------------------------------------------------------------------
;	Set strobe (CRU bit 2).
;
;	Sending 1 to prepare device, send 0 to end transfer.
;--------------------------------------------------------------------------

pioreset:
	push	ax
	push	dx
	push	si

	mov	lastpio,dx
	call	pselect
       	jc	prsout

	mov	dx,[si].pport
	add	dx,2
	mov	ah,al
	and	ah,1				; isolate bit
	xor	ah,1
	mov	[si].strobe,ah
	in	al,dx
	jmp	$+2

	and	al,not 1
	or	al,ah
	out	dx,al

prsout:
	pop	si
	pop	dx
	pop	ax
	ret



;--------------------------------------------------------------------------
;	Get PIO busy status (CRU bit 2).
;--------------------------------------------------------------------------

piobusy:
	push	dx
	push	si

	mov	lastpio,dx
	call	pselect
       	jc	pbsout

	push	ax
	mov	ah,80h
	cmp	[si].strobe,0
	jz	pbsnotack

	mov	ah,40h
 	mov	dx,[si].pport
	inc	dx	     			; get status byte
	in	al,dx
	jmp	$+2
	in	al,dx
	test	al,ah				; set carry if active
	pop	ax

	jz	pbsout
	or	ax,8000h
	jmp	pbsout

pbsnotack:
 	mov	dx,[si].pport
	inc	dx	     			; get status byte
	in	al,dx
	jmp	$+2
	in	al,dx
	test	al,ah				; set carry if active
	pop	ax

	jnz	pbsout
	or	ax,8000h

pbsout:
	pop	si
	pop	dx
	ret

;       Uh, this seems to be hanging out in space

 	mov	dx,[si].pport
	inc	dx	     			; get status byte
	in	al,dx
	jmp	$+2
	in	al,dx
	test	al,ah				; set carry if active
	pop	ax
