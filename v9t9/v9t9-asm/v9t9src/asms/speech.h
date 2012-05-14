;=================================================
;	SPEECH.H
;
;	Speech header file
;=================================================


IFDEF	_SPEECH_

	.data

	public	sphrase
	public	sdelay

	public	speechexcite
	public	speechpitches
	public	uselpc
;;;;;;	public	speechavail

	public	cmmnd
	public	queueing



;--------------------------------------------------------------------------

	.code

	public	speech_preconfiginit
	public	speech_postconfiginit
	public	speech_restart
	public	speech_restop
	public	speech_shutdown


	public	handlespeechwrite
	public	handlespeechread


	public	startspeech
	public	stopspeech
	public	addspeech
	public	terminatespeech



ELSE

	.data

	extrn	sphrase:byte
	extrn	sdelay:word

	extrn	speechexcite:byte
	extrn	speechpitches:byte
	extrn	uselpc:byte
	extrn	speechavail:byte

	extrn	cmmnd:byte
	extrn	queueing:byte



;--------------------------------------------------------------------------

	.code

	extrn	speech_preconfiginit:near
	extrn	speech_postconfiginit:near
	extrn	speech_restart:near
	extrn	speech_restop:near
	extrn	speech_shutdown:near


	extrn	handlespeechwrite:near
	extrn	handlespeechread:near

	extrn	startspeech:near
	extrn	stopspeech:near
	extrn	addspeech:near
	extrn	terminatespeech:near




ENDIF
