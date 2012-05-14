;================================================
;	DEBUG.H
;
;	Debug header file
;================================================


IFDEF	_DEBUG_

	.data

	public	debugdelay
	public	debugcnt


;-------------------------------------------------------------------------

	.code

	public	debug_preconfiginit
	public	debug_postconfiginit
	public	debug_restart
	public	debug_restop
	public	debug_shutdown


	public  debug

	public	setdebugfromtext
	public	setdebugfromti
	public	settifromdebug
	public	settextfromdebug
	public	setnormaltext


ELSE


	.data

	extrn	debugdelay:byte
	extrn	debugcnt:byte


;-------------------------------------------------------------------------

	.code

	extrn	debug_preconfiginit:near
	extrn	debug_postconfiginit:near
	extrn	debug_restart:near
	extrn	debug_restop:near
	extrn	debug_shutdown:near

	extrn  debug:near


	extrn	setdebugfromtext:near
	extrn	setdebugfromti:near
	extrn	settifromdebug:near
	extrn	settextfromdebug:near
	extrn	setnormaltext:near




ENDIF
