;   rom_equs.inc
; 
;   (c) 2014 Ed Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 

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
SPCHDR 		equ		MMIO + >1c

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

_SharedRAM  equ    >FE80
 
 			aorg	_SharedRAM
 			
; this block of variables is visible via equates to FORTH 

vintflags   bss 1       ; VDP interrupt flags

nvblnk      equ >80     ;       1: suppress blink cursor and blank screen ; FORTH: v-int-nocursor
nvkeyscan   equ >40     ;       1: suppress scan keyboard  ; FORTH: v-int-nokbd
nvabort     equ >20     ;       1: suppress Ctrl-Fctn-Shift-Space ABORT support ; FORTH: v-int-noabort
nvsprmot    equ >20     ;       1: suppress sprite motion ; FORTH: v-int-nosprmot
nsoundlist  equ >10     ;       1: suppress sound list and/or synth; FORTH: v-int-nosound
vhorizint	equ >08     ;       1: detect and honor horizontal scanning interrupt (FH via IE1)

vstatus     bss 1       ; VDP status last read during interrupt

userint     bss 2       ; user VDP interrupt handler (called when VDP interrupts)
timerisr    bss 2       ; user timer handler (called when clock ticks)

nsprmot     bss 1       ; number of sprites in motion (00 = none)

sndflags    bss 1       ; sound list flags
                        ;       $00: CPU RAM, $80: VDP 
                        ;       $0f: tempo adjust (signed: -4 to 3) 
sndlist     bss 2       ; address of classic sound list (0 = none), incremented on tick
snddur      bss 1       ; duration of current group

vinthoriz   bss 1	  	; set when in horizontal scanning interrupt (lower part of screen)

        even

uptime		bss	4		; time in 1/60 seconds
timeout		bss	2		; timeout counter

;--------------------------------------

_VIDVARSTART equ $

vscreen	bss	2			; VDP addr of screen
vscreensz	bss	2			; VDP size of screen table
vpatts	bss	2			; VDP addr of patterns
vpattsz	bss	2			; VDP size of pattern table
vcolors bss	2			; VDP addr of colors
vcolorsz bss	2			; VDP size of color table
vsprites bss	2			; VDP addr of sprites
vsprcol bss 2           ; VDP addr of sprite color table (0 if not sprite 2 mode)
vsprpat bss	2			; VDP addr of sprite patterns
vsprmot	bss	2			; VDP addr of sprite motion
vfree	bss	2			; usable space

vdrawchar bss	2			; draw char in window (BLWP @)
vscroll	bss	2			; scroll window up a line (BLWP @)
vclearline	bss	2			; clear line (BL @)  [R0=window coord, R2=length; preserve 0, 3]

vbsize	bss	2			; bitmapped font size (x/y)

vcoordaddr bss	2			; get SIT addr of R0 coord   [R0=window coord => R0=addr, R1=shift; 
                            ;                               preserve R2]

vcrstimer bss	1			; timer for blink
vcrsblink bss	1			; limit in 1/60 s
vcursor	bss	2			; cursor blinker (BLWP @)
vcurs	bss	1			; cursor blink state (0 or >80)

vcursunder bss	8	   	; char or bits under cursor

vheight bss 1           ; phys screen height in chars
vwidth  bss 2           ; phys screen width in chars
    even
    
vbit4stride bss 2        ; row stride  
vbit4shift bss 2        ; shift for column # to byte
vbit4mask bss 2        ;  mask for column # to byte portion

vfont	bss	2			; GROM font addr

;---------------------------------------

vfgbg	bss	2			; foreground|background color
vch	bss	1			; current char

vcurschar bss	1			; char of cursor

vwx		bss	1			; window left
vwy		bss	1			; window right
vwxs	bss	1			; width of window
vwys	bss	1			; height of window
vwcy	bss	1			; last cleared row

vmode	bss	1         ; last set video mode (not M_xxx)

    even
    
vtermptr bss 2          ; pointer to standard term stuff for mode
    
vx		bss	1			; x-coord of cursor in window
vy		bss	1			; y-coord of cursor in window

vmono	bss	1
vidmode	bss	1			; what mode are we in?  (M_xxxx)

M_text	equ	0
M_graph	equ	1
M_multi equ 2
M_bit	equ	3			; both mono and color
M_bit4	equ	4			; new bitmap modes
M_text2 equ 5           ; 80-column

vscrnx  bss 2           ; res x
vscrny  bss 2           ; res y

vlinex	bss	2
vliney	bss	2

savedvregs bss 16       ; first 16 VRs set via vwreg
vregr1  equ savedvregs + 1           ; VDP register 1
vregr7  equ savedvregs + 7           ; VDP register 7

_VIDVARSIZE  equ $ - _VIDVARSTART

;--------------------------------------

DP			bss	2		; current dictionary
GDP			bss	2		; current GROM dictionary

tasks		bss	2		; pointer to task list; FORTH: 'tasks
me			bss	2		; current task; FORTH: 'me
ticked		bss	2		; last switch time; FORTH: 'ticked

;--------------------------------------
        
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
v_blank				equ		3	; FORTH: #v.blank		( t|f )
v_setcolor			equ		4	; FORTH: #v.fgbg!		( fg|bg )
v_line				equ		5	; FORTH: #v.line		( x y x2 y2 op|color )
v_circle			equ		6	; FORTH: #v.circle		( x y r op|color )

XOP_TERM	equ		1			; FORTH: TERM^
t_init				equ		0	; FORTH: #t.init		( )
t_ttychar			equ		1	; FORTH: #t.tty			( char )
t_drawchar			equ		2	; FORTH: #t.draw		( char )	( w/o interpret )
t_ttystr			equ		3	; FORTH: #t.ttystr		( caddr len )
t_clear				equ		4	; FORTH: #t.clear		(  )
t_readkey			equ		5	; FORTH: #t.rdkey		( char )
t_keyavl			equ		6	; FORTH: #t.key?		( char )
t_getwin			equ		7	; FORTH: #t.win@		( x y w h )

XOP_SOUND	equ		2			; FORTH: SOUND^
s_init				equ		0	; FORTH: #s.init		( )
s_queue				equ		1	; FORTH: #s.queue		( track )

XOP_UTIL	equ		3			; FORTH: UTIL^
u_init				equ		0	; FORTH: #u.init		( )
u_seed				equ		1	; FORTH: #u.seed		( seed -- )
u_rand1				equ		2	; FORTH: #u.rand1		( -- rnd )
u_rand2				equ		3	; FORTH: #u.rand2		( -- rnd )
u_noise				equ		4	; FORTH: #u.noise		( rnd -- rnd' )

