;
;	Equates that f9900rom.f99 needs.
;
;	These are in assembly syntax.  A post-assembly build step (EquatesToConstants)
;	converts them into f9900romequs.fi for use by the following 
;	FORTH ROM build step.
;

;	------------------------------------------ ROM/MMIO/hardware layout

; These addresses, when written, switch ROM to the given banks 
BANK0		equ 	>0						; FORTH bank
BANK1		equ 	>4						; ROM bank

INTVECS		equ		>00				; 16 vectors 

XOPS		equ		>40				; 16 vectors

MMIO		equ		>80				; >40 bytes

VDPRD		equ		MMIO + >00
VDPST		equ		MMIO + >02
VDPWD		equ		MMIO + >04
VDPWA		equ		MMIO + >06       ; high then low
VDPCL		equ		MMIO + >08
VDPWI		equ 	MMIO + >0A

GPLRD		equ		MMIO + >10
GPLRA		equ		MMIO + >12       ; high then low
GPLWD		equ		MMIO + >14
GPLWA		equ		MMIO + >16

SPCHRD		equ		MMIO + >18
SPCHWT 		equ		MMIO + >1a

SOUND		equ		MMIO + >20


CRU			equ		>C0				; >40 bytes

INTS		equ		CRU + >00		; FORTH: 'INTS
INTSP		equ		CRU + >01		; FORTH: 'INTSP

M_INT_BKPT	equ		>1    
M_INT_EXT	equ		>2    
M_INT_VDP	equ		>4    
M_INT_KBD	equ		>8    

KBD			equ		CRU + >02		; FORTH: 'KBD
KBDA		equ		CRU + >03		; FORTH: 'KBDA

GATE		equ		CRU + >04		; FORTH: 'GATE
RND			equ		CRU + >07		; FORTH: 'RND

DSK			equ		CRU + >08		; FORTH: 'DSK

;  write: cmd
DSKCMD		equ		DSK + >0		; FORTH: 'DSKCMD
dskCmdRestore 		equ	>00				; FORTH: #dskCmdRestore
dskCmdSeek 			equ	>10				; FORTH: #dskCmdSeek
dskCmdReadSector 	equ	>80				; FORTH: #dskCmdReadSector
dskCmdWriteSector  equ	>A0				; FORTH: #dskCmdWriteSector
dskCmdInterrupt  	equ	>F0				; FORTH: #dskCmdInterrupt

dskCmdBitHead		equ	>08				; FORTH: #dskCmd'Head
dskCmdBitVerify		equ	>04				; FORTH: #dskCmd'Verify
dskCmdBit10ms		equ	>02				; FORTH: #dskCmd'10ms
        
;  read: status
DSKST		equ		DSK + >0			; FORTH: 'DSKST
dskStRdy			equ >80				; FORTH: #dskStRdy
dskStDrq			equ >02				; FORTH: #dskStDrq
dskStBusy			equ >01				; FORTH: #dskStBusy
        
;   1: write/read: track addr
DSKTRK		equ		DSK + >1			; FORTH: 'DSKTRK
  
;  2: write/read: sector addr
DSKSEC		equ		DSK + >2			; FORTH: 'DSKSEC
  
;  3: write/read: data
DSKDAT		equ		DSK + >3			; FORTH: 'DSKDAT
  
;  4: write/read: [0 | 0 | 0 | 0 | motor | hold | head | side ] 
DSKFLG		equ		DSK + >4			; FORTH: 'DSKFLG
dskFlgSide			equ >01				; FORTH: #dskFlgSide
dskFlgHead			equ >02				; FORTH: #dskFlgHead
dskFlgHold			equ >04				; FORTH: #dskFlgHold
dskFlgMotor			equ >08				; FORTH: #dskFlgMotor
  
;  5: write/read: [0 | 0 | 0 | 0 | 0 | D2 | D1 | D0]
DSKNUM		equ		DSK + >5			; FORTH: 'DSKNUM 

DBG			equ		>FF					; FORTH: 'DBG

;	Shared ROM start

SharedROM	equ		>0100

ForthROM	equ		>0200

			aorg	ForthROM
			
_RESET		bss		2				; FORTH: @RESET
_TICK		bss		2				; FORTH: @TICK
_COLD		bss		2				; FORTH: @COLD
_ABORT		bss		2				; FORTH: @ABORT
_DP0		bss		2				; FORTH: @DP0
_LATEST		bss		2				; FORTH: @LATEST

;	------------------------------------------ RAM layout

_RAM		equ		>FB00

	; definitions in ram.inc, not here (to avoid FORTH exposure)

;   Special "system variables" that the FORTH can modify

_SharedRAM  equ    >FF00
 
 			aorg	_SharedRAM
 			
; this block of variables is visible via equates to FORTH 

vintflags   bss 1       ; VDP interrupt flags

nosprmot 	equ		>80		; 
nocursor 	equ 	>40		
noabort 	equ 	>20		
nvblnk      equ >80     ;       1: suppress blink cursor and blank screen ; FORTH: v-int-nocursor
nvkeyscan   equ >40     ;       1: suppress scan keyboard  ; FORTH: v-int-nokbd
nvabort     equ >20     ;       1: suppress Ctrl-Fctn-Shift-Space ABORT support ; FORTH: v-int-noabort
nvsprmot    equ >20     ;       1: suppress sprite motion ; FORTH: v-int-nosprmot
nsoundlist  equ >10     ;       1: suppress sound list and/or synth; FORTH: v-int-nosound

vstatus     bss 1       ; VDP status last read during interrupt

userint     bss 2       ; user VDP interrupt handler (called when VDP interrupts)
timerisr    bss 2       ; user timer handler (called when clock ticks)

nsprmot     bss 1       ; number of sprites in motion (00 = none)

sndflags    bss 1       ; sound list flags
                        ;       $00: CPU RAM, $80: VDP 
                        ;       $0f: tempo adjust (signed: -4 to 3) 
sndlist     bss 2       ; address of classic sound list (0 = none), incremented on tick
snddur      bss 1       ; duration of current group

        even

uptime		bss	4		; time in 1/60 seconds
timeout		bss	2		; timeout counter

DP			bss	2		; current dictionary
GDP			bss	2		; current GROM dictionary

tasks		bss	2		; pointer to task list; FORTH: 'tasks
me			bss	2		; current task; FORTH: 'me
ticked		bss	2		; last switch time; FORTH: 'ticked
        
; this block of variables is included in a video mode save/restore 
_CPURAMSTART equ $

vpob        bss 1       ; VDP "page offset" bank (added to V9938 bank to select the page outside 64k)
vblinkflag  bss 1       ; flag to set blink bit in text2 
vpgrow      bss 2       ; VDP "page row offset" (added to V9938 commands to select the page)
vtextpage   bss 2       ; VDP text-ish page offset (screen, patterns, colors) (in addition to vpob)

vidvarstart bss 2       ; start addr of important video variables
vidvarsize  bss 2       ; size of important video variables

_CPURAMSIZE  equ $ - _CPURAMSTART

_SharedRAMEnd	equ	$		; checked later for conflict with _WPs

	; ---------		workspaces: +>60 -> >fffc, with room for NMI

_WPs		equ		>FF9C
StdWS		equ		_WPs + >00		; for FORTH
RomWS		equ		_WPs + >20		; for XOPs
IntWS		equ		_WPs + >20		; for interrupts (mutex with XOPs)
VidWS		equ		_WPs + >40		; for video operations



;	XOPs

XOP_VIDEO	equ		0			; FORTH: VIDEO^
v_init				equ		0	; FORTH: #v.init		( )
v_setmode			equ		1	; FORTH: #v.mode		( mode-number )
v_reset				equ		2	; FORTH: #v.reset		( )

XOP_TERM	equ		1			; FORTH: TERM^
t_init				equ		0	; FORTH: #t.init		( )
t_ttychar			equ		1	; FORTH: #t.tty			( char )
t_drawchar			equ		2	; FORTH: #t.draw		( char )
t_readkey			equ		3	; FORTH: #t.rdkey		( char )
t_keyavl			equ		4	; FORTH: #t.key?		( char )

XOP_SOUND	equ		2			; FORTH: SOUND^
s_init				equ		0	; FORTH: #s.init		( )
s_queue				equ		1	; FORTH: #s.queue		( track )
