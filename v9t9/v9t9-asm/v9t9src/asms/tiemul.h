;========================================
;	TIEMUL.H
;
;	Header file for TIEMUL.ASM
;========================================


IFDEF	_TIEMUL_

	.stack	400h

	.data

	public	stateflag
	public	vaddr
	public	vregs

	public	features		; which modules do we support?
	public	patches			; CPU ROM patches

	public	psp			; program segment prefix
	public	rompath			; path for rom files
	public	modulespath		; path for module files
	public	timerdelay		; VDP interrupt speed
	public	ticpufilename		; filename of cpu block
	public	tigplfilename		; filename of gpl blpck
	public	tispeechfilename	; filename of speech block

	public	lastval,lastcmp

	IFDEF	COMPROM
	public	compiledrom,compiledpath
	ENDIF


	public	intmask			; saved copy of interrupt mask
	public	preventmultipleinterrupts


	public	videodatachanged	; has screen been affected?


	public	delayamount		; in between instructions
	public	processor		; 286, 386



	public	tidiskpathname		; paths for DSKx devices


	public	speechpath		; path for speech file


	public	ips,cips,uptime		; # instructions executed, in time

	public	startdebug


;-------------------------------------------------------------------------

	.code

	public	die			; how to get out fast

	public	reversememory		; of CPU segments (9900-80x86)

	public	writememorymapped	; write to memory-mapped devices
	public	readmemorymapped	; read

	public	checksoftstate

	public	emustop
	public	emustart

ELSE

	.data

	extrn	stateflag:word
	extrn	vaddr:word
	extrn	vregs:byte

	extrn	features:word		
	extrn	patches:word

	extrn	psp:word		
	extrn	rompath:byte	     
	extrn	modulespath:byte     
	extrn	timerdelay:word	     
	extrn	ticpufilename:byte   
	extrn	tigplfilename:byte   
	extrn	tispeechfilename:byte 

	extrn	lastval:word,lastcmp:word

	IFDEF	COMPROM
	extrn	compiledrom:byte,compiledpath:byte
	ENDIF


	extrn	intmask:word	      
	extrn	preventmultipleinterrupts:byte


	extrn	videodatachanged:byte 


	extrn	delayamount:word      
	extrn	processor:word	      



	extrn	tidiskpathname:byte


	extrn	speechpath:byte


	extrn	ips:dword,cips:word,uptime:dword	       

	extrn	startdebug:byte

;-------------------------------------------------------------------------

	.code

	extrn	die:near

	extrn	reversememory:near	       

	extrn	writememorymapped:near      
	extrn	readmemorymapped:near       

	extrn	checksoftstate:near


	extrn	emustop:near
	extrn	emustart:near

ENDIF
