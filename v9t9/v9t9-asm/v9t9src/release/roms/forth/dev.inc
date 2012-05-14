;	V9t9: the TI Emulator! v6.0 Source 
;	Copyright (c) 1996 by Edward Swartz

;	DEV.INC
;
;	Peripheral device access.
;
;


;==========================================================================
;	Device init.
;
;	Execute each DSR's initialization routine.
;==========================================================================

haa	byte	>aa
	byte	0

dinit	li	0,>3fff
	mov	0,@vdplimit			; set up 16k VDP RAM

	lwpi	>83e0  				; be standard
	li	13,>9800
	li	14,>100
	li	15,>8c02

	li	12,>0f00			; start below first DSR
di00	sbz	0
	ab	@h01,12				; point to next DSR
	ci	12,>2000
	jhe	diout				; done

	sbo	0				; turn on ROM
	cb	@>4000,@haa			; legal rom?
	jne	di00				; nope

	mov	@>4004,1			; get init ptr
	jeq	di00				; none
di01	mov	@2(1),11			; get addr for init
	mov	12,@>83d0
	bl	*11				; call powerup routine
	mov	*1,1				; get next powerup
	jne	di01				; if it exists
	jmp	di00

diout	lwpi	mainws				; restore WP
	rt


;--------------------------------------------------------------------------
;	Disk init.
;
;	We want to limit to two buffers, since bitmap mode would otherwise
;	overwrite it.
;
;	With this setup we will ONLY use the emulated disk DSR for stuff.
;	So, we'll simply write the new info for the real DSR, if installed,
;	and probe the emulated disk DSR.
;--------------------------------------------------------------------------

noemuerr db	"No emulated disk DSR found!",>ff
	db	"Please install the emulated disk DSR",>ff
	db	"by adding 'EmuDisk' to the DSRCombo",>ff
	db	"variable in FORTH.CNF.",>ff,>ff
	db	"(See DISKS.TXT for info.)",>ff,>ff
	db	"Press Ctrl+Break to halt."
	db	0

forthdskdef db	"FORTHDSK  "
	even

diskinit dect	SP
	mov	11,*SP

	li	0,>3fff
	mov	0,@vdplimit			; setup

	li	12,>1000			; DSR base for emulated DSR
	sbo	0				; turn on
	cb	@haa,@>4000			; installed?
	jeq	dskiokay

	li	2,noemuerr			; print error message
	b	@dieerr

dskiokay:
	sbz	0

	li	0,forthdskdef
	li	1,forthdsk
	li	2,10
dskifn	movb	+*0,+*1
	dec	2
	jgt	dskifn

	mov	+*SP,11
	rt


;-------------------------------

vmbw	dect	SP
	mov	11,*SP
	bl	@vwaddr
vmbw0	movb	+*1,@>8c00
	dec	2
	jgt	vmbw0
	mov	+*SP,11
	rt

vmbr	dect	SP
	mov	11,*SP
	bl	@vraddr
vmbr0	movb	@>8800,+*1
	dec	2
	jgt	vmbr0
	mov	+*SP,11
	rt


vsbw	dect	SP
	mov	11,*SP
	bl	@vwaddr
	movb	1,@>8c00
	mov	+*SP,11
	rt


vsbr	dect	SP
	mov	11,*SP
	bl	@vraddr
	movb	@>8800,1
	mov	+*SP,11
	rt


;--------------------------------------------------------------------------
;	This routine will READ one block (1k) to CPU RAM.
;
;	>3F00 is used as a sector buffer.
;	>3EF0 is used as a PAB.
;
;	>8300->83E0 used too.
;	
;	R1=block #
;	R2=addr
;
;	BLWP @
;
;	Returns hiR0=0 for no error
;--------------------------------------------------------------------------

rblockpab db	>01,>14

rblock	data	dskws,rblock 4+
	limi	0
	li	SP,dskstack dskstacksize +


	li	0,>3ef0
	li	1,forthdsk
	li	2,10
	bl	@vmbw				; set filename
	mov	0,@>834e

	li	3,4				; # secs to read
	mov	@4(13),1			; CPU addr
	mov	@2(13),5			; block #
	sla	5,2				; sector #
rblks 	li	0,>0101
	mov	0,@>834C			; read from drive 1

	clr	@>8350

	li	0,>3f00
	mov	0,@>8300			; VDP buff addr 
	mov	5,@>8302			; sector #

	li	6,rblockpab
	bl	@dodsr				; do read
	movb	@>8350,*13
	jne	rblkerr
	li	0,>3f00
	li	2,256
	bl	@vmbr  				; copy to CPU RAM

						; R1 autoincremented
	inc	5				; next sector
	dec	3
	jgt	rblks

rblkerr	movb	@>8350,*13

	rtwp



;--------------------------------------------------------------------------
;	This routine will WRITE one block (1k) from CPU RAM.
;
;	>3F00 is used as a sector buffer.
;	>3EF0 is used as a PAB.
;
;	>8300->83E0 used too.
;	
;	R1=block #
;	R2=addr
;
;	BLWP @
;
;	Returns hiR0=0 for no error
;--------------------------------------------------------------------------

wblockpab db	>01,>15

wblock	data	dskws,wblock 4+
	limi	0
	li	SP,dskstack dskstacksize +


	li	0,>3ef0
	li	1,forthdsk
	li	2,10
	bl	@vmbw				; set filename
	mov	0,@>834e

	li	3,4				; # secs to read
	mov	@4(13),1			; CPU addr
	mov	@2(13),5			; block #
	sla	5,2				; sector #
wblks 	li	0,>0101
	mov	0,@>834C			; write to drive 1

	clr	@>8350

	li	0,>3f00
	mov	0,@>8300			; VDP buff addr 
	mov	5,@>8302			; sector #

	li	2,256
	bl	@vmbw  				; copy from CPU RAM

	li	6,wblockpab
	bl	@dodsr				; do write
	movb	@>8350,*13
	jne	wblkerr
						; R1 autoincremented
	inc	5				; next sector
	dec	3
	jgt	wblks

wblkerr	movb	@>8350,*13

	rtwp


;==========================================================================

;	Do a "generic" block read or write.
;
;	R6 -> device name

dodsr	mov	6,@>83e0 12 +
	lwpi	>83e0				; be standard
	li	13,>9800
	li	14,>100
	li	15,>8c02

	li	12,>1000			; our CRU base
	sbo	0				; turn it on

	li	1,>400A				; subprograms
	mov	*1,1
	jeq	dodsrerr
dodsrfnd mov	@4(1),2				; complete name
	c	2,*6				; same name?
	jeq	dodsrrun
	mov	*1,1				; get next
	jne	dodsrfnd

dodsrerr movb	@hff,@>8350			; fake error
	jmp	dodsrout

dodsrrun mov	@2(1),11       			; get addr
	mov	12,@>83d0			; save CRU addr
	bl	*11				; call routine
	nop					; error 

dodsrout li	12,>1000 
	sbz	0				; turn off ROM
	
	lwpi	dskws
	rt


equals	db	>20
period	db	"."

;	DSRLNK
;
;	Caller's R1 contains 8 or 10 for device/subroutine calls

dsrlnk	data	dskws,dsrlnk 4+
	limi	0
	li	SP,dskstack dskstacksize +

	mov	@2(13),5			; get offset
	szcb	@equals,15			; no error
	mov	@>8356,0			; get ptr to name
	mov	0,9				
	si	9,8				; point to error code

	bl	@vsbr				; get len
	movb	1,3				; save
	srl	3,8				
	seto	4				; # chars

	li	2,>834a				; buffer
dsr00	inc	0    				; move device name
	inc	4
	c	4,3
	jeq	dsr01
	bl	@vsbr
	movb	1,+*2
	cb	1,@period
	jne	dsr00
dsr01	mov	4,4				; any chars read?
	jeq	dsr09
	ci	4,7
	jgt	dsr09				; too many?
	clr	@>83d0
	mov	4,@>8354			; # chars in device name
	inc	4
	a	4,@>8356			; point to '.' in name
	lwpi	>83e0				; GPLWS
	clr	1				; init card counter
	li	12,>f00
dsr03	sbz	0
	ai	12,>100				; start scan at >1000
	clr	@>83d0
	ci	12,>2000			; last base?
	jeq	dsr08
	mov	12,@>83d0			; store CRU
	sbo	0				; turn on rom
	li	2,>4000
	cb	*2,@haa				; legal rom?
	jne	dsr03
	a	@dskws 10 +,2			; add offset
	jmp	dsr05
dsr04	mov	@>83d2,2
	sbo	0
dsr05	mov	*2,2				; any devices?
	jeq	dsr03		    		; nope... next rom pleez
	mov	2,@>83d2			; save next link
	inct	2
	mov	+*2,9				; get routine addr
	movb	@>8355,5			; get len of caller
	jeq	dsr07				; ??? no length?
	cb	5,+*2				; match name
	jne	dsr04
	srl	5,8
	li	6,>834a
dsr06	cb	+*6,+*2
	jne	dsr04
	dec	5
	jne	dsr06
dsr07	inc	1	    			; increment card #
	bl	*9				; run it
	jmp	dsr04				; if no error, skip this word
	sbz	0				; turn off rom
	lwpi	dskws				
	mov	9,0				; get error code
	bl	@vsbr
	srl	1,13
	jne	dsr10				; any error?
	rtwp					; nope
dsr08	lwpi	dskws
dsr09	clr	1
dsr10	swpb	1
	movb	1,*13
	socb	@equals,15
	rtwp

