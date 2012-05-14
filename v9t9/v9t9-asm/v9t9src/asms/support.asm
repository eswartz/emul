; ***************************************************
; SUPPORT.ASM  V9t9 support file routines
; ***************************************************
; by Edward Swartz  4/30/1994
; ***************************************************


;	This module provides a hodgepodge of useful string, 
;	config file, error message, etc., routines.
;


_SUPPORT_ = 1

	include	standard.h
	include	tiemul.h
	include	video.h
	include	special.h
	include	int.h
	include	hardware.h
	include	files.h
	include	sound.h
	include	keyboard.h
	include	debug.h
	include	speech.h
	include	record.h
	include	log.h

	include	support.h


	.data


nullpath	db	0
backspaces	db	8,8,8,8,8,8,8,'        ',0

startuppath	db	64 dup(?)
filename	db	80 dup(?)
errormessage	db	'$',159 dup(?)
line		db	160 dup(?)

root		db	':\',0
configfilename	db	'V9t9.CNF',0
uconfigfilename	db	64 dup (0)
moduleinfname	db	'MODULES.INF',0
umoduleinfname	db	64 dup (0)

e_romnotfound	db	'Needed ROM file % not found.',0
e_romfileshort	db	'ROM file % is too short.',0

e_noconfigmem	db	'Could not allocate 8k for configuration file.',0
e_nomoduleinfmem db	'Could not allocate 8k for module information file.',0
e_nomodulemem	db	'Could not allocate 8k for module database.',0
e_confignotfound db	'Configuration file % not found.',0
e_moduleinfnotfound db	'Module information file % not found.',0
e_filetoobig	db	'Configuration files must be less than 8k.',0

e_parselineis	db	'In line:  ',0dh,0ah,0
e_crlf		db	0dh,0ah,0dh,0ah,0
e_parsenoequals	db	'There is no "=" after the field name.',0
e_parsebadnumber db	'Number expected.',0
e_parsebadbyte	db	'Number should be less than 256.',0
e_parsebadboolean db	'Invalid boolean value.  Legal:  on,off,true,false,yes,no.',0
e_parsenodrive	db	'There must be a drive specified (eg C:).',0
e_parsebaddrive	db	'The drive specification is invalid.',0
e_parsebadfield	db	'Invalid variable name.',0
e_parsebadpath	db	'Path specification should be less than 60 chars.',0
e_parsebadfilename db	'Filename should be at most 12 characters.',0

e_parsenoquote	db	'String missing leading quote mark (").',0
e_parsetrunc	db	'String missing trailing quote mark (").',0
e_parsetoolong	db	'String should at most 32 characters.',0
e_parsebadbase	db	'Bad base name for module file.',0dh,0ah,'Should be at most 7 characters, with no extension.',0
e_parsebadword	db	'Undefined word in list.',0dh,0ah,0

e_parsetoohigh	db	'Module number exceeds number of modules in MODULES.INF.',0
e_parsetoomany	db	'More than 32 modules selected.',0

e_parsetoofewjoy db	'Three arguments are required in the list.',0
e_parsers232err	db	'An RS232 entry should be in the form "<port #>,<IRQ>".',0
e_parsepioerr	db	'A PIO entry should be 1-4.',0
e_parselongext	db	'Extension should be at most 3 characters long.',0

e_parmnotype	db	'Command-line parameter has a bad type.  Run V9t9 /? for help.',0
e_needafile	db	'A filename is required after the parameter.',0dh,0ah,0


	even


;	Buffer for configuration file reading

bufseg		dw	0
buflen		dw	0
bufpos		dw	0


;-----------------------------------------------------------------
;	Configuration file table
;	------------------------
;	
;	This is a hash table for the V9t9.CNF configuration file
;	variables.  Each variable is encoded into a 16-bit number
;	(ignoring case) and matched with this list.
;	
;	Format:
;
;		Encoded number, routine to handle parsing, pointer to var
;		Zero is end of table
;
;	DO NOT USE PWORD AS A TYPE!  THAT IS NOT A TYPE!
;
;
configtable	dw	50936,PByte,videoredrawlatency	; VIDEOUPDATESPEED
		dw	1212,PByte,videoredrawmaxdelay	; VIDEOUPDATEMAXDELAY
		dw	16410,PInt,delayamount		; DELAYBETWEENINSTRUCTIONS
		dw	26730,PRelPath,ROMpath		; ROMSPATH
		dw	23926,PRelPath,modulespath 	; MODULESPATH
		dw	43135,PPath,tidiskpathname	; DSK1PATH
		dw	51327,PPath,tidiskpathname+64	; DSK2PATH
		dw	59519,PPath,tidiskpathname+128	; DSK3PATH
		dw	02175,PPath,tidiskpathname+192 	; DSK4PATH
		dw	10367,PPath,tidiskpathname+256	; DSK5PATH
		dw	04090,PFileName,disk1name	; DISKIMAGE1
		dw	03706,PFileName,disk2name	; DISKIMAGE2
		dw	03834,PFileName,disk3name	; DISKIMAGE3
		dw	08506,PRelPath,diskpath		; DISKIMAGEPATH

		dw	17947,PRelPath,demopath		; RecordedDemosPath

		dw	39066,PBoolean,keyboardledset	; SETKEYBOARDLED
		dw	05320,PRelPath,speechpath      	; DIGITIZEDSPEECHFILESPATH
		dw	30061,PInt,timerdelay		; TIMERSPEED
		dw	36329,PBoolean,diskled		; SHOWDISKLED
		dw	15273,PBoolean,rs232led		; SHOWRS232LED
		dw	47577,PBoolean,emudiskled	; SHOWEMUDISKLED
		dw	45506,PByte,mouseemulationtype	; MOUSEEMULATIONTYPE
		dw	55406,PInt,ROMKeyboardDelay	; ROMKEYBOARDDELAY

		dw	15515,PJoyBounds,0		; Joystick1Bounds
		dw	16283,PJoyBounds,1		; Joystick2Bounds

		dw	34603,PByte,sbirq		; SoundBlasterIRQ
		dw	34627,PByte,sbdma		; SoundBlasterDMA
		dw	54134,PInt,sync			; NoiseSync
		dw	52342,PByte,noisetop		; NoiseTop

		dw	25003,PFileName,ticpufilename	; CPUROMFILENAME
		dw	08617,PFileName,tigplfilename	; GPLROMFILENAME
		dw	13406,PFileName,tispeechfilename ;SPEECHROMFILENAME

		dw	42867,PInt,maxclockhertz	; MaximumInterruptSpeed

		dw	47722,PBoolean,check5sprites	; CHECK5SPRITES
		dw	37215,PBoolean,checkspritecoinc ; CHECKSPRITECOINC

		dw	49216,PByte,kbdelay		; KeyboardDelay

		dw	30817,PAndBoolean,vga  		; UseVga

		dw	33910,PModuleList,moduleslist	; Modules
		dw	45033,PByte,defaultmodule 	; DefaultModule

		dw	42091,PGetMasks,soundstruc 	; Playsound
		dw	52331,PBoolean,silence		; Silence
		dw	47609,PByte,pcspeakersilence 	; PcSpeakerSilenceLevel

		dw	19606,PFileName,diskdsrname	; DiskDSRFileName
		dw	02532,PFileName,emudiskdsrname  ; EMUDISKDSRFILENAME
		dw	10585,PFileName,shareddiskdsrname  ; SharedDISKDSRFILENAME
		dw	62983,PFileName,rs232dsrname	; RS232DSRFileName
		dw	16054,PFileName,emurs232dsrname	; EmuRs232DSRFilename

		dw	12506,PBoolean,preventmultipleinterrupts 
							; PreventMultipleInterrupts
		dw	33051,PBoolean,debugint		; InterruptTracing

		dw	64627,PRSList,rs1		; RS232/1
		dw	61555,PRSList,rs2		; RS232/2
		dw	45153,PPioList,pio1	 	; PIO/1
		dw	32865,PPioList,pio2  		; PIO/2

		IFDEF	COMPROM
		dw	50706,PRelPath,compiledpath 	; CompiledROMsPath
		dw	51898,PFileName,compiledrom 	; CompiledROM
		ENDIF

		dw	18541,PFileName,speechexcite  	; SpeechExcitation
		dw	10357,PFileName,speechpitches	; SpeechPitches
		dw	51746,PBoolean,uselpc		; UseLPCSpeech

		dw	33003,PGetMasks,patchstruc	; ROMPatches
		dw	23669,PGetMasks,logstruc	; Logging

		dw	28278,PGetMasks,dsrstruc	; DSRCombo

		dw	52336,PGetExt,modextension	; DefaultModuleExtension

		dw	24139,PBoolean,startdebug	; StartupDebugging

		dw	0


;======================================================================

;	Below are assorted other hash tables.  The names are still
;	encoded in the same way as above, but the associations
;	are different.

;-----------------------------------------------
;	Match boolean values

Boolmatch	dw	104,0ffffh	; on
		dw	57440,0ffffh	; true
		dw	16495,0ffffh	; yes
		dw	32893,0		; off
		dw	45179,0		; false
		dw	32873,0		; no
		dw	0

;-------------------------------------------------
;	Match module memory types

Memmatch	dw	49254,mod_rom		; ROM
		dw	57440,mod_rom		; ROM1
		dw	32864,mod_rom2		; ROM2
		dw	04222,mod_rom+mod_rom2	; BANKED
		dw	24692,mod_grom		; GROM
		dw	61555,mod_minimem	; MMRAM
		dw	0

;----------------------------------------------
;	Match wanted/unwanted sound devices

soundstruc	dw	soundmatch,playsound,soundcard

soundmatch	dw	60520,pcspeakermask		; PCSPEAKER
		dw	125,adlibmask			; ADLIB
		dw	111,sblastermask		; SBLASTER
		dw	45166,sbdmamask   		; SBDMA
		dw	0

;--------------------------------------------------
;	Match ROM patch types

patchstruc	dw	patchmatch,patches,allpatch

allpatch	dw	0ffffh				; legal masks
patchmatch	dw	34245,PT_keyboard		; EMULATEKEYBOARDROM
		dw	32864,PT_keyboard		; EK
		dw	48619,PT_reboot			; SHIFTREBOOT
		dw	00122,PT_reboot			; SR
		dw	02272,PT_sprites     		; SPRITEMOTION
		dw	32885,PT_sprites		; SM
		dw	05875,PT_transfer		; MEMORYMOVE
		dw	32875,PT_transfer		; MM
		dw	39807,PT_int			; FASTINTERRUPT
		dw	32866,PT_int			; FI
		dw	43219,PT_kbdelay		; SLOWDOWNKEYBOARD
		dw	32886,PT_kbdelay		; SK
		dw	24681,PT_screenfill		; SCREENFILL
		dw	00112,PT_screenfill		; SF

		dw	00116,0ffffh			; ALL

		dw	0

;-----------------------------------------
;	Match logging types (must run as V9t9 /L to use)

logstruc	dw	logmatch,islogging,alllog

alllog		dw	0ffffh
logmatch	dw	01144,LG_tiemul			; EMULATION
		dw	53359,LG_video			; VIDEO
		dw	08819,LG_video			; GRAPHICS
		dw	27475,LG_special		; SPECIALFUNCTIONS
		dw	39793,LG_special		; FUNCTIONS
		dw	39413,LG_int			; INTERRUPTS
		dw	14962,LG_hardware		; HARDWARE
		dw	04420,LG_files			; FILES
		dw	25721,LG_files			; EMUDISK
		dw	47216,LG_keyboard		; KEYBOARD
		dw	53368,LG_debug			; DEBUG
		dw	36964,LG_speech			; SPEECH
		dw	26745,LG_support		; CONFIG
		dw	08316,LG_record			; DEMO
		dw	16498,LG_rs232			; RS232
		dw	16487,LG_pio			; PIO
		dw	08317,LG_disk			; DISK
		dw	00116,0ffffh			; ALL
		dw	0

;------------------------------
;	Match DSR ROM types

dsrstruc	dw	dsrmatch,features,alldsrs
alldsrs		dw	0ffffh

dsrmatch	dw	25721,FE_emudisk		; EmuDisk
		dw	37486,FE_realdisk		; RealDisk
		dw	21118,FE_emudisk+FE_realdisk	; BothDisk
		dw	34936,FE_emurs232		; EmuRs232
		dw	25710,FE_realrs232		; RealRS232
		dw	0

    	.code

;===========================================================================
;	SUPPORT:	Pre-config initialization
;
;	þ  Get the path from which V9t9.EXE was executed.
;	þ  Read command-line parameters.
;===========================================================================

support_preconfiginit proc near
	call	getexecpath
	call	readparams

	ret
	endp


;===========================================================================
;	SUPPORT: post-config initialization
;===========================================================================

support_postconfiginit proc near
	clc
	ret
	endp


;===========================================================================
;	SUPPORT: Restart.
;===========================================================================

support_restart proc near
	clc
	ret
	endp


;===========================================================================
;	SUPPORT: Restop.
;===========================================================================

support_restop proc near
	clc
	ret
	endp


;===========================================================================
;	SUPPORT: Shutdown.
;===========================================================================

support_shutdown proc near
	clc
	ret
	endp



;--------------------------------------------------------------------------
;	Get V9t9.EXE path
;--------------------------------------------------------------------------

getexecpath	proc	near
	push	ax
	push	di
	push	si
	push	cx
	push	bx
	push	es

	mov	es,psp
	mov	es,es:[2ch]		; get environment block
	xor	di,di
	xor	al,al
	mov	cx,-1			; 64k
gepfindend:
	repne	scasb			; find eos char
	cmp	es:[di+1],al		; is next char null too?
	jne	gepfindend

	add	di,2			; skip envir length

	lea	si,startuppath		; now copy path
	mov	bx,si
	mov	cx,64
gepcopypath:
	mov	al,es:[di]
	inc	di
	or	al,al
	jz	gependofstring		; copy whole filename

	mov	[si],al
	inc	si
	cmp	al,'\'
	jne	gepcopypath
	mov	bx,si			; save current EOS

	loop	gepcopypath

gependofstring:
	mov	[bx],al			; put EOS after last slash

	pop	es
	pop	bx
	pop	cx
	pop	si
	pop	di
	pop	ax
	ret
	endp



;---------------------------------------------------------------------------
;	READPARAMS -- 	Read command-line parameters.
;
;	These are allowed:
;
;	/C xxxxx   -- relocation of V9t9.CNF file.
;	/M xxxxx   -- relocation of MODULES.INF file.
;	/D [xxx]   -- specify that demos are allowed, or one to run.
;
;	/L xxxxx   -- do error logging (undocumented because it's lowly)
;
;	Returns CY=1 if error in paramline.
;--------------------------------------------------------------------------

	.data

tihelp	db	'V9t9:  TI Emulator! v6.0 by Edward Swartz, (c) 1995',0dh,0ah
	db	0dh,0ah
	db	'V9t9 [/C configfilename] [/M moduleinfofile] [/D [demofile]]',0dh,0ah
	db	0dh,0ah
	db	'Please read V9t9.TXT for basic operating information.',0dh,0ah,'$'


	.code

readparams proc	near
	mov	es,psp
	mov	si,81h
	mov	cl,es:[80h]			; length of tail
	xor	ch,ch
rprmloop:
	cmp	cx,0
	jg	rprmmore
	jmp	rprmdone
rprmmore:
	mov	al,es:[si]
	cmp	al,20h
	je	rprmskp				; skip space
	cmp	al,9
	je	rprmskp				; skip tab
	jmp	rprmget
rprmskp:
	inc	si
	dec	cx
	jmp	rprmloop

rprmget:
	cmp	al,'/'				; accept '-' or '/'
	je	rprmtype
	cmp	al,'-'
	je	rprmtype
	
rprmbadtype:
	lea	dx,e_parmnotype
	jmp	rprmerr

rprmtype:
	inc	si
	dec	cx
	jcxz	rprmbadtype

	mov	al,es:[si]
	cmp	al,'?'				
	jne	rprm00

	mov	ah,9				; '?' : help
	lea	dx,tihelp
	int	21h

	mov	ah,4ch
	int	21h

	lea	dx,backspaces
	or	stateflag,happymessage
	jmp	rprmerr

rprm00:
	cmp	al,'C'			
	je	rprm01
	cmp	al,'c'
	je	rprm01
	jmp	rprm10
rprm01:
	lea	di,uconfigfilename		; 'C' : config filename
	call	getpstring
	jc	rprmerr
	jmp	rprmloop

rprm10:
	cmp	al,'M'
	je	rprm11
	cmp	al,'m'
	je	rprm11
	jmp	rprm20
rprm11:
	lea	di,umoduleinfname		; 'M' : module database file
	call	getpstring
	jc	rprmerr
	jmp	rprmloop

rprm20:
	cmp	al,'D'
	je	rprm21
	cmp	al,'d'
	je	rprm21
	jmp	rprm30
rprm21:
	or	features,FE_demo		; 'D' : demo options
	and	features,not FE_emulating 	; assume playing

	lea	di,givendemo
	call	getpstring
	jnc	rprm22
	cmp	dx,offset e_needafile
	jne	rprmerr

	mov	givendemo,0			; clear filename
	or	features,FE_emulating		; we'll record.
rprm22:
	jmp	rprmloop

rprm30:
	cmp	al,'L'	      			; 'L': start error logging
	je	rprm31
	cmp	al,'l'
	je	rprm31
	lea	dx,e_parmnotype
	jmp	rprmerr
rprm31:
	lea	di,logfilename			; the filename is required
	call	getpstring
	jc	rprmerr
	jmp	rprmloop

rprmerr:
	call	setuperror
	stc
	jmp	rprmout
rprmdone:
	clc
rprmout:
	ret
	endp


getspaces proc	near
	jmp	gssmore
gssloop:
	jcxz	gssout
	mov	al,es:[si]
	cmp	al,20h
	je	gssmore
	cmp	al,9
	je	gssmore
	jmp	gssout
gssmore:
	inc	si
	dec	cx
	jmp	gssloop
gssout:
	ret
	endp


getpstring proc	near
	xor	dx,dx				; # chars, error
	call	getspaces
gpsloop:
	mov	al,es:[si]
	mov	[di],al
	inc	si
	inc	di
	dec	cx
	jle	gpsgood
	inc	dx
	cmp	dx,64				; max length 63 chars
	jae	gpsbad
	cmp	al,20h
	je	gpsgood
	cmp	al,9
	je	gpsgood
	jmp	gpsloop

gpsbad:
	stc
	lea	dx,e_parsebadpath
	jmp	gpsout
gpsgood:
	or	dx,dx
	jnz	gpslong
	lea	dx,e_needafile
	stc
	jmp	gpsout

gpslong:
	xor	al,al
	mov	[di],al			; terminate string
	clc
gpsout:
	ret
	endp



;==========================================================
;	Open a file.
;	SI=path, BX=filename
;
;	Assumes VALID SI and BX
;
;	Returns AX=handle
;
opensupportfile	proc	near
	push	si
	push	di
	push	bx
	push	cx
	
	lea	di,filename
	mov	cx,50
	call	concatasciiz
	mov	si,bx
	mov	cx,0
	call	concatasciiz
	mov	byte ptr [di],0

	lea	dx,filename
	mov	ax,3d00h
	int	21h
	jnc	osfok

	xor	ax,ax
osfok:
	pop	cx
	pop	bx
	pop	di
	pop	si
	ret
	endp


;=======================================================================
;	Concatenate at most CX chars from SI to DI, making a DOS string.
;	(The name of the procedure is a lie.)
;
;	If CX=0, then don't worry
concatasciiz	proc	near
	push	ax
	push	si
	or	cx,cx
	jnz	ccazloop
	dec	cx
ccazloop:
	mov	al,[si]
	or	al,al
	jz	ccazout
	mov	[di],al
	inc	si
	inc	di
	loop	ccazloop

ccazout:
	mov	byte ptr [di],'$'
	pop	si
	pop	ax
	ret
	endp


;=========================================================================
;	Set up the error message
;
;	Inputs:		DX=>error
;			SI=>offending filename (substituted into error
;						message at %)
;
;	Outputs:	DOS-terminated string
;
setuperror	proc	near
	push	ax
	push	dx
	push	si
	push	di
	xchg	si,dx
	lea	di,errormessage
sueloop:
	lodsb
	or	al,al
	jz	sueout
	cmp	al,'%'
	jne	suepush
       
	xchg	si,dx			; SI=>filename, DX=>error msg
sueins:
	lodsb
	or	al,al
	jz	sueinsout
	mov	[di],al
	inc	di
	jmp	sueins

sueinsout:
	xchg	si,dx			; SI=>error, DX=>filename
	jmp	sueloop

suepush:
	mov	[di],al
	inc	di
	jmp	sueloop
sueout:
	mov	al,'$'
	mov	[di],al

	pop	di
	pop	si
	pop	dx
	pop	ax
	ret
	endp


;=======================================================================
;	readROM will use opensupportfile to read a file into
;	a segment
;
;	Will create a result string for possible termination
;
;	SI=path, BX=filename, ES=segment, CX=size (-1 if unknown, 0=64k)
;	
;	Returns C=1 if error
readROM	proc	near
	push	bx
	push	cx
	push	dx
	push	si


	call	opensupportfile
	or	ax,ax
	jnz	rRok

	push	si
	push	dx

	lea	dx,e_romnotfound
	lea	si,filename
	call	setuperror

	pop	dx
	pop	si

	stc
	jmp	rRNoFixStack

rRok:
	xor	dx,dx
	push	si
	push	bx
rRRead:
	call	readchunk
	jc	rRBadFileSize

	jcxz	rROut
	jmp	rRRead

rRBadFileSize:
	push	si			
	push	dx

	lea	dx,e_romfileshort
	lea	si,filename
	call	setuperror

	pop	dx
	pop	si

	stc
rROut:
	pushf
	mov	bx,ax
	mov	ah,3eh
	int	21h			; close the file!
	popf

	pop	bx
	pop	si

rRNoFixStack:
	pop	si
	pop	dx
	pop	cx
	pop	bx
	ret
	endp


;====================================================================
;	Read a chunk of a file.
;
;	ES:DX 	= where
;	AX	= handle
;	CX	= max # bytes, or -1 if unknown
;
;
;	Returns CX=# bytes left to read, C=1 if less found than needed
;	and DX=next part
;
;	If whole file read, closes file
;
readchunk	proc	near
	push	ax
	push	bx
	push	ds
	push	di

	mov	bx,ax
	mov	ax,es
	mov	ds,ax
	
	mov	di,cx
	or	cx,cx
	jz	rc64k
	cmp	cx,-1
	jne	rcknown
rc64k:
	mov	cx,8000h

rcknown:
	mov	ah,3fh
	int	21h
	jc	rcErr

	mov	cx,di

	cmp	ax,di
	jne	rcNoClose

	push	ax		; EOF, so close file
	push	bx
	mov	ah,3eh
	int	21h
	pop	bx
	pop	ax

rcNoClose:

	cmp	ax,cx
	je	rcOut

	or	di,di
	jz	rc64kFix

	cmp	di,-1
	jne	rcBad

	or	ax,ax
	jnz	rcUnknown

	mov	cx,ax		; all read, so make it return CX=0
	jmp	rcOut

rc64kFix:
	cmp	ax,8000h
	jne	rcBad
	xor	cx,cx
	jmp	rcOut
rcBad:
	stc
	jmp	rcErr
rcOut:
	sub	cx,ax
rcUnknown:
	add	dx,ax
	clc
rcErr:
	pop	di
	pop	ds
	pop	bx
	pop	ax

	ret
	endp


;=====================================================================
;	READ CONFIGURATION FILE   V9t9.CNF.
;
;
;	Whatever directory V9t9 was started from, is the directory
;	where to expect the config file.  (startuppath)
;
;
;	Parse each line and assign stuff according to encoded name for
;	the uppercase field name.
;
;
;	Acceptable lines:
;
;	#,[,;,! = comment
;
;	fieldname=xxxxx, spaces don't matter
;	
;	The entire line to CR/LF is considered to be valid
;=======================================================================

readconfigfile	proc	near
	pusha
	push	es

	lea	si,nullpath
	lea	bx,uconfigfilename
	cmp	byte ptr [bx],0
	jnz	rcfus

	lea	si,startuppath
	lea	bx,configfilename
rcfus:
	call	opensupportfile
	or	ax,ax
	jnz	rcffound

	lea	dx,e_confignotfound
	lea	si,filename
	call	setuperror
	stc
	jmp	rcfnofree

rcffound:				; AX will contain the handle
	mov	bx,ax
	xor	cx,cx
	mov	dx,cx
	mov	ax,4202h
	int	21h
	or	dx,dx
	jz	rcfno64k
	mov	dx,0ffffh
rcfno64k:
	or	ax,dx
	push	ax
	xor	cx,cx
	mov	dx,cx
	mov	ax,4200h
	int	21h
	mov	ax,bx
	pop	cx
	cmp	cx,8192
	jb	rcfno8k			; make sure it's not >8k

	mov	ah,3eh
	int	21h

	lea	dx,e_filetoobig
	lea	si,filename
	call	setuperror
	stc
	jmp	rcfnofree

rcfno8k:
	push	ax
	mov	ah,48h
	mov	bx,8192/16		; get mem
	int	21h
	mov	bufseg,ax
	jnc	rcfgotmem

	pop	ax
	lea	dx,e_noconfigmem
	call	setuperror
	stc
	jmp	rcfnofree

rcfgotmem:
	pop	ax

	push	ds
	push	ax
	push	cx
	mov	bx,ax
	mov	ah,3fh			; read whole file
	mov	cx,8192
	mov	ds,bufseg
	xor	dx,dx
	int	21h
	push	di
	mov	di,ax
	mov	byte ptr [di],0dh	; end last line with CR
	inc	ax
	mov	dx,ax
	pop	di
	pop	cx
	pop	ax
	pop	ds
	mov	buflen,dx
	mov	bufpos,0

rcfreadlines:
	call	parsealine		; parse one line
	jc	rcfclose
	or	ax,ax
	jz	rcfout
	jmp	rcfreadlines
rcfclose:
	mov	bx,ax
	mov	ah,3eh
	int	21h
	stc
rcfout:
	pushf
	cmp	bufseg,0
	jz	rcfgetflags
	mov	ah,49h			; free memory
	mov	es,bufseg
	int	21h
rcfgetflags:
	popf
rcfnofree:
	pop	es
	popa
	ret
	endp


;====================================================================
;	READ MODULE INFORMATION FILE   MODULES.INF.
;
;
;	Whatever directory V9t9 was started from, is the directory
;	where to expect this file.  (startuppath)
;
;	Parse each line, store in MODINFOSEG.
;
;	Acceptable lines:
;
;	#,[,;,! = comment
;
;	fieldname=a number, spaces don't matter
;	
;	The entire line to CR/LF is considered to be valid
;===================================================================

readmodulefile	proc	near
	pusha
	push	es

	lea	si,nullpath
	lea	bx,umoduleinfname
	cmp	byte ptr [bx],0
	jnz	rmfus

	lea	si,startuppath
	lea	bx,moduleinfname

rmfus:
	call	opensupportfile
	or	ax,ax
	jnz	rmffound

	lea	dx,e_moduleinfnotfound
	lea	si,filename
	call	setuperror
	stc
	jmp	rmfnofree

rmffound:				; AX will contain the handle
	mov	bx,ax
	xor	cx,cx
	mov	dx,cx
	mov	ax,4202h
	int	21h
	or	dx,dx
	jz	rmfno64k
	mov	dx,0ffffh
rmfno64k:
	or	ax,dx
	push	ax
	xor	cx,cx
	mov	dx,cx
	mov	ax,4200h
	int	21h
	mov	ax,bx
	pop	cx
	cmp	cx,8192
	jb	rmfno8k

	mov	ah,3eh
	int	21h

	lea	dx,e_filetoobig
	lea	si,filename
	call	setuperror
	stc
	jmp	rmfnofree

rmfno8k:
	push	ax
	mov	ah,48h
	mov	bx,8192/16
	int	21h
	mov	bufseg,ax
	jnc	rmfgotmem

	pop	ax
	lea	dx,e_nomoduleinfmem
	call	setuperror
	stc
	jmp	rmfnofree

rmfgotmem:
	mov	ah,48h
	mov	bx,8192/16
	int	21h
	mov	modinfoseg,ax
	jnc	rmfgotmodmem

	pop	ax
	lea	dx,e_nomodulemem
	call	setuperror
	stc
	jmp	rmfnofree

rmfgotmodmem:
	pop	ax

	push	ds
	push	ax
	push	cx
	mov	bx,ax
	mov	ah,3fh
	mov	cx,8192
	mov	ds,bufseg
	xor	dx,dx
	int	21h
	push	di
	mov	di,ax
	mov	byte ptr [di],0dh
	inc	ax
	mov	dx,ax
	pop	di
	pop	cx
	pop	ax
	pop	ds
	mov	buflen,dx
	mov	bufpos,0

	mov	es,modinfoseg
	xor	di,di
	mov	nummodulesinlist,0

rmfreadlines:
	cmp	di,8192-(size modrec)
	jae	rmfout			; no more than 204 titles!
	call	parsemoduleline
	jc	rmfclose
	or	ax,ax
	jz	rmfout
	jmp	rmfreadlines
rmfclose:
	mov	byte ptr [di],0		; null title means end of list
	mov	bx,ax
	mov	ah,3eh
	int	21h
	stc
rmfout:
	pushf
	cmp	bufseg,0
	jz	rmfgetflags
	mov	ah,49h
	mov	es,bufseg
	int	21h
rmfgetflags:
	popf
rmfnofree:
	pop	es
	popa
	ret
	endp




;================================================================
;	Parse one line from the configuration file.
;
;	AX = handle
;	
;	Returns:
;	C=1 if error (errormessage has been set)
;	AX=0 if EOF
;
parsealine	proc	near
	push	cx
	call	readaline		; read a line, close file if end
	jnc	parseit			; C=1 = eof
	
	xor	ax,ax
	clc

parseit:
	lea	si,line
	call	skipspace
	mov	cl,[si]
	or	cl,cl
	jz	parseout
	cmp	cl,'#'
	je	parseout
	cmp	cl,'['
	je	parseout
	cmp	cl,'!'
	je	parseout
	cmp	cl,';'
	je	parseout		; comment chars
	cmp	cl,1ah
	je	parseout		; EOF = comment

	call	getfieldnum
	jc	parseerror
	call	skipspace
	mov	cl,[si]
	cmp	cl,'='
	je	gfnskipafter

	lea	dx,e_parsenoequals
	call	setupparseerror
	stc
	jmp	parseout

gfnskipafter:
	inc	si
	call	skipspace

	call	findfield
	jc	parseerror
	call	decodefield
	jc	parseerror
	jmp	parseout

parseerror:
	stc
	jmp	parseout

parseout:
	pop	cx
	ret
	endp


;=======================================================================
;	Parse one line from the module information file.
;
;	AX = handle
;	
;	Returns:
;	C=1 if error (errormessage has been set)
;	AX=0 if EOF
;
parsemoduleline	proc	near
	push	cx
	call	readaline		; read a line, close file if end
	jnc	pmlit			; C=1 = eof
	
	xor	ax,ax
	clc

pmlit:
	lea	si,line
	call	skipspace
	mov	cl,[si]
	or	cl,cl
	jz	pmlnone
	cmp	cl,'#'
	je	pmlnone
	cmp	cl,'['
	je	pmlnone
	cmp	cl,'!'
	je	pmlnone
	cmp	cl,';'
	je	pmlnone			; comment chars
	cmp	cl,1ah
	je	pmlnone			; EOF = comment

	call	mgettitle
	jc	pmlerror
	call	skipspace
	call	mgetfilename
	jc	pmlerror
	mov	byte ptr es:[di],0 	; no ROM, no GROM, no RAM, not banked
pmlgetmemories:
	call	skipspace
	call	mgetmemtype
	jc	pmlerror
	jz	pmlout			; end of line
	jmp	pmlgetmemories

	jmp	pmlout

pmlerror:
	stc
	jmp	pmlnone

pmlout:
	inc	di			; skip memory type
	inc	nummodulesinlist
	clc
pmlnone:
	pop	cx
	ret
	endp


;====================================================================

;	Read a char from buffer
;
readabufferedchar	proc	near
	push	bx 		; handle
	push	cx
	push	dx
	push	es
	push	di

	mov	di,dx
	mov	ax,bufpos
	cmp	ax,buflen
	jb	rabcinbuff

	xor	ax,ax		; EOF
	jmp	rabcout
rabcinbuff:

	mov	es,bufseg
	mov	bx,bufpos
	mov	al,es:[bx]
	mov	[di],al
	inc	bufpos
	mov	ax,1
rabcout:
	pop	di
	pop	es
	pop	dx
	pop	cx
	pop	bx
	ret
	endp


;================================================================

;	Read one line,
;	Return C=1 if EOF
readaline	proc	near
	push	di
	push	dx
	push	ax
	push	bx

	lea	dx,line
	mov	bx,ax
ralchar:

	call	readabufferedchar

	or	ax,ax
	jz	raleof

	mov	di,dx
	mov	al,[di]
	cmp	al,0dh		; eol= 0dh.... skipspace will skip 0ah
	je	raleol
	cmp	di,offset line+160
	jb	ralnottoolong
	dec	dx		; too long, just overwrite last char
ralnottoolong:
	inc	dx
	jmp	ralchar

raleol:
	xor	al,al
	mov	[di],al
	jmp	ralout

raleof:
	mov	ah,3eh
	int	21h
	stc
	jmp	ralout


ralout:
	pop	bx
	pop	ax
	pop	dx
	pop	di
	ret
	endp


;------------------------------------------------------------------

;	Skip space at SI.
;
skipspace	proc	near
	push	ax
ssloop:
	mov	al,[si]
	or	al,al
	jz	ssout		; whoops... don't skip EOL
	cmp	al,' '
	je	ssskip
	cmp	al,0ah
	je	ssskip
	cmp	al,9
	je	ssskip
	jmp	ssout		; okay, not a skippable char

ssskip:
	inc	si
	jmp	ssloop
ssout:
	pop	ax
	ret
	endp



;=======================================================================
;	Get the field to define.
;
;	This is the HASHING routine which takes a variable name 
;	and encodes it.
;
;	SI=ptr to line
;	Return BX=encoded # of field
;
;=======================================================================

getfieldnum	proc	near
	push	ax
	push	cx

	xor	bx,bx			; encoded value
	xor	cx,cx			; place counter
gfncount:
	mov	al,[si]
	or	al,al
	je	gfnout
	cmp	al,32
	je	gfnout
	cmp	al,'='
	je	gfnout
	cmp	al,','
	je	gfnout
	cmp	al,9
	je	gfnout

	cmp	al,'a'
	jb	gfnnotupper
	cmp	al,'z'
	ja	gfnnotupper
	sub	al,32
gfnnotupper:
	xor	ah,ah
	ror	ax,cl
	xor	bx,ax
	inc	cx
	inc	si
	jmp	gfncount

gfnout:
	pop	cx
	pop	ax
 	ret
	endp


;===================================================================
;	Set up parsing error's error message.
;
;	This is where you might want to add line-number printing.
;===================================================================

setupparseerror	proc	near
	push	ax
	push	dx
	push	si
	push	di
	push	cx

	lea	si,e_parselineis
	lea	di,errormessage
	xor	cx,cx
	call	concatasciiz
	lea	si,line
	xor	cx,cx
	call	concatasciiz
	lea	si,e_crlf
	xor	cx,cx
	call	concatasciiz
	mov	si,dx
	xor	cx,cx
	call	concatasciiz
	lea	si,e_crlf
	xor	cx,cx
	call	concatasciiz

	pop	cx
	pop	di
	pop	si
	pop	dx
	pop	ax
	ret
	endp


;=====================================================================

;	Find the field (=hash) number in the table.
;	BX=encoded #
;	Return DI=table entry, C=1 if not found
;
findfield	proc	near
	push	ax

	lea	di,configtable
fflook:
	cmp	word ptr [di],0
	je	fferr
	
	cmp	[di],bx
	clc
	je	ffdone

	add	di,6
	jmp	fflook

fferr:
	lea	dx,e_parsebadfield
	call	setupparseerror

	stc
ffdone:
	pop	ax
	ret
	endp


;---------------------------------------------------------------------

;	Decode the field using parser for this type of entry
;
;	DI=table entry
;	SI=pointer to string
decodefield	proc	near
	push	ax

	mov	ax,[di+2]		; AX=routine
	mov	di,[di+4]		; DI=pointer
	call	ax

	pop	ax
	ret
	endp


;==================================================================

;	Get title field from MODULES.INF line
;
;	DI=> modules structure
;
mgettitle proc	near
	call	PGetString	
	jc	mgterr
       	call	PGetComma
	jc	mgterr
	clc
	jmp	mgtout
mgterr:
	stc
mgtout:
	ret
	endp


;	Get filename field from MODULES.INF line
;
;	DI=> modules structure
;
mgetfilename proc near
	call	PFileBase
	jc	mgfnerr
	call	PGetComma
	jc	mgfnerr
	clc
	jmp	mgfnout
mgfnerr:
	stc
mgfnout:
	ret
	endp


;	MGETMEMTYPE --	Read a memory descriptor word	
;
mgetmemtype proc near
	call	PGetMemType
	jc	mgmterr
	call	PGetComma
	jz	mgmtout
	jc	mgmterr
	clc
	jmp	mgmtout
mgmterr:
	stc
mgmtout:
	ret
	endp


;=======================================================================

;	Parsing routines

;	Generic... not to be specified!
;
;	Given SI is the pointer to the variable value.
;	Test termination with ' ' or #0.
;
;	Returns AX=val

;-----------------------------------------------------------------

;	Read a decimal number.
;
;
PNum	proc	near
	push	cx
	push	bx
	xor	ax,ax
PNumLoop:
	mov	bl,[si]
	or	bl,bl
	clc
	jz	PNumOut
	cmp	bl,' '
	clc
	je	PNumOut
	cmp	bl,','
	clc
	je	PNumOut

	cmp	bl,'0'
	jb	PNumErr
	cmp	bl,'9'
	ja	PNumErr

	sub	bl,'0'
	xor	bh,bh

	mov	dx,10
	mul	dx			; mul accum by 10

	add	ax,bx
	inc	si
	jmp	PNumLoop

PNumErr:
	xor	ax,ax
	stc
PNumOut:
	pop	bx
	pop	cx
	ret
	endp


;	Read a hex number.
;
;
PHexNum	proc	near
	push	cx
	push	bx

	xor	ax,ax
PHNumLoop:
	mov	bl,[si]
	or	bl,bl
	clc
	jz	PHNumOut
	cmp	bl,' '
	clc
	je	PHNumOut
	cmp	bl,','
	clc
	je	PHNumOut

	cmp	bl,'a'
	jb	PHNumUpper
	cmp	bl,'f'
	ja	PHNumUpper

	sub	bl,32

PHNumUpper:
	cmp	bl,'0'
	jb	PHNumErr
	cmp	bl,'F'
	ja	PHNumErr
	cmp	bl,'9'
	jbe	PHLegal
	sub	bl,7

PHLegal:
	sub	bl,'0'
	xor	bh,bh

	shl	ax,4

	or	ax,bx
	inc	si
	jmp	PHNumLoop

PHNumErr:
	xor	ax,ax
	stc
PHNumOut:
	pop	bx
	pop	cx
	ret
	endp




;	Read a decimal number into a byte.
;	
;
PByte	proc	near
	push	ax
	push	dx

	call	PNum
	jnc	PBOk

     	lea	dx,e_parsebadnumber
	call	setupparseerror
	stc
	jmp	PBOut
PBOk:
	cmp	ax,256
	jb	PBGoodByte

	lea	dx,e_parsebadbyte
	call	setupparseerror
	stc
	jmp	PBOut

PBGoodByte:
	mov	[di],al	      	
	clc
PBOut:
	pop	dx
	pop	ax
	ret
	endp



;	Read an integer.
;
;
PInt	proc	near
	push	ax
	push	dx

	call	PNum
	jnc	PIOk

     	lea	dx,e_parsebadnumber
	call	setupparseerror
	stc
	jmp	PIOut
PIOk:
	mov	[di],ax	      	
PIOut:
	pop	dx
	pop	ax
	ret
	endp


;	Read a boolean value, which is ANDed with the current
;	value of the variable in memory.
;
;	(I.e., USEVGA.  Before config file is read, it is set to
;	0 or 0ffh depending on if a VGA was found.  By ANDing it
;	with true or false, UseVga can only disallow use of a VGA, 
;	not allow it.)

PAndBoolean	proc	near
	push	ax
	push	bx
	push	di
	push	bp
	lea	bp,PBoolAndIt
	jmp	pBoolEnter
	endp

;-------------------------------------------------------

PBoolean proc	near
	push	ax
	push	bx
	push	di
	push	bp
	lea	bp,PBoolSetit
PBoolEnter:
	call	getfieldnum
	mov	ax,bx
	lea	bx,Boolmatch
PBoLook:
	cmp	word ptr [bx],0
	jz	PBoErr
	cmp	[bx],ax
	je	PBoFound
	add	bx,4
	jmp	PBoLook
PBoFound:
	mov	ax,[bx+2]
	call	bp				; and, or, set
	clc
	jmp	PBoOut
PBoErr:
	lea	dx,e_ParseBadBoolean
	call	setupparseerror
	stc
PBoOut:
	pop	bp
	pop	di
	pop	bx
	pop	ax
	ret
	endp



PBoolSetIt	proc	near
	mov	[di],al
	ret
	endp

PBoolAndIt	proc	near
	and	[di],al
	ret
	endp


;----------------------------------------------------------------------
;
;	Get path which MUST have a drive letter.
;

PPath	proc	near
	push	ax
	push	bx
	push	di
	push	cx

;	First make sure there is a drive letter

	mov	al,[si]
	mov	ah,[si+1]
	cmp	ah,':'
	je	PPTestDrive

	lea	dx,e_parsenodrive
	call	setupparseerror
	stc
	jmp	PPOut

PPTestDrive:
	and	al,not 32
	cmp	al,'A'
	jb	PPDrErr
	cmp	al,'Z'
	ja	PPDrErr
	jmp	PPCont

PPDrErr:
	lea	dx,e_parsebaddrive
	call	setupparseerror
	stc
	jmp	PPOut

PPCont:
;	Now put the drive and pathname into the string

	mov	cx,60			; max chars
PPLoop:
	mov	al,[si]
	or	al,al
	jz	PPEOS
	cmp	al,' '
	je	PPEOS
	mov	[di],al
	inc	si
	inc	di
	loop	PPLoop

	lea	dx,e_parsebadpath
	call	setupparseerror
	stc
	jmp	PPOut

PPEOS:
	mov	al,[di-1]
	cmp	al,'\'
	je	PPNull
	mov	al,'\'
	mov	[di],al
	inc	di
PPNull:
	xor	al,al
	mov	[di],al
	clc
PPOut:
	pop	cx
	pop	di
	pop	bx
	pop	ax
	ret
	endp


;----------------------------------------------------------------
;
;	Get any sort of path.
;

PRelPath proc	near
	push	ax
	push	bx
	push	di
	push	cx

;	Put the drive and pathname into the string

	mov	cx,60
PRPCont:
	mov	al,[si]
	or	al,al
	jz	PRPEOS
	cmp	al,' '
	je	PRPEOS
	mov	[di],al
	inc	si
	inc	di
	loop	PRPCont

	lea	dx,e_parsebadpath
	call	setupparseerror
	stc
	jmp	PRPOut

PRPEOS:
	mov	al,[di-1]
	cmp	al,'\'
	je	PRPNull
	mov	al,'\'
	mov	[di],al
	inc	di
PRPNull:
	xor	al,al
	mov	[di],al
	clc
PRPOut:
	pop	cx
	pop	di
	pop	bx
	pop	ax
	ret
	endp

;-------------------------------------------------------------------
;
;	Get a filename.
;

PFileName proc	near
	push	ax
	push	di
	push	cx

;	Put the drive and pathname into the string

	mov	cx,13
PFNCont:
	mov	al,[si]
	or	al,al
	jz	PFNEOS
	cmp	al,' '
	je	PFNEOS
	mov	[di],al
	inc	si
	inc	di
	loop	PFNCont
	
	lea	dx,e_parsebadfilename
	call	setupparseerror
	stc
	jmp	PFNOut
PFNEOS:
	xor	al,al
	mov	[di],al
	clc
PFNOut:
	pop	cx
	pop	di
	pop	ax
	ret
	endp


;----------------------------------------------------------------------
;	Get a list of numbers representing 
;	modules to put in the startup list
;
PModuleList proc near
	push	ax
	push	cx
	push	bx

	xor	cx,cx			; # modules in list
	mov	byte ptr [di],0ffh	; null list
	mov	nummodulesselected,0
PMLtLoop:
	call	PNum
	jc	PMLtOut
       	cmp	al,nummodulesinlist
	lea	dx,e_parsetoohigh
	ja	PMLtErr
	dec	al
	mov	[di],al
	inc	di
	mov	byte ptr [di],0ffh
	inc	cx
	inc	nummodulesselected

	call	PGetComma
	jz	PMLtOut

	cmp	cx,32
	jb	PMLtLoop

	lea	dx,e_parsetoomany
PMLtErr:
	call	setupparseerror
	stc
PMLtOut:	  
	pop	bx
	pop	cx
	pop	ax

	ret
	endp


;-----------------------------------------------------------------
;	Get a list of 3 numbers representing joystick center
;	and range.
;
;	DI is the joystick #
;
PJoyBounds proc near
	push	ax
	push	cx
	push	bx
	push	dx

	or	di,di
	lea	di,joy1c
	jz	PJBNot2
	lea	di,joy2c
PJBNot2:

	xor	cx,cx			; # numbers read
PJBLoop:
	call	PNum
	jc	PJBOut
	mov	[di],ax
	add	di,2
	inc	cx
	cmp	cx,3
	je	PJBGood
	cmp	cx,2
	jne	PJBNotMax

	cmp	di,offset joy1c+4	; count offset
	lea	di,joy1max
	je	PJBNotMax
	lea	di,joy2max

PJBNotMax:

	call	PGetComma
	jz	PJBErr
       	jmp	PJBLoop

PJBErr:
	lea	dx,e_parsetoofewjoy
	call	setupparseerror
	stc
	jmp	PJBOut
PJBGood:
	clc
PJBOut:	  
	pop	dx
	pop	bx
	pop	cx
	pop	ax

	ret
	endp


;--------------------------------------------------------------------
;	Get a list of 2 numbers representing an RS232 port's
;	base port and IRQ.
;
PRSList proc near
	push	ax     
	push	bx
	push	dx
	push	es

	mov	ax,40h
	mov	es,ax

	call	PNum			; first entry is port (1-4)
	jc	PRSLErr

	mov	[di].smap,al		; save mapped BIOS port
	mov	bx,ax
	dec	bx
	and	bx,3
	add	bx,bx

	mov	ax,es:[bx]		; get BIOS serial port addr
	mov	[di].port,ax			

	call	PGetComma
	jz	PRSLErr

	call	PNum
	jc	PRSLErr
	and	al,7			; IRQ
	mov	[di].irq,al
	jmp	PRSLGood

PRSLErr:
	lea	dx,e_parsers232err
	call	setupparseerror
	stc
	jmp	PRSLOut
PRSLGood:
	clc
PRSLOut:
	pop	es	  
	pop	dx
	pop	bx
	pop	ax

	ret
	endp

;----------------------------------------------------------------

;	Get parallel port addr.
;
PPIOList proc	near
	push	ax
	push	bx
	push	es

	mov	ax,40h
	mov	es,ax

	call	PNum
	jc	PPIOErr

	mov	[di].pmap,al
	mov	bx,ax
	dec	bx
	and	bx,3
	add	bx,bx
	mov	ax,es:[bx+8]

	mov	[di].pport,ax
	clc
	jmp	PPIOOut

PPIOErr:
	lea	dx,e_parsepioerr
	call	setupparseerror
	stc

PPIOOut:
	pop	es
	pop	bx
	pop	ax
	ret
	endp


;-----------------------------------------------------------------
;	Get extension, either '.xxx' or 'xxx'
;
;
pgetext proc	near
	push	ax
	push	cx

	mov	cx,3
	mov	al,[si]
	inc	si
	cmp	al,'.'
	je	pgeskip

	mov	byte ptr [di],'.'
	inc	di

pgeskip:
	mov	[di],al
	inc	di
	mov	al,[si]
	inc	si
	or	al,al
	jz	pgeout
	dec	cx
	jcxz	pgeerr
	jmp	pgeskip

pgeerr:
	lea	dx,e_parselongext
	call	setupparseerror
	stc

pgeout:
	mov	byte ptr [di],0
	pop	cx
	pop	ax
	ret
	endp



;=======================================================================
;	PGETMASKS --	Get a list of words separated by commas
;			and add to a mask
;
;	format:		"adlib,-sbdma,+pcspeaker"
;
;	note:		only 8 bits are allowed!
;=====================================================================

PGetMasks proc	near
	push	ax
	push	bx
	push	cx
	push	bp
	push	di

	mov	bx,[di]	 		; word list ptr
	mov	bp,[di+4]		; legal masks addr
	mov	di,[di+2]		; mask byte ptr
PGMLoop:
	mov	al,[si]
	mov	ch,1			; CH=1 means ADD bit
	cmp	al,'+'
	jne	PGM00
	mov	ch,1
	inc	si
PGM00:
	cmp	al,'-'
	jne	PGMMore
	mov	ch,0			; CH=0 means CLEAR bit
	inc	si
PGMMore:
	call	PMatchWordInList
	jc	PGMErr

	or	[di],al
	or	ch,ch
	jnz	PGMNext		
	xor	[di],al			; off, actually
PGMNext:
	mov	cl,ds:[bp]
	and	[di],cl			; legalize bits

	call	PGetComma
	jz	PGMGood
	jc	PGMErr
	jmp	PGMLoop

PGMGood:
	clc
	jmp	PGMOut
PGMErr:
	stc
PGMOut:
	pop	di
	pop	bp
	pop	cx
	pop	bx
	pop	ax
	ret
	endp

;-------------------------------------------------------

;	Match a word in a list --
;
;	Inputs:		BX=word list
;	Outputs:	AX=value
;
PMatchWordInList proc	near
	push	bx
	push	di
PMWLEnter:
	push	bx
	call	getfieldnum
	mov	ax,bx
	pop	bx
PMWLLook:
	cmp	word ptr [bx],0
	jz	PMWLErr
	cmp	[bx],ax
	je	PMWLFound
	add	bx,4
	jmp	PMWLLook
PMWLFound:
	mov	ax,[bx+2]
	clc
	jmp	PMWLOut
PMWLErr:
	lea	dx,e_ParseBadWord
	call	setupparseerror
	stc
PMWLOut:
	pop	di
	pop	bx
	ret
	endp

;----------------------------------------------

;	Get a comma.
;
;	Return C=1 if char isn't a comma,
;	or Z=1 if EOS.
;
PGetComma proc	near
	push	ax
	mov	al,[si]
	clc
	or	al,al
	jz	PGCout
	inc	si
	cmp	al,' '
	je	PGCOk
	cmp	al,','
	jne	PGCerr
PGCOk:
	or	al,1
	clc
	jmp	PGCOut
PGCErr:
	lea	dx,e_parsebadbase
	call	setupparseerror
	stc
PGCOut:
	pop	ax
	ret
	endp


;*********************************************************************
;	Modules.inf parsing routines
;*********************************************************************

;
;	Get a MODULE filename's base part (7 chars).
;

PFileBase proc	near
	push	ax
	push	cx

;	Put the drive and pathname into the string

	mov	cx,7
PFBCont:
	mov	al,[si]
	or	al,al
	jz	PFBEOS
	cmp	al,'.'
	lea	dx,e_parsebadbase
	je	PFBErr
	cmp	al,' '
	je	PFBEOS
	cmp	al,','
	je	PFBEOS
	mov	es:[di],al
	inc	si
	inc	di
	loop	PFBCont
	jmp	PFBGEOS
PFBErr:
	call	setupparseerror
	stc
	jmp	PFBOut
PFBEOS:
	xor	al,al
	mov	es:[di],al
	inc	di
	loop	PFBEOS
PFBGEOS:
	clc
PFBOut:
	pop	cx
	pop	ax
	ret
	endp


;---------------------------------------------------------

;	Get a quoted string (32 chars)
;
;
PGetString proc	near
	push	ax
	push	cx

	mov	cx,32			; max # characters
	mov	al,[si]
	inc	si
	cmp	al,'"'	      		; need leading quote
	lea	dx,e_parsenoquote
	jne	PGSErr
PGSLoop:
	mov	al,[si]
	inc	si
	or	al,al
	lea	dx,e_parsetrunc
	jz	PGSErr
	cmp	al,'"'
	je	PGSPad			; need ending quote
	mov	es:[di],al
	inc	di
	loop	PGSLoop
	lea	dx,e_parsetoolong
	jmp	PGSErr
PGSPad:
	mov	byte ptr es:[di],' '
	inc	di
	loop	PGSPad	
PGSOut:
	clc
	jmp	PGSExit
PGSErr:
	call	setupparseerror
	stc
PGSExit:
	pop	cx
	pop	ax
	ret
	endp

;-----------------------------------------------------

PGetMemType proc	near
	push	ax
	push	bx

	lea	bx,memmatch
	call	PMatchWordInList
	jc	PGMTErr

	or	es:[di],al
	clc
	jmp	PGMTOut
PGMTErr:
	stc
PGMTOut:
	pop	bx
	pop	ax
	ret
	endp


	end
