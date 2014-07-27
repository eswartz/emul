;
;	Equates that f9900rom.f99 needs.
;
;	These are in assembly syntax.  A post-assembly build step (EquatesToConstants)
;	converts them into f9900romequs.fi for use by the following 
;	FORTH ROM build step.
;

;	------------------------------------------ ROM/MMIO/hardware layout

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

INTS		equ		CRU + >00
INTSP		equ		CRU + >01


;	Shared ROM start

SHARED		equ		>0100

;	------------------------------------------ RAM layout

_RAM		equ		>FB00

	; definitions in ram.inc, not here (to avoid FORTH exposure)

;   Special "system variables" that the FORTH can modify

_Shared    equ    >FF00
 
 			aorg	_Shared
 			
; this block of variables is visible via equates to FORTH 

vintflags   bss 1       ; VDP interrupt flags

nosprmot 	equ		>80		; 
nocursor 	equ 	>40		
noabort 	equ 	>20		
nvblnk      equ >80     ;       1: suppress blink cursor and blank screen ; FORTH: v-int-nocursor
nvkeyscan   equ >40     ;       1: suppress scan keyboard  ; FORTH: v-int-nokbd
nvabort     equ >20     ;       1: suppress Ctrl-Fctn-Shift-Space ABORT support ; FORTH: v-int-noabort
nvsprmot    equ >20     ;       1: suppress sprite motion ; FORTH: v-int-nosprmot
nsoundlist  equ >10     ;       1: suppress sound list ; FORTH: v-int-nosound

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
        
; this block of variables is included in a video mode save/restore 
_CPURAMSTART equ $

vpob        bss 1       ; VDP "page offset" bank (added to V9938 bank to select the page outside 64k)
vblinkflag  bss 1       ; flag to set blink bit in text2 
vpgrow      bss 2       ; VDP "page row offset" (added to V9938 commands to select the page)
vtextpage   bss 2       ; VDP text-ish page offset (screen, patterns, colors) (in addition to vpob)

vidvarstart bss 2       ; start addr of important video variables
vidvarsize  bss 2       ; size of important video variables

_CPURAMSIZE  equ $ - _CPURAMSTART

_SharedEnd	equ	$		; checked later for conflict with _WPs

	; ---------		workspaces: +>60 -> >fffc, with room for NMI

_WPs		equ		>FF9C
StdWS		equ		_WPs + >00
RomWS		equ		_WPs + >20
IntWS		equ		_WPs + >40
VidWS		equ		_WPs + >40		; overlaps: ints turned off



;	XOPs

XOP_VIDEO	equ		0			; FORTH: VIDEO^
v_setmode			equ		0	; FORTH: #v.mode		( mode-number )
v_reset				equ		1	; FORTH: #v.reset		( )

XOP_TERM	equ		1			; FORTH: TERM^
t_ttychar			equ		0	; FORTH: #t.tty			( char )
t_drawchar			equ		0	; FORTH: #t.draw		( char )

