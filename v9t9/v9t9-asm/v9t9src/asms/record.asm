;   record.asm
; 
;   (c) 1991-2012 Edward Swartz
; 
;   This program is free software; you can redistribute it and/or modify
;   it under the terms of the GNU General Public License as published by
;   the Free Software Foundation; either version 2 of the License, or
;   (at your option) any later version.
;  
;   This program is distributed in the hope that it will be useful, but
;   WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;   General Public License for more details.
;  
;   You should have received a copy of the GNU General Public License
;   along with this program; if not, write to the Free Software
;   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
;   02111-1307, USA.
; 


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