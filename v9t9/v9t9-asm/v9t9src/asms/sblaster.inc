; ********************************************
; SBLASTER.INC include file for SoundBlaster
; ********************************************
; by Edward Swartz  1995
; ********************************************

;	SBLASTER.INC
;
;
;	Routines for controlling the SoundBlaster card.
;
;

	.data

;
;	SoundBlaster variables
;

sbirq		db	7
sbdma		db	1
noisetop 	db	30h			; top value for noise 
						; generation
	even
sbport		dw	220h			; defaults
ctvoicedrv	dw	0
ctvoiceseg	dw	0
sbstat		dw	0
buff		dw	0
buff1		dw	0
buff2		dw	0
sync		dw	220			; how much to clip samples

lastlownoise	db	0
last11bx	dw	0
last11dx	dw	0
last11di	dw	0
last21bx	dw	0
last21dx	dw	0
last21di	dw	0			; record of last sound made



ctvoicenameptr	dw	ctvoicename
ctvoicedir	db	64 dup(?)
ctvoicename	db	'DRV\CT-VOICE.DRV',0
soundvar	db	'SOUND='
blastvar	db	'BLASTER='

header	db	'Creative Voice File',1ah,1ah,0
	db	0ah,01h,29h,11h
	db	06h,02h,0,0,0fh
	db	27h,01,23h,3eh,0,6,0	; can be altered by program

	even



	.code


;	DETECT_SB --	Detect SoundBlaster card
;
;	[DI] = where to put found port

detect_sb       proc    near
	push	dx
	push	cx
	push	ax
	push	si
	push	bp

	mov	si,5		; look at all ports 5 times
dsportsloop:
	mov	dx,210h		; first possible port
dsportloop:
	add	dx,6		; DX=2x6h
	mov	al,1
	out	dx,al

	mov	cx,40
dswait1:
	aad
	aad
	aad
	loop	dswait1

	mov	al,0
	out	dx,al

	mov	bp,10		; test reset 10 times
	add	dx,-6+0eh

dsloop2:			; DX=2xEh
	mov	cx,40
dswait2:
	in	al,dx
	test	al,80h
	jnz	dsout2
	aad
	aad
	loop	dswait2
	sub	dx,0eh
	jmp	dsfail

dsout2:			      	
	add	dx,-0eh+0ah	; DX=2xAh

	aad
	aad

	in	al,dx
	cmp	al,0aah
	je	dsfound

	add	dx,-0ah+0eh
	dec	bp
	jg	dsloop2
	sub	dx,0eh
	jmp	dsfail

dsfail:				; DX=2x0h
	add	dx,10h
	cmp	dx,260h
	jbe	dsportloop
	dec	si
	jg	dsportsloop

	mov	word ptr [di],0
	xor	ax,ax
	jmp	dsout

dsfound:
	sub	dx,0ah
	mov	[di],dx
	mov	ax,1
dsout:
	pop	bp
	pop	si
	pop	ax
	pop	cx
	pop	dx
	ret
	endp





;	FINDENVIR --	Find environment variable.
;
;	Inputs:  	SI=var, CX=length 
;	Outputs: 	ES:SI=value, C=0  else c=1
;
findenvir	proc	near
	push	ax
	push	di
	push	cx

	mov	es,psp
	mov	ax,es:[2ch]
	mov	es,ax
	xor	di,di

fename:
	push	si			; save
	push	cx

	cld
	repe	cmpsb
	jcxz	fefound

	pop	cx
	pop	si
fegetnull:
	mov	al,es:[di]
	inc	di
	or	al,al
	jnz	fegetnull		; null?
	cmp	es:[di],al
	jne	fename			; eol?
	stc
	jmp	feout

fefound:
	pop	cx
	pop	si
	mov	si,di
	clc

feout:
	pop	cx
	pop	di
	pop	ax
	ret
	endp


;	SBPARSE --	Parse BLASTER= environment variable
;
;	Inputs:		ES:SI = ptr to it

sbparse	proc	near
sbploop:
	mov	al,es:[si]
	inc	si
	or	al,al
	jz	sbpout
	cmp	al,' '
	je	sbploop

	and	al,not 32
	cmp	al,'A'
	jne	sbptest1
	call	readhex
	mov	sbport,ax
	jmp	sbploop

sbptest1:
	cmp	al,'I'
	jne	sbptest2
	call	readhex
	cmp	ax,10h
	jne	sbp1ok
	mov	ax,10
sbp1ok:
	mov	sbirq,al
	jmp	sbploop

sbptest2:	
	cmp	al,'D'
	jne	sbptest3
	call	readhex
	mov	sbdma,al
	jmp	sbploop

sbptest3:
	cmp	al,'T'
	jne	sbptest4
	call	readhex
	dec	al
	or	al,al
	jz	sbploop
	or	soundcard,sbpromask
	jmp	sbploop

sbptest4:	; who cares, skip it
	mov	al,es:[si]
	inc	si
	or	al,al
	jz	sbpout
	cmp	al,' '
	jne	sbptest3
	jmp	sbploop

sbpout:
	ret
	endp



;	READHEX --	Read hexadecimal from ES:SI
;	Return in AX
;
readhex	proc	near
	xor	ax,ax
rhloop:
	mov	bl,es:[si]
	cmp	bl,'0'
	jb	rhout

	cmp	bl,'9'
	jbe	rhl1
	sub	bl,7
rhl1:
	sub	bl,'0'
	shl	ax,4
	add	al,bl
	inc	si
	jmp	rhloop	
rhout:
	ret
	endp


;*********************************************************************


;	SETSBVOICEVOL	Set volume of SB voice data
;	
;	AH = full volume amount
;
setsbvoicevol	proc	near
	push	ax
	push	dx
	mov	dx,sbport
	add	dx,4
	mov	al,04h			; set voice volume
	out	dx,al
	inc	dx
	in	al,dx
	mov	al,ah
	out	dx,al
	pop	dx
	pop	ax
	ret
	endp




;	INITSB --	Initialize SoundBlaster for direct-data
;
initsb	proc	near
	ret
	endp






;	INITSBDMA --	Read environment variables and such
;			concerning the SoundBlaster, load
;			drivers, and init.
;
;	Called once after V9t9.CNF is read.
;
;	Outputs:	Variables updated
;			CY=1 if any error

initsbdma proc	near
	push	es
	pusha

	mov	failreason,0
	lea	si,soundvar
	mov	cx,6
	call	findenvir
	mov	ctvoicenameptr,offset ctvoicename+4	; "CT-VOICE.DRV"
	jc	gsdnoctvar

	mov	ctvoicenameptr,offset ctvoicename	; "DRV\CT-VOICE.DRV"
	lea	di,ctvoicedir
gsdcpy:					
	mov	al,es:[si]     		; copy pathname
	mov	[di],al
	or	al,al
	jz	gsdcpyd
	inc	si
	inc	di
	jmp	gsdcpy

gsdcpyd:
	cmp	byte ptr [di-1],'\'
	je	gsdgotslash
	mov	byte ptr [di],'\'
	mov	byte ptr [di+1],0

gsdgotslash:
gsdnoctvar:
	lea	si,blastvar
	mov	cx,8
	call	findenvir
	jc	gsdnovar

	call	sbparse

gsdnovar:

;	Now load this file

	lea	bx,ctvoicename
	lea	si,ctvoicedir
	call	opensupportfile
	or	ax,ax
	jnz	gsdloaded
	mov	failreason,fr_ctvoice
	jmp	gsderrout

gsdloaded:

	mov	bx,ax			; now figure filesize
	mov	ax,4202h
	xor	cx,cx
	mov	dx,cx
	int	21h			; seek to end

					; AX is size
	push	bx			; save handle

	push	ax
	mov	ax,4200h
	xor	cx,cx
	mov	dx,cx
	int	21h			; back to start
	pop	bx

	mov	ah,48h
	add	bx,15
	shr	bx,4
	int	21h
	mov	ctvoiceseg,ax
	jnc	gsdgotmem

	pop	bx
	mov	ah,3eh
	int	21h			; close

	mov	failreason,fr_memory
	jmp	gsderrout

gsdgotmem:
	pop	bx
	mov	ah,3fh
	mov	cx,32767		; read
	push	ds
	mov	ds,ctvoiceseg
	xor	dx,dx
	int	21h
	pop	ds
	mov	ah,3eh
	int	21h
	
	mov	failreason,0
	jmp	gsdout
gsdcterrout:
	mov	ah,49h
	mov	es,ctvoiceseg
	int	21h

gsderrout:
	stc
	jmp	gsdleave
gsdout:
	xor	ax,ax
	mov	buff,ax
	mov	buff1,ax
	mov	buff2,ax
	clc
gsdleave:
	popa
	pop	es
	ret
	endp


;	STARTSBDMA -- 	When "sbdma" is requested, allocate memory,
;			(re-)initialize the driver, and turn on the speaker
;
;	When the memory is unallocated, buff|buff1|buff2==0
;
;	Returns CY=1 if memory failure
;
startsbdma proc near
	push	ax
	push	bx
	push	es

	mov	bx,1
	mov	ax,sbport
	ctvoicedrvcall			; Set SB port

	mov	bx,2
	mov	al,sbirq
	xor	ah,ah
	ctvoicedrvcall			; Set SB irq

	mov	bx,3
	ctvoicedrvcall			; initialize driver
	or	ax,ax
	jz	ssdinit

	add	ax,fr_cterr
	mov	failreason,ax
	jmp	ssdcterrout

ssdinit:
	mov	bx,4
	mov	ax,0
	ctvoicedrvcall			; turn off speaker

	mov	bx,5
	mov	ax,ds
	mov	es,ax
	lea	di,sbstat
	ctvoicedrvcall			; set status word offset

	mov	failreason,0
	jmp	ssdcont

ssdcterrout:
	mov	ah,49h
	mov	es,ctvoiceseg
	int	21h
ssderrout:
	stc
	jmp	ssdout

ssdcont:
	mov	failreason,fr_memory

	mov	ah,48h
	mov	bx,(22016/16)*3		; see if enough (66k?)
	int	21h
	jc	ssdout

	mov	es,ax
	mov	ah,49h
	int	21h			; release 66k block

	mov	ah,48h
	mov	bx,22200/16
	int	21h			; get buffer for
	mov	buff,ax			; general noise, 110-4000hz

	mov	ah,48h
	mov	bx,22200/16
	int	21h			; get buffer for
	mov	buff1,ax		; periodic noise, 4000-22000hz

	mov	ah,48h
	mov	bx,22200/16
	int	21h	   		; get buffer for
	mov	buff2,ax		; white noise, 4000-22000hz

	call	makenoise1_4000		; set up noise buffers
	call	makenoise2_4000
	call	initbuff

	mov	bx,4
	mov	ax,1
	ctvoicedrvcall			; turn on speaker

	mov	failreason,0
	clc
ssdout:
	pop	es
	pop	bx
	pop	ax
	ret
	endp


;	Initialize the CT-VOICE.DRV header.
;
;
initbuff	proc	near
	push	es
	push	di
	push	si
	push	cx

	mov	es,buff
	mov	cx,26h
	lea	si,header
	xor	di,di
	rep	movsb

	pop	cx
	pop	si
	pop	di
	pop	es
	ret
	endp




;	GETSBDMA --	If wanted in PLAYSOUND, load the CT-VOICE.DRV
;			driver, init it, and get memory.
;
;	Returns C=1 and error message if failure.

	.data

gsbfails dw	gsb_none,gsb_mem,gsb_ctv,gsb_card,gsb_port,gsb_dma

gsb_none db	'Spurious error from GetSBDMA.  Notify the author.',0dh,0ah,0
gsb_mem	db	'Not enough memory for Sound Blaster DMA init.',0dh,0ah,0
gsb_ctv	db	'Sound Blaster DMA driver % not found.',0dh,0ah
	db	'See SOUND.TXT for more info.',0dh,0ah,0
gsb_cter db	0
gsb_card db	'%:  Not a true Sound Blaster card.',0dh,0ah,0
gsb_port db	'%:  Bad Sound Blaster base port given.',0dh,0ah,0
gsb_dma	db	'%:  Sound Blaster DMA/IRQ error.',0dh,0ah
	db	'Be sure the BLASTER variable is correct.',0dh,0ah,0


	.code

getsbdma proc near

	test	playsound,sbdmamask
	jz	gsbdout

	call	initsbdma
	jc	gsbderr

	call	startsbdma
	jc	gsbderr

	xor	ax,ax				; force recalc of noise
	mov	lastlownoise,al
	mov	last11bx,ax
	mov	last11dx,ax
	mov	last11di,ax
	mov	last21bx,ax
	mov	last21dx,ax
	mov	last21di,ax
	clc
	jmp	gsbdout

gsbderr:
	mov	bx,failreason
	add	bx,bx
	mov	dx,gsbfails[bx]
	lea	si,filename
	call	setuperror
	stc

gsbdout:
	ret
	endp


;	LOSESBDMA --	Lose SB DMA driver and buffers.
;
losesbdma proc	near
	test	playsound,sbdmamask
	jz	lsbdout
	call	stopsbdma
	call	dropsbdma
lsbdout:
	ret
	endp
	


;	STOPSBDMA --	Stop SB DMA usage (free memory, turn off speaker)
;
;	Sets buff|buff1|buff2==0
;
stopsbdma proc	near
	push	ax
	push	es

	mov	bx,4
	mov	ax,0
	ctvoicedrvcall			; turn off connection to speaker

	mov	ah,49h			; lose buffers
	mov	es,buff
	int	21h
	mov	buff,0

	mov	ah,49h			; lose samples
	mov	es,buff1
	int	21h
	mov	buff1,0

	mov	ah,49h			; lose samples
	mov	es,buff2
	int	21h
	mov	buff2,0

ssdnothere:
	pop	es
	pop	ax
	ret
	endp


;	DROPSBDMA --	Drop SB DMA driver.
;	
dropsbdma proc	near
	push	ax
	push	bx
	push	es

	mov	ah,0ffh
	call	setsbvoicevol		; restore volume

	mov	bx,9			; terminate driver
	ctvoicedrvcall

	mov	ah,49h
	mov	es,ctvoiceseg
	int	21h			; lose driver

	mov	ctvoiceseg,0
	mov	ctvoicedrv,0

	pop	es
	pop	bx
	pop	ax

	ret
	endp






;	STARTDMA --	Start playing a sample
;
;	Inputs:		ES:DI --> sample
;
startdma proc	near
	push	bx

	mov	bx,6
	ctvoicedrvcall			; start new 

	pop	bx
	ret
	endp



;	STOPDMA --	Just stop the sample
;
;	(A new one follows, prolly)
;
stopdma proc	near
	push	bx

	mov	bx,8			; terminate playing
	ctvoicedrvcall

	pop	bx
	ret
	endp



;	CLEARSBDMA --	Clear the SoundBlaster DMA
;			(stop sample)
;
clearsbdma proc	near
	call	stopdma
	ret
	endp





;	CLEANSB --	Clean up SB.
;
cleansb proc	near
	and	playsound,not sblastermask
	and	soundcard,not sblastermask
	ret
	endp





;	CLEANSBDMA --	Clean up SB DMA stuff.
;	
cleansbdma proc	near
	and	soundcard,not sbdmamask
	and	playsound,not sbdmamask
	ret
	endp




;	Set noise output
;	AL = value
;
;	Noises from 4000+ hz have been created as one buffer; 
;	to use them just set the frequency byte to be different.
;	from 110-4000 hz, the buffer is created on the fly,
;	and played at 11186 Hz.
;
;
sbnoisetone proc	near
	pusha
	push	es

	mov	ax,voicen.hertz

	cmp	ax,109
	jb	snzap
	cmp	ax,22000
	jbe	sndo
	mov	ax,22000
	jmp	sndo

snzap:
	call	stopdma
	jmp	snout

sndo:
	cmp	ax,4000
	jb	snmakeit
	jmp	snpredone
snmakeit:

;	mov	bx,10
;	mov	ax,voicen.period
;	xor	dx,dx
;	div	bx			; AX=per/10, DX=per mod 10
	
;	push	ax
;	xor	ax,ax
;	mov	bx,10			; dx:ax=(per mod 10)*65536
;	div	bx			; ax=minor 
;	mov	dx,ax
;	pop	ax


;	Period = (4000/55930)*period

	mov	ax,4000
	mul	voicen.period
	mov	bx,55930
	div	bx

	mov	di,26h			; delta high
	xor	si,si			; delta low
	mov	bx,ax			; BX=major add
					; DX=minor add

	test	voicen.stype,4
	jnz	snmwhite

	call	makenoise1_110
	jmp	snmfixsize

snmwhite:
	call	makenoise2_110

snmfixsize:
	sub	di,sync
	add	di,2-26h
	mov	es:[21h],di	   	; length of sample
	sub	di,2-26h
	mov	al,7			; repeat
	stosb
	xor	ax,ax
	stosw
	stosw
	mov	bl,131			; speed=8000, ustabe 167,11186 -- ustabe 131,8000
	jmp	sncontinue

snpredone:
	mov	es,buff2
	test	voicen.stype,4
	jnz	snpwhite
	mov	es,buff1
snpwhite:
	
	mov	ax,4240h
	mov	dx,0fh
	div     word ptr voicen.hertz	; AX=256-sample code
	neg	al			; AL=sample code
	mov	bl,al
	jmp	sncontinue

sncontinue:
	cmp	sbstat,0
	jz	snnotpl

	call	stopdma

snnotpl:
	mov	byte ptr es:[24h],bl  		; location of sample rate
	mov	word ptr es:[1eh],0ffffh	; infinite repetitions

	mov	di,01ah
	call	startdma

snout:
	pop	es
	popa
	ret
	endp


;	Set noise volume
;
sbnoisevol	proc	near
	push	ax

	mov	al,voicen.volume
	xor	al,0fh
	mov	ah,al
	shl	al,4
	or	ah,al
	call	setsbvoicevol

	cmp	voicen.volume,0fh	; off?
	je	snvstop

	cmp	sbstat,0		; is it still going?
	jnz	snvout			; yup

	call	sbnoisetone		; well, turn it on!
	jmp	snvout		

snvstop:
	call	stopdma

snvout:
	pop	ax
	ret
	endp



;	Make the periodic noise which will be used from 4000-22000 Hz.
;

makenoise1_4000	proc	near
	push	es
	push	di
	push	si
	push	cx
	push	dx
	push	bp

	mov	es,buff1
	mov	cx,26h
	lea	si,header

	xor	di,di
	rep	movsb

	mov	dx,22000/15
	mov	ah,80h
	mov	al,noisetop
mn14loop:
	stosb
	xchg	ah,al
	mov	cx,14
	rep	stosb
	xchg	ah,al
     	dec	dx
	jg	mn14loop

	mov	ax,di
	add	ax,2-26h
	mov	es:[21h],ax			; set up sample size

	mov	al,7
	stosb
	xor	ax,ax
	stosw
	stosw

	pop	bp
	pop	dx
	pop	cx
	pop	si
	pop	di
	pop	es
	ret
	endp



;	Make the white noise to be used from 4000-22000 Hz.
;

makenoise2_4000	proc	near
	push	es
	push	di
	push	si
	push	cx
	push	dx
	push	bp

	mov	es,buff2
	mov	cx,26h
	lea	si,header

	xor	di,di
	rep	movsb

	mov	dx,22000
	mov	cx,0aaaah
	mov	bp,1
mn24loop:
	mov	al,80h
	test	cx,1
	jz	mn24high
	mov	al,noisetop
mn24high:
	stosb
	rol	cx,1
	xor	cx,bp
	add	bp,cx
	jnz	mn24no
	inc	bp
mn24no:
	dec	dx
	jg	mn24loop

	mov	ax,di
	add	ax,2-26h
	mov	es:[21h],ax

	mov	al,7
	stosb
	xor	ax,ax
	stosw
	stosw

	pop	bp
	pop	dx
	pop	cx
	pop	si
	pop	di
	pop	es
	ret
	endp



;	Make periodic noise from 110-4000 Hz.
;	The sample will be played at a fixed rate (8000 hz).
;
;	BX:DX = add delta, DI:SI=current pointer
;

makenoise1_110	proc	near
	mov	es,buff
	
	cmp	lastlownoise,1
	jne	mn11yeah
	cmp	bx,last11bx
	jne	mn11yeah
	cmp	dx,last11dx
	jne	mn11yeah		
	mov	di,last11di
	jmp	mn11not
mn11yeah:
	mov	last11bx,bx
	mov	last11dx,dx
	mov	lastlownoise,1

	jmp	mn11high
mn11loop:
	mov	cx,bx
	add	si,dx
	jnc	mn11ok			;
	inc	cx
mn11ok:

	shr	cx,1
	rep	stosw
	jnc	mn11nomore
	stosb
mn11nomore:
	mov	ax,8080h
	add	bp,bp
	cmp	bp,8000h
	jb	mn11loop
mn11high:
	mov	ah,noisetop
	mov	al,ah
	mov	bp,1
        cmp     di,8000                ; just the length
	jb	mn11loop
	mov	last11di,di
mn11not:
	ret
	endp


;	Make white noise from 110-4000 Hz
;	The sample will be played at 8000hz.
;
;	BX:DX=add delta, DI:SI = current pointer
;

makenoise2_110	proc	near
	mov	es,buff
	push	si

	cmp	lastlownoise,2
	jne	mn21yeah
	cmp	bx,last21bx
	jne	mn21yeah
	cmp	dx,last21dx
	jne	mn21yeah
	mov	di,last21di
	jmp	mn21not

mn21yeah:
	mov	last21bx,bx
	mov	last21dx,dx
	mov	lastlownoise,2

	mov	ax,8080h		; shift value
	mov	bp,1			; shift register

mn21loop:
	mov	cx,bx
	add	si,dx
	jnc	mn21ok			;
	inc	cx
mn21ok:
	test	ax,1

	push	ax

	mov	ax,8080h
	jz	mn21high
	mov	ah,noisetop
	mov	al,ah
mn21high:

	shr	cx,1
	rep	stosw
	jnc	mn21nomore
	stosb
mn21nomore:

	pop	ax
	rol	ax,1
	xor	ax,bp
	add	bp,ax
	jnz	mn21nofix
	inc	bp
mn21nofix:
	cmp	di,22000
	jb	mn21loop
	mov	last21di,di
mn21not:
	pop	si
	ret
	endp


	.data

sbspeech spchstruc <sbspinit,sbsaybyte,sbspshut,sbsayonebyte>

	.code

;	DDELAY --	Dumb delay
;
;
ddelay	proc	near
	mov	ax,2000
dd1:
	jmp	dd2
dd2:
	dec	ax
	jg	dd1
	ret
    	endp


;	SBSPINIT --	Initialize Soundblaster for direct data
;
;
sbspinit proc	near
	push	ax
	push	bx
	push	cx
	push	dx

	test	soundcard,sbdmamask
	jz	ssinodma

	call	stopdma			; speech and noise conflict...
	mov	sbstat,0		; turn off noise

ssinodma:
	mov	ah,0ffh
	call	setsbvoicevol
ssis8:
	mov     ax,0001H      		; signify direct output
	mov	dx,sbport
	add	dx,6
	out	dx,ax
	call	ddelay
	mov	dx,sbport
	add	dx,6
	mov	ax,0
	out	dx,ax
	call	ddelay

	mov	bx,10
ssisres0:
	mov	dx,sbport
	add	dx,0eh
        mov     cx,0ffffH
ssis9:  
	in      al,dx
	test	al,80h		     	; timing
        jnz     ssis10
        loop    ssis9
        jmp     ssis8
ssis10:
	mov	dx,sbport
	add	dx,0ah
        in      al,dx
        cmp     al,0aaH		    	; result code:  okay
        jne     ssis11
	clc
	jmp	ssis12
ssis11:
	dec	bx
	jg	ssisres0
	stc
ssis12:
	jc	ssisout
	mov	dx,sbport
	add	dx,0ch
ssis6:
	in      al,dx
	test	al,80h
	jnz	ssis6
	mov     al,0d1H
	out     dx,al			; turn on speaker
	clc
ssisout:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
  	endp


sbspshut proc	near
	push	ax
	push	dx

	test	soundcard,sbdmamask
	jnz	sssnoshut

	mov	dx,sbport
	add	dx,0ch
L7:
	in      al,dx
	test	al,80h
	jnz	l7
        mov     al,0d3H			; turn off speaker
        out     dx,al

sssnoshut:
	test	playsound,sbdmamask
	jz	sssnorestart

	lea	bx,voicen		; speech and noise conlfict...
	call	word ptr noisetone
	lea	bx,voicen		; restart noise
	call	word ptr noisevol

sssnorestart:
	pop	dx
	pop	ax
	ret
	endp
	

;	Speak a byte.  AH=value
;
;
sbsayonebyte proc near
	push	dx
	push	ax

	mov	dx,sbport
	add	dx,0ch
L4:
	in      al,dx
	test	al,80h
	jnz	L4

        mov     al,10H
        out     dx,al
L5:     
	in      al,dx
	test	al,80h
	jnz	L5
        mov     al,ah
        out     dx,al

	pop	ax
	pop	dx
	ret
	endp


sbsaybyte proc	near
	push	ax
	mov	al,20h
	out	20h,al
	pop	ax
	iret
	endp


	comment	\
sbsaybyte proc	near
	push	ds
	push	es
	push	ax
	push	dx
	push	bx

	mov	ax,@data
	mov	ds,ax
	mov	es,saddr
	mov	bx,soffs
	mov	ah,es:[bx]

	call	sbsayonebyte

	dec	slen
	inc	soffs
	
	mov	al,20h
	out	20h,al
	pop	bx
	pop	dx
	pop	ax
	pop	es
	pop	ds
	iret
	endp
	\
