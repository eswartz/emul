;==========================================
;	RECORD.H
;
;	Recording header file
;==========================================

IFDEF	_RECORD_

	.data

	public	givendemo

	public	demopath

	public	lastvwaddr


;-------------------------------------------------------------------------

	.code

	public	record_preconfiginit
	public	record_postconfiginit
	public	record_restart
	public	record_restop
	public	record_shutdown


	public	demostart		; open a new file
	public	demostop		; dump buffers and close
	public	democlose
	public	rundemo

	public	dvdpreg			; register has changed
	public	dvdpdata		; data has been written
	public	dsounddata		; sound data was written

;	IFDEF	LPCSPEECH
	public	dspeechdata		; speech data was written
;	ENDIF

	public	dtimerint

	public	demodumpspeech


ELSE

	.data

	extrn	givendemo:byte

	extrn	demopath:byte

	extrn	lastvwaddr:word


;-------------------------------------------------------------------------

	.code

	extrn	record_preconfiginit:near
	extrn	record_postconfiginit:near
	extrn	record_restart:near
	extrn	record_restop:near
	extrn	record_shutdown:near


	extrn	demostart:near		; open a new file
	extrn	demostop:near		; dump buffers and close
	extrn	democlose:near
	extrn	rundemo:near

	extrn	dvdpreg:near			; register has changed
	extrn	dvdpdata:near		; data has been written
	extrn	dsounddata:near		; sound data was written

	extrn	dspeechdata:near		; speech data was written

	extrn	dtimerint:near

	extrn	demodumpspeech:near




ENDIF
