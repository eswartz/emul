; ********************************************
; SBDMA.INC include file for SoundBlaster DMA
; ********************************************
; by Edward Swartz  5/1996
; ********************************************

;	SBDMA.INC
;
;
;	Routines for controlling the SoundBlaster card's
;	DMA buffering.
;

;	Noise and speech will share the DMA resource.
;	Rather than forcing both to play seesaw, this module
;	will keep a stack of active DMA calls, and restore
;	samples when playing ones time out.

;	sbdmaplay	-- start playing a sample immediately
;


	.data


;	Offsets relative to base I/O address.

SB_LEFT_FM_STATUS	equ	00h    ; Pro only 
SB_LEFT_FM_ADDRESS	equ	00h    ; Pro only 
SB_LEFT_FM_DATA		equ	01h    ; Pro only 
SB_RIGHT_FM_STATUS	equ	02h    ; Pro only 
SB_RIGHT_FM_ADDRESS	equ	02h    ; Pro only 
SB_RIGHT_FM_DATA	equ	03h    ; Pro only 
SB_MIXER_ADDRESS	equ	04h    ; Pro only 
SB_MIXER_DATA		equ	05h    ; Pro only 
SB_DSP_RESET		equ	06h
SB_FM_STATUS		equ	08h
SB_FM_ADDRESS		equ	08h
SB_FM_DATA		equ	09h
SB_DSP_READ_DATA	equ	0Ah
SB_DSP_WRITE_DATA	equ	0Ch
SB_DSP_WRITE_STATUS	equ	0Ch
SB_DSP_DATA_AVAIL	equ	0Eh
SB_CD_ROM_DATA		equ	10h    ; Pro only 
SB_CD_ROM_STATUS	equ	11h    ; Pro only 
SB_CD_ROM_RESET		equ	12h    ; Pro only 
SB_CD_ROM_ENABLE	equ	13h    ; Pro only 
				  
SB_ADLIB_FM_STATUS	equ	388h
SB_ADLIB_FM_ADDRESS	equ	388h
SB_ADLIB_FM_DATA	equ	389h

;	Defines for 8237 DMA Controller IO addresses

SB_DMA		equ	0
SB_CH0_BASE	equ	0
SB_CH0_COUNT	equ	1
SB_CH1_BASE	equ	2
SB_CH1_COUNT	equ	3
SB_CH2_BASE	equ	4
SB_CH2_COUNT	equ	5
SB_CH3_BASE	equ	6
SB_CH3_COUNT	equ	7
SB_DMA_STATUS	equ	8
SB_DMA_CMD	equ	8
SB_DMA_REQUEST	equ	9
SB_DMA_MASK	equ	10
SB_DMA_MODE	equ	11
SB_DMA_FF	equ	12
SB_DMA_TMP	equ	13
SB_DMA_CLEAR	equ	13
SB_DMA_CLRMSK	equ	14
SB_DMA_WRMSK	equ	15
SB_DMAPAGE	equ	80h

;	Types of Soundblaster Cards 

SB_TYPE_15	equ	1
SB_TYPE_PRO	equ	2
SB_TYPE_20	equ	3

;	DSP Commands 

SB_DIRECT_8_BIT_DAC	equ	10h
SB_DMA_8_BIT_DAC	equ	14h
SB_DMA_2_BIT_DAC	equ	16h
SB_DMA_2_BIT_REF_DAC	equ	17h
SB_DIRECT_ADC		equ	20h
SB_DMA_ADC		equ	24h
SB_MIDI_READ_POLL	equ	30h
SB_MIDI_READ_IRQ	equ	31h
SB_MIDI_WRITE_POLL	equ	38h
SB_TIME_CONSTANT	equ	40h
SB_DMA_4_BIT_DAC	equ	74h
SB_DMA_4_BIT_REF_DAC	equ	75h
SB_DMA_26_BIT_DAC	equ	76h
SB_DMA_26_BIT_REF_DAC	equ	77h
SB_HALT_DMA		equ	0D0h
SB_CONTINUE_DMA		equ	0D4h
SB_SPEAKER_ON		equ	0D1h
SB_SPEAKER_OFF		equ	0D3h
SB_DSP_ID		equ	0E0h
SB_DSP_VER		equ	0E1h
SB_MDAC1		equ	61h
SB_MDAC2		equ	62h
SB_MDAC3		equ	63h
SB_MDAC4		equ	64h
SB_MDAC5		equ	65h
SB_MDAC6		equ	66h
SB_MDAC7		equ	67h



DMAcurr		dw	0		; DMAstruc addr of current sample

DMAgoing	db	0		; is DMA sample played?



	.code

;	SBDMAPLAY	-- start playing a sample
;
;	Input
;		SI	-> DMAstruc 

sbdmaplay proc	near
	push	di
	pushf

	test	playsound,sbdmamask
	jnz	sdp00

	jmp	sdpout

sdp00:
	cli

	cmp	DMAcurr,0		; another sample playing?
	je	sdp01			; nope

	call	sbdmaabort		; no more sample (turns off int)
		
sdp01:
	mov	DMAcurr,si		; set current sample addr
	mov	[si].sampleplayed,0	; 0 bytes played
	call	sbdmaqueue

sdpout:
	popf
	pop	di
	ret
	endp




;	SBDMAQUEUE	-- queue part of a sample
;
;	Called from sbdmaplay (original) or sbintr (during)
;
;	Won't play so that sample crosses 64k boundary
;
;	PREREQ: DMAcurr.sampleplayed<DMAcurr.sampleleng
;
;	Input:
;		DMAcurr	-> record block for sample to play


	.data


dmaregs	db	87h,83h,81h,82h,8fh,8bh,89h,8ah

	.code

sbdmaqueue proc	near
	pusha
	pushf

	cli				; don't let other sample end

	cmp	DMAgoing,0		; anything going now?
	jz	sdq01			; nope

	call	sbdmaabort

sdq01:					
;	Enable interrupt on PIC

	in	al,21h
	mov	ah,1
	mov	cl,sbirq
	shl	ah,cl
	not	ah
	and	al,ah
	out	21h,al

	
; 	Set DMA mode
	
	mov	al,5
	out	SB_DMA_MASK,al
	mov	al,0
	out	SB_DMA_FF,al
	mov	al,49h
	out	SB_DMA_MODE,al
	
		
; 	Set transfer address

	mov	si,DMAcurr

	mov	bx,[si].sampleseg	; XXXX -> XXXX0
	mov	cx,[si].sampleoffs	; XXXX -> 0XXXX

	mov	ax,bx
	shl	ax,4 			; AX holds 0XXX to add to offset
	shr	bx,12			; BX holds 000X[0000] part of offset
	add	cx,ax			; get complete offset
	adc	bx,0			; fix 64k block, if necc
	add	cx,[si].sampleplayed	; add played count
	adc	bx,0			; fix 64k block, if necc

	xor	ah,ah
	mov	al,sbdma
	mov	di,ax
	xor	dh,dh
	mov	dl,dmaregs[di]		; get DMA page reg 

	mov	al,bl
	out	dx,al			; output the 64k block

	xor	dh,dh
	mov	dl,sbdma
	shl	dx,1
	add	dx,SB_DMA		; figure DMA address

	mov	al,cl
	out	dx,al
	mov	al,ch
	out	dx,al			; output the offset


;	Set transfer length byte count

	mov	bx,[si].sampleleng
	sub	bx,[si].sampleplayed

;	Be sure we don't queue over a 64k boundary!

	mov	cx,[si].sampleseg
	shl	cx,4
	add	cx,[si].sampleoffs	
	add	cx,[si].sampleplayed	; CX is current offset
	neg	cx			; CX is maximum sample size
	jz	sdq02			; right-up-to-the-limit, okay

	cmp	cx,bx
	ja	sdq02			; okay size in BX

	mov	bx,cx			; take up-to-64k boundary

sdq02:
	add	[si].sampleplayed,bx

	inc	dx
	dec	bx
	mov	al,bl
	out	dx,al			; output low byte of leng
	mov	al,bh
	out	dx,al			; output high byte of leng


;	Unmask DMA channel

	mov	al,sbdma
	out	SB_DMA_MASK,al

	sti				; turn on ints

	mov	ah,[si].samplevol
	call	sbsetvoicevol		; set volume

	mov	al,SB_TIME_CONSTANT
	call	sbwritedac
	mov	al,[si].samplespeed
	call	sbwritedac		; set sample speed

	mov	al,SB_DMA_8_BIT_DAC
	call	sbwritedac	   	; 8-bit output please

	mov	al,bl
	call	sbwritedac
	mov	al,bh
	call	sbwritedac		; write length of sample

	mov	DMAgoing,1

	popf
	popa
	ret
	endp



;	SBINTR		-- interrupt routine on SB IRQ
;
;	End of sample, call DMAcurr.stopper.

sbintr	proc	near
	push	ax
	push	dx
	push	si
	push	ds

	mov	ax,@data
	mov	ds,ax

;	Acknowledge soundblaster

	mov	dx,sbport
	add	dx,SB_DSP_DATA_AVAIL
	in	al,dx

	mov	DMAgoing,0

	mov	si,DMAcurr
	or	si,si
	jz	sbiout			; huh?!

	mov	ax,[si].sampleplayed	; how much has been played
	cmp	ax,[si].sampleleng	; how much can be played
	jb	sbimore			; are we done?

	mov	DMAcurr,0		; nothing playing now
	call	[si].stopper
	jmp	sbiout

sbimore:
	call	sbdmaqueue		; queue more data

sbiout:
	mov	al,20h
	out	20h,al
	
	pop	ds
	pop	si
	pop	dx
	pop	ax
	iret
	endp
	



;	SBDMAABORT	-- abort playing current sample
;
;	Does nothing to DMAcurr, etc.
;

sbdmaabort proc near
	push	ax
	push	cx

	mov	al,SB_HALT_DMA
	call	sbwritedac

;	Disable interrupt on PIC

	in	al,21h
	mov	ah,1
	mov	cl,sbirq
	shl	ah,cl
	or	al,ah
	out	21h,al

	mov	dx,sbport
	add	dx,SB_DSP_DATA_AVAIL
	in	al,dx

	mov	DMAgoing,0

	pop	cx
	pop	ax
	ret
	endp




;	SBSPEAKERON/OFF	-- affect speaker
;
;
sbspeakeron:
	push	ax
	mov	al,SB_SPEAKER_ON
	jmp	sbspeakeroff1
sbspeakeroff:
	push	ax
	mov	al,SB_SPEAKER_OFF
sbspeakeroff1:
	call	sbwritedac
	pop	ax
	ret


;	SBWRITEDAC	-- write command to SB
;
;	Inputs:
;		AL	= command
;

sbwritedac proc near
	push	ax
	push	dx
	push	cx
 
	mov	ah,al
	mov	dx,sbport
	add	dx,SB_DSP_WRITE_STATUS

	mov	cx,65535
sbwd00:	
	in	al,dx
	test	al,80h
	jz	sbwd01
       	loop	sbwd00

sbwd01:
	add	dx,-SB_DSP_WRITE_STATUS+SB_DSP_WRITE_DATA
	mov	al,ah
	out	dx,al

	pop	cx
	pop	dx
	pop	ax
	ret
	endp



;	SBSETVOICEVOL	Set volume of SB voice data
;	
;	AH = full volume amount
;
sbsetvoicevol	proc	near
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



