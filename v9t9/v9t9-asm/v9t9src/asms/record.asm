; ************************************************
; RECORD.ASM  V9t9 state recording routines
; ************************************************
; by Edward Swartz  12/22/1994, 4/11/95
; ************************************************


_RECORD_ = 1

	include	standard.h
	include	tiemul.h
	include	video.h
	include	int.h
	include	sound.h
	include	speech.h
	include	support.h
	include	record.h

	include	demo.inc
	include state.inc

	.code

;===========================================================================
;	RECORD:	Pre-config initialization
;===========================================================================

record_preconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	RECORD:	Post-config initialization
;
;	This is called LAST in the list.
;
;	þ  If we're about to run a demo or we'll record demos, get memory
;===========================================================================

record_postconfiginit proc near

	test	features,FE_demo		; /D option?
	jz	rpociout			; nope.

	call	demoinit			; get memory
rpociout:
	ret
	endp


;===========================================================================
;	RECORD:	Restart.
;
;	All recording restart/restop happens through checkstate.
;===========================================================================

record_restart proc near
	clc
	ret
	endp


;===========================================================================
;	RECORD:	Restop.
;
;	All recording restart/restop happens through checkstate.
;===========================================================================

record_restop proc near
	clc
	ret
	endp


;===========================================================================
;	RECORD:	Shutdown.
;
;	All recording shutdown goes through checkstate.
;===========================================================================

record_shutdown proc near
	clc
	ret
	endp




	end
