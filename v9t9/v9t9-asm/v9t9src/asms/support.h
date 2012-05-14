;=========================================
;	SUPPORT.H
;
;	Support header file
;=========================================

IFDEF	_SUPPORT_

	.data

	public	errormessage
	public	configfilename
	public	moduleinfname
	public	filename
	public	startuppath


;---------------------------------------------------------------------------

	.code

	public	support_preconfiginit
	public	support_postconfiginit
	public	support_restart
	public	support_restop
	public	support_shutdown

	public	opensupportfile
	public	readROM
	public	readchunk
	public	setuperror
	public	concatasciiz

	public	readconfigfile
	public	readmodulefile


ELSE

	.data

	extrn	errormessage:byte
	extrn	configfilename:byte
	extrn	moduleinfname:byte
	extrn	filename:byte
	extrn	startuppath:byte



;---------------------------------------------------------------------------

	.code

	extrn	support_preconfiginit:near
	extrn	support_postconfiginit:near
	extrn	support_restart:near
	extrn	support_restop:near
	extrn	support_shutdown:near

	extrn	opensupportfile:near
	extrn	readROM:near
	extrn	readchunk:near
	extrn	setuperror:near
	extrn	concatasciiz:near

	extrn	readconfigfile:near
	extrn	readmodulefile:near



ENDIF
