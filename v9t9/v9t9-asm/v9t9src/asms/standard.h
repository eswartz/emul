;===========================================
;	STANDARD.H
;
;	Standard defines for all modules
;===========================================

	DOSSEG
	.MODEL  small
	.286

;	The DEMO below means that demonstration recording/playback is
;	enabled.
;
;	LPCSPEECH means that speech data is directly decoded, rather
;	than using a digitized speech file.
;
;	BOTH OF THESE are required now, since things probably depend
;	on demos in an unhealthy manner now, and there is no longer
;	code to handle digitized speech files.
;

        DEMO    = 1                     ; these must ALWAYS be defined
        LPCSPEECH = 1                   ; since leaving them out causes
                                        ; some compile bugs now

	IFDEF	SUPERFAST
		IFDEF	T386
			IFDEF	_TIEMUL_
				SUPER = 1
			ENDIF
		ENDIF
	ENDIF
	


	include	strucs.inc


