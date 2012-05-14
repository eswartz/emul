; ***************************************
; SOUND.ASM  V9t9 sound routines
; ***************************************
; by Edward Swartz  6/3/1993
; ***************************************

_SOUND_	= 1

	comment	\

ออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออ

     When a write is made to address >8400, routine "handlesound" in
SOUND.ASM is called.  This routine decodes the sound bytes sent and
maintains four "voiceinfo" structures which tell what each of the three
voices and noise channel are doing.

     Sounds are played immediately upon being programmed.  Depending on
how the sound devices were set up, a call to PCSPEAK.INC, ADLIB.INC, or
SBLASTER.INC follows.


ออออออออออออออออออออออออฺฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฟอออออออออออออออออออออออออ
                        ณ DEVICE INDEPENDENCE ณ
ออออออออออออออออออออออออภฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤูอออออออออออออออออออออออออ

     V9t9 is able to achieve some device independence.  The "devassn"
array of structures in SOUND.ASM is a list that tells V9t9 which devices
will handle things like tones, noise, and speech output.  Depending on
what devices were found and which devices were excluded (i.e. "Sound" in
V9t9.CNF), the routine pointers "voicetone", "voicevol", "noisetone",
"noisevol", "speech", and "toggler" are set up to point to the
appropriate device routines.

     "Voicetone" and "voicevol" control the pitch and volume of the
three voices.  "Noisetone" and "noisevol" control the noise output.
"Speech" plays one byte of speech data.  "Toggler" is used to simulate
three voices with one channel -- only used in the PC speaker.

     Note that if a device combination cannot handle some feature (for
example, the PC speaker cannot handle noise), the routine "nullhandler"
is assigned to the corresponding routine pointer, which does nothing.
This completely eliminates device-checking in the sound and speech
units.  SPEECH.ASM doesn't have to, say, check that a Sound Blaster or
PC speaker is in use before outputting a speech byte -- it simply calls
"speech".  (Again, I'm patting myself on the back because I finally
implemented this in v6.0.)


อออออออออออออออออออออออออออออฺฤฤฤฤฤฤฤฤฤฤฤฤฟอออออออออออออออออออออออออออออ
                             ณ PC SPEAKER ณ
อออออออออออออออออออออออออออออภฤฤฤฤฤฤฤฤฤฤฤฤูอออออออออออออออออออออออออออออ

     The PC speaker code is actually pretty complex, since it is called
upon to emulate three voices using a single speaker channel.  The way
that this is done is by rapidly arpeggiating through any voices that are
currently playing.  The 1/60 timer tick in INT.ASM calls the routine
"pctoggle" which performs this duty.  (Truthfully, INT.ASM calls a
generic "toggler" routine depending on which device is being used; but
only the PC speaker does anything with it.)

     Special care is taken to not switch the voice if only one voice is
playing, since this would result in clicking in a supposedly continuous
tone.


อออออออออออออออออออออออออออออออฺฤฤฤฤฤฤฤฟออออออออออออออออออออออออออออออออ
                               ณ ADLIB ณ
อออออออออออออออออออออออออออออออภฤฤฤฤฤฤฤูออออออออออออออออออออออออออออออออ

     The Adlib, or FM chips, exist in Adlib cards and Sound Blasters and
compatibles.  They are only used for three-voice emulation.  Due to the
sine-wave nature of the chips, a perfect square-wave instrument can't
exist, but I've hopefully come as close as possible.

     Uh, that's basically it for Adlib.


อออออออออออออออออออออออออออฺฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤฟออออออออออออออออออออออออออออ
                           ณ SOUND BLASTER ณ
อออออออออออออออออออออออออออภฤฤฤฤฤฤฤฤฤฤฤฤฤฤฤูออออออออออออออออออออออออออออ

     The Sound Blaster handles noise and speech.  The V9t9 documentation
on sound goes into great detail on how noise is emulated.  As for
speech, it is played one byte at a time 8000 or 10000 times a second.

ออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออ

	\


	include	standard.h
	include	tiemul.h
	include	int.h
	include	speech.h
	include	special.h		; dosprint
	include	support.h
	include	record.h
	include	log.h
	include	sound.h

	include	memory.inc

	INCLUDE	pcspeak.inc
	INCLUDE	adlib.inc
	INCLUDE	sblaster.inc


	.data

soundcard	db	pcspeakermask	; what do we have?
playsound	db	pcspeakermask	; which to use to make noise?
silence		db	0		; silence?

failreason	dw	0

asnstrc	struc
	avtone	dw	?
	avvol	dw	?
	antone	dw	?
	anvol	dw	?
	aspch	dw	?
	avtgle	dw	?		; voice toggler for PC speaker
	ends


nullspeech  	spchstruc <nullhandler,nullint,nullhandler,nullhandler>


;	Device Assignments -- what devices control what aspects
;	of sound.
;
;	This is HIGHLY dependent on the values of the *mask constants
;	in STRUCS.INC!
;
devassn	asnstrc	<nullhandler,nullhandler,nullhandler,nullhandler,nullspeech,nulltoggle>			; no sound (0)
					; pcspeaker  (1)
	asnstrc	<pcspeakertone,pcspeakervol,nullhandler,nullhandler,pcspeech,pctoggle>
					; adlib (2)
	asnstrc <adlibtone,adlibvol,adlibnoise,adlibnoisevol,nullspeech,nulltoggle>
					; pcspeaker+adlib  (3)
	asnstrc <adlibtone,adlibvol,adlibnoise,adlibnoisevol,pcspeech,nulltoggle>
					; sblaster (4)
	asnstrc	<nullhandler,nullhandler,nullhandler,nullhandler,sbspeech,nulltoggle>
					; sblaster+pcspeaker (5)
	asnstrc	<pcspeakertone,pcspeakervol,nullhandler,nullhandler,sbspeech,pctoggle>
					; adlibmask+sblastermask (6)
	asnstrc	<adlibtone,adlibvol,adlibnoise,adlibnoisevol,sbspeech,nulltoggle>
					; adlibmask+sblastermask+pcspeaker (7)
	asnstrc	<adlibtone,adlibvol,adlibnoise,adlibnoisevol,sbspeech,nulltoggle>
					; sbdma (8)
	asnstrc <nullhandler,nullhandler,sbnoisetone,sbnoisevol,sbspeech,nulltoggle>
					; sbdma+pcspeaker  (9)
	asnstrc	<pcspeakertone,pcspeakervol,sbnoisetone,sbnoisevol,sbspeech,pctoggle>
					; sbdma+adlib (10)
	asnstrc <adlibtone,adlibvol,sbnoisetone,sbnoisevol,sbspeech,nulltoggle>
					; sbdma+pcspeaker+adlib  (11)
	asnstrc <adlibtone,adlibvol,sbnoisetone,sbnoisevol,sbspeech,nulltoggle>
					; sbdma+sblaster (12)
	asnstrc	<nullhandler,nullhandler,sbnoisetone,sbnoisevol,sbspeech,nulltoggle>
					; sbdma+sblaster+pcspeaker (13)
	asnstrc	<pcspeakertone,pcspeakervol,sbnoisetone,sbnoisevol,sbspeech,pctoggle>
					; sbdma+adlibmask+sblastermask (14)
	asnstrc	<adlibtone,adlibvol,sbnoisetone,sbnoisevol,sbspeech,nulltoggle>
					; sbdma+adlibmask+sblastermask+pcspeaker (15)
	asnstrc	<adlibtone,adlibvol,sbnoisetone,sbnoisevol,sbspeech,nulltoggle>


voicetone	dw	nullhandler	; handlers for outputting stuff
voicevol	dw	nullhandler
noisetone	dw	nullhandler
noisevol	dw	nullhandler

speech		dw	nullspeech	; ptr to spchstruc

toggler		dw	nulltoggle	; toggle voices in PC speaker


voice1		voiceinfo <>
voice2		voiceinfo <>
voice3		voiceinfo <>
voicen		voiceinfo <>


noisehertz	dw	6976,3488,1744,0
noiseperiod	dw	16,32,64,0

curnoisehertz	dw	0
curnoisetype	db	0

cvoice		db	0		; current voice
	even

	.code

;===========================================================================
;	SOUND:	Pre-config init.
;
;	þ  Probe supported sound cards/devices.
;===========================================================================

	.data

spci_msg db	'Probing sound devices...',0dh,0ah,'$'

	.code

sound_preconfiginit proc near
	lea	dx,spci_msg
	call	dosprint

	call	findsound	
	ret
	endp


;	FINDSOUND --	Find the sound cards we can use
;
;	Changes	SOUNDCARD & PLAYSOUND
;
;	Called ONCE at the beginning of the program
;	*before* V9t9.CNF is read
;
findsound proc	near
	mov	soundcard,pcspeakermask
	mov	failreason,0
	
	call	detect_adlib
	or	al,al
	jz	fsout			; no Adlib => no SB

	or	soundcard,adlibmask

	lea	di,sbport
	call	detect_sb
	or	al,al
	jz	fsout

	or	soundcard,sblastermask

	or	soundcard,sbdmamask	; assume it's available

fsout:
	mov	al,soundcard
	mov	playsound,al
	ret
	endp



;===========================================================================
;	SOUND:	Post-config init.
;
;	þ  Initialize sound cards/devices.
;	þ  Get CT-VOICE.DRV if SB DMA to be active.
;===========================================================================

sound_postconfiginit proc near
	call	initsound  
	ret
	endp


;	INITSOUND --	Initialize the sound cards ONCE
;
;	Get memory, drivers, and such.
;	SOUNDCARD has the masks for sound methods available
;
;	Called ONCE after V9t9.CNF is read
;	
;	V9t9.CNF can cause SB DMA to *not* be used.
;
initsound proc	near
	mov	al,playsound			; selected devices
	and	al,sbdmamask			; wanted SB DMA
	xor	al,sbdmamask			; exclude <--> include
	not	al				; not!

	and	soundcard,al			; elim SB DMA if not selected

	mov	al,soundcard

	test	al,pcspeakermask
	jz	isnopc

	call	initpcspeaker

isnopc:
	test	al,adlibmask
	jz	isnoadlib

	call	initadlib

isnoadlib:
	test	al,sblastermask
	jz	isnosb

	call	initsb

isnosb:
	test	al,sbdmamask
	jz	isnosbdma

;;	call	initsbdma

isnosbdma:	
	ret
	endp



;==========================================================================
;	SOUND:	Restart.
;
;	þ  Get SB DMA memory, if necessary.
;	þ  Updates the devices.
;	þ  Starts sound.
;
;==========================================================================

sound_restart proc near
	push	ax
	call	getsbdma
	jc	srerr
	call	restartsound			; does all the stuff
	clc
srerr:
	pop	ax
	ret
	endp


;==========================================================================
;	SOUND:	Restop.
;
;	þ  Turns off sound devices.
;	þ  Lose SB DMA memory.
;
;==========================================================================

sound_restop proc near
	call	clearsound
	call	losesbdma
	clc
	ret
	endp


;==========================================================================
;	SOUND:	Shutdown.
;
;	þ  Turns off sound devices.
;
;==========================================================================

sound_shutdown proc near
	call	cleansound
	clc
	ret
	endp



nullint	proc	far
	push	ax
	mov	al,20h
	out	20h,al
	pop	ax
	iret
	endp


;============================================================================


;------------------------------------
;	RESTARTSOUND -- Restart sound
;------------------------------------

restartsound proc	near
	call	updatedevices
	call	startsound
	ret
	endp


;	UPDATEDEVICES 	Update pointers that HANDLESOUND
;			uses to set sounds
;
;	Called sorta often when PLAYSOUND changes
;
updatedevices proc near
	push	ax
	push	bx
	push	dx

	mov	al,0
	cmp	silence,0
	jnz	udenter

	mov	al,playsound		; Now set the pointers
	or	al,al
	jnz	udenter
	mov	al,pcspeakermask

udenter:
	and	al,pcspeakermask+sblastermask+adlibmask+sbdmamask
	xor	ah,ah
	mov	bx,size asnstrc
	mul	bx
	lea	bx,devassn
	add	bx,ax

	mov	ax,[bx].avtone
	mov	voicetone,ax
	mov	ax,[bx].avvol
	mov	voicevol,ax
	mov	ax,[bx].antone
	mov	noisetone,ax
	mov	ax,[bx].anvol
	mov	noisevol,ax
	mov	ax,[bx].aspch
	mov	speech,ax
	mov	ax,[bx].avtgle
	mov	toggler,ax

udrestart:

	pop	dx
	pop	bx
	pop	ax
	ret
	endp

;========================================================================
;	FINDSOUND -- at start of program, finds devices, sets bits in
;		     SOUNDCARD.
;
;	INITSOUND -- according to what's in SOUNDCARD, initialize sound
;		     devices, get SB DMA driver
;
;	UPDATEDEVICES-- update pointers used to play the voices/noises/speech	
;		     (here is where we get and free memory for DMA buffers)
;
;	STARTSOUND-- after an interruption, play all the voices
;
;	CLEARSOUND --during the program, halt sound so it doesn't annoy
;		     us while we're doing other things
;
;	CLEANSOUND --shut down devices, lose drivers
;
;=======================================================================




;	STARTSOUND --	Turn on sound
;
;	Called whenever sound has been interrupted.
;
startsound	proc	near
	push	ax
	push	bx

	mov	ah,0
	lea	bx,voice1
ssloop:
	call	voicetone
	call	voicevol
	add	bx,size voiceinfo
       	inc	ah
	cmp	ah,3
	jb	ssloop

	lea	bx,voicen
	call	noisetone
	lea	bx,voicen
	call	noisevol

	pop	bx
	pop	ax
	ret
	endp




;	CLEARSOUND --	Turn off sound completely
;
;	Called when interrupting sound.
;
;
clearsound	proc	near
	push	ax

	mov	al,playsound

	test	al,pcspeakermask
	jz	csnopc
	call	clearpcspeaker
csnopc:
	test	al,adlibmask
	jz	csnoadlib
	call	clearadlib

csnoadlib:
	test	al,sblastermask
	jz	csnosb

csnosb:
	test	al,sbdmamask
	jz	csnosbdma

	call	clearsbdma			

csnosbdma:
	pop	ax
	ret
	endp




;	CLEANSOUND --	Clean up sound (END OF PROGRAM)
;
;
cleansound proc	near
	push	ax

	mov	al,soundcard		; First turn off all devices

	test	al,pcspeakermask
	jz	cnsnopc
	call	cleanpcspeaker

cnsnopc:
	test	al,adlibmask
	jz	cnsnoadlib
	call	cleanadlib

cnsnoadlib:
	test	al,sblastermask
	jz	cnsnosb
	call	cleansb

cnsnosb:
	test	al,sbdmamask
	jz	cnsnosbdma

;;	call	cleansbdma

cnsnosbdma:
	
	pop	ax
	ret
	endp


;	=====================
;	HANDLESOUND PROCEDURE
;	=====================

;	AL = byte sent to port	
;
handlesound	proc	near
	push	ax
	push	bx
	push	cx
	push	dx

IFDEF	DEMO
	test	stateflag,demoing
	jz	hsnowrite
	call	dsounddata
hsnowrite:
ENDIF

	test	al,80h
	jnz	hsfirst
	jmp	hssecond

hsfirst:
	mov	bl,al			; First sound byte
	and	bl,070h
	shr	bl,3
	xor	bh,bh
	and	ax,15
	jmp	cs:[word ptr hsfirsttable+bx]

hsfirsttable	dw	t1frq,t1att,t2frq,t2att,t3frq,t3att,nctl,natt

;	Frequencies

t1frq:
	lea	bx,voice1
	mov	ah,0
	jmp	tfrq
t2frq:
	lea	bx,voice2
	mov	ah,1
	jmp	tfrq
t3frq:
	lea	bx,voice3
	mov	ah,2
tfrq:
	mov	cx,[bx].period
	and	cx,not 0fh
	or	cl,al
	mov	[bx].period,cx
	mov	cvoice,ah
	jmp	hsgethertz


;	Attenuations

t1att:
	lea	bx,voice1
	mov	ah,0
	jmp	tatt
t2att:
	lea	bx,voice2
	mov	ah,1
	jmp	tatt
t3att:
	lea	bx,voice3
	mov	ah,2
tatt:
	mov	[bx].volume,al
	call	voicevol		; AH=voice #, BX=voice^
	jmp	hsout


;	Noise stuff

nctl:	
	mov	voicen.stype,al
	call	updatenoisevals
	lea	bx,voicen
	mov	ah,3
	call	noisetone
	jmp	hsout

natt:	
	lea	bx,voicen
	mov	[bx].volume,al
	mov	ah,3
	call	noisevol
	jmp	hsout
	

;	Handle second byte
;	AL=whole byte
;
hssecond:
	mov	bl,cvoice
	xor	bh,bh
	push	ax
	mov	ax,size voiceinfo
	mul	bx
	lea	bx,voice1
	add	bx,ax
	pop	ax

	mov	cx,[bx].period
	and	cx,not 03f0h
	shl	ax,4
	and	ax,03f0h
	or	cx,ax
	mov	[bx].period,cx

hsgethertz:
	push	bx
	mov	bx,[bx].period
	mov	ax,0b4f4h
	mov	dx,1	 	
	cmp	bx,1		; 55938 max
	ja	hsinrange
	mov	bx,2	
hsinrange:
	div	bx		; get hertz
	pop	bx
	mov	[bx].hertz,ax	; save hertz

	cmp	cvoice,2
	jne	hsdohertz
	call	updatenoisevals	

hsdohertz:

	mov	ah,cvoice
	call	voicetone	; play.   AH=voice #, BX=voice^

	cmp	cvoice,2
	jne	hsout

	call	updatenoise	; third voice alters noise if noisetype=3

hsout:
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	UPDATENOISE --	If voicen.stype is controlled by voice 3,
;			update noise.
;
;	BX=voice3^
;
updatenoise proc near
	push	ax

	mov	al,voicen.stype
	and	al,3
	cmp	al,3
	jne	unnot

	call	updatenoisevals
	call	noisetone		; replay noise

unnot:
	pop	ax
	ret
	endp




;	UPDATENOISEVALS --	Be sure voicen.hertz & voicen.period are
;				correct
;
updatenoisevals proc	near
	push	ax
	push	bx

	mov	al,voicen.stype
	and	al,3
	mov	bl,al
	xor	bh,bh
	add	bx,bx
	mov	ax,[bx+noiseperiod]
	or	ax,ax
	jnz	unvpnot3
	mov	ax,voice3.period	; voice 3 controls
	mov	voicen.period,ax
	mov	ax,voice3.hertz
	mov	voicen.hertz,ax
	jmp	unvout

unvpnot3:
	mov	voicen.period,ax	; noise type controls
	mov	ax,[bx+noisehertz]
	mov	voicen.hertz,ax

unvout:
	pop	bx
	pop	ax
	ret
	endp


;
;	HANDLERS for tone and volume
;


nullhandler proc	near
	ret
	endp


nulltoggle proc	near
	ret
	endp


	end
