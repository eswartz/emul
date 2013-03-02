;   tiemul.asm
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

	name    TIEMUL
	title   V9t9:  TI Emulator! v6.0 by Edward Swartz, (c) 1995


_TIEMUL_ =	1			; we're in TIEMUL.ASM

	include	standard.h

	.data


vdp     db      4000h dup (?)		; VDP memory MUST be at ds:0


	include	video.h
	include	special.h
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

	include	tiemul.h


	include	registers.inc
	include	memory.inc

	include	memcode.inc
	include	emulate.inc


;===========================================================================

	.data

	IFDEF	SUPER
addrmap	dw	0
	ENDIF

lastval		dw	0      		; last value which sets L,A,E
lastcmp		dw	0      		; 0 usually, or second in CMP

processor 	dw	0      		; 86, 286, 386...

curwp		dw	0      		; used by compiled ROM
intmask	   	dw	0		; saved mask of ST register


ips		dd	0		; total instructions in (uptime/60)
uptime		dd	0		; 1/60 second increments since reset
cips		dw	0		; current (1/60 sec) ips

features 	dw	0	   	; which extra stuff do we have?
stateflag	dw	0		; ya know


_IP		dw	0		; instruction pointer

dsrreadbyte	dw	nulldsrread	; which routine handles read from DSR
					; ROM space?
dsrwritebyte	dw	nulldsrwrite	; or a write?


cpuseg		dw	0
ms_rom		dw	0	   	; Derived CPU ROM, >0000->1FFF
ms_lram		dw	0	   	; Derived CPU RAM, >2000->3FFF
ms_dsrrom	dw	0		; Derived DSR ROM ptr, >4000->5FFF
ms_cartmem	dw	0		; Derived CART MEM ptr,>6000->7FFF
ms_pad		dw	0		; Derived CPU PAD, >8x00->8xFF
ms_hram		dw	0		; Derived CPU RAM, >A000->BFFF
ms_hram2	dw	0		; Derived CPU RAM, >C000->DFFF
ms_hram3	dw	0		; Derived CPU RAM, >E000->FFFF

gplseg		dw	0  		; grom segment
moduleseg	dw	0		; module segment
moduleoffset	dw	0		; offset within module
speechseg	dw	0		; speech memory segment

vregs		db	16 dup (0h)  	; VDP w-o registers
		even
vdpramsize	dw	03fffh		; size of VDP RAM

vaddr		dw	0  		; VDP address
vwriteoffset 	dw 	0 		; see comment in WWVDP
gaddr		dw	0 		; GPL address

vwrite		db	0 		; state of address writing
vdpstat		db	0      	
gwrite		db	0      		; state of address writing
gread		db	0      		; state of address reading

psp		dw	0      		; Program segment prefix

TICpuFileName   db      'TICPU.HEX',0	; default ROM filename
		db	3 dup(?)
TIGplFileName   db      'TIGPL.HEX',0	; default GROM filename
		db	3 dup(?)
TISpeechfilename db	14 dup (0)	; default speech ROM filename
					; (blank=don't load)
	

		even

tidiskpathname	db	6*64 dup (0)	; paths for DSKx emulation
delayamount	dw	0		; inter-instruction delay
timerdelay	dw	60		; timer delay
rompath		db	'.\',0		; where to find ROMs?
		db	61 dup(?)
modulespath	db	'.\',0		; where to find module ROMs?
		db	61 dup(?)
speechpath	db	'.\',0		; where to find speech stuff?
		db	61 dup(?)	; only used for patches

		even
videodatachanged	db	0	; prevent interrupt from causing
					; a rewrite
interruptsinstalled	db	0	; interrupts have been installed
preventmultipleinterrupts db	0	; prevent multiple interrupts (when limiing)
happymessage	db	0

		even

	IFDEF	COMPROM

compiledpath	db	64	dup(0)	; where are compiled ROMs?

compiledrom	db	14  	dup(0)	; filename of compiled ROM?
compiledromseg	dw	0		; where is the program in memory?

romstruc	romparams <>

	ENDIF

v9t9rom	db	0	 		; using V9T9 ROM? (to allow patching)
startdebug db	0	 		; startup in debugger?


;==========================================================================

	.code

;--------------------------------------------------------------------------
;	TIEMULATOR --	Start V9t9
;
;--------------------------------------------------------------------------

tiemulator proc	far
	mov	ax,@data
	mov	ds,ax
	mov	psp,es

	call	installctrlc	  		; CTRL-C handler

	call	preconfigemuinit		; init before reading configs
	jc	tiemulerr

	call	emugetconfig			; load config files
	jc	tiemulerr

	call	postconfigemuinit		; init after config files
	jc	tiemulerr

	call	go				; do it

tiemulerr:
	call	resetctrlc			; remove CTRL-C handler
	call	tiemulexit			; exit program only
	endp


;---------------------------------------------------------------------------
;	DIE --	Completely halt the emulator.
;
;	Should be called from emulating state.
;---------------------------------------------------------------------------

die	proc	near
	call	emustop				; pause
	call	emushutdown			; shutdown
	call	resetctrlc			
	call	tiemulexit			; exit program
	ret
	endp



;--------------------------------------------------------------------------
;	Pre-configuation initialization.
;
;	Put stuff here that would cause an immediate fatal error,
;	or affect the reading of the configuration files.
;
;	No registers need to be saved.
;--------------------------------------------------------------------------

preconfigemuinit proc near
	call	tiemul_preconfiginit
	jc	pcei_fatal
	call	support_preconfiginit		; get paths, command-line
	jc	pcei_fatal			; parameters

	call	video_preconfiginit
	jc	pcei_fatal
	call	special_preconfiginit
	jc	pcei_fatal
	call	int_preconfiginit
	jc	pcei_fatal
	call	hardware_preconfiginit
	jc	pcei_fatal
	call	files_preconfiginit
	jc	pcei_fatal
	call	sound_preconfiginit
	jc	pcei_fatal
	call	keyboard_preconfiginit
	jc	pcei_fatal
	call	debug_preconfiginit
	jc	pcei_fatal
	call	speech_preconfiginit
	jc	pcei_fatal
	call	record_preconfiginit
	jc	pcei_fatal
	call	log_preconfiginit
pcei_fatal:
	ret
	endp


;--------------------------------------------------------------------------
;	Post-configuation initialization.
;
;	Put stuff here that changes the effect of configuration
;	variables or stuff to get memory.
;
;	Do nothing which requires a specific deinitialization.
;
;	No registers need to be saved.
;
;	If demonstration-playing mode has been specified, we skip
;	initializing unused modules.
;
;--------------------------------------------------------------------------

postconfigemuinit proc near

	call	video_postconfiginit
	jc	pocei_fatal
	call	special_postconfiginit
	jc	pocei_fatal
	call	int_postconfiginit
	jc	pocei_fatal
	call	hardware_postconfiginit
	jc	pocei_fatal
	call	files_postconfiginit
	jc	pocei_fatal
	call	sound_postconfiginit
	jc	pocei_fatal
	call	keyboard_postconfiginit
	jc	pocei_fatal
	call	debug_postconfiginit
	jc	pocei_fatal
	call	speech_postconfiginit
	jc	pocei_fatal
	call	support_postconfiginit
	jc	pocei_fatal
	call	log_postconfiginit
	jc	pocei_fatal

	call	tiemul_postconfiginit
	jc	pocei_fatal
	call	record_postconfiginit
	jc	pocei_fatal

pocei_fatal:
	ret
	endp


;--------------------------------------------------------------------------
;	Emulator restart.
;
;	This is called when the emulator is being restarted from 
;	either an interactive pause or a DOS shell or something.
;
;	Also called to start it up for the first time.
;
;	The "emustate" word will tell which modules were started,
;	in case an error forces a call to "emustop" and "emushutdown".
;--------------------------------------------------------------------------

	.data

ES_video 	equ	1
ES_special	equ	2
ES_int		equ	4
ES_hardware	equ	8
ES_files	equ	16
ES_sound	equ	32
ES_keyboard	equ	64
ES_debug	equ	128
ES_speech	equ	256
ES_support	equ	512
ES_log		equ	1024
ES_tiemul	equ	2048
ES_record	equ	4096

emustate dw	0


	.code

emustart proc	near
	pusha
	push	es

	mov	ax,0

	call	special_restart
	jc	es_fatal
	or	ax,ES_special

	call	hardware_restart
	jc	es_fatal
	or	ax,ES_hardware

	call	files_restart
	jc	es_fatal
	or	ax,ES_files

	call	sound_restart
	jc	es_fatal
	or	ax,ES_sound

	call	keyboard_restart
	jc	es_fatal
	or	ax,ES_keyboard

	call	debug_restart
	jc	es_fatal
	or	ax,ES_debug

	call	speech_restart
	jc	es_fatal
	or	ax,ES_speech

	call	support_restart
	jc	es_fatal
	or	ax,ES_support

	call	log_restart
	jc	es_fatal
	or	ax,ES_log


	call	int_restart
	jc	es_fatal
	or	ax,ES_int

	call	video_restart			; come up last to make
	jc	es_fatal			; user aware he can continue
	or	ax,ES_video



	call	tiemul_restart
	jc	es_fatal
	or	ax,ES_tiemul

	call	record_restart
	jc	es_fatal
	or	ax,ES_record
	jmp	es_out

es_fatal:
	mov	emustate,ax

	call	emustop
	call	emushutdown
	call	tiemulexit

es_out:
	mov	emustate,ax

	pop	es
	popa
	clc
	ret
	endp


;--------------------------------------------------------------------------
;	Emulator stop.
;
;	This is called to pause emulation for some reason, such as
;	to perform an interactive task or shell to DOS.
;
;	Also called before final shutdown.
;
;	Only those thingies that got restarted will be restopped.
;
;	There can be no restopping errors.
;
;	Note that we set the bits in one order, so we must 
;	skip initial clear ones.
;--------------------------------------------------------------------------

emustop	proc	near
	pusha
	push	es

	test	emustate,ES_record
	jz	et_00
	call	record_restop
et_00:
	test	emustate,ES_tiemul
	jz	et_01
	call	tiemul_restop
et_01:
	test	emustate,ES_video
	jz	et_12
	call	video_restop
et_12:
	test	emustate,ES_int
	jz	et_10
	call	int_restop
et_10:

	test	emustate,ES_log
	jz	et_02
	call	log_restop
et_02:
	test	emustate,ES_support
	jz	et_03
	call	support_restop
et_03:
	test	emustate,ES_speech
	jz	et_04
	call	speech_restop
et_04:
	test	emustate,ES_debug
	jz	et_05
	call	debug_restop
et_05:
	test	emustate,ES_keyboard
	jz	et_06
	call	keyboard_restop
et_06:
	test	emustate,ES_sound
	jz	et_07
	call	sound_restop
et_07:
	test	emustate,ES_files
	jz	et_08
	call	files_restop
et_08:
	test	emustate,ES_hardware
	jz	et_09
	call	hardware_restop
et_09:
	test	emustate,ES_special
	jz	et_11
	call	special_restop
et_11:
et_out:
	mov	emustate,0

	pop	es
	popa
	clc
	ret
	endp



;--------------------------------------------------------------------------
;	Shutdown.
;
;	The "emustop" routine is called before this one, so don't
;	duplicate stuff unless necessary.
;
;	No registers need to be saved.
;--------------------------------------------------------------------------

emushutdown proc near

	call	special_shutdown
	call	hardware_shutdown
	call	files_shutdown
	call	debug_shutdown

	call	speech_shutdown
	call	sound_shutdown

	call	support_shutdown
	call	log_shutdown

	call	keyboard_shutdown
	call	int_shutdown
	call	video_shutdown

	call	record_shutdown
	call	tiemul_shutdown
	ret
	endp


;===========================================================================

;------------------------------------------------------------------------
;	RESET the emulator's "9900" chip.
;
;	This is only called from a force-emulator-reset, not a BLWP @0.
;
;	þ  Initialize buffers and variables, set up for reset.
;	þ  Read the CPU ROM and GROM and module files, as fast emulation
;	   may have written them over.
;	þ  Read DSR images
;------------------------------------------------------------------------

reset	proc	near
	push	ax
	push	cx
	push	di

	call	reset9901

	call	closeallfiles			; FILES

	and	stateflag,intdebug		; only keep this flag

	cmp	delayamount,0
	jz	resnod

	or	stateflag,delaying		; if delaying, this bit
						; stays set, causing big
						; delay via checkstate
resnod:

;;	mov	sdelay,0
;;	cmp	ontiscreen,0			; we might be in debug mode
;;	je	resnoer
;;	call	erasephrase			; really unnecessary since
						; it's obselete now

resnoer:

	cmp	startdebug,0
	jz	resnodebug
	or	stateflag,debugrequest		; enable this to start
						; debugging immediately
resnodebug:

	mov	es,cpuseg			; clear memory-mapped area
	mov	di,8000h
	xor	ax,ax
	mov	cx,8192/2
	rep	stosw

	mov	es,cpuseg			; clear 'rom'
	mov	di,4000h
	mov	cx,8192/2
	rep	stosw

	mov	es,gplseg
	xor	di,di
	mov	cx,32768
	rep	stosw

	call	loadROMs			; in case lockup overwrote
	jc	reserr
	call	loadparts			; of the current module
	jc	reserr
	call	loaddsrs			; read DSR ROMs
	jc	reserr
	call	reversememory			; Intel byte order
	call	patchCPU			; patch ROM for speedups

	IFDEF	SUPER
	call	setupaddrmap			; setup permissions on R/W
	ENDIF					; throughout address space

	mov	es,cpuseg


	mov	ax,es:[0]
	mov	WP,ax
	mov	ax,es:[2]
	mov	IP,ax				; RESET interrupt

	xor	ax,ax				; force known state
	mov	STAT,ax
	mov	lastval,ax
	mov	lastcmp,ax
       	clc

reserr:
	pop	di
	pop	cx
	pop	ax
	ret
	endp


	IFDEF	SUPER

;	For super-slow emulation, this provides a speedup and also
;	wastes memory.  For every single address in the 64k CPU-addressable
;	memory space, we set a byte to indicate if it is read-only (0),
;	read-write (0FFh), or memory-mapped (7fh).
;
;	This allows a simple check rather than tons of compares on the
;	address and the currently-loaded DSR ROM.
;

setupaddrmap proc near
	mov	es,addrmap
	xor	di,di
	xor	ax,ax				; ROM
	mov	cx,32768
	rep	stosw

	dec	ax				; RAM
	mov	di,2000h
	mov	cx,8192/2
	rep	stosw

	test	selected.memtype,mod_minimem	; Mini Memory?
	jz	samnomm

	mov	di,7000h
	mov	cx,4096/2
	rep	stosw

samnomm:
	mov	di,8000h
	mov	cx,400h/2			; set ALL >8000->83FF as RAM
	rep	stosw

	mov	di,0a000h
	mov	cx,24576/2
	rep	stosw


	mov	ax,7f7fh			; memory-mapped ROM

	cmp	v9t9rom,0
	jnz	samuselow

	mov	di,8000h 			; >8000->82FF are mirrors
	mov	cx,300h/2
	rep	stosw

samuselow:
	test	selected.memtype,mod_banked	; banked ROMs?
	jz	samnobank

	mov	di,6000h			; yes -- so we must trap
	mov	cx,8192/2			; writes to switch banks
	rep	stosw

samnobank:
	mov	di,8400h
	mov	cx,1c00h/2			; all of 8400-9FFF is
	rep	stosw	  			; memory-mapped 
		

	test	features,FE_realdisk		; using DOAD emulation?
	jz	samnodsk

	mov	di,5ff0h			; then we have to trap
	mov	cx,16/2				; writes to FDC registers
	rep	stosw

samnodsk:
	test	features,FE_realrs232+FE_emurs232
	jz	samnors232			; RS232 includes PIO

	mov	di,5000h			; this is PIO write data
	mov	cx,1				; register
	rep	stosw

samnors232:


	.386
	mov	fs,addrmap			; this is why 386 is required
	.286

	ret
	endp

	ENDIF


;---------------------------------------------------------------------------
;	This routine will patch the CPU image according to the PATCHES
;	config variable, which sets a 16-bit word called "patches".
;	The PT_xxxx constants are used to identify the bits.
;
;	Called AFTER memory is byte-swapped.
;---------------------------------------------------------------------------

	.data

patches	dw	PT_reboot+PT_sprites+PT_transfer+PT_int+PT_screenfill

	.code

patchCPU proc	near
	push	ax
	push	es

	mov	v9t9rom,0
	mov	es,cpuseg

	mov	ax,es:[1ffeh]
	cmp	ax,0ed99h		  	; FORTH ROM?
	jne	pc000				; nope, check patching
       	jmp	pc9				; yup

pc000:
	cmp	patches,0			; any patches specified?
	jnz	pc00

	jmp	pcout				; no patches AT ALL	

;-------------------------------------------------------------------------

pc00:

;	First, unpatch all previous versions.

	mov	byte ptr es:[0ah],92h		; really >B
	mov	word ptr es:[2b2h],0c80bh	
	mov	word ptr es:[2bah],04cch
	mov	word ptr es:[65eh],0456h
	mov	word ptr es:[962h],098ch	
	mov	word ptr es:[0a7ch],004ch
	mov	word ptr es:[0abeh],0380h
	mov	byte ptr es:[1ff9h],0		; really >1FF8

;	Apply patches in PATCHES

	mov	ax,patches
	test	ax,PT_keyboard
	jz	pc0

	mov	word ptr es:[2b2h],KEYOP    	; keyboard optimizer
	push	ax
	mov	ax,ROMKeyboardDelay		; set the delay in the CPU ROM
	mov	word ptr es:[49Ah],ax
	pop	ax


pc0:
	test	ax,PT_reboot
	jz	pc1

	mov	word ptr es:[0a7ch],1ff8h
	mov	byte ptr es:[1ff9h],31h		; make fctn+SHIFT+'=' reboot

pc1:
	test	ax,PT_sprites
	jz	pc2

	mov	word ptr es:[962h],SPRITEOP	; optimize sprite motion

pc2:
	test	ax,PT_transfer
	jz	pc3

	mov	word ptr es:[65eh],TRANSOP	; block memory transfer

pc3:
	test	ax,PT_int
	jz	pc4

	mov	word ptr es:[94ch],INT1OP	; optimize INT 1

pc4:
	test	ax,PT_kbdelay
	jz	pc5

	mov	word ptr es:[484h],KEYSLOWOP	; keyoard delay
	push	ax
	mov	ax,ROMKeyboardDelay		; set the delay in the CPU ROM
	mov	word ptr es:[49Ah],ax
	pop	ax

pc5:
	test	ax,PT_screenfill
	jz	pc6

	mov	word ptr es:[5ach],SCREENOP	; fill screen

pc6:
      	jmp	pcout

;---------------------------------------------------------------------------

pc9:
	mov	v9t9rom,1

pcout:
	pop	es
	pop	ax
	ret
	endp



;--------------------------------------------------------------------------
;	Load up basic CPU ROM files, and Speech ROM.
;
;	This function is stuffed into "reset" because our fast
;	emulation may in fact overwrite the ROM sometimes.
;
;	Can't print a diagnostic message here.
;
;	Returns C=1 if file not found, and error set up.
;--------------------------------------------------------------------------

	.data

lr_Err	db	'ROM image % not found or invalid size.',0dh,0ah,0

	.code

loadROMs proc	near
	push	ax
	push	bx
	push	cx
	push	dx
	push	si
	push	di

	lea	si,ROMpath			; get CPU ROM
	lea	bx,TICpuFileName
	mov	es,cpuseg
	mov	cx,8192
	call	readROM
	jc	LRerr

	lea	si,ROMpath			; get GROM
	lea	bx,TIGplFileName
	mov	es,gplseg
	mov	cx,24576
	call	readROM
	jc	LRerr

	test	features,FE_speechROM	    	; we want speech ROM?
	jz	LRskip
	lea	si,ROMpath
	lea	bx,TISpeechFileName
	mov	es,speechseg
	mov	cx,32768
	call	readROM				; get speech ROM
	jnc	LRskip

lrerr:
	lea	dx,lr_Err
	lea	si,filename
	call	setuperror			; otherwise die
	stc

LRskip:
	pop	di
	pop	si
	pop	dx
	pop	cx
	pop	bx
	pop	ax
	ret
	endp



;---------------------------------------------------------------------------
;	GO
;
;	Called once, this routine will start the emulator or the demo
;	executor.
;---------------------------------------------------------------------------

go	proc	near
	test	features,FE_emulating		; emulating or demo?
	jnz	goemul				; emulating

	call	emustart			; setup
	jc	godone

	call	rundemo				; run the program
	jmp	godone

;-------------------------

goemul:
	call	pickamodule			; pick a module
	jc	goerr

	call	emustart			; setup
	jc	godone

	call	reset				; reset 9900
	jc	godone

	jmp	emulate
godone:
	call	emustop
	call	emushutdown
	stc
goerr:
	ret
	endp






;===========================================================================
;	TIEMUL:	Pre-configuration initialization.
;
;	Check system status:
;		þ  High enough DOS version?
;		þ  Correct processor?
;		þ  AT system?
;===========================================================================

tiemul_preconfiginit proc near

IFDEF	BETA
	call	betago			; BETA warning message.
ENDIF

	call	getdosver		; DOS 3.31+ needed
	jc	tpci_out

	call	getcpu	  		; 286+ needed
	jc	tpci_out

;	call	checkAT			; make sure AT+ system
;	jc	tpci_out

	call	resizeprogram		; free extra memory
	jc	tpci_out

	or	features,FE_emulating	; support's readparams may change
tpci_out:
	ret
	endp


;===========================================================================
;	TIEMUL:	Post configuration-file initialization.
;
;	Executed next-to-last in post-config sequence, before RECORD.
;
;	If not executing a demo:
;
;		þ  Get memory
;		þ  Reset module banking
;		þ  Reset memory pointers
;	
;===========================================================================

tiemul_postconfiginit proc near

	test	features,FE_emulating		; doing 9900 emulation,
	clc
	jz	tpciout				; or just a demo?

	call	getCPUmemory	     		; get mem for CPU/GROM
	jc	tpciout				; module/speech ROM

	mov	moduleoffset,0			; init module banking
	mov	ax,moduleseg
	mov	ms_cartmem,ax

	mov	vaddr,0				; initialize memory-mapping
	mov	gaddr,0
	mov	vwrite,0
	mov	gwrite,0
	mov	gread,0
	mov	vdpstat,0
	mov	vwriteoffset,0
  
;;	call	terminatespeech			; initialize speech


tpciout:
	ret
	endp


;===========================================================================
;	TIEMUL:	Restart.
;
;	This restart means continue with emulation.
;	The "restart" procedure should be called to reset the 99/4A.
;===========================================================================

tiemul_restart proc near
	clc
	ret
	endp


;===========================================================================
;	TIEMUL:	Restop.
;===========================================================================

tiemul_restop proc near
	clc
	ret
	endp


;==========================================================================
;	TIEMUL:	Shutdown.
;
;	DOS will free memory for us, so we won't do it.
;==========================================================================

tiemul_shutdown proc near
	clc
	ret
	endp


;==========================================================================
;	TIEMUL:	Final exit.
;
;	þ  Print the error message
;==========================================================================

	.data

backtonormal	db	27,'[0m',8,8,8,8,'    ',0dh,0ah,'$'
error		db	0dh,0ah,'ERROR:',0dh,0ah,'$'
byebye		db	0dh
		db	'			Thanks for using',0dh,0ah
		db	0dh,0ah
		db	'		  	ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ',0dh,0ah
		db	'			V9t9:  TI Emulator! v6.0.',0dh,0ah
		db	'			ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ',0dh,0ah
		db	0dh,0ah
		db	'	This program is fairware.',0dh,0ah
		db	'	It may be distributed and used without charge.',0dh,0ah
		db	0dh,0ah
		db	'	Send bug reports and questions to:',0dh,0ah
		db	0dh,0ah
		db	'		edswartz@io.com    or  swartze@southwestern.edu',0dh,0ah
		db	0dh,0ah
		db	'	Send orders to:',0dh,0ah
		db	0dh,0ah
		db	'		Edward Swartz',0dh,0ah
		db	'		1401 E. 18th Street',0dh,0ah
		db	'		Georgetown, TX  78626',0dh,0ah
		db	0dh,0ah
		db	'	See V9t9.TXT, CONTACT.TXT, and FAIRWARE.TXT for more information.',0dh,0ah
		db	'$'

	.code

tiemulexit proc near

;	test	stateflag,happymessage		; simply Ctrl+Break?
;	jnz	tenoterror
	cmp	happymessage,0
	jnz	tenoterror

	lea	dx,error			; nope... print "ERROR!!!"
	call	dosprint

	lea	dx,errormessage			; print error message
	call	dosprint			
	jmp	teexit

tenoterror:
	lea	dx,byebye			; say bye-bye
	call	dosprint			

teexit:
	mov	ah,4ch				; DOS terminate
	mov	al,0
	int	21h

	ret					; should never be reached
	endp




;---------------------------------------------------------------------------
;	Get DOS version.
;
;	Must be 3.31 or above.
;---------------------------------------------------------------------------

	.data

gdv_err	db	'V9t9 requires MS-DOS version 3.31 (or DR-DOS 6.0) or higher.',0dh,0ah,0

	.code

getdosver proc	near
	mov	ax,3000h
	int	21h
	cmp	al,3
	jb	gdvlow
	ja	gdvok
	cmp	ah,31
	jae	gdvok
gdvlow:
	lea	dx,gdv_err
	call	setuperror
	stc
	jmp	gdvout
gdvok:
	clc
gdvout:
	ret
	endp


;---------------------------------------------------------------------------
;	Get CPU type.
;
;	Must be 286 or above.
;
;	Sets "processor" to 86, 286, 386, 486, or 586.
;
;	Adapted from "the Undocumented PC" by Frank Van Gilluwe
;---------------------------------------------------------------------------

	.data

gc_err	db	'V9t9 requires a 286 or higher.',0dh,0ah,0

	.code

gc_int1 proc	far
	iret
	endp


;	Get CPU type.
;	Returns 86,186,286, or 386.
;

;	This code may trip your debugger, so be prepared to skip over it.
;
getcpu	proc	near
	pushf
	xor	ax,ax
	push	ax
	popf
	pushf
	pop	ax
	and	ax,8000h
	cmp	ax,8000h
	je	gctest86
	mov	ax,7000h
	push	ax
	popf
	pushf
	pop	ax
	and	ax,7000h
	je	gcis286
	mov	ax,386
	jmp	gcpout
gcis286:
	mov	ax,286
	jmp	gcpout
gctest86:
	mov	ax,0ffffh
	mov	cl,21h
	shl	ax,cl
	jne	gcis186
	mov	ax,86
	jmp	gcpout
gcis186:
	mov	ax,186
gcpout:
	popf

	mov	processor,ax

	IFDEF	T386
	cmp	ax,386
	ELSE
	cmp	ax,286			
	ENDIF

	jae	gcok

	lea	dx,gc_err
	call	setuperror
	stc

gcok:
	ret
	endp


;---------------------------------------------------------------------------
;	Check for an AT system 
;
;	þ  Do indirect check for AT keyboard using BIOS calls
;	þ  Check BIOS revision for AT-ness or PS/2-ness
;---------------------------------------------------------------------------

	.data

cAT_kerr db	'System doesn''t appear to have an enhanced (101/102 key) keyboard.',0dh,0ah,0
cAT_serr db	'This doesn''t appear to be an AT or PS/2 system.',0dh,0ah,0

	.code

checkAT	proc	near
	mov	ax,40h
	mov	es,ax
	mov	al,es:[96h]
	test	al,16			; 101/102 key keyboard
	clc
	jnz	cATsys

	mov	ah,12h
	int	16h			; get extended shift flag status
	cmp	al,es:[17h]		; same as BIOS value?
	je	cATsys			; yup... function implemented

	lea	dx,cAT_kerr
	call	setuperror
	stc
	jmp	cATout


cATsys:
	mov	ah,0c0h
	int	15h			; get BIOS config data es:bx
	jnc	cATsys00		; supported?

	mov	ax,0f000h		; hopefully BIOS call worked because
	mov	es,ax			; systems with memory managers might
	mov	dx,es:[0fffeh]		; alter these values
	jmp	cATsys01

cATsys00:
	mov	dl,es:[bx+2]
	mov	dh,es:[bx+3]		; retrieve bytes

cATsys01:
	cmp	dl,0ffh			; PC
	je	cATserr		
	cmp	dl,0feh
	je	cATserr			; PC/XT
	cmp	dl,0fbh
	je	cATserr			; PC/XT
	cmp	dl,0fdh
	je	cATserr			; PCjr
	cmp	dx,02fch	
	je	cATserr			; PC/XT-286
	cmp	dl,0f9h			
	je	cATserr			; PC "convertible"
	clc
	jne	cATout			; others should be PC/AT or PS/2

cATserr:
	lea	dx,cAT_serr
	call	setuperror
	stc

cATout:
	ret
	endp


;---------------------------------------------------------------------------
;	Resize program.
;
;	Gimme .EXE file function.
;---------------------------------------------------------------------------

	.data

rp_err	db	'DOS memory chain is broken!',0dh,0ah
	db	'Reboot your system...',0dh,0ah,0

	.code

resizeprogram 	proc near
	mov     bx,sp
	add     bx,15    		; convert SP into paragraphs
	shr     bx,4
	mov     ax,ss    		; calculate size of program using PSP
	add     bx,ax
	mov     ax,psp
	sub     bx,ax
	mov     ah,4Ah			; resize memory block with PSP
	mov	es,psp
	int     21h      		; address in ES
	jnc	rpout			; error?

	lea	dx,rp_err
	call	setuperror
	stc

rpout:
      	ret
	endp


;--------------------------------------------------------------------------
;	Get memory for CPU ROM, GROM, and Speech ROM (if necessary)
;
;	If the "tispeechfilename" is blank, we have no speech ROM
;	and therefore no memory is allocated for it.
;
;	Returns C=1 if lack of memory, and error set up.
;--------------------------------------------------------------------------

	.data

gcm_msg	db	'Allocating memory for CPU...',0dh,0ah,'$'
gcm_Err db	'Not enough memory for basic CPU memory!',0dh,0ah,0

	.code

getCPUmemory proc near

	lea	dx,gcm_msg
	call	dosprint

	IFDEF	SUPER

	mov	ah,48h			; SUPER emulation must have 
	mov	bx,1000h		; 64k block for address checks
	int	21h
	jc	gcmerr
	mov	addrmap,ax

	ENDIF


	mov     ah,48h			
	mov     bx,1000h
	int     21h			; get 64k
	jc      gcmerr
	mov	cpuseg,ax		; CPU RAM/ROM
	mov	ms_rom,ax

	mov     ah,48h
	mov     bx,1000h		
	int     21h			; get 64k
	jc      gcmerr
	mov     GplSeg,ax               ; GROM block

	mov	ah,48h
	mov	bx,16384/16		; get 16k
	int	21h
	jc	gcmerr
	mov	moduleseg,ax		; module banks block >6000->7FFF
	mov	ms_cartmem,ax		; (two banks of 8k)

	cmp	byte ptr tispeechfilename,0
	jz	gcmout
	or	features,FE_speechROM	; we have speech ROM!

	mov	ah,48h
	mov	bx,32768/16		; get 32k
	int	21h
	jc	gcmerr
	mov	speechseg,ax		; speech segment
	clc
      	jmp	gcmout

gcmerr:
	lea	dx,gcm_err
	call	setuperror
	stc

gcmout:	
	ret
	endp


;==========================================================================


;===========================================================================


;---------------------------------------------------------------------------
;	Read configuration files.
;
;	Read MODULES.INF first, then V9t9.CNF.
;---------------------------------------------------------------------------

	.data

egc_modules db	'Reading module database...',0dh,0ah,'$'
egc_config db	'Reading configuration file...',0dh,0ah,'$'

	.code

emugetconfig proc near

	lea	dx,egc_modules
	call	dosprint
	call	readmodulefile			; read module info file
	jc	egcerr

	lea	dx,egc_config
	call	dosprint
	call	readconfigfile			; read configuration

egcerr:
	ret
	endp


	call	stoplogging			; ??? in case is was going?

	ret
	endp



;--------------------------------------------------------------------------
;	Reverse CPU memory block
;
;	9900 and 90x86 have opposite byte-orders.  Since most of the
;	instructions are word instructions, all the 16-bit ROM and RAM
;	(CPU ROM/RAM/module ROM) are stored in 80x86 byte-order to
;	have quick access.  Byte instructions, however, must XOR their
;	addresses by 1 before proceeding.
;
;	Since this is called elsewhere, we need to reverse all the memory,
;	like it or not.
;--------------------------------------------------------------------------

ReverseMemory   proc    near

	mov     cx,32768		; 64k
	mov	es,cpuseg
	call	swab			; reverse ROM

	mov	cx,16384/2		; 16k
	mov	es,moduleseg
	call	swab	    		; reverse module ROM

	mov	cx,dsrseg_realdisk
	jcxz	RMnot00
	mov	es,cx
	mov	cx,8192/2		; 8k
	call	swab

rmnot00:
	mov	cx,dsrseg_emudisk
	jcxz	rmnot01
	mov	es,cx
	mov	cx,8192/2		; 8k
	call	swab

rmnot01:
	mov	cx,dsrseg_realrs232
	jcxz	rmnot02
	mov	es,cx
	mov	cx,8192/2		; 8k
	call	swab

rmnot02:
	mov	cx,dsrseg_emurs232
	jcxz	rmnot03
	mov	es,cx
	mov	cx,8192/2		; 8k
	call	swab

rmnot03:

	push	ds			; copy current module segment to
					; cpu ROM at >6000
	mov	es,cpuseg
	mov	di,6000h
	mov	ds,ms_cartmem
	mov	si,0
	mov	cx,8192/2
	rep	movsw
	pop	ds

	ret
	endp


;	SWAB --		Swap bytes
;
;	Inputs:		ES:DI=start, CX=# words
;
swab	proc	near
	xor     di,di
swabloop:     
	mov     ax,es:[di]
	xchg    ah,al
	mov     es:[di],ax
	add	di,2
	loop    swabloop
	ret
	endp




;	Emulator!
;	These registers will be used:
;
;	AX = temp
;	BX = source/destination address
;	CX = shift count/temp
;	DX = status	(only interrupt mask now)
;	SI = opcode
;	DS = VDP 
;	ES = CPU


emulate	proc	near

	NEXT				; find in EMULATE.INC

	endp







;	HANDLEINTERRUPT will try to process an interrupt waiting in
;	CURRENTINTS.
;
;	ONLY "interruptoccuring" set by handle9901 should cause
;	this routine to be called.
;
;	The action of initiating an interrupt relaxes the INT pin
;	on the 9901, so here we reset the bit in "currentints" for
;	which we are initiating the interrupt.
;
;	Call handle9901 again in case other interrupts are pending.
;
;	Called from CHECKSTATE due to a condition set by HARDWARE.ASM.
;

	.data

himsg	db	'Initiating interrupt at %4h/%4h/%4h%n',0

	.code

handleinterrupt proc	near
	push	ax
	push	bx
	push	cx
	push	si

	mov	ax,1			; find which interrupt tripped us
	mov	cx,16
hifind:
	test	currentints,ax		; figure out which one
	jnz	hireset
	add	ax,ax
	loop	hifind

hireset:
	or	handledints,ax		; handled

	LOG3	LG_tiemul,himsg,IP,WP,STAT

	mov	bx,4			; INT 1 is the only level we 
					; recognize

	mov	si,WP			; preserve

	mov	ax,es:[bx]		; get the vector's WP
	and	ax,not 1
	mov	WP,ax

	mov	es:[WP+R13],si		; context switch
	mov	ax,IP
	mov	es:[WP+R14],ax
	SETSTAT				; copy broken-up status bytes into
					; STAT
	mov	es:[WP+R15],dx		; should be "STAT" (equates to DX)

hinoblwp:
	mov	ax,es:[bx+2]
	and	ax,not 1
	mov	IP,ax			; get vector's IP



	call	testWP			; make sure we don't try to have
					; R/O vector space
					; this is ONLY used here

hiout:
	pop	si
	pop	cx
	pop	bx
	pop	ax
	ret
	endp


;	Make sure workspace pointer is legal.
;
;	This is ONLY called from handleinterrupt, and is
;	sort of useless, as a fixed vector from ROM provides the
;	WP that is checked.
;
;	"validateWP" is in MEMORY.INC or EMULATE.INC and is used
;	for general-purpose WP switches.

testwp	proc	near
	cmp	wp,0ffe0h
	ja	twnok
	cmp	wp,0a000h
	jae	twok
	cmp	wp,83e0h
	ja	twnok
	cmp	wp,8300h
	jae	twok
	cmp	wp,2000h
	jae	twok
twnok:
	or	stateflag,ctrlbreakpressed	; ** FIX ** say "emulated prog lockup"
twok:
	ret
	endp



;xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

	IFNDEF	LPCSPEECH

;	LPCSPEECH is always defined, so ignore this.
;	This is only used in TI Emulator v5.0.

;	DOSPEAK
;
;	Do delay operation on written phrase of speech
;
dospeak	proc	near
	push	ax

	mov	ax,sdelay		; is speak timer done?
	or	ax,ax
	jz	ds0			; yup
	dec	sdelay			; else decrement
	cmp	ax,maxspeechdelay
	jne	dsout
	cmp	ontiscreen,0
	jz	dsout
	call	drawphrase
	jmp	dsout
ds0:
	and	stateflag,not speaking
	cmp	ontiscreen,0
	jz	dsout
	call	erasephrase
dsout:
	pop	ax
	ret
	endp
	ENDIF

;xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx


;=========================================================================
;
;	CHECKSTATE
;
;=========================================================================
;
;	Check the STATEFLAG variable and do stuff.
;
;	Could be called between every instruction by "execute" 
;	(in EMULATE.INC) if certain bits in STATEFLAG are set
;	(those in the "checkable" mask, see STRUCS.INC).
;
;=========================================================================


;	Divided into "soft" state and "hard" state changes.
;	The biggest plus for this scheme is that when a demo is
;	running, only "soft" state changes matter anyway, and 
;	only they have to be checked.
;

checkstate:
	call	checksoftstate		; check user functions

	test	ax,delaying
	jz	cs0000


;-------------------------------------------------------------------------
;	The user has asked the emulator to DELAY between instructions.
;	This flag stays selected until the delay rate changes.
;-------------------------------------------------------------------------

	mov	cx,delayamount
	loop	$

cs0000:
	test	ax,debugrequest
	jz	cs0002

;--------------------------------------------------------------------------
;	It's time to process a DEBUG REQUEST.	
;--------------------------------------------------------------------------

	IFDEF	DEMO		  	; ignore this when demoing
	test	features,FE_emulating
	jz	cs0001_1
	ENDIF

	SETSTAT
	call	debug
	GETSTAT
cs0001_1:


cs0002:
	test	ax,interruptoccuring
	jz	cs0003

;---------------------------------------------------------------------------
;	A hardware INTERRUPT has occurred.
;---------------------------------------------------------------------------

	call	handleinterrupt

	IFDEF	COMPROM
	CHECKROM
	ENDIF

cs0003:
	jmp	nostate


;=========================================================================
;
;	CHECKSOFTSTATE checks conditions set by the user or the 1/60
;	timer.  This is most useful because when a demo is running, 
;	only these need to be checked.
;
;-------------------------------------------------------------------------
;=========================================================================

	.data

cs_stopdemo db	'Demo interrupted.',0dh,0ah,0

	.code

checksoftstate 	proc	near

	mov	ax,stateflag
	test	ax,ctrlbreakpressed+reboot
	jz	csthread

;-------------------------------------------------------------------------
;	Terminal conditions.
;-------------------------------------------------------------------------

	test	ax,ctrlbreakpressed+reboot
	jz	csthread

IFDEF	DEMO
	call	democlose
ENDIF

	add	sp,2			; lose return address from CHECKSTATE

	test	al,ctrlbreakpressed
	jnz	csctrlbreak

	test	al,reboot
	jnz	csreboot

;-------------------------------------------------------------------------
;	CTRL-BREAK was pressed or some other error terminates the program.
;-------------------------------------------------------------------------

csctrlbreak:

IFDEF	DEMO	
	test	features,FE_emulating	; were we running a demo?
	jnz	csterminate
	lea	dx,cs_stopdemo		; yup... say "stopped demo"
	call	setuperror
;	or	stateflag,happymessage
	mov	happymessage,1
ENDIF

csterminate:
	call	die			; end program

;-------------------------------------------------------------------------
;	The user REBOOTed with Shifts+Ctrl+Alt+'='.
;-------------------------------------------------------------------------

csreboot:
	call	reset
	jc	csterminate
	jmp	emulate


;-------------------------------------------------------------------------
;	NON-TERMINAL conditions.
;-------------------------------------------------------------------------

csthread:


cs1000:
	test	ax,paused
	jz	cs1002

;-------------------------------------------------------------------------
;	We're paused here!  Another PAUSE will reset...
;
;	Only allow special functions to be performed.
;-------------------------------------------------------------------------

	mov	ax,stateflag
	test	ax,specialfunctionrequest
	jnz	cs1005
	test	ax,ctrlbreakpressed
	jnz	cs1000_1
	test	ax,paused
	jnz	cs1000				; still paused?
	jmp	cs1002			       	; if not, catch up on others

cs1000_1:
	jmp	checksoftstate		       


cs1002:
	test	ax,sixtieth
	jz	cs1003


;-------------------------------------------------------------------------
;	The 1/60 second flag has ticked.
;-------------------------------------------------------------------------

	call	toggler				; PC tone toggle

	and	stateflag,not sixtieth


cs1003:
	test	ax,titick
	jz	cs1004

;-------------------------------------------------------------------------
;	TI interrupt tick happened.
;	
;	This is independent of "interruptoccuring" because demos need 
;	exact ticking times unaffected by timers (like for speech,
;	cassette, etc.)
;-------------------------------------------------------------------------

	IFDEF	DEMO
	test	features,FE_emulating
	jz	cs1004_1
	test	ax,demoing
	jz	cs1004_1

	call	dtimerint			; write 1/"60" marker to demo

cs1004_1:
	ENDIF	
	
	and	stateflag,not titick

cs1004:
	test	ax,videointoccured
	jz	cs1005

;---------------------------------------------------------------------------
;	VIDEO redraw requested.
;---------------------------------------------------------------------------

	cmp	ontiscreen,0
	jz	cs1005
	and	stateflag,not videointoccured
	call	updatevideoscreen	; won't draw unless ontiscreen<>0


cs1005:
	test	ax,specialfunctionrequest
	jz	cs1007

;---------------------------------------------------------------------------
;	A SPECIAL FUNCTION key has been pressed.
;---------------------------------------------------------------------------

	and	stateflag,not specialfunctionrequest
	call	handlespecialfunctions

	call	clearkeyboard

cs1007:
	IFNDEF	LPCSPEECH
	test	ax,speaking
	jz	cs1008

;--------------------------------------------------------------------------
;	The SPOKEN WORD on the screen is timing out.
;--------------------------------------------------------------------------

	call	dospeak
	ENDIF

cs1008:
	ret

	endp


;====================================================================
;
;	Some ROM speedups.  Certain areas of the 99/4A CPU ROM
;	are translated into 80x86 code here to gain more speed.
;	On my 386SX/20 it was very important to do this, as the
;	interrupt handler below would often take up 90% of the execution
;	time!
;
;	These routines are ARCHAIC.  I don't use this style anymore.
;	(i.e., directly reading RAM, the use of "readmemorymapped", etc.)
;	
;	See the macros in MEMORY.INC for cleaner memory access.
;
;====================================================================


;	Emulated INT 1 controller
;	at >94C
;

doint1	proc	near
	push	ax

	mov	al,es:[83c3h]
	add	al,al
	jnc	di1

	CARRY	

	jmp	di4
di1:
	add	al,al
	jc	di2
	call	movesprites
di2:
	add	al,al
	jc	di3
	call	dosound
di3:
	add	al,al
	jc	di4
	push	ax
	mov	dx,0024h
	xor	ax,ax
	mov	cx,3
	call	writeseveralcru
	mov	dx,0006h
	xor	ax,ax
	mov	cx,8
	call	readseveralcru
	push	bx
	mov	bl,31h			; fctn+shift+plus
	test	patches,PT_reboot
	jnz	di30
     	mov	bl,11h			; fctn+plus
di30:
	not	ah
	and	ah,bl
	cmp	ah,bl
	pop	bx
	pop	ax
	jne	di4
	mov	IP,0a80h
	jmp	diout
di4:
	mov	ah,vdpstat
	mov	es:[837ah],ah
	mov	wp,83c0h
	add	word ptr es:[wp+R11],2
	jne	di5
	mov	IP,0a92h
	jmp	diout
di5:
	mov	wp,83e0h
	mov	ah,es:[wp+R14+1]
	add	byte ptr es:[8378h],ah
	mov	ax,es:[83c4h]
	or	ax,ax
	jz	di6
	mov	es:[wp+R12],ax
	mov	IP,0ab6h
	jmp	diout
di6:
	mov	word ptr es:[wp+R8],0
	mov	wp,83c0h
	mov	IP,0abeh
diout:
	pop	ax
	ret
	endp


;-------------------------------------------------------

;	Handle sound-list processing
;

dosound	proc	near
	pusha
	mov	dl,es:[83cfh]
	or	dl,dl		; dl=r2
	jnz	ds00
	jmp	dslout
ds00:
	mov	ah,es:[83e0h+R14+1]
	sub	es:[83cfh],ah
	jz	ds01
	jmp	dslout
ds01:
	mov	bx,es:[83cch]	; di=R3
	mov	ax,es:[83e0h+R14]
	shr	ax,1
	jc	ds1

;	GPL sound list

	push	gaddr		; save
	push	9c02h
	mov	gaddr,bx
	mov	di,es:[83e0h+R13]	; bx=R6
	jmp	ds2

ds1:
	mov	vaddr,bx
	push	8c02h
	mov	di,8800h	; bx=R6
ds2:
	xor	di,1
	call	readmemorymapped
	mov	cl,al
	or	cl,cl		; cl=r8
	je	ds3		; a46
	cmp	cl,0ffh
	je	ds4		; a42
	xor	ch,ch
	add	bx,cx
ds5:
	push	cx
	call	readmemorymapped
	push	di
	mov	di,8401h
	call	writememorymapped
	pop	di
	pop	cx
	dec	cx
	jne	ds5
	add	bx,2
	call	readmemorymapped 
	mov	dl,al
	or	dl,dl
	je	ds6		; a52
	jmp	ds7		; a54
ds4:
	mov	ax,es:[378h]
	xor	es:[83e0h+R14],ax
ds3:
	call	readmemorymapped
	mov	ch,al
	push	cx
	call	readmemorymapped
	pop	cx
	mov	cl,al
	mov	bx,cx
	mov	dl,1
	jmp	ds7
ds6:
	mov	dl,0
ds7:
	mov	es:[83cch],bx
	mov	es:[83cfh],dl
	pop	di
	cmp	di,8c02h
	je	ds8
	pop	gaddr
ds8:
dslout:
	popa
	ret
	endp
	








	end	tiemulator
