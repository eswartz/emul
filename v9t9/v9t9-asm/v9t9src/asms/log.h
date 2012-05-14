;==============================================
;	LOG.H
;
;	Logging header file
;==============================================


IFDEF	_LOG_

	.data

	public	logfilename
	public	islogging



;--------------------------------------------------------------------------

	.code

	public	log_preconfiginit
	public	log_postconfiginit
	public	log_restart
	public	log_restop
	public	log_shutdown

	public	startlogging
	public	stoplogging
	public	logout


ELSE

	.data

	extrn	logfilename:byte
	extrn	islogging:byte



;--------------------------------------------------------------------------

	.code

	extrn	log_preconfiginit:near
	extrn	log_postconfiginit:near
	extrn	log_restart:near
	extrn	log_restop:near
	extrn	log_shutdown:near

	extrn	startlogging:near
	extrn	stoplogging:near
	extrn	logout:near


ENDIF
