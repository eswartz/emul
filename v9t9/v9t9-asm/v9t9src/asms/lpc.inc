; *******************************************
; LPC.INC  V9t9 LPC decoding routines
; *******************************************
; by Edward Swartz.  3/16/1995
; *******************************************

;	This module takes incoming LPC data and decodes its
;	equations and creates digitized speech output.
;
;	The SOUND.ASM procedure pointer "speech" is used to
;	emit one byte of data every 8000 times a second
;	(poor, I know, but at least it supports PC speaker).
;
;	There is a critical logic error in here.  The speech data
;	is spoken during an interrupt, but is generated in a
;	called procedure below.  Therefore, there are potential
;	(or very oft-occurring) data corruption conflicts,
;	making speech stutter, stop, and lock up the machine.



	.data

bufind	db	0		; offset into decoding table
bufbit	db	0

BUFSIZEMASK	equ	1
buffer	db	2 dup (?)	; big enuff for all vocab words...

bufred	db	0

	even

bufcurbit dw	0
oenergy	dw	0
energy	dw	0
penergy	dw	0
opitch	dw	0
pitch	dw	0
ppitch	dw	0
repeat	dw	0
o_k	dw	11 dup (0)
k	dw	11 dup (0)
p_k	dw	11 dup (0)
b	dw	11 dup (0)

ns1	dw	0
ns2	dw	0
ns3	dw	0
ns4	dw	0

pitchcntr dw	0
pitchind dw	0

lpcdecoding db	0

	even


	.code


;	Called from HANDLESPEECHWRITE.
;
;	Speak the word at speech address "addr".
;
hsspeak proc	near
	push	es
	push	si

	call	startspeech

	mov	es,speechseg
	mov	si,word ptr addr

hssploop:
	mov	al,es:[si]
	call	addspeech
	jc	hsspsaid     		; C=1 means we got end-of-speech
	inc	si
	jmp	hssploop
hsspsaid:
	
	pop	si
	pop	es
	ret
	endp


;	SWAPBITS --	Reverse a byte.
;
;	Bytes fed into the speech synthesizer are reversed.
;	Bytes read from the speech synthesizer are NOT.
;
;	In AL.
;
swapbits proc	near
	push	cx

	mov	ah,al
	xor	al,al
	mov	cx,8
sbsloop:
	add	al,al
	shr	ah,1
	jnc	sbsdoloop
	or	al,1
sbsdoloop:
	loop	sbsloop

	pop	cx
	ret
	endp



;
;	This routine initializes the mathematical crud and the
;	buffers used to decode a slew of LPC parameters.
;

lpcparaminit	proc	near
	push	si
	push	cx

	mov	bufind,0
	mov	bufbit,0
	mov	bufred,0

	mov	bufcurbit,0			; # of bit, >=50==end

	mov	oenergy,0			; clear 'old' values
	mov	opitch,0
	lea	si,o_k
	mov	cx,11
	call	hsclear

	mov	energy,0			; clear 'new' values
	mov	pitch,0
	lea	si,k
	mov	cx,11
	call	hsclear

	mov	pitchcntr,0
	mov	pitchind,0

	mov	lpcdecoding,1			; we're doin' it!

	pop	cx
	pop	si
	ret
	endp



;	Clear memory from [si] to [si+cx*2-1]
;
;
hsclear	proc	near
	push	ax
	xor	ax,ax
hscloop:
	mov	[si],ax
	add	si,2
	loop	hscloop
	pop	ax
	ret
	endp



;	HSPUSH	-- push a byte into the parameter reader.
;	If we spoke the word, return C=1, else C=0.
;
hspush	proc	near

	push	bx
	mov	bl,bufind
	xor	bh,bh
	mov	[buffer+bx],al
	inc	bx
	and	bx,BUFSIZEMASK
	mov	bufind,bl			; circular queue
	pop	bx

hsploop:
	call	hsgetparam			; returns C=1 if out of bits
	jc	hspnospeakout
	cmp	lpcdecoding,0			; this is reset on e-o-s
	jnz	hsploop

	stc
	jmp	hspout

hspnospeakout:
	clc
hspout:
	ret
	endp


;	HSGETPARAM -- try to construct a complete equation for SOMETHING
;	from bytes in BUFFER.
;
;	Return C=1 if not enough bits to get next parameter.
;
;
;
;	BUFIND 		-- 0-15 -- index to last new byte in BUFFER
;
;	BUFBIT 		-- 0-7  -- bit position into byte in BUFFER
;	BUFRED 		-- 0-15 -- index of last processed byte (pending a new arg)
;
;	BUFCURBIT       -- 0-49 -- # bit in equation
;	
;	OENERGY, OPITCH, O_K 	-- last values
;	ENERGY, PITCH, K	-- current values
;	PENERGY, PPITCH, P_K	-- current interPolated values
;

	.data

	even
paramtable equ	$
	dw	0 ,energy,0104h,   4 ,repeat,0001h,   5 ,pitch ,0106h
	dw	11,k     ,0305h,   16,k+2   ,0105h
	dw	21,k+4   ,0004h,   25,k+6   ,0104h,   29,k+8   ,0204h
	dw	33,k+10  ,0004h,   37,k+12  ,0104h
	dw	41,k+14  ,0103h,   44,k+16  ,0203h,   47,k+18  ,0303h 
	dw	0				; 50-->speak!


energytrans equ $
	dw	000,00ch,104h,10ch,208h,308h,500h,704h
	dw	0a00h,0e04h,1404h,1c08h,2804h,380ch,5004h


pitchtrans equ	$
	dw	0ch,10h,11h,12h,13h,14h,15h
	dw	16h,17h,18h,19h,1ah,1bh,1ch
	dw	1dh,1eh,1fh,20h,21h,22h,23h
	dw	24h,25h,26h,27h,28h,29h,2ah
	dw	2bh,2dh,2fh,31h,33h,35h,36h
	dw	39h,3bh,3dh,3fh,42h,45h,47h
	dw	49h,4dh,4fh,51h,55h,57h,5ch
	dw	5fh,63h,66h,6ah,6eh,73h,77h
	dw	7bh,80h,85h,8ah,8fh,95h,9ah,0a0h


	comment	\
	dw	0,60,170,270,341,396,441,479,512,541,566,590,611,631
	dw	649,666,682,697,711,724,737,749,761,772,782,792,802
	dw	811,820,829,837,845,853,860,868,875,882,889,895,902
	dw	908,914,920,926,931,937,942,947,953,958,963,968,972
	dw	977,982,986,991,995,999,1003,1008,1012,1016,1020


	dw	0,20,24,25,27,28,30,32,33
	dw	35,36,39,41,42,44,45,47
	dw	48,50,51,54,56,57,59,60
	dw	61,63,64,66,69,70,71,73
	dw	74,76,77,78,80,82,84,85
	dw	87,88,89,91,92,93,96,97
	dw	98,100,101,102,104,105,106,109
	dw	110,111,113,114,115,116,118
	\


	.code

hsgetparam proc	near
	push	ax
	push	bx
	push	cx
	push	si
	push	di


	mov	ax,bufcurbit			; find out what param we 
						; are decoding
	lea	si,paramtable
hgploop:
	cmp	[si],ax
	je	hgpmatch			; same bit number?
	ja	hgperr				; boing!
	add	si,6   				; skip
	jmp	hgploop

hgperr:
	stc
	jmp	hgpout				; don't play!
hgpmatch:
	mov	di,[si+2] 			; addr of result
	mov	cx,[si+4]			; CH=expand to GET10?
						; CL=# bits needed

	call	getbits	    			; try to get CX bits into DI
						; don't alter CX or DI!
	jnc	hgpcheck			
	jmp	hgperr				; nope, not enough bits

hgpcheck:
	mov	ax,bufcurbit
	add	al,cl
	adc	ah,0				; new bufcurbit... check
						; special cases

;--------------------------------------------------------------------------
	cmp	ax,4				; after energy
	ja	hgp00
	cmp	energy,15
	je	hgpend				; is it the end?
	cmp	energy,0			; is it silence?
	jne	hgpre
      	jmp	hgpdocalc

hgpre:  
	push	bx
	mov	bx,[di]
	add	bx,bx
	mov	bx,[word ptr energytrans+bx]
	mov	[di],bx
	pop	bx
	jmp	hgpnone
	
hgpend:
;;	cmp	uselpc,0
	test	features,FE_lpcspeech
	je	hgpend0

hgpend0:
	call	stopspeech
	jmp	hgpnot

;--------------------------------------------------------------------------
hgp00:
	cmp	ax,11				; after repeat & pitch
	jb	hgpnone
	ja	hgp01

	mov	bx,[di]

	add	bx,bx
	mov	bx,word ptr pitchtrans[bx]

	mov	[di],bx
	

						; fix pitch

	cmp	repeat,1
	jne	hgpnone

	jmp	hgpdocalc			; repeat

;---------------------------------------------------------------------------
hgp01:
	call	get10				; mightaswelldoithere
	cmp	ax,29				; after K0-K3
	jne	hgpnone
	cmp	pitch,0	 			; whisper?
	jne	hgpnone  			; nope... need more
	jmp	hgpdocalc			; go do it

;--------------------------------------------------------------------------
hgpnone:
	mov	bufcurbit,ax			; got it, read it, and...?
	cmp	ax,50
	jb	hgpnot

hgpdocalc:
	call	hscalc				; calculate this formoola!
	mov	bufcurbit,0			; reset

hgpnot:
       	clc					; didn't speak yet
hgpout:
	pop	di
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;	GETBITS --	Try to get CL bits into DI from the buffer.
;
;
getbits	proc	near
	push	cx
	push	di
	push	ax
	push	bx
	push	dx

	mov	bl,bufind
	shl	bl,3
	mov	al,bufred
	shl	al,3
	add	al,bufbit
	cmp	al,bl
	jbe	gbnot
	add	bl,8*2
gbnot:
	add	al,cl
	cmp	al,bl
	jbe	gbcan
	jmp	gbcant
gbcan:

	mov	bl,bufred
	xor	bh,bh
	mov	ah,[buffer+bx]
	inc	bx
	and	bx,BUFSIZEMASK
	mov	al,[buffer+bx]

	mov	ch,cl				; wanted

	mov	cl,bufbit			; 0-7
	shl	ax,cl				; move to top
	mov	cl,16				; AX>>=16-wanted
	sub	cl,ch
	shr	ax,cl

	mov	cl,bufbit			; figure new pos'ns
	add	cl,ch
	mov	ch,cl
	and	cl,7 				; bufbit
	and	ch,0f8h
	shr	ch,3				; add this to
	mov	bl,bufred			; bufred
	add	bl,ch				; and
	and	bl,BUFSIZEMASK			; mask

gbgot:
	mov	bufbit,cl			; store vars if changed
	mov	bufred,bl

;	xor	ah,ah
	mov	[di],ax				; save val
	clc
	jmp	gbout

gbcant:
	stc					; not enough bits
gbout:

	pop	dx
	pop	bx
	pop	ax
	pop	di
	pop	cx
	ret
	endp



;	GET10	-- badly named function which upgrades a value from a byte
;		   to a logarithmically-scaled 13-bit fixed-point value
;		   from -1 to 1.
;


	.data


parammatch dw	16,paramk1,21,paramk2,25,paramk3,29,paramk4
	dw	33,paramk5,37,paramk6,41,paramk7,44,paramk8
	dw	47,paramk9,50,paramk10

paramk1	dw	820ch,8308h,830ch,8404h
	dw	840ch,8504h,8600h,8708h
	dw	8808h,8908h,8a0ch,8c00h
	dw	8d04h,8f00h,900ch,920ch
	dw	9900h,0a104h,0ab08h,0b804h
	dw	0c704h,0d80ch,0eb0ch,0000h
	dw	1404h,2704h,380ch,470ch
	dw	5408h,5e0ch,6700h,6d04h

paramk2	dw	0ae00h,0b408h,0bb08h,0c304h
	dw	0cb08h,0d404h,0dd0ch,0e708h
	dw	0f108h,0fb0ch,0600h,1004h
	dw	1a04h,2400h,2d04h,3600h
	dw	3e04h,450ch,4c0ch,5300h
	dw	5808h,5d0ch,6204h,6604h
	dw	690ch,6c0ch,6f08h,710ch
	dw	730ch,7508h,7700h,7e08h

paramk3	dw	9200h,9f00h,0ad00h,0ba00h
	dw	0c800h,0d500h,0e300h,0f000h
	dw	0fe00h,0b00h,1900h,2600h
	dw	3400h,4100h,4f00h,5c00h

paramk4	dw	0ae00h,0bc00h,0ca00h,0d800h
	dw	0e600h,0f400h,0100h,0f00h
	dw	1d00h,2b00h,3900h,4700h
	dw	5500h,6300h,7100h,7e00h

paramk5	dw	0ae00h,0ba00h,0c500h,0d100h
	dw	0dd00h,0e800h,0f400h,0ff00h
	dw	0b00h,1700h,2200h,2e00h
	dw	3900h,4500h,5100h,5c00h

paramk6	dw	0c000h,0cb00h,0d600h,0e100h
	dw	0ec00h,0f700h,0300h,0e00h
	dw	1900h,2400h,2f00h,3a00h
	dw	4500h,5000h,5b00h,6600h

paramk7	dw	0b300h,0bf00h,0cb00h,0d700h
	dw	0e300h,0ef00h,0fb00h,0700h
	dw	1300h,1f00h,2b00h,3700h
	dw	4300h,4f00h,5a00h,6600h

paramk8	dw	0c000h,0d800h,0f000h,0700h,1f00h,3700h,4f00h,6600h

paramk9	dw	0c000h,0d400h,0e800h,0fc00h,1000h,2500h,3900h,4d00h

paramk10 dw	0cd00h,0df00h,0f100h,0400h,1600h,2800h,3b00h,4d00h



	comment	\

paramk1	dw	82h,83h,83h,84h
	dw	84h,85h,86h,87h
	dw	88h,89h,8ah,8ch
	dw	8dh,8fh,90h,92h
	dw	99h,0a1h,0abh,0b8h
	dw	0c7h,0d8h,0ebh,00h
	dw	14h,27h,38h,47h
	dw	54h,5eh,67h,6dh

paramk2	dw	0aeh,0b4h,0bbh,0c3h
	dw	0cbh,0d4h,0ddh,0e7h
	dw	0f1h,0fbh,06h,10h
	dw	1ah,24h,2dh,36h
	dw	3eh,45h,4ch,53h
	dw	58h,5dh,62h,66h
	dw	69h,6ch,6fh,71h
	dw	73h,75h,77h,7eh

paramk3	dw	92h,9fh,0adh,0bah
	dw	0c8h,0d5h,0e3h,0f0h
	dw	0feh,0bh,19h,26h
	dw	34h,41h,4fh,5ch

paramk4	dw	0aeh,0bch,0cah,0d8h
	dw	0e6h,0f4h,01h,0fh
	dw	1dh,2bh,39h,47h
	dw	55h,63h,71h,7eh

paramk5	dw	0aeh,0bah,0c5h,0d1h
	dw	0ddh,0e8h,0f4h,0ffh
	dw	0bh,17h,22h,2eh
	dw	39h,45h,51h,5ch

paramk6	dw	0c0h,0cbh,0d6h,0e1h
	dw	0ech,0f7h,03h,0eh
	dw	19h,24h,2fh,3ah
	dw	45h,50h,5bh,66h

paramk7	dw	0b3h,0bfh,0cbh,0d7h
	dw	0e3h,0efh,0fbh,07h
	dw	13h,1fh,2bh,37h
	dw	43h,4fh,5ah,66h

paramk8	dw	0c0h,0d8h,0f0h,07h,1fh,37h,4fh,66h

paramk9	dw	0c0h,0d4h,0e8h,0fch,10h,25h,39h,4dh

paramk10 dw	0cdh,0dfh,0f1h,04h,16h,28h,3bh,4dh
	\

	.code

;	AX= bit # past last-read parameter.
;
;
get10	proc	near
	push	bx
	push	cx
	push	ax
	push	dx

	lea	bx,parammatch
get10find:
	cmp	ax,[bx]
	jbe	get10go
	add	bx,4
	jmp	get10find

get10go:
	mov	ax,[bx+2]
	add	ax,[di]
	add	ax,[di]
	mov	bx,ax
	mov	ax,[bx]
	xchg	al,ah
	shr	ah,6
	mov	[di],ax


	pop	dx
	pop	ax
	pop	cx
	pop	bx
	ret
	endp



	.data

	even
periodic equ	$
;	more forceful than below

	dw	0,23,1223,4024,2105,112,0,-2,-582,-3493,-3053,-356,0,0
	dw	216,2616,3800,847,8,0,-57,-1669,-4095,-1614,-52,0,9,887
	dw	3834,2556,201,0,0,-379,-3109,-3446,-551,-2,0,122,2165,4005
	dw	1175,21,0,-26,-1272,-4041,-2045,-103


ZAP	macro	offs
	mov	ax,[p_k+offs]
	MAD	[b+offs]
	sub	si,ax

	mov	ax,[p_k+offs]
	MAD	si
	add	ax,[b+offs]
	mov	[b+offs+2],ax
	endm

MAD	macro	b			; AX=value, B=scaleup
	imul	b
	idiv	ONE
	endm


INTERP	macro	r,a,b
	mov	ax,[b]
	add	ax,[a]
	sar	ax,1
	mov	[a],ax
	mov	[r],ax
	endm

	comment	\
INTERP	macro	r,a,b
	mov	ax,[b]
	mov	[r],ax
	endm
	\
	


	.data

	even
eight	dw	8
ONE	dw	8192
ONE10	dw	1024
ONE14	dw	32768
HALF	dw	4096
sixteen dw	16



	.code

;	HSCALC	-- calculate digitized data for the current equation
;		   (200 bytes)
;


hscalc	proc	near
	pusha
	push	es

	test	features,FE_lpcspeech
	jne	hsyup

hscnone:
	jmp	hscskip

hsyup:

	mov	spchcntr,10

	mov	ax,energy			; not 'penergy' HERE!
	or	ax,ax
	jnz	hscalcsomething

	mov	cx,200

hsspeaksilence:
	mov	al,80h
	call	hsqueuebyte
	loop	hsspeaksilence

	jmp	hscfinish			; 'silence'

hscalcsomething:
	lea	si,b
	mov	cx,11
	call	hsclear

	mov	ax,opitch			; if ((opitch!=0) ^
	cmp	ax,0				;     (pitch!=0))
	mov	bx,0
	je	hsc00
	mov	bx,1
hsc00:
	mov	ax,pitch
	cmp	ax,0
	mov	ax,0
	je	hsc01
	mov	ax,1
hsc01:
	xor	ax,bx
	jz	hsc02

	xor	ax,ax
	mov	pitchcntr,ax
	mov	ax,08888h
	mov	ns1,ax
	mov	ns2,ax
	mov	ns3,ax
	xor	ax,ax
	mov	ns4,ax


hsc02:
	xor	bp,bp				; EIGHT
	
hscdoeightloop:
	push	di				; save offset into buffer

	INTERP	ppitch,opitch,pitch		; interpolate values
	INTERP	penergy,oenergy,energy
	mov	cx,11
	lea	si,p_k
	lea	di,o_k
	lea	bx,k
hscintloop:
	INTERP	si,di,bx
	add	si,2
	add	di,2
	add	bx,2
	loop	hscintloop


	pop	di

	xor	cx,cx				; PART

hscdopartloop:
	cmp	ppitch,0			; whisper?
	jnz	hscchirp			; nope

hscnoise:
	mov	ax,ns1				; do noise manipulations
	ror	ax,1
	xor	ax,ns2
	mov	ns1,ax

	mov	ax,ns1
	add	ns2,ax

	and	ax,1
 	
	mov	ax,HALF
	jz	hscpole
	neg	ax
hscpole:
	sar	ax,1

	jmp	hscdolpc

hscchirp:
	mov	bx,pitchcntr			; do voiced sound
	inc	bx
	cmp	bx,ppitch
	jb	hscinbound
	xor	bx,bx
hscinbound:
	cmp	bx,50
	mov	pitchcntr,bx
	mov	ax,0
	jae	hscsilent
	add	bx,bx
	
	mov	ax,word ptr periodic[bx]

hscsilent:



hscdolpc:
	MAD	penergy	

	mov	si,ax				; SI= yy   == y11 = 14 bits

	mov	bx,9*2

hscdocloop:
	mov	ax,[p_k+bx]			
	MAD	[b+bx]				
	sub	si,ax				

	mov	ax,[p_k+bx]			
	MAD	si	  			
	add	ax,[b+bx]		
	mov	[b+bx+2],ax			
	
	sub	bx,2
	jge	hscdocloop

	mov	word ptr b,si

	mov	ax,si

	call	speechDA
	call	hsqueuebyte

	inc	cx
	cmp	cx,200/8
	jae	hscfinisheightloop
	
	jmp	hscdopartloop

hscfinisheightloop:
	inc	bp
	cmp	bp,8
	jae	hscfinish

	jmp	hscdoeightloop

hscfinish:
	mov	ax,energy			; copy current values
	mov	oenergy,ax			; to "old" values
	mov	ax,pitch
	mov	opitch,ax
	mov	cx,11
	lea	bx,k
	lea	si,o_k
hscmoveloop:
	mov	ax,[bx]
	mov	[si],ax
	add	bx,2
	add	si,2
	loop	hscmoveloop
hscskip:

	pop	es
	popa
	ret
	endp


;	Convert 14 bits in AX to a 8-bit Sound Blaster byte.
;
;	Returns in AL.
;

speechDA proc	near
	push	bx

	sar	ax,7			; 16(?) -> 9
	mov	bx,ax
	xor	al,80h

	cmp	bx,127
	jge	sDAhi

	cmp	bx,-128
	jge	sDAout

	mov	al,80h
	jmp	sDAout
sDAhi:
	mov	al,7fh

sDAout:
	pop	bx
	ret
	endp
	
