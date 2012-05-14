;=========================================
;	INT.H
;
;	Interrupt header file
;=========================================


IFDEF	_INT_

	.data

	public	videodatachanges
	public	debugint

	public	spchcntr

	public	currentfunc

	public	elapsed
	public	timer0interval


;--------------------------------------------------------------------------

	.code


	public	int_preconfiginit
	public	int_postconfiginit
	public	int_restart
	public	int_restop
	public	int_shutdown


	public	installctrlc
	public	resetctrlc

	public	setspeed

	public	setcurrentspeed
	public	nullinterrupter


ELSE
	.data

	extrn	videodatachanges:byte
	extrn	debugint:byte

	extrn	spchcntr:byte

	extrn	currentfunc:word

	extrn	elapsed:word
	extrn	timer0interval:word


;--------------------------------------------------------------------------

	.code

	extrn	int_preconfiginit:near
	extrn	int_postconfiginit:near
	extrn	int_restart:near
	extrn	int_restop:near
	extrn	int_shutdown:near


	extrn	installctrlc:near
	extrn	resetctrlc:near

	extrn	setspeed:near

	extrn	setcurrentspeed:near
	extrn	nullinterrupter:near



ENDIF
