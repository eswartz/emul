;============================================
;	KEYBOARD.H
;
;	Keyboard header file
;============================================

IFDEF	_KEYBOARD_

	.data

	public	curscan
	public	shift
	public	tishift
	public	oldled
	public	updateled

	public	specialfunctionkey
	public	fctn4
	public	shift
	public	sysrq

	public	emulatejoystick1
	public	emulatejoystick2

	public	joysticktick

	public	mouseemulationtype

	public	keyscn
	public	keycol
	public	alphalockline
	public	shift

	public	kbcount
	public	kbactive
	public	kbtimer
	public	kbdelay
	public	kbreadallowed

	public	kbcare

	public	ROMkeyboarddelay

	public	keyboardledset


;	JOYSTICK

	public	isjoystick
	public	joy1,joy2
	public	joy1c,joy2c
	public	joy1max,joy2max




;-------------------------------------------------------------------------

	.code

	public	keyboard_preconfiginit
	public	keyboard_postconfiginit
	public	keyboard_restart
	public	keyboard_restop
	public	keyboard_shutdown

	public	keyboard		; interrupt

	public	handlekeyboard

	public	readjoysticks		; from CRU routines

	public	clearkeyboard

	public	kscandelay


;	JOYSTICK

	public	savejoystickcenter	; calculates according to center
	public	savejoystickmax		; calculates according to extremes

	public	readjoystick1		; reads joystick #1
	public	readjoystick2		; reads joystick #2

	
ELSE


	.data

	extrn	curscan:byte
	extrn	shift:byte
	extrn	tishift:byte
	extrn	oldled:byte
	extrn	updateled:byte

	extrn	specialfunctionkey:byte
	extrn	fctn4:byte
	extrn	shift:byte
	extrn	sysrq:byte

	extrn	emulatejoystick1:byte
	extrn	emulatejoystick2:byte

	extrn	joysticktick:byte

	extrn	mouseemulationtype:byte

	extrn	keyscn:byte
	extrn	keycol:byte
	extrn	alphalockline:byte

	extrn	kbcount:byte
	extrn	kbactive:byte
	extrn	kbtimer:byte
	extrn	kbdelay:byte
	extrn	kbreadallowed:byte

	extrn	kbcare:byte

	extrn	ROMkeyboarddelay:word

	extrn	keyboardledset:byte


;	JOYSTICK

	extrn	isjoystick:byte
	extrn	joy1,joy2:byte
	extrn	joy1c,joy2c:byte
	extrn	joy1max,joy2max:byte




;-------------------------------------------------------------------------

	.code

	extrn	keyboard_preconfiginit:near
	extrn	keyboard_postconfiginit:near
	extrn	keyboard_restart:near
	extrn	keyboard_restop:near
	extrn	keyboard_shutdown:near


	extrn	keyboard:near			; keyboard interrupt

	extrn	handlekeyboard:near

	extrn	readjoysticks:near		; from CRU routines

	extrn	clearkeyboard:near

	extrn	kscandelay:near


;	JOYSTICK

	extrn	savejoystickcenter:near	; calculates according to center
	extrn	savejoystickmax:near		; calculates according to extremes

	extrn	readjoystick1:near		; reads joystick #1
	extrn	readjoystick2:near		; reads joystick #2

	


ENDIF
