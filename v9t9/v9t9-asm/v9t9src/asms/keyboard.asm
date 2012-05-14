; ********************************************
; KEYBOARD.ASM  V9t9 keyboard routines
; ********************************************
; by Edward Swartz.  6/3/1993
; ********************************************

_KEYBOARD_ = 1

	include	standard.h
	include	tiemul.h
	include	video.h
	include	special.h			; PATCHES!!
	include	int.h
	include	hardware.h
	include	log.h
	include keyboard.h

	include	registers.inc
	include	memory.inc

	include	joystick.inc


	.data

MAT_FCTN	equ	byte ptr [keyscn+3]
MAT_SHIFT	equ	byte ptr [keyscn+2]
MAT_CTRL	equ	byte ptr [keyscn+1]
MAT_4		equ	byte ptr [keyscn+27]

keyboardledset	db	0
prevscan	db	0
curscan		db	0
lastfake	db	0
up		db	0	
enhanced	db	0

ack		db	0
resend		db	0

kbactive 	db	0			; set to 1 in KEYBOARD
kbcount		db	1
kbreadallowed	db	1		; has the time passed?
kbdelay		db	1		; 1/60's second to wait between repeats
kbtimer		db	0		; timer for kbdelay
kbcare		db	1	  	; do we care to check ultrarepeating?

	even

cbptr		dw	kcbrk		; these lists are used to detect
kcbrk		db	29,70,198	; Ctrl-Break, Pause, and Prtsc.
cbend		equ	$

paptr		dw	kpause
kpause		db	225,69,225,157,197
paend		equ	$

prptr		dw	kprtsc
kprtsc		db	42,55
prend		equ	$



capping		db	0		; is Caps Lock being held down?

fctn4		db	0		; did Fctn-4 get typed?  (unused)

shift		db	0
tishift		db	0		; shift for 99/4A
newshift	db	0
oldled		db	0		; old LED values

specialfunctionkey db	0
emulatejoystick1 db	0		; how to handle joystick 1
emulatejoystick2 db	0		; how to handle joystick 2
					; 0 = joy1, 1 = joy2
					; 2 = mouse
					; 3 = keyboard
ismouse		db	0
mouseemulationtype db	0		; 0=positional, 1=motional

fakefctncount	db	0		; in matrix handling
fakeshiftcount	db	0
callagain	db	0

sysrq		db	0		; did Alt-Sysrq get pressed?

		even
mlx		dw	0
mly		dw	0		; last coords for mouse
mx		dw	0
my		dw	0		; coords of mouse

joysticktick	db	0		; time for next read?  (1/60 s)

mousecenter	joyvals <320,100>

k_ctrl		equ	29		; scancodes
k_alt		equ	56
k_leftshift	equ	42
k_rightshift	equ	54
k_capslock	equ	58
k_sysrq		equ	84


;	This table maps IBM scancodes (1=ESC, 2='1', 3='2', ...)
;	to the 99/4A CRU keyboard matrix.
;
;	The first nybble of the byte tells which row is set (0-7),
;	the second nybble tells which column is set (0-7).
;
;	A value of 255 means there is no representation.
;  
;	Other tables may handle unrepresented keys.  This table
;	only handles keys actually on the 99/4A keyboard.
;
scantable db	255	; esc
	db	255	; `/~
	db	35h	; 1
	db	31h 	; 2
	db	32h   	; 3
	db	33h 	; 4
	db	34h 	; 5
	db	44h	; 6
	db	43h	; 7
	db	42h 	; 8
	db	41h	; 9
	db	45h	; 0
	db	255	; -
	db	70h 	; =
	db	255   	; bksp
	db	255	; tab

	db	15h	; q
	db	11h 	; w
	db	12h    	; e
	db	13h 	; r
	db	14h    	; t
	db	54h 	; y
	db	53h 	; u
	db	52h 	; i
	db	51h   	; o
	db	55h   	; p
	db	255 	; [
	db	255  	; ]
	db	50h 	; enter
	db	10h 	; ctrl
	db	25h  	; a
	db	21h	; s

	db	22h  	; d
	db	23h 	; f
	db	24h 	; g
	db	64h 	; h
	db	63h 	; i
	db	62h  	; j
	db	61h  	; k
	db	65h  	; l
	db	255  	; ;/:
	db	255  	; '/"
	db	20h  	; shift
	db	255  	; |\
	db	05h  	; z
	db	01h  	; x
	db	02h  	; c
	db	03h	; v
			
	db	04h	; b
	db	74h	; n
	db	73h	; m
	db	72h	; ,
	db	71h	; .
	db	255	; /?
	db	20h	; shift
	db	255	; 
	db	30h	; fctn
	db	60h	; space
	db	255	; extra keys
	db	255	
	db	255	
	db	255	
	db	255	
	db	255


;	How to define extra keys ("`", "-", ":") on TI CRU matrix
;
;	This table maps various extra scancodes to multiple
;	bits on the CRU matrix.  (Pressing left arrow will make
;	FCTN and S be "pressed".)
;
;	Format:  augmented scancode, matrix descriptor.
;
;	If scancode>80h, then select only if shifted.
;	If descriptor high nybble>=8h, then set FCTN
;	If descriptor low nybble >=8h, then set SHIFT
;
extrakeys db	029h,082h 	; ` 		= FCTN-C
	db	0A9h,091h 	; shift	+ ~	= FCTN-W
	db	00Ch,07Dh 	; - 		= shift+'/'
	db	08Ch,0D3h	; shift + _	= FCTN+U
	db	01Ah,093h 	; [		= FCTN+R
	db	09Ah,0A3h 	; shift + {	= FCTN+F
	db	01Bh,094h 	; ]		= FCTN+T
	db	09Bh,0A4h 	; shift + }	= FCTN+G
	db	028h,0D1h	; '		= FCTN+O
	db	0A8h,0D5h	; shift + "	= FCTN+P
	db	035h,075h 	; /		= /
	db	0B5h,0D2h 	; shift + ?	= FCTN+I
	db	02Bh,085h 	; \		= FCTN+Z
	db	0ABh,0A5h 	; shift + |	= FCTN+A

	db	04Eh,078h     	; keypad +	= shift+=

	db	00Eh,0A1h	; backspace	= FCTN+S


;	These are the dreaded F1-F10 keys.  I leave these
;	commented out because of the conflict with
;	special function keys.
;
;	I guess the best thing would have been to have made
;	ALT+Fxx be the only way to do a special function,
;	and then there wouldn't be the foreseen conflict
;	with such combinations as Ctrl+Fctn+xx.
;	(Alt+Fctn == Fctn+Fctn, which is impossible anyway.)
;

;	db	03Bh,0B5h	; F1		= FCTN+1
;	db	03Ch,0B1h	; F2		= FCTN+2
;	db	03Dh,0B2h	; F3
;	db	03Eh,0B3h	; F4
;	db	03Fh,0B4h	; F5
;	db	040h,0C4h	; F6
;	db	041h,0C3h	; F7
;	db	042h,0C2h	; F8
;	db	043h,0C1h	; F9
;	db	044h,0C5h	; F10		= FCTN+0

extraleng	equ ($-extrakeys)/2



;	How to handle enhanced/nonenhanced scancodes
;
;	Enhanced scancodes come from the IBM keyboard
;	preceeded by the "enhanced" scancode 0E0h.
;
;	Keys that either do or don't send the enhanced scancode 
;	(that I care about) are arrow keys and the 
;	ins/del/home/end/pgup/pgdn block.
;
;	V9t9 distinguishes between the numeric keypad and
;	the other keys because the numeric keypad can be used
;	as a joystick.
;
;	Format:  scancode, matrix byte
;	scancode>=80h means enhanced
;	matrix byte>=80h means set fctn
;
;
enhancedkeys db	04Bh,066h 	; left
	db	04Dh,056h 	; right
	db	048h,036h 	; up
	db	050h,046h 	; down 
	db	045h,076h	; FIRE!  (numlock)
	
	db	0CBh,0A1h	; fctn-s
	db	0CDh,0A2h	; fctn-d
	db	0C8h,092h	; fctn-e
	db	0D0h,081h	; fctn-x
		
	db	0D2h,0B1h	; fctn-2 = ins
	db	0D3h,0B5h	; fctn-1 = del
	db	0C9h,0C4h	; fctn-6 = pgup
	db	0D1h,0B3h	; fctn-4 = pgdn
enhancedleng	equ ($-enhanced)/2


keyscn	db	64 dup (0)
keycol	db	0

alphalockline	db	0


	even

romkeyboarddelay dw	1250

	even


	.code

;===========================================================================
;	KEYBOARD:	Pre-config init
;===========================================================================

keyboard_preconfiginit proc near
	clc	

	ret
	endp


;===========================================================================
;	KEYBOARD:	Post-config init
;
;	þ  Figure out mouse/joystick status, and set "isjoystick"
;					     and "emulatejoystickX"	
;===========================================================================

	.data

kpoci_msg db	'Checking joysticks and mouse...',0dh,0ah,'$'

	.code

keyboard_postconfiginit proc near
		
	test	features,FE_emulating
	clc
	jz	kpociout       		; just demoing, no device detect

	lea	dx,kpoci_msg
	call	dosprint

	call	findjoysticks		; see if we have any

	call	findpointingdevices	; find other stuff, and set vars

kpociout:
	ret
	endp


;===========================================================================
;	KEYBOARD:	Restart.
;	
;	þ  Save the incoming LED state.
;	þ  Clear the CRU bitmaps and the fast keyboard flags.
;===========================================================================

keyboard_restart proc near
	push	ax

	mov	ah,2
	int	16h
	mov	oldled,al
	and	al,s_capslock+s_numlock+s_scrolllock
	mov	shift,al
	mov	tishift,al
	call	updateled		; show this fact

	call	clearkeyboard
	call	clearjoyst		; reset CRU and stuff

	pop	ax
	ret
	endp


;===========================================================================
;	KEYBOARD:	Restop.
;	
;	þ  Clear the keyboard buffers.
;	þ  Update the BIOS keyboard LED flag.
;===========================================================================

keyboard_restop proc near

krclearbuffer:
	mov	ah,11h			; get enhanced kbd status
	int	16h
	jz	krbufferempty
	mov	ah,10h			; read enhanced kbd key
	int	16h
	jmp	krclearbuffer
krbufferempty:

	mov	ax,0040h
	mov	es,ax
	mov	al,shift
	and	al,s_numlock+s_capslock+s_scrolllock
	mov	es:[17h],al   		; set BIOS keyboard LED flag
			      		; and reset shift keys
	ret
	endp


;===========================================================================
;	KEYBOARD:	Shutdown.
;===========================================================================

keyboard_shutdown proc near
	clc	

	ret
	endp





;	Figure the joystick/mouse/keyboard settings, set up
;	defaults.
;
findpointingdevices proc near

	mov	ax,0
	int	33h
	mov	ismouse,al		; detect mouse (=0ffh if here)

	mov	emulatejoystick1,3	; keypad
	mov	emulatejoystick2,4  	; none

	test	isjoystick,1
	jnz	fpdcount		; no joy #1		

	cmp	ismouse,0		
	je	fpdout			; no mouse either

	mov	emulatejoystick1,2	; joy1 = mouse
	jmp	fpdout

fpdcount:
	mov	emulatejoystick1,0	; joy1 = IBM joystick #1
	mov	emulatejoystick2,1	; assume joy2 exists too

	test	isjoystick,2
	jnz	fpdout			; it does... continue

	cmp	ismouse,0	 	; no joy #2
	jz	fpdactkey

	mov	emulatejoystick2,2	; joy2 = mouse
	jmp	fpdout
	
fpdactkey:
	mov	emulatejoystick2,3	; joy2 = keyboard
fpdout:
	clc
	ret
	endp


;	Clear out keyboard data (used in restart/restop)
;
;
clearkeyboard	proc	near
	push	si
	push	cx
	push	ax
	lea	si,keyscn
	mov	cx,6*8/2
	xor	ax,ax
ckclear:
	mov	[si],ax
	add	si,2
	loop	ckclear

	mov	enhanced,0
	mov	fakefctncount,0
	mov	fakeshiftcount,0
	mov	sysrq,0
	mov	prevscan,0
	mov	capping,0
	mov	curscan,0
	mov	prevscan,0
	mov	cbptr,offset kcbrk
	mov	specialfunctionkey,0

	pop	ax
	pop	cx
	pop	si
	ret
	endp


;	Clear joystick info
;
;
clearjoyst	proc	near
	push	si
	push	cx
	push	ax
	lea	si,keyscn+6*8
	mov	cx,2*8/2
	xor	ax,ax
cjclear:
	mov	[si],ax
	add	si,2
	loop	cjclear

	pop	ax
	pop	cx
	pop	si
	ret
	endp




;	A KSCAN has been executed; wait until the correct amount of
;	time has passed to prevent superreating.
;
;

	.data

lastchar db	0
kscanshift db	1

	even

	.code

kscandelay	proc	near
	push	ax

	ror	kscanshift,1
	jnc	kdout

	READBYTE 8375h,al			; get read key
	cmp	al,0ffh				; no key?
	je	kdnodelay			; then no delay

	cmp	lastchar,0ffh			; no key pressed last?
	je	kdnodelay			; then this is new, no delay

	cmp	al,lastchar			; same char as last?
	je	kddelay				; delay if same

kdnodelay:
	mov	lastchar,al			; save last key and skip
	jmp	kdfast

kddelay:
	mov	al,kbdelay			; wait for interrupt routine
	cmp	kbtimer,al			; to increment KBTIMER
	jb	kddelay
	sub	kbtimer,al			; force more delay
	jmp	kdout

kdfast:
	mov	al,kbdelay			; force no delay
	mov	kbtimer,al

kdout:
	pop	ax
	ret
	endp
	

;=========================================================================
;	Keyboard interrupt
;=========================================================================

;
;	This interrupt handler operates under the assumption that 
;	the keyboard is in the medium-raw (keycode) mode.
;
;	The job of the handler is to:
;	1)  Translate "ordinary" keys in a CRU mapping for the emulator
;	2)  Intercept function calls (Ctrl or Alt + Fx) and see that
;	    Handlespecialfunctions is called.
;	3)  Save the current shift state
;	3)  Record diagnostics sent back from the keyboard
;

keyboard	proc	far
	push	ax
	push	bx
	push	ds

	mov	ax,@data
	mov	ds,ax

	mov	kbactive,1

	call	kbwait

	in	al,64h
;	test	al,1
;	jnz	kbokay			; data waiting or fluke?
;	jmp	kbend			; (this screws up debuggers)

kbokay:
	in	al,60h
	mov	curscan,al

	cmp	al,0fah
	jne	kb01
	mov	ack,1			; KB sent acknowledge
	jmp	kbend

kb01:
	cmp	al,0feh
	jne	kb02
	mov	resend,1		; KB wants resend
	jmp	kbend

kb02:
	cmp	al,0
	jne	kb03
	or	stateflag,ctrlbreakpressed+happymessage	; buffer overflow!  panic!
	jmp	kbend

kb03:
	cmp	al,0ffh
	jne	kb04
	mov	prevscan,0	       	; kbd error
	mov	enhanced,0
	jmp	kbend

kb04:
	cmp	al,0e0h
	jne	kb06
	mov	enhanced,1		; enhanced scancode
	jmp	kbend			; this will be cleared BEFORE kbend

kb06:
					; See if Ctrl+Break
	mov	bx,cbptr
	cmp	[bx],al
	je	kbcb01
	cmp	[bx-1],al
	je	kbcb011
	mov	bx,cbptr
	jmp	kbcb02
kbcb01:
	inc	bx
kbcb011:
	cmp	bx,offset cbend
	jb	kbcb02
	mov	bx,cbptr
	or	stateflag,ctrlbreakpressed+happymessage
kbcb02:
	mov	cbptr,bx

					; See if Pause
	mov	bx,paptr
	cmp	[bx],al
	je	kbpa01
	cmp	[bx-1],al
	je	kbpa011
	mov	bx,paptr
	jmp	kbpa02
kbpa01:
	inc	bx
kbpa011:
	cmp	bx,offset paend
	jb	kbpa02
	mov	bx,paptr
	xor	stateflag,paused
kbpa02:
	mov	paptr,bx


	mov	up,al
	and	up,80h
	and	al,7fh
	mov	curscan,al		; released?

					; Test shift states
	cmp	al,k_ctrl
	jne	kbs01
	mov	ah,s_ctrl
	jmp	kbs00
kbs01:
	cmp	al,k_alt
	jne	kbs02
	mov	ah,s_alt
	jmp	kbs00
kbs02:
	cmp	al,k_leftshift
	jne	kbs03
	cmp	enhanced,0
	jz	kbs021			
	mov	enhanced,0
	jmp	kbend			; ignore this "shift"
kbs021:
	mov	ah,s_leftshift
	jmp	kbs00
kbs03:
	cmp	al,k_rightshift
	jne	kbs99
	mov	ah,s_rightshift
kbs00:
	not	ah
	and	shift,ah		; zero out shift
	not	ah
	cmp	up,0
	jnz	kbs04
	or	shift,ah		; set shift

kbs04:
	mov	ah,shift
	and	shift,not s_shift
	and	ah,s_leftshift+s_rightshift
	jz	kbs05
	or	shift,s_shift		; any shift?

kbs05:

kbs99:
kbnonewtishift:
	cmp	up,0
	jz	kbtestspecial		; otherwise we released shift key

kbtestspecial:				; Test for Ctrl or Alt + Fx
	cmp	up,0
	jnz	kbnf			; only allow press activation
	cmp	al,k_f1
	jb	kbnf
	cmp	al,k_f12
	ja	kbnf
	cmp	al,k_f10
	jbe	kbif
	cmp	al,k_f11
	jb	kbnf

kbif:
	mov	specialfunctionkey,al
	or	stateflag,specialfunctionrequest
	jmp	kbout			; save this as last scancode

kbnf:
					; Test Alt-SysRq
	cmp	al,k_sysrq
	jne	kbnas
	test	shift,s_alt
	jz	kbnas

	cmp	up,0			; pressed or released?
	jnz	kbas2
	or	stateflag,debugrequest	; initiate interrupt
	mov	sysrq,1
	and	shift,not s_alt
	jmp	kbout

kbas2:
	mov	sysrq,0			; else clear flag just in case
	jmp	kbout

kbnas:
					; Test CapsLock
	cmp	al,k_capslock
	jne	kbncl

	cmp	up,80h
	jne	kbccs

	mov	capping,0
	jmp	kbout

kbccs:
	cmp	capping,0		; don't allow repeats
	jnz	kbicl

	xor	shift,s_capslock
	xor	tishift,s_capslock
	call	updateled
	mov	capping,1

kbicl:
	jmp	kbout

kbncl:

;	Uh, I don't know why this is commented out.
;	???????????????????????????????????????????
;
;	Oh, okay.  Whoops, documentation error.
;
;	Ctrl+F12 reboots the machine.  All the shifts+'='
;	just happens to coincide with Fctn+'='.  
;
;	See SPECIAL.ASM for how it reboots.
;

					; Test Reboot -- All shifts + '='
;;	cmp	al,k_equals
;;	jne	kbnrb
;;
;;	mov	ah,shift
;;	and	ah,s_ctrl+s_alt+s_leftshift+s_rightshift
;;	cmp	ah,s_ctrl+s_alt+s_leftshift+s_rightshift
;;	jne	kbnrb
;;
;;	or	stateflag,reboot
;;	jmp	kbout			; moved to Ctrl+F12

kbnrb:
;	
;	Now, actual checking for TI emulation.
;

	cmp	up,0
	jnz	kbrl

	mov	keypressed,1		; some 99/4A key may be pressed

	cmp	al,prevscan
	je	kbout			; ignore repeats

	call	settishift

	call	setkeyboardmap		; faster than kbdelay/60ths/sec

	mov	fctn4,0
	cmp	MAT_FCTN,0
	jz	kbout
	cmp	MAT_4,0
	jz	kbout

	inc	fctn4			; used for SERIAL.INC or RS232.INC
	jmp	kbout

kbrl:
	call	resettishift

	mov	keypressed,0
	call	resetkeyboardmap
	mov	curscan,0

kbout:
	mov	al,curscan
	mov	prevscan,al

      	mov	enhanced,0

kbend:

kbno4:

	mov	al,20h
	out	20h,al

	pop	ds
	pop	bx
	pop	ax
	iret
	endp


;==========================================================================
;	Set the shift state for the 99/4A direct keyboard emulation
;

settishift proc	near
	cmp	al,k_ctrl
	jne	sts00

	or	tishift,s_ctrl
	jmp	stsout

sts00:
	cmp	al,k_alt
	jne	sts01

	or	tishift,s_alt
	jmp	stsout

sts01:
	cmp	al,k_leftshift
	je	sts011
	cmp	al,k_rightshift
	jne	stsout
sts011:
	or	tishift,s_shift
stsout:
	ret
	endp


;===================================================================
;	Reset the shift state for the 99/4A direct keyboard emulation
;

resettishift proc near
	cmp	al,k_ctrl
	jne	rts00

	and	tishift,not s_ctrl
	jmp	rtsout

rts00:
	cmp	al,k_alt
	jne	rts01

	and	tishift,not s_alt
	jmp	rtsout

rts01:
	cmp	al,k_leftshift
	je	rts011
	cmp	al,k_rightshift
	jne	rtsout
rts011:
	and	tishift,not s_shift
rtsout:
	ret
	endp


;======================================================================
;	Set IBM->TI keyboard map for CRU map
;
;======================================================================

;	1)  Check scantable for direct correlation of keys and set.
;	2)  If need to emulate fctn+ or shift+, set vars then do above.
;	3)  Check if keypad/joystick emul.
;

setkeyboardmap	proc	near
	push	ax
	push	bx
	push	cx

	mov	al,curscan
	cmp	al,3ah
	jae	skbmnoTIequiv		; legal scancode?
	lea	bx,scantable
	xor	ah,ah
	add	bx,ax
	mov	al,[bx]			; check scantable for direct match
	cmp	al,255
	je	skbmnoTIequiv		; nope

	call	setmatrix		; call with AL=coords
	jmp	skbmout

skbmnoTIequiv:
 	test	shift,s_shift		; if shift+key, match that
	jz	skbmnoshift
	or	al,80h
skbmnoshift:
	call	findextrakey
	jc	skbmnotextra		; not found

	call	setfctnorshift		; set fake fctn, shift
	call	setmatrix
	jmp	skbmout

skbmnotextra:
	call	findenhkey		; see if we pressed an enhanced key
	jc	skbmout			; nope

	call	setfctnorshift
	call	setmatrix

skbmout:
	pop	cx
	pop	bx
	pop	ax
	ret
	endp

;------------------------------------------

;	Set CRU keyboard matrix.  AL=(row,col)
;
;
setmatrix	proc	near
	push	bx
	push	ax
	mov	bl,al
	shr	bl,4
	and	al,15

	xor	bh,bh
	xor	ah,ah
	shl	ax,3
	add	bx,ax

	mov	[bx+keyscn],1
	pop	ax
	pop	bx
	ret
	endp


;---------------------------------------------

;	Reset keyboard matrix.  AL=(row,col)
;
;
resetmatrix	proc	near
	push	bx
	push	ax
	and	al,77h
	mov	bl,al
	shr	bl,4
	and	al,15

	xor	bh,bh
	xor	ah,ah
	shl	ax,3
	add	bx,ax

	mov	[bx+keyscn],0
	pop	ax
	pop	bx
	ret
	endp


;------------------------------------------------


findextrakey	proc	near
	push	bx
	push	cx

	lea	bx,extrakeys
	mov	al,curscan
	test	shift,s_shift
	jz	fexknoshift
	or	al,80h
fexknoshift:
	mov	cx,extraleng
fexklookext:
	cmp	[bx],al
	je	fexkfound
	add	bx,2
	loop	fexklookext
	stc
	jmp	fexkout
fexkfound:
	mov	ah,al
	mov	al,[bx+1]
	clc
fexkout:
	pop	cx
	pop	bx
	ret
	endp


;-----------------------------------------

findenhkey	proc	near
	push	bx
	push	cx

	lea	bx,enhancedkeys
	mov	al,curscan
	and	al,7fh
	cmp	enhanced,0
	jz	feknotenh
	or	al,80h
feknotenh:
	mov	cx,enhancedleng
feklookenh:
	cmp	[bx],al
	je      fekfound		; same code
	add	bx,2
	loop	feklookenh		
feknotfound:
	stc
	jmp	fekout
fekfound:
	mov	al,[bx+1]
	mov	ah,al
	and	ah,0fh
	cmp	ah,6
	jne	feknotjoy

	cmp	emulatejoystick1,3
	je	feknotjoy		; defaults to joystick 1

	cmp	emulatejoystick2,3
	jne 	feknotfound		; whoops!  ignore it then

	inc	al			; make key work on joystick 2

feknotjoy:
	clc
fekout:
	pop	cx
	pop	bx
	ret
	endp


;=========================================================

;	Try to maintain sanity between fake fctn, shift
;	(such as emulating "`" as Fctn+C) and real
;	shifts.  (Such as pressing Alt+"`").
;
setfctnorshift	proc	near
	push	bx
	mov	bl,curscan
	cmp	bl,lastfake
	je	sfosnosetshift			; repeating?

	mov	lastfake,bl			; new key
	mov	MAT_FCTN,0			; clear shift/fctn
	mov	MAT_SHIFT,0

	test	al,80h
	jz	sfosnosetfctn

	mov	MAT_FCTN,1

sfosnosetfctn:
	
	test	al,8
	jz	sfosnosetshift

	mov	MAT_SHIFT,1

sfosnosetshift:
	test	shift,s_alt
	jz	sfosnorealalt

	mov	MAT_FCTN,1

sfosnorealalt:
	test	shift,s_shift
	jz	sfosnorealshift

	mov	MAT_SHIFT,1

sfosnorealshift:
	and	al,77h
	pop	bx
	ret
	endp


;--------------------------------------

resetfctnorshift	proc	near
	push	bx
	mov	bl,curscan
	and	bl,7fh
	cmp	bl,lastfake
	jne	rfosnosetshift			; is this the last key?

	mov	MAT_FCTN,0
	mov	MAT_SHIFT,0

rfosnosetfctn:

rfosnosetshift:
	mov	lastfake,0
	jmp	sfosnosetshift			; fix real shifts

	endp


;==================================================================

;	Reset IBM->TI keyboard map for CRU operations
;
;

resetkeyboardmap	proc	near
	push	ax
	push	bx
	push	cx

	mov	al,curscan
	cmp	al,3ah
	jae	rkbmnoTIequiv
	lea	bx,scantable
	xor	ah,ah
	add	bx,ax
	mov	al,[bx]
	cmp	al,255
	je	rkbmnoTIequiv

rkbmfake:
	call	resetmatrix
	mov	bl,curscan
	mov	lastfake,bl
	call	resetfctnorshift
	jmp	rkbmout

rkbmnoTIequiv:
	call	findextrakey
	jc	rkbmnotext

	call	resetfctnorshift
	call	resetmatrix

	test	ah,80h
	jnz	rkbmout

	mov	bl,shift
	or	shift,s_shift
	call	findextrakey
	jc	rkbmnolookalike

	call	resetmatrix

rkbmnolookalike:
	mov	shift,bl
	jmp	rkbmout

rkbmnotext:
	call	findenhkey
	jc	rkbmout

	call	resetfctnorshift
	call	resetmatrix

rkbmout:

rkbmleave:
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;==========================================================================
;	Handle KSCAN.
;
;	This is the code from v5.01 which the user can specify to
;	use to avoid using the overly slow CRU scan unless a game
;	accesses the CRU itself.
;
;	Only modes 3, 4, and 5 will be handled by this function,
;	as split keyboard may have two keys pressed at once (as well
;	as joysticks and stuff), and its complexity is better handled
;	as a CRU map.
;==========================================================================

	.data

nkeys	db	255,15,'1234567890-=',8		; 15
	db	255,'qwertyuiop[]',13		; 14
	db	255,'asdfghjkl;',39		; 12
	db	'`',255,'\zxcvbnm,./',255	; 14
	db	'*',255,' ',255			; 4

skeys	db	255,15,'!@#$%^&*()_+',8
	db	255,'QWERTYUIOP{}',13
	db	255,'ASDFGHJKL:"'
	db	'~',255,'|ZXCVBNM<>?',255
	db	'*',255,' ',255

fkey5	db	255,255,3,4,7,2,14,12,1,6,15,188,255,5,8	
	db	255,197,'~',11,'[]',198,'_?',39,'"[]',13	
	db	255,'|',8,9,'{}',191,192,193,194,189,39		
	db	'`',255,'\\',10,'`',127,190,196,195,184,185,186,255
	db	'*',255,' ',255

fkey4	db	225,255,131,132,135,130,142,140,129,139,143,182,255,133,136
	db	255,197,126,139,91,93,198,'_?',39,'"[]',13
	db	255,'|',136,137,'{}',191,192,193,194,189,39
	db	'`',255,'\\',138,'`',128,190,196,195,184,185,186,255
	db	'*',255,' ',255

ckey5	db	255,255,177,178,179,180,181,182,183,158,159,176,255,157,147
	db	255,145,151,133,146,148,153,149,137,143,144,255,255,13
	db	255,129,147,132,134,135,136,138,139,140,155
	db	255,255,255,255,154,152,131,150,130,142,141,156,128,187
	db	255,255,255,' ',255

ckey4	db	255,255,177,178,179,180,181,182,183,30,31,176,255,29,255
	db	255,17,23,5,18,20,25,21,9,15,16,255,255,13
	db	255,1,19,4,6,7,8,10,11,12,28,255,255
	db	255,255,26,24,3,22,2,14,13,0,27,187
	db	255,255,255,' ',255


ibmtoti	db	75,8,77,9,72,11,80,10,82,4,83,3,73,12,81,2
ibmtotisize equ	($-offset ibmtoti)/2

keypressed db	0
lastscan db	0
lastkeymode db	0

	even
		
	.code

handlekeyboard	proc	near
	push	es
	pusha
	mov	es,cpuseg	

	READBYTE 8374h,ah
	or	ah,ah			; mode 0?
	jz	hkbmode0

hkbfixmode:
	cmp	ah,5
	ja	hkbskip
	cmp	ah,2			; only handle modes 3-5
	ja	hkbhandle
	
hkbskip:
	READREG	R11,ax			; do instruction at >2B2
	WRITEWORD 83D8h,ax
	popa
	jmp	hkbnoscan		

hkbhandle:
	WRITEBYTE 8374h,0	
	mov	lastkeymode,ah
	mov	al,ah
	sub	al,3
	WRITEBYTE 83c6h,al
	jmp	hkbnoreset

hkbmode0:
	mov	ah,lastkeymode
	or	ah,ah
	jnz	hkbnoreset
	mov	ah,5			; this shouldn't happen

hkbnoreset:
	cmp	keypressed,0
	je	hkbnokeywaiting


	cmp	curscan,58		; "normal" keys?
	jbe	hkbtestthekey

	cmp	enhanced,0
	jnz	hkbnokeywaiting		; numpad keys only for joysticks

	mov	al,curscan
					; Test arrow keys, etc
	lea	bx,ibmtoti
	mov	cx,ibmtotisize
hkbcomp:
	cmp	[bx],al
	je	hkbgottheibmcode
	add	bx,2
	loop	hkbcomp

	mov	keypressed,0
	jmp	hkbnokeywaiting

hkbgottheibmcode:
	MOV	ch,curscan
	mov	al,[bx+1]
	mov	bl,ch
	jmp	hkbgotkey

hkbnokeywaiting:
	WRITEBYTE 8375h,0ffh
	WRITEBYTE 837ch,0
	mov	al,255
	jmp	hkbout

hkbtestthekey:
	mov	bl,curscan
	sub	bh,bh			; set up index
	mov	cl,tishift		; CL holds shift

	test	cl,s_ctrl
	je	hkbnoctrl
	cmp	MAT_CTRL,0
	je	hkbnoctrl

	cmp	ah,4
	jne	hkbnotc4
	mov	al,[ckey4+bx]
	jmp	hkbgotkey
hkbnotc4:
	mov	al,[ckey5+bx]
	jmp	hkbgotkey

hkbnoctrl:
	test	cl,s_alt
	je	hkbnoalt
	cmp	MAT_FCTN,0
	je	hkbnoalt

	cmp	ah,4
	je	hkbf4
	mov	al,[fkey5+bx]		; modes 3 & 5 have same functions
	jmp	hkbgotkey
hkbf4:
	mov	al,[fkey4+bx]
	jmp	hkbgotkey

hkbnoalt:
	test	cl,s_shift	
	je	hkbnormalkey
	cmp	MAT_SHIFT,0
	je	hkbnormalkey

	mov	al,[skeys+bx]
	jmp	hkbgotkey		; no need to test caps lock
					; because shift never "lowers"
					; chars on TI
hkbnormalkey:
	mov	al,[nkeys+bx]
	cmp	ah,3
	je	hkbcapit		; mode 3 only has uppercase
	test	cl,s_capslock
	jz	hkbnocaps
hkbcapit:
	cmp	al,'a'
	jb	hkbnocaps
	cmp	al,'z'
	ja	hkbnocaps
	sub	al,32			; get rid of lower-case bias

hkbnocaps:
					; got the normal key
hkbgotkey:
	cmp	ah,3
	jne	hkbnocheck3
	cmp	al,96
	jb	hkbnocheck3
	mov	al,255			; mode 3 only allows 32-95
hkbnocheck3:
	WRITEBYTE 8375h,al
	cmp	al,255
	je	hkbnonewkey		

	WRITEWORD 83d6h,0		; reset timeout counter
	push	ax
	READBYTE 83D4h,al		; reset VREG #1
	mov	ah,81h
	mov	vaddr,ax
	call	handlevdpreg
	pop	ax

	cmp	bl,0
	je	hkbnonewkey
	cmp	bl,lastscan
	je	hkbnonewkey
	READBYTE 837ch,al
	or	al,20h
	WRITEBYTE 837ch,al
	jmp	hkbout

hkbnonewkey:
	WRITEBYTE 837ch,0
hkbout:
	mov	al,curscan
	mov	lastscan,al

	WRITEBYTE 83eeh,0		; byte that holds shift combos
	mov	al,0
	cmp	MAT_CTRL,0
	jz	hkbnospecctrl
	or	al,64
hkbnospecctrl:
	cmp	MAT_FCTN,0
	jz	hkbnospecfctn
	or	al,16
hkbnospecfctn:
	cmp	MAT_SHIFT,0
	jz	hkbnospecshift
	or	al,32
hkbnospecshift:
	WRITEBYTE 83eeh,al

	popa
	push	ax

	READREG R11,ax
	WRITEWORD 83d8h,ax

	mov	IP,ax
	pop	ax

	test	patches,PT_kbdelay
	jz	hkbnoscan

	call	kscandelay			; because we skip ROM

hkbnoscan:
	pop	es
	ret
	endp




;==========================================================================
;	Read joysticks which are installed
;	and set values in KEYSCN.
;
;	Triggered by KEYSCN being read
;
;
;	EMULATEJOYSTICKx meanings:
;
;
;	0 = PC joystick 1
;	1 = PC joystick 2
;	2 = mouse
;	3 = keyboard
;	4 = none
;
;==========================================================================


readjoysticks	proc	near

	cmp	joysticktick,0
	jnz	rjsdoit

	jmp	rjsskip

rjsdoit:
	pusha

	mov	al,emulatejoystick1
	or	al,al
	jnz	rj11

	lea	di,joy1
	call	readjoystick1
	jc	rj2do			; whoops!
	mov	cl,al
	mov	ax,joy1.xx
	mov	bx,joy1.yy
	lea	bp,joy1c
	jmp	rj1set

rj11:
	cmp	al,1
	jne	rj12

	lea	di,joy2
	call	readjoystick2
	jc	rj2do
	mov	cl,al
	mov	ax,joy1.xx
	mov	bx,joy1.yy
	lea	bp,joy2c
	jmp	rj1set

rj12:
	cmp	al,2
	jne	rj2do			; 3 and 4 are automatic

	call	readmouse
	lea	bp,mousecenter

rj1set:
	lea	di,keyscn+6*8
	call	setjoyvals

rj2do:

	mov	al,emulatejoystick2
	or	al,al
	jnz	rj21

	lea	di,joy1
	call	readjoystick1
	jc	rjsout			; whoops!
	mov	cl,al
	mov	ax,joy1.xx
	mov	bx,joy1.yy
	lea	bp,joy1c
	jmp	rj2set

rj21:
	cmp	al,1
	jne	rj22

	lea	di,joy2
	call	readjoystick2
	jc	rjsout
	mov	cl,al
	mov	ax,joy1.xx
	mov	bx,joy1.yy
	lea	bp,joy2c
	jmp	rj2set

rj22:
	cmp	al,2
	jne	rjsout			; 3 and 4 are automatic

	call	readmouse
	lea	bp,mousecenter

rj2set:
	lea	di,keyscn+7*8
	call	setjoyvals

rjsout:
	mov	joysticktick,0

	popa
rjsskip:
	ret
	endp


;-----------------------------------------------------

;	Read attached mouse 
;	Return coords in Ax,Bx,  button CL
;
readmouse 	proc	near
	cmp	mouseemulationtype,0
	jnz	rm1

;	Type 0 is positional

	mov	ax,3
	int	33h			; get position and buttons

	xchg	bx,dx			; BX=y, DX=button
	xchg	cx,dx			; CX=button, DX=X
	mov	ax,dx			; AX=x, Bx=y, CX=button

	jmp	rmout

rm1:
;	Type 1 is motional

	mov	ax,3
	int	33h			; get position and buttons

	mov	ax,0bh
	int	33h			; get motion counts

	sal	cx,4
	sal	dx,4

	xchg	bx,dx
	xchg	cx,dx
	mov	ax,dx

	add	ax,320
	add	bx,100			; center

	push	ax
	push	bx
	push	cx

	mov	ax,4
	mov	cx,320
	mov	dx,100
	int	33h

	pop	cx
	pop	bx
	pop	ax

rmout:
	ret
	endp


;-----------------------------------------------------------

;	Read and set values for one joystick
;
;	AX,BX=x,y  	CL=button	BP=joyXc	DI=keyscn ptr
;
;

setjoyvals	proc	near
	mov	byte ptr [di+5],0	; clear left/right
	mov	byte ptr [di+6],0

	push	bx
	push	bp
	mov	si,ds:[bp].xx	       	; center X
	
	mov	bx,si
	shr	bx,1			; get 1/2 area of center

	mov	bp,si
	add	bp,bx			; bounds for center/right
	cmp	ax,bp
	jge	sjvright1		; right side

	mov	bp,si
	sub	bp,bx			; bounds for left/center
	cmp	ax,bp
	jg	sjvtestup1		; else left

sjvleft1:
	mov	byte ptr [di+6],1
	jmp	sjvtestup1
sjvright1:
	mov	byte ptr [di+5],1

sjvtestup1:
	pop	bp
	pop	bx

	push	bx
	push	bp
	
	mov	byte ptr [di+4],0
	mov	byte ptr [di+3],0

	mov	si,ds:[bp].yy		; si = center

	mov	ax,bx
	mov	bx,si
	shr	bx,1			; get center size/2

	mov	bp,si
	add	bp,bx			; center/bottom boundary
	cmp	ax,bp
	jge	sjvdown1		

	mov	bp,si
	sub	bp,bx			; top/center boundary
	cmp	ax,bp
	jg	sjvout


	mov	byte ptr [di+3],1
	jmp	sjvout
sjvdown1:
	mov	byte ptr [di+4],1

sjvout:
	pop	bp
	pop	bx

	test	cl,3			; fire button
	mov	byte ptr [di+7],0
	jz	sjvdone
	mov	byte ptr [di+7],1
sjvdone:
	ret
	endp


;-----------------------------------------------------------------

;	KBWAIT -- wait to send data to keyboard
;

kbwait	proc	near
	push	ax
	push	cx

	mov	cx,0ffffh
kbwloop:
	in	al,64h
	nop
	nop
	test	al,2
	jz	kbwout
	loop	kbwloop

kbwout:
	pop	cx
	pop	ax
	ret
	endp


kbwaito	proc	near
	push	ax
	push	cx

	mov	cx,0ffffh
kbwoloop:
	in	al,64h
	nop
	nop
	test	al,1
	jz	kbwoout
	loop	kbwoloop

kbwoout:
	pop	cx
	pop	ax
	ret
	endp



;	SENDCMD -- send command to keyboard controller
;
;	AL=command
;
sendcmd	proc	near
	call	kbwait
	out	64h,al
	ret
	endp


;	SENDDATA -- send data to kb controller
;
;	AL=byte
;
senddata	proc	near
	push	ax
	push	cx
	push	dx

	mov	ah,al

	mov	dx,3

sdloop:
;	call	kbwait
	mov	ack,0
	mov	resend,0
	
	mov	al,ah
	out	60h,al

	mov	cx,10

sdloop2:
	in	al,64h			; delay
	cmp	ack,0
	jnz	sdgood
	cmp	resend,0
	jnz	sdretry
	loop	sdloop2
	jmp	sdgood

sdretry:
	cmp	resend,0
	jz	sdfail

	dec	dx
	jg	sdloop

sdfail:
	stc
	jmp	sdout

sdgood:
	clc

sdout:
	pop	dx
	pop	cx
	pop	ax
	ret
	endp


	.data
ullog	db	'Setting keyboard LEDs...%n',0
ullog2	db	'Done!%n',0

	.code

updateled	proc	near
	cmp	keyboardledset,0
	jnz	ulgo
	ret

ulgo:
	LOG0	LG_keyboard,ullog

	sti
	push	ax

	mov	al,0adh
	call	sendcmd
	call	kbwaito

	in	al,60h

	mov	al,0edh
	call	senddata
	jc	ulbad
	in	al,60h

	call	kbwaito

	sub	al,al
	test	shift,s_capslock
	jz	slnocaps
	or	al,4
slnocaps:
	test	shift,s_numlock	
	jz	slnonum
	or	al,2
slnonum:
	test	shift,s_scrolllock
	jz	slnoscroll
	or	al,1
slnoscroll:
	call	senddata
	jnc	ulgood

ulbad:
	call	kbwaito
	mov	al,0f4h
	call	senddata
	call	kbwaito

ulgood:
	in	al,60h
	mov	al,0aeh
	call	sendcmd

	LOG0	LG_keyboard,ullog2

	pop	ax
	ret
	endp


	end
