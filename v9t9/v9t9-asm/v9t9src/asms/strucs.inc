; **************************************************
; STRUCS.INC include file for structures and equates
; **************************************************
; by Edward Swartz  1/9/1995
; **************************************************


;
;	My opcodes
;

BREAKOP			equ	0e00h
SCREENOP		equ	0d60h
KEYSLOWOP		equ	0d40h
INT1OP			equ	0d00h
IRETOP			equ	0ce0h
TRANSOP			equ	0cc0h
SPRITEOP		equ	0ca0h
KEYOP			equ	0c80h
DSROP			equ	0c00h
TIDSROP			equ	0d20h

st_l	equ	80h
st_a	equ	40h
st_e	equ	20h
st_c	equ	10h
st_o	equ	08h
st_p	equ	04h
st_x	equ	02h

M_INT1	equ	1
M_INT2	equ	2
M_INT3	equ	4

maxvdp			equ	3fffh

DEBUGGING = 1		; means DEBUGGER (alt+sysrq) is compiled in
			; don't turn off -- there will be errors

;
;	Masks in STATEFLAG.
;


speechon		equ	8000h		; speech synthesis active
intdebug		equ	4000h
demoing			equ	2000h		; only in TIEMULD
romming			equ	1000h
sixtieth		equ	0800h
speechinterrupt		equ	0400h
delaying		equ	0200h		; are we delaying?
;;happymessage		equ	0100h
videointoccured     	equ	0080h
specialfunctionrequest	equ	0040h
interruptoccuring	equ	0020h
debugrequest		equ	0010h
paused			equ	0008h
reboot			equ	0004h
titick			equ	0002h
ctrlbreakpressed	equ	0001h

	IFDEF	DEMO
checkable	equ	ctrlbreakpressed+reboot+paused+debugrequest+ \
			interruptoccuring+specialfunctionrequest+ \
			videointoccured+delaying+sixtieth+titick
	ELSE
checkable	equ	ctrlbreakpressed+reboot+paused+debugrequest+ \
			interruptoccuring+specialfunctionrequest+    \  
			videointoccured+delaying+sixtieth
	ENDIF

maxspeechdelay		equ	90

;
;	Masks in VDP status register
;

vdpinterrupt		equ	80h
fivespritesonalineflag	equ	40h
coincidenceflag		equ	20h


;
;	Scancodes for several keys.
;

k_f1	equ	59
k_f2	equ	60
k_f3	equ	61
k_f4	equ	62
k_f5	equ	63
k_f6	equ	64
k_f7	equ	65
k_f8	equ	66
k_f9	equ	67
k_f10	equ	68
k_f11	equ	87
k_f12	equ	88
k_esc	equ	1
k_equals equ	13
k_enter	equ	28

;
;	Shift masks in SHIFT.
;

s_ctrl		equ	4
s_alt		equ	8
s_shift   	equ	128

s_leftshift	equ	2
s_rightshift	equ	1

;
;	Masks for locking keys in SHIFT.
;

s_capslock	equ	64
s_numlock	equ	32
s_scrolllock	equ	16


;
;	Joystick values structure.
;

joyvals		struc
		xx	dw	0
		yy	dw	0
		ends


;	Masks for SOUNDCARD and PLAYSOUND
;

pcspeakermask	equ	1
adlibmask	equ	2
sblastermask	equ	4
sbdmamask	equ	8

sbpromask	equ	16

;	FAILREASON values
;

fr_memory	equ	1		; not enough memory
fr_ctvoice	equ	2		; couldn't find CTVOICE.DRV

fr_cterr	equ	2
fr_card		equ	3		; CTVOICE.DRV said bad card
fr_port		equ	4		; CTVOICE.DRV said bad port
fr_dma		equ	5		; DMA/interrupt error


ctvoicedrvcall	macro
	call	dword ptr ctvoicedrv
	endm


;
;	MODULES type
;

mod_rom		equ	1		; CPU ROM segment
mod_rom2	equ	2		; banked extra segment
mod_minimem	equ	4		; Mini Memory RAM 7000-7fff
mod_banked	equ	2		; used only for testing bankedness
mod_grom	equ	8		; GROM segment


modrec	struc
	mtitle	db	32 dup (?)
	basename db	7 dup (?)
	memtype	db	?
	ends


spchstruc	struc
	sinit	dw	?
	sbyte	dw	?
	sshut	dw	?
	sone	dw	?
	ends


voiceinfo 	struc
	period	dw	0		; as written to >8400
	hertz	dw	0		; 111860.8/period
	volume	db	15		; 0=loudest, 15=off
	octave	db	0		; octave byte and key-on for voice
	stype	db	0		; for noise only: 0-3=periodic, 4-7=white
	ends


;
;	Masks in DSRS
;

dsr_realdisk	equ	1
dsr_emudisk	equ	2
dsr_rs232	equ	4

;
;	RS232 structure
;

SERBUFMAX equ	1024				; maximum buffer size


rsstruc	struc
	port	dw	0			; PC base port
	smap	db	0			; maps to BIOS port xxx...
	irq	db	0			; PC IRQ (rel)

	char	db	0			; char read
	lns	db	0			; line status
	mdm	db	0			; modem status

	bits	db	8			; 5-8 databits
	stop	db	0			; stop -- 0=1, 1=2
	parity	db	0			; parity, 00-11
	clk4m	db	3			; clk4m value (3 or 4)

	rdiv8	db	1			; divide by 8 flag for read
	rbaud	dw	0			; read baud value
	rpcbaud	dw	0			; PC value

	sdiv8	db	1			; divide by 8 flag for write
	sbaud	dw	0			; write baud value
	spcbaud	dw	0			; PC value

	timer	db	0			; interval timer
	lflags	db	0			; LXDR, LRDR, LDIR, LDCTRL
	flagset	db	0			; LOAD CTRL flag just set

	intgoing db	0			; are interrupts going?
	intflag	db	0			; which interrupts tripped

	bufst	dw	0			; buffer start
	bufen	dw	0			; buffer end
	buffr	db	SERBUFMAX dup (?)  	; char/LNSTAT buffer
	ends

piostruc struc
	pport	dw	0			; parallel port addr
	pmap	db	0			; maps to BIOS port x
	strobe	db	0
	ends


s_readbyte	equ	2
s_speakext	equ	1
s_nothing	equ	0



;	PATCHES values

PT_keyboard	equ	1		; fast keyboard
PT_reboot	equ	2		; let shift+fctn+'=' be reboot
PT_sprites	equ	4		; do sprite motion
PT_transfer	equ	8		; block memory move
PT_int		equ	16		; int 1 handler
PT_kbdelay	equ	32		; slow down keyboard response
PT_screenfill	equ	64		; fill screen 


;	LOGGING values

LG_tiemul	equ	1
LG_video	equ	2
LG_special	equ	4
LG_int		equ	8
LG_hardware	equ	16
LG_files	equ	32
LG_sound	equ	64
LG_keyboard	equ	128
LG_debug	equ	256
LG_speech	equ	512
LG_support	equ	1024
LG_record	equ	2048

LG_rs232	equ	4096
LG_pio		equ	8192
LG_disk		equ	16384


LOGGINQ	macro	what,where
	test	islogging,what
	jz	where
	endm

LOG0	macro	which,name
	local	not
	LOGGINQ	which,not
	push	offset name
	call	logout
	add	sp,2
not:
	endm

LOG1	macro	which,name,arg0
	local	not
	LOGGINQ	which,not
	push	arg0
	push	offset name
	call	logout
	add	sp,4
not:
	endm

LOG2	macro	which,name,arg0,arg1
	local	not
	LOGGINQ	which,not
	push	arg1
	push	arg0
	push	offset name
	call	logout
	add	sp,6
not:
	endm


LOG3	macro	which,name,arg0,arg1,arg2
	local	not
	LOGGINQ	which,not
	push	arg2
	push	arg1
	push	arg0
	push	offset name
	call	logout
	add	sp,8
not:
	endm


LOG4	macro	which,name,arg0,arg1,arg2,arg3
	local	not
	LOGGINQ	which,not
	push	arg3
	push	arg2
	push	arg1
	push	arg0
	push	offset name
	call	logout
	add	sp,10
not:
	endm


;	FEATURES


FE_realdisk 	equ	1
FE_emudisk	equ	2
FE_realrs232	equ	4
FE_emurs232	equ	8
FE_LPCspeech	equ	16
FE_digispeech	equ	32		; not yet
FE_speechROM	equ	64		; ROM loaded?
FE_emulating	equ	128		; emulating or demoing?
FE_demo		equ	256		; we have memory for demo?

FE_speech	equ	16		; general mask for speech


;	DMA structure for sound samples
;
;
DMAstruc struc
	sampleoffs	dw	0	; beginning addr of sample
	sampleseg	dw	0
	sampleleng	dw	0
	stopper		dw	0	; what function to call at termination
	sampleplayed	dw	0	; how many bytes have/are being
					; played?
	samplespeed    	db	0	; 256-(1000000/speedhz)
	samplevol	db	0	; volume (mixer-style, 0ffh loudest)
	ends



LPC_in_direct	equ 	1	; direct data coming in
	;	Set, used, by handlespeechwrite
	;	intspeech resets LPC_in_direct when done.

LPC_out_memory	equ	2	; vocab memory coming out
	;	Controlled by handlespeechwrite, handlespeechread.
	;

LPC_from_vocab	equ	4	; data for LPC comes from vocabulary ROM
	;	set by handlespeechwrite.

LPC_have_speaker equ	8	; we have control of the sound device
	;	set by LPCinit, reset by LPCterminate, LPCover

LPC_audioing	equ	16	; speech sample is going to audio
	;	set by LPCoutspeech, reset by speechsoundended

LPC_stuffed	equ	32	; when LPC_audioing set, means we have
				; another buffer waiting to be spoken
	;	set by LPCoutspeech

LPC_calculating	equ	64	; we are in LPCConstruct already

LPC_time_passed equ	128	; set by interrupt60 after a timeout
LPC_time_flag	equ	256	; set by LPCpushbyte each time a byte pushed
LPC_timing	equ	512	; we're counting down now

