; **************************************************
; SPECIAL.ASM  V9t9 special functions module
; **************************************************
; by Edward Swartz.  6/4/1993
; **************************************************

_SPECIAL_ = 1
_MODULES_ = 1

	include	standard.h
	include	tiemul.h
	include	video.h
	include	int.h
	include	hardware.h
	include	files.h
	include	sound.h
	include	keyboard.h
	include	debug.h
	include	speech.h
	include	support.h
	include	record.h
	include	log.h

	include	special.h

	include	registers.inc
	include	memory.inc


	.data


line	db	' ออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออออ ',0

hlphead	db	'                              V 9 t 9    H E L P',0dh
	db	'$'

	comment	\
wperror	db	0dh,0dh,0dh,0dh,0dh
	db	'	Your program has loaded an invalid address into the workspace pointer ',0dh
	db	'	register.  This would inevitably cause the emulator to freeze up.',0dh
	db	0dh
	db	'	No need to worry, though, ',39,'cause this is only a simulation!',0dh
	db	0dh
	db	'	When you press <esc>, the emulator will abort.',0dh
	DB	'$'

threebeep	db	7,7,7,'$'
	\

error	db	0dh,0dh,0dh,0dh,0dh,0dh
	db	'	V9t9 does not treat IBM function keys like 99/4A Fctn+xx keys.',0dh
	db	'	ALT acts like the 99/4A "FCTN" key.',0dh
	db	0dh
	db	0dh
	db	'	Pressing Ctrl and a function key will select a V9t9 function.',0dh
	db	'	(Ctrl + F1 = Help )',0dh
	db	0dh,0dh,0dh,0dh
	db	'$'      

help	db	0dh,'  Press Ctrl+ or Alt+ :',0dh 
	db	'	F1 = this help screen		Shift+F1 = see current settings',0dh 
	db	'	F2 = less video updates		Shift+F2 = more video updates' ,0dh 
	db	'	F3 = slow down emulator		Shift+F3 = reverse slowdown',0dh 
	db	'	F4 = toggles sound on/off',0dh 
	db	'	F5 = toggle sound sources on/off',0dh 
	db	'	F6 = set joystick emulation',0dh 
	db	'	F7 = center joystick #1		Shift+F7 = center joystick #2',0dh 
	db	'	F8 = select modules',0dh  
	db	'	F9 = shell to DOS		Shift+F9 = select pathnames',0dh 

IFDEF	DEMO
	db	'	F10= start demo recording	Shift+F10= stop demo recording',0dh 
ENDIF
;	db	'	F11= save emulator''s state',0dh

	db	'	F12= force emulator to reset	(F12 by itself kicks V9t9''s butt)',0dh
	db	0dh 
	db	'	Ctrl-Break = will stop V9t9',0dh 
	db	'	Alt-SysRq  = invoke execution tracer',0dh 
	db	0dh 

	db	'$'

help2	db	0dh,'	Fctn-"=" (quit) has been reassigned as Alt+Shift+"="',0dh 
	db	'	to make it difficult to inadvertently reset the "computer."',0dh 
	db	0dh 
	db	'	In cases where Fctn-"=" doesn''t reset, then you can force a reset',0dh 
	db	'	with LeftShift+RightShift+Ctrl+Alt+"=".',0dh 
	db	0dh 
	db	'	The TI function keys are named as follows:',0dh 
	db	0dh 
	db	'     	Fctn-1 = DELETE		Fctn-2 = INSERT		Fctn-3 = ERASE',0dh 
	db	'     	Fctn-4 = CLEAR		Fctn-5 = BEGIN		Fctn-6 = PROC''D',0dh 
	db	'     	Fctn-7 = AID		Fctn-8 = REDO		Fctn-9 = BACK',0dh 
	db	0dh 
	db	'     	Fctn-A = |	Fctn-C = `	Fctn-F = {	Fctn-G = }',0dh 
	db	'     	Fctn-I = ?	Fctn-O = ''	Fctn-P = "	Fctn-R = [',0dh 
	db	'     	Fctn-T = ]	Fctn-U = _	Fctn-W = ~	Fctn-Z = \',0dh 
	db	0dh 
	db	'     	When not using joysticks, player 1 moves with E,S,D,X, fire=Q;',0dh 
	db	'     	and player 2 moves with      	              I,J,K,M, fire=Y.',0dh 
	db	'$'

togglehead db	'                      T O G G L E   S O U N D   D E V I C E S',0dh 
	db	'$'

toggleinfo db	0dh,0dh,0dh,0dh
	db	'	Press one of the letter keys below to toggle a sound device from',0dh
	db	'	being on to off, or off to on.',0dh
	db	0dh
	db	'	Only the devices appearing in the first list can be toggled.'
	db	'$'

togglechoice db	0dh,0dh
	db	'	(P)  	PC Speaker (music, speech)',0dh
	db	'	(A)	Adlib (music)',0dh
	db	'	(S)	Sound Blaster (speech synthesis)',0dh
	db	'	(D)	SB DMA channeling (noise)',0dh
	db	0dh,0dh
	db	'	Press <enter> to accept changes, or <esc> to ignore.',0dh
	db	'$'


joyhead	 db	'                         C E N T E R   J O Y S T I C K S',0dh 
	db	'$'

nojoy1error	db	0dh,0dh ,0dh ,0dh ,0dh ,0dh ,0dh 
	db	'			IBM analog joystick #1 not detected.',0dh 
	db	0dh ,0dh ,0dh ,0dh ,0dh ,0dh ,0dh ,0dh 
	db	'$'

nojoy2error	db	0dh,0dh ,0dh ,0dh ,0dh ,0dh ,0dh 
	db	'			IBM analog joystick #2 not detected.',0dh 
	db	0dh ,0dh ,0dh ,0dh ,0dh ,0dh ,0dh ,0dh 
	db	'$'



joy1downright	db	0dh,0dh,0dh,0dh,0dh,0dh
	db	'			Hold joystick ONE in the lower-right',0dh
	db	'			corner and press the stick button.',0dh,0dh 
	db	'$'
joy2downright	db	0dh,0dh,0dh,0dh,0dh,0dh
	db	'			Hold joystick TWO in the lower-right',0dh
	db	'			corner and press the stick button.',0dh,0dh 
	db	'$'
joy1center	db	0dh,0dh,0dh,0dh 
	db	'			Release joystick ONE to its center',0dh
	db	'			position and press the base button.',0dh ,0dh 
	db	'$'
joy2center	db	0dh,0dh,0dh,0dh 
	db	'			Release joystick TWO to its center',0dh
	db	'			position and press the base button.',0dh ,0dh 
	db	'$'

pathhead db	'                        E N T E R    P A T H N A M E S',0DH 
	db	'$'

	even

pthreal	dw	path1r,pth_file,disk1name
	dw	path2r,pth_file,disk2name
	dw	path3r,pth_file,disk3name
	dw	0

pthemu	dw	path1e,pth_dir,tidiskpathname
	dw	path2e,pth_dir,tidiskpathname+64
	dw	path3e,pth_dir,tidiskpathname+128
	dw	path4e,pth_dir,tidiskpathname+192
	dw	path5e,pth_dir,tidiskpathname+256
	dw	0

pthboth	dw	path1r,pth_file,disk1name
	dw	path2r,pth_file,disk2name
	dw	path3e,pth_dir,tidiskpathname+128
	dw	path4e,pth_dir,tidiskpathname+192
	dw	path5e,pth_dir,tidiskpathname+256
	dw	0

;		 01234567890123456789012345678901234
path1r	db	'	1 : DSK1 (disk filename)   = ',0
path2r	db	'	2 : DSK2 (disk filename)   = ',0
path3r	db	'	3 : DSK3 (disk filename)   = ',0
path1e	db	'	1 : DSK1 (MS-DOS pathname) = ',0
path2e	db	'	2 : DSK2 (MS-DOS pathname) = ',0
path3e	db	'	3 : DSK3 (MS-DOS pathname) = ',0
path4e	db	'	4 : DSK4 (MS-DOS pathname) = ',0
path5e	db	'	5 : DSK5 (MS-DOS pathname) = ',0

pthskip1 db	0dh,0
pthskip2 db	9,9,0
pthnext	db	0dh,0dh,0
pthdist	db	3

pthfoot	db	'	Press the key next to the disk entry you want to change,',0dh
	db	'		     or press <esc> when finished.',0

pthnone	db	0dh,0dh,0dh,0dh
	db	'	There are no Disk DSR ROMs loaded.',0dh,0dh,0dh,0dh
	db	'	Check out the "DSRCombo" variable in V9t9.CNF.',0dh
	db	0


blanks	db	'   ','$'


warnhead db	'                      C A R T R I D G E   S W I T C H',0dh 
	db	'$'

warning	db	0dh ,0dh ,0dh ,0dh ,0dh ,0dh 
	db	'       You are about to switch cartridges -- this will erase the cartridge',0dh 
	db	'       memory space and anything you''re working on now.',0dh 
	db	0dh 
	db	0dh 
	db	'       Are you sure you want to change the current cartridge?',0dh 
	db	0dh ,0dh 
	db	'$'

sethead	db	'                         C U R R E N T   S E T T I N G S',0dh 
	db	'$'

set2a	db	0dh ,0dh ,'	Video update frequency: ','$'
set2b	db	'/60 second','$'
set3a	db	0dh ,0dh ,'	Delay between instructions: ','$'
set3b	db	' cycles','$'
set5a	db	0dh ,0dh ,'	Detected sound sources are ','$'
set5b	db	0dh ,	'	Digitized sound ','$'
set4a	db	0dh ,	'	Sound is ','$'
set4b	db				'playing through ','$'
set6a	db	0dh, 0dh ,'	TI Joystick 1 is ','$'
set7a	db	0dh ,	'	Joystick 1 extremes are ','$'
set6b	db	0dh ,'	TI Joystick 2 is ','$'
set7b	db	0dh ,	'	Joystick 2 extremes are ','$'
set8a	db	0dh,0dh,'$'
set8b	db		'	Emulation is paused.','$'
set8c	db		'	Average instructions per second:  ','$'

set8e	db	0dh,0dh,'	A demonstration is executing.','$'

setcrlf db	0dh,0
seten	db	'$'


pressenteroresc	db	0dh,0dh,0dh
	db	'            Press <enter> for YES or <esc> for NO.',0dh 
	db	'$'


ender	db	0dh 
	db	'                 Please see the file V9t9.TXT for more details.',0dh 
	db	'                              PRESS <ESC> TO EXIT.'
	db	'$'

cender	db	0dh 
	db	'                 Please see the file CONFIG.TXT for more details.',0dh 
	db	'                              PRESS <ESC> TO EXIT.'
	db	'$'


hender	db	0dh
	db	'                Please see the file V9t9.TXT for more details.',0dh 
	db	'                  Press <enter> for more, or <esc> to exit.'
	db	'$'


onebeep	db	7,'$'
twobeep	db	7,7,'$'

;ansi1	db	27,'[0;','$'
;ansi2	db	'1;','$'
;ansi3	db	'3','$'
;ansi4	db	';4','$'

tempstring db	64	dup (?)


	even

;	This is a list of (procedure,variable) pairs which are
; 	used to print out the Ctrl+Shift+F1 diagnostics page.
;	It ain't a simple matter.

setprnt	dw	s_text,set2a,s_byte,videoredrawlatency,s_text,set2b
	dw	s_text,set3a,s_word,delayamount,s_text,set3b
	dw	s_text,set5a,s_sdev,soundcard
	dw	s_text,set5b,s_digi,failreason
	dw	s_text,set4a,s_onoff,playsound
	dw	s_text,set6a,s_jdev,emulatejoystick1
	dw	s_ifge,emulatejoystick1,2,sp_1
	dw	s_text,set7a,S_word,joy1c.xx,s_word,joy1c.yy,s_word,joy1max,s_text,setcrlf
sp_1	dw	s_text,set6b,s_jdev,emulatejoystick2
	dw	s_ifge,emulatejoystick2,2,sp_2
	dw	s_text,set7b,S_word,joy2c.xx,s_word,joy2c.yy,s_word,joy2max,s_text,setcrlf
sp_2	dw	s_and,features,FE_emulating,sp_20,sp_25
sp_20	dw	s_text,set8e,s_goto,sp_5
sp_25	dw	s_text,set8a,s_and,stateflag,paused,sp_4,sp_3
sp_3	dw	s_text,set8b,s_goto,sp_5
sp_4	dw	s_text,set8c,s_div,ips,uptime
sp_5	dw	s_btext,seten
	dw	0

toggleprnt dw	s_text,set5a,s_sdev,soundcard
	dw	s_text,set4a,s_onoff,playsound
	dw	0

joydevprnt dw	s_text,joyset
	dw	s_text,set6a,s_jdev,emulatejoystick1
	dw	s_text,set6b,s_jdev,emulatejoystick2
	dw	0

joydevhead db	'                     J O Y S T I C K    E M U L A T I O N',0dh,0

joyinfo	db	0dh
	db	'	Press "1" to change the device emulating TI joystick #1,',0dh
	db	'	and   "2" to change the device emulating TI joystick #2.',0dh
	db	0dh
	db	'	Press <Enter> to accept changes, or <ESC> to cancel.',0dh
	db	0

joyset	db	0dh,'	Current settings:',0

joyallow db	0dh,0dh
	db	'	Allowed joystick devices are:',0dh
	db	0dh
	db	'	þ  IBM joystick #1',0dh
	db	'	þ  IBM joystick #2',0dh
	db	'	þ  Microsoft-compatible mouse',0dh
	db	'	þ  Numeric keypad',0dh
	db	'	þ  None',0dh
	db	0dh
	db	'	Each TI joystick must have a unique device associated with it.',0dh
	db	0



numtemp	db	'          ','$'
st_off	db	'off','$'
st_pcsound db	'PC speaker, ','$'
st_sblast db	'Sound Blaster, ','$'
st_sbpro db	8,8,' Pro, ','$'
st_adlib db	'Adlib, ','$'
st_sbdma db	8,8,' w/DMA, ','$'
st_bksp	db	8,8,'  $'
;		 1234567890123456
st_joy	db	'PC joystick 1  ','$'
	db	'PC joystick 2  ','$'
	db	'Microsoft mouse','$'
	db	'numeric keypad ','$'
	db	'(nothing)      ','$'

st_diginw db	'excluded in V9t9.CNF','$'
st_digi	db	'is available','$'
	

demohead db	'                   R E C O R D    D E M O N S T R A T I O N',0DH 
	db	'$'

demoprmpt	db	'Enter the filename to name this demonstration, in the form XXXXX.DEM:',0dh
	db	0dh
	db	9,9,9,9,9,9,9,9,9,9
	db	'$'

demostopped db	'			Demonstration recording stopped.',0dh
	db	0dh
	db	'			Flushing buffers...'	
	db	'$'

demowritten db	' done!','$'


cursor	dw	0
cursX	db	0
cursY	db	0
color	db	0

mcursX	db	0
mcursY	db	0				; for READCHOICE
lcursX	db	0
lcursY	db	0

defaultmodule	db	1

modinfoseg	dw	0		; segment for module info

modextension	db	'.HEX',0	; set by V9t9.CNF
moduleslist db	32 dup (0ffh)		; set by V9t9.CNF

nummodulesinlist db	0		; in MODULES.INF
nummodulesselected db	0		; in V9t9.CNF

c_header	db	5fh
c_list		db	1eh
c_prompt	db	71h
c_arrow		db	1fh


modhead	db	' ===                V 9 t 9   M O D U L E   S E L E C T I O N               === '
	db	'$'

modinst	db	0dh
	db	'    Pick one of the following modules by number, or select 0 for no modules:',0dh
	db	0dh
modspace db	'    $'
lines	db	0dh,0dh,'$'
prompt	db	9,9,'     Your choice (ESC=exit)?     ',8,8,8,'$'
mferr1	db	'Could not read the file ',0
mferr2	db	'.',0dh,0ah,'Please be sure it is in the correct directory.',0dh,0ah,0

arrow	db	'ฤฤ>','$'
noarrow	db	'   ','$'

nomoderr db	8,8,8,8,8,8,8,'          ',0


ascmax	db	?
asclen	db	?
ascnum	db	4 dup (?)

selection db	?			; # of module asked for
selected	modrec	<>
tempfname db	13 dup (?)

	even


	.code


;===========================================================================
;	HANDLESPECIALFUNCTIONS:	Pre-config init.
;===========================================================================

special_preconfiginit proc near
	clc
	ret
	endp

;===========================================================================
;	HANDLESPECIALFUNCTIONS:	Post-config init.
;===========================================================================

special_postconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	HANDLESPECIALFUNCTIONS:	Restart.
;===========================================================================

special_restart proc near
	clc
	ret
	endp


;===========================================================================
;	HANDLESPECIALFUNCTIONS:	Restop.
;===========================================================================

special_restop proc near
	clc
	ret
	endp


;===========================================================================
;	HANDLESPECIALFUNCTIONS: Shutdown.
;===========================================================================

special_shutdown proc near
	clc
	ret
	endp



	.data


;	Ctrl+Alt+Fx values....
;
hsftable	dw	k_f1,hsf_help
		dw	k_f2,hsf_video
		dw	k_f3,hsf_speed
		dw	k_f4,hsf_sound
		dw	k_f5,hsf_sounddev
		dw	k_f6,hsf_joyemul
		dw	k_f7,hsf_center
		dw	k_f8,hsf_modules
		dw	k_f9,hsf_dos

IFDEF	DEMO
		dw	k_f10,hsf_demo
ENDIF

		dw	k_f12,hsf_reset


		dw	0



	.code


handlespecialfunctions	proc	near
	pusha
	push	es

	mov	al,shift
	test	al,s_ctrl+s_alt
	jnz	hsffunction	       

	cmp	specialfunctionkey,k_f12
	je	hsfnotfound

	mov	specialfunctionkey,k_f1
	jmp	hsffunction

hsfbadshift:
	call	hsferror		; whoops... invalid use of Fx
	jmp	hsffunctionout

hsffunction:
	lea	di,hsftable		; search for key
	mov	al,specialfunctionkey
	xor	ah,ah
hsffindloop:
	cmp	word ptr [di],0
	je	hsfnotfound
	cmp	[di],ax
	je	hsfffound
	add	di,4
	jmp	hsffindloop

hsfffound:
	mov	ax,[di+2]
	call	ax
	jmp	hsffunctionout		; If c=1, beep with DX

hsfnotfound:
	call	hsferror
	jmp	hsffunctionout



hsffunctionout:
	jc	hsfdobeep
	jz	hsfnobeeps

;	Invert screen (really, FLASH screen)

	push	dx
	call	invertpalette
	mov	cx,10
hsfwait460:
	mov	dx,65535
	and	stateflag,not sixtieth
hsfwait160:
	dec	dx
	jz	hsfbreak
	test	stateflag,sixtieth
	jz	hsfwait160
hsfbreak:
	loop	hsfwait460
	pop	dx

	call	setpalette
	test	stateflag,intdebug
	jz	hsfnobeeps
	call	setdebugbg

	jmp	hsfnobeeps

hsfdobeep:
	call	print

hsfnobeeps:

	and	stateflag,not specialfunctionrequest

	pop	es	
	popa

	ret
	endp



hsferror proc	near
	mov	dx,offset onebeep
	call	print

	call	emustop

	cmp	specialfunctionkey,k_F12
	je	heskip

	mov	al,04fh
	call	clearscreen

	call	printline
	mov	dx,offset hlphead
	call	print
	call	printline
	mov	dx,offset error
	call	print
	mov	dx,offset ender
	call	print	

	call	waituntilesc

heskip:
	call	emustart
	clc
	ret
	endp


;============================================
;	Ctrl+ or Alt+ Fx functions
;
;===========================================


hsf_help	proc	near
	call	emustop

	mov	al,01fh
	call	clearscreen

	test	shift,s_shift
	jnz	hsfhset

	call	printline
	lea	dx,hlphead
	call	print
	call	printline
	lea	dx,help
	call	print
	mov	bx,1600h
	call	gotoxy
	lea	dx,hender
	call	print	

hsfhout:
	call	waituntilenteroresc
	jc	hsfhrout

	mov	al,01fh
	call	clearscreen

	call	printline
	lea	dx,hlphead
	call	print
	call	printline
	lea	dx,help2
	call	print
	lea	dx,ender
	call	print
	call	waituntilesc

hsfhrout:
	call	emustart

	clc
	ret


hsfhset:
	call	printline
	lea	dx,sethead
	call	print
	call	printline
	lea	bx,setprnt
hsfhset1:
	mov	ax,[bx]
	or	ax,ax
	jz	hsfhsetd
	mov	si,[bx+2]
	call	ax
	add	bx,4
	jmp	hsfhset1
hsfhsetd:
	lea	dx,ender
	call	print
	call	waituntilesc
	call	emustart
	clc
	ret
	endp


s_text	proc	near
	mov	dx,si
	call	print
	ret
	endp

;	Write a byte
;
s_byte	proc	near
	mov	al,[si]
	xor	ah,ah
	xor	dx,dx
	mov	cx,3
	call	s_number
	ret
	endp

;	Write a word
;
s_word	proc	near
	mov	ax,[si]
	xor	dx,dx
	mov	cx,5
	call	s_number
	ret
	endp

;	Generic ascii number printer
;
;	DX:AX=number, CX=# digits 
;
s_number proc	near
	push	bx
	mov	si,2020h
	lea	bx,numtemp
	mov	[bx],si
	mov	[bx+2],si
	mov	[bx+4],si
	mov	[bx+6],si
	mov	[bx+8],si
	lea	bx,numtemp+9
	mov	si,10

	IFDEF	T386

	.386
	shl	edx,16
	and	eax,0ffffh
	add	eax,edx
	mov	esi,10
s_num0:
	xor	edx,edx
	div	esi
	add	dl,'0'
	mov	[bx],dl
	dec	bx
	or	eax,eax
	jz	s_numo
	loop	s_num0
	.286

	ELSE

s_num1:		 	
	xor	dx,dx
	div	si
	add	dl,'0'
	mov	[bx],dl
	dec	bx
	or	ax,ax
	jz	s_numo
	loop	s_num1

	ENDIF

s_numo:
	pop	bx
	lea	dx,numtemp
	call	print
	ret
	endp


;	If sound is off, say so, else print devices
;
;
s_onoff proc	near
	mov	al,silence
	or	al,al
	jnz	s_on0

	mov	al,[si]			; playsound
	test	al,pcspeakermask+sblastermask+adlibmask+sbdmamask
	jnz	s_on2
	mov	al,pcspeakermask
	jmp	s_on2

s_on0:
	lea	dx,st_off		; "off"
	call	print
	jmp	s_onout

s_on2:
	lea	dx,set4b
	call	print			; "playing through"
	call	s_sdevent		; xxx devices

s_onout:
	ret
	endp


;	Print the sound devices
;
s_sdev	proc	near
	mov	al,[si]
s_sdevent:
	test	al,pcspeakermask
	jz	s_sdev00
	lea	dx,st_pcsound
	call	print
s_sdev00:
	test	al,adlibmask
	jz	s_sdev01
	lea	dx,st_adlib
	call	print
s_sdev01:
	test	al,sblastermask
	jz	s_sdev02
	lea	dx,st_sblast
	call	print
	test	al,sbpromask
	jz	s_sdev02
	lea	dx,st_sbpro
	call	print			; unused and unnecessary
s_sdev02:
	test	al,sbdmamask
	jz	s_sdev03
	test	al,sblastermask
	jnz	s_sdev021
	lea	dx,st_sblast
	call	print
s_sdev021:
	lea	dx,st_sbdma
	call	print
s_sdev03:
	lea	dx,st_bksp
	call	print
	ret
	endp


;	Print a joystick device
;
s_jdev	proc	near
	mov	al,[si]
	xor	ah,ah
	shl	ax,4
	lea	dx,st_joy
	add	dx,ax
	call	print
	ret
	endp


;	Print status of digitized stuff
;
s_digi	proc	near
	test	soundcard,sbdmamask
	jnz	s_digih
	lea	dx,st_diginw
	jmp	s_digip
s_digih:
	lea	dx,st_digi
s_digip:
	call	print
	ret
	endp



;	If val at [si] >= val at [bx+4], skip to [bx+6]
;
;	(Used to skip joystick information in the printing list)
;
s_ifge	proc	near
	xor	ax,ax
	mov	al,[si]
	cmp	ax,[bx+4]
	jb	s_ifl
	mov	bx,[bx+6]
	sub	bx,4			; calling routine increments
	jmp	s_ifout
s_ifl:
	add	bx,4
s_ifout:
	ret
	endp

	
;	If/then with AND.  
;	If [si]&&[bx+4]==[bx+4] then go to [bx+6] else to [bx+8]
;	
s_and	proc	near
	mov	ax,[si]
 	and	ax,[bx+4]
	cmp	ax,[bx+4]
	mov	ax,[bx+6]
	jnz	s_anddo
	mov	ax,[bx+8]
s_anddo:
	mov	bx,ax
	sub	bx,4
	ret
	endp


;	Print a quotient.
;	4[si]/4[bx+2]
;
s_div	proc	near
	push	cx
	push	dx
	push	si

	IFDEF	T386

	.386
	mov	eax,[si]		; ips
	xor	edx,edx
	mov	si,[bx+4]		; uptime
	mov	ecx,60
	mul	ecx
	div	dword ptr [si]
	mov	edx,eax
	shr	edx,16
	and	eax,65535
	.286

	ELSE

s_divlow:				; assume it's way slow
	push	bx
	mov	ax,[si]			; ips
	mov	dx,[si+2]
	mov	si,[bx+4]
	mov	cx,[si]			; uptime
	mov	bx,[si+2]
s_divshft:
	or	bx,bx			; get it so dx:ax / cx is accurate
	jz	s_divdiv
	shr	cx,1
	shr	bx,1
	jnc	s_divsh0
	or	cx,8000h
s_divsh0:
	shr	ax,1
	shr	dx,1
	jnc	s_divshft
	or	ax,8000h
	jmp	s_divshft
	
s_divdiv:
	pop	bx
	div	cx
	push	bx
	mov	bx,60
	mul	bx
	pop	bx
	
	ENDIF

s_divgo:
	mov	cx,9
	call	s_number
	add	bx,2		; skip arg

s_divo:
	pop	si
	pop	dx
	pop	cx
	ret
	endp


;	Go to another label.
;
s_goto	proc	near
	mov	bx,[bx+2]
	sub	bx,4
	ret
	endp


;	Print text at bottom of screen
;
s_btext	proc	near
	push	bx
	mov	bh,22
	mov	bl,0
	call	gotoxy
	pop	bx
	jmp	s_text
	endp


;===================================================================

;	Handle video update speed
;
hsf_video	proc	near
	mov	al,videoredrawlatency
    
	test	shift,s_shift		
	jnz	hsfvmore

	cmp	al,63			
	jae	hsfverr

	inc	al
	jmp	hsfvset

hsfvmore:
	cmp	al,1			; shift+ctrl+alt+f2 = more screen updates
	jbe	hsfverr

	dec	al

hsfvset:
	mov	videoredrawlatency,al
	shl	al,2
	mov	videoredrawmaxdelay,al
	mov	videodatachanges,0
	mov	vdprestcounter,0
	clc
	jmp	hsfvout

hsfverr:
	stc
	lea	dx,onebeep
	jmp	hsfvbadout
hsfvout:
	or	al,1
hsfvbadout:
	ret
	endp


;===============================================================

;	Toggle sound
;
hsf_sound	proc	near
	not	silence
	call	sound_restop 		; this may prove dangerous later
	call	sound_restart
	or	al,1
	clc
	ret
	endp


;=================================================================

;	Toggle SOUND SOURCES
;
hsf_sounddev	proc	near
	push	cx
	push	bx

	call	emustop

	mov	cl,playsound			; save initial state

hsfsdloop:
	mov	al,030h
	call	clearscreen

	call	printline
	lea	dx,togglehead
	call	print
	call	printline

	lea	dx,toggleinfo
	call	print

	lea	bx,toggleprnt			; print current status
						; of sound devices, etc.
hsfsdset1:
	mov	ax,[bx]
	or	ax,ax
	jz	hsfsdsetd
	mov	si,[bx+2]
	push	cx				; save old playsound
	call	ax
	pop	cx
	add	bx,4
	jmp	hsfsdset1

hsfsdsetd:
	lea	dx,togglechoice
	call	print

	mov	ah,10h
	int	16h			; get enhanced key

	cmp	al,27
	je	hsfsdignore		; ignore

	cmp	al,13
	je	hsfsdsetit		; keep changes

	and	al,not 32
	mov	ah,pcspeakermask
	cmp	al,'P'
	je	hsfsdtoggle

	mov	ah,sblastermask
	cmp	al,'S'
	je	hsfsdtoggle

	mov	ah,sbdmamask
	cmp	al,'D'
	je	hsfsdtoggle

	mov	ah,adlibmask
	cmp	al,'A'
	je	hsfsdtoggle
	jmp	hsfsdloop

hsfsdtoggle:
	mov	al,playsound
	and	ah,soundcard
	xor	al,ah

	mov	playsound,al
	jmp	hsfsdloop
	
hsfsdignore:
	mov	playsound,cl			; restore old value

hsfsdsetit:
	call	emustart			; calls updatedevices

	xor	ax,ax
	clc
	pop	bx
	pop	cx
	ret

	endp


;=====================================================================

;	Toggle JOYSTICK EMULATION.
;
hsf_joyemul proc near
	push	cx
	push	bx
	push	si

	call	emustop

	mov	ch,emulatejoystick1		; save initial state
	mov	cl,emulatejoystick2

hsfjeloop:
	mov	al,05fh
	call	clearscreen

	call	printline
	lea	dx,joydevhead
	call	print
	call	printline

	lea	bx,joydevprnt			; print current status
						; of sound devices, etc.
hsfjeset1:
	mov	ax,[bx]
	or	ax,ax
	jz	hsfjesetd
	mov	si,[bx+2]
	push	cx				; save old playsound
	call	ax
	pop	cx
	add	bx,4
	jmp	hsfjeset1

hsfjesetd:
	lea	dx,joyallow
	call	print

	lea	dx,joyinfo
	call	print

	mov	ah,10h
	int	16h			; get enhanced key

	cmp	al,27
	je	hsfjeignore		; ignore

	cmp	al,13
	je	hsfjesetit		; keep changes

	lea	bx,emulatejoystick1	; to change
	lea	si,emulatejoystick2	; to compare

	cmp	al,'1'
	je	hsfjetoggle1
	cmp	al,'2'
	jne	hsfjeloop

	xchg	bx,si

hsfjetoggle1:
	mov	al,[bx]
	inc	al			; next dev
	cmp	al,4			; last dev
	jbe	hsfjetogglenoov
	xor	al,al

hsfjetogglenoov:
	mov	[bx],al			; save
	cmp	al,[si]			; compare
	je	hsfjetoggle1
	jmp	hsfjeloop

hsfjeignore:
	mov	emulatejoystick1,ch
	mov	emulatejoystick2,cl

hsfjesetit:


	call	emustart 

	xor	ax,ax
	clc
	pop	si
	pop	bx
	pop	cx
	ret

	endp


;================================================================


;	Center joysticks
;
hsf_center	proc	near
	call	emustop

	mov	al,1fh
	call	clearscreen

	call	printline
	lea	dx,joyhead		; print header
	call	print
	call	printline

	test	shift,s_shift
	jnz	hsf_center2		; shift means joystick #2

hsf_center1:
	test	isjoystick,1		; joystick #1 here?
	lea	dx,nojoy1error
	jz	hsf_nojoyerror		; nope

	lea	dx,joy1center		; test center
	call	print
	mov	al,1
	call	savejoystickcenter		

	lea	dx,joy1downright	; test extreme
	call	print
	mov	al,1
	call	savejoystickmax


	jmp	hsf_joydone


;	Center joystick #2
;
hsf_center2:
	test	isjoystick,2
	lea	dx,nojoy2error
	jz	hsf_nojoyerror

	lea	dx,joy2center		; test center
	call	print
	mov	al,2
	call	savejoystickcenter

	lea	dx,joy2downright	; test extreme
	call	print
	mov	al,2
	call	savejoystickmax


	jmp	hsf_joydone

hsf_nojoyerror:
	push	dx

	lea	dx,onebeep
	call	print

	pop	dx

	call	print

	lea	dx,ender
	call	print
	call	waituntilesc

hsf_joydone:
	call	emustart

	clc
	ret
	endp


;===================================================================

;	Switch modules
;
hsf_modules	proc	near
	test	features,FE_emulating
	stc
	jz	hsf_noway
	call	getmodule
hsf_noway:
	ret
	endp

;==================================================================

;	Set emulator slowdown
;
hsf_speed	proc	near
	mov	ax,5
	mov	bx,delayamount
	test	shift,s_shift
	jz	hsfsnotfast
	neg	ax
hsfsnotfast:
	add	bx,ax
	cmp	bx,0
	jge	hsfsok

	stc
	lea	dx,onebeep
	mov	delayamount,0
	jmp	hsfsbadout

hsfsok:
	mov	delayamount,bx
	clc
hsfsout:
	or	al,1
hsfsbadout:
	pushf
	and	stateflag,not delaying		; if delay=0, then
	cmp	delayamount,0			; nothing is done about it.
	jz	hsfsnot				; otherwise, the DELAYING
	or	stateflag,delaying		; bit in stateflag is set
hsfsnot:					
	popf
	ret
	endp


;=========================================================================

;	Dos shell or pathname changing
;

	.data

pb	dw	0,offset cmdtail,seg cmdtail
	dw	offset fcb1,seg fcb1,offset fcb2,seg fcb2
COMSPEC	db	'COMSPEC='
comspecpath	db	64 dup (0)
cmdtail	db	' ',0dh,0
	even
fcb1	db	10h dup (0)
fcb2	db	10h dup (0)

retdos	db	'Entering DOS shell...  type EXIT to return to V9t9.',0dh,0ah,'$'
	.code

hsf_dos	proc	near
	test	shift,s_shift
	jz	hsfisdos

	call	enterpathnames
	xor	al,al
	ret


;	Shell to DOS

hsfisdos:

;	Find COMSPEC.

	push	es
	mov	es,psp
	mov	ax,es:[2ch]
	mov	es,ax
	xor	di,di

;	Check vars to point to COMSPEC.

hsf9compname:
	lea	si,COMSPEC
	mov	cx,8
hsf9cnl:
	cld
	repe	cmpsb
	jcxz	hsffound

;	Else, search for 0

hsfget0:
	mov	al,es:[di]
	inc	di
	or	al,al
	jne	hsfget0
	cmp	es:[di],al		; end of block?
	jne	hsf9compname
	pop	es
	jmp	hsffailed

hsffound:
	lea	si,comspecpath
hsftransfer:
	mov	al,es:[di]
	mov	[si],al
	inc	si
	inc	di
	or	al,al
	jne	hsftransfer


;
;	Check that we have at least 64k free.
;

	mov	ah,48h
	mov	bx,100h
	int	21h			; try to allocate 64k
	jc	hsfdosfailed

	mov	es,ax
	mov	ah,49h
	int	21h   			; we have it?  so release it
	clc
	jmp	hsfdosokay

hsfdosfailed:
	stc
hsfdosokay:

	pop	es

	jc	hsfdosskip

	call	emustop
	call	resetctrlc

	lea	dx,retdos
	mov	ah,9
	int	21h

	mov	ah,4bh
	mov	al,0
	push	ds
	pop	es
	lea	bx,pb
	lea	dx,comspecpath
	int	21h

	call	installctrlc
	call	emustart
hsffailed:
hsfdosskip:
	xor	al,al
	ret
	endp


;-----------------------------------------------------------

;	Wait until user presses <esc>
;
waituntilesc	proc	near
	push	ax
wuloop:
	mov	ah,10h			; read char from enhanced kbd
	int	16h
	cmp	al,27
	jne	wuloop
	pop	ax
	ret
	endp


;	Wait until user presses <esc> or <enter>
;
waituntilenteroresc proc near
	push	ax
wuoenloop:
	mov	ah,10h
	int	16h			; read char from enhanced kbd
	cmp	al,27
	stc
	je	wuoenout
	cmp	al,13
	jne	wuoenloop
	clc
wuoenout:
	pop	ax
	ret
	endp

;----------------------------------------------------------

enterpathnames	proc	near
	push	ax
	push	bx
	push	dx


	call	emustop
	mov	al,1fh
	call	clearscreen


;	Now, throw up the header

	call	printline
	lea	dx,pathhead
	call	print
	call	printline

epnredraw:
	mov	al,1fh
	call	setcolor

	mov	bx,0600h
	call	gotoxy

	mov	ax,features
	and	ax,FE_realdisk+FE_emudisk	; only consider disks now
	jnz	epnone				; no DSRS?

	lea	dx,pthnone
	call	print
	mov	bx,1600h
	call	gotoxy
	lea	dx,cender
	call	print
	call	waituntilesc

	jmp	epnout

epnone:
	lea	bx,pthreal
	cmp	ax,FE_realdisk
	je	epngo

	lea	bx,pthemu
	cmp	ax,FE_emudisk
	je	epngo

	lea	bx,pthboth

epngo:
	mov	si,bx			; save ptr to pointers

epnprint:
	mov	bx,si
	mov	ah,'1'			; high-bounded acceptable key
epnprintloop:
	mov	dx,[bx]
	or	dx,dx
	jz	epninput

	call	print			; print "F1 : DSK1 = "
	lea	dx,pthskip1
	call	print			; go to next line, two tabs
	call	clearline
	lea	dx,pthskip2
	call	print
	mov	dx,[bx+4]
	call	print			; print path or disk
	lea	dx,pthnext		
	call	print			; next line

	inc	ah
	add	bx,6			; point to next entry
	jmp	epnprintloop

epninput:
	mov	bx,1600h
	call	gotoxy
	lea	dx,pthfoot
	call	print

epninputwait:
	push	bx			; need to save AH.
	mov	bh,ah			; save it in BH
	mov	ah,10h
	int	16h  
	mov	ah,bh			; restore AH
	pop	bx			; restore BX

	cmp	al,27			; exit?
	je	epnout

	cmp	al,ah			; key too high?
	jae	epninputwait
	cmp	al,'1'			; too low?
	jb	epninputwait

	push	ax			; save key

	mov	bx,si			; point to beginning of list again
	sub	al,'1'
	mov	dl,6
	mul	dl
	add	bx,ax			; point to proper entry in list

	mov	cx,[bx+2]		; get routine to verify
	mov	dx,[bx+4]		; get ptr to string to edit

	pop	ax			; restore key

	mov	bh,al
	sub	bh,'1'
	mov	al,pthdist
	mul	bh
	mov	bh,al
	add	bh,6+1			; get row
	mov	bl,16			; get column

epninputloop:
	call	gotoxy			; move to BX
	call	input			; edit string in DX
	jc	epnskip			; don't keep changes
	call	cx			; verify string in DX
	jc	epnskip

	call	replace			; replace string

epnskip:
	jmp	epnredraw		; edit some more or exit


epnout:
	call	emustart

	pop	dx
	pop	bx
	pop	ax
	ret
	endp


;=======================================================================

;	Edit a string.
;
;	Copy string at DX into a buffer, edit it.
;	Max length 63 chars with 0 ender.
;
;	Assumes GOTOXY was used to "prime" the cursor.
;
;	BH/BL=cursor position
;	DX=string.
;
input	proc	near
	pusha
	push	es

	mov	si,dx
	lea	di,tempstring
	xor	cx,cx				; current length of string
inputcopy:
	mov	al,[si]				; copy string to temp buffer
	or	al,al
	jz	inputprint
	inc	cx
	cmp	cx,63
	jae	inputprint
	mov	[di],al
	inc	si
	inc	di
	jmp	inputcopy

inputprint:
	xor	al,al
	mov	[di],al				; EOS

	mov	ax,0b800h
	mov	es,ax
	mov	di,cursor			; point to start
	mov	si,cx				; start at end
	add	cursX,cl
	add	di,cx
	add	di,cx
	jmp    	inputpos

inputloop:
	call	checkkey
	jc	inputloop

	cmp	ah,75
	jne	input00
	jmp	inputleft
input00:
	cmp	ah,77
	jne	input01
	jmp	inputright
input01:
;	cmp	ah,82				; not written
;	jne	input02
;	jmp	inputinsert
input02:
;	cmp	ah,83				; not written
;	jne	input03
;	jmp	insertdelete
input03:
	or	al,al
	je	inputloop			; don't recognize others

	cmp	al,8
	je	inputbackspace
	cmp	al,13
	je	inputdone
	cmp	al,27
	je	inputignore
	
	cmp	si,63
	je	inputloop			; not too many chars

	mov	es:[di],al			; store char
	mov	[tempstring+si],al
	add	di,2
	inc	cursX
	inc	si	 
	cmp	si,cx
	jb	inputpos	  		; end of string?
	inc	cx				; yup...
	xor	al,al
	mov	[tempstring+si],al
inputpos:
	call	positioncursor
	jmp	inputloop

inputdone:
	clc
	jmp	inputout

inputignore:
	stc
	jmp	inputout

inputbackspace:					; do a backspace
	cmp	si,0
	je	inputbkspdone

	cmp	si,cx				; moving back or erasing?
	jb	inputleft			; moving back
	
	dec	cx				; erasing
	dec	si
	sub	di,2
	dec	cursX

	mov	al,20h				; blank out
	mov	es:[di],al
	xor	al,al
	mov	[tempstring+si],al
inputbkspdone:
	jmp	inputpos


inputleft:
	cmp	si,0
	je	inputbkspdone
	dec	si
	sub	di,2
	dec	cursX
	jmp	inputbkspdone

inputright:
	cmp	si,cx
	jae	inputbkspdone

	inc	si
	add	di,2
	inc	cursX
	jmp	inputbkspdone


inputout:
	pop	es
	popa
	ret
	endp

;--------------------------------------------------------------

;	Replace the string at DX with that at TEMPSTRING.
;	Uppercase it.
;
replace proc	near
	push	ax
	push	si
	push	di

	lea	si,tempstring
	mov	di,dx
replaceloop:
	mov	al,[si]
	cmp	al,'a'
	jb	replaceup
	cmp	al,'z'
	ja	replaceup
	sub	al,20h
replaceup:
	mov	[di],al
	or	al,al
	jz	replaceout
	inc	si
	inc	di
	jmp	replaceloop

replaceout:
	pop	di
	pop	si
	pop	ax
	ret
	endp


;---------------------------------------------------------------

;	Verify routine for string.
;	Make sure tempstring is a valid filename.
;
pth_file proc	near
	push	ax
	push	si
	push	di

	lea	si,tempstring
	mov	al,[si]
	or	al,al				; no string?
	jz	p_feill

	xor	di,di
	call	pth_filename
	jmp	p_feout

p_feill:
	stc
p_feout:
	pop	di
	pop	si
	pop	ax
	ret
	endp


;---------------------------------------------------------------------

;	Common subroutine to PTH_FILE and PTH_DIR.
;
;	DI=0 means filename only, DI<>0 means directory.
;	
;	Returns DI=0 means EOS.
;
pth_filename proc near
	push	ax
	push	bx
	push	cx

	xor	cx,cx				; reset # chars counter
	xor	bx,bx				; period counter
p_f0:
	mov	al,[si]				; get a char
	inc	si				; point to next
	or	al,al				; end of string?
	jz	p_fgood				
	or	di,di				; directory or filename?
	jz	p_f00				; filename
      	cmp	al,'\'				; backslash?
	je	p_fgood2      			; yup... exit

p_f00:
	or	bx,bx				; did we already get '.'?
	jnz	p_f1

	cmp	al,'.'				; nope... is this it?
	je	p_f2

	inc	cx
	cmp	cx,9				; more than 8 chars in base?
	jae	p_fill
	jmp	p_f0


p_f2:
	jcxz	p_fill				; no chars at all?  Baad...
	mov	bx,cx				; got first half
	xor	cx,cx				; clear after-period counter
	jmp	p_f0				

p_f1:
	cmp	al,'.'				; two periods?
	je	p_fill				; no way!
	cmp	cx,4				; more than 3 chars in ext?
	jae	p_fill				; baaad...
	jmp	p_f0

p_fgood2:
	cmp	byte ptr [si],0			; last '\'?
	jne	p_fgood3
p_fgood:
	xor	di,di				; return of 0 means EOS
p_fgood3:
	clc
	jmp	p_fout
p_fill:
	stc
p_fout:
	pop	cx
	pop	bx
	pop	ax
	ret
	endp

;------------------------------------------------------------------

;	Verify routine for string.
;	Make sure tempstring is a valid pathname.
;
pth_dir	proc	near
	push	ax
	push	bx
	push	cx
	push	si

	lea	si,tempstring
	lodsb
	and	al,not 20h
	cmp	al,'A'
	jb	p_dill
	cmp	al,'Z'
	ja	p_dill
	lodsb
	cmp	al,':'
	jne	p_dill

	mov	cx,si
p_dloop:
	mov	di,1
	call	pth_filename
	jc	p_dill
	or	di,di
	jz	p_dgood

	inc	cx
	cmp	cx,offset tempstring+3
	je	p_d2

	cmp	cx,si
	je	p_dill				; we have "\\" here!
p_d2:
	mov	cx,si
	jmp	p_dloop

p_dgood:
	mov	al,[si-1]
	mov	ah,'\'
	cmp	al,ah
	je	p_dslashed
	mov	[si-1],ah
	sub	al,al
	mov	[si],al
p_dslashed:
	clc
	jmp	p_dout
p_dill:
	stc
p_dout:
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp

;------------------------------------------------------------

; 	Pick a module from list
;	
;
getmodule	proc	near
	pusha
	push	es

	call	emustop
	mov	al,07h
	call	clearscreen


;	Print warning message

		       
	call	printline
	lea	dx,warnhead
	call	print
	call	printline
	lea	dx,warning
	call	print
	lea	dx,pressenteroresc
	call	print

gmcheck:
	mov	ah,10h
	int	16h			; get enhanced key

	cmp	al,13
	je	gmdoit
	cmp	al,27
	je	gmabort
	jmp	gmcheck


;	Clear out GPL and CPU Cart ROM

gmdoit:
	mov	ax,gplseg
	mov	es,ax
	xor	ax,ax
	mov	di,6000h
	mov	cx,0a000h/2
	rep	stosw


	call	pickamodule
	jnc	gmnoerr
	or	stateflag,ctrlbreakpressed
gmnoerr:
	or	stateflag,reboot
gmabort:
	call	emustart
	jnc	gmdnoerr
	
	call	die			; terminates program

gmdnoerr:
	xor	ax,ax
	shr	ax,1			; set Z=C=0

	pop	es
	popa
	ret
	endp


;=================================================================

IFDEF	DEMO

;
;	HSF_DEMO --	Start demo recording
;
hsf_demo proc	near
	test	features,FE_demo
	lea	dx,twobeep
	stc
	jnz	hsfdtest
	jmp	hsfdskip

hsfdtest:
	test	features,FE_emulating
	stc
	jnz	hsfdtostop
	jmp	hsfdskip

hsfdtostop:
	test	shift,s_shift
	jz	hsfdisstarting			; shift means stop demo
	test	stateflag,demoing
	jnz	hsfdtext			; was it recording?

	jmp	hsfderr				; nope

hsfdisstarting:
	test	stateflag,demoing		; stop demoing, make sure
	jz	hsfdtext			; we can

	jmp	hsfderr

hsfdtext:
	call	emustop				; stop the emulator

	mov	al,2fh
	call	clearscreen

	call	printline			; say what's going on
	lea	dx,demohead
	call	print
	call	printline

	test	shift,s_shift			; shift+F10? (start/stop)
	jz	hsfdstart

	mov	bx,0c00h			; stopping
	call	gotoxy
	lea	dx,demostopped
	call	print			

	call	demostop			; stop it, flush

	lea	dx,demowritten			; done!
	call	print

	mov	bx,1600h			; wait for user to wake up
	call	gotoxy
	lea	dx,ender
	call	print
	call	waituntilesc

	pushf
	call	emustart
	popf

	clc
	jmp	hsfdout

hsfdstart:
	test	stateflag,demoing		; start demoing, make
	jnz	hsfderr				 ; sure we can


hsfdretry:
	mov	bh,12
	mov	bl,8
	call	gotoxy
	lea	dx,demoprmpt
	call	print			; ask for demo filename

	mov	bh,14
	mov	bl,30
	call	gotoxy
	lea	dx,givendemo		; old demo name
	call	print
	mov	bh,14
	mov	bl,30
	call	gotoxy
	lea	dx,givendemo
	call	input	   		; enter it at tempstring
	jnc	hsfddoit	       	; ignore

	clc
	jmp	hsfdredraw

hsfddoit:
	xor	di,di
	lea	si,tempstring
	call	pth_filename		; verify it's a filename
	jc	hsfdretry		; illegal; retry
	or	di,di
	jnz	hsfdretry		; uh, pathname?

	lea	dx,givendemo
	call	replace			; set up demo filename in MDEMO

	call	demostart

hsfdredraw:
	pushf
	call	emustart
	popf

	jc	hsfderr
	jmp	hsfdout
hsfderr:
	or	al,1
	lea	dx,onebeep
	stc
	jmp	hsfdskip
hsfdout:
	clc

hsfdskip:
	ret
	endp

ENDIF

;==================================================================

;	Reset emulator.
;
;

hsf_reset proc	near
	or	stateflag,reboot
	or	al,1
	clc
	ret
	endp



;***************************************************************************

;	dosprint
;
;	DX=message, terminated with '$'
;
dosprint	proc	near
	push	ax
	mov	ah,9
	int	21h
	pop	ax
	ret
	endp


;================================================================
;	I think the below routine is used widely.  It provides
;	fast-fast-fast colorized text printing.
;



;	Print to screen using current colors.
;
;	DX==>string
;
print	proc	near
	push	ax
	push	bx
	push	dx
	push	si
	push	di
	push	es

	mov	ax,0b800h
	mov	es,ax
	mov	si,dx

print00:
	mov	bh,cursY
	mov	bl,cursX
	call	gotoxy				; move cursor

	mov	di,cursor
	mov	ah,color
print0:
	lodsb
	or	al,al				; EOS
	jz	print9		
	cmp	al,'$'				; EOS
	je	print9

	cmp	al,0dh				; CR
	je	print1
	
	cmp	al,7				; *beep*
	je	print2

	cmp	al,8				; backspace
	je	print3

	cmp	al,9
	je	print4				; tab

	stosw					; store color and char

	inc	cursX
	mov	al,cursX
	cmp	al,80
	jb	print0
	mov	cursX,0
	inc	cursY
	mov	al,cursY
	cmp	al,25
	jb	print0
	mov	cursY,0
	jmp	print0

print1:
	mov	al,cursY			; do a CR
	inc	al
	cmp	al,25
	jb	print11
	xor	al,al
print11:
	mov	cursY,al
	mov	cursX,0
	jmp	print00

print2:
	mov	dl,al
	mov	ah,2
	int	21h				; rely on DOS to beep
	mov	pclasthertz,0
	jmp	print0

print3:
	sub	di,2				; backspace
	mov	al,20h
	mov	es:[di],al
	dec	cursX
	jmp	print0

print4:						
	mov	dl,cursX			; tab
	mov	al,20h
print40:
	stosw					; put space
	inc	dl
	test	dl,7
	jnz	print40
	mov	cursX,dl
	jmp	print00

print9:
	mov	cursor,di			; done

	pop	es
	pop	di
	pop	si
	pop	dx
	pop	bx
	pop	ax
	ret
	endp


;	Print one char.
;
;	DL=char
;
printchar proc	near
	push	dx
	mov	tempstring,dl
	xor	dl,dl
	mov	tempstring+1,dl
	lea	dx,tempstring
	call	print
	pop	dx
	ret
	endp



;	Move the cursor to the current cursor place.
;
;
positioncursor proc near
	push	ax
	push	bx
	push	dx
	mov	dh,cursY
	mov	dl,cursX
	mov	bh,0
	mov	ah,2
	int	10h
	pop	dx
	pop	bx
	pop	ax
	ret
	endp


;	Go to a specific X,Y location.
;
;	BH=row, BL=col  (0-23, 0-79)	
;
gotoxy	proc	near
	push	ax
	push	bx
	push	dx

	mov	cursY,bh
	mov	cursX,bl

	call	positioncursor

	mov	al,160				; bytes/char/row
	mul	bh
	xor	bh,bh
	add	bx,bx				; make cols-->bytes/col
	add	ax,bx
	mov	cursor,ax

	pop	dx
	pop	bx
	pop	ax
	ret
	endp


;	Set the current color to something.
;
;	AL=color (background*16+foreground)
;
setcolor proc	near
	mov	color,al
	ret
	endp


;	Clear the current line
;
;
clearline proc near
	push	ax
	push	bx
	push	cx
	push	di
	push	es

	mov	ax,0b800h
	mov	es,ax

	mov	bh,cursY
	mov	bl,0
	call	gotoxy

	mov	di,cursor
	mov	cx,80
	mov	ah,color
	mov	al,20h
	rep	stosw

	pop	es
	pop	di
	pop	cx
	pop	bx
	pop	ax
	ret
	endp




;	Clear the screen to a specific color.
;
;	AL = color
;
clearscreen proc near
	push	ax
	push	cx
	push	di
	push	es

	call	setcolor

	mov	ax,0b800h
	mov	es,ax
	xor	di,di
	mov	cx,160*25/2
	mov	ah,color
	mov	al,20h
	rep	stosw
	
	mov	cursor,0
	mov	cursX,0
	mov	cursY,0

	pop	es
	pop	di
	pop	cx
	pop	ax
	ret
	endp



;	Check if key has been pressed
;
;	CY=1 means no key, else AX=get
;
checkkey proc	near
	mov	ah,11h
	int	16h
	stc		
	jz	ckout

	mov	ah,10h
	int	16h
	clc

ckout:
	ret
	endp


printline proc	near
	push	dx
	lea	dx,line
	call	print
	pop	dx
	ret
	endp


;======================================================================
;	PICKAMODULE --	Pick a module from a list defined
;			in MODULES.INF and V9t9.CNF
;
;	MODINFOSEG contains MODREC records for all the modules.
;
;	Expects normal DOS keyboard handler to be loaded.
;
;=======================================================================

pickamodule proc near

	cld
	mov	es,moduleseg
	mov	di,0
	mov	cx,16384/2
	xor	ax,ax
	rep	stosw			; clear out module memory

	mov	ms_cartmem,0		; no segment there

	mov	ax,83h
	int	10h

	mov	al,c_list
	call	clearscreen

	mov	al,c_header		; print headers
	call	setcolor
	call	printline
	lea	dx,modhead
	call	print
	call	printline
	
	mov	al,c_list
	call	setcolor
	lea	dx,modinst
	call	print

	mov	al,cursX
	sub	al,4
	mov	mcursX,al
	mov	al,cursY
	mov	mcursY,al		; save coords!

pamprintlist:

	mov	ch,1			; current module #
	mov	es,modinfoseg
	lea	bx,moduleslist
pamprintloop:
	mov	al,[bx]
	cmp	al,0ffh
	je	pamprintdone

	push	ax			; print number of module
	mov	al,ch
	call	printnum
	call	printparen
	pop	ax

	inc	bx
	xor	ah,ah
	mov	dx,size modrec
	mul	dx
	mov	si,ax			; point to title

	mov	cl,32
pamprintname:
	mov	dl,es:[si]
	call	printchar		; print title
	inc	si
	dec	cl
	jg	pamprintname

	lea	dx,modspace
	call	print

	inc	ch
	jmp	pamprintloop

pamprintdone:
	call	readchoice
	jc	pamnone

pamnone:
	pushf
	mov	al,7
	call	clearscreen

	popf
	ret
	endp


;-------------------------------------------------------

;	Prints the number in AL in ASCII to the screen.
;
printnum	proc	near
	push	dx
	push	ax
	push	bx
	xor	ah,ah
	mov	bl,10
	div	bl			; AL=tens, AH=ones
	add	al,30h
	add	ah,30h
	mov	dx,ax
	cmp	dl,30h
	jne	pnnotzero
	mov	dl,20h
pnnotzero:
	call	printchar
	mov	dl,dh
	call	printchar
	pop	bx
	pop	ax
	pop	dx
	ret
	endp


printparen proc	near
	push	dx
	mov	dl,')'
	call	printchar
	mov	dl,' '
	call	printchar
	pop	dx
	ret
	endp


;----------------------------------------------------------

;	READCHOICE --	Read the user's choice.
;
;
readchoice proc	near
	push	ax
	push	bx
	push	cx
	push	dx
	push	bp

rcloop:

	mov	bh,24
	mov	bl,0
	call	gotoxy

	mov	al,c_prompt
	call	setcolor
	call	clearline
	lea	dx,prompt
	call	print

	call	getnumber

rcgotnum:
	mov	selection,0ffh		; none
	mov	selected.memtype,0
	jc	rcnone

	or	al,al
	je	rcbasic
	cmp	al,nummodulesselected
	ja	rcloop
	dec	al
	xor	ah,ah
	mov	bx,ax
	mov	al,moduleslist[bx]
	mov	selection,al
rcbasic:
	clc
   	jmp	rcout
rcnone:
	lea	dx,nomoderr
	call	setuperror
	or	stateflag,happymessage
	stc
rcout:
	pop	bp
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;---------------------------------------------------------------------

;	Get a module number by allowing the user to either 
;	enter a number or use the arrow keys to look through
;	the list.
;
;	Return AL=number, or CY=1 if ESC pressed.
;
getnumber proc	near
	push	bx
	push	cx

	xor	cx,cx				; # digits entered
	mov	bl,defaultmodule		; get default
	xor	bh,bh

gndraw:
	call	updateselection

gnloop:
	call	checkkey
	jc	gnloop

	cmp	ah,75
	je	gnleft
	cmp	ah,77
	je	gnright
	cmp	ah,72
	je	gnup
	cmp	ah,80
	je	gndown

	cmp	al,9
	je	gntoggle
	cmp	al,8
	je	gnclear
	cmp	al,32
	je	gnclear
	cmp	al,13
	je	gndone
	cmp	al,27
	je	gnnone

	cmp	al,'0'
	jb	gnloop
	cmp	al,'9'
	ja	gnloop

	mov	bh,al				; got a digit
	sub	bh,'0'				; make BH=value
	xor	ax,ax
	jcxz	gnfirst				; first digit, = BH

	mov	al,10
	mul	bl   				; AX=BX*10
gnfirst:
	shr	bx,8				
	add	bx,ax				; new module #
	inc	cx				; one more digit received
	jmp	gndraw

gnforget:
	jmp	gnloop

gnleft:
	test	bx,1
	jnz	gnforget
	dec	bx				; left side
	jmp	gndraw

gnright:
	test	bx,1
	jz	gnforget
	inc	bx				; right side
	jmp	gndraw

gnup:
	sub	bx,2
	jmp	gndraw

gndown:
	add	bx,2
	jmp	gndraw

gntoggle:
	test	bx,1
	jz	gnleft
	jmp	gnright

gnclear:
	xor	bx,bx
	xor	cx,cx
	jmp	gndraw

gndone:
	clc
	jmp	gnout
gnnone:
	stc
gnout:
	mov	ax,bx
	pop	cx
	pop	bx
	ret
	endp


;----------------------------------------------------------------------

;	UPDATESELECTION -- 	Move the arrow to the proper place
;				and redraw the number.
;
updateselection proc near
	push	ax
	push	dx
	push	di

	mov	al,c_arrow
	call	setcolor

	mov	di,bx
	mov	bh,lcursY
	mov	bl,lcursX
	mov	al,bl
	or	al,bh
	jz	usnoprev

	call	gotoxy
	lea	dx,noarrow
	call	print

usnoprev:
	mov	bx,di
	cmp	bx,0
	jl	uslower
	cmp	bx,255
	ja	ushigher
	cmp	bl,nummodulesselected
	ja	ushigher
	jmp	usfine

uslower:
	xor	bx,bx
	xor	cx,cx
	jmp	usfine
ushigher:
	mov	bl,nummodulesselected
	xor	bh,bh
	xor	cx,cx
usfine:
	mov	di,bx				; save current #
	dec	bx
	jl	usnoarrow
	shr	bx,1
	mov	bh,mcursX
	jnc	usnoright
	add	bh,40
usnoright:
	add	bl,mcursY
	xchg	bh,bl
	mov	lcursX,bl
	mov	lcursY,bh
	call	gotoxy
	lea	dx,arrow
	call	print
	jmp	usnum

usnoarrow:
	mov	lcursX,0
	mov	lcursY,0
	   
usnum:
	mov	al,c_prompt
	call	setcolor
	mov	bx,1830h
	call	gotoxy
	mov	ax,di
	call	printnum

	mov	bx,di
	jmp	usout

usout:
	pop	di
	pop	dx
	pop	ax
	ret
	endp


;----------------------------------------------------------------

;	loadparts --	Read the parts of the module, 
;			as specified in "selection"
;
loadparts proc	near
	pusha
	push	es

	mov	bl,selection
	cmp	bl,0ffh			; 0ffh = BASIC
	jne	rpreal
	jmp	rpout

rpreal:
	xor	bh,bh
	mov	ax,size modrec
	mul	bx
	mov	si,ax
	push	ds
	push	ds
	mov	ds,modinfoseg		
	lea	di,selected
	pop	es
	mov	cx,size modrec
	rep	movsb
	pop	ds			; selected == record for module

	mov	al,selected.memtype
	test	al,mod_banked
	jz	rpnobanked

	mov	dx,'C'
	mov	cx,2000h
	mov	es,moduleseg
	call	readoneROM
	jc	rpbad

	mov	dx,'D'
	mov	cx,2000h
	mov	ax,moduleseg
	add	ax,2000h/16
	mov	es,ax
	call	readoneROM
	jc	rpbad
	jmp	rpcheckgpl

rpnobanked:
	mov	al,selected.memtype
	test	al,mod_rom
	jz	rpcheckgpl

	mov	dx,'C'
	mov	cx,2000h
	mov	es,moduleseg
	call	readoneROM
	jc	rpbad
	mov	ax,2000h/16
	add	ax,moduleseg
	call	readoneROM
	jc	rpbad

rpcheckgpl:
	mov	al,selected.memtype
	test	al,mod_grom
	jz	rpcopy

	mov	dx,'G'
	mov	cx,-1
	mov	ax,gplseg
	add	ax,6000h/16
	mov	es,ax
	call	readoneROM
	jc	rpbad

rpcopy:
	mov	ax,moduleseg
	add	ax,moduleoffset
	mov	ms_cartmem,ax		

rpgoodout:
	clc
	jmp	rpout
rpbad:
	stc
rpout:
	pop	es
	popa
	ret
	endp


;--------------------------------------------------------------------

;	READONEROM --	Read a ROM segment
;
;	Inputs:		ES=segment, CX=size, DX=extension
;
readonerom proc	near
	pusha

	push	cx

	lea	di,tempfname
	lea	si,selected.basename
	mov	cx,7
rorname:
	mov	al,[si]
	or	al,al
	jz	roraddext
	mov	[di],al
	inc	si
	inc	di
	loop	rorname
roraddext:
	mov	[di],dl
	inc	di

	lea	si,modextension
	mov	cx,5
rorext:
	mov	al,[si]
	mov	[di],al
	inc	si
	inc	di
	loop	rorext

	pop	cx

	lea	si,modulespath
	lea	bx,tempfname
	call	readROM
	
	popa
	ret
	endp



;=========================================================================

	.data

IFDEF	BETA
betamess	db	0dh,0dh
		db	'=============================================================================',0dh
                db      '                                 V9t9 v6.0 BETA',0dh
		db	'=============================================================================',0dh
		db	0dh
		db	0dh
		db	'	This program is a beta version and is NOT to be distributed but to',0dh
		db	'	testers selected by the author.',0dh
		db	0dh
		db	0dh
		db	'	Testers -- please read BETATEST.TXT, V9t9.TXT, and BUGS.TXT in',0dh
		db	'	their entirety before running this program.',0dh
		db	0dh
		db	0dh
		db	'=============================================================================',0dh
		db	'				  DO NOT DISTRIBUTE',0dh
		db	'=============================================================================',0dh
		db	13,13,13,13,13
		db	'		              Press <enter> to start...',0dh
		db	0

ENDIF

	.code

IFDEF	BETA


betago	proc	near
	mov	al,00ch
	call	setcolor
	call	clearscreen
	lea	dx,betamess
	call	print

rpbloop:
	mov	ah,0h
	int	16h
	cmp	al,13
	jne	rpbloop

	mov	al,07h
	call	clearscreen
	ret
	endp
ENDIF








	end
