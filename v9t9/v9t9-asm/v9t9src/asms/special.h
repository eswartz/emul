;===========================================
;	SPECIAL.H
;
;	Header file
;===========================================

IFDEF	_SPECIAL_

	.data

	public	modinfoseg
	public	nummodulesinlist
	public	nummodulesselected
	public	moduleslist

	public	selected
	public	defaultmodule
	public	modextension


;--------------------------------------------------------------------------

	.code

	public	special_preconfiginit
	public	special_postconfiginit
	public	special_restart
	public	special_restop
	public	special_shutdown

	public	handlespecialfunctions
	public	pickamodule
       	public	print
	public	dosprint

	public	loadparts			; of a module

	IFDEF	BETA
	public	betago
	ENDIF

ELSE

	.data

	extrn	modinfoseg:word
	extrn	nummodulesinlist:byte
	extrn	nummodulesselected:byte
	extrn	moduleslist:byte

	extrn	selected:byte
	extrn	defaultmodule:byte
	extrn	modextension:byte


;--------------------------------------------------------------------------

	.code

	extrn	special_preconfiginit:near
	extrn	special_postconfiginit:near
	extrn	special_restart:near
	extrn	special_restop:near
	extrn	special_shutdown:near


	extrn	handlespecialfunctions:near
	extrn	pickamodule:near
       	extrn	print:near
	extrn	dosprint:near

	extrn	loadparts:near

	IFDEF	BETA
	extrn	betago:near
	ENDIF



ENDIF
