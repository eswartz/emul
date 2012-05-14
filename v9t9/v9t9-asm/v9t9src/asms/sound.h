;==========================================
;	SOUND.H
;
;	Sound header file
;==========================================


IFDEF	_SOUND_

	.data

	public	ctvoicedrv
	public	failreason
	public	sbirq
	public	sbdma
	public	sbstat
	public	sync	
	public	sbport
	public	speech
	public	soundcard
	public	playsound
	public	silence
	public	noisetop
	public	toggler


;
;	PC Speaker variables
;

	public	pclasthertz
	public	pcspeakersilence



	.code

	public	sound_preconfiginit
	public	sound_postconfiginit
	public	sound_restart
	public	sound_restop
	public	sound_shutdown


	public	handlesound

	public	updatedevices			; special
	public	restartsound			; special, toggle silence

	public	setsbvoicevol			; speech


;	PC speaker

	public	pcsoundoff


ELSE

	.data

	extrn	ctvoicedrv:word
	extrn	failreason:byte
	extrn	sbirq:byte
	extrn	sbdma:byte
	extrn	sbstat:word
	extrn	sync:word
	extrn	sbport:word
	extrn	speech:word
	extrn	soundcard:byte
	extrn	playsound:byte
	extrn	silence:byte
	extrn	noisetop:byte
	extrn	toggler:word


;
;	PC Speaker variables
;

	extrn	pclasthertz:word
	extrn	pcspeakersilence:byte



	.code

	extrn	sound_preconfiginit:near
	extrn	sound_postconfiginit:near
	extrn	sound_restart:near
	extrn	sound_restop:near
	extrn	sound_shutdown:near


	extrn	handlesound:near
	extrn	restartsound:near


	extrn	updatedevices:near

	extrn	setsbvoicevol:near


;	PC speaker

	extrn	pcsoundoff:near



ENDIF
