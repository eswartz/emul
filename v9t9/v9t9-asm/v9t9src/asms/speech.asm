; *******************************************
; SPEECH.ASM  V9t9 speech routines
; *******************************************
; by Edward Swartz.  3/27/1994
;		     2/28/1995
;		     6/26/1996	
; *******************************************

_SPEECH_ = 1

	include	standard.h
	include	tiemul.h
	include	int.h
	include	sound.h
	include	special.h		; for DOSPRINT
	include	support.h
	include	record.h
	include	speech.h

	include	memory.inc
	include	lpc.inc

	include	sbspeech.inc
	include pcspeech.inc

	.data


numinterps 	equ	8
speechspeed	equ	8000
speechSBspeed	equ	131		;256-(1000000/speechspeed)
speechnumbytes	equ	speechspeed/50
						
speechbuffsize 	equ	speechnumbytes*25	; MUST be 1/2 second!
						; (4000 bytes, here)


;-----------------------------------------

speechactive	db	0		; DMA is for speech
	even

speechrec	DMAstruc <0,0,0,sbspeechsampleend,0,speechSBspeed,0ffh>	 ; 156
						; SB speaking information

;------------------------------------------

spchbuf	struc
	spchseg		dw	?	; segment for speech data
	spchleng	dw	?	; how far we've written	in
	ends

lpcout0		spchbuf	<0,0>		; for digital output
lpcout1		spchbuf <0,0>

lpcoutbuffer	dw	lpcout0

playinptr	dw	lpcout0
playinbuffer	spchbuf <0,0>		; copy for DMA		


;------------------------------------------

	even

uselpc	db	1			; from V9t9.CNF, used only in init

speechexcite	db	14 dup(0)	; filename for chirp excitation data

	even


IFDEF	DEMO
	include demoequs.inc
ENDIF



	.code


;===========================================================================
;	SPEECH:	Pre-config init.
;===========================================================================

speech_preconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	SPEECH:	Post-config initialization.
;
;	þ  Initialize LPC speech.	
;===========================================================================

speech_postconfiginit proc near
	call	speechinitlpc

	ret
	endp


;===========================================================================
;	SPEECH:	Restart.
;
;	þ  If speech was going, then we need to turn on the
;	speech device again.  (Clearsound should've turned it off.)
;===========================================================================

speech_restart proc near
	mov	LPCstat,0
	and	stateflag,not SPEECHON
	mov	speechstatus,SS_LOW+SS_EMPTY

	lea	si,lpcout0
	mov	lpcoutbuffer,si
	mov	playinptr,si

	clc
	ret
	endp


;===========================================================================
;	SPEECH:	Restop.
;
;	þ  If speech is going, then we need to turn off the
;	speech device.  (Clearsound should turn it off.)
;
;===========================================================================

speech_restop proc near
	clc
	ret
	endp


;===========================================================================
;	SPEECH:	Shutdown.
;
;	DOS frees memory for us, so we won't do it here.
;===========================================================================

speech_shutdown proc near
	clc
	ret
	endp



;	Initialize LPC stuff.
;
;	þ  Allocate buffer for LPC speech.
;	þ  Load patches for "periodic" and "pitchtrans"
;

	.data

sil_msg	db	'Setting up LPC speech...',0dh,0ah,'$'
sil_merr db	'Not enough memory for LPC speech buffers.',0dh,0ah,0
sil_ferr db	'LPC patch file % not found or invalid size.',0dh,0ah,0


	.code

speechinitlpc proc near
	mov	LPCstat,0

	cmp	uselpc,0
	jne	silwant
       	jmp	silout

silwant:
	lea	dx,sil_msg
	call	dosprint

	mov	ah,48h				; get memory for buff 0
	mov	bx,(speechbuffsize+15)/16
	int	21h
	jc	silmerr
	mov	lpcout0.spchseg,ax

	mov	ah,48h				; for buff 1
	mov	bx,(speechbuffsize+15)/16
	int	21h
	jc	silmerr
	mov	lpcout1.spchseg,ax

	mov	ah,48h				; for output
	mov	bx,(speechbuffsize+15)/16
	int	21h
	jc	silmerr
	mov	playinbuffer.spchseg,ax

	or	features,FE_LPCspeech

	lea	si,speechpath
	lea	bx,speechexcite
	cmp	byte ptr [bx],0
	je	silnoexc
	call	opensupportfile
	or	ax,ax
	jz	silferr

	push	ax
	mov	ax,ds
	mov	es,ax
	pop	ax
	lea	dx,periodic
	mov	cx,50*2
	call	readchunk
	mov	bx,ax
	mov	ah,3eh
	int	21h
silnoexc:

	clc
	jmp	silout

silmerr:
	lea	dx,sil_merr
	jmp	silerrout

silferr:
	lea	dx,sil_ferr

silerrout:
	call	setuperror
	mov	uselpc,0
	and	features,not (FE_lpcspeech)
	stc
silout:
	ret
	endp



;===========================================================

	.data

;	Internal information
;

addr	db	3 dup (0)	; only 5 nybbles used
spdata	db	0		; what is read from speech port

speechstatus db 0		; status of speech synthesizer
SS_TALKING	equ 80h
SS_LOW		equ 40h
SS_EMPTY	equ 20h
	

LPCstat	dw	0		; LPC dataword

	.code

;====================================================
;	Handle Speech Synthesizer write
;
;	AL=byte sent
;	
;====================================================

handlespeechwrite	proc	near
	pusha
	push	es

	test	LPCstat,LPC_in_direct	; who's controlling this port?
	jz	hswcommand

	call	swapbits		; LPC decoder is
	call	LPCpushbyte

	jmp	hswout

;----------------------------------------------------

hswcommand:
	mov	ah,al
	and	al,70h

hsw70:
	cmp	al,70h			; terminate speech
	jne	hsw10

	call	LPCterminate

	jmp	hswout

hsw10:
	cmp	al,10h			; ask to read a byte
	jne	hsw30

	mov	al,0
	test	features,FE_speechROM
	jz	hsw100			; nothing to read

	mov	bx,word ptr addr      	; ignore high nybble
	mov	es,speechseg
	mov	al,es:[bx]

hsw100:
	mov	spdata,al
	inc	word ptr addr		; ?!  I guess so

	or	LPCstat,LPC_out_memory	; notify HSRead to return value
	jmp	hswout

hsw30:
	cmp	al,30h			; read and branch
	jne	hsw50

	test	features,FE_speechROM
	jz	hsw300

	mov	bx,word ptr addr
	mov	es,speechseg
	mov	ah,es:[bx]
	mov	al,es:[bx+1]
	and	word ptr addr,0c000h
	and	ax,3fffh
	or	word ptr addr,ax
	jmp	hswout

hsw300:
	mov	addr,0
	jmp	hswout

hsw50:
	cmp	al,50h			; speak a vocabulary word at ADDR
	jne	hsw60

hsw54:
	or	LPCstat,LPC_from_vocab	; need to send bytes to synth
	call	LPCinit
	or	speechstatus,SS_TALKING	; don't need any input

hsw51:
	call	intspeech
	test	speechstatus,SS_TALKING
	jnz	hsw51

hsw52:
	test	LPCstat,LPC_stuffed
	jnz	hsw52

	jmp	hswout

hsw60:
	cmp	al,60h			; speak direct data
	jne	hsw40

	and	LPCstat,not LPC_from_vocab	; comes from writes to this
	or	LPCstat,LPC_in_direct	; reroute those writes
	or	stateflag,speechinterrupt
	call	LPCinit

	jmp	hswout

hsw40:
	mov	al,ah
	and	ah,0f0h
	cmp	ah,40h
	je	hsw400

	jmp	hswout			; unknown command

hsw400:

;	Shift address right and put least-sig nybble at top

	mov	bx,word ptr addr
	mov	cl,byte ptr addr+2

	shr	bx,4
	mov	ch,cl
	shl	ch,4
	or	bh,ch
	and	al,0fh
	mov	cl,al

	mov	word ptr addr,bx
	mov	byte ptr addr+2,cl

hswout:
	pop	es
	popa
	ret
	endp


;--------------------------------------------------------------------
;	SWAPBITS --	Reverse a byte.
;
;	Bytes fed into the speech synthesizer are reversed.
;	Bytes read from the speech synthesizer are NOT.
;
;	In AL.
;--------------------------------------------------------------------

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




;==========================================================
;	Handle speech read
;
;	Return AL=byte read
;
;==========================================================

handlespeechread	proc	near

	mov	al,speechstatus

	test	LPCstat,LPC_out_memory
	jz	hsrout

	mov	al,spdata

hsrout:
	and	LPCstat,not LPC_out_memory
	ret
	endp

;=====================================================================


;-------------------------------------------------------------------
;	LPCinit
;
;	We know for sure that the program wants the speech synthesizer 
;	to speak a phrase.
;
;	This routine will initialize the LPC decoder (used in LPCdecode)
;	so that input data can correctly be decoded into LPC equations.
;	
;	(Does NOT steal output device yet.  This is done by intspeech
;	when the first block of data is ready.  Also, this enables
;	speechsoundintr to tell if it should play more data.)
;	
;------------------------------------------------------------------


LPCinit	proc	near
	push	si
	push	cx


IFDEF	DEMO
	test	stateflag,demoing
	jz	sschnope
	push	ax
	mov	ah,SPEECHSTARTING
	call	dspeechdata
	pop	ax
sschnope:
ENDIF


Li0:
	test	LPCstat,LPC_stuffed
	jnz	Li0


	mov	LPCvocabbits,0

;	The following is not necessary but helps clean up after
;	messy spills.  (I.e., this resets the synthesizer completely
;	when a phrase is spoken, as opposed to the 99/4A synthesizer's
;	habit of storing old speech data after a crash and therefore
;	causing burps on the next phrase.)


	mov	LPCbufind,0			; initialize LPC input
	mov	LPCbufbit,0			; buffer
	mov	LPCbufred,0
	mov	LPCbufsiz,0

	lea	si,ppv				; clear memory
liclear:
	mov	word ptr [si],0	  		; just a general good idea
	add	si,2
	cmp	si,offset y+22
	jb	liclear

	mov	si,lpcoutbuffer
	mov	[si].spchleng,0


	mov	pbf,12				; have some pitch
	mov	env,0				; was silent before
	mov	DECODE,FL_first			; flag first LPC frame


	mov	speechstatus,SS_LOW+SS_EMPTY	; empty

	or	stateflag,SPEECHON		; turn on LPC handler 

	and	LPCstat,not (LPC_timing+LPC_time_flag+LPC_time_passed)

	pop	cx
	pop	si
	ret
	endp


;-------------------------------------------------------------------
;	LPCterminate
;
;	The 99/4A program has terminated the speech.
;	This is also called from LPCfetchbuffer if out of data.
;	
;	(Yes, we will blindly call LPCconstruct, etc, even if we
;	know the buffer is empty.  This is because we call LPCconstruct
;	when we're not waiting for the SB to finish speaking.
;	Therefore, if after 1/2 second of 99/4A activity, it doesn't
;	care to send more data, we know it's really timed out.)
;
;	Returns control to the noise channel.
;
;-------------------------------------------------------------------

LPCterminate proc near

IFDEF	DEMO
	test	stateflag,demoing
	jz	tsnope
	push	ax
	mov	ah,TERMINATINGSPEECH
	call	dspeechdata
	pop	ax
tsnope:
ENDIF

	and	LPCstat,not (LPC_calculating+LPC_in_direct)

	mov	speechstatus,SS_LOW+SS_EMPTY

	test	stateflag,SPEECHON
	jz	Ltm0

	and	stateflag,not SPEECHON

	call	LPCoutput

Ltm0:
	ret
	endp


;--------------------------------------------------------------------
;	LPCover
;
;	The speech phrase has ended ordinarily.
;
;	(When the speaking sample finishes, it'll see "speechon" is
;	off and return control to the noise channel if necessary.)
;
;--------------------------------------------------------------------

LPCover	proc	near
	push	cx
	push	si

IFDEF	DEMO
	test	stateflag,demoing
	jz	spschnope
	push	ax
	mov	ah,SPEECHSTOPPING
	call	dspeechdata
	pop	ax
spschnope:
ENDIF


Lov0:
	test	LPCstat,LPC_stuffed
	jnz	Lov0


	call	LPCoutput			; spit it out!

	and	speechstatus,NOT SS_TALKING

;
;	This is a trick to emulate the immediate calcuations of
;	the TMS5200.  By this time in that chip, all that was
;	decoded would have been spoken, except for the last frame,
;	which is interpolating to zero.  Here we calculate how
;	much time that really is and delay for that long.
;

	and	stateflag,not SPEECHON

	pop	si
	pop	cx
	ret
	endp


;-------------------------------------------------------------------
;	LPCpushbyte
;
;	Feed another byte to the LPC decoder, in AL.
;
;	Called for direct data only.
;
;	Handles "speechstatus" SS_LOW and SS_EMPTY.
;
;	(Vocab data is detected by LPCpullbyte.)
;
;
;	For DEMOS, however, all vocab calls will be translated
;	into calls to this.
;
;-------------------------------------------------------------------

LPCpushbyte proc near
	push	bx

IFDEF	DEMO
	test	stateflag,demoing
	jz	aschnope
     	push	ax
	mov	ah,ADDINGBYTE
	call	dspeechdata
	pop	ax
aschnope:
ENDIF

	mov	bl,LPCbufind
	xor	bh,bh
	mov	[LPCbuffer+bx],al
	inc	bx
	and	bx,LPCBUFSIZEMASK
	mov	LPCbufind,bl			; circular queue
	inc	LPCbufsiz

	or	speechstatus,SS_LOW		; init

	cmp	LPCbufsiz,9
	jb	Lpbstilllow			; and

	and	speechstatus,not SS_LOW		; update
	or	speechstatus,SS_TALKING		; ready to go!

Lpbstilllow:
	and	speechstatus,not SS_EMPTY

	call	dirspeech

	pop	bx
	ret
	endp



;=======================================================================
;
;	If we fill the buffer, and if not LPCstat & LPC_have_speaker,
;	then we'll send the data out to the speaker and set
;	LPC_audioing, since the DMA buffer will be locked.
;
;	Otherwise, the speaker interrupt will play the data itself later.
;	In the meantime, however, we can write into the other buffer
;	while waiting for that to happen --
;	so we set LPC_stuffed to prevent any more calls to LPCconstruct.
;
;	It's safe also if the LPC decoding is damned slow since
;	the speaker interrupt will reset LPC_sound_on if it doesn't
;	have anything else to play.
;
;=======================================================================

LPCoutput proc near
	push	si
	test	LPCstat,LPC_have_speaker
	jnz	Lopgoing

;-------------------------------------------------------
;
;	Speaker is not in use by speech yet.
;
;-------------------------------------------------------

	call	speechallocspeaker		; take the speaker
	mov	si,LPCoutbuffer
	mov	playinptr,si
	call	speechstartsample		; play what's in the buffer
						; and switch buffers
	jmp	Lopout

;-------------------------------------------------------
;
;	Speaker is being used by speech already.
;
;-------------------------------------------------------

Lopgoing:
	test	LPCstat,LPC_audioing		; still going?
	jnz	Lopstuffed

;	technically this shouldn't happen

	mov	si,LPCoutbuffer
	mov	playinptr,si
	call	speechstartsample		; play what's in the buffer
						; and switch buffers
	jmp	Lopout

;-------------------------------------------------------------------------
;
;	Speaker is playing.  We can switch to the other buffer but
;	we mustn't calculate any more.  
;
;-------------------------------------------------------------------------

Lopstuffed:

      	mov	cx,elapsed5
	add	cx,4
LopS0:
	test	LPCstat,LPC_stuffed		; already set
	jz	LopS1
    	cmp	elapsed5,cx
	jb	LopS0

;	call	speechsoundabort
LopS1:
	or	LPCstat,LPC_stuffed		; prevent more calculation
	and	LPCstat,not (LPC_timing+LPC_time_passed)
	or	LPCstat,LPC_time_flag

;	call	LPCswitchbuffers
;	CALL	LPCSWITCHPLAYBUFFERS
		
Lopout:
	pop	si
	ret
	endp
		
	

;===================================================================
;	INTSPEECH
;
;	Called 60 times a second when "speechon" set in stateflag.
;
;	This routine and DirSpeech (below) handle the "between" times
;	during speech construction.  They make sure the output buffers
;	are not overwritten by overeager decoding, and that speech
;	decoding proceeds constantly, and that a timeout hasn't occurred.
;
;====================================================================


intspeech proc near
	push	cx


	test	speechstatus,SS_TALKING
	jz	isout				; yawn

is00:
;	Here, this bit means absolutely that we can start decoding
;	and producing speech data.
;	
;	However, since we use buffers, we may have to delay while the
;	previously calculated sample is playing.
;

IFDEF	DEMO
	test	stateflag,demoing
	jz	ischnope
     	push	ax
	mov	ah,DOINGINTSPEECH
	call	dspeechdata
	pop	ax
ischnope:
ENDIF

	test	LPCstat,LPC_audioing		; are we playing something?
	jz	is02				; not yet, babe

	mov	cx,elapsed5
	add	cx,3				; wait up to 1.5 seconds
is03:
	test	LPCstat,LPC_stuffed		; are we waiting for sample
						; to end just so we can 
						; get on with it?
	jz	is02				; nope
	cmp	elapsed5,cx
	jb	is03

is02:
	call	LPCconstruct			; construct more data

	
isout:
	pop	cx
	ret
	endp




;===================================================================
;	DIRSPEECH
;
;	Called every time "LPCpushbyte" is called.
;
;====================================================================


dirspeech proc near
	push	cx
	test	speechstatus,SS_TALKING
	jz	dsout				; yawn

ds00:

;	Here, this bit means absolutely that we can start decoding
;	and producing speech data.
;	
;	However, since we use buffers, we may have to delay while the
;	previously calculated sample is playing.
;
;	Here, we set LPC_time_flag so that Interrupt60 can tell us
;	when an empty buffer condition (in LPCpull) is fatal or not.
;
;	(In the real TMS5200, data is fed constantly to the synthesizer
;	which immediately decodes and translates it and generates the
;	10000 hz output stream automatically.  In this one-processor
;	computer, however, we must buffer the data.)

	and	LPCstat,not (LPC_time_passed+LPC_timing)

	test	LPCstat,LPC_audioing		; are we playing something?
	jz	ds02				; not yet, babe

	mov	cx,elapsed5
	add	cx,3				; wait up to 1.5 seconds
ds03:
	test	LPCstat,LPC_stuffed		; are we waiting for sample
						; to end just so we can 
						; get on with it?
	jz	ds02				; nope
	cmp	elapsed5,cx
	jb	ds03

ds02:
	test	LPCstat,LPC_time_passed
	jz	dsnottimeout

	call	LPCterminate			; ouch!!!
	jmp	dsout

dsnottimeout:
	call	LPCconstruct			; construct more data
	or	LPCstat,LPC_time_flag

	jmp	dsout

	
dsout:
	pop	cx
	ret
	endp



;======================================================================
;	SpeechAllocSpeaker
;
;	Take the speaker away from anything else using it.
;
;	Sets LPCstat | LPC_have_speaker (even if we won't speak anything)
;
;======================================================================

speechallocspeaker proc near
	test	features,FE_lpcspeech
	jz	sasout				; hmm

	test	playsound,sbdmamask
	jnz	sassb

	test	playsound,pcspeakermask
	jz	sasout

	call	pcspeechabort
	jmp	sasout

;-----------------------------------------

sassb:
	cmp	noiseactive,0
	jz	sasfree

	call	sbdmaabort
	mov	noiseactive,0

sasfree:
	mov	speechactive,1

sasout:
	or	LPCstat,LPC_have_speaker		
	ret
	endp


;=================================================================
;	SpeechFreeSpeaker
;
;	Free the speaker.  Resets LPC_have_speaker.
;
;=================================================================

speechfreespeaker proc near
	test	features,FE_lpcspeech
	jz	sfsout

	test	playsound,sbdmamask
	jnz	sfssb

	test	playsound,pcspeakermask
	jz	sfsout

	call	pcspeechabort
	jmp	sfsout

;---------------------------------------

sfssb:
	call	sbdmaabort			; assume something's going
	mov	speechactive,0

sfsout:
	and	LPCstat,not LPC_have_speaker
	ret
	endp


;================================================================
;	SpeechStartSample
;
;	Play the current buffer of LPC output data.
;
;	If the buffer is empty, fakes an end-of-audio interrupt.
;	
;================================================================

speechstartsample proc near
	push	si

	test	features,FE_lpcspeech
	jz	sssout

	test	playsound,sbdmamask
	jnz	ssssb
	
	test	playsound,pcspeakermask
	jz	sssout

	call	pcspeechplaysample		; play on PC speaker
	call	LPCswitchbuffers
	mov	si,lpcoutbuffer
	mov	[si].spchleng,0			; clear buffer
	jmp	sssout

;----------------------------------------------
	
ssssb:
	cmp	silence,0
	jnz	sssfakeend

	mov	si,lpcoutbuffer
	cmp	[si].spchleng,0			; empty?
	jnz	sssok

sssfakeend:
	or	LPCstat,LPC_audioing		; fake out zero-length write
	call	sbspeechsampleend
	call	LPCswitchplaybuffers
	jmp	sssout

sssok:
	call	sbdmaspeech			; play on SB
	call	LPCswitchbuffers
	mov	si,lpcoutbuffer
	mov	[si].spchleng,0			; clear buffer

sssout:
	pop	si
	ret
	endp


;=====================================================================
;	SpeechIntr
;
;	Speech sample has finished playing.
;	
;	Called from interrupt.  (sbspeechsampleend actually.)
;
;=====================================================================

speechintr proc	near

	test	LPCstat,LPC_have_speaker	; should have
	jz	ssse_err       			; ouch!

	test	LPCstat,LPC_audioing		; should be
	jz	ssse_err       			; ouch!

;
;	Speech has correctly called the routine to play sound
;	and things are still going normally.
;

	test	LPCstat,LPC_stuffed		; ready to go with next?
	jz	ssse_lone

;
;	intspeech is waiting for us to play the next sample so
;	everyone can get on with their lives.
;
	
	and	LPCstat,not LPC_stuffed		; you're free

	call	LPCswitchplaybuffers
	call	speechstartsample
	jmp	ssse_out


ssse_lone:
	and	LPCstat,not LPC_audioing

	test	stateflag,SPEECHON		; still want it?
	jnz	ssse_out

	call	speechsoundabort		; done
	jmp	ssse_out

ssse_err:
	int	3

ssse_out:
	ret
	endp



;======================================================================
;	SpeechSoundAbort
;
;	Done using the speaker.
;
;======================================================================

speechsoundabort proc near
	test	features,FE_lpcspeech
	jz	ssaout

	cmp	silence,0
	jnz	ssaout

	test	playsound,sbdmamask
	jnz	ssasb

	test	playsound,pcspeakermask
	jz	ssaout

	call	pcspeechabort
	jmp	ssaout

;-----------------------------------------------

ssasb:
	call	sbspeechabort

ssaout:
	and	LPCstat,not LPC_have_speaker
	ret
	endp


;
;	Switch LPC output buffers (there are two).
;

lpcswitchbuffers proc near
	push	si

	lea	si,LPCout0
	cmp	LPCoutbuffer,si
	jne	lsbout
       	lea	si,LPCout1
lsbout:
	mov	LPCoutbuffer,si

	pop	si
	ret
	endp


;
;	Switch LPC play pointer (which buffer to play next).
;

lpcswitchplaybuffers proc near
	push	si

	lea	si,LPCout0
	cmp	playinptr,si
	jne	lspbout
       	lea	si,LPCout1
lspbout:
	mov	playinptr,si

	pop	si
	ret
	endp


	end








	
